package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotebookCoverStyle
import com.example.model.NotebookDocument
import com.example.model.PaperColorTheme
import com.example.model.PaperTemplate
import com.example.model.PrimaryTool
import com.example.model.RulerState
import com.example.model.ToolbarDockPosition
import com.example.model.WritingSubTool
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NewButtonBlue
import com.example.ui.theme.PopoverBg
import com.example.ui.theme.PopoverCardSelected
import com.example.ui.theme.PopoverDivider
import com.example.ui.theme.PopoverInnerBg
import com.example.ui.theme.SwitchBlue
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * Interactive on-screen Ruler overlay that can be dragged, rotated, and used to snap straight lines.
 */
@Composable
fun InteractiveRulerOverlay(
  rulerState: RulerState,
  onMoveRuler: (Offset) -> Unit,
  onRotateRuler: (Float) -> Unit,
  onCloseRuler: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (!rulerState.isVisible) return

  Box(
    modifier =
      modifier
        .offset {
          IntOffset(
            (rulerState.centerOffset.x - 160f).roundToInt(),
            (rulerState.centerOffset.y - 32f).roundToInt(),
          )
        }
        .rotate(rulerState.angleDegrees)
        .width(340.dp)
        .height(64.dp)
        .shadow(10.dp, RoundedCornerShape(10.dp))
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xD9242832))
        .border(1.5.dp, Color(0xFF67C8EB), RoundedCornerShape(10.dp))
        .pointerInput(Unit) {
          detectDragGestures { change, dragAmount ->
            change.consume()
            onMoveRuler(dragAmount)
          }
        }
        .testTag("interactive_ruler_overlay"),
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val step = w / 34f
      for (i in 1 until 34) {
        val x = i * step
        val tickH = if (i % 5 == 0) h * 0.28f else h * 0.15f
        drawLine(
          color = Color.White.copy(alpha = 0.75f),
          start = Offset(x, 0f),
          end = Offset(x, tickH),
          strokeWidth = if (i % 5 == 0) 2.2f else 1.2f,
        )
        drawLine(
          color = Color.White.copy(alpha = 0.45f),
          start = Offset(x, h),
          end = Offset(x, h - tickH * 0.7f),
          strokeWidth = 1.2f,
        )
      }
    }

    Row(
      modifier = Modifier.align(Alignment.Center).padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Default.RotateLeft,
        contentDescription = "Rotate Ruler Counter-Clockwise",
        tint = Color.White,
        modifier =
          Modifier
            .size(20.dp)
            .clickable { onRotateRuler(rulerState.angleDegrees - 15f) },
      )
      Text(
        text = "${rulerState.angleDegrees.roundToInt()}° RULER",
        color = Color(0xFF67C8EB),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable { onRotateRuler(0f) },
      )
      Icon(
        imageVector = Icons.Default.RotateRight,
        contentDescription = "Rotate Ruler Clockwise",
        tint = Color.White,
        modifier =
          Modifier
            .size(20.dp)
            .clickable { onRotateRuler(rulerState.angleDegrees + 15f) },
      )
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Hide Ruler",
        tint = Color.White.copy(alpha = 0.8f),
        modifier = Modifier.size(18.dp).clickable(onClick = onCloseRuler),
      )
    }
  }
}

/**
 * Docked Magnified Zoom Window at the bottom of the screen with auto-advance zone.
 */
