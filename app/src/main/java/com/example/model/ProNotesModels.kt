package com.example.model

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.PenColorGridPalette
import com.example.ui.theme.SwatchMagenta
import com.example.ui.theme.SwatchNeonGreen
import com.example.ui.theme.SwatchPurple
import com.example.ui.theme.SwatchRoyalBlue
import com.example.ui.theme.SwatchWhite
import kotlin.math.hypot

enum class ScreenMode {
  DOCUMENTS,
  EDITOR,
  SETTINGS,
}

typealias AppScreen = ScreenMode

enum class AppThemeMode(val label: String) {
  DARK("Dark"),
  LIGHT("Light"),
  SYSTEM("System"),
}

enum class DocumentType(val label: String) {
  NOTEBOOK("Notebook"),
  WHITEBOARD("Whiteboard"),
}

enum class PageSizePreset(
  val label: String,
  val subtitle: String,
  val widthPt: Int,
  val heightPt: Int,
) {
  GOODNOTES_STANDARD("Goodnotes Standard", "Optimized for tablet screens", 595, 780),
  A4("A4 Paper", "210 × 297 mm ISO standard", 595, 842),
  LETTER("US Letter", "8.5 × 11 in standard", 612, 792);

  val aspectRatioPortrait: Float
    get() = widthPt.toFloat() / heightPt.toFloat()
}

enum class PageOrientation(val label: String) {
  PORTRAIT("Portrait"),
  LANDSCAPE("Landscape"),
}

enum class LibraryViewMode(val label: String) {
  GRID("Grid"),
  LIST("List"),
}

enum class DocumentSortOption(val label: String) {
  RECENT("Date Modified"),
  NAME_ASC("Name (A–Z)"),
  FAVORITES_FIRST("Favorites First"),
}

enum class SidebarSection(val label: String) {
  DOCUMENTS("Documents"),
  FAVORITES("Favorites"),
  SHARED("Templates"),
  MARKETPLACE("Whiteboards"),
  TRASH("Trash"),
}

typealias DocumentsSidebarSection = SidebarSection

/**
 * Main Docked Toolbar Tools.
 */
enum class PrimaryTool(val label: String) {
  WRITING_TOOLS("Pen & Ink"),
  ERASER("Eraser"),
  LASSO("Lasso"),
  SHAPE("Shapes"),
  TEXT("Text"),
  IMAGE("Images"),
  ELEMENTS("Elements"),
  STICKY_NOTE("Note"),
  LASER("Laser"),
}

/**
 * Writing Sub-Tools in the Contextual Toolbar Strip.
 */
enum class WritingSubTool(val label: String) {
  PEN("Pen"),
  PENCIL("Pencil"),
  HIGHLIGHTER("Highlighter"),
  TAPE("Study Tape"),
  DRAW_SHAPE("Shapes"),
}

enum class ToolbarDockPosition(val label: String) {
  TOP("Top"),
  BOTTOM("Bottom"),
  LEFT("Left"),
  RIGHT("Right"),
}

/**
 * Page Scrolling Direction (Horizontal book turn vs Vertical continuous scroll).
 */
enum class PageScrollDirection(val label: String) {
  HORIZONTAL("Horizontal"),
  VERTICAL("Vertical"),
}

enum class PenStyle(val title: String, val subtitle: String) {
  FOUNTAIN("Fountain Pen", "Pressure-sensitive calligraphy"),
  BALL("Ball Pen", "Uniform stroke width"),
  BRUSH("Brush Pen", "High pressure & tilt expression"),
}

enum class StrokePattern(val label: String) {
  SOLID("Solid"),
  DASHED("Dashed"),
  DOTTED("Dotted"),
}

enum class EraserMode(val label: String) {
  PRECISION("Precision Eraser"),
  STROKE("Stroke Eraser"),
  HIGHLIGHTER_ONLY("Highlighter Only"),
}

/**
 * 11 Auto-Detected Geometric Shapes Suite.
 */
enum class ShapeKind(val label: String) {
  LINE("Line"),
  ARROW("Arrow"),
  CURVE("Arc Curve"),
  CIRCLE("Circle"),
  OVAL("Oval / Ellipse"),
  TRIANGLE("Triangle"),
  RECTANGLE("Rectangle"),
  DIAMOND("Diamond"),
  PENTAGON("Pentagon"),
  HEXAGON("Hexagon"),
  STAR("Star"),
}

