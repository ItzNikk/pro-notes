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
  val createdAtMs: Long = System.currentTimeMillis(),
)

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

fun isClosedCircleLoop(points: List<Offset>): Boolean {
  if (points.size < 10) return false
  val minX = points.minOf { it.x }
  val maxX = points.maxOf { it.x }
  val minY = points.minOf { it.y }
  val maxY = points.maxOf { it.y }
  val diag = hypot(maxX - minX, maxY - minY)
  if (diag < 90f) return false // ignore handwriting-sized loops
  if ((points.last() - points.first()).getDistance() > diag * 0.40f) return false
  var pathLen = 0f
  for (i in 1 until points.size) pathLen += (points[i] - points[i - 1]).getDistance()
  return pathLen in (diag * 1.9f)..(diag * 3.6f) // a circle is about 2.2x diag; scribbles are longer
}
