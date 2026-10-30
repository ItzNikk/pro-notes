package com.example

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ActivePopover
import com.example.model.ScreenMode
import com.example.ui.components.CreateNotebookDialog
import com.example.ui.components.TopDocumentTabBar
import com.example.ui.components.UnlockVaultDialog
import com.example.ui.screens.DocumentsScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PopoverBg
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TopTabBarBg
import com.example.util.PdfImportHelper
import com.example.viewmodel.ProNotesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.parseColor("#0E1015")),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.parseColor("#161719")),
    )
    setContent {
      MyApplicationTheme {
        ProNotesApp()
      }
    }
  }
}

@Composable
fun ProNotesApp(
  viewModel: ProNotesViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // Ensure screen recording and screenshots are always unrestricted
  LaunchedEffect(Unit) {
    val activity = context as? Activity
    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
  }

  val openTabDocuments =
    uiState.openTabIds.mapNotNull { tabId ->
      uiState.notebooks.find { it.id == tabId }
    }
  val activeDocument = uiState.notebooks.find { it.id == uiState.activeTabId }

  val documentSha256Hash =
    remember(activeDocument) {
      if (activeDocument != null) {
        PdfImportHelper.computeDocumentSha256(activeDocument)
      } else {
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
      }
    }

  val pdfPickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        coroutineScope.launch {
          val imported = PdfImportHelper.importPdfFromUri(context, uri)
          if (imported != null) {
            viewModel.importPdfDocument(title = imported.first, pages = imported.second)
          }
        }
      }
    }

  val exportPdfLauncher =
    rememberLauncherForActivityResult(
      ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
      val doc = activeDocument
      if (uri != null && doc != null) {
        coroutineScope.launch {
          val ok =
            PdfImportHelper.exportNotebookToPdfUri(
              context = context,
              document = doc,
              targetUri = uri,
              includeSecurityWatermark = uiState.watermarkConfidentialExports,
            )
          viewModel.showToast(
            if (ok) "Exported \"${doc.title}.pdf\" (${doc.pages.size} pages) with SHA-256 verification!"
            else "Failed to export PDF."
          )
        }
      }
    }

  val exportPngLauncher =
    rememberLauncherForActivityResult(
      ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
      val doc = activeDocument
      val page = doc?.currentPage
      if (uri != null && doc != null && page != null) {
        coroutineScope.launch {
          val ok =
            PdfImportHelper.exportPageToPngUri(
              context = context,
              document = doc,
              page = page,
              targetUri = uri,
              includeSecurityWatermark = uiState.watermarkConfidentialExports,
            )
          viewModel.showToast(
            if (ok) "Exported Page ${page.pageNumber} as high-res PNG!"
            else "Failed to export PNG."
          )
        }
      }
    }

  val photoPickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        coroutineScope.launch {
          val bmp = PdfImportHelper.decodeImageFromUri(context, uri)
          if (bmp != null) {
            viewModel.insertCanvasImage(bmp)
          }
        }
      }
    }

  LaunchedEffect(uiState.statusToastMessage) {
    if (uiState.statusToastMessage != null) {
      delay(2600L)
      viewModel.clearToast()
    }
  }

  BackHandler(
    enabled =
      uiState.pendingUnlockNotebookId != null ||
        uiState.activePopover != ActivePopover.NONE ||
        uiState.screenMode == ScreenMode.EDITOR ||
        uiState.screenMode == ScreenMode.SETTINGS
  ) {
    when {
      uiState.pendingUnlockNotebookId != null -> viewModel.dismissPendingVaultUnlock()
      uiState.activePopover != ActivePopover.NONE -> viewModel.dismissPopover()
      uiState.screenMode == ScreenMode.SETTINGS -> viewModel.closeSettingsScreen()
      uiState.screenMode == ScreenMode.EDITOR -> viewModel.switchToDocuments()
    }
  }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(TopTabBarBg)
        .windowInsetsPadding(WindowInsets.safeDrawing),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopDocumentTabBar(
        screenMode = uiState.screenMode,
        openTabs = openTabDocuments,
        activeTabId = uiState.activeTabId,
        isReadOnlyMode = uiState.isReadOnlyMode,
        onHomeClick = { viewModel.switchToDocuments() },
        onTabClick = { docId -> viewModel.openDocument(docId) },
        onTabClose = { docId -> viewModel.closeTab(docId) },
        onNewTabClick = { viewModel.togglePopover(ActivePopover.NEW_NOTE_DIALOG) },
        onToggleReadOnly = { viewModel.toggleReadOnlyMode() },
      )

      AnimatedContent(
        targetState = uiState.screenMode,
        transitionSpec = {
          fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "screen_mode_transition",
        modifier = Modifier.weight(1f),
      ) { mode ->
        when (mode) {
          ScreenMode.DOCUMENTS -> {
            DocumentsScreen(
              selectedSection = uiState.sidebarSection,
              notebooks = uiState.notebooks,
              sortByDateAscending = uiState.sortByDateAscending,
              onlyDrawWithStylus = uiState.onlyDrawWithStylus,
              isSearchBarVisible = uiState.isSearchBarVisible,
              searchQuery = uiState.searchQuery,
              onSelectSection = { viewModel.selectSidebarSection(it) },
              onOpenNotebook = { viewModel.openDocument(it) },
              onToggleFavorite = { viewModel.toggleFavorite(it) },
              onMoveToTrash = { viewModel.moveNotebookToTrash(it) },
              onDeletePermanently = { viewModel.deleteNotebookPermanently(it) },
              onOpenCreateNoteDialog = { viewModel.togglePopover(ActivePopover.NEW_NOTE_DIALOG) },
              onCreateNewNotebook = { cover, template ->
                viewModel.createNewNotebook(coverStyle = cover, paperTemplate = template)
              },
              onImportPdfFromDevice = {
                pdfPickerLauncher.launch(arrayOf("application/pdf"))
              },
              onImportSamplePdf = {
                coroutineScope.launch {
                  val pages = PdfImportHelper.createSamplePdfPages()
                  viewModel.importPdfDocument("Quantitative Calculus PDF", pages)
                }
              },
              onToggleSortOrder = { viewModel.toggleSortOrder() },
              onToggleOnlyDrawWithStylus = {
                viewModel.toggleOnlyDrawWithStylus(!uiState.onlyDrawWithStylus)
              },
              onToggleSearchBar = { viewModel.toggleSearchBar() },
              onUpdateSearchQuery = { viewModel.updateSearchQuery(it) },
              onOpenSettings = { viewModel.openSettingsScreen() },
              modifier = Modifier.fillMaxSize(),
            )
          }

          ScreenMode.EDITOR -> {
            EditorScreen(
              activeDocument = activeDocument,
              primaryTool = uiState.primaryTool,
              activeWritingSubTool = uiState.activeWritingSubTool,
              enabledToolbarTools = uiState.enabledToolbarTools,
              enabledWritingSubTools = uiState.enabledWritingSubTools,
              toolbarDockPosition = uiState.toolbarDockPosition,
              pageScrollDirection = uiState.pageScrollDirection,
              subToolbarOffset = uiState.subToolbarOffset,
              onlyDrawWithStylus = uiState.onlyDrawWithStylus,
              pullToAddPage = uiState.pullToAddPage,
              isReadOnlyMode = uiState.isReadOnlyMode,
              penStyle = uiState.penStyle,
              strokePattern = uiState.strokePattern,
              strokeStabilization = uiState.strokeStabilization,
              drawAndHold = uiState.drawAndHold,
              holdToErase = uiState.holdToErase,
              scribbleToErase = uiState.scribbleToErase,
              circleToLasso = uiState.circleToLasso,
              highlighterStraightLine = uiState.highlighterStraightLine,
              eraserMode = uiState.eraserMode,
              eraserRadius = uiState.eraserRadius,
              eraserAutoDeselect = uiState.eraserAutoDeselect,
              lastAutoDetectedShape = uiState.lastAutoDetectedShape,
              fillShapes = uiState.fillShapes,
              activeShapeEditStrokeId = uiState.activeShapeEditStrokeId,
              laserMode = uiState.laserMode,
              lassoFilters = uiState.lassoFilters,
              selectedThicknessIndex = uiState.selectedThicknessIndex,
              thicknessPresets = uiState.thicknessPresets,
              quickSwatches = uiState.quickSwatches,
              customPalette = uiState.customPalette,
              selectedSwatchIndex = uiState.selectedSwatchIndex,
              selectedColor = uiState.selectedColor,
              isEyedropperActive = uiState.isEyedropperActive,
              activePopover = uiState.activePopover,
              showLassoSelection = uiState.showLassoSelection,
              isLassoResizeMode = uiState.isLassoResizeMode,
              lassoLoopPoints = uiState.lassoLoopPoints,
              lassoSelectedStrokeIds = uiState.lassoSelectedStrokeIds,
              rulerState = uiState.rulerState,
              isZoomWindowVisible = uiState.isZoomWindowVisible,
              zoomTargetBoxOffset = uiState.zoomTargetBoxOffset,
              isAudioBarVisible = uiState.isAudioBarVisible,
              isRecordingAudio = uiState.isRecordingAudio,
              isPlayingAudioReplay = uiState.isPlayingAudioReplay,
              recordedAudioSeconds = uiState.recordedAudioSeconds,
              isPageSidebarOpen = uiState.isPageSidebarOpen,
              isSPenBarrelButtonActive = uiState.isSPenBarrelButtonActive,
              privacyScreenShieldEnabled = uiState.privacyScreenShieldEnabled,
              watermarkConfidentialExports = uiState.watermarkConfidentialExports,
              documentSha256Hash = documentSha256Hash,
              canUndo = activeDocument?.currentPage?.strokes?.isNotEmpty() == true,
              canRedo = uiState.redoStack[uiState.activeTabId]?.isNotEmpty() == true,
              onSelectPrimaryTool = { viewModel.selectPrimaryTool(it) },
              onSelectWritingSubTool = { viewModel.selectWritingSubTool(it) },
              onSelectToolbarDockPosition = { viewModel.setToolbarDockPosition(it) },
              onCycleToolbarDockPosition = { viewModel.cycleToolbarDockPosition() },
              onTogglePageScrollDirection = { viewModel.togglePageScrollDirection() },
              onMoveSubToolbar = { viewModel.moveSubToolbar(it) },
              onResetSubToolbarPosition = { viewModel.resetSubToolbarPosition() },
              onToggleOnlyDrawWithStylus = { viewModel.toggleOnlyDrawWithStylus(it) },
              onToggleSPenBarrelButtonMode = { viewModel.toggleSPenBarrelButtonMode() },
              onTogglePopover = { viewModel.togglePopover(it) },
              onDismissPopover = { viewModel.dismissPopover() },
              onSelectPenStyle = { viewModel.selectPenStyle(it) },
              onSelectStrokePattern = { viewModel.selectStrokePattern(it) },
              onStabilizationChange = { viewModel.updateStrokeStabilization(it) },
              onToggleDrawAndHold = { viewModel.toggleDrawAndHold(it) },
              onToggleHoldToErase = { viewModel.toggleHoldToErase(it) },
              onToggleScribbleToErase = { viewModel.toggleScribbleToErase(it) },
              onToggleCircleToLasso = { viewModel.toggleCircleToLasso(it) },
              onToggleHighlighterStraightLine = { viewModel.toggleHighlighterStraightLine() },
              onSelectEraserMode = { viewModel.selectEraserMode(it) },
              onEraserRadiusChange = { viewModel.updateEraserRadius(it) },
              onToggleEraserAutoDeselect = { viewModel.toggleEraserAutoDeselect(it) },
              onInsertQuickShape = { viewModel.insertQuickShape(it) },
              onToggleFillShapes = { viewModel.toggleFillShapes(it) },
              onMoveShapeControlHandle = { strokeId, idx, delta ->
                viewModel.moveShapeControlHandle(strokeId, idx, delta)
              },
              onToggleLaserMode = { viewModel.toggleLaserMode() },
              onUpdateLassoFilters = { viewModel.updateLassoFilters(it) },
              onBringToFront = { viewModel.bringSelectionToFront() },
              onSendToBack = { viewModel.sendSelectionToBack() },
              onScaleSelection = { viewModel.scaleSelectedStrokes() },
              onScaleLassoSelectionStep = { viewModel.scaleLassoSelectionStep(it) },
              onCopyLassoSelection = { viewModel.copyLassoSelection() },
              onCutLassoSelection = { viewModel.cutLassoSelection() },
              onPasteLassoSelection = { viewModel.pasteLassoClipboard() },
              onRecolorLassoSelection = { viewModel.recolorLassoSelection(it) },
              hasClipboardContent = uiState.lassoClipboard != null,
              isLassoColorMenuOpen = uiState.isLassoColorMenuOpen,
              onToggleLassoColorMenu = { viewModel.toggleLassoColorMenu() },
              onDuplicateSelection = { viewModel.duplicateSelectedStrokes() },
              onDeleteLassoSelection = { viewModel.deleteLassoSelection() },
              onMoveLassoSelection = { viewModel.moveLassoSelection(it) },
              onTransformLassoSelection = { scale, rot ->
                viewModel.transformLassoSelection(scale, rot)
              },
              onClearLassoSelection = { viewModel.clearLassoSelection() },
              onCommitLassoChanges = { viewModel.commitLassoChanges() },
              onSetCanvasDensity = { viewModel.setCanvasDensity(it) },
              onSelectThicknessIndex = { viewModel.selectThicknessIndex(it) },
              onSelectQuickSwatch = { viewModel.selectQuickSwatch(it) },
              onSelectPaletteColor = { viewModel.selectPaletteColor(it) },
              onAddNewColorToToolbar = { viewModel.addNewColorToToolbar(it) },
              onAddCustomColorToPalette = { viewModel.addCustomColorToPalette(it) },
              onRemoveCurrentToolbarSlot = { viewModel.removeCurrentToolbarSwatchSlot() },
              onToggleEyedropper = { viewModel.toggleEyedropper() },
              onSampleColorAt = { viewModel.sampleColorAt(it) },
              onInstantCircleToLasso = { viewModel.triggerInstantCircleToLasso(it) },
              onTryCircleToLassoAtHold = { viewModel.tryConvertDrawnCircleStrokeToLassoAtHold(it) },
              onAddStroke = { pts, pressures, heldEnd ->
                viewModel.addStroke(pts, pressures, heldEnd)
              },
              onEraseAtPoints = { pts, temp -> viewModel.eraseAtPoints(pts, temp) },
              onToggleStudyTapeReveal = { viewModel.toggleStudyTapeReveal(it) },
              onAddTextBoxAt = { pos, txt -> viewModel.addTextBoxAt(pos, txt) },
              onUpdateTextBox = { id, txt -> viewModel.updateTextBox(id, txt) },
              onMoveTextBox = { id, delta -> viewModel.moveTextBox(id, delta) },
              onAddStickyNoteAt = { viewModel.addStickyNoteAt(it) },
              onToggleStickyNoteExpanded = { viewModel.toggleStickyNoteExpanded(it) },
              onUpdateStickyNoteText = { id, txt -> viewModel.updateStickyNoteText(id, txt) },
              onDeleteStickyNote = { viewModel.deleteStickyNote(it) },
              onInsertElementStamp = { viewModel.insertElementStamp(it) },
              onMoveElementStamp = { id, delta -> viewModel.moveElementStamp(id, delta) },
              onMoveCanvasImage = { id, delta -> viewModel.moveCanvasImage(id, delta) },
              onPickImageFromDevice = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              onImportPdfFromDevice = {
                pdfPickerLauncher.launch(arrayOf("application/pdf"))
              },
              onExportFullPdf = {
                val safeTitle = (activeDocument?.title ?: "ProNotes_Export").replace(" ", "_")
                exportPdfLauncher.launch("$safeTitle.pdf")
              },
              onExportCurrentPagePng = {
                val safeTitle = (activeDocument?.title ?: "ProNotes_Page").replace(" ", "_")
                val pageNum = activeDocument?.currentPage?.pageNumber ?: 1
                exportPngLauncher.launch("${safeTitle}_Page_$pageNum.png")
              },
              onCyclePaperColorTheme = { viewModel.cyclePaperColorTheme() },
              onSetVaultPin = { viewModel.lockCurrentNotebookWithPin(it) },
              onRemoveVaultLock = { viewModel.removeCurrentNotebookLock() },
              onTogglePrivacyScreenShield = { viewModel.togglePrivacyScreenShield(it) },
              onToggleWatermark = { viewModel.toggleWatermarkConfidentialExports(it) },
              onUndo = { viewModel.undo() },
              onRedo = { viewModel.redo() },
              onClearPageStrokes = { viewModel.clearCurrentPageStrokes() },
              onAddNewPage = { viewModel.addNewPageToCurrentDocument() },
              onSelectPageIndex = { viewModel.selectPageIndex(it) },
              onToggleCurrentPageBookmark = { viewModel.toggleCurrentPageBookmark() },
              onTogglePageSidebar = { viewModel.togglePageSidebar() },
              onCyclePaperTemplate = { viewModel.cyclePaperTemplate() },
              onToggleLassoSelection = { viewModel.toggleLassoSelectionVisibility() },
              onToggleRuler = { viewModel.toggleRuler() },
              onMoveRuler = { viewModel.moveRuler(it) },
              onRotateRuler = { viewModel.rotateRuler(it) },
              onToggleZoomWindow = { viewModel.toggleZoomWindow() },
              onAdvanceZoomBox = { dx, dy -> viewModel.advanceZoomTargetBox(dx, dy) },
              onToggleAudioBar = { viewModel.toggleAudioBar() },
              onToggleAudioRecording = { viewModel.toggleAudioRecording() },
              onToggleAudioReplay = { viewModel.toggleAudioReplay() },
              onToggleToolbarToolEnabled = { viewModel.toggleToolbarToolEnabled(it) },
              onToggleWritingSubToolEnabled = { viewModel.toggleWritingSubToolEnabled(it) },
              onResetToolbarDefaults = { viewModel.resetToolbarDefaults() },
              onResetWritingToolsDefaults = { viewModel.resetWritingToolsDefaults() },
              onOpenSettings = { viewModel.openSettingsScreen() },
              modifier = Modifier.fillMaxSize(),
            )
          }

          ScreenMode.SETTINGS -> {
            SettingsScreen(
              notebooks = uiState.notebooks,
              onlyDrawWithStylus = uiState.onlyDrawWithStylus,
              palmRejectionSensitivity = uiState.palmRejectionSensitivity,
              isLeftHandedPosture = uiState.isLeftHandedPosture,
              toolbarDockPosition = uiState.toolbarDockPosition,
              pageScrollDirection = uiState.pageScrollDirection,
              pullToAddPage = uiState.pullToAddPage,
              eraserAutoDeselect = uiState.eraserAutoDeselect,
              circleToLasso = uiState.circleToLasso,
              drawAndHold = uiState.drawAndHold,
              scribbleToErase = uiState.scribbleToErase,
              holdToErase = uiState.holdToErase,
              privacyScreenShieldEnabled = uiState.privacyScreenShieldEnabled,
              watermarkConfidentialExports = uiState.watermarkConfidentialExports,
              onToggleOnlyDrawWithStylus = { viewModel.toggleOnlyDrawWithStylus(it) },
              onSelectPalmSensitivity = { viewModel.setPalmRejectionSensitivity(it) },
              onToggleLeftHandedPosture = { viewModel.toggleLeftHandedPosture(it) },
              onSelectToolbarDockPosition = { viewModel.setToolbarDockPosition(it) },
              onSelectPageScrollDirection = { viewModel.setPageScrollDirection(it) },
              onResetSubToolbarPosition = { viewModel.resetSubToolbarPosition() },
              onTogglePullToAddPage = { viewModel.togglePullToAddPage(it) },
              onToggleEraserAutoDeselect = { viewModel.toggleEraserAutoDeselect(it) },
              onToggleCircleToLasso = { viewModel.toggleCircleToLasso(it) },
              onToggleDrawAndHold = { viewModel.toggleDrawAndHold(it) },
              onToggleScribbleToErase = { viewModel.toggleScribbleToErase(it) },
              onToggleHoldToErase = { viewModel.toggleHoldToErase(it) },
              onTogglePrivacyScreenShield = { viewModel.togglePrivacyScreenShield(it) },
              onToggleWatermarkConfidentialExports = {
                viewModel.toggleWatermarkConfidentialExports(it)
              },
              onEmptyTrash = { viewModel.emptyTrash() },
              onClearAllLocalData = { viewModel.clearAllLocalStorage() },
              onBackClick = { viewModel.closeSettingsScreen() },
              modifier = Modifier.fillMaxSize(),
            )
          }
        }
      }
    }

    if (uiState.activePopover == ActivePopover.NEW_NOTE_DIALOG) {
      CreateNotebookDialog(
        onCreateNotebook = { title, cover, template ->
          viewModel.createNewNotebook(
            title = title,
            coverStyle = cover,
            paperTemplate = template,
          )
        },
        onPickPdfFromDevice = {
          viewModel.dismissPopover()
          pdfPickerLauncher.launch(arrayOf("application/pdf"))
        },
        onLoadSamplePdf = {
          coroutineScope.launch {
            val pages = PdfImportHelper.createSamplePdfPages()
            viewModel.importPdfDocument("Quantitative Calculus PDF", pages)
          }
        },
        onDismiss = { viewModel.dismissPopover() },
      )
    }

    val pendingVaultDoc =
      uiState.pendingUnlockNotebookId?.let { id -> uiState.notebooks.find { it.id == id } }
    if (pendingVaultDoc != null) {
      UnlockVaultDialog(
        notebookTitle = pendingVaultDoc.title,
        onAttemptUnlock = { pin -> viewModel.attemptUnlockVaultNotebook(pendingVaultDoc.id, pin) },
        onDismiss = { viewModel.dismissPendingVaultUnlock() },
      )
    }

    uiState.statusToastMessage?.let { msg ->
      Box(
        modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 24.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(PopoverBg)
            .padding(horizontal = 16.dp, vertical = 8.dp),
      ) {
        Text(
          text = msg,
          color = TextPrimaryWhite,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
        )
      }
    }
  }
}
