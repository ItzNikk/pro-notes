package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.Security
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotebookDocument
import com.example.model.PageScrollDirection
import com.example.model.PrimaryTool
import com.example.model.ToolbarDockPosition
import com.example.ui.components.DockPositionIcon
import com.example.ui.components.PrimaryToolCustomIcon
import com.example.ui.components.SPenBarrelButtonIcon
import com.example.ui.components.SecurityVaultShieldIcon
import com.example.ui.theme.ActiveToolIconDark
import com.example.ui.theme.ActiveToolPill
import com.example.ui.theme.CanvasWorkspaceBg
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PopoverBg
import com.example.ui.theme.PopoverCardSelected
import com.example.ui.theme.PopoverDivider
import com.example.ui.theme.SubToolbarActiveCyan
import com.example.ui.theme.SubToolbarActiveTeal
import com.example.ui.theme.SubToolbarBorder
import com.example.ui.theme.SubToolbarPillBg
import com.example.ui.theme.SwitchBlue
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray
import com.example.ui.theme.ToolbarIconLight
import com.example.ui.theme.ToolbarNavy

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

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(CanvasWorkspaceBg)
        .testTag("settings_screen"),
  ) {
    // 1:1 Goodnotes 6 Navy Top Bar (Matches Editor MainNavyToolbar & Documents Workspace)
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(54.dp)
          .background(ToolbarNavy)
          .padding(horizontal = 14.dp),
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
              .size(36.dp)
              .clip(RoundedCornerShape(9.dp))
              .background(ActiveToolPill.copy(alpha = 0.16f))
              .clickable(onClick = onBackClick)
              .testTag("settings_back_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = ToolbarIconLight,
            modifier = Modifier.size(20.dp),
          )
        }
        Column {
          Text(
            text = "Workspace & Stylus Settings",
            color = TextPrimaryWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Connected live to your Goodnotes 6 Editor & PDF Reader",
            color = ToolbarIconLight.copy(alpha = 0.78f),
            fontSize = 11.sp,
          )
        }
      }

      // Connected Quick Tools Badge Cluster (Matches Editor Navy Toolbar)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(ActiveToolPill),
          contentAlignment = Alignment.Center,
        ) {
          PrimaryToolCustomIcon(
            tool = PrimaryTool.WRITING_TOOLS,
            tint = ActiveToolIconDark,
            modifier = Modifier.size(20.dp),
          )
        }
        Box(
          modifier =
            Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.White.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center,
        ) {
          PrimaryToolCustomIcon(
            tool = PrimaryTool.LASSO,
            tint = ToolbarIconLight,
            modifier = Modifier.size(20.dp),
          )
        }
        Row(
          modifier =
            Modifier
              .height(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(SwitchBlue)
              .clickable(onClick = onBackClick)
              .padding(horizontal = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(15.dp),
          )
          Text(
            text = "Done",
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }

    // Live Connected Editor Status Pill Strip (Matches FloatingWritingSubToolbar aesthetic)
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(SubToolbarPillBg)
          .border(width = 0.5.dp, color = SubToolbarBorder)
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      ConnectedStatusChip(
        label = if (onlyDrawWithStylus) "Stylus-Only Mode" else "Stylus + Finger Ink",
        active = onlyDrawWithStylus,
        onClick = { onToggleOnlyDrawWithStylus(!onlyDrawWithStylus) },
      )
      ConnectedStatusChip(
        label = "Scroll: ${pageScrollDirection.label}",
        active = true,
        onClick = {
          onSelectPageScrollDirection(
            if (pageScrollDirection == PageScrollDirection.HORIZONTAL) PageScrollDirection.VERTICAL
            else PageScrollDirection.HORIZONTAL
          )
        },
      )
      ConnectedStatusChip(
        label = "Toolbar: ${toolbarDockPosition.label}",
        active = true,
        onClick = {},
      )
      ConnectedStatusChip(
        label = if (circleToLasso) "Circle-to-Lasso: ON" else "Circle-to-Lasso: OFF",
        active = circleToLasso,
        onClick = { onToggleCircleToLasso(!circleToLasso) },
      )
      ConnectedStatusChip(
        label = if (!privacyScreenShieldEnabled) "Screen Recording: Allowed" else "FLAG_SECURE: Locked",
        active = !privacyScreenShieldEnabled,
        onClick = { onTogglePrivacyScreenShield(!privacyScreenShieldEnabled) },
      )
    }

    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier
            .widthIn(max = 700.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Section 1: Stylus & Palm Rejection (Finger Swipe & Zoom)
        SettingsSectionCard(
          title = "Stylus, S Pen & Palm Rejection",
          subtitle = "Controls how active styluses, S Pen side buttons, and finger gestures interact with PDFs and pages",
          icon = Icons.Outlined.PanTool,
          badgeContent = {
            SPenBarrelButtonIcon(
              tint = SubToolbarActiveCyan,
              isButtonPressed = holdToErase,
              modifier = Modifier.size(18.dp),
            )
          },
        ) {
          SettingsSwitchItem(
            title = "Only Draw with Stylus (Finger Swipe & PDF Pinch-Zoom)",
            subtitle = "When enabled, only an active Stylus draws ink while 1-finger swipes turn pages (or pan when zoomed) and 2 fingers pinch-zoom.",
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
                    .height(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) SwitchBlue else PopoverCardSelected)
                    .border(
                      width = 1.dp,
                      color = if (isSelected) SubToolbarActiveCyan.copy(alpha = 0.65f) else SubToolbarBorder,
                      shape = RoundedCornerShape(9.dp),
                    )
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
          title = "Document Editing, PDF Zoom & Toolbar Dock",
          subtitle = "Configure book-style horizontal or vertical page scrolling and main navy toolbar placement",
          icon = Icons.Outlined.Tune,
          badgeContent = {
            DockPositionIcon(
              position = toolbarDockPosition,
              tint = SubToolbarActiveCyan,
              modifier = Modifier.size(18.dp),
            )
          },
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
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) SwitchBlue else PopoverCardSelected)
                    .border(
                      width = 1.dp,
                      color = if (isSelected) SubToolbarActiveCyan.copy(alpha = 0.65f) else SubToolbarBorder,
                      shape = RoundedCornerShape(9.dp),
                    )
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
            text = "Main Navy Toolbar Position (Top, Bottom, Left, Right)",
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
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) SwitchBlue else PopoverCardSelected)
                    .border(
                      width = 1.dp,
                      color = if (isSelected) SubToolbarActiveCyan.copy(alpha = 0.65f) else SubToolbarBorder,
                      shape = RoundedCornerShape(9.dp),
                    )
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
                .height(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(SubToolbarPillBg)
                .border(1.dp, SubToolbarBorder, RoundedCornerShape(9.dp))
                .clickable(onClick = onResetSubToolbarPosition),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "Reset Floating Writing Tools Pill to Top-Center",
              color = SubToolbarActiveCyan,
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
          title = "Pen Gestures, Circle-to-Lasso & Smart Ink",
          subtitle = "Interactive Lasso actions (Move, Cut, Copy, Paste, Resize, Rotate, Recolor, Duplicate, Delete)",
          icon = Icons.Outlined.Draw,
          badgeContent = {
            PrimaryToolCustomIcon(
              tool = PrimaryTool.LASSO,
              tint = SubToolbarActiveCyan,
              modifier = Modifier.size(19.dp),
            )
          },
        ) {
          SettingsSwitchItem(
            title = "Circle to Lasso (Circle → Hold or Lift → Hold Circle)",
            subtitle = "Draw a closed circle around content and hold (or lift and hold on/inside the circle) to convert it into an active Lasso selection with Cut, Copy, Paste, Resize, Recolor, and Delete.",
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
            subtitle = "Press and hold stationary on the page OR press the S Pen barrel side button to activate immediate precision erasing (including Study Tape).",
            checked = holdToErase,
            onCheckedChange = onToggleHoldToErase,
            testTag = "settings_hold_to_erase_switch",
          )
        }

        // Section 4: Cybersecurity & Privacy Vault
        SettingsSectionCard(
          title = "Cybersecurity, Screen Recording & Privacy Vault",
          subtitle = "Screen recording & screenshots are unlocked by default unless Privacy Shield is enabled",
          icon = Icons.Outlined.Security,
          badgeContent = {
            SecurityVaultShieldIcon(
              tint = if (privacyScreenShieldEnabled) Color(0xFF34C759) else SubToolbarActiveCyan,
              isLocked = privacyScreenShieldEnabled,
              modifier = Modifier.size(18.dp),
            )
          },
        ) {
          SettingsSwitchItem(
            title = "Privacy Screen Shield (Optional FLAG_SECURE)",
            subtitle = "Off by default so you can freely record your screen and take screenshots. Turn ON only if you want Android hardware protection against screen recording.",
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

        // Section 5: Local Device Storage (Room SQLite)
        SettingsSectionCard(
          title = "On-Device Local Storage (Room SQLite)",
          subtitle = "Instant offline persistence for all notebooks, imported PDFs, ink strokes, and study tapes",
          icon = Icons.Outlined.Storage,
        ) {
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SubToolbarPillBg)
                .border(1.dp, SubToolbarBorder, RoundedCornerShape(10.dp))
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
                  .clip(RoundedCornerShape(9.dp))
                  .background(PopoverCardSelected)
                  .border(1.dp, SubToolbarBorder, RoundedCornerShape(9.dp))
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
                  .clip(RoundedCornerShape(9.dp))
                  .background(DangerRed.copy(alpha = 0.16f))
                  .border(1.dp, DangerRed.copy(alpha = 0.45f), RoundedCornerShape(9.dp))
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
}

@Composable
private fun ConnectedStatusChip(
  label: String,
  active: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .height(28.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(if (active) SubToolbarActiveTeal else Color(0xFF23252C))
        .border(
          width = 1.dp,
          color = if (active) SubToolbarActiveCyan.copy(alpha = 0.55f) else SubToolbarBorder,
          shape = RoundedCornerShape(14.dp),
        )
        .clickable(onClick = onClick)
        .padding(horizontal = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    Box(
      modifier =
        Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(if (active) SubToolbarActiveCyan else TextSecondaryGray),
    )
    Text(
      text = label,
      color = if (active) SubToolbarActiveCyan else TextSecondaryGray,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

@Composable
private fun SettingsSectionCard(
  title: String,
  subtitle: String? = null,
  icon: ImageVector,
  badgeContent: (@Composable () -> Unit)? = null,
  content: @Composable () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .shadow(8.dp, RoundedCornerShape(14.dp))
        .clip(RoundedCornerShape(14.dp))
        .background(PopoverBg)
        .border(1.dp, SubToolbarBorder, RoundedCornerShape(14.dp))
        .padding(16.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(9.dp))
              .background(ToolbarNavy)
              .border(1.dp, SubToolbarActiveCyan.copy(alpha = 0.35f), RoundedCornerShape(9.dp)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SubToolbarActiveCyan,
            modifier = Modifier.size(18.dp),
          )
        }
        Column {
          Text(
            text = title,
            color = TextPrimaryWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
          )
          if (subtitle != null) {
            Text(
              text = subtitle,
              color = TextSecondaryGray,
              fontSize = 11.sp,
            )
          }
        }
      }
      if (badgeContent != null) {
        Box(
          modifier =
            Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(SubToolbarPillBg)
              .border(1.dp, SubToolbarBorder, RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center,
        ) {
          badgeContent()
        }
      }
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
      color = SubToolbarActiveCyan,
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
