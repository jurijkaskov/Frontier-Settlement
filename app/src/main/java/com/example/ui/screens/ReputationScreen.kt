package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.ReputationBalanceConfig
import com.example.domain.model.GameState
import com.example.domain.model.reputation.*
import com.example.domain.service.reputation.ReputationLevelResolver
import com.example.domain.service.reputation.ReputationManager
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Full diplomatic & standing screen: "Репутация и Дипломатия" (Пункт 28).
 *
 * Implements:
 * - Settlement Global Reputation score (-100..100) & dynamic tier resolver.
 * - Faction catalog: Торговая Гильдия, Братство Инженеров, Кочевники Пустошей, Союз Выживших, Рейдеры Чёрного Черепа.
 * - Active faction perks, diplomatic status badges, and perk unlock tree.
 * - Chronicle History log tracking every choice and expedition consequence.
 * - Debug testing tools to test dynamic thresholds, trade modifier calculations, and level shifts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReputationScreen(
    gameState: GameState,
    onBack: () -> Unit,
    onSettlementReputationChange: (delta: Int, reason: String) -> Unit,
    onFactionRelationChange: (factionId: String, delta: Int, reason: String) -> Unit,
    onResetReputationDebug: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedFactionForDetails by remember { mutableStateOf<FactionDefinition?>(null) }

    val tabs = listOf("Обзор", "Фракции (5)", "Летопись", "Отладка")

    val tradeModifier = ReputationManager.getEffectiveTradeModifier(gameState)
    val researchBonus = ReputationManager.getEffectiveResearchBonusPercent(gameState)
    val speedBonus = ReputationManager.getEffectiveTravelSpeedBonusPercent(gameState)
    val moraleBonus = ReputationManager.getEffectiveRecruitMoraleBonus(gameState)

    // Faction Detail Dialog
    selectedFactionForDetails?.let { factionDef ->
        val relation = gameState.factionRelations[factionDef.id] ?: FactionRelation(
            factionId = factionDef.id,
            points = factionDef.baseRelation
        )
        FactionDetailDialog(
            faction = factionDef,
            relation = relation,
            onDismiss = { selectedFactionForDetails = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "РЕПУТАЦИЯ И ДИПЛОМАТИЯ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Отношения с миром пустошей и фракциями",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TechCyan,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("button_back_reputation")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FrontierDarkSurfaceElevated
                )
            )
        },
        containerColor = FrontierDarkBackground,
        modifier = modifier.testTag("screen_reputation")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tactical Tabs Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = FrontierDarkSurfaceElevated,
                contentColor = TechCyan,
                divider = { HorizontalDivider(color = FrontierBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                        },
                        modifier = Modifier.testTag("tab_reputation_$index")
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> ReputationOverviewTab(
                    gameState = gameState,
                    tradeModifier = tradeModifier,
                    researchBonus = researchBonus,
                    speedBonus = speedBonus,
                    moraleBonus = moraleBonus,
                    onOpenFaction = { factionDef -> selectedFactionForDetails = factionDef }
                )
                1 -> FactionsListTab(
                    gameState = gameState,
                    onOpenFaction = { factionDef -> selectedFactionForDetails = factionDef }
                )
                2 -> ReputationHistoryTab(
                    history = gameState.reputationHistory
                )
                3 -> ReputationDebugTab(
                    gameState = gameState,
                    onSettlementReputationChange = onSettlementReputationChange,
                    onFactionRelationChange = onFactionRelationChange,
                    onResetReputationDebug = onResetReputationDebug
                )
            }
        }
    }
}

/**
 * Tab 0: Comprehensive Overview of Global Standing, Active Modifiers & Quick Faction Matrix.
 */
