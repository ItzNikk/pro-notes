package com.example

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.model.CustomPageTemplate
import com.example.model.DocumentType
import com.example.model.NotebookCoverStyle
import com.example.model.PageOrientation
import com.example.model.PageSizePreset
import com.example.model.PaperColorTheme
import com.example.model.PaperTemplate
import com.example.model.ScreenMode
import com.example.ui.components.TemplatePreviewCanvas
import com.example.ui.components.UnlockVaultDialog
import com.example.ui.screens.DocumentsScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LocalProNotesColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NewButtonBlue
import com.example.ui.theme.SwitchBlue
import com.example.util.PdfImportHelper
import com.example.viewmodel.ProNotesViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: ProNotesViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()

      MyApplicationTheme(themeMode = uiState.appThemeMode) {
        val colors = LocalProNotesColors.current
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = colors.background,
        ) {
          ProNotesAppRoot(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun ProNotesAppRoot(
  viewModel: ProNotesViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val ioScope = rememberCoroutineScope()

  // Stage 2F: Dynamically enforce or clear FLAG_SECURE on the Activity window when Privacy Screen Shield changes
  LaunchedEffect(uiState.privacyScreenShieldEnabled) {
    val window = (context as? Activity)?.window ?: return@LaunchedEffect
    if (uiState.privacyScreenShieldEnabled) {
      window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
      window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
  }

  // Stage 2A: Flush active document to disk whenever the Activity pauses or goes to background
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
        viewModel.flushActiveDocumentOnPause()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  // System File Picker for real PDF files
  val pdfPickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
      if (uri != null) {
        ioScope.launch {
          val imported = PdfImportHelper.importPdfFromUri(context, uri)
          if (imported != null) {
            viewModel.importPdfDocument(imported.first, imported.second)
          } else {
            val sample = PdfImportHelper.generateAndImportSamplePdf(context)
            viewModel.importPdfDocument(sample.first, sample.second)
          }
        }
      }
    }

  // System Image Picker for inserting gallery images onto the active page
  val imagePickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent(),
    ) { uri ->
      if (uri != null) {
        ioScope.launch {
          val bmp = PdfImportHelper.decodeImageFromUri(context, uri)
          if (bmp != null) {
            viewModel.insertCanvasImage(bmp)
          }
        }
      }
    }

  // System Picker for importing a Custom Page Template (Image or PDF first page)
  val customTemplatePickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent(),
    ) { uri ->
      if (uri != null) {
        ioScope.launch {
          val decoded = PdfImportHelper.decodeTemplateFromUri(context, uri)
          if (decoded != null) {
            viewModel.importCustomTemplate(decoded.first, decoded.second)
          }
        }
      }
    }

  // System CreateDocument launchers for saving exported PDF / PNG
  val exportPdfLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { destUri ->
      if (destUri != null) {
        val activeDoc = uiState.notebooks.find { it.id == uiState.activeTabId } ?: return@rememberLauncherForActivityResult
        ioScope.launch {
          val ok =
            PdfImportHelper.exportNotebookToPdfUri(
              context = context,
              document = activeDoc,
              destUri = destUri,
              includeConfidentialWatermark = uiState.watermarkConfidentialExports,
            )
          viewModel.showToast(
            if (ok) "Exported ${activeDoc.pages.size}-page PDF" else "Failed to export PDF"
          )
        }
      }
    }

  val exportPngLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.CreateDocument("image/png"),
    ) { destUri ->
      if (destUri != null) {
        val activeDoc = uiState.notebooks.find { it.id == uiState.activeTabId } ?: return@rememberLauncherForActivityResult
        ioScope.launch {
          val ok =
            PdfImportHelper.exportPageToPngUri(
              context = context,
              document = activeDoc,
              notePage = activeDoc.currentPage,
              destUri = destUri,
              includeConfidentialWatermark = uiState.watermarkConfidentialExports,
            )
          viewModel.showToast(if (ok) "Exported Page PNG" else "Failed to export PNG")
        }
      }
    }

  val colors = LocalProNotesColors.current

  val palmSensitivityFloat =
    when (uiState.palmRejectionSensitivity) {
      "Low" -> 35f
      "Medium" -> 65f
      else -> 90f
    }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(colors.background)
        .windowInsetsPadding(WindowInsets.safeDrawing),
  ) {
    when (uiState.screenMode) {
      ScreenMode.DOCUMENTS -> {
        val breadcrumbs =
          remember(uiState.folders, uiState.currentFolderId) {
            viewModel.getFolderBreadcrumbs()
          }
        DocumentsScreen(
          notebooks = uiState.notebooks,
          folders = uiState.folders,
          currentFolderId = uiState.currentFolderId,
          folderBreadcrumbs = breadcrumbs,
          selectedSection = uiState.sidebarSection,
          searchQuery = uiState.searchQuery,
          sortOption = uiState.sortOption,
          libraryViewMode = uiState.libraryViewMode,
          onSelectSection = viewModel::selectSidebarSection,
          onSearchQueryChange = viewModel::updateSearchQuery,
          onSelectSortOption = viewModel::selectSortOption,
          onToggleLibraryViewMode = viewModel::toggleLibraryViewMode,
          onOpenNotebook = viewModel::openDocument,
          onCreateNewNotebook = { viewModel.togglePopover(com.example.model.ActivePopover.NEW_NOTE_DIALOG) },
          onCreateNewWhiteboard = {
            viewModel.createNewWhiteboard(
              title = "Whiteboard ${(uiState.notebooks.count { it.documentType == DocumentType.WHITEBOARD } + 1)}",
            )
          },
          onImportPdfDocument = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
          onCreateFolder = { name, colorArgb -> viewModel.createFolder(name, colorArgb = colorArgb) },
          onNavigateToFolder = viewModel::navigateToFolder,
          onNavigateUpFolder = viewModel::navigateUpFolder,
          onRenameFolder = viewModel::renameFolder,
          onMoveFolder = viewModel::moveFolder,
          onCanMoveFolder = { fldId, targetId -> viewModel.canMoveFolder(fldId, targetId) },
          onMoveFolderToTrash = viewModel::moveFolderToTrash,
          onRestoreFolderFromTrash = viewModel::moveFolderToTrash,
          onDeleteFolderPermanently = viewModel::deleteFolderPermanently,
          onRenameNotebook = viewModel::renameNotebook,
          onMoveNotebookToFolder = viewModel::moveNotebookToFolder,
          onToggleFavorite = viewModel::toggleFavorite,
          onMoveToTrash = viewModel::moveNotebookToTrash,
          onRestoreFromTrash = viewModel::moveNotebookToTrash,
          onDeletePermanently = viewModel::deleteNotebookPermanently,
          onOpenSettings = viewModel::openSettingsScreen,
        )
      }

      ScreenMode.SETTINGS -> {
        BackHandler { viewModel.closeSettingsScreen() }

        val activeDocs = uiState.notebooks.filter { !it.isTrashed }
        val activeFolders = uiState.folders.count { !it.isTrashed }
        val trashedDocs =
          uiState.notebooks.count { it.isTrashed } + uiState.folders.count { it.isTrashed }
        val totalPages = activeDocs.sumOf { it.pages.size }

        SettingsScreen(
          appThemeMode = uiState.appThemeMode,
          onlyDrawWithStylus = uiState.onlyDrawWithStylus,
          palmRejectionSensitivity = palmSensitivityFloat,
          leftHandedPosture = uiState.isLeftHandedPosture,
          pullToAddPage = uiState.pullToAddPage,
          pageScrollDirection = uiState.pageScrollDirection,
          toolbarDockPosition = uiState.toolbarDockPosition,
          eraserAutoDeselect = uiState.eraserAutoDeselect,
          drawAndHold = uiState.drawAndHold,
          circleToLasso = uiState.circleToLasso,
          scribbleToErase = uiState.scribbleToErase,
          holdToErase = uiState.holdToErase,
          privacyScreenShieldEnabled = uiState.privacyScreenShieldEnabled,
          watermarkConfidentialExports = uiState.watermarkConfidentialExports,
          activeNotebookCount = activeDocs.size,
          activeFolderCount = activeFolders,
          trashedNotebookCount = trashedDocs,
          totalPagesCount = totalPages,
          onSelectAppThemeMode = viewModel::setAppThemeMode,
          onToggleOnlyDrawWithStylus = viewModel::toggleOnlyDrawWithStylus,
          onPalmRejectionSensitivityChange = { v ->
            val label =
              when {
                v < 45f -> "Low"
                v < 75f -> "Medium"
                else -> "High"
              }
            viewModel.setPalmRejectionSensitivity(label)
          },
          onToggleLeftHandedPosture = viewModel::toggleLeftHandedPosture,
          onTogglePullToAddPage = viewModel::togglePullToAddPage,
          onSelectPageScrollDirection = viewModel::setPageScrollDirection,
          onSelectToolbarDockPosition = viewModel::setToolbarDockPosition,
          onResetSubToolbarPosition = viewModel::resetSubToolbarPosition,
          onToggleEraserAutoDeselect = viewModel::toggleEraserAutoDeselect,
          onToggleDrawAndHold = viewModel::toggleDrawAndHold,
          onToggleCircleToLasso = viewModel::toggleCircleToLasso,
          onToggleScribbleToErase = viewModel::toggleScribbleToErase,
          onToggleHoldToErase = viewModel::toggleHoldToErase,
          onTogglePrivacyScreenShield = viewModel::togglePrivacyScreenShield,
          onToggleWatermarkConfidentialExports = viewModel::toggleWatermarkConfidentialExports,
          onEmptyTrash = viewModel::emptyTrash,
          onResetAllDataToDefaults = viewModel::clearAllLocalStorage,
          onNavigateBack = viewModel::closeSettingsScreen,
          onReplayOnboarding = viewModel::replayOnboardingDebug,
        )
      }

      ScreenMode.EDITOR -> {
        BackHandler {
          if (uiState.activePopover != com.example.model.ActivePopover.NONE) {
            viewModel.dismissPopover()
          } else {
            viewModel.switchToDocuments()
          }
        }

        val activeDoc =
          uiState.notebooks.find { it.id == uiState.activeTabId } ?: uiState.notebooks.firstOrNull()
        val activePage = activeDoc?.currentPage
        val docSha256 =
          remember(activeDoc?.id, activeDoc?.revision, activePage?.strokes?.size, activePage?.textBoxes?.size) {
            if (activeDoc != null) {
              PdfImportHelper.hashPinSha256(activeDoc.id, "${activeDoc.title}:${activeDoc.revision}:${activeDoc.pages.size}")
            } else {
              "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
            }
          }

        val clip = uiState.lassoClipboard
        val hasClip =
          clip != null &&
            (clip.strokes.isNotEmpty() ||
              clip.textBoxes.isNotEmpty() ||
              clip.elements.isNotEmpty() ||
              clip.stickyNotes.isNotEmpty() ||
              clip.images.isNotEmpty())

        EditorScreen(
          uiState = uiState,
          viewModel = viewModel,
          palmRejectionSensitivity = palmSensitivityFloat,
          hasClipboardContent = hasClip,
          documentSha256Hash = docSha256,
          onImportPdfFromDevice = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
          onImportImageFromDevice = { imagePickerLauncher.launch("image/*") },
          onImportCustomTemplateFromDevice = { customTemplatePickerLauncher.launch("*/*") },
          onExportFullPdf = {
            if (activeDoc != null) {
              ioScope.launch {
                val outFile = File(context.cacheDir, "${activeDoc.title.replace(" ", "_")}.pdf")
                val destUri = android.net.Uri.fromFile(outFile)
                val ok =
                  PdfImportHelper.exportNotebookToPdfUri(
                    context = context,
                    document = activeDoc,
                    destUri = destUri,
                    includeConfidentialWatermark = uiState.watermarkConfidentialExports,
                  )
                if (ok) {
                  viewModel.showToast("Exported ${activeDoc.pages.size}-page PDF (${outFile.name})")
                  runCatching { exportPdfLauncher.launch("${activeDoc.title}.pdf") }
                } else {
                  viewModel.showToast("Failed to export PDF")
                }
              }
            }
          },
          onExportCurrentPagePng = {
            if (activeDoc != null) {
              ioScope.launch {
                val outFile =
                  File(
                    context.cacheDir,
                    "${activeDoc.title.replace(" ", "_")}_Page_${activeDoc.currentPageIndex + 1}.png",
                  )
                val destUri = android.net.Uri.fromFile(outFile)
                val ok =
                  PdfImportHelper.exportPageToPngUri(
                    context = context,
                    document = activeDoc,
                    notePage = activeDoc.currentPage,
                    destUri = destUri,
                    includeConfidentialWatermark = uiState.watermarkConfidentialExports,
                  )
                if (ok) {
                  viewModel.showToast("Exported Page PNG (${outFile.name})")
                  runCatching { exportPngLauncher.launch(outFile.name) }
                }
              }
            }
          },
        )
      }
    }

    // Create Document & Custom Template Studio Dialog (Stages 6 & 7)
    if (uiState.activePopover == com.example.model.ActivePopover.NEW_NOTE_DIALOG) {
      CreateNotebookStudioDialog(
        customTemplates = uiState.customTemplates,
        onCreateNotebook = { title, cover, template, colorTheme, docType, pageSize, orientation, customTmpl ->
          if (docType == DocumentType.WHITEBOARD) {
            viewModel.createNewWhiteboard(title)
          } else {
            viewModel.createNewNotebook(
              title = title,
              coverStyle = cover,
              paperTemplate = template,
              paperColorTheme = colorTheme,
              documentType = docType,
              pageSizePreset = pageSize,
              pageOrientation = orientation,
              customTemplate = customTmpl,
            )
          }
        },
        onPickPdfFromDevice = {
          pdfPickerLauncher.launch(arrayOf("application/pdf"))
        },
        onLoadSamplePdf = {
          ioScope.launch {
            val sample =
              withContext(Dispatchers.IO) {
                PdfImportHelper.generateAndImportSamplePdf(context)
              }
            viewModel.importPdfDocument(sample.first, sample.second)
          }
        },
        onImportCustomTemplate = {
          customTemplatePickerLauncher.launch("*/*")
        },
        onDismiss = viewModel::dismissPopover,
      )
    }

    // Vault PIN Unlock Modal when tapping a locked notebook
    val pendingVaultId = uiState.pendingUnlockNotebookId
    if (pendingVaultId != null) {
      val lockedDoc = uiState.notebooks.find { it.id == pendingVaultId }
      UnlockVaultDialog(
        notebookTitle = lockedDoc?.title ?: "Locked Vault Notebook",
        onAttemptUnlock = { pin -> viewModel.attemptUnlockVaultNotebook(pendingVaultId, pin) },
        onDismiss = viewModel::dismissPendingVaultUnlock,
      )
    }

    if (uiState.isOnboardingVisible) {
      OnboardingScreen(
        onStartCreating = viewModel::completeOnboarding,
      )
    }
  }
}

