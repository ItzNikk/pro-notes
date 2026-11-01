package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.PrimaryTool
import com.example.model.ShapeKind
import com.example.model.StrokePattern
import com.example.model.ToolbarDockPosition
import com.example.model.WritingSubTool
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders the exact 1:1 official Goodnotes 6 Main Toolbar icons from IMG_0531 ("Toolbar Customization")
 * and IMG_0526.
 */
@Composable
fun PrimaryToolCustomIcon(
  tool: PrimaryTool,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.082f).coerceAtLeast(2.1f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (tool) {
      PrimaryTool.LASSO -> {
        // 1:1 Dashed Lasso Loop with bottom-left crossed tail (IMG_0526 & IMG_0530)
        val lassoPath =
          Path().apply {
            moveTo(w * 0.24f, h * 0.76f)
            cubicTo(w * 0.08f, h * 0.45f, w * 0.24f, h * 0.16f, w * 0.56f, h * 0.18f)
            cubicTo(w * 0.88f, h * 0.20f, w * 0.90f, h * 0.62f, w * 0.58f, h * 0.68f)
            cubicTo(w * 0.36f, h * 0.72f, w * 0.22f, h * 0.62f, w * 0.16f, h * 0.84f)
          }
        drawPath(
          path = lassoPath,
          color = tint,
          style =
            Stroke(
              width = strokeW,
              cap = StrokeCap.Round,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(w * 0.13f, w * 0.10f), 0f),
            ),
        )
        drawLine(
          color = tint,
          start = Offset(w * 0.16f, h * 0.76f),
          end = Offset(w * 0.36f, h * 0.84f),
          strokeWidth = strokeW,
          cap = StrokeCap.Round,
        )
      }

      PrimaryTool.WRITING_TOOLS -> {
        // 1:1 Official Goodnotes "Writing Tools" Icon (IMG_0531 Row 1):
        // Angled pen nib pointing to bottom-left + signature cursive loop curl ('e' loop) at bottom
        val penBody =
          Path().apply {
            moveTo(w * 0.20f, h * 0.68f)
            lineTo(w * 0.30f, h * 0.38f)
            lineTo(w * 0.68f, h * 0.12f)
            lineTo(w * 0.84f, h * 0.28f)
            lineTo(w * 0.52f, h * 0.60f)
            close()
          }
        drawPath(path = penBody, color = tint, style = stroke)
        // Pen collar line
        drawLine(
          color = tint,
          start = Offset(w * 0.33f, h * 0.40f),
          end = Offset(w * 0.51f, h * 0.58f),
          strokeWidth = strokeW * 0.85f,
        )
        // Signature cursive loop curl at bottom-left -> bottom-right (exact from IMG_0531)
        val loopPath =
          Path().apply {
            moveTo(w * 0.20f, h * 0.70f)
            cubicTo(w * 0.14f, h * 0.86f, w * 0.52f, h * 0.86f, w * 0.48f, h * 0.72f)
            cubicTo(w * 0.44f, h * 0.62f, w * 0.28f, h * 0.74f, w * 0.40f, h * 0.84f)
            quadraticTo(w * 0.58f, h * 0.90f, w * 0.84f, h * 0.82f)
          }
        drawPath(path = loopPath, color = tint, style = stroke)
      }

      PrimaryTool.ERASER -> {
        // 1:1 Official Goodnotes "Eraser" Icon (IMG_0531 Row 2):
        // Angled two-part rounded rectangle eraser + short baseline to the right
        rotate(degrees = -42f, pivot = Offset(w * 0.48f, h * 0.48f)) {
          drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.22f, h * 0.32f),
            size = Size(w * 0.52f, h * 0.32f),
            cornerRadius = CornerRadius(w * 0.07f, w * 0.07f),
            style = stroke,
          )
          drawLine(
            color = tint,
            start = Offset(w * 0.45f, h * 0.32f),
            end = Offset(w * 0.45f, h * 0.64f),
            strokeWidth = strokeW,
          )
        }
        drawLine(
          color = tint,
          start = Offset(w * 0.44f, h * 0.82f),
          end = Offset(w * 0.82f, h * 0.82f),
          strokeWidth = strokeW,
          cap = StrokeCap.Round,
        )
      }

      PrimaryTool.SHAPE -> {
        // 2x2 Grid: Top-left Triangle, Top-right Curved Arrow, Bottom-left Circle, Bottom-right Square
        // 1) Top-left: Triangle
        val tri =
          Path().apply {
            moveTo(w * 0.30f, h * 0.15f)
            lineTo(w * 0.45f, h * 0.43f)
            lineTo(w * 0.15f, h * 0.43f)
            close()
          }
        drawPath(path = tri, color = tint, style = stroke)

        // 2) Top-right: Curved Arrow (arcs from left to bottom-right with arrowhead)
        val arcArrow =
          Path().apply {
            moveTo(w * 0.56f, h * 0.24f)
            quadraticTo(w * 0.78f, h * 0.16f, w * 0.76f, h * 0.42f)
          }
        drawPath(path = arcArrow, color = tint, style = stroke)
        // Arrowhead at (0.76, 0.42)
        drawLine(
          color = tint,
          start = Offset(w * 0.67f, h * 0.35f),
          end = Offset(w * 0.76f, h * 0.43f),
          strokeWidth = strokeW * 0.9f,
          cap = StrokeCap.Round,
        )
        drawLine(
          color = tint,
          start = Offset(w * 0.84f, h * 0.35f),
          end = Offset(w * 0.76f, h * 0.43f),
          strokeWidth = strokeW * 0.9f,
          cap = StrokeCap.Round,
        )

        // 3) Bottom-left: Circle
        drawCircle(
          color = tint,
          radius = w * 0.14f,
          center = Offset(w * 0.30f, h * 0.70f),
          style = stroke,
        )

        // 4) Bottom-right: Square
        drawRoundRect(
          color = tint,
          topLeft = Offset(w * 0.56f, h * 0.56f),
          size = Size(w * 0.27f, h * 0.27f),
          cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
          style = stroke,
        )
      }

      PrimaryTool.LASER -> {
        // 1:1 Official Goodnotes "Laser Pointer" Icon (IMG_0531 Row 8):
        // Angled hollow wand rod from bottom-left to center + starburst of 8 rays around top-right tip
        rotate(degrees = -45f, pivot = Offset(w * 0.38f, h * 0.62f)) {
          drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.14f, h * 0.55f),
            size = Size(w * 0.48f, h * 0.14f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
            style = stroke,
          )
        }
        // Starburst center at (0.68, 0.32)
        val cx = w * 0.68f
        val cy = h * 0.32f
        drawCircle(color = tint, radius = strokeW * 0.85f, center = Offset(cx, cy))
        for (i in 0 until 8) {
          val angle = Math.toRadians((i * 45).toDouble())
          val r1 = w * 0.12f
          val r2 = w * 0.20f
          drawLine(
            color = tint,
            start = Offset(cx + (r1 * cos(angle)).toFloat(), cy + (r1 * sin(angle)).toFloat()),
            end = Offset(cx + (r2 * cos(angle)).toFloat(), cy + (r2 * sin(angle)).toFloat()),
            strokeWidth = strokeW * 0.8f,
            cap = StrokeCap.Round,
          )
        }
      }
      else -> {
        // Fallback for TEXT, IMAGE, ELEMENTS, STICKY_NOTE
      }
    }
  }
}

