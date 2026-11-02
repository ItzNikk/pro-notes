package shapes_engine.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Pure Kotlin 2D Point and vector operations with zero external library dependencies.
 */
data class Point2D(val x: Float, val y: Float) {
    operator fun plus(other: Point2D): Point2D = Point2D(x + other.x, y + other.y)
    operator fun minus(other: Point2D): Point2D = Point2D(x - other.x, y - other.y)
    operator fun times(scalar: Float): Point2D = Point2D(x * scalar, y * scalar)
    operator fun div(scalar: Float): Point2D = Point2D(x / scalar, y / scalar)
    operator fun unaryMinus(): Point2D = Point2D(-x, -y)

    fun distanceTo(other: Point2D): Float = hypot(x - other.x, y - other.y)
    fun distanceSquaredTo(other: Point2D): Float = (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)
    fun length(): Float = hypot(x, y)
    fun lengthSquared(): Float = x * x + y * y

    fun normalized(): Point2D {
        val len = length()
        return if (len > 1e-6f) this / len else Point2D(0f, 0f)
    }

    fun dot(other: Point2D): Float = x * other.x + y * other.y
    fun cross(other: Point2D): Float = x * other.y - y * other.x

    fun rotate(angleRad: Float): Point2D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Point2D(x * cosA - y * sinA, x * sinA + y * cosA)
    }

    fun rotateAround(center: Point2D, angleRad: Float): Point2D {
        val translated = this - center
        val rotated = translated.rotate(angleRad)
        return rotated + center
    }

    fun angle(): Float = atan2(y, x)

    fun lerp(other: Point2D, t: Float): Point2D = this + (other - this) * t

    companion object {
        val Zero = Point2D(0f, 0f)
    }
}
