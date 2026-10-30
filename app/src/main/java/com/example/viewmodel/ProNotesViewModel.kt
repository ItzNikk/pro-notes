package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NotebookRepository
import com.example.data.ProNotesDatabase
import com.example.model.ActivePopover
import com.example.model.CanvasElementItem
import com.example.model.CanvasImageItem
import com.example.model.CanvasTextBox
import com.example.model.DefaultCustomPalette
import com.example.model.DefaultQuickSwatches
import com.example.model.DrawnStroke
import com.example.model.ElementStampType
import com.example.model.EraserMode
import com.example.model.LaserMode
import com.example.model.LassoClipboard
import com.example.model.LassoFilterSettings
import com.example.model.LassoShape
import com.example.model.NotePage
import com.example.model.isClosedCircleLoop
import com.example.model.NotebookCoverStyle
import com.example.model.NotebookDocument
import com.example.model.PageScrollDirection
import com.example.model.PaperColorTheme
import com.example.model.PaperTemplate
import com.example.model.PenStyle
import com.example.model.PrimaryTool
import com.example.model.RulerState
import com.example.model.ScreenMode
import com.example.model.ShapeKind
import com.example.model.SidebarSection
import com.example.model.StickyNoteItem
import com.example.model.StrokePattern
import com.example.model.StudyTapeItem
import com.example.model.ToolbarDockPosition
import com.example.model.WritingSubTool
import com.example.ui.theme.SwatchRoyalBlue
import com.example.util.PdfImportHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProNotesUiState(
  val screenMode: ScreenMode = ScreenMode.DOCUMENTS,
  val sidebarSection: SidebarSection = SidebarSection.DOCUMENTS,
  val notebooks: List<NotebookDocument> = emptyList(),
  val openTabIds: List<String> = emptyList(),
  val activeTabId: String = "",
  val primaryTool: PrimaryTool = PrimaryTool.WRITING_TOOLS,
  val activeWritingSubTool: WritingSubTool = WritingSubTool.PEN,
  val enabledToolbarTools: Set<PrimaryTool> = PrimaryTool.entries.toSet(),
  val enabledWritingSubTools: Set<WritingSubTool> = WritingSubTool.entries.toSet(),
  val toolbarDockPosition: ToolbarDockPosition = ToolbarDockPosition.TOP,
  val pageScrollDirection: PageScrollDirection = PageScrollDirection.HORIZONTAL,
  val subToolbarOffset: Offset = Offset.Zero,
  val onlyDrawWithStylus: Boolean = false,
  val isSPenBarrelButtonActive: Boolean = false,
  val palmRejectionSensitivity: String = "High",
  val isLeftHandedPosture: Boolean = false,
  val pullToAddPage: Boolean = true,
  val privacyScreenShieldEnabled: Boolean = false,
  val watermarkConfidentialExports: Boolean = true,
  val unlockedVaultNotebookIds: Set<String> = emptySet(),
  val pendingUnlockNotebookId: String? = null,
  val penStyle: PenStyle = PenStyle.BALL,
  val strokePattern: StrokePattern = StrokePattern.SOLID,
  val strokeStabilization: Float = 15f,
  val drawAndHold: Boolean = true,
  val holdToErase: Boolean = true,
  val scribbleToErase: Boolean = true,
  val circleToLasso: Boolean = true,
  val highlighterStraightLine: Boolean = false,
  val eraserMode: EraserMode = EraserMode.STROKE,
  val eraserRadius: Float = 28f,
  val eraserAutoDeselect: Boolean = false,
  val selectedShapeKind: ShapeKind = ShapeKind.RECTANGLE,
  val lastAutoDetectedShape: ShapeKind? = null,
  val fillShapes: Boolean = true,
  val activeShapeEditStrokeId: String? = null,
  val laserMode: LaserMode = LaserMode.FADING_TRAIL,
  val lassoFilters: LassoFilterSettings = LassoFilterSettings(),
  val selectedThicknessIndex: Int = 1,
  val thicknessPresets: List<Float> = listOf(3.5f, 6.5f, 11.5f),
  val quickSwatches: List<Color> = DefaultQuickSwatches,
  val customPalette: List<Color> = DefaultCustomPalette,
  val selectedSwatchIndex: Int = 0,
  val selectedColor: Color = SwatchRoyalBlue,
  val isEyedropperActive: Boolean = false,
  val activePopover: ActivePopover = ActivePopover.NONE,
  val showLassoSelection: Boolean = false,
  val lassoLoopPoints: List<Offset> = emptyList(),
  val lassoSelectedStrokeIds: Set<String> = emptySet(),
  val lassoSelectedTextBoxIds: Set<String> = emptySet(),
  val lassoSelectedElementIds: Set<String> = emptySet(),
  val lassoSelectedStickyIds: Set<String> = emptySet(),
  val lassoSelectedImageIds: Set<String> = emptySet(),
  val isLassoResizeMode: Boolean = false,
  val isLassoColorMenuOpen: Boolean = false,
  val lassoClipboard: LassoClipboard? = null,
  val rulerState: RulerState = RulerState(isVisible = false),
  val isZoomWindowVisible: Boolean = false,
  val zoomTargetBoxOffset: Offset = Offset(80f, 180f),
  val isAudioBarVisible: Boolean = false,
  val isRecordingAudio: Boolean = false,
  val isPlayingAudioReplay: Boolean = false,
  val recordedAudioSeconds: Int = 14,
  val isPageSidebarOpen: Boolean = false,
  val redoStack: Map<String, List<DrawnStroke>> = emptyMap(),
  val isReadOnlyMode: Boolean = false,
  val sortByDateAscending: Boolean = false,
  val searchQuery: String = "",
  val isSearchBarVisible: Boolean = false,
  val statusToastMessage: String? = null,
)

private data class ComputedLassoSelection(
  val strokeIds: Set<String>,
  val textBoxIds: Set<String>,
  val elementIds: Set<String>,
  val stickyIds: Set<String>,
  val imageIds: Set<String>,
) {
  val totalCount: Int
    get() = strokeIds.size + textBoxIds.size + elementIds.size + stickyIds.size + imageIds.size
}

class ProNotesViewModel(application: Application) : AndroidViewModel(application) {
  private val database = ProNotesDatabase.getInstance(application)
  private val repository = NotebookRepository(database.notebookDao(), application)
  private val prefs = application.getSharedPreferences("pronotes_settings_v3", Context.MODE_PRIVATE)

  private val _uiState = MutableStateFlow(loadInitialPreferences())
  val uiState: StateFlow<ProNotesUiState> = _uiState.asStateFlow()

  private var pxPerDp = 1f
  fun setCanvasDensity(d: Float) {
    pxPerDp = d
  }

  init {
    viewModelScope.launch {
      repository.allNotebooksFlow.collectLatest { savedNotebooks ->
        _uiState.update { state ->
          val validTabIds =
            state.openTabIds.filter { id -> savedNotebooks.any { it.id == id && !it.isTrashed } }
          val resolvedActiveTab =
            when {
              validTabIds.contains(state.activeTabId) -> state.activeTabId
              validTabIds.isNotEmpty() -> validTabIds.last()
              savedNotebooks.any { !it.isTrashed } -> savedNotebooks.first { !it.isTrashed }.id
              else -> ""
            }
          state.copy(
            notebooks = savedNotebooks,
            openTabIds = validTabIds,
            activeTabId = resolvedActiveTab,
          )
        }
      }
    }
  }

  private fun loadInitialPreferences(): ProNotesUiState {
    val dockName = prefs.getString("toolbar_dock", ToolbarDockPosition.TOP.name) ?: ToolbarDockPosition.TOP.name
    val dockPos = runCatching { ToolbarDockPosition.valueOf(dockName) }.getOrDefault(ToolbarDockPosition.TOP)
    val scrollDirName =
      prefs.getString("page_scroll_direction", PageScrollDirection.HORIZONTAL.name)
        ?: PageScrollDirection.HORIZONTAL.name
    val scrollDir =
      runCatching { PageScrollDirection.valueOf(scrollDirName) }.getOrDefault(PageScrollDirection.HORIZONTAL)
    val stylusOnly = prefs.getBoolean("only_draw_with_stylus", false)
    val circleLasso = prefs.getBoolean("circle_to_lasso", true)
    val drawHold = prefs.getBoolean("draw_and_hold", true)
    val holdErase = prefs.getBoolean("hold_to_erase", true)
    val scribbleErase = prefs.getBoolean("scribble_to_erase", true)

    val savedSwatchColors = prefs.getString("quick_swatches", null)
    val loadedSwatches =
      if (!savedSwatchColors.isNullOrBlank()) {
        savedSwatchColors.split(",").mapNotNull { token ->
          token.toIntOrNull()?.let { Color(it) }
        }.ifEmpty { DefaultQuickSwatches }
      } else {
        DefaultQuickSwatches
      }

    val savedCustomPalette = prefs.getString("custom_palette", null)
    val loadedPalette =
      if (!savedCustomPalette.isNullOrBlank()) {
        savedCustomPalette.split(",").mapNotNull { token ->
          token.toIntOrNull()?.let { Color(it) }
        }.ifEmpty { DefaultCustomPalette }
      } else {
        DefaultCustomPalette
      }

    return ProNotesUiState(
      toolbarDockPosition = dockPos,
      pageScrollDirection = scrollDir,
      onlyDrawWithStylus = stylusOnly,
      circleToLasso = circleLasso,
      drawAndHold = drawHold,
      holdToErase = holdErase,
      scribbleToErase = scribbleErase,
      quickSwatches = loadedSwatches,
      customPalette = loadedPalette,
      selectedColor = loadedSwatches.firstOrNull() ?: SwatchRoyalBlue,
    )
  }

  private fun persistSwatchesAndPalette(swatches: List<Color>, palette: List<Color>) {
    prefs
      .edit()
      .putString("quick_swatches", swatches.joinToString(",") { it.toArgb().toString() })
      .putString("custom_palette", palette.joinToString(",") { it.toArgb().toString() })
      .apply()
  }

  private fun persistDocumentAsync(doc: NotebookDocument?) {
    if (doc == null) return
    viewModelScope.launch {
      repository.saveNotebook(doc)
    }
  }

