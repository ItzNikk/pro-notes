package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FlipToBack
import androidx.compose.material.icons.outlined.FlipToFront
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PhotoSizeSelectSmall
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ElementStampType
import com.example.model.EraserMode
import com.example.model.LassoFilterSettings
import com.example.model.PenStyle
import com.example.model.ShapeKind
import com.example.model.StrokePattern
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PopoverBg
import com.example.ui.theme.PopoverCardSelected
import com.example.ui.theme.PopoverDivider
import com.example.ui.theme.PopoverInnerBg
import com.example.ui.theme.SwitchBlue
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@Composable
fun PenSettingsPopover(
  selectedPenStyle: PenStyle,
  strokePattern: StrokePattern,
  strokeStabilization: Float,
  drawAndHold: Boolean,
  holdToErase: Boolean,
  scribbleToErase: Boolean,
  circleToLasso: Boolean,
  strokeWidth: Float,
  onSelectPenStyle: (PenStyle) -> Unit,
  onSelectStrokePattern: (StrokePattern) -> Unit,
  onStabilizationChange: (Float) -> Unit,
  onToggleDrawAndHold: (Boolean) -> Unit,
  onToggleHoldToErase: (Boolean) -> Unit,
  onToggleScribbleToErase: (Boolean) -> Unit,
  onToggleCircleToLasso: (Boolean) -> Unit,
  onOpenAiAssist: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .width(308.dp)
        .shadow(18.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("pen_settings_popover"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = selectedPenStyle.title,
        color = TextPrimaryWhite,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = selectedPenStyle.subtitle,
        color = TextSecondaryGray,
        fontSize = 10.sp,
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Live S-Curve Sinusoidal Stroke Preview Box (1:1 from Screenshot 3)
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(78.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(PopoverInnerBg),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 14.dp)) {
        val w = size.width
        val h = size.height
        val previewStroke =
          when (selectedPenStyle) {
            PenStyle.FOUNTAIN -> strokeWidth * 1.15f
            PenStyle.BALL -> strokeWidth * 1.25f
            PenStyle.BRUSH -> strokeWidth * 1.65f
          }
        val path =
          Path().apply {
            moveTo(w * 0.08f, h * 0.42f)
            cubicTo(w * 0.25f, h * 1.15f, w * 0.42f, h * 0.95f, w * 0.54f, h * 0.45f)
            cubicTo(w * 0.66f, -h * 0.08f, w * 0.84f, -h * 0.05f, w * 0.94f, h * 0.55f)
          }
        val dashEffect =
          when (strokePattern) {
            StrokePattern.SOLID -> null
            StrokePattern.DASHED -> PathEffect.dashPathEffect(floatArrayOf(22f, 14f), 0f)
            StrokePattern.DOTTED -> PathEffect.dashPathEffect(floatArrayOf(6f, 14f), 0f)
          }
        drawPath(
          path = path,
          color = Color.White,
          style =
            Stroke(
              width = previewStroke.coerceIn(4f, 18f),
              cap = StrokeCap.Round,
              join = StrokeJoin.Round,
              pathEffect = dashEffect,
            ),
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 3 Pen Style Selector Cards: Fountain Pen | Ball Pen | Brush Pen
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      PenStyle.entries.forEach { style ->
        val isSelected = style == selectedPenStyle
        Column(
          modifier =
            Modifier
              .weight(1f)
              .height(56.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) PopoverCardSelected else Color.Transparent)
              .clickable { onSelectPenStyle(style) }
              .padding(vertical = 6.dp)
              .testTag("pen_style_${style.name.lowercase()}"),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          PenStyleNibIcon(
            style = style,
            isSelected = isSelected,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = style.title,
            color = if (isSelected) TextPrimaryWhite else TextSecondaryGray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Stroke Styles: Solid | Dashed | Dotted
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      StrokePattern.entries.forEach { pattern ->
        val isSelected = pattern == strokePattern
        Row(
          modifier =
            Modifier
              .weight(1f)
              .height(28.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) PopoverCardSelected else PopoverInnerBg)
              .clickable { onSelectStrokePattern(pattern) }
              .padding(horizontal = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Text(
            text = pattern.label,
            color = if (isSelected) TextPrimaryWhite else TextSecondaryGray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "STROKE STABILIZATION",
        color = TextSecondaryGray,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
      )
      Text(
        text = "${strokeStabilization.toInt()}%",
        color = TextSecondaryGray,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
      )
    }

    Slider(
      value = strokeStabilization,
      onValueChange = onStabilizationChange,
      valueRange = 0f..100f,
      colors =
        SliderDefaults.colors(
          thumbColor = Color.White,
          activeTrackColor = SwitchBlue,
          inactiveTrackColor = Color(0xFF38383C),
        ),
      modifier = Modifier.fillMaxWidth().height(28.dp).testTag("stabilization_slider"),
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "SETTINGS & PEN GESTURES",
      color = TextSecondaryGray,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 0.5.sp,
    )

    Spacer(modifier = Modifier.height(2.dp))

    PopoverToggleRow(
      label = "Draw and Hold (Auto-Snap Shape)",
      checked = drawAndHold,
      onCheckedChange = onToggleDrawAndHold,
      testTag = "draw_and_hold_switch",
    )

    HorizontalDivider(color = PopoverDivider, thickness = 0.5.dp)

    PopoverToggleRow(
      label = "Circle to Lasso (Circle → Lift → Hold)",
      checked = circleToLasso,
      onCheckedChange = onToggleCircleToLasso,
      testTag = "circle_to_lasso_switch",
    )

    HorizontalDivider(color = PopoverDivider, thickness = 0.5.dp)

    PopoverToggleRow(
      label = "Scribble to Erase",
      checked = scribbleToErase,
      onCheckedChange = onToggleScribbleToErase,
      testTag = "scribble_to_erase_switch",
    )

    HorizontalDivider(color = PopoverDivider, thickness = 0.5.dp)

    PopoverToggleRow(
      label = "Hold in Place / S Pen Button Erase",
      checked = holdToErase,
      onCheckedChange = onToggleHoldToErase,
      testTag = "hold_to_erase_switch",
    )

    HorizontalDivider(color = PopoverDivider, thickness = 0.5.dp)

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(34.dp)
          .clickable(onClick = onOpenAiAssist),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Pen Gestures & AI Assist",
        color = TextPrimaryWhite,
        fontSize = 13.sp,
      )
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = TextSecondaryGray,
        modifier = Modifier.size(18.dp),
      )
    }

    HorizontalDivider(color = PopoverDivider, thickness = 0.5.dp)

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(32.dp)
          .clickable(onClick = onDismiss)
          .testTag("disconnect_stylus_row"),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Disconnect Stylus",
        color = DangerRed,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
      )
    }
  }
}