enum class LaserMode(val label: String) {
  FADING_TRAIL("Laser Trail"),
  HOVERING_DOT("Laser Dot"),
}

enum class ActivePopover {
  NONE,
  PEN_SETTINGS,
  HIGHLIGHTER_SETTINGS,
  PEN_COLOR,
  ERASER_SETTINGS,
  SHAPE_SETTINGS,
  LASSO_SETTINGS,
  ELEMENTS_PICKER,
  TIME_KEEPER,
  TOOLBAR_CUSTOMIZE,
  NEW_NOTE_DIALOG,
  ADD_PAGE_DIALOG,
  AI_HANDWRITING_ASSIST,
  EXPORT_SHARE,
  SECURITY_VAULT,
}

enum class NotebookCoverStyle(val label: String) {
  KRAFT_TAN("Kraft Classic"),
  CHARCOAL_DARK("Charcoal Slate"),
  WHITE_DOCUMENT("Clean Ivory"),
  WHITEBOARD("Infinite Canvas"),
  HANDWRITTEN_NOTES("Academic Blue"),
  GRID_PAPER("Technical Grid"),
}

enum class PaperTemplate(val label: String, val category: String = "Essentials") {
  BLANK("Blank", "Essentials"),
  RULED("Ruled Narrow", "Writing"),
  COLLEGE_RULED("College Ruled", "Writing"),
  WIDE_RULED("Wide Ruled", "Writing"),
  DOTTED("Dotted Grid", "Grid & Dots"),
  GRID("Square Grid", "Grid & Dots"),
  GRAPH("Engineering Graph", "Grid & Dots"),
  CORNELL("Cornell Notes", "Study & Plan"),
  PLANNER("Daily Planner", "Study & Plan"),
  MUSIC("Music Staff", "Specialty"),
  CUSTOM("Custom Template", "Custom"),
}

/**
 * Paper Color Tints for comfortable reading & eye care.
 */
enum class PaperColorTheme(val label: String, val color: Color) {
  PURE_WHITE("Pure White", Color(0xFFFFFFFF)),
  GOODNOTES_CREAM("Warm Cream", Color(0xFFFDF8E7)),
  NIGHT_DARK("Carbon Dark", Color(0xFF1A1E26)),
  SAGE_EYE_CARE("Sage Tint", Color(0xFFEEF5EE));

  val paperBgColor: Color
    get() = color

  companion object {
    val SAGE_EYECARE: PaperColorTheme
      get() = SAGE_EYE_CARE
  }
}

data class DrawnStroke(
  val id: String = java.util.UUID.randomUUID().toString(),
  val points: List<Offset>,
  val pressures: List<Float> = emptyList(),
  val color: Color,
  val strokeWidth: Float,
  val penStyle: PenStyle = PenStyle.BALL,
  val strokePattern: StrokePattern = StrokePattern.SOLID,
  val isHighlighter: Boolean = false,
  val isPencil: Boolean = false,
  val isShape: Boolean = false,
  val shapeKind: ShapeKind? = null,
  val shapeFill: Boolean = false,
  val shapeControlPoints: List<Offset> = emptyList(),
  val zIndex: Int = 0,
  val timestampMs: Long = System.currentTimeMillis(),
)

data class LaserTrailStroke(
  val id: String = java.util.UUID.randomUUID().toString(),
  val points: List<Offset>,
  val color: Color = Color(0xFFFF1744),
  val createdAtMs: Long = System.currentTimeMillis(),
)

