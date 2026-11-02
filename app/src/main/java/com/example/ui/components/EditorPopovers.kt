package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
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
import com.example.model.TapePattern
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
            PenStyle.BALL -> strokeWidth * 1.15f
            PenStyle.FINELINER -> (strokeWidth * 0.72f).coerceAtLeast(2.2f)
            PenStyle.BRUSH -> strokeWidth * 1.65f
            PenStyle.CALLIGRAPHY -> strokeWidth * 1.45f
            PenStyle.MARKER -> strokeWidth * 1.85f
            PenStyle.NEON_GLOW -> strokeWidth * 1.25f
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
        if (selectedPenStyle == PenStyle.NEON_GLOW) {
          drawPath(
            path = path,
            color = Color(0xFF38BDF8).copy(alpha = 0.35f),
            style =
              Stroke(
                width = (previewStroke * 2.4f).coerceIn(10f, 34f),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = dashEffect,
              ),
          )
        }
        drawPath(
          path = path,
          color = if (selectedPenStyle == PenStyle.NEON_GLOW) Color(0xFFE0F2FE) else Color.White,
          style =
            Stroke(
              width = previewStroke.coerceIn(2.5f, 20f),
              cap = if (selectedPenStyle == PenStyle.MARKER || selectedPenStyle == PenStyle.CALLIGRAPHY) StrokeCap.Square else StrokeCap.Round,
              join = StrokeJoin.Round,
              pathEffect = dashEffect,
            ),
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 7 Studio Pen Style Selector Cards (Horizontally Scrollable Strip)
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      PenStyle.entries.forEach { style ->
        val isSelected = style == selectedPenStyle
        Column(
          modifier =
            Modifier
              .width(82.dp)
              .height(58.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) PopoverCardSelected else Color(0xFF1F222B))
              .clickable { onSelectPenStyle(style) }
              .padding(horizontal = 6.dp, vertical = 6.dp)
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
            fontSize = 10.sp,
            maxLines = 1,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
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
      label = "Stylus Side Button Erase",
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

      PenStyle.FINELINER -> {
        // Needle-point technical fineliner nib
        drawLine(
          color = strokeColor,
          start = Offset(w * 0.22f, h * 0.78f),
          end = Offset(w * 0.42f, h * 0.58f),
          strokeWidth = 2.2f,
          cap = StrokeCap.Round,
        )
        val collar =
          Path().apply {
            moveTo(w * 0.36f, h * 0.52f)
            lineTo(w * 0.48f, h * 0.64f)
            lineTo(w * 0.78f, h * 0.34f)
            lineTo(w * 0.66f, h * 0.22f)
            close()
          }
        drawPath(path = collar, color = strokeColor, style = Stroke(width = 2.6f))
      }

      PenStyle.CALLIGRAPHY -> {
        // 45° Italic Chisel Calligraphy Nib with breather hole
        val nib =
          Path().apply {
            moveTo(w * 0.18f, h * 0.70f)
            lineTo(w * 0.32f, h * 0.84f)
            lineTo(w * 0.78f, h * 0.38f)
            lineTo(w * 0.60f, h * 0.20f)
            close()
          }
        drawPath(path = nib, color = strokeColor, style = Stroke(width = 2.6f))
        drawCircle(color = strokeColor, radius = w * 0.06f, center = Offset(w * 0.48f, h * 0.52f))
      }

      PenStyle.MARKER -> {
        // Bold Studio Chisel Marker
        val marker =
          Path().apply {
            moveTo(w * 0.20f, h * 0.76f)
            lineTo(w * 0.38f, h * 0.84f)
            lineTo(w * 0.80f, h * 0.42f)
            lineTo(w * 0.58f, h * 0.20f)
            close()
          }
        drawPath(path = marker, color = strokeColor)
      }

      PenStyle.NEON_GLOW -> {
        // Glowing cyber pen with halo ring
        drawCircle(
          color = strokeColor.copy(alpha = 0.32f),
          radius = w * 0.34f,
          center = Offset(w * 0.50f, h * 0.50f),
        )
        drawLine(
          color = strokeColor,
          start = Offset(w * 0.24f, h * 0.76f),
          end = Offset(w * 0.76f, h * 0.24f),
          strokeWidth = 3.2f,
          cap = StrokeCap.Round,
        )
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
  selectedShapeKind: ShapeKind = ShapeKind.RECTANGLE,
  shapeAutoDetectMode: Boolean = true,
  lastAutoDetectedShape: ShapeKind?,
  fillShapes: Boolean,
  onSelectShapeForDraw: (ShapeKind) -> Unit = {},
  onToggleShapeAutoDetect: (Boolean) -> Unit = {},
  onInsertQuickShape: (ShapeKind) -> Unit,
  onToggleFillShapes: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .width(332.dp)
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
        text = "Shape Studio (11 Geometries)",
        color = TextPrimaryWhite,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.SemiBold,
      )
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
              .background(if (shapeAutoDetectMode) SwitchBlue else Color.Transparent)
              .clickable { onToggleShapeAutoDetect(true) }
              .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Text(
            text = "Auto-Snap",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
          )
        }
        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (!shapeAutoDetectMode) SwitchBlue else Color.Transparent)
              .clickable { onToggleShapeAutoDetect(false) }
              .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Text(
            text = "Drag Shape",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text =
        if (shapeAutoDetectMode) {
          "Freehand Auto-Snap: Sketch any shape and it snaps into crisp geometry. Or pick a shape below to drag-draw directly."
        } else {
          "Direct Drag Mode: Drag across the page to draw a live ${selectedShapeKind.label} with adjustable handles."
        },
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
          text = "Last created: ${lastAutoDetectedShape.label}",
          color = Color(0xFF67C8EB),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    PopoverToggleRow(
      label = "Fill Closed Shapes (Translucent Tint)",
      checked = fillShapes,
      onCheckedChange = onToggleFillShapes,
      testTag = "shape_fill_switch",
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PopoverDivider)

    Text(
      text = "SELECT SHAPE TO DRAG-DRAW ON PAGE",
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
            val isActive = !shapeAutoDetectMode && selectedShapeKind == kind
            Row(
              modifier =
                Modifier
                  .weight(1f)
                  .height(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isActive) SwitchBlue.copy(alpha = 0.28f) else PopoverCardSelected)
                  .border(
                    width = if (isActive) 1.5.dp else 0.dp,
                    color = if (isActive) SwitchBlue else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                  )
                  .clickable { onSelectShapeForDraw(kind) }
                  .padding(horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              ShapeKindPreviewIcon(
                kind = kind,
                tint = if (isActive) Color.White else Color(0xFF67C8EB),
                modifier = Modifier.size(13.dp),
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = kind.label.substringBefore(" "),
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(SwitchBlue)
          .clickable { onInsertQuickShape(selectedShapeKind) }
          .testTag("stamp_selected_shape_button"),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "Stamp ${selectedShapeKind.label} on Page",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
      )
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
  val activeCount =
    listOf(
      filters.includeHandwriting,
      filters.includeShapes,
      filters.includeStudyTapes,
      filters.includeTextBoxes,
      filters.includeImages,
      filters.includeComments,
    ).count { it }

  Column(
    modifier =
      modifier
        .width(326.dp)
        .shadow(22.dp, RoundedCornerShape(18.dp))
        .clip(RoundedCornerShape(18.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(Color(0xFF1D202A), Color(0xFF15171F)),
          ),
        )
        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(18.dp))
        .padding(16.dp)
        .testTag("lasso_settings_popover"),
  ) {
    // Header Row with Active Filter Counter & Select All Quick Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = "Lasso Studio",
          color = TextPrimaryWhite,
          fontSize = 15.5.sp,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "$activeCount of 6 layers active • Smart selection",
          color = Color(0xFF67C8EB),
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Medium,
        )
      }

      val allSelected = activeCount == 6
      Box(
        modifier =
          Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (allSelected) Color(0xFF252936) else SwitchBlue.copy(alpha = 0.22f))
            .border(
              1.dp,
              if (allSelected) Color(0xFF383E52) else SwitchBlue,
              RoundedCornerShape(8.dp),
            )
            .clickable {
              val next = !allSelected
              onUpdateFilters(
                filters.copy(
                  includeHandwriting = next || true,
                  includeShapes = next,
                  includeStudyTapes = next,
                  includeTextBoxes = next,
                  includeImages = next,
                  includeComments = next,
                )
              )
            }
            .padding(horizontal = 9.dp, vertical = 5.dp)
            .testTag("lasso_toggle_all_filters_button"),
      ) {
        Text(
          text = if (allSelected) "Ink Only" else "Select All",
          color = if (allSelected) TextSecondaryGray else Color(0xFF67C8EB),
          fontSize = 10.5.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Selection Geometry Mode: Freehand Loop vs Rectangle Marquee
    Text(
      text = "SELECTION GEOMETRY",
      color = TextSecondaryGray,
      fontSize = 9.5.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF101218))
          .padding(3.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      val modes = listOf(false to "Freehand Loop", true to "Rectangle Box")
      modes.forEach { (isRect, label) ->
        val isSel = filters.rectangularMarquee == isRect
        Row(
          modifier =
            Modifier
              .weight(1f)
              .height(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSel) SwitchBlue else Color.Transparent)
              .clickable { onUpdateFilters(filters.copy(rectangularMarquee = isRect)) }
              .testTag(if (isRect) "lasso_mode_rectangle" else "lasso_mode_freehand"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Canvas(modifier = Modifier.size(13.dp)) {
            val c = if (isSel) Color.White else Color(0xFF8E95A5)
            if (isRect) {
              drawRect(
                color = c,
                style =
                  Stroke(
                    width = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f),
                  ),
              )
            } else {
              drawCircle(
                color = c,
                radius = size.minDimension * 0.44f,
                style =
                  Stroke(
                    width = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f),
                  ),
              )
            }
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = label,
            color = if (isSel) Color.White else TextSecondaryGray,
            fontSize = 11.5.sp,
            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2x3 Visual Filter Cards Grid
    Text(
      text = "SELECTABLE CONTENT LAYERS",
      color = TextSecondaryGray,
      fontSize = 9.5.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp,
    )
    Spacer(modifier = Modifier.height(6.dp))

    data class FilterCardItem(
      val title: String,
      val subtitle: String,
      val checked: Boolean,
      val accent: Color,
      val testTag: String,
      val onToggle: (Boolean) -> Unit,
    )

    val filterItems =
      listOf(
        FilterCardItem(
          title = "Handwriting",
          subtitle = "Ink & Highlighter",
          checked = filters.includeHandwriting,
          accent = Color(0xFF38BDF8),
          testTag = "lasso_filter_handwriting",
          onToggle = { onUpdateFilters(filters.copy(includeHandwriting = it)) },
        ),
        FilterCardItem(
          title = "Shapes",
          subtitle = "Snapped Geometry",
          checked = filters.includeShapes,
          accent = Color(0xFFA78BFA),
          testTag = "lasso_filter_shapes",
          onToggle = { onUpdateFilters(filters.copy(includeShapes = it)) },
        ),
        FilterCardItem(
          title = "Study Tapes",
          subtitle = "Washi & Flashcards",
          checked = filters.includeStudyTapes,
          accent = Color(0xFFF472B6),
          testTag = "lasso_filter_tapes",
          onToggle = { onUpdateFilters(filters.copy(includeStudyTapes = it)) },
        ),
        FilterCardItem(
          title = "Text Boxes",
          subtitle = "Typed Notes",
          checked = filters.includeTextBoxes,
          accent = Color(0xFF34D399),
          testTag = "lasso_filter_text",
          onToggle = { onUpdateFilters(filters.copy(includeTextBoxes = it)) },
        ),
        FilterCardItem(
          title = "Images",
          subtitle = "Photos & Stamps",
          checked = filters.includeImages,
          accent = Color(0xFFFBBF24),
          testTag = "lasso_filter_images",
          onToggle = { onUpdateFilters(filters.copy(includeImages = it)) },
        ),
        FilterCardItem(
          title = "Sticky Notes",
          subtitle = "Pinned Cards",
          checked = filters.includeComments,
          accent = Color(0xFFFB923C),
          testTag = "lasso_filter_comments",
          onToggle = { onUpdateFilters(filters.copy(includeComments = it)) },
        ),
      )

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
      filterItems.chunked(2).forEach { rowItems ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
          rowItems.forEach { item ->
            Row(
              modifier =
                Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(11.dp))
                  .background(
                    if (item.checked) item.accent.copy(alpha = 0.14f)
                    else Color(0xFF21242F),
                  )
                  .border(
                    width = if (item.checked) 1.5.dp else 1.dp,
                    color = if (item.checked) item.accent else Color(0xFF2E3240),
                    shape = RoundedCornerShape(11.dp),
                  )
                  .clickable { item.onToggle(!item.checked) }
                  .padding(horizontal = 10.dp, vertical = 8.dp)
                  .testTag(item.testTag),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.title,
                  color = if (item.checked) Color.White else TextSecondaryGray,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                )
                Text(
                  text = item.subtitle,
                  color = if (item.checked) item.accent else TextSecondaryGray.copy(alpha = 0.65f),
                  fontSize = 9.sp,
                  maxLines = 1,
                )
              }
              Box(
                modifier =
                  Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (item.checked) item.accent else Color(0xFF323646)),
                contentAlignment = Alignment.Center,
              ) {
                if (item.checked) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF0F131A),
                    modifier = Modifier.size(12.dp),
                  )
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = PopoverDivider, thickness = 0.5.dp)
    Spacer(modifier = Modifier.height(4.dp))

    PopoverToggleRow(
      label = "Auto-Return to Pen After Moving",
      checked = filters.autoReturnToPenAfterMove,
      onCheckedChange = { onUpdateFilters(filters.copy(autoReturnToPenAfterMove = it)) },
      testTag = "lasso_auto_return_pen_switch",
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = PopoverDivider)

    Text(
      text = "LAYER ORDERING",
      color = TextSecondaryGray,
      fontSize = 9.5.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp,
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
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(PopoverCardSelected)
            .border(1.dp, Color(0xFF383E52), RoundedCornerShape(10.dp))
            .clickable(onClick = onBringToFront)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.FlipToFront,
          contentDescription = null,
          tint = Color(0xFF67C8EB),
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text("Bring to Front", color = TextPrimaryWhite, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
      }

      Row(
        modifier =
          Modifier
            .weight(1f)
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(PopoverCardSelected)
            .border(1.dp, Color(0xFF383E52), RoundedCornerShape(10.dp))
            .clickable(onClick = onSendToBack)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.FlipToBack,
          contentDescription = null,
          tint = Color(0xFF67C8EB),
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text("Send to Back", color = TextPrimaryWhite, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

/**
 * Study Tape & Decorative Washi Tape Studio Popover:
 * Lets the user pick from 7 decorative tape patterns (Diagonal Stripe, Polka Dot, Grid Washi,
 * Stardust, Gradient Glow, Exam Caution, Solid), adjust tape thickness, and reveal/cover all tapes on page.
 */
@Composable
fun TapeSettingsPopover(
  selectedPattern: TapePattern,
  tapeThickness: Float,
  selectedColor: Color,
  studyTapeCountOnPage: Int,
  onSelectPattern: (TapePattern) -> Unit,
  onThicknessChange: (Float) -> Unit,
  onRevealAllTapes: () -> Unit,
  onCoverAllTapes: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .width(322.dp)
        .shadow(20.dp, RoundedCornerShape(18.dp))
        .clip(RoundedCornerShape(18.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(Color(0xFF1D202A), Color(0xFF15171F)),
          ),
        )
        .border(1.dp, Color(0xFF2E3344), RoundedCornerShape(18.dp))
        .padding(16.dp)
        .testTag("tape_settings_popover"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = "Study & Washi Tape Studio",
          color = TextPrimaryWhite,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "${selectedPattern.label} • ${selectedPattern.subtitle}",
          color = Color(0xFF67C8EB),
          fontSize = 10.5.sp,
        )
      }
      Box(
        modifier =
          Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF242836))
            .padding(horizontal = 8.dp, vertical = 4.dp),
      ) {
        Text(
          text = "$studyTapeCountOnPage on page",
          color = TextSecondaryGray,
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Live Tape Preview Box on White Paper Card
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(64.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFFF8FAFC))
          .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "Hidden Formula: E = mc²",
        color = Color(0xFF1E293B),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
      )
      Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        val start = Offset(12f, size.height * 0.5f)
        val end = Offset(size.width - 12f, size.height * 0.5f)
        val h = tapeThickness.coerceIn(22f, 52f)
        // Base strip
        val baseTint =
          if (selectedPattern == TapePattern.EXAM_CAUTION) Color(0xFFFACC15) else selectedColor
        drawLine(
          color = baseTint.copy(alpha = 0.94f),
          start = start,
          end = end,
          strokeWidth = h,
          cap = StrokeCap.Round,
        )
        when (selectedPattern) {
          TapePattern.SOLID -> {
            drawLine(
              color = Color.White.copy(alpha = 0.32f),
              start = start,
              end = end,
              strokeWidth = 2f,
              cap = StrokeCap.Round,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
            )
          }
          TapePattern.DIAGONAL_STRIPE -> {
            var x = start.x
            while (x <= end.x) {
              drawLine(
                color = Color.White.copy(alpha = 0.42f),
                start = Offset((x - 8f).coerceAtLeast(start.x), start.y - h * 0.36f),
                end = Offset((x + 8f).coerceAtMost(end.x), start.y + h * 0.36f),
                strokeWidth = 3f,
                cap = StrokeCap.Round,
              )
              x += 16f
            }
          }
          TapePattern.POLKA_DOT -> {
            var x = start.x + 8f
            var rowFlip = false
            while (x <= end.x - 8f) {
              drawCircle(
                color = Color.White.copy(alpha = 0.68f),
                radius = 3.5f,
                center = Offset(x, start.y + (if (rowFlip) -h * 0.18f else h * 0.18f)),
              )
              rowFlip = !rowFlip
              x += 14f
            }
          }
          TapePattern.GRID_WASHI -> {
            drawLine(
              color = Color.White.copy(alpha = 0.38f),
              start = Offset(start.x, start.y - h * 0.2f),
              end = Offset(end.x, end.y - h * 0.2f),
              strokeWidth = 1.6f,
            )
            drawLine(
              color = Color.White.copy(alpha = 0.38f),
              start = Offset(start.x, start.y + h * 0.2f),
              end = Offset(end.x, end.y + h * 0.2f),
              strokeWidth = 1.6f,
            )
            var x = start.x + 10f
            while (x <= end.x - 10f) {
              drawLine(
                color = Color.White.copy(alpha = 0.38f),
                start = Offset(x, start.y - h * 0.38f),
                end = Offset(x, start.y + h * 0.38f),
                strokeWidth = 1.6f,
              )
              x += 14f
            }
          }
          TapePattern.STARDUST -> {
            var x = start.x + 12f
            var idx = 0
            while (x <= end.x - 12f) {
              val cy = start.y + (if (idx % 2 == 0) -h * 0.15f else h * 0.15f)
              drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 2.6f, center = Offset(x, cy))
              drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(x - 5f, cy),
                end = Offset(x + 5f, cy),
                strokeWidth = 1.4f,
              )
              drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(x, cy - 5f),
                end = Offset(x, cy + 5f),
                strokeWidth = 1.4f,
              )
              x += 20f
              idx++
            }
          }
          TapePattern.GRADIENT_GLOW -> {
            drawLine(
              color = Color.White.copy(alpha = 0.45f),
              start = start,
              end = end,
              strokeWidth = h * 0.38f,
              cap = StrokeCap.Round,
            )
          }
          TapePattern.EXAM_CAUTION -> {
            var x = start.x
            while (x <= end.x) {
              drawLine(
                color = Color(0xFF1E293B).copy(alpha = 0.78f),
                start = Offset((x - 7f).coerceAtLeast(start.x), start.y - h * 0.36f),
                end = Offset((x + 7f).coerceAtMost(end.x), start.y + h * 0.36f),
                strokeWidth = 4.5f,
                cap = StrokeCap.Square,
              )
              x += 16f
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 7 Washi Tape Design Selector Cards
    Text(
      text = "WASHI TAPE DESIGN",
      color = TextSecondaryGray,
      fontSize = 9.5.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp,
    )
    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
      TapePattern.entries.forEach { pattern ->
        val isSel = pattern == selectedPattern
        Column(
          modifier =
            Modifier
              .width(86.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSel) SwitchBlue.copy(alpha = 0.24f) else Color(0xFF21242F))
              .border(
                width = if (isSel) 1.5.dp else 1.dp,
                color = if (isSel) SwitchBlue else Color(0xFF2E3240),
                shape = RoundedCornerShape(10.dp),
              )
              .clickable { onSelectPattern(pattern) }
              .padding(horizontal = 8.dp, vertical = 8.dp)
              .testTag("tape_pattern_${pattern.name.lowercase()}"),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = pattern.label,
            color = if (isSel) Color.White else TextPrimaryWhite,
            fontSize = 10.5.sp,
            fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = pattern.subtitle,
            color = if (isSel) Color(0xFF67C8EB) else TextSecondaryGray,
            fontSize = 8.5.sp,
            maxLines = 1,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Tape Thickness Slider
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "TAPE THICKNESS",
        color = TextSecondaryGray,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "${tapeThickness.toInt()} px",
        color = Color(0xFF67C8EB),
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
      )
    }
    Slider(
      value = tapeThickness,
      onValueChange = onThicknessChange,
      valueRange = 20f..68f,
      colors =
        SliderDefaults.colors(
          thumbColor = Color.White,
          activeTrackColor = SwitchBlue,
          inactiveTrackColor = Color(0xFF38383C),
        ),
      modifier = Modifier.fillMaxWidth().height(26.dp).testTag("tape_thickness_slider"),
    )

    if (studyTapeCountOnPage > 0) {
      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PopoverDivider)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(PopoverCardSelected)
              .clickable(onClick = onRevealAllTapes)
              .testTag("tape_reveal_all_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Reveal All Tapes",
            color = Color(0xFF67C8EB),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(PopoverCardSelected)
              .clickable(onClick = onCoverAllTapes)
              .testTag("tape_cover_all_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Cover All Tapes",
            color = TextPrimaryWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
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
  onConvertAiTextClick: (() -> Unit)? = null,
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
          .height(46.dp)
          .shadow(14.dp, RoundedCornerShape(23.dp))
          .clip(RoundedCornerShape(23.dp))
          .background(Color(0xFF16171B))
          .border(1.dp, Color(0xFF2B2D35), RoundedCornerShape(23.dp))
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 10.dp)
          .testTag("lasso_floating_action_pill"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      // 1. Move Indicator / Layer Filters Icon
      Box(
        modifier =
          Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xFF1A4FA0))
            .clickable(onClick = onOpenLassoFiltersClick),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = "Lasso filter and layer options",
          tint = Color.White,
          modifier = Modifier.size(16.dp),
        )
      }

      // 2. Change Color Button
      Row(
        modifier =
          Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isColorMenuOpen) Color(0x3367C8EB) else Color.Transparent)
            .clickable(onClick = onToggleColorMenu)
            .padding(horizontal = 6.dp)
            .testTag("lasso_color_button"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = "A",
          color = if (isColorMenuOpen) Color(0xFF67C8EB) else TextPrimaryWhite,
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
      Box(
        modifier =
          Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onCutClick)
            .testTag("lasso_cut_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.ContentCut,
          contentDescription = "Cut selection",
          tint = TextPrimaryWhite,
          modifier = Modifier.size(18.dp),
        )
      }

      // 4. Copy
      Box(
        modifier =
          Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onCopyClick)
            .testTag("lasso_copy_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.ContentCopy,
          contentDescription = "Copy selection",
          tint = TextPrimaryWhite,
          modifier = Modifier.size(18.dp),
        )
      }

      // 5. Paste
      Box(
        modifier =
          Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (hasClipboardContent) Color(0x2267C8EB) else Color.Transparent)
            .clickable(enabled = hasClipboardContent, onClick = onPasteClick)
            .testTag("lasso_paste_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.ContentPaste,
          contentDescription = "Paste from clipboard",
          tint = if (hasClipboardContent) Color(0xFF67C8EB) else TextSecondaryGray.copy(alpha = 0.45f),
          modifier = Modifier.size(18.dp),
        )
      }

      // 6. Resize Mode Toggle
      Box(
        modifier =
          Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isResizeModeActive) Color(0x3367C8EB) else Color.Transparent)
            .clickable(onClick = onResizeToggleClick)
            .testTag("lasso_resize_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.PhotoSizeSelectSmall,
          contentDescription = "Resize & Rotate selection",
          tint = if (isResizeModeActive) Color(0xFF67C8EB) else TextPrimaryWhite,
          modifier = Modifier.size(19.dp),
        )
      }

      // 7. Quick Scale Down (-) and Up (+)
      Row(
        modifier =
          Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF25272E))
            .border(1.dp, Color(0xFF343741), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(24.dp)
              .clip(RoundedCornerShape(6.dp))
              .clickable { onScaleStepClick(false) }
              .testTag("lasso_scale_down_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Scale Down",
            tint = TextPrimaryWhite,
            modifier = Modifier.size(14.dp),
          )
        }
        Text(
          text = "Size",
          color = TextSecondaryGray,
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium,
        )
        Box(
          modifier =
            Modifier
              .size(24.dp)
              .clip(RoundedCornerShape(6.dp))
              .clickable { onScaleStepClick(true) }
              .testTag("lasso_scale_up_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Scale Up",
            tint = TextPrimaryWhite,
            modifier = Modifier.size(14.dp),
          )
        }
      }

      // 8. Duplicate
      Box(
        modifier =
          Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onDuplicateClick)
            .testTag("lasso_duplicate_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.LibraryAdd,
          contentDescription = "Duplicate selection",
          tint = TextPrimaryWhite,
          modifier = Modifier.size(18.dp),
        )
      }

      if (onConvertAiTextClick != null) {
        Box(
          modifier =
            Modifier
              .height(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable(onClick = onConvertAiTextClick)
              .padding(horizontal = 6.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "AI",
            color = TextPrimaryWhite,
            fontSize = 13.5.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      // 10. Delete
      Box(
        modifier =
          Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DangerRed.copy(alpha = 0.14f))
            .clickable(onClick = onDeleteClick)
            .testTag("lasso_delete_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.DeleteOutline,
          contentDescription = "Delete selection",
          tint = DangerRed,
          modifier = Modifier.size(19.dp),
        )
      }

      // 11. Dismiss
      Box(
        modifier =
          Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onDismissLassoClick)
            .testTag("lasso_dismiss_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Dismiss selection",
          tint = TextSecondaryGray,
          modifier = Modifier.size(17.dp),
        )
      }
    }
  }
}