/**
 * Renders the exact 1:1 official Goodnotes 6 Writing Sub-Tools icons from IMG_0531
 * ("Writing Tools Customization" & Floating Sub-Toolbar in IMG_0526):
 * 1. Pen
 * 2. Pencil
 * 3. Highlighter
 * 4. Tape
 * 5. Draw Shape
 */
@Composable
fun WritingSubToolCustomIcon(
  subTool: WritingSubTool,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.085f).coerceAtLeast(2.0f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (subTool) {
      WritingSubTool.PEN -> {
        // 1:1 Official Goodnotes "Pen" Icon (IMG_0531 Right Panel Row 1):
        // Sharp cone nib pointing to bottom-left + collar + open shaft lines going up-right
        val cone =
          Path().apply {
            moveTo(w * 0.18f, h * 0.82f)
            lineTo(w * 0.34f, h * 0.44f)
            lineTo(w * 0.56f, h * 0.66f)
            close()
          }
        drawPath(path = cone, color = tint, style = stroke)
        // Shaft lines extending to top-right
        drawLine(
          color = tint,
          start = Offset(w * 0.34f, h * 0.44f),
          end = Offset(w * 0.66f, h * 0.12f),
          strokeWidth = strokeW,
          cap = StrokeCap.Round,
        )
        drawLine(
          color = tint,
          start = Offset(w * 0.56f, h * 0.66f),
          end = Offset(w * 0.88f, h * 0.34f),
          strokeWidth = strokeW,
          cap = StrokeCap.Round,
        )
      }

      WritingSubTool.PENCIL -> {
        // 1:1 Official Goodnotes "Pencil" Icon (IMG_0531 Right Panel Row 2):
        // Angled pencil with scalloped zig-zag wood collar and dark lead tip
        val cone =
          Path().apply {
            moveTo(w * 0.18f, h * 0.82f)
            lineTo(w * 0.32f, h * 0.44f)
            lineTo(w * 0.56f, h * 0.68f)
            close()
          }
        drawPath(path = cone, color = tint, style = stroke)
        // Scalloped collar wave between (0.32, 0.44) and (0.56, 0.68)
        val scallop =
          Path().apply {
            moveTo(w * 0.32f, h * 0.44f)
            lineTo(w * 0.42f, h * 0.48f)
            lineTo(w * 0.46f, h * 0.58f)
            lineTo(w * 0.56f, h * 0.68f)
          }
        drawPath(path = scallop, color = tint, style = stroke)
        // Parallel shaft lines + center ridge line
        drawLine(
          color = tint,
          start = Offset(w * 0.32f, h * 0.44f),
          end = Offset(w * 0.64f, h * 0.12f),
          strokeWidth = strokeW,
          cap = StrokeCap.Round,
        )
        drawLine(
          color = tint,
          start = Offset(w * 0.44f, h * 0.56f),
          end = Offset(w * 0.76f, h * 0.24f),
          strokeWidth = strokeW * 0.8f,
          cap = StrokeCap.Round,
        )
        drawLine(
          color = tint,
          start = Offset(w * 0.56f, h * 0.68f),
          end = Offset(w * 0.88f, h * 0.36f),
          strokeWidth = strokeW,
          cap = StrokeCap.Round,
        )
      }

      WritingSubTool.HIGHLIGHTER -> {
        // 1:1 Official Goodnotes "Highlighter" Icon (IMG_0531 Right Panel Row 3):
        // Flared marker collar + angled chisel nib sticking out at bottom-left
        val nib =
          Path().apply {
            moveTo(w * 0.18f, h * 0.78f)
            lineTo(w * 0.28f, h * 0.58f)
            lineTo(w * 0.42f, h * 0.72f)
            lineTo(w * 0.26f, h * 0.84f)
            close()
          }
        drawPath(path = nib, color = tint, style = stroke)
        // Flared marker body collar
        val body =
          Path().apply {
            moveTo(w * 0.24f, h * 0.54f)
            lineTo(w * 0.46f, h * 0.76f)
            lineTo(w * 0.84f, h * 0.38f)
            lineTo(w * 0.62f, h * 0.16f)
            close()
          }
        drawPath(path = body, color = tint, style = stroke)
      }

      WritingSubTool.TAPE -> {
        // 1:1 Official Goodnotes "Tape" Icon (IMG_0531 Right Panel Row 4):
        // Circular tape roll with concentric inner core + horizontal tape strip at bottom-left
        val cx = w * 0.56f
        val cy = h * 0.48f
        drawCircle(
          color = tint,
          radius = w * 0.28f,
          center = Offset(cx, cy),
          style = stroke,
        )
        drawCircle(
          color = tint,
          radius = w * 0.11f,
          center = Offset(cx, cy),
          style = stroke,
        )
        // Tape strip extending to bottom-left from bottom of roll
        val strip =
          Path().apply {
            moveTo(cx, cy + w * 0.28f)
            lineTo(w * 0.16f, cy + w * 0.28f)
            lineTo(w * 0.22f, cy + w * 0.12f)
            lineTo(cx - w * 0.24f, cy + w * 0.12f)
          }
        drawPath(path = strip, color = tint, style = stroke)
      }

      WritingSubTool.DRAW_SHAPE -> {
        // 1:1 Official Goodnotes "Draw Shape" Icon (IMG_0531 Right Panel Row 5):
        // Overlapping Circle (top-left) + Triangle with vertex dots (bottom-right)
        drawCircle(
          color = tint,
          radius = w * 0.23f,
          center = Offset(w * 0.38f, h * 0.38f),
          style = stroke,
        )
        val v1 = Offset(w * 0.62f, h * 0.34f)
        val v2 = Offset(w * 0.84f, h * 0.78f)
        val v3 = Offset(w * 0.38f, h * 0.78f)
        val tri =
          Path().apply {
            moveTo(v1.x, v1.y)
            lineTo(v2.x, v2.y)
            lineTo(v3.x, v3.y)
            close()
          }
        drawPath(path = tri, color = tint, style = stroke)
        // Small vertex handle dots on the triangle
        val dotR = strokeW * 0.75f
        drawCircle(color = tint, radius = dotR, center = v1)
        drawCircle(color = tint, radius = dotR, center = v2)
        drawCircle(color = tint, radius = dotR, center = v3)
      }
    }
  }
}

