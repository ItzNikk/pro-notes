package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivePopover
import com.example.model.CustomPageTemplate
import com.example.model.DocumentType
import com.example.model.DrawnStroke
import com.example.model.EraserMode
import com.example.model.LaserMode
import com.example.model.LaserTrailPoint
import com.example.model.LassoFilterSettings
import com.example.model.LassoShape
import com.example.model.NotePage
import com.example.model.NotebookDocument
import com.example.model.PageOrientation
import com.example.model.PageScrollDirection
import com.example.model.PaperColorTheme
import com.example.model.PaperTemplate
import com.example.model.PenStyle
import com.example.model.PrimaryTool
import com.example.model.RulerState
import com.example.model.ShapeKind
import com.example.model.StrokePattern
import com.example.model.ToolbarDockPosition
import com.example.model.WritingSubTool
import com.example.model.isClosedCircleLoop
import com.example.model.isPointNearPendingLoopEndpoint
import com.example.ui.components.AddPageDocumentIcon
import com.example.ui.components.DottedColorRingIcon
import com.example.ui.components.EraserSettingsPopover
import com.example.ui.components.ExportAndSharePopover
import com.example.ui.components.ExportShareBoxIcon
import com.example.ui.components.HighlighterSettingsPopover
import com.example.ui.components.InteractiveRulerOverlay
import com.example.ui.components.LassoFloatingMenuPill
import com.example.ui.components.LassoSettingsPopover
import com.example.ui.components.MoreCircleDotsIcon
import com.example.ui.components.PageBookmarkIcon
import com.example.ui.components.PenColorPopover
import com.example.ui.components.PenSettingsPopover
import com.example.ui.components.PrimaryToolCustomIcon
import com.example.ui.components.RedoCurvedArrowIcon
import com.example.ui.components.SecurityVaultPopover
import com.example.ui.components.SecurityVaultShieldIcon
import com.example.ui.components.ShapeSettingsPopover
import com.example.ui.components.SidebarThumbnailsIcon
import com.example.ui.components.StrokePatternIcon
import com.example.ui.components.TemplatePreviewCanvas
import com.example.ui.components.TimeKeeperPopover
import com.example.ui.components.ToolbarCustomizationPopover
import com.example.ui.components.TopDocumentTabBar
import com.example.ui.components.UndoCurvedArrowIcon
import com.example.ui.components.WritingSubToolCustomIcon
import com.example.ui.components.ZoomWindowPanel
import com.example.ui.components.drawPaperTemplateLines
import com.example.ui.theme.ActiveToolIconDark
import com.example.ui.theme.ActiveToolPill
import com.example.ui.theme.CanvasOuterGray
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LocalProNotesColors
import com.example.ui.theme.PopoverBg
import com.example.ui.theme.PopoverCardSelected
import com.example.ui.theme.PopoverDivider
import com.example.ui.theme.PopoverInnerBg
import com.example.ui.theme.SidebarDarkBg
import com.example.ui.theme.SubToolbarActiveCyan
import com.example.ui.theme.SubToolbarActiveTeal
import com.example.ui.theme.SubToolbarBorder
import com.example.ui.theme.SubToolbarPillBg
import com.example.ui.theme.SwitchBlue
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray
import com.example.ui.theme.ToolbarIconLight
import com.example.ui.theme.ToolbarNavy
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun EditorScreen(
  uiState: com.example.viewmodel.ProNotesUiState,
  viewModel: com.example.viewmodel.ProNotesViewModel,
  palmRejectionSensitivity: Float,
  hasClipboardContent: Boolean,
  documentSha256Hash: String,
  onImportPdfFromDevice: () -> Unit,
  onImportImageFromDevice: () -> Unit,
  onImportCustomTemplateFromDevice: () -> Unit = {},
  onExportFullPdf: () -> Unit,
  onExportCurrentPagePng: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val notebooks = remember(uiState.notebooks) { uiState.notebooks.filter { !it.isTrashed } }
  val activeTabId = uiState.activeTabId
  val isReadOnlyMode = uiState.isReadOnlyMode
  val primaryTool = uiState.primaryTool
  val activeWritingSubTool = uiState.activeWritingSubTool
  val enabledToolbarTools = uiState.enabledToolbarTools
  val enabledWritingSubTools = uiState.enabledWritingSubTools
  val penStyle = uiState.penStyle
  val strokePattern = uiState.strokePattern
  val selectedColor = uiState.selectedColor
  val quickSwatches = uiState.quickSwatches
  val selectedSwatchIndex = uiState.selectedSwatchIndex
  val customPalette = uiState.customPalette
  val selectedThicknessIndex = uiState.selectedThicknessIndex
  val thicknessPresets = uiState.thicknessPresets
  val strokeStabilization = uiState.strokeStabilization
  val drawAndHold = uiState.drawAndHold
  val holdToErase = uiState.holdToErase
  val scribbleToErase = uiState.scribbleToErase
  val circleToLasso = uiState.circleToLasso
  val isSPenBarrelButtonActive = uiState.isSPenBarrelButtonActive
  val onlyDrawWithStylus = uiState.onlyDrawWithStylus
  val leftHandedPosture = uiState.isLeftHandedPosture
  val pullToAddPage = uiState.pullToAddPage
  val pageScrollDirection = uiState.pageScrollDirection
  val toolbarDockPosition = uiState.toolbarDockPosition
  val subToolbarOffset = uiState.subToolbarOffset
  val highlighterStraightLine = uiState.highlighterStraightLine
  val eraserMode = uiState.eraserMode
  val eraserRadius = uiState.eraserRadius
  val eraserAutoDeselect = uiState.eraserAutoDeselect
  val fillShapes = uiState.fillShapes
  val lastAutoDetectedShape = uiState.lastAutoDetectedShape
  val activeShapeEditStrokeId = uiState.activeShapeEditStrokeId
  val lassoFilters = uiState.lassoFilters
  val showLassoSelection = uiState.showLassoSelection
  val isLassoResizeMode = uiState.isLassoResizeMode
  val isLassoColorMenuOpen = uiState.isLassoColorMenuOpen
  val lassoLoopPoints = uiState.lassoLoopPoints
  val lassoSelectedStrokeIds = uiState.lassoSelectedStrokeIds
  val lassoSelectedTextBoxIds = uiState.lassoSelectedTextBoxIds
  val lassoSelectedElementIds = uiState.lassoSelectedElementIds
  val lassoSelectedStickyIds = uiState.lassoSelectedStickyIds
  val lassoSelectedImageIds = uiState.lassoSelectedImageIds
  val activePopover = uiState.activePopover
  val isPageSidebarOpen = uiState.isPageSidebarOpen
  val rulerState = uiState.rulerState
  val isZoomWindowVisible = uiState.isZoomWindowVisible
  val zoomTargetBoxOffset = uiState.zoomTargetBoxOffset
  val laserMode = uiState.laserMode
  val laserColor = uiState.laserColor
  val pendingCircleLoop = uiState.pendingCircleLoop
  val circleLassoConversionToken = uiState.circleLassoConversionToken
  val convertingCircleLassoColor = uiState.convertingCircleLassoColor
  val convertingCircleLassoWidth = uiState.convertingCircleLassoWidth
  val isEyedropperActive = uiState.isEyedropperActive
  val privacyScreenShieldEnabled = uiState.privacyScreenShieldEnabled
  val watermarkConfidentialExports = uiState.watermarkConfidentialExports
  val customTemplates = uiState.customTemplates

  val activeDocument =
    notebooks.find { it.id == activeTabId } ?: notebooks.firstOrNull()
  val canUndo = uiState.canUndo() || (activeDocument?.currentPage?.strokes?.isNotEmpty() == true)
  val canRedo = uiState.canRedo()

  val onNavigateBackToDocuments: () -> Unit = viewModel::switchToDocuments
  val onOpenSettings: () -> Unit = viewModel::openSettingsScreen
  val onSelectTab: (String) -> Unit = viewModel::openDocument
  val onCloseTab: (String) -> Unit = viewModel::closeTab
  val onAddNewNotebookTab: () -> Unit = { viewModel.togglePopover(ActivePopover.NEW_NOTE_DIALOG) }
  val onSetVaultPin: (String) -> Unit = viewModel::lockCurrentNotebookWithPin
  val onRemoveVaultLock: () -> Unit = viewModel::removeCurrentNotebookLock
  val onTogglePrivacyScreenShield: (Boolean) -> Unit = viewModel::togglePrivacyScreenShield
  val onToggleWatermark: (Boolean) -> Unit = viewModel::toggleWatermarkConfidentialExports
  val onCyclePaperColorTheme: () -> Unit = viewModel::cyclePaperColorTheme
  val onToggleReadOnlyMode: () -> Unit = viewModel::toggleReadOnlyMode
  val onSelectPrimaryTool: (PrimaryTool) -> Unit = viewModel::selectPrimaryTool
  val onSelectWritingSubTool: (WritingSubTool) -> Unit = viewModel::selectWritingSubTool
  val onSelectToolbarDockPosition: (ToolbarDockPosition) -> Unit = viewModel::setToolbarDockPosition
  val onCycleToolbarDockPosition: () -> Unit = viewModel::cycleToolbarDockPosition
  val onMoveSubToolbar: (Offset) -> Unit = viewModel::moveSubToolbar
  val onResetSubToolbarPosition: () -> Unit = viewModel::resetSubToolbarPosition
  val onToggleToolbarToolEnabled: (PrimaryTool) -> Unit = viewModel::toggleToolbarToolEnabled
  val onToggleWritingSubToolEnabled: (WritingSubTool) -> Unit = viewModel::toggleWritingSubToolEnabled
  val onResetToolbarDefaults: () -> Unit = viewModel::resetToolbarDefaults
  val onResetWritingToolsDefaults: () -> Unit = viewModel::resetWritingToolsDefaults
  val onSelectPenStyle: (PenStyle) -> Unit = viewModel::selectPenStyle
  val onSelectStrokePattern: (StrokePattern) -> Unit = viewModel::selectStrokePattern
  val onSelectThicknessIndex: (Int) -> Unit = viewModel::selectThicknessIndex
  val onSelectQuickSwatch: (Int) -> Unit = viewModel::selectQuickSwatch
  val onSelectPaletteColor: (Color) -> Unit = viewModel::selectPaletteColor
  val onAddNewColorToToolbar: (Color) -> Unit = viewModel::addNewColorToToolbar
  val onAddCustomColorToPalette: (Color) -> Unit = viewModel::addCustomColorToPalette
  val onRemoveCurrentToolbarSlot: () -> Unit = viewModel::removeCurrentToolbarSwatchSlot
  val onToggleEyedropper: () -> Unit = viewModel::toggleEyedropper
  val onSampleColorAtPoint: (Offset) -> Unit = viewModel::sampleColorAt
  val onStabilizationChange: (Float) -> Unit = viewModel::updateStrokeStabilization
  val onToggleDrawAndHold: (Boolean) -> Unit = viewModel::toggleDrawAndHold
  val onToggleHoldToErase: (Boolean) -> Unit = viewModel::toggleHoldToErase
  val onToggleScribbleToErase: (Boolean) -> Unit = viewModel::toggleScribbleToErase
  val onToggleCircleToLasso: (Boolean) -> Unit = viewModel::toggleCircleToLasso
  val onToggleSPenBarrelButtonMode: () -> Unit = viewModel::toggleSPenBarrelButtonMode
  val onToggleOnlyDrawWithStylus: (Boolean) -> Unit = viewModel::toggleOnlyDrawWithStylus
  val onTogglePageScrollDirection: () -> Unit = viewModel::togglePageScrollDirection
  val onToggleHighlighterStraightLine: () -> Unit = viewModel::toggleHighlighterStraightLine
  val onSelectEraserMode: (EraserMode) -> Unit = viewModel::selectEraserMode
  val onEraserRadiusChange: (Float) -> Unit = viewModel::updateEraserRadius
  val onToggleEraserAutoDeselect: (Boolean) -> Unit = viewModel::toggleEraserAutoDeselect
  val onClearPageStrokes: () -> Unit = viewModel::clearCurrentPageStrokes
  val onInsertQuickShape: (ShapeKind) -> Unit = viewModel::insertQuickShape
  val onToggleFillShapes: (Boolean) -> Unit = viewModel::toggleFillShapes
  val onMoveShapeControlHandle: (String, Int, Offset) -> Unit = viewModel::moveShapeControlHandle
  val onUpdateLassoFilters: (LassoFilterSettings) -> Unit = viewModel::updateLassoFilters
  val onToggleLassoSelection: () -> Unit = viewModel::dismissLassoSelection
  val onClearLassoSelection: () -> Unit = viewModel::clearLassoSelection
  val onMoveLassoSelection: (Offset) -> Unit = viewModel::moveLassoSelection
  val onTransformLassoSelection: (Float, Float) -> Unit = viewModel::transformLassoSelection
  val onCommitLassoChanges: () -> Unit = viewModel::commitLassoChanges
  val onRecordUndoBeforeGesture: () -> Unit = { viewModel.recordUndoSnapshotBeforeGesture("Gesture") }
  val onUpdatePageCanvasSize: (Float, Float) -> Unit = viewModel::updateCurrentPageCanvasSize
  val onSetCanvasDensity: (Float) -> Unit = viewModel::setCanvasDensity
  val onScaleLassoSelectionStep: (Boolean) -> Unit = viewModel::scaleLassoSelectionStep
  val onCutLassoSelection: () -> Unit = viewModel::cutLassoSelection
  val onCopyLassoSelection: () -> Unit = viewModel::copyLassoSelection
  val onPasteLassoSelection: () -> Unit = { viewModel.pasteLassoClipboard(null) }
  val onToggleLassoColorMenu: () -> Unit = viewModel::toggleLassoColorMenu
  val onRecolorLassoSelection: (Color) -> Unit = viewModel::recolorLassoSelection
  val onDeleteLassoSelection: () -> Unit = viewModel::deleteLassoSelection
  val onDuplicateSelection: () -> Unit = viewModel::duplicateSelectedStrokes
  val onScaleSelection: () -> Unit = viewModel::scaleSelectedStrokes
  val onBringToFront: () -> Unit = viewModel::bringSelectionToFront
  val onSendToBack: () -> Unit = viewModel::sendSelectionToBack
  val onMoveElementStamp: (String, Offset) -> Unit = viewModel::moveElementStamp
  val onUpdateTextBox: (String, String) -> Unit = viewModel::updateTextBox
  val onMoveTextBox: (String, Offset) -> Unit = viewModel::moveTextBox
  val onMoveInsertedImage: (String, Offset) -> Unit = viewModel::moveCanvasImage
  val onResizeInsertedImage: (String, Float) -> Unit = viewModel::resizeCanvasImage
  val onDeleteInsertedImage: (String) -> Unit = viewModel::deleteCanvasImage
  val onToggleStickyNoteExpanded: (String) -> Unit = viewModel::toggleStickyNoteExpanded
  val onUpdateStickyNoteText: (String, String) -> Unit = viewModel::updateStickyNoteText
  val onDeleteStickyNote: (String) -> Unit = viewModel::deleteStickyNote
  val onToggleRuler: () -> Unit = viewModel::toggleRuler
  val onMoveRuler: (Offset) -> Unit = viewModel::moveRuler
  val onRotateRuler: (Float) -> Unit = viewModel::rotateRuler
  val onToggleZoomWindow: () -> Unit = viewModel::toggleZoomWindow
  val onAdvanceZoomBox: (Float, Float) -> Unit = viewModel::advanceZoomTargetBox
  val onSelectLaserMode: (LaserMode) -> Unit = viewModel::selectLaserMode
  val onSelectLaserColor: (Color) -> Unit = viewModel::selectLaserColor
  val onAddStroke: (List<Offset>, List<Float>, Boolean, Float) -> Unit = viewModel::addStroke
  val onCommitPendingCircleLoop: () -> Unit = viewModel::commitPendingCircleLoopAsHandwriting
  val onActivatePendingCircleLoop: (Offset) -> Boolean = viewModel::activatePendingCircleLoopToLasso
  val onEraseAtPoint: (Offset, Boolean, Boolean, Float) -> Unit = { pt, recordUndo, forceTemp, scale ->
    viewModel.eraseAtPoints(
      points = listOf(pt),
      forceTemporaryEraser = forceTemp,
      zoomScale = scale,
      recordUndo = recordUndo,
    )
  }
  val onFinishEraserGesture: () -> Unit = viewModel::finishEraserGesture
  val onUndo: () -> Unit = viewModel::undo
  val onRedo: () -> Unit = viewModel::redo
  val onTogglePageBookmark: () -> Unit = viewModel::toggleCurrentPageBookmark
  val onTogglePageSidebar: () -> Unit = viewModel::togglePageSidebar
  val onSelectPageIndex: (Int) -> Unit = viewModel::selectPageIndex
  val onAddNewPage: () -> Unit = viewModel::addNewPageToCurrentDocument
  val onAddPageWithTemplate: (PaperTemplate, PaperColorTheme, CustomPageTemplate?, Int) -> Unit = { tmpl, theme, customTmpl, posMode ->
    viewModel.addPageWithTemplate(
      template = tmpl,
      colorTheme = theme,
      customTemplate = customTmpl,
      insertBeforeCurrent = posMode == 1,
    )
  }
  val onChangeCurrentPageTemplate: (PaperTemplate, PaperColorTheme, CustomPageTemplate?) -> Unit = { tmpl, theme, customTmpl ->
    viewModel.changeCurrentPageTemplate(
      template = tmpl,
      colorTheme = theme,
      customTemplate = customTmpl,
    )
  }
  val onDuplicateCurrentPage: () -> Unit = viewModel::duplicateCurrentPage
  val onDeleteCurrentPage: () -> Unit = viewModel::deleteCurrentPage
  val onCyclePaperTemplate: () -> Unit = viewModel::cyclePaperTemplate
  val onDismissPopover: () -> Unit = viewModel::dismissPopover
  val onTogglePopover: (ActivePopover) -> Unit = viewModel::togglePopover

  val proColors = LocalProNotesColors.current
  val isWhiteboard = activeDocument?.documentType == DocumentType.WHITEBOARD
  val currentPage = activeDocument?.currentPage ?: NotePage(pageNumber = 1)
  val density = LocalDensity.current.density
  LaunchedEffect(density) { onSetCanvasDensity(density) }

  val inProgressPoints = remember { mutableStateListOf<Offset>() }
  val inProgressPressures = remember { mutableStateListOf<Float>() }
  var isDrawingSnappedHold by remember { mutableStateOf(false) }
  var eraserCursorPosition by remember { mutableStateOf<Offset?>(null) }
  var activeLaserPoint by remember { mutableStateOf<Offset?>(null) }
  val laserTrailPoints = remember { mutableStateListOf<LaserTrailPoint>() }
  var laserStrokeCounter by remember { mutableIntStateOf(0) }
  var laserFadeTick by remember { mutableIntStateOf(0) }
  val laserTrailDurationMs = 520L

  val circleLassoConversionProgress = remember { Animatable(1f) }

  LaunchedEffect(circleLassoConversionToken, showLassoSelection, primaryTool) {
    if (circleLassoConversionToken != 0L && showLassoSelection && primaryTool == PrimaryTool.LASSO) {
      circleLassoConversionProgress.snapTo(0f)
      circleLassoConversionProgress.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
      )
    } else {
      circleLassoConversionProgress.snapTo(1f)
    }
  }

  // Clear preview stroke, eraser cursor, laser state, and lasso animation on document switch, page change, or tool change
  LaunchedEffect(activeTabId, activeDocument?.currentPageIndex, primaryTool, activeWritingSubTool) {
    inProgressPoints.clear()
    inProgressPressures.clear()
    isDrawingSnappedHold = false
    eraserCursorPosition = null
    activeLaserPoint = null
    laserTrailPoints.clear()
  }

  DisposableEffect(Unit) {
    onDispose {
      activeLaserPoint = null
      laserTrailPoints.clear()
      eraserCursorPosition = null
      inProgressPoints.clear()
      inProgressPressures.clear()
    }
  }

  // Interactive Multi-Touch Pinch-to-Zoom & 2-Finger Pan State
  var canvasScale by remember(activeTabId) { mutableFloatStateOf(1f) }
  var canvasPanOffset by remember(activeTabId) { mutableStateOf(Offset.Zero) }

  // Hardware stylus side button & tool-type tracker (non-recomposing on move frames)
  val hardwareStylusState = remember {
    object {
      var buttonDown: Boolean = false
      var isStylusOrEraser: Boolean = false
      var isEraserTail: Boolean = false
    }
  }

  // Smoothly fade and prune timestamped laser trail points in real time and after pointer-up
  LaunchedEffect(activeLaserPoint != null, laserTrailPoints.isNotEmpty()) {
    while (activeLaserPoint != null || laserTrailPoints.isNotEmpty()) {
      delay(16L)
      val nowMs = System.currentTimeMillis()
      laserTrailPoints.removeAll { pt -> nowMs - pt.timestampMs > laserTrailDurationMs }
      laserFadeTick++
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize().background(proColors.canvasViewportBg)) {
    val isCompact = maxWidth < 680.dp

    Column(modifier = Modifier.fillMaxSize()) {
      // 1. DOCKED TOP DOCUMENT TAB BAR
      TopDocumentTabBar(
        screenMode = com.example.model.ScreenMode.EDITOR,
        openTabs = notebooks,
        activeTabId = activeTabId,
        isReadOnlyMode = isReadOnlyMode,
        onHomeClick = onNavigateBackToDocuments,
        onTabClick = onSelectTab,
        onTabClose = onCloseTab,
        onNewTabClick = onAddNewNotebookTab,
        onToggleReadOnly = onToggleReadOnlyMode,
      )

      // 2. DOCKED MAIN WORKSPACE TOOLBAR (Always docked at top — never overlaps zoomed PDFs)
      if (!isReadOnlyMode) {
        DockedMainToolbar(
          primaryTool = primaryTool,
          enabledToolbarTools = enabledToolbarTools,
          isBookmarked = currentPage.isBookmarked,
          isVaultLocked = activeDocument?.isLockedVault == true,
          isWhiteboard = isWhiteboard,
          onlyDrawWithStylus = onlyDrawWithStylus,
          pageScrollDirection = pageScrollDirection,
          canUndo = canUndo,
          canRedo = canRedo,
          onUndo = onUndo,
          onRedo = onRedo,
          onSelectPrimaryTool = onSelectPrimaryTool,
          onTogglePageSidebar = onTogglePageSidebar,
          onToggleBookmark = onTogglePageBookmark,
          onToggleRuler = onToggleRuler,
          onToggleZoomWindow = onToggleZoomWindow,
          onOpenTimeKeeper = { onTogglePopover(ActivePopover.TIME_KEEPER) },
          onOpenToolbarCustomize = { onTogglePopover(ActivePopover.TOOLBAR_CUSTOMIZE) },
          onOpenExportShare = { onTogglePopover(ActivePopover.EXPORT_SHARE) },
          onOpenSecurityVault = { onTogglePopover(ActivePopover.SECURITY_VAULT) },
          onOpenAddPagePopover = { onTogglePopover(ActivePopover.ADD_PAGE_DIALOG) },
          onTogglePageScrollDirection = onTogglePageScrollDirection,
          onImportPdfFromDevice = onImportPdfFromDevice,
          onCyclePaperTemplate = onCyclePaperTemplate,
          onCyclePaperColorTheme = onCyclePaperColorTheme,
          onClearPageStrokes = onClearPageStrokes,
          onToggleOnlyDrawWithStylus = { onToggleOnlyDrawWithStylus(!onlyDrawWithStylus) },
          onOpenSettings = onOpenSettings,
        )
      }

      // 3. DOCKED CONTEXTUAL SECONDARY TOOL STRIP (Pen styles, thicknesses, color swatches, or Laser controls)
      if (!isReadOnlyMode && primaryTool == PrimaryTool.LASER) {
        DockedLaserStrip(
          laserMode = laserMode,
          laserColor = laserColor,
          onSelectLaserMode = onSelectLaserMode,
          onSelectLaserColor = onSelectLaserColor,
        )
      } else if (!isReadOnlyMode && primaryTool != PrimaryTool.LASSO) {
        DockedContextualWritingStrip(
          activeWritingSubTool = activeWritingSubTool,
          enabledWritingSubTools = enabledWritingSubTools,
          strokePattern = strokePattern,
          selectedThicknessIndex = selectedThicknessIndex,
          quickSwatches = quickSwatches,
          selectedSwatchIndex = selectedSwatchIndex,
          isPenPopoverOpen =
            activePopover == ActivePopover.PEN_SETTINGS ||
              activePopover == ActivePopover.HIGHLIGHTER_SETTINGS,
          isColorPopoverOpen = activePopover == ActivePopover.PEN_COLOR,
          isEyedropperActive = isEyedropperActive,
          onSelectWritingSubTool = onSelectWritingSubTool,
          onSelectThicknessIndex = onSelectThicknessIndex,
          onSelectQuickSwatch = onSelectQuickSwatch,
          onOpenColorPicker = { onTogglePopover(ActivePopover.PEN_COLOR) },
        )
      }

      // 5. MAIN WORKSPACE AREA (Sidebar + Clipped Canvas Viewport)
      Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
        if (isPageSidebarOpen && !isWhiteboard) {
          PageThumbnailsSidebar(
            document = activeDocument ?: NotebookDocument(" fallback", "Notes", "Now"),
            onSelectPage = onSelectPageIndex,
            onAddPage = { onTogglePopover(ActivePopover.ADD_PAGE_DIALOG) },
            onDuplicatePage = onDuplicateCurrentPage,
            onDeletePage = onDeleteCurrentPage,
            onCloseSidebar = onTogglePageSidebar,
          )
        }

        // CLIPPED CANVAS VIEWPORT CONTAINER:
        // Strictly clips any zoomed PDF/page/whiteboard content so it NEVER overlaps the docked toolbars above!
        Box(
          modifier =
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .clipToBounds()
              .background(proColors.canvasViewportBg)
              .testTag("clipped_canvas_viewport"),
        ) {
          val pages = activeDocument?.pages ?: listOf(currentPage)
          val currentPageIdx =
            (activeDocument?.currentPageIndex ?: 0).coerceIn(0, (pages.size - 1).coerceAtLeast(0))
          val pagerScope = rememberCoroutineScope()
          val pagerState =
            rememberPagerState(
              initialPage = currentPageIdx,
              pageCount = { pages.size.coerceAtLeast(1) },
            )

          LaunchedEffect(currentPageIdx, pages.size) {
            if (!isWhiteboard && pagerState.currentPage != currentPageIdx && currentPageIdx in pages.indices) {
              pagerState.animateScrollToPage(currentPageIdx)
            }
          }

          LaunchedEffect(pagerState.settledPage) {
            if (!isWhiteboard && pagerState.settledPage != currentPageIdx && pagerState.settledPage in pages.indices) {
              onSelectPageIndex(pagerState.settledPage)
            }
          }

          val navigateByPageSwipe: (Int) -> Unit = { delta ->
            val targetIdx = currentPageIdx + delta
            if (targetIdx in pages.indices) {
              onSelectPageIndex(targetIdx)
              pagerScope.launch { pagerState.animateScrollToPage(targetIdx) }
            } else if (delta > 0 && pullToAddPage && !isWhiteboard) {
              onAddNewPage()
            }
          }

          // Reusable renderer for either a Notebook/PDF Page or an Unbounded Whiteboard Canvas
          val renderSingleNotePage: @Composable (Int) -> Unit = { pageIndex ->
            val pageData = pages.getOrElse(pageIndex) { currentPage }
            val isCurrentActivePage = pageIndex == currentPageIdx
            val effectiveTemplate =
              pageData.pageTemplate ?: activeDocument?.paperTemplate ?: PaperTemplate.RULED
            val effectivePaperTheme =
              pageData.pageColorTheme ?: activeDocument?.paperColorTheme ?: PaperColorTheme.PURE_WHITE
            val pagePaperBg =
              if (pageData.pdfPageBitmap != null) Color.White else effectivePaperTheme.paperBgColor

            // Determine aspect ratio from PageSizePreset & PageOrientation for Notebook pages
            val pageAspectRatio =
              if (activeDocument != null && !isWhiteboard) {
                val baseRatio = activeDocument.pageSizePreset.aspectRatioPortrait
                if (activeDocument.pageOrientation == PageOrientation.LANDSCAPE) {
                  1f / baseRatio
                } else {
                  baseRatio
                }
              } else {
                null
              }

            Box(
              modifier =
                Modifier
                  .fillMaxSize()
                  .padding(
                    horizontal = if (isWhiteboard) 0.dp else if (isCompact) 8.dp else 20.dp,
                    vertical = if (isWhiteboard) 0.dp else 10.dp,
                  ),
              contentAlignment = Alignment.Center,
            ) {
              val pageSurfaceModifier =
                if (isWhiteboard) {
                  Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                      scaleX = canvasScale
                      scaleY = canvasScale
                      translationX = canvasPanOffset.x
                      translationY = canvasPanOffset.y
                    }
                    .background(pagePaperBg)
                } else {
                  Modifier
                    .then(
                      if (pageAspectRatio != null && !isCompact) {
                        Modifier.fillMaxHeight().aspectRatio(pageAspectRatio, matchHeightConstraintsFirst = true)
                      } else {
                        Modifier.fillMaxSize()
                      }
                    )
                    .graphicsLayer {
                      scaleX = canvasScale
                      scaleY = canvasScale
                      translationX = canvasPanOffset.x
                      translationY = canvasPanOffset.y
                    }
                    .shadow(8.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(pagePaperBg)
                }

              Box(modifier = pageSurfaceModifier) {
                val lassoShape =
                  remember(lassoLoopPoints) {
                    if (lassoLoopPoints.size >= 3) LassoShape(lassoLoopPoints) else null
                  }
                val lassoBounds =
                  remember(lassoLoopPoints) {
                    if (lassoLoopPoints.size >= 3) {
                      floatArrayOf(
                        lassoLoopPoints.minOf { it.x },
                        lassoLoopPoints.minOf { it.y },
                        lassoLoopPoints.maxOf { it.x },
                        lassoLoopPoints.maxOf { it.y },
                      )
                    } else {
                      null
                    }
                  }
                val lassoActive = primaryTool == PrimaryTool.LASSO && showLassoSelection
                val shapeState = rememberUpdatedState(lassoShape)
                val boundsState = rememberUpdatedState(lassoBounds)
                val loopState = rememberUpdatedState(lassoLoopPoints)

                // 1) Custom Template Background Bitmap (if template == CUSTOM)
                pageData.customTemplateBitmap?.let { tmplBmp ->
                  Image(
                    bitmap = tmplBmp.asImageBitmap(),
                    contentDescription = "Custom Template Background",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                  )
                }

                // 2) Imported PDF Page Background Bitmap
                pageData.pdfPageBitmap?.let { pdfBitmap ->
                  Image(
                    bitmap = pdfBitmap.asImageBitmap(),
                    contentDescription = "Imported PDF Page ${pageData.pageNumber}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                  )
                }

                // 3) Inserted Gallery Images on Page
                pageData.insertedImages.forEach { img ->
                  val isSelectedByLasso = lassoSelectedImageIds.contains(img.id)
                  Box(
                    modifier =
                      Modifier
                        .offset {
                          IntOffset(img.position.x.roundToInt(), img.position.y.roundToInt())
                        }
                        .width(img.widthDp.dp)
                        .height(img.heightDp.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                          width = if (isSelectedByLasso) 2.5.dp else 1.5.dp,
                          color =
                            if (isSelectedByLasso) SubToolbarActiveCyan
                            else SwitchBlue.copy(alpha = 0.65f),
                          shape = RoundedCornerShape(8.dp),
                        )
                        .then(
                          if (lassoActive) Modifier
                          else
                            Modifier.pointerInput(img.id) {
                              detectDragGestures(
                                onDragStart = { onRecordUndoBeforeGesture() },
                              ) { change, dragAmount ->
                                change.consume()
                                onMoveInsertedImage(img.id, dragAmount)
                              }
                            }
                        ),
                  ) {
                    Image(
                      bitmap = img.bitmap.asImageBitmap(),
                      contentDescription = "Inserted Note Image",
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize(),
                    )
                    Row(
                      modifier =
                        Modifier
                          .align(Alignment.TopEnd)
                          .padding(4.dp)
                          .clip(RoundedCornerShape(6.dp))
                          .background(Color.Black.copy(alpha = 0.65f))
                          .padding(horizontal = 4.dp, vertical = 2.dp),
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Shrink Image",
                        tint = Color.White,
                        modifier =
                          Modifier
                            .size(13.dp)
                            .clickable { onResizeInsertedImage(img.id, 0.85f) },
                      )
                      Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Enlarge Image",
                        tint = Color.White,
                        modifier =
                          Modifier
                            .size(13.dp)
                            .clickable { onResizeInsertedImage(img.id, 1.18f) },
                      )
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Image",
                        tint = DangerRed,
                        modifier =
                          Modifier
                            .size(13.dp)
                            .clickable { onDeleteInsertedImage(img.id) },
                      )
                    }
                  }
                }

                // 4) Interactive Ink, Shape, Lasso, Eyedropper, S Pen & Multi-Touch Zoom Canvas
                Canvas(
                  modifier =
                    Modifier
                      .fillMaxSize()
                      .testTag(if (isCurrentActivePage) "note_drawing_canvas" else "note_page_canvas_$pageIndex")
                      .pointerInteropFilter { motionEvent ->
                        val action = motionEvent.actionMasked
                        val buttonState = motionEvent.buttonState
                        val hasStylusSideButton =
                          (buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY) != 0 ||
                            (buttonState and MotionEvent.BUTTON_STYLUS_SECONDARY) != 0
                        when (action) {
                          MotionEvent.ACTION_DOWN,
                          MotionEvent.ACTION_POINTER_DOWN,
                          MotionEvent.ACTION_MOVE,
                          MotionEvent.ACTION_HOVER_MOVE,
                          MotionEvent.ACTION_BUTTON_PRESS -> {
                            hardwareStylusState.buttonDown = hasStylusSideButton
                            var foundStylus = false
                            var foundEraser = false
                            for (i in 0 until motionEvent.pointerCount) {
                              when (motionEvent.getToolType(i)) {
                                MotionEvent.TOOL_TYPE_STYLUS -> foundStylus = true
                                MotionEvent.TOOL_TYPE_ERASER -> {
                                  foundStylus = true
                                  foundEraser = true
                                }
                              }
                            }
                            hardwareStylusState.isStylusOrEraser = foundStylus
                            hardwareStylusState.isEraserTail = foundEraser
                          }
                          MotionEvent.ACTION_BUTTON_RELEASE -> {
                            hardwareStylusState.buttonDown = hasStylusSideButton
                          }
                          MotionEvent.ACTION_UP,
                          MotionEvent.ACTION_CANCEL,
                          MotionEvent.ACTION_HOVER_EXIT -> {
                            hardwareStylusState.buttonDown = false
                            hardwareStylusState.isStylusOrEraser = false
                            hardwareStylusState.isEraserTail = false
                          }
                        }
                        false
                      }
                      .pointerInput(
                        isCurrentActivePage,
                        isReadOnlyMode,
                        isWhiteboard,
                        primaryTool,
                        activeWritingSubTool,
                        penStyle,
                        selectedColor,
                        selectedThicknessIndex,
                        eraserMode,
                        eraserRadius,
                        laserMode,
                        highlighterStraightLine,
                        drawAndHold,
                        holdToErase,
                        circleToLasso,
                        pendingCircleLoop?.id,
                        isSPenBarrelButtonActive,
                        onlyDrawWithStylus,
                        palmRejectionSensitivity,
                        leftHandedPosture,
                        rulerState.isVisible,
                        rulerState.angleDegrees,
                        showLassoSelection,
                        isEyedropperActive,
                        currentPageIdx,
                        pages.size,
                      ) {
                        if (!isCurrentActivePage) return@pointerInput

                        fun readSafePressure(
                          change: androidx.compose.ui.input.pointer.PointerInputChange
                        ): Float {
                          val p = change.pressure
                          return if (p.isFinite() && p > 0f) p.coerceIn(0.1f, 1.5f) else 0.5f
                        }

                        awaitEachGesture {
                          var activeDown = awaitFirstDown(requireUnconsumed = false)
                          var isStylusDown =
                            activeDown.type == PointerType.Stylus ||
                              activeDown.type == PointerType.Eraser ||
                              hardwareStylusState.isStylusOrEraser
                          var isHardwareEraserTail =
                            activeDown.type == PointerType.Eraser ||
                              hardwareStylusState.isEraserTail

                          // Edge Palm-Guard Zone when drawing with Touch
                          val canvasWidthPx = size.width.toFloat()
                          val edgeGuardWidth = (palmRejectionSensitivity / 100f) * 58f
                          val inPalmZone =
                            !isStylusDown &&
                              !onlyDrawWithStylus &&
                              edgeGuardWidth > 12f &&
                              if (leftHandedPosture) {
                                activeDown.position.x < edgeGuardWidth
                              } else {
                                activeDown.position.x > (canvasWidthPx - edgeGuardWidth)
                              }

                          // When Stylus Only is enabled, or in Palm Guard / Read-Only mode,
                          // prevent touch input from drawing while still allowing 2-finger zoom/pan
                          // or promoting an arriving stylus pointer immediately.
                          if (inPalmZone || isReadOnlyMode || (onlyDrawWithStylus && !isStylusDown)) {
                            var promotedStylusChange: androidx.compose.ui.input.pointer.PointerInputChange? = null
                            do {
                              val moveEvt = awaitPointerEvent()
                              val stylusArrival =
                                if (!isReadOnlyMode) {
                                  moveEvt.changes.firstOrNull {
                                    it.pressed &&
                                      (it.type == PointerType.Stylus || it.type == PointerType.Eraser)
                                  }
                                } else null

                              if (stylusArrival != null) {
                                promotedStylusChange = stylusArrival
                                break
                              }

                              if (!inPalmZone) {
                                if (moveEvt.changes.size >= 2) {
                                  val z = moveEvt.calculateZoom()
                                  val p = moveEvt.calculatePan()
                                  val minScale = if (isWhiteboard) 0.35f else 0.65f
                                  if (z != 1f || p != Offset.Zero) {
                                    canvasScale = (canvasScale * z).coerceIn(minScale, 4.5f)
                                    if (isWhiteboard || canvasScale > 1.02f) {
                                      canvasPanOffset += p
                                    } else {
                                      canvasPanOffset = Offset.Zero
                                    }
                                  }
                                  moveEvt.changes.forEach { it.consume() }
                                } else if (isWhiteboard || canvasScale > 1.04f) {
                                  val ch = moveEvt.changes.firstOrNull() ?: break
                                  val panDelta = ch.position - ch.previousPosition
                                  canvasPanOffset += panDelta
                                  ch.consume()
                                }
                              }
                            } while (moveEvt.changes.any { it.pressed })

                            if (promotedStylusChange != null) {
                              activeDown = promotedStylusChange
                              isStylusDown = true
                              isHardwareEraserTail =
                                promotedStylusChange.type == PointerType.Eraser ||
                                  hardwareStylusState.isEraserTail
                            } else {
                              return@awaitEachGesture
                            }
                          }

                          // Consume down IMMEDIATELY so HorizontalPager/VerticalPager never steals a stroke
                          activeDown.consume()
                          val activePointerId = activeDown.id

                          // Eyedropper color sampling
                          if (isEyedropperActive) {
                            onSampleColorAtPoint(activeDown.position)
                            return@awaitEachGesture
                          }

                          // Helper to resolve the active stylus/primary pointer from an event
                          fun resolveActiveChange(
                            event: androidx.compose.ui.input.pointer.PointerEvent
                          ): androidx.compose.ui.input.pointer.PointerInputChange? {
                            return event.changes.firstOrNull { it.id == activePointerId }
                              ?: if (isStylusDown) {
                                event.changes.firstOrNull {
                                  it.type == PointerType.Stylus || it.type == PointerType.Eraser
                                }
                              } else {
                                event.changes.firstOrNull()
                              }
                          }

                          // Helper to handle 2-finger pinch-zoom when NOT using stylus
                          suspend fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.runTwoFingerZoomLoop() {
                            do {
                              val zEvt = awaitPointerEvent()
                              val zoomChange = zEvt.calculateZoom()
                              val panChange = zEvt.calculatePan()
                              val minScale = if (isWhiteboard) 0.35f else 0.65f
                              if (zoomChange != 1f || panChange != Offset.Zero) {
                                canvasScale = (canvasScale * zoomChange).coerceIn(minScale, 4.5f)
                                if (isWhiteboard || canvasScale > 1.02f) {
                                  canvasPanOffset += panChange
                                } else {
                                  canvasPanOffset = Offset.Zero
                                }
                              }
                              zEvt.changes.forEach { it.consume() }
                            } while (zEvt.changes.any { it.pressed })
                          }

                          // Step 2 of Circle-to-Lasso:
                          // If a pending closed loop exists and the user presses near its start or end point,
                          // holding for ~300ms converts the loop into an active lasso selection.
                          // Pressing outside the endpoint area or dragging away commits the loop as handwriting.
                          val activePendingLoop = pendingCircleLoop
                          if (activePendingLoop != null) {
                            if (
                              circleToLasso &&
                                isPointNearPendingLoopEndpoint(
                                  point = activeDown.position,
                                  loop = activePendingLoop,
                                  zoomScale = canvasScale,
                                )
                            ) {
                              val holdStartMs = System.currentTimeMillis()
                              val holdOrigin = activeDown.position
                              val maxHoldDrift = 22f / canvasScale.coerceIn(0.25f, 5f)
                              var convertedToLasso = false
                              var holdWatcherJob: kotlinx.coroutines.Job? = null
                              holdWatcherJob =
                                pagerScope.launch {
                                  delay(300L)
                                  if (onActivatePendingCircleLoop(holdOrigin)) {
                                    convertedToLasso = true
                                    inProgressPoints.clear()
                                    inProgressPressures.clear()
                                  }
                                }
                              try {
                                while (true) {
                                  val evt = awaitPointerEvent()
                                  val ch = resolveActiveChange(evt) ?: break
                                  evt.changes.forEach { it.consume() }
                                  if (!ch.pressed) {
                                    if (!convertedToLasso) {
                                      holdWatcherJob.cancel()
                                      onCommitPendingCircleLoop()
                                    }
                                    return@awaitEachGesture
                                  }
                                  if (convertedToLasso) {
                                    // Keep consuming until stylus lifts after conversion
                                    continue
                                  }
                                  if ((ch.position - holdOrigin).getDistance() > maxHoldDrift) {
                                    // User started dragging a new stroke instead of holding
                                    holdWatcherJob.cancel()
                                    onCommitPendingCircleLoop()
                                    break
                                  }
                                  if (System.currentTimeMillis() - holdStartMs >= 300L && !convertedToLasso) {
                                    if (onActivatePendingCircleLoop(holdOrigin)) {
                                      convertedToLasso = true
                                      inProgressPoints.clear()
                                      inProgressPressures.clear()
                                    }
                                  }
                                }
                              } finally {
                                holdWatcherJob?.cancel()
                              }
                              if (convertedToLasso) {
                                return@awaitEachGesture
                              }
                            } else {
                              onCommitPendingCircleLoop()
                            }
                          }

                          // Temporary erasing when an Android stylus side button (PRIMARY or SECONDARY)
                          // or hardware eraser tail is held, automatically restoring the previous tool on release.
                          val stylusButtonOrTailErase =
                            isHardwareEraserTail ||
                              isSPenBarrelButtonActive ||
                              hardwareStylusState.buttonDown

                          if (
                            stylusButtonOrTailErase &&
                              primaryTool != PrimaryTool.ERASER &&
                              primaryTool != PrimaryTool.LASER
                          ) {
                            var recordedErase = false
                            try {
                              eraserCursorPosition = activeDown.position
                              onEraseAtPoint(activeDown.position, true, true, canvasScale)
                              recordedErase = true
                              while (true) {
                                val event = awaitPointerEvent()
                                val change = resolveActiveChange(event) ?: break
                                event.changes.forEach { it.consume() }
                                if (!change.pressed) break
                                val stillErasing =
                                  isHardwareEraserTail ||
                                    isSPenBarrelButtonActive ||
                                    hardwareStylusState.buttonDown ||
                                    hardwareStylusState.isEraserTail
                                if (!stillErasing) {
                                  // Button released while pointer down -> restore writing tool immediately
                                  eraserCursorPosition = null
                                  onFinishEraserGesture()
                                  break
                                }
                                eraserCursorPosition = change.position
                                onEraseAtPoint(change.position, !recordedErase, true, canvasScale)
                                recordedErase = true
                              }
                            } finally {
                              eraserCursorPosition = null
                              onFinishEraserGesture()
                            }
                            return@awaitEachGesture
                          }

                          when (primaryTool) {
                            PrimaryTool.ERASER -> {
                              var didRecordEraseUndo = false
                              try {
                                eraserCursorPosition = activeDown.position
                                onEraseAtPoint(activeDown.position, true, false, canvasScale)
                                didRecordEraseUndo = true
                                while (true) {
                                  val event = awaitPointerEvent()
                                  if (!isStylusDown && event.changes.count { it.pressed } >= 2) {
                                    eraserCursorPosition = null
                                    runTwoFingerZoomLoop()
                                    break
                                  }
                                  val change = resolveActiveChange(event) ?: break
                                  event.changes.forEach { it.consume() }
                                  if (!change.pressed) break
                                  eraserCursorPosition = change.position
                                  onEraseAtPoint(change.position, !didRecordEraseUndo, false, canvasScale)
                                  didRecordEraseUndo = true
                                }
                              } finally {
                                eraserCursorPosition = null
                                onFinishEraserGesture()
                              }
                            }

                            PrimaryTool.LASER -> {
                              val minStep = 1.6f / canvasScale.coerceIn(0.25f, 5f)
                              val currentStrokeId = ++laserStrokeCounter
                              inProgressPoints.clear()
                              inProgressPressures.clear()
                              val downTimeMs = System.currentTimeMillis()
                              activeLaserPoint = activeDown.position
                              var lastTrailPosition = activeDown.position
                              if (laserMode == LaserMode.FADING_TRAIL) {
                                laserTrailPoints.add(
                                  LaserTrailPoint(
                                    position = activeDown.position,
                                    timestampMs = downTimeMs,
                                    strokeId = currentStrokeId,
                                  )
                                )
                              }
                              try {
                                while (true) {
                                  val event = awaitPointerEvent()
                                  if (!isStylusDown && event.changes.count { it.pressed } >= 2) {
                                    activeLaserPoint = null
                                    laserTrailPoints.clear()
                                    runTwoFingerZoomLoop()
                                    break
                                  }
                                  val change = resolveActiveChange(event) ?: break
                                  event.changes.forEach { it.consume() }
                                  if (!change.pressed) break
                                  val nowMs = System.currentTimeMillis()
                                  activeLaserPoint = change.position
                                  if (laserMode == LaserMode.FADING_TRAIL) {
                                    if ((change.position - lastTrailPosition).getDistance() >= minStep) {
                                      lastTrailPosition = change.position
                                      laserTrailPoints.add(
                                        LaserTrailPoint(
                                          position = change.position,
                                          timestampMs = nowMs,
                                          strokeId = currentStrokeId,
                                        )
                                      )
                                    }
                                    laserTrailPoints.removeAll { pt ->
                                      nowMs - pt.timestampMs > laserTrailDurationMs
                                    }
                                  }
                                }
                              } finally {
                                activeLaserPoint = null
                              }
                            }

                            PrimaryTool.LASSO -> {
                              val activeShape = shapeState.value
                              val edgeHitDistance = 28f / canvasScale.coerceIn(0.25f, 5f)
                              val isInsideSelection =
                                showLassoSelection &&
                                  activeShape != null &&
                                  (activeShape.contains(activeDown.position) ||
                                    activeShape.distanceToEdge(activeDown.position) < edgeHitDistance)

                              if (isInsideSelection) {
                                var prevPos = activeDown.position
                                var recordedMoveUndo = false
                                while (true) {
                                  val event = awaitPointerEvent()
                                  if (!isStylusDown && event.changes.count { it.pressed } >= 2) {
                                    runTwoFingerZoomLoop()
                                    break
                                  }
                                  val change = resolveActiveChange(event) ?: break
                                  event.changes.forEach { it.consume() }
                                  if (!change.pressed) break
                                  val currPos = change.position
                                  val delta = currPos - prevPos
                                  if (delta.getDistance() > 0.5f) {
                                    if (!recordedMoveUndo) {
                                      onRecordUndoBeforeGesture()
                                      recordedMoveUndo = true
                                    }
                                    onMoveLassoSelection(delta)
                                    prevPos = currPos
                                  }
                                }
                                if (recordedMoveUndo) {
                                  onCommitLassoChanges()
                                }
                              } else {
                                val minLassoStep = 1.8f / canvasScale.coerceIn(0.25f, 5f)
                                inProgressPoints.clear()
                                inProgressPressures.clear()
                                inProgressPoints.add(activeDown.position)
                                var switchedToZoom = false
                                var wasCancelled = false
                                try {
                                  while (true) {
                                    val event = awaitPointerEvent()
                                    if (!isStylusDown && event.changes.count { it.pressed } >= 2) {
                                      inProgressPoints.clear()
                                      switchedToZoom = true
                                      runTwoFingerZoomLoop()
                                      break
                                    }
                                    val change = resolveActiveChange(event)
                                    if (change == null) {
                                      wasCancelled = true
                                      break
                                    }
                                    event.changes.forEach { it.consume() }
                                    if (!change.pressed) break
                                    if ((change.position - inProgressPoints.last()).getDistance() > minLassoStep) {
                                      inProgressPoints.add(change.position)
                                    }
                                  }

                                  if (!switchedToZoom && !wasCancelled && inProgressPoints.isNotEmpty()) {
                                    val dragDist =
                                      (inProgressPoints.last() - activeDown.position).getDistance()
                                    val minTapClearDist = 18f / canvasScale.coerceIn(0.25f, 5f)
                                    if (showLassoSelection && (inProgressPoints.size < 5 || dragDist < minTapClearDist)) {
                                      onClearLassoSelection()
                                    } else if (inProgressPoints.size >= 3) {
                                      onAddStroke(inProgressPoints.toList(), emptyList(), false, canvasScale)
                                    } else {
                                      onClearLassoSelection()
                                    }
                                  }
                                } finally {
                                  inProgressPoints.clear()
                                  inProgressPressures.clear()
                                }
                              }
                            }

                            PrimaryTool.WRITING_TOOLS,
                            PrimaryTool.SHAPE -> {
                              inProgressPoints.clear()
                              inProgressPressures.clear()
                              isDrawingSnappedHold = false

                              val startPoint = activeDown.position
                              val startPressure = readSafePressure(activeDown)
                              inProgressPoints.add(startPoint)
                              inProgressPressures.add(startPressure)

                              var lastMovePoint = startPoint
                              var lastMoveTimeMs = System.currentTimeMillis()
                              var holdEraseTriggered = false
                              var switchedToTwoFingerZoom = false
                              var pointerCancelled = false

                              val holdWatcher =
                                pagerScope.launch {
                                  while (true) {
                                    delay(75L)
                                    val totalDisplacement =
                                      (lastMovePoint - startPoint).getDistance()
                                    val stationaryMs = System.currentTimeMillis() - lastMoveTimeMs
                                    val holdDispThreshold = 28f / canvasScale.coerceIn(0.25f, 5f)

                                    if (
                                      drawAndHold &&
                                        !isDrawingSnappedHold &&
                                        totalDisplacement > holdDispThreshold &&
                                        stationaryMs > 400L
                                    ) {
                                      isDrawingSnappedHold = true
                                    }
                                  }
                                }

                              try {
                                val minStepDist = 1.8f / canvasScale.coerceIn(0.25f, 5f)
                                val moveNoiseThreshold = 4.2f / canvasScale.coerceIn(0.25f, 5f)

                                while (true) {
                                  val event = awaitPointerEvent()
                                  // Only switch to pinch-zoom if using fingers (!isStylusDown)
                                  // before drawing a long stroke; never abort a stylus stroke on palm contact!
                                  if (
                                    !isStylusDown &&
                                      event.changes.count { it.pressed } >= 2 &&
                                      inProgressPoints.size <= 6
                                  ) {
                                    inProgressPoints.clear()
                                    inProgressPressures.clear()
                                    switchedToTwoFingerZoom = true
                                    runTwoFingerZoomLoop()
                                    break
                                  }

                                  val change = resolveActiveChange(event)
                                  if (change == null) {
                                    pointerCancelled = true
                                    break
                                  }
                                  event.changes.forEach { it.consume() }
                                  if (!change.pressed) break

                                  // Check if stylus side button is currently pressed mid-stroke for temporary erasing
                                  if (hardwareStylusState.buttonDown || hardwareStylusState.isEraserTail) {
                                    val firstEraseFrame = !holdEraseTriggered
                                    holdEraseTriggered = true
                                    inProgressPoints.clear()
                                    inProgressPressures.clear()
                                    eraserCursorPosition = change.position
                                    onEraseAtPoint(change.position, firstEraseFrame, true, canvasScale)
                                    continue
                                  } else if (holdEraseTriggered) {
                                    // Side button was released mid-stroke -> immediately restore writing tool and flush erase
                                    holdEraseTriggered = false
                                    eraserCursorPosition = null
                                    onFinishEraserGesture()
                                    inProgressPoints.clear()
                                    inProgressPressures.clear()
                                    inProgressPoints.add(change.position)
                                    inProgressPressures.add(readSafePressure(change))
                                    continue
                                  }

                                  val rawPos = change.position
                                  val pressureVal = readSafePressure(change)
                                  val stepMove = (rawPos - lastMovePoint).getDistance()
                                  val nowMs = System.currentTimeMillis()

                                  if (stepMove > moveNoiseThreshold) {
                                    lastMovePoint = rawPos
                                    lastMoveTimeMs = nowMs
                                  }

                                  val isHighlighterSub =
                                    primaryTool == PrimaryTool.WRITING_TOOLS &&
                                      activeWritingSubTool == WritingSubTool.HIGHLIGHTER

                                  val constrainedPos =
                                    when {
                                      rulerState.isVisible -> {
                                        snapPointAlongRuler(
                                          start = startPoint,
                                          current = rawPos,
                                          angleDegrees = rulerState.angleDegrees,
                                        )
                                      }
                                      isHighlighterSub && highlighterStraightLine -> {
                                        Offset(rawPos.x, startPoint.y)
                                      }
                                      else -> rawPos
                                    }

                                  val distFromLast = (constrainedPos - inProgressPoints.last()).getDistance()
                                  if (distFromLast > minStepDist) {
                                    // Lightweight jitter smoothing that preserves sharp handwriting corners
                                    val count = inProgressPoints.size
                                    if (
                                      count >= 2 &&
                                        !rulerState.isVisible &&
                                        !(isHighlighterSub && highlighterStraightLine)
                                    ) {
                                      val prevPrev = inProgressPoints[count - 2]
                                      val prev = inProgressPoints[count - 1]
                                      val v1x = prev.x - prevPrev.x
                                      val v1y = prev.y - prevPrev.y
                                      val v2x = constrainedPos.x - prev.x
                                      val v2y = constrainedPos.y - prev.y
                                      val len1 = hypot(v1x, v1y)
                                      val len2 = hypot(v2x, v2y)
                                      if (len1 > 0.5f && len2 > 0.5f) {
                                        val cosAngle = (v1x * v2x + v1y * v2y) / (len1 * len2)
                                        // Only smooth when the stroke is not making a sharp corner (cosAngle > 0.35f)
                                        if (cosAngle > 0.35f) {
                                          val midX = (prevPrev.x + constrainedPos.x) * 0.5f
                                          val midY = (prevPrev.y + constrainedPos.y) * 0.5f
                                          inProgressPoints[count - 1] =
                                            Offset(
                                              x = prev.x * 0.74f + midX * 0.26f,
                                              y = prev.y * 0.74f + midY * 0.26f,
                                            )
                                        }
                                      }
                                    }
                                    inProgressPoints.add(constrainedPos)
                                    inProgressPressures.add(pressureVal)
                                  }
                                }

                                if (
                                  !switchedToTwoFingerZoom &&
                                    !pointerCancelled &&
                                    !holdEraseTriggered &&
                                    inProgressPoints.isNotEmpty()
                                ) {
                                  // Support single-tap stylus dots / periods
                                  if (inProgressPoints.size == 1) {
                                    val dotOffset = 0.8f / canvasScale.coerceIn(0.25f, 5f)
                                    inProgressPoints.add(startPoint + Offset(dotOffset, dotOffset))
                                    inProgressPressures.add(startPressure)
                                  }
                                  onAddStroke(
                                    inProgressPoints.toList(),
                                    inProgressPressures.toList(),
                                    isDrawingSnappedHold,
                                    canvasScale,
                                  )
                                }
                              } finally {
                                holdWatcher.cancel()
                                if (holdEraseTriggered) {
                                  onFinishEraserGesture()
                                }
                                eraserCursorPosition = null
                                inProgressPoints.clear()
                                inProgressPressures.clear()
                                isDrawingSnappedHold = false
                              }
                            }
                          }
                        }
                      },
                ) {
                  // Record logical page canvas size for 1:1 coordinate scaling on PDF/PNG export
                  if (isCurrentActivePage) {
                    onUpdatePageCanvasSize(size.width, size.height)
                  }

                  // Draw paper template lines if not a PDF page and not CUSTOM bitmap
                  if (pageData.pdfPageBitmap == null && effectiveTemplate != PaperTemplate.CUSTOM) {
                    drawPaperTemplateLines(
                      template = effectiveTemplate,
                      paperColorTheme = effectivePaperTheme,
                      scaleFactor = 1f,
                    )
                  }

                  // Render Highlighters FIRST (underneath Pen Ink)
                  pageData.strokes
                    .filter { it.isHighlighter }
                    .forEach { stroke ->
                      drawRichStroke(
                        stroke = stroke,
                        isSelectedByLasso = lassoSelectedStrokeIds.contains(stroke.id),
                      )
                    }

                  // Render Pen, Pencil, Tape, and Auto-Shapes ON TOP
                  pageData.strokes
                    .filter { !it.isHighlighter }
                    .forEach { stroke ->
                      drawRichStroke(
                        stroke = stroke,
                        isSelectedByLasso = lassoSelectedStrokeIds.contains(stroke.id),
                      )
                    }

                  // Subtle temporary preview for Pending Circle-to-Lasso Loop (800ms window)
                  if (isCurrentActivePage && pendingCircleLoop != null && pendingCircleLoop.points.size >= 3) {
                    val pendingPts = pendingCircleLoop.points
                    val pendingPath =
                      Path().apply {
                        moveTo(pendingPts.first().x, pendingPts.first().y)
                        for (i in 1 until pendingPts.size) {
                          val prev = pendingPts[i - 1]
                          val curr = pendingPts[i]
                          quadraticTo(prev.x, prev.y, (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
                        }
                      }
                    drawPath(
                      path = pendingPath,
                      color = pendingCircleLoop.color,
                      style =
                        Stroke(
                          width = pendingCircleLoop.strokeWidth,
                          cap = StrokeCap.Round,
                          join = StrokeJoin.Round,
                        ),
                    )
                    drawPath(
                      path = pendingPath,
                      color = SwitchBlue.copy(alpha = 0.55f),
                      style =
                        Stroke(
                          width = (2.2f / canvasScale.coerceIn(0.25f, 5f)).coerceAtLeast(1f),
                          cap = StrokeCap.Round,
                          pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                        ),
                    )
                    val endpointIndicatorRadius = 7f / canvasScale.coerceIn(0.25f, 5f)
                    drawCircle(
                      color = SwitchBlue.copy(alpha = 0.35f),
                      radius = endpointIndicatorRadius,
                      center = pendingCircleLoop.lastPoint,
                    )
                  }

                  // Active in-progress writing/shape stroke (excludes Lasso and Laser)
                  if (
                    isCurrentActivePage &&
                      inProgressPoints.size >= 2 &&
                      primaryTool != PrimaryTool.LASSO &&
                      primaryTool != PrimaryTool.LASER
                  ) {
                    val isShapeOrSnap =
                      primaryTool == PrimaryTool.SHAPE ||
                        (primaryTool == PrimaryTool.WRITING_TOOLS &&
                          activeWritingSubTool == WritingSubTool.DRAW_SHAPE) ||
                        isDrawingSnappedHold

                    val path =
                      Path().apply {
                        moveTo(inProgressPoints.first().x, inProgressPoints.first().y)
                        if (isShapeOrSnap && inProgressPoints.size >= 2) {
                          lineTo(inProgressPoints.last().x, inProgressPoints.last().y)
                        } else {
                          for (i in 1 until inProgressPoints.size) {
                            val prev = inProgressPoints[i - 1]
                            val curr = inProgressPoints[i]
                            quadraticTo(prev.x, prev.y, (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
                          }
                        }
                      }

                    val isHigh =
                      primaryTool == PrimaryTool.WRITING_TOOLS &&
                        activeWritingSubTool == WritingSubTool.HIGHLIGHTER
                    val isPencil =
                      primaryTool == PrimaryTool.WRITING_TOOLS &&
                        activeWritingSubTool == WritingSubTool.PENCIL
                    val isTape =
                      primaryTool == PrimaryTool.WRITING_TOOLS &&
                        activeWritingSubTool == WritingSubTool.TAPE
                    val baseW = thicknessPresets.getOrElse(selectedThicknessIndex) { 6.5f }
                    val liveW =
                      when {
                        isHigh -> baseW * 4.2f
                        isTape -> baseW * 3.6f
                        else -> baseW
                      }
                    val liveColor =
                      when {
                        isHigh -> selectedColor.copy(alpha = 0.36f)
                        isPencil -> selectedColor.copy(alpha = 0.82f)
                        isTape -> Color(0xFFFFAB40).copy(alpha = 0.85f)
                        else -> selectedColor
                      }

                    drawPath(
                      path = path,
                      color = liveColor,
                      style =
                        Stroke(
                          width = liveW,
                          cap = StrokeCap.Round,
                          join = StrokeJoin.Round,
                        ),
                    )
                  }

                  // Active Lasso Freehand Loop while dragging
                  if (isCurrentActivePage && primaryTool == PrimaryTool.LASSO && inProgressPoints.size >= 2) {
                    val lassoPath =
                      Path().apply {
                        moveTo(inProgressPoints.first().x, inProgressPoints.first().y)
                        for (i in 1 until inProgressPoints.size) {
                          lineTo(inProgressPoints[i].x, inProgressPoints[i].y)
                        }
                      }
                    drawPath(
                      path = lassoPath,
                      color = SwitchBlue,
                      style =
                        Stroke(
                          width = (3f / canvasScale.coerceIn(0.25f, 5f)).coerceAtLeast(1.2f),
                          cap = StrokeCap.Round,
                          pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 9f), 0f),
                        ),
                    )
                  }

                  // Completed Lasso Selection Outline + Progressive Circle-to-Lasso Conversion Animation
                  if (isCurrentActivePage && showLassoSelection && primaryTool == PrimaryTool.LASSO && lassoLoopPoints.size >= 3) {
                    val animProgress = circleLassoConversionProgress.value.coerceIn(0f, 1f)
                    val safeZoom = canvasScale.coerceIn(0.25f, 5f)
                    if (animProgress < 0.999f) {
                      val splitIdx =
                        (animProgress * (lassoLoopPoints.size - 1))
                          .roundToInt()
                          .coerceIn(1, lassoLoopPoints.size - 1)

                      // 1. Remaining unconverted portion of the hand-drawn ink loop (from splitIdx to end)
                      if (splitIdx < lassoLoopPoints.size - 1) {
                        val remainingInkPath =
                          Path().apply {
                            moveTo(lassoLoopPoints[splitIdx].x, lassoLoopPoints[splitIdx].y)
                            for (i in (splitIdx + 1) until lassoLoopPoints.size) {
                              lineTo(lassoLoopPoints[i].x, lassoLoopPoints[i].y)
                            }
                          }
                        drawPath(
                          path = remainingInkPath,
                          color = convertingCircleLassoColor.copy(alpha = (1f - animProgress * 0.45f).coerceIn(0.2f, 1f)),
                          style =
                            Stroke(
                              width = convertingCircleLassoWidth,
                              cap = StrokeCap.Round,
                              join = StrokeJoin.Round,
                            ),
                        )
                      }

                      // 2. Traveling dotted/dashed lasso outline from held endpoint (0) to splitIdx
                      val convertedLassoPath =
                        Path().apply {
                          moveTo(lassoLoopPoints.first().x, lassoLoopPoints.first().y)
                          for (i in 1..splitIdx) {
                            lineTo(lassoLoopPoints[i].x, lassoLoopPoints[i].y)
                          }
                        }
                      drawPath(
                        path = convertedLassoPath,
                        color = SwitchBlue,
                        style =
                          Stroke(
                            width = (2.6f / safeZoom).coerceAtLeast(1.1f),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                          ),
                      )

                      // Subtle traveling wavefront dot at the conversion head
                      val headPt = lassoLoopPoints[splitIdx]
                      drawCircle(
                        color = SwitchBlue,
                        radius = (4.5f / safeZoom).coerceAtLeast(2f),
                        center = headPt,
                      )
                    } else {
                      val loopPath =
                        Path().apply {
                          moveTo(lassoLoopPoints.first().x, lassoLoopPoints.first().y)
                          for (i in 1 until lassoLoopPoints.size) {
                            lineTo(lassoLoopPoints[i].x, lassoLoopPoints[i].y)
                          }
                          close()
                        }
                      drawPath(
                        path = loopPath,
                        color = SwitchBlue.copy(alpha = 0.05f),
                      )
                      drawPath(
                        path = loopPath,
                        color = SwitchBlue.copy(alpha = 0.75f),
                        style =
                          Stroke(
                            width = (2.2f / safeZoom).coerceAtLeast(1f),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                          ),
                      )
                      if (lassoBounds != null) {
                        val pad = 8f
                        drawRect(
                          color = SwitchBlue.copy(alpha = if (isLassoResizeMode) 0.85f else 0.45f),
                          topLeft = Offset(lassoBounds[0] - pad, lassoBounds[1] - pad),
                          size =
                            androidx.compose.ui.geometry.Size(
                              width = (lassoBounds[2] - lassoBounds[0] + pad * 2f).coerceAtLeast(24f),
                              height = (lassoBounds[3] - lassoBounds[1] + pad * 2f).coerceAtLeast(24f),
                            ),
                          style =
                            Stroke(
                              width = (if (isLassoResizeMode) 2.2f else 1.4f) / safeZoom,
                              pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                            ),
                        )
                      }
                    }
                  }

                  // Temporary Eraser Cursor while erasing
                  if (isCurrentActivePage && eraserCursorPosition != null) {
                    val cursorCenter = eraserCursorPosition!!
                    val scaledEraserRadius =
                      (eraserRadius / canvasScale.coerceIn(0.25f, 5f)).coerceAtLeast(4f)
                    drawCircle(
                      color = Color(0x24FF5252),
                      radius = scaledEraserRadius,
                      center = cursorCenter,
                    )
                    drawCircle(
                      color = Color(0xCCFF5252),
                      radius = scaledEraserRadius,
                      center = cursorCenter,
                      style =
                        Stroke(
                          width = (1.8f / canvasScale.coerceIn(0.25f, 5f)).coerceAtLeast(1f),
                        ),
                    )
                  }

                  // Temporary Presentation Laser Pointer (Bright Current Point + Stationary Fading Trail)
                  if (isCurrentActivePage) {
                    // Read laserFadeTick so Canvas invalidates smoothly during trail fade-out
                    val currentTick = laserFadeTick
                    val nowMs = System.currentTimeMillis()
                    val invScale = 1f / canvasScale.coerceIn(0.25f, 5f)

                    // 1) Stationary per-point timestamped trail segments (oldest points fade out in place)
                    if (laserTrailPoints.size >= 2 && currentTick >= 0) {
                      for (i in 1 until laserTrailPoints.size) {
                        val p0 = laserTrailPoints[i - 1]
                        val p1 = laserTrailPoints[i]
                        if (p0.strokeId != p1.strokeId) continue
                        val ageMs = (nowMs - p1.timestampMs).coerceAtLeast(0L)
                        val alphaFactor =
                          (1f - (ageMs.toFloat() / laserTrailDurationMs.toFloat())).coerceIn(0f, 1f)
                        if (alphaFactor > 0.02f) {
                          drawLine(
                            color = laserColor.copy(alpha = 0.34f * alphaFactor),
                            start = p0.position,
                            end = p1.position,
                            strokeWidth = (6f + 8f * alphaFactor) * invScale,
                            cap = StrokeCap.Round,
                          )
                          drawLine(
                            color = laserColor.copy(alpha = 0.94f * alphaFactor),
                            start = p0.position,
                            end = p1.position,
                            strokeWidth = (2.2f + 2.6f * alphaFactor) * invScale,
                            cap = StrokeCap.Round,
                          )
                        }
                      }
                    }

                    // 2) Responsive bright laser point at latest stylus position while pointer is down
                    activeLaserPoint?.let { laserPt ->
                      drawCircle(
                        color = laserColor.copy(alpha = 0.35f),
                        radius = 13f * invScale,
                        center = laserPt,
                      )
                      drawCircle(
                        color = laserColor,
                        radius = 6.5f * invScale,
                        center = laserPt,
                      )
                      drawCircle(
                        color = Color.White,
                        radius = 3f * invScale,
                        center = laserPt,
                      )
                    }

                    // Zoom Window Target Box Indicator
                    if (isZoomWindowVisible) {
                      drawRect(
                        color = Color(0x222196F3),
                        topLeft = zoomTargetBoxOffset,
                        size = androidx.compose.ui.geometry.Size(220f, 76f),
                      )
                      drawRect(
                        color = SwitchBlue,
                        topLeft = zoomTargetBoxOffset,
                        size = androidx.compose.ui.geometry.Size(220f, 76f),
                        style = Stroke(width = 3f),
                      )
                    }
                  }
                }

                // Interactive Lasso Resize & Rotate Handles
                if (isCurrentActivePage && showLassoSelection && primaryTool == PrimaryTool.LASSO && lassoBounds != null) {
                  if (isLassoResizeMode) {
                    val cornerOffsets =
                      listOf(
                        Offset(lassoBounds[0] - 8f, lassoBounds[1] - 8f),
                        Offset(lassoBounds[2] + 8f, lassoBounds[1] - 8f),
                        Offset(lassoBounds[0] - 8f, lassoBounds[3] + 8f),
                      )
                    cornerOffsets.forEachIndexed { idx, corner ->
                      Box(
                        modifier =
                          Modifier
                            .offset {
                              IntOffset((corner.x - 10f).roundToInt(), (corner.y - 10f).roundToInt())
                            }
                            .size(20.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.5.dp, SwitchBlue, CircleShape)
                            .pointerInput(idx) {
                              var handle = Offset.Zero
                              var pivot = Offset.Zero
                              detectDragGestures(
                                onDragStart = {
                                  onRecordUndoBeforeGesture()
                                  val b = boundsState.value
                                  val lp = loopState.value
                                  if (b != null && lp.isNotEmpty()) {
                                    handle =
                                      when (idx) {
                                        0 -> Offset(b[0] - 8f, b[1] - 8f)
                                        1 -> Offset(b[2] + 8f, b[1] - 8f)
                                        else -> Offset(b[0] - 8f, b[3] + 8f)
                                      }
                                    pivot =
                                      Offset(
                                        lp.map { it.x }.average().toFloat(),
                                        lp.map { it.y }.average().toFloat(),
                                      )
                                  }
                                },
                                onDragEnd = { onCommitLassoChanges() },
                                onDragCancel = { onCommitLassoChanges() },
                              ) { change, drag ->
                                change.consume()
                                val oldVec = handle - pivot
                                handle += drag
                                val newVec = handle - pivot
                                val scale =
                                  (newVec.getDistance().coerceAtLeast(20f) /
                                      oldVec.getDistance().coerceAtLeast(20f))
                                    .coerceIn(0.92f, 1.08f)
                                onTransformLassoSelection(scale, 0f)
                              }
                            }
                            .testTag("lasso_corner_resize_handle_$idx"),
                      )
                    }
                  }

                  val handleX = lassoBounds[2] + 8f
                  val handleY = lassoBounds[3] + 8f
                  Box(
                    modifier =
                      Modifier
                        .offset {
                          IntOffset((handleX - 15f).roundToInt(), (handleY - 15f).roundToInt())
                        }
                        .size(30.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(SwitchBlue)
                        .border(2.dp, Color.White, CircleShape)
                        .pointerInput(Unit) {
                          var handle = Offset.Zero
                          var pivot = Offset.Zero
                          detectDragGestures(
                            onDragStart = {
                              onRecordUndoBeforeGesture()
                              val b = boundsState.value
                              val lp = loopState.value
                              if (b != null && lp.isNotEmpty()) {
                                handle = Offset(b[2] + 8f, b[3] + 8f)
                                pivot =
                                  Offset(
                                    lp.map { it.x }.average().toFloat(),
                                    lp.map { it.y }.average().toFloat(),
                                  )
                              }
                            },
                            onDragEnd = { onCommitLassoChanges() },
                            onDragCancel = { onCommitLassoChanges() },
                          ) { change, drag ->
                            change.consume()
                            val oldVec = handle - pivot
                            handle += drag
                            val newVec = handle - pivot
                            val scale =
                              (newVec.getDistance().coerceAtLeast(20f) /
                                  oldVec.getDistance().coerceAtLeast(20f))
                                .coerceIn(0.92f, 1.08f)
                            val rot =
                              Math.toDegrees(
                                  (atan2(newVec.y, newVec.x) - atan2(oldVec.y, oldVec.x)).toDouble()
                                )
                                .toFloat()
                                .coerceIn(-12f, 12f)
                            onTransformLassoSelection(scale, rot)
                          }
                        }
                        .testTag("lasso_resize_rotate_handle"),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.OpenInFull,
                      contentDescription = "Drag to Resize and Rotate Selection",
                      tint = Color.White,
                      modifier = Modifier.size(15.dp),
                    )
                  }
                }

                // Interactive Shape Vertex Control Handles
                if (isCurrentActivePage) {
                  val editableShapeStroke =
                    pageData.strokes.lastOrNull { s ->
                      s.isShape &&
                        s.shapeControlPoints.isNotEmpty() &&
                        (s.id == activeShapeEditStrokeId ||
                          primaryTool == PrimaryTool.SHAPE ||
                          (primaryTool == PrimaryTool.WRITING_TOOLS &&
                            activeWritingSubTool == WritingSubTool.DRAW_SHAPE))
                    }
                  editableShapeStroke?.shapeControlPoints?.forEachIndexed { handleIdx, pt ->
                    Box(
                      modifier =
                        Modifier
                          .offset {
                            IntOffset((pt.x - 10f).roundToInt(), (pt.y - 10f).roundToInt())
                          }
                          .size(20.dp)
                          .shadow(4.dp, CircleShape)
                          .clip(CircleShape)
                          .background(Color.White)
                          .border(2.5.dp, SwitchBlue, CircleShape)
                          .pointerInput(editableShapeStroke.id, handleIdx) {
                            detectDragGestures(
                              onDragStart = { onRecordUndoBeforeGesture() },
                            ) { change, dragAmount ->
                              change.consume()
                              onMoveShapeControlHandle(editableShapeStroke.id, handleIdx, dragAmount)
                            }
                          }
                          .testTag("shape_handle_$handleIdx"),
                    )
                  }
                }

                // Element Stamps
                pageData.elements.forEach { element ->
                  Box(
                    modifier =
                      Modifier
                        .offset {
                          IntOffset(element.position.x.roundToInt(), element.position.y.roundToInt())
                        }
                        .clip(RoundedCornerShape(10.dp))
                        .background(element.stampType.tint.copy(alpha = 0.16f))
                        .border(1.5.dp, element.stampType.tint, RoundedCornerShape(10.dp))
                        .then(
                          if (lassoActive) Modifier
                          else
                            Modifier.pointerInput(element.id) {
                              detectDragGestures(
                                onDragStart = { onRecordUndoBeforeGesture() },
                              ) { change, dragAmount ->
                                change.consume()
                                onMoveElementStamp(element.id, dragAmount)
                              }
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                  ) {
                    Text(
                      text = element.stampType.badgeText,
                      color = element.stampType.tint,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                }

                // Text Boxes
                pageData.textBoxes.forEach { textBox ->
                  Box(
                    modifier =
                      Modifier
                        .offset {
                          IntOffset(textBox.position.x.roundToInt(), textBox.position.y.roundToInt())
                        }
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.88f))
                        .border(1.dp, SwitchBlue.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                        .then(
                          if (lassoActive) Modifier
                          else
                            Modifier.pointerInput(textBox.id) {
                              detectDragGestures(
                                onDragStart = { onRecordUndoBeforeGesture() },
                              ) { change, dragAmount ->
                                change.consume()
                                onMoveTextBox(textBox.id, dragAmount)
                              }
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                  ) {
                    BasicTextField(
                      value = textBox.text,
                      onValueChange = { onUpdateTextBox(textBox.id, it) },
                      textStyle =
                        TextStyle(
                          color =
                            if (textBox.color == Color.White) Color(0xFF1A1A1C) else textBox.color,
                          fontSize = textBox.fontSizeSp.sp,
                          fontWeight = FontWeight.Medium,
                        ),
                    )
                  }
                }

                // Sticky Notes
                pageData.stickyNotes.forEach { sticky ->
                  Column(
                    modifier =
                      Modifier
                        .offset {
                          IntOffset(sticky.position.x.roundToInt(), sticky.position.y.roundToInt())
                        }
                        .width(if (sticky.isExpanded) 170.dp else 110.dp)
                        .shadow(6.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(sticky.backgroundColor)
                        .border(1.dp, Color(0xFFE0C860), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = if (sticky.isExpanded) "STICKY NOTE" else "📌 NOTE",
                        color = Color(0xFF5D4037),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onToggleStickyNoteExpanded(sticky.id) },
                      )
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete sticky note",
                        tint = Color(0xFF5D4037),
                        modifier =
                          Modifier
                            .size(13.dp)
                            .clickable { onDeleteStickyNote(sticky.id) },
                      )
                    }
                    if (sticky.isExpanded) {
                      Spacer(modifier = Modifier.height(4.dp))
                      BasicTextField(
                        value = sticky.text,
                        onValueChange = { onUpdateStickyNoteText(sticky.id, it) },
                        textStyle =
                          TextStyle(
                            color = Color(0xFF2C221E),
                            fontSize = 11.5.sp,
                          ),
                      )
                    }
                  }
                }

                if (isCurrentActivePage) {
                  InteractiveRulerOverlay(
                    rulerState = rulerState,
                    onMoveRuler = onMoveRuler,
                    onRotateRuler = onRotateRuler,
                    onCloseRuler = onToggleRuler,
                  )

                  // Contextual Lasso Action Pill (Anchored near active selection)
                  androidx.compose.animation.AnimatedVisibility(
                    visible = primaryTool == PrimaryTool.LASSO && showLassoSelection,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier =
                      if (lassoBounds != null) {
                        val pillTop =
                          if (lassoBounds[1] > 64f) (lassoBounds[1] - 56f).coerceAtLeast(8f)
                          else (lassoBounds[3] + 18f)
                        Modifier.offset { IntOffset(0, pillTop.roundToInt()) }
                      } else {
                        Modifier.padding(top = 18.dp)
                      },
                  ) {
                    Box(
                      modifier = Modifier.fillMaxWidth(),
                      contentAlignment = Alignment.TopCenter,
                    ) {
                      LassoFloatingMenuPill(
                        isResizeModeActive = isLassoResizeMode,
                        hasClipboardContent = hasClipboardContent,
                        isColorMenuOpen = isLassoColorMenuOpen,
                        onToggleColorMenu = onToggleLassoColorMenu,
                        onSelectColor = onRecolorLassoSelection,
                        onCopyClick = onCopyLassoSelection,
                        onPasteClick = onPasteLassoSelection,
                        onCutClick = onCutLassoSelection,
                        onScaleStepClick = onScaleLassoSelectionStep,
                        onResizeToggleClick = onScaleSelection,
                        onDuplicateClick = onDuplicateSelection,
                        onDeleteClick = onDeleteLassoSelection,
                        onOpenLassoFiltersClick = { onTogglePopover(ActivePopover.LASSO_SETTINGS) },
                        onDismissLassoClick = onToggleLassoSelection,
                      )
                    }
                  }

                  // Paste Clipboard Pill when items are in clipboard
                  androidx.compose.animation.AnimatedVisibility(
                    visible = hasClipboardContent && (!showLassoSelection || primaryTool != PrimaryTool.LASSO),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier =
                      Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp),
                  ) {
                    Row(
                      modifier =
                        Modifier
                          .shadow(8.dp, RoundedCornerShape(18.dp))
                          .clip(RoundedCornerShape(18.dp))
                          .background(Color(0xEE18315A))
                          .border(1.dp, Color(0xFF4C8DF6), RoundedCornerShape(18.dp))
                          .clickable { onPasteLassoSelection() }
                          .padding(horizontal = 14.dp, vertical = 7.dp)
                          .testTag("floating_paste_clipboard_chip"),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Outlined.ContentPaste,
                        contentDescription = "Paste Clipboard Selection",
                        tint = Color(0xFF67C8EB),
                        modifier = Modifier.size(15.dp),
                      )
                      Text(
                        text = "Paste Clipboard",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                      )
                    }
                  }
                }
              }
            }
          }

          // Render Whiteboard or Multi-Page Pager inside the clipped viewport
          if (isWhiteboard) {
            renderSingleNotePage(0)
          } else if (pageScrollDirection == PageScrollDirection.HORIZONTAL) {
            HorizontalPager(
              state = pagerState,
              userScrollEnabled = (onlyDrawWithStylus || isReadOnlyMode) && canvasScale <= 1.05f,
              modifier = Modifier.fillMaxSize().testTag("horizontal_page_pager"),
            ) { pageIdx ->
              renderSingleNotePage(pageIdx)
            }
          } else {
            VerticalPager(
              state = pagerState,
              userScrollEnabled = (onlyDrawWithStylus || isReadOnlyMode) && canvasScale <= 1.05f,
              modifier = Modifier.fillMaxSize().testTag("vertical_page_pager"),
            ) { pageIdx ->
              renderSingleNotePage(pageIdx)
            }
          }

          // Compact Zoom Indicator & Reset Pill (Bottom-Start inside clipped canvas)
          Row(
            modifier =
              Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xDD1C1C1E))
                .border(1.dp, Color(0xFF34353A), RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Remove,
              contentDescription = "Zoom Out",
              tint = ToolbarIconLight,
              modifier =
                Modifier
                  .size(16.dp)
                  .clickable {
                    val minS = if (isWhiteboard) 0.35f else 0.65f
                    canvasScale = (canvasScale - 0.25f).coerceIn(minS, 4.5f)
                    if (!isWhiteboard && canvasScale <= 1.04f) canvasPanOffset = Offset.Zero
                  },
            )
            Text(
              text = "${(canvasScale * 100).roundToInt()}%",
              color = if (kotlin.math.abs(canvasScale - 1f) > 0.04f) SubToolbarActiveCyan else TextPrimaryWhite,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              modifier =
                Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .clickable {
                    canvasScale = 1f
                    canvasPanOffset = Offset.Zero
                  }
                  .padding(horizontal = 4.dp, vertical = 2.dp)
                  .testTag("zoom_reset_percentage"),
            )
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Zoom In",
              tint = ToolbarIconLight,
              modifier =
                Modifier
                  .size(16.dp)
                  .clickable {
                    val minS = if (isWhiteboard) 0.35f else 0.65f
                    canvasScale = (canvasScale + 0.25f).coerceIn(minS, 4.5f)
                  },
            )
            if (isWhiteboard || kotlin.math.abs(canvasScale - 1f) > 0.05f || canvasPanOffset != Offset.Zero) {
              Icon(
                imageVector = Icons.Outlined.CenterFocusStrong,
                contentDescription = "Fit to Content / Reset View",
                tint = SubToolbarActiveCyan,
                modifier =
                  Modifier
                    .size(15.dp)
                    .clickable {
                      canvasScale = 1f
                      canvasPanOffset = Offset.Zero
                    }
                    .testTag("whiteboard_fit_content_button"),
              )
            }
          }

          // Bottom-End Page Navigation Pill (for Notebooks/PDFs) OR Whiteboard Minimap Indicator
          if (isWhiteboard) {
            WhiteboardMinimapBadge(
              strokes = currentPage.strokes,
              canvasScale = canvasScale,
              canvasPanOffset = canvasPanOffset,
              onResetView = {
                canvasScale = 1f
                canvasPanOffset = Offset.Zero
              },
              modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
            )
          } else {
            Row(
              modifier =
                Modifier
                  .align(Alignment.BottomEnd)
                  .padding(12.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color(0xDD1C1C1E))
                  .border(1.dp, Color(0xFF34353A), RoundedCornerShape(14.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Page",
                tint = if (currentPageIdx > 0) Color.White else TextSecondaryGray.copy(alpha = 0.4f),
                modifier =
                  Modifier
                    .size(18.dp)
                    .clickable(enabled = currentPageIdx > 0) { navigateByPageSwipe(-1) }
                    .testTag("page_prev_button"),
              )

              Text(
                text = "Page ${currentPageIdx + 1} of ${pages.size}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onTogglePageSidebar),
              )

              Icon(
                imageVector =
                  if (currentPageIdx < pages.lastIndex) Icons.AutoMirrored.Filled.KeyboardArrowRight
                  else Icons.Default.Add,
                contentDescription =
                  if (currentPageIdx < pages.lastIndex) "Next Page" else "Add New Page",
                tint = Color(0xFF67C8EB),
                modifier =
                  Modifier
                    .size(18.dp)
                    .clickable {
                      if (currentPageIdx < pages.lastIndex) navigateByPageSwipe(+1)
                      else onAddNewPage()
                    }
                    .testTag("page_next_button"),
              )

              Box(
                modifier =
                  Modifier
                    .width(1.dp)
                    .height(12.dp)
                    .background(Color.White.copy(alpha = 0.25f)),
              )

              Row(
                modifier =
                  Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onTogglePageScrollDirection)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .testTag("toggle_scroll_direction_badge"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
              ) {
                Icon(
                  imageVector =
                    if (pageScrollDirection == PageScrollDirection.HORIZONTAL) {
                      Icons.Outlined.SwapHoriz
                    } else {
                      Icons.Outlined.SwapVert
                    },
                  contentDescription = "Toggle Horizontal or Vertical Page Swipe",
                  tint = Color(0xFF67C8EB),
                  modifier = Modifier.size(14.dp),
                )
                Text(
                  text = if (pageScrollDirection == PageScrollDirection.HORIZONTAL) "H" else "V",
                  color = Color(0xFF67C8EB),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }

          // Active Popovers Anchored Cleanly Below Top Docked Bars
          if (activePopover != ActivePopover.NONE) {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp),
              contentAlignment = Alignment.TopCenter,
            ) {
              when (activePopover) {
                ActivePopover.PEN_SETTINGS -> {
                  PenSettingsPopover(
                    selectedPenStyle = penStyle,
                    isPencil = activeWritingSubTool == WritingSubTool.PENCIL,
                    selectedColor = selectedColor,
                    strokePattern = strokePattern,
                    strokeStabilization = strokeStabilization,
                    drawAndHold = drawAndHold,
                    holdToErase = holdToErase,
                    scribbleToErase = scribbleToErase,
                    circleToLasso = circleToLasso,
                    strokeWidth = thicknessPresets.getOrElse(selectedThicknessIndex) { 6.5f },
                    selectedThicknessIndex = selectedThicknessIndex,
                    onSelectPenStyle = onSelectPenStyle,
                    onSelectStrokePattern = onSelectStrokePattern,
                    onSelectThicknessIndex = onSelectThicknessIndex,
                    onStabilizationChange = onStabilizationChange,
                    onToggleDrawAndHold = onToggleDrawAndHold,
                    onToggleHoldToErase = onToggleHoldToErase,
                    onToggleScribbleToErase = onToggleScribbleToErase,
                    onToggleCircleToLasso = onToggleCircleToLasso,
                    onDismiss = onDismissPopover,
                  )
                }

                ActivePopover.HIGHLIGHTER_SETTINGS -> {
                  HighlighterSettingsPopover(
                    selectedColor = selectedColor,
                    highlighterStraightLine = highlighterStraightLine,
                    drawAndHold = drawAndHold,
                    selectedThicknessIndex = selectedThicknessIndex,
                    onToggleStraightLine = onToggleHighlighterStraightLine,
                    onToggleDrawAndHold = onToggleDrawAndHold,
                    onSelectThicknessIndex = onSelectThicknessIndex,
                  )
                }

                ActivePopover.PEN_COLOR -> {
                  PenColorPopover(
                    selectedColor = selectedColor,
                    customPalette = customPalette,
                    toolbarSwatchCount = quickSwatches.size,
                    selectedSwatchIndex = selectedSwatchIndex,
                    isEyedropperActive = isEyedropperActive,
                    onSelectColorReplaceSlot = onSelectPaletteColor,
                    onAddNewColorSlotToToolbar = onAddNewColorToToolbar,
                    onAddColorToCustomPalette = onAddCustomColorToPalette,
                    onRemoveCurrentToolbarSlot = onRemoveCurrentToolbarSlot,
                    onToggleEyedropper = onToggleEyedropper,
                  )
                }

                ActivePopover.ERASER_SETTINGS -> {
                  EraserSettingsPopover(
                    selectedMode = eraserMode,
                    eraserRadius = eraserRadius,
                    autoDeselect = eraserAutoDeselect,
                    onSelectMode = onSelectEraserMode,
                    onEraserRadiusChange = onEraserRadiusChange,
                    onToggleAutoDeselect = onToggleEraserAutoDeselect,
                    onClearPage = onClearPageStrokes,
                  )
                }

                ActivePopover.SHAPE_SETTINGS -> {
                  ShapeSettingsPopover(
                    lastAutoDetectedShape = lastAutoDetectedShape,
                    fillShapes = fillShapes,
                    onInsertQuickShape = onInsertQuickShape,
                    onToggleFillShapes = onToggleFillShapes,
                  )
                }

                ActivePopover.LASSO_SETTINGS -> {
                  LassoSettingsPopover(
                    filters = lassoFilters,
                    onUpdateFilters = onUpdateLassoFilters,
                    onBringToFront = onBringToFront,
                    onSendToBack = onSendToBack,
                  )
                }

                ActivePopover.TIME_KEEPER -> {
                  TimeKeeperPopover(onDismiss = onDismissPopover)
                }

                ActivePopover.TOOLBAR_CUSTOMIZE -> {
                  ToolbarCustomizationPopover(
                    enabledTools = enabledToolbarTools,
                    enabledWritingSubTools = enabledWritingSubTools,
                    toolbarDockPosition = toolbarDockPosition,
                    onSelectToolbarDockPosition = onSelectToolbarDockPosition,
                    onResetSubToolbarPosition = onResetSubToolbarPosition,
                    onToggleToolEnabled = onToggleToolbarToolEnabled,
                    onToggleWritingSubToolEnabled = onToggleWritingSubToolEnabled,
                    onResetDefaults = onResetToolbarDefaults,
                    onResetWritingDefaults = onResetWritingToolsDefaults,
                    onDismiss = onDismissPopover,
                  )
                }

                ActivePopover.EXPORT_SHARE -> {
                  ExportAndSharePopover(
                    document = activeDocument,
                    watermarkConfidentialExports = watermarkConfidentialExports,
                    documentSha256Hash = documentSha256Hash,
                    onExportFullPdf = onExportFullPdf,
                    onExportCurrentPagePng = onExportCurrentPagePng,
                    onToggleWatermark = onToggleWatermark,
                    onCyclePaperColorTheme = onCyclePaperColorTheme,
                    onOpenSecurityVault = { onTogglePopover(ActivePopover.SECURITY_VAULT) },
                    onDismiss = onDismissPopover,
                  )
                }

                ActivePopover.SECURITY_VAULT -> {
                  SecurityVaultPopover(
                    document = activeDocument,
                    privacyScreenShieldEnabled = privacyScreenShieldEnabled,
                    watermarkConfidentialExports = watermarkConfidentialExports,
                    documentSha256Hash = documentSha256Hash,
                    onSetVaultPin = onSetVaultPin,
                    onRemoveVaultLock = onRemoveVaultLock,
                    onTogglePrivacyScreenShield = onTogglePrivacyScreenShield,
                    onToggleWatermark = onToggleWatermark,
                    onDismiss = onDismissPopover,
                  )
                }

                ActivePopover.ADD_PAGE_DIALOG -> {
                  AddPageAndTemplatePopover(
                    currentTemplate =
                      currentPage.pageTemplate ?: activeDocument?.paperTemplate ?: PaperTemplate.RULED,
                    currentColorTheme =
                      currentPage.pageColorTheme
                        ?: activeDocument?.paperColorTheme
                        ?: PaperColorTheme.PURE_WHITE,
                    customTemplates = customTemplates,
                    canDeleteCurrentPage = (activeDocument?.pages?.size ?: 1) > 1,
                    onAddPageWithTemplate = { tmpl, theme, customTmpl, posMode ->
                      onAddPageWithTemplate(tmpl, theme, customTmpl, posMode)
                      onDismissPopover()
                    },
                    onApplyTemplateToCurrentPage = { tmpl, theme, customTmpl ->
                      onChangeCurrentPageTemplate(tmpl, theme, customTmpl)
                      onDismissPopover()
                    },
                    onDuplicateCurrentPage = {
                      onDuplicateCurrentPage()
                      onDismissPopover()
                    },
                    onDeleteCurrentPage = {
                      onDeleteCurrentPage()
                      onDismissPopover()
                    },
                    onImportCustomTemplate = onImportCustomTemplateFromDevice,
                    onImportPdfDocument = {
                      onDismissPopover()
                      onImportPdfFromDevice()
                    },
                    onDismiss = onDismissPopover,
                  )
                }

                else -> Unit
              }
            }
          }
        }
      }

      if (isZoomWindowVisible) {
        ZoomWindowPanel(
          strokeColor = selectedColor,
          strokeWidth = thicknessPresets.getOrElse(selectedThicknessIndex) { 6.5f },
          targetBoxOffset = zoomTargetBoxOffset,
          onCommitZoomStroke = { scaledPts ->
            onAddStroke(scaledPts, emptyList(), false, canvasScale)
          },
          onAdvanceZoomBox = onAdvanceZoomBox,
          onCloseZoomWindow = onToggleZoomWindow,
        )
      }
    }
  }
}