  private fun currentTimestamp(): String {
    val fmt = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US)
    return fmt.format(Date())
  }

  fun clearToast() {
    _uiState.update { it.copy(statusToastMessage = null) }
  }

  fun switchToDocuments() {
    _uiState.update {
      it.copy(
        screenMode = ScreenMode.DOCUMENTS,
        activePopover = ActivePopover.NONE,
      )
    }
  }

  fun openSettingsScreen() {
    _uiState.update {
      it.copy(
        screenMode = ScreenMode.SETTINGS,
        activePopover = ActivePopover.NONE,
      )
    }
  }

  fun closeSettingsScreen() {
    _uiState.update { state ->
      val nextScreen =
        if (state.activeTabId.isNotEmpty() && state.notebooks.any { it.id == state.activeTabId }) {
          ScreenMode.EDITOR
        } else {
          ScreenMode.DOCUMENTS
        }
      state.copy(screenMode = nextScreen)
    }
  }

  fun openDocument(notebookId: String) {
    _uiState.update { state ->
      val targetDoc = state.notebooks.find { it.id == notebookId }
      if (
        targetDoc != null &&
          targetDoc.isLockedVault &&
          !state.unlockedVaultNotebookIds.contains(notebookId)
      ) {
        return@update state.copy(
          pendingUnlockNotebookId = notebookId,
          statusToastMessage = "Enter Vault PIN to unlock \"${targetDoc.title}\"",
        )
      }
      val updatedTabs =
        if (state.openTabIds.contains(notebookId)) {
          state.openTabIds
        } else {
          state.openTabIds + notebookId
        }
      state.copy(
        screenMode = ScreenMode.EDITOR,
        openTabIds = updatedTabs,
        activeTabId = notebookId,
        pendingUnlockNotebookId = null,
        activePopover = ActivePopover.NONE,
        showLassoSelection = false,
        isLassoResizeMode = false,
      )
    }
  }

  fun closeTab(notebookId: String) {
    _uiState.update { state ->
      val remaining = state.openTabIds.filterNot { it == notebookId }
      if (remaining.isEmpty()) {
        state.copy(
          screenMode = ScreenMode.DOCUMENTS,
          openTabIds = emptyList(),
          activeTabId = "",
          activePopover = ActivePopover.NONE,
        )
      } else {
        val nextActive =
          if (state.activeTabId == notebookId) remaining.last() else state.activeTabId
        state.copy(
          openTabIds = remaining,
          activeTabId = nextActive,
          activePopover = ActivePopover.NONE,
        )
      }
    }
  }

  fun selectSidebarSection(section: SidebarSection) {
    _uiState.update { it.copy(sidebarSection = section) }
  }

  fun toggleSearchBar() {
    _uiState.update { state ->
      state.copy(
        isSearchBarVisible = !state.isSearchBarVisible,
        searchQuery = if (state.isSearchBarVisible) "" else state.searchQuery,
      )
    }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun toggleFavorite(notebookId: String) {
    var updatedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val updatedList =
        state.notebooks.map { nb ->
          if (nb.id == notebookId) {
            nb.copy(isFavorite = !nb.isFavorite).also { updatedDoc = it }
          } else nb
        }
      state.copy(notebooks = updatedList)
    }
    persistDocumentAsync(updatedDoc)
  }

  fun moveNotebookToTrash(notebookId: String) {
    var updatedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val updatedList =
        state.notebooks.map { nb ->
          if (nb.id == notebookId) {
            nb.copy(isTrashed = !nb.isTrashed).also { updatedDoc = it }
          } else nb
        }
      val remainingTabs = state.openTabIds.filterNot { it == notebookId }
      state.copy(
        notebooks = updatedList,
        openTabIds = remainingTabs,
        statusToastMessage = if (updatedDoc?.isTrashed == true) "Moved to Trash" else "Restored from Trash",
      )
    }
    persistDocumentAsync(updatedDoc)
  }

  fun deleteNotebookPermanently(notebookId: String) {
    viewModelScope.launch {
      repository.deleteNotebook(notebookId)
      _uiState.update { it.copy(statusToastMessage = "Deleted notebook permanently") }
    }
  }

  fun emptyTrash() {
    viewModelScope.launch {
      repository.emptyTrash()
      _uiState.update { it.copy(statusToastMessage = "Trash emptied") }
    }
  }

  fun clearAllLocalStorage() {
    viewModelScope.launch {
      repository.clearAllLocalData()
      _uiState.update {
        it.copy(
          notebooks = emptyList(),
          openTabIds = emptyList(),
          activeTabId = "",
          screenMode = ScreenMode.DOCUMENTS,
          statusToastMessage = "All local notes cleared from device",
        )
      }
    }
  }

  fun createNewNotebook(
    title: String = "",
    coverStyle: NotebookCoverStyle = NotebookCoverStyle.KRAFT_TAN,
    paperTemplate: PaperTemplate = PaperTemplate.BLANK,
    openImmediately: Boolean = true,
  ) {
    var createdDoc: NotebookDocument? = null
    _uiState.update { state ->
      val count = state.notebooks.size + 1
      val newId = "nb_${System.currentTimeMillis()}"
      val resolvedTitle =
        title.ifBlank {
          if (coverStyle == NotebookCoverStyle.WHITEBOARD) {
            "Untitled Whiteboard ($count)"
          } else {
            "Untitled Notebook ($count)"
          }
        }
      val newDoc =
        NotebookDocument(
          id = newId,
          title = resolvedTitle,
          timestamp = currentTimestamp(),
          coverStyle = coverStyle,
          isFavorite = false,
          hasStylusBadge = state.onlyDrawWithStylus,
          paperTemplate = paperTemplate,
          pages = listOf(NotePage(pageNumber = 1)),
        )
      createdDoc = newDoc
      val updatedTabs =
        if (openImmediately) state.openTabIds + newId else state.openTabIds
      state.copy(
        notebooks = listOf(newDoc) + state.notebooks,
        openTabIds = updatedTabs,
        activeTabId = if (openImmediately) newId else state.activeTabId,
        screenMode = if (openImmediately) ScreenMode.EDITOR else state.screenMode,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Saved \"$resolvedTitle\" to device",
      )
    }
    persistDocumentAsync(createdDoc)
  }

  fun importPdfDocument(title: String, pages: List<NotePage>) {
    if (pages.isEmpty()) return
    var createdDoc: NotebookDocument? = null
    _uiState.update { state ->
      val newId = "pdf_${System.currentTimeMillis()}"
      val newDoc =
        NotebookDocument(
          id = newId,
          title = title,
          timestamp = currentTimestamp(),
          coverStyle = NotebookCoverStyle.WHITE_DOCUMENT,
          isFavorite = true,
          isImportedPdf = true,
          paperTemplate = PaperTemplate.BLANK,
          pages = pages,
          currentPageIndex = 0,
        )
      createdDoc = newDoc
      state.copy(
        notebooks = listOf(newDoc) + state.notebooks,
        openTabIds = state.openTabIds + newId,
        activeTabId = newId,
        screenMode = ScreenMode.EDITOR,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Imported PDF \"$title\" (${pages.size} pages)",
      )
    }
    persistDocumentAsync(createdDoc)
  }

  fun selectPrimaryTool(tool: PrimaryTool) {
    _uiState.update { state ->
      val nextPopover =
        when {
          tool == state.primaryTool && tool == PrimaryTool.LASSO ->
            if (state.activePopover == ActivePopover.LASSO_SETTINGS) ActivePopover.NONE
            else ActivePopover.LASSO_SETTINGS
          tool == state.primaryTool && tool == PrimaryTool.WRITING_TOOLS ->
            when (state.activeWritingSubTool) {
              WritingSubTool.HIGHLIGHTER ->
                if (state.activePopover == ActivePopover.HIGHLIGHTER_SETTINGS) ActivePopover.NONE
                else ActivePopover.HIGHLIGHTER_SETTINGS
              WritingSubTool.DRAW_SHAPE ->
                if (state.activePopover == ActivePopover.SHAPE_SETTINGS) ActivePopover.NONE
                else ActivePopover.SHAPE_SETTINGS
              else ->
                if (state.activePopover == ActivePopover.PEN_SETTINGS) ActivePopover.NONE
                else ActivePopover.PEN_SETTINGS
            }
          tool == state.primaryTool && tool == PrimaryTool.ERASER ->
            if (state.activePopover == ActivePopover.ERASER_SETTINGS) ActivePopover.NONE
            else ActivePopover.ERASER_SETTINGS
          tool == state.primaryTool && tool == PrimaryTool.SHAPE ->
            if (state.activePopover == ActivePopover.SHAPE_SETTINGS) ActivePopover.NONE
            else ActivePopover.SHAPE_SETTINGS
          tool == PrimaryTool.ELEMENTS ->
            if (state.activePopover == ActivePopover.ELEMENTS_PICKER) ActivePopover.NONE
            else ActivePopover.ELEMENTS_PICKER
          else -> ActivePopover.NONE
        }
      state.copy(
        primaryTool = tool,
        activePopover = nextPopover,
        showLassoSelection = if (tool == PrimaryTool.LASSO) state.showLassoSelection else false,
        isLassoResizeMode = if (tool == PrimaryTool.LASSO) state.isLassoResizeMode else false,
        activeShapeEditStrokeId = if (tool == PrimaryTool.SHAPE) state.activeShapeEditStrokeId else null,
        isEyedropperActive = false,
      )
    }
  }

  fun selectWritingSubTool(subTool: WritingSubTool) {
    _uiState.update { state ->
      val isAlreadySelected =
        state.primaryTool == PrimaryTool.WRITING_TOOLS && state.activeWritingSubTool == subTool
      val nextPopover =
        when {
          isAlreadySelected && (subTool == WritingSubTool.PEN || subTool == WritingSubTool.PENCIL) ->
            if (state.activePopover == ActivePopover.PEN_SETTINGS) ActivePopover.NONE
            else ActivePopover.PEN_SETTINGS
          isAlreadySelected && subTool == WritingSubTool.HIGHLIGHTER ->
            if (state.activePopover == ActivePopover.HIGHLIGHTER_SETTINGS) ActivePopover.NONE
            else ActivePopover.HIGHLIGHTER_SETTINGS
          isAlreadySelected && subTool == WritingSubTool.DRAW_SHAPE ->
            if (state.activePopover == ActivePopover.SHAPE_SETTINGS) ActivePopover.NONE
            else ActivePopover.SHAPE_SETTINGS
          else -> ActivePopover.NONE
        }
      state.copy(
        primaryTool = PrimaryTool.WRITING_TOOLS,
        activeWritingSubTool = subTool,
        activePopover = nextPopover,
        showLassoSelection = false,
        isLassoResizeMode = false,
        isEyedropperActive = false,
      )
    }
  }

  fun togglePopover(popover: ActivePopover) {
    _uiState.update { state ->
      state.copy(
        activePopover = if (state.activePopover == popover) ActivePopover.NONE else popover
      )
    }
  }

  fun dismissPopover() {
    _uiState.update { it.copy(activePopover = ActivePopover.NONE) }
  }

  fun setToolbarDockPosition(position: ToolbarDockPosition) {
    prefs.edit().putString("toolbar_dock", position.name).apply()
    _uiState.update {
      it.copy(
        toolbarDockPosition = position,
        statusToastMessage = "Toolbar docked to ${position.label} (Long-press toolbar to reposition)",
      )
    }
  }

  /**
   * Cycles the toolbar dock position across TOP -> RIGHT -> BOTTOM -> LEFT when long-pressed!
   */
  fun cycleToolbarDockPosition() {
    val entries = ToolbarDockPosition.entries
    val current = _uiState.value.toolbarDockPosition
    val next = entries[(entries.indexOf(current) + 1) % entries.size]
    setToolbarDockPosition(next)
  }

  fun toggleSPenBarrelButtonMode() {
    _uiState.update { state ->
      val next = !state.isSPenBarrelButtonActive
      state.copy(
        isSPenBarrelButtonActive = next,
        statusToastMessage =
          if (next) "S Pen Side Button HELD → Instant Precision/Stroke Eraser Active"
          else "S Pen Side Button Released → Returned to ${state.primaryTool.label}",
      )
    }
  }

  fun togglePrivacyScreenShield(enabled: Boolean) {
    prefs.edit().putBoolean("privacy_screen_shield", enabled).apply()
    _uiState.update {
      it.copy(
        privacyScreenShieldEnabled = enabled,
        statusToastMessage =
          if (enabled) "Cybersecurity Screen Shield ON (FLAG_SECURE blocks screenshots & screen capture)"
          else "Cybersecurity Screen Shield OFF",
      )
    }
  }

  fun toggleWatermarkConfidentialExports(enabled: Boolean) {
    _uiState.update {
      it.copy(
        watermarkConfidentialExports = enabled,
        statusToastMessage =
          if (enabled) "PDF Export SHA-256 Watermark Enabled"
          else "PDF Export Watermark Disabled",
      )
    }
  }

  fun lockCurrentNotebookWithPin(rawPin: String) {
    if (rawPin.trim().length < 4) {
      _uiState.update { it.copy(statusToastMessage = "Vault PIN must be at least 4 digits") }
      return
    }
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      if (docId.isEmpty()) return@update state
      val pinHash = PdfImportHelper.hashPinSha256(docId, rawPin)
      val updatedList =
        state.notebooks.map { nb ->
          if (nb.id == docId) {
            nb.copy(isLockedVault = true, vaultPinHash = pinHash).also { savedDoc = it }
          } else nb
        }
      state.copy(
        notebooks = updatedList,
        unlockedVaultNotebookIds = state.unlockedVaultNotebookIds + docId,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Locked notebook with SHA-256 Vault PIN",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun removeCurrentNotebookLock() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      val updatedList =
        state.notebooks.map { nb ->
          if (nb.id == docId) {
            nb.copy(isLockedVault = false, vaultPinHash = "").also { savedDoc = it }
          } else nb
        }
      state.copy(
        notebooks = updatedList,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Removed Vault PIN Lock",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun attemptUnlockVaultNotebook(notebookId: String, enteredPin: String) {
    _uiState.update { state ->
      val doc = state.notebooks.find { it.id == notebookId } ?: return@update state
      val expectedHash = doc.vaultPinHash
      val actualHash = PdfImportHelper.hashPinSha256(notebookId, enteredPin)
      if (expectedHash.isEmpty() || actualHash == expectedHash) {
        val updatedTabs =
          if (state.openTabIds.contains(notebookId)) state.openTabIds
          else state.openTabIds + notebookId
        state.copy(
          unlockedVaultNotebookIds = state.unlockedVaultNotebookIds + notebookId,
          pendingUnlockNotebookId = null,
          screenMode = ScreenMode.EDITOR,
          openTabIds = updatedTabs,
          activeTabId = notebookId,
          statusToastMessage = "Vault Unlocked • SHA-256 Verified",
        )
      } else {
        state.copy(statusToastMessage = "Incorrect Vault PIN — Access Denied")
      }
    }
  }

  fun dismissPendingVaultUnlock() {
    _uiState.update { it.copy(pendingUnlockNotebookId = null) }
  }

  fun setPageScrollDirection(direction: PageScrollDirection) {
    prefs.edit().putString("page_scroll_direction", direction.name).apply()
    _uiState.update {
      it.copy(
        pageScrollDirection = direction,
        statusToastMessage = "Scrolling Direction: ${direction.label}",
      )
    }
  }

  fun togglePageScrollDirection() {
    val next =
      if (_uiState.value.pageScrollDirection == PageScrollDirection.HORIZONTAL) {
        PageScrollDirection.VERTICAL
      } else {
        PageScrollDirection.HORIZONTAL
      }
    setPageScrollDirection(next)
  }

  fun moveSubToolbar(delta: Offset) {
    _uiState.update { state ->
      val nextX = (state.subToolbarOffset.x + delta.x).coerceIn(-260f, 260f)
      val nextY = (state.subToolbarOffset.y + delta.y).coerceIn(-40f, 520f)
      state.copy(subToolbarOffset = Offset(nextX, nextY))
    }
  }

  fun resetSubToolbarPosition() {
    _uiState.update {
      it.copy(
        subToolbarOffset = Offset.Zero,
        statusToastMessage = "Reset floating toolbar position",
      )
    }
  }

  fun toggleOnlyDrawWithStylus(enabled: Boolean) {
    prefs.edit().putBoolean("only_draw_with_stylus", enabled).apply()
    _uiState.update {
      it.copy(
        onlyDrawWithStylus = enabled,
        statusToastMessage =
          if (enabled) "Stylus-Only ON: Stylus draws, Finger swipes turn pages & pinch zooms"
          else "Stylus+Touch ON: Stylus & 1 finger draw, 2 fingers swipe pages & zoom",
      )
    }
  }

  fun setPalmRejectionSensitivity(level: String) {
    _uiState.update { it.copy(palmRejectionSensitivity = level) }
  }

  fun toggleLeftHandedPosture(enabled: Boolean) {
    _uiState.update { it.copy(isLeftHandedPosture = enabled) }
  }

  fun togglePullToAddPage(enabled: Boolean) {
    _uiState.update { it.copy(pullToAddPage = enabled) }
  }

  fun selectPenStyle(style: PenStyle) {
    _uiState.update { it.copy(penStyle = style) }
  }

  fun selectStrokePattern(pattern: StrokePattern) {
    _uiState.update { it.copy(strokePattern = pattern) }
  }

  fun updateStrokeStabilization(value: Float) {
    _uiState.update { it.copy(strokeStabilization = value.coerceIn(0f, 100f)) }
  }

  fun toggleDrawAndHold(enabled: Boolean) {
    prefs.edit().putBoolean("draw_and_hold", enabled).apply()
    _uiState.update { it.copy(drawAndHold = enabled) }
  }

  fun toggleHoldToErase(enabled: Boolean) {
    prefs.edit().putBoolean("hold_to_erase", enabled).apply()
    _uiState.update { it.copy(holdToErase = enabled) }
  }

  fun toggleScribbleToErase(enabled: Boolean) {
    prefs.edit().putBoolean("scribble_to_erase", enabled).apply()
    _uiState.update { it.copy(scribbleToErase = enabled) }
  }

  fun toggleCircleToLasso(enabled: Boolean) {
    prefs.edit().putBoolean("circle_to_lasso", enabled).apply()
    _uiState.update { it.copy(circleToLasso = enabled) }
  }

  fun toggleHighlighterStraightLine() {
    _uiState.update {
      val next = !it.highlighterStraightLine
      it.copy(
        highlighterStraightLine = next,
        statusToastMessage =
          if (next) "Highlighter: Draw in Straight Line ON"
          else "Highlighter: Freehand mode (Hold at end to snap straight)",
      )
    }
  }

  fun selectEraserMode(mode: EraserMode) {
    _uiState.update { it.copy(eraserMode = mode) }
  }

  fun updateEraserRadius(radius: Float) {
    _uiState.update { it.copy(eraserRadius = radius.coerceIn(14f, 64f)) }
  }

  fun toggleEraserAutoDeselect(enabled: Boolean) {
    _uiState.update { it.copy(eraserAutoDeselect = enabled) }
  }

  fun selectShapeKind(kind: ShapeKind) {
    _uiState.update { it.copy(selectedShapeKind = kind) }
  }

  /**
   * Inserts a clean geometric shape stamp onto the current page if the user taps a shape badge
   * inside the Shapes popover (while freehand drawing in the Shapes tool always auto-decides!).
   */
  fun insertQuickShape(kind: ShapeKind) {
    val pts =
      when (kind) {
        ShapeKind.LINE, ShapeKind.ARROW -> listOf(Offset(140f, 240f), Offset(340f, 240f))
        ShapeKind.CURVE -> listOf(Offset(140f, 260f), Offset(240f, 170f), Offset(340f, 260f))
        else ->
          listOf(
            Offset(160f, 180f),
            Offset(320f, 180f),
            Offset(320f, 310f),
            Offset(160f, 310f),
          )
      }
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (geomPoints, handles) = buildGeometricShapeWithHandles(pts, kind)
      val baseWidth = state.thicknessPresets.getOrElse(state.selectedThicknessIndex) { 6.5f }
      val strokeId = java.util.UUID.randomUUID().toString()
      val stroke =
        DrawnStroke(
          id = strokeId,
          points = geomPoints,
          color = state.selectedColor,
          strokeWidth = baseWidth,
          penStyle = state.penStyle,
          strokePattern = state.strokePattern,
          isShape = true,
          shapeKind = kind,
          shapeFill =
            state.fillShapes &&
              kind != ShapeKind.LINE &&
              kind != ShapeKind.ARROW &&
              kind != ShapeKind.CURVE,
          shapeControlPoints = handles,
          zIndex = 1,
        )
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(strokes = page.strokes + stroke)
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        selectedShapeKind = kind,
        lastAutoDetectedShape = kind,
        activeShapeEditStrokeId = strokeId,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Inserted ${kind.label} (drag blue handles to adjust)",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun toggleFillShapes(enabled: Boolean) {
    _uiState.update { it.copy(fillShapes = enabled) }
  }

  fun toggleLaserMode() {
    _uiState.update { state ->
      val next =
        if (state.laserMode == LaserMode.FADING_TRAIL) LaserMode.HOVERING_DOT
        else LaserMode.FADING_TRAIL
      state.copy(laserMode = next)
    }
  }

  fun updateLassoFilters(filters: LassoFilterSettings) {
    _uiState.update { state ->
      val currentDoc = state.notebooks.find { it.id == state.activeTabId }
      val recomputed =
        if (state.showLassoSelection && currentDoc != null) {
          computeLassoSelection(state.lassoLoopPoints, currentDoc.currentPage, filters)
        } else null
      state.copy(
        lassoFilters = filters,
        lassoSelectedStrokeIds = recomputed?.strokeIds ?: state.lassoSelectedStrokeIds,
        lassoSelectedTextBoxIds = recomputed?.textBoxIds ?: state.lassoSelectedTextBoxIds,
        lassoSelectedElementIds = recomputed?.elementIds ?: state.lassoSelectedElementIds,
        lassoSelectedStickyIds = recomputed?.stickyIds ?: state.lassoSelectedStickyIds,
        lassoSelectedImageIds = recomputed?.imageIds ?: state.lassoSelectedImageIds,
      )
    }
  }

  fun selectThicknessIndex(index: Int) {
    _uiState.update { it.copy(selectedThicknessIndex = index.coerceIn(0, 2)) }
  }

  fun selectQuickSwatch(index: Int) {
    _uiState.update { state ->
      val safeIdx = index.coerceIn(0, (state.quickSwatches.size - 1).coerceAtLeast(0))
      val color = state.quickSwatches.getOrElse(safeIdx) { SwatchRoyalBlue }
      val nextPopover =
        if (state.selectedSwatchIndex == safeIdx) {
          if (state.activePopover == ActivePopover.PEN_COLOR) ActivePopover.NONE
          else ActivePopover.PEN_COLOR
        } else {
          ActivePopover.NONE
        }
      state.copy(
        selectedSwatchIndex = safeIdx,
        selectedColor = color,
        activePopover = nextPopover,
        isEyedropperActive = false,
      )
    }
  }

  /**
   * Replaces the currently selected quick swatch in the sub-toolbar with [color].
   * If a Lasso selection is active, also recolors the selected strokes & text boxes inside the lasso!
   */
  fun selectPaletteColor(color: Color) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val updatedSwatches =
        state.quickSwatches.toMutableList().apply {
          if (isNotEmpty()) {
            this[state.selectedSwatchIndex.coerceIn(0, size - 1)] = color
          } else {
            add(color)
          }
        }
      persistSwatchesAndPalette(updatedSwatches, state.customPalette)

      if (state.primaryTool == PrimaryTool.LASSO && state.showLassoSelection) {
        val selectedStrokeIds = state.lassoSelectedStrokeIds
        val selectedTextIds = state.lassoSelectedTextBoxIds
        val (updatedNotebooks, modifiedDoc) =
          updateCurrentPage(state) { page ->
            page.copy(
              strokes =
                page.strokes.map { stroke ->
                  if (selectedStrokeIds.contains(stroke.id)) {
                    stroke.copy(
                      color = if (stroke.isHighlighter) color.copy(alpha = 0.38f) else color
                    )
                  } else stroke
                },
              textBoxes =
                page.textBoxes.map { tb ->
                  if (selectedTextIds.contains(tb.id)) tb.copy(color = color) else tb
                },
            )
          }
        savedDoc = modifiedDoc
        state.copy(
          notebooks = updatedNotebooks,
          selectedColor = color,
          quickSwatches = updatedSwatches,
          activePopover = ActivePopover.NONE,
          isEyedropperActive = false,
          statusToastMessage = "Recolored selected items",
        )
      } else {
        state.copy(
          selectedColor = color,
          quickSwatches = updatedSwatches,
          isEyedropperActive = false,
        )
      }
    }
    persistDocumentAsync(savedDoc)
  }

  /**
   * Adds a new color slot directly to the floating toolbar swatches AND custom palette.
   */
  fun addNewColorToToolbar(color: Color) {
    _uiState.update { state ->
      val updatedPalette =
        if (state.customPalette.any { it.toArgb() == color.toArgb() }) state.customPalette
        else state.customPalette + color
      val updatedSwatches =
        if (state.quickSwatches.size < 12) state.quickSwatches + color
        else state.quickSwatches.toMutableList().apply { this[lastIndex] = color }
      val newIdx = updatedSwatches.lastIndex.coerceAtLeast(0)
      persistSwatchesAndPalette(updatedSwatches, updatedPalette)
      state.copy(
        customPalette = updatedPalette,
        quickSwatches = updatedSwatches,
        selectedSwatchIndex = newIdx,
        selectedColor = color,
        statusToastMessage = "Added new color slot #${newIdx + 1} to toolbar",
      )
    }
  }

  /**
   * Adds a custom color to the saved palette grid.
   */
  fun addCustomColorToPalette(color: Color) {
    _uiState.update { state ->
      val updatedPalette =
        if (state.customPalette.any { it.toArgb() == color.toArgb() }) state.customPalette
        else state.customPalette + color
      persistSwatchesAndPalette(state.quickSwatches, updatedPalette)
      state.copy(
        customPalette = updatedPalette,
        selectedColor = color,
        statusToastMessage = "Saved custom color to palette",
      )
    }
  }

  /**
   * Removes the active color slot from the toolbar (maintaining at least 2 slots).
   */
  fun removeCurrentToolbarSwatchSlot() {
    _uiState.update { state ->
      if (state.quickSwatches.size <= 2) {
        return@update state.copy(statusToastMessage = "At least 2 color slots must remain in toolbar")
      }
      val removeIdx = state.selectedSwatchIndex.coerceIn(0, state.quickSwatches.lastIndex)
      val updatedSwatches = state.quickSwatches.filterIndexed { idx, _ -> idx != removeIdx }
      val nextIdx = removeIdx.coerceAtMost(updatedSwatches.lastIndex)
      persistSwatchesAndPalette(updatedSwatches, state.customPalette)
      state.copy(
        quickSwatches = updatedSwatches,
        selectedSwatchIndex = nextIdx,
        selectedColor = updatedSwatches[nextIdx],
        statusToastMessage = "Removed color slot from toolbar",
      )
    }
  }

  fun toggleEyedropper() {
    _uiState.update { state ->
      state.copy(
        isEyedropperActive = !state.isEyedropperActive,
        activePopover = ActivePopover.NONE,
        statusToastMessage =
          if (!state.isEyedropperActive) "Eyedropper: tap any stroke on page to sample color"
          else null,
      )
    }
  }

  fun sampleColorAt(offset: Offset) {
    _uiState.update { state ->
      val doc = state.notebooks.find { it.id == state.activeTabId } ?: return@update state
      val hitStroke =
        doc.currentPage.strokes.lastOrNull { stroke ->
          isStrokeHitByPoint(stroke, offset, 34f)
        }
      val sampled = hitStroke?.color?.copy(alpha = 1f) ?: SwatchRoyalBlue
      val updatedSwatches =
        state.quickSwatches.toMutableList().apply {
          if (isNotEmpty()) {
            this[state.selectedSwatchIndex.coerceIn(0, size - 1)] = sampled
          }
        }
      persistSwatchesAndPalette(updatedSwatches, state.customPalette)
      state.copy(
        selectedColor = sampled,
        quickSwatches = updatedSwatches,
        isEyedropperActive = false,
        statusToastMessage = "Sampled color to toolbar slot",
      )
    }
  }

  private fun updateCurrentPage(
    state: ProNotesUiState,
    transform: (NotePage) -> NotePage,
  ): Pair<List<NotebookDocument>, NotebookDocument?> {
    val docId = state.activeTabId
    var modifiedDoc: NotebookDocument? = null
    val list =
      state.notebooks.map { nb ->
        if (nb.id != docId) nb
        else {
          val idx = nb.currentPageIndex.coerceIn(0, (nb.pages.size - 1).coerceAtLeast(0))
          val updatedPages = nb.pages.toMutableList()
          updatedPages[idx] = transform(updatedPages[idx])
          nb.copy(pages = updatedPages, timestamp = currentTimestamp()).also { modifiedDoc = it }
        }
      }
    return list to modifiedDoc
  }

  /**
   * Accurate segment-based & bounding-box hit test so the Eraser reliably erases
   * straight lines, arrows, rectangles, circles, triangles, polygons, stars, and freehand strokes!
   */
  private fun isStrokeHitByPoint(stroke: DrawnStroke, target: Offset, radius: Float): Boolean {
    val effectiveRadius = radius + (stroke.strokeWidth * 0.5f)
    if (stroke.points.isEmpty()) return false
    if (stroke.points.size == 1) {
      return (stroke.points.first() - target).getDistance() <= effectiveRadius
    }
    for (i in 0 until stroke.points.size - 1) {
      val a = stroke.points[i]
      val b = stroke.points[i + 1]
      if (distanceToSegment(target, a, b) <= effectiveRadius) {
        return true
      }
    }
    if (stroke.isShape && stroke.shapeFill) {
      val minX = stroke.points.minOf { it.x }
      val maxX = stroke.points.maxOf { it.x }
      val minY = stroke.points.minOf { it.y }
      val maxY = stroke.points.maxOf { it.y }
      if (target.x in minX..maxX && target.y in minY..maxY) {
        return true
      }
    }
    return false
  }

  private fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val lenSq = dx * dx + dy * dy
    if (lenSq == 0f) return (p - a).getDistance()
    val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / lenSq).coerceIn(0f, 1f)
    val proj = Offset(a.x + t * dx, a.y + t * dy)
    return (p - proj).getDistance()
  }

  private fun densifyPolyline(vertices: List<Offset>, stepPx: Float = 8f): List<Offset> {
    if (vertices.size < 2) return vertices
    val result = mutableListOf<Offset>()
    for (i in 0 until vertices.size - 1) {
      val a = vertices[i]
      val b = vertices[i + 1]
      val dist = (b - a).getDistance()
      val steps = max(1, (dist / stepPx).toInt())
      for (s in 0 until steps) {
        val t = s.toFloat() / steps.toFloat()
        result.add(Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t))
      }
    }
    result.add(vertices.last())
    return result
  }

  fun eraseAtPoints(points: List<Offset>, forceTemporaryEraser: Boolean = false) {
    if (points.isEmpty()) return
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      if (state.isReadOnlyMode) return@update state
      val mode = state.eraserMode
      val hitRadius = state.eraserRadius
      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { page ->
          val nextStrokes =
            when (mode) {
              EraserMode.HIGHLIGHTER_ONLY -> {
                page.strokes.filterNot { stroke ->
                  stroke.isHighlighter &&
                    points.any { erasePt -> isStrokeHitByPoint(stroke, erasePt, hitRadius) }
                }
              }

              EraserMode.PRECISION -> {
                page.strokes.flatMap { stroke ->
                  val densePts =
                    if (stroke.isShape || stroke.points.size < 12) {
                      densifyPolyline(stroke.points, 7f)
                    } else {
                      stroke.points
                    }
                  val hitsAny =
                    points.any { erasePt -> isStrokeHitByPoint(stroke, erasePt, hitRadius * 0.85f) }
                  if (!hitsAny) {
                    listOf(stroke)
                  } else {
                    val segments = mutableListOf<List<Offset>>()
                    var currentSegment = mutableListOf<Offset>()
                    for (pt in densePts) {
                      val erased =
                        points.any { erasePt -> (pt - erasePt).getDistance() <= hitRadius * 0.85f }
                      if (!erased) {
                        currentSegment.add(pt)
                      } else {
                        if (currentSegment.size >= 2) {
                          segments.add(currentSegment)
                        }
                        currentSegment = mutableListOf()
                      }
                    }
                    if (currentSegment.size >= 2) {
                      segments.add(currentSegment)
                    }
                    segments.map { segPts ->
                      stroke.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        points = segPts,
                        isShape = false,
                        shapeKind = null,
                        shapeFill = false,
                        shapeControlPoints = emptyList(),
                      )
                    }
                  }
                }
              }

              EraserMode.STROKE -> {
                page.strokes.filterNot { stroke ->
                  points.any { erasePt -> isStrokeHitByPoint(stroke, erasePt, hitRadius) }
                }
              }
            }
          val nextTapes =
            if (mode == EraserMode.HIGHLIGHTER_ONLY) {
              page.studyTapes
            } else {
              page.studyTapes.filterNot { tape ->
                points.any { erasePt ->
                  distanceFromPointToSegment(erasePt, tape.start, tape.end) <= (hitRadius + tape.thickness / 2f)
                }
              }
            }
          page.copy(strokes = nextStrokes, studyTapes = nextTapes)
        }
      savedDoc = modifiedDoc

      val nextTool =
        if (!forceTemporaryEraser && state.primaryTool == PrimaryTool.ERASER && state.eraserAutoDeselect) {
          PrimaryTool.WRITING_TOOLS
        } else {
          state.primaryTool
        }
      state.copy(
        notebooks = updatedNotebooks,
        primaryTool = nextTool,
        activeShapeEditStrokeId = null,
      )
    }
    persistDocumentAsync(savedDoc)
  }

  /**
   * Computes the exact IDs of all strokes, shapes, text boxes, elements, sticky notes, and images
   * enclosed by [loopPoints] on [page], respecting [filters].
   */
  private fun computeLassoSelection(
    loopPoints: List<Offset>,
    page: NotePage?,
    filters: LassoFilterSettings,
  ): ComputedLassoSelection {
    if (page == null || loopPoints.size < 3) {
      return ComputedLassoSelection(emptySet(), emptySet(), emptySet(), emptySet(), emptySet())
    }
    val shape = LassoShape(loopPoints)

    val strokeIds =
      if (filters.includeHandwriting) {
        page.strokes
          .filter { stroke ->
            shape.fractionInside(stroke.points) >= 0.35f || stroke.points.any { shape.contains(it) }
          }
          .map { it.id }
          .toSet()
      } else emptySet()

    val textBoxIds =
      if (filters.includeTextBoxes) {
        page.textBoxes
          .filter { tb ->
            shape.contains(tb.position) || shape.contains(tb.position + Offset(40f, 14f))
          }
          .map { it.id }
          .toSet()
      } else emptySet()

    val elementIds =
      if (filters.includeHandwriting || filters.includeImages) {
        page.elements
          .filter { el ->
            shape.contains(el.position) || shape.contains(el.position + Offset(36f, 16f))
          }
          .map { it.id }
          .toSet()
      } else emptySet()

    val stickyIds =
      if (filters.includeComments) {
        page.stickyNotes
          .filter { sn ->
            shape.contains(sn.position) || shape.contains(sn.position + Offset(55f, 35f))
          }
          .map { it.id }
          .toSet()
      } else emptySet()

    val imageIds =
      if (filters.includeImages) {
        page.images
          .filter { img ->
            shape.contains(img.position) ||
              shape.contains(img.position + Offset(img.widthDp * pxPerDp * 0.5f, img.heightDp * pxPerDp * 0.5f))
          }
          .map { it.id }
          .toSet()
      } else emptySet()

    return ComputedLassoSelection(
      strokeIds = strokeIds,
      textBoxIds = textBoxIds,
      elementIds = elementIds,
      stickyIds = stickyIds,
      imageIds = imageIds,
    )
  }

  fun tryConvertDrawnCircleStrokeToLassoAtHold(holdPoint: Offset): Boolean {
    val state = _uiState.value
    if (!state.circleToLasso || state.isReadOnlyMode) return false
    val isPenOrPencil =
      state.primaryTool == PrimaryTool.WRITING_TOOLS &&
        (state.activeWritingSubTool == WritingSubTool.PEN || state.activeWritingSubTool == WritingSubTool.PENCIL)
    if (!isPenOrPencil) return false
    val page = state.notebooks.find { it.id == state.activeTabId }?.currentPage ?: return false

    // Must be the stroke that was JUST drawn, not any old circle on the page
    val circle = page.strokes.maxByOrNull { it.timestampMs } ?: return false
    if (System.currentTimeMillis() - circle.timestampMs > 4000L) return false
    if (circle.isHighlighter || circle.isShape || !isClosedCircleLoop(circle.points)) return false
    if (!LassoShape(circle.points).contains(holdPoint) && !isStrokeHitByPoint(circle, holdPoint, 48f)) return false

    val remaining = page.copy(strokes = page.strokes.filterNot { it.id == circle.id })
    val selection = computeLassoSelection(circle.points, remaining, state.lassoFilters)
    if (selection.totalCount == 0) return false

    var savedDoc: NotebookDocument? = null
    _uiState.update { s ->
      val (nbs, doc) = updateCurrentPage(s) { p -> p.copy(strokes = p.strokes.filterNot { it.id == circle.id }) }
      savedDoc = doc
      s.copy(
        notebooks = nbs,
        primaryTool = PrimaryTool.LASSO,
        showLassoSelection = true,
        isLassoResizeMode = false,
        lassoLoopPoints = circle.points,
        lassoSelectedStrokeIds = selection.strokeIds,
        lassoSelectedTextBoxIds = selection.textBoxIds,
        lassoSelectedElementIds = selection.elementIds,
        lassoSelectedStickyIds = selection.stickyIds,
        lassoSelectedImageIds = selection.imageIds,
        statusToastMessage = "${selection.totalCount} item${if (selection.totalCount == 1) "" else "s"} selected",
      )
    }
    persistDocumentAsync(savedDoc)
    return true
  }

  fun triggerInstantCircleToLasso(loopPoints: List<Offset>): Boolean {
    if (!isClosedCircleLoop(loopPoints)) return false
    val state = _uiState.value
    val page = state.notebooks.find { it.id == state.activeTabId }?.currentPage
    val selection = computeLassoSelection(loopPoints, page, state.lassoFilters)
    if (selection.totalCount == 0) return false
    _uiState.update { s ->
      s.copy(
        primaryTool = PrimaryTool.LASSO,
        showLassoSelection = true,
        isLassoResizeMode = false,
        lassoLoopPoints = loopPoints,
        lassoSelectedStrokeIds = selection.strokeIds,
        lassoSelectedTextBoxIds = selection.textBoxIds,
        lassoSelectedElementIds = selection.elementIds,
        lassoSelectedStickyIds = selection.stickyIds,
        lassoSelectedImageIds = selection.imageIds,
        statusToastMessage = "${selection.totalCount} item${if (selection.totalCount == 1) "" else "s"} selected",
      )
    }
    return true
  }

  /**
   * Moves all items enclosed by the active Lasso selection in real time when the user drags inside the Lasso!
   */
  fun moveLassoSelection(delta: Offset) {
    if (delta == Offset.Zero) return
    _uiState.update { state ->
      if (!state.showLassoSelection) return@update state
      val selectedStrokes = state.lassoSelectedStrokeIds
      val selectedTexts = state.lassoSelectedTextBoxIds
      val selectedElements = state.lassoSelectedElementIds
      val selectedStickies = state.lassoSelectedStickyIds
      val selectedImages = state.lassoSelectedImageIds

      val (updatedNotebooks, _) =
        updateCurrentPage(state) { page ->
          page.copy(
            strokes =
              page.strokes.map { stroke ->
                if (selectedStrokes.contains(stroke.id)) {
                  stroke.copy(
                    points = stroke.points.map { it + delta },
                    shapeControlPoints = stroke.shapeControlPoints.map { it + delta },
                  )
                } else stroke
              },
            textBoxes =
              page.textBoxes.map { tb ->
                if (selectedTexts.contains(tb.id)) tb.copy(position = tb.position + delta) else tb
              },
            elements =
              page.elements.map { el ->
                if (selectedElements.contains(el.id)) el.copy(position = el.position + delta) else el
              },
            stickyNotes =
              page.stickyNotes.map { sn ->
                if (selectedStickies.contains(sn.id)) sn.copy(position = sn.position + delta) else sn
              },
            images =
              page.images.map { img ->
                if (selectedImages.contains(img.id)) img.copy(position = img.position + delta) else img
              },
          )
        }
      state.copy(
        notebooks = updatedNotebooks,
        lassoLoopPoints = state.lassoLoopPoints.map { it + delta },
      )
    }
  }

  fun toggleLassoResizeMode() {
    _uiState.update { state ->
      val next = !state.isLassoResizeMode
      state.copy(
        isLassoResizeMode = next,
        statusToastMessage =
          if (next) "Resize/Rotate Mode: Drag bottom-right blue handle to scale & rotate"
          else null,
      )
    }
  }

  /**
   * Scales and rotates all Lasso-selected items around the selection centroid in real time!
   */
  fun transformLassoSelection(scaleFactor: Float, rotationDeltaDegrees: Float) {
    val clampedScale = scaleFactor.coerceIn(0.85f, 1.18f)
    val rad = Math.toRadians(rotationDeltaDegrees.toDouble())
    val cosA = cos(rad).toFloat()
    val sinA = sin(rad).toFloat()

    _uiState.update { state ->
      if (!state.showLassoSelection || state.lassoLoopPoints.isEmpty()) return@update state
      val cx = state.lassoLoopPoints.map { it.x }.average().toFloat()
      val cy = state.lassoLoopPoints.map { it.y }.average().toFloat()

      fun transformPoint(pt: Offset): Offset {
        val dx = (pt.x - cx) * clampedScale
        val dy = (pt.y - cy) * clampedScale
        return Offset(
          x = cx + dx * cosA - dy * sinA,
          y = cy + dx * sinA + dy * cosA,
        )
      }

      val selectedStrokes = state.lassoSelectedStrokeIds
      val selectedTexts = state.lassoSelectedTextBoxIds
      val selectedElements = state.lassoSelectedElementIds
      val selectedStickies = state.lassoSelectedStickyIds
      val selectedImages = state.lassoSelectedImageIds

      val (updatedNotebooks, _) =
        updateCurrentPage(state) { page ->
          page.copy(
            strokes =
              page.strokes.map { stroke ->
                if (selectedStrokes.contains(stroke.id)) {
                  stroke.copy(
                    points = stroke.points.map(::transformPoint),
                    shapeControlPoints = stroke.shapeControlPoints.map(::transformPoint),
                  )
                } else stroke
              },
            textBoxes =
              page.textBoxes.map { tb ->
                if (selectedTexts.contains(tb.id)) tb.copy(position = transformPoint(tb.position))
                else tb
              },
            elements =
              page.elements.map { el ->
                if (selectedElements.contains(el.id)) el.copy(position = transformPoint(el.position))
                else el
              },
            stickyNotes =
              page.stickyNotes.map { sn ->
                if (selectedStickies.contains(sn.id)) sn.copy(position = transformPoint(sn.position))
                else sn
              },
            images =
              page.images.map { img ->
                if (selectedImages.contains(img.id)) {
                  img.copy(
                    position = transformPoint(img.position),
                    widthDp = (img.widthDp * clampedScale).coerceIn(60f, 480f),
                    heightDp = (img.heightDp * clampedScale).coerceIn(50f, 420f),
                    rotationDegrees = img.rotationDegrees + rotationDeltaDegrees,
                  )
                } else img
              },
          )
        }
      state.copy(
        notebooks = updatedNotebooks,
        lassoLoopPoints = state.lassoLoopPoints.map(::transformPoint),
      )
    }
  }

  fun commitLassoChanges() {
    val s = _uiState.value
    persistDocumentAsync(s.notebooks.find { it.id == s.activeTabId })
  }

  /** Clears the selection but stays in the Lasso tool (tap outside). */
  fun clearLassoSelection() {
    _uiState.update {
      it.copy(
        showLassoSelection = false,
        isLassoResizeMode = false,
        isLassoColorMenuOpen = false,
        lassoLoopPoints = emptyList(),
        lassoSelectedStrokeIds = emptySet(),
        lassoSelectedTextBoxIds = emptySet(),
        lassoSelectedElementIds = emptySet(),
        lassoSelectedStickyIds = emptySet(),
        lassoSelectedImageIds = emptySet(),
      )
    }
  }

  fun toggleLassoColorMenu() {
    _uiState.update { it.copy(isLassoColorMenuOpen = !it.isLassoColorMenuOpen) }
  }

  fun recolorLassoSelection(color: Color) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val selectedStrokes = state.lassoSelectedStrokeIds
      val selectedTexts = state.lassoSelectedTextBoxIds
      if (selectedStrokes.isEmpty() && selectedTexts.isEmpty()) return@update state

      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            strokes =
              page.strokes.map { stroke ->
                if (selectedStrokes.contains(stroke.id)) {
                  stroke.copy(
                    color = if (stroke.isHighlighter) color.copy(alpha = 0.38f) else color
                  )
                } else stroke
              },
            textBoxes =
              page.textBoxes.map { tb ->
                if (selectedTexts.contains(tb.id)) tb.copy(color = color) else tb
              },
          )
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updatedNotebooks,
        selectedColor = color,
        isLassoColorMenuOpen = false,
        statusToastMessage = "Recolored selection",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun scaleLassoSelectionStep(scaleUp: Boolean) {
    val factor = if (scaleUp) 1.20f else 0.83f
    transformLassoSelection(scaleFactor = factor, rotationDeltaDegrees = 0f)
    commitLassoChanges()
  }

  fun copyLassoSelection() {
    val state = _uiState.value
    if (!state.showLassoSelection) return
    val currentDoc = state.notebooks.find { it.id == state.activeTabId } ?: return
    val page = currentDoc.currentPage

    val strokes = page.strokes.filter { state.lassoSelectedStrokeIds.contains(it.id) }
    val texts = page.textBoxes.filter { state.lassoSelectedTextBoxIds.contains(it.id) }
    val elements = page.elements.filter { state.lassoSelectedElementIds.contains(it.id) }
    val stickies = page.stickyNotes.filter { state.lassoSelectedStickyIds.contains(it.id) }
    val images = page.images.filter { state.lassoSelectedImageIds.contains(it.id) }

    val total = strokes.size + texts.size + elements.size + stickies.size + images.size
    if (total == 0) return

    val cx = if (state.lassoLoopPoints.isNotEmpty()) state.lassoLoopPoints.map { it.x }.average().toFloat() else 0f
    val cy = if (state.lassoLoopPoints.isNotEmpty()) state.lassoLoopPoints.map { it.y }.average().toFloat() else 0f

    val clipboard =
      LassoClipboard(
        strokes = strokes,
        textBoxes = texts,
        elements = elements,
        stickyNotes = stickies,
        images = images,
        originalCentroid = Offset(cx, cy),
      )

    _uiState.update {
      it.copy(
        lassoClipboard = clipboard,
        statusToastMessage = "Copied $total item${if (total == 1) "" else "s"} to clipboard",
      )
    }
  }

  fun cutLassoSelection() {
    copyLassoSelection()
    deleteLassoSelection()
    _uiState.update { it.copy(statusToastMessage = "Cut selection to clipboard") }
  }

  fun pasteLassoClipboard(targetPosition: Offset? = null) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val clipboard = state.lassoClipboard ?: return@update state.copy(statusToastMessage = "Clipboard is empty")
      val total = clipboard.strokes.size + clipboard.textBoxes.size + clipboard.elements.size + clipboard.stickyNotes.size + clipboard.images.size
      if (total == 0) return@update state.copy(statusToastMessage = "Clipboard is empty")

      val shift =
        if (targetPosition != null && clipboard.originalCentroid != Offset.Zero) {
          targetPosition - clipboard.originalCentroid
        } else {
          Offset(34f, 34f)
        }

      val newStrokeIds = mutableSetOf<String>()
      val newTextIds = mutableSetOf<String>()
      val newElementIds = mutableSetOf<String>()
      val newStickyIds = mutableSetOf<String>()
      val newImageIds = mutableSetOf<String>()

      val pastedStrokes =
        clipboard.strokes.map { s ->
          val newId = java.util.UUID.randomUUID().toString()
          newStrokeIds.add(newId)
          s.copy(
            id = newId,
            points = s.points.map { it + shift },
            shapeControlPoints = s.shapeControlPoints.map { it + shift },
          )
        }

      val pastedTexts =
        clipboard.textBoxes.map { tb ->
          val newId = java.util.UUID.randomUUID().toString()
          newTextIds.add(newId)
          tb.copy(id = newId, position = tb.position + shift)
        }

      val pastedElements =
        clipboard.elements.map { el ->
          val newId = java.util.UUID.randomUUID().toString()
          newElementIds.add(newId)
          el.copy(id = newId, position = el.position + shift)
        }

      val pastedStickies =
        clipboard.stickyNotes.map { sn ->
          val newId = java.util.UUID.randomUUID().toString()
          newStickyIds.add(newId)
          sn.copy(id = newId, position = sn.position + shift)
        }

      val pastedImages =
        clipboard.images.map { img ->
          val newId = java.util.UUID.randomUUID().toString()
          newImageIds.add(newId)
          img.copy(id = newId, position = img.position + shift)
        }

      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            strokes = page.strokes + pastedStrokes,
            textBoxes = page.textBoxes + pastedTexts,
            elements = page.elements + pastedElements,
            stickyNotes = page.stickyNotes + pastedStickies,
            images = page.images + pastedImages,
          )
        }
      savedDoc = modifiedDoc

      val newLassoLoop =
        if (state.lassoLoopPoints.isNotEmpty()) {
          state.lassoLoopPoints.map { it + shift }
        } else if (pastedStrokes.isNotEmpty()) {
          val allPts = pastedStrokes.flatMap { it.points }
          val minX = allPts.minOf { it.x } - 14f
          val maxX = allPts.maxOf { it.x } + 14f
          val minY = allPts.minOf { it.y } - 14f
          val maxY = allPts.maxOf { it.y } + 14f
          listOf(Offset(minX, minY), Offset(maxX, minY), Offset(maxX, maxY), Offset(minX, maxY))
        } else {
          emptyList()
        }

      state.copy(
        notebooks = updatedNotebooks,
        primaryTool = PrimaryTool.LASSO,
        showLassoSelection = true,
        isLassoResizeMode = false,
        isLassoColorMenuOpen = false,
        lassoLoopPoints = newLassoLoop,
        lassoSelectedStrokeIds = newStrokeIds,
        lassoSelectedTextBoxIds = newTextIds,
        lassoSelectedElementIds = newElementIds,
        lassoSelectedStickyIds = newStickyIds,
        lassoSelectedImageIds = newImageIds,
        statusToastMessage = "Pasted $total item${if (total == 1) "" else "s"}",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun dismissLassoSelection() {
    _uiState.update {
      it.copy(
        showLassoSelection = false,
        isLassoResizeMode = false,
        isLassoColorMenuOpen = false,
        lassoLoopPoints = emptyList(),
        lassoSelectedStrokeIds = emptySet(),
        lassoSelectedTextBoxIds = emptySet(),
        lassoSelectedElementIds = emptySet(),
        lassoSelectedStickyIds = emptySet(),
        lassoSelectedImageIds = emptySet(),
        primaryTool = if (it.primaryTool == PrimaryTool.LASSO) PrimaryTool.WRITING_TOOLS else it.primaryTool,
      )
    }
  }

  fun addStroke(
    points: List<Offset>,
    pressures: List<Float> = emptyList(),
    snappedFromHold: Boolean = false,
  ) {
    if (points.size < 2) return
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      if (state.isReadOnlyMode) return@update state
      val docId = state.activeTabId
      val currentDoc = state.notebooks.find { it.id == docId }

      if (state.primaryTool == PrimaryTool.ERASER) {
        return@update state
      }

      // Direct Lasso Tool: compute exact enclosed items inside the drawn lasso loop
      if (state.primaryTool == PrimaryTool.LASSO) {
        val diag = hypot(points.maxOf { it.x } - points.minOf { it.x }, points.maxOf { it.y } - points.minOf { it.y })
        if (diag < 24f) {
          return@update state.copy(
            showLassoSelection = false,
            lassoLoopPoints = emptyList(),
            lassoSelectedStrokeIds = emptySet(),
            lassoSelectedTextBoxIds = emptySet(),
            lassoSelectedElementIds = emptySet(),
            lassoSelectedStickyIds = emptySet(),
            lassoSelectedImageIds = emptySet(),
          )
        }
        val selection = computeLassoSelection(points, currentDoc?.currentPage, state.lassoFilters)
        return@update state.copy(
          showLassoSelection = true,
          isLassoResizeMode = false,
          isLassoColorMenuOpen = false,
          lassoLoopPoints = points,
          lassoSelectedStrokeIds = selection.strokeIds,
          lassoSelectedTextBoxIds = selection.textBoxIds,
          lassoSelectedElementIds = selection.elementIds,
          lassoSelectedStickyIds = selection.stickyIds,
          lassoSelectedImageIds = selection.imageIds,
          statusToastMessage =
            if (selection.totalCount > 0) {
              "Selected ${selection.totalCount} item${if (selection.totalCount == 1) "" else "s"} — drag inside lasso to move"
            } else {
              "Lasso loop active (no items inside loop)"
            },
        )
      }

      val isWritingToolsActive = state.primaryTool == PrimaryTool.WRITING_TOOLS
      val subTool = state.activeWritingSubTool

      // Study Tape Sub-Tool inside Writing Tools
      if (isWritingToolsActive && subTool == WritingSubTool.TAPE) {
        val tape =
          StudyTapeItem(
            start = points.first(),
            end = points.last(),
            color = state.selectedColor,
            thickness = 38f,
            isRevealed = false,
          )
        val (updated, modifiedDoc) =
          updateCurrentPage(state) { page ->
            page.copy(studyTapes = page.studyTapes + tape)
          }
        savedDoc = modifiedDoc
        return@update state.copy(
          notebooks = updated,
          statusToastMessage = "Added Study Tape (tap strip to reveal/conceal)",
        )
      }

      val isPenOrPencil =
        isWritingToolsActive && (subTool == WritingSubTool.PEN || subTool == WritingSubTool.PENCIL)

      // 2) Check "Scribble to Erase" Gesture when writing with Pen or Pencil
      if (
        state.scribbleToErase &&
          isPenOrPencil &&
          !snappedFromHold &&
          isScribbleEraseGesture(points)
      ) {
        val (updated, modifiedDoc) =
          updateCurrentPage(state) { page ->
            val minX = points.minOf { it.x } - 22f
            val maxX = points.maxOf { it.x } + 22f
            val minY = points.minOf { it.y } - 22f
            val maxY = points.maxOf { it.y } + 22f
            page.copy(
              strokes =
                page.strokes.filterNot { s ->
                  s.points.any { pt -> pt.x in minX..maxX && pt.y in minY..maxY }
                }
            )
          }
        savedDoc = modifiedDoc
        return@update state.copy(
          notebooks = updated,
          statusToastMessage = "Scribble to Erase removed stroke",
        )
      }

      val isHighlighter = isWritingToolsActive && subTool == WritingSubTool.HIGHLIGHTER
      val isPencil = isWritingToolsActive && subTool == WritingSubTool.PENCIL
      val isExplicitShapeTool =
        state.primaryTool == PrimaryTool.SHAPE ||
          (isWritingToolsActive && subTool == WritingSubTool.DRAW_SHAPE)
      val isShapeStroke = (isExplicitShapeTool || snappedFromHold) && !isHighlighter

      // Highlighter snaps to a crisp straight line when either "Draw in Straight Line" is ON OR when held at the end!
      val finalPoints =
        if (isHighlighter && (state.highlighterStraightLine || snappedFromHold)) {
          densifyPolyline(listOf(points.first(), points.last()), 8f)
        } else {
          smoothPoints(points, state.strokeStabilization)
        }

      val baseWidth =
        state.thicknessPresets.getOrElse(state.selectedThicknessIndex) { 6.5f }

      // Auto-decide geometric shape from the user's freehand stroke (both in Shapes Tool & Draw-and-Hold!)
      val detectedShapeKind =
        if (isShapeStroke) {
          autoDetectShapeKind(finalPoints)
        } else null

      val (geomPoints, controlPts) =
        if (detectedShapeKind != null) {
          buildGeometricShapeWithHandles(finalPoints, detectedShapeKind)
        } else {
          finalPoints to emptyList()
        }

      val strokeId = java.util.UUID.randomUUID().toString()
      val stroke =
        DrawnStroke(
          id = strokeId,
          points = geomPoints,
          pressures = pressures,
          color =
            when {
              isHighlighter -> state.selectedColor.copy(alpha = 0.38f)
              isPencil -> state.selectedColor.copy(alpha = 0.78f)
              else -> state.selectedColor
            },
          strokeWidth =
            when {
              isHighlighter -> baseWidth * 3.4f
              isPencil -> baseWidth * 0.9f
              else -> baseWidth
            },
          penStyle = state.penStyle,
          strokePattern = state.strokePattern,
          isHighlighter = isHighlighter,
          isPencil = isPencil,
          isShape = detectedShapeKind != null,
          shapeKind = detectedShapeKind,
          shapeFill =
            isShapeStroke &&
              state.fillShapes &&
              detectedShapeKind != ShapeKind.LINE &&
              detectedShapeKind != ShapeKind.ARROW &&
              detectedShapeKind != ShapeKind.CURVE,
          shapeControlPoints = controlPts,
          zIndex = if (isHighlighter) -1 else 1,
        )

      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(strokes = page.strokes + stroke)
        }
      savedDoc = modifiedDoc
      val updatedRedo = state.redoStack.toMutableMap().apply { remove(docId) }
      state.copy(
        notebooks = updatedNotebooks,
        redoStack = updatedRedo,
        selectedShapeKind = detectedShapeKind ?: state.selectedShapeKind,
        lastAutoDetectedShape = detectedShapeKind ?: state.lastAutoDetectedShape,
        activeShapeEditStrokeId = if (detectedShapeKind != null) strokeId else null,
        statusToastMessage =
          when {
            detectedShapeKind != null -> "Auto-snapped to ${detectedShapeKind.label}"
            isHighlighter && snappedFromHold -> "Snapped Highlighter to Straight Line"
            else -> state.statusToastMessage
          },
      )
    }
    persistDocumentAsync(savedDoc)
  }

  /**
   * Updates a vertex control handle on an existing snapped shape so the user can resize/reshape
   * lines, arrows, triangles, rectangles, circles, polygons, stars, and curves after drawing!
   */
  fun moveShapeControlHandle(strokeId: String, handleIndex: Int, delta: Offset) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            strokes =
              page.strokes.map { stroke ->
                if (stroke.id != strokeId || !stroke.isShape || stroke.shapeKind == null) {
                  stroke
                } else {
                  val updatedHandles = stroke.shapeControlPoints.toMutableList()
                  if (handleIndex in updatedHandles.indices) {
                    updatedHandles[handleIndex] = updatedHandles[handleIndex] + delta
                  }
                  val rebuiltPoints =
                    rebuildShapeFromControlHandles(updatedHandles, stroke.shapeKind)
                  stroke.copy(
                    points = rebuiltPoints,
                    shapeControlPoints = updatedHandles,
                  )
                }
              }
          )
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updatedNotebooks)
    }
    persistDocumentAsync(savedDoc)
  }

  private fun isScribbleEraseGesture(points: List<Offset>): Boolean {
    if (points.size < 18) return false
    var xReversals = 0
    var lastDx = 0f
    var pathLength = 0f
    for (i in 1 until points.size) {
      val dx = points[i].x - points[i - 1].x
      val dy = points[i].y - points[i - 1].y
      pathLength += hypot(dx, dy)
      if (abs(dx) > 4f) {
        if (lastDx != 0f && (dx * lastDx) < 0f) {
          xReversals++
        }
        lastDx = dx
      }
    }
    val endToEnd = (points.last() - points.first()).getDistance()
    return xReversals >= 4 && pathLength > endToEnd * 3.2f
  }

  /**
   * Upgraded 11-Shape Goodnotes 6 Auto-Deciding Geometric Shape Recognition Engine:
   * Analyzes any freehand stroke and automatically recognizes:
   * - LINE (Straight Line)
   * - ARROW (Straight Line with end arrowhead hook)
   * - CURVE (Smooth Quadratic/Cubic Arc)
   * - CIRCLE (Closed loop with near-1:1 aspect ratio and low radial variance)
   * - OVAL (Closed loop with elongated aspect ratio and low radial variance)
   * - TRIANGLE (3 sharp corners — preserves actual drawn vertex orientations!)
   * - RECTANGLE (4 axis-aligned corners)
   * - DIAMOND (4 corners oriented near top/right/bottom/left midpoints)
   * - PENTAGON (5 corners)
   * - HEXAGON (6 corners)
   * - STAR (5-pointed star with alternating radial peaks)
   */
  fun autoDetectShapeKind(points: List<Offset>): ShapeKind {
    if (points.size < 3) return ShapeKind.LINE
    val start = points.first()
    val end = points.last()
    val directDist = (end - start).getDistance()
    var pathLen = 0f
    for (i in 1 until points.size) {
      pathLen += (points[i] - points[i - 1]).getDistance()
    }
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }
    val width = maxX - minX
    val height = maxY - minY
    val diag = hypot(width, height).coerceAtLeast(1f)

    // 1. Open Stroke (Start and End are far apart) -> Line, Arrow, or Curved Arc
    if (directDist > diag * 0.38f && pathLen < diag * 2.20f) {
      return if (pathLen <= directDist * 1.16f) {
        ShapeKind.LINE
      } else {
        val eightyIdx = (points.size * 0.78f).toInt().coerceIn(1, points.lastIndex)
        val mainTip = points[eightyIdx]
        val mainDirect = (mainTip - start).getDistance().coerceAtLeast(1f)
        var mainPath = 0f
        for (i in 1..eightyIdx) {
          mainPath += (points[i] - points[i - 1]).getDistance()
        }
        val tailLen = pathLen - mainPath
        if (mainPath <= mainDirect * 1.15f && tailLen in 12f..(diag * 0.48f)) {
          ShapeKind.ARROW
        } else {
          ShapeKind.CURVE
        }
      }
    }

    // 2. Closed Contour -> Analyze radial circularity, aspect ratio, and corner vertices
    val cx = (minX + maxX) / 2f
    val cy = (minY + maxY) / 2f
    val rx = (width / 2f).coerceAtLeast(12f)
    val ry = (height / 2f).coerceAtLeast(12f)
    val aspect = width / height.coerceAtLeast(1f)

    val normRadii =
      points.map { pt ->
        hypot((pt.x - cx) / rx, (pt.y - cy) / ry)
      }
    val meanR = normRadii.average().toFloat().coerceAtLeast(0.01f)
    val varianceR =
      normRadii.map { r -> (r - meanR) * (r - meanR) }.average().toFloat()
    val stdR = sqrt(varianceR) / meanR

    val closedPts =
      if ((points.last() - points.first()).getDistance() > diag * 0.12f) {
        points + points.first()
      } else {
        points
      }
    val rawSimplified = simplifyPolygon(closedPts, epsilon = diag * 0.065f).dropLast(1)
    val mergedCorners = mutableListOf<Offset>()
    val minCornerGap = diag * 0.135f
    for (pt in rawSimplified) {
      if (mergedCorners.isEmpty() || (pt - mergedCorners.last()).getDistance() >= minCornerGap) {
        mergedCorners.add(pt)
      }
    }
    if (
      mergedCorners.size >= 2 &&
        (mergedCorners.last() - mergedCorners.first()).getDistance() < minCornerGap
    ) {
      mergedCorners.removeAt(mergedCorners.lastIndex)
    }

    var sharpCornerCount = 0
    if (mergedCorners.size >= 3) {
      for (i in mergedCorners.indices) {
        val prev = mergedCorners[(i - 1 + mergedCorners.size) % mergedCorners.size]
        val curr = mergedCorners[i]
        val next = mergedCorners[(i + 1) % mergedCorners.size]
        val v1 = prev - curr
        val v2 = next - curr
        val mag = (v1.getDistance() * v2.getDistance()).coerceAtLeast(0.001f)
        val cosTheta = ((v1.x * v2.x + v1.y * v2.y) / mag).coerceIn(-1f, 1f)
        val angleDeg = Math.toDegrees(kotlin.math.acos(cosTheta.toDouble()))
        if (angleDeg < 146.0) {
          sharpCornerCount++
        }
      }
    }

    // Check if 4-corner shape is oriented as a Diamond (corners near top/right/bottom/left midpoints)
    val isDiamondOrientation =
      if (mergedCorners.size == 4) {
        val midTargets =
          listOf(
            Offset(cx, minY),
            Offset(maxX, cy),
            Offset(cx, maxY),
            Offset(minX, cy),
          )
        val boxCorners =
          listOf(
            Offset(minX, minY),
            Offset(maxX, minY),
            Offset(maxX, maxY),
            Offset(minX, maxY),
          )
        val distToMid =
          mergedCorners.sumOf { c -> midTargets.minOf { (c - it).getDistance() }.toDouble() }
        val distToBox =
          mergedCorners.sumOf { c -> boxCorners.minOf { (c - it).getDistance() }.toDouble() }
        distToMid < distToBox * 0.85
      } else false

    return when {
      stdR < 0.086f && sharpCornerCount != 3 && sharpCornerCount != 4 -> {
        if (aspect in 0.78f..1.28f) ShapeKind.CIRCLE else ShapeKind.OVAL
      }
      sharpCornerCount == 3 || mergedCorners.size == 3 -> ShapeKind.TRIANGLE
      sharpCornerCount == 4 || (mergedCorners.size == 4 && stdR >= 0.080f) -> {
        if (isDiamondOrientation) ShapeKind.DIAMOND else ShapeKind.RECTANGLE
      }
      sharpCornerCount == 5 || mergedCorners.size == 5 -> ShapeKind.PENTAGON
      sharpCornerCount == 6 || mergedCorners.size == 6 -> ShapeKind.HEXAGON
      sharpCornerCount >= 7 && stdR >= 0.20f -> ShapeKind.STAR
      else -> if (aspect in 0.78f..1.28f) ShapeKind.CIRCLE else ShapeKind.OVAL
    }
  }

  private fun simplifyPolygon(points: List<Offset>, epsilon: Float): List<Offset> {
    if (points.size < 3) return points
    var maxDist = 0f
    var index = 0
    val first = points.first()
    val last = points.last()
    for (i in 1 until points.size - 1) {
      val d = distanceToSegment(points[i], first, last)
      if (d > maxDist) {
        index = i
        maxDist = d
      }
    }
    return if (maxDist > epsilon) {
      val left = simplifyPolygon(points.subList(0, index + 1), epsilon)
      val right = simplifyPolygon(points.subList(index, points.size), epsilon)
      left.dropLast(1) + right
    } else {
      listOf(first, last)
    }
  }

  /**
   * Builds both the densely interpolated stroke points AND the draggable Goodnotes vertex handles.
   */
  private fun buildGeometricShapeWithHandles(
    points: List<Offset>,
    kind: ShapeKind,
  ): Pair<List<Offset>, List<Offset>> {
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }
    val cx = (minX + maxX) / 2f
    val cy = (minY + maxY) / 2f
    val rx = ((maxX - minX) / 2f).coerceAtLeast(24f)
    val ry = ((maxY - minY) / 2f).coerceAtLeast(24f)

    val handles: List<Offset> =
      when (kind) {
        ShapeKind.LINE, ShapeKind.ARROW -> listOf(points.first(), points.last())
        ShapeKind.CURVE -> {
          val mid = points[points.size / 2]
          listOf(points.first(), mid, points.last())
        }
        ShapeKind.RECTANGLE ->
          listOf(
            Offset(minX, minY),
            Offset(maxX, minY),
            Offset(maxX, maxY),
            Offset(minX, maxY),
          )
        ShapeKind.DIAMOND ->
          listOf(
            Offset(cx, minY),
            Offset(maxX, cy),
            Offset(cx, maxY),
            Offset(minX, cy),
          )
        ShapeKind.TRIANGLE -> {
          val diag = hypot(maxX - minX, maxY - minY).coerceAtLeast(1f)
          val simplified = simplifyPolygon(points + points.first(), epsilon = diag * 0.08f).dropLast(1)
          if (simplified.size == 3) {
            simplified
          } else {
            listOf(
              Offset(cx, minY),
              Offset(maxX, maxY),
              Offset(minX, maxY),
            )
          }
        }
        ShapeKind.CIRCLE -> {
          val r = ((rx + ry) / 2f).coerceAtLeast(24f)
          listOf(
            Offset(cx, cy - r),
            Offset(cx + r, cy),
            Offset(cx, cy + r),
            Offset(cx - r, cy),
          )
        }
        ShapeKind.OVAL ->
          listOf(
            Offset(cx, minY),
            Offset(maxX, cy),
            Offset(cx, maxY),
            Offset(minX, cy),
          )
        ShapeKind.PENTAGON -> {
          (0 until 5).map { i ->
            val angle = -PI / 2.0 + i * (2.0 * PI / 5.0)
            Offset(cx + (rx * cos(angle)).toFloat(), cy + (ry * sin(angle)).toFloat())
          }
        }
        ShapeKind.HEXAGON -> {
          (0 until 6).map { i ->
            val angle = -PI / 2.0 + i * (2.0 * PI / 6.0)
            Offset(cx + (rx * cos(angle)).toFloat(), cy + (ry * sin(angle)).toFloat())
          }
        }
        ShapeKind.STAR -> {
          (0 until 10).map { i ->
            val rFactor = if (i % 2 == 0) 1.0f else 0.42f
            val angle = -PI / 2.0 + i * (PI / 5.0)
            Offset(
              cx + (rx * rFactor * cos(angle)).toFloat(),
              cy + (ry * rFactor * sin(angle)).toFloat(),
            )
          }
        }
      }

    val densePoints = rebuildShapeFromControlHandles(handles, kind)
    return densePoints to handles
  }

  private fun rebuildShapeFromControlHandles(
    handles: List<Offset>,
    kind: ShapeKind,
  ): List<Offset> {
    if (handles.isEmpty()) return emptyList()
    return when (kind) {
      ShapeKind.LINE -> densifyPolyline(handles, 8f)
      ShapeKind.ARROW -> {
        if (handles.size < 2) return handles
        val start = handles.first()
        val end = handles.last()
        val angle = atan2(end.y - start.y, end.x - start.x)
        val headLen = 24f
        val wing1 =
          Offset(
            end.x - headLen * cos(angle - 0.45f),
            end.y - headLen * sin(angle - 0.45f),
          )
        val wing2 =
          Offset(
            end.x - headLen * cos(angle + 0.45f),
            end.y - headLen * sin(angle + 0.45f),
          )
        densifyPolyline(listOf(start, end, wing1, end, wing2), 7f)
      }
      ShapeKind.CURVE -> {
        if (handles.size < 3) return densifyPolyline(handles, 8f)
        val p0 = handles[0]
        val p1 = handles[1]
        val p2 = handles[2]
        val curvePts =
          (0..32).map { step ->
            val t = step / 32f
            val u = 1f - t
            Offset(
              x = u * u * p0.x + 2f * u * t * p1.x + t * t * p2.x,
              y = u * u * p0.y + 2f * u * t * p1.y + t * t * p2.y,
            )
          }
        densifyPolyline(curvePts, 7f)
      }
      ShapeKind.CIRCLE, ShapeKind.OVAL -> {
        val minX = handles.minOf { it.x }
        val maxX = handles.maxOf { it.x }
        val minY = handles.minOf { it.y }
        val maxY = handles.maxOf { it.y }
        val cx = (minX + maxX) / 2f
        val cy = (minY + maxY) / 2f
        val rx = ((maxX - minX) / 2f).coerceAtLeast(10f)
        val ry = ((maxY - minY) / 2f).coerceAtLeast(10f)
        (0..48).map { step ->
          val angle = (step.toDouble() / 48.0) * 2.0 * PI
          Offset(cx + (rx * cos(angle)).toFloat(), cy + (ry * sin(angle)).toFloat())
        }
      }
      ShapeKind.RECTANGLE,
      ShapeKind.DIAMOND,
      ShapeKind.TRIANGLE,
      ShapeKind.PENTAGON,
      ShapeKind.HEXAGON,
      ShapeKind.STAR -> {
        densifyPolyline(handles + handles.first(), 8f)
      }
    }
  }

  private fun smoothPoints(points: List<Offset>, stabilization: Float): List<Offset> {
    if (stabilization <= 5f || points.size < 4) return points
    val window = if (stabilization > 50f) 3 else 2
    return points.indices.map { i ->
      var sumX = 0f
      var sumY = 0f
      var count = 0
      for (j in (i - window).coerceAtLeast(0)..(i + window).coerceAtMost(points.size - 1)) {
        sumX += points[j].x
        sumY += points[j].y
        count++
      }
      Offset(sumX / count, sumY / count)
    }
  }

  fun toggleStudyTapeReveal(tapeId: String) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            studyTapes =
              page.studyTapes.map { t ->
                if (t.id == tapeId) t.copy(isRevealed = !t.isRevealed) else t
              }
          )
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated)
    }
    persistDocumentAsync(savedDoc)
  }

  fun addTextBoxAt(position: Offset, initialText: String = "Tap to edit note") {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val box =
        CanvasTextBox(
          position = position,
          text = initialText,
          color = state.selectedColor,
        )
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(textBoxes = page.textBoxes + box)
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated)
    }
    persistDocumentAsync(savedDoc)
  }

  fun updateTextBox(id: String, newText: String) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            textBoxes =
              if (newText.isBlank()) page.textBoxes.filterNot { it.id == id }
              else page.textBoxes.map { if (it.id == id) it.copy(text = newText) else it }
          )
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated)
    }
    persistDocumentAsync(savedDoc)
  }

  fun moveTextBox(id: String, delta: Offset) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            textBoxes =
              page.textBoxes.map {
                if (it.id == id) it.copy(position = it.position + delta) else it
              }
          )
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated)
    }
    persistDocumentAsync(savedDoc)
  }

  fun addStickyNoteAt(position: Offset) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val note =
        StickyNoteItem(
          position = position,
          text = "Study Note:\nTap to edit your sticky note.",
        )
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(stickyNotes = page.stickyNotes + note)
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        primaryTool = PrimaryTool.WRITING_TOOLS,
        statusToastMessage = "Placed Sticky Note",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun toggleStickyNoteExpanded(id: String) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            stickyNotes =
              page.stickyNotes.map {
                if (it.id == id) it.copy(isExpanded = !it.isExpanded) else it
              }
          )
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated)
    }
    persistDocumentAsync(savedDoc)
  }

  fun updateStickyNoteText(id: String, text: String) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            stickyNotes =
              page.stickyNotes.map { if (it.id == id) it.copy(text = text) else it }
          )
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated)
    }
    persistDocumentAsync(savedDoc)
  }

  fun deleteStickyNote(id: String) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(stickyNotes = page.stickyNotes.filterNot { it.id == id })
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated)
    }
    persistDocumentAsync(savedDoc)
  }

  fun insertElementStamp(stampType: ElementStampType) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val existingCount =
        state.notebooks.find { it.id == state.activeTabId }?.currentPage?.elements?.size ?: 0
      val item =
        CanvasElementItem(
          position = Offset(110f + (existingCount % 3) * 110f, 160f + (existingCount / 3) * 70f),
          stampType = stampType,
        )
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(elements = page.elements + item)
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Inserted \"${stampType.label}\" element",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun moveElementStamp(id: String, delta: Offset) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            elements =
              page.elements.map {
                if (it.id == id) it.copy(position = it.position + delta) else it
              }
          )
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated)
    }
    persistDocumentAsync(savedDoc)
  }

  fun insertCanvasImage(bitmap: Bitmap) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val img =
        CanvasImageItem(
          position = Offset(90f, 140f),
          bitmap = bitmap,
        )
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(images = page.images + img)
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        statusToastMessage = "Inserted Image onto page",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun moveCanvasImage(id: String, delta: Offset) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            images =
              page.images.map {
                if (it.id == id) it.copy(position = it.position + delta) else it
              }
          )
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated)
    }
    persistDocumentAsync(savedDoc)
  }

  fun bringSelectionToFront() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val selectedStrokes = state.lassoSelectedStrokeIds
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          val maxZ = (page.strokes.maxOfOrNull { it.zIndex } ?: 1) + 1
          page.copy(
            strokes =
              page.strokes.map { stroke ->
                if (selectedStrokes.isEmpty() || selectedStrokes.contains(stroke.id)) {
                  stroke.copy(zIndex = maxZ)
                } else stroke
              }
          )
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Brought selection to front",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun sendSelectionToBack() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val selectedStrokes = state.lassoSelectedStrokeIds
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          val minZ = (page.strokes.minOfOrNull { it.zIndex } ?: 0) - 1
          page.copy(
            strokes =
              page.strokes.map { stroke ->
                if (selectedStrokes.isEmpty() || selectedStrokes.contains(stroke.id)) {
                  stroke.copy(zIndex = minZ)
                } else stroke
              }
          )
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Sent selection to back",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  /**
   * Toggles interactive Resize/Rotate handles on the Lasso selection AND scales the selected items.
   */
  fun scaleSelectedStrokes() {
    toggleLassoResizeMode()
  }

  /**
   * Duplicates ONLY the items inside the active Lasso selection!
   */
  fun duplicateSelectedStrokes() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val selectedStrokes = state.lassoSelectedStrokeIds
      val selectedTexts = state.lassoSelectedTextBoxIds
      val selectedElements = state.lassoSelectedElementIds
      val selectedStickies = state.lassoSelectedStickyIds
      val selectedImages = state.lassoSelectedImageIds
      val offsetShift = Offset(28f, 28f)

      val newStrokeIds = mutableSetOf<String>()
      val newTextIds = mutableSetOf<String>()
      val newElementIds = mutableSetOf<String>()
      val newStickyIds = mutableSetOf<String>()
      val newImageIds = mutableSetOf<String>()

      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          val strokeCopies =
            page.strokes
              .filter { selectedStrokes.isEmpty() || selectedStrokes.contains(it.id) }
              .map { s ->
                val newId = java.util.UUID.randomUUID().toString()
                newStrokeIds.add(newId)
                s.copy(
                  id = newId,
                  points = s.points.map { it + offsetShift },
                  shapeControlPoints = s.shapeControlPoints.map { it + offsetShift },
                )
              }
          val textCopies =
            page.textBoxes
              .filter { selectedTexts.contains(it.id) }
              .map { tb ->
                val newId = java.util.UUID.randomUUID().toString()
                newTextIds.add(newId)
                tb.copy(id = newId, position = tb.position + offsetShift)
              }
          val elementCopies =
            page.elements
              .filter { selectedElements.contains(it.id) }
              .map { el ->
                val newId = java.util.UUID.randomUUID().toString()
                newElementIds.add(newId)
                el.copy(id = newId, position = el.position + offsetShift)
              }
          val stickyCopies =
            page.stickyNotes
              .filter { selectedStickies.contains(it.id) }
              .map { sn ->
                val newId = java.util.UUID.randomUUID().toString()
                newStickyIds.add(newId)
                sn.copy(id = newId, position = sn.position + offsetShift)
              }
          val imageCopies =
            page.images
              .filter { selectedImages.contains(it.id) }
              .map { img ->
                val newId = java.util.UUID.randomUUID().toString()
                newImageIds.add(newId)
                img.copy(id = newId, position = img.position + offsetShift)
              }

          page.copy(
            strokes = page.strokes + strokeCopies,
            textBoxes = page.textBoxes + textCopies,
            elements = page.elements + elementCopies,
            stickyNotes = page.stickyNotes + stickyCopies,
            images = page.images + imageCopies,
          )
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        lassoLoopPoints = state.lassoLoopPoints.map { it + offsetShift },
        lassoSelectedStrokeIds = newStrokeIds,
        lassoSelectedTextBoxIds = newTextIds,
        lassoSelectedElementIds = newElementIds,
        lassoSelectedStickyIds = newStickyIds,
        lassoSelectedImageIds = newImageIds,
        statusToastMessage = "Duplicated selected items",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  /**
   * Deletes ONLY the items enclosed by the active Lasso selection!
   */
  fun deleteLassoSelection() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val selectedStrokes = state.lassoSelectedStrokeIds
      val selectedTexts = state.lassoSelectedTextBoxIds
      val selectedElements = state.lassoSelectedElementIds
      val selectedStickies = state.lassoSelectedStickyIds
      val selectedImages = state.lassoSelectedImageIds
      val hasSpecificSelection =
        selectedStrokes.isNotEmpty() ||
          selectedTexts.isNotEmpty() ||
          selectedElements.isNotEmpty() ||
          selectedStickies.isNotEmpty() ||
          selectedImages.isNotEmpty()

      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          if (!hasSpecificSelection) {
            page
          } else {
            page.copy(
              strokes = page.strokes.filterNot { selectedStrokes.contains(it.id) },
              textBoxes = page.textBoxes.filterNot { selectedTexts.contains(it.id) },
              elements = page.elements.filterNot { selectedElements.contains(it.id) },
              stickyNotes = page.stickyNotes.filterNot { selectedStickies.contains(it.id) },
              images = page.images.filterNot { selectedImages.contains(it.id) },
            )
          }
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        showLassoSelection = false,
        isLassoResizeMode = false,
        lassoLoopPoints = emptyList(),
        lassoSelectedStrokeIds = emptySet(),
        lassoSelectedTextBoxIds = emptySet(),
        lassoSelectedElementIds = emptySet(),
        lassoSelectedStickyIds = emptySet(),
        lassoSelectedImageIds = emptySet(),
        activePopover = ActivePopover.NONE,
        statusToastMessage =
          if (hasSpecificSelection) "Deleted selected items" else "Dismissed empty Lasso",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun undo() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      val currentDoc = state.notebooks.find { it.id == docId } ?: return@update state
      val page = currentDoc.currentPage
      if (page.strokes.isEmpty()) return@update state
      val lastStroke = page.strokes.last()
      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { p ->
          p.copy(strokes = p.strokes.dropLast(1))
        }
      savedDoc = modifiedDoc
      val currentRedo = state.redoStack[docId].orEmpty()
      val updatedRedo =
        state.redoStack.toMutableMap().apply { put(docId, currentRedo + lastStroke) }
      state.copy(
        notebooks = updatedNotebooks,
        redoStack = updatedRedo,
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun redo() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      val currentRedo = state.redoStack[docId].orEmpty()
      if (currentRedo.isEmpty()) return@update state
      val strokeToRestore = currentRedo.last()
      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { p ->
          p.copy(strokes = p.strokes + strokeToRestore)
        }
      savedDoc = modifiedDoc
      val updatedRedo =
        state.redoStack.toMutableMap().apply { put(docId, currentRedo.dropLast(1)) }
      state.copy(
        notebooks = updatedNotebooks,
        redoStack = updatedRedo,
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun clearCurrentPageStrokes() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            strokes = emptyList(),
            studyTapes = emptyList(),
            textBoxes = emptyList(),
            stickyNotes = emptyList(),
            elements = emptyList(),
          )
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        showLassoSelection = false,
        isLassoResizeMode = false,
        lassoLoopPoints = emptyList(),
        lassoSelectedStrokeIds = emptySet(),
        activeShapeEditStrokeId = null,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Cleared page annotations",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun addNewPageToCurrentDocument() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      val updated =
        state.notebooks.map { nb ->
          if (nb.id != docId) nb
          else {
            val nextNum = nb.pages.size + 1
            val nextPages = nb.pages + NotePage(pageNumber = nextNum)
            nb.copy(
              pages = nextPages,
              currentPageIndex = nextPages.size - 1,
              timestamp = currentTimestamp(),
            ).also { savedDoc = it }
          }
        }
      state.copy(
        notebooks = updated,
        showLassoSelection = false,
        isLassoResizeMode = false,
        statusToastMessage = "Added Page ${savedDoc?.pages?.size ?: 1}",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun selectPageIndex(index: Int) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      val currentDoc = state.notebooks.find { it.id == docId } ?: return@update state
      val clamped = index.coerceIn(0, (currentDoc.pages.size - 1).coerceAtLeast(0))
      if (clamped == currentDoc.currentPageIndex) return@update state
      val updated =
        state.notebooks.map { nb ->
          if (nb.id != docId) nb
          else {
            nb.copy(currentPageIndex = clamped).also { savedDoc = it }
          }
        }
      state.copy(
        notebooks = updated,
        showLassoSelection = false,
        isLassoResizeMode = false,
        lassoLoopPoints = emptyList(),
        activeShapeEditStrokeId = null,
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun toggleCurrentPageBookmark() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(isBookmarked = !page.isBookmarked)
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated)
    }
    persistDocumentAsync(savedDoc)
  }

  fun togglePageSidebar() {
    _uiState.update { it.copy(isPageSidebarOpen = !it.isPageSidebarOpen) }
  }

  fun cyclePaperTemplate() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      state.copy(
        notebooks =
          state.notebooks.map { nb ->
            if (nb.id != docId) nb
            else {
              val entries = PaperTemplate.entries
              val nextTemplate = entries[(entries.indexOf(nb.paperTemplate) + 1) % entries.size]
              nb.copy(paperTemplate = nextTemplate).also { savedDoc = it }
            }
          },
        statusToastMessage = "Paper Template: ${savedDoc?.paperTemplate?.label ?: ""}",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun cyclePaperColorTheme() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      state.copy(
        notebooks =
          state.notebooks.map { nb ->
            if (nb.id != docId) nb
            else {
              val entries = PaperColorTheme.entries
              val nextColor = entries[(entries.indexOf(nb.paperColorTheme) + 1) % entries.size]
              nb.copy(paperColorTheme = nextColor).also { savedDoc = it }
            }
          },
        statusToastMessage = "Paper Tint: ${savedDoc?.paperColorTheme?.label ?: ""}",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun showToast(message: String) {
    _uiState.update { it.copy(statusToastMessage = message) }
  }

  fun toggleRuler() {
    _uiState.update { state ->
      state.copy(rulerState = state.rulerState.copy(isVisible = !state.rulerState.isVisible))
    }
  }

  fun moveRuler(delta: Offset) {
    _uiState.update { state ->
      state.copy(
        rulerState =
          state.rulerState.copy(centerOffset = state.rulerState.centerOffset + delta)
      )
    }
  }

  fun rotateRuler(angleDegrees: Float) {
    _uiState.update { state ->
      state.copy(
        rulerState = state.rulerState.copy(angleDegrees = angleDegrees % 360f)
      )
    }
  }

  fun toggleZoomWindow() {
    _uiState.update { it.copy(isZoomWindowVisible = !it.isZoomWindowVisible) }
  }

  fun advanceZoomTargetBox(dx: Float, dy: Float) {
    _uiState.update { state ->
      val nextX = (state.zoomTargetBoxOffset.x + dx).coerceIn(24f, 380f)
      val nextY = (state.zoomTargetBoxOffset.y + dy).coerceIn(40f, 620f)
      state.copy(zoomTargetBoxOffset = Offset(nextX, nextY))
    }
  }

  fun toggleAudioBar() {
    _uiState.update { it.copy(isAudioBarVisible = !it.isAudioBarVisible) }
  }

  fun toggleAudioRecording() {
    _uiState.update { state ->
      state.copy(
        isRecordingAudio = !state.isRecordingAudio,
        isPlayingAudioReplay = false,
      )
    }
  }

  fun toggleAudioReplay() {
    _uiState.update { state ->
      state.copy(
        isPlayingAudioReplay = !state.isPlayingAudioReplay,
        isRecordingAudio = false,
      )
    }
  }

  fun toggleToolbarToolEnabled(tool: PrimaryTool) {
    _uiState.update { state ->
      val current = state.enabledToolbarTools.toMutableSet()
      if (current.contains(tool) && current.size > 2) {
        current.remove(tool)
      } else {
        current.add(tool)
      }
      state.copy(enabledToolbarTools = current)
    }
  }

  fun toggleWritingSubToolEnabled(subTool: WritingSubTool) {
    _uiState.update { state ->
      val current = state.enabledWritingSubTools.toMutableSet()
      if (current.contains(subTool) && current.size > 1) {
        current.remove(subTool)
      } else {
        current.add(subTool)
      }
      state.copy(enabledWritingSubTools = current)
    }
  }

  fun resetToolbarDefaults() {
    _uiState.update {
      it.copy(
        enabledToolbarTools = PrimaryTool.entries.toSet(),
        toolbarDockPosition = ToolbarDockPosition.TOP,
        subToolbarOffset = Offset.Zero,
      )
    }
  }

  fun resetWritingToolsDefaults() {
    _uiState.update { it.copy(enabledWritingSubTools = WritingSubTool.entries.toSet()) }
  }

  fun toggleLassoSelectionVisibility() {
    _uiState.update {
      it.copy(
        showLassoSelection = false,
        isLassoResizeMode = false,
        lassoLoopPoints = emptyList(),
        lassoSelectedStrokeIds = emptySet(),
        lassoSelectedTextBoxIds = emptySet(),
        lassoSelectedElementIds = emptySet(),
        lassoSelectedStickyIds = emptySet(),
        lassoSelectedImageIds = emptySet(),
      )
    }
  }

  fun toggleReadOnlyMode() {
    _uiState.update { it.copy(isReadOnlyMode = !it.isReadOnlyMode) }
  }

  fun toggleSortOrder() {
    _uiState.update { it.copy(sortByDateAscending = !it.sortByDateAscending) }
  }
}

private fun distanceFromPointToSegment(p: Offset, a: Offset, b: Offset): Float {
  val ab = b - a
  val ap = p - a
  val lenSq = ab.x * ab.x + ab.y * ab.y
  if (lenSq == 0f) return ap.getDistance()
  val t = ((ap.x * ab.x + ap.y * ab.y) / lenSq).coerceIn(0f, 1f)
  val proj = Offset(a.x + t * ab.x, a.y + t * ab.y)
  return (p - proj).getDistance()
}
