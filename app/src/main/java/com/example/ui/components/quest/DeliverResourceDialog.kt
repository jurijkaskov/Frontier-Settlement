package com.example.ui.components.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.window.Dialog
import com.example.domain.model.GameState
import com.example.domain.model.ResourceType
import com.example.domain.model.quest.QuestDefinition
import com.example.domain.model.quest.QuestObjectiveDefinition
import com.example.domain.model.quest.QuestObjectiveProgress
import com.example.ui.theme.*

@Composable
fun DeliverResourceDialog(
    questDef: QuestDefinition,
    objDef: QuestObjectiveDefinition,
    objProg: QuestObjectiveProgress?,
    gameState: GameState,
    onDeliver: (amount: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val resType = ResourceType.entries.find { it.name.equals(objDef.targetId, ignoreCase = true) }
        ?: ResourceType.MATERIALS

    val available = gameState.resources[resType]

    val curAmount = objProg?.currentAmount ?: 0
    val neededTotal = objDef.requiredAmount
    val neededRemaining = (neededTotal - curAmount).coerceAtLeast(0)
    val maxDeliverable = minOf(available, neededRemaining)

    var deliverAmount by remember { mutableStateOf(maxDeliverable.coerceAtLeast(1)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("deliver_resource_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FrontierDarkSurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorderLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Передача ресурсов",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted)
                    }
                }

                Text(
                    text = "Задание: «${questDef.titleRu}»\nЦель: ${objDef.descriptionRu}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Status info box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FrontierDarkBackground)
                        .border(1.dp, FrontierBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ресурс:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text("${resType.symbol} ${resType.nameRu}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("На складе базы:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text("$available ед.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = if (available > 0) SafeEmerald else DangerCrimson)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Сдано / Требуется:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text("$curAmount / $neededTotal ед.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TechCyan)
                        }
                    }
                }

                if (available <= 0) {
                    Text(
                        text = "На складе нет нужного ресурса (${resType.nameRu}) для передачи.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DangerCrimson
                    )
                } else {
                    // Quantity selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (deliverAmount > 1) deliverAmount -= 1 },
                            enabled = deliverAmount > 1,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FrontierDarkSurface)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Меньше", tint = TextWhite)
                        }

                        Text(
                            text = "$deliverAmount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CreditsYellow,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        IconButton(
                            onClick = { if (deliverAmount < maxDeliverable) deliverAmount += 1 },
                            enabled = deliverAmount < maxDeliverable,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FrontierDarkSurface)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Больше", tint = TextWhite)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        TextButton(
                            onClick = { deliverAmount = maxDeliverable }
                        ) {
                            Text("Все ($maxDeliverable)", color = TechCyan, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = {
                            onDeliver(deliverAmount)
                            onDismiss()
                        },
                        enabled = available > 0 && deliverAmount > 0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_deliver_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafeEmerald,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Передать", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
