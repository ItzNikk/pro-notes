package com.example.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.acos
import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * Recognises hand-drawn shapes. Works on the stroke resampled to even spacing (so drawing speed
 * doesn't matter) and counts real corners from the turning angle along the outline. Circles turn
 * evenly everywhere; polygons turn sharply at a few places, which is what tells them apart.
 */
object ShapeRecognizer {
  private const val SAMPLES = 96
  private const val WINDOW = 5
  private const val CORNER_MIN_TURN_DEG = 48.0

  fun recognize(raw: List<Offset>): ShapeKind {
    if (raw.size < 3) return ShapeKind.LINE
    val minX = raw.minOf { it.x }
    val maxX = raw.maxOf { it.x }
    val minY = raw.minOf { it.y }
    val maxY = raw.maxOf { it.y }
    val diag = hypot(maxX - minX, maxY - minY).coerceAtLeast(1f)
    val pathLen = length(raw)
    val chord = (raw.last() - raw.first()).getDistance()

    val loop = closedLoopPolygon(raw)
    val closed = loop != null || (chord <= diag * 0.2f && pathLen >= diag * 1.5f)
    return if (closed) {
      classifyClosed(resample(loop ?: raw, SAMPLES), minX, maxX, minY, maxY)
    } else {
      classifyOpen(raw, diag, pathLen, chord)
    }
  }

  private fun classifyOpen(raw: List<Offset>, diag: Float, pathLen: Float, chord: Float): ShapeKind {
    if (chord < 1f) return ShapeKind.CURVE
    val maxDeviation = raw.maxOf { distanceToLine(it, raw.first(), raw.last()) }
    if (maxDeviation <= chord * 0.05f) return ShapeKind.LINE

    if (chord > diag * 0.38f && pathLen < diag * 2.2f) {
      val pts = resample(raw, SAMPLES)
      val idx = (pts.size * 0.78f).toInt().coerceIn(1, pts.lastIndex)
      val shaftDirect = (pts[idx] - pts.first()).getDistance().coerceAtLeast(1f)
      val shaftPath = length(pts.subList(0, idx + 1))
      val tail = length(pts) - shaftPath
      if (shaftPath <= shaftDirect * 1.15f && tail in 12f..(diag * 0.48f)) return ShapeKind.ARROW
    }
    return ShapeKind.CURVE
  }

  private fun classifyClosed(
    ring: List<Offset>,
    minX: Float,
    maxX: Float,
    minY: Float,
    maxY: Float,
  ): ShapeKind {
    val n = ring.size
    val w = maxX - minX
    val h = maxY - minY
    val aspect = w / h.coerceAtLeast(1f)
    val ellipse = if (aspect in 0.8f..1.25f) ShapeKind.CIRCLE else ShapeKind.OVAL
    if (n < 12) return ellipse

    // Turning angle at every outline sample
    val turn = DoubleArray(n) { i ->
      val a = ring[(i - WINDOW + n) % n]
      val b = ring[i]
      val c = ring[(i + WINDOW) % n]
      val v1 = b - a
      val v2 = c - b
      val mag = (v1.getDistance() * v2.getDistance()).coerceAtLeast(0.0001f)
      val cos = ((v1.x * v2.x + v1.y * v2.y) / mag).coerceIn(-1f, 1f)
      Math.toDegrees(acos(cos.toDouble()))
    }

    // Strongest turns first, keeping them at least n/12 samples apart
    val minSeparation = n / 12
    val corners = mutableListOf<Int>()
    for (i in turn.indices.sortedByDescending { turn[it] }) {
      if (turn[i] < CORNER_MIN_TURN_DEG) break
      val farFromOthers =
        corners.all { c ->
          val d = kotlin.math.abs(c - i)
          minOf(d, n - d) >= minSeparation
        }
      if (farFromOthers) corners.add(i)
    }
    val k = corners.size

    // How far the outline strays from a perfect ellipse (high for stars, low for round shapes)
    val cx = (minX + maxX) / 2f
    val cy = (minY + maxY) / 2f
    val rx = (w / 2f).coerceAtLeast(12f)
    val ry = (h / 2f).coerceAtLeast(12f)
    val radii = ring.map { hypot((it.x - cx) / rx, (it.y - cy) / ry) }
    val mean = radii.average().toFloat().coerceAtLeast(0.01f)
    val stdR = sqrt(radii.map { (it - mean) * (it - mean) }.average().toFloat()) / mean

    return when {
      k <= 2 -> ellipse
      k == 3 -> if (stdR < 0.05f) ellipse else ShapeKind.TRIANGLE
      k == 4 -> {
        if (stdR < 0.05f) {
          ellipse
        } else {
          val mids = listOf(Offset(cx, minY), Offset(maxX, cy), Offset(cx, maxY), Offset(minX, cy))
          val box = listOf(Offset(minX, minY), Offset(maxX, minY), Offset(maxX, maxY), Offset(minX, maxY))
          val toMids = corners.sumOf { i -> mids.minOf { (ring[i] - it).getDistance() }.toDouble() }
          val toBox = corners.sumOf { i -> box.minOf { (ring[i] - it).getDistance() }.toDouble() }
          if (toMids < toBox * 0.85) ShapeKind.DIAMOND else ShapeKind.RECTANGLE
        }
      }
      k == 5 -> ShapeKind.PENTAGON
      k == 6 -> ShapeKind.HEXAGON
      k >= 8 && stdR >= 0.17f -> ShapeKind.STAR
      else -> ellipse
    }
  }

  private fun length(pts: List<Offset>): Float {
    var total = 0f
    for (i in 1 until pts.size) total += (pts[i] - pts[i - 1]).getDistance()
    return total
  }

  private fun distanceToLine(p: Offset, a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val len = hypot(dx, dy).coerceAtLeast(0.0001f)
    return kotlin.math.abs((p.x - a.x) * dy - (p.y - a.y) * dx) / len
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
