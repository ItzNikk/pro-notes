package com.example.model

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.hypot
import com.example.ui.theme.PenColorGridPalette
import com.example.ui.theme.SwatchMagenta
import com.example.ui.theme.SwatchNeonGreen
import com.example.ui.theme.SwatchPurple
import com.example.ui.theme.SwatchRoyalBlue
import com.example.ui.theme.SwatchWhite

enum class ScreenMode {
  DOCUMENTS,
  EDITOR,
  SETTINGS,
}

enum class SidebarSection(val label: String) {
  DOCUMENTS("Documents"),
  FAVORITES("Favorites"),
  SHARED("Shared"),
  MARKETPLACE("Marketplace"),
  TRASH("Trash"),
}

/**
 * Official Goodnotes 6 Main Toolbar Tools (from IMG_0531 "Toolbar Customization" + IMG_0526).
 */
enum class PrimaryTool(val label: String) {
  LASSO("Lasso"),
  WRITING_TOOLS("Writing Tools"),
  ERASER("Eraser"),
  TEXT("Text"),
  ELEMENTS("Elements"),
  IMAGE("Images"),
  SHAPE("Shapes"),
  STICKY_NOTE("Sticky Notes"),
  LASER("Laser Pointer"),
}

/**
 * Official Goodnotes 6 Writing Sub-Tools (from IMG_0531 "Writing Tools Customization" & Floating Sub-Toolbar).
 */
enum class WritingSubTool(val label: String) {
  PEN("Pen"),
  PENCIL("Pencil"),
  HIGHLIGHTER("Highlighter"),
  TAPE("Tape"),
  DRAW_SHAPE("Draw Shape"),
}

/**
 * Full 4-edge Toolbar Docking (Top, Bottom, Left Rail, Right Rail) + Long-Press to Reposition.
 */
enum class ToolbarDockPosition(val label: String) {
  TOP("Top"),
  BOTTOM("Bottom"),
  LEFT("Left"),
  RIGHT("Right"),
}

/**
 * Goodnotes 6 Page Scrolling Direction (Horizontal book turn vs Vertical continuous scroll).
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
  HIGHLIGHTER_ONLY("Erase Highlighter Only"),
}

/**
 * Expanded 11-Shape Goodnotes 6 Auto-Detected Geometric Shapes Suite.
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
  AI_HANDWRITING_ASSIST,
  EXPORT_SHARE,
  SECURITY_VAULT,
}

enum class NotebookCoverStyle(val label: String) {
  KRAFT_TAN("Kraft Tan"),
  CHARCOAL_DARK("Charcoal Pro"),
  WHITE_DOCUMENT("Clean White"),
  WHITEBOARD("Infinite Whiteboard"),
  HANDWRITTEN_NOTES("Study Notes"),
  GRID_PAPER("Engineering Grid"),
}

enum class PaperTemplate(val label: String) {
  BLANK("Blank Paper"),
  RULED("Ruled Lines"),
  GRID("Square Grid"),
  DOTTED("Dotted Grid"),
  CORNELL("Cornell Notes"),
}

/**
 * Goodnotes 6 Paper Color Tints for comfortable reading & eye care.
 */
enum class PaperColorTheme(val label: String, val color: Color) {
  PURE_WHITE("Pure White", Color(0xFFFFFFFF)),
  GOODNOTES_CREAM("Goodnotes Cream", Color(0xFFFDF8E7)),
  NIGHT_DARK("Night Dark", Color(0xFF1A1E26)),
  SAGE_EYE_CARE("Sage Eye-Care", Color(0xFFEEF5EE)),
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
  IMPORTANT_STAR("Important", "★ IMPORTANT", Color(0xFFFFB300)),
  EXAM_TIP("Exam Tip", "💡 KEY CONCEPT", Color(0xFF1E88E5)),
  CHECK_VERIFIED("Verified", "✓ VERIFIED", Color(0xFF00C853)),
  FORMULA_BOX("Formula", "∑ FORMULA", Color(0xFF8E24AA)),
  QUESTION_FLAG("Review", "? REVIEW LATER", Color(0xFFE53935)),
  GRAPH_AXES("Axes", "┼ Y = f(X)", Color(0xFF00897B)),
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
)

data class NotebookDocument(
  val id: String,
  val title: String,
  val timestamp: String,
  val coverStyle: NotebookCoverStyle,
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
) {
  val currentPage: NotePage
    get() = pages.getOrElse(currentPageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))) {
      NotePage(pageNumber = 1)
    }
}

val DefaultQuickSwatches = listOf(
  SwatchRoyalBlue,
  SwatchWhite,
  SwatchPurple,
  SwatchNeonGreen,
  SwatchMagenta,
)

val DefaultCustomPalette: List<Color> = PenColorGridPalette

/**
 * Empty initial notebooks list — all user notes are created by the user and persisted locally on-device via Room SQLite.
 */
val InitialNotebooks: List<NotebookDocument> = emptyList()

class LassoShape(private val poly: List<Offset>) {
  private val minX = poly.minOf { it.x }
  private val maxX = poly.maxOf { it.x }
  private val minY = poly.minOf { it.y }
  private val maxY = poly.maxOf { it.y }

  fun contains(pt: Offset): Boolean {
    if (poly.size < 3) return false
    if (pt.x < minX || pt.x > maxX || pt.y < minY || pt.y > maxY) return false
    var inside = false
    var j = poly.lastIndex
    for (i in poly.indices) {
      val xi = poly[i].x; val yi = poly[i].y
      val xj = poly[j].x; val yj = poly[j].y
      if ((yi > pt.y) != (yj > pt.y) && pt.x < (xj - xi) * (pt.y - yi) / (yj - yi) + xi) inside = !inside
      j = i
    }
    return inside
  }

  fun fractionInside(points: List<Offset>): Float =
    if (points.isEmpty()) 0f else points.count { contains(it) }.toFloat() / points.size

  fun distanceToEdge(p: Offset): Float {
    var best = Float.MAX_VALUE
    for (i in poly.indices) {
      val a = poly[i]; val b = poly[(i + 1) % poly.size]
      val ab = b - a
      val len2 = ab.x * ab.x + ab.y * ab.y
      val t = if (len2 == 0f) 0f else (((p.x - a.x) * ab.x + (p.y - a.y) * ab.y) / len2).coerceIn(0f, 1f)
      best = minOf(best, (p - Offset(a.x + ab.x * t, a.y + ab.y * t)).getDistance())
    }
    return best
  }
}

fun isClosedCircleLoop(points: List<Offset>): Boolean {
  if (points.size < 10) return false
  val minX = points.minOf { it.x }; val maxX = points.maxOf { it.x }
  val minY = points.minOf { it.y }; val maxY = points.maxOf { it.y }
  val diag = hypot(maxX - minX, maxY - minY)
  if (diag < 90f) return false                                   // ignore handwriting-sized loops
  if ((points.last() - points.first()).getDistance() > diag * 0.40f) return false
  var pathLen = 0f
  for (i in 1 until points.size) pathLen += (points[i] - points[i - 1]).getDistance()
  return pathLen in (diag * 1.9f)..(diag * 3.6f)                 // a circle is about 2.2x diag; scribbles are longer
}

