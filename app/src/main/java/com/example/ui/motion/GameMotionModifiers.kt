package com.example.ui.motion

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GameTheme

/**
 * Reusable press animation providing tactile, grounded tactile feedback.
 * Smoothly scales down on press and restores on release.
 */
fun Modifier.pressFeedback(
    scaleDown: Float = 0.97f,
    isEnabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val motion = GameTheme.motion

    val scale by animateFloatAsState(
        targetValue = if (isEnabled && isPressed && !motion.isReducedMotion) scaleDown else 1.0f,
        animationSpec = motion.quickTween(),
        label = "press_scale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Single-shot pulse modifier to draw attention to newly selected, unlocked, or updated elements.
 * Runs once upon trigger change, never looping continuously.
 */
fun Modifier.pulseOnce(
    trigger: Any?,
    pulseScale: Float = 1.04f
): Modifier = composed {
    val motion = GameTheme.motion
    val scaleAnim = remember { Animatable(1.0f) }

    LaunchedEffect(trigger) {
        if (trigger != null && !motion.isReducedMotion) {
            scaleAnim.animateTo(
                targetValue = pulseScale,
                animationSpec = tween(motion.duration(MotionDuration.QUICK), easing = motion.emphasisEasing)
            )
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(motion.duration(MotionDuration.QUICK), easing = motion.standardEasing)
            )
        } else {
            scaleAnim.snapTo(1.0f)
        }
    }

    this.graphicsLayer {
        scaleX = scaleAnim.value
        scaleY = scaleAnim.value
    }
}

/**
 * Staggered entrance animation for lists (e.g. event choices, loot items, return summary).
 * Applies light offset and fade-in for the first few items without excessive delay.
 */
fun Modifier.staggeredEnter(
    index: Int,
    baseDelayMs: Int = 35,
    maxStaggerIndex: Int = 4
): Modifier = composed {
    val motion = GameTheme.motion
    if (motion.isReducedMotion || index > maxStaggerIndex) {
        return@composed this
    }

    val alphaAnim = remember { Animatable(0.0f) }
    val slideAnim = remember { Animatable(8.0f) }

    LaunchedEffect(Unit) {
        val delay = (index * baseDelayMs * motion.speedMultiplier).toLong()
        if (delay > 0) {
            kotlinx.coroutines.delay(delay)
        }
        alphaAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(motion.duration(MotionDuration.STANDARD), easing = motion.enterEasing)
        )
    }

    LaunchedEffect(Unit) {
        val delay = (index * baseDelayMs * motion.speedMultiplier).toLong()
        if (delay > 0) {
            kotlinx.coroutines.delay(delay)
        }
        slideAnim.animateTo(
            targetValue = 0.0f,
            animationSpec = tween(motion.duration(MotionDuration.STANDARD), easing = motion.enterEasing)
        )
    }

    this.graphicsLayer {
        alpha = alphaAnim.value
        translationY = slideAnim.value
    }
}

/**
 * Animated border highlight for selectable cards.
 */
fun Modifier.animatedSelectionBorder(
    isSelected: Boolean,
    selectedColor: Color,
    normalColor: Color,
    borderWidth: Dp = 1.5.dp,
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier = composed {
    val motion = GameTheme.motion
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else normalColor,
        animationSpec = motion.quickTween(),
        label = "selection_border_color"
    )

    this.border(borderWidth, animatedColor, shape)
}
