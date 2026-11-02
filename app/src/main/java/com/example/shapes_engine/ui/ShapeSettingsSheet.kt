package shapes_engine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import shapes_engine.model.ShapeStyle
import shapes_engine.model.ShapeType
import shapes_engine.persistence.ShapeSettingsRepository

/**
 * Material 3 On-Demand Shape Settings Bottom Sheet / Dialog.
 *
 * Replaces the persistent floating docked bottom bar.
 * Provides:
 *  - Instant fill toggle (Boolean)
 *  - Fill opacity slider (0% to 100% with live percentage text)
 *  - Live geometric preview box ([ShapeStylePreviewBox])
 *  - Quick color swatch selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeSettingsSheet(
    repository: ShapeSettingsRepository,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style by repository.styleFlow.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xFF181A20),
        contentColor = Color.White,
        modifier = modifier
    ) {
        ShapeSettingsContent(
            style = style,
            onFillEnabledChanged = { repository.setFillEnabled(it) },
            onFillOpacityChanged = { repository.setFillOpacity(it) },
            onColorSelected = { repository.setStrokeColor(it) },
            onClose = onDismissRequest
        )
    }
}

/**
 * Embedded content version of shape settings, usable inside bottom sheets, dialogs, or popovers.
 */
@Composable
fun ShapeSettingsContent(
    style: ShapeStyle,
    onFillEnabledChanged: (Boolean) -> Unit,
    onFillOpacityChanged: (Float) -> Unit,
    onColorSelected: (Long) -> Unit = {},
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertAlignment
        ) {
            Text(
                text = "Shape Style Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF9E9E9E)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Preview Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertAlignment,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShapeStylePreviewBox(
                style = style,
                previewShape = ShapeType.CIRCLE,
                size = 72.dp
            )
            ShapeStylePreviewBox(
                style = style,
                previewShape = ShapeType.RECTANGLE,
                size = 72.dp
            )
            ShapeStylePreviewBox(
                style = style,
                previewShape = ShapeType.HEART,
                size = 72.dp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fill Toggle Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF22252D))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertAlignment
        ) {
            Column {
                Text(
                    text = "Fill Shape Interior",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = if (style.fillEnabled) "Enabled (auto-fills closed shapes)" else "Disabled (outline only)",
                    fontSize = 12.sp,
                    color = Color(0xFFAAAAAA)
                )
            }
            Switch(
                checked = style.fillEnabled,
                onCheckedChange = onFillEnabledChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF3B82F6),
                    uncheckedThumbColor = Color(0xFF888888),
                    uncheckedTrackColor = Color(0xFF333333)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fill Opacity Slider Row (interactive when fillEnabled is true)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF22252D))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertAlignment
            ) {
                Text(
                    text = "Fill Opacity",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if (style.fillEnabled) Color.White else Color(0xFF777777)
                )
                Text(
                    text = style.opacityPercentageText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (style.fillEnabled) Color(0xFF60A5FA) else Color(0xFF777777)
                )
            }

            Slider(
                value = style.fillOpacity,
                onValueChange = onFillOpacityChanged,
                valueRange = 0.0f..1.0f,
                enabled = style.fillEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF3B82F6),
                    activeTrackColor = Color(0xFF3B82F6),
                    inactiveTrackColor = Color(0xFF444751)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color Presets Row
        Text(
            text = "Color Palette",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFFAAAAAA)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val palette = listOf(
                0xFF000000L, // Black
                0xFFEF4444L, // Red
                0xFF3B82F6L, // Blue
                0xFF10B981L, // Green
                0xFFF59E0BL, // Amber
                0xFF8B5CF6L, // Purple
                0xFFEC4899L  // Pink
            )
            palette.forEach { colorArgb ->
                val isSelected = style.strokeColorArgb == colorArgb
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(colorArgb))
                        .clickable { onColorSelected(colorArgb) }
                        .padding(if (isSelected) 3.dp else 0.dp)
                ) {
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                            modifier = Modifier.matchParentSize()
                        ) {}
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