/**
 * Dedicated Goodnotes 6 Highlighter Settings Popover:
 * - Shows live preview of highlighter ink layering underneath dark text using Multiply/Darken blend
 * - Draw in Straight Line toggle
 * - Draw and Hold straight-line snap toggle
 */
@Composable
fun HighlighterSettingsPopover(
  selectedColor: Color,
  highlighterStraightLine: Boolean,
  drawAndHold: Boolean,
  selectedThicknessIndex: Int,
  onToggleStraightLine: () -> Unit,
  onToggleDrawAndHold: (Boolean) -> Unit,
  onSelectThicknessIndex: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .width(288.dp)
        .shadow(18.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("highlighter_settings_popover"),
  ) {
    Text(
      text = "Highlighter",
      color = TextPrimaryWhite,
      fontSize = 15.sp,
      fontWeight = FontWeight.SemiBold,
    )
    Text(
      text = "Renders behind ink using Multiply blend so dark text never washes out.",
      color = TextSecondaryGray,
      fontSize = 10.5.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Live Preview on white paper showing crisp dark text with highlighter layered behind it
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(58.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color.White),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        val w = size.width
        val h = size.height
        val barWidth =
          when (selectedThicknessIndex) {
            0 -> 18f
            1 -> 28f
            else -> 38f
          }
        drawLine(
          color = selectedColor.copy(alpha = 0.42f),
          start = Offset(w * 0.08f, h * 0.52f),
          end = Offset(w * 0.92f, h * 0.52f),
          strokeWidth = barWidth,
          cap = StrokeCap.Round,
          blendMode = BlendMode.Darken,
        )
      }
      Text(
        text = "APC = Total Consumption / Income",
        color = Color(0xFF141518),
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Chisel Thickness Selector
    val thicknessLabels = listOf("Fine", "Medium", "Broad")
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      thicknessLabels.forEachIndexed { idx, label ->
        val isSelected = selectedThicknessIndex == idx
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) SwitchBlue else PopoverCardSelected)
              .clickable { onSelectThicknessIndex(idx) },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = label,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(color = PopoverDivider, thickness = 0.5.dp)

    PopoverToggleRow(
      label = "Draw in Straight Line",
      checked = highlighterStraightLine,
      onCheckedChange = { onToggleStraightLine() },
      testTag = "highlighter_straight_line_switch",
    )

    HorizontalDivider(color = PopoverDivider, thickness = 0.5.dp)

    PopoverToggleRow(
      label = "Draw & Hold to Snap Line",
      checked = drawAndHold,
      onCheckedChange = onToggleDrawAndHold,
      testTag = "highlighter_draw_hold_switch",
    )
  }
}

