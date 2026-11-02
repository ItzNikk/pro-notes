package shapes_engine.model

/**
 * Parameterized clean geometric shape output by the Shapes Engine.
 *
 * @param type Recognized shape category ([ShapeType]).
 * @param points Clean fitted coordinate vertices / polyline points representing the geometry.
 * @param boundingBox Axis-aligned bounding box of the clean shape.
 * @param style Applied visual styling (stroke, fill, opacity).
 * @param confidence Recognition confidence score in range [0.0, 1.0].
 * @param center Centroid of the shape.
 * @param rotationRad Orientation angle in radians.
 * @param radius Circumradius or circle radius, if applicable.
 * @param semiMajorAxis Ellipse semi-major axis length, if applicable.
 * @param semiMinorAxis Ellipse semi-minor axis length, if applicable.
 * @param isSquare True if classified as a square (aspect ratio >= 0.88).
 * @param isCircle True if classified as a circle (aspect ratio <= 1.18).
 * @param controlPoints Key landmark vertices (e.g. arrow shaft + wings, polygon corners).
 */
data class RecognizedShape(
    val type: ShapeType,
    val points: List<Point2D>,
    val boundingBox: BoundingBox,
    val style: ShapeStyle = ShapeStyle(),
    val confidence: Float = 1.0f,
    val center: Point2D = boundingBox.center,
    val rotationRad: Float = 0f,
    val radius: Float = 0f,
    val semiMajorAxis: Float = 0f,
    val semiMinorAxis: Float = 0f,
    val isSquare: Boolean = (type == ShapeType.SQUARE),
    val isCircle: Boolean = (type == ShapeType.CIRCLE),
    val controlPoints: List<Point2D> = emptyList()
) {
    val isClosed: Boolean get() = type.isClosed
    val supportsFill: Boolean get() = type.supportsFill
}
