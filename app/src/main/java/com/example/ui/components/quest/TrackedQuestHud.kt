package com.example.ui.components.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.domain.model.quest.ObjectiveStatus
import com.example.domain.service.quest.QuestCatalog
import com.example.ui.theme.*

@Composable
fun TrackedQuestHud(
    gameState: GameState,
    onNavigateToQuests: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trackedId = gameState.trackedQuestId
        ?: gameState.questStates.values.firstOrNull { it.isActive || it.status == QuestStatus.READY_TO_CLAIM }?.questId
        ?: return

    val def = QuestCatalog.get(trackedId) ?: return
    val questState = gameState.questStates[trackedId] ?: return

    val isReadyToClaim = questState.status == QuestStatus.READY_TO_CLAIM

    // Find current active objective
    val currentObj = def.objectives.firstOrNull { objDef ->
        val prog = questState.objectiveProgress[objDef.id]
        prog?.status == ObjectiveStatus.IN_PROGRESS
    } ?: def.objectives.lastOrNull()

    val objProg = currentObj?.let { questState.objectiveProgress[it.id] }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                1.dp,
                if (isReadyToClaim) SafeEmerald else TechCyan.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp)
            )
            .clickable { onNavigateToQuests() }
            .testTag("tracked_quest_hud"),
        color = if (isReadyToClaim) FrontierDarkSurfaceElevated else FrontierDarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = if (isReadyToClaim) SafeEmerald else TechCyan,
                    modifier = Modifier.size(20.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = def.titleRu,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isReadyToClaim) {
                            Surface(
                                color = SafeEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ГОТОВО",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SafeEmerald,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    if (currentObj != null) {
                        Text(
                            text = if (isReadyToClaim) "Сдать задание в штабе базы" else currentObj.descriptionRu,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isReadyToClaim) SafeEmerald else TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (currentObj != null && currentObj.requiredAmount > 1 && !isReadyToClaim) {
                        val cur = objProg?.currentAmount ?: 0
                        LinearProgressIndicator(
                            progress = { (cur.toFloat() / currentObj.requiredAmount.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = TechCyan,
                            trackColor = FrontierDarkBackground
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "К заданиям",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
