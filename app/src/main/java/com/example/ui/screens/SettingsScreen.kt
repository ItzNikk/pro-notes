package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppThemeMode
import com.example.model.PageScrollDirection
import com.example.model.ToolbarDockPosition
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LocalProNotesColors
import com.example.ui.theme.SwitchBlue

private enum class SettingsCategory(val label: String, val icon: ImageVector, val tag: String) {
  GENERAL("General", Icons.Outlined.Tune, "settings_nav_general"),
  PEN("Pen & Stylus", Icons.Outlined.Draw, "settings_nav_pen"),
  PRIVACY("Privacy & Data", Icons.Outlined.Security, "settings_nav_privacy"),
  ABOUT("About", Icons.Outlined.Info, "settings_nav_about"),
}

/**
 * GoodNotes-style settings: category sidebar on the left, grouped inset lists on the right.
 *
 * NOTE: The signature is unchanged so existing call sites keep compiling. These parameters are
 * no longer shown in the UI and can be deleted (here and at the call site) whenever you like:
 * palmRejectionSensitivity, onPalmRejectionSensitivityChange, eraserAutoDeselect,
 * onToggleEraserAutoDeselect, watermarkConfidentialExports, onToggleWatermarkConfidentialExports,
 * activeNotebookCount, activeFolderCount, totalPagesCount.
 */
