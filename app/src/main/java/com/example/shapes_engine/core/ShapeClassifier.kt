package shapes_engine.core

import shapes_engine.model.BoundingBox
import shapes_engine.model.Point2D
import shapes_engine.model.ShapeType
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min

/**
 * Result of geometric stroke classification.
 */
data class ShapeClassificationResult(
    val shapeType: ShapeType,
    val confidence: Float,
    val isClosed: Boolean,
    val resampledPoints: List<Point2D>,
    val detectedCorners: List<Point2D> = emptyList(),
    val boundingBox: BoundingBox,
    val compactness: Float = 0f,
    val covarianceAspectRatio: Float = 1f,
    val edgeWobble: Float = 0f,
    val reflexVertexCount: Int = 0
)

/**
 * Pure Kotlin Shape Classifier adhering to Explorer 2 mathematical formulations.
 * Detects: LINE, ARROW, RECTANGLE, SQUARE, CIRCLE, ELLIPSE, TRIANGLE, PENTAGON, HEXAGON, STAR, HEART.
 * Zero external math/ML library dependencies.
 */
class ShapeClassifier {

    /**
     * Classifies a sequence of freehand drawing points into a [ShapeClassificationResult].
     */
    fun classify(rawPoints: List<Point2D>): ShapeClassificationResult {
        if (rawPoints.size < 2) {
            val bbox = BoundingBox.fromPoints(rawPoints)
            return ShapeClassificationResult(
                shapeType = ShapeType.LINE,
                confidence = 0f,
                isClosed = false,
                resampledPoints = rawPoints,
                boundingBox = bbox
            )
        }

        val resampled = StrokeResampler.resample(rawPoints, StrokeResampler.DEFAULT_SAMPLE_COUNT)
        val bbox = BoundingBox.fromPoints(resampled)
        val diag = max(bbox.diagonal, 1f)

        // 1. Open vs Closed determination (trim overshoot first so overshot circles close cleanly)
        val startPt = resampled.first()
        val endPt = resampled.last()
        val endpointGap = startPt.distanceTo(endPt)
        val closureRatio = endpointGap / diag

        val trimmed = StrokeResampler.trimClosureOvershoot(resampled, diag)
        val trimmedGap = trimmed.first().distanceTo(trimmed.last())
        val trimmedClosureRatio = trimmedGap / diag
        val totalArcLen = StrokeResampler.computeTotalArcLength(resampled)

        val isClosed =
            closureRatio < 0.34f ||
                trimmedClosureRatio < 0.36f ||
                (totalArcLen >= 2.15f * diag && trimmedClosureRatio < 0.46f)

        return if (!isClosed) {
            classifyOpenStroke(resampled, bbox, diag)
        } else {
            val trimmedBbox = BoundingBox.fromPoints(trimmed)
            classifyClosedStroke(trimmed, trimmedBbox, max(trimmedBbox.diagonal, 1f))
        }
    }

