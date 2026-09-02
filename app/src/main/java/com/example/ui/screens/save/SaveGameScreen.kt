package com.example.ui.screens.save

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
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
import com.example.data.save.SaveMetadata
import com.example.data.save.SaveSlotId
import com.example.domain.model.GameState
import com.example.ui.components.AutosaveIndicator
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveGameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val metadataMap by viewModel.saveSlotsMetadata.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val lastSaveResult by viewModel.lastSaveOperationResult.collectAsStateWithLifecycle()

    var slotToConfirmOverwrite by remember { mutableStateOf<SaveSlotId?>(null) }
    var customSaveName by remember { mutableStateOf("") }
    var showCustomNameDialog by remember { mutableStateOf(false) }
    var selectedSlotForCustomName by remember { mutableStateOf<SaveSlotId?>(null) }

    val manualSlots = remember { SaveSlotId.allManualSlots() }

    LaunchedEffect(Unit) {
        viewModel.refreshSaveMetadata()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Сохранение игры",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Выберите слот для записи текущего прогресса",
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
            // Header Info Card
            item {
                CurrentGameSnapshotCard(gameState = gameState)
            }

            // Quick Manual Save Action
            item {
                Text(
                    text = "РУЧНЫЕ СЛОТЫ СОХРАНЕНИЯ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Manual Slots List
            items(manualSlots) { slot ->
                val meta = metadataMap[slot.id]
                SaveSlotCard(
                    slot = slot,
                    metadata = meta,
                    onSaveClicked = {
                        if (meta != null && !meta.isCorrupted) {
                            slotToConfirmOverwrite = slot
                        } else {
                            viewModel.saveToSlot(slot.id, slot.displayName)
                        }
                    },
                    onCustomNameClicked = {
                        selectedSlotForCustomName = slot
                        customSaveName = meta?.displayName ?: slot.displayName
                        showCustomNameDialog = true
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Confirmation Dialog for Overwriting Slot
    slotToConfirmOverwrite?.let { slot ->
        AlertDialog(
            onDismissRequest = { slotToConfirmOverwrite = null },
            title = { Text("Перезаписать сохранение?") },
            text = {
                Text(
                    "Слот «${slot.displayName}» уже содержит сохранение. Вы уверены, что хотите перезаписать его текущим состоянием игры?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveToSlot(slot.id, slot.displayName)
                        slotToConfirmOverwrite = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson)
                ) {
                    Text("Перезаписать")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { slotToConfirmOverwrite = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Custom Name Dialog
    if (showCustomNameDialog && selectedSlotForCustomName != null) {
        val slot = selectedSlotForCustomName!!
        AlertDialog(
            onDismissRequest = { showCustomNameDialog = false },
            title = { Text("Название сохранения") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Введите пользовательское название для слота ${slot.displayName}:")
                    OutlinedTextField(
                        value = customSaveName,
                        onValueChange = { customSaveName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveToSlot(slot.id, customSaveName.ifBlank { slot.displayName })
                        showCustomNameDialog = false
                        selectedSlotForCustomName = null
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showCustomNameDialog = false
                        selectedSlotForCustomName = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun CurrentGameSnapshotCard(gameState: GameState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "ТЕКУЩИЙ ПРОГРЕСС",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TechCyan
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${gameState.settlement.name} (Уровень ${gameState.settlement.level})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "День ${gameState.day}, ${gameState.gameDateTime.formattedTime}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = WarningAmber
                )
            }
            Text(
                text = "Жители: ${gameState.characters.size} | Квесты: ${gameState.questStates.count { it.value.status == com.example.domain.model.QuestStatus.IN_PROGRESS }} активных",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SaveSlotCard(
    slot: SaveSlotId,
    metadata: SaveMetadata?,
    onSaveClicked: () -> Unit,
    onCustomNameClicked: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (metadata != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = metadata?.displayName ?: slot.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (metadata != null) {
                        IconButton(
                            onClick = onCustomNameClicked,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Переименовать",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (metadata != null) {
                    Text(
                        text = dateFormat.format(Date(metadata.updatedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            if (metadata != null) {
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
                            text = "Занято",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SafeEmerald
                        )
                    }
                }
            } else {
                Text(
                    text = "Пустой слот — сохранение отсутствует",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onSaveClicked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (metadata != null) "Перезаписать слот" else "Сохранить в этот слот")
            }
        }
    }
}
