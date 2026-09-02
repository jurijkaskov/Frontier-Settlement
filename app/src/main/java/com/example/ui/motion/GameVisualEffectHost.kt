package com.example.ui.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Types of transient visual notifications in the Frontier UI.
 */
enum class VisualNotificationType {
    INFO,
    SUCCESS,
    WARNING,
    DANGER,
    NEW_DAY,
    LEVEL_UP
}

/**
 * Presentation-only visual notification model.
 */
data class VisualNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String = "",
    val type: VisualNotificationType = VisualNotificationType.INFO,
    val icon: ImageVector? = null,
    val durationMs: Long = 2600L
)

/**
 * In-memory thread-safe visual notification manager for presentation effects.
 */
class VisualNotificationController(
    val maxQueueSize: Int = 10
) {
    var currentNotification by mutableStateOf<VisualNotification?>(null)
        private set

    private val queue = mutableListOf<VisualNotification>()

    val queueSize: Int get() = queue.size
    val isQueueEmpty: Boolean get() = queue.isEmpty()

    fun show(notification: VisualNotification) {
        if (currentNotification == null) {
            currentNotification = notification
        } else {
            // Prevent flooding identical notifications and limit queue capacity
            if (queue.size < maxQueueSize && queue.none { it.title == notification.title }) {
                queue.add(notification)
            }
        }
    }

    fun dismissCurrent() {
        currentNotification = if (queue.isNotEmpty()) {
            queue.removeAt(0)
        } else {
            null
        }
    }

    fun clear() {
        clearAll()
    }

    fun clearAll() {
        queue.clear()
        currentNotification = null
    }
}

val LocalVisualNotificationController = staticCompositionLocalOf { VisualNotificationController() }

/**
 * Visual Effect Host mounted at the top-level game overlay layer.
 */
@Composable
fun GameVisualEffectHost(
    controller: VisualNotificationController,
    modifier: Modifier = Modifier
) {
    val current = controller.currentNotification
    val motion = GameTheme.motion

    LaunchedEffect(current?.id) {
        if (current != null) {
            delay(current.durationMs)
            controller.dismissCurrent()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = current != null,
            enter = fadeIn(tween(motion.duration(MotionDuration.STANDARD), easing = motion.enterEasing)) +
                    slideInVertically(
                        animationSpec = tween(motion.duration(MotionDuration.STANDARD), easing = motion.enterEasing),
                        initialOffsetY = { -it }
                    ),
            exit = fadeOut(tween(motion.duration(MotionDuration.QUICK), easing = motion.exitEasing)) +
                    slideOutVertically(
                        animationSpec = tween(motion.duration(MotionDuration.QUICK), easing = motion.exitEasing),
                        targetOffsetY = { -it }
                    )
        ) {
            if (current != null) {
                VisualNotificationBanner(
                    notification = current,
                    onDismiss = { controller.dismissCurrent() }
                )
            }
        }
    }
}

@Composable
fun VisualNotificationBanner(
    notification: VisualNotification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor: Color = when (notification.type) {
        VisualNotificationType.INFO -> TechCyan
        VisualNotificationType.SUCCESS -> SafeEmerald
        VisualNotificationType.WARNING -> WarningAmber
        VisualNotificationType.DANGER -> DangerRed
        VisualNotificationType.NEW_DAY -> SafeEmerald
        VisualNotificationType.LEVEL_UP -> CreditsYellow
    }

    val defaultIcon: ImageVector = when (notification.type) {
        VisualNotificationType.INFO -> Icons.Default.Info
        VisualNotificationType.SUCCESS -> Icons.Default.CheckCircle
        VisualNotificationType.WARNING -> Icons.Default.Warning
        VisualNotificationType.DANGER -> Icons.Default.Error
        VisualNotificationType.NEW_DAY -> Icons.Default.WbSunny
        VisualNotificationType.LEVEL_UP -> Icons.Default.MilitaryTech
    }

    val icon = notification.icon ?: defaultIcon

    Surface(
        color = GameTheme.colors.surfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.8f)),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDismiss() }
            .testTag("notification_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = GameTheme.typography.sectionTitle.copy(
                        color = GameTheme.colors.textPrimary,
                        fontSize = 14.sp
                    )
                )
                if (notification.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notification.message,
                        style = GameTheme.typography.bodySecondary.copy(
                            color = GameTheme.colors.textMuted,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = GameTheme.colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Atmospheric Day Change Overlay ("ДЕНЬ X").
 * Features graceful fade-in, display pause, and fade-out without blocking user interactions.
 */
@Composable
fun NewDayBannerOverlay(
    dayNumber: Int,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motion = GameTheme.motion

    LaunchedEffect(isVisible, dayNumber) {
        if (isVisible) {
            delay(1600L)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(motion.duration(MotionDuration.STANDARD), easing = motion.enterEasing)),
        exit = fadeOut(tween(motion.duration(MotionDuration.EMPHASIS), easing = motion.exitEasing)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "ФРОНТИР",
                    style = GameTheme.typography.caption.copy(
                        color = TechCyan,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ДЕНЬ $dayNumber",
                    style = GameTheme.typography.screenTitle.copy(
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Новый день приносит новые возможности и испытания",
                    style = GameTheme.typography.bodySecondary.copy(
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