    private fun classifyOpenStroke(
        points: List<Point2D>,
        bbox: BoundingBox,
        diag: Float
    ): ShapeClassificationResult {
        val startPt = points.first()
        val endPt = points.last()
        val chordDist = startPt.distanceTo(endPt)
        val totalArcLen = StrokeResampler.computeTotalArcLength(points)
        val straightness = if (totalArcLen > 1e-4f) chordDist / totalArcLen else 1f

        // 1. Check Arrow
        // An arrow stroke has a long shaft followed by a sharp reverse turn for the barb
        val arrowEps = 0.04f * diag
        val simplified = PolygonSimplifier.simplify(points, arrowEps, isClosed = false)

        val isArrow = checkArrowStroke(points, simplified, totalArcLen, diag)
        if (isArrow) {
            return ShapeClassificationResult(
                shapeType = ShapeType.ARROW,
                confidence = 0.95f,
                isClosed = false,
                resampledPoints = points,
                detectedCorners = simplified,
                boundingBox = bbox
            )
        }

        // 2. Check Straight Line first if very straight
        var maxDev = 0f
        var apexIdx = 0
        for (i in points.indices) {
            val d = PolygonSimplifier.perpendicularDistanceToSegment(points[i], startPt, endPt)
            if (d > maxDev) {
                maxDev = d
                apexIdx = i
            }
        }
        val maxDevRatio = maxDev / diag

        val isStraightLine = (straightness >= 0.91f && maxDevRatio <= 0.075f) || (straightness >= 0.95f)
        if (isStraightLine) {
            val confidence = (straightness * (1f - maxDevRatio * 3f)).coerceIn(0.6f, 1f)
            return ShapeClassificationResult(
                shapeType = ShapeType.LINE,
                confidence = confidence,
                isClosed = false,
                resampledPoints = points,
                detectedCorners = listOf(startPt, endPt),
                boundingBox = bbox
            )
        }

        // 3. Check Angle Bracket / Chevron (>, <, ^, V, L)
        val angleCorners = checkAngleBracketStroke(points, apexIdx, maxDevRatio, totalArcLen, diag)
        if (angleCorners != null) {
            return ShapeClassificationResult(
                shapeType = ShapeType.ANGLE_BRACKET,
                confidence = 0.94f,
                isClosed = false,
                resampledPoints = points,
                detectedCorners = angleCorners,
                boundingBox = bbox
            )
        }

        // 4. Smooth Open Arc Curve
        if (maxDevRatio > 0.08f) {
            val midPt = points[apexIdx.coerceIn(1, (points.size - 2).coerceAtLeast(1))]
            return ShapeClassificationResult(
                shapeType = ShapeType.CURVE,
                confidence = 0.89f,
                isClosed = false,
                resampledPoints = points,
                detectedCorners = listOf(startPt, midPt, endPt),
                boundingBox = bbox
            )
        }

        return ShapeClassificationResult(
            shapeType = ShapeType.LINE,
            confidence = 0.75f,
            isClosed = false,
            resampledPoints = points,
            detectedCorners = listOf(startPt, endPt),
            boundingBox = bbox
        )
    }

    /**
     * Detects a 2-segment sharp open angle bracket / chevron (`>`, `<`, `^`, `V`, `L`).
     * Returns the 3 vertices `[start, apex, end]` when both legs are straight and meet at a clear corner.
     */
    private fun checkAngleBracketStroke(
        points: List<Point2D>,
        apexIdx: Int,
        maxDevRatio: Float,
        totalArcLen: Float,
        diag: Float
    ): List<Point2D>? {
        if (points.size < 5 || maxDevRatio < 0.11f) return null
        if (apexIdx < 2 || apexIdx > points.size - 3) return null

        val startPt = points.first()
        val apexPt = points[apexIdx]
        val endPt = points.last()

        val leg1Len = startPt.distanceTo(apexPt)
        val leg2Len = apexPt.distanceTo(endPt)
        if (leg1Len < 0.18f * totalArcLen || leg2Len < 0.18f * totalArcLen) return null

        // Compute interior angle between the two legs at apexPt
        val v1 = (startPt - apexPt).normalized()
        val v2 = (endPt - apexPt).normalized()
        val dot = v1.dot(v2).coerceIn(-1f, 1f)
        val interiorAngleDeg = Math.toDegrees(acos(dot).toDouble()).toFloat()
        if (interiorAngleDeg !in 20f..152f) return null

        // Measure straightness of Leg 1 (start -> apex) and Leg 2 (apex -> end)
        var maxLeg1Dev = 0f
        for (i in 0..apexIdx) {
            val d = PolygonSimplifier.perpendicularDistanceToSegment(points[i], startPt, apexPt)
            if (d > maxLeg1Dev) maxLeg1Dev = d
        }
        var maxLeg2Dev = 0f
        for (i in apexIdx until points.size) {
            val d = PolygonSimplifier.perpendicularDistanceToSegment(points[i], apexPt, endPt)
            if (d > maxLeg2Dev) maxLeg2Dev = d
        }

        val leg1Wobble = maxLeg1Dev / max(leg1Len, 1f)
        val leg2Wobble = maxLeg2Dev / max(leg2Len, 1f)

        // For an angle bracket (>, <, ^, V), both legs are relatively straight compared to the sharp corner
        if (leg1Wobble <= 0.14f && leg2Wobble <= 0.14f && (maxLeg1Dev + maxLeg2Dev) * 0.5f < maxDevRatio * diag * 0.48f) {
            return listOf(startPt, apexPt, endPt)
        }
        return null
    }

