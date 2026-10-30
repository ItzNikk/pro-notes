package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ProNotesDarkColorScheme =
  darkColorScheme(
    primary = NewButtonBlue,
    onPrimary = TextPrimaryWhite,
    secondary = ActiveToolPill,
    onSecondary = ActiveToolIconDark,
    tertiary = SubToolbarActiveCyan,
    background = DocumentsMainBg,
    onBackground = TextPrimaryWhite,
    surface = SidebarDarkBg,
    onSurface = TextPrimaryWhite,
    surfaceVariant = SubToolbarPillBg,
    onSurfaceVariant = TextSecondaryGray,
    error = DangerRed,
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ProNotesDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
