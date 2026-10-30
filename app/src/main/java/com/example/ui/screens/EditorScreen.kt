package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.MicNone
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivePopover
import com.example.model.DrawnStroke
import com.example.model.ElementStampType
import com.example.model.EraserMode
import com.example.model.LaserMode
import com.example.model.LassoFilterSettings
import com.example.model.LassoShape
import com.example.model.NotePage
import com.example.model.NotebookDocument
import com.example.model.PageScrollDirection
import com.example.model.PaperTemplate
import com.example.model.PenStyle
import com.example.model.PrimaryTool
import com.example.model.RulerState
import com.example.model.ShapeKind
import com.example.model.StrokePattern
import com.example.model.ToolbarDockPosition
import com.example.model.WritingSubTool
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import com.example.ui.components.AddPageDocumentIcon
import com.example.ui.components.AiHandwritingAssistPopover
import com.example.ui.components.AudioRecordingTimelineBar
import com.example.ui.components.DockPositionIcon
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
import com.example.ui.components.SPenBarrelButtonIcon
import com.example.ui.components.SecurityVaultPopover
import com.example.ui.components.SecurityVaultShieldIcon
import com.example.ui.components.ShapeSettingsPopover
import com.example.ui.components.SidebarThumbnailsIcon
import com.example.ui.components.StrokePatternIcon
import com.example.ui.components.TimeKeeperPopover
import com.example.ui.components.ToolbarCustomizationPopover
import com.example.ui.components.UndoCurvedArrowIcon
import com.example.ui.components.WritingSubToolCustomIcon
import com.example.ui.components.ZoomWindowPanel
import com.example.ui.theme.ActiveToolIconDark
import com.example.ui.theme.ActiveToolPill
import com.example.ui.theme.CanvasWorkspaceBg
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PageWhite
import com.example.ui.theme.PopoverBg
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
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
  activeDocument: NotebookDocument?,
  primaryTool: PrimaryTool,
  activeWritingSubTool: WritingSubTool,
  enabledToolbarTools: Set<PrimaryTool>,
  enabledWritingSubTools: Set<WritingSubTool>,
  toolbarDockPosition: ToolbarDockPosition,
  pageScrollDirection: PageScrollDirection,
  subToolbarOffset: Offset,
  onlyDrawWithStylus: Boolean,
  pullToAddPage: Boolean,
  isReadOnlyMode: Boolean,
  penStyle: PenStyle,
  strokePattern: StrokePattern,
  strokeStabilization: Float,
  drawAndHold: Boolean,
  holdToErase: Boolean,
  scribbleToErase: Boolean,
  circleToLasso: Boolean,
  highlighterStraightLine: Boolean,
  eraserMode: EraserMode,
  eraserRadius: Float,
  eraserAutoDeselect: Boolean,
  lastAutoDetectedShape: ShapeKind?,
  fillShapes: Boolean,
  activeShapeEditStrokeId: String?,
  laserMode: LaserMode,
  lassoFilters: LassoFilterSettings,
  selectedThicknessIndex: Int,
  thicknessPresets: List<Float>,
  quickSwatches: List<Color>,
  customPalette: List<Color>,
  selectedSwatchIndex: Int,
  selectedColor: Color,
  isEyedropperActive: Boolean,
  activePopover: ActivePopover,
  showLassoSelection: Boolean,
  isLassoResizeMode: Boolean,
  lassoLoopPoints: List<Offset>,
  lassoSelectedStrokeIds: Set<String>,
  rulerState: RulerState,
  isZoomWindowVisible: Boolean,
  zoomTargetBoxOffset: Offset,
  isAudioBarVisible: Boolean,
  isRecordingAudio: Boolean,
  isPlayingAudioReplay: Boolean,
  recordedAudioSeconds: Int,
  isPageSidebarOpen: Boolean,
  isSPenBarrelButtonActive: Boolean,
  privacyScreenShieldEnabled: Boolean,
  watermarkConfidentialExports: Boolean,
  documentSha256Hash: String,
  canUndo: Boolean,
  canRedo: Boolean,
  onSelectPrimaryTool: (PrimaryTool) -> Unit,
  onSelectWritingSubTool: (WritingSubTool) -> Unit,
  onSelectToolbarDockPosition: (ToolbarDockPosition) -> Unit,
  onCycleToolbarDockPosition: () -> Unit,
  onTogglePageScrollDirection: () -> Unit,
  onMoveSubToolbar: (Offset) -> Unit,
  onResetSubToolbarPosition: () -> Unit,
  onToggleOnlyDrawWithStylus: (Boolean) -> Unit,
  onToggleSPenBarrelButtonMode: () -> Unit,
  onTogglePopover: (ActivePopover) -> Unit,
  onDismissPopover: () -> Unit,
  onSelectPenStyle: (PenStyle) -> Unit,
  onSelectStrokePattern: (StrokePattern) -> Unit,
  onStabilizationChange: (Float) -> Unit,
  onToggleDrawAndHold: (Boolean) -> Unit,
  onToggleHoldToErase: (Boolean) -> Unit,
  onToggleScribbleToErase: (Boolean) -> Unit,
  onToggleCircleToLasso: (Boolean) -> Unit,
  onToggleHighlighterStraightLine: () -> Unit,
  onSelectEraserMode: (EraserMode) -> Unit,
  onEraserRadiusChange: (Float) -> Unit,
  onToggleEraserAutoDeselect: (Boolean) -> Unit,
  onInsertQuickShape: (ShapeKind) -> Unit,
  onToggleFillShapes: (Boolean) -> Unit,
  onMoveShapeControlHandle: (String, Int, Offset) -> Unit,
  onToggleLaserMode: () -> Unit,
  onUpdateLassoFilters: (LassoFilterSettings) -> Unit,
  onBringToFront: () -> Unit,
  onSendToBack: () -> Unit,
  onScaleSelection: () -> Unit,
  onScaleLassoSelectionStep: (Boolean) -> Unit,
  onDuplicateSelection: () -> Unit,
  onDeleteLassoSelection: () -> Unit,
  onCopyLassoSelection: () -> Unit,
  onCutLassoSelection: () -> Unit,
  onPasteLassoSelection: () -> Unit,
  onRecolorLassoSelection: (Color) -> Unit,
  hasClipboardContent: Boolean,
  isLassoColorMenuOpen: Boolean,
  onToggleLassoColorMenu: () -> Unit,
  onMoveLassoSelection: (Offset) -> Unit,
  onTransformLassoSelection: (Float, Float) -> Unit,
  onClearLassoSelection: () -> Unit,
  onCommitLassoChanges: () -> Unit,
  onSetCanvasDensity: (Float) -> Unit,
  onSelectThicknessIndex: (Int) -> Unit,
  onSelectQuickSwatch: (Int) -> Unit,
  onSelectPaletteColor: (Color) -> Unit,
  onAddNewColorToToolbar: (Color) -> Unit,
  onAddCustomColorToPalette: (Color) -> Unit,
  onRemoveCurrentToolbarSlot: () -> Unit,
  onToggleEyedropper: () -> Unit,
  onSampleColorAt: (Offset) -> Unit,
  onInstantCircleToLasso: (List<Offset>) -> Boolean,
  onTryCircleToLassoAtHold: (Offset) -> Boolean,
  onAddStroke: (List<Offset>, List<Float>, Boolean) -> Unit,
  onEraseAtPoints: (List<Offset>, Boolean) -> Unit,
  onToggleStudyTapeReveal: (String) -> Unit,
  onAddTextBoxAt: (Offset, String) -> Unit,
  onUpdateTextBox: (String, String) -> Unit,
  onMoveTextBox: (String, Offset) -> Unit,
  onAddStickyNoteAt: (Offset) -> Unit,
  onToggleStickyNoteExpanded: (String) -> Unit,
  onUpdateStickyNoteText: (String, String) -> Unit,
  onDeleteStickyNote: (String) -> Unit,
  onInsertElementStamp: (ElementStampType) -> Unit,
  onMoveElementStamp: (String, Offset) -> Unit,
  onMoveCanvasImage: (String, Offset) -> Unit,
  onPickImageFromDevice: () -> Unit,
  onImportPdfFromDevice: () -> Unit,
  onExportFullPdf: () -> Unit,
  onExportCurrentPagePng: () -> Unit,
  onCyclePaperColorTheme: () -> Unit,
  onSetVaultPin: (String) -> Unit,
  onRemoveVaultLock: () -> Unit,
  onTogglePrivacyScreenShield: (Boolean) -> Unit,
  onToggleWatermark: (Boolean) -> Unit,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  onClearPageStrokes: () -> Unit,
  onAddNewPage: () -> Unit,
  onSelectPageIndex: (Int) -> Unit,
  onToggleCurrentPageBookmark: () -> Unit,
  onTogglePageSidebar: () -> Unit,
  onCyclePaperTemplate: () -> Unit,
  onToggleLassoSelection: () -> Unit,
  onToggleRuler: () -> Unit,
  onMoveRuler: (Offset) -> Unit,
  onRotateRuler: (Float) -> Unit,
  onToggleZoomWindow: () -> Unit,
  onAdvanceZoomBox: (Float, Float) -> Unit,
  onToggleAudioBar: () -> Unit,
  onToggleAudioRecording: () -> Unit,
  onToggleAudioReplay: () -> Unit,
  onToggleToolbarToolEnabled: (PrimaryTool) -> Unit,
  onToggleWritingSubToolEnabled: (WritingSubTool) -> Unit,
  onResetToolbarDefaults: () -> Unit,
  onResetWritingToolsDefaults: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val currentDragPoints = remember { mutableStateListOf<Offset>() }
  val currentDragPressures = remember { mutableStateListOf<Float>() }

  // Goodnotes 6 Laser Pointer State
  val laserCommittedTrails = remember { mutableStateListOf<List<Offset>>() }
  val activeLaserPoints = remember { mutableStateListOf<Offset>() }
  var isLaserPointerHeldDown by remember { mutableStateOf(false) }
  var laserReleaseCounter by remember { mutableLongStateOf(0L) }
  val laserAlpha = remember { Animatable(1f) }

  LaunchedEffect(isLaserPointerHeldDown, laserReleaseCounter, primaryTool) {
    if (primaryTool != PrimaryTool.LASER) {
      laserCommittedTrails.clear()
      activeLaserPoints.clear()
      laserAlpha.snapTo(1f)
      return@LaunchedEffect
    }
    if (isLaserPointerHeldDown) {
      laserAlpha.snapTo(1f)
    } else if (laserCommittedTrails.isNotEmpty() || activeLaserPoints.isNotEmpty()) {
      delay(750L)
      laserAlpha.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = 680))
      if (!isLaserPointerHeldDown) {
        laserCommittedTrails.clear()
        activeLaserPoints.clear()
        laserAlpha.snapTo(1f)
      }
    }
  }

  val d = LocalDensity.current.density
  LaunchedEffect(d) { onSetCanvasDensity(d) }

  // Continuous Hold-Monitor for Hold-Only Circle-to-Lasso & Draw-and-Hold Shape/Line Snap while stylus is STILL DOWN
  var isStylusDrawingDown by remember { mutableStateOf(false) }
  var lastPointerMoveEpochMs by remember { mutableLongStateOf(0L) }
  var instantLassoConsumedDuringHold by remember { mutableStateOf(false) }
  var instantShapeSnappedDuringHold by remember { mutableStateOf(false) }

  LaunchedEffect(
    isStylusDrawingDown,
    circleToLasso,
    drawAndHold,
    primaryTool,
    activeWritingSubTool,
  ) {
    while (isStylusDrawingDown) {
      delay(50L)
      val idleMs = System.currentTimeMillis() - lastPointerMoveEpochMs
      val isPenOrPencil =
        primaryTool == PrimaryTool.WRITING_TOOLS &&
          (activeWritingSubTool == WritingSubTool.PEN || activeWritingSubTool == WritingSubTool.PENCIL)
      if (!isStylusDrawingDown || currentDragPoints.isEmpty()) continue
      val pts = currentDragPoints.toList()

      if (circleToLasso && isPenOrPencil && !instantLassoConsumedDuringHold) {
        // A) Live: drew a circle, pen still down and paused
        if (idleMs >= 400L && pts.size >= 10 && onInstantCircleToLasso(pts)) {
          instantLassoConsumedDuringHold = true
          isStylusDrawingDown = false
          currentDragPoints.clear()
          currentDragPressures.clear()
          continue
        }
        // B) 2-step: circle already drawn and lifted, now holding still on/inside it
        if (idleMs >= 320L && pts.size < 8 && onTryCircleToLassoAtHold(pts.first())) {
          instantLassoConsumedDuringHold = true
          isStylusDrawingDown = false
          currentDragPoints.clear()
          currentDragPressures.clear()
          continue
        }
      }
      if (idleMs >= 360L && drawAndHold && !instantShapeSnappedDuringHold && pts.size >= 8) {
        instantShapeSnappedDuringHold = true
      }
    }
  }

  var isHoldToEraseActiveNow by remember { mutableStateOf(false) }
  var holdEraserCursor by remember { mutableStateOf<Offset?>(null) }

  // Finger Pinch-to-Zoom & Pan State
  var canvasScale by remember { mutableFloatStateOf(1f) }
  var canvasPanOffset by remember { mutableStateOf(Offset.Zero) }

  val pages = activeDocument?.pages?.ifEmpty { listOf(NotePage(pageNumber = 1)) }
    ?: listOf(NotePage(pageNumber = 1))
  val currentPageIdx =
    (activeDocument?.currentPageIndex ?: 0).coerceIn(0, (pages.size - 1).coerceAtLeast(0))
  val currentPage = pages[currentPageIdx]

  val pagerState =
    rememberPagerState(
      initialPage = currentPageIdx,
      pageCount = { pages.size },
    )

  // Keep pagerState synced when user selects a page from sidebar or adds a new page
  LaunchedEffect(currentPageIdx, pages.size) {
    if (pagerState.currentPage != currentPageIdx && currentPageIdx in 0 until pages.size) {
      pagerState.animateScrollToPage(currentPageIdx)
    }
  }

  // Keep ViewModel synced when pager settles on a new page via horizontal/vertical swipe
  LaunchedEffect(pagerState.settledPage) {
    if (pagerState.settledPage != currentPageIdx && pagerState.settledPage in pages.indices) {
      onSelectPageIndex(pagerState.settledPage)
    }
  }

  // Helper to navigate to next/prev page via swipe (or add a page when pulling past the end)
  fun navigateByPageSwipe(deltaPage: Int) {
    val target = currentPageIdx + deltaPage
    if (target in pages.indices) {
      coroutineScope.launch {
        pagerState.animateScrollToPage(target)
      }
      onSelectPageIndex(target)
    } else if (target >= pages.size && deltaPage > 0 && pullToAddPage) {
      onAddNewPage()
    }
  }

  @Composable
  fun renderMainToolbar(isVerticalRail: Boolean = false) {
    if (isVerticalRail) {
      MainNavyVerticalRail(
        primaryTool = primaryTool,
        enabledToolbarTools = enabledToolbarTools,
        toolbarDockPosition = toolbarDockPosition,
        isBookmarked = currentPage.isBookmarked,
        onSelectPrimaryTool = { tool ->
          if (tool == PrimaryTool.IMAGE) {
            onSelectPrimaryTool(tool)
            onPickImageFromDevice()
          } else {
            onSelectPrimaryTool(tool)
          }
        },
        onCycleToolbarDockPosition = onCycleToolbarDockPosition,
        onTogglePageSidebar = onTogglePageSidebar,
        onToggleBookmark = onToggleCurrentPageBookmark,
        onToggleAudioBar = onToggleAudioBar,
        onAddNewPage = onAddNewPage,
        onOpenExportShare = { onTogglePopover(ActivePopover.EXPORT_SHARE) },
        onOpenToolbarCustomize = { onTogglePopover(ActivePopover.TOOLBAR_CUSTOMIZE) },
      )
    } else {
      MainNavyToolbar(
        primaryTool = primaryTool,
        enabledToolbarTools = enabledToolbarTools,
        toolbarDockPosition = toolbarDockPosition,
        isBookmarked = currentPage.isBookmarked,
        isVaultLocked = activeDocument?.isLockedVault == true,
        onlyDrawWithStylus = onlyDrawWithStylus,
        pageScrollDirection = pageScrollDirection,
        onSelectPrimaryTool = { tool ->
          if (tool == PrimaryTool.IMAGE) {
            onSelectPrimaryTool(tool)
            onPickImageFromDevice()
          } else {
            onSelectPrimaryTool(tool)
          }
        },
        onCycleToolbarDockPosition = onCycleToolbarDockPosition,
        onTogglePageSidebar = onTogglePageSidebar,
        onToggleBookmark = onToggleCurrentPageBookmark,
        onToggleRuler = onToggleRuler,
        onToggleZoomWindow = onToggleZoomWindow,
        onToggleAudioBar = onToggleAudioBar,
        onOpenTimeKeeper = { onTogglePopover(ActivePopover.TIME_KEEPER) },
        onOpenToolbarCustomize = { onTogglePopover(ActivePopover.TOOLBAR_CUSTOMIZE) },
        onOpenExportShare = { onTogglePopover(ActivePopover.EXPORT_SHARE) },
        onOpenSecurityVault = { onTogglePopover(ActivePopover.SECURITY_VAULT) },
        onTogglePageScrollDirection = onTogglePageScrollDirection,
        onImportPdfFromDevice = onImportPdfFromDevice,
        onCyclePaperTemplate = onCyclePaperTemplate,
        onCyclePaperColorTheme = onCyclePaperColorTheme,
        onAddNewPage = onAddNewPage,
        onClearPageStrokes = onClearPageStrokes,
        onToggleOnlyDrawWithStylus = { onToggleOnlyDrawWithStylus(!onlyDrawWithStylus) },
        onOpenSettings = onOpenSettings,
      )
    }
  }

  Column(modifier = modifier.fillMaxSize().background(CanvasWorkspaceBg)) {
    if (toolbarDockPosition == ToolbarDockPosition.TOP) {
      renderMainToolbar(isVerticalRail = false)
    }

    if (isAudioBarVisible) {
      AudioRecordingTimelineBar(
        isRecording = isRecordingAudio,
        isPlayingReplay = isPlayingAudioReplay,
        recordedSeconds = recordedAudioSeconds,
        strokeCount = currentPage.strokes.size,
        onToggleRecord = onToggleAudioRecording,
        onToggleReplay = onToggleAudioReplay,
        onCloseAudioBar = onToggleAudioBar,
      )
    }

    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
      if (toolbarDockPosition == ToolbarDockPosition.LEFT) {
        renderMainToolbar(isVerticalRail = true)
      }

      if (isPageSidebarOpen && activeDocument != null) {
        PageThumbnailsSidebar(
          document = activeDocument,
          onSelectPage = onSelectPageIndex,
          onAddPage = onAddNewPage,
          onCloseSidebar = onTogglePageSidebar,
        )
      }

      BoxWithConstraints(
        modifier =
          Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(CanvasWorkspaceBg),
      ) {
        val isCompact = maxWidth < 600.dp
        val isTablet = maxWidth >= 720.dp
        val pageHorizontalPadding =
          when {
            isTablet -> 96.dp
            isCompact -> 12.dp
            else -> 44.dp
          }

        @Composable
        fun renderSingleNotePage(pageIndex: Int) {
          val pageData = pages.getOrElse(pageIndex) { currentPage }
          val isCurrentActivePage = pageIndex == currentPageIdx
          val pdfBitmap = pageData.pdfPageBitmap
          // Preserve exact PDF aspect ratio so text and tables NEVER stretch or distort!
          val pageAspectRatio =
            remember(pdfBitmap) {
              if (pdfBitmap != null && pdfBitmap.height > 0) {
                (pdfBitmap.width.toFloat() / pdfBitmap.height.toFloat()).coerceIn(0.45f, 2.2f)
              } else {
                0.707f // Standard A4 portrait aspect ratio (1 / sqrt(2))
              }
            }
          val pageBgColor = activeDocument?.paperColorTheme?.color ?: PageWhite

          // Compute active Lasso bounding box if Lasso selection is active on this page
          val lassoBounds: FloatArray? =
            remember(lassoLoopPoints, showLassoSelection, primaryTool) {
              if (primaryTool == PrimaryTool.LASSO && showLassoSelection && lassoLoopPoints.size >= 3) {
                floatArrayOf(
                  lassoLoopPoints.minOf { it.x },
                  lassoLoopPoints.minOf { it.y },
                  lassoLoopPoints.maxOf { it.x },
                  lassoLoopPoints.maxOf { it.y },
                )
              } else null
            }

          val lassoShapeState =
            rememberUpdatedState(
              if (showLassoSelection && lassoLoopPoints.size >= 3) LassoShape(lassoLoopPoints) else null
            )

          Box(
            modifier =
              Modifier
                .fillMaxSize()
                .padding(horizontal = pageHorizontalPadding, vertical = 10.dp),
            contentAlignment = Alignment.Center,
          ) {
            Box(
              modifier =
                Modifier
                  .widthIn(max = if (isTablet) 720.dp else 580.dp)
                  .aspectRatio(
                    ratio = pageAspectRatio,
                    matchHeightConstraintsFirst = !isCompact,
                  )
                  .graphicsLayer {
                    scaleX = canvasScale
                    scaleY = canvasScale
                    translationX = canvasPanOffset.x
                    translationY = canvasPanOffset.y
                  }
                  .shadow(14.dp, RoundedCornerShape(4.dp))
                  .clip(RoundedCornerShape(4.dp))
                  .background(if (pdfBitmap != null) Color.White else pageBgColor)
                  .pointerInput(
                    pageIndex,
                    primaryTool,
                    isEyedropperActive,
                    pageData.studyTapes,
                  ) {
                    detectTapGestures { tapOffset ->
                      onDismissPopover()
                      if (isEyedropperActive) {
                        onSampleColorAt(tapOffset)
                        return@detectTapGestures
                      }
                      val hitTape =
                        pageData.studyTapes.find { tape ->
                          distanceFromPointToSegment(tapOffset, tape.start, tape.end) < tape.thickness
                        }
                      if (hitTape != null) {
                        onToggleStudyTapeReveal(hitTape.id)
                        return@detectTapGestures
                      }
                      if (primaryTool == PrimaryTool.TEXT) {
                        onAddTextBoxAt(tapOffset, "New Note Text")
                      } else if (primaryTool == PrimaryTool.STICKY_NOTE) {
                        onAddStickyNoteAt(tapOffset)
                      }
                    }
                  }
                  .pointerInput(
                    pageIndex,
                    primaryTool,
                    activeWritingSubTool,
                    penStyle,
                    strokePattern,
                    selectedColor,
                    selectedThicknessIndex,
                    holdToErase,
                    drawAndHold,
                    circleToLasso,
                    onlyDrawWithStylus,
                    isReadOnlyMode,
                    pageScrollDirection,
                    rulerState,
                    isEyedropperActive,
                    isSPenBarrelButtonActive,
                  ) {
                    if (isEyedropperActive) return@pointerInput
                    awaitEachGesture {
                      val down = awaitFirstDown(requireUnconsumed = false)
                      val downTime = System.currentTimeMillis()
                      val pointerType = down.type
                      val isStylusOrEraser =
                        pointerType == PointerType.Stylus || pointerType == PointerType.Eraser
                      val isHardwareEraser = pointerType == PointerType.Eraser
                      val isTouchFinger = pointerType == PointerType.Touch
                      val startPos = down.position

                      // Helper to clamp canvasPanOffset so zoomed page never flies off-screen
                      fun clampPan(pan: Offset, scale: Float): Offset {
                        if (scale <= 1.02f) return Offset.Zero
                        val maxX = (size.width * (scale - 1f) * 0.65f).coerceAtLeast(40f)
                        val maxY = (size.height * (scale - 1f) * 0.65f).coerceAtLeast(40f)
                        return Offset(
                          x = pan.x.coerceIn(-maxX, maxX),
                          y = pan.y.coerceIn(-maxY, maxY),
                        )
                      }

                      // 1) Read-Only Mode OR Stylus-Only Mode when touched by a Finger:
                      // Single-finger swipes turn pages (Horizontal or Vertical) when unzoomed,
                      // or pan/pinch-zoom the page when zoomed! Stylus always draws immediately!
                      if ((onlyDrawWithStylus && isTouchFinger && !isStylusOrEraser) || isReadOnlyMode) {
                        var prevCentroid = startPos
                        var prevSpread = 0f
                        var totalFingerDelta = Offset.Zero
                        var hadMultiTouch = false
                        while (true) {
                          val event = awaitPointerEvent()
                          val pressed = event.changes.filter { it.pressed }
                          if (pressed.isEmpty()) break
                          if (pressed.size >= 2) {
                            hadMultiTouch = true
                            val p1 = pressed[0].position
                            val p2 = pressed[1].position
                            val centroid = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
                            val spread = (p1 - p2).getDistance().coerceAtLeast(10f)
                            if (prevSpread > 0f) {
                              val zoomChange = spread / prevSpread
                              canvasScale = (canvasScale * zoomChange).coerceIn(0.85f, 4.0f)
                            }
                            canvasPanOffset = clampPan(canvasPanOffset + (centroid - prevCentroid), canvasScale)
                            prevCentroid = centroid
                            prevSpread = spread
                          } else {
                            val pos = pressed[0].position
                            val step = pos - prevCentroid
                            totalFingerDelta += step
                            if (canvasScale > 1.04f) {
                              canvasPanOffset = clampPan(canvasPanOffset + step, canvasScale)
                            }
                            prevCentroid = pos
                            prevSpread = 0f
                          }
                          pressed.forEach { it.consume() }
                        }

                        // If single-finger swipe at normal zoom -> turn PDF/Notebook page Horizontally or Vertically!
                        if (!hadMultiTouch && canvasScale <= 1.04f) {
                          if (pageScrollDirection == PageScrollDirection.HORIZONTAL) {
                            if (totalFingerDelta.x < -70f) navigateByPageSwipe(+1)
                            else if (totalFingerDelta.x > 70f) navigateByPageSwipe(-1)
                          } else {
                            if (totalFingerDelta.y < -70f) navigateByPageSwipe(+1)
                            else if (totalFingerDelta.y > 70f) navigateByPageSwipe(-1)
                          }
                        }
                        return@awaitEachGesture
                      }

                      // 2) Functional Interactive Lasso Mode:
                      val shape = lassoShapeState.value
                      if (
                        primaryTool == PrimaryTool.LASSO &&
                          shape != null &&
                          (shape.contains(startPos) || shape.distanceToEdge(startPos) < 28f)
                      ) {
                        var last = startPos
                        while (true) {
                          val event = awaitPointerEvent()
                          val pressed = event.changes.filter { it.pressed }
                          if (pressed.isEmpty()) break
                          val curr = pressed.first().position
                          val delta = curr - last
                          if (delta.getDistance() > 0.5f) {
                            onMoveLassoSelection(delta)
                            last = curr
                          }
                          pressed.forEach { it.consume() }
                        }
                        onCommitLassoChanges()
                        return@awaitEachGesture
                      }

                      // 3) Standard Stylus / S Pen Side Button / 2-Step Circle-to-Lasso / Drawing / Eraser / Laser Flow
                      currentDragPoints.clear()
                      currentDragPressures.clear()
                      currentDragPoints.add(startPos)
                      currentDragPressures.add(down.pressure.coerceIn(0.15f, 1.5f))

                      var movedBeyondSlop = false
                      var holdEraseTriggered =
                        isHardwareEraser ||
                          isSPenBarrelButtonActive ||
                          primaryTool == PrimaryTool.ERASER
                      var lastMoveTime = downTime
                      var lastPos = startPos
                      var multiTouchZoomTriggered = false
                      var prevPinchSpread = 0f
                      var prevPinchCentroid = startPos
                      var twoFingerSwipeDelta = Offset.Zero

                      instantLassoConsumedDuringHold = false
                      instantShapeSnappedDuringHold = false

                      if (holdEraseTriggered) {
                        isHoldToEraseActiveNow = true
                        holdEraserCursor = startPos
                        onEraseAtPoints(
                          listOf(startPos),
                          !isHardwareEraser && !isSPenBarrelButtonActive && primaryTool != PrimaryTool.ERASER,
                        )
                      } else if (primaryTool == PrimaryTool.LASER) {
                        isLaserPointerHeldDown = true
                        activeLaserPoints.clear()
                        activeLaserPoints.add(startPos)
                      } else {
                        isStylusDrawingDown = true
                        lastPointerMoveEpochMs = downTime
                      }

                      while (true) {
                        val event = awaitPointerEvent()
                        val pressedChanges = event.changes.filter { it.pressed }
                        if (pressedChanges.isEmpty()) {
                          event.changes.forEach { it.consume() }
                          break
                        }

                        // Detect Samsung S Pen / Active Stylus side barrel button press mid-stroke or on down!
                        val sPenSideButtonPressedNow =
                          isSPenBarrelButtonActive ||
                            (isStylusOrEraser &&
                              (event.buttons.isPrimaryPressed || event.buttons.isSecondaryPressed))
                        if (sPenSideButtonPressedNow && !holdEraseTriggered) {
                          holdEraseTriggered = true
                          isStylusDrawingDown = false
                          currentDragPoints.clear()
                          currentDragPressures.clear()
                        }

                        // 2-Finger Gesture: Pinch-to-Zoom OR 2-Finger Horizontal/Vertical Page Swipe!
                        if (pressedChanges.size >= 2) {
                          multiTouchZoomTriggered = true
                          isStylusDrawingDown = false
                          currentDragPoints.clear()
                          currentDragPressures.clear()
                          val p1 = pressedChanges[0].position
                          val p2 = pressedChanges[1].position
                          val centroid = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
                          val spread = (p1 - p2).getDistance().coerceAtLeast(10f)
                          if (prevPinchSpread > 0f) {
                            val zoomFactor = spread / prevPinchSpread
                            val centroidStep = centroid - prevPinchCentroid
                            twoFingerSwipeDelta += centroidStep
                            if (kotlin.math.abs(zoomFactor - 1f) > 0.012f || canvasScale > 1.04f) {
                              canvasScale = (canvasScale * zoomFactor).coerceIn(0.85f, 4.0f)
                              canvasPanOffset = clampPan(canvasPanOffset + centroidStep, canvasScale)
                            }
                          }
                          prevPinchSpread = spread
                          prevPinchCentroid = centroid
                          pressedChanges.forEach { it.consume() }
                          continue
                        }

                        if (multiTouchZoomTriggered) {
                          pressedChanges.forEach { it.consume() }
                          continue
                        }

                        val change = pressedChanges.first()
                        val pos = change.position
                        val now = System.currentTimeMillis()
                        val distFromStart = (pos - startPos).getDistance()
                        val stepDist = (pos - lastPos).getDistance()

                        if (distFromStart > 14f) {
                          movedBeyondSlop = true
                        }

                        // Hold in Place to Erase (if not holding on a drawn circle stroke)
                        if (
                          !movedBeyondSlop &&
                            !holdEraseTriggered &&
                            holdToErase &&
                            primaryTool == PrimaryTool.WRITING_TOOLS &&
                            (now - downTime) >= 420L
                        ) {
                          holdEraseTriggered = true
                          isStylusDrawingDown = false
                          isHoldToEraseActiveNow = true
                          holdEraserCursor = pos
                        }

                        if (stepDist > 3.2f) {
                          lastMoveTime = now
                          lastPointerMoveEpochMs = now
                          lastPos = pos
                        }

                        if (holdEraseTriggered) {
                          isHoldToEraseActiveNow = true
                          holdEraserCursor = pos
                          onEraseAtPoints(
                            listOf(pos),
                            !isHardwareEraser && !sPenSideButtonPressedNow && primaryTool != PrimaryTool.ERASER,
                          )
                        } else if (primaryTool == PrimaryTool.LASER) {
                          isLaserPointerHeldDown = true
                          activeLaserPoints.add(pos)
                        } else {
                          val adjustedPos =
                            if (rulerState.isVisible && (pos - rulerState.centerOffset).getDistance() < 160f) {
                              snapPointAlongRuler(startPos, pos, rulerState.angleDegrees)
                            } else {
                              pos
                            }
                          currentDragPoints.add(adjustedPos)
                          currentDragPressures.add(change.pressure.coerceIn(0.15f, 1.5f))
                        }
                        change.consume()
                      }

                      // If user performed a 2-finger swipe at 100% zoom, turn page Horizontally or Vertically!
                      if (multiTouchZoomTriggered && canvasScale <= 1.04f) {
                        if (pageScrollDirection == PageScrollDirection.HORIZONTAL) {
                          if (twoFingerSwipeDelta.x < -70f) navigateByPageSwipe(+1)
                          else if (twoFingerSwipeDelta.x > 70f) navigateByPageSwipe(-1)
                        } else {
                          if (twoFingerSwipeDelta.y < -70f) navigateByPageSwipe(+1)
                          else if (twoFingerSwipeDelta.y > 70f) navigateByPageSwipe(-1)
                        }
                      }

                      isStylusDrawingDown = false
                      val upTime = System.currentTimeMillis()
                      val heldStillAtEnd =
                        instantShapeSnappedDuringHold ||
                          (drawAndHold &&
                            movedBeyondSlop &&
                            (upTime - lastMoveTime) >= 340L &&
                            currentDragPoints.size >= 6)

                      if (primaryTool == PrimaryTool.LASER) {
                        if (activeLaserPoints.isNotEmpty()) {
                          laserCommittedTrails.add(activeLaserPoints.toList())
                          activeLaserPoints.clear()
                        }
                        isLaserPointerHeldDown = false
                        laserReleaseCounter += 1L
                      } else if (holdEraseTriggered) {
                        isHoldToEraseActiveNow = false
                        holdEraserCursor = null
                      } else if (
                        primaryTool == PrimaryTool.LASSO &&
                          lassoShapeState.value != null &&
                          !movedBeyondSlop &&
                          !multiTouchZoomTriggered
                      ) {
                        onClearLassoSelection()
                      } else if (
                        !multiTouchZoomTriggered &&
                          !instantLassoConsumedDuringHold &&
                          currentDragPoints.size >= 2
                      ) {
                        onAddStroke(
                          currentDragPoints.toList(),
                          currentDragPressures.toList(),
                          heldStillAtEnd,
                        )
                      }
                      currentDragPoints.clear()
                      currentDragPressures.clear()
                    }
                  }
                  .testTag(if (isCurrentActivePage) "note_canvas_page" else "note_canvas_page_$pageIndex"),
            ) {
              if (pdfBitmap != null) {
                Image(
                  bitmap = pdfBitmap.asImageBitmap(),
                  contentDescription = "PDF Page ${pageData.pageNumber}",
                  contentScale = ContentScale.Fit,
                  modifier = Modifier.fillMaxSize(),
                )
              }

              val lassoActive = primaryTool == PrimaryTool.LASSO && showLassoSelection

              pageData.images.forEach { imageItem ->
                Image(
                  bitmap = imageItem.bitmap.asImageBitmap(),
                  contentDescription = "Inserted photo",
                  contentScale = ContentScale.Crop,
                  modifier =
                    Modifier
                      .offset {
                        IntOffset(imageItem.position.x.roundToInt(), imageItem.position.y.roundToInt())
                      }
                      .size(width = imageItem.widthDp.dp, height = imageItem.heightDp.dp)
                      .graphicsLayer { rotationZ = imageItem.rotationDegrees }
                      .clip(RoundedCornerShape(8.dp))
                      .border(1.dp, SwitchBlue.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                      .then(
                        if (lassoActive) Modifier
                        else
                          Modifier.pointerInput(imageItem.id) {
                            detectDragGestures { change, dragAmount ->
                              change.consume()
                              onMoveCanvasImage(imageItem.id, dragAmount)
                            }
                          }
                      ),
                )
              }

              val template = activeDocument?.paperTemplate ?: PaperTemplate.BLANK
              val sortedStrokes = remember(pageData.strokes) {
                pageData.strokes.sortedBy { it.zIndex }
              }

              Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                if (pdfBitmap == null) {
                  drawPaperTemplate(template = template, width = w, height = h)
                }

                // Render all strokes (Highlighter uses BlendMode.Darken at zIndex = -1 so dark ink stays crisp!)
                sortedStrokes.forEach { stroke ->
                  drawRichStroke(
                    stroke = stroke,
                    isSelectedByLasso =
                      showLassoSelection &&
                        primaryTool == PrimaryTool.LASSO &&
                        lassoSelectedStrokeIds.contains(stroke.id),
                  )
                }

                // Active In-Progress Stroke Preview
                if (
                  isCurrentActivePage &&
                    currentDragPoints.size >= 2 &&
                    !isHoldToEraseActiveNow &&
                    primaryTool != PrimaryTool.ERASER &&
                    primaryTool != PrimaryTool.LASER
                ) {
                  if (primaryTool == PrimaryTool.LASSO) {
                    // Live Lasso Loop while user is drawing a lasso selection
                    val liveLassoPath =
                      Path().apply {
                        moveTo(currentDragPoints.first().x, currentDragPoints.first().y)
                        for (i in 1 until currentDragPoints.size) {
                          lineTo(currentDragPoints[i].x, currentDragPoints[i].y)
                        }
                      }
                    drawPath(
                      path = liveLassoPath,
                      color = Color(0xFF1E5AA8),
                      style =
                        Stroke(
                          width = 3.2f,
                          cap = StrokeCap.Round,
                          pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                        ),
                    )
                  } else {
                    val isW = primaryTool == PrimaryTool.WRITING_TOOLS
                    val isHighlighter = isW && activeWritingSubTool == WritingSubTool.HIGHLIGHTER
                    val isPencil = isW && activeWritingSubTool == WritingSubTool.PENCIL
                    val isTape = isW && activeWritingSubTool == WritingSubTool.TAPE
                    val baseWidth = thicknessPresets.getOrElse(selectedThicknessIndex) { 6.5f }
                    val previewPts =
                      if (
                        isTape ||
                          (isHighlighter &&
                            (highlighterStraightLine || instantShapeSnappedDuringHold))
                      ) {
                        listOf(currentDragPoints.first(), currentDragPoints.last())
                      } else {
                        currentDragPoints.toList()
                      }
                    val activePreview =
                      DrawnStroke(
                        points = previewPts,
                        pressures = currentDragPressures.toList(),
                        color =
                          when {
                            isTape -> selectedColor.copy(alpha = 0.85f)
                            isHighlighter -> selectedColor.copy(alpha = 0.38f)
                            isPencil -> selectedColor.copy(alpha = 0.75f)
                            else -> selectedColor
                          },
                        strokeWidth =
                          when {
                            isTape -> 38f
                            isHighlighter -> baseWidth * 3.4f
                            else -> baseWidth
                          },
                        penStyle = penStyle,
                        strokePattern = strokePattern,
                        isHighlighter = isHighlighter,
                        isPencil = isPencil,
                      )
                    drawRichStroke(activePreview, isSelectedByLasso = false)
                  }
                }

                // Study Tape Strips
                pageData.studyTapes.forEach { tape ->
                  val alpha = if (tape.isRevealed) 0.22f else 0.96f
                  drawLine(
                    color = tape.color.copy(alpha = alpha),
                    start = tape.start,
                    end = tape.end,
                    strokeWidth = tape.thickness,
                    cap = StrokeCap.Butt,
                  )
                  drawLine(
                    color = Color.White.copy(alpha = if (tape.isRevealed) 0.45f else 0.30f),
                    start = tape.start,
                    end = tape.end,
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                  )
                }

                if (isCurrentActivePage && isZoomWindowVisible) {
                  drawRoundRect(
                    color = Color(0x221E88E5),
                    topLeft = zoomTargetBoxOffset,
                    size = Size(190f, 56f),
                    cornerRadius = CornerRadius(6f, 6f),
                  )
                  drawRoundRect(
                    color = Color(0xFF1E88E5),
                    topLeft = zoomTargetBoxOffset,
                    size = Size(190f, 56f),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 2.5f),
                  )
                }

                if (isCurrentActivePage) {
                  holdEraserCursor?.let { cursor ->
                    drawCircle(
                      color = DangerRed.copy(alpha = 0.24f),
                      radius = eraserRadius,
                      center = cursor,
                    )
                    drawCircle(
                      color = DangerRed,
                      radius = eraserRadius,
                      center = cursor,
                      style = Stroke(width = 2.5f),
                    )
                  }
                }

                // Goodnotes 6 Laser Pointer Rendering
                if (isCurrentActivePage && primaryTool == PrimaryTool.LASER) {
                  val currentAlpha = laserAlpha.value.coerceIn(0f, 1f)
                  val allTrails =
                    if (activeLaserPoints.isNotEmpty()) {
                      laserCommittedTrails + listOf(activeLaserPoints.toList())
                    } else {
                      laserCommittedTrails
                    }

                  allTrails.forEach { trail ->
                    if (trail.isNotEmpty()) {
                      if (laserMode == LaserMode.HOVERING_DOT || trail.size == 1) {
                        val tip = trail.last()
                        drawCircle(
                          color = Color(0xFFFF1744).copy(alpha = 0.36f * currentAlpha),
                          radius = 22f,
                          center = tip,
                        )
                        drawCircle(
                          color = Color(0xFFFF1744).copy(alpha = currentAlpha),
                          radius = 8.5f,
                          center = tip,
                        )
                        drawCircle(
                          color = Color.White.copy(alpha = currentAlpha),
                          radius = 3.5f,
                          center = tip,
                        )
                      } else {
                        val path =
                          Path().apply {
                            moveTo(trail.first().x, trail.first().y)
                            for (i in 1 until trail.size) {
                              val prev = trail[i - 1]
                              val curr = trail[i]
                              quadraticTo(
                                prev.x,
                                prev.y,
                                (prev.x + curr.x) / 2f,
                                (prev.y + curr.y) / 2f,
                              )
                            }
                          }
                        drawPath(
                          path = path,
                          color = Color(0xFFFF1744).copy(alpha = 0.38f * currentAlpha),
                          style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                        drawPath(
                          path = path,
                          color = Color(0xFFFF1744).copy(alpha = currentAlpha),
                          style = Stroke(width = 5.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                        drawPath(
                          path = path,
                          color = Color.White.copy(alpha = 0.85f * currentAlpha),
                          style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                      }
                    }
                  }
                }

                // 1:1 Goodnotes Lasso Selection Dashed Loop + Resize Bounding Frame
                if (isCurrentActivePage && primaryTool == PrimaryTool.LASSO && showLassoSelection) {
                  if (lassoLoopPoints.size >= 3) {
                    val customLasso =
                      Path().apply {
                        moveTo(lassoLoopPoints.first().x, lassoLoopPoints.first().y)
                        for (i in 1 until lassoLoopPoints.size) {
                          lineTo(lassoLoopPoints[i].x, lassoLoopPoints[i].y)
                        }
                        close()
                      }
                    drawPath(
                      path = customLasso,
                      color = Color(0x161E5AA8),
                    )
                    drawPath(
                      path = customLasso,
                      color = Color(0xFF1E5AA8),
                      style =
                        Stroke(
                          width = 3.5f,
                          cap = StrokeCap.Round,
                          pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                        ),
                    )
                    if (isLassoResizeMode && lassoBounds != null) {
                      drawRoundRect(
                        color = SwitchBlue,
                        topLeft = Offset(lassoBounds[0] - 8f, lassoBounds[1] - 8f),
                        size =
                          Size(
                            (lassoBounds[2] - lassoBounds[0]) + 16f,
                            (lassoBounds[3] - lassoBounds[1]) + 16f,
                          ),
                        cornerRadius = CornerRadius(6f, 6f),
                        style =
                          Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                          ),
                      )
                    }
                  }
                }
              }

              val loopState = rememberUpdatedState(lassoLoopPoints)
              val boundsState = rememberUpdatedState(lassoBounds)

              // Interactive Bottom-Right Resize & Rotate Handle for Active Lasso Selection
              if (
                isCurrentActivePage &&
                  primaryTool == PrimaryTool.LASSO &&
                  showLassoSelection &&
                  lassoBounds != null
              ) {
                val handleX = lassoBounds[2] + 6f
                val handleY = lassoBounds[3] + 6f

                Box(
                  modifier =
                    Modifier
                      .offset {
                        IntOffset((handleX - 13f).roundToInt(), (handleY - 13f).roundToInt())
                      }
                      .size(26.dp)
                      .shadow(6.dp, CircleShape)
                      .clip(CircleShape)
                      .background(SwitchBlue)
                      .border(2.dp, Color.White, CircleShape)
                      .pointerInput(Unit) {
                        var handle = Offset.Zero
                        var pivot = Offset.Zero
                        detectDragGestures(
                          onDragStart = {
                            val b = boundsState.value
                            val lp = loopState.value
                            if (b != null && lp.isNotEmpty()) {
                              handle = Offset(b[2] + 6f, b[3] + 6f)
                              pivot = Offset(lp.map { it.x }.average().toFloat(), lp.map { it.y }.average().toFloat())
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
                            (newVec.getDistance().coerceAtLeast(20f) / oldVec.getDistance().coerceAtLeast(20f))
                              .coerceIn(0.92f, 1.08f)
                          val rot =
                            Math.toDegrees((atan2(newVec.y, newVec.x) - atan2(oldVec.y, oldVec.x)).toDouble())
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
                    modifier = Modifier.size(14.dp),
                  )
                }
              }

              // Interactive Goodnotes 6 Shape Vertex Control Handles (drag blue handles to resize/reshape)
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
                          detectDragGestures { change, dragAmount ->
                            change.consume()
                            onMoveShapeControlHandle(editableShapeStroke.id, handleIdx, dragAmount)
                          }
                        }
                        .testTag("shape_handle_$handleIdx"),
                  )
                }
              }

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
                            detectDragGestures { change, dragAmount ->
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
                            detectDragGestures { change, dragAmount ->
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

                // Floating Dark Lasso Action Pill (Screenshot 5 & Circle-to-Lasso)
                androidx.compose.animation.AnimatedVisibility(
                  visible = primaryTool == PrimaryTool.LASSO && showLassoSelection,
                  enter = fadeIn(),
                  exit = fadeOut(),
                  modifier =
                    if (lassoBounds != null) {
                      val pillTop =
                        if (lassoBounds[1] > 60f) (lassoBounds[1] - 54f).coerceAtLeast(8f)
                        else (lassoBounds[3] + 16f)
                      Modifier.offset { IntOffset(0, pillTop.roundToInt()) }
                    } else {
                      Modifier.padding(top = 110.dp)
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
              }

              // Interactive Page Navigation & Scrolling Direction Badge (Bottom-Right)
              Row(
                modifier =
                  Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xDD1C1C1E))
                    .border(1.dp, Color(0xFF34353A), RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                // Previous Page Button
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
                  text = "Page ${pageIndex + 1} of ${pages.size}",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.clickable(onClick = onTogglePageSidebar),
                )

                // Next Page (or + Add Page at end) Button
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

                // Quick Toggle between Horizontal and Vertical Page Swiping
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
          }
        }

        // Render Multi-Page PDF / Notebook inside HorizontalPager or VerticalPager!
        if (pageScrollDirection == PageScrollDirection.HORIZONTAL) {
          HorizontalPager(
            state = pagerState,
            userScrollEnabled = onlyDrawWithStylus || isReadOnlyMode,
            modifier = Modifier.fillMaxSize().testTag("horizontal_page_pager"),
          ) { pageIdx ->
            renderSingleNotePage(pageIdx)
          }
        } else {
          VerticalPager(
            state = pagerState,
            userScrollEnabled = onlyDrawWithStylus || isReadOnlyMode,
            modifier = Modifier.fillMaxSize().testTag("vertical_page_pager"),
          ) { pageIdx ->
            renderSingleNotePage(pageIdx)
          }
        }

        // Floating Undo / Redo Pill on Top-Left (1:1 Custom Vector Icons from Screenshots 1, 3, 4, 5)
        Row(
          modifier =
            Modifier
              .align(Alignment.TopStart)
              .padding(start = 12.dp, top = 12.dp)
              .height(40.dp)
              .shadow(6.dp, RoundedCornerShape(10.dp))
              .clip(RoundedCornerShape(10.dp))
              .background(SubToolbarPillBg)
              .border(1.dp, SubToolbarBorder, RoundedCornerShape(10.dp))
              .padding(horizontal = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Box(
            modifier =
              Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onUndo)
                .testTag("undo_button"),
            contentAlignment = Alignment.Center,
          ) {
            UndoCurvedArrowIcon(
              tint = if (canUndo) ToolbarIconLight else TextSecondaryGray.copy(alpha = 0.45f),
              modifier = Modifier.size(18.dp),
            )
          }
          Box(
            modifier =
              Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onRedo)
                .testTag("redo_button"),
            contentAlignment = Alignment.Center,
          ) {
            RedoCurvedArrowIcon(
              tint = if (canRedo) ToolbarIconLight else TextSecondaryGray.copy(alpha = 0.45f),
              modifier = Modifier.size(18.dp),
            )
          }
        }

        // Floating S Pen Button Erase, Stylus-Only & Zoom Controls Pill on Top-Right
        Row(
          modifier =
            Modifier
              .align(Alignment.TopEnd)
              .padding(end = 12.dp, top = 12.dp)
              .height(38.dp)
              .shadow(6.dp, RoundedCornerShape(10.dp))
              .clip(RoundedCornerShape(10.dp))
              .background(SubToolbarPillBg)
              .border(1.dp, SubToolbarBorder, RoundedCornerShape(10.dp))
              .padding(horizontal = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          // S Pen Barrel Button Quick-Erase Toggle (also auto-detects hardware S Pen side button)
          Row(
            modifier =
              Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSPenBarrelButtonActive) DangerRed.copy(alpha = 0.25f) else Color.Transparent)
                .clickable(onClick = onToggleSPenBarrelButtonMode)
                .padding(horizontal = 6.dp, vertical = 3.dp)
                .testTag("spen_barrel_button_toggle"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            SPenBarrelButtonIcon(
              tint = if (isSPenBarrelButtonActive) DangerRed else ToolbarIconLight,
              isButtonPressed = isSPenBarrelButtonActive,
              modifier = Modifier.size(16.dp),
            )
            if (!isCompact) {
              Text(
                text = if (isSPenBarrelButtonActive) "S Pen Erase ON" else "S Pen Btn",
                color = if (isSPenBarrelButtonActive) DangerRed else TextSecondaryGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }

          Row(
            modifier =
              Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (onlyDrawWithStylus) SubToolbarActiveTeal else Color.Transparent)
                .clickable { onToggleOnlyDrawWithStylus(!onlyDrawWithStylus) }
                .padding(horizontal = 6.dp, vertical = 3.dp)
                .testTag("editor_stylus_only_toggle"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = if (onlyDrawWithStylus) Icons.Outlined.PanTool else Icons.Outlined.Draw,
              contentDescription = "Stylus-Only Mode",
              tint = if (onlyDrawWithStylus) SubToolbarActiveCyan else ToolbarIconLight,
              modifier = Modifier.size(14.dp),
            )
            if (!isCompact) {
              Text(
                text = if (onlyDrawWithStylus) "Stylus Only" else "Stylus+Touch",
                color = if (onlyDrawWithStylus) SubToolbarActiveCyan else TextSecondaryGray,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }

          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Zoom Out",
            tint = ToolbarIconLight,
            modifier =
              Modifier
                .size(16.dp)
                .clickable {
                  canvasScale = (canvasScale - 0.2f).coerceIn(0.85f, 4.0f)
                  if (canvasScale <= 1.04f) canvasPanOffset = Offset.Zero
                },
          )
          Text(
            text = "${(canvasScale * 100).roundToInt()}%",
            color = TextPrimaryWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier =
              Modifier
                .clickable {
                  canvasScale = 1f
                  canvasPanOffset = Offset.Zero
                }
                .testTag("zoom_reset_percentage"),
          )
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Zoom In",
            tint = ToolbarIconLight,
            modifier =
              Modifier
                .size(16.dp)
                .clickable { canvasScale = (canvasScale + 0.2f).coerceIn(0.85f, 4.0f) },
          )
        }

        // Draggable Floating Contextual Writing Tools Sub-Toolbar + Active Popovers
        Column(
          modifier =
            Modifier
              .align(Alignment.TopCenter)
              .offset {
                IntOffset(subToolbarOffset.x.roundToInt(), subToolbarOffset.y.roundToInt())
              }
              .padding(
                start = if (isCompact) 76.dp else 0.dp,
                end = if (isCompact) 8.dp else 0.dp,
                top = 12.dp,
              ),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          if (primaryTool != PrimaryTool.LASSO) {
            FloatingWritingSubToolbar(
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
              onDragSubToolbar = onMoveSubToolbar,
              onSelectWritingSubTool = onSelectWritingSubTool,
              onSelectThicknessIndex = onSelectThicknessIndex,
              onSelectQuickSwatch = onSelectQuickSwatch,
              onOpenColorPicker = { onTogglePopover(ActivePopover.PEN_COLOR) },
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Render Active Popover
          when (activePopover) {
            ActivePopover.PEN_SETTINGS -> {
              Box(modifier = Modifier.offset(x = if (isCompact) (-12).dp else (-90).dp)) {
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
            }

            ActivePopover.HIGHLIGHTER_SETTINGS -> {
              Box(modifier = Modifier.offset(x = if (isCompact) (-12).dp else (-70).dp)) {
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
            }

            ActivePopover.PEN_COLOR -> {
              Box(modifier = Modifier.offset(x = if (isCompact) 8.dp else 110.dp)) {
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
                onInsertConvertedTextBox = { text ->
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

            else -> Unit
          }
        }
      }

      if (toolbarDockPosition == ToolbarDockPosition.RIGHT) {
        renderMainToolbar(isVerticalRail = true)
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

    if (toolbarDockPosition == ToolbarDockPosition.BOTTOM) {
      renderMainToolbar(isVerticalRail = false)
    }
  }
}

/**
 * Vertical Navy Toolbar Rail when docked to LEFT or RIGHT of the workspace.
 * Long-pressing anywhere on the rail or dock icon cycles the toolbar dock position!
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainNavyVerticalRail(
  primaryTool: PrimaryTool,
  enabledToolbarTools: Set<PrimaryTool>,
  toolbarDockPosition: ToolbarDockPosition,
  isBookmarked: Boolean,
  onSelectPrimaryTool: (PrimaryTool) -> Unit,
  onCycleToolbarDockPosition: () -> Unit,
  onTogglePageSidebar: () -> Unit,
  onToggleBookmark: () -> Unit,
  onToggleAudioBar: () -> Unit,
  onAddNewPage: () -> Unit,
  onOpenExportShare: () -> Unit,
  onOpenToolbarCustomize: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .width(54.dp)
        .fillMaxHeight()
        .background(ToolbarNavy)
        .combinedClickable(
          onClick = {},
          onLongClick = onCycleToolbarDockPosition,
        )
        .verticalScroll(rememberScrollState())
        .padding(vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Box(
      modifier =
        Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = onTogglePageSidebar)
          .testTag("toolbar_sidebar_button"),
      contentAlignment = Alignment.Center,
    ) {
      SidebarThumbnailsIcon(tint = ToolbarIconLight, modifier = Modifier.size(22.dp))
    }

    Box(
      modifier =
        Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = onToggleBookmark)
          .testTag("toolbar_bookmark_button"),
      contentAlignment = Alignment.Center,
    ) {
      PageBookmarkIcon(
        tint = if (isBookmarked) Color(0xFFFFD54F) else ToolbarIconLight,
        isBookmarked = isBookmarked,
        modifier = Modifier.size(22.dp),
      )
    }

    Box(
      modifier =
        Modifier
          .width(28.dp)
          .height(1.dp)
          .background(ToolbarIconLight.copy(alpha = 0.25f)),
    )

    PrimaryTool.entries
      .filter { enabledToolbarTools.contains(it) }
      .forEach { tool ->
        val isSelected = primaryTool == tool
        Box(
          modifier =
            Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) ActiveToolPill else Color.Transparent)
              .combinedClickable(
                onClick = { onSelectPrimaryTool(tool) },
                onLongClick = onCycleToolbarDockPosition,
              )
              .testTag("primary_tool_${tool.name.lowercase()}"),
          contentAlignment = Alignment.Center,
        ) {
          PrimaryToolCustomIcon(
            tool = tool,
            tint = if (isSelected) ActiveToolIconDark else ToolbarIconLight,
            modifier = Modifier.size(24.dp),
          )
        }
      }

    Box(
      modifier =
        Modifier
          .width(28.dp)
          .height(1.dp)
          .background(ToolbarIconLight.copy(alpha = 0.25f)),
    )

    Box(
      modifier =
        Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = onToggleAudioBar)
          .testTag("toolbar_audio_record_button"),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Outlined.MicNone,
        contentDescription = "Record Audio",
        tint = ToolbarIconLight,
        modifier = Modifier.size(22.dp),
      )
    }

    Box(
      modifier =
        Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = onAddNewPage)
          .testTag("toolbar_add_page_button"),
      contentAlignment = Alignment.Center,
    ) {
      AddPageDocumentIcon(tint = ToolbarIconLight, modifier = Modifier.size(22.dp))
    }

    Box(
      modifier =
        Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = onOpenExportShare)
          .testTag("toolbar_share_button"),
      contentAlignment = Alignment.Center,
    ) {
      ExportShareBoxIcon(tint = ToolbarIconLight, modifier = Modifier.size(22.dp))
    }

    Box(
      modifier =
        Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(8.dp))
          .combinedClickable(
            onClick = onCycleToolbarDockPosition,
            onLongClick = onOpenToolbarCustomize,
          )
          .testTag("toolbar_reposition_button"),
      contentAlignment = Alignment.Center,
    ) {
      DockPositionIcon(
        position = toolbarDockPosition,
        tint = Color(0xFF67C8EB),
        modifier = Modifier.size(21.dp),
      )
    }

    Box(
      modifier =
        Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = onOpenToolbarCustomize)
          .testTag("toolbar_more_button"),
      contentAlignment = Alignment.Center,
    ) {
      MoreCircleDotsIcon(tint = ToolbarIconLight, modifier = Modifier.size(22.dp))
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainNavyToolbar(
  primaryTool: PrimaryTool,
  enabledToolbarTools: Set<PrimaryTool>,
  toolbarDockPosition: ToolbarDockPosition,
  isBookmarked: Boolean,
  isVaultLocked: Boolean,
  onlyDrawWithStylus: Boolean,
  pageScrollDirection: PageScrollDirection,
  onSelectPrimaryTool: (PrimaryTool) -> Unit,
  onCycleToolbarDockPosition: () -> Unit,
  onTogglePageSidebar: () -> Unit,
  onToggleBookmark: () -> Unit,
  onToggleRuler: () -> Unit,
  onToggleZoomWindow: () -> Unit,
  onToggleAudioBar: () -> Unit,
  onOpenTimeKeeper: () -> Unit,
  onOpenToolbarCustomize: () -> Unit,
  onOpenExportShare: () -> Unit,
  onOpenSecurityVault: () -> Unit,
  onTogglePageScrollDirection: () -> Unit,
  onImportPdfFromDevice: () -> Unit,
  onCyclePaperTemplate: () -> Unit,
  onCyclePaperColorTheme: () -> Unit,
  onAddNewPage: () -> Unit,
  onClearPageStrokes: () -> Unit,
  onToggleOnlyDrawWithStylus: () -> Unit,
  onOpenSettings: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(52.dp)
        .background(ToolbarNavy)
        .combinedClickable(
          onClick = {},
          onLongClick = onCycleToolbarDockPosition,
        )
        .padding(horizontal = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Left Cluster: Larger 22.dp Custom Sidebar Thumbnails, Search, Bookmark Icons (Screenshot 1)
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onTogglePageSidebar)
            .testTag("toolbar_sidebar_button"),
        contentAlignment = Alignment.Center,
      ) {
        SidebarThumbnailsIcon(tint = ToolbarIconLight, modifier = Modifier.size(22.dp))
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
          modifier = Modifier.size(22.dp),
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
          modifier = Modifier.size(22.dp),
        )
      }
    }

    // Center Cluster: Larger 24.dp Official Goodnotes 6 Tool Icons inside 40.dp Pills (Long-Press to Reposition Dock!)
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
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        PrimaryTool.entries
          .filter { enabledToolbarTools.contains(it) }
          .forEach { tool ->
            val isSelected = primaryTool == tool
            Box(
              modifier =
                Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) ActiveToolPill else Color.Transparent)
                  .combinedClickable(
                    onClick = { onSelectPrimaryTool(tool) },
                    onLongClick = onCycleToolbarDockPosition,
                  )
                  .testTag("primary_tool_${tool.name.lowercase()}"),
              contentAlignment = Alignment.Center,
            ) {
              PrimaryToolCustomIcon(
                tool = tool,
                tint = if (isSelected) ActiveToolIconDark else ToolbarIconLight,
                modifier = Modifier.size(24.dp),
              )
              if (isSelected) {
                Box(
                  modifier =
                    Modifier
                      .align(Alignment.BottomEnd)
                      .padding(3.5.dp)
                      .size(4.5.dp)
                      .clip(CircleShape)
                      .background(ActiveToolIconDark),
                )
              }
            }
          }

        // Separator before Microphone / Audio Record cluster
        Box(
          modifier =
            Modifier
              .padding(horizontal = 4.dp)
              .width(1.dp)
              .height(22.dp)
              .background(ToolbarIconLight.copy(alpha = 0.25f))
        )

        // Microphone + AI Sparkle/Waveform cluster
        Row(
          modifier =
            Modifier
              .height(38.dp)
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
            modifier = Modifier.size(21.dp),
          )
          Icon(
            imageVector = Icons.Outlined.GraphicEq,
            contentDescription = null,
            tint = ToolbarIconLight,
            modifier = Modifier.size(16.dp),
          )
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = ToolbarIconLight.copy(alpha = 0.75f),
            modifier = Modifier.size(13.dp),
          )
        }
      }
    }

    // Right Cluster: Larger 22.dp Add Page, Export PDF/PNG, Vault Shield, Dock Reposition, More Menu
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
      var showAddPageMenu by remember { mutableStateOf(false) }
      Box {
        Box(
          modifier =
            Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable { showAddPageMenu = true }
              .testTag("toolbar_add_page_button"),
          contentAlignment = Alignment.Center,
        ) {
          AddPageDocumentIcon(tint = ToolbarIconLight, modifier = Modifier.size(22.dp))
        }
        DropdownMenu(
          expanded = showAddPageMenu,
          onDismissRequest = { showAddPageMenu = false },
          modifier = Modifier.background(PopoverBg),
        ) {
          DropdownMenuItem(
            text = { Text("Add New Page", color = TextPrimaryWhite, fontSize = 13.sp) },
            onClick = {
              showAddPageMenu = false
              onAddNewPage()
            },
          )
          DropdownMenuItem(
            text = { Text("Cycle Paper Template", color = TextPrimaryWhite, fontSize = 13.sp) },
            onClick = {
              showAddPageMenu = false
              onCyclePaperTemplate()
            },
          )
          DropdownMenuItem(
            text = { Text("Cycle Paper Color Tint", color = TextPrimaryWhite, fontSize = 13.sp) },
            onClick = {
              showAddPageMenu = false
              onCyclePaperColorTheme()
            },
          )
          DropdownMenuItem(
            text = { Text("Import PDF Document...", color = Color(0xFF67C8EB), fontSize = 13.sp) },
            onClick = {
              showAddPageMenu = false
              onImportPdfFromDevice()
            },
          )
        }
      }

      // Export & Share PDF/PNG Button
      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onOpenExportShare)
            .testTag("toolbar_share_button"),
        contentAlignment = Alignment.Center,
      ) {
        ExportShareBoxIcon(tint = ToolbarIconLight, modifier = Modifier.size(22.dp))
      }

      // Cybersecurity Vault Shield Button
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
          modifier = Modifier.size(21.dp),
        )
      }

      // Quick Dock Reposition Button (Tap or Long-Press to move toolbar Top / Right / Bottom / Left)
      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
              onClick = onCycleToolbarDockPosition,
              onLongClick = onOpenToolbarCustomize,
            )
            .testTag("toolbar_reposition_button"),
        contentAlignment = Alignment.Center,
      ) {
        DockPositionIcon(
          position = toolbarDockPosition,
          tint = Color(0xFF67C8EB),
          modifier = Modifier.size(20.dp),
        )
      }

      var showAccessoriesMenu by remember { mutableStateOf(false) }
      Box {
        Box(
          modifier =
            Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable { showAccessoriesMenu = true }
              .testTag("toolbar_more_button"),
          contentAlignment = Alignment.Center,
        ) {
          MoreCircleDotsIcon(tint = ToolbarIconLight, modifier = Modifier.size(22.dp))
        }

        DropdownMenu(
          expanded = showAccessoriesMenu,
          onDismissRequest = { showAccessoriesMenu = false },
          modifier = Modifier.background(PopoverBg),
        ) {
          DropdownMenuItem(
            text = {
              Text(
                "Export PDF & Page Image...",
                color = Color(0xFF67C8EB),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
              )
            },
            onClick = {
              showAccessoriesMenu = false
              onOpenExportShare()
            },
          )
          DropdownMenuItem(
            text = {
              Text(
                "Cybersecurity Vault & PIN Lock...",
                color = Color(0xFF34C759),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
              )
            },
            onClick = {
              showAccessoriesMenu = false
              onOpenSecurityVault()
            },
          )
          DropdownMenuItem(
            text = {
              Text(
                "Scrolling Direction: ${pageScrollDirection.label}",
                color = TextPrimaryWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
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
              showAccessoriesMenu = false
              onTogglePageScrollDirection()
            },
          )
          DropdownMenuItem(
            text = {
              Text(
                "Toolbar Customization & Dock (Top/Bottom/Left/Right)...",
                color = TextPrimaryWhite,
                fontSize = 13.sp,
              )
            },
            leadingIcon = {
              Icon(Icons.Outlined.Tune, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showAccessoriesMenu = false
              onOpenToolbarCustomize()
            },
          )
          DropdownMenuItem(
            text = {
              Text(
                if (onlyDrawWithStylus) "Disable Stylus-Only Mode"
                else "Enable Stylus-Only (Finger Swipe & Zoom)",
                color = TextPrimaryWhite,
                fontSize = 13.sp,
              )
            },
            leadingIcon = {
              Icon(Icons.Outlined.PanTool, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showAccessoriesMenu = false
              onToggleOnlyDrawWithStylus()
            },
          )
          DropdownMenuItem(
            text = { Text("Cycle Paper Color Tint", color = TextPrimaryWhite, fontSize = 13.sp) },
            onClick = {
              showAccessoriesMenu = false
              onCyclePaperColorTheme()
            },
          )
          DropdownMenuItem(
            text = { Text("Goodnotes Settings...", color = TextPrimaryWhite, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.Settings, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showAccessoriesMenu = false
              onOpenSettings()
            },
          )
          DropdownMenuItem(
            text = { Text("Time Keeper (Timer / Stopwatch)", color = TextPrimaryWhite, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.Timer, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showAccessoriesMenu = false
              onOpenTimeKeeper()
            },
          )
          DropdownMenuItem(
            text = { Text("Ruler Tool", color = TextPrimaryWhite, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.Straighten, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showAccessoriesMenu = false
              onToggleRuler()
            },
          )
          DropdownMenuItem(
            text = { Text("Zoom Window", color = TextPrimaryWhite, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.ZoomIn, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showAccessoriesMenu = false
              onToggleZoomWindow()
            },
          )
          DropdownMenuItem(
            text = { Text("Import PDF...", color = TextPrimaryWhite, fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = ToolbarIconLight)
            },
            onClick = {
              showAccessoriesMenu = false
              onImportPdfFromDevice()
            },
          )
          DropdownMenuItem(
            text = { Text("Clear Current Page", color = DangerRed, fontSize = 13.sp) },
            onClick = {
              showAccessoriesMenu = false
              onClearPageStrokes()
            },
          )
        }
      }
    }
  }
}

@Composable
private fun FloatingWritingSubToolbar(
  activeWritingSubTool: WritingSubTool,
  enabledWritingSubTools: Set<WritingSubTool>,
  strokePattern: StrokePattern,
  selectedThicknessIndex: Int,
  quickSwatches: List<Color>,
  selectedSwatchIndex: Int,
  isPenPopoverOpen: Boolean,
  isColorPopoverOpen: Boolean,
  isEyedropperActive: Boolean,
  onDragSubToolbar: (Offset) -> Unit,
  onSelectWritingSubTool: (WritingSubTool) -> Unit,
  onSelectThicknessIndex: (Int) -> Unit,
  onSelectQuickSwatch: (Int) -> Unit,
  onOpenColorPicker: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .height(46.dp)
        .shadow(12.dp, RoundedCornerShape(14.dp))
        .clip(RoundedCornerShape(14.dp))
        .background(SubToolbarPillBg)
        .border(1.dp, SubToolbarBorder, RoundedCornerShape(14.dp))
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 8.dp)
        .testTag("floating_pen_subtoolbar"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    // Drag Handle to reposition the floating sub-toolbar anywhere on screen
    Box(
      modifier =
        Modifier
          .size(24.dp)
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              onDragSubToolbar(dragAmount)
            }
          }
          .testTag("subtoolbar_drag_handle"),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Default.DragIndicator,
        contentDescription = "Drag to reposition toolbar",
        tint = TextSecondaryGray.copy(alpha = 0.75f),
        modifier = Modifier.size(17.dp),
      )
    }

    // Left Cluster: Larger 20.dp Official Goodnotes Writing Sub-Tools (Pen, Pencil, Highlighter, Tape, Draw Shape)
    WritingSubTool.entries
      .filter { enabledWritingSubTools.contains(it) }
      .forEachIndexed { idx, subTool ->
        val isSelected = activeWritingSubTool == subTool
        if (isSelected) {
          Row(
            modifier =
              Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(9.dp))
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
              modifier = Modifier.size(20.dp),
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
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }

    // Divider
    Box(
      modifier =
        Modifier
          .padding(horizontal = 2.dp)
          .width(1.dp)
          .height(22.dp)
          .background(SubToolbarBorder)
    )

    // Center Cluster: Three Stroke Thickness Slots (Thin, Medium, Thick)
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
                .background(if (isSelected) Color.White else TextSecondaryGray)
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

    // Divider
    Box(
      modifier =
        Modifier
          .padding(horizontal = 2.dp)
          .width(1.dp)
          .height(22.dp)
          .background(SubToolbarBorder)
    )

    // Right Cluster: Customizable Color Swatches + Dotted Color Ring Trigger
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
              modifier = Modifier.size(14.dp),
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

@Composable
private fun PageThumbnailsSidebar(
  document: NotebookDocument,
  onSelectPage: (Int) -> Unit,
  onAddPage: () -> Unit,
  onCloseSidebar: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .width(176.dp)
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
            page.pdfPageBitmap?.let { bmp ->
              Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Page ${page.pageNumber}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (page.isBookmarked) "Page ${index + 1} ★" else "Page ${index + 1}",
            color = TextPrimaryWhite,
            fontSize = 11.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
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
      Text("Add Page", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
  }
}

private fun DrawScope.drawPaperTemplate(
  template: PaperTemplate,
  width: Float,
  height: Float,
) {
  when (template) {
    PaperTemplate.BLANK -> Unit
    PaperTemplate.RULED -> {
      val lineSpacing = 34.dp.toPx()
      var y = lineSpacing * 2f
      while (y < height - lineSpacing) {
        drawLine(
          color = Color(0xFFE3EAF2),
          start = Offset(24.dp.toPx(), y),
          end = Offset(width - 24.dp.toPx(), y),
          strokeWidth = 1.5f,
        )
        y += lineSpacing
      }
    }

    PaperTemplate.GRID -> {
      val step = 28.dp.toPx()
      var x = step
      while (x < width) {
        drawLine(
          color = Color(0xFFEAEAE6),
          start = Offset(x, 0f),
          end = Offset(x, height),
          strokeWidth = 1.2f,
        )
        x += step
      }
      var y = step
      while (y < height) {
        drawLine(
          color = Color(0xFFEAEAE6),
          start = Offset(0f, y),
          end = Offset(width, y),
          strokeWidth = 1.2f,
        )
        y += step
      }
    }

    PaperTemplate.DOTTED -> {
      val step = 26.dp.toPx()
      var x = step
      while (x < width - 12f) {
        var y = step
        while (y < height - 12f) {
          drawCircle(
            color = Color(0xFFD0D5DD),
            radius = 2.2f,
            center = Offset(x, y),
          )
          y += step
        }
        x += step
      }
    }

    PaperTemplate.CORNELL -> {
      val headerY = 72.dp.toPx()
      val summaryY = height - 110.dp.toPx()
      val cueX = width * 0.28f
      drawLine(
        color = Color(0xFFCB6D6D),
        start = Offset(0f, headerY),
        end = Offset(width, headerY),
        strokeWidth = 2f,
      )
      drawLine(
        color = Color(0xFFCB6D6D),
        start = Offset(cueX, headerY),
        end = Offset(cueX, summaryY),
        strokeWidth = 2f,
      )
      drawLine(
        color = Color(0xFFCB6D6D),
        start = Offset(0f, summaryY),
        end = Offset(width, summaryY),
        strokeWidth = 2f,
      )
      val spacing = 32.dp.toPx()
      var y = headerY + spacing
      while (y < summaryY - 8f) {
        drawLine(
          color = Color(0xFFE5EAF0),
          start = Offset(cueX + 8f, y),
          end = Offset(width - 16f, y),
          strokeWidth = 1.4f,
        )
        y += spacing
      }
    }
  }
}

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

  // Goodnotes 6 Highlighter uses BlendMode.Darken so dark text & pen ink underneath remain 100% crisp!
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

private fun distanceFromPointToSegment(p: Offset, a: Offset, b: Offset): Float {
  val ab = b - a
  val ap = p - a
  val lenSq = ab.x * ab.x + ab.y * ab.y
  if (lenSq == 0f) return ap.getDistance()
  val t = ((ap.x * ab.x + ap.y * ab.y) / lenSq).coerceIn(0f, 1f)
  val proj = Offset(a.x + t * ab.x, a.y + t * ab.y)
  return (p - proj).getDistance()
}
