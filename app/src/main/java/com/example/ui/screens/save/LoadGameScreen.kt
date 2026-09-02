package com.example.ui.screens.save

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.save.SaveLoadResult
import com.example.data.save.SaveMetadata
import com.example.data.save.SaveSlotId
import com.example.ui.components.AutosaveIndicator
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadGameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToGame: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val metadataMap by viewModel.saveSlotsMetadata.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val lastLoadResult by viewModel.lastLoadResult.collectAsStateWithLifecycle()

    var slotToConfirmLoad by remember { mutableStateOf<SaveSlotId?>(null) }
    var slotToConfirmDelete by remember { mutableStateOf<SaveSlotId?>(null) }

    val visibleSlots = remember { SaveSlotId.allPlayerVisibleSlots() }

    LaunchedEffect(Unit) {
        viewModel.refreshSaveMetadata()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Загрузка игры",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Выберите сохранение для продолжения кампании",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    AutosaveIndicator(isSaving = isSaving, modifier = Modifier.padding(end = 12.dp))
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(visibleSlots) { slot ->
                val meta = metadataMap[slot.id]
                LoadSlotCard(
                    slot = slot,
                    metadata = meta,
                    onLoadClicked = {
                        slotToConfirmLoad = slot
                    },
                    onDeleteClicked = {
                        slotToConfirmDelete = slot
                    },
                    onRestoreBackupClicked = {
                        viewModel.restoreFromBackup(
                            backupSlotId = SaveSlotId.AUTOSAVE_BACKUP.id,
                            targetSlotId = slot.id,
                            onNavigateToRoute = onNavigateToGame
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Confirmation Dialog for Loading
    slotToConfirmLoad?.let { slot ->
        AlertDialog(
            onDismissRequest = { slotToConfirmLoad = null },
            title = { Text("Загрузить сохранение?") },
            text = {
                Text("Вы уверены, что хотите загрузить «${slot.displayName}»? Несохраненный прогресс текущей сессии будет заменен.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadFromSlot(slot.id, onNavigateToRoute = onNavigateToGame)
                        slotToConfirmLoad = null
                    }
                ) {
                    Text("Загрузить")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { slotToConfirmLoad = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Confirmation Dialog for Deletion
    slotToConfirmDelete?.let { slot ->
        AlertDialog(
            onDismissRequest = { slotToConfirmDelete = null },
            title = { Text("Удалить слот сохранения?") },
            text = {
                Text("Вы действительно хотите удалить сохранение из слота «${slot.displayName}»? Это действие необратимо.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSaveSlot(slot.id)
                        slotToConfirmDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { slotToConfirmDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Load Result Alert for errors/corruptions
    lastLoadResult?.let { res ->
        when (res) {
            is SaveLoadResult.Corrupted -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearLastLoadResult() },
                    title = { Text("Файл сохранения поврежден", color = DangerCrimson) },
                    text = {
                        Text("Ошибка при чтении файла сохранения: ${res.error}.\n\nВы можете попытаться восстановить состояние из резервной копии.")
                    },
                    confirmButton = {
                        if (res.canTryBackup) {
                            Button(
                                onClick = {
                                    viewModel.clearLastLoadResult()
                                    viewModel.restoreFromBackup(onNavigateToRoute = onNavigateToGame)
                                }
                            ) {
                                Text("Восстановить из бэкапа")
                            }
                        } else {
                            Button(onClick = { viewModel.clearLastLoadResult() }) {
                                Text("Понятно")
                            }
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { viewModel.clearLastLoadResult() }) {
                            Text("Закрыть")
                        }
                    }
                )
            }
            is SaveLoadResult.UnsupportedNewerVersion -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearLastLoadResult() },
                    title = { Text("Неподдерживаемая версия сохранения") },
                    text = {
                        Text("Версия схемы файла (v${res.saveVersion}) новее поддерживаемой текущим приложением (v${res.appMaxVersion}). Обновите приложение.")
                    },
                    confirmButton = {
                        Button(onClick = { viewModel.clearLastLoadResult() }) {
                            Text("ОК")
                        }
                    }
                )
            }
            else -> Unit
        }
    }
}

@Composable
fun LoadSlotCard(
    slot: SaveSlotId,
    metadata: SaveMetadata?,
    onLoadClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onRestoreBackupClicked: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = when {
                    metadata?.isCorrupted == true -> DangerCrimson.copy(alpha = 0.6f)
                    metadata != null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metadata?.displayName ?: slot.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (metadata != null && !metadata.isCorrupted) {
                    Text(
                        text = dateFormat.format(Date(metadata.updatedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            if (metadata != null) {
                if (metadata.isCorrupted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerCrimson.copy(alpha = 0.12f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Ошибка",
                            tint = DangerCrimson
                        )
                        Text(
                            text = "Файл сохранения поврежден или имеет неверный формат.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DangerCrimson
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRestoreBackupClicked,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Восстановить")
                        }
                        IconButton(onClick = onDeleteClicked) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Удалить", tint = DangerCrimson)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${metadata.settlementName} (Ур. ${metadata.settlementLevel})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "День ${metadata.gameDay}, ${metadata.gameTimeFormatted} • ${metadata.locationContext.titleRu}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SafeEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = metadata.locationName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SafeEmerald
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onLoadClicked,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Загрузить")
                        }

                        if (slot.isManual) {
                            IconButton(onClick = onDeleteClicked) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить слот",
                                    tint = DangerCrimson.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Пустой слот — нет данных для загрузки",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
