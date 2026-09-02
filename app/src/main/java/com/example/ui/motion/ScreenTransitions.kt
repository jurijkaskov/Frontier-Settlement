package com.example.ui.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable

/**
 * Unified Navigation transitions for Frontier Settlement.
 * Ensures consistent screen traversal hierarchy across the application.
 */
object GameScreenTransitions {

    /**
     * Standard forward navigation enter transition (subtle slide right-to-left + fade).
     */
    fun enter(motion: FrontierGameMotion = FrontierGameMotion()): EnterTransition {
        if (motion.isReducedMotion) {
            return fadeIn(animationSpec = tween(0))
        }
        val duration = motion.duration(MotionDuration.STANDARD)
        return fadeIn(animationSpec = tween(duration, easing = motion.enterEasing)) +
                slideInHorizontally(
                    animationSpec = tween(duration, easing = motion.enterEasing),
                    initialOffsetX = { fullWidth -> fullWidth / 8 }
                )
    }

    /**
     * Standard forward navigation exit transition (subtle slide left + fade).
     */
    fun exit(motion: FrontierGameMotion = FrontierGameMotion()): ExitTransition {
        if (motion.isReducedMotion) {
            return fadeOut(animationSpec = tween(0))
        }
        val duration = motion.duration(MotionDuration.QUICK)
        return fadeOut(animationSpec = tween(duration, easing = motion.exitEasing)) +
                slideOutHorizontally(
                    animationSpec = tween(duration, easing = motion.exitEasing),
                    targetOffsetX = { fullWidth -> -fullWidth / 8 }
                )
    }

    /**
     * Back/Pop navigation enter transition (subtle slide left-to-right + fade).
     */
    fun popEnter(motion: FrontierGameMotion = FrontierGameMotion()): EnterTransition {
        if (motion.isReducedMotion) {
            return fadeIn(animationSpec = tween(0))
        }
        val duration = motion.duration(MotionDuration.STANDARD)
        return fadeIn(animationSpec = tween(duration, easing = motion.enterEasing)) +
                slideInHorizontally(
                    animationSpec = tween(duration, easing = motion.enterEasing),
                    initialOffsetX = { fullWidth -> -fullWidth / 8 }
                )
    }

    /**
     * Back/Pop navigation exit transition (subtle slide right + fade).
     */
    fun popExit(motion: FrontierGameMotion = FrontierGameMotion()): ExitTransition {
        if (motion.isReducedMotion) {
            return fadeOut(animationSpec = tween(0))
        }
        val duration = motion.duration(MotionDuration.QUICK)
        return fadeOut(animationSpec = tween(duration, easing = motion.exitEasing)) +
                slideOutHorizontally(
                    animationSpec = tween(duration, easing = motion.exitEasing),
                    targetOffsetX = { fullWidth -> fullWidth / 8 }
                )
    }

    /**
     * Cinematic scale/fade transition for Map <-> Settlement scale changes.
     */
    fun scaleFadeEnter(motion: FrontierGameMotion = FrontierGameMotion()): EnterTransition {
        if (motion.isReducedMotion) {
            return fadeIn(animationSpec = tween(0))
        }
        val duration = motion.duration(MotionDuration.STANDARD)
        return fadeIn(animationSpec = tween(duration, easing = motion.enterEasing)) +
                scaleIn(
                    animationSpec = tween(duration, easing = motion.enterEasing),
                    initialScale = 0.94f
                )
    }

    /**
     * Cinematic scale/fade exit transition.
     */
    fun scaleFadeExit(motion: FrontierGameMotion = FrontierGameMotion()): ExitTransition {
        if (motion.isReducedMotion) {
            return fadeOut(animationSpec = tween(0))
        }
        val duration = motion.duration(MotionDuration.QUICK)
        return fadeOut(animationSpec = tween(duration, easing = motion.exitEasing)) +
                scaleOut(
                    animationSpec = tween(duration, easing = motion.exitEasing),
                    targetScale = 0.94f
                )
    }
}