    private fun checkArrowStroke(
        resampledPoints: List<Point2D>,
        simplified: List<Point2D>,
        totalArcLen: Float,
        diag: Float
    ): Boolean {
        if (simplified.size < 3) return false

        // Case A: 3 to 5 simplified vertices
        // Shaft is typically from vertex 0 to vertex 1 or n-2 to n-1
        // Look for the longest segment as candidate shaft
        var longestSegLen = -1f
        var longestSegIdx = -1
        for (i in 0 until simplified.size - 1) {
            val len = simplified[i].distanceTo(simplified[i + 1])
            if (len > longestSegLen) {
                longestSegLen = len
                longestSegIdx = i
            }
        }

        if (longestSegIdx == -1 || longestSegLen < 0.45f * totalArcLen) return false

        // Check if there is a sharp turn at the tip of the longest segment
        val tipIdx = if (longestSegIdx == 0) 1 else longestSegIdx
        if (tipIdx > 0 && tipIdx < simplified.size - 1) {
            val vIn = (simplified[tipIdx] - simplified[tipIdx - 1]).normalized()
            val vOut = (simplified[tipIdx + 1] - simplified[tipIdx]).normalized()
            val dot = vIn.dot(vOut).coerceIn(-1f, 1f)
            val turnAngleDeg = Math.toDegrees(acos(dot).toDouble()).toFloat()

            val barbLen = simplified[tipIdx].distanceTo(simplified[tipIdx + 1])
            val barbRatio = barbLen / longestSegLen

            if (turnAngleDeg >= 95f && barbRatio in 0.05f..0.55f) {
                return true
            }
        }

        // Also check if tip is at end of stroke and user drew wings backwards
        if (simplified.size >= 4) {
            val vIn = (simplified[simplified.size - 2] - simplified[simplified.size - 3]).normalized()
            val vOut = (simplified.last() - simplified[simplified.size - 2]).normalized()
            val dot = vIn.dot(vOut).coerceIn(-1f, 1f)
            val turnAngleDeg = Math.toDegrees(acos(dot).toDouble()).toFloat()
            if (turnAngleDeg >= 90f) {
                return true
            }
        }

        return false
    }