@Composable
fun ZoomWindowPanel(
  strokeColor: Color,
  strokeWidth: Float,
  targetBoxOffset: Offset,
  onCommitZoomStroke: (List<Offset>) -> Unit,
  onAdvanceZoomBox: (Float, Float) -> Unit,
  onCloseZoomWindow: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val localPoints = remember { mutableStateListOf<Offset>() }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .height(172.dp)
        .background(Color(0xFF18191D))
        .border(1.dp, Color(0xFF3A3C42))
        .testTag("zoom_window_panel"),
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(34.dp)
          .background(Color(0xFF22242A))
          .padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "ZOOM WINDOW (2.5×)",
          color = Color(0xFF67C8EB),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "Write large below — scales neatly into the target box on page",
          color = TextSecondaryGray,
          fontSize = 11.sp,
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = "Move Zoom Box Left",
          tint = TextPrimaryWhite,
          modifier = Modifier.size(20.dp).clickable { onAdvanceZoomBox(-70f, 0f) },
        )
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = "Advance Zoom Box Right",
          tint = TextPrimaryWhite,
          modifier = Modifier.size(20.dp).clickable { onAdvanceZoomBox(70f, 0f) },
        )
        Text(
          text = "Next Line ↵",
          color = SwitchBlue,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.clickable { onAdvanceZoomBox(-140f, 42f) },
        )
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close Zoom Window",
          tint = TextSecondaryGray,
          modifier = Modifier.size(18.dp).clickable(onClick = onCloseZoomWindow),
        )
      }
    }

    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(8.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color.White)
          .pointerInput(strokeColor, strokeWidth, targetBoxOffset) {
            detectDragGestures(
              onDragStart = { pt ->
                localPoints.clear()
                localPoints.add(pt)
              },
              onDrag = { change, _ ->
                change.consume()
                localPoints.add(change.position)
              },
              onDragEnd = {
                if (localPoints.size >= 2) {
                  val enteredAutoAdvance = localPoints.any { it.x > size.width * 0.76f }
                  val scaledPoints =
                    localPoints.map { pt ->
                      Offset(
                        x = targetBoxOffset.x + pt.x * 0.36f,
                        y = targetBoxOffset.y + pt.y * 0.36f,
                      )
                    }
                  onCommitZoomStroke(scaledPoints)
                  if (enteredAutoAdvance) {
                    onAdvanceZoomBox(68f, 0f)
                  }
                }
                localPoints.clear()
              },
            )
          },
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawLine(
          color = Color(0xFFD6E4F0),
          start = Offset(0f, h * 0.5f),
          end = Offset(w, h * 0.5f),
          strokeWidth = 1.5f,
        )
        drawLine(
          color = Color(0xFFBFD4E8),
          start = Offset(0f, h * 0.78f),
          end = Offset(w, h * 0.78f),
          strokeWidth = 2f,
        )

        drawRect(
          color = Color(0x181E88E5),
          topLeft = Offset(w * 0.76f, 0f),
          size = androidx.compose.ui.geometry.Size(w * 0.24f, h),
        )
        drawLine(
          color = Color(0xFF1E88E5),
          start = Offset(w * 0.76f, 0f),
          end = Offset(w * 0.76f, h),
          strokeWidth = 2f,
        )

        if (localPoints.size >= 2) {
          val path =
            Path().apply {
              moveTo(localPoints.first().x, localPoints.first().y)
              for (i in 1 until localPoints.size) {
                lineTo(localPoints[i].x, localPoints[i].y)
              }
            }
          drawPath(
            path = path,
            color = strokeColor,
            style =
              Stroke(
                width = strokeWidth * 1.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
              ),
          )
        }
      }

      Text(
        text = "AUTO-ADVANCE ZONE →",
        color = Color(0xFF1E88E5),
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
      )
    }
  }
}



/**
 * Time Keeper (Countdown Study Timer & Stopwatch) Popover inside the toolbar.
 */
