package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core Pro Notes Dark UI Palette (Default constants preserved for backward compatibility)
val TopTabBarBg = Color(0xFF0E1015)
val ActiveTabBg = Color(0xFF1B2B4B)
val InactiveTabBg = Color(0xFF14171F)
val ToolbarNavy = Color(0xFF172642)
val ActiveToolPill = Color(0xFF9CD3E8)
val ActiveToolIconDark = Color(0xFF0E1A2B)
val ToolbarIconLight = Color(0xFFD6E2F0)

val SubToolbarPillBg = Color(0xFF22242A)
val SubToolbarBorder = Color(0xFF333640)
val SubToolbarActiveTeal = Color(0xFF24576E)
val SubToolbarActiveCyan = Color(0xFF67C8EB)

val CanvasWorkspaceBg = Color(0xFF18191D)
val PageWhite = Color(0xFFFFFFFF)

// Documents Library Palette
val SidebarDarkBg = Color(0xFF141519)
val SidebarActiveBluePill = Color(0xFF19355E)
val SidebarActiveBlueIcon = Color(0xFF4C95F8)
val DocumentsMainBg = Color(0xFF1C1E23)
val NewButtonBlue = Color(0xFF1E74D4)
val SortPillBg = Color(0xFF282A30)

// Popover & Surface Palette
val PopoverBg = Color(0xFF1D1F24)
val PopoverInnerBg = Color(0xFF131519)
val PopoverCardSelected = Color(0xFF2B2E36)
val PopoverDivider = Color(0xFF2E313A)
val TextPrimaryWhite = Color(0xFFF2F3F7)
val TextSecondaryGray = Color(0xFF9498A3)
val TextMutedGray = Color(0xFF686C77)
val DangerRed = Color(0xFFE5534B)
val SyncOffRed = Color(0xFFD94A4A)
val SwitchBlue = Color(0xFF2684FF)

// Notebook Cover Colors
val KraftTanCover = Color(0xFFA68F78)
val KraftSpineLight = Color(0xFFEDEAE4)
val CharcoalCover = Color(0xFF2B2D30)
val CharcoalCoverAccent = Color(0xFF1A4B8C)
val GridPaperBg = Color(0xFFF3F3F0)
val WhiteboardBg = Color(0xFFF5F6F8)

val CanvasOuterGray = CanvasWorkspaceBg
val BookCoverKraftTan = KraftTanCover
val BookCoverSpineKraft = KraftSpineLight
val BookCoverCharcoal = CharcoalCover
val BookCoverSpineDark = Color(0xFF1F2023)
val BookCoverRibbonBlue = CharcoalCoverAccent
val BookCoverGridPaper = GridPaperBg
val BookCoverGridLine = Color(0xFFD5D7DC)
val BookCoverWhiteboard = WhiteboardBg

// Quick Toolbar Swatches
val SwatchRoyalBlue = Color(0xFF1E6AE1)
val SwatchWhite = Color(0xFFFFFFFF)
val SwatchPurple = Color(0xFF9C27B0)
val SwatchNeonGreen = Color(0xFF64DD17)
val SwatchMagenta = Color(0xFFFF007F)

val PenColorGridPalette =
  listOf(
    Color(0xFF0A0A0C),
    Color(0xFF3A3A3C),
    Color(0xFF6C6C70),
    Color(0xFFB0B0B5),
    Color(0xFFFFFFFF),
    Color(0xFFFF1744),
    Color(0xFFD50000),
    Color(0xFFFF5252),
    Color(0xFFFFAB91),
    Color(0xFFFFC400),
    Color(0xFF00B0FF),
    Color(0xFF1E6AE1),
    Color(0xFF00C853),
    Color(0xFFAEEA00),
    Color(0xFFFFF59D),
    Color(0xFF000000),
  )

@Immutable
data class ProNotesColors(
  val isDark: Boolean,
  val backgroundPrimary: Color,
  val backgroundSecondary: Color,
  val surfaceElevated: Color,
  val surfaceCard: Color,
  val surfacePopover: Color,
  val surfaceInput: Color,
  val toolbarPrimary: Color,
  val toolbarSecondary: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val textMuted: Color,
  val accentPrimary: Color,
  val accentSubtle: Color,
  val accentCyan: Color,
  val borderSubtle: Color,
  val divider: Color,
  val danger: Color,
  val canvasSurround: Color,
) {
  val background: Color
    get() = backgroundPrimary
  val surface: Color
    get() = surfaceElevated
  val surfaceVariant: Color
    get() = surfaceCard
  val popoverBg: Color
    get() = surfacePopover
  val sidebarBg: Color
    get() = backgroundSecondary
  val sidebarSelectedBg: Color
    get() = accentSubtle
  val accentBlue: Color
    get() = accentPrimary
  val border: Color
    get() = borderSubtle
  val canvasViewportBg: Color
    get() = canvasSurround
}

val DarkProNotesColors =
  ProNotesColors(
    isDark = true,
    backgroundPrimary = DocumentsMainBg,
    backgroundSecondary = SidebarDarkBg,
    surfaceElevated = PopoverBg,
    surfaceCard = Color(0xFF25272E),
    surfacePopover = PopoverBg,
    surfaceInput = PopoverInnerBg,
    toolbarPrimary = ToolbarNavy,
    toolbarSecondary = SubToolbarPillBg,
    textPrimary = TextPrimaryWhite,
    textSecondary = TextSecondaryGray,
    textMuted = TextMutedGray,
    accentPrimary = SwitchBlue,
    accentSubtle = SidebarActiveBluePill,
    accentCyan = SubToolbarActiveCyan,
    borderSubtle = SubToolbarBorder,
    divider = PopoverDivider,
    danger = DangerRed,
    canvasSurround = CanvasWorkspaceBg,
  )

val LightProNotesColors =
  ProNotesColors(
    isDark = false,
    backgroundPrimary = Color(0xFFF5F6F9),
    backgroundSecondary = Color(0xFFEBEEF4),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceCard = Color(0xFFFFFFFF),
    surfacePopover = Color(0xFFFFFFFF),
    surfaceInput = Color(0xFFF0F2F7),
    toolbarPrimary = Color(0xFF1A2D4C),
    toolbarSecondary = Color(0xFF24385A),
    textPrimary = Color(0xFF16181D),
    textSecondary = Color(0xFF5A6070),
    textMuted = Color(0xFF858B99),
    accentPrimary = Color(0xFF1B6EC2),
    accentSubtle = Color(0xFFDCEAFB),
    accentCyan = Color(0xFF187BCD),
    borderSubtle = Color(0xFFDADEE8),
    divider = Color(0xFFE3E6EF),
    danger = Color(0xFFD93831),
    canvasSurround = Color(0xFFE2E5EC),
  )

val LocalProNotesColors = staticCompositionLocalOf { DarkProNotesColors }
