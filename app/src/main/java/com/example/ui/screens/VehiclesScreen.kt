package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.*
import com.example.ui.components.GameCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

/**
 * Filter options for the vehicle garage.
 */
enum class VehicleFilter(val titleRu: String) {
    ALL("Все"),
    AVAILABLE("Доступные"),
    IN_USE("В пути"),
    NO_FUEL("Без топлива"),
    MOTORIZED("Моторизованные")
}

/**
 * Full-featured Garage & Transport Management Screen.
 */
@Composable
fun VehiclesScreen(
    gameState: GameState,
    onSelectVehicle: (String) -> Unit,
    onRepairVehicle: (vehicleId: String) -> Unit,
    onCraftVehicle: (type: VehicleType, name: String, materialsCost: Int, creditsCost: Int) -> Unit,
    onNavigateToMap: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(VehicleFilter.ALL) }
    var selectedVehicleForDetail by remember { mutableStateOf<Vehicle?>(null) }
    var showCraftDialog by remember { mutableStateOf(false) }

    val filteredVehicles = remember(gameState.vehicles, selectedFilter) {
        when (selectedFilter) {
            VehicleFilter.ALL -> gameState.vehicles
            VehicleFilter.AVAILABLE -> gameState.vehicles.filter { it.isReadyForTrip }
            VehicleFilter.IN_USE -> gameState.vehicles.filter { it.status == VehicleStatus.IN_USE }
            VehicleFilter.NO_FUEL -> gameState.vehicles.filter { !it.isMotorized }
            VehicleFilter.MOTORIZED -> gameState.vehicles.filter { it.isMotorized }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("btn_garage_back")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад",
                                tint = TextWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Гараж и автопарк",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            Text(
                                text = "Техника аванпоста и транспорт экспедиций",
                                style = MaterialTheme.typography.bodySmall.copy(color = TechCyan)
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showCraftDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WarningAmber.copy(alpha = 0.2f),
                            contentColor = WarningAmber
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_open_craft_vehicle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Собрать",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Summary Stats Cards
            item {
                FleetStatsPanel(
                    totalVehicles = gameState.vehicles.size,
                    availableCount = gameState.availableVehicles.size,
                    inUseCount = gameState.inUseVehicles.size,
                    totalCapacity = gameState.totalFleetCapacityKg,
                    fuelReserve = gameState.resources.fuel
                )
            }

            // Filter Chips Row
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(VehicleFilter.values()) { filter ->
                        val isSelected = filter == selectedFilter
                        Surface(
                            color = if (isSelected) WarningAmber.copy(alpha = 0.2f) else FrontierDarkSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) WarningAmber else FrontierBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedFilter = filter }
                                .testTag("filter_chip_${filter.name.lowercase()}")
                        ) {
                            Text(
                                text = filter.titleRu,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) WarningAmber else TextMuted
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                SectionHeader(
                    title = "Единицы техники (${filteredVehicles.size})",
                    accentColor = WarningAmber
                )
            }

            // Empty State
            if (filteredVehicles.isEmpty()) {
                item {
                    Surface(
                        color = FrontierDarkSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, FrontierBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = TextSubtle,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Нет транспорта по выбранному фильтру",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextWhite)
                            )
                            Text(
                                text = "Соберите новую машину в мастерской или выберите другой фильтр.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                    }
                }
            }

            // Vehicle Cards List
            items(filteredVehicles, key = { it.id }) { vehicle ->
                val isSelectedForTravel = vehicle.id == gameState.selectedVehicleId

                VehicleCardItem(
                    vehicle = vehicle,
                    isSelectedForTravel = isSelectedForTravel,
                    availableFuel = gameState.resources.fuel,
                    onSelect = { onSelectVehicle(vehicle.id) },
                    onInspect = { selectedVehicleForDetail = vehicle },
                    onRepair = { onRepairVehicle(vehicle.id) }
                )
            }
        }

        // Details Bottom Sheet / Modal
        selectedVehicleForDetail?.let { veh ->
            VehicleDetailDialog(
                vehicle = veh,
                gameState = gameState,
                isSelected = veh.id == gameState.selectedVehicleId,
                onSelectForTravel = {
                    onSelectVehicle(veh.id)
                    selectedVehicleForDetail = null
                },
                onRepair = {
                    onRepairVehicle(veh.id)
                    selectedVehicleForDetail = null
                },
                onNavigateToMap = {
                    selectedVehicleForDetail = null
                    onNavigateToMap()
                },
                onDismiss = { selectedVehicleForDetail = null }
            )
        }

        // Craft Vehicle Dialog
        if (showCraftDialog) {
            CraftVehicleDialog(
                gameState = gameState,
                onCraft = { type, name, mat, cred ->
                    onCraftVehicle(type, name, mat, cred)
                    showCraftDialog = false
                },
                onDismiss = { showCraftDialog = false }
            )
        }
    }
}

