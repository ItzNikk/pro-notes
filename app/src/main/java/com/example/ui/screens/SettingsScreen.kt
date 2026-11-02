package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.model.PageScrollDirection
import com.example.model.StylusBarrelAction
import com.example.model.StylusPenBrand
import com.example.model.ToolbarDockPosition
import com.example.ui.theme.LocalProNotesColors
import com.example.ui.theme.NewButtonBlue
import com.example.ui.theme.SwitchBlue
import kotlin.math.roundToInt

private enum class SettingsCategory(val label: String) {
  WRITING("Writing"),
  STYLUS("Stylus & Pens"),
  GESTURES("Gestures"),
  READING("Reading"),
  PRIVACY("Privacy"),
  SUPPORT("Support Dev"),
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
  selectedStylusBrand: StylusPenBrand = StylusPenBrand.SAMSUNG_S_PEN,
  stylusBarrelAction: StylusBarrelAction = StylusBarrelAction.ERASE_WHILE_HELD,
  stylusPressureSensitivity: Float = 1.0f,
  stylusTiltShadingEnabled: Boolean = true,
  stylusHoverCursorEnabled: Boolean = true,
  stylusTailEraserEnabled: Boolean = true,
  developerUpiId: String = "itznikk@upi",
  developerDisplayName: String = "ItzNikk (Pro Notes Dev)",
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
  onSelectStylusBrand: (StylusPenBrand) -> Unit = {},
  onSelectStylusBarrelAction: (StylusBarrelAction) -> Unit = {},
  onUpdateStylusPressureSensitivity: (Float) -> Unit = {},
  onToggleStylusTiltShading: (Boolean) -> Unit = {},
  onToggleStylusHoverCursor: (Boolean) -> Unit = {},
  onToggleStylusTailEraser: (Boolean) -> Unit = {},
  onUpdateDeveloperUpiInfo: (String, String) -> Unit = { _, _ -> },
  onOpenThankYouScreen: () -> Unit = {},
  onStartInteractiveTour: () -> Unit = {},
  onEmptyTrash: () -> Unit,
  onResetAllDataToDefaults: () -> Unit,
  onNavigateBack: () -> Unit,
  onReplayOnboarding: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val colors = LocalProNotesColors.current
  val context = LocalContext.current
  var selectedCategory by rememberSaveable { mutableStateOf(SettingsCategory.WRITING) }