enum class StylusPenBrand(
  val label: String,
  val subtitle: String,
  val badge: String,
  val supportsSideButton: Boolean = true,
  val supportsTailEraser: Boolean = true,
  val supportsTilt: Boolean = true,
  val defaultPressureCurve: Float = 1.0f,
) {
  SAMSUNG_S_PEN(
    label = "Samsung S Pen",
    subtitle = "Galaxy Tab S / S Ultra · Wacom EMR · 4096 pressure levels & side button",
    badge = "S PEN",
    supportsSideButton = true,
    supportsTailEraser = false,
    supportsTilt = true,
    defaultPressureCurve = 1.0f,
  ),
  XIAOMI_SMART_PEN(
    label = "Xiaomi Smart Pen (Gen 1 & 2)",
    subtitle = "Xiaomi Pad 5 / 6 / 7 · Dual primary & secondary side buttons · 240Hz",
    badge = "XIAOMI",
    supportsSideButton = true,
    supportsTailEraser = false,
    supportsTilt = true,
    defaultPressureCurve = 1.05f,
  ),
  ONEPLUS_STYLO(
    label = "OnePlus Stylo / OPPO Pencil",
    subtitle = "OnePlus Pad / Pad 2 · Low-latency haptic ink & side button erase",
    badge = "STYLO",
    supportsSideButton = true,
    supportsTailEraser = false,
    supportsTilt = true,
    defaultPressureCurve = 1.0f,
  ),
  LENOVO_PRECISION_PEN(
    label = "Lenovo Precision / Tab Pen Plus",
    subtitle = "Lenovo Tab P11 / P12 / Extreme · Barrel button & tail eraser support",
    badge = "LENOVO",
    supportsSideButton = true,
    supportsTailEraser = true,
    supportsTilt = true,
    defaultPressureCurve = 0.95f,
  ),
  HUAWEI_M_PENCIL(
    label = "Huawei M-Pencil (2nd & 3rd Gen)",
    subtitle = "MatePad Pro / Air · NearLink / 4096 pressure levels & tilt shading",
    badge = "M-PENCIL",
    supportsSideButton = true,
    supportsTailEraser = false,
    supportsTilt = true,
    defaultPressureCurve = 1.0f,
  ),
  USI_STYLUS(
    label = "USI 2.0 Stylus (Pixel / Chromebook)",
    subtitle = "Pixel Tablet & USI 2.0 devices · Tail eraser & barrel button",
    badge = "USI 2.0",
    supportsSideButton = true,
    supportsTailEraser = true,
    supportsTilt = true,
    defaultPressureCurve = 1.05f,
  ),
  GENERIC_STYLUS(
    label = "Universal Active / Capacitive Pen",
    subtitle = "Standard Android stylus protocol with velocity-assisted pressure",
    badge = "UNIVERSAL",
    supportsSideButton = true,
    supportsTailEraser = true,
    supportsTilt = false,
    defaultPressureCurve = 1.15f,
  ),
}

enum class StylusBarrelAction(val label: String, val description: String) {
  ERASE_WHILE_HELD("Hold to Erase", "Temporarily activates Eraser while holding the side button"),
  LASSO_WHILE_HELD("Hold for Lasso", "Temporarily selects strokes with Lasso while held"),
  LASER_WHILE_HELD("Hold for Laser", "Temporarily activates Laser pointer while held"),
  IGNORE("Disabled", "Ignore side button presses to prevent accidental switches"),
}

