package shapes_engine.core

import shapes_engine.model.BoundingBox
import shapes_engine.model.Point2D
import shapes_engine.model.RecognizedShape
import shapes_engine.model.ShapeStyle
import shapes_engine.model.ShapeType
import kotlin.math.*

/**
 * Pure Kotlin Shape Fitter adhering to Explorer 2 mathematical formulations.
 * Generates clean, fitted, publication-quality geometry for all 9 shapes:
 * Line, Arrow, Rectangle, Square, Circle, Ellipse, Triangle, Pentagon, Hexagon, Star, Heart.
 * Zero external math/ML library dependencies.
 */
class ShapeFitter {

    /**
     * Fits clean mathematical geometry to the classified stroke points.
     */
    fun fit(
        classification: ShapeClassificationResult,
        rawPoints: List<Point2D>,
        style: ShapeStyle = ShapeStyle()
    ): RecognizedShape {
        return when (classification.shapeType) {
            ShapeType.LINE -> fitLine(classification, style)
            ShapeType.ARROW -> fitArrow(classification, style)
            ShapeType.RECTANGLE -> fitRectangle(classification, style, isSquare = false)
            ShapeType.SQUARE -> fitRectangle(classification, style, isSquare = true)
            ShapeType.CIRCLE -> fitCircle(classification, style)
            ShapeType.ELLIPSE -> fitEllipse(classification, style)
            ShapeType.TRIANGLE -> fitTriangle(classification, style)
            ShapeType.PENTAGON -> fitPolygon(classification, sides = 5, style = style)
            ShapeType.HEXAGON -> fitPolygon(classification, sides = 6, style = style)
            ShapeType.STAR -> fitStar(classification, style)
            ShapeType.HEART -> fitHeart(classification, style)
        }
    }

    // 1. LINE FITTING
    private fun fitLine(
        classification: ShapeClassificationResult,
        style: ShapeStyle
    ): RecognizedShape {
        val points = classification.resampledPoints
        val start = points.first()
        var end = points.last()

        val dx = end.x - start.x
        val dy = end.y - start.y
        val angleRad = atan2(dy, dx)
        val angleDeg = abs(Math.toDegrees(angleRad.toDouble()).toFloat())

        // Axis Snapping: Snap near horizontal, vertical, or 45-degree diagonal
        val snapToleranceDeg = 5.0f
        val diagonalToleranceDeg = 4.0f

        val snappedEnd = when {
            // Horizontal snap (0° or 180°)
            angleDeg <= snapToleranceDeg || abs(180f - angleDeg) <= snapToleranceDeg -> {
                Point2D(end.x, start.y)
            }
            // Vertical snap (90°)
            abs(90f - angleDeg) <= snapToleranceDeg -> {
                Point2D(start.x, end.y)
            }
            // Diagonal 45° or 135° snap
            abs(45f - angleDeg) <= diagonalToleranceDeg || abs(135f - angleDeg) <= diagonalToleranceDeg -> {
                val signX = if (dx >= 0) 1f else -1f
                val signY = if (dy >= 0) 1f else -1f
                val avgDist = (abs(dx) + abs(dy)) * 0.5f
                Point2D(start.x + signX * avgDist, start.y + signY * avgDist)
            }
            else -> end
        }

        val fittedPoints = listOf(start, snappedEnd)
        val bbox = BoundingBox.fromPoints(fittedPoints)

        return RecognizedShape(
            type = ShapeType.LINE,
            points = fittedPoints,
            boundingBox = bbox,
            style = style,
            confidence = classification.confidence,
            center = bbox.center,
            rotationRad = angleRad,
            controlPoints = fittedPoints
        )
    }

