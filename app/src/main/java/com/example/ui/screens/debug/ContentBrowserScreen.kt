package com.example.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.content.registry.GameContentRegistry
import com.example.domain.content.registry.GameContentValidator
import com.example.domain.content.registry.ValidationSeverity
import com.example.ui.components.GameCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

enum class ContentBrowserTab(val titleRu: String) {
    VALIDATOR("Проверка"),
    LOCATIONS("Локации"),
    EVENTS("События"),
    ENCOUNTERS("Бои и враги"),
    LOOT("Лут-таблицы"),
    ARCHETYPES("Архетипы"),
    QUESTS("Контракты")
}

@Composable
fun ContentBrowserScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ContentBrowserTab.VALIDATOR) }
    val validationReport = remember { GameContentValidator.validateRegistry() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WastelandDark)
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF24262E), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Каталог контента (Registry)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "Пакеты контента: ${GameContentRegistry.allPacks.size} • Статус: ${if (validationReport.isValid) "Валиден" else "Ошибки"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color(0xFF1E2028),
            contentColor = TechCyan,
            edgePadding = 0.dp
        ) {
            ContentBrowserTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.titleRu,
                            color = if (selectedTab == tab) TechCyan else TextSecondary,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        when (selectedTab) {
            ContentBrowserTab.VALIDATOR -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        GameCard {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Отчёт валидации реестра контента",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = if (validationReport.isValid) SafeEmerald else DangerCrimson,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Пакеты: ${validationReport.totalPacksChecked}", color = TextSecondary, fontSize = 13.sp)
                                Text("Шаблоны локаций: ${validationReport.totalLocationsChecked}", color = TextSecondary, fontSize = 13.sp)
                                Text("События: ${validationReport.totalEventsChecked}", color = TextSecondary, fontSize = 13.sp)
                                Text("Враги: ${validationReport.totalEnemiesChecked}", color = TextSecondary, fontSize = 13.sp)
                                Text("Схватки: ${validationReport.totalEncountersChecked}", color = TextSecondary, fontSize = 13.sp)
                                Text("Лут-таблицы: ${validationReport.totalLootTablesChecked}", color = TextSecondary, fontSize = 13.sp)
                                Text("Архетипы персонажей: ${validationReport.totalArchetypesChecked}", color = TextSecondary, fontSize = 13.sp)
                                Text("Шаблоны квестов: ${validationReport.totalQuestsChecked}", color = TextSecondary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ошибок: ${validationReport.errorCount} • Предупреждений: ${validationReport.warningCount}",
                                    color = if (validationReport.isValid) SafeEmerald else DangerCrimson,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    items(validationReport.issues) { issue ->
                        GameCard {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (issue.severity == ValidationSeverity.ERROR) DangerCrimson else WarningAmber,
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "[${issue.domain}] ${issue.contentId}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = issue.message, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            ContentBrowserTab.LOCATIONS -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(GameContentRegistry.locationTemplates.values.toList()) { tmpl ->
                        GameCard {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(tmpl.id, color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Тип: ${tmpl.type.titleRu} • Опасность: ${tmpl.minDangerLevel.titleRu}..${tmpl.maxDangerLevel.titleRu}", color = Color.White, fontSize = 12.sp)
                                Text("Обязательные зоны: ${tmpl.mandatoryAreas.size} • Опциональные: ${tmpl.optionalAreaPool.size}", color = TextSecondary, fontSize = 12.sp)
                                Text("Теги: ${tmpl.tags.joinToString { it.titleRu }}", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            ContentBrowserTab.EVENTS -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(GameContentRegistry.events.values.toList()) { event ->
                        GameCard {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(event.title, color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ID: ${event.id} • Категория: ${event.category.titleRu} • Редкость: ${event.rarity.titleRu}", color = Color.White, fontSize = 12.sp)
                                Text(event.description, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
                                Text("Вариантов выбора: ${event.choices.size}", color = MaterialsOrange, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            ContentBrowserTab.ENCOUNTERS -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        SectionHeader(title = "Шаблоны схваток", accentColor = DangerCrimson)
                    }
                    items(GameContentRegistry.encounterTemplates.values.toList()) { enc ->
                        GameCard {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(enc.titleRu, color = DangerCrimson, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ID: ${enc.id} • Врагов: ${enc.minEnemies}..${enc.maxEnemies} • XP: +${enc.baseRewardXp}", color = Color.White, fontSize = 12.sp)
                                Text("Пул врагов: ${enc.enemyPool.map { it.enemyTemplateId }}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                    item {
                        SectionHeader(title = "Шаблоны врагов", accentColor = WarningAmber)
                    }
                    items(GameContentRegistry.enemyTemplates.values.toList()) { enemy ->
                        GameCard {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(enemy.nameRu, color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ID: ${enemy.id} • HP: ${enemy.baseHp} • Atk: ${enemy.baseAttack} • Def: ${enemy.baseDefense} • Init: ${enemy.baseInitiative}", color = Color.White, fontSize = 12.sp)
                                Text("Роль: ${enemy.role.titleRu} • AI: ${enemy.aiProfileId}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            ContentBrowserTab.LOOT -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(GameContentRegistry.lootTables.values.toList()) { loot ->
                        GameCard {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(loot.titleRu, color = CreditsYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ID: ${loot.id} • Кредиты: ${loot.minCredits}..${loot.maxCredits}", color = Color.White, fontSize = 12.sp)
                                Text("Ресурсы: ${loot.resourceEntries.map { "${it.resourceType.titleRu} (${it.minAmount}..${it.maxAmount})" }}", color = TextSecondary, fontSize = 11.sp)
                                Text("Предметы: ${loot.itemEntries.map { it.itemId }}", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            ContentBrowserTab.ARCHETYPES -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(GameContentRegistry.characterArchetypes.values.toList()) { arch ->
                        GameCard {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(arch.titleRu, color = SafeEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ID: ${arch.id} • Роль: ${arch.role.titleRu} • Бюджет статов: ${arch.minStatBudget}..${arch.maxStatBudget}", color = Color.White, fontSize = 12.sp)
                                Text("Специализации: ${arch.specializations.joinToString()}", color = TextSecondary, fontSize = 11.sp)
                                Text("Предпочитаемые черты: ${arch.preferredTraits.joinToString()}", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            ContentBrowserTab.QUESTS -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(GameContentRegistry.repeatableQuestTemplates.values.toList()) { q ->
                        GameCard {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(q.id, color = StoragePurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Тип цели: ${q.objectiveType.titleRu} • КД: ${q.cooldownDays} дн.", color = Color.White, fontSize = 12.sp)
                                Text("Награды: +${q.baseRewardCredits} Кр, +${q.baseRewardXp} XP, +${q.baseReputationReward} Реп", color = CreditsYellow, fontSize = 12.sp)
                                Text("Шаблон: ${q.titleTemplateRu}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