/**
 * Custom 1:1 Left Sidebar / Page Grid View Icon from the Navy Toolbar (Screenshot 1).
 */
@Composable
fun SidebarThumbnailsIcon(
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.085f).coerceAtLeast(2.2f)
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.12f, h * 0.16f),
      size = Size(w * 0.76f, h * 0.68f),
      cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
      style = Stroke(width = strokeW),
    )
    drawLine(
      color = tint,
      start = Offset(w * 0.38f, h * 0.16f),
      end = Offset(w * 0.38f, h * 0.84f),
      strokeWidth = strokeW,
    )
  }
}

/**
 * Custom 1:1 Bookmark Page Outline Icon from the Navy Toolbar (Screenshot 1).
 */
@Composable
fun PageBookmarkIcon(
  tint: Color,
  isBookmarked: Boolean,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.085f).coerceAtLeast(2.2f)
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.18f, h * 0.12f),
      size = Size(w * 0.64f, h * 0.76f),
      cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
      style = Stroke(width = strokeW),
    )
    val ribbon =
      Path().apply {
        moveTo(w * 0.36f, h * 0.12f)
        lineTo(w * 0.64f, h * 0.12f)
        lineTo(w * 0.64f, h * 0.52f)
        lineTo(w * 0.50f, h * 0.40f)
        lineTo(w * 0.36f, h * 0.52f)
        close()
      }
    if (isBookmarked) {
      drawPath(path = ribbon, color = tint)
    } else {
      drawPath(path = ribbon, color = tint, style = Stroke(width = strokeW * 0.9f))
    }
  }
}

