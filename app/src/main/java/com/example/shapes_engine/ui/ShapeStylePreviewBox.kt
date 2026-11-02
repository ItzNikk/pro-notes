package shapes_engine.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import shapes_engine.model.ShapeStyle
import shapes_engine.model.ShapeType

/**
 * Preview widget rendering a representative closed geometric shape (circle, rounded rectangle,
 * triangle, or heart) with the active [ShapeStyle] applied.
 * Shows stroke color, stroke width, and interior fill with the live opacity.
 */
@Composable
fun ShapeStylePreviewBox(
    style: ShapeStyle,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    previewShape: ShapeType = ShapeType.CIRCLE,
    backgroundColor: Color = Color(0xFF1E2024),
    borderColor: Color = Color(0xFF333842)
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeColor = Color(style.strokeColorArgb)
            val fillColor = if (style.fillColorArgb != null) {
                Color(style.fillColorArgb).copy(alpha = style.fillOpacity)
            } else {
                strokeColor.copy(alpha = style.fillOpacity)
            }

            val strokeWidthPx = (style.strokeWidth * density).coerceIn(1f, 16f)
            val strokeStyle = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )

            val padding = strokeWidthPx + 10f
            val drawWidth = (this.size.width - padding * 2).coerceAtLeast(10f)
            val drawHeight = (this.size.height - padding * 2).coerceAtLeast(10f)
            val topLeft = Offset(padding, padding)

            when (previewShape) {
                ShapeType.RECTANGLE, ShapeType.SQUARE -> {
                    val rectSize = Size(drawWidth, drawHeight)
                    val cornerRadius = CornerRadius(12f, 12f)
                    if (style.fillEnabled) {
                        drawRoundRect(
                            color = fillColor,
                            topLeft = topLeft,
                            size = rectSize,
                            cornerRadius = cornerRadius,
                            style = Fill
                        )
                    }
                    drawRoundRect(
                        color = strokeColor,
                        topLeft = topLeft,
                        size = rectSize,
                        cornerRadius = cornerRadius,
                        style = strokeStyle
                    )
                }

                ShapeType.TRIANGLE -> {
                    val p1 = Offset(this.size.width / 2f, padding)
                    val p2 = Offset(padding, this.size.height - padding)
                    val p3 = Offset(this.size.width - padding, this.size.height - padding)
                    val path = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                        close()
                    }
                    if (style.fillEnabled) {
                        drawPath(path, fillColor, style = Fill)
                    }
                    drawPath(path, strokeColor, style = strokeStyle)
                }

                ShapeType.HEART -> {
                    val cx = this.size.width / 2f
                    val top = padding
                    val bottom = this.size.height - padding
                    val halfW = drawWidth / 2f
                    val path = Path().apply {
                        moveTo(cx, top + drawHeight * 0.3f)
                        cubicTo(
                            cx - halfW * 0.9f, top - drawHeight * 0.1f,
                            cx - halfW * 1.1f, top + drawHeight * 0.6f,
                            cx, bottom
                        )
                        cubicTo(
                            cx + halfW * 1.1f, top + drawHeight * 0.6f,
                            cx + halfW * 0.9f, top - drawHeight * 0.1f,
                            cx, top + drawHeight * 0.3f
                        )
                        close()
                    }
                    if (style.fillEnabled) {
                        drawPath(path, fillColor, style = Fill)
                    }
                    drawPath(path, strokeColor, style = strokeStyle)
                }

                else -> {
                    // Default CIRCLE / ELLIPSE
                    val radius = (minOf(drawWidth, drawHeight) / 2f).coerceAtLeast(4f)
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    if (style.fillEnabled) {
                        drawCircle(
                            color = fillColor,
                            radius = radius,
                            center = center,
                            style = Fill
                        )
                    }
                    drawCircle(
                        color = strokeColor,
                        radius = radius,
                        center = center,
                        style = strokeStyle
                    )
                }
            }
        }
    }
}