/**
 * Fleet Statistics Header Panel.
 */
@Composable
fun FleetStatsPanel(
    totalVehicles: Int,
    availableCount: Int,
    inUseCount: Int,
    totalCapacity: Int,
    fuelReserve: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, FrontierBorderLight),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "СТАТИСТИКА АВТОПАРКА",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TechCyan,
                        letterSpacing = 1.sp
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (availableCount > 0) SafeEmerald else WarningAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (availableCount > 0) "$availableCount машин готово" else "Все машины заняты",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (availableCount > 0) SafeEmerald else WarningAmber,
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
                StatChip(
                    label = "Всего машин",
                    value = "$totalVehicles ед.",
                    accentColor = TextWhite,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "В экспедициях",
                    value = "$inUseCount ед.",
                    accentColor = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Грузоподъёмность",
                    value = "$totalCapacity кг",
                    accentColor = FoodGreen,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Запас топлива",
                    value = "$fuelReserve л",
                    accentColor = FuelAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FrontierDarkSurfaceHighlight,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSubtle,
                    fontSize = 9.sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * Individual Card representation of a vehicle.
 */
@Composable
fun VehicleCardItem(
    vehicle: Vehicle,
    isSelectedForTravel: Boolean,
    availableFuel: Int,
    onSelect: () -> Unit,
    onInspect: () -> Unit,
    onRepair: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (vehicle.status) {
        VehicleStatus.AVAILABLE -> SafeEmerald
        VehicleStatus.IN_USE -> WarningAmber
        VehicleStatus.MAINTENANCE -> FuelAmber
        VehicleStatus.DAMAGED -> DangerCrimson
        VehicleStatus.UNAVAILABLE -> TextSubtle
    }

    val maxRangeKm = if (vehicle.fuelConsumptionPerKm > 0f) {
        (availableFuel / vehicle.fuelConsumptionPerKm).toInt()
    } else {
        null
    }

    Surface(
        color = if (isSelectedForTravel) Color(0xFF16253B) else FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (isSelectedForTravel) WarningAmber else FrontierBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onInspect() }
            .testTag("card_vehicle_${vehicle.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Illustration Icon + Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    VehicleGraphicAvatar(
                        type = vehicle.type,
                        isSelected = isSelectedForTravel
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vehicle.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            if (isSelectedForTravel) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = WarningAmber.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "АКТИВЕН",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = WarningAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${vehicle.type.titleRu} • ${vehicle.speedRatingRu} скорость",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TechCyan,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = vehicle.status.titleRu,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VehicleSpecPill(
                    icon = Icons.Default.Speed,
                    label = "${vehicle.speedKmH} км/ч",
                    sublabel = "Скорость",
                    color = TechCyan,
                    modifier = Modifier.weight(1f)
                )
                VehicleSpecPill(
                    icon = Icons.Default.Luggage,
                    label = "${vehicle.capacityKg} кг",
                    sublabel = "Груз",
                    color = FoodGreen,
                    modifier = Modifier.weight(1f)
                )
                VehicleSpecPill(
                    icon = Icons.Default.LocalGasStation,
                    label = vehicle.fuelRatingRu,
                    sublabel = "Расход",
                    color = FuelAmber,
                    modifier = Modifier.weight(1.1f)
                )
                VehicleSpecPill(
                    icon = Icons.Default.Group,
                    label = "до ${vehicle.maxPassengers} ч.",
                    sublabel = "Экипаж",
                    color = SafeEmerald,
                    modifier = Modifier.weight(0.9f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer / Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (maxRangeKm != null) {
                    Text(
                        text = "Запас хода: ~${maxRangeKm} км",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                } else {
                    Text(
                        text = "Без ограничений по топливу",
                        style = MaterialTheme.typography.labelSmall.copy(color = SafeEmerald)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (vehicle.durabilityPercent < 100 && vehicle.status != VehicleStatus.IN_USE) {
                        OutlinedButton(
                            onClick = onRepair,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, FuelAmber),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Ремонт (15м)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = FuelAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (vehicle.isReadyForTrip) {
                        Button(
                            onClick = onSelect,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelectedForTravel) SafeEmerald else WarningAmber,
                                contentColor = FrontierOnPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_select_vehicle_${vehicle.id}")
                        ) {
                            Text(
                                text = if (isSelectedForTravel) "Выбран" else "Выбрать",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleSpecPill(
    icon: ImageVector,
    label: String,
    sublabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FrontierDarkSurfaceHighlight,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSubtle,
                    fontSize = 8.sp
                )
            )
        }
    }
}

/**
 * Custom Canvas vector art avatar for vehicle classes.
 */
@Composable
fun VehicleGraphicAvatar(
    type: VehicleType,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = when (type) {
        VehicleType.FOOT -> TechCyan
        VehicleType.BICYCLE -> SafeEmerald
        VehicleType.MOTORCYCLE -> WarningAmber
        VehicleType.LIGHT_BUGGY -> WarningAmber
        VehicleType.OFFROAD -> FuelAmber
        VehicleType.ARMORED_TRUCK -> DangerCrimson
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = 0.25f),
                        FrontierDarkSurfaceHighlight
                    )
                )
            )
            .border(
                1.dp,
                if (isSelected) WarningAmber else accent.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (type) {
                VehicleType.FOOT -> Icons.Default.DirectionsWalk
                VehicleType.BICYCLE -> Icons.Default.PedalBike
                VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
                VehicleType.LIGHT_BUGGY -> Icons.Default.DirectionsCar
                VehicleType.OFFROAD -> Icons.Default.TimeToLeave
                VehicleType.ARMORED_TRUCK -> Icons.Default.LocalShipping
            },
            contentDescription = type.titleRu,
            tint = accent,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Detailed Inspection Sheet for a Vehicle.
 */
@Composable
fun VehicleDetailDialog(
    vehicle: Vehicle,
    gameState: GameState,
    isSelected: Boolean,
    onSelectForTravel: () -> Unit,
    onRepair: () -> Unit,
    onNavigateToMap: () -> Unit,
    onDismiss: () -> Unit
) {
    val maxRangeKm = if (vehicle.fuelConsumptionPerKm > 0f) {
        (gameState.resources.fuel / vehicle.fuelConsumptionPerKm).toInt()
    } else {
        null
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FrontierDarkSurfaceElevated,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, TechCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VehicleGraphicAvatar(type = vehicle.type, isSelected = isSelected)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = vehicle.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            Text(
                                text = vehicle.type.titleRu,
                                style = MaterialTheme.typography.bodySmall.copy(color = TechCyan)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextSubtle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lore / Description
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = vehicle.description.ifBlank { "Надёжное средство передвижения по радиоактивным пустошам." },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Tactical Specs Grid
                Text(
                    text = "ТАКТИКО-ТЕХНИЧЕСКИЕ ХАРАКТЕРИСТИКИ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TechCyan,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SpecDetailRow(
                        label = "Скорость движения",
                        value = "${vehicle.speedKmH} км/ч (${vehicle.speedRatingRu})",
                        color = TechCyan
                    )
                    SpecDetailRow(
                        label = "Грузоподъёмность",
                        value = "${vehicle.capacityKg} кг багажа",
                        color = FoodGreen
                    )
                    SpecDetailRow(
                        label = "Расход топлива",
                        value = vehicle.fuelRatingRu,
                        color = FuelAmber
                    )
                    SpecDetailRow(
                        label = "Вместимость экипажа",
                        value = "до ${vehicle.maxPassengers} человек",
                        color = SafeEmerald
                    )
                    SpecDetailRow(
                        label = "Запас хода (при текущем топливе)",
                        value = if (maxRangeKm != null) "~$maxRangeKm км" else "Неограничен (без топлива)",
                        color = TextWhite
                    )
                    SpecDetailRow(
                        label = "Состояние / Прочность",
                        value = "${vehicle.durabilityPercent}%",
                        color = if (vehicle.durabilityPercent >= 80) SafeEmerald else WarningAmber
                    )
                    SpecDetailRow(
                        label = "Рейсов завершено",
                        value = "${vehicle.tripsCompleted}",
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (vehicle.isReadyForTrip) {
                        Button(
                            onClick = onSelectForTravel,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningAmber,
                                contentColor = FrontierOnPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isSelected) "Выбран для вылазок" else "Назначить на вылазку",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        FilledTonalButton(
                            onClick = onNavigateToMap,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = TechCyan.copy(alpha = 0.2f),
                                contentColor = TechCyan
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "На карту",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else if (vehicle.status == VehicleStatus.IN_USE) {
                        Surface(
                            color = WarningAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Транспорт выполняет переход по маршруту.",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = WarningAmber,
                                        fontWeight = FontWeight.Bold
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

@Composable
private fun SpecDetailRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 12.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 12.sp
            )
        )
    }
}

/**
 * Dialog allowing the player to craft/assemble new vehicles from materials in the workshop.
 */
@Composable
fun CraftVehicleDialog(
    gameState: GameState,
    onCraft: (type: VehicleType, name: String, materialsCost: Int, creditsCost: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(VehicleType.BICYCLE) }
    var customName by remember { mutableStateOf("") }

    val craftOptions = listOf(
        Triple(VehicleType.BICYCLE, 30, 40),
        Triple(VehicleType.MOTORCYCLE, 55, 90),
        Triple(VehicleType.LIGHT_BUGGY, 85, 150),
        Triple(VehicleType.OFFROAD, 120, 220),
        Triple(VehicleType.ARMORED_TRUCK, 180, 350)
    )

    val currentCraft = craftOptions.find { it.first == selectedType } ?: craftOptions.first()
    val canAfford = gameState.resources.materials >= currentCraft.second && gameState.resources.money >= currentCraft.third

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FrontierDarkSurfaceElevated,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, WarningAmber),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Сборка техники",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = TextSubtle)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Выберите тип транспорта для производства в мастерской:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type Options
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    craftOptions.forEach { (type, mat, cred) ->
                        val isSel = type == selectedType
                        Surface(
                            color = if (isSel) Color(0xFF1F2E45) else FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSel) WarningAmber else FrontierBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedType = type
                                    if (customName.isBlank()) {
                                        customName = "${type.titleRu} №${gameState.vehicles.count { it.type == type } + 1}"
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSel,
                                        onClick = {
                                            selectedType = type
                                            customName = "${type.titleRu} №${gameState.vehicles.count { it.type == type } + 1}"
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = WarningAmber)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = type.titleRu,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                        )
                                        Text(
                                            text = "${type.defaultSpeedKmH} км/ч • ${type.defaultCapacityKg} кг • ${if (type.defaultFuelPerKm > 0) "${type.defaultFuelPerKm}л/км" else "без топлива"}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TechCyan,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$mat Мат. / $cred Кр.",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (gameState.resources.materials >= mat && gameState.resources.money >= cred) FoodGreen else DangerCrimson
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Name Input
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Название транспорта") },
                    placeholder = { Text("Например: Дозорный багги") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarningAmber,
                        unfocusedBorderColor = FrontierBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        val finalName = customName.ifBlank { "${selectedType.titleRu} №${gameState.vehicles.count { it.type == selectedType } + 1}" }
                        onCraft(selectedType, finalName, currentCraft.second, currentCraft.third)
                    },
                    enabled = canAfford,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningAmber,
                        contentColor = FrontierOnPrimary,
                        disabledContainerColor = FrontierBorder,
                        disabledContentColor = TextSubtle
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_confirm_craft_vehicle")
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (canAfford) "Начать сборку" else "Недостаточно ресурсов",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