/**
 * Custom 1:1 Add Page Icon (Document with '+' inside) from the right side of the Navy Toolbar (Screenshot 1).
 */
@Composable
fun AddPageDocumentIcon(
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.085f).coerceAtLeast(2.2f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val docPath =
      Path().apply {
        moveTo(w * 0.20f, h * 0.12f)
        lineTo(w * 0.62f, h * 0.12f)
        lineTo(w * 0.80f, h * 0.30f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.20f, h * 0.88f)
        close()
      }
    drawPath(path = docPath, color = tint, style = stroke)
    drawLine(
      color = tint,
      start = Offset(w * 0.50f, h * 0.40f),
      end = Offset(w * 0.50f, h * 0.68f),
      strokeWidth = strokeW,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = tint,
      start = Offset(w * 0.36f, h * 0.54f),
      end = Offset(w * 0.64f, h * 0.54f),
      strokeWidth = strokeW,
      cap = StrokeCap.Round,
    )
  }
}

/**
 * Custom 1:1 Share / Export Box with Upward Arrow from the Navy Toolbar (Screenshot 1).
 */
@Composable
fun ExportShareBoxIcon(
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.085f).coerceAtLeast(2.2f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val boxPath =
      Path().apply {
        moveTo(w * 0.32f, h * 0.40f)
        lineTo(w * 0.20f, h * 0.40f)
        lineTo(w * 0.20f, h * 0.84f)
        lineTo(w * 0.80f, h * 0.84f)
        lineTo(w * 0.80f, h * 0.40f)
        lineTo(w * 0.68f, h * 0.40f)
      }
    drawPath(path = boxPath, color = tint, style = stroke)

    drawLine(
      color = tint,
      start = Offset(w * 0.50f, h * 0.62f),
      end = Offset(w * 0.50f, h * 0.14f),
      strokeWidth = strokeW,
      cap = StrokeCap.Round,
    )
    val arrowHead =
      Path().apply {
        moveTo(w * 0.34f, h * 0.30f)
        lineTo(w * 0.50f, h * 0.14f)
        lineTo(w * 0.66f, h * 0.30f)
      }
    drawPath(path = arrowHead, color = tint, style = stroke)
  }
}

