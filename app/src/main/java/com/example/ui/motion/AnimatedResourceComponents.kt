package com.example.ui.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ResourceType
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GameTheme
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay

/**
 * Coalescing container that accumulates rapid resource changes into a single summarized delta.
 */
class ResourceDeltaCoalescer {
    private val deltas = mutableMapOf<ResourceType, Int>()

    var accumulatedDelta: Int = 0
        private set

    fun processUpdate(oldVal: Int, newVal: Int): Int {
        val diff = newVal - oldVal
        accumulatedDelta += diff
        return accumulatedDelta
    }

    fun reset() {
        accumulatedDelta = 0
        deltas.clear()
    }

    fun addDelta(resourceType: ResourceType, amount: Int, now: Long = System.currentTimeMillis()): Int {
        val current = deltas.getOrDefault(resourceType, 0)
        val combined = current + amount
        deltas[resourceType] = combined
        return combined
    }

    fun getDelta(resourceType: ResourceType): Int {
        return deltas.getOrDefault(resourceType, 0)
    }

    fun clear(resourceType: ResourceType) {
        deltas.remove(resourceType)
    }

    fun clearAll() {
        deltas.clear()
    }
}

/**
 * Smooth numeric counter for resource values with optional gain/loss highlight.
 */
@Composable
fun AnimatedResourceValue(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle? = null,
    showDeltaHighlight: Boolean = true
) {
    val motion = GameTheme.motion
    val dangerColor = GameTheme.colors.danger
    val textStyle = style ?: GameTheme.typography.statValue
    var previousValue by remember { mutableIntStateOf(targetValue) }
    var highlightColor by remember { mutableStateOf<Color?>(null) }

    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = motion.standardTween(),
        label = "animated_resource_value"
    )

    LaunchedEffect(targetValue) {
        if (targetValue != previousValue && showDeltaHighlight && !motion.isReducedMotion) {
            highlightColor = if (targetValue > previousValue) SafeEmerald else dangerColor
            delay(motion.duration(MotionDuration.STANDARD).toLong())
            highlightColor = null
            previousValue = targetValue
        } else {
            previousValue = targetValue
        }
    }

    val textColor = highlightColor ?: textStyle.color

    Text(
        text = "$animatedValue",
        style = textStyle.copy(color = textColor),
        modifier = modifier
    )
}

/**
 * Floating / inline delta badge displaying "+N" or "-N" when a resource changes.
 */
@Composable
fun ResourceDeltaBadge(
    delta: Int,
    onDismiss: () -> Unit = {},
    durationMs: Long = 1400L,
    modifier: Modifier = Modifier
) {
    if (delta == 0) return

    val isPositive = delta > 0
    val text = if (isPositive) "+$delta" else "$delta"
    val color = if (isPositive) SafeEmerald else DangerRed

    var isVisible by remember(delta) { mutableStateOf(true) }

    LaunchedEffect(delta) {
        isVisible = true
        delay(durationMs)
        isVisible = false
        onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = GameTheme.motion.quickTween()) +
                scaleIn(initialScale = 0.8f, animationSpec = GameTheme.motion.quickTween()),
        exit = fadeOut(animationSpec = GameTheme.motion.quickTween()) +
                scaleOut(targetScale = 0.8f, animationSpec = GameTheme.motion.quickTween()),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = text,
                style = GameTheme.typography.caption.copy(
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Animated Capacity Meter Bar (e.g. Warehouse or Vehicle Cargo).
 * Features smooth progress interpolation and subtle warning state on overload (>90%).
 */
@Composable
fun AnimatedCapacityBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    warningThreshold: Float = 0.90f
) {
    val motion = GameTheme.motion
    val fraction = if (max > 0) (current.toFloat() / max.toFloat()).coerceIn(0f, 1.2f) else 0f
    val isOverloaded = fraction >= warningThreshold

    val animatedFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = motion.standardTween(),
        label = "capacity_bar_progress"
    )

    val targetBarColor = when {
        fraction >= 1.0f -> DangerRed
        fraction >= warningThreshold -> WarningAmber
        else -> GameTheme.colors.primary
    }

    val animatedBarColor by animateColorAsState(
        targetValue = targetBarColor,
        animationSpec = motion.quickTween(),
        label = "capacity_bar_color"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(GameTheme.colors.surfaceHighlight)
                .then(
                    if (isOverloaded && !motion.isReducedMotion) {
                        Modifier.border(1.dp, targetBarColor.copy(alpha = 0.6f), RoundedCornerShape(height / 2))
                    } else Modifier
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(height / 2))
                    .background(animatedBarColor)
            )
        }
    }
}
