package shapes_engine.core

import shapes_engine.model.BoundingBox
import shapes_engine.model.Point2D
import kotlin.math.*

/**
 * Geometric feature extractor and curvature analyzer.
 * Calculates Shoelace area, compactness, reflex vertices, 2D covariance/eigenvalues,
 * edge wobble, and bilateral symmetry.
 * Pure Kotlin with zero external math or ML libraries.
 */
object CurvatureAnalyzer {

    data class CovarianceResult(
        val centroid: Point2D,
        val lambda1: Float,
        val lambda2: Float,
        val aspectRatio: Float,
        val orientationRad: Float
    )

    data class HeartAnalysisResult(
        val isCandidate: Boolean,
        val cuspIndex: Int,
        val apexIndex: Int,
        val cuspPoint: Point2D,
        val apexPoint: Point2D,
        val symmetryScore: Float,
        val reflexCount: Int
    )

    /**
     * Computes signed Shoelace area for a polygon or closed stroke.
     * Positive for counter-clockwise, negative for clockwise.
     */
    fun computeSignedArea(points: List<Point2D>): Float {
        if (points.size < 3) return 0f
        var sum = 0f
        val n = points.size
        for (i in 0 until n) {
            val curr = points[i]
            val next = points[(i + 1) % n]
            sum += (curr.x * next.y - next.x * curr.y)
        }
        return sum * 0.5f
    }

    /**
     * Computes polygon perimeter.
     */
    fun computePerimeter(points: List<Point2D>, isClosed: Boolean = true): Float {
        if (points.size < 2) return 0f
        var p = 0f
        for (i in 0 until points.size - 1) {
            p += points[i].distanceTo(points[i + 1])
        }
        if (isClosed && points.first().distanceTo(points.last()) > 1e-4f) {
            p += points.last().distanceTo(points.first())
        }
        return p
    }

    /**
     * Computes isoperimetric quotient (compactness): 4 * PI * |Area| / (Perimeter^2).
     * Value is 1.0 for a perfect circle, ~0.785 for a square, ~0.60 for a triangle,
     * ~0.20-0.40 for a star, and ~0.50-0.68 for a heart.
     */
    fun computeCompactness(points: List<Point2D>): Float {
        val area = abs(computeSignedArea(points))
        val perimeter = computePerimeter(points, isClosed = true)
        if (perimeter < 1e-4f) return 0f
        return (4f * PI.toFloat() * area) / (perimeter * perimeter)
    }

    /**
     * Computes geometric centroid of points.
     */
    fun computeCentroid(points: List<Point2D>): Point2D {
        if (points.isEmpty()) return Point2D.Zero
        var sumX = 0f
        var sumY = 0f
        for (p in points) {
            sumX += p.x
            sumY += p.y
        }
        return Point2D(sumX / points.size, sumY / points.size)
    }

    /**
     * Computes mean and standard deviation of radial distances from [centroid].
     */
    fun computeRadiusStats(points: List<Point2D>, centroid: Point2D): Pair<Float, Float> {
        if (points.isEmpty()) return Pair(0f, 0f)
        val distances = FloatArray(points.size) { i -> points[i].distanceTo(centroid) }
        val mean = distances.average().toFloat()
        var varianceSum = 0f
        for (d in distances) {
            val diff = d - mean
            varianceSum += diff * diff
        }
        val stdDev = sqrt(varianceSum / points.size)
        return Pair(mean, stdDev)
    }

    /**
     * Computes 2D covariance matrix, eigenvalues, principal aspect ratio, and orientation angle.
     */
    fun computeCovariance(points: List<Point2D>): CovarianceResult {
        if (points.isEmpty()) return CovarianceResult(Point2D.Zero, 0f, 0f, 1f, 0f)
        val centroid = computeCentroid(points)
        var sxx = 0.0
        var syy = 0.0
        var sxy = 0.0

        for (p in points) {
            val dx = (p.x - centroid.x).toDouble()
            val dy = (p.y - centroid.y).toDouble()
            sxx += dx * dx
            syy += dy * dy
            sxy += dx * dy
        }
        val n = points.size.toDouble()
        sxx /= n
        syy /= n
        sxy /= n

        // Eigenvalues of 2x2 covariance matrix
        val trace = sxx + syy
        val diff = sxx - syy
        val delta = sqrt(diff * diff + 4.0 * sxy * sxy)
        val lambda1 = ((trace + delta) * 0.5).toFloat().coerceAtLeast(1e-6f)
        val lambda2 = ((trace - delta) * 0.5).toFloat().coerceAtLeast(1e-6f)
        val aspectRatio = sqrt(lambda1 / lambda2)
        val orientationRad = (0.5 * atan2(2.0 * sxy, diff)).toFloat()

        return CovarianceResult(
            centroid = centroid,
            lambda1 = lambda1,
            lambda2 = lambda2,
            aspectRatio = aspectRatio,
            orientationRad = orientationRad
        )
    }