    // 2. ARROW FITTING
    private fun fitArrow(
        classification: ShapeClassificationResult,
        style: ShapeStyle
    ): RecognizedShape {
        val corners = classification.detectedCorners
        val points = classification.resampledPoints
        val start = points.first()

        // Locate shaft tip: either corner 1 or corner near end of shaft
        val tip = if (corners.size >= 3) corners[1] else points[points.size * 3 / 4]

        val shaftVec = tip - start
        val shaftLen = max(shaftVec.length(), 1f)
        val unitShaft = shaftVec / shaftLen

        val wingAngleRad = Math.toRadians(28.0).toFloat()
        val wingLen = (shaftLen * 0.22f).coerceIn(16f, 40f)

        // Wings point back toward start
        val backVec = -unitShaft
        val wing1 = tip + backVec.rotate(wingAngleRad) * wingLen
        val wing2 = tip + backVec.rotate(-wingAngleRad) * wingLen

        // Polyline order: Start -> Tip -> Wing1 -> Tip -> Wing2
        val fittedPoints = listOf(start, tip, wing1, tip, wing2)
        val bbox = BoundingBox.fromPoints(fittedPoints)

        return RecognizedShape(
            type = ShapeType.ARROW,
            points = fittedPoints,
            boundingBox = bbox,
            style = style,
            confidence = classification.confidence,
            center = bbox.center,
            rotationRad = unitShaft.angle(),
            controlPoints = listOf(start, tip, wing1, wing2)
        )
    }

    // 3. RECTANGLE / SQUARE FITTING
    private fun fitRectangle(
        classification: ShapeClassificationResult,
        style: ShapeStyle,
        isSquare: Boolean
    ): RecognizedShape {
        val points = classification.resampledPoints
        val cov = CurvatureAnalyzer.computeCovariance(points)
        val center = cov.centroid
        val bbox = classification.boundingBox

        // Determine orientation angle and snap to axis if within 6 degrees
        var angleRad = cov.orientationRad
        val angleDeg = abs(Math.toDegrees(angleRad.toDouble()).toFloat())
        if (angleDeg <= 6.0f || abs(90f - angleDeg) <= 6.0f || abs(180f - angleDeg) <= 6.0f) {
            angleRad = 0f
        }

        val halfW: Float
        val halfH: Float
        if (isSquare) {
            val side = (bbox.width + bbox.height) * 0.5f
            halfW = side * 0.5f
            halfH = side * 0.5f
        } else {
            halfW = bbox.width * 0.5f
            halfH = bbox.height * 0.5f
        }

        val v0 = center + Point2D(-halfW, -halfH).rotate(angleRad)
        val v1 = center + Point2D(halfW, -halfH).rotate(angleRad)
        val v2 = center + Point2D(halfW, halfH).rotate(angleRad)
        val v3 = center + Point2D(-halfW, halfH).rotate(angleRad)
        val fittedPoints = listOf(v0, v1, v2, v3, v0)
        val fittedBbox = BoundingBox.fromPoints(fittedPoints)

        return RecognizedShape(
            type = if (isSquare) ShapeType.SQUARE else ShapeType.RECTANGLE,
            points = fittedPoints,
            boundingBox = fittedBbox,
            style = style,
            confidence = classification.confidence,
            center = center,
            rotationRad = angleRad,
            isSquare = isSquare,
            controlPoints = listOf(v0, v1, v2, v3)
        )
    }

    // 4. CIRCLE FITTING
    private fun fitCircle(
        classification: ShapeClassificationResult,
        style: ShapeStyle
    ): RecognizedShape {
        val points = classification.resampledPoints
        val center = CurvatureAnalyzer.computeCentroid(points)
        val (radiusMean, _) = CurvatureAnalyzer.computeRadiusStats(points, center)
        val radius = if (radiusMean > 1f) radiusMean else (classification.boundingBox.diagonal * 0.35f)

        val sampleCount = 48
        val fittedPoints = ArrayList<Point2D>(sampleCount + 1)
        for (i in 0 until sampleCount) {
            val angle = (2.0 * PI * i / sampleCount).toFloat()
            fittedPoints.add(center + Point2D(radius * cos(angle), radius * sin(angle)))
        }
        fittedPoints.add(fittedPoints.first())
        val bbox = BoundingBox(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )

        return RecognizedShape(
            type = ShapeType.CIRCLE,
            points = fittedPoints,
            boundingBox = bbox,
            style = style,
            confidence = classification.confidence,
            center = center,
            radius = radius,
            isCircle = true,
            controlPoints = listOf(center, center + Point2D(radius, 0f))
        )
    }