/**
 * Docked Primary Toolbar (Always anchored at the top below the Document Tab Bar).
 * Includes Sidebar toggle, Bookmark, Undo/Redo, Primary Tools, Audio Record, Add Page, Share, Vault, and More menu.
 */
@Composable
private fun DockedMainToolbar(
  primaryTool: PrimaryTool,
  enabledToolbarTools: Set<PrimaryTool>,
  isBookmarked: Boolean,
  isVaultLocked: Boolean,
  isWhiteboard: Boolean,
  onlyDrawWithStylus: Boolean,
  pageScrollDirection: PageScrollDirection,
  canUndo: Boolean,
  canRedo: Boolean,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  onSelectPrimaryTool: (PrimaryTool) -> Unit,
  onTogglePageSidebar: () -> Unit,
  onToggleBookmark: () -> Unit,
  onToggleRuler: () -> Unit,
  onToggleZoomWindow: () -> Unit,
  onOpenTimeKeeper: () -> Unit,
  onOpenToolbarCustomize: () -> Unit,
  onOpenExportShare: () -> Unit,
  onOpenSecurityVault: () -> Unit,
  onOpenAddPagePopover: () -> Unit,
  onTogglePageScrollDirection: () -> Unit,
  onImportPdfFromDevice: () -> Unit,
  onCyclePaperTemplate: () -> Unit,
  onCyclePaperColorTheme: () -> Unit,
  onClearPageStrokes: () -> Unit,
  onToggleOnlyDrawWithStylus: () -> Unit,
  onOpenSettings: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(50.dp)
        .background(ToolbarNavy)
        .padding(horizontal = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Left Cluster: Page Thumbnails, Search, Bookmark, and Docked Undo / Redo
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (!isWhiteboard) {
        Box(
          modifier =
            Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable(onClick = onTogglePageSidebar)
              .testTag("toolbar_sidebar_button"),
          contentAlignment = Alignment.Center,
        ) {
          SidebarThumbnailsIcon(tint = ToolbarIconLight, modifier = Modifier.size(21.dp))
        }

        Box(
          modifier =
            Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable(onClick = onTogglePageSidebar)
              .testTag("toolbar_search_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Search in document",
            tint = ToolbarIconLight,
            modifier = Modifier.size(21.dp),
          )
        }

        Box(
          modifier =
            Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable(onClick = onToggleBookmark)
              .testTag("toolbar_bookmark_button"),
          contentAlignment = Alignment.Center,
        ) {
          PageBookmarkIcon(
            tint = if (isBookmarked) Color(0xFFFFD54F) else ToolbarIconLight,
            isBookmarked = isBookmarked,
            modifier = Modifier.size(21.dp),
          )
        }

        Box(
          modifier =
            Modifier
              .width(1.dp)
              .height(22.dp)
              .background(ToolbarIconLight.copy(alpha = 0.22f)),
        )
      }

      // Docked Undo & Redo Buttons
      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onUndo)
            .testTag("undo_button"),
        contentAlignment = Alignment.Center,
      ) {
        UndoCurvedArrowIcon(
          tint = if (canUndo) ToolbarIconLight else TextSecondaryGray.copy(alpha = 0.4f),
          modifier = Modifier.size(19.dp),
        )
      }

      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onRedo)
            .testTag("redo_button"),
        contentAlignment = Alignment.Center,
      ) {
        RedoCurvedArrowIcon(
          tint = if (canRedo) ToolbarIconLight else TextSecondaryGray.copy(alpha = 0.4f),
          modifier = Modifier.size(19.dp),
        )
      }
    }

    // Center Cluster: Focused Core Note-Taking Tools (Writing Tools, Eraser, Lasso, Shapes, Laser)
    Row(
      modifier =
        Modifier
          .weight(1f)
          .padding(horizontal = 8.dp)
          .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        PrimaryTool.entries
          .filter { enabledToolbarTools.contains(it) }
          .forEach { tool ->
            val isSelected = primaryTool == tool
            Box(
              modifier =
                Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) ActiveToolPill else Color.Transparent)
                  .clickable { onSelectPrimaryTool(tool) }
                  .testTag("primary_tool_${tool.name.lowercase()}"),
              contentAlignment = Alignment.Center,
            ) {
              PrimaryToolCustomIcon(
                tool = tool,
                tint = if (isSelected) ActiveToolIconDark else ToolbarIconLight,
                modifier = Modifier.size(23.dp),
              )
              if (isSelected) {
                Box(
                  modifier =
                    Modifier
                      .align(Alignment.BottomEnd)
                      .padding(3.dp)
                      .size(4.dp)
                      .clip(CircleShape)
                      .background(ActiveToolIconDark),
                )
              }
            }
          }
      }
    }

    // Right Cluster: Add Page & Template Studio, Export/Share, Vault Lock, and More Menu
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (!isWhiteboard) {
        Box(
          modifier =
            Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable(onClick = onOpenAddPagePopover)
              .testTag("toolbar_add_page_button"),
          contentAlignment = Alignment.Center,
        ) {
          AddPageDocumentIcon(tint = ToolbarIconLight, modifier = Modifier.size(21.dp))
        }
      }

      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onOpenExportShare)
            .testTag("toolbar_share_button"),
        contentAlignment = Alignment.Center,
      ) {
        ExportShareBoxIcon(tint = ToolbarIconLight, modifier = Modifier.size(21.dp))
      }

      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onOpenSecurityVault)
            .testTag("toolbar_security_vault_button"),
        contentAlignment = Alignment.Center,
      ) {
        SecurityVaultShieldIcon(
          tint = if (isVaultLocked) Color(0xFF34C759) else ToolbarIconLight,
          isLocked = isVaultLocked,
          modifier = Modifier.size(20.dp),
        )
      }

      var showMoreMenu by remember { mutableStateOf(false) }
      Box {
        Box(
          modifier =
            Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable { showMoreMenu = true }
              .testTag("toolbar_more_button"),
          contentAlignment = Alignment.Center,
        ) {
          MoreCircleDotsIcon(tint = ToolbarIconLight, modifier = Modifier.size(21.dp))
        }

        DropdownMenu(
          expanded = showMoreMenu,
          onDismissRequest = { showMoreMenu = false },
          modifier = Modifier.background(PopoverBg),
        ) {
          if (!isWhiteboard) {
            DropdownMenuItem(
              text = {
                Text(
                  "Add Page & Change Template...",
                  color = Color(0xFF67C8EB),
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                )
              },
              onClick = {
                showMoreMenu = false
                onOpenAddPagePopover()
              },
            )
          }
          DropdownMenuItem(
            text = {
              Text(
                "Export PDF & Page Image...",
                color = TextPrimaryWhite,
                fontSize = 13.sp,
              )
            },
            onClick = {
              showMoreMenu = false
              onOpenExportShare()
            },
          )
          DropdownMenuItem(
            text = {
              Text(
                "Vault Lock & Privacy Shield...",
                color = Color(0xFF34C759),
                fontSize = 13.sp,
              )
            },
            onClick = {
              showMoreMenu = false
              onOpenSecurityVault()
            },
          )
          if (!isWhiteboard) {
            DropdownMenuItem(
              text = {
                Text(
                  "Scrolling Direction: ${pageScrollDirection.label}",
                  color = TextPrimaryWhite,
                  fontSize = 13.sp,
                )
              },
              leadingIcon = {
                Icon(
                  if (pageScrollDirection == PageScrollDirection.HORIZONTAL) Icons.Outlined.SwapHoriz
                  else Icons.Outlined.SwapVert,
                  contentDescription = null,
                  tint = Color(0xFF67C8EB),
                )
              },
              onClick = {
                showMoreMenu = false
                onTogglePageScrollDirection()
              },
            )
          }
          DropdownMenuItem(
            text = {
              Text(
                "Customize Toolbar Tools...",
                color = TextPrimaryWhite,
                fontSize = 13.sp,
              )
            },
            leadingIcon = {
              Icon(Icons.Outlined.Tune, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showMoreMenu = false
              onOpenToolbarCustomize()
            },
          )
          DropdownMenuItem(
            text = {
              Text(
                if (onlyDrawWithStylus) "Disable Stylus-Only Mode"
                else "Enable Stylus-Only (Finger Pan & Swipe)",
                color = TextPrimaryWhite,
                fontSize = 13.sp,
              )
            },
            leadingIcon = {
              Icon(Icons.Outlined.PanTool, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showMoreMenu = false
              onToggleOnlyDrawWithStylus()
            },
          )
          DropdownMenuItem(
            text = { Text("Cycle Paper Color Tint", color = TextPrimaryWhite, fontSize = 13.sp) },
            onClick = {
              showMoreMenu = false
              onCyclePaperColorTheme()
            },
          )
          DropdownMenuItem(
            text = { Text("Ruler Tool", color = TextPrimaryWhite, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.Straighten, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showMoreMenu = false
              onToggleRuler()
            },
          )
          DropdownMenuItem(
            text = { Text("Zoom Window", color = TextPrimaryWhite, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.ZoomIn, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showMoreMenu = false
              onToggleZoomWindow()
            },
          )
          DropdownMenuItem(
            text = { Text("Time Keeper (Timer / Stopwatch)", color = TextPrimaryWhite, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.Timer, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showMoreMenu = false
              onOpenTimeKeeper()
            },
          )
          DropdownMenuItem(
            text = { Text("Import PDF...", color = TextPrimaryWhite, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showMoreMenu = false
              onImportPdfFromDevice()
            },
          )
          DropdownMenuItem(
            text = { Text("Document & Stylus Settings...", color = TextPrimaryWhite, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.Settings, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showMoreMenu = false
              onOpenSettings()
            },
          )
          DropdownMenuItem(
            text = { Text("Clear Current Page", color = DangerRed, fontSize = 13.sp) },
            onClick = {
              showMoreMenu = false
              onClearPageStrokes()
            },
          )
        }
      }
    }
  }
}

/**
 * Docked Contextual Secondary Tool Strip (sits directly under DockedMainToolbar, no manual drag handle).
 */
@Composable
private fun DockedContextualWritingStrip(
  activeWritingSubTool: WritingSubTool,
  enabledWritingSubTools: Set<WritingSubTool>,
  strokePattern: StrokePattern,
  selectedThicknessIndex: Int,
  quickSwatches: List<Color>,
  selectedSwatchIndex: Int,
  isPenPopoverOpen: Boolean,
  isColorPopoverOpen: Boolean,
  isEyedropperActive: Boolean,
  onSelectWritingSubTool: (WritingSubTool) -> Unit,
  onSelectThicknessIndex: (Int) -> Unit,
  onSelectQuickSwatch: (Int) -> Unit,
  onOpenColorPicker: () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(44.dp)
        .background(SubToolbarPillBg)
        .border(0.5.dp, SubToolbarBorder)
        .padding(horizontal = 12.dp)
        .testTag("floating_pen_subtoolbar"),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      modifier = Modifier.horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      // Writing Sub-Tools (Pen, Pencil, Highlighter, Tape, Draw Shape)
      WritingSubTool.entries
        .filter { enabledWritingSubTools.contains(it) }
        .forEachIndexed { idx, subTool ->
          val isSelected = activeWritingSubTool == subTool
          if (isSelected) {
            Row(
              modifier =
                Modifier
                  .height(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (isPenPopoverOpen) SubToolbarActiveCyan.copy(alpha = 0.28f)
                    else SubToolbarActiveTeal
                  )
                  .clickable { onSelectWritingSubTool(subTool) }
                  .padding(horizontal = 9.dp)
                  .testTag(
                    if (subTool == WritingSubTool.PEN) "subtoolbar_pen_style_button"
                    else "subtoolbar_writing_${subTool.name.lowercase()}"
                  ),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
              WritingSubToolCustomIcon(
                subTool = subTool,
                tint = SubToolbarActiveCyan,
                modifier = Modifier.size(19.dp),
              )
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = subTool.label,
                tint = ToolbarIconLight,
                modifier = Modifier.size(14.dp),
              )
            }
          } else {
            Box(
              modifier =
                Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { onSelectWritingSubTool(subTool) }
                  .testTag(
                    if (subTool == WritingSubTool.PEN) "subtoolbar_pen_style_button"
                    else "subtoolbar_mode_$idx"
                  ),
              contentAlignment = Alignment.Center,
            ) {
              WritingSubToolCustomIcon(
                subTool = subTool,
                tint = ToolbarIconLight.copy(alpha = 0.9f),
                modifier = Modifier.size(19.dp),
              )
            }
          }
        }

      Box(
        modifier =
          Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(20.dp)
            .background(SubToolbarBorder),
      )

      // Stroke Thickness Slots (Thin, Medium, Thick)
      val lineHeights = listOf(2.dp, 3.8.dp, 6.dp)
      lineHeights.forEachIndexed { index, barHeight ->
        val isSelected = selectedThicknessIndex == index
        Box(
          modifier =
            Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(7.dp))
              .background(if (isSelected) Color.White.copy(alpha = 0.14f) else Color.Transparent)
              .clickable { onSelectThicknessIndex(index) }
              .testTag("thickness_slot_$index"),
          contentAlignment = Alignment.Center,
        ) {
          if (strokePattern == StrokePattern.SOLID) {
            Box(
              modifier =
                Modifier
                  .width(18.dp)
                  .height(barHeight)
                  .clip(RoundedCornerShape(3.dp))
                  .background(if (isSelected) Color.White else TextSecondaryGray),
            )
          } else {
            StrokePatternIcon(
              pattern = strokePattern,
              tint = if (isSelected) Color.White else TextSecondaryGray,
              modifier = Modifier.width(19.dp).height(13.dp),
            )
          }
        }
      }

      Box(
        modifier =
          Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(20.dp)
            .background(SubToolbarBorder),
      )

      // Quick Color Swatches + Color Studio Trigger
      quickSwatches.forEachIndexed { index, color ->
        val isSelected = selectedSwatchIndex == index
        Box(
          modifier =
            Modifier
              .size(30.dp)
              .clip(CircleShape)
              .clickable { onSelectQuickSwatch(index) }
              .testTag("quick_swatch_$index"),
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier =
              Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                  width = if (isSelected) 1.8.dp else 1.dp,
                  color = if (isSelected) Color.White else Color.White.copy(alpha = 0.25f),
                  shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
          ) {
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Open color palette",
                tint = if (color == Color.White) Color.Black else Color.White,
                modifier = Modifier.size(13.dp),
              )
            }
          }
        }
      }

      Box(
        modifier =
          Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(
              if (isColorPopoverOpen || isEyedropperActive) Color.White.copy(alpha = 0.16f)
              else Color.Transparent
            )
            .clickable(onClick = onOpenColorPicker)
            .testTag("subtoolbar_color_wheel_button"),
        contentAlignment = Alignment.Center,
      ) {
        if (isEyedropperActive) {
          Icon(
            imageVector = Icons.Outlined.Colorize,
            contentDescription = "Eyedropper active",
            tint = SubToolbarActiveCyan,
            modifier = Modifier.size(18.dp),
          )
        } else {
          DottedColorRingIcon(
            tint = ToolbarIconLight,
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun DockedLaserStrip(
  laserMode: LaserMode,
  laserColor: Color,
  onSelectLaserMode: (LaserMode) -> Unit,
  onSelectLaserColor: (Color) -> Unit,
) {
  val laserPalette =
    remember {
      listOf(
        Color(0xFFFF2D55),
        Color(0xFFFF9500),
        Color(0xFF30D158),
        Color(0xFF0A84FF),
      )
    }

  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(44.dp)
        .background(SubToolbarPillBg)
        .border(0.5.dp, SubToolbarBorder)
        .padding(horizontal = 12.dp)
        .testTag("floating_laser_subtoolbar"),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      LaserMode.entries.forEach { mode ->
        val isSelected = laserMode == mode
        Box(
          modifier =
            Modifier
              .height(30.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) SwitchBlue else PopoverInnerBg)
              .clickable { onSelectLaserMode(mode) }
              .padding(horizontal = 12.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = mode.label,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          )
        }
      }

      Box(
        modifier =
          Modifier
            .width(1.dp)
            .height(20.dp)
            .background(SubToolbarBorder),
      )

      laserPalette.forEach { swatch ->
        val isSelected = laserColor == swatch
        Box(
          modifier =
            Modifier
              .size(26.dp)
              .clip(CircleShape)
              .background(swatch)
              .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.25f),
                shape = CircleShape,
              )
              .clickable { onSelectLaserColor(swatch) },
        )
      }
    }
  }
}

