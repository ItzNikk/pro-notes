package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeMode

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

private val ProNotesLightColorScheme =
  lightColorScheme(
    primary = Color(0xFF1B6EC2),
    onPrimary = Color.White,
    secondary = Color(0xFF285F76),
    onSecondary = Color.White,
    tertiary = Color(0xFF187BCD),
    background = Color(0xFFF5F6F9),
    onBackground = Color(0xFF16181D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16181D),
    surfaceVariant = Color(0xFFEBEEF4),
    onSurfaceVariant = Color(0xFF5A6070),
    error = DangerRed,
  )

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.DARK,
  content: @Composable () -> Unit,
) {
  val useDark =
    when (themeMode) {
      AppThemeMode.DARK -> true
      AppThemeMode.LIGHT -> false
      AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
  val proNotesColors = if (useDark) DarkProNotesColors else LightProNotesColors
  val colorScheme = if (useDark) ProNotesDarkColorScheme else ProNotesLightColorScheme

  CompositionLocalProvider(LocalProNotesColors provides proNotesColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content,
    )
  }
}
