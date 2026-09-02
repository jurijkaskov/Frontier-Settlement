package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.domain.model.Settlement
import com.example.ui.theme.*

/**
 * Dialog displaying settlement event logs and radio communications chronicle.
 */
@Composable
fun SettlementEventsDialog(
    logs: List<String>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FrontierDarkSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_settlement_events")
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Podcasts,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Радиоэфир и сводка событий",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = FrontierBorder)

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (logs.isEmpty()) {
                        item {
                            Text(
                                text = "Эфир чист. Новых сводок нет.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                            )
                        }
                    } else {
                        items(logs) { log ->
                            Surface(
                                color = FrontierDarkSurfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = null,
                                        tint = if (log.contains("Внимание") || log.contains("бой")) DangerCrimson else TechCyan,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = log,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextWhite,
                                            lineHeight = 16.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_events_close_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FrontierDarkSurfaceHighlight,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Закрыть журнал")
                }
            }
        }
    }
}

/**
 * Dialog displaying full outpost status report when the player inspects the central scene.
 */
@Composable
fun SettlementInfoDialog(
    settlement: Settlement,
    gameState: GameState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FrontierDarkSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_settlement_info")
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SafeEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = settlement.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            Text(
                                text = "Паспорт объекта • ${settlement.tier.titleRu}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = FrontierBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Stats breakdown
                Text(
                    text = "ТАКТИЧЕСКИЕ ПОКАЗАТЕЛИ И ПРОГРЕСС",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(label = "Ранг поселения", value = settlement.tier.titleRu, color = TechCyan)
                InfoRow(label = "Уровень базы", value = "Ур. ${settlement.level}", color = SafeEmerald)
                InfoRow(label = "Опыт развития (XP)", value = "${settlement.xp} / ${settlement.xpToNextLevel} XP", color = TechCyan)
                InfoRow(label = "Построено объектов", value = "${settlement.constructedBuildingsCount} / ${settlement.buildings.size} шт.", color = TextWhite)
                InfoRow(label = "Суммарный уровень зданий", value = "${settlement.totalBuildingLevels} ур.", color = SafeEmerald)
                InfoRow(label = "Численность населения", value = "${settlement.population} / ${settlement.maxPopulation} чел.", color = FoodGreen)
                InfoRow(label = "Защитный периметр", value = "${settlement.defenseRating} ед.", color = DangerCrimson)
                InfoRow(label = "Репутация", value = "${settlement.reputation} очков", color = CreditsYellow)
                InfoRow(label = "Суточный расход", value = "-${settlement.dailyFoodConsumption} еда, -${settlement.dailyWaterConsumption} вода", color = WarningAmber)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = FrontierDarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Бонус ранга (${settlement.tier.titleRu}):",
                            style = MaterialTheme.typography.labelSmall.copy(color = SafeEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = settlement.tier.perkDescription,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_info_close_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafeEmerald,
                        contentColor = FrontierOnPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Понятно",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 12.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        )
    }
}
