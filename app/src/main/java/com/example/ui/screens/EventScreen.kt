package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.components.DangerBadge
import com.example.ui.components.GameCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatProgressBar
import com.example.ui.components.events.EventCategoryBadge
import com.example.ui.components.events.EventChoiceCard
import com.example.ui.components.events.EventIllustrationBanner
import com.example.ui.components.events.EventOutcomeDisplay
import com.example.ui.components.events.EventRarityBadge
import com.example.ui.theme.*

/**
 * Event Screen (Point 18) - Fully data-driven interactive encounter view with choices,
 * skill check mechanics, character role synergies, deterministic dice roll outcomes,
 * and expedition cargo management.
 */
@Composable
fun EventScreen(
    gameState: GameState,
    expedition: Expedition,
    activeEventState: ActiveEventState,
    selectedActorId: String?,
    onSelectActor: (String) -> Unit,
    onExecuteChoice: (choiceId: String) -> Unit,
    onContinueExploration: () -> Unit,
    onStartCombat: () -> Unit,
    onReturnToBase: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val event = activeEventState.event

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FrontierDarkBackground)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Location & Step Header
        item {
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = event.category.badgeColor
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(event.category.badgeColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = event.category.badgeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = expedition.location.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                )
                                Text(
                                    text = "Сектор: ${expedition.location.sectorCode} • Прогресс: ${expedition.explorationProgress}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TechCyan,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        DangerBadge(dangerLevel = expedition.location.dangerLevel)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    StatProgressBar(
                        label = "Исследование локации (Шаг ${expedition.currentStep}/${expedition.maxSteps})",
                        current = expedition.currentStep,
                        max = expedition.maxSteps,
                        barColor = event.category.badgeColor
                    )
                }
            }
        }

        // Squad Status Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                expedition.squad.forEach { fighter ->
                    val isSelectedActor = fighter.id == selectedActorId
                    Surface(
                        color = if (isSelectedActor) TechCyan.copy(alpha = 0.15f) else FrontierDarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSelectedActor) TechCyan else FrontierBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = fighter.name.split(" ").firstOrNull() ?: fighter.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelectedActor) TechCyan else TextWhite
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = fighter.role.titleRu,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(FrontierBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fighter.healthFraction)
                                        .background(if (fighter.health < 30) DangerCrimson else FoodGreen)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Event Atmospheric Banner Illustration
        item {
            EventIllustrationBanner(
                visualAssetId = event.visualAssetId,
                category = event.category
            )
        }

        // Event Header & Story Narrative Box
        item {
            GameCard(
                backgroundColor = FrontierDarkSurfaceHighlight,
                borderColor = event.category.badgeColor.copy(alpha = 0.7f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EventCategoryBadge(category = event.category)
                        EventRarityBadge(rarity = event.rarity)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextWhite.copy(alpha = 0.9f),
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }

        // Dynamic State: Either Choice Selection or Resolved Outcome
        if (!activeEventState.isResolved) {
            item {
                SectionHeader(title = "Возможные действия отряда", accentColor = WarningAmber)
            }

            items(event.choices.size) { index ->
                val choice = event.choices[index]
                EventChoiceCard(
                    choice = choice,
                    choiceIndex = index,
                    gameState = gameState,
                    expedition = expedition,
                    selectedActorId = selectedActorId,
                    onSelectActor = onSelectActor,
                    onChoose = { onExecuteChoice(choice.id) }
                )
            }
        } else {
            // Outcome Results View
            item {
                SectionHeader(title = "Результат действий", accentColor = SafeEmerald)
                EventOutcomeDisplay(
                    activeState = activeEventState,
                    expedition = expedition,
                    onContinueExploration = onContinueExploration,
                    onStartCombat = onStartCombat,
                    onReturnToBase = onReturnToBase
                )
            }
        }

        // Expedition Cargo Live Summary
        item {
            SectionHeader(title = "Грузовой отсек экспедиции", accentColor = MaterialsOrange)
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = FrontierBorder
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Загрузка: ${String.format("%.1f", expedition.cargoWeightKg)} / ${expedition.cargoCapacityKg} кг",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = "Свободно: ${String.format("%.1f", expedition.freeLootCapacityKg)} кг",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (expedition.freeLootCapacityKg < 5f) WarningAmber else SafeEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        LootCounterChip(label = "Кредиты", value = "+${expedition.gatheredLoot.money}", color = CreditsYellow)
                        LootCounterChip(label = "Материалы", value = "+${expedition.gatheredLoot.materials}", color = MaterialsOrange)
                        LootCounterChip(label = "Еда", value = "+${expedition.gatheredLoot.food}", color = FoodGreen)
                        LootCounterChip(label = "Топливо", value = "+${expedition.gatheredLoot.fuel}", color = FuelAmber)
                    }
                }
            }
        }

        // Field Radio Log
        item {
            SectionHeader(title = "Полевой радиожурнал")
            GameCard(
                backgroundColor = FrontierDarkBackground,
                borderColor = FrontierBorderLight
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    expedition.logs.takeLast(6).forEach { log ->
                        Text(
                            text = "› $log",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TechCyan,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LootCounterChip(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontSize = 10.sp
            )
        )
    }
}