    // 5. ELLIPSE FITTING
    private fun fitEllipse(
        classification: ShapeClassificationResult,
        style: ShapeStyle
    ): RecognizedShape {
        val points = classification.resampledPoints
        val cov = CurvatureAnalyzer.computeCovariance(points)
        val center = cov.centroid
        val bbox = classification.boundingBox

        val a = bbox.width * 0.5f
        val b = bbox.height * 0.5f
        var angleRad = cov.orientationRad
        val angleDeg = abs(Math.toDegrees(angleRad.toDouble()).toFloat())
        if (angleDeg <= 6.0f || abs(90f - angleDeg) <= 6.0f) {
            angleRad = 0f
        }

        val sampleCount = 48
        val fittedPoints = ArrayList<Point2D>(sampleCount + 1)
        val cosRot = cos(angleRad)
        val sinRot = sin(angleRad)

        for (i in 0 until sampleCount) {
            val phi = (2.0 * PI * i / sampleCount).toFloat()
            val ex = a * cos(phi)
            val ey = b * sin(phi)
            val rx = center.x + ex * cosRot - ey * sinRot
            val ry = center.y + ex * sinRot + ey * cosRot
            fittedPoints.add(Point2D(rx, ry))
        }
        fittedPoints.add(fittedPoints.first())

        val fittedBbox = BoundingBox.fromPoints(fittedPoints)

        return RecognizedShape(
            type = ShapeType.ELLIPSE,
            points = fittedPoints,
            boundingBox = fittedBbox,
            style = style,
            confidence = classification.confidence,
            center = center,
            rotationRad = angleRad,
            semiMajorAxis = max(a, b),
            semiMinorAxis = min(a, b),
            controlPoints = listOf(center, center + Point2D(a, 0f).rotate(angleRad))
        )
    }

    // 6. TRIANGLE FITTING
    private fun fitTriangle(
        classification: ShapeClassificationResult,
        style: ShapeStyle
    ): RecognizedShape {
        val corners = classification.detectedCorners
        val bbox = classification.boundingBox
        val center = bbox.center

        val v0: Point2D
        val v1: Point2D
        val v2: Point2D

        if (corners.size >= 3) {
            v0 = corners[0]
            v1 = corners[1]
            v2 = corners[2]
        } else {
            // Default equilateral orientation pointing up
            val r = bbox.height * 0.6f
            v0 = center + Point2D(0f, -r)
            v1 = center + Point2D(r * cos(Math.toRadians(30.0).toFloat()), r * sin(Math.toRadians(30.0).toFloat()))
            v2 = center + Point2D(-r * cos(Math.toRadians(30.0).toFloat()), r * sin(Math.toRadians(30.0).toFloat()))
        }

        val fittedPoints = listOf(v0, v1, v2, v0)
        val fittedBbox = BoundingBox.fromPoints(fittedPoints)

        return RecognizedShape(
            type = ShapeType.TRIANGLE,
            points = fittedPoints,
            boundingBox = fittedBbox,
            style = style,
            confidence = classification.confidence,
            center = center,
            controlPoints = listOf(v0, v1, v2)
        )
    }

    // 7. REGULAR POLYGON FITTING (Pentagon / Hexagon)
    private fun fitPolygon(
        classification: ShapeClassificationResult,
        sides: Int,
        style: ShapeStyle
    ): RecognizedShape {
        val points = classification.resampledPoints
        val center = CurvatureAnalyzer.computeCentroid(points)
        val bbox = classification.boundingBox
        val r = (bbox.width + bbox.height) * 0.28f

        // Top vertex points up (-PI/2)
        val startAngle = -PI.toFloat() * 0.5f
        val angleStep = (2.0 * PI / sides).toFloat()

        val vertices = ArrayList<Point2D>(sides + 1)
        for (i in 0 until sides) {
            val angle = startAngle + i * angleStep
            vertices.add(center + Point2D(r * cos(angle), r * sin(angle)))
        }
        vertices.add(vertices.first())

        val fittedBbox = BoundingBox.fromPoints(vertices)
        val shapeType = if (sides == 5) ShapeType.PENTAGON else ShapeType.HEXAGON

        return RecognizedShape(
            type = shapeType,
            points = vertices,
            boundingBox = fittedBbox,
            style = style,
            confidence = classification.confidence,
            center = center,
            radius = r,
            controlPoints = vertices.subList(0, sides)
        )
    }