@Composable
fun TimeKeeperPopover(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var isStopwatchMode by remember { mutableStateOf(false) }
  var isRunning by remember { mutableStateOf(false) }
  var secondsValue by remember { mutableIntStateOf(25 * 60) }

  LaunchedEffect(isRunning, isStopwatchMode) {
    while (isRunning) {
      delay(1000L)
      if (isStopwatchMode) {
        secondsValue += 1
      } else {
        if (secondsValue > 0) secondsValue -= 1 else isRunning = false
      }
    }
  }

  Column(
    modifier =
      modifier
        .width(264.dp)
        .shadow(16.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("time_keeper_popover"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Time Keeper",
        color = TextPrimaryWhite,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close Timer",
        tint = TextSecondaryGray,
        modifier = Modifier.size(16.dp).clickable(onClick = onDismiss),
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(32.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(PopoverInnerBg)
          .padding(2.dp),
    ) {
      Box(
        modifier =
          Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(if (!isStopwatchMode) PopoverCardSelected else Color.Transparent)
            .clickable {
              isStopwatchMode = false
              isRunning = false
              secondsValue = 25 * 60
            },
        contentAlignment = Alignment.Center,
      ) {
        Text("Timer", color = TextPrimaryWhite, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
      }
      Box(
        modifier =
          Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isStopwatchMode) PopoverCardSelected else Color.Transparent)
            .clickable {
              isStopwatchMode = true
              isRunning = false
              secondsValue = 0
            },
        contentAlignment = Alignment.Center,
      ) {
        Text("Stopwatch", color = TextPrimaryWhite, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    val mins = secondsValue / 60
    val secs = secondsValue % 60
    Text(
      text = "%02d:%02d".format(mins, secs),
      color = TextPrimaryWhite,
      fontSize = 34.sp,
      fontWeight = FontWeight.Bold,
    )

    if (!isStopwatchMode) {
      Spacer(modifier = Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(5, 15, 25, 45).forEach { presetMin ->
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(PopoverCardSelected)
                .clickable {
                  isRunning = false
                  secondsValue = presetMin * 60
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
          ) {
            Text("${presetMin}m", color = TextSecondaryGray, fontSize = 11.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier =
          Modifier
            .weight(1f)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isRunning) DangerRed else SwitchBlue)
            .clickable { isRunning = !isRunning },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (isRunning) "Pause" else "Start",
          color = Color.White,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }

      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PopoverCardSelected)
            .clickable {
              isRunning = false
              secondsValue = if (isStopwatchMode) 0 else 25 * 60
            },
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Reset",
          tint = TextPrimaryWhite,
          modifier = Modifier.size(17.dp),
        )
      }
    }
  }
}

/**
 * 1:1 Official Goodnotes 6 "Toolbar Customization" & "Writing Tools Customization" Popover
 * matching IMG_0531_c1a72747.png.
 */
@Composable
fun ToolbarCustomizationPopover(
  enabledTools: Set<PrimaryTool>,
  enabledWritingSubTools: Set<WritingSubTool>,
  toolbarDockPosition: ToolbarDockPosition = ToolbarDockPosition.TOP,
  onSelectToolbarDockPosition: (ToolbarDockPosition) -> Unit = {},
  onResetSubToolbarPosition: () -> Unit = {},
  onToggleToolEnabled: (PrimaryTool) -> Unit,
  onToggleWritingSubToolEnabled: (WritingSubTool) -> Unit,
  onResetDefaults: () -> Unit,
  onResetWritingDefaults: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var isWritingToolsSubScreen by remember { mutableStateOf(false) }
  var selectedTab by remember { mutableIntStateOf(0) } // 0 = Tools, 1 = Accessories

  val sheetBg = Color(0xFFF2F2F7)
  val cardWhite = Color(0xFFFFFFFF)
  val textDark = Color(0xFF1C1C1E)
  val textGray = Color(0xFF6C6C70)
  val iosBlue = Color(0xFF007AFF)
  val iosRed = Color(0xFFFF3B30)
  val dividerColor = Color(0xFFE5E5EA)

  Column(
    modifier =
      modifier
        .width(340.dp)
        .shadow(22.dp, RoundedCornerShape(18.dp))
        .clip(RoundedCornerShape(18.dp))
        .background(sheetBg)
        .padding(horizontal = 16.dp, vertical = 14.dp)
        .testTag("toolbar_customize_popover"),
  ) {
    // Top Navigation Bar: '<' Back | Title | 'Done'
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
        contentDescription = "Back",
        tint = iosBlue,
        modifier =
          Modifier
            .size(24.dp)
            .clickable {
              if (isWritingToolsSubScreen) isWritingToolsSubScreen = false else onDismiss()
            },
      )
      Text(
        text =
          if (isWritingToolsSubScreen) "Writing Tools Customization"
          else "Toolbar Customization",
        color = textDark,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = "Done",
        color = iosBlue,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable(onClick = onDismiss),
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (!isWritingToolsSubScreen) {
      // Segmented Control: Tools | Accessories (1:1 from IMG_0531 Left Panel)
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE3E3E8))
            .padding(2.dp),
      ) {
        Box(
          modifier =
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .clip(RoundedCornerShape(6.dp))
              .background(if (selectedTab == 0) cardWhite else Color.Transparent)
              .clickable { selectedTab = 0 },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Tools",
            color = textDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
        Box(
          modifier =
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .clip(RoundedCornerShape(6.dp))
              .background(if (selectedTab == 1) cardWhite else Color.Transparent)
              .clickable { selectedTab = 1 },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Accessories",
            color = textDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "VISIBLE IN TOOLBAR",
        color = textGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
      )

      val customizableMainTools = PrimaryTool.entries

      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardWhite),
      ) {
        customizableMainTools.forEachIndexed { idx, tool ->
          val isVisible = enabledTools.contains(tool)
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable {
                  if (tool == PrimaryTool.WRITING_TOOLS) {
                    isWritingToolsSubScreen = true
                  } else {
                    onToggleToolEnabled(tool)
                  }
                }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // Red Minus Circle (or Green Plus when hidden)
            Box(
              modifier =
                Modifier
                  .size(20.dp)
                  .clip(CircleShape)
                  .background(if (isVisible) iosRed else Color(0xFF34C759))
                  .clickable { onToggleToolEnabled(tool) },
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = if (isVisible) Icons.Default.Remove else Icons.Default.Add,
                contentDescription = "Toggle tool visibility",
                tint = Color.White,
                modifier = Modifier.size(14.dp),
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Official Goodnotes 6 Tool Icon
            PrimaryToolCustomIcon(
              tool = tool,
              tint = textDark,
              modifier = Modifier.size(21.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
              text = tool.label,
              color = if (isVisible) textDark else textGray,
              fontSize = 14.sp,
              modifier = Modifier.weight(1f),
            )

            if (tool == PrimaryTool.WRITING_TOOLS) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                  Modifier
                    .clickable { isWritingToolsSubScreen = true }
                    .padding(end = 8.dp)
                    .testTag("customize_writing_tools_link"),
              ) {
                Text(
                  text = "Customize",
                  color = iosBlue,
                  fontSize = 13.sp,
                )
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                  contentDescription = null,
                  tint = iosBlue,
                  modifier = Modifier.size(16.dp),
                )
              }
            }

            // 3-bar drag handle icon
            Icon(
              imageVector = Icons.Default.Menu,
              contentDescription = "Reorder",
              tint = Color(0xFFC7C7CC),
              modifier = Modifier.size(18.dp),
            )
          }

          if (idx < customizableMainTools.lastIndex) {
            HorizontalDivider(
              color = dividerColor,
              thickness = 0.6.dp,
              modifier = Modifier.padding(start = 44.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "At least one tool must be visible in the docked toolbar.",
        color = textGray,
        fontSize = 10.5.sp,
        modifier = Modifier.padding(horizontal = 8.dp),
      )

      Spacer(modifier = Modifier.height(10.dp))

      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cardWhite)
            .clickable(onClick = onResetDefaults)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Text(
          text = "Reset to Default Toolbar",
          color = iosRed,
          fontSize = 14.sp,
          fontWeight = FontWeight.Normal,
        )
      }
    } else {
      // Right Panel from IMG_0531: "Writing Tools Customization"
      Text(
        text = "VISIBLE IN WRITING TOOLS",
        color = textGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
      )

      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardWhite),
      ) {
        WritingSubTool.entries.forEachIndexed { idx, subTool ->
          val isVisible = enabledWritingSubTools.contains(subTool)
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clickable { onToggleWritingSubToolEnabled(subTool) }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier =
                Modifier
                  .size(20.dp)
                  .clip(CircleShape)
                  .background(if (isVisible) iosRed else Color(0xFF34C759))
                  .clickable { onToggleWritingSubToolEnabled(subTool) },
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = if (isVisible) Icons.Default.Remove else Icons.Default.Add,
                contentDescription = "Toggle writing tool visibility",
                tint = Color.White,
                modifier = Modifier.size(14.dp),
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Official Goodnotes 6 Writing Sub-Tool Icon (Pen, Pencil, Highlighter, Tape, Draw Shape)
            WritingSubToolCustomIcon(
              subTool = subTool,
              tint = textDark,
              modifier = Modifier.size(21.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
              text = subTool.label,
              color = if (isVisible) textDark else textGray,
              fontSize = 14.sp,
              modifier = Modifier.weight(1f),
            )

            Icon(
              imageVector = Icons.Default.Menu,
              contentDescription = "Reorder",
              tint = Color(0xFFC7C7CC),
              modifier = Modifier.size(18.dp),
            )
          }

          if (idx < WritingSubTool.entries.lastIndex) {
            HorizontalDivider(
              color = dividerColor,
              thickness = 0.6.dp,
              modifier = Modifier.padding(start = 44.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "At least one tool must be visible in writing tools.",
        color = textGray,
        fontSize = 11.sp,
        modifier = Modifier.padding(horizontal = 8.dp),
      )

      Spacer(modifier = Modifier.height(14.dp))

      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cardWhite)
            .clickable(onClick = onResetWritingDefaults)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Text(
          text = "Reset to Default Writing Tools Layout",
          color = iosRed,
          fontSize = 14.sp,
          fontWeight = FontWeight.Normal,
        )
      }
    }
  }
}



