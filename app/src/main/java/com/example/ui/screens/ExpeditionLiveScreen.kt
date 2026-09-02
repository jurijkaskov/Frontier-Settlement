package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun ExpeditionLiveScreen(
    gameState: GameState,
    onChoiceA: () -> Unit,
    onChoiceB: () -> Unit,
    onStartCombat: () -> Unit,
    onFinishAndReturn: () -> Unit,
    onBackToSettlement: () -> Unit,
    onExecuteChoice: (choiceId: String) -> Unit = {},
    onSelectActor: (actorId: String) -> Unit = {},
    onContinueExploration: () -> Unit = {},
    selectedActorId: String? = null,
    modifier: Modifier = Modifier
) {
    val exp = gameState.activeExpedition

    if (exp == null) {
        Box(
            modifier = modifier.fillMaxSize().background(FrontierDarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Нет активной экспедиции.", style = MaterialTheme.typography.titleMedium.copy(color = TextMuted))
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onBackToSettlement,
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = FrontierDarkBackground),
                    modifier = Modifier.testTag("btn_back_settlement_empty")
                ) {
                    Text("Вернуться в поселение")
                }
            }
        }
        return
    }

    // If an active event state exists, display the full rich EventScreen
    if (exp.activeEventState != null) {
        EventScreen(
            gameState = gameState,
            expedition = exp,
            activeEventState = exp.activeEventState,
            selectedActorId = selectedActorId ?: exp.activeEventState.selectedActorId,
            onSelectActor = onSelectActor,
            onExecuteChoice = { choiceId ->
                onExecuteChoice(choiceId)
            },
            onContinueExploration = onContinueExploration,
            onStartCombat = onStartCombat,
            onReturnToBase = onFinishAndReturn,
            onBack = onBackToSettlement,
            modifier = modifier
        )
        return
    }

    // Fallback standard view if activeEventState is not yet generated
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FrontierDarkBackground)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // Live Expedition Status Header
        item {
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = if (exp.status == ExpeditionStatus.COMBAT) DangerCrimson else SafeEmerald
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
                                    .background(SafeEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = SafeEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = exp.location.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                )
                                Text(
                                    text = "Транспорт: ${exp.vehicle.name}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TechCyan,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        DangerBadge(dangerLevel = exp.location.dangerLevel)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    StatProgressBar(
                        label = "Статус: ${exp.status.titleRu}",
                        current = exp.currentStep,
                        max = exp.maxSteps,
                        barColor = if (exp.status == ExpeditionStatus.RETURNING) SafeEmerald else TechCyan
                    )
                }
            }
        }

        // Squad In-Field Status
        item {
            SectionHeader(title = "Состояние отряда на задании", accentColor = TechCyan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                exp.squad.forEach { fighter ->
                    Surface(
                        color = FrontierDarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = fighter.name.split(" ").firstOrNull() ?: fighter.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${fighter.health} HP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            )
                        }
                    }
                }
            }
        }

        // Current Event / Exploration Choice Card (Legacy fallback)
        if (exp.currentEvent != null) {
            item {
                SectionHeader(title = "Событие на локации", accentColor = WarningAmber)
                GameCard(
                    backgroundColor = FrontierDarkSurfaceHighlight,
                    borderColor = WarningAmber
                ) {
                    Column {
                        Text(
                            text = exp.currentEvent.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = exp.currentEvent.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextWhite,
                                lineHeight = 20.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (exp.currentEvent.requiresCombat) {
                            Button(
                                onClick = onStartCombat,
                                colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_event_start_combat")
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Вступить в бой с противником",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onChoiceA,
                                    colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_event_choice_a")
                                ) {
                                    Text(
                                        text = "Вариант А: ${exp.currentEvent.choiceA}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = FrontierOnPrimary
                                        )
                                    )
                                }

                                OutlinedButton(
                                    onClick = onChoiceB,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_event_choice_b")
                                ) {
                                    Text(
                                        text = "Вариант Б: ${exp.currentEvent.choiceB}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gathered Loot Live Display
        item {
            SectionHeader(title = "Собранная добыча в вылазке", accentColor = MaterialsOrange)
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = FrontierBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LootMiniTag(label = "Кредиты", value = "+${exp.gatheredLoot.money}", color = CreditsYellow)
                    LootMiniTag(label = "Материалы", value = "+${exp.gatheredLoot.materials}", color = MaterialsOrange)
                    LootMiniTag(label = "Еда/Вода", value = "+${exp.gatheredLoot.food + exp.gatheredLoot.water}", color = FoodGreen)
                    LootMiniTag(label = "Топливо", value = "+${exp.gatheredLoot.fuel}", color = FuelAmber)
                }
            }
        }

        // Return to Base Action Button
        if (exp.status == ExpeditionStatus.RETURNING || exp.status == ExpeditionStatus.COMPLETED) {
            item {
                Button(
                    onClick = onFinishAndReturn,
                    colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_return_settlement")
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Завершить вылазку и доставить добычу",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = FrontierOnPrimary
                        )
                    )
                }
            }
        }

        // Field Radio Log
        item {
            SectionHeader(title = "Журнал полевой радиосвязи")
            GameCard(
                backgroundColor = FrontierDarkBackground,
                borderColor = FrontierBorderLight
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    exp.logs.forEach { log ->
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
fun LootMiniTag(label: String, value: String, color: Color) {
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