/**
 * Custom 1:1 More Circle '...' Icon from the Navy Toolbar (Screenshot 1).
 */
@Composable
fun MoreCircleDotsIcon(
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.085f).coerceAtLeast(2.2f)
    drawCircle(
      color = tint,
      radius = w * 0.38f,
      center = Offset(w * 0.50f, h * 0.50f),
      style = Stroke(width = strokeW),
    )
    val dotR = strokeW * 0.65f
    drawCircle(color = tint, radius = dotR, center = Offset(w * 0.34f, h * 0.50f))
    drawCircle(color = tint, radius = dotR, center = Offset(w * 0.50f, h * 0.50f))
    drawCircle(color = tint, radius = dotR, center = Offset(w * 0.66f, h * 0.50f))
  }
}

/**
 * Custom 1:1 Sub-toolbar Color Ring Icon (dotted circle on the right of the 5 color swatches in Screenshot 1).
 */
@Composable
fun DottedColorRingIcon(
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    drawCircle(
      color = tint,
      radius = w * 0.36f,
      center = Offset(w * 0.5f, h * 0.5f),
      style =
        Stroke(
          width = w * 0.11f,
          cap = StrokeCap.Round,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(w * 0.06f, w * 0.11f), 0f),
        ),
    )
  }
}

/**
 * Custom Stroke Pattern Preview Icon (Solid, Dashed, Dotted) for the Sub-Toolbar.
 */
@Composable
fun StrokePatternIcon(
  pattern: StrokePattern,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val effect =
      when (pattern) {
        StrokePattern.SOLID -> null
        StrokePattern.DASHED -> PathEffect.dashPathEffect(floatArrayOf(w * 0.22f, w * 0.14f), 0f)
        StrokePattern.DOTTED -> PathEffect.dashPathEffect(floatArrayOf(w * 0.06f, w * 0.14f), 0f)
      }
    drawLine(
      color = tint,
      start = Offset(w * 0.12f, h * 0.5f),
      end = Offset(w * 0.88f, h * 0.5f),
      strokeWidth = h * 0.16f,
      cap = StrokeCap.Round,
      pathEffect = effect,
    )
  }
}

/**
 * Custom 1:1 Goodnotes Undo Curved Arrow Icon (Screenshot 1 Top-Left Pill).
 */
@Composable
fun UndoCurvedArrowIcon(
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.095f).coerceAtLeast(2.2f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val path =
      Path().apply {
        moveTo(w * 0.24f, h * 0.42f)
        lineTo(w * 0.62f, h * 0.42f)
        cubicTo(w * 0.84f, h * 0.42f, w * 0.84f, h * 0.78f, w * 0.62f, h * 0.78f)
        lineTo(w * 0.34f, h * 0.78f)
      }
    drawPath(path = path, color = tint, style = stroke)
    val head =
      Path().apply {
        moveTo(w * 0.40f, h * 0.24f)
        lineTo(w * 0.22f, h * 0.42f)
        lineTo(w * 0.40f, h * 0.60f)
      }
    drawPath(path = head, color = tint, style = stroke)
  }
}