    // 8. STAR FITTING (5-Pointed Regular Star)
    private fun fitStar(
        classification: ShapeClassificationResult,
        style: ShapeStyle
    ): RecognizedShape {
        val points = classification.resampledPoints
        val center = CurvatureAnalyzer.computeCentroid(points)
        val bbox = classification.boundingBox

        val outerRadius = (bbox.width + bbox.height) * 0.28f
        val innerRadius = outerRadius * 0.40f

        val startAngle = -PI.toFloat() * 0.5f // top tip points up
        val angleStep = (PI / 5.0).toFloat() // 36 degrees

        val vertices = ArrayList<Point2D>(11)
        for (i in 0 until 10) {
            val angle = startAngle + i * angleStep
            val r = if (i % 2 == 0) outerRadius else innerRadius
            vertices.add(center + Point2D(r * cos(angle), r * sin(angle)))
        }
        vertices.add(vertices.first())

        val fittedBbox = BoundingBox.fromPoints(vertices)

        return RecognizedShape(
            type = ShapeType.STAR,
            points = vertices,
            boundingBox = fittedBbox,
            style = style,
            confidence = classification.confidence,
            center = center,
            radius = outerRadius,
            controlPoints = vertices.subList(0, 10)
        )
    }

    // 9. HEART FITTING (Parametric Cardiod Formulation)
    private fun fitHeart(
        classification: ShapeClassificationResult,
        style: ShapeStyle
    ): RecognizedShape {
        val bbox = classification.boundingBox
        val center = bbox.center
        val width = bbox.width
        val height = bbox.height

        val sampleCount = 64
        val rawHeartPoints = ArrayList<Point2D>(sampleCount + 1)

        // Canonical Cardiod Heart Curve:
        // x(t) = 16 * sin^3(t)
        // y(t) = -(13 * cos(t) - 5 * cos(2t) - 2 * cos(3t) - cos(4t))
        // Screen Y goes downward, so minus sign aligns top cleft up and bottom apex down.
        var minX = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY

        val rawCoords = ArrayList<Point2D>(sampleCount + 1)
        for (i in 0 until sampleCount) {
            val t = (2.0 * PI * i / sampleCount).toFloat()
            val sinT = sin(t)
            val hx = 16f * sinT * sinT * sinT
            val hy = -(13f * cos(t) - 5f * cos(2f * t) - 2f * cos(3f * t) - cos(4f * t))

            if (hx < minX) minX = hx
            if (hx > maxX) maxX = hx
            if (hy < minY) minY = hy
            if (hy > maxY) maxY = hy

            rawCoords.add(Point2D(hx, hy))
        }

        val rawW = max(maxX - minX, 1e-4f)
        val rawH = max(maxY - minY, 1e-4f)

        // Scale and translate into original bounding box
        for (p in rawCoords) {
            val normX = (p.x - minX) / rawW
            val normY = (p.y - minY) / rawH
            val mappedX = bbox.minX + normX * width
            val mappedY = bbox.minY + normY * height
            rawHeartPoints.add(Point2D(mappedX, mappedY))
        }
        rawHeartPoints.add(rawHeartPoints.first())

        val fittedBbox = BoundingBox.fromPoints(rawHeartPoints)

        return RecognizedShape(
            type = ShapeType.HEART,
            points = rawHeartPoints,
            boundingBox = fittedBbox,
            style = style,
            confidence = classification.confidence,
            center = center,
            controlPoints = listOf(
                Point2D(center.x, bbox.minY + height * 0.20f), // Cusp
                Point2D(center.x, bbox.maxY)                 // Apex
            )
        )
    }
}