/**
 * In-editor Add Page & Page Template Studio Popover:
 * Lets the user insert a new page (Before/After/End) with any of the 10 built-in templates or a custom template,
 * or change the current page's template without erasing existing strokes.
 */
@Composable
private fun AddPageAndTemplatePopover(
  currentTemplate: PaperTemplate,
  currentColorTheme: PaperColorTheme,
  customTemplates: List<CustomPageTemplate>,
  canDeleteCurrentPage: Boolean,
  onAddPageWithTemplate: (PaperTemplate, PaperColorTheme, CustomPageTemplate?, Int) -> Unit,
  onApplyTemplateToCurrentPage: (PaperTemplate, PaperColorTheme, CustomPageTemplate?) -> Unit,
  onDuplicateCurrentPage: () -> Unit,
  onDeleteCurrentPage: () -> Unit,
  onImportCustomTemplate: () -> Unit,
  onImportPdfDocument: () -> Unit,
  onDismiss: () -> Unit,
) {
  var selectedTemplate by remember { mutableStateOf(currentTemplate) }
  var selectedColorTheme by remember { mutableStateOf(currentColorTheme) }
  var selectedCustomTemplate by remember { mutableStateOf<CustomPageTemplate?>(null) }
  // 0 = After Current Page, 1 = Before Current Page, 2 = End of Document
  var insertPositionMode by remember { mutableIntStateOf(0) }

  val builtInTemplates =
    remember {
      PaperTemplate.entries.filter { it != PaperTemplate.CUSTOM }
    }

  Column(
    modifier =
      Modifier
        .widthIn(max = 390.dp)
        .fillMaxWidth(0.94f)
        .shadow(20.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("add_page_popover"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Add Page & Paper Template",
        color = TextPrimaryWhite,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
      )
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close",
        tint = TextSecondaryGray,
        modifier = Modifier.size(18.dp).clickable(onClick = onDismiss),
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Insert Position Selector: After | Before | Last Page
    Text(
      text = "INSERT POSITION",
      color = TextSecondaryGray,
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(5.dp))
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(32.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(PopoverInnerBg)
          .padding(2.dp),
    ) {
      listOf("After Page", "Before Page", "Last Page").forEachIndexed { idx, label ->
        val isSel = insertPositionMode == idx
        Box(
          modifier =
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSel) SwitchBlue else Color.Transparent)
              .clickable { insertPositionMode = idx },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Paper Color Theme Selector
    Text(
      text = "PAPER COLOR",
      color = TextSecondaryGray,
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(5.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      PaperColorTheme.entries.forEach { theme ->
        val isSel = theme == selectedColorTheme
        Row(
          modifier =
            Modifier
              .weight(1f)
              .height(30.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSel) SwitchBlue.copy(alpha = 0.25f) else PopoverInnerBg)
              .border(
                width = if (isSel) 1.5.dp else 1.dp,
                color = if (isSel) SwitchBlue else PopoverDivider,
                shape = RoundedCornerShape(8.dp),
              )
              .clickable { selectedColorTheme = theme }
              .padding(horizontal = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Box(
            modifier =
              Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(theme.paperBgColor)
                .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = theme.label.substringBefore(" "),
            color = TextPrimaryWhite,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Built-In & Custom Template Visual Gallery
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "TEMPLATE GALLERY",
        color = TextSecondaryGray,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = "+ Import Custom Template",
        color = Color(0xFF67C8EB),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier =
          Modifier
            .clickable(onClick = onImportCustomTemplate)
            .testTag("import_custom_template_button"),
      )
    }
    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      items(builtInTemplates) { tmpl ->
        val isSel = selectedTemplate == tmpl && selectedCustomTemplate == null
        Column(
          modifier =
            Modifier
              .width(74.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSel) SwitchBlue.copy(alpha = 0.22f) else PopoverInnerBg)
              .border(
                width = if (isSel) 2.dp else 1.dp,
                color = if (isSel) SwitchBlue else PopoverDivider,
                shape = RoundedCornerShape(8.dp),
              )
              .clickable {
                selectedTemplate = tmpl
                selectedCustomTemplate = null
              }
              .padding(6.dp)
              .testTag("editor_template_option_${tmpl.name.lowercase()}"),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          TemplatePreviewCanvas(
            template = tmpl,
            paperColorTheme = selectedColorTheme,
            modifier =
              Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(4.dp)),
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = tmpl.label,
            color = TextPrimaryWhite,
            fontSize = 9.5.sp,
            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      items(customTemplates) { customTmpl ->
        val isSel = selectedCustomTemplate?.id == customTmpl.id
        Column(
          modifier =
            Modifier
              .width(74.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSel) SwitchBlue.copy(alpha = 0.22f) else PopoverInnerBg)
              .border(
                width = if (isSel) 2.dp else 1.dp,
                color = if (isSel) SwitchBlue else PopoverDivider,
                shape = RoundedCornerShape(8.dp),
              )
              .clickable {
                selectedTemplate = PaperTemplate.CUSTOM
                selectedCustomTemplate = customTmpl
              }
              .padding(6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          TemplatePreviewCanvas(
            template = PaperTemplate.CUSTOM,
            paperColorTheme = selectedColorTheme,
            customBitmap = customTmpl.previewBitmap,
            modifier =
              Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(4.dp)),
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = customTmpl.name,
            color = Color(0xFF67C8EB),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Primary Actions: + Add Page | Apply to Current Page
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier =
          Modifier
            .weight(1f)
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SwitchBlue)
            .clickable {
              onAddPageWithTemplate(
                selectedTemplate,
                selectedColorTheme,
                selectedCustomTemplate,
                insertPositionMode,
              )
            }
            .testTag("confirm_add_page_with_template_button"),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "+ Add Page",
          color = Color.White,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold,
        )
      }

      Box(
        modifier =
          Modifier
            .weight(1f)
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(PopoverCardSelected)
            .clickable {
              onApplyTemplateToCurrentPage(
                selectedTemplate,
                selectedColorTheme,
                selectedCustomTemplate,
              )
            }
            .testTag("apply_template_current_page_button"),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "Apply to Current Page",
          color = TextPrimaryWhite,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = PopoverDivider)

    // Secondary Page Operations: Duplicate Page, Import PDF, Delete Page
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier =
          Modifier
            .weight(1f)
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PopoverInnerBg)
            .clickable(onClick = onDuplicateCurrentPage)
            .padding(horizontal = 6.dp)
            .testTag("duplicate_current_page_button"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = null,
          tint = TextPrimaryWhite,
          modifier = Modifier.size(13.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Duplicate", color = TextPrimaryWhite, fontSize = 11.sp)
      }

      Row(
        modifier =
          Modifier
            .weight(1f)
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PopoverInnerBg)
            .clickable(onClick = onImportPdfDocument)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.PictureAsPdf,
          contentDescription = null,
          tint = Color(0xFF67C8EB),
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Import PDF", color = Color(0xFF67C8EB), fontSize = 11.sp)
      }

      if (canDeleteCurrentPage) {
        Row(
          modifier =
            Modifier
              .weight(1f)
              .height(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(DangerRed.copy(alpha = 0.16f))
              .clickable(onClick = onDeleteCurrentPage)
              .padding(horizontal = 6.dp)
              .testTag("delete_current_page_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = null,
            tint = DangerRed,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text("Delete Page", color = DangerRed, fontSize = 11.sp)
        }
      }
    }
  }
}

/**
 * Minimap indicator for Infinite/Expansive Whiteboard mode (Stage 7).
 */
@Composable
private fun WhiteboardMinimapBadge(
  strokes: List<DrawnStroke>,
  canvasScale: Float,
  canvasPanOffset: Offset,
  onResetView: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .width(116.dp)
        .shadow(8.dp, RoundedCornerShape(10.dp))
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xDD1C1C1E))
        .border(1.dp, Color(0xFF34353A), RoundedCornerShape(10.dp))
        .clickable(onClick = onResetView)
        .padding(6.dp)
        .testTag("whiteboard_minimap"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(54.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF131519)),
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        strokes.forEach { s ->
          if (s.points.size >= 2) {
            val p0 = s.points.first()
            val p1 = s.points.last()
            drawLine(
              color = s.color.copy(alpha = 0.85f),
              start = Offset((p0.x / 1800f).coerceIn(0f, 1f) * w, (p0.y / 1400f).coerceIn(0f, 1f) * h),
              end = Offset((p1.x / 1800f).coerceIn(0f, 1f) * w, (p1.y / 1400f).coerceIn(0f, 1f) * h),
              strokeWidth = 2f,
            )
          }
        }
        // Viewport indicator box
        val vpW = (w / canvasScale.coerceAtLeast(0.5f)).coerceIn(14f, w)
        val vpH = (h / canvasScale.coerceAtLeast(0.5f)).coerceIn(10f, h)
        val vpX = ((w - vpW) * 0.5f - (canvasPanOffset.x / 40f)).coerceIn(0f, w - vpW)
        val vpY = ((h - vpH) * 0.5f - (canvasPanOffset.y / 40f)).coerceIn(0f, h - vpH)
        drawRect(
          color = Color(0xFF67C8EB),
          topLeft = Offset(vpX, vpY),
          size = androidx.compose.ui.geometry.Size(vpW, vpH),
          style = Stroke(width = 1.5f),
        )
      }
    }
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = "WHITEBOARD • ${(canvasScale * 100).roundToInt()}%",
      color = Color(0xFF67C8EB),
      fontSize = 8.5.sp,
      fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
private fun PageThumbnailsSidebar(
  document: NotebookDocument,
  onSelectPage: (Int) -> Unit,
  onAddPage: () -> Unit,
  onDuplicatePage: () -> Unit,
  onDeletePage: () -> Unit,
  onCloseSidebar: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .width(184.dp)
        .fillMaxHeight()
        .background(SidebarDarkBg)
        .padding(12.dp)
        .testTag("page_thumbnails_sidebar"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Pages (${document.pages.size})",
        color = TextPrimaryWhite,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close sidebar",
        tint = TextSecondaryGray,
        modifier = Modifier.size(17.dp).clickable(onClick = onCloseSidebar),
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      itemsIndexed(document.pages) { index, page ->
        val isCurrent = index == document.currentPageIndex
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(if (isCurrent) Color(0xFF17325A) else Color(0xFF222327))
              .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = if (isCurrent) SwitchBlue else Color(0xFF323338),
                shape = RoundedCornerShape(8.dp),
              )
              .clickable { onSelectPage(index) }
              .padding(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(86.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White),
          ) {
            if (page.pdfPageBitmap != null) {
              Image(
                bitmap = page.pdfPageBitmap.asImageBitmap(),
                contentDescription = "Page ${page.pageNumber}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
              )
            } else {
              TemplatePreviewCanvas(
                template = page.pageTemplate ?: document.paperTemplate,
                paperColorTheme = page.pageColorTheme ?: document.paperColorTheme,
                customBitmap = page.customTemplateBitmap,
                modifier = Modifier.fillMaxSize(),
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = if (page.isBookmarked) "Page ${index + 1} ★" else "Page ${index + 1}",
              color = TextPrimaryWhite,
              fontSize = 11.sp,
              fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            )
            if (isCurrent && document.pages.size > 1) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete Page",
                tint = DangerRed,
                modifier = Modifier.size(14.dp).clickable(onClick = onDeletePage),
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier =
          Modifier
            .weight(1f)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SwitchBlue)
            .clickable(onClick = onAddPage),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Page", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
      }

      Box(
        modifier =
          Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PopoverCardSelected)
            .clickable(onClick = onDuplicatePage),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = "Duplicate Current Page",
          tint = Color.White,
          modifier = Modifier.size(15.dp),
        )
      }
    }
  }
}

