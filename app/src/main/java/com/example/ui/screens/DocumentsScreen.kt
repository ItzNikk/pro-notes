package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentSortOption
import com.example.model.DocumentType
import com.example.model.DocumentsSidebarSection
import com.example.model.FolderItem
import com.example.model.LibraryViewMode
import com.example.model.NotebookCoverStyle
import com.example.model.NotebookDocument
import com.example.ui.theme.BookCoverCharcoal
import com.example.ui.theme.BookCoverGridLine
import com.example.ui.theme.BookCoverGridPaper
import com.example.ui.theme.BookCoverKraftTan
import com.example.ui.theme.BookCoverRibbonBlue
import com.example.ui.theme.BookCoverSpineDark
import com.example.ui.theme.BookCoverSpineKraft
import com.example.ui.theme.BookCoverWhiteboard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LocalProNotesColors
import com.example.ui.theme.NewButtonBlue
import com.example.ui.theme.SwitchBlue

@Composable
fun DocumentsScreen(
  notebooks: List<NotebookDocument>,
  folders: List<FolderItem> = emptyList(),
  currentFolderId: String? = null,
  folderBreadcrumbs: List<FolderItem> = emptyList(),
  selectedSection: DocumentsSidebarSection,
  searchQuery: String,
  sortOption: DocumentSortOption,
  libraryViewMode: LibraryViewMode = LibraryViewMode.GRID,
  onSelectSection: (DocumentsSidebarSection) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onSelectSortOption: (DocumentSortOption) -> Unit,
  onToggleLibraryViewMode: () -> Unit = {},
  onOpenNotebook: (String) -> Unit,
  onCreateNewNotebook: () -> Unit,
  onCreateNewWhiteboard: () -> Unit = {},
  onImportPdfDocument: () -> Unit = {},
  onCreateFolder: (String, Long) -> Unit = { _, _ -> },
  onUpdateFolderColor: (String, Long) -> Unit = { _, _ -> },
  onNavigateToFolder: (String?) -> Unit = {},
  onNavigateUpFolder: () -> Unit = {},
  onRenameFolder: (String, String) -> Unit = { _, _ -> },
  onMoveFolder: (String, String?) -> Unit = { _, _ -> },
  onCanMoveFolder: (String, String?) -> Boolean = { _, _ -> true },
  onMoveFolderToTrash: (String) -> Unit = {},
  onRestoreFolderFromTrash: (String) -> Unit = {},
  onDeleteFolderPermanently: (String) -> Unit = {},
  onRenameNotebook: (String, String) -> Unit = { _, _ -> },
  onMoveNotebookToFolder: (String, String?) -> Unit = { _, _ -> },
  onToggleFavorite: (String) -> Unit,
  onMoveToTrash: (String) -> Unit,
  onRestoreFromTrash: (String) -> Unit,
  onDeletePermanently: (String) -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalProNotesColors.current

  // Modals for "+ New" creation menu, Folder creation, Rename, and Move
  var showNewCreationMenu by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var renameTarget by remember { mutableStateOf<Pair<Boolean, String>?>(null) } // (isFolder, id)
  var renameInitialTitle by remember { mutableStateOf("") }
  var moveTarget by remember { mutableStateOf<Pair<Boolean, String>?>(null) } // (isFolder, id)

  val folderById = remember(folders) { folders.associateBy { it.id } }

  val visibleFolders =
    remember(folders, notebooks, currentFolderId, selectedSection, searchQuery, sortOption) {
      val trimmedQuery = searchQuery.trim()
      val base =
        when (selectedSection) {
          DocumentsSidebarSection.DOCUMENTS ->
            if (trimmedQuery.isEmpty()) {
              folders.filter { !it.isTrashed && it.parentFolderId == currentFolderId }
            } else {
              // When searching, search across all active folders (or descendants if inside a folder)
              folders.filter { folder ->
                !folder.isTrashed &&
                  (currentFolderId == null || folder.parentFolderId == currentFolderId || folder.id == currentFolderId)
              }
            }
          DocumentsSidebarSection.TRASH ->
            folders.filter { it.isTrashed }
          else -> emptyList()
        }
      val searched =
        if (trimmedQuery.isEmpty()) {
          base
        } else {
          base.filter { folder ->
            val folderMatches = folder.name.contains(trimmedQuery, ignoreCase = true)
            val hasMatchingChildNotebook =
              notebooks.any { nb ->
                !nb.isTrashed &&
                  nb.folderId == folder.id &&
                  nb.title.contains(trimmedQuery, ignoreCase = true)
              }
            folderMatches || hasMatchingChildNotebook
          }
        }
      when (sortOption) {
        DocumentSortOption.RECENT -> searched.sortedByDescending { it.updatedAtEpochMs }
        DocumentSortOption.NAME_ASC -> searched.sortedBy { it.name.lowercase() }
        DocumentSortOption.FAVORITES_FIRST -> searched.sortedBy { it.name.lowercase() }
      }
    }

  val filteredNotebooks =
    remember(notebooks, folders, currentFolderId, selectedSection, searchQuery, sortOption) {
      val trimmedQuery = searchQuery.trim()
      val bySection =
        when (selectedSection) {
          DocumentsSidebarSection.DOCUMENTS ->
            notebooks.filter {
              !it.isTrashed &&
                (if (trimmedQuery.isNotEmpty()) {
                  currentFolderId == null || it.folderId == currentFolderId
                } else {
                  it.folderId == currentFolderId
                })
            }
          DocumentsSidebarSection.FAVORITES ->
            notebooks.filter { !it.isTrashed && it.isFavorite }
          DocumentsSidebarSection.SHARED ->
            notebooks.filter { !it.isTrashed && it.isShared }
          DocumentsSidebarSection.MARKETPLACE ->
            notebooks.filter { !it.isTrashed && it.documentType == DocumentType.WHITEBOARD }
          DocumentsSidebarSection.TRASH ->
            notebooks.filter { it.isTrashed }
        }
      val byQuery =
        if (trimmedQuery.isEmpty()) {
          bySection
        } else {
          bySection.filter { doc ->
            val parentFolder = doc.folderId?.let { folderById[it] }
            val parentFolderMatches =
              parentFolder != null &&
                !parentFolder.isTrashed &&
                parentFolder.name.contains(trimmedQuery, ignoreCase = true)
            doc.title.contains(trimmedQuery, ignoreCase = true) ||
              parentFolderMatches ||
              doc.pages.any { page ->
                page.textBoxes.any { tb -> tb.text.contains(trimmedQuery, ignoreCase = true) } ||
                  page.stickyNotes.any { st -> st.text.contains(trimmedQuery, ignoreCase = true) }
              }
          }
        }
      when (sortOption) {
        DocumentSortOption.RECENT -> byQuery.sortedByDescending { it.updatedAtEpochMs }
        DocumentSortOption.NAME_ASC -> byQuery.sortedBy { it.title.lowercase() }
        DocumentSortOption.FAVORITES_FIRST ->
          byQuery.sortedWith(compareByDescending<NotebookDocument> { it.isFavorite }.thenBy { it.title.lowercase() })
      }
    }

  BoxWithConstraints(modifier = modifier.fillMaxSize().background(colors.background)) {
    val isCompact = maxWidth < 680.dp

    Row(modifier = Modifier.fillMaxSize()) {
      // Left Sidebar Navigation (Tablet/Expanded)
      if (!isCompact) {
        Column(
          modifier =
            Modifier
              .width(248.dp)
              .fillMaxHeight()
              .background(colors.sidebarBg)
              .padding(horizontal = 14.dp, vertical = 20.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Image(
              painter = painterResource(id = R.drawable.app_logo),
              contentDescription = "Pro Notes Logo",
              contentScale = ContentScale.Crop,
              modifier =
                Modifier
                  .size(30.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.White),
            )
            Text(
              text = "Pro Notes",
              color = colors.textPrimary,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          DocumentsSidebarSection.entries.forEach { section ->
            val icon =
              when (section) {
                DocumentsSidebarSection.DOCUMENTS -> Icons.Outlined.Folder
                DocumentsSidebarSection.FAVORITES -> Icons.Outlined.StarBorder
                DocumentsSidebarSection.SHARED -> Icons.Outlined.Description
                DocumentsSidebarSection.MARKETPLACE -> Icons.Outlined.Draw
                DocumentsSidebarSection.TRASH -> Icons.Default.Delete
              }
            SidebarNavItem(
              icon = icon,
              label = section.label,
              selected = selectedSection == section,
              onClick = { onSelectSection(section) },
              testTag = "sidebar_section_${section.name.lowercase()}",
            )
            Spacer(modifier = Modifier.height(4.dp))
          }

          Spacer(modifier = Modifier.weight(1f))

          HorizontalDivider(color = colors.divider, modifier = Modifier.padding(vertical = 8.dp))

          val activeDocCount = notebooks.count { !it.isTrashed }
          val activeFolderCount = folders.count { !it.isTrashed }
          Text(
            text = "$activeDocCount Documents • $activeFolderCount Folders",
            color = colors.textSecondary,
            fontSize = 11.5.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      // Main Right Content Area
      Column(
        modifier =
          Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(colors.background)
            .padding(
              horizontal = if (isCompact) 14.dp else 28.dp,
              vertical = if (isCompact) 12.dp else 20.dp,
            ),
      ) {
        // Top Header: Section Title | Search Bar | + New Button | Settings Gear
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = selectedSection.label,
            color = colors.textPrimary,
            fontSize = if (isCompact) 22.sp else 26.sp,
            fontWeight = FontWeight.Bold,
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            // Search Bar
            Row(
              modifier =
                Modifier
                  .width(if (isCompact) 160.dp else 280.dp)
                  .height(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(colors.surfaceVariant)
                  .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                  .padding(horizontal = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = colors.textSecondary,
                modifier = Modifier.size(17.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (searchQuery.isEmpty()) {
                  Text(
                    text = "Search notes & folders...",
                    color = colors.textSecondary,
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                BasicTextField(
                  value = searchQuery,
                  onValueChange = onSearchQueryChange,
                  singleLine = true,
                  textStyle =
                    TextStyle(
                      color = colors.textPrimary,
                      fontSize = 13.sp,
                    ),
                  modifier = Modifier.fillMaxWidth().testTag("documents_search_input"),
                )
              }
              if (searchQuery.isNotEmpty()) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear search",
                  tint = colors.textSecondary,
                  modifier =
                    Modifier
                      .size(16.dp)
                      .clickable { onSearchQueryChange("") }
                      .testTag("clear_search_button"),
                )
              }
            }

            // "+ New" Dropdown Trigger Button
            Box {
              Row(
                modifier =
                  Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NewButtonBlue)
                    .clickable { showNewCreationMenu = true }
                    .padding(horizontal = 12.dp)
                    .testTag("top_new_notebook_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Create New",
                  tint = Color.White,
                  modifier = Modifier.size(17.dp),
                )
                if (!isCompact) {
                  Text(
                    text = "New",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }

              NewCreationDropdownMenu(
                expanded = showNewCreationMenu,
                onDismiss = { showNewCreationMenu = false },
                onCreateNotebook = {
                  showNewCreationMenu = false
                  onCreateNewNotebook()
                },
                onCreateWhiteboard = {
                  showNewCreationMenu = false
                  onCreateNewWhiteboard()
                },
                onImportDocument = {
                  showNewCreationMenu = false
                  onImportPdfDocument()
                },
                onCreateFolder = {
                  showNewCreationMenu = false
                  showCreateFolderDialog = true
                },
              )
            }

            // Grid / List View Toggle
            Box(
              modifier =
                Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(colors.surfaceVariant)
                  .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                  .clickable(onClick = onToggleLibraryViewMode)
                  .testTag("library_view_mode_toggle"),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector =
                  if (libraryViewMode == LibraryViewMode.GRID) Icons.Outlined.ViewList
                  else Icons.Default.GridView,
                contentDescription = "Toggle Grid or List View",
                tint = colors.textPrimary,
                modifier = Modifier.size(19.dp),
              )
            }

            // Settings Button
            Box(
              modifier =
                Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(colors.surfaceVariant)
                  .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                  .clickable(onClick = onOpenSettings)
                  .testTag("open_settings_button"),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = colors.textPrimary,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }

        // Compact Section Tabs on Phones / Narrow Windows
        if (isCompact) {
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            DocumentsSidebarSection.entries.forEach { section ->
              val isSelected = selectedSection == section
              Box(
                modifier =
                  Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) colors.accentBlue else colors.surfaceVariant)
                    .clickable { onSelectSection(section) }
                    .testTag("compact_section_${section.name.lowercase()}"),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = section.label,
                  color = if (isSelected) Color.White else colors.textSecondary,
                  fontSize = 11.5.sp,
                  fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                  maxLines = 1,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Breadcrumb Navigation + Sort Selector Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Clickable Folder Breadcrumb Trail (Documents > Folder > Subfolder)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
          ) {
            if (selectedSection == DocumentsSidebarSection.DOCUMENTS && currentFolderId != null) {
              Box(
                modifier =
                  Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surfaceVariant)
                    .clickable(onClick = onNavigateUpFolder)
                    .testTag("folder_up_button"),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Up to parent folder",
                  tint = colors.textPrimary,
                  modifier = Modifier.size(16.dp),
                )
              }
              Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
              text = selectedSection.label,
              color =
                if (currentFolderId == null) colors.textPrimary else colors.accentBlue,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              modifier =
                Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .clickable(enabled = currentFolderId != null) { onNavigateToFolder(null) }
                  .padding(horizontal = 4.dp, vertical = 2.dp)
                  .testTag("breadcrumb_root"),
            )

            folderBreadcrumbs.forEach { crumb ->
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(16.dp),
              )
              val isCurrentCrumb = crumb.id == currentFolderId
              Text(
                text = crumb.name,
                color = if (isCurrentCrumb) colors.textPrimary else colors.accentBlue,
                fontSize = 13.sp,
                fontWeight = if (isCurrentCrumb) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                  Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(enabled = !isCurrentCrumb) { onNavigateToFolder(crumb.id) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .testTag("breadcrumb_folder_${crumb.id}"),
              )
            }
          }

          // Sort Option Selector
          Row(
            modifier =
              Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceVariant)
                .clickable {
                  val entries = DocumentSortOption.entries
                  val nextOption = entries[(entries.indexOf(sortOption) + 1) % entries.size]
                  onSelectSortOption(nextOption)
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("sort_selector_pill"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = sortOption.label,
              color = colors.textSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
            )
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = "Cycle sort order",
              tint = colors.textSecondary,
              modifier = Modifier.size(16.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Library View (Grid, List, or Empty Search State)
        if (searchQuery.isNotBlank() && visibleFolders.isEmpty() && filteredNotebooks.isEmpty()) {
          Box(
            modifier =
              Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("search_empty_state"),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colors.textSecondary.copy(alpha = 0.55f),
                modifier = Modifier.size(48.dp),
              )
              Text(
                text = "No matching notes or folders",
                color = colors.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = "Nothing matched \"${searchQuery.trim()}\". Try another title or folder name.",
                color = colors.textSecondary,
                fontSize = 13.5.sp,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Box(
                modifier =
                  Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                    .clickable { onSearchQueryChange("") }
                    .padding(horizontal = 16.dp, vertical = 9.dp)
                    .testTag("empty_state_clear_search_button"),
              ) {
                Text(
                  text = "Clear search",
                  color = colors.textPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                )
              }
            }
          }
        } else if (libraryViewMode == LibraryViewMode.GRID) {
          LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = if (isCompact) 142.dp else 160.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 14.dp else 22.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            // 1. "+ New..." Dashed Creation Card in Documents Root or Folder (hidden while actively filtering)
            if (selectedSection == DocumentsSidebarSection.DOCUMENTS && searchQuery.isBlank()) {
              item(key = "new_notebook_card") {
                Box {
                  NewNotebookDashedCard(
                    onClick = { showNewCreationMenu = true },
                  )
                }
              }
            }

            // 2. Folder Cards
            items(visibleFolders, key = { "folder_${it.id}" }) { folder ->
              val matchingNotebooksInFolder =
                if (searchQuery.isBlank()) {
                  emptyList()
                } else {
                  val q = searchQuery.trim()
                  notebooks.filter {
                    !it.isTrashed &&
                      it.folderId == folder.id &&
                      (it.title.contains(q, ignoreCase = true) ||
                        folder.name.contains(q, ignoreCase = true))
                  }
                }
              val childCount =
                folders.count { !it.isTrashed && it.parentFolderId == folder.id } +
                  notebooks.count { !it.isTrashed && it.folderId == folder.id }
              FolderGridCard(
                folder = folder,
                itemCount = childCount,
                matchingNotebookTitles = matchingNotebooksInFolder.map { it.title },
                onOpenFolder = {
                  if (!folder.isTrashed) {
                    onNavigateToFolder(folder.id)
                  }
                },
                onRenameFolder = {
                  renameTarget = true to folder.id
                  renameInitialTitle = folder.name
                },
                onChangeFolderColor = { newColorArgb ->
                  onUpdateFolderColor(folder.id, newColorArgb)
                },
                onMoveFolder = {
                  moveTarget = true to folder.id
                },
                onMoveToTrash = { onMoveFolderToTrash(folder.id) },
                onRestoreFromTrash = { onRestoreFolderFromTrash(folder.id) },
                onDeletePermanently = { onDeleteFolderPermanently(folder.id) },
              )
            }

            // 3. Notebook & Whiteboard Cards
            items(filteredNotebooks, key = { it.id }) { doc ->
              val parentFolderName =
                doc.folderId?.let { fid -> folderById[fid]?.takeIf { !it.isTrashed }?.name }
              NotebookGridCard(
                document = doc,
                parentFolderName = if (searchQuery.isNotBlank() || currentFolderId == null) parentFolderName else null,
                onClick = { onOpenNotebook(doc.id) },
                onRename = {
                  renameTarget = false to doc.id
                  renameInitialTitle = doc.title
                },
                onMoveToFolder = {
                  moveTarget = false to doc.id
                },
                onToggleFavorite = { onToggleFavorite(doc.id) },
                onMoveToTrash = { onMoveToTrash(doc.id) },
                onRestoreFromTrash = { onRestoreFromTrash(doc.id) },
                onDeletePermanently = { onDeletePermanently(doc.id) },
              )
            }
          }
        } else {
          // LIST VIEW MODE
          LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            items(visibleFolders, key = { "list_folder_${it.id}" }) { folder ->
              val childCount =
                folders.count { !it.isTrashed && it.parentFolderId == folder.id } +
                  notebooks.count { !it.isTrashed && it.folderId == folder.id }
              FolderListRow(
                folder = folder,
                itemCount = childCount,
                onOpenFolder = {
                  if (!folder.isTrashed) onNavigateToFolder(folder.id)
                },
                onRenameFolder = {
                  renameTarget = true to folder.id
                  renameInitialTitle = folder.name
                },
                onChangeFolderColor = { newColorArgb ->
                  onUpdateFolderColor(folder.id, newColorArgb)
                },
                onMoveFolder = { moveTarget = true to folder.id },
                onMoveToTrash = { onMoveFolderToTrash(folder.id) },
                onRestoreFromTrash = { onRestoreFolderFromTrash(folder.id) },
                onDeletePermanently = { onDeleteFolderPermanently(folder.id) },
              )
            }

            items(filteredNotebooks, key = { "list_doc_${it.id}" }) { doc ->
              val parentFolderName =
                doc.folderId?.let { fid -> folderById[fid]?.takeIf { !it.isTrashed }?.name }
              NotebookListRow(
                document = doc,
                parentFolderName = parentFolderName,
                onClick = { onOpenNotebook(doc.id) },
                onRename = {
                  renameTarget = false to doc.id
                  renameInitialTitle = doc.title
                },
                onMoveToFolder = { moveTarget = false to doc.id },
                onToggleFavorite = { onToggleFavorite(doc.id) },
                onMoveToTrash = { onMoveToTrash(doc.id) },
                onRestoreFromTrash = { onRestoreFromTrash(doc.id) },
                onDeletePermanently = { onDeletePermanently(doc.id) },
              )
            }
          }
        }
      }
    }

    // Create Folder Modal Dialog
    if (showCreateFolderDialog) {
      CreateFolderModalDialog(
        onConfirm = { name, colorArgb ->
          onCreateFolder(name, colorArgb)
          showCreateFolderDialog = false
        },
        onDismiss = { showCreateFolderDialog = false },
      )
    }

    // Rename Folder / Notebook Modal Dialog
    renameTarget?.let { (isFolder, targetId) ->
      RenameItemModalDialog(
        titleLabel = if (isFolder) "Rename Folder" else "Rename Document",
        initialValue = renameInitialTitle,
        onConfirm = { newName ->
          if (isFolder) onRenameFolder(targetId, newName)
          else onRenameNotebook(targetId, newName)
          renameTarget = null
        },
        onDismiss = { renameTarget = null },
      )
    }

    // Move to Folder Modal Dialog
    moveTarget?.let { (isFolder, targetId) ->
      val availableDestinations =
        folders.filter { candidate ->
          !candidate.isTrashed &&
            (!isFolder || onCanMoveFolder(targetId, candidate.id))
        }
      MoveToFolderModalDialog(
        isMovingFolder = isFolder,
        folders = availableDestinations,
        onSelectDestination = { destFolderId ->
          if (isFolder) onMoveFolder(targetId, destFolderId)
          else onMoveNotebookToFolder(targetId, destFolderId)
          moveTarget = null
        },
        onDismiss = { moveTarget = null },
      )
    }
  }
}

/**
 * Goodnotes-style "+ New" Creation Menu with 4 structured options:
 * 1. Notebook
 * 2. Whiteboard
 * 3. Import document (PDF)
 * 4. Folder
 */
@Composable
private fun NewCreationDropdownMenu(
  expanded: Boolean,
  onDismiss: () -> Unit,
  onCreateNotebook: () -> Unit,
  onCreateWhiteboard: () -> Unit,
  onImportDocument: () -> Unit,
  onCreateFolder: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismiss,
    modifier =
      Modifier
        .width(230.dp)
        .background(colors.popoverBg)
        .testTag("new_creation_dropdown_menu"),
  ) {
    DropdownMenuItem(
      text = {
        Column {
          Text(
            "Notebook",
            color = colors.textPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            "Templates, covers & paper sizes",
            color = colors.textSecondary,
            fontSize = 10.5.sp,
          )
        }
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Outlined.MenuBook,
          contentDescription = null,
          tint = NewButtonBlue,
        )
      },
      onClick = onCreateNotebook,
      modifier = Modifier.testTag("new_menu_notebook_option"),
    )

    DropdownMenuItem(
      text = {
        Column {
          Text(
            "Whiteboard",
            color = colors.textPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            "Infinite canvas for diagrams & mindmaps",
            color = colors.textSecondary,
            fontSize = 10.5.sp,
          )
        }
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Outlined.Draw,
          contentDescription = null,
          tint = Color(0xFF00ACC1),
        )
      },
      onClick = onCreateWhiteboard,
      modifier = Modifier.testTag("new_menu_whiteboard_option"),
    )

    DropdownMenuItem(
      text = {
        Column {
          Text(
            "Import document",
            color = colors.textPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            "Annotate multi-page PDF files",
            color = colors.textSecondary,
            fontSize = 10.5.sp,
          )
        }
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Outlined.PictureAsPdf,
          contentDescription = null,
          tint = Color(0xFFFF7043),
        )
      },
      onClick = onImportDocument,
      modifier = Modifier.testTag("new_menu_import_pdf_option"),
    )

    HorizontalDivider(color = colors.divider)

    DropdownMenuItem(
      text = {
        Column {
          Text(
            "Folder",
            color = colors.textPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            "Organize notebooks into folders",
            color = colors.textSecondary,
            fontSize = 10.5.sp,
          )
        }
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.CreateNewFolder,
          contentDescription = null,
          tint = Color(0xFFFFCA28),
        )
      },
      onClick = onCreateFolder,
      modifier = Modifier.testTag("new_menu_folder_option"),
    )
  }
}

