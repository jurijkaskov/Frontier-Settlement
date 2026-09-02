package com.example.ui.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Animated Combat HP Bar with smooth interpolation.
 */
@Composable
fun CombatHpBar(
    currentHp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    val motion = GameTheme.motion
    val fraction = if (maxHp > 0) (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = motion.standardTween(),
        label = "combat_hp_fraction"
    )

    val barColor = when {
        fraction <= 0.25f -> DangerRed
        fraction <= 0.5f -> WarningAmber
        else -> SafeEmerald
    }

    val animatedBarColor by animateColorAsState(
        targetValue = barColor,
        animationSpec = motion.quickTween(),
        label = "combat_hp_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(GameTheme.colors.surfaceHighlight)
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

/**
 * Animated Action Points (AP) pips for turn-based combat.
 */
@Composable
fun CombatApPips(
    currentAp: Int,
    maxAp: Int,
    modifier: Modifier = Modifier,
    pipSize: Dp = 8.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until maxAp) {
            val isFilled = i < currentAp
            val animatedAlpha by animateFloatAsState(
                targetValue = if (isFilled) 1.0f else 0.25f,
                animationSpec = tween(MotionDuration.QUICK),
                label = "ap_pip_alpha"
            )

            Box(
                modifier = Modifier
                    .size(pipSize)
                    .clip(CircleShape)
                    .background(CreditsYellow.copy(alpha = animatedAlpha))
                    .border(
                        1.dp,
                        if (isFilled) CreditsYellow else GameTheme.colors.border,
                        CircleShape
                    )
            )
        }
    }
}

/**
 * Floating damage or heal popups in combat.
 */
@Composable
fun CombatFloatingDelta(
    delta: Int,
    isHeal: Boolean = delta > 0,
    onFinished: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val motion = GameTheme.motion
    val color = if (isHeal) SafeEmerald else DangerRed
    val text = if (isHeal) "+$delta" else "-${kotlin.math.abs(delta)}"

    val offsetY = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1f) }

    LaunchedEffect(delta) {
        if (!motion.isReducedMotion) {
            offsetY.animateTo(
                targetValue = -24f,
                animationSpec = tween(motion.duration(MotionDuration.EMPHASIS), easing = motion.enterEasing)
            )
            alphaAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(motion.duration(MotionDuration.QUICK), easing = motion.exitEasing)
            )
        }
        onFinished()
    }

    Text(
        text = text,
        style = GameTheme.typography.screenTitle.copy(
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        ),
        modifier = modifier.graphicsLayer {
            translationY = offsetY.value
            alpha = alphaAnim.value
        }
    )
}

/**
 * Target Selection Reticle indicator for targeting combatants.
 */
@Composable
fun CombatTargetReticle(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isSelected) return

    val motion = GameTheme.motion
    val scaleAnim = remember { Animatable(1.08f) }

    LaunchedEffect(isSelected) {
        if (!motion.isReducedMotion) {
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(motion.duration(MotionDuration.QUICK), easing = motion.emphasisEasing)
            )
        } else {
            scaleAnim.snapTo(1.0f)
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            }
            .border(2.dp, FrontierPrimary, RoundedCornerShape(12.dp))
    )
}

/**
 * Combat Outcome Result Banner (Victory / Defeat).
 */
@Composable
fun CombatOutcomeOverlay(
    isVictory: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motion = GameTheme.motion
    val accentColor = if (isVictory) SafeEmerald else DangerRed
    val title = if (isVictory) "ПОБЕДА!" else "ОТРЯД РАЗБИТ"
    val icon = if (isVictory) Icons.Default.CheckCircle else Icons.Default.Dangerous

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = GameTheme.colors.surfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, accentColor),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.5.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    style = GameTheme.typography.screenTitle.copy(
                        color = Color.White,
                        fontSize = 20.sp,
                        letterSpacing = 1.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isVictory) "Сектор зачищен. Трофеи готовы к погрузке." else "Экспедиция понесла тяжелые потери и отступает.",
                    style = GameTheme.typography.bodySecondary.copy(
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isVictory) "Собрать трофеи" else "Вернуться на базу",
                        style = GameTheme.typography.buttonText.copy(color = Color.Black)
                    )
                }
            }
        }
    }
}
