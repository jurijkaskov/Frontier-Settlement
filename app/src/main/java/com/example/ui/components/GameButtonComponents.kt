package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.motion.pressFeedback
import com.example.ui.theme.GameTheme

/**
 * Primary Dominant Action Button (e.g. "Отправиться", "Начать исследование", "Забрать добычу").
 * Only one primary action button should dominate each screen context.
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    height: Dp = 48.dp,
    containerColor: Color = GameTheme.colors.primary,
    contentColor: Color = GameTheme.colors.onPrimary,
    testTag: String? = null
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = modifier
            .pressFeedback(scaleDown = 0.96f, isEnabled = isEnabled && !isLoading)
            .fillMaxWidth()
            .height(height)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = GameTheme.colors.disabled,
            disabledContentColor = GameTheme.colors.disabledContent
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = GameTheme.typography.buttonText
                )
            }
        }
    }
}

/**
 * Secondary Tactical Action Button (e.g. "Снаряжение", "Подробнее", "Отмена").
 */
@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isEnabled: Boolean = true,
    height: Dp = 44.dp,
    borderColor: Color = GameTheme.colors.borderLight,
    testTag: String? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .pressFeedback(scaleDown = 0.96f, isEnabled = isEnabled)
            .height(height)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isEnabled) borderColor else GameTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = GameTheme.colors.surfaceElevated,
            contentColor = GameTheme.colors.textPrimary,
            disabledContainerColor = GameTheme.colors.surface.copy(alpha = 0.5f),
            disabledContentColor = GameTheme.colors.disabledContent
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) GameTheme.colors.textSecondary else GameTheme.colors.disabledContent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = GameTheme.typography.buttonText.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

/**
 * Danger / Destructive Action Button (e.g. "Отступить", "Сбросить сохранение", "Выгнать").
 */
@Composable
fun DangerActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isEnabled: Boolean = true,
    height: Dp = 44.dp,
    testTag: String? = null
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .pressFeedback(scaleDown = 0.96f, isEnabled = isEnabled)
            .height(height)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GameTheme.colors.danger,
            contentColor = Color.White,
            disabledContainerColor = GameTheme.colors.disabled,
            disabledContentColor = GameTheme.colors.disabledContent
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = GameTheme.typography.buttonText.copy(fontSize = 12.sp)
            )
        }
    }
}

/**
 * Compact Action Button for tables, list rows, inventory actions (34dp height).
 */
@Composable
fun CompactActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isEnabled: Boolean = true,
    containerColor: Color = GameTheme.colors.surfaceHighlight,
    contentColor: Color = GameTheme.colors.textPrimary,
    borderColor: Color = GameTheme.colors.borderLight,
    testTag: String? = null
) {
    Surface(
        color = if (isEnabled) containerColor else GameTheme.colors.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isEnabled) borderColor else GameTheme.colors.border),
        modifier = modifier
            .pressFeedback(scaleDown = 0.96f, isEnabled = isEnabled)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clip(RoundedCornerShape(8.dp))
            .then(if (isEnabled) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) contentColor else GameTheme.colors.disabledContent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = GameTheme.typography.buttonTextSmall.copy(
                    color = if (isEnabled) contentColor else GameTheme.colors.disabledContent
                )
            )
        }
    }
}

/**
 * Unified Tactical Icon Button with optional badge indicator.
 */
@Composable
fun IconGameButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    badgeColor: Color? = null,
    tint: Color = GameTheme.colors.textSecondary,
    backgroundColor: Color = GameTheme.colors.surfaceElevated,
    size: Dp = 40.dp,
    testTag: String? = null
) {
    Surface(
        color = if (isEnabled) backgroundColor else GameTheme.colors.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isEnabled) GameTheme.colors.borderLight else GameTheme.colors.border),
        modifier = modifier
            .pressFeedback(scaleDown = 0.94f, isEnabled = isEnabled)
            .size(size)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clip(RoundedCornerShape(10.dp))
            .then(if (isEnabled) Modifier.clickable { onClick() } else Modifier)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isEnabled) tint else GameTheme.colors.disabledContent,
                modifier = Modifier.size(size * 0.5f)
            )
            if (badgeColor != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor)
                )
            }
        }
    }
}