@Composable
private fun SidebarNavItem(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  val colors = LocalProNotesColors.current
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(44.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(if (selected) colors.sidebarSelectedBg else Color.Transparent)
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp)
        .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (selected) colors.accentBlue else colors.textSecondary,
      modifier = Modifier.size(20.dp),
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = label,
      color = if (selected) colors.textPrimary else colors.textSecondary,
      fontSize = 14.sp,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
    )
  }
}

@Composable
private fun NewNotebookDashedCard(
  onClick: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      Modifier
        .width(150.dp)
        .clickable(onClick = onClick)
        .testTag("create_new_notebook_card"),
  ) {
    Box(
      modifier =
        Modifier
          .width(142.dp)
          .height(186.dp),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f)
        drawRoundRect(
          color = colors.textSecondary.copy(alpha = 0.45f),
          size = size,
          cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
          style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect),
        )
      }
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Create New",
        tint = NewButtonBlue,
        modifier = Modifier.size(34.dp),
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = "New...",
      color = NewButtonBlue,
      fontSize = 13.5.sp,
      fontWeight = FontWeight.Medium,
    )
  }
}

private val FolderPresetColors =
  listOf(
    0xFF4C8DF6L,
    0xFFFFCA28L,
    0xFF26A69AL,
    0xFFAB47BCL,
    0xFFEF5350L,
    0xFF66BB6AL,
    0xFFEC407AL,
    0xFFFF7043L,
  )

