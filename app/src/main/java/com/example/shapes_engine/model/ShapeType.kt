package shapes_engine.model

/**
 * Supported geometric shapes recognized and fitted by the Shapes Engine.
 */
enum class ShapeType(
    val displayName: String,
    val isClosed: Boolean,
    val supportsFill: Boolean,
    val minPoints: Int
) {
    // Open Stroke Shapes
    LINE("Straight Line", isClosed = false, supportsFill = false, minPoints = 2),
    ARROW("Arrow", isClosed = false, supportsFill = false, minPoints = 3),
    ANGLE_BRACKET("Angle Bracket (< >)", isClosed = false, supportsFill = false, minPoints = 3),
    CURVE("Arc Curve", isClosed = false, supportsFill = false, minPoints = 3),

    // Closed Standard Shapes
    RECTANGLE("Rectangle", isClosed = true, supportsFill = true, minPoints = 4),
    SQUARE("Square", isClosed = true, supportsFill = true, minPoints = 4),
    DIAMOND("Diamond", isClosed = true, supportsFill = true, minPoints = 4),
    CIRCLE("Circle", isClosed = true, supportsFill = true, minPoints = 8),
    ELLIPSE("Ellipse", isClosed = true, supportsFill = true, minPoints = 8),
    TRIANGLE("Triangle", isClosed = true, supportsFill = true, minPoints = 3),

    // Closed Extended Shapes
    PENTAGON("Pentagon", isClosed = true, supportsFill = true, minPoints = 5),
    HEXAGON("Hexagon", isClosed = true, supportsFill = true, minPoints = 6),
    STAR("Star", isClosed = true, supportsFill = true, minPoints = 10),
    HEART("Heart", isClosed = true, supportsFill = true, minPoints = 12);
}