@Composable
private fun ReputationOverviewTab(
    gameState: GameState,
    tradeModifier: TradeReputationModifier,
    researchBonus: Int,
    speedBonus: Int,
    moraleBonus: Int,
    onOpenFaction: (FactionDefinition) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
    ) {
        // Global Reputation Card
        item {
            GlobalReputationCard(
                reputation = gameState.settlement.reputation,
                gameState = gameState
            )
        }

        // Active Global Empire Modifiers Grid
        item {
            Text(
                text = "СВОДНЫЕ МОДИФИКАТОРЫ МИРА",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TechCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Trade Modifier
                ModifierSummaryRow(
                    icon = Icons.Default.Storefront,
                    title = "Торговый баланс (Цены покупки / продажи)",
                    valueText = "${tradeModifier.buyModifierPercent.let { if (it > 0) "+$it%" else "$it%" }} / +${tradeModifier.sellModifierPercent}%",
                    explanation = tradeModifier.descriptionRu,
                    accentColor = CreditsYellow
                )

                // Research Speed Modifier
                ModifierSummaryRow(
                    icon = Icons.Default.Science,
                    title = "Скорость исследований (Инженеры)",
                    valueText = if (researchBonus > 0) "+$researchBonus%" else if (researchBonus < 0) "$researchBonus%" else "Базовая (0%)",
                    explanation = if (researchBonus > 0) "Бонус за дружбу с Братством Инженеров" else "Стандартная скорость работы лабораторий",
                    accentColor = TechCyan
                )

                // Travel Speed Modifier
                ModifierSummaryRow(
                    icon = Icons.Default.Explore,
                    title = "Скорость экспедиций по карте (Кочевники)",
                    valueText = if (speedBonus > 0) "+$speedBonus%" else if (speedBonus < 0) "$speedBonus%" else "Базовая (0%)",
                    explanation = if (speedBonus > 0) "Проводники кочевников сокращают время переходов" else "Обычная скорость караванов и отряда",
                    accentColor = WarningAmber
                )

                // Recruit Morale Modifier
                ModifierSummaryRow(
                    icon = Icons.Default.Groups,
                    title = "Бонус морали новобранцев (Альянс Выживших)",
                    valueText = if (moraleBonus > 0) "+$moraleBonus" else if (moraleBonus < 0) "$moraleBonus" else "0",
                    explanation = "Увеличивает стартовую мораль прибывающих поселенцев",
                    accentColor = SafeEmerald
                )
            }
        }

        // Factions Quick Peek
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ФРАКЦИОННЫЙ БАЛАНС СИЛ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "5 фракций",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }

        items(ReputationBalanceConfig.FACTION_CATALOG) { factionDef ->
            val relation = gameState.factionRelations[factionDef.id] ?: FactionRelation(
                factionId = factionDef.id,
                points = factionDef.baseRelation
            )
            FactionCardItem(
                faction = factionDef,
                relation = relation,
                onClick = { onOpenFaction(factionDef) }
            )
        }
    }
}

/**
 * Single Tactical Modifier Summary Row.
 */
@Composable
private fun ModifierSummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    valueText: String,
    explanation: String,
    accentColor: Color
) {
    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Tab 1: Detailed List of all 5 Factions.
 */
@Composable
private fun FactionsListTab(
    gameState: GameState,
    onOpenFaction: (FactionDefinition) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "ФРАКЦИИ И ОРГАНИЗАЦИИ ПУСТОШЕЙ",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TechCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Нажмите на фракцию, чтобы увидеть дерево уникальных бонусов и условия их открытия.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSubtle,
                    fontSize = 11.sp
                )
            )
        }

        items(ReputationBalanceConfig.FACTION_CATALOG) { factionDef ->
            val relation = gameState.factionRelations[factionDef.id] ?: FactionRelation(
                factionId = factionDef.id,
                points = factionDef.baseRelation
            )
            FactionCardItem(
                faction = factionDef,
                relation = relation,
                onClick = { onOpenFaction(factionDef) }
            )
        }
    }
}

/**
 * Tab 2: Chronicle History Log of Reputation and Faction Changes.
 */