/**
 * Custom 1:1 Goodnotes Redo Curved Arrow Icon (Screenshot 1 Top-Left Pill).
 */
@Composable
fun RedoCurvedArrowIcon(
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.095f).coerceAtLeast(2.2f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val path =
      Path().apply {
        moveTo(w * 0.76f, h * 0.42f)
        lineTo(w * 0.38f, h * 0.42f)
        cubicTo(w * 0.16f, h * 0.42f, w * 0.16f, h * 0.78f, w * 0.38f, h * 0.78f)
        lineTo(w * 0.66f, h * 0.78f)
      }
    drawPath(path = path, color = tint, style = stroke)
    val head =
      Path().apply {
        moveTo(w * 0.60f, h * 0.24f)
        lineTo(w * 0.78f, h * 0.42f)
        lineTo(w * 0.60f, h * 0.60f)
      }
    drawPath(path = head, color = tint, style = stroke)
  }
}

/**
 * Unified Goodnotes Stroke-Style Cybersecurity Shield & Lock Icon.
 */
@Composable
fun SecurityVaultShieldIcon(
  tint: Color,
  isLocked: Boolean = false,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.088f).coerceAtLeast(2.1f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val shield =
      Path().apply {
        moveTo(w * 0.50f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.24f)
        cubicTo(w * 0.82f, h * 0.56f, w * 0.68f, h * 0.78f, w * 0.50f, h * 0.88f)
        cubicTo(w * 0.32f, h * 0.78f, w * 0.18f, h * 0.56f, w * 0.18f, h * 0.24f)
        close()
      }
    drawPath(path = shield, color = tint, style = stroke)
    if (isLocked) {
      drawRoundRect(
        color = tint,
        topLeft = Offset(w * 0.38f, h * 0.48f),
        size = Size(w * 0.24f, h * 0.20f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        style = stroke,
      )
    } else {
      val check =
        Path().apply {
          moveTo(w * 0.36f, h * 0.52f)
          lineTo(w * 0.46f, h * 0.62f)
          lineTo(w * 0.65f, h * 0.40f)
        }
      drawPath(path = check, color = tint, style = stroke)
    }
  }
}

/**
 * Unified Goodnotes Stroke-Style S Pen with Side Barrel Button Icon.
 */
@Composable
fun SPenBarrelButtonIcon(
  tint: Color,
  isButtonPressed: Boolean,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.088f).coerceAtLeast(2.0f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val penPath =
      Path().apply {
        moveTo(w * 0.16f, h * 0.84f)
        lineTo(w * 0.28f, h * 0.54f)
        lineTo(w * 0.70f, h * 0.12f)
        lineTo(w * 0.86f, h * 0.28f)
        lineTo(w * 0.44f, h * 0.70f)
        close()
      }
    drawPath(path = penPath, color = tint, style = stroke)
    // Highlighted Side Barrel Button on S Pen shaft
    drawRoundRect(
      color = if (isButtonPressed) Color(0xFFFF5252) else tint,
      topLeft = Offset(w * 0.46f, h * 0.34f),
      size = Size(w * 0.18f, h * 0.11f),
      cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
    )
  }
}

/**
 * Unified Dock Position Preview Icon (Top, Bottom, Left, Right).
 */
@Composable
fun DockPositionIcon(
  position: ToolbarDockPosition,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.085f).coerceAtLeast(1.8f)
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.12f, h * 0.14f),
      size = Size(w * 0.76f, h * 0.72f),
      cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
      style = Stroke(width = strokeW),
    )
    when (position) {
      ToolbarDockPosition.TOP ->
        drawRect(color = tint, topLeft = Offset(w * 0.12f, h * 0.14f), size = Size(w * 0.76f, h * 0.22f))
      ToolbarDockPosition.BOTTOM ->
        drawRect(color = tint, topLeft = Offset(w * 0.12f, h * 0.64f), size = Size(w * 0.76f, h * 0.22f))
      ToolbarDockPosition.LEFT ->
        drawRect(color = tint, topLeft = Offset(w * 0.12f, h * 0.14f), size = Size(w * 0.24f, h * 0.72f))
      ToolbarDockPosition.RIGHT ->
        drawRect(color = tint, topLeft = Offset(w * 0.64f, h * 0.14f), size = Size(w * 0.24f, h * 0.72f))
    }
  }
}

