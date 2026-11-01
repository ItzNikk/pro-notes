package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.OpenWith
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import com.example.util.StylusInputTracker
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.model.ElementStampType
import com.example.model.EraserMode
import com.example.model.LaserTrailStroke
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
import com.example.ui.components.AddPageDocumentIcon
import com.example.ui.components.AiHandwritingAssistPopover
import com.example.ui.components.AudioRecordingTimelineBar
import com.example.ui.components.DottedColorRingIcon
import com.example.ui.components.ElementsPickerPopover
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
  val isAudioRecording = uiState.isRecordingAudio
  val isAudioPlayingReplay = uiState.isPlayingAudioReplay
  val audioRecordedSeconds = uiState.recordedAudioSeconds
  val isAudioBarVisible = uiState.isAudioBarVisible
  val laserTrails = uiState.laserTrails
  val isEyedropperActive = uiState.isEyedropperActive
  val privacyScreenShieldEnabled = uiState.privacyScreenShieldEnabled
  val watermarkConfidentialExports = uiState.watermarkConfidentialExports
  val statusToastMessage = uiState.statusToastMessage
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
  val onInsertElementStamp: (ElementStampType) -> Unit = viewModel::insertElementStamp
  val onMoveElementStamp: (String, Offset) -> Unit = viewModel::moveElementStamp
  val onAddTextBoxAt: (Offset, String) -> Unit = viewModel::addTextBoxAt
  val onUpdateTextBox: (String, String) -> Unit = viewModel::updateTextBox
  val onMoveTextBox: (String, Offset) -> Unit = viewModel::moveTextBox
  val onMoveInsertedImage: (String, Offset) -> Unit = viewModel::moveCanvasImage
  val onResizeInsertedImage: (String, Float) -> Unit = viewModel::resizeCanvasImage
  val onDeleteInsertedImage: (String) -> Unit = viewModel::deleteCanvasImage
  val onAddStickyNoteAt: (Offset) -> Unit = viewModel::addStickyNoteAt
  val onToggleStickyNoteExpanded: (String) -> Unit = viewModel::toggleStickyNoteExpanded
  val onUpdateStickyNoteText: (String, String) -> Unit = viewModel::updateStickyNoteText
  val onDeleteStickyNote: (String) -> Unit = viewModel::deleteStickyNote
  val onToggleRuler: () -> Unit = viewModel::toggleRuler
  val onMoveRuler: (Offset) -> Unit = viewModel::moveRuler
  val onRotateRuler: (Float) -> Unit = viewModel::rotateRuler
  val onToggleZoomWindow: () -> Unit = viewModel::toggleZoomWindow
  val onAdvanceZoomBox: (Float, Float) -> Unit = viewModel::advanceZoomTargetBox
  val onToggleAudioRecording: () -> Unit = viewModel::toggleAudioRecording
  val onToggleAudioReplay: () -> Unit = viewModel::toggleAudioReplay
  val onToggleAudioBar: () -> Unit = viewModel::toggleAudioBar
  val onAddLaserTrail: (List<Offset>) -> Unit = viewModel::addLaserTrail
  val onPruneExpiredLaserTrails: () -> Unit = viewModel::pruneExpiredLaserTrails
  val onAddStroke: (List<Offset>, List<Float>, Boolean) -> Unit = viewModel::addStroke
  val onTriggerInstantCircleToLasso: (List<Offset>) -> Boolean = viewModel::triggerInstantCircleToLasso
  val onTryConvertCircleStrokeToLassoAtHold: (Offset) -> Boolean = viewModel::tryConvertDrawnCircleStrokeToLassoAtHold
  val onBeginEraseGesture: () -> Unit = viewModel::beginEraseGesture
  val onSelectLassoItemAt: (Offset) -> Unit = viewModel::selectLassoItemAt
  val onEraseAtPoints: (List<Offset>) -> List<DrawnStroke> = { pts -> viewModel.eraseAtPoints(pts) }
  val onEndEraseGesture: (Boolean) -> Unit = { temporary -> viewModel.endEraseGesture(temporary) }
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
  val onClearStatusToast: () -> Unit = viewModel::clearToast

  val proColors = LocalProNotesColors.current
  val isWhiteboard = activeDocument?.documentType == DocumentType.WHITEBOARD
  val currentPage = activeDocument?.currentPage ?: NotePage(pageNumber = 1)
  val density = LocalDensity.current.density
  LaunchedEffect(density) { onSetCanvasDensity(density) }

  val inProgressPoints = remember { mutableStateListOf<Offset>() }
  val inProgressPressures = remember { mutableStateListOf<Float>() }
  var isDrawingSnappedHold by remember { mutableStateOf(false) }
  var circleLassoHoldProgress by remember { mutableFloatStateOf(0f) }
  var circleLassoHoldCenter by remember { mutableStateOf(Offset.Zero) }

  // Erase feedback: cursor ring, ripples where ink was removed, and ink that fades out.
  // eraserActive flips twice per gesture; the cursor position is only read while drawing.
  var eraserActive by remember { mutableStateOf(false) }
  var eraserCursorPos by remember { mutableStateOf(Offset.Zero) }
  val erasedGhosts = remember { mutableStateListOf<ErasedInkGhost>() }
  val eraseRipples = remember { mutableStateListOf<EraseRipple>() }
  var effectsClockNanos by remember { mutableLongStateOf(0L) }
  val haptic = LocalHapticFeedback.current
  val viewConfiguration = LocalViewConfiguration.current

  LaunchedEffect(erasedGhosts.size, eraseRipples.size) {
    while (erasedGhosts.isNotEmpty() || eraseRipples.isNotEmpty()) {
      withFrameNanos { effectsClockNanos = it }
      val now = System.nanoTime()
      erasedGhosts.removeAll { now - it.startNanos > ERASE_FADE_NANOS }
      eraseRipples.removeAll { now - it.startNanos > ERASE_FADE_NANOS }
    }
  }

  // Interactive Multi-Touch Pinch-to-Zoom & 2-Finger Pan State
  var canvasScale by remember(activeTabId) { mutableFloatStateOf(1f) }
  var canvasPanOffset by remember(activeTabId) { mutableStateOf(Offset.Zero) }

  // Raw stylus state (side button, eraser tail, hover) fed from the Activity's MotionEvent stream.
  val hardwareStylusState = StylusInputTracker

  // Prune laser trails periodically
  LaunchedEffect(laserTrails.isNotEmpty()) {
    // Animate the fade and drop expired trails (the old code pruned only once)
    while (laserTrails.isNotEmpty()) {
      withFrameNanos { effectsClockNanos = it }
      onPruneExpiredLaserTrails()
    }
  }

  // Audio recording timer
  LaunchedEffect(isAudioRecording) {
    while (isAudioRecording) {
      delay(1000L)
    }
  }

  // Auto-clear toast message
  LaunchedEffect(statusToastMessage) {
    if (statusToastMessage != null) {
      delay(2200L)
      onClearStatusToast()
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
          onSelectPrimaryTool = { tool ->
            if (tool == PrimaryTool.IMAGE) {
              onSelectPrimaryTool(tool)
              onImportImageFromDevice()
            } else {
              onSelectPrimaryTool(tool)
            }
          },
          onTogglePageSidebar = onTogglePageSidebar,
          onToggleBookmark = onTogglePageBookmark,
          onToggleRuler = onToggleRuler,
          onToggleZoomWindow = onToggleZoomWindow,
          onToggleAudioBar = onToggleAudioBar,
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

      // 3. DOCKED CONTEXTUAL SECONDARY TOOL STRIP (Pen styles, thicknesses, color swatches)
      if (!isReadOnlyMode && primaryTool != PrimaryTool.LASSO) {
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

      // 3b. DOCKED SHAPE STUDIO STRIP (Visible when Shape tool or Draw Shape sub-tool is active)
      if (
        !isReadOnlyMode &&
          (primaryTool == PrimaryTool.SHAPE ||
            (primaryTool == PrimaryTool.WRITING_TOOLS &&
              activeWritingSubTool == WritingSubTool.DRAW_SHAPE))
      ) {
        DockedShapeStudioStrip(
          selectedShapeKind = uiState.selectedShapeKind,
          shapeAutoDetectMode = uiState.shapeAutoDetectMode,
          fillShapes = fillShapes,
          onToggleAutoDetect = viewModel::setShapeAutoDetectMode,
          onSelectShapeForDraw = viewModel::selectShapeForDirectDraw,
          onToggleFillShapes = { onToggleFillShapes(!fillShapes) },
          onStampShape = { onInsertQuickShape(uiState.selectedShapeKind) },
        )
      }

      // 4. OPTIONAL DOCKED AUDIO RECORDING TIMELINE BAR
      if (isAudioBarVisible) {
        AudioRecordingTimelineBar(
          isRecording = isAudioRecording,
          isPlayingReplay = isAudioPlayingReplay,
          recordedSeconds = audioRecordedSeconds,
          strokeCount = currentPage.strokes.size,
          onToggleRecord = onToggleAudioRecording,
          onToggleReplay = onToggleAudioReplay,
          onCloseAudioBar = onToggleAudioBar,
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

                val primaryToolRef = rememberUpdatedState(primaryTool)
                val activeSubToolRef = rememberUpdatedState(activeWritingSubTool)
                val eraserRadiusRef = rememberUpdatedState(eraserRadius)
                val strokeStabilizationRef = rememberUpdatedState(strokeStabilization)
                val highlighterStraightRef = rememberUpdatedState(highlighterStraightLine)
                val drawAndHoldRef = rememberUpdatedState(drawAndHold)
                val holdToEraseRef = rememberUpdatedState(holdToErase)
                val circleToLassoRef = rememberUpdatedState(circleToLasso)
                val barrelButtonActiveRef = rememberUpdatedState(isSPenBarrelButtonActive)
                val barrelActionRef = rememberUpdatedState(uiState.stylusBarrelAction)
                val pressureSensRef = rememberUpdatedState(uiState.stylusPressureSensitivity)
                val tailEraserEnabledRef = rememberUpdatedState(uiState.stylusTailEraserEnabled)
                val onlyStylusRef = rememberUpdatedState(onlyDrawWithStylus)
                val palmSensRef = rememberUpdatedState(palmRejectionSensitivity)
                val leftHandedRef = rememberUpdatedState(leftHandedPosture)
                val rulerStateRef = rememberUpdatedState(rulerState)
                val showLassoRef = rememberUpdatedState(showLassoSelection)
                val eyedropperRef = rememberUpdatedState(isEyedropperActive)

                // 4) Interactive Ink, Shape, Lasso, Eyedropper, S Pen & Multi-Touch Zoom Canvas
                Canvas(
                  modifier =
                    Modifier
                      .fillMaxSize()
                      .testTag(if (isCurrentActivePage) "note_drawing_canvas" else "note_page_canvas_$pageIndex")
                      .pointerInput(
                        isCurrentActivePage,
                        isReadOnlyMode,
                        isWhiteboard,
                        currentPageIdx,
                        pages.size,
                      ) {
                        if (!isCurrentActivePage) return@pointerInput
                        awaitEachGesture {
                          val currentPrimaryTool = primaryToolRef.value
                          val currentSubTool = activeSubToolRef.value
                          val currentEraserRadius = eraserRadiusRef.value
                          val currentStabilization = strokeStabilizationRef.value
                          val currentHighlighterStraight = highlighterStraightRef.value
                          val currentDrawAndHold = drawAndHoldRef.value
                          val currentHoldToErase = holdToEraseRef.value
                          val currentCircleToLasso = circleToLassoRef.value
                          val currentBarrelButtonActive = barrelButtonActiveRef.value
                          val currentBarrelAction = barrelActionRef.value
                          val currentPressureSens = pressureSensRef.value
                          val currentTailEraserEnabled = tailEraserEnabledRef.value
                          val currentOnlyStylus = onlyStylusRef.value
                          val currentPalmSens = palmSensRef.value
                          val currentLeftHanded = leftHandedRef.value
                          val currentRuler = rulerStateRef.value
                          val currentShowLasso = showLassoRef.value
                          val currentEyedropper = eyedropperRef.value

                          var activeDown = awaitFirstDown(requireUnconsumed = false)
                          // Stylus, eraser tail and mouse are precision pointers; only fingers are "touch".
                          var isStylusDown =
                            activeDown.type == PointerType.Stylus ||
                              activeDown.type == PointerType.Eraser ||
                              activeDown.type == PointerType.Mouse
                          var isHardwareEraserTail =
                            currentTailEraserEnabled &&
                              (activeDown.type == PointerType.Eraser ||
                                (isStylusDown && hardwareStylusState.isEraserTail))

                          // Edge Palm-Guard Zone when drawing with Touch
                          val canvasWidthPx = size.width.toFloat()
                          val edgeGuardWidth = (currentPalmSens / 100f) * 58f
                          val inEdgeGuard =
                            edgeGuardWidth > 12f &&
                              if (currentLeftHanded) {
                                activeDown.position.x < edgeGuardWidth
                              } else {
                                activeDown.position.x > (canvasWidthPx - edgeGuardWidth)
                              }
                          // A finger landing while the pen hovers over the screen is a resting palm.
                          val inPalmZone =
                            !isStylusDown &&
                              !currentOnlyStylus &&
                              (inEdgeGuard || hardwareStylusState.isStylusNear())

                          if (inPalmZone || isReadOnlyMode || (currentOnlyStylus && !isStylusDown)) {
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
                                currentTailEraserEnabled &&
                                  (promotedStylusChange.type == PointerType.Eraser ||
                                    hardwareStylusState.isEraserTail)
                            } else {
                              return@awaitEachGesture
                            }
                          }

                          // Consume down IMMEDIATELY so HorizontalPager/VerticalPager never steals a stylus stroke
                          activeDown.consume()
                          val activePointerId = activeDown.id

                          // Eyedropper color sampling
                          if (currentEyedropper) {
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

                          // Shared by the Eraser tool and the stylus side-button / eraser-tail erase:
                          // one undo step, no gaps on fast strokes, animated feedback.
                          suspend fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.runEraseGesture(
                            temporaryEraser: Boolean
                          ) {
                            onBeginEraseGesture()
                            eraserActive = true
                            var lastErased = activeDown.position
                            eraserCursorPos = lastErased

                            fun eraseSamples(samples: List<Offset>, tip: Offset) {
                              val stepPx = (currentEraserRadius * 0.5f).coerceAtLeast(3f)
                              val dense = ArrayList<Offset>()
                              var prev = lastErased
                              for (p in samples) {
                                val steps = max(1, ((p - prev).getDistance() / stepPx).toInt())
                                for (k in 1..steps) {
                                  dense.add(prev + (p - prev) * (k / steps.toFloat()))
                                }
                                prev = p
                              }
                              lastErased = tip
                              eraserCursorPos = tip
                              val erased = onEraseAtPoints(dense)
                              if (erased.isNotEmpty()) {
                                val now = System.nanoTime()
                                erased.forEach { erasedGhosts.add(ErasedInkGhost(it, now)) }
                                eraseRipples.add(EraseRipple(tip, now))
                                if (eraseRipples.size > 6) eraseRipples.removeAt(0)
                              }
                            }

                            try {
                              eraseSamples(listOf(activeDown.position), activeDown.position)
                              while (true) {
                                val event = awaitPointerEvent()
                                if (!isStylusDown && event.changes.count { it.pressed } >= 2) {
                                  runTwoFingerZoomLoop()
                                  break
                                }
                                val change = resolveActiveChange(event) ?: break
                                event.changes.forEach { it.consume() }
                                if (!change.pressed) break
                                if (
                                  temporaryEraser &&
                                    !hardwareStylusState.buttonDown &&
                                    !hardwareStylusState.isEraserTail &&
                                    !currentBarrelButtonActive
                                ) {
                                  break
                                }
                                val samples = ArrayList<Offset>()
                                change.historical.forEach { samples.add(it.position) }
                                samples.add(change.position)
                                eraseSamples(samples, change.position)
                              }
                            } finally {
                              eraserActive = false
                              onEndEraseGesture(temporaryEraser)
                            }
                          }

                          val effectiveTool =
                            if (
                              hardwareStylusState.buttonDown &&
                                currentBarrelAction == com.example.model.StylusBarrelAction.LASER_WHILE_HELD
                            ) {
                              PrimaryTool.LASER
                            } else if (
                              hardwareStylusState.buttonDown &&
                                currentBarrelAction == com.example.model.StylusBarrelAction.LASSO_WHILE_HELD
                            ) {
                              PrimaryTool.LASSO
                            } else {
                              currentPrimaryTool
                            }

                          when (effectiveTool) {
                            PrimaryTool.TEXT -> {
                              onAddTextBoxAt(activeDown.position, "Double-tap to edit note")
                            }

                            PrimaryTool.STICKY_NOTE -> {
                              onAddStickyNoteAt(activeDown.position)
                            }

                            PrimaryTool.ELEMENTS -> {
                              onInsertElementStamp(ElementStampType.QUESTION_FLAG)
                            }

                            PrimaryTool.IMAGE -> {
                              onImportImageFromDevice()
                            }

                            PrimaryTool.ERASER -> {
                              runEraseGesture(temporaryEraser = false)
                            }

                            PrimaryTool.LASER -> {
                              inProgressPoints.clear()
                              inProgressPoints.add(activeDown.position)
                              while (true) {
                                val event = awaitPointerEvent()
                                if (!isStylusDown && event.changes.count { it.pressed } >= 2) {
                                  inProgressPoints.clear()
                                  runTwoFingerZoomLoop()
                                  break
                                }
                                val change = resolveActiveChange(event) ?: break
                                event.changes.forEach { it.consume() }
                                if (!change.pressed) break
                                change.historical.forEach { inProgressPoints.add(it.position) }
                                inProgressPoints.add(change.position)
                              }
                              if (inProgressPoints.size >= 2) {
                                onAddLaserTrail(inProgressPoints.toList())
                              }
                              inProgressPoints.clear()
                            }

                            PrimaryTool.LASSO -> {
                              val activeShape = shapeState.value
                              val isInsideSelection =
                                currentShowLasso &&
                                  activeShape != null &&
                                  (activeShape.contains(activeDown.position) ||
                                    activeShape.distanceToEdge(activeDown.position) < 28f)

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
                                inProgressPoints.clear()
                                inProgressPoints.add(activeDown.position)
                                var switchedToZoom = false
                                while (true) {
                                  val event = awaitPointerEvent()
                                  if (!isStylusDown && event.changes.count { it.pressed } >= 2) {
                                    inProgressPoints.clear()
                                    switchedToZoom = true
                                    runTwoFingerZoomLoop()
                                    break
                                  }
                                  val change = resolveActiveChange(event) ?: break
                                  event.changes.forEach { it.consume() }
                                  if (!change.pressed) break
                                  change.historical.forEach { inProgressPoints.add(it.position) }
                                  inProgressPoints.add(change.position)
                                }

                                if (!switchedToZoom) {
                                  val dragDist =
                                    (inProgressPoints.last() - activeDown.position).getDistance()
                                  val isTap = inProgressPoints.size < 5 || dragDist < 18f
                                  if (isTap) {
                                    if (currentShowLasso) onClearLassoSelection()
                                    else onSelectLassoItemAt(activeDown.position)
                                  } else if (inProgressPoints.size >= 3) {
                                    onAddStroke(inProgressPoints.toList(), emptyList(), false)
                                  }
                                }
                                inProgressPoints.clear()
                              }
                            }

                            PrimaryTool.WRITING_TOOLS,
                            PrimaryTool.SHAPE -> {
                              val sPenButtonEraseTriggered =
                                isHardwareEraserTail ||
                                  currentBarrelButtonActive ||
                                  (currentHoldToErase &&
                                    currentBarrelAction == com.example.model.StylusBarrelAction.ERASE_WHILE_HELD &&
                                    hardwareStylusState.buttonDown)

                              if (sPenButtonEraseTriggered) {
                                runEraseGesture(temporaryEraser = true)
                                return@awaitEachGesture
                              }

                              inProgressPoints.clear()
                              inProgressPressures.clear()
                              isDrawingSnappedHold = false
                              circleLassoHoldProgress = 0f

                              val startPoint = activeDown.position
                              val startPressure =
                                (activeDown.pressure * currentPressureSens).coerceIn(0.15f, 1.5f)
                              inProgressPoints.add(startPoint)
                              inProgressPressures.add(startPressure)

                              var lastMovePoint = startPoint
                              var lastMoveTimeMs = System.currentTimeMillis()
                              var lastPressure = startPressure
                              var circleLassoHoldTriggered = false
                              var switchedToTwoFingerZoom = false
                              var loopCandidate = false
                              var switchToErase = false

                              val isHighlighterSub =
                                currentPrimaryTool == PrimaryTool.WRITING_TOOLS &&
                                  currentSubTool == WritingSubTool.HIGHLIGHTER
                              val isPenInk =
                                currentPrimaryTool == PrimaryTool.WRITING_TOOLS &&
                                  (currentSubTool == WritingSubTool.PEN ||
                                    currentSubTool == WritingSubTool.PENCIL)
                              val isExplicitShapeTool =
                                currentPrimaryTool == PrimaryTool.SHAPE ||
                                  (currentPrimaryTool == PrimaryTool.WRITING_TOOLS &&
                                    currentSubTool == WritingSubTool.DRAW_SHAPE)
                              // Pen jitter below this never counts as "moving" for hold detection
                              val holdSlop = max(viewConfiguration.touchSlop * 0.6f, 6f)

                              // Adds one raw stylus sample (position + pressure) to the live stroke.
                              fun appendInkSample(rawPos: Offset, pressureVal: Float, nowMs: Long) {
                                if ((rawPos - lastMovePoint).getDistance() > holdSlop) {
                                  lastMovePoint = rawPos
                                  lastMoveTimeMs = nowMs
                                  circleLassoHoldProgress = 0f
                                }
                                val constrainedPos =
                                  when {
                                    currentRuler.isVisible -> {
                                      snapPointAlongRuler(
                                        start = startPoint,
                                        current = rawPos,
                                        angleDegrees = currentRuler.angleDegrees,
                                      )
                                    }
                                    isHighlighterSub && currentHighlighterStraight -> {
                                      Offset(rawPos.x, startPoint.y)
                                    }
                                    else -> rawPos
                                  }
                                val stabilizedPos =
                                  if (currentStabilization > 0f && inProgressPoints.isNotEmpty() && !isExplicitShapeTool) {
                                    val lastPt = inProgressPoints.last()
                                    val blend = (currentStabilization / 100f).coerceIn(0f, 0.58f)
                                    Offset(
                                      lastPt.x * blend + constrainedPos.x * (1f - blend),
                                      lastPt.y * blend + constrainedPos.y * (1f - blend),
                                    )
                                  } else {
                                    constrainedPos
                                  }
                                val minPointDist = if (isStylusDown) 0.85f else 1.35f
                                if ((stabilizedPos - inProgressPoints.last()).getDistance() > minPointDist) {
                                  inProgressPoints.add(stabilizedPos)
                                  inProgressPressures.add(pressureVal)
                                }
                              }

                              val holdWatcher =
                                pagerScope.launch {
                                  var candidateEpoch = -1L
                                  var lassoAttemptEpoch = -1L
                                  while (true) {
                                    delay(35L)
                                    val totalDisplacement =
                                      (lastMovePoint - startPoint).getDistance()
                                    val stationaryMs = System.currentTimeMillis() - lastMoveTimeMs

                                    // Circle to Lasso, GoodNotes style: draw a closed loop and keep
                                    // the pen resting at its end, and the loop becomes a lasso.
                                    if (currentCircleToLasso && isPenInk && !circleLassoHoldTriggered) {
                                      if (inProgressPoints.size <= 14) {
                                        if (stationaryMs >= 120L) {
                                          circleLassoHoldCenter = lastMovePoint
                                          circleLassoHoldProgress =
                                            ((stationaryMs - 120f) / (CIRCLE_LASSO_HOLD_MS - 120f))
                                              .coerceIn(0f, 1f)
                                        }
                                        // Pen came back down on the end of the loop it just drew.
                                        if (
                                          stationaryMs >= CIRCLE_LASSO_HOLD_MS &&
                                            lassoAttemptEpoch != lastMoveTimeMs
                                        ) {
                                          lassoAttemptEpoch = lastMoveTimeMs
                                          if (onTryConvertCircleStrokeToLassoAtHold(lastMovePoint)) {
                                            circleLassoHoldTriggered = true
                                            circleLassoHoldProgress = 0f
                                            inProgressPoints.clear()
                                            inProgressPressures.clear()
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            break
                                          } else {
                                            circleLassoHoldProgress = 0f
                                          }
                                        }
                                      } else {
                                        if (stationaryMs >= 150L && candidateEpoch != lastMoveTimeMs) {
                                          candidateEpoch = lastMoveTimeMs
                                          loopCandidate = isClosedCircleLoop(inProgressPoints)
                                        }
                                        if (loopCandidate && stationaryMs >= 150L) {
                                          circleLassoHoldCenter = lastMovePoint
                                          circleLassoHoldProgress =
                                            ((stationaryMs - 150f) / (CIRCLE_LASSO_HOLD_MS - 150f))
                                              .coerceIn(0f, 1f)
                                        }
                                        if (
                                          loopCandidate &&
                                            stationaryMs >= CIRCLE_LASSO_HOLD_MS &&
                                            lassoAttemptEpoch != lastMoveTimeMs
                                        ) {
                                          lassoAttemptEpoch = lastMoveTimeMs
                                          if (onTriggerInstantCircleToLasso(inProgressPoints.toList())) {
                                            circleLassoHoldTriggered = true
                                            circleLassoHoldProgress = 0f
                                            inProgressPoints.clear()
                                            inProgressPressures.clear()
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            break
                                          }
                                          // Nothing inside the loop: let Draw-and-Hold handle it.
                                          loopCandidate = false
                                          circleLassoHoldProgress = 0f
                                        }
                                      }
                                    }

                                    if (
                                      currentDrawAndHold &&
                                        !isDrawingSnappedHold &&
                                        !circleLassoHoldTriggered &&
                                        !loopCandidate &&
                                        totalDisplacement > 28f &&
                                        stationaryMs > 380L
                                    ) {
                                      isDrawingSnappedHold = true
                                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                  }
                                }

                              try {
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
                                    circleLassoHoldProgress = 0f
                                    switchedToTwoFingerZoom = true
                                    runTwoFingerZoomLoop()
                                    break
                                  }

                                  val change = resolveActiveChange(event) ?: break
                                  event.changes.forEach { it.consume() }
                                  if (!change.pressed) break

                                  // Some pens only report the side button after touchdown
                                  if (
                                    currentHoldToErase &&
                                      currentBarrelAction == com.example.model.StylusBarrelAction.ERASE_WHILE_HELD &&
                                      hardwareStylusState.buttonDown
                                  ) {
                                    switchToErase = true
                                    break
                                  }

                                  if (circleLassoHoldTriggered) continue

                                  val nowMs = System.currentTimeMillis()
                                  val endPressure =
                                    (change.pressure * currentPressureSens).coerceIn(0.15f, 1.5f)

                                  val history = change.historical
                                  if (history.isNotEmpty()) {
                                    val n = history.size
                                    for (h in 0 until n) {
                                      val t = (h + 1f) / (n + 1f)
                                      appendInkSample(
                                        history[h].position,
                                        lastPressure + (endPressure - lastPressure) * t,
                                        nowMs,
                                      )
                                    }
                                  }
                                  appendInkSample(change.position, endPressure, nowMs)
                                  lastPressure = endPressure
                                }
                              } finally {
                                holdWatcher.cancel()
                                circleLassoHoldProgress = 0f
                              }
                              if (switchToErase) {
                                inProgressPoints.clear()
                                inProgressPressures.clear()
                                isDrawingSnappedHold = false
                                runEraseGesture(temporaryEraser = true)
                                return@awaitEachGesture
                              }

                              if (!switchedToTwoFingerZoom && !circleLassoHoldTriggered && inProgressPoints.isNotEmpty()) {
                                val totalDragDist =
                                  (inProgressPoints.last() - startPoint).getDistance()
                                if (isExplicitShapeTool && totalDragDist < 14f && inProgressPoints.size <= 5) {
                                  // Tapping on an existing shape in Shape Mode selects it for vertex/move editing
                                  viewModel.selectShapeAtPoint(startPoint)
                                } else {
                                  // Support single-tap stylus dots / periods
                                  if (inProgressPoints.size == 1) {
                                    inProgressPoints.add(startPoint + Offset(0.8f, 0.8f))
                                    inProgressPressures.add(startPressure)
                                  }
                                  onAddStroke(
                                    inProgressPoints.toList(),
                                    inProgressPressures.toList(),
                                    isDrawingSnappedHold,
                                  )
                                }
                              }
                              inProgressPoints.clear()
                              inProgressPressures.clear()
                              isDrawingSnappedHold = false
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

                  // Laser Pointer Trails
                  if (isCurrentActivePage) {
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

                // 4b) Overlay layer: live ink, lasso outline and erase feedback. It is a separate canvas
                // so each pen sample only redraws this light layer instead of every stroke on the page.
                val lassoOutlineVisible =
                  isCurrentActivePage &&
                    showLassoSelection &&
                    primaryTool == PrimaryTool.LASSO &&
                    lassoLoopPoints.size >= 3
                val lassoDashPhase: State<Float> =
                  rememberInfiniteTransition(label = "lassoDash")
                    .animateFloat(
                      initialValue = 0f,
                      targetValue = 24f,
                      animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
                      label = "lassoDashPhase",
                    )
                val eraserCursorAlpha =
                  animateFloatAsState(
                    targetValue = if (eraserActive) 1f else 0f,
                    animationSpec = tween(150),
                    label = "eraserCursorAlpha",
                  )
                val lassoSelectionTransitionAlpha =
                  animateFloatAsState(
                    targetValue = if (lassoOutlineVisible) 1f else 0f,
                    animationSpec = tween(220),
                    label = "lassoSelectionTransitionAlpha",
                  )
                Canvas(modifier = Modifier.fillMaxSize()) {
                  if (!isCurrentActivePage) return@Canvas

                  // Live ink while the pen is down
                  if (inProgressPoints.size >= 2 && primaryTool != PrimaryTool.LASSO && primaryTool != PrimaryTool.LASER) {
                    val isExplicitShape =
                      primaryTool == PrimaryTool.SHAPE ||
                        (primaryTool == PrimaryTool.WRITING_TOOLS &&
                          activeWritingSubTool == WritingSubTool.DRAW_SHAPE)
                    val liveIsShapeOrSnap = isExplicitShape || isDrawingSnappedHold
                    val isHigh =
                      primaryTool == PrimaryTool.WRITING_TOOLS &&
                        activeWritingSubTool == WritingSubTool.HIGHLIGHTER
                    val isPencilTool =
                      primaryTool == PrimaryTool.WRITING_TOOLS &&
                        activeWritingSubTool == WritingSubTool.PENCIL
                    val isTape =
                      primaryTool == PrimaryTool.WRITING_TOOLS &&
                        activeWritingSubTool == WritingSubTool.TAPE
                    val baseW = thicknessPresets.getOrElse(selectedThicknessIndex) { 6.5f }
                    val liveW =
                      when {
                        isHigh -> baseW * 3.4f
                        isTape -> baseW * 3.6f
                        isPencilTool -> baseW * 0.9f
                        else -> baseW
                      }
                    val liveColor =
                      when {
                        isHigh -> selectedColor.copy(alpha = 0.38f)
                        isTape -> Color(0xFFFFAB40).copy(alpha = 0.85f)
                        isPencilTool -> selectedColor.copy(alpha = 0.78f)
                        else -> selectedColor
                      }
                    if (isTape) {
                      val linePath =
                        Path().apply {
                          moveTo(inProgressPoints.first().x, inProgressPoints.first().y)
                          lineTo(inProgressPoints.last().x, inProgressPoints.last().y)
                        }
                      drawPath(
                        path = linePath,
                        color = liveColor,
                        style = Stroke(width = liveW, cap = StrokeCap.Round, join = StrokeJoin.Round),
                      )
                    } else if (liveIsShapeOrSnap && !isHigh) {
                      val previewPtsSnapshot = inProgressPoints.toList()
                      val previewKind =
                        if (isExplicitShape && !uiState.shapeAutoDetectMode) {
                          uiState.selectedShapeKind
                        } else {
                          viewModel.autoDetectShapeKind(previewPtsSnapshot)
                        }
                      val (geomPts, previewHandles) =
                        com.example.model.buildShapePreviewPoints(previewPtsSnapshot, previewKind)
                      val shouldFill =
                        fillShapes &&
                          previewKind != ShapeKind.LINE &&
                          previewKind != ShapeKind.ARROW &&
                          previewKind != ShapeKind.CURVE
                      drawRichStroke(
                        stroke =
                          DrawnStroke(
                            points = geomPts,
                            color = liveColor,
                            strokeWidth = liveW,
                            penStyle = penStyle,
                            strokePattern = uiState.strokePattern,
                            isShape = true,
                            shapeKind = previewKind,
                            shapeFill = shouldFill,
                          ),
                        isSelectedByLasso = false,
                      )
                      previewHandles.forEach { hPt ->
                        drawCircle(
                          color = Color.White,
                          radius = 6.5f,
                          center = hPt,
                        )
                        drawCircle(
                          color = SwitchBlue,
                          radius = 6.5f,
                          center = hPt,
                          style = Stroke(width = 2.2f),
                        )
                      }
                    } else {
                      drawRichStroke(
                        stroke =
                          DrawnStroke(
                            points = inProgressPoints.toList(),
                            pressures = inProgressPressures.toList(),
                            color = liveColor,
                            strokeWidth = liveW,
                            penStyle = penStyle,
                            strokePattern = uiState.strokePattern,
                            isHighlighter = isHigh,
                            isPencil = isPencilTool,
                          ),
                        isSelectedByLasso = false,
                      )
                      // Circle-to-Lasso closing loop & hold progress indicator
                      if (circleLassoHoldProgress > 0.04f) {
                        val prog = circleLassoHoldProgress.coerceIn(0f, 1f)
                        val firstPt = inProgressPoints.first()
                        val lastPt = inProgressPoints.last()
                        drawLine(
                          color = SwitchBlue.copy(alpha = 0.55f * prog),
                          start = lastPt,
                          end = firstPt,
                          strokeWidth = 3f,
                          pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                        )
                        drawCircle(
                          color = SwitchBlue.copy(alpha = 0.18f * prog),
                          radius = 26f * (0.65f + 0.35f * prog),
                          center = circleLassoHoldCenter,
                        )
                        drawArc(
                          color = SwitchBlue,
                          startAngle = -90f,
                          sweepAngle = 360f * prog,
                          useCenter = false,
                          topLeft = Offset(circleLassoHoldCenter.x - 22f, circleLassoHoldCenter.y - 22f),
                          size = androidx.compose.ui.geometry.Size(44f, 44f),
                          style = Stroke(width = 3.5f, cap = StrokeCap.Round),
                        )
                      }
                    }
                  }

                  // Laser pointer: smooth glowing trail using the user's selected color, without any end circle/dot
                  if (effectsClockNanos >= 0L) {
                    val nowMs = System.currentTimeMillis()
                    fun laserPath(pts: List<Offset>) =
                      Path().apply {
                        moveTo(pts.first().x, pts.first().y)
                        for (i in 1 until pts.size) {
                          val a = pts[i - 1]
                          val b = pts[i]
                          quadraticTo(a.x, a.y, (a.x + b.x) / 2f, (a.y + b.y) / 2f)
                        }
                        lineTo(pts.last().x, pts.last().y)
                      }
                    fun drawLaser(pts: List<Offset>, laserColor: Color, alpha: Float) {
                      if (pts.size < 2 || alpha <= 0.01f) return
                      val path = laserPath(pts)
                      drawPath(
                        path = path,
                        color = laserColor.copy(alpha = 0.24f * alpha),
                        style = Stroke(width = 22f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                      )
                      drawPath(
                        path = path,
                        color = laserColor.copy(alpha = 0.62f * alpha),
                        style = Stroke(width = 11f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                      )
                      drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.92f * alpha),
                        style = Stroke(width = 3.8f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                      )
                    }
                    laserTrails.forEach { trail ->
                      val age = (nowMs - trail.createdAtMs).toFloat()
                      drawLaser(
                        pts = trail.points,
                        laserColor = trail.color,
                        alpha = (1f - (age - 1500f) / 700f).coerceIn(0f, 1f),
                      )
                    }
                    if (primaryTool == PrimaryTool.LASER && inProgressPoints.size >= 2) {
                      drawLaser(
                        pts = inProgressPoints.toList(),
                        laserColor = selectedColor,
                        alpha = 1f,
                      )
                    }
                  }

                  // Freehand lasso loop while dragging (smooth curve + closing preview chord)
                  if (primaryTool == PrimaryTool.LASSO && inProgressPoints.size >= 2) {
                    val lassoPath =
                      Path().apply {
                        moveTo(inProgressPoints.first().x, inProgressPoints.first().y)
                        for (i in 1 until inProgressPoints.size) {
                          val prev = inProgressPoints[i - 1]
                          val curr = inProgressPoints[i]
                          quadraticTo(prev.x, prev.y, (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
                        }
                        lineTo(inProgressPoints.last().x, inProgressPoints.last().y)
                      }
                    val closedPreviewPath =
                      Path().apply {
                        addPath(lassoPath)
                        close()
                      }
                    drawPath(
                      path = closedPreviewPath,
                      color = SwitchBlue.copy(alpha = 0.06f),
                    )
                    drawLine(
                      color = SwitchBlue.copy(alpha = 0.35f),
                      start = inProgressPoints.last(),
                      end = inProgressPoints.first(),
                      strokeWidth = 2f,
                      pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
                    )
                    drawPath(
                      path = lassoPath,
                      color = SwitchBlue,
                      style =
                        Stroke(
                          width = 3.5f,
                          cap = StrokeCap.Round,
                          join = StrokeJoin.Round,
                          pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), lassoDashPhase.value),
                        ),
                    )
                  }

                  // Completed lasso selection: smooth marching-ants outline + glowing fill + bounding box
                  if (lassoOutlineVisible) {
                    val phase = lassoDashPhase.value
                    val animAlpha = lassoSelectionTransitionAlpha.value
                    val loopPath =
                      Path().apply {
                        moveTo(lassoLoopPoints.first().x, lassoLoopPoints.first().y)
                        for (i in 1 until lassoLoopPoints.size) {
                          val prev = lassoLoopPoints[i - 1]
                          val curr = lassoLoopPoints[i]
                          quadraticTo(prev.x, prev.y, (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
                        }
                        close()
                      }
                    drawPath(path = loopPath, color = SwitchBlue.copy(alpha = 0.09f * animAlpha))
                    drawPath(
                      path = loopPath,
                      color = SwitchBlue.copy(alpha = 0.25f * animAlpha),
                      style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                    drawPath(
                      path = loopPath,
                      color = SwitchBlue.copy(alpha = animAlpha),
                      style =
                        Stroke(
                          width = 3.5f,
                          cap = StrokeCap.Round,
                          join = StrokeJoin.Round,
                          pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), phase),
                        ),
                    )
                    if (lassoBounds != null) {
                      val pad = 8f
                      drawRect(
                        color = SwitchBlue.copy(alpha = (if (isLassoResizeMode) 0.85f else 0.42f) * animAlpha),
                        topLeft = Offset(lassoBounds[0] - pad, lassoBounds[1] - pad),
                        size =
                          androidx.compose.ui.geometry.Size(
                            width = (lassoBounds[2] - lassoBounds[0] + pad * 2f).coerceAtLeast(24f),
                            height = (lassoBounds[3] - lassoBounds[1] + pad * 2f).coerceAtLeast(24f),
                          ),
                        style =
                          Stroke(
                            width = if (isLassoResizeMode) 2.5f else 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                          ),
                      )
                    }
                  }

                  // Erase feedback: ink fading out, ripples where it was removed, and the eraser ring
                  if (effectsClockNanos < 0L) return@Canvas
                  val nowNs = System.nanoTime()
                  erasedGhosts.forEach { ghost ->
                    val t = ((nowNs - ghost.startNanos).toFloat() / ERASE_FADE_NANOS).coerceIn(0f, 1f)
                    val fade = (1f - t) * (1f - t)
                    drawRichStroke(
                      stroke =
                        ghost.stroke.copy(
                          color = ghost.stroke.color.copy(alpha = ghost.stroke.color.alpha * fade)
                        ),
                      isSelectedByLasso = false,
                    )
                  }
                  eraseRipples.forEach { ripple ->
                    val t = ((nowNs - ripple.startNanos).toFloat() / ERASE_FADE_NANOS).coerceIn(0f, 1f)
                    drawCircle(
                      color = Color(0xFF8E8E93).copy(alpha = 0.5f * (1f - t)),
                      radius = eraserRadius * (0.6f + 0.7f * t),
                      center = ripple.center,
                      style = Stroke(width = 2.5f),
                    )
                  }
                  val cursorAlpha = eraserCursorAlpha.value
                  if (cursorAlpha > 0.01f) {
                    val ringRadius = eraserRadius * (0.92f + 0.08f * cursorAlpha)
                    drawCircle(
                      color = Color(0xFF8E8E93).copy(alpha = 0.14f * cursorAlpha),
                      radius = ringRadius,
                      center = eraserCursorPos,
                    )
                    drawCircle(
                      color = Color(0xFF6B6B70).copy(alpha = 0.8f * cursorAlpha),
                      radius = ringRadius,
                      center = eraserCursorPos,
                      style = Stroke(width = 2f),
                    )
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

                // Interactive Shape Vertex Control Handles, Center Move Handle & Floating Shape Actions
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
                  if (editableShapeStroke != null) {
                    val handles = editableShapeStroke.shapeControlPoints
                    val centerPt =
                      Offset(
                        handles.map { it.x }.average().toFloat(),
                        handles.map { it.y }.average().toFloat(),
                      )
                    val topY = handles.minOf { it.y }

                    // Center Drag-to-Move Shape Handle
                    Box(
                      modifier =
                        Modifier
                          .offset {
                            IntOffset((centerPt.x - 13f).roundToInt(), (centerPt.y - 13f).roundToInt())
                          }
                          .size(26.dp)
                          .shadow(5.dp, CircleShape)
                          .clip(CircleShape)
                          .background(SwitchBlue)
                          .border(2.dp, Color.White, CircleShape)
                          .pointerInput(editableShapeStroke.id) {
                            detectDragGestures(
                              onDragStart = { onRecordUndoBeforeGesture() },
                            ) { change, dragAmount ->
                              change.consume()
                              viewModel.moveEntireShapeStroke(editableShapeStroke.id, dragAmount)
                            }
                          }
                          .testTag("shape_center_move_handle"),
                      contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                        imageVector = Icons.Outlined.OpenWith,
                        contentDescription = "Move Shape",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                      )
                    }

                    // Individual Vertex Control Handles
                    handles.forEachIndexed { handleIdx, pt ->
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

                    // Floating Shape Action Pill (Fill, Duplicate, Delete, Done)
                    val actionTopY =
                      if (topY > 64f) (topY - 52f).coerceAtLeast(8f)
                      else (handles.maxOf { it.y } + 18f)
                    Row(
                      modifier =
                        Modifier
                          .offset {
                            IntOffset(
                              (centerPt.x - 135f).coerceAtLeast(10f).roundToInt(),
                              actionTopY.roundToInt(),
                            )
                          }
                          .shadow(8.dp, RoundedCornerShape(16.dp))
                          .clip(RoundedCornerShape(16.dp))
                          .background(Color(0xEE1C1E26))
                          .border(1.dp, Color(0xFF363A48), RoundedCornerShape(16.dp))
                          .padding(horizontal = 10.dp, vertical = 5.dp)
                          .testTag("active_shape_action_pill"),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      Text(
                        text = editableShapeStroke.shapeKind?.label ?: "Shape",
                        color = Color(0xFF67C8EB),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                      )
                      if (
                        editableShapeStroke.shapeKind != ShapeKind.LINE &&
                          editableShapeStroke.shapeKind != ShapeKind.ARROW &&
                          editableShapeStroke.shapeKind != ShapeKind.CURVE
                      ) {
                        Text(
                          text = if (editableShapeStroke.shapeFill) "Filled" else "Outline",
                          color = Color.White,
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Medium,
                          modifier =
                            Modifier
                              .clip(RoundedCornerShape(6.dp))
                              .background(
                                if (editableShapeStroke.shapeFill) SwitchBlue.copy(alpha = 0.35f)
                                else Color(0xFF2C2E38)
                              )
                              .clickable { viewModel.toggleActiveShapeFill() }
                              .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                      }
                      Text(
                        text = "Copy",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier =
                          Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewModel.duplicateActiveShape() }
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                      )
                      Text(
                        text = "Delete",
                        color = DangerRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier =
                          Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewModel.deleteActiveShape() }
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                      )
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done editing shape",
                        tint = Color(0xFF4CAF50),
                        modifier =
                          Modifier
                            .size(16.dp)
                            .clickable { viewModel.clearActiveShapeEdit() },
                      )
                    }
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
                        onConvertAiTextClick = { onTogglePopover(ActivePopover.AI_HANDWRITING_ASSIST) },
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
                    strokePattern = strokePattern,
                    strokeStabilization = strokeStabilization,
                    drawAndHold = drawAndHold,
                    holdToErase = holdToErase,
                    scribbleToErase = scribbleToErase,
                    circleToLasso = circleToLasso,
                    strokeWidth = thicknessPresets.getOrElse(selectedThicknessIndex) { 6.5f },
                    onSelectPenStyle = onSelectPenStyle,
                    onSelectStrokePattern = onSelectStrokePattern,
                    onStabilizationChange = onStabilizationChange,
                    onToggleDrawAndHold = onToggleDrawAndHold,
                    onToggleHoldToErase = onToggleHoldToErase,
                    onToggleScribbleToErase = onToggleScribbleToErase,
                    onToggleCircleToLasso = onToggleCircleToLasso,
                    onOpenAiAssist = { onTogglePopover(ActivePopover.AI_HANDWRITING_ASSIST) },
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
                    selectedShapeKind = uiState.selectedShapeKind,
                    shapeAutoDetectMode = uiState.shapeAutoDetectMode,
                    lastAutoDetectedShape = lastAutoDetectedShape,
                    fillShapes = fillShapes,
                    onSelectShapeForDraw = { kind ->
                      viewModel.selectShapeForDirectDraw(kind)
                      onDismissPopover()
                    },
                    onToggleShapeAutoDetect = viewModel::setShapeAutoDetectMode,
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

                ActivePopover.ELEMENTS_PICKER -> {
                  ElementsPickerPopover(onInsertElement = onInsertElementStamp)
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

                ActivePopover.AI_HANDWRITING_ASSIST -> {
                  AiHandwritingAssistPopover(
                    strokeCount = currentPage.strokes.size,
                    onInsertConvertedTextBox = { text: String ->
                      onAddTextBoxAt(Offset(100f, 120f), text)
                    },
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

          // Interactive Hands-On Feature Tour Coach Overlay
          if (uiState.showInteractiveTour) {
            InteractiveFeatureTourOverlay(
              step = uiState.interactiveTourStep,
              strokeCount = currentPage.strokes.size,
              shapeCount = currentPage.strokes.count { it.isShape },
              showLassoSelection = showLassoSelection,
              laserTrailCount = laserTrails.size,
              onPrevStep = viewModel::previousInteractiveTourStep,
              onNextStep = viewModel::advanceInteractiveTourStep,
              onDismiss = viewModel::dismissInteractiveTour,
              modifier =
                Modifier
                  .align(Alignment.BottomCenter)
                  .padding(bottom = 52.dp, start = 16.dp, end = 16.dp),
            )
          }

          // Subtle Status Toast Banner
          androidx.compose.animation.AnimatedVisibility(
            visible = statusToastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier =
              Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp),
          ) {
            Box(
              modifier =
                Modifier
                  .shadow(8.dp, RoundedCornerShape(20.dp))
                  .clip(RoundedCornerShape(20.dp))
                  .background(Color(0xEE1F2229))
                  .border(1.dp, Color(0xFF363A45), RoundedCornerShape(20.dp))
                  .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
              Text(
                text = statusToastMessage.orEmpty(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
              )
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
            onAddStroke(scaledPts, emptyList(), false)
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
  onToggleAudioBar: () -> Unit,
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

    // Center Cluster: Primary Editing Tools + Audio Recording
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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

        Box(
          modifier =
            Modifier
              .padding(horizontal = 4.dp)
              .width(1.dp)
              .height(22.dp)
              .background(ToolbarIconLight.copy(alpha = 0.25f)),
        )

        Row(
          modifier =
            Modifier
              .height(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable(onClick = onToggleAudioBar)
              .padding(horizontal = 6.dp)
              .testTag("toolbar_audio_record_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.MicNone,
            contentDescription = "Record Audio",
            tint = ToolbarIconLight,
            modifier = Modifier.size(20.dp),
          )
          Icon(
            imageVector = Icons.Outlined.GraphicEq,
            contentDescription = null,
            tint = ToolbarIconLight,
            modifier = Modifier.size(15.dp),
          )
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
        // The midpoint curve stops half a segment short; finish at the real last point.
        lineTo(stroke.points.last().x, stroke.points.last().y)
      }
    }

  if (stroke.isShape && stroke.shapeFill && stroke.points.size >= 3) {
    drawPath(path = path, color = stroke.color.copy(alpha = 0.18f))
  }

  val avgPressure =
    if (stroke.pressures.isNotEmpty()) stroke.pressures.average().toFloat() else 0.65f
  val dynamicWidth =
    when {
      stroke.isHighlighter || stroke.isShape -> stroke.strokeWidth
      stroke.penStyle == PenStyle.FOUNTAIN -> stroke.strokeWidth * (0.75f + avgPressure * 0.55f)
      stroke.penStyle == PenStyle.BRUSH -> stroke.strokeWidth * (0.65f + avgPressure * 0.95f)
      else -> stroke.strokeWidth
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

  // Variable-width pressure-modulated rendering when per-point stylus pressures are present
  val useVariablePressure =
    !stroke.isHighlighter &&
      !stroke.isShape &&
      stroke.strokePattern == StrokePattern.SOLID &&
      stroke.pressures.size == stroke.points.size &&
      (stroke.penStyle == PenStyle.FOUNTAIN || stroke.penStyle == PenStyle.BRUSH)

  if (useVariablePressure) {
    for (i in 1 until stroke.points.size) {
      val p0 = stroke.points[i - 1]
      val p1 = stroke.points[i]
      val pr = ((stroke.pressures[i - 1] + stroke.pressures[i]) * 0.5f).coerceIn(0.15f, 1.5f)
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
      blendMode = if (stroke.isHighlighter) BlendMode.Darken else DrawScope.DefaultBlendMode,
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

/** How long erased ink / erase ripples take to fade away. */
private const val ERASE_FADE_NANOS = 240_000_000L

/** How long the pen must rest at the end of a closed loop before it turns into a lasso. */
private const val CIRCLE_LASSO_HOLD_MS = 420L

private data class ErasedInkGhost(val stroke: DrawnStroke, val startNanos: Long)

private data class EraseRipple(val center: Offset, val startNanos: Long)

@Composable
private fun DockedShapeStudioStrip(
  selectedShapeKind: ShapeKind,
  shapeAutoDetectMode: Boolean,
  fillShapes: Boolean,
  onToggleAutoDetect: (Boolean) -> Unit,
  onSelectShapeForDraw: (ShapeKind) -> Unit,
  onToggleFillShapes: () -> Unit,
  onStampShape: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(44.dp)
        .background(Color(0xFF1A1C23))
        .padding(horizontal = 10.dp)
        .horizontalScroll(rememberScrollState())
        .testTag("docked_shape_studio_strip"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    // Auto-Snap vs Direct Drag Mode Pill
    Box(
      modifier =
        Modifier
          .height(30.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(if (shapeAutoDetectMode) SwitchBlue else Color(0xFF282B36))
          .border(
            width = 1.dp,
            color = if (shapeAutoDetectMode) Color(0xFF67C8EB) else Color(0xFF383C4A),
            shape = RoundedCornerShape(8.dp),
          )
          .clickable { onToggleAutoDetect(true) }
          .padding(horizontal = 10.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "Auto-Snap",
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
      )
    }

    Box(
      modifier =
        Modifier
          .width(1.dp)
          .height(20.dp)
          .background(Color.White.copy(alpha = 0.16f)),
    )

    // All 11 Geometric Shapes for Direct Live Drag-to-Draw
    ShapeKind.entries.forEach { kind ->
      val isSelected = !shapeAutoDetectMode && selectedShapeKind == kind
      Row(
        modifier =
          Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SwitchBlue.copy(alpha = 0.30f) else Color(0xFF252832))
            .border(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) Color(0xFF67C8EB) else Color(0xFF343846),
              shape = RoundedCornerShape(8.dp),
            )
            .clickable { onSelectShapeForDraw(kind) }
            .padding(horizontal = 9.dp)
            .testTag("shape_strip_item_${kind.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
      ) {
        com.example.ui.components.ShapeKindPreviewIcon(
          kind = kind,
          tint = if (isSelected) Color.White else Color(0xFF67C8EB),
          modifier = Modifier.size(13.dp),
        )
        Text(
          text = kind.label.substringBefore(" "),
          color = if (isSelected) Color.White else Color(0xFFD5D9E2),
          fontSize = 11.sp,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        )
      }
    }

    Box(
      modifier =
        Modifier
          .width(1.dp)
          .height(20.dp)
          .background(Color.White.copy(alpha = 0.16f)),
    )

    // Fill Toggle Pill
    Box(
      modifier =
        Modifier
          .height(30.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(if (fillShapes) SwitchBlue.copy(alpha = 0.25f) else Color(0xFF252832))
          .border(
            width = 1.dp,
            color = if (fillShapes) SwitchBlue else Color(0xFF343846),
            shape = RoundedCornerShape(8.dp),
          )
          .clickable(onClick = onToggleFillShapes)
          .padding(horizontal = 9.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = if (fillShapes) "Fill: ON" else "Fill: OFF",
        color = if (fillShapes) Color(0xFF67C8EB) else TextSecondaryGray,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
    }

    // One-Tap Stamp Button
    Box(
      modifier =
        Modifier
          .height(30.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF2D3342))
          .clickable(onClick = onStampShape)
          .padding(horizontal = 10.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "+ Stamp ${selectedShapeKind.label.substringBefore(" ")}",
        color = Color.White,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
private fun InteractiveFeatureTourOverlay(
  step: Int,
  strokeCount: Int,
  shapeCount: Int,
  showLassoSelection: Boolean,
  laserTrailCount: Int,
  onPrevStep: () -> Unit,
  onNextStep: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val baselineStrokes = remember(step) { strokeCount }
  val baselineShapes = remember(step) { shapeCount }
  val baselineLasers = remember(step) { laserTrailCount }

  val stepCompleted =
    when (step) {
      0 -> strokeCount > baselineStrokes
      1 -> shapeCount > baselineShapes || strokeCount > baselineStrokes
      2 -> showLassoSelection
      3 -> laserTrailCount > baselineLasers
      else -> strokeCount < baselineStrokes
    }

  val title =
    when (step) {
      0 -> "Step 1 of 5 · Zero-Lag Pen & Stylus Ink"
      1 -> "Step 2 of 5 · Live Shape Studio (11 Shapes)"
      2 -> "Step 3 of 5 · Circle-to-Lasso Gesture"
      3 -> "Step 4 of 5 · Color-Synced Laser Pointer"
      else -> "Step 5 of 5 · Precision & Side-Button Eraser"
    }

  val instruction =
    when (step) {
      0 -> "Try writing or sketching anywhere on this page right now. Notice how the ink responds with pressure & zero lag!"
      1 -> "Drag across the page to draw a live Star, or tap any of the 11 shapes in the Shape Studio bar above to draw & adjust vertices."
      2 -> "Draw a closed circle around your strokes and hold your pen still at the end for 0.4s — watch it morph into a Lasso selection!"
      3 -> "Pick any color swatch in the top bar and glide across the canvas to project a glowing, pointer-free laser trail."
      else -> "Swipe across your strokes (or hold your stylus side button while writing) to erase with smooth fade-out."
    }

  Column(
    modifier =
      modifier
        .widthIn(max = 520.dp)
        .fillMaxWidth()
        .shadow(16.dp, RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xF5171A24))
        .border(
          width = 1.5.dp,
          color = if (stepCompleted) Color(0xFF4CAF50) else Color(0xFF4C8DF6),
          shape = RoundedCornerShape(20.dp),
        )
        .padding(16.dp)
        .testTag("interactive_tour_coach_card"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (stepCompleted) Color(0xFF4CAF50).copy(alpha = 0.22f)
                else Color(0xFF4C8DF6).copy(alpha = 0.22f)
              )
              .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Text(
            text = if (stepCompleted) "✓ GREAT JOB!" else "HANDS-ON TOUR",
            color = if (stepCompleted) Color(0xFF81C784) else Color(0xFF67C8EB),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
          )
        }
        Text(
          text = title,
          color = Color.White,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold,
        )
      }

      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Exit Tour",
        tint = TextSecondaryGray,
        modifier =
          Modifier
            .size(20.dp)
            .clickable(onClick = onDismiss)
            .testTag("dismiss_interactive_tour_button"),
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = instruction,
      color = Color(0xFFD5D9E4),
      fontSize = 12.sp,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Progress Dots
      Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        for (i in 0 until 5) {
          Box(
            modifier =
              Modifier
                .width(if (i == step) 20.dp else 7.dp)
                .height(7.dp)
                .clip(CircleShape)
                .background(
                  when {
                    i == step && stepCompleted -> Color(0xFF4CAF50)
                    i == step -> Color(0xFF4C8DF6)
                    i < step -> Color(0xFF67C8EB).copy(alpha = 0.6f)
                    else -> Color.White.copy(alpha = 0.2f)
                  }
                ),
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (step > 0) {
          Box(
            modifier =
              Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF282C3A))
                .clickable(onClick = onPrevStep)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "Back",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
            )
          }
        }

        Box(
          modifier =
            Modifier
              .height(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(if (stepCompleted) Color(0xFF4CAF50) else SwitchBlue)
              .clickable(onClick = onNextStep)
              .padding(horizontal = 16.dp)
              .testTag("interactive_tour_next_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = if (step >= 4) "Finish Tour" else if (stepCompleted) "Next Feature →" else "Next Step →",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}
