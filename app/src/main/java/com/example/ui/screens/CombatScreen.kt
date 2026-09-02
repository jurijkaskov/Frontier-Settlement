package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.*
import com.example.domain.service.combat.CombatActionCatalog
import com.example.domain.service.combat.CombatTargetValidator
import com.example.ui.components.GameCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

/**
 * Main Tactical Turn-Based Combat Screen (Point 21).
 * Features rich AP management, skill cooldowns, active combat effects,
 * interactive targeting mode, dynamic turn queue, and battle terminal logs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombatScreen(
    gameState: GameState,
    onCombatAction: (CombatAction) -> Unit,
    onSelectTarget: (String) -> Unit,
    onUseItem: (String) -> Unit,
    onFinishCombatVictory: () -> Unit,
    onRetreat: () -> Unit,
    onCancelTargeting: () -> Unit = {},
    onDebugRestoreAP: () -> Unit = {},
    onDebugSkipTurn: () -> Unit = {},
    onDebugForceVictory: () -> Unit = {},
    onDebugForceDefeat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val combat = gameState.activeCombat
    var showItemDialog by remember { mutableStateOf(false) }
    var showDebugPanel by remember { mutableStateOf(false) }

    if (combat == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(FrontierDarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = TechCyan,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Боевой контакт не зафиксирован",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextMuted)
                )
                Button(
                    onClick = onRetreat,
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Вернуться к исследованию")
                }
            }
        }
        return
    }

    val activeCombatant = combat.currentActiveCombatant
    val isPlayerTurn = combat.isPlayerTurn && !combat.isEnded
    val roleSkill = CombatActionCatalog.getSkillForRole(activeCombatant?.role)
    val targetingAction = combat.targetingAction

    val validTargetIds = remember(combat.combatants, activeCombatant, targetingAction) {
        if (targetingAction != null && activeCombatant != null) {
            CombatTargetValidator.getValidTargets(targetingAction, activeCombatant, combat.combatants).map { it.id }.toSet()
        } else {
            emptySet()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FrontierDarkBackground)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // 1. Encounter Header & Round Status
        item {
            CombatHeader(combat = combat, isPlayerTurn = isPlayerTurn)
        }

        // 2. Initiative & Turn Order Queue Bar
        item {
            CombatTurnQueueBar(combat = combat)
        }

        // 3. Interactive Target Selection Banner (if active)
        if (targetingAction != null) {
            item {
                TargetingModeBanner(
                    targetingAction = targetingAction,
                    onCancel = onCancelTargeting
                )
            }
        }

        // 4. Enemy Squad Sector (Living vs Defeated)
        item {
            SectionHeader(
                title = "Враждебные цели (${combat.livingEnemies.size}/${combat.enemies.size})",
                accentColor = DangerCrimson
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                combat.combatants.filter { it.team == CombatantTeam.ENEMY }.forEach { enemy ->
                    val isValidTarget = validTargetIds.contains(enemy.id)
                    EnemyCombatantCard(
                        enemy = enemy,
                        isSelectedTarget = enemy.id == combat.selectedTargetId,
                        isActiveTurn = enemy.id == activeCombatant?.id,
                        isHighlightTarget = isValidTarget,
                        onSelect = { onSelectTarget(enemy.id) }
                    )
                }
            }
        }

        // 5. Tactical Separator & Status Line
        item {
            BattlefieldSeparator()
        }

        // 6. Player Squad Sector
        item {
            SectionHeader(
                title = "Ваш отряд (${combat.livingPlayers.size}/${combat.playerSquad.size})",
                accentColor = SafeEmerald
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                combat.combatants.filter { it.team == CombatantTeam.PLAYER }.forEach { player ->
                    val isValidTarget = validTargetIds.contains(player.id)
                    PlayerCombatantCard(
                        player = player,
                        isSelectedTarget = player.id == combat.selectedTargetId,
                        isActiveTurn = player.id == activeCombatant?.id,
                        isHighlightTarget = isValidTarget,
                        onSelect = { onSelectTarget(player.id) }
                    )
                }
            }
        }

        // 7. Active Turn Actions or Victory/Defeat Banners
        item {
            if (combat.isVictory) {
                CombatVictoryCard(combat = combat, onClaimVictory = onFinishCombatVictory)
            } else if (combat.isDefeat) {
                CombatDefeatCard(combat = combat, onRetreat = onRetreat)
            } else if (activeCombatant != null && isPlayerTurn) {
                PlayerActionControlDeck(
                    activeCombatant = activeCombatant,
                    roleSkill = roleSkill,
                    selectedTarget = combat.selectedTarget,
                    onExecuteAction = onCombatAction,
                    onOpenItems = { showItemDialog = true }
                )
            } else {
                EnemyTurnIndicatorCard(activeEnemy = activeCombatant)
            }
        }

        // 8. Tactical Battle Terminal Log
        item {
            CombatTerminalLog(logs = combat.logs)
        }

        // 9. Collapsible Debug / Sandbox Tools
        item {
            CombatDebugPanel(
                isOpen = showDebugPanel,
                combat = combat,
                onToggle = { showDebugPanel = !showDebugPanel },
                onRestoreAP = onDebugRestoreAP,
                onSkipTurn = onDebugSkipTurn,
                onForceVictory = onDebugForceVictory,
                onForceDefeat = onDebugForceDefeat
            )
        }
    }

    // Modal Item Selection Dialog
    if (showItemDialog) {
        CombatItemSelectionDialog(
            inventoryItems = gameState.inventoryItems,
            onDismiss = { showItemDialog = false },
            onItemChosen = { itemId ->
                showItemDialog = false
                onUseItem(itemId)
            }
        )
    }
}

// -----------------------------------------------------------------------------
// UI Subcomponents
// -----------------------------------------------------------------------------

@Composable
private fun CombatHeader(combat: CombatState, isPlayerTurn: Boolean) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = if (isPlayerTurn) TechCyan else DangerCrimson
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isPlayerTurn) SafeEmerald else DangerCrimson)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlayerTurn) "ВАШ ТАКТИЧЕСКИЙ ХОД" else "ХОД ПРОТИВНИКА",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isPlayerTurn) TechCyan else DangerCrimson,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
                Text(
                    text = combat.encounterTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                color = if (isPlayerTurn) TechCyan.copy(alpha = 0.15f) else Color(0xFF450A0A),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isPlayerTurn) TechCyan.copy(alpha = 0.5f) else DangerCrimson)
            ) {
                Text(
                    text = "Раунд ${combat.roundNumber}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPlayerTurn) TechCyan else DangerCrimson
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun TargetingModeBanner(
    targetingAction: CombatAction,
    onCancel: () -> Unit
) {
    Surface(
        color = TechCyan.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, TechCyan),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                    tint = TechCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ВЫБЕРИТЕ ЦЕЛЬ ДЛЯ НАВЫКА",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "«${targetingAction.name}» (${targetingAction.apCost} ОД)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, DangerCrimson),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCrimson),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Отмена", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun CombatTurnQueueBar(combat: CombatState) {
    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, FrontierBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ОЧЕРЕДЬ ИНИЦИАТИВЫ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = "Ход: ${combat.currentTurnIndex + 1}/${combat.turnOrder.size.coerceAtLeast(1)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                combat.turnOrder.forEachIndexed { index, combatantId ->
                    val unit = combat.combatants.find { it.id == combatantId }
                    if (unit != null) {
                        val isActive = index == combat.currentTurnIndex
                        val isPlayer = unit.team == CombatantTeam.PLAYER

                        Surface(
                            color = when {
                                unit.isDefeated -> FrontierDarkBackground.copy(alpha = 0.5f)
                                isActive -> (if (isPlayer) TechCyan else DangerCrimson).copy(alpha = 0.2f)
                                else -> FrontierDarkSurfaceElevated
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                if (isActive) 2.dp else 1.dp,
                                when {
                                    unit.isDefeated -> FrontierBorder
                                    isActive -> if (isPlayer) TechCyan else DangerCrimson
                                    isPlayer -> SafeEmerald.copy(alpha = 0.4f)
                                    else -> DangerCrimson.copy(alpha = 0.4f)
                                }
                            ),
                            modifier = Modifier.widthIn(min = 68.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPlayer) Icons.Default.Person else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = when {
                                            unit.isDefeated -> TextMuted
                                            isPlayer -> SafeEmerald
                                            else -> DangerCrimson
                                        },
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${unit.initiative}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isActive) TechCyan else TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                Text(
                                    text = unit.displayName.split(" ").firstOrNull() ?: unit.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (unit.isDefeated) TextSubtle else TextWhite,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (isActive) {
                                    Text(
                                        text = "ХОД",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isPlayer) TechCyan else DangerCrimson,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 8.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnemyCombatantCard(
    enemy: Combatant,
    isSelectedTarget: Boolean,
    isActiveTurn: Boolean,
    isHighlightTarget: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = when {
        enemy.isDefeated -> FrontierBorder
        isHighlightTarget -> TechCyan
        isSelectedTarget -> DangerCrimson
        isActiveTurn -> WarningAmber
        else -> DangerCrimson.copy(alpha = 0.35f)
    }

    Surface(
        color = if (enemy.isDefeated) FrontierDarkSurface.copy(alpha = 0.5f) else FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(if (isSelectedTarget || isActiveTurn || isHighlightTarget) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = !enemy.isDefeated, onClick = onSelect)
            .testTag("enemy_card_${enemy.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Enemy Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (enemy.isDefeated) FrontierDarkBackground else Color(0xFF450A0A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            enemy.isDefeated -> Icons.Default.Close
                            enemy.displayName.contains("пёс", ignoreCase = true) -> Icons.Default.Pets
                            enemy.displayName.contains("Главарь", ignoreCase = true) -> Icons.Default.MilitaryTech
                            else -> Icons.Default.SportsKabaddi
                        },
                        contentDescription = null,
                        tint = if (enemy.isDefeated) TextMuted else DangerCrimson,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Name, HP, Defense
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = enemy.displayName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (enemy.isDefeated) TextSubtle else TextWhite
                            )
                        )
                        val aiProfile = enemy.aiProfileId?.let { com.example.domain.service.combat.ai.EnemyAIProfileCatalog.getProfile(it) }
                        if (aiProfile != null && !enemy.isDefeated) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = DangerCrimson.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = aiProfile.archetype.titleRu.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DangerCrimson,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (enemy.status == CombatantStatus.DEFENDING) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = TechCyan.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "В УКРЫТИИ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TechCyan,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // HP Bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(FrontierBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(enemy.hpFraction)
                                    .background(if (enemy.isDefeated) TextMuted else DangerCrimson)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${enemy.currentHealth}/${enemy.maxHealth} HP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Action / Selection Tag
                Column(horizontalAlignment = Alignment.End) {
                    if (enemy.isDefeated) {
                        Surface(
                            color = FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "НЕЙТРАЛИЗОВАН",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DangerCrimson,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isHighlightTarget) {
                        Button(
                            onClick = onSelect,
                            colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "ЦЕЛЬ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FrontierDarkBackground
                                )
                            )
                        }
                    } else if (isSelectedTarget) {
                        Surface(
                            color = DangerCrimson,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = FrontierOnPrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "ВЫБРАН",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = FrontierOnPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onSelect,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Выбрать", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "АТК: ${enemy.attack} | ЗАЩ: ${enemy.effectiveDefense}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            // Active Effects Badges
            if (enemy.activeEffects.isNotEmpty() && !enemy.isDefeated) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    enemy.activeEffects.forEach { effect ->
                        CombatEffectBadge(effect = effect)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerCombatantCard(
    player: Combatant,
    isSelectedTarget: Boolean,
    isActiveTurn: Boolean,
    isHighlightTarget: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = when {
        player.isDefeated -> FrontierBorder
        isHighlightTarget -> TechCyan
        isActiveTurn -> TechCyan
        isSelectedTarget -> SafeEmerald
        else -> SafeEmerald.copy(alpha = 0.35f)
    }

    Surface(
        color = if (player.isDefeated) FrontierDarkSurface.copy(alpha = 0.5f) else FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(if (isActiveTurn || isSelectedTarget || isHighlightTarget) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = !player.isDefeated, onClick = onSelect)
            .testTag("player_card_${player.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (player.isDefeated) FrontierDarkBackground else Color(0xFF064E3B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (player.role) {
                            CharacterRole.SOLDIER -> Icons.Default.SportsKabaddi
                            CharacterRole.MEDIC -> Icons.Default.MedicalServices
                            CharacterRole.ENGINEER -> Icons.Default.Build
                            CharacterRole.SCOUT -> Icons.Default.Explore
                            CharacterRole.SCAVENGER -> Icons.Default.Search
                            null -> Icons.Default.Person
                        },
                        contentDescription = null,
                        tint = if (player.isDefeated) TextMuted else SafeEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.displayName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (player.isDefeated) TextSubtle else TextWhite
                            )
                        )
                        player.role?.let { role ->
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${role.titleRu})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TechCyan,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // HP Bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(FrontierBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(player.hpFraction)
                                    .background(
                                        when {
                                            player.isDefeated -> TextMuted
                                            player.hpFraction > 0.5f -> SafeEmerald
                                            player.hpFraction > 0.25f -> WarningAmber
                                            else -> DangerCrimson
                                        }
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${player.currentHealth}/${player.maxHealth} HP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Stats & AP dots
                Column(horizontalAlignment = Alignment.End) {
                    if (isActiveTurn) {
                        Surface(
                            color = TechCyan,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "АКТИВЕН",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = FrontierDarkBackground,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (player.isDefeated) {
                        Text(
                            text = "Вне боя",
                            style = MaterialTheme.typography.labelSmall.copy(color = DangerCrimson, fontSize = 10.sp)
                        )
                    } else if (isHighlightTarget) {
                        Surface(
                            color = TechCyan,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "СОЮЗНИК",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = FrontierDarkBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // AP Dots Preview
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ОД: ",
                            style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        )
                        (1..player.maxActionPoints).forEach { dotIndex ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 1.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (dotIndex <= player.actionPoints) WarningAmber else FrontierBorder
                                    )
                            )
                        }
                    }

                    Text(
                        text = "АТК: ${player.attack} | ЗАЩ: ${player.effectiveDefense}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            // Active Effects Badges
            if (player.activeEffects.isNotEmpty() && !player.isDefeated) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    player.activeEffects.forEach { effect ->
                        CombatEffectBadge(effect = effect)
                    }
                }
            }
        }
    }
}

@Composable
private fun CombatEffectBadge(effect: CombatEffectInstance) {
    val durationText = when (effect.durationType) {
        EffectDurationType.UNTIL_NEXT_TURN -> "1х"
        EffectDurationType.UNTIL_END_OF_TURN -> "ход"
        EffectDurationType.TURNS -> "${effect.remainingTurns}х"
        EffectDurationType.ROUNDS -> "${effect.remainingRounds}р"
    }

    val stackText = if (effect.stacks > 1) " (${effect.stacks}x)" else ""

    val (icon, badgeColor, label) = when (effect.effectType) {
        CombatEffectType.REGENERATION -> {
            val healVal = if (effect.tickHeal > 0) effect.tickHeal * effect.stacks else effect.modifier
            Triple("💉", SafeEmerald, "+$healVal HP [$durationText]$stackText")
        }
        CombatEffectType.BUFF_DEFENSE -> Triple("🛡️", TechCyan, "+${effect.modifier} Броня [$durationText]")
        CombatEffectType.DEFENDING -> Triple("🛡️", TechCyan, "Стойка +${effect.modifier} [$durationText]")
        CombatEffectType.BUFF_ATTACK -> Triple("⚔️", WarningAmber, "+${effect.modifier} АТК [$durationText]")
        CombatEffectType.BUFF_AP -> Triple("⚡", WarningAmber, "+${effect.modifier} ОД [$durationText]")
        CombatEffectType.BUFF_INITIATIVE -> Triple("⏩", TechCyan, "+${effect.modifier} ИНИЦ [$durationText]")
        CombatEffectType.DEBUFF_DEFENSE -> Triple("🎯", DangerCrimson, "-${effect.modifier} ЗАЩ [$durationText]")
        CombatEffectType.DEBUFF_ATTACK -> Triple("🥀", DangerCrimson, "-${effect.modifier} АТК [$durationText]")
        CombatEffectType.DEBUFF_BLEED -> {
            val dmg = effect.tickDamage * effect.stacks
            Triple("🩸", DangerCrimson, "Кровотечение -$dmg HP [$durationText]$stackText")
        }
        CombatEffectType.DEBUFF_POISON -> {
            val dmg = effect.tickDamage * effect.stacks
            Triple("🧪", DangerCrimson, "Яд -$dmg HP [$durationText]$stackText")
        }
        CombatEffectType.STUN -> Triple("💫", WarningAmber, "ОГЛУШЕНИЕ [$durationText]")
        CombatEffectType.DISORIENT -> Triple("🌀", DangerCrimson, "-${effect.modifier} ИНИЦ [$durationText]")
        CombatEffectType.FOCUS -> Triple("🎯", TechCyan, "Фокус +${effect.modifier} [$durationText]")
    }

    Surface(
        color = badgeColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 9.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = badgeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun BattlefieldSeparator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = FrontierBorder)
        Text(
            text = "  ТАКТИЧЕСКИЙ БОЙ  ",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TechCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = FrontierBorder)
    }
}

@Composable
private fun PlayerActionControlDeck(
    activeCombatant: Combatant,
    roleSkill: CombatAction,
    selectedTarget: Combatant?,
    onExecuteAction: (CombatAction) -> Unit,
    onOpenItems: () -> Unit
) {
    val skillCooldown = activeCombatant.abilityCooldowns[roleSkill.id] ?: 0
    val isSkillReady = skillCooldown == 0 && activeCombatant.actionPoints >= roleSkill.apCost

    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = TechCyan
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Active Hero HUD
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
                            .background(TechCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = activeCombatant.displayName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = "Цель: ${selectedTarget?.displayName ?: "Не выбрана"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selectedTarget != null) TechCyan else WarningAmber,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Action Points Counter
                Surface(
                    color = WarningAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ОД: ${activeCombatant.actionPoints}/${activeCombatant.maxActionPoints}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = WarningAmber,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Primary Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Basic Attack
                Button(
                    onClick = { onExecuteAction(CombatActionCatalog.BASIC_ATTACK) },
                    enabled = activeCombatant.actionPoints >= CombatActionCatalog.BASIC_ATTACK.apCost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerCrimson,
                        disabledContainerColor = DangerCrimson.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_combat_attack")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SportsKabaddi, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Атака (2 ОД)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // 2. Role Skill (with cooldown feedback)
                Button(
                    onClick = { onExecuteAction(roleSkill) },
                    enabled = isSkillReady,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningAmber,
                        disabledContainerColor = WarningAmber.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_combat_skill")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = FrontierOnTertiary, modifier = Modifier.size(16.dp))
                        Text(
                            text = if (skillCooldown > 0) "КД: ${skillCooldown}р" else "${roleSkill.name.take(10)} (${roleSkill.apCost} ОД)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = FrontierOnTertiary),
                            maxLines = 1
                        )
                    }
                }

                // 3. Defend / Stance
                Button(
                    onClick = { onExecuteAction(CombatActionCatalog.DEFEND) },
                    enabled = activeCombatant.actionPoints >= CombatActionCatalog.DEFEND.apCost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechCyan,
                        disabledContainerColor = TechCyan.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_combat_defend")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = FrontierOnSecondary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Укрытие (1 ОД)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = FrontierOnSecondary)
                        )
                    }
                }
            }

            // Secondary Row (Items & End Turn)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Item Button
                OutlinedButton(
                    onClick = onOpenItems,
                    enabled = activeCombatant.actionPoints >= 2,
                    border = BorderStroke(1.dp, SafeEmerald),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SafeEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_combat_item")
                ) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Предмет (2 ОД)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // End Turn Button
                OutlinedButton(
                    onClick = { onExecuteAction(CombatActionCatalog.END_TURN) },
                    border = BorderStroke(1.dp, TextMuted),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_combat_pass")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Сдать ход",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnemyTurnIndicatorCard(activeEnemy: Combatant?) {
    GameCard(
        backgroundColor = Color(0xFF450A0A),
        borderColor = DangerCrimson
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = DangerCrimson,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Ход противника: [${activeEnemy?.displayName ?: "Враг"}] совершает действие...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun CombatVictoryCard(combat: CombatState, onClaimVictory: () -> Unit) {
    GameCard(
        backgroundColor = Color(0xFF064E3B),
        borderColor = SafeEmerald
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🎉 ТАКТИЧЕСКАЯ ПОБЕДА!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SafeEmerald
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Все враждебные цели нейтрализованы. Поле боя зачищено.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextWhite),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bonus Rewards preview
            Surface(
                color = FrontierDarkBackground.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Трофеи с поля боя:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TechCyan, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💰 ${combat.bonusLoot.money} Кр  |  🧱 ${combat.bonusLoot.materials} Матер.  |  ⭐ +${combat.xpReward} XP",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextWhite, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClaimVictory,
                colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_combat_victory_claim")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Забрать трофеи и продолжить",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = FrontierOnPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun CombatDefeatCard(combat: CombatState, onRetreat: () -> Unit) {
    GameCard(
        backgroundColor = Color(0xFF450A0A),
        borderColor = DangerCrimson
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚠️ ПОРАЖЕНИЕ ОТРЯДА",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DangerCrimson
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Все бойцы отряда получили критические ранения. Требуется экстренная эвакуация.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextWhite),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRetreat,
                colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_combat_retreat")
            ) {
                Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Эвакуироваться на базу",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun CombatTerminalLog(logs: List<CombatLogEntry>) {
    SectionHeader(title = "Тактический лог боя")
    GameCard(
        backgroundColor = FrontierDarkBackground,
        borderColor = FrontierBorderLight
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            logs.takeLast(8).forEach { logEntry ->
                Text(
                    text = "[Рnd ${logEntry.turn}] ${logEntry.text}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (logEntry.isPlayerAction) SafeEmerald else DangerCrimson,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun CombatDebugPanel(
    isOpen: Boolean,
    combat: CombatState,
    onToggle: () -> Unit,
    onRestoreAP: () -> Unit,
    onSkipTurn: () -> Unit,
    onForceVictory: () -> Unit,
    onForceDefeat: () -> Unit
) {
    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, FrontierBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙️ ТАКТИЧЕСКИЕ ИНСТРУМЕНТЫ ОТЛАДКИ И ИИ (DEBUG)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
                Icon(
                    imageVector = if (isOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (isOpen) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onRestoreAP,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("⚡ +ОД", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                    }
                    OutlinedButton(
                        onClick = onSkipTurn,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("⏭️ Ход", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                    }
                    OutlinedButton(
                        onClick = onForceVictory,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SafeEmerald),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("🏆 Победа", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                    }
                    OutlinedButton(
                        onClick = onForceDefeat,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCrimson),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("💀 Слив", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                    }
                }

                // AI Decision Telemetry
                val lastAiLog = combat.aiDecisionLogs.lastOrNull()
                if (lastAiLog != null) {
                    val profile = com.example.domain.service.combat.ai.EnemyAIProfileCatalog.getProfile(lastAiLog.profileId)
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = FrontierBorderLight)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🤖 Последнее решение ИИ (Раунд ${lastAiLog.round}):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "Юнит: ${lastAiLog.actorName} | Профиль: ${profile.archetype.titleRu}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 10.sp)
                    )
                    val chosen = lastAiLog.chosenCandidate
                    if (chosen != null) {
                        Text(
                            text = "Действие: «${chosen.action.name}» (Оценка: ${"%.1f".format(chosen.finalScore)}) -> ${chosen.targetName ?: "Без цели"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                    }
                    if (lastAiLog.reason.isNotBlank()) {
                        Text(
                            text = "Логика: ${lastAiLog.reason}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 9.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CombatItemSelectionDialog(
    inventoryItems: List<WarehouseItem>,
    onDismiss: () -> Unit,
    onItemChosen: (String) -> Unit
) {
    val usableItems = inventoryItems.filter {
        it.category == ItemCategory.MEDICINE_AND_AID ||
                it.id.contains("aid", ignoreCase = true) ||
                it.id.contains("first", ignoreCase = true) ||
                it.id.contains("bandage", ignoreCase = true) ||
                it.id.contains("repair", ignoreCase = true) ||
                it.id.contains("multitool", ignoreCase = true)
    }.ifEmpty {
        listOf(
            WarehouseItem(
                id = "item_spec_firstaid",
                name = "Полевой реанимационный комплект",
                category = ItemCategory.MEDICINE_AND_AID,
                description = "Восстанавливает 40 HP выбранному союзнику",
                quantity = 2
            ),
            WarehouseItem(
                id = "item_tool_repairkit",
                name = "Ремкомплект брони",
                category = ItemCategory.EQUIPMENT_AND_TOOLS,
                description = "Укрепляет броню (+8 к защите)",
                quantity = 1
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FrontierDarkSurfaceElevated,
        title = {
            Text(
                text = "Использовать припас в бою",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Использование предмета расходует 2 ОД активного бойца:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )

                usableItems.forEach { item ->
                    Surface(
                        color = FrontierDarkSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemChosen(item.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                )
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SafeEmerald,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Text(
                                text = "x${item.quantity}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextMuted)
            }
        }
    )
}