/**
 * Unified Custom Vector Icon for each of the 11 Goodnotes ShapeKinds.
 */
@Composable
fun ShapeKindPreviewIcon(
  kind: ShapeKind,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.095f).coerceAtLeast(2.0f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when (kind) {
      ShapeKind.LINE -> {
        drawLine(tint, Offset(w * 0.18f, h * 0.80f), Offset(w * 0.82f, h * 0.20f), strokeWidth = strokeW, cap = StrokeCap.Round)
      }
      ShapeKind.ARROW -> {
        drawLine(tint, Offset(w * 0.18f, h * 0.78f), Offset(w * 0.80f, h * 0.22f), strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.56f, h * 0.22f), Offset(w * 0.80f, h * 0.22f), strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.80f, h * 0.22f), Offset(w * 0.80f, h * 0.46f), strokeWidth = strokeW, cap = StrokeCap.Round)
      }
      ShapeKind.CURVE -> {
        val p = Path().apply {
          moveTo(w * 0.16f, h * 0.74f)
          quadraticTo(w * 0.50f, h * 0.12f, w * 0.84f, h * 0.74f)
        }
        drawPath(p, tint, style = stroke)
      }
      ShapeKind.CIRCLE -> {
        drawCircle(tint, radius = w * 0.34f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
      }
      ShapeKind.OVAL -> {
        drawOval(tint, topLeft = Offset(w * 0.12f, h * 0.26f), size = Size(w * 0.76f, h * 0.48f), style = stroke)
      }
      ShapeKind.TRIANGLE -> {
        val p = Path().apply {
          moveTo(w * 0.50f, h * 0.16f)
          lineTo(w * 0.84f, h * 0.82f)
          lineTo(w * 0.16f, h * 0.82f)
          close()
        }
        drawPath(p, tint, style = stroke)
      }
      ShapeKind.RECTANGLE -> {
        drawRoundRect(tint, topLeft = Offset(w * 0.16f, h * 0.22f), size = Size(w * 0.68f, h * 0.56f), cornerRadius = CornerRadius(4f, 4f), style = stroke)
      }
      ShapeKind.DIAMOND -> {
        val p = Path().apply {
          moveTo(w * 0.50f, h * 0.14f)
          lineTo(w * 0.86f, h * 0.50f)
          lineTo(w * 0.50f, h * 0.86f)
          lineTo(w * 0.14f, h * 0.50f)
          close()
        }
        drawPath(p, tint, style = stroke)
      }
      ShapeKind.PENTAGON -> {
        val p = Path()
        for (i in 0 until 5) {
          val a = -PI / 2.0 + i * (2.0 * PI / 5.0)
          val x = w * 0.5f + (w * 0.35f * cos(a)).toFloat()
          val y = h * 0.52f + (h * 0.35f * sin(a)).toFloat()
          if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        drawPath(p, tint, style = stroke)
      }
      ShapeKind.HEXAGON -> {
        val p = Path()
        for (i in 0 until 6) {
          val a = -PI / 2.0 + i * (2.0 * PI / 6.0)
          val x = w * 0.5f + (w * 0.35f * cos(a)).toFloat()
          val y = h * 0.50f + (h * 0.35f * sin(a)).toFloat()
          if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        drawPath(p, tint, style = stroke)
      }
      ShapeKind.STAR -> {
        val p = Path()
        for (i in 0 until 10) {
          val r = if (i % 2 == 0) w * 0.38f else w * 0.16f
          val a = -PI / 2.0 + i * (PI / 5.0)
          val x = w * 0.5f + (r * cos(a)).toFloat()
          val y = h * 0.52f + (r * sin(a)).toFloat()
          if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        drawPath(p, tint, style = stroke)
      }
    }
  }
}

