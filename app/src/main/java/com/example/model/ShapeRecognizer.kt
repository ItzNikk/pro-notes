package com.example.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Recognises hand-drawn shapes by *fitting* instead of counting corners.
 *
 *  1. Clean the stroke (drop duplicate points, trim pen overshoot at the closing point).
 *  2. Simplify the outline with Ramer-Douglas-Peucker into a handful of vertices.
 *  3. Ask "is this a straight-edged polygon?" by measuring how far the real ink wanders from the
 *     simplified edges (wobble). Hand-drawn polygons hug their edges; circles bow away from them.
 *  4. Whiten the points with PCA, so rotated ovals and circles are told apart by axis ratio.
 *  5. Stars are caught first: many vertices, several reflex (inward) ones, uneven radius.
 *
 * All thresholds are relative to the stroke's bounding diagonal, so size doesn't matter.
 */
object ShapeRecognizer {
  private const val SAMPLES = 96

  // Simplification tolerance as a fraction of the bounding diagonal
  private const val POLY_EPS = 0.045f
  private const val STAR_EPS = 0.03f

  // Closed-stroke detection
  private const val CLOSE_GAP = 0.2f
  private const val MIN_LOOP_LEN = 1.5f

  // A vertex that turns less than this is just noise on a straight edge
  private const val MIN_VERTEX_TURN_DEG = 28.0

  // Max average (distance to edge / edge length) for an outline to count as straight-edged
  private const val MAX_WOBBLE = 0.05f

  private const val CIRCLE_MAX_ASPECT = 1.2f
  private const val STAR_MIN_RADIUS_VARIATION = 0.14f

  /** [vertices] holds the user's own corner points (line ends, arrow tail/tip, polygon corners). */
  class Recognition(val kind: ShapeKind, val vertices: List<Offset> = emptyList())

  fun recognize(raw: List<Offset>): ShapeKind = analyze(raw).kind

  fun analyze(raw: List<Offset>): Recognition {
    val pts = dedupe(raw)
    if (pts.size < 3) return Recognition(ShapeKind.LINE, listOfNotNull(pts.firstOrNull(), pts.lastOrNull()))

    val diag = hypot(pts.maxOf { it.x } - pts.minOf { it.x }, pts.maxOf { it.y } - pts.minOf { it.y })
      .coerceAtLeast(1f)
    val pathLen = length(pts)

    val loop = trimToLoop(pts, diag, pathLen)
    if (loop == null) {
      val kind = classifyOpen(pts, diag, pathLen)
      val vertices = when (kind) {
        ShapeKind.LINE -> listOf(pts.first(), pts.last())
        ShapeKind.ARROW -> rdp(pts, diag * 0.04f)
        else -> emptyList()
      }
      return Recognition(kind, vertices)
    }

    val ring = resample(loop, SAMPLES)
    val kind = classifyClosed(ring, diag)
    val vertices = when (kind) {
      ShapeKind.TRIANGLE, ShapeKind.PENTAGON, ShapeKind.HEXAGON ->
        cleanVertices(simplifyClosed(ring, diag * POLY_EPS))
      ShapeKind.STAR -> cleanVertices(simplifyClosed(ring, diag * STAR_EPS))
      else -> emptyList()
    }
    return Recognition(kind, vertices)
  }

  // ---------------------------------------------------------------- open strokes

  private fun classifyOpen(pts: List<Offset>, diag: Float, pathLen: Float): ShapeKind {
    val first = pts.first()
    val last = pts.last()
    val chord = (last - first).getDistance()
    if (chord < 1f) return ShapeKind.CURVE

    val maxDeviation = pts.maxOf { distToSegment(it, first, last) }
    if (maxDeviation <= chord * 0.05f) return ShapeKind.LINE

    // Arrow = one long straight shaft, then a sharp reversal into a short barb.
    val s = rdp(pts, diag * 0.04f)
    if (s.size in 3..4) {
      val shaft = (s[1] - s[0]).getDistance()
      val barb = (s.last() - s[s.size - 2]).getDistance()
      val turn = turnDeg(s[1] - s[0], s[2] - s[1])
      if (shaft >= pathLen * 0.6f && turn >= 100.0 && barb in (shaft * 0.08f)..(shaft * 0.45f)) {
        return ShapeKind.ARROW
      }
    }
    return ShapeKind.CURVE
  }

  // ---------------------------------------------------------------- closed strokes