fun buildShapePreviewPoints(
  points: List<Offset>,
  kind: ShapeKind,
): Pair<List<Offset>, List<Offset>> {
  if (points.size < 2) return points to emptyList()
  val first = points.first()
  val last = points.last()
  val minX = minOf(first.x, last.x).let { if (points.size > 4) minOf(it, points.minOf { p -> p.x }) else it }
  val maxX = maxOf(first.x, last.x).let { if (points.size > 4) maxOf(it, points.maxOf { p -> p.x }) else it }
  val minY = minOf(first.y, last.y).let { if (points.size > 4) minOf(it, points.minOf { p -> p.y }) else it }
  val maxY = maxOf(first.y, last.y).let { if (points.size > 4) maxOf(it, points.maxOf { p -> p.y }) else it }
  val cx = (minX + maxX) / 2f
  val cy = (minY + maxY) / 2f
  val rx = ((maxX - minX) / 2f).coerceAtLeast(18f)
  val ry = ((maxY - minY) / 2f).coerceAtLeast(18f)

  return when (kind) {
    ShapeKind.LINE -> {
      val linePts = (0..24).map { idx ->
        val t = idx / 24f
        Offset(first.x + (last.x - first.x) * t, first.y + (last.y - first.y) * t)
      }
      linePts to listOf(first, last)
    }
    ShapeKind.ARROW -> {
      val dx = last.x - first.x
      val dy = last.y - first.y
      val len = kotlin.math.hypot(dx, dy).coerceAtLeast(1f)
      val ux = dx / len
      val uy = dy / len
      val headLen = (len * 0.22f).coerceIn(22f, 44f)
      val wingAngle = Math.toRadians(28.0)
      val cosA = kotlin.math.cos(wingAngle).toFloat()
      val sinA = kotlin.math.sin(wingAngle).toFloat()
      val leftWing = Offset(
        last.x - headLen * (ux * cosA - uy * sinA),
        last.y - headLen * (uy * cosA + ux * sinA),
      )
      val rightWing = Offset(
        last.x - headLen * (ux * cosA + uy * sinA),
        last.y - headLen * (uy * cosA - ux * sinA),
      )
      val shaftPts = (0..18).map { idx ->
        val t = idx / 18f
        Offset(first.x + dx * t, first.y + dy * t)
      }
      (shaftPts + listOf(leftWing, last, rightWing)) to listOf(first, last)
    }
    ShapeKind.CURVE -> {
      val mid = points[points.size / 2]
      val ctrl = Offset(
        x = (mid.x * 1.35f - (first.x + last.x) * 0.175f),
        y = (mid.y * 1.35f - (first.y + last.y) * 0.175f),
      )
      val curvePts = (0..36).map { idx ->
        val t = idx / 36f
        val inv = 1f - t
        Offset(
          x = inv * inv * first.x + 2f * inv * t * ctrl.x + t * t * last.x,
          y = inv * inv * first.y + 2f * inv * t * ctrl.y + t * t * last.y,
        )
      }
      curvePts to listOf(first, ctrl, last)
    }
    ShapeKind.CIRCLE -> {
      val r = maxOf(rx, ry)
      val circlePts = (0..48).map { idx ->
        val angle = (idx.toDouble() / 48.0) * 2.0 * Math.PI
        Offset(
          x = cx + (r * kotlin.math.cos(angle)).toFloat(),
          y = cy + (r * kotlin.math.sin(angle)).toFloat(),
        )
      }
      val handles = listOf(
        Offset(cx, cy - r),
        Offset(cx + r, cy),
        Offset(cx, cy + r),
        Offset(cx - r, cy),
      )
      circlePts to handles
    }
    ShapeKind.OVAL -> {
      val ovalPts = (0..48).map { idx ->
        val angle = (idx.toDouble() / 48.0) * 2.0 * Math.PI
        Offset(
          x = cx + (rx * kotlin.math.cos(angle)).toFloat(),
          y = cy + (ry * kotlin.math.sin(angle)).toFloat(),
        )
      }
      val handles = listOf(
        Offset(cx, cy - ry),
        Offset(cx + rx, cy),
        Offset(cx, cy + ry),
        Offset(cx - rx, cy),
      )
      ovalPts to handles
    }
    ShapeKind.RECTANGLE -> {
      val tl = Offset(cx - rx, cy - ry)
      val tr = Offset(cx + rx, cy - ry)
      val br = Offset(cx + rx, cy + ry)
      val bl = Offset(cx - rx, cy + ry)
      val vertices = listOf(tl, tr, br, bl)
      interpolateClosedPolygon(vertices, 10) to vertices
    }
    ShapeKind.TRIANGLE -> {
      val top = Offset(cx, cy - ry)
      val br = Offset(cx + rx, cy + ry)
      val bl = Offset(cx - rx, cy + ry)
      val vertices = listOf(top, br, bl)
      interpolateClosedPolygon(vertices, 12) to vertices
    }
    ShapeKind.DIAMOND -> {
      val top = Offset(cx, cy - ry)
      val right = Offset(cx + rx, cy)
      val bottom = Offset(cx, cy + ry)
      val left = Offset(cx - rx, cy)
      val vertices = listOf(top, right, bottom, left)
      interpolateClosedPolygon(vertices, 10) to vertices
    }
    ShapeKind.PENTAGON -> {
      val vertices = (0 until 5).map { idx ->
        val angle = -Math.PI / 2.0 + idx * (2.0 * Math.PI / 5.0)
        Offset(
          x = cx + (rx * kotlin.math.cos(angle)).toFloat(),
          y = cy + (ry * kotlin.math.sin(angle)).toFloat(),
        )
      }
      interpolateClosedPolygon(vertices, 8) to vertices
    }
    ShapeKind.HEXAGON -> {
      val vertices = (0 until 6).map { idx ->
        val angle = -Math.PI / 2.0 + idx * (2.0 * Math.PI / 6.0)
        Offset(
          x = cx + (rx * kotlin.math.cos(angle)).toFloat(),
          y = cy + (ry * kotlin.math.sin(angle)).toFloat(),
        )
      }
      interpolateClosedPolygon(vertices, 8) to vertices
    }
    ShapeKind.STAR -> {
      val vertices = (0 until 10).map { idx ->
        val isOuter = idx % 2 == 0
        val factor = if (isOuter) 1.0f else 0.42f
        val angle = -Math.PI / 2.0 + idx * (Math.PI / 5.0)
        Offset(
          x = cx + (rx * factor * kotlin.math.cos(angle)).toFloat(),
          y = cy + (ry * factor * kotlin.math.sin(angle)).toFloat(),
        )
      }
      interpolateClosedPolygon(vertices, 5) to vertices
    }
  }
}

