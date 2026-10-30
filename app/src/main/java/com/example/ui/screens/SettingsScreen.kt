package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.Security
import com.example.model.NotebookDocument
import com.example.model.PageScrollDirection
import com.example.model.ToolbarDockPosition
import com.example.ui.components.DockPositionIcon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DocumentsMainBg
import com.example.ui.theme.PopoverBg
import com.example.ui.theme.PopoverCardSelected
import com.example.ui.theme.PopoverDivider
import com.example.ui.theme.SwitchBlue
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@Composable
fun SettingsScreen(
  notebooks: List<NotebookDocument>,
  onlyDrawWithStylus: Boolean,
  palmRejectionSensitivity: String,
  isLeftHandedPosture: Boolean,
  toolbarDockPosition: ToolbarDockPosition,
  pageScrollDirection: PageScrollDirection,
  pullToAddPage: Boolean,
  eraserAutoDeselect: Boolean,
  circleToLasso: Boolean,
  drawAndHold: Boolean,
  scribbleToErase: Boolean,
  holdToErase: Boolean,
  privacyScreenShieldEnabled: Boolean,
  watermarkConfidentialExports: Boolean,
  onToggleOnlyDrawWithStylus: (Boolean) -> Unit,
  onSelectPalmSensitivity: (String) -> Unit,
  onToggleLeftHandedPosture: (Boolean) -> Unit,
  onSelectToolbarDockPosition: (ToolbarDockPosition) -> Unit,
  onSelectPageScrollDirection: (PageScrollDirection) -> Unit,
  onResetSubToolbarPosition: () -> Unit,
  onTogglePullToAddPage: (Boolean) -> Unit,
  onToggleEraserAutoDeselect: (Boolean) -> Unit,
  onToggleCircleToLasso: (Boolean) -> Unit,
  onToggleDrawAndHold: (Boolean) -> Unit,
  onToggleScribbleToErase: (Boolean) -> Unit,
  onToggleHoldToErase: (Boolean) -> Unit,
  onTogglePrivacyScreenShield: (Boolean) -> Unit,
  onToggleWatermarkConfidentialExports: (Boolean) -> Unit,
  onEmptyTrash: () -> Unit,
  onClearAllLocalData: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val totalPages = notebooks.sumOf { it.pages.size }
  val totalStrokes = notebooks.sumOf { nb -> nb.pages.sumOf { it.strokes.size } }
  val trashedCount = notebooks.count { it.isTrashed }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(DocumentsMainBg)
        .testTag("settings_screen"),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 680.dp)
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Top Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier =
              Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PopoverBg)
                .border(1.dp, PopoverDivider, RoundedCornerShape(10.dp))
                .clickable(onClick = onBackClick)
                .testTag("settings_back_button"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = TextPrimaryWhite,
              modifier = Modifier.size(20.dp),
            )
          }
          Column {
            Text(
              text = "Goodnotes Settings & Preferences",
              color = TextPrimaryWhite,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Stylus & Palm Rejection, Page Scrolling, Toolbar Layout, and Local Storage",
              color = TextSecondaryGray,
              fontSize = 12.sp,
            )
          }
        }
      }

      // Section 1: Stylus & Palm Rejection (Finger Swipe & Zoom)
      SettingsSectionCard(
        title = "Stylus & Palm Rejection",
        icon = Icons.Outlined.PanTool,
      ) {
        SettingsSwitchItem(
          title = "Only Draw with Stylus (Finger Swipe & Zoom)",
          subtitle = "When enabled, only an active Stylus draws ink while finger swipes turn pages (Horizontal or Vertical) and 2 fingers pinch-zoom.",
          checked = onlyDrawWithStylus,
          onCheckedChange = onToggleOnlyDrawWithStylus,
          testTag = "settings_stylus_only_switch",
        )

        HorizontalDivider(color = PopoverDivider, modifier = Modifier.padding(vertical = 8.dp))

        Text(
          text = "Palm Rejection Sensitivity",
          color = TextPrimaryWhite,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf("Low", "Standard", "High").forEach { level ->
            val isSelected = palmRejectionSensitivity == level
            Box(
              modifier =
                Modifier
                  .weight(1f)
                  .height(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) SwitchBlue else PopoverCardSelected)
                  .clickable { onSelectPalmSensitivity(level) },
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = level,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              )
            }
          }
        }

        HorizontalDivider(color = PopoverDivider, modifier = Modifier.padding(vertical = 8.dp))

        SettingsSwitchItem(
          title = "Left-Handed Writing Posture",
          subtitle = "Optimizes palm rejection zone for left-handed stylus writing angles.",
          checked = isLeftHandedPosture,
          onCheckedChange = onToggleLeftHandedPosture,
          testTag = "settings_left_handed_switch",
        )
      }

      // Section 2: Document Editing, Scrolling Direction & Repositionable Toolbar
      SettingsSectionCard(
        title = "Document Editing & Scrolling Direction",
        icon = Icons.Outlined.Tune,
      ) {
        Text(
          text = "Page Scrolling Direction (PDFs & Notebooks)",
          color = TextPrimaryWhite,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Medium,
        )
        Text(
          text = "Choose whether swiping navigates pages Horizontally (book-style) or Vertically (continuous scroll).",
          color = TextSecondaryGray,
          fontSize = 11.5.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          PageScrollDirection.entries.forEach { dir ->
            val isSelected = pageScrollDirection == dir
            Box(
              modifier =
                Modifier
                  .weight(1f)
                  .height(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) SwitchBlue else PopoverCardSelected)
                  .clickable { onSelectPageScrollDirection(dir) }
                  .testTag("settings_scroll_${dir.name.lowercase()}"),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "${dir.label} Swipe",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              )
            }
          }
        }

        HorizontalDivider(color = PopoverDivider, modifier = Modifier.padding(vertical = 8.dp))

        Text(
          text = "Main Toolbar Position (Top, Bottom, Left, Right)",
          color = TextPrimaryWhite,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Medium,
        )
        Text(
          text = "Dock the primary navy toolbar at the Top, Bottom, Left, or Right edge, or long-press the toolbar in the editor to reposition it anytime.",
          color = TextSecondaryGray,
          fontSize = 11.5.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          ToolbarDockPosition.entries.forEach { pos ->
            val isSelected = toolbarDockPosition == pos
            Row(
              modifier =
                Modifier
                  .weight(1f)
                  .height(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) SwitchBlue else PopoverCardSelected)
                  .clickable { onSelectToolbarDockPosition(pos) }
                  .padding(horizontal = 6.dp)
                  .testTag("settings_dock_${pos.name.lowercase()}"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              DockPositionIcon(
                position = pos,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = pos.label,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(PopoverCardSelected)
              .clickable(onClick = onResetSubToolbarPosition),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Reset Floating Writing Tools Pill to Center",
            color = Color(0xFF67C8EB),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }

        HorizontalDivider(color = PopoverDivider, modifier = Modifier.padding(vertical = 8.dp))

        SettingsSwitchItem(
          title = "Pull to Add New Page",
          subtitle = "Automatically append a matching template page when swiping past the last page.",
          checked = pullToAddPage,
          onCheckedChange = onTogglePullToAddPage,
          testTag = "settings_pull_add_page_switch",
        )

        HorizontalDivider(color = PopoverDivider, modifier = Modifier.padding(vertical = 8.dp))

        SettingsSwitchItem(
          title = "Auto-Deselect Eraser",
          subtitle = "Automatically switch back to Writing Tools after lifting the Eraser.",
          checked = eraserAutoDeselect,
          onCheckedChange = onToggleEraserAutoDeselect,
          testTag = "settings_auto_deselect_eraser_switch",
        )
      }

      // Section 3: Pen Gestures & Smart Ink
      SettingsSectionCard(
        title = "Pen Gestures & Smart Ink",
        icon = Icons.Outlined.Draw,
      ) {
        SettingsSwitchItem(
          title = "Circle to Lasso (Circle → Lift → Hold Circle)",
          subtitle = "Official Goodnotes 6 2-step gesture: draw a circle around content, lift your stylus, then touch and hold on or inside that circle stroke to turn it into an active Lasso selection!",
          checked = circleToLasso,
          onCheckedChange = onToggleCircleToLasso,
          testTag = "settings_circle_to_lasso_switch",
        )

        HorizontalDivider(color = PopoverDivider, modifier = Modifier.padding(vertical = 8.dp))

        SettingsSwitchItem(
          title = "Draw and Hold (11 Auto-Recognized Shapes)",
          subtitle = "Pause at the end of a stroke to automatically recognize and snap into Lines, Arrows, Curves, Circles, Ovals, Triangles, Rectangles, Diamonds, Pentagons, Hexagons, or Stars.",
          checked = drawAndHold,
          onCheckedChange = onToggleDrawAndHold,
          testTag = "settings_draw_and_hold_switch",
        )

        HorizontalDivider(color = PopoverDivider, modifier = Modifier.padding(vertical = 8.dp))

        SettingsSwitchItem(
          title = "Scribble to Erase",
          checked = scribbleToErase,
          subtitle = "Scratch out handwriting rapidly back-and-forth to erase strokes beneath.",
          onCheckedChange = onToggleScribbleToErase,
          testTag = "settings_scribble_to_erase_switch",
        )

        HorizontalDivider(color = PopoverDivider, modifier = Modifier.padding(vertical = 8.dp))

        SettingsSwitchItem(
          title = "Hold in Place & S Pen Side Button Erase",
          subtitle = "Press and hold stationary on the page OR press the S Pen barrel side button to activate immediate precision erasing.",
          checked = holdToErase,
          onCheckedChange = onToggleHoldToErase,
          testTag = "settings_hold_to_erase_switch",
        )
      }

      // Section 4: Cybersecurity & Privacy Vault
      SettingsSectionCard(
        title = "Cybersecurity & Privacy Vault",
        icon = Icons.Outlined.Security,
      ) {
        SettingsSwitchItem(
          title = "Privacy Screen Shield (FLAG_SECURE)",
          subtitle = "Hardware-level Android window protection that blocks unauthorized screenshots, screen recording, and hides sensitive notes in the recent apps switcher.",
          checked = privacyScreenShieldEnabled,
          onCheckedChange = onTogglePrivacyScreenShield,
          testTag = "settings_privacy_shield_switch",
        )

        HorizontalDivider(color = PopoverDivider, modifier = Modifier.padding(vertical = 8.dp))

        SettingsSwitchItem(
          title = "Confidential SHA-256 PDF Export Watermark",
          subtitle = "Embeds a cryptographic SHA-256 document integrity fingerprint and timestamp footer on exported PDF documents.",
          checked = watermarkConfidentialExports,
          onCheckedChange = onToggleWatermarkConfidentialExports,
          testTag = "settings_watermark_exports_switch",
        )
      }

      // Section 4: Local Device Storage (Room SQLite)
      SettingsSectionCard(
        title = "On-Device Local Storage (Room SQLite)",
        icon = Icons.Outlined.Storage,
      ) {
        Text(
          text = "All your notebooks, PDFs, pages, and ink strokes are stored privately on your local device.",
          color = TextSecondaryGray,
          fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF16181D))
              .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
          StorageStatItem(label = "Notebooks", value = "${notebooks.size}")
          StorageStatItem(label = "Total Pages", value = "$totalPages")
          StorageStatItem(label = "Ink Strokes", value = "$totalStrokes")
          StorageStatItem(label = "In Trash", value = "$trashedCount")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier =
              Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PopoverCardSelected)
                .clickable(onClick = onEmptyTrash),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
          ) {
            Icon(
              imageVector = Icons.Outlined.DeleteSweep,
              contentDescription = null,
              tint = TextPrimaryWhite,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Empty Trash ($trashedCount)",
              color = TextPrimaryWhite,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
            )
          }

          Row(
            modifier =
              Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DangerRed.copy(alpha = 0.16f))
                .border(1.dp, DangerRed.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                .clickable(onClick = onClearAllLocalData)
                .testTag("settings_clear_all_data_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
          ) {
            Text(
              text = "Reset All Local Notes",
              color = DangerRed,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun SettingsSectionCard(
  title: String,
  icon: ImageVector,
  content: @Composable () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(14.dp))
        .padding(16.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color(0xFF67C8EB),
        modifier = Modifier.size(19.dp),
      )
      Text(
        text = title,
        color = TextPrimaryWhite,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
      )
    }
    Spacer(modifier = Modifier.height(12.dp))
    content()
  }
}

@Composable
private fun SettingsSwitchItem(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
      Text(
        text = title,
        color = TextPrimaryWhite,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Medium,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        color = TextSecondaryGray,
        fontSize = 11.5.sp,
      )
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = SwitchBlue,
          uncheckedThumbColor = Color.White,
          uncheckedTrackColor = Color(0xFF3A3A3C),
        ),
      modifier = Modifier.testTag(testTag),
    )
  }
}

@Composable
private fun StorageStatItem(label: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      color = Color(0xFF67C8EB),
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = label,
      color = TextSecondaryGray,
      fontSize = 11.sp,
    )
  }
}
