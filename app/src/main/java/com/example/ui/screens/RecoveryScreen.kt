package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GameCard
import com.example.ui.theme.*

/**
 * Clean tactical recovery screen presented when a save file is corrupted or fails to load.
 */
@Composable
fun RecoveryScreen(
    slotName: String,
    errorMessage: String,
    hasBackupAvailable: Boolean,
    onRestoreBackup: () -> Unit,
    onOpenLoadScreen: () -> Unit,
    onStartNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FrontierDarkBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alert Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(DangerCrimson.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .border(1.dp, DangerCrimson, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = DangerCrimson,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "ОШИБКА ЧТЕНИЯ СОХРАНЕНИЯ",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = DangerCrimson,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 16.sp
                )
            )

            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = DangerCrimson.copy(alpha = 0.5f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Слот сохранения: $slotName",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = errorMessage.ifBlank { "Файл сохранения поврежден или содержит несовместимую структуру данных." },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )

                    if (hasBackupAvailable) {
                        Surface(
                            color = SafeEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ Обнаружена автоматическая резервная копия (.bak)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SafeEmerald,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (hasBackupAvailable) {
                Button(
                    onClick = onRestoreBackup,
                    colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_recovery_restore_backup")
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = FrontierOnPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Восстановить из резервной копии",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = FrontierOnPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Button(
                onClick = onOpenLoadScreen,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838)),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_recovery_open_load")
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TechCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Выбрать другой слот сохранения",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            OutlinedButton(
                onClick = onStartNewGame,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_recovery_new_game")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, tint = WarningAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Начать новую игру",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