@Composable
fun SettingsScreen(
  appThemeMode: AppThemeMode = AppThemeMode.DARK,
  onlyDrawWithStylus: Boolean,
  palmRejectionSensitivity: Float,
  leftHandedPosture: Boolean,
  pullToAddPage: Boolean,
  pageScrollDirection: PageScrollDirection,
  toolbarDockPosition: ToolbarDockPosition = ToolbarDockPosition.TOP,
  eraserAutoDeselect: Boolean,
  drawAndHold: Boolean,
  circleToLasso: Boolean,
  scribbleToErase: Boolean,
  holdToErase: Boolean,
  privacyScreenShieldEnabled: Boolean = true,
  watermarkConfidentialExports: Boolean = true,
  activeNotebookCount: Int,
  activeFolderCount: Int = 0,
  trashedNotebookCount: Int,
  totalPagesCount: Int,
  onSelectAppThemeMode: (AppThemeMode) -> Unit = {},
  onToggleOnlyDrawWithStylus: (Boolean) -> Unit,
  onPalmRejectionSensitivityChange: (Float) -> Unit,
  onToggleLeftHandedPosture: (Boolean) -> Unit,
  onTogglePullToAddPage: (Boolean) -> Unit,
  onSelectPageScrollDirection: (PageScrollDirection) -> Unit,
  onSelectToolbarDockPosition: (ToolbarDockPosition) -> Unit = {},
  onResetSubToolbarPosition: () -> Unit = {},
  onToggleEraserAutoDeselect: (Boolean) -> Unit,
  onToggleDrawAndHold: (Boolean) -> Unit,
  onToggleCircleToLasso: (Boolean) -> Unit,
  onToggleScribbleToErase: (Boolean) -> Unit,
  onToggleHoldToErase: (Boolean) -> Unit,
  onTogglePrivacyScreenShield: (Boolean) -> Unit = {},
  onToggleWatermarkConfidentialExports: (Boolean) -> Unit = {},
  onEmptyTrash: () -> Unit,
  onResetAllDataToDefaults: () -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalProNotesColors.current
  var selected by rememberSaveable { mutableStateOf(SettingsCategory.GENERAL) }
  var showEmptyTrashConfirm by remember { mutableStateOf(false) }
  var showResetFactoryConfirm by remember { mutableStateOf(false) }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(colors.background)
        .testTag("settings_screen"),
  ) {
    Row(modifier = Modifier.fillMaxSize()) {
      // ───────────── Sidebar ─────────────
      Column(
        modifier =
          Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(colors.surface)
            .padding(horizontal = 14.dp, vertical = 18.dp),
      ) {
        Row(
          modifier = Modifier.padding(start = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier =
              Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onNavigateBack)
                .testTag("settings_back_button"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = colors.textPrimary,
              modifier = Modifier.size(22.dp),
            )
          }
          Text(
            text = "Settings",
            color = colors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
          )
        }

        Spacer(modifier = Modifier.height(22.dp))

        SettingsCategory.entries.forEach { category ->
          SidebarItem(
            category = category,
            isSelected = category == selected,
            onClick = { selected = category },
          )
          Spacer(modifier = Modifier.height(2.dp))
        }
      }

      Box(
        modifier =
          Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(colors.divider),
      )

      // ───────────── Content ─────────────
      key(selected) {
        Box(
          modifier =
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .verticalScroll(rememberScrollState())
              .padding(horizontal = 32.dp, vertical = 28.dp),
          contentAlignment = Alignment.TopCenter,
        ) {
          Column(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(28.dp),
          ) {
            Text(
              text = selected.label,
              color = colors.textPrimary,
              fontSize = 30.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(start = 4.dp),
            )

            when (selected) {
              SettingsCategory.GENERAL -> {
                SettingsGroup(header = "Appearance") {
                  SettingsRow(
                    title = "Theme",
                    showDivider = false,
                    trailing = {
                      SegmentedControl(
                        options = AppThemeMode.entries,
                        selected = appThemeMode,
                        label = { it.label },
                        tag = { "settings_theme_${it.name.lowercase()}" },
                        onSelect = onSelectAppThemeMode,
                      )
                    },
                  )
                }

                SettingsGroup(header = "Pages") {
                  SettingsRow(
                    title = "Page Scrolling",
                    trailing = {
                      SegmentedControl(
                        options = PageScrollDirection.entries,
                        selected = pageScrollDirection,
                        label = { it.label },
                        tag = { "settings_scroll_${it.name.lowercase()}" },
                        onSelect = onSelectPageScrollDirection,
                      )
                    },
                  )
                  SettingsToggleRow(
                    title = "Pull to Add Page",
                    subtitle = "Swipe past the last page to add a new one",
                    checked = pullToAddPage,
                    onCheckedChange = onTogglePullToAddPage,
                    testTag = "settings_pull_to_add_page_switch",
                    showDivider = false,
                  )
                }

                SettingsGroup(header = "Toolbar") {
                  SettingsRow(
                    title = "Toolbar Position",
                    trailing = {
                      SegmentedControl(
                        options = ToolbarDockPosition.entries,
                        selected = toolbarDockPosition,
                        label = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                        tag = { "settings_toolbar_${it.name.lowercase()}" },
                        onSelect = onSelectToolbarDockPosition,
                        segmentWidth = 76.dp,
                      )
                    },
                  )
                  SettingsRow(
                    title = "Reset Toolbar Position",
                    titleColor = colors.accentBlue,
                    showDivider = false,
                    modifier =
                      Modifier
                        .clickable(onClick = onResetSubToolbarPosition)
                        .testTag("settings_reset_toolbar_button"),
                  )
                }
              }

              SettingsCategory.PEN -> {
                SettingsGroup(header = "Input") {
                  SettingsToggleRow(
                    title = "Only Draw with Stylus",
                    subtitle = "Your S Pen draws while your finger scrolls and zooms",
                    checked = onlyDrawWithStylus,
                    onCheckedChange = onToggleOnlyDrawWithStylus,
                    testTag = "settings_stylus_only_switch",
                  )
                  SettingsToggleRow(
                    title = "Left-Handed Mode",
                    subtitle = "Moves palm rejection to the left side of the page",
                    checked = leftHandedPosture,
                    onCheckedChange = onToggleLeftHandedPosture,
                    testTag = "settings_left_handed_switch",
                    showDivider = false,
                  )
                }

                SettingsGroup(header = "Gestures") {
                  SettingsToggleRow(
                    title = "Draw and Hold",
                    subtitle = "Pause at the end of a stroke to snap it into a clean shape",
                    checked = drawAndHold,
                    onCheckedChange = onToggleDrawAndHold,
                    testTag = "settings_draw_and_hold_switch",
                  )
                  SettingsToggleRow(
                    title = "Circle to Lasso",
                    subtitle = "Loop around your writing and hold to select it",
                    checked = circleToLasso,
                    onCheckedChange = onToggleCircleToLasso,
                    testTag = "settings_circle_to_lasso_switch",
                  )
                  SettingsToggleRow(
                    title = "Scribble to Erase",
                    subtitle = "Scribble over ink to erase it without changing tools",
                    checked = scribbleToErase,
                    onCheckedChange = onToggleScribbleToErase,
                    testTag = "settings_scribble_to_erase_switch",
                  )
                  SettingsToggleRow(
                    title = "S Pen Button Erases",
                    subtitle = "Hold the side button, or press and hold, to erase",
                    checked = holdToErase,
                    onCheckedChange = onToggleHoldToErase,
                    testTag = "settings_hold_to_erase_switch",
                    showDivider = false,
                  )
                }
              }

              SettingsCategory.PRIVACY -> {
                SettingsGroup(
                  header = "Privacy",
                  footer = "Also hides your notes in the recent apps view.",
                ) {
                  SettingsToggleRow(
                    title = "Block Screenshots & Recording",
                    checked = privacyScreenShieldEnabled,
                    onCheckedChange = onTogglePrivacyScreenShield,
                    testTag = "settings_privacy_shield_switch",
                    showDivider = false,
                  )
                }

                SettingsGroup(
                  header = "Data",
                  footer = "Resetting deletes all your notebooks and folders and restores the starter notebooks. This can't be undone.",
                ) {
                  val hasTrash = trashedNotebookCount > 0
                  SettingsRow(
                    title = "Empty Trash",
                    titleColor = if (hasTrash) DangerRed else colors.textSecondary,
                    modifier =
                      Modifier
                        .clickable(enabled = hasTrash) { showEmptyTrashConfirm = true }
                        .testTag("settings_empty_trash_button"),
                    trailing = {
                      Text(
                        text = "$trashedNotebookCount",
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                      )
                    },
                  )
                  SettingsRow(
                    title = "Reset All Notes",
                    titleColor = DangerRed,
                    showDivider = false,
                    modifier =
                      Modifier
                        .clickable { showResetFactoryConfirm = true }
                        .testTag("settings_reset_defaults_button"),
                  )
                }
              }

              SettingsCategory.ABOUT -> {
                Column(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Pro Notes Logo",
                    contentScale = ContentScale.Crop,
                    modifier =
                      Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, colors.border, RoundedCornerShape(18.dp)),
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = "Pro Notes",
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Handwriting, PDF annotation, and infinite whiteboards",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                  )
                }
                SettingsGroup {
                  SettingsRow(
                    title = "Version",
                    showDivider = false,
                    trailing = {
                      Text(text = "2.0.0", color = colors.textSecondary, fontSize = 14.sp)
                    },
                  )
                }
              }
            }
          }
        }
      }
    }

    if (showEmptyTrashConfirm) {
      DestructiveConfirmationModal(
        title = "Empty Trash?",
        message = "Everything in the trash will be permanently deleted. This can't be undone.",
        confirmText = "Empty Trash",
        onConfirm = {
          showEmptyTrashConfirm = false
          onEmptyTrash()
        },
        onDismiss = { showEmptyTrashConfirm = false },
      )
    }

    if (showResetFactoryConfirm) {
      DestructiveConfirmationModal(
        title = "Reset All Notes?",
        message = "All your notebooks and folders will be deleted and the starter notebooks restored. This can't be undone.",
        confirmText = "Reset Everything",
        onConfirm = {
          showResetFactoryConfirm = false
          onResetAllDataToDefaults()
        },
        onDismiss = { showResetFactoryConfirm = false },
      )
    }
  }
}

