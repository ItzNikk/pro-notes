package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.model.PageScrollDirection
import com.example.model.ToolbarDockPosition
import com.example.ui.theme.LocalProNotesColors
import com.example.ui.theme.SwitchBlue

private enum class SettingsCategory(val label: String) {
  WRITING("Writing"),
  GESTURES("Gestures"),
  READING("Reading"),
  PRIVACY("Privacy"),
  SUPPORT("Support"),
}

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
  onReplayOnboarding: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val colors = LocalProNotesColors.current
  var selectedCategory by rememberSaveable { mutableStateOf(SettingsCategory.WRITING) }

  val titleGradient = Brush.linearGradient(
    colors = listOf(
      Color(0xFFB57EDC),
      Color(0xFFF472B6),
    ),
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
      .testTag("settings_screen"),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 680.dp)
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
      // Header: Back button + Title with purple-to-pink gradient + Subtitle
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(colors.surfaceVariant)
            .clickable(onClick = onNavigateBack)
            .testTag("settings_back_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = colors.textPrimary,
            modifier = Modifier.size(20.dp),
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
          Text(
            text = "Settings",
            style = TextStyle(
              brush = titleGradient,
              fontSize = 28.sp,
              fontWeight = FontWeight.Bold,
            ),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Personalise your Pro Notes space",
            color = colors.textSecondary,
            fontSize = 13.5.sp,
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Two-column layout
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Left Column: Narrow text-only category menu
        Column(
          modifier = Modifier.width(136.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          SettingsCategory.entries.forEach { category ->
            val isSelected = selectedCategory == category
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) colors.surfaceVariant else Color.Transparent)
                .clickable { selectedCategory = category }
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("settings_nav_${category.name.lowercase()}"),
            ) {
              Text(
                text = category.label,
                color = if (isSelected) colors.textPrimary else colors.textSecondary,
                fontSize = 14.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
              )
            }
          }
        }

        // Right Column: One focused settings panel for the selected category
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        ) {
          AnimatedContent(
            targetState = selectedCategory,
            transitionSpec = {
              (fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(260)))
                .togetherWith(fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(200)))
            },
            label = "SettingsCategoryTransition",
          ) { category ->
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            ) {
              when (category) {
                SettingsCategory.WRITING -> {
                  SettingsSwitchRow(
                    title = "Stylus only",
                    checked = onlyDrawWithStylus,
                    onCheckedChange = onToggleOnlyDrawWithStylus,
                    testTag = "settings_stylus_only_switch",
                  )

                  SettingsDivider()

                  val palmOptions = listOf(
                    25f to "Low",
                    55f to "Standard",
                    85f to "High",
                  )
                  val selectedPalm = when {
                    palmRejectionSensitivity < 40f -> 25f
                    palmRejectionSensitivity < 70f -> 55f
                    else -> 85f
                  }
                  SettingsSegmentedRow(
                    title = "Palm rejection",
                    options = palmOptions,
                    selected = selectedPalm,
                    onSelect = onPalmRejectionSensitivityChange,
                  )

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Left-handed writing",
                    checked = leftHandedPosture,
                    onCheckedChange = onToggleLeftHandedPosture,
                    testTag = "settings_left_handed_switch",
                  )
                }

                SettingsCategory.GESTURES -> {
                  SettingsSwitchRow(
                    title = "Circle to select",
                    checked = circleToLasso,
                    onCheckedChange = onToggleCircleToLasso,
                    testTag = "settings_circle_to_lasso_switch",
                  )

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Shape recognition",
                    checked = drawAndHold,
                    onCheckedChange = onToggleDrawAndHold,
                    testTag = "settings_draw_and_hold_switch",
                  )

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Hold to erase",
                    checked = holdToErase,
                    onCheckedChange = onToggleHoldToErase,
                    testTag = "settings_hold_to_erase_switch",
                  )
                }

                SettingsCategory.READING -> {
                  val directionOptions = listOf(
                    PageScrollDirection.HORIZONTAL to "Horizontal",
                    PageScrollDirection.VERTICAL to "Vertical",
                  )
                  SettingsSegmentedRow(
                    title = "Page direction",
                    options = directionOptions,
                    selected = pageScrollDirection,
                    onSelect = onSelectPageScrollDirection,
                  )
                }

                SettingsCategory.PRIVACY -> {
                  SettingsSwitchRow(
                    title = "Hide notes in app previews",
                    checked = privacyScreenShieldEnabled,
                    onCheckedChange = onTogglePrivacyScreenShield,
                    testTag = "settings_privacy_shield_switch",
                  )
                }

                SettingsCategory.SUPPORT -> {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 8.dp),
                  ) {
                    Text(
                      text = "Support",
                      color = colors.textPrimary,
                      fontSize = 16.sp,
                      fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = "Support options will be available here soon.",
                      color = colors.textSecondary,
                      fontSize = 13.5.sp,
                      lineHeight = 20.sp,
                    )

                    if (com.example.BuildConfig.DEBUG) {
                      Spacer(modifier = Modifier.height(16.dp))
                      SettingsDivider()
                      Spacer(modifier = Modifier.height(12.dp))
                      Text(
                        text = "Developer Options (Debug Only)",
                        color = colors.textSecondary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                      )
                      Spacer(modifier = Modifier.height(10.dp))
                      Box(
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(44.dp)
                          .clip(RoundedCornerShape(10.dp))
                          .background(colors.surfaceVariant)
                          .clickable(onClick = onReplayOnboarding)
                          .testTag("settings_debug_replay_onboarding"),
                        contentAlignment = Alignment.Center,
                      ) {
                        Text(
                          text = "Replay Onboarding",
                          color = colors.textPrimary,
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Medium,
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Small muted footer
      Text(
        text = "Pro Notes · Founded by ItzNikk",
        color = colors.textMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 18.dp, bottom = 2.dp),
      )
    }
  }
}

@Composable
private fun SettingsSwitchRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  testTag: String? = null,
) {
  val colors = LocalProNotesColors.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = title,
      color = colors.textPrimary,
      fontSize = 15.sp,
      fontWeight = FontWeight.Medium,
    )
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = SwitchBlue,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = colors.surfaceVariant,
        uncheckedBorderColor = Color.Transparent,
      ),
    )
  }
}

@Composable
private fun <T> SettingsSegmentedRow(
  title: String,
  options: List<Pair<T, String>>,
  selected: T,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalProNotesColors.current
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
  ) {
    Text(
      text = title,
      color = colors.textPrimary,
      fontSize = 15.sp,
      fontWeight = FontWeight.Medium,
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(colors.surfaceVariant)
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      options.forEach { (value, label) ->
        val isSelected = value == selected
        Box(
          modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) SwitchBlue else Color.Transparent)
            .clickable { onSelect(value) },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = label,
            color = if (isSelected) Color.White else colors.textSecondary,
            fontSize = 13.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
          )
        }
      }
    }
  }
}

@Composable
private fun SettingsDivider(modifier: Modifier = Modifier) {
  val colors = LocalProNotesColors.current
  HorizontalDivider(
    color = colors.divider.copy(alpha = 0.45f),
    thickness = 0.5.dp,
    modifier = modifier.padding(vertical = 4.dp),
  )
}