    /**
     * Identifies reflex (concave/inward-pointing) vertices in a polygon loop.
     * In a CCW polygon, a cross product < 0 indicates a reflex turn.
     * In a CW polygon, a cross product > 0 indicates a reflex turn.
     */
    fun findReflexVertices(vertices: List<Point2D>): List<Int> {
        val n = if (vertices.size > 2 && vertices.first().distanceTo(vertices.last()) < 1e-3f) {
            vertices.size - 1
        } else {
            vertices.size
        }
        if (n < 4) return emptyList()

        val signedArea = computeSignedArea(vertices.subList(0, n))
        val isCCW = signedArea > 0f
        val reflexIndices = ArrayList<Int>()

        for (i in 0 until n) {
            val prev = vertices[(i - 1 + n) % n]
            val curr = vertices[i]
            val next = vertices[(i + 1) % n]

            val cross = (curr.x - prev.x) * (next.y - curr.y) - (curr.y - prev.y) * (next.x - curr.x)
            val isReflex = if (isCCW) cross < -1e-4f else cross > 1e-4f
            if (isReflex) {
                reflexIndices.add(i)
            }
        }
        return reflexIndices
    }

    /**
     * Computes the mean edge wobble (chord deviation) of points between corner landmarks.
     * Low wobble (< 0.045) signifies straight polygon edges.
     */
    fun computeEdgeWobble(resampledPoints: List<Point2D>, corners: List<Point2D>): Float {
        if (corners.size < 3 || resampledPoints.size < corners.size) return 0f

        val cleanCorners = if (corners.first().distanceTo(corners.last()) < 1e-3f) {
            corners.subList(0, corners.size - 1)
        } else {
            corners
        }
        val m = cleanCorners.size
        var totalDeviation = 0f
        var totalChordLength = 0f

        for (i in 0 until m) {
            val c1 = cleanCorners[i]
            val c2 = cleanCorners[(i + 1) % m]
            val chordLen = c1.distanceTo(c2)
            if (chordLen < 1e-4f) continue

            // Find closest index in resampledPoints for c1 and c2
            val idx1 = resampledPoints.indices.minByOrNull { resampledPoints[it].distanceSquaredTo(c1) } ?: 0
            val idx2 = resampledPoints.indices.minByOrNull { resampledPoints[it].distanceSquaredTo(c2) } ?: 0

            var maxDev = 0f
            val count = resampledPoints.size
            var curr = idx1
            while (curr != idx2) {
                val dev = PolygonSimplifier.perpendicularDistanceToSegment(resampledPoints[curr], c1, c2)
                if (dev > maxDev) maxDev = dev
                curr = (curr + 1) % count
            }

            totalDeviation += maxDev
            totalChordLength += chordLen
        }

        return if (totalChordLength > 1e-4f) totalDeviation / cleanCorners.size / (totalChordLength / cleanCorners.size) else 0f
    }

