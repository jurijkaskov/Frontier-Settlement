package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.DailySummary
import com.example.domain.model.DayPeriod
import com.example.domain.model.GameDateTime
import com.example.domain.model.GameState
import com.example.ui.theme.*

/**
 * Tactical in-game time inspector and manual time advancement dialog.
 */
@Composable
fun TimeManagementDialog(
    gameState: GameState,
    onAdvanceHours: (Int) -> Unit,
    onNextDayClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateTime = gameState.gameDateTime
    val period = dateTime.dayPeriod
    val lastSummary = gameState.lastDailySummary

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FrontierDarkSurface,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, TechCyan.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_time_management")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ИГРОВОЕ ВРЕМЯ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                letterSpacing = 1.sp
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

                // Current Time Display Card
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "День ${dateTime.day}",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                )
                                Text(
                                    text = "%02d:%02d".format(dateTime.hour, dateTime.minute),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TechCyan,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            // Day period badge
                            Surface(
                                color = when (period) {
                                    DayPeriod.MORNING -> FuelAmber.copy(alpha = 0.2f)
                                    DayPeriod.DAY -> CreditsYellow.copy(alpha = 0.2f)
                                    DayPeriod.EVENING -> WarningAmber.copy(alpha = 0.2f)
                                    DayPeriod.NIGHT -> TechCyan.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (period) {
                                        DayPeriod.MORNING -> FuelAmber
                                        DayPeriod.DAY -> CreditsYellow
                                        DayPeriod.EVENING -> WarningAmber
                                        DayPeriod.NIGHT -> TechCyan
                                    }
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = when (period) {
                                            DayPeriod.MORNING -> Icons.Default.WbTwilight
                                            DayPeriod.DAY -> Icons.Default.WbSunny
                                            DayPeriod.EVENING -> Icons.Default.WbTwilight
                                            DayPeriod.NIGHT -> Icons.Default.NightsStay
                                        },
                                        contentDescription = null,
                                        tint = TextWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = period.titleRu,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    )
                                }
                            }
                        }

                        // Day progress bar
                        val dayFraction = (dateTime.hour * 60 + dateTime.minute).toFloat() / 1440f
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("00:00 (Ночь)", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                                Text("06:00 (Утро)", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                                Text("12:00 (День)", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                                Text("18:00 (Вечер)", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                            }
                            LinearProgressIndicator(
                                progress = { dayFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = TechCyan,
                                trackColor = FrontierDarkBackground
                            )
                        }
                    }
                }

                // Time Controls
                Text(
                    text = "ПРОМОТКА ВРЕМЕНИ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAdvanceHours(1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_advance_1h"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorderLight)
                    ) {
                        Text("+1 час", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onAdvanceHours(4) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_advance_4h"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorderLight)
                    ) {
                        Text("+4 часа", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = onNextDayClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_dialog_next_day"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafeEmerald,
                        contentColor = FrontierOnPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Завершить день (До 08:00)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Daily Report Summary Card
                if (lastSummary != null) {
                    Text(
                        text = "СВОДКА ЗА ДЕНЬ ${lastSummary.day}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SafeEmerald,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Surface(
                        color = FrontierDarkBackground,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DailyStatRow("Производство еды", "+${lastSummary.foodProduced}", FoodGreen)
                            DailyStatRow("Потребление еды", "-${lastSummary.foodConsumed}", if (lastSummary.isStarving) DangerCrimson else TextMuted)
                            DailyStatRow("Добыча воды", "+${lastSummary.waterProduced}", WaterCyan)
                            DailyStatRow("Потребление воды", "-${lastSummary.waterConsumed}", if (lastSummary.isDehydrated) DangerCrimson else TextMuted)
                            DailyStatRow("Материалы базы", "+${lastSummary.materialsProduced}", MaterialsOrange)
                            DailyStatRow("Торговый доход", "+${lastSummary.creditsProduced} кр.", CreditsYellow)
                            if (lastSummary.charactersHealedCount > 0) {
                                DailyStatRow("Медпункт / Лечение", "${lastSummary.charactersHealedCount} бойцов восстановлено", SafeEmerald)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyStatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp))
    }
}