  private fun classifyClosed(ring: List<Offset>, diag: Float): ShapeKind {
    if (ring.size < 12) return ShapeKind.OVAL

    val fit = whiten(ring)

    // Stars: lots of vertices, several pointing inward, radius far from constant
    val fine = cleanVertices(simplifyClosed(ring, diag * STAR_EPS))
    if (fine.size in 8..12 && reflexCount(fine) >= 3 && fit.radiusVariation >= STAR_MIN_RADIUS_VARIATION) {
      return ShapeKind.STAR
    }

    val poly = cleanVertices(simplifyClosed(ring, diag * POLY_EPS))
    val k = poly.size
    val round = if (fit.aspect <= CIRCLE_MAX_ASPECT) ShapeKind.CIRCLE else ShapeKind.OVAL

    if (k !in 3..6 || wobble(ring, poly) > MAX_WOBBLE) return round

    return when (k) {
      3 -> ShapeKind.TRIANGLE
      4 -> if (axisSkewDeg(poly) > 25.0) ShapeKind.DIAMOND else ShapeKind.RECTANGLE
      5 -> ShapeKind.PENTAGON
      else -> ShapeKind.HEXAGON
    }
  }

  private class Fit(val aspect: Float, val radiusVariation: Float)

  /** PCA-whitens the ring: gives axis ratio and how uneven the radius is around the shape. */
  private fun whiten(ring: List<Offset>): Fit {
    val n = ring.size
    val mx = ring.sumOf { it.x.toDouble() } / n
    val my = ring.sumOf { it.y.toDouble() } / n
    var sxx = 0.0
    var syy = 0.0
    var sxy = 0.0
    for (p in ring) {
      val dx = p.x - mx
      val dy = p.y - my
      sxx += dx * dx
      syy += dy * dy
      sxy += dx * dy
    }
    val theta = 0.5 * atan2(2 * sxy, sxx - syy)
    val c = cos(theta)
    val s = sin(theta)
    val us = ArrayList<Double>(n)
    val vs = ArrayList<Double>(n)
    for (p in ring) {
      val dx = p.x - mx
      val dy = p.y - my
      us.add(dx * c + dy * s)
      vs.add(-dx * s + dy * c)
    }
    val su = sqrt(us.sumOf { it * it } / n).coerceAtLeast(1e-3)
    val sv = sqrt(vs.sumOf { it * it } / n).coerceAtLeast(1e-3)
    val radii = List(n) { hypot(us[it] / su, vs[it] / sv) }
    val mean = radii.average().coerceAtLeast(1e-3)
    val std = sqrt(radii.sumOf { (it - mean) * (it - mean) } / n) / mean
    return Fit(aspect = (maxOf(su, sv) / minOf(su, sv)).toFloat(), radiusVariation = std.toFloat())
  }

  /**
   * Average of (distance from ink to its nearest polygon edge) / (that edge's length).
   * Straight-sided shapes score near 0; a circle approximated by chords scores noticeably higher.
   */
  private fun wobble(ring: List<Offset>, poly: List<Offset>): Float {
    var sum = 0f
    for (p in ring) {
      var best = Float.MAX_VALUE
      var bestLen = 1f
      for (i in poly.indices) {
        val a = poly[i]
        val b = poly[(i + 1) % poly.size]
        val d = distToSegment(p, a, b)
        if (d < best) {
          best = d
          bestLen = (b - a).getDistance().coerceAtLeast(1f)
        }
      }
      sum += best / bestLen
    }
    return sum / ring.size
  }

  /** Mean angle of the edges away from the nearest axis: ~0 for upright boxes, ~45 for diamonds. */
  private fun axisSkewDeg(poly: List<Offset>): Double {
    var total = 0.0
    for (i in poly.indices) {
      val e = poly[(i + 1) % poly.size] - poly[i]
      val a = Math.toDegrees(atan2(abs(e.y).toDouble(), abs(e.x).toDouble())) // 0..90
      total += minOf(a, 90.0 - a)
    }
    return total / poly.size
  }

  private fun reflexCount(poly: List<Offset>): Int {
    val n = poly.size
    var area2 = 0.0
    for (i in 0 until n) {
      val a = poly[i]
      val b = poly[(i + 1) % n]
      area2 += a.x.toDouble() * b.y - b.x.toDouble() * a.y
    }
    var count = 0
    for (i in 0 until n) {
      val a = poly[(i - 1 + n) % n]
      val b = poly[i]
      val c = poly[(i + 1) % n]
      val cross = (b.x - a.x).toDouble() * (c.y - b.y) - (b.y - a.y).toDouble() * (c.x - b.x)
      if (cross * area2 < 0) count++
    }
    return count
  }