    private fun classifyClosedStroke(
        points: List<Point2D>,
        bbox: BoundingBox,
        diag: Float
    ): ShapeClassificationResult {
        val compactness = CurvatureAnalyzer.computeCompactness(points)
        val centroid = CurvatureAnalyzer.computeCentroid(points)
        val (radiusMean, radiusStdDev) = CurvatureAnalyzer.computeRadiusStats(points, centroid)
        val radiusVariationRatio = if (radiusMean > 1e-4f) radiusStdDev / radiusMean else 0f

        val covResult = CurvatureAnalyzer.computeCovariance(points)
        val covAspectRatio = covResult.aspectRatio

        // 1. Test for HEART
        val heartResult = CurvatureAnalyzer.analyzeHeartLandmarks(points, bbox)
        if (heartResult.isCandidate && heartResult.symmetryScore >= 0.65f) {
            return ShapeClassificationResult(
                shapeType = ShapeType.HEART,
                confidence = (0.75f + heartResult.symmetryScore * 0.25f).coerceIn(0f, 1f),
                isClosed = true,
                resampledPoints = points,
                detectedCorners = listOf(heartResult.cuspPoint, heartResult.apexPoint),
                boundingBox = bbox,
                compactness = compactness,
                covarianceAspectRatio = covAspectRatio,
                reflexVertexCount = heartResult.reflexCount
            )
        }

        // 2. Test for STAR
        // Star has low compactness (0.12 - 0.45), high radius variation (>= 0.18), and multiple reflex vertices
        val starEps = 0.035f * diag
        val starSimplified = PolygonSimplifier.simplify(points, starEps, isClosed = true)
        val starReflexIndices = CurvatureAnalyzer.findReflexVertices(starSimplified)

        val isStar = (compactness in 0.10f..0.45f && radiusVariationRatio >= 0.18f) ||
                (starReflexIndices.size >= 3 && compactness < 0.50f)

        if (isStar) {
            return ShapeClassificationResult(
                shapeType = ShapeType.STAR,
                confidence = 0.92f,
                isClosed = true,
                resampledPoints = points,
                detectedCorners = starSimplified,
                boundingBox = bbox,
                compactness = compactness,
                covarianceAspectRatio = covAspectRatio,
                reflexVertexCount = starReflexIndices.size
            )
        }

        // 3. Early Smooth Circle / Ellipse check before polygon simplification can quantize a circle
        val polyEps = 0.045f * diag
        val rawCorners = PolygonSimplifier.simplify(points, polyEps, isClosed = true)
        val filteredCorners = PolygonSimplifier.filterCollinearVertices(rawCorners, minTurnAngleDeg = 16f, isClosed = true)
        val mergedCorners = PolygonSimplifier.mergeCloseVertices(filteredCorners, minDistancePx = 0.12f * diag, isClosed = true)

        val uniqueCornerCount = if (mergedCorners.size >= 2 && mergedCorners.first().distanceTo(mergedCorners.last()) < 1e-2f) {
            mergedCorners.size - 1
        } else {
            mergedCorners.size
        }

        val edgeWobble = CurvatureAnalyzer.computeEdgeWobble(points, mergedCorners)

        // Hand-drawn circles have low radial variation and high compactness, and curved edges (higher wobble vs chords)
        val isSmoothCircularLoop =
            (radiusVariationRatio <= 0.165f && compactness >= 0.76f && (edgeWobble > 0.044f || uniqueCornerCount >= 5)) ||
                (radiusVariationRatio <= 0.125f && compactness >= 0.80f)

        if (isSmoothCircularLoop) {
            val circleOrOval = if (covAspectRatio <= 1.28f && bbox.squareAspectRatio >= 0.74f) {
                ShapeType.CIRCLE
            } else {
                ShapeType.ELLIPSE
            }
            return ShapeClassificationResult(
                shapeType = circleOrOval,
                confidence = 0.96f,
                isClosed = true,
                resampledPoints = points,
                detectedCorners = emptyList(),
                boundingBox = bbox,
                compactness = compactness,
                covarianceAspectRatio = covAspectRatio,
                edgeWobble = edgeWobble
            )
        }

        // Check if vertices form straight polygon edges
        val isPolygonCandidate = (edgeWobble <= 0.072f && uniqueCornerCount in 3..6)

        if (isPolygonCandidate) {
            when (uniqueCornerCount) {
                3 -> {
                    return ShapeClassificationResult(
                        shapeType = ShapeType.TRIANGLE,
                        confidence = 0.95f,
                        isClosed = true,
                        resampledPoints = points,
                        detectedCorners = mergedCorners,
                        boundingBox = bbox,
                        compactness = compactness,
                        covarianceAspectRatio = covAspectRatio,
                        edgeWobble = edgeWobble
                    )
                }
                4 -> {
                    // Compute aspect ratio along polygon edges for rotation invariance
                    val side1 = if (mergedCorners.size >= 4) {
                        (mergedCorners[0].distanceTo(mergedCorners[1]) + mergedCorners[2].distanceTo(mergedCorners[3])) * 0.5f
                    } else bbox.width
                    val side2 = if (mergedCorners.size >= 4) {
                        (mergedCorners[1].distanceTo(mergedCorners[2]) + mergedCorners[3].distanceTo(mergedCorners[0])) * 0.5f
                    } else bbox.height

                    val edgeAspect = if (max(side1, side2) > 1e-4f) min(side1, side2) / max(side1, side2) else 1f
                    val shapeType = if (edgeAspect >= 0.88f) ShapeType.SQUARE else ShapeType.RECTANGLE
                    return ShapeClassificationResult(
                        shapeType = shapeType,
                        confidence = 0.95f,
                        isClosed = true,
                        resampledPoints = points,
                        detectedCorners = mergedCorners,
                        boundingBox = bbox,
                        compactness = compactness,
                        covarianceAspectRatio = covAspectRatio,
                        edgeWobble = edgeWobble
                    )
                }
                5 -> {
                    return ShapeClassificationResult(
                        shapeType = ShapeType.PENTAGON,
                        confidence = 0.92f,
                        isClosed = true,
                        resampledPoints = points,
                        detectedCorners = mergedCorners,
                        boundingBox = bbox,
                        compactness = compactness,
                        covarianceAspectRatio = covAspectRatio,
                        edgeWobble = edgeWobble
                    )
                }
                6 -> {
                    return ShapeClassificationResult(
                        shapeType = ShapeType.HEXAGON,
                        confidence = 0.92f,
                        isClosed = true,
                        resampledPoints = points,
                        detectedCorners = mergedCorners,
                        boundingBox = bbox,
                        compactness = compactness,
                        covarianceAspectRatio = covAspectRatio,
                        edgeWobble = edgeWobble
                    )
                }
            }
        }

        // Secondary fallback for polygons with higher noise or non-regular corners
        if (uniqueCornerCount == 3 && compactness < 0.72f) {
            return ShapeClassificationResult(
                shapeType = ShapeType.TRIANGLE,
                confidence = 0.88f,
                isClosed = true,
                resampledPoints = points,
                detectedCorners = mergedCorners,
                boundingBox = bbox,
                compactness = compactness,
                covarianceAspectRatio = covAspectRatio,
                edgeWobble = edgeWobble
            )
        }

        if (uniqueCornerCount == 4 && compactness in 0.56f..0.84f && edgeWobble <= 0.082f) {
            val shapeType = classifyFourCornerQuad(mergedCorners, bbox)
            return ShapeClassificationResult(
                shapeType = shapeType,
                confidence = 0.90f,
                isClosed = true,
                resampledPoints = points,
                detectedCorners = mergedCorners,
                boundingBox = bbox,
                compactness = compactness,
                covarianceAspectRatio = covAspectRatio,
                edgeWobble = edgeWobble
            )
        }

        if (uniqueCornerCount == 5 && edgeWobble <= 0.078f && compactness < 0.82f) {
            return ShapeClassificationResult(
                shapeType = ShapeType.PENTAGON,
                confidence = 0.88f,
                isClosed = true,
                resampledPoints = points,
                detectedCorners = mergedCorners,
                boundingBox = bbox,
                compactness = compactness,
                covarianceAspectRatio = covAspectRatio,
                edgeWobble = edgeWobble
            )
        }

        if (uniqueCornerCount == 6 && edgeWobble <= 0.078f && compactness < 0.83f) {
            return ShapeClassificationResult(
                shapeType = ShapeType.HEXAGON,
                confidence = 0.88f,
                isClosed = true,
                resampledPoints = points,
                detectedCorners = mergedCorners,
                boundingBox = bbox,
                compactness = compactness,
                covarianceAspectRatio = covAspectRatio,
                edgeWobble = edgeWobble
            )
        }

        // 4. Smooth Curves: Circle vs Ellipse
        // Aspect ratio threshold: aspect ratio <= 1.28 -> CIRCLE, else ELLIPSE
        val shapeType = if (covAspectRatio <= 1.28f && bbox.squareAspectRatio >= 0.74f) {
            ShapeType.CIRCLE
        } else {
            ShapeType.ELLIPSE
        }

        return ShapeClassificationResult(
            shapeType = shapeType,
            confidence = 0.95f,
            isClosed = true,
            resampledPoints = points,
            detectedCorners = emptyList(),
            boundingBox = bbox,
            compactness = compactness,
            covarianceAspectRatio = covAspectRatio,
            edgeWobble = edgeWobble
        )
    }

