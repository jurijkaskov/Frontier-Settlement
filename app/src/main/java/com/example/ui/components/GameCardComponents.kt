package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DangerLevel
import com.example.ui.motion.pressFeedback
import com.example.ui.theme.*

/**
 * Standard tactical card with subtle border and grounded dark surface.
 */
@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GameTheme.colors.border,
    backgroundColor: Color = GameTheme.colors.surface,
    shapeRadius: Dp = 14.dp,
    borderWidth: Dp = 1.dp,
    padding: PaddingValues = PaddingValues(14.dp),
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(shapeRadius)
    Surface(
        color = backgroundColor,
        shape = shape,
        border = BorderStroke(borderWidth, borderColor),
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .clip(shape)
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}

/**
 * Interactive card with press feedback, selected outline, and hover/active states.
 */
@Composable
fun InteractiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    selectedBorderColor: Color = GameTheme.colors.primary,
    normalBorderColor: Color = GameTheme.colors.border,
    selectedBackgroundColor: Color = GameTheme.colors.surfaceSelected,
    normalBackgroundColor: Color = GameTheme.colors.surface,
    shapeRadius: Dp = 14.dp,
    padding: PaddingValues = PaddingValues(14.dp),
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            !isEnabled -> GameTheme.colors.disabled
            isSelected -> selectedBorderColor
            isPressed -> GameTheme.colors.borderLight
            else -> normalBorderColor
        },
        label = "interactive_card_border"
    )

    val animatedBgColor by animateColorAsState(
        targetValue = when {
            !isEnabled -> GameTheme.colors.surface.copy(alpha = 0.5f)
            isSelected -> selectedBackgroundColor
            else -> normalBackgroundColor
        },
        label = "interactive_card_bg"
    )

    val shape = RoundedCornerShape(shapeRadius)

    Surface(
        color = animatedBgColor,
        shape = shape,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, animatedBorderColor),
        modifier = modifier
            .pressFeedback(scaleDown = 0.98f, isEnabled = isEnabled, interactionSource = interactionSource)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clip(shape)
            .then(
                if (isEnabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}

/**
 * Compact informational card for dense lists and grids.
 */
@Composable
fun CompactCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GameTheme.colors.border,
    backgroundColor: Color = GameTheme.colors.surfaceElevated,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Surface(
        color = backgroundColor,
        shape = shape,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .clip(shape)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Tactical Warning Card for deficits, hazard zones, or low supplies.
 */
@Composable
fun WarningCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    isCritical: Boolean = false,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val accentColor = if (isCritical) GameTheme.colors.danger else GameTheme.colors.warning
    val containerBg = if (isCritical) GameTheme.colors.dangerContainer.copy(alpha = 0.4f) else GameTheme.colors.warningContainer.copy(alpha = 0.4f)

    Surface(
        color = containerBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.7f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Предупреждение",
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = GameTheme.typography.cardTitle.copy(color = accentColor)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = GameTheme.typography.bodySecondary.copy(color = GameTheme.colors.textSecondary)
                )
            }
            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onActionClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = actionText,
                        style = GameTheme.typography.buttonTextSmall.copy(color = accentColor)
                    )
                }
            }
        }
    }
}

/**
 * Unified Section Header with vertical accent pill and optional action text.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    accentColor: Color = GameTheme.colors.primary,
    counterText: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = GameTheme.typography.sectionTitle
            )
            if (counterText != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = GameTheme.colors.surfaceHighlight,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, GameTheme.colors.borderLight)
                ) {
                    Text(
                        text = counterText,
                        style = GameTheme.typography.badgeText.copy(
                            color = GameTheme.colors.textMuted,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = actionText,
                    style = GameTheme.typography.buttonTextSmall.copy(
                        color = GameTheme.colors.secondary
                    )
                )
            }
        }
    }
}

/**
 * Danger Badge mapping DangerLevel to consistent semantic colors and tags.
 */
@Composable
fun DangerBadge(
    dangerLevel: DangerLevel,
    modifier: Modifier = Modifier
) {
    val (color, containerColor) = when (dangerLevel) {
        DangerLevel.SAFE -> GameTheme.colors.dangerSafe to Color(0xFF064E3B)
        DangerLevel.LOW -> GameTheme.colors.dangerLow to Color(0xFF0C4A6E)
        DangerLevel.MODERATE -> GameTheme.colors.dangerModerate to Color(0xFF78350F)
        DangerLevel.HIGH -> GameTheme.colors.dangerHigh to Color(0xFF7F1D1D)
        DangerLevel.EXTREME -> GameTheme.colors.dangerExtreme to Color(0xFF4C0519)
        DangerLevel.UNKNOWN -> GameTheme.colors.dangerUnknown to Color(0xFF3B0764)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Text(
            text = dangerLevel.titleRu.uppercase(),
            style = GameTheme.typography.badgeText.copy(
                color = color,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Unified Game Chip for filter selections and status tags.
 */
@Composable
fun GameChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selectedColor: Color = GameTheme.colors.primary,
    unselectedColor: Color = GameTheme.colors.textMuted,
    testTag: String? = null
) {
    val bgColor = if (isSelected) selectedColor.copy(alpha = 0.15f) else GameTheme.colors.surfaceElevated
    val borderColor = if (isSelected) selectedColor else GameTheme.colors.border

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) selectedColor else unselectedColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = GameTheme.typography.buttonTextSmall.copy(
                    color = if (isSelected) GameTheme.colors.textPrimary else unselectedColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        }
    }
}

/**
 * Unified Progress Bar for HP, Cargo, Storage, XP, etc.
 */
@Composable
fun StatProgressBar(
    label: String,
    current: Int,
    max: Int,
    barColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    suffix: String = ""
) {
    val fraction = if (max > 0) (current.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = GameTheme.typography.caption.copy(color = GameTheme.colors.textMuted)
            )
            Text(
                text = "$current / $max$suffix",
                style = GameTheme.typography.numericSecondary.copy(
                    fontSize = 11.sp,
                    color = GameTheme.colors.textPrimary
                )
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(GameTheme.colors.background)
                .border(0.5.dp, GameTheme.colors.border, RoundedCornerShape(height / 2))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(height / 2))
                    .background(barColor)
            )
        }
    }
}

/**
 * Unified Segmented Action Points (AP) indicator with tactical pips.
 */
@Composable
fun ActionPointsBar(
    currentAP: Int,
    maxAP: Int,
    modifier: Modifier = Modifier,
    pipSize: Dp = 10.dp,
    activeColor: Color = GameTheme.colors.secondary
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxAP) {
            val isActive = i <= currentAP
            Box(
                modifier = Modifier
                    .size(pipSize)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isActive) activeColor else GameTheme.colors.surfaceHighlight)
                    .border(
                        0.75.dp,
                        if (isActive) activeColor else GameTheme.colors.borderLight,
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
