package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GameTheme

/**
 * Unified Game Top App Bar for Sub-Screens.
 */
@Composable
fun GameTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    testTag: String = "game_top_bar"
) {
    Surface(
        color = GameTheme.colors.surface,
        border = BorderStroke(1.dp, GameTheme.colors.border),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GameTheme.colors.surfaceHighlight)
                    .border(1.dp, GameTheme.colors.borderLight, RoundedCornerShape(8.dp))
                    .testTag("top_bar_btn_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = GameTheme.colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = GameTheme.typography.screenTitle.copy(fontSize = 17.sp)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = GameTheme.typography.caption.copy(color = GameTheme.colors.textMuted)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

/**
 * Unified Tactical Confirmation Dialog.
 */
@Composable
fun GameConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Подтвердить",
    cancelText: String = "Отмена",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDanger: Boolean = false,
    icon: ImageVector = Icons.Default.HelpOutline
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = GameTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (isDanger) GameTheme.colors.danger else GameTheme.colors.borderLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("game_confirmation_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isDanger) GameTheme.colors.dangerContainer else GameTheme.colors.surfaceHighlight)
                        .border(
                            1.dp,
                            if (isDanger) GameTheme.colors.danger else GameTheme.colors.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDanger) GameTheme.colors.danger else GameTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    style = GameTheme.typography.screenTitle.copy(
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = GameTheme.typography.body.copy(
                        color = GameTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecondaryActionButton(
                        text = cancelText,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        height = 42.dp
                    )

                    if (isDanger) {
                        DangerActionButton(
                            text = confirmText,
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            height = 42.dp
                        )
                    } else {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GameTheme.colors.primary,
                                contentColor = GameTheme.colors.onPrimary
                            )
                        ) {
                            Text(
                                text = confirmText,
                                style = GameTheme.typography.buttonText.copy(fontSize = 12.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Unified Empty State Component.
 */
@Composable
fun GameEmptyState(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Inbox,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GameTheme.colors.surfaceHighlight)
                .border(1.dp, GameTheme.colors.borderLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GameTheme.colors.textMuted,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            style = GameTheme.typography.sectionTitle.copy(
                color = GameTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = GameTheme.typography.bodySecondary.copy(
                color = GameTheme.colors.textMuted,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            CompactActionButton(
                text = actionText,
                onClick = onActionClick,
                containerColor = GameTheme.colors.primaryContainer,
                contentColor = GameTheme.colors.onPrimaryContainer,
                borderColor = GameTheme.colors.primary
            )
        }
    }
}

/**
 * Tactical Radar / Scan Loading State.
 */
@Composable
fun GameLoadingState(
    message: String = "Сканирование сектора...",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "game_loading")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(GameTheme.colors.surfaceHighlight.copy(alpha = pulse))
                .border(1.5.dp, GameTheme.colors.primary.copy(alpha = pulse), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = GameTheme.colors.primary,
                strokeWidth = 2.5.dp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = GameTheme.typography.bodySecondary.copy(
                color = GameTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * Reusable Hero Image Gradient Scrim Overlay.
 */
@Composable
fun HeroImageOverlay(
    modifier: Modifier = Modifier,
    scrimHeight: Dp = 120.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(scrimHeight)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        GameTheme.colors.background.copy(alpha = 0.6f),
                        GameTheme.colors.background
                    )
                )
            )
    )
}
