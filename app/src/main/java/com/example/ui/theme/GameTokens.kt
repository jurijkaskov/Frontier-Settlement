package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.motion.FrontierGameMotion

/**
 * Frontier Settlement Design Tokens
 * Grounded, tactical post-collapse survival strategy visual system.
 */

// ============================================================================
// 1. COLORS
// ============================================================================

data class FrontierGameColors(
    // Backgrounds
    val background: Color = Color(0xFF090D14),
    val backgroundAlt: Color = Color(0xFF06090F),
    val backgroundModal: Color = Color(0xFF0D131D),

    // Surfaces
    val surface: Color = Color(0xFF111722),
    val surfaceElevated: Color = Color(0xFF182232),
    val surfaceHighlight: Color = Color(0xFF222F44),
    val surfaceSelected: Color = Color(0xFF162B28),

    // Borders & Dividers
    val border: Color = Color(0xFF243247),
    val borderLight: Color = Color(0xFF3B4F6E),
    val borderFocus: Color = Color(0xFF10B981),
    val borderWarning: Color = Color(0xFFF59E0B),
    val borderDanger: Color = Color(0xFFEF4444),

    // Primary & Accent
    val primary: Color = Color(0xFF10B981), // Safe Emerald
    val onPrimary: Color = Color(0xFF022C22),
    val primaryContainer: Color = Color(0xFF064E3B),
    val onPrimaryContainer: Color = Color(0xFFA7F3D0),

    val secondary: Color = Color(0xFF38BDF8), // Tech Cyan
    val onSecondary: Color = Color(0xFF082F49),
    val secondaryContainer: Color = Color(0xFF075985),
    val onSecondaryContainer: Color = Color(0xFFBAE6FD),

    val accentWarm: Color = Color(0xFFF59E0B), // Warm Amber
    val accentOrange: Color = Color(0xFFFB923C), // Wasteland Orange
    val accentMilitary: Color = Color(0xFFE11D48), // Tactical Crimson

    // Feedback & Semantic Status
    val success: Color = Color(0xFF10B981),
    val successContainer: Color = Color(0xFF064E3B),
    val warning: Color = Color(0xFFF59E0B),
    val warningContainer: Color = Color(0xFF451A03),
    val danger: Color = Color(0xFFEF4444),
    val dangerContainer: Color = Color(0xFF450A0A),
    val info: Color = Color(0xFF38BDF8),
    val infoContainer: Color = Color(0xFF0C4A6E),
    val disabled: Color = Color(0xFF334155),
    val disabledContent: Color = Color(0xFF64748B),

    // Typography Colors
    val textPrimary: Color = Color(0xFFF8FAFC),
    val textSecondary: Color = Color(0xFFCBD5E1),
    val textMuted: Color = Color(0xFF94A3B8),
    val textSubtle: Color = Color(0xFF64748B),
    val textDisabled: Color = Color(0xFF475569),

    // Resources
    val resFood: Color = Color(0xFF22C55E),
    val resWater: Color = Color(0xFF06B6D4),
    val resFuel: Color = Color(0xFFF97316),
    val resMaterials: Color = Color(0xFFFB923C),
    val resMedicine: Color = Color(0xFF14B8A6),
    val resAmmo: Color = Color(0xFFE11D48),
    val resComponents: Color = Color(0xFF818CF8),
    val resRareAlloy: Color = Color(0xFFA855F7),
    val resCredits: Color = Color(0xFFFACC15),
    val resStorage: Color = Color(0xFFA855F7),

    // Threat / Danger Levels
    val dangerSafe: Color = Color(0xFF10B981),
    val dangerLow: Color = Color(0xFF38BDF8),
    val dangerModerate: Color = Color(0xFFF59E0B),
    val dangerHigh: Color = Color(0xFFEF4444),
    val dangerExtreme: Color = Color(0xFFE11D48),
    val dangerUnknown: Color = Color(0xFFA855F7)
)

// ============================================================================
// 2. SPACING & SIZING
// ============================================================================

data class FrontierGameSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,

    // Layout margins
    val screenHorizontal: Dp = 16.dp,
    val screenVertical: Dp = 12.dp,
    val cardPadding: Dp = 14.dp,
    val compactCardPadding: Dp = 10.dp,
    val sectionSpacing: Dp = 16.dp,
    val itemSpacing: Dp = 8.dp,
    val touchTargetMin: Dp = 48.dp,
    val maxContentWidth: Dp = 640.dp
)

// ============================================================================
// 3. SHAPES & CORNER RADIUS
// ============================================================================

data class FrontierGameShapes(
    val badge: RoundedCornerShape = RoundedCornerShape(4.dp),
    val small: RoundedCornerShape = RoundedCornerShape(8.dp),
    val medium: RoundedCornerShape = RoundedCornerShape(12.dp),
    val card: RoundedCornerShape = RoundedCornerShape(14.dp),
    val large: RoundedCornerShape = RoundedCornerShape(18.dp),
    val modal: RoundedCornerShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    val full: RoundedCornerShape = RoundedCornerShape(999.dp)
)

// ============================================================================
// 4. TYPOGRAPHY
// ============================================================================

data class FrontierGameTypography(
    // Headings
    val gameTitle: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.5.sp,
        color = Color(0xFFF8FAFC)
    ),
    val screenTitle: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.25.sp,
        color = Color(0xFFF8FAFC)
    ),
    val sectionTitle: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp,
        color = Color(0xFFF8FAFC)
    ),
    val cardTitle: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.15.sp,
        color = Color(0xFFF8FAFC)
    ),

    // Body
    val body: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
        color = Color(0xFFCBD5E1)
    ),
    val bodySecondary: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.15.sp,
        color = Color(0xFF94A3B8)
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.1.sp,
        color = Color(0xFF64748B)
    ),

    // Numbers & Tactical Values
    val numericHero: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp,
        color = Color(0xFFF8FAFC)
    ),
    val numericValue: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
        color = Color(0xFFF8FAFC)
    ),
    val numericSecondary: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.1.sp,
        color = Color(0xFFCBD5E1)
    ),

    // Buttons & Badges
    val buttonText: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    val buttonTextSmall: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp
    ),
    val badgeText: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.2.sp
    ),
    val statValue: TextStyle = numericValue,
    val valueText: TextStyle = numericValue
)

// ============================================================================
// 5. MOTION & ELEVATION
// ============================================================================

data class FrontierGameElevation(
    val flat: Dp = 0.dp,
    val card: Dp = 2.dp,
    val elevated: Dp = 6.dp,
    val modal: Dp = 12.dp,
    val hud: Dp = 8.dp
)

// ============================================================================
// 6. COMPOSITION LOCALS & GAME THEME ACCESSOR
// ============================================================================

val LocalFrontierColors = staticCompositionLocalOf { FrontierGameColors() }
val LocalFrontierSpacing = staticCompositionLocalOf { FrontierGameSpacing() }
val LocalFrontierShapes = staticCompositionLocalOf { FrontierGameShapes() }
val LocalFrontierTypography = staticCompositionLocalOf { FrontierGameTypography() }
val LocalFrontierMotion = staticCompositionLocalOf { FrontierGameMotion() }
val LocalFrontierElevation = staticCompositionLocalOf { FrontierGameElevation() }

object GameTheme {
    val colors: FrontierGameColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFrontierColors.current

    val spacing: FrontierGameSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalFrontierSpacing.current

    val shapes: FrontierGameShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalFrontierShapes.current

    val typography: FrontierGameTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalFrontierTypography.current

    val motion: FrontierGameMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalFrontierMotion.current

    val elevation: FrontierGameElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalFrontierElevation.current
}