  var upiCopiedRecently by remember { mutableStateOf(false) }

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
        .widthIn(max = 720.dp)
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
            text = "Personalise your Pro Notes space & stylus hardware",
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
          modifier = Modifier.width(142.dp),
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
                fontSize = 14.sp,
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
                  val themeOptions = listOf(
                    AppThemeMode.DARK to "Dark",
                    AppThemeMode.LIGHT to "Light",
                    AppThemeMode.SYSTEM to "Auto",
                  )
                  SettingsSegmentedRow(
                    title = "App appearance",
                    options = themeOptions,
                    selected = appThemeMode,
                    onSelect = onSelectAppThemeMode,
                  )

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Stylus only mode",
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
                    title = "Left-handed writing posture",
                    checked = leftHandedPosture,
                    onCheckedChange = onToggleLeftHandedPosture,
                    testTag = "settings_left_handed_switch",
                  )
                }

                SettingsCategory.STYLUS -> {
                  Text(
                    text = "Active Stylus Hardware Profile",
                    color = colors.textPrimary,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Select your stylus for low-latency pressure curve & side-button optimization.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                  )
                  Spacer(modifier = Modifier.height(12.dp))

                  Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    StylusPenBrand.entries.forEach { penBrand ->
                      val isSelected = penBrand == selectedStylusBrand
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clip(RoundedCornerShape(12.dp))
                          .background(
                            if (isSelected) SwitchBlue.copy(alpha = 0.16f)
                            else colors.surfaceVariant
                          )
                          .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) SwitchBlue else colors.border,
                            shape = RoundedCornerShape(12.dp),
                          )
                          .clickable { onSelectStylusBrand(penBrand) }
                          .padding(horizontal = 14.dp, vertical = 10.dp)
                          .testTag("settings_pen_brand_${penBrand.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = penBrand.label,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                          )
                          Spacer(modifier = Modifier.height(2.dp))
                          Text(
                            text = penBrand.subtitle,
                            color = colors.textSecondary,
                            fontSize = 11.5.sp,
                          )
                        }
                        if (isSelected) {
                          Box(
                            modifier = Modifier
                              .size(24.dp)
                              .clip(CircleShape)
                              .background(SwitchBlue),
                            contentAlignment = Alignment.Center,
                          ) {
                            Icon(
                              imageVector = Icons.Default.Check,
                              contentDescription = "Selected",
                              tint = Color.White,
                              modifier = Modifier.size(15.dp),
                            )
                          }
                        }
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))
                  SettingsDivider()
                  Spacer(modifier = Modifier.height(8.dp))

                  Text(
                    text = "Side / Barrel Button Action",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StylusBarrelAction.entries.forEach { action ->
                      val isSel = action == stylusBarrelAction
                      Box(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clip(RoundedCornerShape(10.dp))
                          .background(if (isSel) SwitchBlue else colors.surfaceVariant)
                          .clickable { onSelectStylusBarrelAction(action) }
                          .padding(horizontal = 12.dp, vertical = 10.dp)
                          .testTag("settings_barrel_action_${action.name.lowercase()}"),
                      ) {
                        Text(
                          text = action.label,
                          color = if (isSel) Color.White else colors.textPrimary,
                          fontSize = 13.sp,
                          fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium,
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))
                  SettingsDivider()

                  // Pressure Sensitivity Slider
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 8.dp),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = "Pressure curve sensitivity",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                      )
                      Text(
                        text = "${(stylusPressureSensitivity * 100).roundToInt()}%",
                        color = SwitchBlue,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                      )
                    }
                    Slider(
                      value = stylusPressureSensitivity,
                      onValueChange = onUpdateStylusPressureSensitivity,
                      valueRange = 0.4f..1.8f,
                      colors = SliderDefaults.colors(
                        thumbColor = SwitchBlue,
                        activeTrackColor = SwitchBlue,
                        inactiveTrackColor = colors.surfaceVariant,
                      ),
                      modifier = Modifier.testTag("settings_stylus_pressure_slider"),
                    )
                  }

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Tilt shading (Pencil & Fountain)",
                    checked = stylusTiltShadingEnabled,
                    onCheckedChange = onToggleStylusTiltShading,
                    testTag = "settings_stylus_tilt_switch",
                  )

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Stylus hover preview cursor",
                    checked = stylusHoverCursorEnabled,
                    onCheckedChange = onToggleStylusHoverCursor,
                    testTag = "settings_stylus_hover_switch",
                  )

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Hardware tail eraser support",
                    checked = stylusTailEraserEnabled,
                    onCheckedChange = onToggleStylusTailEraser,
                    testTag = "settings_stylus_tail_eraser_switch",
                  )
                }

                SettingsCategory.GESTURES -> {
                  SettingsSwitchRow(
                    title = "Circle to select (Lasso)",
                    checked = circleToLasso,
                    onCheckedChange = onToggleCircleToLasso,
                    testTag = "settings_circle_to_lasso_switch",
                  )

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Draw & hold shape snap",
                    checked = drawAndHold,
                    onCheckedChange = onToggleDrawAndHold,
                    testTag = "settings_draw_and_hold_switch",
                  )

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Hold stylus button to erase",
                    checked = holdToErase,
                    onCheckedChange = onToggleHoldToErase,
                    testTag = "settings_hold_to_erase_switch",
                  )

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Scribble to erase",
                    checked = scribbleToErase,
                    onCheckedChange = onToggleScribbleToErase,
                    testTag = "settings_scribble_to_erase_switch",
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

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Pull to add new page",
                    checked = pullToAddPage,
                    onCheckedChange = onTogglePullToAddPage,
                    testTag = "settings_pull_to_add_page_switch",
                  )
                }

                SettingsCategory.PRIVACY -> {
                  SettingsSwitchRow(
                    title = "Hide notes in recent apps switcher",
                    checked = privacyScreenShieldEnabled,
                    onCheckedChange = onTogglePrivacyScreenShield,
                    testTag = "settings_privacy_shield_switch",
                  )

                  SettingsDivider()

                  SettingsSwitchRow(
                    title = "Watermark confidential PDF exports",
                    checked = watermarkConfidentialExports,
                    onCheckedChange = onToggleWatermarkConfidentialExports,
                    testTag = "settings_watermark_exports_switch",
                  )
                }

                SettingsCategory.SUPPORT -> {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp),
                  ) {
                    // Support the Developer Card (Fixed Developer UPI & Rainbow Thank You Screen)
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                          Brush.linearGradient(
                            colors = listOf(
                              Color(0xFF1E293B),
                              Color(0xFF172033),
                            ),
                          ),
                        )
                        .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                      ) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                          Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(20.dp),
                          )
                          Text(
                            text = "Support the Developer",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                          )
                        }
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                              Brush.linearGradient(
                                colors = listOf(
                                  Color(0xFFEC4899),
                                  Color(0xFF8B5CF6),
                                  Color(0xFF06B6D4),
                                ),
                              ),
                            )
                            .clickable(onClick = onOpenThankYouScreen)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("settings_open_thank_you_button"),
                        ) {
                          Text(
                            text = "Thank You Screen",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                          )
                        }
                      }

                      Spacer(modifier = Modifier.height(6.dp))
                      Text(
                        text = "Enjoying Pro Notes? Support independent development via UPI or open the Rainbow Thank You experience.",
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                      )

                      Spacer(modifier = Modifier.height(12.dp))

                      // Display active UPI ID pill + Copy button (non-editable in-app)
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clip(RoundedCornerShape(10.dp))
                          .background(Color.Black.copy(alpha = 0.32f))
                          .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                          .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = developerDisplayName,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                          )
                          Text(
                            text = developerUpiId,
                            color = Color(0xFF67C8EB),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("settings_upi_id_text"),
                          )
                        }
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable {
                              val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                              clipboard?.setPrimaryClip(ClipData.newPlainText("UPI ID", developerUpiId))
                              upiCopiedRecently = true
                              Toast.makeText(context, "Copied UPI ID: $developerUpiId", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("settings_copy_upi_button"),
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                          ) {
                            Icon(
                              imageVector = if (upiCopiedRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                              contentDescription = "Copy UPI ID",
                              tint = Color.White,
                              modifier = Modifier.size(14.dp),
                            )
                            Text(
                              text = if (upiCopiedRecently) "Copied" else "Copy UPI",
                              color = Color.White,
                              fontSize = 12.sp,
                              fontWeight = FontWeight.SemiBold,
                            )
                          }
                        }
                      }

                      Spacer(modifier = Modifier.height(10.dp))

                      // Launch UPI Payment App button
                      Box(
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(44.dp)
                          .clip(RoundedCornerShape(10.dp))
                          .background(Color(0xFF10B981))
                          .clickable {
                            runCatching {
                              val upiUri =
                                Uri.parse("upi://pay")
                                  .buildUpon()
                                  .appendQueryParameter("pa", developerUpiId.trim())
                                  .appendQueryParameter("pn", developerDisplayName.trim())
                                  .appendQueryParameter("tn", "Support Pro Notes Development")
                                  .appendQueryParameter("cu", "INR")
                                  .build()
                              val intent = Intent(Intent.ACTION_VIEW, upiUri)
                              context.startActivity(intent)
                            }.onFailure {
                              val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                              clipboard?.setPrimaryClip(ClipData.newPlainText("UPI ID", developerUpiId))
                              Toast.makeText(
                                context,
                                "No UPI app found. Copied $developerUpiId to clipboard!",
                                Toast.LENGTH_SHORT,
                              ).show()
                            }
                            onOpenThankYouScreen()
                          }
                          .testTag("settings_pay_upi_button"),
                        contentAlignment = Alignment.Center,
                      ) {
                        Text(
                          text = "Support via UPI App (GPay / PhonePe / Paytm)",
                          color = Color.White,
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold,
                        )
                      }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive Hands-On Feature Tour Launcher (Available to all users)
                    Text(
                      text = "Interactive Hands-On Tour",
                      color = colors.textPrimary,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "Jump into the live canvas and try Stylus Ink, Shapes Studio, Circle-to-Lasso, Color Laser, and Eraser step-by-step.",
                      color = colors.textSecondary,
                      fontSize = 12.5.sp,
                      lineHeight = 18.sp,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SwitchBlue)
                        .clickable(onClick = onStartInteractiveTour)
                        .testTag("settings_start_interactive_tour_button"),
                      contentAlignment = Alignment.Center,
                    ) {
                      Text(
                        text = "Take Interactive Feature Tour",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                      )
                    }

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
                        text = "Replay Welcome Screen",
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

      // Small muted footer (Triple-tap to launch Video Showcase)
      var footerTapCount by remember { mutableStateOf(0) }
      var footerLastTapMs by remember { mutableStateOf(0L) }
      Text(
        text = "Pro Notes · Founded by ItzNikk · Play Store Release Edition (3x Tap for Video Reel)",
        color = colors.textMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            val now = System.currentTimeMillis()
            footerTapCount = if (now - footerLastTapMs <= 550L) footerTapCount + 1 else 1
            footerLastTapMs = now
            if (footerTapCount >= 3) {
              footerTapCount = 0
              onOpenThankYouScreen()
            }
          }
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