/**
 * Renders a stroke with pressure-sensitive variable-width segments for Fountain/Brush pens
 * and smooth quadratic curves for standard strokes.
 */
private fun DrawScope.drawRichStroke(
  stroke: DrawnStroke,
  isSelectedByLasso: Boolean,
) {
  if (stroke.points.size < 2) return

  val dashEffect =
    when (stroke.strokePattern) {
      StrokePattern.SOLID -> null
      StrokePattern.DASHED -> PathEffect.dashPathEffect(floatArrayOf(22f, 14f), 0f)
      StrokePattern.DOTTED -> PathEffect.dashPathEffect(floatArrayOf(5f, 13f), 0f)
    }

  val path =
    Path().apply {
      moveTo(stroke.points.first().x, stroke.points.first().y)
      if (stroke.isShape) {
        for (i in 1 until stroke.points.size) {
          lineTo(stroke.points[i].x, stroke.points[i].y)
        }
      } else {
        for (i in 1 until stroke.points.size) {
          val prev = stroke.points[i - 1]
          val curr = stroke.points[i]
          quadraticTo(prev.x, prev.y, (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
        }
      }
    }

  if (stroke.isShape && stroke.shapeFill && stroke.points.size >= 3) {
    drawPath(path = path, color = stroke.color.copy(alpha = 0.18f))
  }

  val isPencil = stroke.isPencil && !stroke.isHighlighter && !stroke.isShape
  val avgPressure =
    if (stroke.pressures.isNotEmpty()) stroke.pressures.average().toFloat() else 0.5f
  val dynamicWidth =
    when {
      stroke.isHighlighter || stroke.isShape -> stroke.strokeWidth
      isPencil -> stroke.strokeWidth * (0.85f + avgPressure * 0.30f)
      stroke.penStyle == PenStyle.FOUNTAIN -> stroke.strokeWidth * (0.75f + avgPressure * 0.55f)
      stroke.penStyle == PenStyle.BRUSH -> stroke.strokeWidth * (0.65f + avgPressure * 0.95f)
      else -> stroke.strokeWidth * (0.88f + avgPressure * 0.24f)
    }

  // Subtle selection glow around strokes currently enclosed by the Lasso tool
  if (isSelectedByLasso) {
    drawPath(
      path = path,
      color = Color(0x552196F3),
      style =
        Stroke(
          width = dynamicWidth + 8f,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
        ),
    )
  }

  if (stroke.isHighlighter) {
    // Render each highlighter stroke as a single unified translucent path so overlapping segments
    // within a stroke remain evenly translucent and overlapping strokes do not turn fully opaque.
    drawPath(
      path = path,
      color = stroke.color.copy(alpha = 0.36f),
      style =
        Stroke(
          width = dynamicWidth,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
        ),
    )
    return
  }

  if (isPencil) {
    // Lightweight natural pencil appearance without per-point bitmap allocation
    drawPath(
      path = path,
      color = stroke.color.copy(alpha = 0.72f),
      style =
        Stroke(
          width = dynamicWidth,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
          pathEffect = dashEffect,
        ),
    )
    drawPath(
      path = path,
      color = stroke.color.copy(alpha = 0.34f),
      style =
        Stroke(
          width = (dynamicWidth * 0.52f).coerceAtLeast(1f),
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
        ),
    )
    return
  }

  // Variable-width pressure-modulated rendering when per-point stylus pressures are present
  val useVariablePressure =
    !stroke.isShape &&
      stroke.strokePattern == StrokePattern.SOLID &&
      stroke.pressures.size == stroke.points.size &&
      (stroke.penStyle == PenStyle.FOUNTAIN || stroke.penStyle == PenStyle.BRUSH)

  if (useVariablePressure) {
    for (i in 1 until stroke.points.size) {
      val p0 = stroke.points[i - 1]
      val p1 = stroke.points[i]
      val pr = ((stroke.pressures[i - 1] + stroke.pressures[i]) * 0.5f).coerceIn(0.1f, 1.5f)
      val segWidth =
        when (stroke.penStyle) {
          PenStyle.FOUNTAIN -> stroke.strokeWidth * (0.55f + pr * 0.75f)
          PenStyle.BRUSH -> stroke.strokeWidth * (0.40f + pr * 1.15f)
          else -> stroke.strokeWidth
        }
      drawLine(
        color = stroke.color,
        start = p0,
        end = p1,
        strokeWidth = segWidth,
        cap = StrokeCap.Round,
      )
    }
  } else {
    drawPath(
      path = path,
      color = stroke.color,
      style =
        Stroke(
          width = dynamicWidth,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
          pathEffect = dashEffect,
        ),
    )
  }
}

private fun snapPointAlongRuler(
  start: Offset,
  current: Offset,
  angleDegrees: Float,
): Offset {
  val rad = Math.toRadians(angleDegrees.toDouble())
  val dirX = cos(rad).toFloat()
  val dirY = sin(rad).toFloat()
  val dx = current.x - start.x
  val dy = current.y - start.y
  val proj = dx * dirX + dy * dirY
  return Offset(start.x + proj * dirX, start.y + proj * dirY)
}
