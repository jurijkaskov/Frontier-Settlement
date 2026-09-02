package com.example.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.content.character.CharacterGenerator
import com.example.domain.content.core.ContentGenerationContext
import com.example.domain.content.core.ContentGenerationHistory
import com.example.domain.content.encounter.EncounterGenerator
import com.example.domain.content.event.ExpeditionEventGenerator
import com.example.domain.content.location.LocationGenerator
import com.example.domain.content.loot.LootGenerator
import com.example.domain.content.quest.RepeatableQuestGenerator
import com.example.domain.content.registry.GameContentRegistry
import com.example.domain.model.CharacterRole
import com.example.domain.model.DangerLevel
import com.example.domain.model.LocationType
import com.example.ui.components.GameCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

enum class GeneratorCategory(val titleRu: String) {
    LOCATION("Локация"),
    LOOT("Лут"),
    RECRUIT("Рекрут"),
    CONTRACT("Контракт"),
    ENCOUNTER("Боевая схватка"),
    EVENT("Событие")
}

@Composable
fun GeneratorDebugScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(GeneratorCategory.LOCATION) }
    var seed by remember { mutableStateOf(133742L) }
    var dangerLevel by remember { mutableStateOf(DangerLevel.LOW) }
    var genIndex by remember { mutableStateOf(1) }
    var outputLog by remember { mutableStateOf("Нажмите «Сгенерировать» для тестирования выбранного генератора.") }

    fun runGeneration() {
        val context = ContentGenerationContext(
            gameSeed = seed,
            currentGameDay = 1,
            dangerLevel = dangerLevel,
            settlementLevel = 1,
            generationIndex = genIndex
        )

        when (selectedCategory) {
            GeneratorCategory.LOCATION -> {
                val res = LocationGenerator.generateRandomLocation(context)
                val loc = res.getOrNull()
                outputLog = if (loc != null) {
                    """
                    Успешно сгенерирована локация:
                    • ID: ${loc.id}
                    • Название: ${loc.name}
                    • Тип: ${loc.type.titleRu}
                    • Координаты: (${"%.2f".format(loc.coordinateX)}, ${"%.2f".format(loc.coordinateY)})
                    • Дистанция: ${loc.distanceKm} км
                    • Опасность: ${loc.dangerLevel.titleRu}
                    • Описание: ${loc.description}
                    • Локальные зоны (${loc.localAreas.size}):
                    ${loc.localAreas.joinToString("\n") { "  - [${it.typeRu}] ${it.name}" }}
                    • Ожидаемые ресурсы: Мат: ${loc.estimatedLootMaterials}, Кр: ${loc.estimatedLootCredits}, Еда: ${loc.estimatedLootFood}
                    """.trimIndent()
                } else {
                    "Ошибка генерации: ${res.toString()}"
                }
            }

            GeneratorCategory.LOOT -> {
                val table = GameContentRegistry.lootTables.values.firstOrNull()
                if (table != null) {
                    val res = LootGenerator.generateLoot(table, context)
                    val loot = res.getOrNull()
                    outputLog = if (loot != null) {
                        """
                        Успешно сгенерирован лут из таблицы «${table.titleRu}»:
                        • Кредиты: +${loot.credits}
                        • Ресурсы: ${loot.resources.map { "${it.key.titleRu}: +${it.value}" }}
                        • Предметы: ${loot.items.map { "${it.name} x${it.quantity}" }}
                        • Сводка: ${loot.summaryRu}
                        """.trimIndent()
                    } else "Ошибка: ${res.toString()}"
                } else {
                    outputLog = "Нет таблиц лута в реестре"
                }
            }

            GeneratorCategory.RECRUIT -> {
                val res = CharacterGenerator.generateRecruit(context)
                val recruit = res.getOrNull()
                outputLog = if (recruit != null) {
                    """
                    Успешно сгенерирован рекрут:
                    • Имя: ${recruit.name}
                    • Роль: ${recruit.role.titleRu}
                    • Биография: ${recruit.bio}
                    • Специализация: ${recruit.specialization}
                    • Характеристики:
                      - Атака: ${recruit.stats.attack}
                      - Защита: ${recruit.stats.defense}
                      - Сбор: ${recruit.stats.scavengingSkill}
                      - Инженерия: ${recruit.stats.engineeringSkill}
                      - Медицина: ${recruit.stats.medicalSkill}
                    • Черты (${recruit.traits.size}): ${recruit.traits.joinToString { it.name }}
                    """.trimIndent()
                } else "Ошибка: ${res.toString()}"
            }

            GeneratorCategory.CONTRACT -> {
                val res = RepeatableQuestGenerator.generateRandomContract(context, emptyList())
                val pair = res.getOrNull()
                outputLog = if (pair != null) {
                    val (def, state) = pair
                    """
                    Успешно сгенерирован контракт:
                    • ID: ${def.id}
                    • Название: ${def.titleRu}
                    • Описание: ${def.descriptionRu}
                    • Категория: ${def.category.titleRu}
                    • Цель: ${def.objectives.firstOrNull()?.descriptionRu}
                    • Награды: ${def.rewards.summaryRu}
                    • Перезарядка: ${def.cooldownDays} дн.
                    """.trimIndent()
                } else "Ошибка: ${res.toString()}"
            }

            GeneratorCategory.ENCOUNTER -> {
                val dummySquad = listOf(
                    com.example.domain.model.Character(
                        id = "test_lead",
                        name = "Тестовый Командир",
                        role = CharacterRole.SOLDIER,
                        health = 100,
                        maxHealth = 100,
                        stats = com.example.domain.model.CharacterStats(attack = 18, defense = 12)
                    )
                )
                val res = EncounterGenerator.generateRandomEncounter(
                    squad = dummySquad,
                    inventoryItems = emptyList(),
                    context = context
                )
                val combat = res.getOrNull()
                outputLog = if (combat != null) {
                    val enemies = combat.combatants.filter { it.team == com.example.domain.model.CombatantTeam.ENEMY }
                    """
                    Успешно сгенерирован бой:
                    • Схватка: ${combat.encounterTitle}
                    • Бойцов всего: ${combat.combatants.size}
                    • Врагов (${enemies.size}):
                    ${enemies.joinToString("\n") { "  - ${it.displayName} (HP: ${it.currentHealth}, Atk: ${it.attack}, Def: ${it.defense}, Init: ${it.initiative})" }}
                    • Очередь хода: ${combat.turnOrder.joinToString(" -> ")}
                    • Бонусная награда: +${combat.bonusLoot.money} Кр, +${combat.bonusLoot.materials} Мат, +${combat.xpReward} XP
                    """.trimIndent()
                } else "Ошибка: ${res.toString()}"
            }

            GeneratorCategory.EVENT -> {
                val history = ContentGenerationHistory()
                val res = ExpeditionEventGenerator.selectNextEvent(context, history)
                val event = res.getOrNull()
                outputLog = if (event != null) {
                    """
                    Успешно выбрано событие:
                    • ID: ${event.id}
                    • Название: ${event.title}
                    • Категория: ${event.category.titleRu}
                    • Редкость: ${event.rarity.titleRu}
                    • Описание: ${event.description}
                    • Вариантов выбора (${event.choices.size}):
                    ${event.choices.joinToString("\n") { "  - [${it.text}] ${it.description ?: ""}" }}
                    """.trimIndent()
                } else "Ошибка: ${res.toString()}"
            }
        }
    }

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
                    text = "Генератор контента (Generator Tool)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "Тестирование детерминированной генерации мира",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategory.ordinal,
            containerColor = Color(0xFF1E2028),
            contentColor = WarningAmber,
            edgePadding = 0.dp
        ) {
            GeneratorCategory.values().forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = {
                        selectedCategory = cat
                    },
                    text = {
                        Text(
                            text = cat.titleRu,
                            color = if (selectedCategory == cat) WarningAmber else TextSecondary,
                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                GameCard {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Параметры контекста", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Seed: $seed", color = TextSecondary, fontSize = 13.sp)
                            Button(
                                onClick = { seed = (100000L..999999L).random() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3240)),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Случайный Seed", fontSize = 11.sp, color = TechCyan)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Индекс генерации: $genIndex", color = TextSecondary, fontSize = 13.sp)
                            Row {
                                Button(
                                    onClick = { if (genIndex > 1) genIndex-- },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3240)),
                                    modifier = Modifier.height(32.dp).width(44.dp)
                                ) {
                                    Text("-", color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { genIndex++ },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3240)),
                                    modifier = Modifier.height(32.dp).width(44.dp)
                                ) {
                                    Text("+", color = Color.White)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { runGeneration() },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Сгенерировать результат", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Результат генерации", accentColor = TechCyan)
            }

            item {
                GameCard {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = outputLog,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }
}