@Composable
private fun ReputationHistoryTab(
    history: List<ReputationHistoryEntry>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "ЛЕТОПИСЬ РЕШЕНИЙ И ВЛИЯНИЯ",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TechCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Все ключевые выборы, исходы экспедиций и дипломатические события фиксируются в хронике.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSubtle,
                    fontSize = 11.sp
                )
            )
        }

        if (history.isEmpty()) {
            item {
                Surface(
                    color = FrontierDarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "История репутации пока пуста.\nОтправляйте отряды в экспедиции и принимайте сюжетные решения.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            }
        } else {
            items(history) { entry ->
                ReputationHistoryCard(entry = entry)
            }
        }
    }
}

/**
 * Tab 3: Debug Testing Tools to manually shift reputation and faction points.
 */
@Composable
private fun ReputationDebugTab(
    gameState: GameState,
    onSettlementReputationChange: (delta: Int, reason: String) -> Unit,
    onFactionRelationChange: (factionId: String, delta: Int, reason: String) -> Unit,
    onResetReputationDebug: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
    ) {
        item {
            Surface(
                color = FrontierDarkSurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "РЕЖИМ ОТЛАДКИ И ТЕСТИРОВАНИЯ (DEBUG)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Позволяет быстро проверять переход между рангами, открытие фракционных привилегий, перерасчёт скидок торговцев и пороги событий.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 11.sp)
                    )
                }
            }
        }

        // Settlement Global Reputation Controls
        item {
            Surface(
                color = FrontierDarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Репутация аванпоста (Текущая: ${gameState.settlement.reputation} / 100)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                    )
                    Text(
                        text = "Ранг: ${gameState.reputationTier.titleRu}",
                        style = MaterialTheme.typography.bodySmall.copy(color = gameState.reputationTier.badgeColor)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSettlementReputationChange(-25, "Отладочное снижение репутации") },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f).testTag("debug_rep_minus_25")
                        ) {
                            Text("-25")
                        }
                        Button(
                            onClick = { onSettlementReputationChange(-10, "Отладочное снижение репутации") },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f).testTag("debug_rep_minus_10")
                        ) {
                            Text("-10")
                        }
                        Button(
                            onClick = { onSettlementReputationChange(10, "Отладочное повышение репутации") },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f).testTag("debug_rep_plus_10")
                        ) {
                            Text("+10")
                        }
                        Button(
                            onClick = { onSettlementReputationChange(25, "Отладочное повышение репутации") },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f).testTag("debug_rep_plus_25")
                        ) {
                            Text("+25")
                        }
                    }
                }
            }
        }

        // Faction Relations Controls
        items(ReputationBalanceConfig.FACTION_CATALOG) { faction ->
            val points = gameState.factionRelations[faction.id]?.points ?: faction.baseRelation
            val tier = ReputationLevelResolver.resolveFactionTier(points)

            Surface(
                color = FrontierDarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = faction.nameRu,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                            )
                            Text(
                                text = "Статус: ${tier.titleRu} ($points pts)",
                                style = MaterialTheme.typography.bodySmall.copy(color = tier.badgeColor, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onFactionRelationChange(faction.id, -20, "Тестовое ухудшение отношений") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCrimson),
                            modifier = Modifier.weight(1f).testTag("debug_faction_${faction.id}_minus_20")
                        ) {
                            Text("-20")
                        }
                        OutlinedButton(
                            onClick = { onFactionRelationChange(faction.id, 20, "Тестовое улучшение отношений") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SafeEmerald),
                            modifier = Modifier.weight(1f).testTag("debug_faction_${faction.id}_plus_20")
                        ) {
                            Text("+20")
                        }
                    }
                }
            }
        }

        // Reset Button
        item {
            Button(
                onClick = onResetReputationDebug,
                colors = ButtonDefaults.buttonColors(containerColor = FrontierDarkSurfaceHighlight),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debug_reset_reputation")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сбросить репутацию к исходным значениям")
            }
        }
    }
}