@Composable
private fun SidebarItem(
  category: SettingsCategory,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(46.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(if (isSelected) colors.accentBlue.copy(alpha = 0.14f) else Color.Transparent)
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp)
        .testTag(category.tag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Icon(
      imageVector = category.icon,
      contentDescription = null,
      tint = if (isSelected) colors.accentBlue else colors.textSecondary,
      modifier = Modifier.size(21.dp),
    )
    Text(
      text = category.label,
      color = if (isSelected) colors.accentBlue else colors.textPrimary,
      fontSize = 15.sp,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
    )
  }
}

@Composable
private fun SettingsGroup(
  header: String? = null,
  footer: String? = null,
  content: @Composable () -> Unit,
) {
  val colors = LocalProNotesColors.current
  Column(modifier = Modifier.fillMaxWidth()) {
    if (header != null) {
      Text(
        text = header.uppercase(),
        color = colors.textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
      )
    }
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(colors.surface),
    ) {
      content()
    }
    if (footer != null) {
      Text(
        text = footer,
        color = colors.textSecondary,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
      )
    }
  }
}

@Composable
private fun SettingsRow(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  titleColor: Color? = null,
  showDivider: Boolean = true,
  trailing: (@Composable () -> Unit)? = null,
) {
  val colors = LocalProNotesColors.current
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier =
        modifier
          .fillMaxWidth()
          .heightIn(min = 54.dp)
          .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
        Text(
          text = title,
          color = titleColor ?: colors.textPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Medium,
        )
        if (subtitle != null) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(text = subtitle, color = colors.textSecondary, fontSize = 12.sp)
        }
      }
      trailing?.invoke()
    }
    if (showDivider) {
      HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = colors.divider)
    }
  }
}

