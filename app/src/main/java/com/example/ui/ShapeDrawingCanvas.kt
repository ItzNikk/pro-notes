package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ShapeSnapper
import kotlin.math.hypot
import kotlinx.coroutines.withTimeoutOrNull

enum class SnapMode {
  /** Shape replaces the stroke the moment you lift the pen. */
  ON_LIFT,

  /** Shape replaces the stroke when you pause at the end of it (GoodNotes style). */
  HOLD,

  /** Never snap. */
  OFF,
}

/**
 * Draw normally; the stroke is converted into a clean shape automatically.
 * Strokes smaller than [minSnapSizePx] are never converted, so handwriting ("o", "l", "-")
 * is left alone.
 */
@Composable
fun ShapeDrawingCanvas(
  modifier: Modifier = Modifier,
  snapMode: SnapMode = SnapMode.ON_LIFT,
  holdMillis: Long = 500L,
  minSnapSizePx: Float = 80f,
  inkColor: Color = Color.Black,
  strokeWidth: Dp = 3.dp,
) {
  val strokes = remember { mutableStateListOf<List<Offset>>() }
  val live = remember { mutableStateListOf<Offset>() }

  fun bigEnough(pts: List<Offset>): Boolean {
    if (pts.size < 3) return false
    val w = pts.maxOf { it.x } - pts.minOf { it.x }
    val h = pts.maxOf { it.y } - pts.minOf { it.y }
    return hypot(w, h) >= minSnapSizePx
  }

  fun finish(pts: List<Offset>) {
    val snapped = if (snapMode != SnapMode.OFF && bigEnough(pts)) ShapeSnapper.snap(pts) else null
    strokes.add(snapped ?: pts.toList())
    live.clear()
  }

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(snapMode, holdMillis, minSnapSizePx) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          down.consume()
          live.clear()
          live.add(down.position)

          var anchor = down.position
          var anchorTime = System.currentTimeMillis()

          while (true) {
            val event = if (snapMode == SnapMode.HOLD && bigEnough(live)) {
              val remaining = (holdMillis - (System.currentTimeMillis() - anchorTime)).coerceAtLeast(1L)
              withTimeoutOrNull(remaining) { awaitPointerEvent(PointerEventPass.Main) }
            } else {
              awaitPointerEvent(PointerEventPass.Main)
            }

            if (event == null) {
              // Pen held still: snap now, then swallow the rest of the gesture until lift
              finish(live.toList())
              do {
                val e = awaitPointerEvent(PointerEventPass.Main)
                e.changes.forEach { it.consume() }
              } while (e.changes.any { it.pressed })
              break
            }

            val change = event.changes.firstOrNull() ?: break
            if (!change.pressed) {
              change.consume()
              finish(live.toList())
              break
            }
            change.consume()
            live.add(change.position)
            if ((change.position - anchor).getDistance() > 8f) {
              anchor = change.position
              anchorTime = System.currentTimeMillis()
            }
          }
        }
      },
  ) {
    val style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun draw(pts: List<Offset>) {
      if (pts.size < 2) return
      val path = Path().apply {
        moveTo(pts.first().x, pts.first().y)
        for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
      }
      drawPath(path, inkColor, style = style)
    }
    strokes.forEach { draw(it) }
    draw(live)
  }
}
