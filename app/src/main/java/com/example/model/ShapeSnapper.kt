package com.example.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Turns a rough hand-drawn stroke into a clean one. Returns null when the stroke isn't a
 * recognisable shape (handwriting, scribbles, curves) so the caller keeps the original ink.
 */
object ShapeSnapper {
  private const val ELLIPSE_POINTS = 72
  private const val AXIS_SNAP_DEG = 4.0

  fun snap(raw: List<Offset>): List<Offset>? {
    if (raw.size < 3) return null
    val r = ShapeRecognizer.analyze(raw)

    val minX = raw.minOf { it.x }
    val maxX = raw.maxOf { it.x }
    val minY = raw.minOf { it.y }
    val maxY = raw.maxOf { it.y }
    val cx = (minX + maxX) / 2f
    val cy = (minY + maxY) / 2f
    val w = maxX - minX
    val h = maxY - minY

    return when (r.kind) {
      ShapeKind.LINE -> if (r.vertices.size >= 2) line(r.vertices.first(), r.vertices.last()) else null
      ShapeKind.ARROW -> if (r.vertices.size >= 2) arrow(r.vertices[0], r.vertices[1]) else null
      ShapeKind.CIRCLE -> {
        val rad = (w + h) / 4f
        ellipse(cx, cy, rad, rad)
      }
      ShapeKind.OVAL -> ellipse(cx, cy, w / 2f, h / 2f)
      ShapeKind.RECTANGLE ->
        closed(listOf(Offset(minX, minY), Offset(maxX, minY), Offset(maxX, maxY), Offset(minX, maxY)))
      ShapeKind.DIAMOND ->
        closed(listOf(Offset(cx, minY), Offset(maxX, cy), Offset(cx, maxY), Offset(minX, cy)))
      ShapeKind.TRIANGLE, ShapeKind.PENTAGON, ShapeKind.HEXAGON, ShapeKind.STAR ->
        if (r.vertices.size >= 3) closed(r.vertices) else null
      else -> null
    }
  }

  /** Straight line, nudged to perfectly horizontal or vertical when it's nearly so. */
  private fun line(a: Offset, b: Offset): List<Offset> {
    val deg = Math.toDegrees(atan2(abs(b.y - a.y).toDouble(), abs(b.x - a.x).toDouble()))
    return when {
      deg < AXIS_SNAP_DEG -> {
        val y = (a.y + b.y) / 2f
        listOf(Offset(a.x, y), Offset(b.x, y))
      }
      deg > 90.0 - AXIS_SNAP_DEG -> {
        val x = (a.x + b.x) / 2f
        listOf(Offset(x, a.y), Offset(x, b.y))
      }
      else -> listOf(a, b)
    }
  }

  /** Shaft plus two barbs, drawn as one continuous polyline: tail, tip, barb, tip, barb. */
  private fun arrow(tail: Offset, tip: Offset): List<Offset> {
    val dx = tail.x - tip.x
    val dy = tail.y - tip.y
    val shaft = kotlin.math.hypot(dx, dy).coerceAtLeast(1f)
    val head = (shaft * 0.22f).coerceIn(14f, 90f)
    val base = atan2(dy, dx)
    val spread = 28.0 * PI / 180.0
    fun barb(sign: Int): Offset {
      val a = base + sign * spread
      return Offset(tip.x + head * cos(a).toFloat(), tip.y + head * sin(a).toFloat())
    }
    return listOf(tail, tip, barb(1), tip, barb(-1))
  }

  private fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float): List<Offset> =
    List(ELLIPSE_POINTS + 1) { i ->
      val t = 2.0 * PI * i / ELLIPSE_POINTS
      Offset(cx + rx * cos(t).toFloat(), cy + ry * sin(t).toFloat())
    }

  private fun closed(vertices: List<Offset>): List<Offset> = vertices + vertices.first()
}
