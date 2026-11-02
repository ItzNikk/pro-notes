package shapes_engine.core

import shapes_engine.model.Point2D
import kotlin.math.hypot

/**
 * Resamples drawing strokes into equidistant points along their arc length
 * with deduplication and loop closure trimming.
 * Pure Kotlin implementation with zero external libraries.
 */
object StrokeResampler {

    const val DEFAULT_SAMPLE_COUNT = 96
    const val MIN_POINT_SPACING = 0.5f

    /**
     * Deduplicates consecutive points that are closer than [minDistancePx].
     */
    fun deduplicate(rawPoints: List<Point2D>, minDistancePx: Float = MIN_POINT_SPACING): List<Point2D> {
        if (rawPoints.size <= 1) return rawPoints
        val result = ArrayList<Point2D>(rawPoints.size)
        result.add(rawPoints[0])
        var last = rawPoints[0]
        val minSq = minDistancePx * minDistancePx

        for (i in 1 until rawPoints.size) {
            val curr = rawPoints[i]
            if (curr.distanceSquaredTo(last) >= minSq) {
                result.add(curr)
                last = curr
            }
        }
        // Always ensure the final point is retained if distant enough, or replace last if at least 2
        val lastOriginal = rawPoints.last()
        if (result.size > 1 && result.last().distanceSquaredTo(lastOriginal) > 0f) {
            if (result.last().distanceSquaredTo(lastOriginal) < minSq) {
                result[result.size - 1] = lastOriginal
            } else {
                result.add(lastOriginal)
            }
        }
        return result
    }

    /**
     * Computes the total arc length of a sequence of points.
     */
    fun computeTotalArcLength(points: List<Point2D>): Float {
        var len = 0f
        for (i in 1 until points.size) {
            len += points[i - 1].distanceTo(points[i])
        }
        return len
    }

    /**
     * Resamples the given stroke into [targetCount] equidistant points along the arc-length.
     */
    fun resample(
        rawPoints: List<Point2D>,
        targetCount: Int = DEFAULT_SAMPLE_COUNT
    ): List<Point2D> {
        val deduped = deduplicate(rawPoints)
        if (deduped.size < 2) return deduped
        if (targetCount <= 2) return listOf(deduped.first(), deduped.last())

        // Calculate cumulative arc-lengths
        val arcLengths = FloatArray(deduped.size)
        arcLengths[0] = 0f
        for (i in 1 until deduped.size) {
            arcLengths[i] = arcLengths[i - 1] + deduped[i - 1].distanceTo(deduped[i])
        }
        val totalLength = arcLengths[deduped.size - 1]
        if (totalLength < 1e-4f) {
            return List(targetCount) { deduped.first() }
        }

        val step = totalLength / (targetCount - 1)
        val resampled = ArrayList<Point2D>(targetCount)
        resampled.add(deduped.first())

        var currIdx = 0
        for (k in 1 until targetCount - 1) {
            val targetDist = k * step
            while (currIdx < arcLengths.size - 2 && arcLengths[currIdx + 1] < targetDist) {
                currIdx++
            }
            val segStartDist = arcLengths[currIdx]
            val segEndDist = arcLengths[currIdx + 1]
            val segLen = segEndDist - segStartDist
            val t = if (segLen > 1e-6f) ((targetDist - segStartDist) / segLen).coerceIn(0f, 1f) else 0f
            val interpolated = deduped[currIdx].lerp(deduped[currIdx + 1], t)
            resampled.add(interpolated)
        }
        resampled.add(deduped.last())
        return resampled
    }

    /**
     * Checks if a stroke loops past its start point and trims any trailing overshoot.
     */
    fun trimClosureOvershoot(points: List<Point2D>, bboxDiagonal: Float): List<Point2D> {
        if (points.size < 12) return points
        val start = points[0]
        val searchStart = points.size / 2
        var minDistance = Float.MAX_VALUE
        var minIdx = points.size - 1

        for (i in searchStart until points.size) {
            val d = points[i].distanceTo(start)
            if (d < minDistance) {
                minDistance = d
                minIdx = i
            }
        }

        val threshold = 0.18f * bboxDiagonal
        return if (minDistance <= threshold && minIdx < points.size - 2) {
            points.subList(0, minIdx + 1)
        } else {
            points
        }
    }
}
