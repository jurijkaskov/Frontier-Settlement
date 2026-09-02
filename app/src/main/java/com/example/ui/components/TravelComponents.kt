package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TravelCalculator
import com.example.domain.model.*
import com.example.ui.theme.*

/**
 * Bottom sheet for preparing and configuring a travel party to a selected wasteland location.
 */
@Composable
fun TravelPrepSheet(
    destination: Location,
    gameState: GameState,
    selectedMode: TravelTransportMode,
    onSelectMode: (TravelTransportMode) -> Unit,
    onStartTravel: (destinationId: String, mode: TravelTransportMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val originLoc = gameState.locations.find { it.id == gameState.currentLocationId } ?: gameState.locations.find { it.isPlayerBase }
    val squadCount = gameState.selectedSquadIds.size.coerceAtLeast(1)

    // Dynamic cost & duration calculation
    val cost = remember(destination, selectedMode, squadCount, gameState.technologies, originLoc) {
        TravelCalculator.calculateTravelCost(
            destination = destination,
            transportMode = selectedMode,
            participantCount = squadCount,
            technologies = gameState.technologies,
            origin = originLoc
        )
    }

    // Validation
    val validation = remember(destination, selectedMode, gameState) {
        TravelCalculator.validateTravel(
            destination = destination,
            transportMode = selectedMode,
            gameState = gameState,
            originLocationId = gameState.currentLocationId
        )
    }

    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        border = BorderStroke(1.dp, FrontierBorderLight),
        shadowElevation = 24.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("travel_prep_sheet_${destination.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(TextSubtle.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Header: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TechCyan.copy(alpha = 0.15f))
                            .border(1.dp, TechCyan, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Подготовка к путешествию",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = "Цель: ${destination.displayName} • ${cost.distanceKm} км",
                            style = MaterialTheme.typography.bodySmall.copy(color = TechCyan)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_travel_prep")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = TextSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transport Mode Selection Section
            Text(
                text = "СПОСОБ ПЕРЕДВИЖЕНИЯ",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TravelTransportMode.values().forEach { mode ->
                    val isSelected = mode == selectedMode
                    val veh = gameState.vehicles.find { it.id == mode.defaultVehicleId }
                        ?: gameState.vehicles.find { it.type == mode.vehicleType }
                    val isLocked = mode.requiresVehicle && (veh == null || !veh.isUnlocked || !veh.isAvailable)

                    TransportModeCard(
                        mode = mode,
                        isSelected = isSelected,
                        isLocked = isLocked,
                        onClick = {
                            if (!isLocked) onSelectMode(mode)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Travel Cost & Requirements Summary Box
            Surface(
                color = FrontierDarkSurfaceHighlight,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, FrontierBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "СТОИМОСТЬ ПУТИ И ВРЕМЯ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TechCyan,
                                letterSpacing = 1.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Время: ${cost.formattedDuration}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Water Cost Chip
                        ResourceCostBadge(
                            label = "Вода",
                            needed = cost.water,
                            available = gameState.resources.water,
                            icon = Icons.Default.WaterDrop,
                            color = WaterCyan,
                            modifier = Modifier.weight(1f)
                        )

                        // Food Cost Chip
                        ResourceCostBadge(
                            label = "Еда",
                            needed = cost.food,
                            available = gameState.resources.food,
                            icon = Icons.Default.Restaurant,
                            color = FoodGreen,
                            modifier = Modifier.weight(1f)
                        )

                        // Fuel Cost Chip
                        ResourceCostBadge(
                            label = "Топливо",
                            needed = cost.fuel,
                            available = gameState.resources.fuel,
                            icon = Icons.Default.LocalGasStation,
                            color = FuelAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Squad & vehicle description line
                    Text(
                        text = "Отряд: $squadCount чел. • ${selectedMode.description}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Validation error or warning banner
            if (validation is TravelValidationResult.Invalid) {
                Surface(
                    color = DangerCrimson.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = DangerCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = validation.message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DangerCrimson,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Launch Travel CTA Button
            Button(
                onClick = {
                    onStartTravel(destination.id, selectedMode)
                    onDismiss()
                },
                enabled = validation.isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafeEmerald,
                    disabledContainerColor = FrontierBorder,
                    contentColor = FrontierOnPrimary,
                    disabledContentColor = TextSubtle
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_confirm_start_travel")
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (validation.isValid) "Отправиться в путь (${cost.distanceKm} км)"
                    else "Недостаточно ресурсов для перехода",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun TransportModeCard(
    mode: TravelTransportMode,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isSelected -> TechCyan
        isLocked -> FrontierBorder
        else -> FrontierBorderLight
    }

    val bgColor = when {
        isSelected -> TechCyan.copy(alpha = 0.15f)
        isLocked -> FrontierDarkSurface.copy(alpha = 0.5f)
        else -> FrontierDarkSurfaceHighlight
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = !isLocked, onClick = onClick)
            .testTag("transport_mode_${mode.name}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (mode) {
                    TravelTransportMode.FOOT -> Icons.Default.DirectionsWalk
                    TravelTransportMode.BICYCLE -> Icons.Default.PedalBike
                    TravelTransportMode.MOTORCYCLE -> Icons.Default.TwoWheeler
                    TravelTransportMode.BUGGY -> Icons.Default.DirectionsCar
                    TravelTransportMode.OFFROAD -> Icons.Default.TimeToLeave
                    TravelTransportMode.ARMORED_TRUCK -> Icons.Default.LocalShipping
                },
                contentDescription = mode.titleRu,
                tint = if (isLocked) TextSubtle else if (isSelected) TechCyan else TextWhite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mode.titleRu,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isLocked) TextSubtle else if (isSelected) TechCyan else TextWhite,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${mode.baseSpeedKmH.toInt()} км/ч",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isSelected) TextWhite else TextSubtle,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
fun ResourceCostBadge(
    label: String,
    needed: Int,
    available: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isEnough = available >= needed
    val statusColor = if (isEnough) color else DangerCrimson

    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isEnough) FrontierBorder else DangerCrimson.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = statusColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSubtle,
                        fontSize = 10.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (needed > 0) "$needed / $available" else "0 (0%)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    fontSize = 11.sp
                )
            )
        }
    }
}

/**
 * Floating active travel HUD displayed during real-time movement across the map.
 */
@Composable
fun TravelLiveHUD(
    travel: TravelState,
    destination: Location?,
    onAdvanceStep: () -> Unit,
    onInstantArrive: () -> Unit,
    onReturnToBase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val destName = destination?.name ?: "Пункт назначения"

    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, TechCyan),
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("travel_live_hud")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header: Status title + mode badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(TechCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (travel.isReturning) "ВОЗВРАЩЕНИЕ НА БАЗУ" else "В ПУТИ: $destName",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Surface(
                    color = TechCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = travel.transportMode.titleRu,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.1f", travel.traveledKm)} / ${travel.distanceKm} км",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
                Text(
                    text = "${travel.progressPercent}%",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TechCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { travel.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = TechCyan,
                trackColor = FrontierBorder
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Latest travel log message
            Text(
                text = travel.statusMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSubtle,
                    fontSize = 11.sp
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Step forward or Fast arrive
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAdvanceStep,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechCyan,
                        contentColor = FrontierOnPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("btn_advance_travel_step")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Продвинуться (+25%)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onInstantArrive,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    border = BorderStroke(1.dp, FrontierBorderLight),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("btn_instant_arrive")
                ) {
                    Text("Прибыть", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                }
            }
        }
    }
}

/**
 * Arrival dialog shown once the player's travel party reaches their destination.
 */
@Composable
fun TravelArrivalDialog(
    location: Location,
    travel: TravelState,
    onExploreLocation: () -> Unit,
    onReturnToBase: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FrontierDarkSurfaceElevated,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, SafeEmerald),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_travel_arrived")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SafeEmerald.copy(alpha = 0.2f))
                        .border(1.5.dp, SafeEmerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = SafeEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Пункт назначения достигнут!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SafeEmerald
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location summary card
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, FrontierBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Угроза: ${location.dangerLevel.titleRu}", style = MaterialTheme.typography.labelSmall.copy(color = TechCyan))
                            Text("Дистанция: ${location.distanceKm} км", style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = location.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextWhite),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Добыча: ${location.potentialLoot.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Button(
                    onClick = {
                        onExploreLocation()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafeEmerald,
                        contentColor = FrontierOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_arrived_explore")
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Исследовать локацию", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        onReturnToBase()
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    border = BorderStroke(1.dp, FrontierBorderLight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_arrived_return_base")
                ) {
                    Icon(imageVector = Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Вернуться в поселение", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