/**
 * Redesigned Goodnotes-style Create Document & Custom Template Studio Dialog (Stages 6 & 7):
 * - Document Type: Notebook vs Whiteboard
 * - Cover Picker, Paper Size (Goodnotes Standard, A4, US Letter), Orientation (Portrait, Landscape)
 * - Paper Color Theme (Pure White, Goodnotes Cream, Night Dark, Sage Eye-Care)
 * - Visual Template Gallery with all 10 built-in templates + imported custom templates + "+ Import Custom Template"
 */
@Composable
private fun CreateNotebookStudioDialog(
  customTemplates: List<CustomPageTemplate>,
  onCreateNotebook: (
    String,
    NotebookCoverStyle,
    PaperTemplate,
    PaperColorTheme,
    DocumentType,
    PageSizePreset,
    PageOrientation,
    CustomPageTemplate?,
  ) -> Unit,
  onPickPdfFromDevice: () -> Unit,
  onLoadSamplePdf: () -> Unit,
  onImportCustomTemplate: () -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  var title by remember { mutableStateOf("Study Notebook") }
  var selectedDocType by remember { mutableStateOf(DocumentType.NOTEBOOK) }
  var selectedCover by remember { mutableStateOf(NotebookCoverStyle.KRAFT_TAN) }
  var selectedTemplate by remember { mutableStateOf(PaperTemplate.RULED) }
  var selectedCustomTemplate by remember { mutableStateOf<CustomPageTemplate?>(null) }
  var selectedColorTheme by remember { mutableStateOf(PaperColorTheme.PURE_WHITE) }
  var selectedPageSize by remember { mutableStateOf(PageSizePreset.GOODNOTES_STANDARD) }
  var selectedOrientation by remember { mutableStateOf(PageOrientation.PORTRAIT) }

  val builtInTemplates =
    remember {
      PaperTemplate.entries.filter { it != PaperTemplate.CUSTOM }
    }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.64f))
        .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 520.dp)
          .fillMaxWidth(0.94f)
          .shadow(24.dp, RoundedCornerShape(18.dp))
          .clip(RoundedCornerShape(18.dp))
          .background(colors.popoverBg)
          .border(1.dp, colors.border, RoundedCornerShape(18.dp))
          .clickable(enabled = false) {}
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
          .testTag("create_notebook_dialog"),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "New Document & Template Studio",
          color = colors.textPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
        )
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close",
          tint = colors.textSecondary,
          modifier = Modifier.size(20.dp).clickable(onClick = onDismiss),
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Quick PDF Import Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier =
            Modifier
              .weight(1f)
              .height(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF17325A))
              .border(1.dp, SwitchBlue, RoundedCornerShape(10.dp))
              .clickable(onClick = onPickPdfFromDevice)
              .padding(horizontal = 10.dp)
              .testTag("dialog_import_pdf_device_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Outlined.PictureAsPdf,
            contentDescription = null,
            tint = Color(0xFF67C8EB),
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Import PDF File",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }

        Row(
          modifier =
            Modifier
              .weight(1f)
              .height(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(colors.surfaceVariant)
              .border(1.dp, colors.border, RoundedCornerShape(10.dp))
              .clickable(onClick = onLoadSamplePdf)
              .padding(horizontal = 10.dp)
              .testTag("dialog_sample_pdf_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Text(
            text = "Sample Study PDF",
            color = colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
          )
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.divider)

      // Document Type Selector: Notebook vs Whiteboard
      Text(
        text = "DOCUMENT TYPE",
        color = colors.textSecondary,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        DocumentType.entries.forEach { docType ->
          val isSel = selectedDocType == docType
          Box(
            modifier =
              Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSel) SwitchBlue else colors.surfaceVariant)
                .clickable {
                  selectedDocType = docType
                  if (docType == DocumentType.WHITEBOARD && title == "Study Notebook") {
                    title = "Infinite Whiteboard"
                  } else if (docType == DocumentType.NOTEBOOK && title == "Infinite Whiteboard") {
                    title = "Study Notebook"
                  }
                }
                .testTag("doc_type_${docType.name.lowercase()}"),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = docType.label,
              color = if (isSel) Color.White else colors.textPrimary,
              fontSize = 12.5.sp,
              fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "DOCUMENT TITLE",
        color = colors.textSecondary,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        singleLine = true,
        colors =
          OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedBorderColor = SwitchBlue,
            unfocusedBorderColor = colors.border,
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
          ),
        modifier = Modifier.fillMaxWidth().testTag("new_notebook_title_input"),
      )

      if (selectedDocType == DocumentType.NOTEBOOK) {
        Spacer(modifier = Modifier.height(12.dp))

        // Paper Size & Orientation Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Column(modifier = Modifier.weight(1.4f)) {
            Text(
              text = "PAPER SIZE",
              color = colors.textSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              PageSizePreset.entries.forEach { preset ->
                val isSel = selectedPageSize == preset
                Box(
                  modifier =
                    Modifier
                      .weight(1f)
                      .height(32.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isSel) SwitchBlue else colors.surfaceVariant)
                      .clickable { selectedPageSize = preset }
                      .padding(horizontal = 4.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = preset.label.substringBefore(" "),
                    color = if (isSel) Color.White else colors.textPrimary,
                    fontSize = 10.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                  )
                }
              }
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "ORIENTATION",
              color = colors.textSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              PageOrientation.entries.forEach { orient ->
                val isSel = selectedOrientation == orient
                Box(
                  modifier =
                    Modifier
                      .weight(1f)
                      .height(32.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isSel) SwitchBlue else colors.surfaceVariant)
                      .clickable { selectedOrientation = orient }
                      .padding(horizontal = 4.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = orient.label,
                    color = if (isSel) Color.White else colors.textPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "COVER STYLE",
          color = colors.textSecondary,
          fontSize = 10.5.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          listOf(
            NotebookCoverStyle.KRAFT_TAN,
            NotebookCoverStyle.CHARCOAL_DARK,
            NotebookCoverStyle.WHITEBOARD,
            NotebookCoverStyle.GRID_PAPER,
          ).forEach { cover ->
            val isSelected = cover == selectedCover
            Box(
              modifier =
                Modifier
                  .weight(1f)
                  .height(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) SwitchBlue else colors.surfaceVariant)
                  .clickable { selectedCover = cover }
                  .padding(horizontal = 4.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = cover.label.substringBefore(" "),
                color = if (isSelected) Color.White else colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "PAPER COLOR THEME",
        color = colors.textSecondary,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        PaperColorTheme.entries.forEach { theme ->
          val isSelected = theme == selectedColorTheme
          Row(
            modifier =
              Modifier
                .weight(1f)
                .height(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) SwitchBlue.copy(alpha = 0.28f) else colors.surfaceVariant)
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) SwitchBlue else colors.border,
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
              color = colors.textPrimary,
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              maxLines = 1,
            )
          }
        }
      }

      if (selectedDocType == DocumentType.NOTEBOOK) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "PAPER TEMPLATE (${builtInTemplates.size + customTemplates.size} TEMPLATES)",
            color = colors.textSecondary,
            fontSize = 10.5.sp,
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
                .testTag("dialog_import_custom_template_button"),
          )
        }
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          items(builtInTemplates) { tmpl ->
            val isSelected = tmpl == selectedTemplate && selectedCustomTemplate == null
            Column(
              modifier =
                Modifier
                  .width(82.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) SwitchBlue.copy(alpha = 0.22f) else colors.surfaceVariant)
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) SwitchBlue else colors.border,
                    shape = RoundedCornerShape(8.dp),
                  )
                  .clickable {
                    selectedTemplate = tmpl
                    selectedCustomTemplate = null
                  }
                  .padding(6.dp)
                  .testTag("template_option_${tmpl.name.lowercase()}"),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              TemplatePreviewCanvas(
                template = tmpl,
                paperColorTheme = selectedColorTheme,
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(4.dp)),
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = tmpl.label,
                color = colors.textPrimary,
                fontSize = 9.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          items(customTemplates) { customTmpl ->
            val isSelected = selectedCustomTemplate?.id == customTmpl.id
            Column(
              modifier =
                Modifier
                  .width(82.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) SwitchBlue.copy(alpha = 0.22f) else colors.surfaceVariant)
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) SwitchBlue else colors.border,
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
                    .height(68.dp)
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
      }

      Spacer(modifier = Modifier.height(18.dp))

      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(NewButtonBlue)
            .clickable {
              val cleanTitle =
                title.trim().ifEmpty {
                  if (selectedDocType == DocumentType.WHITEBOARD) "Untitled Whiteboard"
                  else "Untitled Notebook"
                }
              onCreateNotebook(
                cleanTitle,
                selectedCover,
                selectedTemplate,
                selectedColorTheme,
                selectedDocType,
                selectedPageSize,
                selectedOrientation,
                selectedCustomTemplate,
              )
            }
            .testTag("confirm_create_notebook_button"),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text =
            if (selectedDocType == DocumentType.WHITEBOARD) "Create Whiteboard"
            else "Create Notebook",
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}
