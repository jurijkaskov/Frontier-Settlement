package com.example.ui.components.events

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.*
import com.example.domain.service.events.EventRequirementEvaluator
import com.example.domain.service.events.SkillCheckResolver
import com.example.ui.components.DangerBadge
import com.example.ui.components.GameCard
import com.example.ui.theme.*

@Composable
fun EventCategoryBadge(
    category: EventCategory,
    modifier: Modifier = Modifier
) {
    Surface(
        color = category.badgeColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, category.badgeColor.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val iconVector = when (category) {
                EventCategory.DISCOVERY -> Icons.Default.Explore
                EventCategory.RESOURCE -> Icons.Default.Inventory2
                EventCategory.ENVIRONMENT -> Icons.Default.Landscape
                EventCategory.ENCOUNTER -> Icons.Default.Groups
                EventCategory.TECHNICAL -> Icons.Default.Build
                EventCategory.CHARACTER -> Icons.Default.Person
                EventCategory.SPECIAL -> Icons.Default.Star
            }
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = category.badgeColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category.titleRu.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = category.badgeColor,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
fun EventRarityBadge(
    rarity: EventRarity,
    modifier: Modifier = Modifier
) {
    Surface(
        color = rarity.badgeColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, rarity.badgeColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Text(
            text = rarity.titleRu,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = rarity.badgeColor
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

/**
 * Atmospheric Canvas-rendered vector backdrop corresponding to event theme.
 */
@Composable
fun EventIllustrationBanner(
    visualAssetId: String?,
    category: EventCategory,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        FrontierDarkSurfaceElevated,
                        FrontierDarkBackground
                    )
                )
            )
            .border(1.dp, FrontierBorder, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background Atmospheric Horizon Glow
            val glowColor = category.badgeColor.copy(alpha = 0.22f)
            drawCircle(
                color = glowColor,
                radius = h * 0.9f,
                center = Offset(w * 0.5f, h * 0.85f)
            )

            // Grid / Horizon Lines
            val gridColor = FrontierBorder.copy(alpha = 0.4f)
            for (i in 1..4) {
                val y = h * 0.5f + (i * h * 0.12f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.5f
                )
            }

            // Thematic silhouettes
            when (visualAssetId) {
                "evt_warehouse", "evt_tunnel", "evt_vault" -> {
                    // Industrial building silhouette
                    val p = Path().apply {
                        moveTo(w * 0.15f, h * 0.9f)
                        lineTo(w * 0.15f, h * 0.4f)
                        lineTo(w * 0.35f, h * 0.25f)
                        lineTo(w * 0.65f, h * 0.25f)
                        lineTo(w * 0.85f, h * 0.4f)
                        lineTo(w * 0.85f, h * 0.9f)
                        close()
                    }
                    drawPath(p, color = FrontierDarkSurfaceHighlight)

                    // Door outline
                    drawRect(
                        color = category.badgeColor.copy(alpha = 0.6f),
                        topLeft = Offset(w * 0.42f, h * 0.55f),
                        size = Size(w * 0.16f, h * 0.35f)
                    )
                }
                "evt_truck", "evt_obstacle", "evt_machinery" -> {
                    // Vehicle / machine outline
                    drawRoundRect(
                        color = FrontierDarkSurfaceHighlight,
                        topLeft = Offset(w * 0.2f, h * 0.45f),
                        size = Size(w * 0.6f, h * 0.4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                    drawCircle(
                        color = FrontierBorderLight,
                        radius = h * 0.14f,
                        center = Offset(w * 0.35f, h * 0.85f)
                    )
                    drawCircle(
                        color = FrontierBorderLight,
                        radius = h * 0.14f,
                        center = Offset(w * 0.65f, h * 0.85f)
                    )
                }
                "evt_campfire", "evt_camp", "evt_shelter" -> {
                    // Campfire / Shelter silhouette
                    val p = Path().apply {
                        moveTo(w * 0.3f, h * 0.9f)
                        lineTo(w * 0.5f, h * 0.3f)
                        lineTo(w * 0.7f, h * 0.9f)
                        close()
                    }
                    drawPath(p, color = FrontierDarkSurfaceHighlight)
                    drawCircle(
                        color = FuelAmber.copy(alpha = 0.8f),
                        radius = h * 0.12f,
                        center = Offset(w * 0.5f, h * 0.78f)
                    )
                }
                else -> {
                    // Ruins / Discovery radar pulse
                    drawCircle(
                        color = TechCyan.copy(alpha = 0.15f),
                        radius = h * 0.4f,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                    drawCircle(
                        color = TechCyan.copy(alpha = 0.35f),
                        radius = h * 0.22f,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                }
            }
        }

        // Overlay category watermark icon
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(FrontierDarkSurface.copy(alpha = 0.85f))
                .border(1.dp, category.badgeColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (category) {
                    EventCategory.DISCOVERY -> Icons.Default.Explore
                    EventCategory.RESOURCE -> Icons.Default.Inventory2
                    EventCategory.ENVIRONMENT -> Icons.Default.Landscape
                    EventCategory.ENCOUNTER -> Icons.Default.Groups
                    EventCategory.TECHNICAL -> Icons.Default.Build
                    EventCategory.CHARACTER -> Icons.Default.Person
                    EventCategory.SPECIAL -> Icons.Default.Star
                },
                contentDescription = null,
                tint = category.badgeColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Interactive Choice Card with requirement checks, skill check odds, character picker, and cost chips.
 */
@Composable
fun EventChoiceCard(
    choice: EventChoice,
    choiceIndex: Int,
    gameState: GameState,
    expedition: Expedition,
    selectedActorId: String?,
    onSelectActor: (String) -> Unit,
    onChoose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reqResult = remember(choice.requirements, gameState, expedition) {
        EventRequirementEvaluator.evaluate(
            requirements = choice.requirements,
            gameState = gameState,
            expedition = expedition
        )
    }

    val isLocked = !reqResult.isMet

    val actor = remember(selectedActorId, expedition.squad) {
        if (selectedActorId != null) {
            expedition.squad.find { it.id == selectedActorId } ?: expedition.leader ?: expedition.squad.first()
        } else {
            expedition.leader ?: expedition.squad.first()
        }
    }

    var showActorSelectorDialog by remember { mutableStateOf(false) }

    if (showActorSelectorDialog && choice.skillCheck != null) {
        ActorSelectionDialog(
            squad = expedition.squad,
            selectedActorId = actor.id,
            skillCheck = choice.skillCheck,
            inventoryItems = gameState.inventoryItems,
            onSelect = { charId ->
                onSelectActor(charId)
                showActorSelectorDialog = false
            },
            onDismiss = { showActorSelectorDialog = false }
        )
    }

    val cardBorder = when {
        isLocked -> BorderStroke(1.dp, BorderSubtle)
        choice.successOutcome.requiresCombat -> BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.8f))
        choice.skillCheck != null -> BorderStroke(1.dp, WarningAmber.copy(alpha = 0.8f))
        else -> BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f))
    }

    val cardBg = when {
        isLocked -> FrontierDarkSurface.copy(alpha = 0.6f)
        choice.successOutcome.requiresCombat -> FrontierDarkSurfaceElevated
        else -> FrontierDarkSurfaceElevated
    }

    Surface(
        color = cardBg,
        shape = RoundedCornerShape(12.dp),
        border = cardBorder,
        modifier = modifier
            .fillMaxWidth()
            .testTag("choice_card_${choice.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Choice title & Risk badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isLocked) BorderSubtle else TechCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${choiceIndex + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isLocked) TextMuted else TechCyan
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = choice.text,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) TextMuted else TextWhite
                        )
                    )
                }

                if (choice.riskLevelText != null) {
                    Surface(
                        color = if (choice.successOutcome.requiresCombat) DangerCrimson.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (choice.successOutcome.requiresCombat) DangerCrimson.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = choice.riskLevelText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (choice.successOutcome.requiresCombat) DangerCrimson else WarningAmber
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Description
            if (choice.description != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = choice.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isLocked) TextSubtle else TextMuted,
                        lineHeight = 16.sp
                    )
                )
            }

            // Lock reason if conditions not met
            if (isLocked && reqResult.lockDescription != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = DangerCrimson.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = DangerCrimson,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = reqResult.lockDescription,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DangerCrimson,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Skill check details & Actor Selector
            if (choice.skillCheck != null && !isLocked) {
                val sc = choice.skillCheck
                val oddsText = SkillCheckResolver.estimateSuccessOdds(actor, sc, gameState.inventoryItems)

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = FrontierDarkBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, FrontierBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Проверка: ${sc.statType.titleRu} (Сложн. ${sc.difficulty})",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber
                                    )
                                )
                            }
                            Text(
                                text = oddsText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (oddsText.contains("Высокий")) SafeEmerald else WarningAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Active character performing check
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(FrontierDarkSurfaceHighlight)
                                .clickable { showActorSelectorDialog = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TechCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Исполнитель: ${actor.name} (${actor.role.titleRu})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Сменить",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TechCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TechCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Supply & Consumables Cost Chips
            if (choice.costResources.isNotEmpty() || choice.consumedItemIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    choice.costResources.forEach { (res, amt) ->
                        Surface(
                            color = FuelAmber.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, FuelAmber.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Расход: -$amt ${res.nameRu}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = FuelAmber,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Action Execute Button
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onChoose,
                enabled = !isLocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (choice.successOutcome.requiresCombat) DangerCrimson else FrontierPrimary,
                    contentColor = if (choice.successOutcome.requiresCombat) TextWhite else FrontierOnPrimary,
                    disabledContainerColor = FrontierDarkBackground,
                    disabledContentColor = TextSubtle
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_execute_choice_${choice.id}")
            ) {
                val btnIcon = when {
                    isLocked -> Icons.Default.Lock
                    choice.successOutcome.requiresCombat -> Icons.Default.Shield
                    choice.skillCheck != null -> Icons.Default.Casino
                    else -> Icons.Default.Check
                }
                Icon(imageVector = btnIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLocked) "Условие заблокировано" else "Выбрать это действие",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Character Selection Modal to choose which squad member takes the skill check.
 */
@Composable
fun ActorSelectionDialog(
    squad: List<Character>,
    selectedActorId: String,
    skillCheck: SkillCheckRequirement,
    inventoryItems: List<WarehouseItem>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FrontierDarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, FrontierBorderLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Выбор исполнителя",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted)
                    }
                }

                Text(
                    text = "Проверка: ${skillCheck.statType.titleRu} (Сложность: ${skillCheck.difficulty})",
                    style = MaterialTheme.typography.bodySmall.copy(color = WarningAmber)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    squad.forEach { member ->
                        val isSelected = member.id == selectedActorId
                        val effectiveStats = member.getEffectiveStats(inventoryItems)
                        val statVal = when (skillCheck.statType) {
                            CharacterStatType.ATTACK -> effectiveStats.attack
                            CharacterStatType.DEFENSE -> effectiveStats.defense
                            CharacterStatType.SCAVENGING -> effectiveStats.scavengingSkill
                            CharacterStatType.ENGINEERING -> effectiveStats.engineeringSkill
                            CharacterStatType.MEDICAL -> effectiveStats.medicalSkill
                            CharacterStatType.MAX_HEALTH -> member.getEffectiveMaxHealth(inventoryItems) / 10
                        }
                        val odds = SkillCheckResolver.estimateSuccessOdds(member, skillCheck, inventoryItems)
                        val hasRoleBonus = skillCheck.applicableRoles.contains(member.role)

                        Surface(
                            color = if (isSelected) TechCyan.copy(alpha = 0.15f) else FrontierDarkBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) TechCyan else FrontierBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(member.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) TechCyan else TextWhite
                                        )
                                    )
                                    Text(
                                        text = "${member.role.titleRu} • ${skillCheck.statType.titleRu}: $statVal ${if (hasRoleBonus) "(+3 Роль)" else ""}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Surface(
                                    color = if (odds.contains("Высокий")) SafeEmerald.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = odds,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (odds.contains("Высокий")) SafeEmerald else WarningAmber,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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

/**
 * Result Outcome Card showing detailed narrative, dice rolls, loot breakdown, and cargo limits.
 */
@Composable
fun EventOutcomeDisplay(
    activeState: ActiveEventState,
    expedition: Expedition,
    onContinueExploration: () -> Unit,
    onStartCombat: () -> Unit,
    onReturnToBase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outcome = activeState.resolvedOutcome ?: return
    val skillResult = activeState.resolvedSkillCheckResult
    val isSuccess = skillResult?.isSuccess ?: true
    val isCombat = activeState.event.choices.find { it.id == activeState.selectedChoiceId }?.successOutcome?.requiresCombat == true

    val statusColor = when {
        isCombat -> DangerCrimson
        skillResult?.isCriticalSuccess == true -> CreditsYellow
        skillResult?.isCriticalFailure == true -> DangerCrimson
        isSuccess -> SafeEmerald
        else -> WarningAmber
    }

    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = statusColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Header Result Title
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
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = outcome.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        )
                        Text(
                            text = if (isSuccess) "Действие выполнено успешно" else "Неудача при проверке навыка",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }
                }
            }

            // Skill Check Roll Breakdown
            if (skillResult != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = FrontierDarkBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, FrontierBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = skillResult.explanation,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Narrative Story Outcome
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = outcome.narrativeText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextWhite,
                    lineHeight = 20.sp
                )
            )

            // Gained Resources / Loot Tags
            if (activeState.awardedResources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "📦 Полученная добыча (в багажный отсек):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SafeEmerald,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activeState.awardedResources.forEach { (res, amt) ->
                        Surface(
                            color = SafeEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "+$amt ${res.nameRu}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SafeEmerald
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Dropped Loot Warning if cargo was exceeded
            if (activeState.droppedResourcesDueToCapacity.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = WarningAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Багажник переполнен! Не удалось загрузить: ${activeState.droppedResourcesDueToCapacity.entries.joinToString { "${it.value} ${it.key.nameRu}" }}",
                            style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber)
                        )
                    }
                }
            }

            // XP and Exploration Gains
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (outcome.xpReward > 0) {
                    Text(
                        text = "⭐ Опыт: +${outcome.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                if (outcome.explorationProgressGain > 0) {
                    Text(
                        text = "🗺️ Исследование сектора: +${outcome.explorationProgressGain}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SafeEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(16.dp))
            if (isCombat) {
                Button(
                    onClick = onStartCombat,
                    colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_outcome_combat")
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Вступить в бой!",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onContinueExploration,
                        colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_outcome_continue")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Продолжить поиск",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = FrontierOnPrimary
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = onReturnToBase,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                        border = BorderStroke(1.dp, TechCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_outcome_return")
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Вернуться на базу",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
