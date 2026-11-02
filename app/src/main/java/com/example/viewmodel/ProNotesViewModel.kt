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
import com.example.model.AppThemeMode
import com.example.model.CanvasElementItem
import com.example.model.CanvasImageItem
import com.example.model.CanvasTextBox
import com.example.model.CustomPageTemplate
import com.example.model.DefaultCustomPalette
import com.example.model.DefaultQuickSwatches
import com.example.model.DocumentHistorySnapshot
import com.example.model.DocumentType
import com.example.model.DrawnStroke
import com.example.model.ElementStampType
import com.example.model.EraserMode
import com.example.model.FolderItem
import com.example.model.LaserMode
import com.example.model.LassoClipboard
import com.example.model.LassoFilterSettings
import com.example.model.LassoShape
import com.example.model.LibraryViewMode
import com.example.model.NotePage
import com.example.model.NotebookCoverStyle
import com.example.model.NotebookDocument
import com.example.model.PageOrientation
import com.example.model.PageScrollDirection
import com.example.model.PageSizePreset
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
import com.example.model.ShapeRecognizer
import com.example.model.closedLoopPolygon
import com.example.model.isClosedCircleLoop
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
  val appThemeMode: AppThemeMode = AppThemeMode.DARK,
  val sidebarSection: SidebarSection = SidebarSection.DOCUMENTS,
  val libraryViewMode: LibraryViewMode = LibraryViewMode.GRID,
  val notebooks: List<NotebookDocument> = emptyList(),
  val folders: List<FolderItem> = emptyList(),
  val currentFolderId: String? = null,
  val customTemplates: List<CustomPageTemplate> = emptyList(),
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
  val shapeAutoDetectMode: Boolean = true,
  val lastAutoDetectedShape: ShapeKind? = null,
  val fillShapes: Boolean = true,
  val activeShapeEditStrokeId: String? = null,
  val selectedStylusBrand: com.example.model.StylusPenBrand = com.example.model.StylusPenBrand.SAMSUNG_S_PEN,
  val stylusBarrelAction: com.example.model.StylusBarrelAction = com.example.model.StylusBarrelAction.ERASE_WHILE_HELD,
  val stylusPressureSensitivity: Float = 1.0f,
  val stylusTiltShadingEnabled: Boolean = true,
  val stylusHoverCursorEnabled: Boolean = true,
  val stylusTailEraserEnabled: Boolean = true,
  val developerUpiId: String = "itznikk@upi",
  val developerDisplayName: String = "ItzNikk",
  val showInteractiveTour: Boolean = false,
  val interactiveTourStep: Int = 0,
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
  val lassoTriggeredFromCircleGesture: Boolean = false,
  val previousToolBeforeCircleLasso: PrimaryTool = PrimaryTool.WRITING_TOOLS,
  val barrelRollAngleDegrees: Float = 0f,
  val barrelRollPreviewEnabled: Boolean = true,
  val showThankYouScreen: Boolean = false,
  val lassoClipboard: LassoClipboard? = null,
  val rulerState: RulerState = RulerState(isVisible = false),
  val isZoomWindowVisible: Boolean = false,
  val zoomTargetBoxOffset: Offset = Offset(80f, 180f),
  val isAudioBarVisible: Boolean = false,
  val isRecordingAudio: Boolean = false,
  val isPlayingAudioReplay: Boolean = false,
  val recordedAudioSeconds: Int = 14,
  val isPageSidebarOpen: Boolean = false,
  val undoHistory: Map<String, List<DocumentHistorySnapshot>> = emptyMap(),
  val redoHistory: Map<String, List<DocumentHistorySnapshot>> = emptyMap(),
  val laserTrails: List<com.example.model.LaserTrailStroke> = emptyList(),
  val isReadOnlyMode: Boolean = false,
  val sortByDateAscending: Boolean = false,
  val sortOption: com.example.model.DocumentSortOption = com.example.model.DocumentSortOption.RECENT,
  val searchQuery: String = "",
  val isSearchBarVisible: Boolean = false,
  val statusToastMessage: String? = null,
) {
  fun canUndo(docId: String = activeTabId): Boolean =
    undoHistory[docId]?.isNotEmpty() == true

  fun canRedo(docId: String = activeTabId): Boolean =
    redoHistory[docId]?.isNotEmpty() == true
}

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
  private val repository =
    NotebookRepository(
      dao = database.notebookDao(),
      context = application,
      folderDao = database.folderDao(),
      customTemplateDao = database.customTemplateDao(),
    )
  private val prefs = application.getSharedPreferences("pronotes_settings_v3", Context.MODE_PRIVATE)

  private val _uiState = MutableStateFlow(loadInitialPreferences())
  val uiState: StateFlow<ProNotesUiState> = _uiState.asStateFlow()

  private var pxPerDp = 1f
  private val deletedNotebookIds = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
  private val deletedFolderIds = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

  fun setCanvasDensity(d: Float) {
    pxPerDp = d
  }

  init {
    viewModelScope.launch {
      repository.allNotebooksFlow.collectLatest { savedNotebooks ->
        _uiState.update { state ->
          // Merge in-memory revision if newer than emitted DB snapshot, and preserve any newly created
          // in-memory notebooks whose background DB insert is still in flight
          val unsavedInMem =
            state.notebooks.filter { mem ->
              !deletedNotebookIds.contains(mem.id) && savedNotebooks.none { it.id == mem.id }
            }
          val mergedSaved =
            savedNotebooks
              .filterNot { deletedNotebookIds.contains(it.id) }
              .map { dbDoc ->
                val inMem = state.notebooks.find { it.id == dbDoc.id }
                if (inMem != null && inMem.revision > dbDoc.revision) inMem else dbDoc
              }
          val mergedNotebooks = unsavedInMem + mergedSaved
          val validTabIds =
            state.openTabIds.filter { id -> mergedNotebooks.any { it.id == id && !it.isTrashed } }
          val resolvedActiveTab =
            when {
              validTabIds.contains(state.activeTabId) -> state.activeTabId
              validTabIds.isNotEmpty() -> validTabIds.last()
              mergedNotebooks.any { !it.isTrashed } -> mergedNotebooks.first { !it.isTrashed }.id
              else -> ""
            }
          state.copy(
            notebooks = mergedNotebooks,
            openTabIds = validTabIds,
            activeTabId = resolvedActiveTab,
          )
        }
      }
    }

    viewModelScope.launch {
      repository.allFoldersFlow?.collectLatest { savedFolders ->
        _uiState.update { state ->
          val unsavedInMem =
            state.folders.filter { mem ->
              !deletedFolderIds.contains(mem.id) && savedFolders.none { it.id == mem.id }
            }
          val mergedFolders =
            unsavedInMem + savedFolders.filterNot { deletedFolderIds.contains(it.id) }
          val validCurrentFolder =
            if (
              state.currentFolderId != null &&
                mergedFolders.any { it.id == state.currentFolderId && !it.isTrashed }
            ) {
              state.currentFolderId
            } else {
              null
            }
          state.copy(
            folders = mergedFolders,
            currentFolderId = validCurrentFolder,
          )
        }
      }
    }

    viewModelScope.launch {
      repository.allCustomTemplatesFlow?.collectLatest { templates ->
        _uiState.update { it.copy(customTemplates = templates) }
      }
    }
  }

  private fun loadInitialPreferences(): ProNotesUiState {
    val themeName = prefs.getString("app_theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
    val themeMode = runCatching { AppThemeMode.valueOf(themeName) }.getOrDefault(AppThemeMode.DARK)
    val viewModeName =
      prefs.getString("library_view_mode", LibraryViewMode.GRID.name) ?: LibraryViewMode.GRID.name
    val viewMode =
      runCatching { LibraryViewMode.valueOf(viewModeName) }.getOrDefault(LibraryViewMode.GRID)
    val scrollDirName =
      prefs.getString("page_scroll_direction", PageScrollDirection.HORIZONTAL.name)
        ?: PageScrollDirection.HORIZONTAL.name
    val scrollDir =
      runCatching { PageScrollDirection.valueOf(scrollDirName) }
        .getOrDefault(PageScrollDirection.HORIZONTAL)
    val stylusOnly = prefs.getBoolean("only_draw_with_stylus", false)
    val circleLasso = prefs.getBoolean("circle_to_lasso", true)
    val drawHold = prefs.getBoolean("draw_and_hold", true)
    val holdErase = prefs.getBoolean("hold_to_erase", true)
    val scribbleErase = prefs.getBoolean("scribble_to_erase", true)
    val privacyShield = prefs.getBoolean("privacy_screen_shield", false)
    val pullAdd = prefs.getBoolean("pull_to_add_page", true)
    val autoDeselectEraser = prefs.getBoolean("eraser_auto_deselect", false)

    val savedSwatchColors = prefs.getString("quick_swatches", null)
    val loadedSwatches =
      if (!savedSwatchColors.isNullOrBlank()) {
        savedSwatchColors
          .split(",")
          .mapNotNull { token -> token.toIntOrNull()?.let { Color(it) } }
          .ifEmpty { DefaultQuickSwatches }
      } else {
        DefaultQuickSwatches
      }

    val savedCustomPalette = prefs.getString("custom_palette", null)
    val loadedPalette =
      if (!savedCustomPalette.isNullOrBlank()) {
        savedCustomPalette
          .split(",")
          .mapNotNull { token -> token.toIntOrNull()?.let { Color(it) } }
          .ifEmpty { DefaultCustomPalette }
      } else {
        DefaultCustomPalette
      }

    val stylusBrandName =
      prefs.getString("stylus_pen_brand", com.example.model.StylusPenBrand.SAMSUNG_S_PEN.name)
        ?: com.example.model.StylusPenBrand.SAMSUNG_S_PEN.name
    val stylusBrand =
      runCatching { com.example.model.StylusPenBrand.valueOf(stylusBrandName) }
        .getOrDefault(com.example.model.StylusPenBrand.SAMSUNG_S_PEN)
    val barrelActionName =
      prefs.getString("stylus_barrel_action", com.example.model.StylusBarrelAction.ERASE_WHILE_HELD.name)
        ?: com.example.model.StylusBarrelAction.ERASE_WHILE_HELD.name
    val barrelAction =
      runCatching { com.example.model.StylusBarrelAction.valueOf(barrelActionName) }
        .getOrDefault(com.example.model.StylusBarrelAction.ERASE_WHILE_HELD)
    val pressureSens = prefs.getFloat("stylus_pressure_sensitivity", 1.0f).coerceIn(0.5f, 1.6f)
    val tiltShading = prefs.getBoolean("stylus_tilt_shading", true)
    val hoverCursor = prefs.getBoolean("stylus_hover_cursor", true)
    val tailEraser = prefs.getBoolean("stylus_tail_eraser", true)
    val shapeAutoDetect = prefs.getBoolean("shape_auto_detect_mode", true)
    val devUpiId = prefs.getString("developer_upi_id", "itznikk@upi")?.ifBlank { "itznikk@upi" } ?: "itznikk@upi"
    val devDisplayName = prefs.getString("developer_display_name", "ItzNikk")?.ifBlank { "ItzNikk" } ?: "ItzNikk"

    return ProNotesUiState(
      appThemeMode = themeMode,
      libraryViewMode = viewMode,
      toolbarDockPosition = ToolbarDockPosition.TOP,
      pageScrollDirection = scrollDir,
      onlyDrawWithStylus = stylusOnly,
      circleToLasso = circleLasso,
      drawAndHold = drawHold,
      holdToErase = holdErase,
      scribbleToErase = scribbleErase,
      privacyScreenShieldEnabled = privacyShield,
      pullToAddPage = pullAdd,
      eraserAutoDeselect = autoDeselectEraser,
      shapeAutoDetectMode = shapeAutoDetect,
      selectedStylusBrand = stylusBrand,
      stylusBarrelAction = barrelAction,
      stylusPressureSensitivity = pressureSens,
      stylusTiltShadingEnabled = tiltShading,
      stylusHoverCursorEnabled = hoverCursor,
      stylusTailEraserEnabled = tailEraser,
      developerUpiId = devUpiId,
      developerDisplayName = devDisplayName,
      quickSwatches = loadedSwatches,
      customPalette = loadedPalette,
      selectedColor = loadedSwatches.firstOrNull() ?: SwatchRoyalBlue,
    )
  }

  fun setAppThemeMode(mode: AppThemeMode) {
    prefs.edit().putString("app_theme_mode", mode.name).apply()
    _uiState.update { it.copy(appThemeMode = mode) }
  }

  fun setLibraryViewMode(mode: LibraryViewMode) {
    prefs.edit().putString("library_view_mode", mode.name).apply()
    _uiState.update { it.copy(libraryViewMode = mode) }
  }

  fun toggleLibraryViewMode() {
    val next =
      if (_uiState.value.libraryViewMode == LibraryViewMode.GRID) LibraryViewMode.LIST
      else LibraryViewMode.GRID
    setLibraryViewMode(next)
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
      runCatching { repository.saveNotebook(doc) }
        .onFailure { err ->
          _uiState.update {
            it.copy(statusToastMessage = "Save warning: ${err.localizedMessage ?: "Retry"}")
          }
        }
    }
  }

  fun flushActiveDocumentOnPause() {
    val state = _uiState.value
    val activeDoc = state.notebooks.find { it.id == state.activeTabId } ?: return
    persistDocumentAsync(activeDoc)
  }

  private fun currentTimestamp(): String {
    val fmt = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US)
    return fmt.format(Date())
  }

  /**
   * Captures the current document's pages & page index onto the per-document undo stack
   * and clears that document's redo stack.
   */
  private fun recordHistorySnapshot(
    state: ProNotesUiState,
    actionDescription: String = "",
  ): Pair<Map<String, List<DocumentHistorySnapshot>>, Map<String, List<DocumentHistorySnapshot>>> {
    val docId = state.activeTabId
    val doc = state.notebooks.find { it.id == docId } ?: return (state.undoHistory to state.redoHistory)
    val snap =
      DocumentHistorySnapshot(
        pages = doc.pages,
        currentPageIndex = doc.currentPageIndex,
        actionDescription = actionDescription,
      )
    val currentUndo = state.undoHistory[docId].orEmpty()
    val updatedUndoList = (currentUndo + snap).takeLast(40)
    val nextUndo = state.undoHistory.toMutableMap().apply { put(docId, updatedUndoList) }
    val nextRedo = state.redoHistory.toMutableMap().apply { remove(docId) }
    return nextUndo to nextRedo
  }

  /**
   * Explicitly records an undo snapshot before a continuous gesture (such as dragging a Lasso
   * selection, resizing/rotating a Lasso selection, or dragging a shape control handle) begins,
   * so that the entire gesture can be undone in a single Undo step.
   */
  fun recordUndoSnapshotBeforeGesture(description: String = "") {
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, description)
      state.copy(undoHistory = nextUndo, redoHistory = nextRedo)
    }
  }

  fun clearToast() {
    _uiState.update { it.copy(statusToastMessage = null) }
  }

  fun switchToDocuments() {
    flushActiveDocumentOnPause()
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
    _uiState.update {
      it.copy(
        sidebarSection = section,
        currentFolderId = if (section == SidebarSection.DOCUMENTS) it.currentFolderId else null,
      )
    }
  }

  // =========================================================================
  // Folder Hierarchy & Breadcrumb Navigation (Stage 6)
  // =========================================================================

  fun navigateToFolder(folderId: String?) {
    _uiState.update {
      it.copy(
        sidebarSection = SidebarSection.DOCUMENTS,
        currentFolderId = folderId,
      )
    }
  }

  fun navigateUpFolder() {
    _uiState.update { state ->
      val currId = state.currentFolderId ?: return@update state
      val currFolder = state.folders.find { it.id == currId }
      state.copy(currentFolderId = currFolder?.parentFolderId)
    }
  }

  fun getFolderBreadcrumbs(
    folders: List<FolderItem> = _uiState.value.folders,
    currentFolderId: String? = _uiState.value.currentFolderId,
  ): List<FolderItem> {
    val trail = mutableListOf<FolderItem>()
    var cursorId = currentFolderId
    val visited = mutableSetOf<String>()
    while (cursorId != null && !visited.contains(cursorId)) {
      visited.add(cursorId)
      val folder = folders.find { it.id == cursorId } ?: break
      trail.add(0, folder)
      cursorId = folder.parentFolderId
    }
    return trail
  }

  fun createFolder(
    name: String,
    colorArgb: Long = 0xFF4C8DF6L,
    parentFolderId: String? = _uiState.value.currentFolderId,
  ) {
    val trimmed = name.trim().ifBlank { "Untitled Folder" }
    val resolvedColor = if (colorArgb == 0L) 0xFF4C8DF6L else (colorArgb or 0xFF000000L)
    val now = System.currentTimeMillis()
    val folder =
      FolderItem(
        id = "fld_$now",
        name = trimmed,
        parentFolderId = parentFolderId,
        colorArgb = resolvedColor,
        createdAtEpochMs = now,
        updatedAtEpochMs = now,
        isTrashed = false,
      )
    _uiState.update { state ->
      state.copy(
        folders = (state.folders + folder).sortedBy { it.name.lowercase() },
        statusToastMessage = "Created folder \"$trimmed\"",
      )
    }
    viewModelScope.launch { repository.saveFolder(folder) }
  }

  fun updateFolderColor(folderId: String, colorArgb: Long) {
    val resolvedColor = if (colorArgb == 0L) 0xFF4C8DF6L else (colorArgb or 0xFF000000L)
    var updatedFolder: FolderItem? = null
    _uiState.update { state ->
      val nextFolders =
        state.folders.map { f ->
          if (f.id == folderId) {
            f.copy(colorArgb = resolvedColor, updatedAtEpochMs = System.currentTimeMillis()).also {
              updatedFolder = it
            }
          } else f
        }
      state.copy(
        folders = nextFolders,
        statusToastMessage = "Updated folder color",
      )
    }
    updatedFolder?.let { folder -> viewModelScope.launch { repository.saveFolder(folder) } }
  }

  fun renameFolder(folderId: String, newName: String) {
    val trimmed = newName.trim()
    if (trimmed.isEmpty()) return
    var updatedFolder: FolderItem? = null
    _uiState.update { state ->
      val nextFolders =
        state.folders.map { f ->
          if (f.id == folderId) {
            f.copy(name = trimmed, updatedAtEpochMs = System.currentTimeMillis()).also {
              updatedFolder = it
            }
          } else f
        }
      state.copy(
        folders = nextFolders,
        statusToastMessage = "Renamed folder to \"$trimmed\"",
      )
    }
    updatedFolder?.let { folder -> viewModelScope.launch { repository.saveFolder(folder) } }
  }

  /**
   * Returns true if moving [folderId] under [targetParentId] does not create a cycle
   * (cannot move a folder into itself or into any of its own descendants).
   */
  fun canMoveFolder(
    folderId: String,
    targetParentId: String?,
    folders: List<FolderItem> = _uiState.value.folders,
  ): Boolean {
    if (folderId == targetParentId) return false
    var cursor = targetParentId
    val visited = mutableSetOf<String>()
    while (cursor != null) {
      if (cursor == folderId) return false
      if (!visited.add(cursor)) return false
      cursor = folders.find { it.id == cursor }?.parentFolderId
    }
    return true
  }

  fun moveFolder(folderId: String, targetParentId: String?) {
    val state = _uiState.value
    if (!canMoveFolder(folderId, targetParentId, state.folders)) {
      _uiState.update {
        it.copy(statusToastMessage = "Cannot move a folder inside itself")
      }
      return
    }
    var updatedFolder: FolderItem? = null
    _uiState.update { s ->
      val nextFolders =
        s.folders.map { f ->
          if (f.id == folderId) {
            f.copy(
                parentFolderId = targetParentId,
                updatedAtEpochMs = System.currentTimeMillis(),
              )
              .also { updatedFolder = it }
          } else f
        }
      s.copy(
        folders = nextFolders,
        statusToastMessage = "Moved folder",
      )
    }
    updatedFolder?.let { folder -> viewModelScope.launch { repository.saveFolder(folder) } }
  }

  fun moveNotebookToFolder(notebookId: String, targetFolderId: String?) {
    var updatedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val targetFolderName =
        if (targetFolderId == null) "Documents Root"
        else state.folders.find { it.id == targetFolderId }?.name ?: "Folder"
      val nextNotebooks =
        state.notebooks.map { nb ->
          if (nb.id == notebookId) {
            nb.copy(
                folderId = targetFolderId,
                revision = nb.revision + 1L,
                updatedAtEpochMs = System.currentTimeMillis(),
              )
              .also { updatedDoc = it }
          } else nb
        }
      state.copy(
        notebooks = nextNotebooks,
        statusToastMessage = "Moved to \"$targetFolderName\"",
      )
    }
    persistDocumentAsync(updatedDoc)
  }

  fun moveFolderToTrash(folderId: String) {
    var updatedFolder: FolderItem? = null
    _uiState.update { state ->
      val nextFolders =
        state.folders.map { f ->
          if (f.id == folderId) {
            f.copy(isTrashed = !f.isTrashed, updatedAtEpochMs = System.currentTimeMillis()).also {
              updatedFolder = it
            }
          } else f
        }
      state.copy(
        folders = nextFolders,
        currentFolderId = if (state.currentFolderId == folderId) null else state.currentFolderId,
        statusToastMessage =
          if (updatedFolder?.isTrashed == true) "Folder moved to Trash" else "Folder restored",
      )
    }
    updatedFolder?.let { folder -> viewModelScope.launch { repository.saveFolder(folder) } }
  }

  fun deleteFolderPermanently(folderId: String) {
    deletedFolderIds.add(folderId)
    _uiState.update { state ->
      state.copy(
        folders = state.folders.filterNot { it.id == folderId },
        currentFolderId = if (state.currentFolderId == folderId) null else state.currentFolderId,
        statusToastMessage = "Deleted folder permanently",
      )
    }
    viewModelScope.launch { repository.deleteFolder(folderId) }
  }

  fun renameNotebook(notebookId: String, newTitle: String) {
    val trimmed = newTitle.trim()
    if (trimmed.isEmpty()) return
    var updatedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val nextList =
        state.notebooks.map { nb ->
          if (nb.id == notebookId) {
            nb.copy(
                title = trimmed,
                timestamp = currentTimestamp(),
                revision = nb.revision + 1L,
                updatedAtEpochMs = System.currentTimeMillis(),
              )
              .also { updatedDoc = it }
          } else nb
        }
      state.copy(
        notebooks = nextList,
        statusToastMessage = "Renamed to \"$trimmed\"",
      )
    }
    persistDocumentAsync(updatedDoc)
  }

  // =========================================================================
  // Custom Templates Import & Management (Stage 6)
  // =========================================================================

  fun importCustomTemplate(name: String, bitmap: Bitmap) {
    val now = System.currentTimeMillis()
    val id = "ctpl_$now"
    val key = "custom_tpl_$now"
    val isLandscape = bitmap.width > bitmap.height
    val template =
      CustomPageTemplate(
        id = id,
        name = name.trim().ifBlank { "Custom Template" },
        bitmapKey = key,
        previewBitmap = bitmap,
        isLandscape = isLandscape,
        createdAtEpochMs = now,
      )
    viewModelScope.launch {
      val saved = repository.saveCustomTemplate(template, bitmap)
      _uiState.update { state ->
        state.copy(
          customTemplates = listOf(saved) + state.customTemplates.filterNot { it.id == saved.id },
          statusToastMessage = "Saved custom template \"${saved.name}\"",
        )
      }
    }
  }

  fun deleteCustomTemplate(templateId: String) {
    val target = _uiState.value.customTemplates.find { it.id == templateId } ?: return
    _uiState.update { state ->
      state.copy(
        customTemplates = state.customTemplates.filterNot { it.id == templateId },
        statusToastMessage = "Removed custom template \"${target.name}\"",
      )
    }
    viewModelScope.launch {
      repository.deleteCustomTemplate(target.id, target.bitmapKey)
    }
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
            nb.copy(
                isFavorite = !nb.isFavorite,
                revision = nb.revision + 1L,
                updatedAtEpochMs = System.currentTimeMillis(),
              )
              .also { updatedDoc = it }
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
            nb.copy(
                isTrashed = !nb.isTrashed,
                revision = nb.revision + 1L,
                updatedAtEpochMs = System.currentTimeMillis(),
              )
              .also { updatedDoc = it }
          } else nb
        }
      val remainingTabs = state.openTabIds.filterNot { it == notebookId }
      state.copy(
        notebooks = updatedList,
        openTabIds = remainingTabs,
        statusToastMessage =
          if (updatedDoc?.isTrashed == true) "Moved to Trash" else "Restored from Trash",
      )
    }
    persistDocumentAsync(updatedDoc)
  }

  fun deleteNotebookPermanently(notebookId: String) {
    deletedNotebookIds.add(notebookId)
    _uiState.update { state ->
      state.copy(
        notebooks = state.notebooks.filterNot { it.id == notebookId },
        openTabIds = state.openTabIds.filterNot { it == notebookId },
        statusToastMessage = "Deleted notebook permanently",
      )
    }
    viewModelScope.launch {
      repository.deleteNotebook(notebookId)
    }
  }

  fun emptyTrash() {
    _uiState.value.notebooks.filter { it.isTrashed }.forEach { deletedNotebookIds.add(it.id) }
    _uiState.value.folders.filter { it.isTrashed }.forEach { deletedFolderIds.add(it.id) }
    _uiState.update { state ->
      state.copy(
        notebooks = state.notebooks.filterNot { it.isTrashed },
        folders = state.folders.filterNot { it.isTrashed },
        statusToastMessage = "Trash emptied",
      )
    }
    viewModelScope.launch {
      repository.emptyTrash()
    }
  }

  fun clearAllLocalStorage() {
    viewModelScope.launch {
      repository.clearAllLocalData()
      _uiState.update {
        it.copy(
          notebooks = emptyList(),
          folders = emptyList(),
          currentFolderId = null,
          openTabIds = emptyList(),
          activeTabId = "",
          undoHistory = emptyMap(),
          redoHistory = emptyMap(),
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
    paperColorTheme: PaperColorTheme = PaperColorTheme.PURE_WHITE,
    documentType: DocumentType =
      if (coverStyle == NotebookCoverStyle.WHITEBOARD) DocumentType.WHITEBOARD
      else DocumentType.NOTEBOOK,
    pageSizePreset: PageSizePreset = PageSizePreset.A4,
    pageOrientation: PageOrientation =
      if (coverStyle == NotebookCoverStyle.WHITEBOARD) PageOrientation.LANDSCAPE
      else PageOrientation.PORTRAIT,
    customTemplate: CustomPageTemplate? = null,
    folderId: String? = _uiState.value.currentFolderId,
    openImmediately: Boolean = true,
  ) {
    var createdDoc: NotebookDocument? = null
    _uiState.update { state ->
      val count = state.notebooks.size + 1
      val now = System.currentTimeMillis()
      val newId = "nb_$now"
      val resolvedTitle =
        title.trim().ifBlank {
          if (documentType == DocumentType.WHITEBOARD || coverStyle == NotebookCoverStyle.WHITEBOARD) {
            "Untitled Whiteboard ($count)"
          } else {
            "Untitled Notebook ($count)"
          }
        }
      val firstPage =
        NotePage(
          pageNumber = 1,
          pageTemplate = paperTemplate,
          pageColorTheme = paperColorTheme,
          customTemplateBitmap = customTemplate?.previewBitmap,
          customTemplateKey = customTemplate?.bitmapKey ?: "",
        )
      val newDoc =
        NotebookDocument(
          id = newId,
          title = resolvedTitle,
          timestamp = currentTimestamp(),
          coverStyle = coverStyle,
          isFavorite = false,
          hasStylusBadge = state.onlyDrawWithStylus,
          paperTemplate = paperTemplate,
          paperColorTheme = paperColorTheme,
          pages = listOf(firstPage),
          folderId = folderId,
          documentType = documentType,
          pageSizePreset = pageSizePreset,
          pageOrientation = pageOrientation,
          customTemplateKey = customTemplate?.bitmapKey ?: "",
          revision = 1L,
          updatedAtEpochMs = now,
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
        statusToastMessage = "Created \"$resolvedTitle\"",
      )
    }
    persistDocumentAsync(createdDoc)
  }

  fun createNewWhiteboard(
    title: String = "",
    folderId: String? = _uiState.value.currentFolderId,
  ) {
    createNewNotebook(
      title = title,
      coverStyle = NotebookCoverStyle.WHITEBOARD,
      paperTemplate = PaperTemplate.DOTTED,
      paperColorTheme = PaperColorTheme.PURE_WHITE,
      documentType = DocumentType.WHITEBOARD,
      pageSizePreset = PageSizePreset.GOODNOTES_STANDARD,
      pageOrientation = PageOrientation.LANDSCAPE,
      folderId = folderId,
      openImmediately = true,
    )
  }

  fun importPdfDocument(title: String, pages: List<NotePage>) {
    if (pages.isEmpty()) return
    var createdDoc: NotebookDocument? = null
    _uiState.update { state ->
      val now = System.currentTimeMillis()
      val newId = "pdf_$now"
      val newDoc =
        NotebookDocument(
          id = newId,
          title = title,
          timestamp = currentTimestamp(),
          coverStyle = NotebookCoverStyle.WHITE_DOCUMENT,
          isFavorite = false,
          isImportedPdf = true,
          paperTemplate = PaperTemplate.BLANK,
          pages = pages,
          currentPageIndex = 0,
          folderId = state.currentFolderId,
          documentType = DocumentType.NOTEBOOK,
          revision = 1L,
          updatedAtEpochMs = now,
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

  fun updateCurrentPageCanvasSize(widthPx: Float, heightPx: Float) {
    if (widthPx <= 10f || heightPx <= 10f) return
    _uiState.update { state ->
      val doc = state.notebooks.find { it.id == state.activeTabId } ?: return@update state
      val currPage = doc.currentPage
      if (
        abs(currPage.canvasWidthPx - widthPx) < 2f &&
          abs(currPage.canvasHeightPx - heightPx) < 2f
      ) {
        return@update state
      }
      val (updated, _) =
        updateCurrentPage(state, bumpRevision = false) { page ->
          page.copy(canvasWidthPx = widthPx, canvasHeightPx = heightPx)
        }
      state.copy(notebooks = updated)
    }
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
    prefs.edit().putString("toolbar_dock", ToolbarDockPosition.TOP.name).apply()
    _uiState.update {
      it.copy(toolbarDockPosition = ToolbarDockPosition.TOP)
    }
  }

  fun cycleToolbarDockPosition() {
    setToolbarDockPosition(ToolbarDockPosition.TOP)
  }

  fun toggleSPenBarrelButtonMode() {
    _uiState.update { state ->
      val next = !state.isSPenBarrelButtonActive
      state.copy(
        isSPenBarrelButtonActive = next,
      )
    }
  }

  fun togglePrivacyScreenShield(enabled: Boolean) {
    prefs.edit().putBoolean("privacy_screen_shield", enabled).apply()
    _uiState.update {
      it.copy(
        privacyScreenShieldEnabled = enabled,
        statusToastMessage =
          if (enabled) "Privacy Screen Shield ON (blocks screenshots & recording)"
          else "Privacy Screen Shield OFF",
      )
    }
  }

  fun toggleWatermarkConfidentialExports(enabled: Boolean) {
    _uiState.update {
      it.copy(
        watermarkConfidentialExports = enabled,
        statusToastMessage =
          if (enabled) "PDF Export Watermark Enabled" else "PDF Export Watermark Disabled",
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
            nb.copy(
                isLockedVault = true,
                vaultPinHash = pinHash,
                revision = nb.revision + 1L,
                updatedAtEpochMs = System.currentTimeMillis(),
              )
              .also { savedDoc = it }
          } else nb
        }
      state.copy(
        notebooks = updatedList,
        unlockedVaultNotebookIds = state.unlockedVaultNotebookIds + docId,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Locked notebook with AES-GCM Vault encryption",
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
            nb.copy(
                isLockedVault = false,
                vaultPinHash = "",
                revision = nb.revision + 1L,
                updatedAtEpochMs = System.currentTimeMillis(),
              )
              .also { savedDoc = it }
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
          statusToastMessage = "Vault Unlocked",
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
    // Sub-toolbar is now cleanly docked below the primary toolbar (Stage 4)
  }

  fun resetSubToolbarPosition() {
    _uiState.update { it.copy(subToolbarOffset = Offset.Zero) }
  }

  fun toggleOnlyDrawWithStylus(enabled: Boolean) {
    prefs.edit().putBoolean("only_draw_with_stylus", enabled).apply()
    _uiState.update {
      it.copy(
        onlyDrawWithStylus = enabled,
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
    prefs.edit().putBoolean("pull_to_add_page", enabled).apply()
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
    prefs.edit().putBoolean("eraser_auto_deselect", enabled).apply()
    _uiState.update { it.copy(eraserAutoDeselect = enabled) }
  }

  fun selectShapeKind(kind: ShapeKind) {
    _uiState.update { it.copy(selectedShapeKind = kind) }
  }

  fun setShapeAutoDetectMode(enabled: Boolean) {
    prefs.edit().putBoolean("shape_auto_detect_mode", enabled).apply()
    _uiState.update { it.copy(shapeAutoDetectMode = enabled) }
  }

  fun selectShapeForDirectDraw(kind: ShapeKind) {
    prefs.edit().putBoolean("shape_auto_detect_mode", false).apply()
    _uiState.update {
      it.copy(
        selectedShapeKind = kind,
        shapeAutoDetectMode = false,
      )
    }
  }

  fun clearActiveShapeEdit() {
    _uiState.update { it.copy(activeShapeEditStrokeId = null) }
  }

  fun selectShapeAtPoint(point: Offset): Boolean {
    val state = _uiState.value
    val page = state.notebooks.find { it.id == state.activeTabId }?.currentPage ?: return false
    val hitShape =
      page.strokes.asReversed().firstOrNull { stroke ->
        stroke.isShape && isStrokeHitByPoint(stroke, point, 20f)
      }
    if (hitShape != null) {
      _uiState.update {
        it.copy(
          activeShapeEditStrokeId = hitShape.id,
          selectedShapeKind = hitShape.shapeKind ?: it.selectedShapeKind,
        )
      }
      return true
    }
    if (state.activeShapeEditStrokeId != null) {
      _uiState.update { it.copy(activeShapeEditStrokeId = null) }
    }
    return false
  }

  fun moveEntireShapeStroke(strokeId: String, delta: Offset) {
    if (delta == Offset.Zero) return
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            strokes =
              page.strokes.map { stroke ->
                if (stroke.id != strokeId || !stroke.isShape) stroke
                else {
                  stroke.copy(
                    points = stroke.points.map { it + delta },
                    shapeControlPoints = stroke.shapeControlPoints.map { it + delta },
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

  fun toggleActiveShapeFill() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val targetId = state.activeShapeEditStrokeId ?: return@update state
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Toggle shape fill")
      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            strokes =
              page.strokes.map { stroke ->
                if (stroke.id != targetId || !stroke.isShape) stroke
                else {
                  val canFill =
                    stroke.shapeKind != ShapeKind.LINE &&
                      stroke.shapeKind != ShapeKind.ARROW &&
                      stroke.shapeKind != ShapeKind.CURVE
                  stroke.copy(shapeFill = if (canFill) !stroke.shapeFill else false)
                }
              }
          )
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updatedNotebooks,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun duplicateActiveShape() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val targetId = state.activeShapeEditStrokeId ?: return@update state
      val currentPage =
        state.notebooks.find { it.id == state.activeTabId }?.currentPage ?: return@update state
      val source = currentPage.strokes.find { it.id == targetId && it.isShape } ?: return@update state
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Duplicate shape")
      val shift = Offset(28f, 28f)
      val copyId = java.util.UUID.randomUUID().toString()
      val copyStroke =
        source.copy(
          id = copyId,
          points = source.points.map { it + shift },
          shapeControlPoints = source.shapeControlPoints.map { it + shift },
        )
      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(strokes = page.strokes + copyStroke)
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updatedNotebooks,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        activeShapeEditStrokeId = copyId,
        statusToastMessage = "Duplicated ${source.shapeKind?.label ?: "Shape"}",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun deleteActiveShape() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val targetId = state.activeShapeEditStrokeId ?: return@update state
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Delete shape")
      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(strokes = page.strokes.filterNot { it.id == targetId })
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updatedNotebooks,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        activeShapeEditStrokeId = null,
      )
    }
    persistDocumentAsync(savedDoc)
  }

  /**
   * Inserts a geometric shape stamp onto the current page when selected from the Shapes popover.
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
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Insert ${kind.label}")
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
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        selectedShapeKind = kind,
        lastAutoDetectedShape = kind,
        activeShapeEditStrokeId = strokeId,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Inserted ${kind.label}",
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
        val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Recolor selection")
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
          undoHistory = nextUndo,
          redoHistory = nextRedo,
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
        statusToastMessage = "Added color slot #${newIdx + 1}",
      )
    }
  }

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

  fun removeCurrentToolbarSwatchSlot() {
    _uiState.update { state ->
      if (state.quickSwatches.size <= 2) {
        return@update state.copy(
          statusToastMessage = "At least 2 color slots must remain in toolbar"
        )
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
    bumpRevision: Boolean = true,
    transform: (NotePage) -> NotePage,
  ): Pair<List<NotebookDocument>, NotebookDocument?> {
    val docId = state.activeTabId
    var modifiedDoc: NotebookDocument? = null
    val now = System.currentTimeMillis()
    val list =
      state.notebooks.map { nb ->
        if (nb.id != docId) nb
        else {
          val idx = nb.currentPageIndex.coerceIn(0, (nb.pages.size - 1).coerceAtLeast(0))
          val updatedPages = nb.pages.toMutableList()
          updatedPages[idx] = transform(updatedPages[idx])
          nb.copy(
              pages = updatedPages,
              timestamp = if (bumpRevision) currentTimestamp() else nb.timestamp,
              revision = if (bumpRevision) nb.revision + 1L else nb.revision,
              updatedAtEpochMs = if (bumpRevision) now else nb.updatedAtEpochMs,
            )
            .also { modifiedDoc = it }
        }
      }
    return list to modifiedDoc
  }

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

  // ── Erase gesture bookkeeping ──────────────────────────────────────────────────────────────
  // One undo step and one disk write per erase gesture (not one per pointer sample).
  private var eraseSnapshotPending = false
  private var erasePendingDoc: NotebookDocument? = null

  fun beginEraseGesture() {
    eraseSnapshotPending = true
    erasePendingDoc = null
  }

  /**
   * Finishes an erase gesture. When the Eraser tool is selected with "Auto-Deselect Eraser" on, the
   * app returns to the pen only now, at the end of the stroke (it used to flip on the first sample).
   * [temporary] is true for the stylus-button / eraser-tail erase, which never changes the tool.
   */
  fun endEraseGesture(temporary: Boolean = false) {
    eraseSnapshotPending = false
    val doc = erasePendingDoc
    erasePendingDoc = null
    _uiState.update { state ->
      if (!temporary && state.primaryTool == PrimaryTool.ERASER && state.eraserAutoDeselect) {
        state.copy(primaryTool = PrimaryTool.WRITING_TOOLS)
      } else {
        state
      }
    }
    persistDocumentAsync(doc)
  }

  /**
   * Erases ink under [points]. Returns the pieces of ink that were removed so the UI can fade
   * them out (whole strokes in Stroke mode, just the cut-out parts in Precision mode).
   */
  fun eraseAtPoints(points: List<Offset>): List<DrawnStroke> {
    if (points.isEmpty()) return emptyList()
    val recordUndo = eraseSnapshotPending
    var erasedInk: List<DrawnStroke> = emptyList()
    var changed = false
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      erasedInk = emptyList()
      changed = false
      savedDoc = null
      if (state.isReadOnlyMode) return@update state
      val currentDoc = state.notebooks.find { it.id == state.activeTabId } ?: return@update state
      val beforePage = currentDoc.currentPage
      val mode = state.eraserMode
      val hitRadius = state.eraserRadius
      val removedInk = mutableListOf<DrawnStroke>()

      val (updatedNotebooks, modifiedDoc) =
        updateCurrentPage(state) { page ->
          val nextStrokes =
            when (mode) {
              EraserMode.HIGHLIGHTER_ONLY -> {
                page.strokes.filterNot { stroke ->
                  val hit =
                    stroke.isHighlighter &&
                      points.any { erasePt -> isStrokeHitByPoint(stroke, erasePt, hitRadius) }
                  if (hit) removedInk.add(stroke)
                  hit
                }
              }

              EraserMode.PRECISION -> {
                val reach = hitRadius * 0.85f
                page.strokes.flatMap { stroke ->
                  if (points.none { erasePt -> isStrokeHitByPoint(stroke, erasePt, reach) }) {
                    return@flatMap listOf(stroke)
                  }
                  // Resample so a fast, sparse stroke is cut exactly where the eraser touched it,
                  // keeping per-point pressure so the surviving ink keeps its width.
                  val step = (hitRadius * 0.25f).coerceIn(2f, 6f)
                  val (densePts, densePr) = densifyWithPressure(stroke.points, stroke.pressures, step)
                  val hasPr = densePr.size == densePts.size
                  val inkReach = reach + stroke.strokeWidth * 0.5f

                  val keptRuns = mutableListOf<Pair<List<Offset>, List<Float>>>()
                  val cutRuns = mutableListOf<Pair<List<Offset>, List<Float>>>()
                  var runPts = mutableListOf<Offset>()
                  var runPr = mutableListOf<Float>()
                  var runErased = false

                  fun flushRun() {
                    if (runPts.size >= 2) {
                      val run = runPts to runPr
                      if (runErased) cutRuns.add(run) else keptRuns.add(run)
                    }
                  }

                  for (idx in densePts.indices) {
                    val pt = densePts[idx]
                    val erased = points.any { erasePt -> (pt - erasePt).getDistance() <= inkReach }
                    if (idx > 0 && erased != runErased) {
                      flushRun()
                      runPts = mutableListOf()
                      runPr = mutableListOf()
                    }
                    runErased = erased
                    runPts.add(pt)
                    if (hasPr) runPr.add(densePr[idx])
                  }
                  flushRun()

                  if (cutRuns.isEmpty()) {
                    listOf(stroke)
                  } else {
                    fun fragment(run: Pair<List<Offset>, List<Float>>): DrawnStroke =
                      stroke.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        points = run.first,
                        pressures = if (run.second.size == run.first.size) run.second else emptyList(),
                        isShape = false,
                        shapeKind = null,
                        shapeFill = false,
                        shapeControlPoints = emptyList(),
                      )
                    cutRuns.forEach { removedInk.add(fragment(it)) }
                    keptRuns.map { fragment(it) }
                  }
                }
              }

              EraserMode.STROKE -> {
                page.strokes.filterNot { stroke ->
                  val hit = points.any { erasePt -> isStrokeHitByPoint(stroke, erasePt, hitRadius) }
                  if (hit) removedInk.add(stroke)
                  hit
                }
              }
            }
          val nextTapes =
            if (mode == EraserMode.HIGHLIGHTER_ONLY) {
              page.studyTapes
            } else {
              page.studyTapes.filterNot { tape ->
                points.any { erasePt ->
                  distanceFromPointToSegment(erasePt, tape.start, tape.end) <=
                    (hitRadius + tape.thickness / 2f)
                }
              }
            }
          if (removedInk.isNotEmpty() || nextTapes.size != beforePage.studyTapes.size) {
            changed = true
          }
          page.copy(strokes = nextStrokes, studyTapes = nextTapes)
        }

      if (!changed) return@update state
      erasedInk = removedInk.toList()
      savedDoc = modifiedDoc
      val (nextUndo, nextRedo) =
        if (recordUndo) recordHistorySnapshot(state, "Erase")
        else (state.undoHistory to state.redoHistory)
      state.copy(
        notebooks = updatedNotebooks,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        activeShapeEditStrokeId = null,
      )
    }
    if (changed) {
      eraseSnapshotPending = false
      erasePendingDoc = savedDoc
    }
    return erasedInk
  }

  private fun densifyWithPressure(
    pts: List<Offset>,
    pressures: List<Float>,
    stepPx: Float,
  ): Pair<List<Offset>, List<Float>> {
    val hasPr = pressures.size == pts.size
    if (pts.size < 2) return pts to (if (hasPr) pressures else emptyList())
    val outPts = ArrayList<Offset>()
    val outPr = ArrayList<Float>()
    for (i in 0 until pts.size - 1) {
      val a = pts[i]
      val b = pts[i + 1]
      val steps = max(1, ((b - a).getDistance() / stepPx).toInt())
      for (k in 0 until steps) {
        val t = k.toFloat() / steps.toFloat()
        outPts.add(Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t))
        if (hasPr) outPr.add(pressures[i] + (pressures[i + 1] - pressures[i]) * t)
      }
    }
    outPts.add(pts.last())
    if (hasPr) outPr.add(pressures.last())
    return outPts to (if (hasPr) outPr else emptyList())
  }

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
            shape.fractionInside(densifyPolyline(stroke.points, 6f)) >= 0.22f ||
              stroke.points.any { shape.contains(it) }
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
              shape.contains(
                img.position + Offset(img.widthDp * pxPerDp * 0.5f, img.heightDp * pxPerDp * 0.5f)
              )
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

  /**
   * GoodNotes-style "draw a loop, then hold": the pen comes back down and rests on one end of the
   * loop that was just drawn (within ~2.5 s). The loop turns into a lasso selection.
   */
  fun tryConvertDrawnCircleStrokeToLassoAtHold(holdPoint: Offset): Boolean {
    val state = _uiState.value
    if (!state.circleToLasso || state.isReadOnlyMode) return false
    val isPenOrPencil =
      state.primaryTool == PrimaryTool.WRITING_TOOLS &&
        (state.activeWritingSubTool == WritingSubTool.PEN ||
          state.activeWritingSubTool == WritingSubTool.PENCIL)
    if (!isPenOrPencil) return false
    val page = state.notebooks.find { it.id == state.activeTabId }?.currentPage ?: return false

    val loop = page.strokes.maxByOrNull { it.timestampMs } ?: return false
    if (System.currentTimeMillis() - loop.timestampMs > 2500L) return false
    if (loop.isHighlighter || loop.isShape) return false

    // The pen has to rest on an end of the loop, so a normal tap inside a circle never triggers this.
    val endpointReach = 64f
    if (
      (loop.points.first() - holdPoint).getDistance() > endpointReach &&
        (loop.points.last() - holdPoint).getDistance() > endpointReach
    ) {
      return false
    }

    val polygon = closedLoopPolygon(loop.points) ?: return false
    val remaining = page.copy(strokes = page.strokes.filterNot { it.id == loop.id })
    val selection = computeLassoSelection(polygon, remaining, state.lassoFilters)

    var savedDoc: NotebookDocument? = null
    _uiState.update { s ->
      val (nbs, doc) =
        updateCurrentPage(s) { p -> p.copy(strokes = p.strokes.filterNot { it.id == loop.id }) }
      savedDoc = doc
      // The loop never becomes real ink, so drop the "Draw stroke" undo step it created.
      val docId = s.activeTabId
      val docUndo = s.undoHistory[docId]
      val nextUndo =
        if (docUndo != null && docUndo.lastOrNull()?.actionDescription == "Draw stroke") {
          s.undoHistory.toMutableMap().apply { put(docId, docUndo.dropLast(1)) }
        } else {
          s.undoHistory
        }
      val priorTool =
        if (s.primaryTool == PrimaryTool.LASSO) s.previousToolBeforeCircleLasso else s.primaryTool
      s.copy(
        notebooks = nbs,
        undoHistory = nextUndo,
        primaryTool = PrimaryTool.LASSO,
        lassoTriggeredFromCircleGesture = true,
        previousToolBeforeCircleLasso = priorTool,
        showLassoSelection = true,
        isLassoResizeMode = false,
        isLassoColorMenuOpen = false,
        lassoLoopPoints = polygon,
        lassoSelectedStrokeIds = selection.strokeIds,
        lassoSelectedTextBoxIds = selection.textBoxIds,
        lassoSelectedElementIds = selection.elementIds,
        lassoSelectedStickyIds = selection.stickyIds,
        lassoSelectedImageIds = selection.imageIds,
      )
    }
    persistDocumentAsync(savedDoc)
    return true
  }

  /**
   * GoodNotes-style "draw a loop and hold at its end without lifting": converts the in-progress
   * freehand loop into a lasso selection. Returns false (leaving the ink alone for shape snap) when
   * the stroke is not a closed loop or nothing is inside it while Draw-and-Hold is active.
   */
  fun triggerInstantCircleToLasso(loopPoints: List<Offset>): Boolean {
    val state = _uiState.value
    if (!state.circleToLasso || state.isReadOnlyMode) return false
    val polygon = closedLoopPolygon(loopPoints) ?: return false
    val page = state.notebooks.find { it.id == state.activeTabId }?.currentPage
    val selection = computeLassoSelection(polygon, page, state.lassoFilters)
    if (selection.totalCount == 0 && state.drawAndHold && !state.showInteractiveTour) return false
    _uiState.update { s ->
      val priorTool =
        if (s.primaryTool == PrimaryTool.LASSO) s.previousToolBeforeCircleLasso else s.primaryTool
      s.copy(
        primaryTool = PrimaryTool.LASSO,
        lassoTriggeredFromCircleGesture = true,
        previousToolBeforeCircleLasso = priorTool,
        showLassoSelection = true,
        isLassoResizeMode = false,
        isLassoColorMenuOpen = false,
        lassoLoopPoints = polygon,
        lassoSelectedStrokeIds = selection.strokeIds,
        lassoSelectedTextBoxIds = selection.textBoxIds,
        lassoSelectedElementIds = selection.elementIds,
        lassoSelectedStickyIds = selection.stickyIds,
        lassoSelectedImageIds = selection.imageIds,
      )
    }
    return true
  }

  private fun simplifyLoop(points: List<Offset>): List<Offset> {
    val simplified = simplifyPolygon(points, 2f)
    return if (simplified.size >= 3) simplified else points
  }

  /** Lasso tool tap: selects the topmost stroke under the pen, like GoodNotes. */
  fun selectLassoItemAt(point: Offset) {
    _uiState.update { state ->
      if (state.isReadOnlyMode) return@update state
      val page = state.notebooks.find { it.id == state.activeTabId }?.currentPage ?: return@update state
      val hit = page.strokes.asReversed().firstOrNull { isStrokeHitByPoint(it, point, 14f) }
        ?: return@update state
      val minX = hit.points.minOf { it.x } - 8f
      val maxX = hit.points.maxOf { it.x } + 8f
      val minY = hit.points.minOf { it.y } - 8f
      val maxY = hit.points.maxOf { it.y } + 8f
      state.copy(
        showLassoSelection = true,
        isLassoResizeMode = false,
        isLassoColorMenuOpen = false,
        lassoLoopPoints =
          listOf(Offset(minX, minY), Offset(maxX, minY), Offset(maxX, maxY), Offset(minX, maxY)),
        lassoSelectedStrokeIds = setOf(hit.id),
        lassoSelectedTextBoxIds = emptySet(),
        lassoSelectedElementIds = emptySet(),
        lassoSelectedStickyIds = emptySet(),
        lassoSelectedImageIds = emptySet(),
      )
    }
  }

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
      )
    }
  }

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
                    strokeWidth = (stroke.strokeWidth * clampedScale).coerceIn(1.5f, 36f),
                  )
                } else stroke
              },
            textBoxes =
              page.textBoxes.map { tb ->
                if (selectedTexts.contains(tb.id)) {
                  tb.copy(
                    position = transformPoint(tb.position),
                    fontSizeSp = (tb.fontSizeSp * clampedScale).coerceIn(9f, 52f),
                  )
                } else tb
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
    if (s.lassoTriggeredFromCircleGesture) {
      _uiState.update { state ->
        state.copy(
          primaryTool = state.previousToolBeforeCircleLasso,
          lassoTriggeredFromCircleGesture = false,
        )
      }
    }
  }

  fun clearLassoSelection() {
    _uiState.update {
      val nextPrimaryTool =
        if (it.lassoTriggeredFromCircleGesture && it.primaryTool == PrimaryTool.LASSO) {
          it.previousToolBeforeCircleLasso
        } else {
          it.primaryTool
        }
      it.copy(
        primaryTool = nextPrimaryTool,
        lassoTriggeredFromCircleGesture = false,
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
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Recolor selection")

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
      val nextTool =
        if (state.lassoTriggeredFromCircleGesture) state.previousToolBeforeCircleLasso
        else state.primaryTool
      state.copy(
        notebooks = updatedNotebooks,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        selectedColor = color,
        primaryTool = nextTool,
        lassoTriggeredFromCircleGesture = false,
        isLassoColorMenuOpen = false,
        statusToastMessage = "Recolored selection",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun scaleLassoSelectionStep(scaleUp: Boolean) {
    recordUndoSnapshotBeforeGesture("Scale selection")
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

    val cx =
      if (state.lassoLoopPoints.isNotEmpty()) state.lassoLoopPoints.map { it.x }.average().toFloat()
      else 0f
    val cy =
      if (state.lassoLoopPoints.isNotEmpty()) state.lassoLoopPoints.map { it.y }.average().toFloat()
      else 0f

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
      val clipboard =
        state.lassoClipboard ?: return@update state.copy(statusToastMessage = "Clipboard is empty")
      val total =
        clipboard.strokes.size +
          clipboard.textBoxes.size +
          clipboard.elements.size +
          clipboard.stickyNotes.size +
          clipboard.images.size
      if (total == 0) return@update state.copy(statusToastMessage = "Clipboard is empty")

      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Paste")

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
        } else {
          val allPts = buildList {
            pastedStrokes.forEach { addAll(it.points) }
            pastedTexts.forEach {
              add(it.position)
              add(it.position + Offset(90f, 28f))
            }
            pastedElements.forEach {
              add(it.position)
              add(it.position + Offset(75f, 32f))
            }
            pastedStickies.forEach {
              add(it.position)
              add(it.position + Offset(120f, 75f))
            }
            pastedImages.forEach {
              add(it.position)
              add(it.position + Offset(it.widthDp * pxPerDp, it.heightDp * pxPerDp))
            }
          }
          if (allPts.isNotEmpty()) {
            val minX = allPts.minOf { it.x } - 16f
            val maxX = allPts.maxOf { it.x } + 16f
            val minY = allPts.minOf { it.y } - 16f
            val maxY = allPts.maxOf { it.y } + 16f
            val midX = (minX + maxX) / 2f
            val midY = (minY + maxY) / 2f
            val rx = ((maxX - minX) / 2f).coerceAtLeast(36f)
            val ry = ((maxY - minY) / 2f).coerceAtLeast(28f)
            (0..24).map { step ->
              val angle = (step.toDouble() / 24.0) * 2.0 * Math.PI
              Offset(
                x = midX + (rx * cos(angle)).toFloat(),
                y = midY + (ry * sin(angle)).toFloat(),
              )
            }
          } else {
            emptyList()
          }
        }

      state.copy(
        notebooks = updatedNotebooks,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
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
      val restoredTool =
        if (it.lassoTriggeredFromCircleGesture) it.previousToolBeforeCircleLasso
        else if (it.primaryTool == PrimaryTool.LASSO) PrimaryTool.WRITING_TOOLS
        else it.primaryTool
      it.copy(
        showLassoSelection = false,
        isLassoResizeMode = false,
        isLassoColorMenuOpen = false,
        lassoTriggeredFromCircleGesture = false,
        lassoLoopPoints = emptyList(),
        lassoSelectedStrokeIds = emptySet(),
        lassoSelectedTextBoxIds = emptySet(),
        lassoSelectedElementIds = emptySet(),
        lassoSelectedStickyIds = emptySet(),
        lassoSelectedImageIds = emptySet(),
        primaryTool = restoredTool,
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

      if (state.primaryTool == PrimaryTool.LASSO) {
        val diag =
          hypot(
            points.maxOf { it.x } - points.minOf { it.x },
            points.maxOf { it.y } - points.minOf { it.y },
          )
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
        val loopPts = simplifyLoop(points)
        val selection = computeLassoSelection(loopPts, currentDoc?.currentPage, state.lassoFilters)
        if (selection.totalCount == 0) {
          // Nothing inside the loop: just drop it, like GoodNotes
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
        return@update state.copy(
          showLassoSelection = true,
          isLassoResizeMode = false,
          isLassoColorMenuOpen = false,
          lassoLoopPoints = loopPts,
          lassoSelectedStrokeIds = selection.strokeIds,
          lassoSelectedTextBoxIds = selection.textBoxIds,
          lassoSelectedElementIds = selection.elementIds,
          lassoSelectedStickyIds = selection.stickyIds,
          lassoSelectedImageIds = selection.imageIds,
        )
      }

      val isWritingToolsActive = state.primaryTool == PrimaryTool.WRITING_TOOLS
      val subTool = state.activeWritingSubTool

      if (isWritingToolsActive && subTool == WritingSubTool.TAPE) {
        val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Add study tape")
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
          undoHistory = nextUndo,
          redoHistory = nextRedo,
        )
      }

      val isPenOrPencil =
        isWritingToolsActive && (subTool == WritingSubTool.PEN || subTool == WritingSubTool.PENCIL)

      if (
        state.scribbleToErase &&
          isPenOrPencil &&
          !snappedFromHold &&
          isScribbleEraseGesture(points)
      ) {
        val scribbleMinX = points.minOf { it.x } - 16f
        val scribbleMaxX = points.maxOf { it.x } + 16f
        val scribbleMinY = points.minOf { it.y } - 16f
        val scribbleMaxY = points.maxOf { it.y } + 16f
        // Only strokes the scribble actually touches are erased (it used to erase anything that
        // merely had a point inside the scribble's bounding box).
        val scribbledIds =
          currentDoc?.currentPage?.strokes.orEmpty()
            .filter { s ->
              strokeTouchedByScribble(
                s, points, scribbleMinX, scribbleMaxX, scribbleMinY, scribbleMaxY, 14f,
              )
            }
            .map { it.id }
            .toSet()
        if (scribbledIds.isNotEmpty()) {
          val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Scribble to erase")
          val (updated, modifiedDoc) =
            updateCurrentPage(state) { page ->
              page.copy(strokes = page.strokes.filterNot { it.id in scribbledIds })
            }
          savedDoc = modifiedDoc
          return@update state.copy(
            notebooks = updated,
            undoHistory = nextUndo,
            redoHistory = nextRedo,
          )
        }
      }

      val isHighlighter = isWritingToolsActive && subTool == WritingSubTool.HIGHLIGHTER
      val isPencil = isWritingToolsActive && subTool == WritingSubTool.PENCIL
      val isExplicitShapeTool =
        state.primaryTool == PrimaryTool.SHAPE ||
          (isWritingToolsActive && subTool == WritingSubTool.DRAW_SHAPE)
      val isShapeStroke = (isExplicitShapeTool || snappedFromHold) && !isHighlighter

      val finalPoints =
        if (isHighlighter && (state.highlighterStraightLine || snappedFromHold)) {
          densifyPolyline(listOf(points.first(), points.last()), 8f)
        } else {
          smoothPoints(points, state.strokeStabilization)
        }

      val baseWidth =
        state.thicknessPresets.getOrElse(state.selectedThicknessIndex) { 6.5f }

      val detectedShapeKind =
        if (isShapeStroke) {
          if (!snappedFromHold && !state.shapeAutoDetectMode) {
            state.selectedShapeKind
          } else {
            autoDetectShapeKind(finalPoints)
          }
        } else null

      val (geomPoints, controlPts) =
        if (detectedShapeKind != null) {
          buildGeometricShapeWithHandles(finalPoints, detectedShapeKind)
        } else {
          finalPoints to emptyList()
        }

      // Align pressure samples 1:1 with geomPoints for natural variable-width rendering
      val alignedPressures =
        if (detectedShapeKind != null || pressures.isEmpty()) {
          emptyList()
        } else if (pressures.size == geomPoints.size) {
          pressures
        } else {
          List(geomPoints.size) { idx ->
            val srcIdx =
              ((idx.toFloat() / (geomPoints.size - 1).coerceAtLeast(1)) *
                  (pressures.size - 1).coerceAtLeast(0))
                .toInt()
                .coerceIn(0, pressures.lastIndex)
            pressures[srcIdx]
          }
        }

      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Draw stroke")
      val strokeId = java.util.UUID.randomUUID().toString()
      val stroke =
        DrawnStroke(
          id = strokeId,
          points = geomPoints,
          pressures = alignedPressures,
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
      state.copy(
        notebooks = updatedNotebooks,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        selectedShapeKind = detectedShapeKind ?: state.selectedShapeKind,
        lastAutoDetectedShape = detectedShapeKind ?: state.lastAutoDetectedShape,
        activeShapeEditStrokeId = if (detectedShapeKind != null) strokeId else null,
      )
    }
    persistDocumentAsync(savedDoc)
  }

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
    var pathLength = 0f
    for (i in 1 until points.size) {
      pathLength += hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y)
    }
    val endToEnd = (points.last() - points.first()).getDistance()
    if (pathLength <= (endToEnd * 3.8f).coerceAtLeast(160f)) return false
    return countAxisReversals(points, horizontal = true) >= 4 ||
      countAxisReversals(points, horizontal = false) >= 4
  }

  private fun countAxisReversals(points: List<Offset>, horizontal: Boolean): Int {
    fun axis(o: Offset) = if (horizontal) o.x else o.y
    var reversals = 0
    var lastTurn = axis(points.first())
    var lastDir = 0
    for (i in 1 until points.size) {
      val span = axis(points[i]) - lastTurn
      if (abs(span) > 22f) {
        val dir = if (span > 0f) 1 else -1
        if (lastDir != 0 && dir != lastDir) reversals++
        lastDir = dir
        lastTurn = axis(points[i])
      }
    }
    return reversals
  }

  private fun strokeTouchedByScribble(
    stroke: DrawnStroke,
    scribble: List<Offset>,
    minX: Float,
    maxX: Float,
    minY: Float,
    maxY: Float,
    reach: Float,
  ): Boolean {
    val pts = stroke.points
    if (pts.isEmpty()) return false
    val pad = stroke.strokeWidth
    if (
      pts.maxOf { it.x } + pad < minX ||
        pts.minOf { it.x } - pad > maxX ||
        pts.maxOf { it.y } + pad < minY ||
        pts.minOf { it.y } - pad > maxY
    ) {
      return false
    }
    val r = reach + stroke.strokeWidth * 0.5f
    for (p in scribble) {
      if (pts.size == 1) {
        if ((pts[0] - p).getDistance() <= r) return true
        continue
      }
      for (k in 0 until pts.size - 1) {
        if (distanceToSegment(p, pts[k], pts[k + 1]) <= r) return true
      }
    }
    return false
  }

  fun autoDetectShapeKind(points: List<Offset>): ShapeKind = ShapeRecognizer.recognize(points)

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
          val simplified =
            simplifyPolygon(points + points.first(), epsilon = diag * 0.08f).dropLast(1)
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
    val smoothed =
      points.indices.map { i ->
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
    // Averaging must not pull the pen-down and pen-up points away from where the pen really was.
    return smoothed.toMutableList().also {
      it[0] = points.first()
      it[it.lastIndex] = points.last()
    }
  }

  fun toggleStudyTapeReveal(tapeId: String) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Toggle study tape")
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
      state.copy(notebooks = updated, undoHistory = nextUndo, redoHistory = nextRedo)
    }
    persistDocumentAsync(savedDoc)
  }

  fun addTextBoxAt(position: Offset, initialText: String = "Tap to edit note") {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Add text box")
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
      state.copy(notebooks = updated, undoHistory = nextUndo, redoHistory = nextRedo)
    }
    persistDocumentAsync(savedDoc)
  }

  fun updateTextBox(id: String, newText: String) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Edit text box")
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            textBoxes =
              if (newText.isBlank()) page.textBoxes.filterNot { it.id == id }
              else page.textBoxes.map { if (it.id == id) it.copy(text = newText) else it }
          )
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated, undoHistory = nextUndo, redoHistory = nextRedo)
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
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Add sticky note")
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
        undoHistory = nextUndo,
        redoHistory = nextRedo,
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
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Delete sticky note")
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(stickyNotes = page.stickyNotes.filterNot { it.id == id })
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated, undoHistory = nextUndo, redoHistory = nextRedo)
    }
    persistDocumentAsync(savedDoc)
  }

  fun insertElementStamp(stampType: ElementStampType) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Insert element")
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
        undoHistory = nextUndo,
        redoHistory = nextRedo,
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
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Insert image")
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
        undoHistory = nextUndo,
        redoHistory = nextRedo,
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

  fun resizeCanvasImage(id: String, scaleFactor: Float) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Resize image")
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            images =
              page.images.map { img ->
                if (img.id == id) {
                  img.copy(
                    widthDp = (img.widthDp * scaleFactor).coerceIn(60f, 480f),
                    heightDp = (img.heightDp * scaleFactor).coerceIn(50f, 420f),
                  )
                } else img
              }
          )
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated, undoHistory = nextUndo, redoHistory = nextRedo)
    }
    persistDocumentAsync(savedDoc)
  }

  fun deleteCanvasImage(id: String) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Delete image")
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(images = page.images.filterNot { it.id == id })
        }
      savedDoc = modifiedDoc
      state.copy(notebooks = updated, undoHistory = nextUndo, redoHistory = nextRedo)
    }
    persistDocumentAsync(savedDoc)
  }

  fun addLaserTrail(points: List<Offset>) {
    if (points.size < 2) return
    _uiState.update { state ->
      val trail =
        com.example.model.LaserTrailStroke(
          points = points,
          color = state.selectedColor,
        )
      state.copy(laserTrails = (state.laserTrails + trail).takeLast(40))
    }
  }

  fun pruneExpiredLaserTrails() {
    val now = System.currentTimeMillis()
    _uiState.update { state ->
      state.copy(laserTrails = state.laserTrails.filter { now - it.createdAtMs < 2300L })
    }
  }

  fun selectSortOption(option: com.example.model.DocumentSortOption) {
    _uiState.update { it.copy(sortOption = option) }
  }

  fun bringSelectionToFront() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Bring to front")
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
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Brought selection to front",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun sendSelectionToBack() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Send to back")
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
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Sent selection to back",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun scaleSelectedStrokes() {
    toggleLassoResizeMode()
  }

  fun duplicateSelectedStrokes() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Duplicate selection")
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
              .filter { selectedStrokes.contains(it.id) }
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
        undoHistory = nextUndo,
        redoHistory = nextRedo,
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

      val (nextUndo, nextRedo) =
        if (hasSpecificSelection) recordHistorySnapshot(state, "Delete selection")
        else (state.undoHistory to state.redoHistory)

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
      val restoredTool =
        if (state.lassoTriggeredFromCircleGesture && state.primaryTool == PrimaryTool.LASSO) {
          state.previousToolBeforeCircleLasso
        } else {
          state.primaryTool
        }
      state.copy(
        notebooks = updated,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        primaryTool = restoredTool,
        lassoTriggeredFromCircleGesture = false,
        showLassoSelection = false,
        isLassoResizeMode = false,
        lassoLoopPoints = emptyList(),
        lassoSelectedStrokeIds = emptySet(),
        lassoSelectedTextBoxIds = emptySet(),
        lassoSelectedElementIds = emptySet(),
        lassoSelectedStickyIds = emptySet(),
        lassoSelectedImageIds = emptySet(),
        activePopover = ActivePopover.NONE,
      )
    }
    persistDocumentAsync(savedDoc)
  }

  // =========================================================================
  // Unified Transactional Undo & Redo (Stage 2C)
  // =========================================================================

  fun undo() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      val currentDoc = state.notebooks.find { it.id == docId } ?: return@update state
      val undoList = state.undoHistory[docId].orEmpty()

      if (undoList.isEmpty()) {
        // Fallback if a stroke was added before snapshot history initialized
        val page = currentDoc.currentPage
        if (page.strokes.isEmpty()) return@update state
        val currSnap =
          DocumentHistorySnapshot(
            pages = currentDoc.pages,
            currentPageIndex = currentDoc.currentPageIndex,
            actionDescription = "Undo stroke",
          )
        val (updatedNotebooks, modifiedDoc) =
          updateCurrentPage(state) { p -> p.copy(strokes = p.strokes.dropLast(1)) }
        savedDoc = modifiedDoc
        val currentRedo = state.redoHistory[docId].orEmpty()
        val nextRedo = state.redoHistory.toMutableMap().apply { put(docId, currentRedo + currSnap) }
        return@update state.copy(
          notebooks = updatedNotebooks,
          redoHistory = nextRedo,
          showLassoSelection = false,
          isLassoResizeMode = false,
        )
      }

      val targetSnap = undoList.last()
      val currentSnapForRedo =
        DocumentHistorySnapshot(
          pages = currentDoc.pages,
          currentPageIndex = currentDoc.currentPageIndex,
          actionDescription = targetSnap.actionDescription,
        )

      val now = System.currentTimeMillis()
      val updatedNotebooks =
        state.notebooks.map { nb ->
          if (nb.id != docId) nb
          else {
            nb.copy(
                pages = targetSnap.pages,
                currentPageIndex =
                  targetSnap.currentPageIndex.coerceIn(
                    0,
                    (targetSnap.pages.size - 1).coerceAtLeast(0),
                  ),
                timestamp = currentTimestamp(),
                revision = nb.revision + 1L,
                updatedAtEpochMs = now,
              )
              .also { savedDoc = it }
          }
        }

      val nextUndo = state.undoHistory.toMutableMap().apply { put(docId, undoList.dropLast(1)) }
      val currentRedo = state.redoHistory[docId].orEmpty()
      val nextRedo =
        state.redoHistory
          .toMutableMap()
          .apply { put(docId, (currentRedo + currentSnapForRedo).takeLast(40)) }

      state.copy(
        notebooks = updatedNotebooks,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        showLassoSelection = false,
        isLassoResizeMode = false,
        lassoLoopPoints = emptyList(),
        lassoSelectedStrokeIds = emptySet(),
        lassoSelectedTextBoxIds = emptySet(),
        lassoSelectedElementIds = emptySet(),
        lassoSelectedStickyIds = emptySet(),
        lassoSelectedImageIds = emptySet(),
        activeShapeEditStrokeId = null,
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun redo() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      val currentDoc = state.notebooks.find { it.id == docId } ?: return@update state
      val redoList = state.redoHistory[docId].orEmpty()
      if (redoList.isEmpty()) return@update state

      val targetSnap = redoList.last()
      val currentSnapForUndo =
        DocumentHistorySnapshot(
          pages = currentDoc.pages,
          currentPageIndex = currentDoc.currentPageIndex,
          actionDescription = targetSnap.actionDescription,
        )

      val now = System.currentTimeMillis()
      val updatedNotebooks =
        state.notebooks.map { nb ->
          if (nb.id != docId) nb
          else {
            nb.copy(
                pages = targetSnap.pages,
                currentPageIndex =
                  targetSnap.currentPageIndex.coerceIn(
                    0,
                    (targetSnap.pages.size - 1).coerceAtLeast(0),
                  ),
                timestamp = currentTimestamp(),
                revision = nb.revision + 1L,
                updatedAtEpochMs = now,
              )
              .also { savedDoc = it }
          }
        }

      val nextRedo = state.redoHistory.toMutableMap().apply { put(docId, redoList.dropLast(1)) }
      val currentUndo = state.undoHistory[docId].orEmpty()
      val nextUndo =
        state.undoHistory
          .toMutableMap()
          .apply { put(docId, (currentUndo + currentSnapForUndo).takeLast(40)) }

      state.copy(
        notebooks = updatedNotebooks,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        showLassoSelection = false,
        isLassoResizeMode = false,
        lassoLoopPoints = emptyList(),
        activeShapeEditStrokeId = null,
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun clearCurrentPageStrokes() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Clear page")
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
        undoHistory = nextUndo,
        redoHistory = nextRedo,
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

  // =========================================================================
  // Page Management & Per-Page Templates (Stage 6)
  // =========================================================================

  fun addNewPageToCurrentDocument() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Add page")
      val docId = state.activeTabId
      val now = System.currentTimeMillis()
      val updated =
        state.notebooks.map { nb ->
          if (nb.id != docId) nb
          else {
            val currentTemplate = nb.currentPage.resolvedTemplate(nb.paperTemplate)
            val currentColor = nb.currentPage.resolvedColorTheme(nb.paperColorTheme)
            val insertIdx = (nb.currentPageIndex + 1).coerceIn(0, nb.pages.size)
            val newPage =
              NotePage(
                pageNumber = insertIdx + 1,
                pageTemplate = currentTemplate,
                pageColorTheme = currentColor,
                customTemplateBitmap = nb.currentPage.customTemplateBitmap,
                customTemplateKey = nb.currentPage.customTemplateKey,
              )
            val mutablePages = nb.pages.toMutableList()
            mutablePages.add(insertIdx, newPage)
            val renumbered =
              mutablePages.mapIndexed { idx, p -> p.copy(pageNumber = idx + 1) }
            nb.copy(
                pages = renumbered,
                currentPageIndex = insertIdx,
                timestamp = currentTimestamp(),
                revision = nb.revision + 1L,
                updatedAtEpochMs = now,
              )
              .also { savedDoc = it }
          }
        }
      state.copy(
        notebooks = updated,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        showLassoSelection = false,
        isLassoResizeMode = false,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Added Page ${(savedDoc?.currentPageIndex ?: 0) + 1}",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun addPageWithTemplate(
    template: PaperTemplate,
    colorTheme: PaperColorTheme? = null,
    customTemplate: CustomPageTemplate? = null,
    insertBeforeCurrent: Boolean = false,
  ) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Add ${template.label} page")
      val docId = state.activeTabId
      val now = System.currentTimeMillis()
      val updated =
        state.notebooks.map { nb ->
          if (nb.id != docId) nb
          else {
            val insertIdx =
              if (insertBeforeCurrent) nb.currentPageIndex.coerceIn(0, nb.pages.size)
              else (nb.currentPageIndex + 1).coerceIn(0, nb.pages.size)
            val newPage =
              NotePage(
                pageNumber = insertIdx + 1,
                pageTemplate = template,
                pageColorTheme = colorTheme ?: nb.paperColorTheme,
                customTemplateBitmap = customTemplate?.previewBitmap,
                customTemplateKey = customTemplate?.bitmapKey ?: "",
              )
            val mutablePages = nb.pages.toMutableList()
            mutablePages.add(insertIdx, newPage)
            val renumbered = mutablePages.mapIndexed { idx, p -> p.copy(pageNumber = idx + 1) }
            nb.copy(
                pages = renumbered,
                currentPageIndex = insertIdx,
                timestamp = currentTimestamp(),
                revision = nb.revision + 1L,
                updatedAtEpochMs = now,
              )
              .also { savedDoc = it }
          }
        }
      state.copy(
        notebooks = updated,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        showLassoSelection = false,
        isLassoResizeMode = false,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Inserted ${template.label} page",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun duplicateCurrentPage() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Duplicate page")
      val docId = state.activeTabId
      val now = System.currentTimeMillis()
      val updated =
        state.notebooks.map { nb ->
          if (nb.id != docId) nb
          else {
            val sourcePage = nb.currentPage
            val clonedPage =
              sourcePage.copy(
                strokes =
                  sourcePage.strokes.map { it.copy(id = java.util.UUID.randomUUID().toString()) },
                studyTapes =
                  sourcePage.studyTapes.map { it.copy(id = java.util.UUID.randomUUID().toString()) },
                textBoxes =
                  sourcePage.textBoxes.map { it.copy(id = java.util.UUID.randomUUID().toString()) },
                stickyNotes =
                  sourcePage.stickyNotes.map { it.copy(id = java.util.UUID.randomUUID().toString()) },
                elements =
                  sourcePage.elements.map { it.copy(id = java.util.UUID.randomUUID().toString()) },
                images =
                  sourcePage.images.map { it.copy(id = java.util.UUID.randomUUID().toString()) },
              )
            val insertIdx = (nb.currentPageIndex + 1).coerceIn(0, nb.pages.size)
            val mutablePages = nb.pages.toMutableList()
            mutablePages.add(insertIdx, clonedPage)
            val renumbered = mutablePages.mapIndexed { idx, p -> p.copy(pageNumber = idx + 1) }
            nb.copy(
                pages = renumbered,
                currentPageIndex = insertIdx,
                timestamp = currentTimestamp(),
                revision = nb.revision + 1L,
                updatedAtEpochMs = now,
              )
              .also { savedDoc = it }
          }
        }
      state.copy(
        notebooks = updated,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Duplicated Page",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun deleteCurrentPage() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      val doc = state.notebooks.find { it.id == docId } ?: return@update state
      if (doc.pages.size <= 1) {
        return@update state.copy(
          statusToastMessage = "A notebook must have at least 1 page"
        )
      }
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Delete page")
      val now = System.currentTimeMillis()
      val updated =
        state.notebooks.map { nb ->
          if (nb.id != docId) nb
          else {
            val removeIdx = nb.currentPageIndex.coerceIn(0, nb.pages.lastIndex)
            val remaining =
              nb.pages
                .filterIndexed { idx, _ -> idx != removeIdx }
                .mapIndexed { idx, p -> p.copy(pageNumber = idx + 1) }
            val nextIdx = removeIdx.coerceAtMost(remaining.lastIndex)
            nb.copy(
                pages = remaining,
                currentPageIndex = nextIdx,
                timestamp = currentTimestamp(),
                revision = nb.revision + 1L,
                updatedAtEpochMs = now,
              )
              .also { savedDoc = it }
          }
        }
      state.copy(
        notebooks = updated,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        showLassoSelection = false,
        isLassoResizeMode = false,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Deleted page",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  /**
   * Changes the current page's template and/or paper tint WITHOUT erasing existing handwriting,
   * shapes, text boxes, or images on the page.
   */
  fun changeCurrentPageTemplate(
    template: PaperTemplate,
    colorTheme: PaperColorTheme? = null,
    customTemplate: CustomPageTemplate? = null,
  ) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val (nextUndo, nextRedo) = recordHistorySnapshot(state, "Change page template")
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(
            pageTemplate = template,
            pageColorTheme = colorTheme ?: page.pageColorTheme,
            customTemplateBitmap =
              if (template == PaperTemplate.CUSTOM) {
                customTemplate?.previewBitmap ?: page.customTemplateBitmap
              } else null,
            customTemplateKey =
              if (template == PaperTemplate.CUSTOM) {
                customTemplate?.bitmapKey ?: page.customTemplateKey
              } else "",
          )
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        undoHistory = nextUndo,
        redoHistory = nextRedo,
        activePopover = ActivePopover.NONE,
        statusToastMessage = "Page template set to ${template.label}",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun updateCurrentNotebookCover(coverStyle: NotebookCoverStyle) {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      val now = System.currentTimeMillis()
      val updated =
        state.notebooks.map { nb ->
          if (nb.id != docId) nb
          else {
            nb.copy(
                coverStyle = coverStyle,
                timestamp = currentTimestamp(),
                revision = nb.revision + 1L,
                updatedAtEpochMs = now,
              )
              .also { savedDoc = it }
          }
        }
      state.copy(
        notebooks = updated,
        statusToastMessage = "Updated cover to ${coverStyle.label}",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun updateBarrelRollAngle(degrees: Float) {
    _uiState.update { it.copy(barrelRollAngleDegrees = ((degrees % 360f) + 360f) % 360f) }
  }

  fun toggleBarrelRollPreview(enabled: Boolean) {
    _uiState.update { it.copy(barrelRollPreviewEnabled = enabled) }
  }

  fun openThankYouScreen() {
    _uiState.update { it.copy(showThankYouScreen = true) }
  }

  fun closeThankYouScreen() {
    _uiState.update { it.copy(showThankYouScreen = false) }
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
            nb.copy(currentPageIndex = clamped, revision = nb.revision + 1L).also { savedDoc = it }
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
      val currDoc = state.notebooks.find { it.id == docId } ?: return@update state
      val entries = PaperTemplate.entries.filter { it != PaperTemplate.CUSTOM }
      val currentTpl = currDoc.currentPage.resolvedTemplate(currDoc.paperTemplate)
      val nextTemplate = entries[(entries.indexOf(currentTpl).coerceAtLeast(0) + 1) % entries.size]
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(pageTemplate = nextTemplate)
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        statusToastMessage = "Page Template: ${nextTemplate.label}",
      )
    }
    persistDocumentAsync(savedDoc)
  }

  fun cyclePaperColorTheme() {
    var savedDoc: NotebookDocument? = null
    _uiState.update { state ->
      val docId = state.activeTabId
      val currDoc = state.notebooks.find { it.id == docId } ?: return@update state
      val entries = PaperColorTheme.entries
      val currentColor = currDoc.currentPage.resolvedColorTheme(currDoc.paperColorTheme)
      val nextColor = entries[(entries.indexOf(currentColor).coerceAtLeast(0) + 1) % entries.size]
      val (updated, modifiedDoc) =
        updateCurrentPage(state) { page ->
          page.copy(pageColorTheme = nextColor)
        }
      savedDoc = modifiedDoc
      state.copy(
        notebooks = updated,
        statusToastMessage = "Paper Tint: ${nextColor.label}",
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
    clearLassoSelection()
  }

  fun toggleReadOnlyMode() {
    _uiState.update { it.copy(isReadOnlyMode = !it.isReadOnlyMode) }
  }

  fun toggleSortOrder() {
    _uiState.update { it.copy(sortByDateAscending = !it.sortByDateAscending) }
  }

  fun selectStylusBrand(brand: com.example.model.StylusPenBrand) {
    prefs
      .edit()
      .putString("stylus_pen_brand", brand.name)
      .putFloat("stylus_pressure_sensitivity", brand.defaultPressureCurve)
      .putBoolean("stylus_tilt_shading", brand.supportsTilt)
      .putBoolean("stylus_tail_eraser", brand.supportsTailEraser)
      .apply()
    _uiState.update {
      it.copy(
        selectedStylusBrand = brand,
        stylusPressureSensitivity = brand.defaultPressureCurve,
        stylusTiltShadingEnabled = brand.supportsTilt,
        stylusTailEraserEnabled = brand.supportsTailEraser,
        statusToastMessage = "Applied ${brand.label} profile",
      )
    }
  }

  fun selectStylusBarrelAction(action: com.example.model.StylusBarrelAction) {
    prefs.edit().putString("stylus_barrel_action", action.name).apply()
    val holdEraseEnabled = action == com.example.model.StylusBarrelAction.ERASE_WHILE_HELD
    prefs.edit().putBoolean("hold_to_erase", holdEraseEnabled).apply()
    _uiState.update {
      it.copy(
        stylusBarrelAction = action,
        holdToErase = holdEraseEnabled,
      )
    }
  }

  fun updateStylusPressureSensitivity(value: Float) {
    val clamped = value.coerceIn(0.5f, 1.6f)
    prefs.edit().putFloat("stylus_pressure_sensitivity", clamped).apply()
    _uiState.update { it.copy(stylusPressureSensitivity = clamped) }
  }

  fun toggleStylusTiltShading(enabled: Boolean) {
    prefs.edit().putBoolean("stylus_tilt_shading", enabled).apply()
    _uiState.update { it.copy(stylusTiltShadingEnabled = enabled) }
  }

  fun toggleStylusHoverCursor(enabled: Boolean) {
    prefs.edit().putBoolean("stylus_hover_cursor", enabled).apply()
    _uiState.update { it.copy(stylusHoverCursorEnabled = enabled) }
  }

  fun toggleStylusTailEraser(enabled: Boolean) {
    prefs.edit().putBoolean("stylus_tail_eraser", enabled).apply()
    _uiState.update { it.copy(stylusTailEraserEnabled = enabled) }
  }

  fun updateDeveloperUpiInfo(upiId: String, displayName: String = _uiState.value.developerDisplayName) {
    val cleanUpi = upiId.trim().ifBlank { "itznikk@upi" }
    val cleanName = displayName.trim().ifBlank { "ItzNikk" }
    prefs
      .edit()
      .putString("developer_upi_id", cleanUpi)
      .putString("developer_display_name", cleanName)
      .apply()
    _uiState.update {
      it.copy(
        developerUpiId = cleanUpi,
        developerDisplayName = cleanName,
        statusToastMessage = "Saved UPI ID: $cleanUpi",
      )
    }
  }

  fun startInteractiveTour() {
    val state = _uiState.value
    if (state.notebooks.none { !it.isTrashed }) {
      createNewNotebook(
        title = "Interactive Tour Sandbox",
        coverStyle = NotebookCoverStyle.CHARCOAL_DARK,
        paperTemplate = PaperTemplate.DOTTED,
        paperColorTheme = PaperColorTheme.PURE_WHITE,
      )
    } else {
      val targetDoc =
        state.notebooks.find { it.id == state.activeTabId && !it.isTrashed }
          ?: state.notebooks.first { !it.isTrashed }
      openDocument(targetDoc.id)
    }
    applyInteractiveTourStepTool(0)
  }

  fun advanceInteractiveTourStep() {
    val nextStep = _uiState.value.interactiveTourStep + 1
    if (nextStep >= 6) {
      dismissInteractiveTour()
      _uiState.update {
        it.copy(statusToastMessage = "Tour complete! You're ready to create.")
      }
    } else {
      applyInteractiveTourStepTool(nextStep)
    }
  }

  fun previousInteractiveTourStep() {
    val prevStep = (_uiState.value.interactiveTourStep - 1).coerceAtLeast(0)
    applyInteractiveTourStepTool(prevStep)
  }

  private fun applyInteractiveTourStepTool(step: Int) {
    _uiState.update { state ->
      when (step) {
        0 ->
          state.copy(
            screenMode = ScreenMode.EDITOR,
            showInteractiveTour = true,
            interactiveTourStep = 0,
            primaryTool = PrimaryTool.WRITING_TOOLS,
            activeWritingSubTool = WritingSubTool.PEN,
            penStyle = PenStyle.FOUNTAIN,
            activePopover = ActivePopover.NONE,
          )
        1 ->
          state.copy(
            screenMode = ScreenMode.EDITOR,
            showInteractiveTour = true,
            interactiveTourStep = 1,
            primaryTool = PrimaryTool.WRITING_TOOLS,
            activeWritingSubTool = WritingSubTool.PEN,
            drawAndHold = true,
            activePopover = ActivePopover.NONE,
          )
        2 ->
          state.copy(
            screenMode = ScreenMode.EDITOR,
            showInteractiveTour = true,
            interactiveTourStep = 2,
            primaryTool = PrimaryTool.SHAPE,
            shapeAutoDetectMode = false,
            selectedShapeKind = ShapeKind.STAR,
            fillShapes = true,
            activePopover = ActivePopover.NONE,
          )
        3 ->
          state.copy(
            screenMode = ScreenMode.EDITOR,
            showInteractiveTour = true,
            interactiveTourStep = 3,
            primaryTool = PrimaryTool.WRITING_TOOLS,
            activeWritingSubTool = WritingSubTool.PEN,
            circleToLasso = true,
            activePopover = ActivePopover.NONE,
          )
        4 ->
          state.copy(
            screenMode = ScreenMode.EDITOR,
            showInteractiveTour = true,
            interactiveTourStep = 4,
            primaryTool = PrimaryTool.LASER,
            laserMode = LaserMode.FADING_TRAIL,
            activePopover = ActivePopover.NONE,
          )
        else ->
          state.copy(
            screenMode = ScreenMode.EDITOR,
            showInteractiveTour = true,
            interactiveTourStep = 5,
            primaryTool = PrimaryTool.ERASER,
            activePopover = ActivePopover.NONE,
          )
      }
    }
  }

  fun dismissInteractiveTour() {
    _uiState.update {
      it.copy(
        showInteractiveTour = false,
        interactiveTourStep = 0,
        primaryTool = PrimaryTool.WRITING_TOOLS,
        activeWritingSubTool = WritingSubTool.PEN,
      )
    }
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
