package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotebookCoverStyle
import com.example.model.NotebookDocument
import com.example.model.PaperTemplate
import com.example.model.SidebarSection
import com.example.ui.components.SecurityVaultShieldIcon
import com.example.ui.theme.CharcoalCover
import com.example.ui.theme.CharcoalCoverAccent
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DocumentsMainBg
import com.example.ui.theme.GridPaperBg
import com.example.ui.theme.KraftSpineLight
import com.example.ui.theme.KraftTanCover
import com.example.ui.theme.NewButtonBlue
import com.example.ui.theme.PopoverBg
import com.example.ui.theme.PopoverDivider
import com.example.ui.theme.PopoverInnerBg
import com.example.ui.theme.SidebarActiveBlueIcon
import com.example.ui.theme.SidebarActiveBluePill
import com.example.ui.theme.SidebarDarkBg
import com.example.ui.theme.SortPillBg
import com.example.ui.theme.SwitchBlue
import com.example.ui.theme.SyncOffRed
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray
import com.example.ui.theme.WhiteboardBg

@Composable
fun DocumentsScreen(
  selectedSection: SidebarSection,
  notebooks: List<NotebookDocument>,
  sortByDateAscending: Boolean,
  onlyDrawWithStylus: Boolean,
  isSearchBarVisible: Boolean,
  searchQuery: String,
  onSelectSection: (SidebarSection) -> Unit,
  onOpenNotebook: (String) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onMoveToTrash: (String) -> Unit,
  onDeletePermanently: (String) -> Unit,
  onOpenCreateNoteDialog: () -> Unit,
  onCreateNewNotebook: (NotebookCoverStyle, PaperTemplate) -> Unit,
  onImportPdfFromDevice: () -> Unit,
  onImportSamplePdf: () -> Unit,
  onToggleSortOrder: () -> Unit,
  onToggleOnlyDrawWithStylus: () -> Unit,
  onToggleSearchBar: () -> Unit,
  onUpdateSearchQuery: (String) -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(
    modifier = modifier.fillMaxSize().background(DocumentsMainBg)
  ) {
    val isCompact = maxWidth < 580.dp
    val isTablet = maxWidth >= 720.dp
    val sidebarWidth =
      when {
        isTablet -> 228.dp
        isCompact -> 142.dp
        else -> 196.dp
      }

    Row(modifier = Modifier.fillMaxSize()) {
      // Left Sidebar (1:1 from Screenshot 2)
      Column(
        modifier =
          Modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(SidebarDarkBg)
            .padding(horizontal = if (isCompact) 8.dp else 14.dp, vertical = 18.dp),
      ) {
        Text(
          text = "Pro Notes",
          color = TextPrimaryWhite,
          fontSize = if (isCompact) 18.sp else 23.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 6.dp, bottom = 18.dp),
        )

        SidebarNavItem(
          label = SidebarSection.DOCUMENTS.label,
          icon = Icons.Default.Folder,
          isSelected = selectedSection == SidebarSection.DOCUMENTS,
          isCompact = isCompact,
          onClick = { onSelectSection(SidebarSection.DOCUMENTS) },
        )
        Spacer(modifier = Modifier.height(4.dp))
        SidebarNavItem(
          label = SidebarSection.FAVORITES.label,
          icon = Icons.Outlined.BookmarkBorder,
          isSelected = selectedSection == SidebarSection.FAVORITES,
          isCompact = isCompact,
          onClick = { onSelectSection(SidebarSection.FAVORITES) },
        )
        Spacer(modifier = Modifier.height(4.dp))
        SidebarNavItem(
          label = SidebarSection.SHARED.label,
          icon = Icons.Outlined.Group,
          isSelected = selectedSection == SidebarSection.SHARED,
          isCompact = isCompact,
          onClick = { onSelectSection(SidebarSection.SHARED) },
        )
        Spacer(modifier = Modifier.height(4.dp))
        SidebarNavItem(
          label = SidebarSection.MARKETPLACE.label,
          icon = Icons.Outlined.Storefront,
          isSelected = selectedSection == SidebarSection.MARKETPLACE,
          isCompact = isCompact,
          onClick = { onSelectSection(SidebarSection.MARKETPLACE) },
        )

        HorizontalDivider(
          modifier = Modifier.padding(vertical = 14.dp),
          thickness = 1.dp,
          color = Color(0xFF26272B),
        )

        SidebarNavItem(
          label = SidebarSection.TRASH.label,
          icon = Icons.Outlined.DeleteOutline,
          isSelected = selectedSection == SidebarSection.TRASH,
          isCompact = isCompact,
          onClick = { onSelectSection(SidebarSection.TRASH) },
        )

        Spacer(modifier = Modifier.weight(1f))

        SidebarNavItem(
          label = "Settings",
          icon = Icons.Outlined.Settings,
          isSelected = false,
          isCompact = isCompact,
          onClick = onOpenSettings,
        )
      }

      // Vertical separator line
      Box(
        modifier =
          Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(Color(0xFF26272B))
      )

      // Right Main Documents Content Area
      val displayedNotebooks =
        remember(notebooks, selectedSection, sortByDateAscending, searchQuery) {
          val sectionFiltered =
            when (selectedSection) {
              SidebarSection.DOCUMENTS -> notebooks.filter { !it.isTrashed }
              SidebarSection.FAVORITES -> notebooks.filter { it.isFavorite && !it.isTrashed }
              SidebarSection.TRASH -> notebooks.filter { it.isTrashed }
              else -> notebooks.filter { !it.isTrashed }
            }
          val queryFiltered =
            if (searchQuery.isBlank()) sectionFiltered
            else sectionFiltered.filter { it.title.contains(searchQuery, ignoreCase = true) }
          if (sortByDateAscending) queryFiltered.reversed() else queryFiltered
        }

      Column(
        modifier =
          Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(DocumentsMainBg)
            .padding(horizontal = if (isCompact) 14.dp else 28.dp, vertical = 12.dp),
      ) {
        // Top Utility Bar (Stylus Mode toggle on left, Search & Settings on right)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier =
              Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onToggleOnlyDrawWithStylus)
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .testTag("stylus_mode_status_toggle"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = if (onlyDrawWithStylus) Icons.Outlined.Draw else Icons.Outlined.EditOff,
              contentDescription = "Stylus mode",
              tint = if (onlyDrawWithStylus) Color(0xFF34C759) else SyncOffRed,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = if (onlyDrawWithStylus) "Stylus Only (Finger Zoom)" else "Stylus + Touch Draw",
              color = TextSecondaryGray,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
            )
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Outlined.Search,
              contentDescription = "Search documents",
              tint = if (isSearchBarVisible) SwitchBlue else TextPrimaryWhite.copy(alpha = 0.85f),
              modifier =
                Modifier
                  .size(20.dp)
                  .clickable(onClick = onToggleSearchBar)
                  .testTag("documents_search_button"),
            )
            Icon(
              imageVector = Icons.Outlined.Settings,
              contentDescription = "Settings",
              tint = TextPrimaryWhite.copy(alpha = 0.85f),
              modifier =
                Modifier
                  .size(20.dp)
                  .clickable(onClick = onOpenSettings)
                  .testTag("documents_settings_button"),
            )
          }
        }

        AnimatedVisibility(
          visible = isSearchBarVisible,
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = onUpdateSearchQuery,
            placeholder = {
              Text("Search notebooks & PDFs...", color = TextSecondaryGray, fontSize = 12.sp)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear search",
                  tint = TextSecondaryGray,
                  modifier = Modifier.size(16.dp).clickable { onUpdateSearchQuery("") },
                )
              }
            },
            singleLine = true,
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimaryWhite,
                unfocusedTextColor = TextPrimaryWhite,
                focusedBorderColor = SwitchBlue,
                unfocusedBorderColor = PopoverDivider,
                focusedContainerColor = PopoverInnerBg,
                unfocusedContainerColor = PopoverInnerBg,
              ),
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("documents_search_input"),
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Header Row: "Documents" title + "+ New v", "Import PDF", "Date v", Checkmark icon
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = selectedSection.label,
            color = TextPrimaryWhite,
            fontSize = if (isCompact) 21.sp else 28.sp,
            fontWeight = FontWeight.Bold,
          )

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (!isCompact) {
              Row(
                modifier =
                  Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(SortPillBg)
                    .border(1.dp, Color(0xFF3E8BF7).copy(alpha = 0.55f), RoundedCornerShape(15.dp))
                    .clickable(onClick = onImportPdfFromDevice)
                    .padding(horizontal = 12.dp)
                    .testTag("documents_import_pdf_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
              ) {
                Icon(
                  imageVector = Icons.Outlined.PictureAsPdf,
                  contentDescription = null,
                  tint = Color(0xFF67C8EB),
                  modifier = Modifier.size(14.dp),
                )
                Text(
                  text = "Import PDF",
                  color = TextPrimaryWhite,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                )
              }
            }

            var showNewMenu by remember { mutableStateOf(false) }
            Box {
              Row(
                modifier =
                  Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(NewButtonBlue)
                    .clickable { showNewMenu = true }
                    .padding(horizontal = 12.dp)
                    .testTag("documents_new_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  tint = TextPrimaryWhite,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text = "New",
                  color = TextPrimaryWhite,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                )
                Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = null,
                  tint = TextPrimaryWhite,
                  modifier = Modifier.size(14.dp),
                )
              }

              DropdownMenu(
                expanded = showNewMenu,
                onDismissRequest = { showNewMenu = false },
                modifier = Modifier.background(PopoverBg),
              ) {
                DropdownMenuItem(
                  text = { Text("Notebook Studio (Custom...)", color = Color(0xFF67C8EB), fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                  onClick = {
                    showNewMenu = false
                    onOpenCreateNoteDialog()
                  },
                )
                DropdownMenuItem(
                  text = { Text("Import PDF from Device...", color = TextPrimaryWhite, fontSize = 13.sp) },
                  onClick = {
                    showNewMenu = false
                    onImportPdfFromDevice()
                  },
                )
                DropdownMenuItem(
                  text = { Text("Import Sample Study PDF", color = TextPrimaryWhite, fontSize = 13.sp) },
                  onClick = {
                    showNewMenu = false
                    onImportSamplePdf()
                  },
                )
                DropdownMenuItem(
                  text = { Text("Quick Blank Notebook", color = TextPrimaryWhite, fontSize = 13.sp) },
                  onClick = {
                    showNewMenu = false
                    onCreateNewNotebook(NotebookCoverStyle.KRAFT_TAN, PaperTemplate.BLANK)
                  },
                )
                DropdownMenuItem(
                  text = { Text("Ruled Notebook", color = TextPrimaryWhite, fontSize = 13.sp) },
                  onClick = {
                    showNewMenu = false
                    onCreateNewNotebook(NotebookCoverStyle.CHARCOAL_DARK, PaperTemplate.RULED)
                  },
                )
                DropdownMenuItem(
                  text = { Text("Cornell Notes Notebook", color = TextPrimaryWhite, fontSize = 13.sp) },
                  onClick = {
                    showNewMenu = false
                    onCreateNewNotebook(NotebookCoverStyle.KRAFT_TAN, PaperTemplate.CORNELL)
                  },
                )
                DropdownMenuItem(
                  text = { Text("Whiteboard", color = TextPrimaryWhite, fontSize = 13.sp) },
                  onClick = {
                    showNewMenu = false
                    onCreateNewNotebook(NotebookCoverStyle.WHITEBOARD, PaperTemplate.BLANK)
                  },
                )
              }
            }

            // "Date v" pill
            Row(
              modifier =
                Modifier
                  .height(30.dp)
                  .clip(RoundedCornerShape(15.dp))
                  .background(SortPillBg)
                  .clickable(onClick = onToggleSortOrder)
                  .padding(horizontal = 12.dp)
                  .testTag("documents_sort_date_button"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = "Date",
                color = TextPrimaryWhite.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
              )
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondaryGray,
                modifier = Modifier.size(14.dp),
              )
            }

            Icon(
              imageVector = Icons.Outlined.CheckCircle,
              contentDescription = "Select documents",
              tint = TextSecondaryGray,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (displayedNotebooks.isEmpty()) {
          // Clean, local-first Goodnotes 6 Empty State (Zero pre-made notes)
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              modifier =
                Modifier
                  .width(380.dp)
                  .clip(RoundedCornerShape(18.dp))
                  .background(PopoverBg)
                  .border(1.dp, PopoverDivider, RoundedCornerShape(18.dp))
                  .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Icon(
                imageVector = Icons.Outlined.NoteAdd,
                contentDescription = null,
                tint = Color(0xFF67C8EB),
                modifier = Modifier.size(44.dp),
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text =
                  if (selectedSection == SidebarSection.TRASH) "Trash is Empty"
                  else "Your Local Library is Ready",
                color = TextPrimaryWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text =
                  if (selectedSection == SidebarSection.TRASH) {
                    "Deleted notebooks will appear here before permanent removal."
                  } else {
                    "Create a new notebook, whiteboard, or import a PDF. All notes are automatically saved locally on your device."
                  },
                color = TextSecondaryGray,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
              )
              if (selectedSection != SidebarSection.TRASH) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Row(
                    modifier =
                      Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NewButtonBlue)
                        .clickable {
                          onCreateNewNotebook(NotebookCoverStyle.KRAFT_TAN, PaperTemplate.RULED)
                        }
                        .testTag("empty_state_quick_notebook_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "New Notebook",
                      color = Color.White,
                      fontSize = 12.5.sp,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }

                  Row(
                    modifier =
                      Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SortPillBg)
                        .border(1.dp, SwitchBlue, RoundedCornerShape(10.dp))
                        .clickable(onClick = onImportPdfFromDevice)
                        .testTag("empty_state_import_pdf_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.PictureAsPdf,
                      contentDescription = null,
                      tint = Color(0xFF67C8EB),
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Import PDF",
                      color = TextPrimaryWhite,
                      fontSize = 12.5.sp,
                      fontWeight = FontWeight.Medium,
                    )
                  }
                }
              }
            }
          }
        } else {
          // Documents Grid (1:1 Notebook & Whiteboard Covers + PDF Thumbnails)
          LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = if (isCompact) 112.dp else 138.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 14.dp else 22.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
          ) {
            items(displayedNotebooks, key = { it.id }) { doc ->
              NotebookGridItem(
                document = doc,
                onClick = { onOpenNotebook(doc.id) },
                onToggleFavorite = { onToggleFavorite(doc.id) },
                onMoveToTrash = { onMoveToTrash(doc.id) },
                onDeletePermanently = { onDeletePermanently(doc.id) },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SidebarNavItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  isCompact: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(38.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(if (isSelected) SidebarActiveBluePill else Color.Transparent)
        .clickable(onClick = onClick)
        .padding(horizontal = 10.dp)
        .testTag("sidebar_item_${label.lowercase()}"),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (isSelected) SidebarActiveBlueIcon else TextSecondaryGray,
      modifier = Modifier.size(18.dp),
    )
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = label,
      color = if (isSelected) TextPrimaryWhite else TextPrimaryWhite.copy(alpha = 0.85f),
      fontSize = if (isCompact) 12.sp else 13.5.sp,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun NotebookGridItem(
  document: NotebookDocument,
  onClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  onMoveToTrash: () -> Unit,
  onDeletePermanently: () -> Unit,
) {
  var showCardMenu by remember { mutableStateOf(false) }

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .testTag("notebook_card_${document.id}"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val isLandscapeCard =
      document.coverStyle == NotebookCoverStyle.WHITEBOARD ||
        document.coverStyle == NotebookCoverStyle.HANDWRITTEN_NOTES

    Box(
      modifier =
        Modifier
          .height(138.dp)
          .fillMaxWidth(),
      contentAlignment = Alignment.BottomCenter,
    ) {
      val cardHeight = if (isLandscapeCard && !document.isImportedPdf) 104.dp else 134.dp
      Box(
        modifier =
          Modifier
            .fillMaxWidth(0.92f)
            .height(cardHeight)
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp)),
      ) {
        val firstPagePdfBitmap = document.pages.firstOrNull()?.pdfPageBitmap
        if (firstPagePdfBitmap != null) {
          Image(
            bitmap = firstPagePdfBitmap.asImageBitmap(),
            contentDescription = document.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().background(Color.White),
          )
        } else {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (document.coverStyle) {
              NotebookCoverStyle.KRAFT_TAN -> {
                drawRoundRect(
                  color = Color(0xFFF3F2EE),
                  size = Size(w, h),
                  cornerRadius = CornerRadius(20f, 20f),
                )
                val coverH = h * 0.94f
                drawRoundRect(
                  color = KraftTanCover,
                  size = Size(w * 0.96f, coverH),
                  cornerRadius = CornerRadius(16f, 16f),
                )
                drawRect(
                  color = KraftSpineLight,
                  topLeft = Offset(0f, 0f),
                  size = Size(w * 0.13f, coverH),
                )
              }

              NotebookCoverStyle.CHARCOAL_DARK -> {
                drawRoundRect(
                  color = Color(0xFFEAECEF),
                  size = Size(w, h),
                  cornerRadius = CornerRadius(20f, 20f),
                )
                val coverH = h * 0.94f
                drawRoundRect(
                  color = CharcoalCover,
                  size = Size(w * 0.96f, coverH),
                  cornerRadius = CornerRadius(16f, 16f),
                )
                drawRect(
                  color = Color(0xFF161719),
                  topLeft = Offset(0f, 0f),
                  size = Size(w * 0.08f, coverH),
                )
                drawLine(
                  color = CharcoalCoverAccent,
                  start = Offset(w * 0.18f, coverH * 0.25f),
                  end = Offset(w * 0.55f, coverH * 0.25f),
                  strokeWidth = 5f,
                  cap = StrokeCap.Round,
                )
                for (i in 0..2) {
                  val y = coverH * (0.68f + i * 0.08f)
                  drawLine(
                    color = Color(0xFF5A5D64),
                    start = Offset(w * 0.65f, y),
                    end = Offset(w * 0.88f, y),
                    strokeWidth = 3f,
                  )
                }
              }

              NotebookCoverStyle.WHITEBOARD -> {
                drawRoundRect(
                  color = WhiteboardBg,
                  size = Size(w, h),
                  cornerRadius = CornerRadius(20f, 20f),
                )
                drawRoundRect(
                  color = Color(0xFFDCDFE5),
                  topLeft = Offset(6f, 6f),
                  size = Size(w - 12f, h - 18f),
                  cornerRadius = CornerRadius(14f, 14f),
                  style = Stroke(width = 3f),
                )
                for (i in 0..2) {
                  val y = h * (0.42f + i * 0.08f)
                  drawLine(
                    color = Color(0xFFD0D4DC),
                    start = Offset(w * 0.42f, y),
                    end = Offset(w * 0.58f, y),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                  )
                }
              }

              NotebookCoverStyle.GRID_PAPER -> {
                drawRoundRect(
                  color = GridPaperBg,
                  size = Size(w, h),
                  cornerRadius = CornerRadius(20f, 20f),
                )
                val step = 14f
                var x = step
                while (x < w) {
                  drawLine(
                    color = Color(0xFFE0E0DC),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.2f,
                  )
                  x += step
                }
                var y = step
                while (y < h) {
                  drawLine(
                    color = Color(0xFFE0E0DC),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.2f,
                  )
                  y += step
                }
              }

              NotebookCoverStyle.HANDWRITTEN_NOTES -> {
                drawRoundRect(
                  color = Color.White,
                  size = Size(w, h),
                  cornerRadius = CornerRadius(20f, 20f),
                )
                for (i in 1..6) {
                  val y = h * (i / 7f)
                  drawLine(
                    color = Color(0xFFE5EAF0),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.5f,
                  )
                }
                drawLine(
                  color = Color(0xFF2855A8),
                  start = Offset(w * 0.14f, h * 0.45f),
                  end = Offset(w * 0.65f, h * 0.45f),
                  strokeWidth = 3.5f,
                  cap = StrokeCap.Round,
                )
                drawLine(
                  color = Color(0xFF2855A8),
                  start = Offset(w * 0.14f, h * 0.58f),
                  end = Offset(w * 0.52f, h * 0.58f),
                  strokeWidth = 3.5f,
                  cap = StrokeCap.Round,
                )
                drawLine(
                  color = Color(0xFFE05280),
                  start = Offset(w * 0.68f, h * 0.22f),
                  end = Offset(w * 0.88f, h * 0.22f),
                  strokeWidth = 4f,
                  cap = StrokeCap.Round,
                )
              }

              NotebookCoverStyle.WHITE_DOCUMENT -> {
                drawRoundRect(
                  color = Color.White,
                  size = Size(w, h),
                  cornerRadius = CornerRadius(20f, 20f),
                )
                drawRoundRect(
                  color = Color(0xFFEAF2FC),
                  topLeft = Offset(w * 0.15f, h * 0.34f),
                  size = Size(w * 0.55f, h * 0.08f),
                  cornerRadius = CornerRadius(6f, 6f),
                )
                drawRoundRect(
                  color = Color(0xFFEAF2FC),
                  topLeft = Offset(w * 0.15f, h * 0.46f),
                  size = Size(w * 0.70f, h * 0.08f),
                  cornerRadius = CornerRadius(6f, 6f),
                )
                drawRoundRect(
                  color = Color(0xFFEAF2FC),
                  topLeft = Offset(w * 0.15f, h * 0.58f),
                  size = Size(w * 0.70f, h * 0.08f),
                  cornerRadius = CornerRadius(6f, 6f),
                )
              }
            }

            if (document.hasStylusBadge) {
              val badgePath =
                Path().apply {
                  moveTo(0f, 0f)
                  lineTo(w * 0.34f, 0f)
                  lineTo(0f, w * 0.34f)
                  close()
                }
              drawPath(path = badgePath, color = Color(0xFF38383A))
            }
          }
        }

        if (document.hasStylusBadge) {
          Icon(
            imageVector = Icons.Outlined.EditOff,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier =
              Modifier
                .align(Alignment.TopStart)
                .padding(start = 5.dp, top = 5.dp)
                .size(12.dp),
          )
        }

        // Top-right favorite star icon
        Box(
          modifier =
            Modifier
              .align(Alignment.TopEnd)
              .padding(6.dp)
              .size(22.dp)
              .clickable(onClick = onToggleFavorite)
              .testTag("favorite_star_${document.id}"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
            contentDescription = "Favorite",
            tint =
              if (document.isFavorite) {
                Color(0xFFFFD54F)
              } else {
                Color(0xFF8E8E93)
              },
            modifier = Modifier.size(16.dp),
          )
        }

        // Cybersecurity Vault Lock Overlay Badge if notebook is PIN-locked
        if (document.isLockedVault) {
          Box(
            modifier =
              Modifier
                .fillMaxSize()
                .background(Color(0xCC12141A)),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
            ) {
              SecurityVaultShieldIcon(
                tint = Color(0xFF67C8EB),
                isLocked = true,
                modifier = Modifier.size(28.dp),
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "VAULT LOCKED",
                color = Color(0xFF67C8EB),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Title + chevron context menu
    Box {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().clickable { showCardMenu = true },
      ) {
        Text(
          text = document.title,
          color = TextPrimaryWhite,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Medium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center,
          modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = "Notebook options",
          tint = TextSecondaryGray,
          modifier = Modifier.size(14.dp),
        )
      }

      DropdownMenu(
        expanded = showCardMenu,
        onDismissRequest = { showCardMenu = false },
        modifier = Modifier.background(PopoverBg),
      ) {
        DropdownMenuItem(
          text = { Text("Open Notebook", color = TextPrimaryWhite, fontSize = 13.sp) },
          onClick = {
            showCardMenu = false
            onClick()
          },
        )
        DropdownMenuItem(
          text = {
            Text(
              if (document.isFavorite) "Remove from Favorites" else "Add to Favorites",
              color = TextPrimaryWhite,
              fontSize = 13.sp,
            )
          },
          onClick = {
            showCardMenu = false
            onToggleFavorite()
          },
        )
        DropdownMenuItem(
          text = {
            Text(
              if (document.isTrashed) "Restore from Trash" else "Move to Trash",
              color = TextPrimaryWhite,
              fontSize = 13.sp,
            )
          },
          onClick = {
            showCardMenu = false
            onMoveToTrash()
          },
        )
        DropdownMenuItem(
          text = { Text("Delete Permanently", color = DangerRed, fontSize = 13.sp) },
          onClick = {
            showCardMenu = false
            onDeletePermanently()
          },
        )
      }
    }

    Spacer(modifier = Modifier.height(2.dp))

    Text(
      text = document.timestamp,
      color = TextMutedGray,
      fontSize = 10.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
