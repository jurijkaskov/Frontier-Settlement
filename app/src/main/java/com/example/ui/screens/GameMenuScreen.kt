package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GameState
import com.example.ui.components.GameCard
import com.example.ui.components.GameConfirmationDialog
import com.example.ui.theme.*

/**
 * Tactical in-game pause and menu screen providing access to session status,
 * save/load, settings, help, and navigation.
 */
@Composable
fun GameMenuScreen(
    gameState: GameState,
    onResume: () -> Unit,
    onNavigateToSave: () -> Unit,
    onNavigateToLoad: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onResetGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FrontierDarkBackground.copy(alpha = 0.96f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FRONTIER SETTLEMENT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "МЕНЮ ИГРЫ",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                )
            }

            // Session Status Card
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = TechCyan.copy(alpha = 0.5f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = gameState.settlement.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Surface(
                            color = TechCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Уровень ${gameState.settlement.level}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TechCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Divider(color = FrontierBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "День ${gameState.day} • ${gameState.gameDateTime.formattedTime}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 12.sp)
                        )
                        Text(
                            text = "Выживших: ${gameState.characters.count { it.isAlive }}",
                            style = MaterialTheme.typography.bodySmall.copy(color = SafeEmerald, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        )
                    }

                    if (gameState.activeExpedition != null) {
                        Surface(
                            color = WarningAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠ Внимание: Экспедиция в пути (${gameState.activeExpedition.location.name})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Actions Menu
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_menu_resume")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = FrontierOnPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ПРОДОЛЖИТЬ ИГРУ",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = FrontierOnPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToSave,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2E24)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_menu_save")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Сохранить", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_menu_load")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TechCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Загрузить", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onNavigateToSettings,
                colors = ButtonDefaults.buttonColors(containerColor = FrontierDarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_menu_settings")
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Настройки приложения", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToHelp,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("btn_menu_help")
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Справка", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNavigateToAbout,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("btn_menu_about")
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Об игре", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = { showResetConfirmDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCrimson),
                border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_menu_reset_game")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Начать заново (Новая игра)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }

    if (showResetConfirmDialog) {
        GameConfirmationDialog(
            title = "Начать новую игру?",
            message = "Текущий прогресс будет сброшен к начальному состоянию дня 1. Автосохранение будет перезаписано. Продолжить?",
            confirmText = "Начать заново",
            cancelText = "Отмена",
            isDanger = true,
            onConfirm = {
                showResetConfirmDialog = false
                onResetGame()
            },
            onDismiss = { showResetConfirmDialog = false }
        )
    }
}
