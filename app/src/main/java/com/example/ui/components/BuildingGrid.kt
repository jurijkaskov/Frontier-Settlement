package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Building
import com.example.domain.model.BuildingType
import com.example.domain.model.GameState
import com.example.domain.model.QuestStatus
import com.example.ui.theme.*

/**
 * Interactive 2-column Building & Hub Grid on the Settlement Screen.
 *
 * Cards:
 * 1. Склад: capacity info (e.g. 620 / 1000)
 * 2. Магазин / Торговля: shop level & caravan status (Ур. 1)
 * 3. Мастерская: workshop level & materials boost (Ур. 1)
 * 4. Жители: population count (18 / 25)
 * 5. Исследования: available/researched tech (2 / 8)
 * 6. Задания: quest badges and ready-to-claim count
 */
@Composable
fun BuildingGrid(
    gameState: GameState,
    onNavigateToWarehouse: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToWorkshop: () -> Unit,
    onNavigateToBuildings: () -> Unit,
    onNavigateToSquad: () -> Unit,
    onNavigateToResidents: () -> Unit = onNavigateToSquad,
    onNavigateToResearch: () -> Unit,
    onNavigateToQuests: () -> Unit,
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToEconomy: () -> Unit = {},
    onNavigateToReputation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val res = gameState.resources
    val sett = gameState.settlement
    val techs = gameState.technologies
    val quests = gameState.quests
    val repTier = gameState.reputationTier

    val tradingBuilding = sett.buildings.find { it.type == BuildingType.TRADING_POST }
    val workshopBuilding = sett.buildings.find { it.type == BuildingType.WORKSHOP }
    val readyQuestsCount = gameState.questStates.values.count { it.status == QuestStatus.READY_TO_CLAIM }.coerceAtLeast(quests.count { it.status == QuestStatus.READY_TO_CLAIM })
    val completedQuestsCount = gameState.questStates.values.count { it.status == QuestStatus.COMPLETED }.coerceAtLeast(quests.count { it.status == QuestStatus.COMPLETED })
    val totalQuestsCount = com.example.domain.service.quest.QuestCatalog.ALL_QUESTS.size.coerceAtLeast(quests.size)
    val researchedCount = techs.count { it.isResearched }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Склад & Магазин
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BuildingHubCard(
                icon = Icons.Default.Inventory2,
                title = "Склад",
                primaryStat = "${gameState.totalWarehouseOccupiedVolume} / ${res.warehouseMaxCapacity}",
                secondaryStat = if (gameState.isWarehouseFull) "Заполнен!" else "${gameState.freeWarehouseCapacity} свободно",
                accentColor = StoragePurple,
                progressFraction = gameState.warehouseOccupancyFraction,
                testTag = "card_building_warehouse",
                onClick = onNavigateToWarehouse,
                modifier = Modifier.weight(1f)
            )

            val tradingStatus = if (tradingBuilding?.isConstructed == true) {
                "Ур. ${tradingBuilding.level}"
            } else {
                "Караван"
            }
            val tradingSubtitle = if (tradingBuilding?.isConstructed == true) {
                "Скидка: -${tradingBuilding.level * 5}%"
            } else {
                "Караван прибыл"
            }

            BuildingHubCard(
                icon = Icons.Default.Storefront,
                title = "Торговля",
                primaryStat = tradingStatus,
                secondaryStat = tradingSubtitle,
                accentColor = CreditsYellow,
                testTag = "card_building_market",
                onClick = onNavigateToMarket,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Мастерская & Жители
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val workshopStatus = if (workshopBuilding?.isConstructed == true) "Ур. ${workshopBuilding.level}" else "Не построена"
            val workshopSubtitle = if (workshopBuilding?.isConstructed == true) "Крафт и сборка" else "Требует постройки"

            BuildingHubCard(
                icon = Icons.Default.Build,
                title = "Мастерская",
                primaryStat = workshopStatus,
                secondaryStat = workshopSubtitle,
                accentColor = TechCyan,
                testTag = "card_building_workshop",
                onClick = onNavigateToWorkshop,
                modifier = Modifier.weight(1f)
            )

            BuildingHubCard(
                icon = Icons.Default.Groups,
                title = "Жители",
                primaryStat = "${gameState.currentPopulation} / ${sett.maxPopulation}",
                secondaryStat = if (gameState.freeHousingSlots > 0) "Свободно: ${gameState.freeHousingSlots}" else "Мест нет",
                accentColor = FoodGreen,
                progressFraction = gameState.populationOccupancyFraction,
                testTag = "card_building_residents",
                onClick = onNavigateToResidents,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Автопарк & Развитие Базы
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BuildingHubCard(
                icon = Icons.Default.DirectionsCar,
                title = "Автопарк",
                primaryStat = "${gameState.availableVehicles.size} / ${gameState.vehicles.size} готово",
                secondaryStat = "${gameState.totalFleetCapacityKg} кг грузоподъём.",
                accentColor = WarningAmber,
                testTag = "card_building_vehicles",
                onClick = onNavigateToVehicles,
                modifier = Modifier.weight(1f)
            )

            BuildingHubCard(
                icon = Icons.Default.Apartment,
                title = "Постройки",
                primaryStat = "${sett.buildings.count { it.isConstructed }} / ${sett.buildings.size}",
                secondaryStat = "Управление базой",
                accentColor = SafeEmerald,
                testTag = "card_building_buildings",
                onClick = onNavigateToBuildings,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: Исследования & Задания
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BuildingHubCard(
                icon = Icons.Default.Science,
                title = "Исследования",
                primaryStat = "$researchedCount / ${techs.size}",
                secondaryStat = if (researchedCount < techs.size) "Доступны схемы" else "Всё изучено",
                accentColor = TechCyan,
                progressFraction = (researchedCount.toFloat() / techs.size.toFloat()).coerceIn(0f, 1f),
                testTag = "card_building_research",
                onClick = onNavigateToResearch,
                modifier = Modifier.weight(1f)
            )

            BuildingHubCard(
                icon = Icons.Default.Assignment,
                title = "Задания",
                primaryStat = "$completedQuestsCount / $totalQuestsCount",
                secondaryStat = if (readyQuestsCount > 0) "Награда ждёт!" else "В процессе",
                accentColor = WarningAmber,
                badgeCount = if (readyQuestsCount > 0) readyQuestsCount else null,
                testTag = "card_building_quests",
                onClick = onNavigateToQuests,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5: Экономика и Бюджет Базы (Full Tactical Card)
        Surface(
            color = FrontierDarkSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CreditsYellow.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onNavigateToEconomy() }
                .testTag("card_building_economy")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(CreditsYellow.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, CreditsYellow.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Экономика",
                            tint = CreditsYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Экономика и Баланс",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = "Казна: ${res.money} Кр. • Прогноз производства и расходов",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSubtle,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Открыть экономику",
                    tint = CreditsYellow,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Row 6: Репутация и Дипломатия (Full Tactical Card)
        Surface(
            color = FrontierDarkSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, repTier.badgeColor.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onNavigateToReputation() }
                .testTag("card_building_reputation")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(repTier.badgeColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, repTier.badgeColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Репутация",
                            tint = repTier.badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Репутация и Дипломатия",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = "Статус: ${repTier.titleRu} (${sett.reputation}/100) • 5 фракций",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSubtle,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Открыть дипломатию",
                    tint = repTier.badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Individual Tactical Building Card for the hub grid.
 * Provides rich visual feedback, depth, glowing accents, and touch target >= 48dp.
 */
@Composable
fun BuildingHubCard(
    icon: ImageVector,
    title: String,
    primaryStat: String,
    secondaryStat: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progressFraction: Float? = null,
    badgeCount: Int? = null,
    testTag: String = ""
) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (badgeCount != null && badgeCount > 0) SafeEmerald else FrontierBorder
        ),
        tonalElevation = 4.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 84.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Icon + Badge + Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badgeCount != null && badgeCount > 0) {
                    Surface(
                        color = SafeEmerald,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "+$badgeCount",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = FrontierOnPrimary,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Title & Stats
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = primaryStat,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = secondaryStat,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Optional mini progress bar
            if (progressFraction != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(FrontierBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .background(accentColor)
                    )
                }
            }
        }
    }
}
