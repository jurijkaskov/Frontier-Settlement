package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DayPeriod
import com.example.domain.model.GameDateTime
import com.example.domain.model.GameResources
import com.example.domain.model.Settlement
import com.example.ui.theme.*

/**
 * Reusable tactical Game HUD displayed at the top of game screens.
 * Displays day, time of day indicator, credits, reputation, events/radio notifications,
 * fast-forward day button, and a horizontally scrollable resource bar with deficit alerts.
 */
@Composable
fun GameTopHUD(
    day: Int = 1,
    resources: GameResources,
    settlement: Settlement,
    onNextDayClick: () -> Unit,
    onWarehouseClick: () -> Unit,
    onEventsClick: () -> Unit,
    onMenuClick: () -> Unit,
    gameDateTime: GameDateTime = GameDateTime(day = day, hour = 8, minute = 0),
    onTimeClick: (() -> Unit)? = null,
    onEconomyClick: (() -> Unit)? = null,
    hasUnreadEvents: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Determine time of day cycle from authoritative GameDateTime
    val period = gameDateTime.dayPeriod
    val timeOfDayName = period.titleRu
    val timeOfDayIcon = when (period) {
        DayPeriod.MORNING -> Icons.Default.WbTwilight
        DayPeriod.DAY -> Icons.Default.WbSunny
        DayPeriod.EVENING -> Icons.Default.WbTwilight
        DayPeriod.NIGHT -> Icons.Default.NightsStay
    }
    val timeOfDayColor = when (period) {
        DayPeriod.MORNING -> FuelAmber
        DayPeriod.DAY -> CreditsYellow
        DayPeriod.EVENING -> WarningAmber
        DayPeriod.NIGHT -> TechCyan
    }

    // Reputation rank title
    val reputationRank = when {
        settlement.reputation >= 80 -> "Оплот надежды"
        settlement.reputation >= 50 -> "Уважаемый аванпост"
        settlement.reputation >= 25 -> "Доверие"
        else -> "Неизвестный лагерь"
    }

    Surface(
        color = FrontierDarkSurface,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = FrontierBorder,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 10.dp, start = 12.dp, end = 12.dp)
        ) {
            // Row 1: Day, Time of Day, Credits, Reputation, Events, Menu, Next Day Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left cluster: Day counter & Time of day
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorderLight),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .then(if (onTimeClick != null) Modifier.clickable { onTimeClick() } else Modifier)
                            .testTag("hud_time_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = timeOfDayIcon,
                                contentDescription = timeOfDayName,
                                tint = timeOfDayColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Д.${gameDateTime.day} %02d:%02d".format(gameDateTime.hour, gameDateTime.minute),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "($timeOfDayName)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = timeOfDayColor.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Credits pill
                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreditsYellow.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .then(if (onEconomyClick != null) Modifier.clickable { onEconomyClick() } else Modifier)
                            .testTag("hud_credits_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Казна аванпоста",
                                tint = CreditsYellow,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${resources.money}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // Right cluster: Reputation mini-chip, Notification & Next Day Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Radio / Events Button with indicator
                    IconButton(
                        onClick = onEventsClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FrontierDarkSurfaceHighlight)
                            .border(1.dp, if (hasUnreadEvents) WarningAmber else FrontierBorder, RoundedCornerShape(8.dp))
                            .testTag("hud_btn_events")
                    ) {
                        BadgedBox(
                            badge = {
                                if (hasUnreadEvents) {
                                    Badge(
                                        containerColor = WarningAmber,
                                        modifier = Modifier.size(6.dp)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "События и радиоэфир",
                                tint = if (hasUnreadEvents) WarningAmber else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Next Day Button
                    Button(
                        onClick = onNextDayClick,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_next_day"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafeEmerald,
                            contentColor = FrontierOnPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Следующий день",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+1 День",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Resources Bar with Deficit Alerts & Storage Capacity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Water
                val isWaterCritical = resources.water < settlement.dailyWaterConsumption
                HudResourceChip(
                    icon = Icons.Default.WaterDrop,
                    label = "Вода",
                    value = "${resources.water}",
                    color = WaterCyan,
                    isCritical = isWaterCritical,
                    warningHint = if (isWaterCritical) "Дефицит!" else null
                )

                // Food
                val isFoodCritical = resources.food < settlement.dailyFoodConsumption
                HudResourceChip(
                    icon = Icons.Default.Restaurant,
                    label = "Еда",
                    value = "${resources.food}",
                    color = FoodGreen,
                    isCritical = isFoodCritical,
                    warningHint = if (isFoodCritical) "Дефицит!" else null
                )

                // Materials
                HudResourceChip(
                    icon = Icons.Default.Build,
                    label = "Материалы",
                    value = "${resources.materials}",
                    color = MaterialsOrange,
                    isCritical = false
                )

                // Fuel
                val isFuelCritical = resources.fuel < 10
                HudResourceChip(
                    icon = Icons.Default.LocalGasStation,
                    label = "Топливо",
                    value = "${resources.fuel}",
                    color = FuelAmber,
                    isCritical = isFuelCritical,
                    warningHint = if (isFuelCritical) "Мало!" else null
                )

                // Storage Capacity
                HudStorageChip(
                    current = resources.totalStoredVolume,
                    max = resources.warehouseMaxCapacity,
                    onClick = onWarehouseClick
                )
            }
        }
    }
}

@Composable
fun HudResourceChip(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    isCritical: Boolean = false,
    warningHint: String? = null
) {
    // Extract integer value for animation if numeric
    val intVal = value.toIntOrNull()
    val animatedInt = if (intVal != null) {
        animateIntAsState(
            targetValue = intVal,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "hud_res_anim_$label"
        ).value
    } else null

    val displayValue = if (animatedInt != null) "$animatedInt" else value

    Surface(
        color = if (isCritical) Color(0xFF331414) else FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCritical) DangerCrimson else FrontierBorder
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isCritical) DangerCrimson else color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) DangerCrimson else TextWhite,
                        fontSize = 11.sp
                    )
                )
                if (warningHint != null) {
                    Text(
                        text = warningHint,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DangerCrimson,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun HudStorageChip(
    current: Int,
    max: Int,
    onClick: () -> Unit
) {
    val animatedCurrent by animateIntAsState(
        targetValue = current,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "hud_storage_anim"
    )

    val fraction = if (max > 0) (animatedCurrent.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
    val isNearFull = fraction > 0.85f

    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isNearFull) DangerCrimson else FrontierBorder
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("hud_chip_storage")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = "Склад",
                tint = if (isNearFull) DangerCrimson else StoragePurple,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "$animatedCurrent / $max",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNearFull) DangerCrimson else TextWhite,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(FrontierBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .background(if (isNearFull) DangerCrimson else StoragePurple)
                    )
                }
            }
        }
    }
}
