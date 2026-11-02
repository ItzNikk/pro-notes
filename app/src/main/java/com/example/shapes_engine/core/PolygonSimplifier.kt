package shapes_engine.core

import shapes_engine.model.BoundingBox
import shapes_engine.model.Point2D
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.hypot

/**
 * Pure Kotlin polygon simplification using Ramer-Douglas-Peucker (RDP) algorithm
 * with closed-loop support and collinear vertex suppression.
 * Zero external library dependencies.
 */
object PolygonSimplifier {

    /**
     * Calculates perpendicular distance from [point] to line segment [start] -> [end].
     */
    fun perpendicularDistanceToSegment(point: Point2D, start: Point2D, end: Point2D): Float {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val lengthSq = dx * dx + dy * dy
        if (lengthSq < 1e-8f) {
            return point.distanceTo(start)
        }

        // Projection factor t
        val t = ((point.x - start.x) * dx + (point.y - start.y) * dy) / lengthSq
        val clampedT = t.coerceIn(0f, 1f)
        val projX = start.x + clampedT * dx
        val projY = start.y + clampedT * dy
        return hypot(point.x - projX, point.y - projY)
    }

    /**
     * Standard RDP implementation on an open sub-sequence of points.
     */
    private fun rdpRecursive(
        points: List<Point2D>,
        startIndex: Int,
        endIndex: Int,
        epsilon: Float,
        outIndices: MutableList<Int>
    ) {
        if (endIndex <= startIndex + 1) return

        val start = points[startIndex]
        val end = points[endIndex]
        var maxDist = -1f
        var maxIdx = -1

        for (i in startIndex + 1 until endIndex) {
            val dist = perpendicularDistanceToSegment(points[i], start, end)
            if (dist > maxDist) {
                maxDist = dist
                maxIdx = i
            }
        }

        if (maxDist > epsilon && maxIdx != -1) {
            rdpRecursive(points, startIndex, maxIdx, epsilon, outIndices)
            outIndices.add(maxIdx)
            rdpRecursive(points, maxIdx, endIndex, epsilon, outIndices)
        }
    }

    /**
     * Simplifies a polyline using RDP with threshold [epsilon].
     * Supports both open strokes and closed loops without degenerate zero-length baselines.
     */
    fun simplify(
        points: List<Point2D>,
        epsilon: Float,
        isClosed: Boolean = false
    ): List<Point2D> {
        if (points.size <= 2) return points

        if (!isClosed) {
            val indices = ArrayList<Int>()
            indices.add(0)
            rdpRecursive(points, 0, points.size - 1, epsilon, indices)
            indices.add(points.size - 1)
            return indices.map { points[it] }
        }

        // For closed loops, find the point farthest from points[0] to serve as the split vertex
        var maxDistToStart = -1f
        var splitIdx = points.size / 2
        val start = points[0]

        for (i in 1 until points.size) {
            val d = points[i].distanceSquaredTo(start)
            if (d > maxDistToStart) {
                maxDistToStart = d
                splitIdx = i
            }
        }

        val firstHalfIndices = ArrayList<Int>()
        firstHalfIndices.add(0)
        rdpRecursive(points, 0, splitIdx, epsilon, firstHalfIndices)
        firstHalfIndices.add(splitIdx)

        val secondHalfIndices = ArrayList<Int>()
        rdpRecursive(points, splitIdx, points.size - 1, epsilon, secondHalfIndices)
        secondHalfIndices.add(points.size - 1)

        val allIndices = firstHalfIndices + secondHalfIndices
        return allIndices.map { points[it] }
    }

    /**
     * Removes intermediate vertices where adjacent edges are nearly collinear
     * (turn angle < [minTurnAngleDeg]).
     */
    fun filterCollinearVertices(
        vertices: List<Point2D>,
        minTurnAngleDeg: Float = 14.0f,
        isClosed: Boolean = true
    ): List<Point2D> {
        if (vertices.size <= (if (isClosed) 3 else 2)) return vertices

        val minTurnRad = Math.toRadians(minTurnAngleDeg.toDouble()).toFloat()
        val result = ArrayList<Point2D>(vertices.size)

        // If closed, the first and last vertex represent the loop closure
        val closureGap = vertices.first().distanceTo(vertices.last())
        val n = if (isClosed && (closureGap < 15.0f || closureGap < 0.10f * BoundingBox.fromPoints(vertices).diagonal)) {
            vertices.size - 1
        } else {
            vertices.size
        }

        for (i in 0 until n) {
            val prev = if (isClosed) vertices[(i - 1 + n) % n] else if (i == 0) null else vertices[i - 1]
            val curr = vertices[i]
            val next = if (isClosed) vertices[(i + 1) % n] else if (i == n - 1) null else vertices[i + 1]

            if (prev == null || next == null) {
                result.add(curr)
                continue
            }

            val v1 = (curr - prev).normalized()
            val v2 = (next - curr).normalized()
            val dot = (v1.dot(v2)).coerceIn(-1f, 1f)
            val turnAngle = acos(dot)

            if (turnAngle >= minTurnRad) {
                result.add(curr)
            }
        }

        if (isClosed && result.size >= 3) {
            result.add(result.first())
        }
        return result
    }

    /**
     * Merges adjacent vertices in a closed polygon that are closer than [minDistancePx].
     * Eliminates duplicate corners caused by closure overshoot or stylus liftoff jitter.
     */
    fun mergeCloseVertices(
        vertices: List<Point2D>,
        minDistancePx: Float,
        isClosed: Boolean = true
    ): List<Point2D> {
        if (vertices.size <= (if (isClosed) 3 else 2)) return vertices

        val minSq = minDistancePx * minDistancePx
        val closureDist = vertices.first().distanceTo(vertices.last())
        val n = if (isClosed && (closureDist < minDistancePx || closureDist < 15.0f)) {
            vertices.size - 1
        } else {
            vertices.size
        }

        val unclosed = vertices.subList(0, n)
        val merged = ArrayList<Point2D>()
        var skipNext = false

        for (i in 0 until n) {
            if (skipNext) {
                skipNext = false
                continue
            }
            val curr = unclosed[i]
            val next = unclosed[(i + 1) % n]

            if (curr.distanceSquaredTo(next) < minSq && merged.size + (n - i - 1) >= (if (isClosed) 3 else 2)) {
                // Average the two close vertices
                merged.add(curr.lerp(next, 0.5f))
                skipNext = true
            } else {
                merged.add(curr)
            }
        }

        // Check if first and last in merged are too close
        if (isClosed && merged.size > 3 && merged.first().distanceSquaredTo(merged.last()) < minSq) {
            val combined = merged.first().lerp(merged.last(), 0.5f)
            merged[0] = combined
            merged.removeAt(merged.size - 1)
        }

        if (isClosed && merged.size >= 3) {
            merged.add(merged.first())
        }
        return merged
    }
}