@Composable
private fun PopoverToggleRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth().height(34.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      color = TextPrimaryWhite,
      fontSize = 12.5.sp,
    )
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
private fun PenStyleNibIcon(
  style: PenStyle,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeColor = if (isSelected) Color(0xFF3CA0F0) else Color(0xFFD0D0D5)
    when (style) {
      PenStyle.FOUNTAIN -> {
        val path =
          Path().apply {
            moveTo(w * 0.25f, h * 0.78f)
            lineTo(w * 0.45f, h * 0.22f)
            lineTo(w * 0.78f, h * 0.55f)
            close()
          }
        drawPath(path = path, color = strokeColor, style = Stroke(width = 3f))
        drawLine(
          color = strokeColor,
          start = Offset(w * 0.25f, h * 0.78f),
          end = Offset(w * 0.52f, h * 0.51f),
          strokeWidth = 2.5f,
        )
      }

      PenStyle.BALL -> {
        val bodyPath =
          Path().apply {
            moveTo(w * 0.22f, h * 0.78f)
            lineTo(w * 0.56f, h * 0.26f)
            lineTo(w * 0.76f, h * 0.46f)
            close()
          }
        drawPath(path = bodyPath, color = strokeColor)
      }

      PenStyle.BRUSH -> {
        val brushPath =
          Path().apply {
            moveTo(w * 0.20f, h * 0.80f)
            quadraticTo(w * 0.40f, h * 0.28f, w * 0.75f, h * 0.25f)
            quadraticTo(w * 0.72f, h * 0.62f, w * 0.20f, h * 0.80f)
          }
        drawPath(path = brushPath, color = strokeColor, style = Stroke(width = 3f))
      }
    }
  }
}

/**
 * Full 1:1 Goodnotes 6 Color Palette & Custom Color Studio Popover:
 * - Presets Grid & Custom 2D Spectrum View
 * - Interactive 2D Saturation-Brightness Color Square + Rainbow Hue Bar
 * - Editable #RRGGBB Hex Code Input Field
 * - Toolbar Color Slot Management (Replace Slot, + Add New Slot to Toolbar, Remove Slot)
 */
