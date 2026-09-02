package com.example.ui.motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard duration tokens for the Frontier Settlement design system.
 * Prevents magic duration numbers across the codebase.
 */
object MotionDuration {
    const val Instant: Int = 80
    const val Quick: Int = 150
    const val Standard: Int = 260
    const val Emphasis: Int = 420
    const val Ambient: Int = 2400

    const val INSTANT: Int = Instant
    const val QUICK: Int = Quick
    const val STANDARD: Int = Standard
    const val EMPHASIS: Int = Emphasis
    const val AMBIENT: Int = Ambient
}

/**
 * Curated easing curves for tactile, grounded motion.
 */
object MotionEasing {
    val Standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f) // Fast out, slow in
    val Enter: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)    // Decelerate
    val Exit: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)     // Accelerate
    val Emphasis: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f) // Distinctive settle
}

/**
 * Motion scale debug & accessibility configuration.
 */
enum class MotionSpeed(val multiplier: Float, val label: String) {
    REDUCED_MOTION(0.0f, "0x (Reduced Motion)"),
    INSTANT(0.0f, "0x (Мгновенно)"),
    FAST(0.5f, "0.5x (Быстро)"),
    NORMAL(1.0f, "1.0x (Стандарт)"),
    SLOW(2.0f, "2.0x (Замедленно)");

    companion object {
        fun fromMultiplier(multiplier: Float): MotionSpeed {
            return entries.find { kotlin.math.abs(it.multiplier - multiplier) < 0.05f } ?: NORMAL
        }
    }
}

/**
 * Centralized motion specifications for the entire game.
 */
@Immutable
data class FrontierGameMotion(
    val instantDuration: Int = MotionDuration.INSTANT,
    val quickDuration: Int = MotionDuration.QUICK,
    val standardDuration: Int = MotionDuration.STANDARD,
    val emphasisDuration: Int = MotionDuration.EMPHASIS,
    val ambientDuration: Int = MotionDuration.AMBIENT,
    val standardEasing: Easing = MotionEasing.Standard,
    val enterEasing: Easing = MotionEasing.Enter,
    val exitEasing: Easing = MotionEasing.Exit,
    val emphasisEasing: Easing = MotionEasing.Emphasis,
    val isReducedMotion: Boolean = false,
    val speedMultiplier: Float = 1.0f,
    val cardPressScale: Float = 0.98f,
    val buttonPressScale: Float = 0.96f,
    val selectionScale: Float = 1.02f
) {
    constructor(speed: MotionSpeed) : this(
        speedMultiplier = speed.multiplier,
        isReducedMotion = speed == MotionSpeed.REDUCED_MOTION || speed == MotionSpeed.INSTANT
    )

    /**
     * Resolves an effective duration applying reduced motion and speed multipliers.
     */
    fun duration(baseDuration: Int): Int {
        if (isReducedMotion) return 0
        return (baseDuration * speedMultiplier).toInt().coerceAtLeast(0)
    }

    val instant: Int get() = duration(instantDuration)
    val quick: Int get() = duration(quickDuration)
    val standard: Int get() = duration(standardDuration)
    val emphasis: Int get() = duration(emphasisDuration)
    val ambient: Int get() = duration(ambientDuration)

    fun <T> quickTween(): TweenSpec<T> = tween(duration(quickDuration), easing = standardEasing)
    fun <T> standardTween(): TweenSpec<T> = tween(duration(standardDuration), easing = standardEasing)
    fun <T> emphasisTween(): TweenSpec<T> = tween(duration(emphasisDuration), easing = emphasisEasing)
    fun <T> enterTween(): TweenSpec<T> = tween(duration(standardDuration), easing = enterEasing)
    fun <T> exitTween(): TweenSpec<T> = tween(duration(quickDuration), easing = exitEasing)

    fun <T> spec(baseDuration: Int, easing: Easing = standardEasing): TweenSpec<T> {
        return tween(duration(baseDuration), easing = easing)
    }
}