@Composable
private fun SettingsToggleRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
  subtitle: String? = null,
  showDivider: Boolean = true,
) {
  val colors = LocalProNotesColors.current
  SettingsRow(
    title = title,
    subtitle = subtitle,
    showDivider = showDivider,
    trailing = {
      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors =
          SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = SwitchBlue,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = colors.border,
            uncheckedBorderColor = Color.Transparent,
          ),
        modifier = Modifier.testTag(testTag),
      )
    },
  )
}

@Composable
private fun <T> SegmentedControl(
  options: List<T>,
  selected: T,
  label: (T) -> String,
  tag: (T) -> String,
  onSelect: (T) -> Unit,
  segmentWidth: androidx.compose.ui.unit.Dp = 88.dp,
) {
  val colors = LocalProNotesColors.current
  Row(
    modifier =
      Modifier
        .clip(RoundedCornerShape(10.dp))
        .background(colors.surfaceVariant)
        .padding(2.dp),
  ) {
    options.forEach { option ->
      val isSelected = option == selected
      Box(
        modifier =
          Modifier
            .width(segmentWidth)
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.accentBlue else Color.Transparent)
            .clickable { onSelect(option) }
            .testTag(tag(option)),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = label(option),
          color = if (isSelected) Color.White else colors.textPrimary,
          fontSize = 13.sp,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        )
      }
    }
  }
}

@Composable
private fun DestructiveConfirmationModal(
  title: String,
  message: String,
  confirmText: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = LocalProNotesColors.current
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f))
        .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 400.dp)
          .fillMaxWidth(0.9f)
          .clip(RoundedCornerShape(18.dp))
          .background(colors.popoverBg)
          .border(1.dp, colors.border, RoundedCornerShape(18.dp))
          .clickable(enabled = false) {}
          .padding(22.dp)
          .testTag("destructive_confirm_dialog"),
    ) {
      Text(
        text = title,
        color = colors.textPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = message, color = colors.textSecondary, fontSize = 14.sp)
      Spacer(modifier = Modifier.height(22.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(colors.surfaceVariant)
              .clickable(onClick = onDismiss),
          contentAlignment = Alignment.Center,
        ) {
          Text("Cancel", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(DangerRed)
              .clickable(onClick = onConfirm)
              .testTag("confirm_destructive_action_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text(confirmText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}