@Composable
fun PenColorPopover(
  selectedColor: Color,
  customPalette: List<Color>,
  toolbarSwatchCount: Int,
  selectedSwatchIndex: Int,
  isEyedropperActive: Boolean,
  onSelectColorReplaceSlot: (Color) -> Unit,
  onAddNewColorSlotToToolbar: (Color) -> Unit,
  onAddColorToCustomPalette: (Color) -> Unit,
  onRemoveCurrentToolbarSlot: () -> Unit,
  onToggleEyedropper: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var isCustomCreatorOpen by remember { mutableStateOf(false) }

  // Initialize HSV from selectedColor
  val initialHsv = remember(selectedColor) {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(selectedColor.toArgb(), hsv)
    hsv
  }
  var hue by remember { mutableFloatStateOf(initialHsv[0]) }
  var saturation by remember { mutableFloatStateOf(initialHsv[1].coerceIn(0.05f, 1f)) }
  var brightness by remember { mutableFloatStateOf(initialHsv[2].coerceIn(0.15f, 1f)) }

  val customPreviewColor =
    remember(hue, saturation, brightness) {
      Color.hsv(hue.coerceIn(0f, 360f), saturation.coerceIn(0f, 1f), brightness.coerceIn(0f, 1f))
    }

  var hexInputText by remember(customPreviewColor) {
    mutableStateOf(String.format("%06X", 0xFFFFFF and customPreviewColor.toArgb()))
  }

  Column(
    modifier =
      modifier
        .width(296.dp)
        .shadow(18.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("pen_color_popover"),
  ) {
    // Header: "Pen Color" | Segmented Presets vs Custom | Eyedropper
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Color (Slot #${selectedSwatchIndex + 1})",
        color = TextPrimaryWhite,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.SemiBold,
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Presets | Custom Segmented Pill
        Row(
          modifier =
            Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(PopoverInnerBg)
              .padding(2.dp),
        ) {
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (!isCustomCreatorOpen) SwitchBlue else Color.Transparent)
                .clickable { isCustomCreatorOpen = false }
                .padding(horizontal = 7.dp, vertical = 3.dp),
          ) {
            Text(
              text = "Presets",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
            )
          }
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isCustomCreatorOpen) SwitchBlue else Color.Transparent)
                .clickable { isCustomCreatorOpen = true }
                .padding(horizontal = 7.dp, vertical = 3.dp)
                .testTag("toggle_custom_color_creator"),
          ) {
            Text(
              text = "Custom",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }

        Row(
          modifier =
            Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isEyedropperActive) SwitchBlue else PopoverCardSelected)
              .clickable(onClick = onToggleEyedropper)
              .padding(horizontal = 7.dp, vertical = 4.dp)
              .testTag("eyedropper_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.Colorize,
            contentDescription = "Eyedropper Color Sampler",
            tint = Color.White,
            modifier = Modifier.size(13.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (!isCustomCreatorOpen) {
      // Grid of Saved Palette Swatches + Rainbow Wheel button at end
      val rows = customPalette.chunked(5)
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEachIndexed { rowIndex, rowColors ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            rowColors.forEachIndexed { colIndex, color ->
              val isSelected = color.toArgb() == selectedColor.toArgb()
              Box(
                modifier =
                  Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                      width = if (isSelected) 2.dp else 1.dp,
                      color =
                        if (isSelected) Color.White
                        else Color.White.copy(alpha = 0.18f),
                      shape = CircleShape,
                    )
                    .clickable { onSelectColorReplaceSlot(color) }
                    .testTag("palette_color_${rowIndex}_$colIndex"),
                contentAlignment = Alignment.Center,
              ) {
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected color",
                    tint =
                      if (color == Color.White || color == Color(0xFFFFF59D)) Color.Black
                      else Color.White,
                    modifier = Modifier.size(14.dp),
                  )
                }
              }
            }

            if (rowIndex == rows.lastIndex && rowColors.size < 5) {
              val rainbowColors =
                listOf(
                  Color.Red,
                  Color.Yellow,
                  Color.Green,
                  Color.Cyan,
                  Color.Blue,
                  Color.Magenta,
                  Color.Red,
                )
              Box(
                modifier =
                  Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.sweepGradient(rainbowColors))
                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clickable { isCustomCreatorOpen = true }
                    .testTag("palette_rainbow_wheel"),
              )
            }
          }
        }
      }
    } else {
      // 1:1 Goodnotes Custom 2D Saturation-Brightness Spectrum Square
      val pureHueColor = remember(hue) { Color.hsv(hue.coerceIn(0f, 360f), 1f, 1f) }
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, PopoverDivider, RoundedCornerShape(10.dp))
            .pointerInput(Unit) {
              detectTapGestures { offset ->
                saturation = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                brightness = (1f - (offset.y / size.height.coerceAtLeast(1))).coerceIn(0.05f, 1f)
              }
            }
            .pointerInput(Unit) {
              detectDragGestures { change, _ ->
                change.consume()
                saturation = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                brightness =
                  (1f - (change.position.y / size.height.coerceAtLeast(1))).coerceIn(0.05f, 1f)
              }
            }
            .testTag("custom_color_2d_spectrum"),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Horizontal White -> Pure Hue
          drawRect(
            brush = Brush.horizontalGradient(listOf(Color.White, pureHueColor)),
          )
          // Vertical Transparent -> Black
          drawRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
          )
          // Crosshair thumb at (saturation, 1 - brightness)
          val thumbX = saturation * size.width
          val thumbY = (1f - brightness) * size.height
          drawCircle(
            color = customPreviewColor,
            radius = 9.dp.toPx(),
            center = Offset(thumbX, thumbY),
          )
          drawCircle(
            color = Color.White,
            radius = 9.dp.toPx(),
            center = Offset(thumbX, thumbY),
            style = Stroke(width = 2.5.dp.toPx()),
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Rainbow Hue Bar
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.horizontalGradient(
                listOf(
                  Color.Red,
                  Color.Yellow,
                  Color.Green,
                  Color.Cyan,
                  Color.Blue,
                  Color.Magenta,
                  Color.Red,
                )
              )
            )
            .pointerInput(Unit) {
              detectTapGestures { offset ->
                hue = ((offset.x / size.width.coerceAtLeast(1)) * 360f).coerceIn(0f, 360f)
              }
            }
            .pointerInput(Unit) {
              detectDragGestures { change, _ ->
                change.consume()
                hue = ((change.position.x / size.width.coerceAtLeast(1)) * 360f).coerceIn(0f, 360f)
              }
            },
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val x = (hue / 360f) * size.width
          drawCircle(
            color = Color.White,
            radius = 8.dp.toPx(),
            center = Offset(x, size.height / 2f),
            style = Stroke(width = 2.5.dp.toPx()),
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Editable #HEX Input Box + Live Swatch + Save to Palette
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PopoverInnerBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Box(
            modifier =
              Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(customPreviewColor)
                .border(1.5.dp, Color.White, CircleShape),
          )
          Text(
            text = "HEX #",
            color = TextSecondaryGray,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
          )
          BasicTextField(
            value = hexInputText,
            onValueChange = { raw ->
              val cleaned =
                raw.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }.take(6)
              hexInputText = cleaned
              if (cleaned.length == 6) {
                runCatching {
                  val parsedInt = android.graphics.Color.parseColor("#$cleaned")
                  val hsv = FloatArray(3)
                  android.graphics.Color.colorToHSV(parsedInt, hsv)
                  hue = hsv[0]
                  saturation = hsv[1]
                  brightness = hsv[2]
                }
              }
            },
            textStyle =
              TextStyle(
                color = TextPrimaryWhite,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
              ),
            modifier =
              Modifier
                .width(64.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF2A2B30))
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .testTag("hex_color_input"),
          )
        }

        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(PopoverCardSelected)
              .clickable { onAddColorToCustomPalette(customPreviewColor) }
              .padding(horizontal = 8.dp, vertical = 5.dp),
        ) {
          Text(
            text = "+ Palette",
            color = Color(0xFF67C8EB),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = PopoverDivider)

    // Toolbar Color Slots Management: Replace Current vs Add New Slot to Toolbar
    val activeChosenColor = if (isCustomCreatorOpen) customPreviewColor else selectedColor
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(PopoverCardSelected)
              .clickable { onSelectColorReplaceSlot(activeChosenColor) }
              .testTag("replace_toolbar_color_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Replace Slot #${selectedSwatchIndex + 1}",
            color = TextPrimaryWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
          )
        }

        Row(
          modifier =
            Modifier
              .weight(1f)
              .height(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(SwitchBlue)
              .clickable { onAddNewColorSlotToToolbar(activeChosenColor) }
              .padding(horizontal = 6.dp)
              .testTag("add_toolbar_color_slot_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "Add to Toolbar",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      if (toolbarSwatchCount > 2) {
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(28.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(DangerRed.copy(alpha = 0.15f))
              .clickable(onClick = onRemoveCurrentToolbarSlot),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Remove Slot #${selectedSwatchIndex + 1} from Toolbar ($toolbarSwatchCount slots)",
            color = DangerRed,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
          )
        }
      }
    }
  }
}