    /**
     * Analyzes heart landmarks:
     * - Inward top cleft (cusp)
     * - Sharp bottom tip (apex)
     * - Bilateral symmetry across the vertical axis connecting cusp and apex
     */
    fun analyzeHeartLandmarks(points: List<Point2D>, bbox: BoundingBox): HeartAnalysisResult {
        if (points.size < 16) {
            return HeartAnalysisResult(false, -1, -1, Point2D.Zero, Point2D.Zero, 0f, 0)
        }

        // 1. Apex candidate: lowest point in screen coordinates (max y)
        var apexIdx = 0
        var maxY = points[0].y
        for (i in 1 until points.size) {
            if (points[i].y > maxY) {
                maxY = points[i].y
                apexIdx = i
            }
        }
        val apexPoint = points[apexIdx]

        // 2. Cusp candidate: top inward dip.
        // It must have y lower than the apex, but locally higher (lower in screen y) than its surroundings.
        // Screen coordinates: 0 is top, higher y is down.
        // In a heart, the two lobes reach minimum y (topmost on screen).
        // The cusp between them has a local maximum y compared to the adjacent lobe peaks!
        val minY = points.minOf { it.y }
        val ySpan = bbox.height
        if (ySpan < 10f) {
            return HeartAnalysisResult(false, -1, -1, Point2D.Zero, Point2D.Zero, 0f, 0)
        }

        var cuspIdx = -1
        var cuspScore = -Float.MAX_VALUE

        // Search for the cusp in the upper half of the shape
        val upperThresholdY = minY + ySpan * 0.55f
        val n = points.size

        val cleftThreshold = max(6f, ySpan * 0.07f)

        for (i in 0 until n) {
            val p = points[i]
            // Cusp must be near the vertical centerline and in the upper third/half of the shape
            if (p.y <= upperThresholdY && abs(p.x - bbox.center.x) <= bbox.width * 0.22f) {
                // Check if it is a local downward dip between higher lobe points (lower y)
                val w = (n * 0.10f).toInt().coerceIn(3, 10)
                val leftY = points[(i - w + n) % n].y
                val rightY = points[(i + w) % n].y

                // In a true heart, the cusp dips down significantly between the left and right lobes
                val dipDepth = min(p.y - leftY, p.y - rightY)
                if (dipDepth >= cleftThreshold) {
                    val score = dipDepth - abs(p.x - bbox.center.x) * 0.5f
                    if (score > cuspScore) {
                        cuspScore = score
                        cuspIdx = i
                    }
                }
            }
        }

        if (cuspIdx == -1) {
            return HeartAnalysisResult(false, -1, -1, Point2D.Zero, Point2D.Zero, 0f, 0)
        }

        val cuspPoint = points[cuspIdx]
        // Cusp must be substantially higher on screen than apex
        if (apexPoint.y - cuspPoint.y < ySpan * 0.35f) {
            return HeartAnalysisResult(false, -1, -1, Point2D.Zero, Point2D.Zero, 0f, 0)
        }

        // Apex must be near the bottom
        if (apexPoint.y < bbox.maxY - ySpan * 0.15f) {
            return HeartAnalysisResult(false, -1, -1, Point2D.Zero, Point2D.Zero, 0f, 0)
        }

        // 3. Bilateral Symmetry Test
        val symAxisX = (cuspPoint.x + apexPoint.x) * 0.5f
        var totalDist = 0f
        var sampleCount = 0

        for (p in points) {
            if (p.x < symAxisX) {
                val mirroredX = symAxisX + (symAxisX - p.x)
                val mirroredPoint = Point2D(mirroredX, p.y)

                // Find closest point on right side
                var minDist = Float.MAX_VALUE
                for (other in points) {
                    if (other.x >= symAxisX - 2f) {
                        val d = mirroredPoint.distanceTo(other)
                        if (d < minDist) minDist = d
                    }
                }
                totalDist += minDist
                sampleCount++
            }
        }

        val meanDev = if (sampleCount > 0) totalDist / sampleCount else bbox.width
        val normalizedDev = (meanDev / max(bbox.width, 1e-4f)).coerceIn(0f, 1f)
        val symmetryScore = 1f - normalizedDev

        val reflexIndices = findReflexVertices(points)

        // A heart candidate requires:
        // - High bilateral symmetry (>= 0.68)
        // - Significant cleft depth
        // - Bottom apex lower than cusp
        // - At least one reflex vertex (the cusp itself)
        val isCandidate = symmetryScore >= 0.68f &&
                (cuspPoint.y - minY >= cleftThreshold) &&
                (apexPoint.y > cuspPoint.y) &&
                reflexIndices.isNotEmpty()

        return HeartAnalysisResult(
            isCandidate = isCandidate,
            cuspIndex = cuspIdx,
            apexIndex = apexIdx,
            cuspPoint = cuspPoint,
            apexPoint = apexPoint,
            symmetryScore = symmetryScore,
            reflexCount = reflexIndices.size
        )
    }
}