/**
 * Create New Notebook / Import PDF Modal Dialog.
 */
@Composable
fun CreateNotebookDialog(
  onCreateNotebook: (String, NotebookCoverStyle, PaperTemplate) -> Unit,
  onPickPdfFromDevice: () -> Unit,
  onLoadSamplePdf: () -> Unit,
  onDismiss: () -> Unit,
) {
  var title by remember { mutableStateOf("Study Notebook") }
  var selectedCover by remember { mutableStateOf(NotebookCoverStyle.KRAFT_TAN) }
  var selectedTemplate by remember { mutableStateOf(PaperTemplate.RULED) }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.62f))
        .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .width(380.dp)
          .shadow(24.dp, RoundedCornerShape(18.dp))
          .clip(RoundedCornerShape(18.dp))
          .background(PopoverBg)
          .border(1.dp, PopoverDivider, RoundedCornerShape(18.dp))
          .clickable(enabled = false) {}
          .padding(20.dp)
          .testTag("create_notebook_dialog"),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "New Document & Import PDF",
          color = TextPrimaryWhite,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
        )
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close",
          tint = TextSecondaryGray,
          modifier = Modifier.size(20.dp).clickable(onClick = onDismiss),
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier =
            Modifier
              .weight(1f)
              .height(40.dp)
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
            color = TextPrimaryWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }

        Row(
          modifier =
            Modifier
              .weight(1f)
              .height(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(PopoverCardSelected)
              .clickable(onClick = onLoadSamplePdf)
              .padding(horizontal = 10.dp)
              .testTag("dialog_sample_pdf_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Text(
            text = "Sample Study PDF",
            color = TextPrimaryWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
          )
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = PopoverDivider)

      Text(
        text = "NOTEBOOK TITLE",
        color = TextSecondaryGray,
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
            focusedTextColor = TextPrimaryWhite,
            unfocusedTextColor = TextPrimaryWhite,
            focusedBorderColor = SwitchBlue,
            unfocusedBorderColor = PopoverDivider,
            focusedContainerColor = PopoverInnerBg,
            unfocusedContainerColor = PopoverInnerBg,
          ),
        modifier = Modifier.fillMaxWidth().testTag("new_notebook_title_input"),
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "COVER STYLE",
        color = TextSecondaryGray,
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
                .background(if (isSelected) SwitchBlue else PopoverCardSelected)
                .clickable { selectedCover = cover }
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = cover.label.substringBefore(" "),
              color = TextPrimaryWhite,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "PAPER TEMPLATE",
        color = TextSecondaryGray,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        PaperTemplate.entries.forEach { tmpl ->
          val isSelected = tmpl == selectedTemplate
          Box(
            modifier =
              Modifier
                .weight(1f)
                .height(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) SwitchBlue else PopoverCardSelected)
                .clickable { selectedTemplate = tmpl }
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = tmpl.label.substringBefore(" "),
              color = TextPrimaryWhite,
              fontSize = 10.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
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
              val cleanTitle = title.trim().ifEmpty { "Untitled Notebook" }
              onCreateNotebook(cleanTitle, selectedCover, selectedTemplate)
            }
            .testTag("confirm_create_notebook_button"),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "Create Notebook",
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

/**
 * Export & Share Popover:
 * - Export Multi-Page PDF Document with all ink strokes, shapes, text boxes, sticky notes, and PDF backgrounds
 * - Export Current Page as high-resolution PNG image
 * - Toggle Confidential SHA-256 Integrity Watermark on exported PDFs
 * - Select Goodnotes Paper Color Tint (Pure White, Goodnotes Cream, Night Dark, Sage Eye-Care)
 */
@Composable
fun ExportAndSharePopover(
  document: NotebookDocument?,
  watermarkConfidentialExports: Boolean,
  documentSha256Hash: String,
  onExportFullPdf: () -> Unit,
  onExportCurrentPagePng: () -> Unit,
  onToggleWatermark: (Boolean) -> Unit,
  onCyclePaperColorTheme: () -> Unit,
  onOpenSecurityVault: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .width(312.dp)
        .shadow(18.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("export_share_popover"),
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
        ExportShareBoxIcon(tint = Color(0xFF67C8EB), modifier = Modifier.size(18.dp))
        Text(
          text = "Export & Share Document",
          color = TextPrimaryWhite,
          fontSize = 14.5.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close",
        tint = TextSecondaryGray,
        modifier = Modifier.size(17.dp).clickable(onClick = onDismiss),
      )
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text =
        "Export \"${document?.title ?: "Notebook"}\" (${document?.pages?.size ?: 1} pages) with all vector ink, shapes, and annotations.",
      color = TextSecondaryGray,
      fontSize = 11.sp,
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Export Multi-Page PDF Button
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(SwitchBlue)
          .clickable {
            onDismiss()
            onExportFullPdf()
          }
          .padding(horizontal = 12.dp)
          .testTag("export_pdf_button"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Outlined.PictureAsPdf,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = "Export Full PDF Document (.pdf)",
          color = Color.White,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold,
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(16.dp),
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Export Current Page PNG Image Button
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(PopoverCardSelected)
          .clickable {
            onDismiss()
            onExportCurrentPagePng()
          }
          .padding(horizontal = 12.dp)
          .testTag("export_png_button"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = "Export Current Page Image (.png)",
        color = TextPrimaryWhite,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
      )
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = TextSecondaryGray,
        modifier = Modifier.size(16.dp),
      )
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = PopoverDivider)

    // Paper Color Tint QoL
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(PopoverInnerBg)
          .clickable(onClick = onCyclePaperColorTheme)
          .padding(horizontal = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Paper Tint: ${document?.paperColorTheme?.label ?: PaperColorTheme.PURE_WHITE.label}",
        color = TextPrimaryWhite,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Medium,
      )
      Text(
        text = "Cycle Tint →",
        color = Color(0xFF67C8EB),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Confidential Watermark Toggle
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(PopoverInnerBg)
          .clickable { onToggleWatermark(!watermarkConfidentialExports) }
          .padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Confidential SHA-256 Watermark",
          color = TextPrimaryWhite,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Medium,
        )
        Text(
          text = "Hash: ${documentSha256Hash.take(12)}...",
          color = TextSecondaryGray,
          fontSize = 9.5.sp,
        )
      }
      Text(
        text = if (watermarkConfidentialExports) "ON" else "OFF",
        color = if (watermarkConfidentialExports) Color(0xFF34C759) else TextSecondaryGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF17325A))
          .border(1.dp, SwitchBlue.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
          .clickable(onClick = onOpenSecurityVault)
          .padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      SecurityVaultShieldIcon(
        tint = Color(0xFF67C8EB),
        isLocked = document?.isLockedVault == true,
        modifier = Modifier.size(15.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Open Cybersecurity Vault & PIN Lock...",
        color = Color(0xFF67C8EB),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

/**
 * Cybersecurity Vault & Document Protection Popover:
 * - Lock / Unlock current notebook with a SHA-256 hashed PIN
 * - Toggle FLAG_SECURE Privacy Screen Shield (blocks screenshots, screen recording & recents)
 * - Live SHA-256 Document Integrity Fingerprint verification
 */
@Composable
fun SecurityVaultPopover(
  document: NotebookDocument?,
  privacyScreenShieldEnabled: Boolean,
  watermarkConfidentialExports: Boolean,
  documentSha256Hash: String,
  onSetVaultPin: (String) -> Unit,
  onRemoveVaultLock: () -> Unit,
  onTogglePrivacyScreenShield: (Boolean) -> Unit,
  onToggleWatermark: (Boolean) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var pinInput by remember { mutableStateOf("") }
  val isLocked = document?.isLockedVault == true

  Column(
    modifier =
      modifier
        .width(320.dp)
        .shadow(20.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("security_vault_popover"),
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
        SecurityVaultShieldIcon(
          tint = if (isLocked) Color(0xFF34C759) else Color(0xFF67C8EB),
          isLocked = isLocked,
          modifier = Modifier.size(19.dp),
        )
        Text(
          text = "Cybersecurity Vault Shield",
          color = TextPrimaryWhite,
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold,
        )
      }
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close Vault",
        tint = TextSecondaryGray,
        modifier = Modifier.size(17.dp).clickable(onClick = onDismiss),
      )
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Zero-trust local protection: SHA-256 PIN Vault, FLAG_SECURE screenshot blocking, and cryptographic integrity.",
      color = TextSecondaryGray,
      fontSize = 10.5.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    // SHA-256 Integrity Fingerprint Box
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(PopoverInnerBg)
          .padding(10.dp),
    ) {
      Text(
        text = "SHA-256 DOCUMENT INTEGRITY FINGERPRINT",
        color = Color(0xFF67C8EB),
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = documentSha256Hash,
        color = TextPrimaryWhite,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Notebook PIN Vault Section
    if (isLocked) {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DangerRed.copy(alpha = 0.18f))
            .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable(onClick = onRemoveVaultLock)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Text(
          text = "Remove Vault PIN Lock from Notebook",
          color = DangerRed,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }
    } else {
      Text(
        text = "SET 4-DIGIT VAULT PIN FOR THIS NOTEBOOK",
        color = TextSecondaryGray,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedTextField(
          value = pinInput,
          onValueChange = { raw ->
            pinInput = raw.filter { it.isDigit() }.take(8)
          },
          placeholder = { Text("Enter 4+ digit PIN", color = TextSecondaryGray, fontSize = 11.sp) },
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
          modifier = Modifier.weight(1f).height(48.dp).testTag("vault_pin_input"),
        )
        Box(
          modifier =
            Modifier
              .height(44.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(SwitchBlue)
              .clickable {
                onSetVaultPin(pinInput)
                pinInput = ""
              }
              .padding(horizontal = 12.dp)
              .testTag("lock_notebook_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Lock Vault",
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = PopoverDivider)

    // Privacy Screen Shield (FLAG_SECURE)
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(PopoverInnerBg)
          .clickable { onTogglePrivacyScreenShield(!privacyScreenShieldEnabled) }
          .padding(horizontal = 10.dp, vertical = 8.dp)
          .testTag("privacy_shield_toggle"),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Privacy Screen Shield (FLAG_SECURE)",
          color = TextPrimaryWhite,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = "Blocks screenshots, screen recording & app-switcher previews",
          color = TextSecondaryGray,
          fontSize = 10.sp,
        )
      }
      Text(
        text = if (privacyScreenShieldEnabled) "ACTIVE" else "OFF",
        color = if (privacyScreenShieldEnabled) Color(0xFF34C759) else TextSecondaryGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(PopoverInnerBg)
          .clickable { onToggleWatermark(!watermarkConfidentialExports) }
          .padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Confidential SHA-256 PDF Watermark",
        color = TextPrimaryWhite,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Medium,
      )
      Text(
        text = if (watermarkConfidentialExports) "ON" else "OFF",
        color = if (watermarkConfidentialExports) Color(0xFF34C759) else TextSecondaryGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

/**
 * Modal Dialog to Unlock a SHA-256 PIN-Protected Vault Notebook from Documents Screen.
 */
@Composable
fun UnlockVaultDialog(
  notebookTitle: String,
  onAttemptUnlock: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var pin by remember { mutableStateOf("") }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.68f))
        .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .width(340.dp)
          .shadow(24.dp, RoundedCornerShape(18.dp))
          .clip(RoundedCornerShape(18.dp))
          .background(PopoverBg)
          .border(1.dp, SwitchBlue.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
          .clickable(enabled = false) {}
          .padding(20.dp)
          .testTag("unlock_vault_dialog"),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      SecurityVaultShieldIcon(
        tint = Color(0xFF67C8EB),
        isLocked = true,
        modifier = Modifier.size(34.dp),
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Vault-Locked Notebook",
        color = TextPrimaryWhite,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Enter your Vault PIN to decrypt and open \"$notebookTitle\"",
        color = TextSecondaryGray,
        fontSize = 12.sp,
      )
      Spacer(modifier = Modifier.height(14.dp))
      OutlinedTextField(
        value = pin,
        onValueChange = { raw -> pin = raw.filter { it.isDigit() }.take(8) },
        placeholder = { Text("Enter Vault PIN", color = TextSecondaryGray, fontSize = 12.sp) },
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
        modifier = Modifier.fillMaxWidth().testTag("unlock_vault_pin_field"),
      )
      Spacer(modifier = Modifier.height(14.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(PopoverCardSelected)
              .clickable(onClick = onDismiss),
          contentAlignment = Alignment.Center,
        ) {
          Text("Cancel", color = TextPrimaryWhite, fontSize = 13.sp)
        }
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(SwitchBlue)
              .clickable { onAttemptUnlock(pin) }
              .testTag("confirm_unlock_vault_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text("Unlock Vault", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun AudioRecordingTimelineBar(
  isRecording: Boolean,
  isPlayingReplay: Boolean,
  recordedSeconds: Int,
  strokeCount: Int,
  onToggleRecord: () -> Unit,
  onToggleReplay: () -> Unit,
  onCloseAudioBar: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val minutes = recordedSeconds / 60
  val seconds = recordedSeconds % 60
  val formattedTime = String.format("%02d:%02d", minutes, seconds)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(44.dp)
        .background(PopoverInnerBg)
        .padding(horizontal = 16.dp),
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
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isRecording) DangerRed else PopoverCardSelected)
            .clickable(onClick = onToggleRecord)
            .testTag("toggle_audio_record_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = if (isRecording) Icons.Filled.Pause else Icons.Filled.FiberManualRecord,
          contentDescription = if (isRecording) "Pause Recording" else "Record Audio",
          tint = if (isRecording) Color.White else DangerRed,
          modifier = Modifier.size(16.dp),
        )
      }
      Text(
        text = formattedTime,
        color = TextPrimaryWhite,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
      )
      if (strokeCount > 0) {
        Text(
          text = "• $strokeCount strokes sync'd",
          color = TextSecondaryGray,
          fontSize = 12.sp,
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier =
          Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(PopoverCardSelected)
            .clickable(onClick = onToggleReplay)
            .testTag("toggle_audio_replay_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = if (isPlayingReplay) Icons.Filled.Pause else Icons.Filled.PlayArrow,
          contentDescription = if (isPlayingReplay) "Pause Replay" else "Replay Audio",
          tint = TextPrimaryWhite,
          modifier = Modifier.size(16.dp),
        )
      }
      Box(
        modifier =
          Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(PopoverCardSelected)
            .clickable(onClick = onCloseAudioBar)
            .testTag("close_audio_bar_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = "Close Audio Bar",
          tint = TextSecondaryGray,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
fun AiHandwritingAssistPopover(
  strokeCount: Int,
  onInsertConvertedTextBox: (String) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .width(320.dp)
        .shadow(16.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("ai_handwriting_assist_popover")
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = SwitchBlue,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = "AI Handwriting Assist",
            color = TextPrimaryWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
        Box(
          modifier =
            Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(PopoverCardSelected)
              .clickable(onClick = onDismiss),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Dismiss",
            tint = TextSecondaryGray,
            modifier = Modifier.size(16.dp),
          )
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = if (strokeCount > 0) "$strokeCount strokes ready for conversion or cleanup." else "No handwritten strokes detected on this page.",
        color = TextSecondaryGray,
        fontSize = 12.sp,
      )
      Spacer(modifier = Modifier.height(16.dp))
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (strokeCount > 0) SwitchBlue else PopoverCardSelected)
            .clickable(enabled = strokeCount > 0) {
              onInsertConvertedTextBox("Converted Handwriting")
              onDismiss()
            }
            .testTag("convert_handwriting_button"),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "Convert Handwriting to Text",
          color = if (strokeCount > 0) Color.White else TextSecondaryGray,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
        )
      }
    }
  }
}