    /**
     * Distinguishes a rotated DIAMOND (vertices near top/right/bottom/left midpoints of bounding box)
     * from an axis-aligned SQUARE or RECTANGLE.
     */
    private fun classifyFourCornerQuad(corners: List<Point2D>, bbox: BoundingBox): ShapeType {
        if (corners.size < 4) return ShapeType.RECTANGLE
        val cx = bbox.center.x
        val cy = bbox.center.y
        val diamondTargets = listOf(
            Point2D(cx, bbox.minY),
            Point2D(bbox.maxX, cy),
            Point2D(cx, bbox.maxY),
            Point2D(bbox.minX, cy)
        )
        val rectTargets = listOf(
            Point2D(bbox.minX, bbox.minY),
            Point2D(bbox.maxX, bbox.minY),
            Point2D(bbox.maxX, bbox.maxY),
            Point2D(bbox.minX, bbox.maxY)
        )
        val v4 = corners.take(4)
        val diamondDist = diamondTargets.sumOf { target ->
            v4.minOf { c -> c.distanceTo(target).toDouble() }
        }.toFloat()
        val rectDist = rectTargets.sumOf { target ->
            v4.minOf { c -> c.distanceTo(target).toDouble() }
        }.toFloat()

        if (diamondDist < rectDist * 0.78f) {
            return ShapeType.DIAMOND
        }

        val side1 = (corners[0].distanceTo(corners[1]) + corners[2].distanceTo(corners[3])) * 0.5f
        val side2 = (corners[1].distanceTo(corners[2]) + corners[3].distanceTo(corners[0])) * 0.5f
        val edgeAspect = if (max(side1, side2) > 1e-4f) min(side1, side2) / max(side1, side2) else 1f
        return if (edgeAspect >= 0.88f) ShapeType.SQUARE else ShapeType.RECTANGLE
    }
}