  // ---------------------------------------------------------------- simplification

  /** Closed-ring RDP: split at the point farthest from the start, simplify both halves. */
  private fun simplifyClosed(ring: List<Offset>, eps: Float): List<Offset> {
    val far = ring.indices.maxByOrNull { (ring[it] - ring[0]).getDistance() } ?: return ring
    if (far == 0) return ring
    val a = rdp(ring.subList(0, far + 1), eps)
    val b = rdp(ring.subList(far, ring.size) + ring[0], eps)
    return a.dropLast(1) + b.dropLast(1)
  }

  /** Repeatedly removes the vertex with the gentlest turn until every vertex is a real corner. */
  private fun cleanVertices(input: List<Offset>): List<Offset> {
    val v = input.toMutableList()
    while (v.size > 3) {
      var worst = -1
      var worstTurn = MIN_VERTEX_TURN_DEG
      for (i in v.indices) {
        val prev = v[(i - 1 + v.size) % v.size]
        val next = v[(i + 1) % v.size]
        val t = turnDeg(v[i] - prev, next - v[i])
        if (t < worstTurn) {
          worstTurn = t
          worst = i
        }
      }
      if (worst < 0) break
      v.removeAt(worst)
    }
    return v
  }

  private fun rdp(pts: List<Offset>, eps: Float): List<Offset> {
    if (pts.size < 3) return pts
    var maxD = 0f
    var idx = 0
    for (i in 1 until pts.lastIndex) {
      val d = distToSegment(pts[i], pts.first(), pts.last())
      if (d > maxD) {
        maxD = d
        idx = i
      }
    }
    if (maxD <= eps) return listOf(pts.first(), pts.last())
    val left = rdp(pts.subList(0, idx + 1), eps)
    val right = rdp(pts.subList(idx, pts.size), eps)
    return left.dropLast(1) + right
  }

  // ---------------------------------------------------------------- stroke cleanup

  private fun dedupe(raw: List<Offset>): List<Offset> {
    val out = ArrayList<Offset>(raw.size)
    for (p in raw) if (out.isEmpty() || (p - out.last()).getDistance() >= 0.5f) out.add(p)
    return out
  }

  /**
   * If the stroke comes back near its start, returns it cut at the closest return point (so a pen
   * overshoot past the start doesn't distort the outline). Otherwise null.
   */
  private fun trimToLoop(pts: List<Offset>, diag: Float, pathLen: Float): List<Offset>? {
    if (pathLen < diag * MIN_LOOP_LEN) return null
    val tol = diag * CLOSE_GAP
    var best = -1
    var bestDist = Float.MAX_VALUE
    for (i in pts.size / 2 until pts.size) {
      val d = (pts[i] - pts.first()).getDistance()
      if (d < bestDist) {
        bestDist = d
        best = i
      }
    }
    return if (best >= 0 && bestDist <= tol) pts.subList(0, best + 1) else null
  }

  // ---------------------------------------------------------------- geometry helpers

  private fun turnDeg(v1: Offset, v2: Offset): Double {
    val mag = (v1.getDistance() * v2.getDistance()).coerceAtLeast(0.0001f)
    val c = ((v1.x * v2.x + v1.y * v2.y) / mag).coerceIn(-1f, 1f)
    return Math.toDegrees(acos(c.toDouble()))
  }

  private fun length(pts: List<Offset>): Float {
    var total = 0f
    for (i in 1 until pts.size) total += (pts[i] - pts[i - 1]).getDistance()
    return total
  }

  private fun distToSegment(p: Offset, a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val len2 = dx * dx + dy * dy
    if (len2 < 1e-6f) return (p - a).getDistance()
    val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / len2).coerceIn(0f, 1f)
    return hypot(p.x - (a.x + dx * t), p.y - (a.y + dy * t))
  }

  /** Resamples a polyline to [count] evenly spaced points. */
  private fun resample(pts: List<Offset>, count: Int): List<Offset> {
    if (pts.size < 2) return pts
    val total = length(pts)
    if (total <= 0f) return pts
    val step = total / count
    val out = ArrayList<Offset>(count + 1)
    out.add(pts.first())
    var carried = 0f
    for (i in 1 until pts.size) {
      var a = pts[i - 1]
      val b = pts[i]
      var seg = (b - a).getDistance()
      while (carried + seg >= step && seg > 0f) {
        val t = (step - carried) / seg
        a = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
        out.add(a)
        seg = (b - a).getDistance()
        carried = 0f
      }
      carried += seg
    }
    return out
  }
}