private fun folderColorFromArgb(argb: Long): Color =
  Color(((if (argb == 0L) 0xFF4C8DF6L else argb) or 0xFF000000L).toInt())

@Composable
private fun FolderGridCard(
  folder: FolderItem,
  itemCount: Int,
  matchingNotebookTitles: List<String> = emptyList(),
  onOpenFolder: () -> Unit,
  onRenameFolder: () -> Unit,
  onChangeFolderColor: (Long) -> Unit = {},
  onMoveFolder: () -> Unit,
  onMoveToTrash: () -> Unit,
  onRestoreFromTrash: () -> Unit,
  onDeletePermanently: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  var showMenu by remember { mutableStateOf(false) }
  val folderColor = folderColorFromArgb(folder.colorArgb)

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      Modifier
        .width(150.dp)
        .clickable(onClick = onOpenFolder)
        .testTag("folder_card_${folder.id}"),
  ) {
    // Main visual element: Tinted folder card with vibrant folder icon and color accent bar
    Box(
      modifier =
        Modifier
          .width(142.dp)
          .height(186.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(folderColor.copy(alpha = 0.14f))
          .border(1.5.dp, folderColor.copy(alpha = 0.42f), RoundedCornerShape(14.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Default.Folder,
        contentDescription = folder.name,
        tint = folderColor,
        modifier = Modifier.size(116.dp),
      )

      // Color badge dot at bottom-start
      Box(
        modifier =
          Modifier
            .align(Alignment.BottomStart)
            .padding(10.dp)
            .size(12.dp)
            .clip(CircleShape)
            .background(folderColor)
            .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
      )

      // Context menu button at top-end
      Box(
        modifier =
          Modifier
            .align(Alignment.TopEnd)
            .padding(top = 8.dp, end = 6.dp),
      ) {
        Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = "Folder Options",
          tint = colors.textPrimary,
          modifier =
            Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(colors.surface.copy(alpha = 0.65f))
              .clickable { showMenu = true }
              .padding(3.dp)
              .testTag("folder_menu_${folder.id}"),
        )
        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false },
          modifier = Modifier.background(colors.popoverBg),
        ) {
          if (!folder.isTrashed) {
            DropdownMenuItem(
              text = { Text("Open Folder", color = colors.textPrimary, fontSize = 13.sp) },
              onClick = {
                showMenu = false
                onOpenFolder()
              },
            )
            DropdownMenuItem(
              text = { Text("Rename Folder", color = colors.textPrimary, fontSize = 13.sp) },
              leadingIcon = {
                Icon(Icons.Default.Edit, contentDescription = null, tint = colors.textSecondary)
              },
              onClick = {
                showMenu = false
                onRenameFolder()
              },
            )
            // Inline quick folder color switcher inside menu
            Column(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
              Text(
                text = "FOLDER COLOR",
                color = colors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                FolderPresetColors.take(6).forEach { presetArgb ->
                  val isCurrent = (folder.colorArgb or 0xFF000000L) == (presetArgb or 0xFF000000L)
                  Box(
                    modifier =
                      Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(folderColorFromArgb(presetArgb))
                        .border(
                          width = if (isCurrent) 2.dp else 1.dp,
                          color = if (isCurrent) Color.White else Color.Transparent,
                          shape = CircleShape,
                        )
                        .clickable {
                          onChangeFolderColor(presetArgb)
                          showMenu = false
                        },
                  )
                }
              }
            }
            DropdownMenuItem(
              text = { Text("Move to Folder...", color = colors.textPrimary, fontSize = 13.sp) },
              leadingIcon = {
                Icon(
                  Icons.AutoMirrored.Outlined.DriveFileMove,
                  contentDescription = null,
                  tint = colors.textSecondary,
                )
              },
              onClick = {
                showMenu = false
                onMoveFolder()
              },
            )
            DropdownMenuItem(
              text = { Text("Move to Trash", color = DangerRed, fontSize = 13.sp) },
              leadingIcon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
              },
              onClick = {
                showMenu = false
                onMoveToTrash()
              },
            )
          } else {
            DropdownMenuItem(
              text = { Text("Restore Folder", color = colors.textPrimary, fontSize = 13.sp) },
              leadingIcon = {
                Icon(Icons.Default.RestoreFromTrash, contentDescription = null, tint = SwitchBlue)
              },
              onClick = {
                showMenu = false
                onRestoreFromTrash()
              },
            )
            DropdownMenuItem(
              text = { Text("Delete Permanently", color = DangerRed, fontSize = 13.sp) },
              leadingIcon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
              },
              onClick = {
                showMenu = false
                onDeletePermanently()
              },
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = folder.name,
      color = colors.textPrimary,
      fontSize = 13.5.sp,
      fontWeight = FontWeight.SemiBold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    val subtitleText =
      if (matchingNotebookTitles.isNotEmpty()) {
        matchingNotebookTitles.joinToString(", ")
      } else {
        "$itemCount item${if (itemCount == 1) "" else "s"}"
      }
    Text(
      text = subtitleText,
      color = if (matchingNotebookTitles.isNotEmpty()) colors.accentBlue else colors.textSecondary,
      fontSize = 11.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun NotebookGridCard(
  document: NotebookDocument,
  parentFolderName: String? = null,
  onClick: () -> Unit,
  onRename: () -> Unit,
  onMoveToFolder: () -> Unit,
  onToggleFavorite: () -> Unit,
  onMoveToTrash: () -> Unit,
  onRestoreFromTrash: () -> Unit,
  onDeletePermanently: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  var showCardMenu by remember { mutableStateOf(false) }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      Modifier
        .width(150.dp)
        .clickable(onClick = onClick)
        .testTag("notebook_card_${document.id}"),
  ) {
    Box(
      modifier =
        Modifier
          .width(142.dp)
          .height(186.dp)
          .shadow(8.dp, RoundedCornerShape(10.dp))
          .clip(RoundedCornerShape(10.dp)),
    ) {
      // PRIVACY RULE (Stage 2F): Never expose page content or PDF thumbnails for locked Vault notebooks!
      val firstPdfBitmap =
        if (document.isLockedVault) null
        else document.pages.firstOrNull()?.pdfPageBitmap

      if (document.isLockedVault) {
        VaultLockedCoverArtwork()
      } else if (firstPdfBitmap != null) {
        Image(
          bitmap = firstPdfBitmap.asImageBitmap(),
          contentDescription = document.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize().background(Color.White),
        )
        Box(
          modifier =
            Modifier
              .align(Alignment.TopStart)
              .padding(6.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xCC1E5AA8))
              .padding(horizontal = 5.dp, vertical = 2.dp),
        ) {
          Text(
            text = "PDF • ${document.pages.size}p",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      } else {
        NotebookCoverArtwork(coverStyle = document.coverStyle)
      }

      if (document.documentType == DocumentType.WHITEBOARD && !document.isLockedVault) {
        Box(
          modifier =
            Modifier
              .align(Alignment.TopStart)
              .padding(6.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xDD00838F))
              .padding(horizontal = 5.dp, vertical = 2.dp),
        ) {
          Text(
            text = "BOARD",
            color = Color.White,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      // Context Menu (`⋮`) at top-right of cover
      Box(
        modifier =
          Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.42f))
              .clickable { showCardMenu = true }
              .testTag("notebook_menu_${document.id}"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Notebook Options",
            tint = Color.White,
            modifier = Modifier.size(15.dp),
          )
        }

        DropdownMenu(
          expanded = showCardMenu,
          onDismissRequest = { showCardMenu = false },
          modifier = Modifier.background(colors.popoverBg),
        ) {
          if (!document.isTrashed) {
            DropdownMenuItem(
              text = { Text("Open", color = colors.textPrimary, fontSize = 13.sp) },
              onClick = {
                showCardMenu = false
                onClick()
              },
            )
            DropdownMenuItem(
              text = { Text("Rename", color = colors.textPrimary, fontSize = 13.sp) },
              leadingIcon = {
                Icon(Icons.Default.Edit, contentDescription = null, tint = colors.textSecondary)
              },
              onClick = {
                showCardMenu = false
                onRename()
              },
            )
            DropdownMenuItem(
              text = { Text("Move to Folder...", color = colors.textPrimary, fontSize = 13.sp) },
              leadingIcon = {
                Icon(
                  Icons.AutoMirrored.Outlined.DriveFileMove,
                  contentDescription = null,
                  tint = colors.textSecondary,
                )
              },
              onClick = {
                showCardMenu = false
                onMoveToFolder()
              },
            )
            DropdownMenuItem(
              text = {
                Text(
                  if (document.isFavorite) "Remove from Favorites" else "Add to Favorites",
                  color = colors.textPrimary,
                  fontSize = 13.sp,
                )
              },
              leadingIcon = {
                Icon(
                  Icons.Default.Star,
                  contentDescription = null,
                  tint = if (document.isFavorite) Color(0xFFFFCA28) else colors.textSecondary,
                )
              },
              onClick = {
                showCardMenu = false
                onToggleFavorite()
              },
            )
            DropdownMenuItem(
              text = { Text("Move to Trash", color = DangerRed, fontSize = 13.sp) },
              leadingIcon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
              },
              onClick = {
                showCardMenu = false
                onMoveToTrash()
              },
            )
          } else {
            DropdownMenuItem(
              text = { Text("Restore Document", color = colors.textPrimary, fontSize = 13.sp) },
              leadingIcon = {
                Icon(Icons.Default.RestoreFromTrash, contentDescription = null, tint = SwitchBlue)
              },
              onClick = {
                showCardMenu = false
                onRestoreFromTrash()
              },
            )
            DropdownMenuItem(
              text = { Text("Delete Permanently", color = DangerRed, fontSize = 13.sp) },
              leadingIcon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
              },
              onClick = {
                showCardMenu = false
                onDeletePermanently()
              },
            )
          }
        }
      }

      if (document.isLockedVault) {
        Box(
          modifier =
            Modifier
              .align(Alignment.BottomEnd)
              .padding(6.dp)
              .size(22.dp)
              .clip(CircleShape)
              .background(Color(0xDD1A1D24))
              .border(1.dp, Color(0xFF34C759), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked with Vault PIN",
            tint = Color(0xFF34C759),
            modifier = Modifier.size(12.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Icon(
        imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
        contentDescription = "Favorite",
        tint = if (document.isFavorite) Color(0xFFFFCA28) else colors.textSecondary,
        modifier =
          Modifier
            .size(15.dp)
            .clickable(onClick = onToggleFavorite)
            .testTag("favorite_star_${document.id}"),
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = document.title,
        color = colors.textPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(modifier = Modifier.height(2.dp))

    val baseMeta =
      if (document.documentType == DocumentType.WHITEBOARD) "Whiteboard • ${document.dateLabel}"
      else "${document.pages.size}p • ${document.dateLabel}"
    Text(
      text = if (!parentFolderName.isNullOrBlank()) "$parentFolderName • $baseMeta" else baseMeta,
      color = colors.textSecondary,
      fontSize = 11.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun FolderListRow(
  folder: FolderItem,
  itemCount: Int,
  onOpenFolder: () -> Unit,
  onRenameFolder: () -> Unit,
  onChangeFolderColor: (Long) -> Unit = {},
  onMoveFolder: () -> Unit,
  onMoveToTrash: () -> Unit,
  onRestoreFromTrash: () -> Unit,
  onDeletePermanently: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  val folderColor = folderColorFromArgb(folder.colorArgb)
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(colors.surfaceVariant)
        .border(1.dp, folderColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
        .clickable(onClick = onOpenFolder)
        .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.weight(1f),
    ) {
      Icon(
        imageVector = Icons.Default.Folder,
        contentDescription = null,
        tint = folderColor,
        modifier = Modifier.size(30.dp),
      )
      Column {
        Text(
          text = folder.name,
          color = colors.textPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = "$itemCount item${if (itemCount == 1) "" else "s"}",
          color = colors.textSecondary,
          fontSize = 11.5.sp,
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
      if (!folder.isTrashed) {
        // Cycle folder color dot button
        Box(
          modifier =
            Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(folderColor)
              .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
              .clickable {
                val currIdx = FolderPresetColors.indexOfFirst { (it or 0xFF000000L) == (folder.colorArgb or 0xFF000000L) }
                val nextColor = FolderPresetColors[(currIdx + 1).coerceAtLeast(0) % FolderPresetColors.size]
                onChangeFolderColor(nextColor)
              },
        )
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = "Rename",
          tint = colors.textSecondary,
          modifier = Modifier.size(18.dp).clickable(onClick = onRenameFolder),
        )
        Icon(
          imageVector = Icons.AutoMirrored.Outlined.DriveFileMove,
          contentDescription = "Move",
          tint = colors.textSecondary,
          modifier = Modifier.size(18.dp).clickable(onClick = onMoveFolder),
        )
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Trash",
          tint = DangerRed,
          modifier = Modifier.size(18.dp).clickable(onClick = onMoveToTrash),
        )
      } else {
        Icon(
          imageVector = Icons.Default.RestoreFromTrash,
          contentDescription = "Restore",
          tint = SwitchBlue,
          modifier = Modifier.size(18.dp).clickable(onClick = onRestoreFromTrash),
        )
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete Permanently",
          tint = DangerRed,
          modifier = Modifier.size(18.dp).clickable(onClick = onDeletePermanently),
        )
      }
    }
  }
}

@Composable
private fun NotebookListRow(
  document: NotebookDocument,
  parentFolderName: String? = null,
  onClick: () -> Unit,
  onRename: () -> Unit,
  onMoveToFolder: () -> Unit,
  onToggleFavorite: () -> Unit,
  onMoveToTrash: () -> Unit,
  onRestoreFromTrash: () -> Unit,
  onDeletePermanently: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(colors.surfaceVariant)
        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.weight(1f),
    ) {
      Icon(
        imageVector =
          when {
            document.isLockedVault -> Icons.Default.Lock
            document.documentType == DocumentType.WHITEBOARD -> Icons.Outlined.Draw
            else -> Icons.Outlined.MenuBook
          },
        contentDescription = null,
        tint =
          if (document.isLockedVault) Color(0xFF34C759)
          else colors.accentBlue,
        modifier = Modifier.size(26.dp),
      )
      Column {
        Text(
          text = document.title,
          color = colors.textPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
        )
        val folderPrefix = if (!parentFolderName.isNullOrBlank()) "$parentFolderName • " else ""
        Text(
          text =
            if (document.isLockedVault) "${folderPrefix}Encrypted Vault Document • ${document.dateLabel}"
            else "$folderPrefix${document.pages.size} pages • ${document.pageSizePreset.label} • ${document.dateLabel}",
          color = colors.textSecondary,
          fontSize = 11.5.sp,
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
      if (!document.isTrashed) {
        Icon(
          imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
          contentDescription = "Favorite",
          tint = if (document.isFavorite) Color(0xFFFFCA28) else colors.textSecondary,
          modifier = Modifier.size(18.dp).clickable(onClick = onToggleFavorite),
        )
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = "Rename",
          tint = colors.textSecondary,
          modifier = Modifier.size(18.dp).clickable(onClick = onRename),
        )
        Icon(
          imageVector = Icons.AutoMirrored.Outlined.DriveFileMove,
          contentDescription = "Move",
          tint = colors.textSecondary,
          modifier = Modifier.size(18.dp).clickable(onClick = onMoveToFolder),
        )
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Trash",
          tint = DangerRed,
          modifier = Modifier.size(18.dp).clickable(onClick = onMoveToTrash),
        )
      } else {
        Icon(
          imageVector = Icons.Default.RestoreFromTrash,
          contentDescription = "Restore",
          tint = SwitchBlue,
          modifier = Modifier.size(18.dp).clickable(onClick = onRestoreFromTrash),
        )
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete Permanently",
          tint = DangerRed,
          modifier = Modifier.size(18.dp).clickable(onClick = onDeletePermanently),
        )
      }
    }
  }
}

@Composable
private fun VaultLockedCoverArtwork() {
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(Color(0xFF182232), Color(0xFF0F1622))
          )
        ),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(
        modifier =
          Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0x2234C759))
            .border(1.5.dp, Color(0xFF34C759), CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Encrypted Vault",
          tint = Color(0xFF34C759),
          modifier = Modifier.size(24.dp),
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "VAULT LOCKED",
        color = Color(0xFF34C759),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "AES-GCM Protected",
        color = Color(0xFF8E95A5),
        fontSize = 8.5.sp,
      )
    }
  }
}

