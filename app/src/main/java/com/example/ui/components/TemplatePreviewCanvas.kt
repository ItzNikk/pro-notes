package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import com.example.model.PaperColorTheme
import com.example.model.PaperTemplate

/**
 * Shared, high-precision renderer for all 11 built-in and custom paper templates.
 * Used both in the full-size Editor page canvas and scaled down inside the Template Chooser
 * and Page Thumbnail cards.
 */
fun DrawScope.drawPaperTemplateLines(
  template: PaperTemplate,
  paperColorTheme: PaperColorTheme,
  scaleFactor: Float = 1f,
) {
  val w = size.width
  val h = size.height
  val isNightPaper = paperColorTheme == PaperColorTheme.NIGHT_DARK
  val lineColor =
    if (isNightPaper) Color(0xFF2D3545) else Color(0xFFDCE3ED)
  val strongLineColor =
    if (isNightPaper) Color(0xFF3E4B63) else Color(0xFFB8C6DA)
  val marginRedColor =
    if (isNightPaper) Color(0xFF8C464E) else Color(0xFFE6A1A1)

  val s = scaleFactor.coerceIn(0.18f, 2f)

  when (template) {
    PaperTemplate.BLANK, PaperTemplate.CUSTOM -> Unit

    PaperTemplate.RULED -> {
      val step = 30f * s
      val topMargin = 58f * s
      var y = topMargin
      while (y < h - 20f * s) {
        drawLine(
          color = lineColor,
          start = Offset(24f * s, y),
          end = Offset(w - 24f * s, y),
          strokeWidth = (1.3f * s).coerceAtLeast(0.8f),
        )
        y += step
      }
    }

    PaperTemplate.COLLEGE_RULED -> {
      val step = 23f * s
      val topMargin = 54f * s
      drawLine(
        color = strongLineColor,
        start = Offset(20f * s, topMargin - 8f * s),
        end = Offset(w - 20f * s, topMargin - 8f * s),
        strokeWidth = (1.6f * s).coerceAtLeast(1f),
      )
      var y = topMargin + step * 0.6f
      while (y < h - 20f * s) {
        drawLine(
          color = lineColor,
          start = Offset(20f * s, y),
          end = Offset(w - 20f * s, y),
          strokeWidth = (1.1f * s).coerceAtLeast(0.75f),
        )
        y += step
      }
      // Vertical red left margin line
      val marginX = (66f * s).coerceAtMost(w * 0.18f)
      drawLine(
        color = marginRedColor,
        start = Offset(marginX, 0f),
        end = Offset(marginX, h),
        strokeWidth = (1.4f * s).coerceAtLeast(0.9f),
      )
    }

    PaperTemplate.WIDE_RULED -> {
      val step = 38f * s
      val topMargin = 62f * s
      var y = topMargin
      while (y < h - 22f * s) {
        drawLine(
          color = lineColor,
          start = Offset(24f * s, y),
          end = Offset(w - 24f * s, y),
          strokeWidth = (1.5f * s).coerceAtLeast(0.9f),
        )
        y += step
      }
    }

    PaperTemplate.GRID -> {
      val step = 28f * s
      var x = step
      while (x < w) {
        drawLine(
          color = lineColor,
          start = Offset(x, 0f),
          end = Offset(x, h),
          strokeWidth = (1.1f * s).coerceAtLeast(0.7f),
        )
        x += step
      }
      var y = step
      while (y < h) {
        drawLine(
          color = lineColor,
          start = Offset(0f, y),
          end = Offset(w, y),
          strokeWidth = (1.1f * s).coerceAtLeast(0.7f),
        )
        y += step
      }
    }

    PaperTemplate.GRAPH -> {
      val minorStep = 14f * s
      var col = 1
      var x = minorStep
      while (x < w) {
        val isMajor = col % 5 == 0
        drawLine(
          color = if (isMajor) strongLineColor else lineColor.copy(alpha = 0.65f),
          start = Offset(x, 0f),
          end = Offset(x, h),
          strokeWidth = if (isMajor) (1.4f * s).coerceAtLeast(0.9f) else (0.8f * s).coerceAtLeast(0.5f),
        )
        x += minorStep
        col++
      }
      var row = 1
      var y = minorStep
      while (y < h) {
        val isMajor = row % 5 == 0
        drawLine(
          color = if (isMajor) strongLineColor else lineColor.copy(alpha = 0.65f),
          start = Offset(0f, y),
          end = Offset(w, y),
          strokeWidth = if (isMajor) (1.4f * s).coerceAtLeast(0.9f) else (0.8f * s).coerceAtLeast(0.5f),
        )
        y += minorStep
        row++
      }
    }

    PaperTemplate.DOTTED -> {
      val step = 26f * s
      val dotColor = if (isNightPaper) Color(0xFF434E66) else Color(0xFFB5C0D0)
      val dotRadius = (1.8f * s).coerceAtLeast(1.0f)
      var x = step
      while (x < w - step * 0.4f) {
        var y = step
        while (y < h - step * 0.4f) {
          drawCircle(
            color = dotColor,
            radius = dotRadius,
            center = Offset(x, y),
          )
          y += step
        }
        x += step
      }
    }

    PaperTemplate.CORNELL -> {
      val topHeaderY = 66f * s
      val summaryTopY = h - 96f * s
      val cueColumnX = w * 0.28f
      val step = 28f * s

      // Header line
      drawLine(
        color = strongLineColor,
        start = Offset(20f * s, topHeaderY),
        end = Offset(w - 20f * s, topHeaderY),
        strokeWidth = (1.8f * s).coerceAtLeast(1f),
      )
      // Ruled notes lines on the right side
      var y = topHeaderY + step
      while (y < summaryTopY - 6f * s) {
        drawLine(
          color = lineColor,
          start = Offset(cueColumnX + 8f * s, y),
          end = Offset(w - 20f * s, y),
          strokeWidth = (1.2f * s).coerceAtLeast(0.75f),
        )
        y += step
      }
      // Cue column vertical divider
      drawLine(
        color = marginRedColor,
        start = Offset(cueColumnX, topHeaderY),
        end = Offset(cueColumnX, summaryTopY),
        strokeWidth = (1.7f * s).coerceAtLeast(1f),
      )
      // Summary box horizontal divider
      drawLine(
        color = marginRedColor,
        start = Offset(20f * s, summaryTopY),
        end = Offset(w - 20f * s, summaryTopY),
        strokeWidth = (1.7f * s).coerceAtLeast(1f),
      )
    }

    PaperTemplate.PLANNER -> {
      val topHeaderY = 64f * s
      val midX = w * 0.50f
      val step = 30f * s
      val boxSize = 11f * s

      drawLine(
        color = strongLineColor,
        start = Offset(22f * s, topHeaderY),
        end = Offset(w - 22f * s, topHeaderY),
        strokeWidth = (1.8f * s).coerceAtLeast(1f),
      )
      drawLine(
        color = strongLineColor,
        start = Offset(midX, topHeaderY + 10f * s),
        end = Offset(midX, h - 24f * s),
        strokeWidth = (1.3f * s).coerceAtLeast(0.85f),
      )
      var y = topHeaderY + step
      while (y < h - 24f * s) {
        // Left schedule column line
        drawLine(
          color = lineColor,
          start = Offset(22f * s, y),
          end = Offset(midX - 10f * s, y),
          strokeWidth = (1.1f * s).coerceAtLeast(0.75f),
        )
        // Right checklist column line + checkbox square
        drawRect(
          color = strongLineColor,
          topLeft = Offset(midX + 12f * s, y - boxSize - 3f * s),
          size = Size(boxSize, boxSize),
          style = Stroke(width = (1.2f * s).coerceAtLeast(0.8f)),
        )
        drawLine(
          color = lineColor,
          start = Offset(midX + 12f * s + boxSize + 6f * s, y),
          end = Offset(w - 22f * s, y),
          strokeWidth = (1.1f * s).coerceAtLeast(0.75f),
        )
        y += step
      }
    }

    PaperTemplate.MUSIC -> {
      val lineGap = 7.5f * s
      val staffStep = 68f * s
      var staffTop = 64f * s
      while (staffTop + 4 * lineGap < h - 28f * s) {
        for (i in 0 until 5) {
          val y = staffTop + i * lineGap
          drawLine(
            color = strongLineColor,
            start = Offset(26f * s, y),
            end = Offset(w - 26f * s, y),
            strokeWidth = (1.1f * s).coerceAtLeast(0.75f),
          )
        }
        // Left & right barlines
        drawLine(
          color = strongLineColor,
          start = Offset(26f * s, staffTop),
          end = Offset(26f * s, staffTop + 4 * lineGap),
          strokeWidth = (1.5f * s).coerceAtLeast(0.9f),
        )
        drawLine(
          color = strongLineColor,
          start = Offset(w - 26f * s, staffTop),
          end = Offset(w - 26f * s, staffTop + 4 * lineGap),
          strokeWidth = (1.5f * s).coerceAtLeast(0.9f),
        )
        staffTop += staffStep
      }
    }
  }
}

@Composable
fun PaperTemplatePreviewThumbnail(
  template: PaperTemplate,
  paperColorTheme: PaperColorTheme,
  customBitmap: Bitmap? = null,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.background(paperColorTheme.color),
  ) {
    if (template == PaperTemplate.CUSTOM && customBitmap != null) {
      Image(
        bitmap = customBitmap.asImageBitmap(),
        contentDescription = template.label,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )
    } else {
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawPaperTemplateLines(
          template = template,
          paperColorTheme = paperColorTheme,
          scaleFactor = (size.width / 320f).coerceIn(0.24f, 0.85f),
        )
      }
    }
  }
}

@Composable
fun TemplatePreviewCanvas(
  template: PaperTemplate,
  paperColorTheme: PaperColorTheme = PaperColorTheme.PURE_WHITE,
  customBitmap: Bitmap? = null,
  modifier: Modifier = Modifier,
) {
  PaperTemplatePreviewThumbnail(
    template = template,
    paperColorTheme = paperColorTheme,
    customBitmap = customBitmap,
    modifier = modifier,
  )
}

