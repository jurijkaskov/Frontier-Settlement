package com.example.ui.components.quest

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GameState
import com.example.domain.model.QuestStatus
import com.example.domain.model.ResourceType
import com.example.domain.model.quest.*
import com.example.domain.service.quest.QuestCompletionEvaluator
import com.example.domain.service.quest.QuestRequirementEvaluator
import com.example.ui.theme.*

@Composable
fun QuestCard(
    definition: QuestDefinition,
    questState: QuestState?,
    gameState: GameState,
    isTracked: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onTurnIn: () -> Unit,
    onTrackToggle: () -> Unit,
    onOpenDeliverResource: (objDef: QuestObjectiveDefinition, objProg: QuestObjectiveProgress?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val status = questState?.status ?: QuestStatus.LOCKED
    val isReadyToClaim = questState?.status == QuestStatus.READY_TO_CLAIM ||
            (questState?.isActive == true && QuestCompletionEvaluator.isReadyForCompletion(definition, questState))

    val borderColor = when {
        isReadyToClaim -> SafeEmerald
        status == QuestStatus.ACTIVE || status == QuestStatus.IN_PROGRESS -> TechCyan.copy(alpha = 0.6f)
        status == QuestStatus.AVAILABLE -> WarningAmber.copy(alpha = 0.5f)
        status == QuestStatus.COMPLETED -> FrontierBorder
        status == QuestStatus.FAILED || status == QuestStatus.EXPIRED -> DangerCrimson.copy(alpha = 0.5f)
        else -> FrontierBorder
    }

    val cardBg = when {
        isReadyToClaim -> FrontierDarkSurfaceElevated
        status == QuestStatus.ACTIVE || status == QuestStatus.IN_PROGRESS -> FrontierDarkSurface
        else -> FrontierDarkSurface.copy(alpha = 0.85f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .testTag("quest_card_${definition.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Category Badge + Status Badge + Track Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    Surface(
                        color = Color(definition.category.badgeColorHex).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(definition.category.badgeColorHex).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = definition.category.titleRu.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(definition.category.badgeColorHex),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (definition.repeatability != QuestRepeatability.ONCE) {
                        Surface(
                            color = TechCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Контракт",
                                style = MaterialTheme.typography.labelSmall,
                                color = TechCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    val (statusLabel, statusColor) = when (status) {
                        QuestStatus.READY_TO_CLAIM -> "ГОТОВО К СДАЧЕ" to SafeEmerald
                        QuestStatus.ACTIVE, QuestStatus.IN_PROGRESS -> "В ПРОЦЕССЕ" to TechCyan
                        QuestStatus.AVAILABLE -> "ДОСТУПНО" to WarningAmber
                        QuestStatus.COMPLETED -> "ЗАВЕРШЕНО" to TextMuted
                        QuestStatus.FAILED -> "ПРОВАЛЕНО" to DangerCrimson
                        QuestStatus.EXPIRED -> "ИСТЕКЛО" to DangerCrimson
                        QuestStatus.DECLINED -> "ОТКЛОНЕНО" to TextMuted
                        QuestStatus.LOCKED -> "ЗАБЛОКИРОВАНО" to TextSubtle
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (status == QuestStatus.ACTIVE || status == QuestStatus.IN_PROGRESS || isReadyToClaim) {
                        IconButton(
                            onClick = onTrackToggle,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("track_toggle_${definition.id}")
                        ) {
                            Icon(
                                imageVector = if (isTracked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isTracked) "Отслеживается" else "Отслеживать",
                                tint = if (isTracked) CreditsYellow else TextMuted
                            )
                        }
                    }
                }
            }

            // Title and Giver
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = definition.titleRu,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                if (definition.giverNameRu != null) {
                    Text(
                        text = "Заказчик: ${definition.giverNameRu}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Description
            Text(
                text = definition.descriptionRu,
                style = MaterialTheme.typography.bodySmall,
                color = TextWhite.copy(alpha = 0.85f),
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            // Objectives Progress List
            if (definition.objectives.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FrontierDarkBackground.copy(alpha = 0.6f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ЦЕЛИ ЗАДАНИЯ:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    definition.objectives.forEach { objDef ->
                        val objProg = questState?.objectiveProgress?.get(objDef.id)
                        val isObjDone = objProg?.status == ObjectiveStatus.COMPLETED
                        val isObjInProgress = objProg?.status == ObjectiveStatus.IN_PROGRESS
                        val isLockedStage = objProg?.status == ObjectiveStatus.NOT_STARTED

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        isObjDone -> Icons.Default.CheckCircle
                                        isObjInProgress -> Icons.Default.RadioButtonUnchecked
                                        else -> Icons.Default.Lock
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isObjDone -> SafeEmerald
                                        isObjInProgress -> TechCyan
                                        else -> TextSubtle
                                    },
                                    modifier = Modifier.size(16.dp)
                                )

                                Column {
                                    Text(
                                        text = objDef.descriptionRu + if (objDef.optional) " (Опционально)" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isObjInProgress) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isObjDone) TextMuted else if (isLockedStage) TextSubtle else TextWhite
                                    )

                                    if (objDef.requiredAmount > 1 && !isObjDone) {
                                        val cur = objProg?.currentAmount ?: 0
                                        Text(
                                            text = "Прогресс: $cur / ${objDef.requiredAmount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TechCyan
                                        )
                                    }
                                }
                            }

                            // Deliver Resource Action Button if applicable
                            if ((objDef.type == QuestObjectiveType.DELIVER_RESOURCE || objDef.type == QuestObjectiveType.DELIVER_ITEM)
                                && (status == QuestStatus.ACTIVE || status == QuestStatus.IN_PROGRESS)
                                && !isObjDone
                            ) {
                                Button(
                                    onClick = { onOpenDeliverResource(objDef, objProg) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TechCyan,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Сдать", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Rewards Preview Section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "НАГРАДА:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val rewards = definition.rewards
                    if (rewards.credits > 0) {
                        RewardPill(text = "+${rewards.credits} Кр", color = CreditsYellow)
                    }
                    if (rewards.xp > 0) {
                        RewardPill(text = "+${rewards.xp} XP", color = TechCyan)
                    }
                    if (rewards.reputationDelta > 0) {
                        RewardPill(text = "+${rewards.reputationDelta} Реп.", color = SafeEmerald)
                    }
                    rewards.factionRelationDeltas.forEach { (fId, delta) ->
                        if (delta > 0) {
                            RewardPill(text = "+$delta Фракция", color = WarningAmber)
                        }
                    }
                    rewards.resources.forEach { (res, amt) ->
                        if (amt > 0) {
                            RewardPill(text = "+$amt ${res.symbol}", color = MaterialsOrange)
                        }
                    }
                }
            }

            // Deadline / Time Limit Indicator
            if (questState?.deadlineGameDateTime != null && (status == QuestStatus.ACTIVE || status == QuestStatus.IN_PROGRESS)) {
                val remainingDays = (questState.deadlineGameDateTime.day - gameState.day).coerceAtLeast(0)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Осталось времени: $remainingDays дн. (до Дня ${questState.deadlineGameDateTime.day})",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningAmber
                    )
                }
            }

            // Failure reason if failed
            if ((status == QuestStatus.FAILED || status == QuestStatus.EXPIRED) && questState?.failureReasonRu != null) {
                Text(
                    text = "Причина провала: ${questState.failureReasonRu}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DangerCrimson
                )
            }

            // Missing Requirements for Available/Locked Quests
            val reqEval = remember(definition, gameState) {
                QuestRequirementEvaluator.evaluate(definition, gameState)
            }
            if (!reqEval.isMet && (status == QuestStatus.AVAILABLE || status == QuestStatus.LOCKED)) {
                Surface(
                    color = DangerCrimson.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("ТРЕБОВАНИЯ ДЛЯ ПРИНЯТИЯ:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DangerCrimson)
                        reqEval.unmetRequirements.forEach { req ->
                            Text("• $req", style = MaterialTheme.typography.labelSmall, color = TextWhite.copy(alpha = 0.9f))
                        }
                    }
                }
            }

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isReadyToClaim -> {
                        Button(
                            onClick = onTurnIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("claim_quest_${definition.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SafeEmerald,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("СДАТЬ ЗАДАНИЕ И ПОЛУЧИТЬ НАГРАДУ", fontWeight = FontWeight.Bold)
                        }
                    }

                    status == QuestStatus.AVAILABLE -> {
                        if (definition.canDecline) {
                            OutlinedButton(
                                onClick = onDecline,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("decline_quest_${definition.id}"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                            ) {
                                Text("Отклонить")
                            }
                        }

                        Button(
                            onClick = onAccept,
                            enabled = reqEval.isMet,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(40.dp)
                                .testTag("accept_quest_${definition.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (reqEval.isMet) FrontierPrimary else FrontierDarkSurfaceElevated,
                                contentColor = if (reqEval.isMet) Color.Black else TextMuted
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Принять задание", fontWeight = FontWeight.Bold)
                        }
                    }

                    else -> {
                        // Expand/Collapse lore toggle
                        TextButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (expanded) "Свернуть подробности" else "Подробнее о задании...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TechCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardPill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