private fun interpolateClosedPolygon(vertices: List<Offset>, stepsPerEdge: Int): List<Offset> {
  if (vertices.size < 2) return vertices
  val result = ArrayList<Offset>(vertices.size * stepsPerEdge + 1)
  for (i in vertices.indices) {
    val a = vertices[i]
    val b = vertices[(i + 1) % vertices.size]
    for (step in 0 until stepsPerEdge) {
      val t = step.toFloat() / stepsPerEdge
      result.add(Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t))
    }
  }
  result.add(vertices.first())
  return result
}

data class StudyTapeItem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val start: Offset,
  val end: Offset,
  val color: Color = Color(0xFF3E8BF7),
  val thickness: Float = 38f,
  val isRevealed: Boolean = false,
)

data class CanvasTextBox(
  val id: String = java.util.UUID.randomUUID().toString(),
  val position: Offset,
  val text: String,
  val color: Color = Color(0xFF1A1A1C),
  val fontSizeSp: Float = 16f,
  val isBold: Boolean = false,
)

data class StickyNoteItem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val position: Offset,
  val text: String,
  val backgroundColor: Color = Color(0xFFFFF59D),
  val isExpanded: Boolean = true,
)

enum class ElementStampType(val label: String, val badgeText: String, val tint: Color) {
  IMPORTANT_STAR("Important", "IMPORTANT", Color(0xFFFFB300)),
  EXAM_TIP("Key Concept", "KEY CONCEPT", Color(0xFF1E88E5)),
  CHECK_VERIFIED("Verified", "VERIFIED", Color(0xFF00C853)),
  FORMULA_BOX("Formula", "FORMULA", Color(0xFF8E24AA)),
  QUESTION_FLAG("Review", "REVIEW LATER", Color(0xFFE53935)),
  GRAPH_AXES("Coordinate Axes", "Y = f(X)", Color(0xFF00897B)),
}

data class CanvasElementItem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val position: Offset,
  val stampType: ElementStampType,
)

data class CanvasImageItem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val position: Offset,
  val bitmap: Bitmap,
  val widthDp: Float = 180f,
  val heightDp: Float = 140f,
  val rotationDegrees: Float = 0f,
)

data class LassoFilterSettings(
  val includeHandwriting: Boolean = true,
  val includeImages: Boolean = true,
  val includeTextBoxes: Boolean = true,
  val includeComments: Boolean = true,
)

data class LassoClipboard(
  val strokes: List<DrawnStroke> = emptyList(),
  val textBoxes: List<CanvasTextBox> = emptyList(),
  val elements: List<CanvasElementItem> = emptyList(),
  val stickyNotes: List<StickyNoteItem> = emptyList(),
  val images: List<CanvasImageItem> = emptyList(),
  val originalCentroid: Offset = Offset.Zero,
)

data class RulerState(
  val isVisible: Boolean = false,
  val centerOffset: Offset = Offset(280f, 340f),
  val angleDegrees: Float = 0f,
)

data class FolderItem(
  val id: String,
  val name: String,
  val parentFolderId: String? = null,
  val colorArgb: Long = 0xFF4C8DF6L,
  val createdAtEpochMs: Long = System.currentTimeMillis(),
  val updatedAtEpochMs: Long = System.currentTimeMillis(),
  val isTrashed: Boolean = false,
)

