package com.example.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.save.GameSaveConstants
import com.example.data.save.SaveSlotId
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.TechCyan
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugSaveScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metadataMap by viewModel.saveSlotsMetadata.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    var statusMessage by remember { mutableStateOf("Готов к выполнению отладочных сценариев") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Debug Save & Recovery Panel",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Log Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "STATUS LOG:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TechCyan
                        )
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Quick Actions
            item {
                Text(
                    text = "ДЕЙСТВИЯ СОХРАНЕНИЯ",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveAutosave(isCritical = true)
                            statusMessage = "Запущено немедленное критическое автосохранение (День ${gameState.day})"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Force Critical Save")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.refreshSaveMetadata()
                            statusMessage = "Метаданные слотов обновлены"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Refresh Metadata")
                    }
                }
            }

            // Corruption Testing
            item {
                Text(
                    text = "ТЕСТИРОВАНИЕ СБОЕВ И ПОВРЕЖДЕНИЙ",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DangerCrimson
                )
            }

            items(SaveSlotId.allPlayerVisibleSlots()) { slot ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = slot.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            val meta = metadataMap[slot.id]
                            Text(
                                text = if (meta != null) "День ${meta.gameDay} (${if (meta.isCorrupted) "ПОВРЕЖДЕН" else "OK"})" else "Пуст",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (meta?.isCorrupted == true) DangerCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.corruptSlotForTesting(slot.id)
                                statusMessage = "Слот «${slot.displayName}» намеренно поврежден для проверки восстановления."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson)
                        ) {
                            Text("Повредить")
                        }
                    }
                }
            }

            // System Metadata details
            item {
                Text(
                    text = "ИНФОРМАЦИЯ О СХЕМЕ",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Current Schema Version: ${GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION}", style = MaterialTheme.typography.bodySmall)
                        Text("App Version: ${GameSaveConstants.DEFAULT_GAME_VERSION}", style = MaterialTheme.typography.bodySmall)
                        Text("Playthrough ID: ${gameState.playthroughId}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