@Composable
fun EraserSettingsPopover(
  selectedMode: EraserMode,
  eraserRadius: Float,
  autoDeselect: Boolean,
  onSelectMode: (EraserMode) -> Unit,
  onEraserRadiusChange: (Float) -> Unit,
  onToggleAutoDeselect: (Boolean) -> Unit,
  onClearPage: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .width(276.dp)
        .shadow(16.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("eraser_settings_popover"),
  ) {
    Text(
      text = "Eraser Tool (Erases Ink, Lines & Shapes)",
      color = TextPrimaryWhite,
      fontSize = 13.5.sp,
      fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(10.dp))

    EraserMode.entries.forEach { mode ->
      val isSelected = mode == selectedMode
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PopoverCardSelected else Color.Transparent)
            .clickable { onSelectMode(mode) }
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = mode.label,
          color = if (isSelected) TextPrimaryWhite else TextSecondaryGray,
          fontSize = 13.sp,
          fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
        )
        if (isSelected) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = SwitchBlue,
            modifier = Modifier.size(16.dp),
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PopoverDivider)

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "ERASER SIZE",
        color = TextSecondaryGray,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
      )
      Text(
        text = "${eraserRadius.toInt()} px",
        color = TextSecondaryGray,
        fontSize = 10.sp,
      )
    }

    Slider(
      value = eraserRadius,
      onValueChange = onEraserRadiusChange,
      valueRange = 14f..64f,
      colors =
        SliderDefaults.colors(
          thumbColor = Color.White,
          activeTrackColor = SwitchBlue,
          inactiveTrackColor = Color(0xFF38383C),
        ),
      modifier = Modifier.fillMaxWidth().height(26.dp),
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = PopoverDivider)

    PopoverToggleRow(
      label = "Auto-Deselect",
      checked = autoDeselect,
      onCheckedChange = onToggleAutoDeselect,
      testTag = "eraser_auto_deselect_switch",
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PopoverDivider)

    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(DangerRed.copy(alpha = 0.16f))
          .clickable(onClick = onClearPage),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "Clear Page",
        color = DangerRed,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