data class CustomPageTemplate(
  val id: String,
  val name: String,
  val bitmapKey: String,
  val previewBitmap: Bitmap? = null,
  val isLandscape: Boolean = false,
  val createdAtEpochMs: Long = System.currentTimeMillis(),
)

data class NotePage(
  val pageNumber: Int = 1,
  val isBookmarked: Boolean = false,
  val pdfPageBitmap: Bitmap? = null,
  val strokes: List<DrawnStroke> = emptyList(),
  val studyTapes: List<StudyTapeItem> = emptyList(),
  val textBoxes: List<CanvasTextBox> = emptyList(),
  val stickyNotes: List<StickyNoteItem> = emptyList(),
  val elements: List<CanvasElementItem> = emptyList(),
  val images: List<CanvasImageItem> = emptyList(),
  val pageTemplate: PaperTemplate? = null,
  val pageColorTheme: PaperColorTheme? = null,
  val customTemplateBitmap: Bitmap? = null,
  val customTemplateKey: String = "",
  val canvasWidthPx: Float = 0f,
  val canvasHeightPx: Float = 0f,
) {
  val insertedImages: List<CanvasImageItem>
    get() = images

  fun resolvedTemplate(notebookDefault: PaperTemplate): PaperTemplate =
    pageTemplate ?: notebookDefault

  fun resolvedColorTheme(notebookDefault: PaperColorTheme): PaperColorTheme =
    pageColorTheme ?: notebookDefault
}

data class DocumentHistorySnapshot(
  val pages: List<NotePage>,
  val currentPageIndex: Int,
  val actionDescription: String = "",
)

data class NotebookDocument(
  val id: String,
  val title: String,
  val timestamp: String,
  val coverStyle: NotebookCoverStyle = NotebookCoverStyle.KRAFT_TAN,
  val isFavorite: Boolean = false,
  val isTrashed: Boolean = false,
  val hasStylusBadge: Boolean = false,
  val isImportedPdf: Boolean = false,
  val paperTemplate: PaperTemplate = PaperTemplate.BLANK,
  val paperColorTheme: PaperColorTheme = PaperColorTheme.PURE_WHITE,
  val isLockedVault: Boolean = false,
  val vaultPinHash: String = "",
  val pages: List<NotePage> = listOf(NotePage(pageNumber = 1)),
  val currentPageIndex: Int = 0,
  val folderId: String? = null,
  val documentType: DocumentType =
    if (coverStyle == NotebookCoverStyle.WHITEBOARD) DocumentType.WHITEBOARD
    else DocumentType.NOTEBOOK,
  val pageSizePreset: PageSizePreset = PageSizePreset.A4,
  val pageOrientation: PageOrientation =
    if (coverStyle == NotebookCoverStyle.WHITEBOARD) PageOrientation.LANDSCAPE
    else PageOrientation.PORTRAIT,
  val customTemplateKey: String = "",
  val revision: Long = 0L,
  val updatedAtEpochMs: Long = System.currentTimeMillis(),
) {
  val dateLabel: String
    get() = timestamp

  val isShared: Boolean
    get() = isImportedPdf

  val currentPage: NotePage
    get() =
      pages.getOrElse(currentPageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))) {
        NotePage(pageNumber = 1)
      }
}

val DefaultQuickSwatches =
  listOf(
    SwatchRoyalBlue,
    SwatchWhite,
    SwatchPurple,
    SwatchNeonGreen,
    SwatchMagenta,
  )

val DefaultCustomPalette: List<Color> = PenColorGridPalette

val InitialNotebooks: List<NotebookDocument> = emptyList()

class LassoShape(private val poly: List<Offset>) {
  private val minX = if (poly.isEmpty()) 0f else poly.minOf { it.x }
  private val maxX = if (poly.isEmpty()) 0f else poly.maxOf { it.x }
  private val minY = if (poly.isEmpty()) 0f else poly.minOf { it.y }
  private val maxY = if (poly.isEmpty()) 0f else poly.maxOf { it.y }

