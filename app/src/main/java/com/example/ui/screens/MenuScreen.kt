package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.*
import com.example.ui.components.GameCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatProgressBar
import com.example.ui.theme.*

@Composable
fun MenuScreen(
    gameState: GameState,
    lastResourceOp: ResourceOperationResult? = null,
    onClaimQuest: (String) -> Unit,
    onModifyResource: (ResourceType, Int) -> Unit = { _, _ -> },
    onTestFillWarehouse: () -> Unit = {},
    onTestDrainSupplies: () -> Unit = {},
    onTestPartialAdd: () -> Unit = {},
    onTestResetResources: () -> Unit = {},
    onAddSettlementXp: (Int) -> Unit = {},
    onLevelUpSettlement: () -> Unit = {},
    onConstructAllBuildings: () -> Unit = {},
    onResetGame: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToSaveGame: () -> Unit = {},
    onNavigateToLoadGame: () -> Unit = {},
    onNavigateToDebugSave: () -> Unit = {},
    onNavigateToContentBrowser: () -> Unit = {},
    onNavigateToGeneratorDebug: () -> Unit = {},
    onNavigateToUiGallery: () -> Unit = {},
    onNavigateToVisualAssetBrowser: () -> Unit = {},
    onNavigateToAudioGallery: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val quests = gameState.quests
    val res = gameState.resources
    val settlement = gameState.settlement
    val audioEngine = com.example.audio.ui.LocalGameAudio.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // Quick Navigation to Settings, Help & About (Пункт 38)
        item {
            SectionHeader(title = "Настройки и руководство (Пункт 38)", accentColor = TechCyan)
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = TechCyan
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onNavigateToSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2D3D)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_menu_open_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = TechCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Настройки приложения (Звук, Графика, Язык)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToHelp,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E261E)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_menu_open_help")
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Справочник", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToAbout,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252830)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_menu_open_about")
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Об игре", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Audio & Atmosphere Settings (Пункт 37)
        item {
            com.example.audio.ui.AudioSettingsCard(audioEngine = audioEngine)
        }

        // Quests & Objectives
        item {
            val readyCount = gameState.questStates.values.count { it.status == QuestStatus.READY_TO_CLAIM }.coerceAtLeast(quests.count { it.status == QuestStatus.READY_TO_CLAIM })
            val completedCount = gameState.questStates.values.count { it.status == QuestStatus.COMPLETED }.coerceAtLeast(quests.count { it.status == QuestStatus.COMPLETED })
            val totalCount = com.example.domain.service.quest.QuestCatalog.ALL_QUESTS.size.coerceAtLeast(quests.size)

            SectionHeader(
                title = "Задания штаба ($completedCount/$totalCount)" + if (readyCount > 0) " • Готово к сдаче: $readyCount" else "",
                accentColor = if (readyCount > 0) SafeEmerald else CreditsYellow
            )
        }

        items(quests) { quest ->
            QuestCardItem(
                quest = quest,
                onClaim = { onClaimQuest(quest.id) }
            )
        }

        // Settlement Development Testing Panel (Пункт 4: Инструменты отладки прогрессии)
        item {
            SectionHeader(title = "Тест прогрессии поселения (Пункт 4)", accentColor = SafeEmerald)
        }

        item {
            SettlementDevDebugPanel(
                settlement = settlement,
                onAddXp = onAddSettlementXp,
                onLevelUp = onLevelUpSettlement,
                onConstructAll = onConstructAllBuildings
            )
        }

        // Dedicated Resource Testing & Debug Console (Пункт 3: Тестовый инструмент)
        item {
            SectionHeader(title = "Тестовая панель ресурсов (Debug Tool)", accentColor = StoragePurple)
        }

        item {
            ResourceDebugPanel(
                resources = res,
                lastOperation = lastResourceOp,
                onModifyResource = onModifyResource,
                onTestFillWarehouse = onTestFillWarehouse,
                onTestDrainSupplies = onTestDrainSupplies,
                onTestPartialAdd = onTestPartialAdd,
                onTestResetResources = onTestResetResources
            )
        }

        // Outpost Radio Chronicle
        item {
            SectionHeader(title = "Хроника аванпоста (Архив записей)", accentColor = TechCyan)
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = FrontierBorder
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    gameState.dayLogs.forEach { log ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text("• ", color = TechCyan, fontWeight = FontWeight.Bold)
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // About & Survival Guide
        item {
            SectionHeader(title = "Справочник выживания", accentColor = WarningAmber)
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = FrontierBorder
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Базовые правила выживания:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                    )
                    Text(
                        text = "1. Каждый день жители потребляют еду и воду. Следите за балансом на складе.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                    )
                    Text(
                        text = "2. Улучшайте ферму, скважину и мастерскую, чтобы увеличивать суточную выработку.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                    )
                    Text(
                        text = "3. Формируйте отряд бойцов перед вылазкой на карту. Опасные зоны требуют хорошей брони и оружия.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                    )
                    Text(
                        text = "4. Используйте транспорт, чтобы перевозить больше трофеев из дальних зон пустошей.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                    )
                }
            }
        }

        // Data-Driven Content System & Diagnostic Tools (Пункт 30)
        item {
            SectionHeader(title = "Генерация контента и реестр (Пункт 30)", accentColor = TechCyan)
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = TechCyan
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Data-Driven каталоги, валидатор и генераторы мира:",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToContentBrowser,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2638)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_content_browser")
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = TechCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Каталог контента", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToGeneratorDebug,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E261E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_generator_debug")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Тест генератора", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onNavigateToUiGallery,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132E27)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_ui_gallery")
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("UI Design System Gallery (Пункт 31)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onNavigateToVisualAssetBrowser,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1F38)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_visual_asset_browser")
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = StoragePurple, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Реестр игровых ассетов (Пункт 32)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onNavigateToAudioGallery,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2B3A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_audio_gallery")
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = TechCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Аудиогалерея и атмосфера (Пункт 37)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Save & Load Game Management (Пункт 34)
        item {
            SectionHeader(title = "Сохранение и загрузка игры (Пункт 34)", accentColor = SafeEmerald)
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = SafeEmerald
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Управление слотами сохранения и резервными копиями:",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToSaveGame,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132E27)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_save_game")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Сохранить", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToLoadGame,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2638)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_load_game")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TechCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Загрузить", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToDebugSave,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_debug_save")
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Тестирование сбоев и бэкапов (Debug)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Reset Game Button
        item {
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onResetGame,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCrimson),
                border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_reset_game")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Сбросить прогресс и начать заново", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun ResourceDebugPanel(
    resources: GameResources,
    lastOperation: ResourceOperationResult?,
    onModifyResource: (ResourceType, Int) -> Unit,
    onTestFillWarehouse: () -> Unit,
    onTestDrainSupplies: () -> Unit,
    onTestPartialAdd: () -> Unit,
    onTestResetResources: () -> Unit
) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = StoragePurple
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = StoragePurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Проверка правил системы ресурсов",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                }

                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Склад: ${resources.totalStoredVolume}/${resources.warehouseMaxCapacity}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Live status of last operation result
            if (lastOperation != null) {
                val statusBg = when (lastOperation) {
                    is ResourceOperationResult.Success -> SafeEmerald.copy(alpha = 0.15f)
                    is ResourceOperationResult.PartialSuccess -> WarningAmber.copy(alpha = 0.15f)
                    is ResourceOperationResult.Failure -> DangerCrimson.copy(alpha = 0.15f)
                }
                val statusBorder = when (lastOperation) {
                    is ResourceOperationResult.Success -> SafeEmerald
                    is ResourceOperationResult.PartialSuccess -> WarningAmber
                    is ResourceOperationResult.Failure -> DangerCrimson
                }
                val statusIcon = when (lastOperation) {
                    is ResourceOperationResult.Success -> Icons.Default.CheckCircle
                    is ResourceOperationResult.PartialSuccess -> Icons.Default.Warning
                    is ResourceOperationResult.Failure -> Icons.Default.Error
                }

                Surface(
                    color = statusBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusBorder,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = lastOperation.message,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Text(
                text = "Быстрое изменение значений (тест безопасного списания и вместимости):",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
            )

            // Resource step modifier rows
            ResourceQuickControlRow(
                name = "Провизия (Еда)",
                current = resources.food,
                color = FoodGreen,
                onAdd = { onModifyResource(ResourceType.FOOD, 50) },
                onSubtract = { onModifyResource(ResourceType.FOOD, -50) }
            )

            ResourceQuickControlRow(
                name = "Очищенная Вода",
                current = resources.water,
                color = WaterCyan,
                onAdd = { onModifyResource(ResourceType.WATER, 50) },
                onSubtract = { onModifyResource(ResourceType.WATER, -50) }
            )

            ResourceQuickControlRow(
                name = "Топливо / Горючее",
                current = resources.fuel,
                color = FuelAmber,
                onAdd = { onModifyResource(ResourceType.FUEL, 50) },
                onSubtract = { onModifyResource(ResourceType.FUEL, -50) }
            )

            ResourceQuickControlRow(
                name = "Стройматериалы",
                current = resources.materials,
                color = MaterialsOrange,
                onAdd = { onModifyResource(ResourceType.MATERIALS, 50) },
                onSubtract = { onModifyResource(ResourceType.MATERIALS, -50) }
            )

            ResourceQuickControlRow(
                name = "Кредиты (Валюта)",
                current = resources.money,
                color = CreditsYellow,
                onAdd = { onModifyResource(ResourceType.MONEY, 200) },
                onSubtract = { onModifyResource(ResourceType.MONEY, -200) }
            )

            Divider(color = FrontierBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "Быстрые сценарии тестирования:",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
            )

            // Scenario buttons grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTestFillWarehouse,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("Заполнить склад (95%)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onTestDrainSupplies,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCrimson),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("Дефицит (0 еды/воды)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTestPartialAdd,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("Тест +500 еды (Переполнение)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onTestResetResources,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SafeEmerald),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("Сброс к норме", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun ResourceQuickControlRow(
    name: String,
    current: Int,
    color: Color,
    onAdd: () -> Unit,
    onSubtract: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FrontierDarkSurfaceHighlight, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    fontSize = 11.sp
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "$current",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(end = 4.dp)
            )

            FilledTonalButton(
                onClick = onSubtract,
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text("-", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }

            FilledTonalButton(
                onClick = onAdd,
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text("+", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun QuestCardItem(
    quest: Quest,
    onClaim: () -> Unit
) {
    GameCard(
        backgroundColor = if (quest.status == QuestStatus.READY_TO_CLAIM) Color(0xFF192A20) else FrontierDarkSurfaceElevated,
        borderColor = if (quest.status == QuestStatus.READY_TO_CLAIM) SafeEmerald else FrontierBorder
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )

                if (quest.status == QuestStatus.COMPLETED) {
                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Выполнено",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SafeEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = quest.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatProgressBar(
                label = quest.requirementDescription,
                current = quest.progress,
                max = quest.target,
                barColor = if (quest.status == QuestStatus.READY_TO_CLAIM) SafeEmerald else TechCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Награда: +${quest.rewardCredits}кр, +${quest.rewardMaterials}м",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CreditsYellow,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )

                if (quest.status == QuestStatus.READY_TO_CLAIM) {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_claim_quest_${quest.id}")
                    ) {
                        Text(
                            text = "Забрать награду",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = FrontierOnPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettlementDevDebugPanel(
    settlement: com.example.domain.model.Settlement,
    onAddXp: (Int) -> Unit,
    onLevelUp: () -> Unit,
    onConstructAll: () -> Unit
) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = SafeEmerald.copy(alpha = 0.5f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Уровень: ${settlement.level} • ${settlement.tier.titleRu}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                    )
                }

                Text(
                    text = "${settlement.xp} / ${settlement.xpToNextLevel} XP",
                    style = MaterialTheme.typography.labelSmall.copy(color = TechCyan, fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = "Быстрое тестирование механик развития, повышения уровня поселения и мгновенного возведения зданий:",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddXp(100) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_debug_add_xp"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("+100 XP базы", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onLevelUp,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SafeEmerald),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_debug_levelup_settlement"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("Повысить уровень", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                }
            }

            OutlinedButton(
                onClick = onConstructAll,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_debug_construct_all"),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Text("Построить и разблокировать все здания", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
            }
        }
    }
}

