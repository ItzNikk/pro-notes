package shapes_engine.model

import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Axis-aligned bounding box and geometric bounds calculation.
 * Zero external library dependencies.
 */
data class BoundingBox(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
) {
    val width: Float get() = max(0f, maxX - minX)
    val height: Float get() = max(0f, maxY - minY)
    val center: Point2D get() = Point2D((minX + maxX) / 2f, (minY + maxY) / 2f)
    val diagonal: Float get() = hypot(width, height)

    val minDimension: Float get() = min(width, height)
    val maxDimension: Float get() = max(width, height)

    /**
     * Normalized aspect ratio in range [0, 1] comparing smaller dimension to larger dimension.
     * Values close to 1.0 indicate square or circular proportions.
     */
    val squareAspectRatio: Float
        get() = if (maxDimension > 1e-4f) minDimension / maxDimension else 1f

    /**
     * Standard width-to-height ratio.
     */
    val aspectRatio: Float
        get() = if (height > 1e-4f) width / height else 1f

    fun contains(point: Point2D): Boolean =
        point.x in minX..maxX && point.y in minY..maxY

    fun expanded(padding: Float): BoundingBox =
        BoundingBox(minX - padding, minY - padding, maxX + padding, maxY + padding)

    companion object {
        val Empty = BoundingBox(0f, 0f, 0f, 0f)

        fun fromPoints(points: Collection<Point2D>): BoundingBox {
            if (points.isEmpty()) return Empty
            var minX = Float.POSITIVE_INFINITY
            var minY = Float.POSITIVE_INFINITY
            var maxX = Float.NEGATIVE_INFINITY
            var maxY = Float.NEGATIVE_INFINITY

            for (p in points) {
                if (p.x < minX) minX = p.x
                if (p.y < minY) minY = p.y
                if (p.x > maxX) maxX = p.x
                if (p.y > maxY) maxY = p.y
            }
            return BoundingBox(minX, minY, maxX, maxY)
        }
    }
}
