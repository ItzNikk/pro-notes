package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.toArgb
import com.example.model.NotePage
import com.example.model.NotebookDocument
import com.example.model.PaperTemplate
import com.example.model.StrokePattern
import java.security.MessageDigest
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfImportHelper {

  /**
   * Imports a multi-page PDF while strictly preserving each page's true aspect ratio
   * so text and tables are never stretched or distorted.
   */
  suspend fun importPdfFromUri(
    context: Context,
    uri: Uri,
  ): Pair<String, List<NotePage>>? =
    withContext(Dispatchers.IO) {
      try {
        val fileName = queryFileName(context, uri)?.removeSuffix(".pdf") ?: "Imported PDF"
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext null
        pfd.use { descriptor ->
          val renderer = PdfRenderer(descriptor)
          renderer.use { pdf ->
            val pageCount = pdf.pageCount.coerceIn(1, 30)
            val pages = mutableListOf<NotePage>()
            for (i in 0 until pageCount) {
              pdf.openPage(i).use { page ->
                val rawW = page.width.coerceAtLeast(1)
                val rawH = page.height.coerceAtLeast(1)
                // Uniform scale factor preserves exact page aspect ratio (zero text distortion)
                val scale = min(min(1650f / rawW.toFloat(), 2330f / rawH.toFloat()), 2.4f).coerceAtLeast(1.0f)
                val width = (rawW * scale).roundToInt().coerceAtLeast(100)
                val height = (rawH * scale).roundToInt().coerceAtLeast(100)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(
                  bitmap,
                  Rect(0, 0, width, height),
                  null,
                  PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                )
                pages.add(
                  NotePage(
                    pageNumber = i + 1,
                    pdfPageBitmap = bitmap,
                  )
                )
              }
            }
            if (pages.isEmpty()) null else fileName to pages
          }
        }
      } catch (_: Exception) {
        null
      }
    }

  suspend fun decodeImageFromUri(
    context: Context,
    uri: Uri,
  ): Bitmap? =
    withContext(Dispatchers.IO) {
      try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream)
        }
      } catch (_: Exception) {
        null
      }
    }

  /**
   * Exports an entire [NotebookDocument] (all pages, PDF backgrounds, paper templates,
   * vector ink strokes, highlighters, shapes, study tapes, text boxes, elements, and sticky notes)
   * into a standard multi-page PDF file at [targetUri].
   */
  suspend fun exportNotebookToPdfUri(
    context: Context,
    document: NotebookDocument,
    targetUri: Uri,
    includeSecurityWatermark: Boolean = false,
  ): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val pdfDocument = PdfDocument()
        val sha256Fingerprint = computeDocumentSha256(document)
        try {
          document.pages.forEachIndexed { index, page ->
            val pdfBmp = page.pdfPageBitmap
            val pageW = pdfBmp?.width ?: 900
            val pageH = pdfBmp?.height ?: 1272
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, index + 1).create()
            val pdfPage = pdfDocument.startPage(pageInfo)
            renderNotePageToCanvas(
              canvas = pdfPage.canvas,
              page = page,
              document = document,
              width = pageW.toFloat(),
              height = pageH.toFloat(),
              includeSecurityWatermark = includeSecurityWatermark,
              sha256Fingerprint = sha256Fingerprint,
            )
            pdfDocument.finishPage(pdfPage)
          }
          context.contentResolver.openOutputStream(targetUri)?.use { out ->
            pdfDocument.writeTo(out)
          } ?: return@withContext false
          true
        } finally {
          pdfDocument.close()
        }
      } catch (_: Exception) {
        false
      }
    }

  /**
   * Exports the current [NotePage] as a high-resolution PNG image to [targetUri].
   */
  suspend fun exportPageToPngUri(
    context: Context,
    document: NotebookDocument,
    page: NotePage,
    targetUri: Uri,
    includeSecurityWatermark: Boolean = false,
  ): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val pdfBmp = page.pdfPageBitmap
        val pageW = pdfBmp?.width ?: 1080
        val pageH = pdfBmp?.height ?: 1527
        val bmp = Bitmap.createBitmap(pageW, pageH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        renderNotePageToCanvas(
          canvas = canvas,
          page = page,
          document = document,
          width = pageW.toFloat(),
          height = pageH.toFloat(),
          includeSecurityWatermark = includeSecurityWatermark,
          sha256Fingerprint = computeDocumentSha256(document),
        )
        context.contentResolver.openOutputStream(targetUri)?.use { out ->
          bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        } ?: return@withContext false
        true
      } catch (_: Exception) {
        false
      }
    }

  private fun renderNotePageToCanvas(
    canvas: Canvas,
    page: NotePage,
    document: NotebookDocument,
    width: Float,
    height: Float,
    includeSecurityWatermark: Boolean,
    sha256Fingerprint: String,
  ) {
    // 1. Paper Background or PDF Page Bitmap
    val pdfBmp = page.pdfPageBitmap
    if (pdfBmp != null) {
      canvas.drawColor(Color.WHITE)
      val destRect = RectF(0f, 0f, width, height)
      canvas.drawBitmap(pdfBmp, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
    } else {
      canvas.drawColor(document.paperColorTheme.color.toArgb())
      drawAndroidPaperTemplate(canvas, document.paperTemplate, width, height)
    }

    // Mapping scale from standard 600x848 logical canvas space if strokes were drawn on screen
    val scaleX = width / 600f
    val scaleY = height / 848f
    val uniformScale = min(scaleX, scaleY).coerceAtLeast(1f)

    // 2. Inserted Images
    page.images.forEach { img ->
      val left = img.position.x * uniformScale
      val top = img.position.y * uniformScale
      val right = left + img.widthDp * uniformScale
      val bottom = top + img.heightDp * uniformScale
      canvas.drawBitmap(img.bitmap, null, RectF(left, top, right, bottom), Paint(Paint.FILTER_BITMAP_FLAG))
    }

    // 3. Strokes (Sorted by zIndex so Highlighter renders behind Pen ink)
    page.strokes.sortedBy { it.zIndex }.forEach { stroke ->
      if (stroke.points.size >= 2) {
        val path = Path()
        val first = stroke.points.first()
        path.moveTo(first.x * uniformScale, first.y * uniformScale)
        if (stroke.isShape) {
          for (i in 1 until stroke.points.size) {
            val pt = stroke.points[i]
            path.lineTo(pt.x * uniformScale, pt.y * uniformScale)
          }
        } else {
          for (i in 1 until stroke.points.size) {
            val prev = stroke.points[i - 1]
            val curr = stroke.points[i]
            path.quadTo(
              prev.x * uniformScale,
              prev.y * uniformScale,
              ((prev.x + curr.x) * 0.5f) * uniformScale,
              ((prev.y + curr.y) * 0.5f) * uniformScale,
            )
          }
        }

        if (stroke.isShape && stroke.shapeFill && stroke.points.size >= 3) {
          val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
              style = Paint.Style.FILL
              color = stroke.color.copy(alpha = 0.18f).toArgb()
            }
          canvas.drawPath(path, fillPaint)
        }

        val strokePaint =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = stroke.color.toArgb()
            strokeWidth = stroke.strokeWidth * uniformScale
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            if (stroke.isHighlighter) {
              xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
            }
            when (stroke.strokePattern) {
              StrokePattern.SOLID -> Unit
              StrokePattern.DASHED ->
                pathEffect = DashPathEffect(floatArrayOf(22f * uniformScale, 14f * uniformScale), 0f)
              StrokePattern.DOTTED ->
                pathEffect = DashPathEffect(floatArrayOf(5f * uniformScale, 13f * uniformScale), 0f)
            }
          }
        canvas.drawPath(path, strokePaint)
      }
    }

    // 4. Study Tapes
    page.studyTapes.forEach { tape ->
      val tapePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          color = tape.color.copy(alpha = if (tape.isRevealed) 0.25f else 0.92f).toArgb()
          strokeWidth = tape.thickness * uniformScale
          strokeCap = Paint.Cap.BUTT
        }
      canvas.drawLine(
        tape.start.x * uniformScale,
        tape.start.y * uniformScale,
        tape.end.x * uniformScale,
        tape.end.y * uniformScale,
        tapePaint,
      )
    }

    // 5. Text Boxes
    page.textBoxes.forEach { tb ->
      val textPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = tb.color.toArgb()
          textSize = tb.fontSizeSp * 1.4f * uniformScale
          typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
      canvas.drawText(
        tb.text,
        tb.position.x * uniformScale,
        (tb.position.y + 20f) * uniformScale,
        textPaint,
      )
    }

    // 6. Elements & Sticky Notes
    page.elements.forEach { el ->
      val elPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = el.stampType.tint.toArgb()
          textSize = 16f * uniformScale
          typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
      canvas.drawText(
        el.stampType.badgeText,
        el.position.x * uniformScale,
        (el.position.y + 18f) * uniformScale,
        elPaint,
      )
    }

    page.stickyNotes.forEach { sn ->
      val left = sn.position.x * uniformScale
      val top = sn.position.y * uniformScale
      val rect = RectF(left, top, left + 170f * uniformScale, top + 90f * uniformScale)
      val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = sn.backgroundColor.toArgb() }
      canvas.drawRoundRect(rect, 10f, 10f, bgPaint)
      val snTextPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = Color.parseColor("#3E2723")
          textSize = 13f * uniformScale
        }
      canvas.drawText(sn.text.lines().firstOrNull().orEmpty(), left + 10f, top + 30f, snTextPaint)
    }

    // 7. Optional Cybersecurity Confidential Watermark & SHA-256 Integrity Footer
    if (includeSecurityWatermark) {
      canvas.save()
      canvas.rotate(-32f, width / 2f, height / 2f)
      val wmPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = Color.argb(28, 30, 90, 168)
          textSize = 38f * uniformScale
          typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          textAlign = Paint.Align.CENTER
        }
      canvas.drawText("PRO NOTES VERIFIED • CONFIDENTIAL", width / 2f, height / 2f, wmPaint)
      canvas.restore()

      val footerPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = Color.parseColor("#6C6C70")
          textSize = 11f * uniformScale
          typeface = Typeface.MONOSPACE
        }
      canvas.drawText(
        "SHA-256 INTEGRITY: ${sha256Fingerprint.take(24)}... • Page ${page.pageNumber}",
        24f * uniformScale,
        height - 18f * uniformScale,
        footerPaint,
      )
    }
  }

  private fun drawAndroidPaperTemplate(
    canvas: Canvas,
    template: PaperTemplate,
    width: Float,
    height: Float,
  ) {
    when (template) {
      PaperTemplate.BLANK -> Unit
      PaperTemplate.RULED -> {
        val linePaint =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DCE6F2")
            strokeWidth = 2f
          }
        var y = 80f
        while (y < height - 50f) {
          canvas.drawLine(40f, y, width - 40f, y, linePaint)
          y += 42f
        }
      }
      PaperTemplate.GRID -> {
        val gridPaint =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E4E6EB")
            strokeWidth = 1.4f
          }
        var x = 36f
        while (x < width) {
          canvas.drawLine(x, 0f, x, height, gridPaint)
          x += 36f
        }
        var y = 36f
        while (y < height) {
          canvas.drawLine(0f, y, width, y, gridPaint)
          y += 36f
        }
      }
      PaperTemplate.DOTTED -> {
        val dotPaint =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C8D0DC")
            style = Paint.Style.FILL
          }
        var x = 34f
        while (x < width - 20f) {
          var y = 34f
          while (y < height - 20f) {
            canvas.drawCircle(x, y, 2.5f, dotPaint)
            y += 34f
          }
          x += 34f
        }
      }
      PaperTemplate.CORNELL -> {
        val cornellPaint =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CB6D6D")
            strokeWidth = 2.5f
          }
        val headerY = 95f
        val summaryY = height - 150f
        val cueX = width * 0.28f
        canvas.drawLine(0f, headerY, width, headerY, cornellPaint)
        canvas.drawLine(cueX, headerY, cueX, summaryY, cornellPaint)
        canvas.drawLine(0f, summaryY, width, summaryY, cornellPaint)
      }
    }
  }

  /**
   * Computes a cryptographic SHA-256 integrity digest over a [NotebookDocument]'s pages, strokes, and text.
   */
  fun computeDocumentSha256(document: NotebookDocument): String {
    return runCatching {
      val digest = MessageDigest.getInstance("SHA-256")
      digest.update(document.id.toByteArray())
      digest.update(document.title.toByteArray())
      document.pages.forEach { page ->
        digest.update(page.pageNumber.toString().toByteArray())
        page.strokes.forEach { s ->
          digest.update(s.id.toByteArray())
          digest.update(s.points.size.toString().toByteArray())
        }
        page.textBoxes.forEach { tb ->
          digest.update(tb.text.toByteArray())
        }
      }
      digest.digest().joinToString("") { "%02x".format(it) }.uppercase()
    }.getOrDefault("E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855")
  }

  /**
   * Salted SHA-256 PIN digest for Notebook Vault locking.
   */
  fun hashPinSha256(notebookId: String, rawPin: String): String {
    return runCatching {
      val digest = MessageDigest.getInstance("SHA-256")
      val salted = "pronotes_vault_v3_${notebookId}_${rawPin.trim()}"
      digest.digest(salted.toByteArray()).joinToString("") { "%02x".format(it) }
    }.getOrDefault(rawPin)
  }

  /**
   * Generates a crisp multi-page vector-rendered PDF study handout on-device with exact A4 aspect ratio (1 : 1.414).
   */
  suspend fun createSamplePdfPages(): List<NotePage> =
    withContext(Dispatchers.Default) {
      val page1 = createHandoutBitmap(
        pageNumber = 1,
        chapterTitle = "CHAPTER 1 • QUANTITATIVE METHODS & CALCULUS",
        heading = "1.1 Fundamental Theorem & Derivative Rules",
        formulaText = "f'(x) = lim (h → 0) [ f(x + h) - f(x) ] / h",
        rows = listOf(
          arrayOf("Function f(x)", "Derivative f'(x)", "Domain", "Notes"),
          arrayOf("x^n", "n · x^(n-1)", "x ∈ ℝ", "Power Rule"),
          arrayOf("e^x", "e^x", "x ∈ ℝ", "Exponential"),
          arrayOf("ln(x)", "1 / x", "x > 0", "Natural Log"),
          arrayOf("sin(x)", "cos(x)", "x ∈ ℝ", "Trigonometric"),
        ),
      )
      val page2 = createHandoutBitmap(
        pageNumber = 2,
        chapterTitle = "CHAPTER 1 • QUANTITATIVE METHODS & CALCULUS",
        heading = "1.2 Integration by Parts & Area Under Curve",
        formulaText = "∫ u dv = u · v - ∫ v du",
        rows = listOf(
          arrayOf("Interval [a, b]", "Riemann Sum", "Exact Integral", "Error Bound"),
          arrayOf("[0, 1]", "0.3350", "0.3333", "0.0017"),
          arrayOf("[0, 2]", "2.6700", "2.6667", "0.0033"),
          arrayOf("[0, 3]", "9.0050", "9.0000", "0.0050"),
        ),
      )
      listOf(
        NotePage(pageNumber = 1, pdfPageBitmap = page1),
        NotePage(pageNumber = 2, pdfPageBitmap = page2),
      )
    }

  private fun createHandoutBitmap(
    pageNumber: Int,
    chapterTitle: String,
    heading: String,
    formulaText: String,
    rows: List<Array<String>>,
  ): Bitmap {
    // True ISO A4 aspect ratio (1 : 1.4142) -> 900 x 1272
    val w = 900
    val h = 1272
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.drawColor(Color.WHITE)

    val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1B2B4B") }
    canvas.drawRect(0f, 60f, 280f, 102f, bannerPaint)

    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = 20f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText("PAGE 1.$pageNumber", 160f, 88f, headerPaint)

    val chapterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#1B2B4B")
      textSize = 19f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText(chapterTitle, 305f, 88f, chapterPaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#1C1C1E")
      textSize = 26f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText(heading, 76f, 175f, titlePaint)

    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#3A3A3C")
      textSize = 18f
    }
    canvas.drawText(
      "Circle any formula with the Pen, lift your stylus, and touch & hold the circle to Lasso!",
      76f,
      215f,
      bodyPaint,
    )

    // Formula Callout Box
    val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#EEF4FC") }
    val boxBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#3E8BF7")
      style = Paint.Style.STROKE
      strokeWidth = 2.5f
    }
    val formulaRect = RectF(76f, 250f, w - 76f, 330f)
    canvas.drawRoundRect(formulaRect, 12f, 12f, boxPaint)
    canvas.drawRoundRect(formulaRect, 12f, 12f, boxBorder)

    val formulaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#17325A")
      textSize = 23f
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText(formulaText, w / 2f, 298f, formulaPaint)

    // Structured Table
    val tableTop = 385f
    val rowHeight = 56f
    val tableLeft = 76f
    val tableRight = w - 76f
    val colWidth = (tableRight - tableLeft) / 4f

    rows.forEachIndexed { rIdx, cols ->
      val top = tableTop + rIdx * rowHeight
      val bottom = top + rowHeight
      val rowFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (rIdx == 0) Color.parseColor("#DCE8F8") else Color.parseColor("#F8FAFD")
      }
      canvas.drawRect(tableLeft, top, tableRight, bottom, rowFill)
      val cellStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0C4DE")
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
      }
      for (cIdx in cols.indices) {
        val left = tableLeft + cIdx * colWidth
        canvas.drawRect(left, top, left + colWidth, bottom, cellStroke)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = Color.parseColor("#1C1C1E")
          textSize = if (rIdx == 0) 17f else 16.5f
          typeface =
            if (rIdx == 0) Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            else Typeface.DEFAULT
          textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cols[cIdx], left + colWidth / 2f, top + 34f, textPaint)
      }
    }

    // Practice Workspace Box at bottom of PDF
    val notesTop = tableTop + rows.size * rowHeight + 55f
    canvas.drawText("Practice & Margin Annotations:", 76f, notesTop, titlePaint)
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#E0E6ED")
      strokeWidth = 2f
    }
    for (i in 1..8) {
      val y = notesTop + 20f + i * 44f
      canvas.drawLine(76f, y, w - 76f, y, linePaint)
    }

    return bmp
  }

  private fun queryFileName(context: Context, uri: Uri): String? {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      if (nameIndex >= 0 && cursor.moveToFirst()) {
        return cursor.getString(nameIndex)
      }
    }
    return uri.lastPathSegment
  }
}