@Composable
private fun CreateFolderModalDialog(
  onConfirm: (String, Long) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  var folderName by remember { mutableStateOf("") }
  val folderColorOptions = FolderPresetColors
  var selectedColorIdx by remember { mutableIntStateOf(0) }
  var useCustomColor by remember { mutableStateOf(false) }
  var customHue by remember { mutableFloatStateOf(215f) }
  var customSaturation by remember { mutableFloatStateOf(0.72f) }
  var customValue by remember { mutableFloatStateOf(0.95f) }

  val customColorArgb =
    remember(customHue, customSaturation, customValue) {
      val rgbInt =
        android.graphics.Color.HSVToColor(floatArrayOf(customHue, customSaturation, customValue))
      (rgbInt.toLong() and 0xFFFFFFFFL) or 0xFF000000L
    }

  val resolvedColorArgb =
    ((if (useCustomColor) customColorArgb else folderColorOptions[selectedColorIdx]) or 0xFF000000L)

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.6f))
        .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 380.dp)
          .fillMaxWidth(0.92f)
          .clip(RoundedCornerShape(16.dp))
          .background(colors.popoverBg)
          .border(1.dp, colors.border, RoundedCornerShape(16.dp))
          .clickable(enabled = false) {}
          .padding(20.dp)
          .testTag("create_folder_dialog"),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "New Folder",
          color = colors.textPrimary,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
        )
        Icon(
          imageVector = Icons.Default.Folder,
          contentDescription = null,
          tint = folderColorFromArgb(resolvedColorArgb),
          modifier = Modifier.size(36.dp),
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = folderName,
        onValueChange = { folderName = it },
        placeholder = { Text("Folder Name", color = colors.textSecondary) },
        singleLine = true,
        colors =
          OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedBorderColor = colors.accentBlue,
            unfocusedBorderColor = colors.border,
          ),
        modifier = Modifier.fillMaxWidth().testTag("folder_name_input"),
      )

      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = "FOLDER COLOR",
        color = colors.textSecondary,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        folderColorOptions.take(6).forEachIndexed { idx, argb ->
          val isSel = !useCustomColor && selectedColorIdx == idx
          Box(
            modifier =
              Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(folderColorFromArgb(argb))
                .border(
                  width = if (isSel) 2.5.dp else 1.dp,
                  color = if (isSel) Color.White else Color.Transparent,
                  shape = CircleShape,
                )
                .clickable {
                  useCustomColor = false
                  selectedColorIdx = idx
                }
                .testTag("folder_color_preset_$idx"),
          )
        }

        // Custom color picker toggle pill
        Box(
          modifier =
            Modifier
              .height(28.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(if (useCustomColor) folderColorFromArgb(customColorArgb) else colors.surfaceVariant)
              .border(
                width = if (useCustomColor) 2.dp else 1.dp,
                color = if (useCustomColor) Color.White else colors.border,
                shape = RoundedCornerShape(14.dp),
              )
              .clickable { useCustomColor = !useCustomColor }
              .padding(horizontal = 10.dp)
              .testTag("folder_custom_color_toggle"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Custom",
            color = if (useCustomColor) Color.White else colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      if (useCustomColor) {
        Spacer(modifier = Modifier.height(12.dp))
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(colors.surfaceVariant)
              .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
          Text(
            text = "Hue",
            color = colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
          )
          Slider(
            value = customHue,
            onValueChange = { customHue = it },
            valueRange = 0f..360f,
            colors =
              SliderDefaults.colors(
                thumbColor = folderColorFromArgb(customColorArgb),
                activeTrackColor = folderColorFromArgb(customColorArgb),
              ),
            modifier = Modifier.height(28.dp).testTag("folder_custom_hue_slider"),
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Saturation",
            color = colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
          )
          Slider(
            value = customSaturation,
            onValueChange = { customSaturation = it },
            valueRange = 0.2f..1f,
            colors =
              SliderDefaults.colors(
                thumbColor = folderColorFromArgb(customColorArgb),
                activeTrackColor = folderColorFromArgb(customColorArgb),
              ),
            modifier = Modifier.height(28.dp).testTag("folder_custom_saturation_slider"),
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable(onClick = onDismiss)
              .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
          Text("Cancel", color = colors.textSecondary, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(NewButtonBlue)
              .clickable {
                onConfirm(
                  folderName.trim().ifEmpty { "Untitled Folder" },
                  resolvedColorArgb,
                )
              }
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .testTag("confirm_create_folder_button"),
        ) {
          Text("Create Folder", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun RenameItemModalDialog(
  titleLabel: String,
  initialValue: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  var nameText by remember(initialValue) { mutableStateOf(initialValue) }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.6f))
        .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 360.dp)
          .fillMaxWidth(0.9f)
          .clip(RoundedCornerShape(16.dp))
          .background(colors.popoverBg)
          .border(1.dp, colors.border, RoundedCornerShape(16.dp))
          .clickable(enabled = false) {}
          .padding(20.dp)
          .testTag("rename_item_dialog"),
    ) {
      Text(
        text = titleLabel,
        color = colors.textPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = nameText,
        onValueChange = { nameText = it },
        singleLine = true,
        colors =
          OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedBorderColor = colors.accentBlue,
            unfocusedBorderColor = colors.border,
          ),
        modifier = Modifier.fillMaxWidth().testTag("rename_item_input"),
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable(onClick = onDismiss)
              .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
          Text("Cancel", color = colors.textSecondary, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(NewButtonBlue)
              .clickable { onConfirm(nameText.trim()) }
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .testTag("confirm_rename_button"),
        ) {
          Text("Save", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun MoveToFolderModalDialog(
  isMovingFolder: Boolean,
  folders: List<FolderItem>,
  onSelectDestination: (String?) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.6f))
        .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 360.dp)
          .fillMaxWidth(0.9f)
          .clip(RoundedCornerShape(16.dp))
          .background(colors.popoverBg)
          .border(1.dp, colors.border, RoundedCornerShape(16.dp))
          .clickable(enabled = false) {}
          .padding(20.dp)
          .testTag("move_to_folder_dialog"),
    ) {
      Text(
        text = if (isMovingFolder) "Move Folder To..." else "Move Document To...",
        color = colors.textPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(10.dp))

      // Root Documents option
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .clickable { onSelectDestination(null) }
            .padding(12.dp)
            .testTag("move_dest_root"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(Icons.Outlined.Folder, contentDescription = null, tint = colors.accentBlue)
        Text("Documents (Root)", color = colors.textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
      }

      Spacer(modifier = Modifier.height(6.dp))

      LazyColumn(
        modifier = Modifier.height(180.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        items(folders, key = { it.id }) { folder ->
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceVariant)
                .clickable { onSelectDestination(folder.id) }
                .padding(12.dp)
                .testTag("move_dest_${folder.id}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = folderColorFromArgb(folder.colorArgb))
            Text(folder.name, color = colors.textPrimary, fontSize = 13.5.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Box(
        modifier =
          Modifier
            .align(Alignment.End)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 12.dp, vertical = 6.dp),
      ) {
        Text("Cancel", color = colors.textSecondary, fontSize = 13.sp)
      }
    }
  }
}

@Composable
private fun NotebookCoverArtwork(coverStyle: NotebookCoverStyle) {
  when (coverStyle) {
    NotebookCoverStyle.WHITEBOARD -> {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(BookCoverWhiteboard),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val path1 =
            Path().apply {
              moveTo(w * 0.25f, h * 0.65f)
              quadraticTo(w * 0.45f, h * 0.35f, w * 0.72f, h * 0.48f)
            }
          drawPath(
            path = path1,
            color = Color(0xFF4285F4),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
          )
        }
      }
    }

    NotebookCoverStyle.CHARCOAL_DARK -> {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(BookCoverCharcoal),
      ) {
        Box(
          modifier =
            Modifier
              .width(14.dp)
              .fillMaxHeight()
              .background(BookCoverSpineDark),
        )
        Box(
          modifier =
            Modifier
              .align(Alignment.TopEnd)
              .padding(end = 18.dp)
              .width(12.dp)
              .height(34.dp)
              .background(BookCoverRibbonBlue),
        )
        Box(
          modifier =
            Modifier
              .align(Alignment.TopCenter)
              .padding(start = 8.dp, top = 36.dp)
              .width(86.dp)
              .height(32.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color.White.copy(alpha = 0.92f)),
        )
      }
    }

    NotebookCoverStyle.GRID_PAPER -> {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(BookCoverGridPaper),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val step = 16.dp.toPx()
          var x = step
          while (x < size.width) {
            drawLine(
              color = BookCoverGridLine,
              start = Offset(x, 0f),
              end = Offset(x, size.height),
              strokeWidth = 1f,
            )
            x += step
          }
          var y = step
          while (y < size.height) {
            drawLine(
              color = BookCoverGridLine,
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = 1f,
            )
            y += step
          }
        }
      }
    }

    NotebookCoverStyle.KRAFT_TAN -> {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(BookCoverKraftTan),
      ) {
        Box(
          modifier =
            Modifier
              .width(14.dp)
              .fillMaxHeight()
              .background(BookCoverSpineKraft),
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val labelTop = h * 0.22f
          val labelHeight = h * 0.20f
          drawRoundRect(
            color = Color(0xFFFDFBF7),
            topLeft = Offset(w * 0.22f, labelTop),
            size = Size(w * 0.64f, labelHeight),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
          )
        }
      }
    }

    NotebookCoverStyle.WHITE_DOCUMENT,
    NotebookCoverStyle.HANDWRITTEN_NOTES -> {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(10.dp),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val spacing = h / 11f
          for (i in 1..10) {
            drawLine(
              color = Color(0xFFE5ECF2),
              start = Offset(0f, i * spacing),
              end = Offset(w, i * spacing),
              strokeWidth = 1.2f,
            )
          }
        }
        Column(modifier = Modifier.fillMaxSize()) {
          Text(
            text = "Study Notes",
            color = Color(0xFF1F5AA6),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
          )
        }
      }
    }
  }
}