  fun contains(pt: Offset): Boolean {
    if (poly.size < 3) return false
    if (pt.x < minX || pt.x > maxX || pt.y < minY || pt.y > maxY) return false
    var inside = false
    var j = poly.lastIndex
    for (i in poly.indices) {
      val xi = poly[i].x
      val yi = poly[i].y
      val xj = poly[j].x
      val yj = poly[j].y
      if ((yi > pt.y) != (yj > pt.y) && pt.x < (xj - xi) * (pt.y - yi) / (yj - yi) + xi) {
        inside = !inside
      }
      j = i
    }
    return inside
  }

  fun fractionInside(points: List<Offset>): Float =
    if (points.isEmpty()) 0f else points.count { contains(it) }.toFloat() / points.size

  fun distanceToEdge(p: Offset): Float {
    if (poly.isEmpty()) return Float.MAX_VALUE
    var best = Float.MAX_VALUE
    for (i in poly.indices) {
      val a = poly[i]
      val b = poly[(i + 1) % poly.size]
      val ab = b - a
      val len2 = ab.x * ab.x + ab.y * ab.y
      val t =
        if (len2 == 0f) 0f
        else (((p.x - a.x) * ab.x + (p.y - a.y) * ab.y) / len2).coerceIn(0f, 1f)
      best = minOf(best, (p - Offset(a.x + ab.x * t, a.y + ab.y * t)).getDistance())
    }
    return best
  }
}

/**
 * Detects a hand-drawn closed shape (circle, blob, rounded box, triangle...) and returns the
 * polygon that encloses the area, or null when the stroke is not a closed loop.
 *
 * A loop counts as closed when the pen ends close to where it started OR when the end of the
 * stroke crosses back over an earlier part of it (people usually overshoot a little). In the
 * overshoot case only the enclosed part is returned.
 */
fun closedLoopPolygon(points: List<Offset>): List<Offset>? {
  if (points.size < 10) return null
  val diag = hypot(points.maxOf { it.x } - points.minOf { it.x }, points.maxOf { it.y } - points.minOf { it.y })
  if (diag < 90f) return null // ignore handwriting-sized loops
  var pathLen = 0f
  for (i in 1 until points.size) pathLen += (points[i] - points[i - 1]).getDistance()
  if (pathLen < diag * 1.5f) return null // a line or an arc, not something that goes around

  var polygon: List<Offset>? = null

  // 1) The tail of the stroke crosses an earlier part of it.
  val tailStart = (points.size / 2).coerceAtLeast(4)
  search@ for (j in points.lastIndex downTo tailStart) {
    for (i in 0 until j - 3) {
      val hit = segmentIntersection(points[i], points[i + 1], points[j - 1], points[j]) ?: continue
      polygon = listOf(hit) + points.subList(i + 1, j)
      break@search
    }
  }

  // 2) The pen ended near where it started.
  if (polygon == null) {
    val gap = (points.last() - points.first()).getDistance()
    if (gap <= maxOf(diag * 0.22f, 40f)) polygon = points
  }

  val poly = polygon ?: return null
  if (poly.size < 8) return null

  val minX = poly.minOf { it.x }
  val maxX = poly.maxOf { it.x }
  val minY = poly.minOf { it.y }
  val maxY = poly.maxOf { it.y }
  val w = maxX - minX
  val h = maxY - minY
  if (hypot(w, h) < 80f) return null

  // Reject thin back-and-forth scribbles that happen to cross themselves.
  var twiceArea = 0f
  for (i in poly.indices) {
    val a = poly[i]
    val b = poly[(i + 1) % poly.size]
    twiceArea += a.x * b.y - b.x * a.y
  }
  val area = kotlin.math.abs(twiceArea) / 2f
  if (area < w * h * 0.2f) return null
  return poly
}

fun isClosedCircleLoop(points: List<Offset>): Boolean = closedLoopPolygon(points) != null

private fun segmentIntersection(a: Offset, b: Offset, c: Offset, d: Offset): Offset? {
  val rx = b.x - a.x
  val ry = b.y - a.y
  val sx = d.x - c.x
  val sy = d.y - c.y
  val denom = rx * sy - ry * sx
  if (kotlin.math.abs(denom) < 1e-6f) return null
  val qpx = c.x - a.x
  val qpy = c.y - a.y
  val t = (qpx * sy - qpy * sx) / denom
  val u = (qpx * ry - qpy * rx) / denom
  return if (t in 0f..1f && u in 0f..1f) Offset(a.x + t * rx, a.y + t * ry) else null
}