fun ShapeSettingsPopover(
  lastAutoDetectedShape: ShapeKind?,
  fillShapes: Boolean,
  onInsertQuickShape: (ShapeKind) -> Unit,
  onToggleFillShapes: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .width(316.dp)
        .shadow(16.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("shape_settings_popover"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Goodnotes Auto-Shapes (11 Shapes)",
        color = TextPrimaryWhite,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Box(
        modifier =
          Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SwitchBlue.copy(alpha = 0.22f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
      ) {
        Text(
          text = "Auto-Detect ON",
          color = Color(0xFF67C8EB),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text =
        "Draw freehand — automatically recognizes Lines, Arrows, Curves, Circles, Ovals, Triangles, Rectangles, Diamonds, Pentagons, Hexagons & Stars!",
      color = TextSecondaryGray,
      fontSize = 10.5.sp,
    )

    if (lastAutoDetectedShape != null) {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        ShapeKindPreviewIcon(
          kind = lastAutoDetectedShape,
          tint = Color(0xFF67C8EB),
          modifier = Modifier.size(15.dp),
        )
        Text(
          text = "Last recognized: ${lastAutoDetectedShape.label}",
          color = Color(0xFF67C8EB),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    PopoverToggleRow(
      label = "Fill Shape Color (Semi-Transparent)",
      checked = fillShapes,
      onCheckedChange = onToggleFillShapes,
      testTag = "shape_fill_switch",
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PopoverDivider)

    Text(
      text = "QUICK-STAMP PERFECT SHAPE (11 GEOMETRIES)",
      color = TextSecondaryGray,
      fontSize = 9.5.sp,
      fontWeight = FontWeight.Medium,
    )
    Spacer(modifier = Modifier.height(6.dp))

    val rows = ShapeKind.entries.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      rows.forEach { rowShapes ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          rowShapes.forEach { kind ->
            Row(
              modifier =
                Modifier
                  .weight(1f)
                  .height(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(PopoverCardSelected)
                  .clickable { onInsertQuickShape(kind) }
                  .padding(horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              ShapeKindPreviewIcon(
                kind = kind,
                tint = Color(0xFF67C8EB),
                modifier = Modifier.size(13.dp),
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = kind.label.substringBefore(" "),
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun LassoSettingsPopover(
  filters: LassoFilterSettings,
  onUpdateFilters: (LassoFilterSettings) -> Unit,
  onBringToFront: () -> Unit,
  onSendToBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .width(268.dp)
        .shadow(16.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("lasso_settings_popover"),
  ) {
    Text(
      text = "Lasso Tool",
      color = TextPrimaryWhite,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(8.dp))

    PopoverToggleRow(
      label = "Handwriting",
      checked = filters.includeHandwriting,
      onCheckedChange = { onUpdateFilters(filters.copy(includeHandwriting = it)) },
      testTag = "lasso_filter_handwriting",
    )
    PopoverToggleRow(
      label = "Images",
      checked = filters.includeImages,
      onCheckedChange = { onUpdateFilters(filters.copy(includeImages = it)) },
      testTag = "lasso_filter_images",
    )
    PopoverToggleRow(
      label = "Text Boxes",
      checked = filters.includeTextBoxes,
      onCheckedChange = { onUpdateFilters(filters.copy(includeTextBoxes = it)) },
      testTag = "lasso_filter_text",
    )
    PopoverToggleRow(
      label = "Comments",
      checked = filters.includeComments,
      onCheckedChange = { onUpdateFilters(filters.copy(includeComments = it)) },
      testTag = "lasso_filter_comments",
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PopoverDivider)

    Text(
      text = "LAYER ORDERING",
      color = TextSecondaryGray,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
    )
    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier =
          Modifier
            .weight(1f)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PopoverCardSelected)
            .clickable(onClick = onBringToFront)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.FlipToFront,
          contentDescription = null,
          tint = TextPrimaryWhite,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Bring to Front", color = TextPrimaryWhite, fontSize = 11.sp)
      }

      Row(
        modifier =
          Modifier
            .weight(1f)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PopoverCardSelected)
            .clickable(onClick = onSendToBack)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.FlipToBack,
          contentDescription = null,
          tint = TextPrimaryWhite,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Send to Back", color = TextPrimaryWhite, fontSize = 11.sp)
      }
    }
  }
}

@Composable
fun ElementsPickerPopover(
  onInsertElement: (ElementStampType) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .width(280.dp)
        .shadow(16.dp, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(PopoverBg)
        .border(1.dp, PopoverDivider, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("elements_picker_popover"),
  ) {
    Text(
      text = "Elements",
      color = TextPrimaryWhite,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
    )
    Text(
      text = "Tap any element to insert onto page",
      color = TextSecondaryGray,
      fontSize = 11.sp,
    )
    Spacer(modifier = Modifier.height(10.dp))

    val rows = ElementStampType.entries.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      rows.forEach { pair ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          pair.forEach { stamp ->
            Box(
              modifier =
                Modifier
                  .weight(1f)
                  .height(42.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(stamp.tint.copy(alpha = 0.18f))
                  .border(1.dp, stamp.tint.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                  .clickable { onInsertElement(stamp) }
                  .padding(horizontal = 8.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = stamp.badgeText,
                color = stamp.tint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun LassoFloatingMenuPill(
  isResizeModeActive: Boolean,
  hasClipboardContent: Boolean,
  isColorMenuOpen: Boolean,
  onToggleColorMenu: () -> Unit,
  onSelectColor: (Color) -> Unit,
  onCopyClick: () -> Unit,
  onPasteClick: () -> Unit,
  onCutClick: () -> Unit,
  onScaleStepClick: (Boolean) -> Unit,
  onResizeToggleClick: () -> Unit,
  onDuplicateClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onConvertAiTextClick: () -> Unit,
  onOpenLassoFiltersClick: () -> Unit,
  onDismissLassoClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val lassoQuickSwatches =
    listOf(
      Color(0xFF1A1A1A),
      Color(0xFF1E5AA8),
      Color(0xFFD32F2F),
      Color(0xFF2E7D32),
      Color(0xFF8E24AA),
      Color(0xFFFB8C00),
      Color(0xFF00ACC1),
    )

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    if (isColorMenuOpen) {
      Row(
        modifier =
          Modifier
            .height(38.dp)
            .shadow(10.dp, RoundedCornerShape(19.dp))
            .clip(RoundedCornerShape(19.dp))
            .background(PopoverBg)
            .border(1.dp, Color(0xFF67C8EB).copy(alpha = 0.6f), RoundedCornerShape(19.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "Recolor:",
          color = Color(0xFF67C8EB),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.SemiBold,
        )
        lassoQuickSwatches.forEach { color ->
          Box(
            modifier =
              Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.5.dp, Color.White, CircleShape)
                .clickable { onSelectColor(color) },
          )
        }
      }
    }

    Row(
      modifier =
        Modifier
          .height(44.dp)
          .shadow(14.dp, RoundedCornerShape(22.dp))
          .clip(RoundedCornerShape(22.dp))
          .background(Color(0xFF1C1D22))
          .border(1.dp, Color(0xFF383A42), RoundedCornerShape(22.dp))
          .padding(horizontal = 10.dp)
          .testTag("lasso_floating_action_pill"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // 1. Move Indicator / Layer Filters Icon
      Box(
        modifier =
          Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(0xFF1A4FA0))
            .clickable(onClick = onOpenLassoFiltersClick),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = "Lasso filter and layer options",
          tint = Color.White,
          modifier = Modifier.size(15.dp),
        )
      }

      // 2. Change Color Button
      Row(
        modifier =
          Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isColorMenuOpen) Color(0x3367C8EB) else Color.Transparent)
            .clickable(onClick = onToggleColorMenu)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = "A",
          color = TextPrimaryWhite,
          fontSize = 15.sp,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
        )
        Icon(
          imageVector = Icons.Outlined.AutoAwesome,
          contentDescription = "Recolor selection",
          tint = if (isColorMenuOpen) Color(0xFF67C8EB) else TextPrimaryWhite,
          modifier = Modifier.size(13.dp),
        )
      }

      // 3. Cut
      Icon(
        imageVector = Icons.Default.ContentCut,
        contentDescription = "Cut selection",
        tint = TextPrimaryWhite,
        modifier =
          Modifier
            .size(18.dp)
            .clickable(onClick = onCutClick)
            .testTag("lasso_cut_button"),
      )

      // 4. Copy
      Icon(
        imageVector = Icons.Outlined.ContentCopy,
        contentDescription = "Copy selection",
        tint = TextPrimaryWhite,
        modifier =
          Modifier
            .size(18.dp)
            .clickable(onClick = onCopyClick)
            .testTag("lasso_copy_button"),
      )

      // 5. Paste
      Icon(
        imageVector = Icons.Outlined.ContentPaste,
        contentDescription = "Paste from clipboard",
        tint = if (hasClipboardContent) Color(0xFF67C8EB) else TextSecondaryGray.copy(alpha = 0.5f),
        modifier =
          Modifier
            .size(18.dp)
            .clickable(enabled = hasClipboardContent, onClick = onPasteClick)
            .testTag("lasso_paste_button"),
      )

      // 6. Resize Mode Toggle
      Icon(
        imageVector = Icons.Outlined.PhotoSizeSelectSmall,
        contentDescription = "Resize & Rotate selection",
        tint = if (isResizeModeActive) Color(0xFF67C8EB) else TextPrimaryWhite,
        modifier =
          Modifier
            .size(19.dp)
            .clickable(onClick = onResizeToggleClick)
            .testTag("lasso_resize_button"),
      )

      // 7. Quick Scale Down (-) and Up (+)
      Row(
        modifier =
          Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF2B2D35))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Icon(
          imageVector = Icons.Default.Remove,
          contentDescription = "Scale Down",
          tint = TextPrimaryWhite,
          modifier = Modifier.size(14.dp).clickable { onScaleStepClick(false) },
        )
        Text(text = "Size", color = TextSecondaryGray, fontSize = 9.5.sp)
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Scale Up",
          tint = TextPrimaryWhite,
          modifier = Modifier.size(14.dp).clickable { onScaleStepClick(true) },
        )
      }

      // 8. Duplicate
      Icon(
        imageVector = Icons.Outlined.LibraryAdd,
        contentDescription = "Duplicate selection",
        tint = TextPrimaryWhite,
        modifier =
          Modifier
            .size(18.dp)
            .clickable(onClick = onDuplicateClick)
            .testTag("lasso_duplicate_button"),
      )

      // 9. AI Handwriting Assist
      Text(
        text = "AI",
        color = TextPrimaryWhite,
        fontSize = 13.5.sp,
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable(onClick = onConvertAiTextClick),
      )

      // 10. Delete
      Icon(
        imageVector = Icons.Outlined.DeleteOutline,
        contentDescription = "Delete selection",
        tint = DangerRed,
        modifier =
          Modifier
            .size(20.dp)
            .clickable(onClick = onDeleteClick)
            .testTag("lasso_delete_button"),
      )

      // 11. Dismiss
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Dismiss selection",
        tint = TextSecondaryGray,
        modifier =
          Modifier
            .size(18.dp)
            .clickable(onClick = onDismissLassoClick)
            .testTag("lasso_dismiss_button"),
      )
    }
  }
}
