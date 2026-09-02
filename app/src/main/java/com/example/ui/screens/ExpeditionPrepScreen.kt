package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.TravelCalculator
import com.example.domain.model.*
import com.example.domain.service.ExpeditionPreparationValidator
import com.example.domain.service.ExpeditionSupplyCalculator
import com.example.ui.components.DangerBadge
import com.example.ui.components.EquipmentManagementSection
import com.example.ui.components.GameCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpeditionPrepScreen(
    locationId: String,
    gameState: GameState,
    expeditionDraft: ExpeditionPreparationState?,
    onInitDraft: (String) -> Unit,
    onToggleParticipant: (String) -> Unit,
    onSetLeader: (String) -> Unit,
    onSelectTravelMode: (TravelTransportMode) -> Unit,
    onSelectVehicle: (String) -> Unit,
    onSetSupply: (ResourceType, Int) -> Unit,
    onApplyRecommendedSupplies: () -> Unit,
    onEquipItem: (characterId: String, slot: EquipmentSlotType, itemId: String) -> Unit,
    onUnequipItem: (characterId: String, slot: EquipmentSlotType) -> Unit,
    onStartExpedition: () -> Unit,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ensure draft is initialized for target location
    LaunchedEffect(locationId) {
        onInitDraft(locationId)
    }

    val destination = gameState.locations.find { it.id == locationId }
        ?: gameState.locations.firstOrNull()
        ?: Location(id = "loc_base", name = "Базовый лагерь", type = LocationType.SETTLEMENT, distanceKm = 0, dangerLevel = DangerLevel.SAFE)

    val effectiveDraft = expeditionDraft ?: ExpeditionPreparationState(
        destinationLocationId = destination.id,
        originLocationId = gameState.currentLocationId,
        participantIds = gameState.squad.memberIds.ifEmpty { listOfNotNull(gameState.characters.firstOrNull()?.id) },
        leaderId = gameState.squad.leaderId ?: gameState.characters.firstOrNull()?.id,
        travelMode = TravelTransportMode.FOOT,
        selectedVehicleId = gameState.selectedVehicleId
    )

    val validationResult = remember(effectiveDraft, gameState) {
        ExpeditionPreparationValidator.validate(effectiveDraft, gameState)
    }

    val participantChars = remember(effectiveDraft.participantIds, gameState.characters) {
        gameState.characters.filter { effectiveDraft.participantIds.contains(it.id) }
    }

    val leaderChar = remember(effectiveDraft.leaderId, participantChars) {
        participantChars.find { it.id == effectiveDraft.leaderId } ?: participantChars.firstOrNull()
    }

    val selectedVehicle = remember(effectiveDraft.selectedVehicleId, effectiveDraft.travelMode, gameState.vehicles) {
        if (effectiveDraft.travelMode.requiresVehicle) {
            gameState.vehicles.find { it.id == effectiveDraft.selectedVehicleId }
                ?: TravelCalculator.resolveVehicle(effectiveDraft.travelMode, gameState)
        } else null
    }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSquadPickerSheet by remember { mutableStateOf(false) }
    var characterForGearDialog by remember { mutableStateOf<Character?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FrontierDarkSurface,
        bottomBar = {
            // Sticky Departure Bottom Bar
            Surface(
                color = FrontierDarkSurfaceElevated,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, FrontierDarkSurfaceHighlight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                ) {
                    if (validationResult.blockingIssues.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = DangerCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = validationResult.blockingIssues.first(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DangerCrimson,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 2
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Готовность: ${validationResult.readinessPercent}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (validationResult.canDepart) SafeEmerald else WarningAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Вес: ${String.format("%.1f", validationResult.cargoSummary.totalCurrentWeightKg)} / ${validationResult.cargoSummary.totalCapacityKg} кг",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 11.sp)
                            )
                        }

                        Button(
                            onClick = { showConfirmationDialog = true },
                            enabled = validationResult.canDepart,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SafeEmerald,
                                contentColor = Color.Black,
                                disabledContainerColor = FrontierDarkSurfaceHighlight,
                                disabledContentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("btn_start_expedition_confirm")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Отправиться",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
        ) {
            // Header Bar
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBackToMap,
                        modifier = Modifier
                            .testTag("btn_prep_back_to_map")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад к карте",
                            tint = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Подготовка к вылазке",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = "План рейда в сектор «${destination.name}»",
                            style = MaterialTheme.typography.bodySmall.copy(color = TechCyan, fontSize = 12.sp)
                        )
                    }
                }
            }

            // Section 1: Destination Intel Card
            item {
                DestinationIntelCard(destination = destination)
            }

            // Section 2: Squad Selection & Members
            item {
                SquadSectionCard(
                    participantChars = participantChars,
                    leaderId = effectiveDraft.leaderId,
                    maxCapacity = selectedVehicle?.maxPassengers ?: 4,
                    allCharacters = gameState.characters,
                    inventoryItems = gameState.inventoryItems,
                    onOpenSquadPicker = { showSquadPickerSheet = true },
                    onSetLeader = onSetLeader,
                    onRemoveMember = { charId -> onToggleParticipant(charId) },
                    onOpenGear = { char -> characterForGearDialog = char }
                )
            }

            // Section 3: Leader Card
            if (leaderChar != null) {
                item {
                    LeaderHighlightCard(
                        leader = leaderChar,
                        onOpenGear = { characterForGearDialog = leaderChar }
                    )
                }
            }

            // Section 4: Group Tactical Stats
            item {
                GroupStatsCard(
                    participants = participantChars,
                    inventoryItems = gameState.inventoryItems
                )
            }

            // Section 5 & 6: Travel Mode & Vehicle Selection
            item {
                TransportSectionCard(
                    selectedMode = effectiveDraft.travelMode,
                    selectedVehicle = selectedVehicle,
                    availableVehicles = gameState.vehicles,
                    squadCount = participantChars.size,
                    onSelectMode = onSelectTravelMode,
                    onSelectVehicle = onSelectVehicle
                )
            }

            // Section 7: Character Equipment Overview
            item {
                CharacterEquipmentSummarySection(
                    participants = participantChars,
                    inventoryItems = gameState.inventoryItems,
                    onOpenGearDialog = { char -> characterForGearDialog = char }
                )
            }

            // Section 8: Supplies Management
            item {
                ExpeditionSuppliesSection(
                    supplies = effectiveDraft.supplies,
                    minSupplies = validationResult.travelCost.let {
                        val m = mutableMapOf<ResourceType, Int>()
                        if (it.water > 0) m[ResourceType.WATER] = it.water
                        if (it.food > 0) m[ResourceType.FOOD] = it.food
                        if (it.fuel > 0) m[ResourceType.FUEL] = it.fuel
                        m
                    },
                    availableResources = gameState.resources,
                    requiresFuel = effectiveDraft.travelMode.requiresVehicle,
                    onSupplyChanged = onSetSupply,
                    onTakeRecommended = onApplyRecommendedSupplies
                )
            }

            // Section 9: Cargo & Weight Capacity
            item {
                CargoWeightSummaryCard(cargoSummary = validationResult.cargoSummary)
            }

            // Section 10: Travel Route & Duration
            item {
                TravelRouteCard(
                    destination = destination,
                    travelCost = validationResult.travelCost,
                    transportMode = effectiveDraft.travelMode
                )
            }

            // Section 11: Readiness Checklist & Warnings
            item {
                ReadinessAndWarningsCard(validationResult = validationResult)
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Confirmation Modal Dialog
    if (showConfirmationDialog) {
        DepartConfirmationDialog(
            destination = destination,
            participants = participantChars,
            leader = leaderChar,
            vehicleName = selectedVehicle?.name ?: effectiveDraft.travelMode.titleRu,
            supplies = effectiveDraft.supplies,
            travelCost = validationResult.travelCost,
            cargoSummary = validationResult.cargoSummary,
            onConfirm = {
                showConfirmationDialog = false
                onStartExpedition()
            },
            onDismiss = { showConfirmationDialog = false }
        )
    }

    // Squad Picker Dialog
    if (showSquadPickerSheet) {
        SquadSelectionDialog(
            allCharacters = gameState.characters,
            selectedIds = effectiveDraft.participantIds,
            leaderId = effectiveDraft.leaderId,
            maxCapacity = selectedVehicle?.maxPassengers ?: 4,
            onToggleCharacter = onToggleParticipant,
            onSetLeader = onSetLeader,
            onDismiss = { showSquadPickerSheet = false }
        )
    }

    // Quick Character Gear Modal Dialog
    if (characterForGearDialog != null) {
        val activeChar = characterForGearDialog!!
        Dialog(
            onDismissRequest = { characterForGearDialog = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(16.dp)),
                color = FrontierDarkSurfaceElevated,
                border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Экипировка: ${activeChar.name}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        IconButton(onClick = { characterForGearDialog = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    EquipmentManagementSection(
                        character = gameState.characters.find { it.id == activeChar.id } ?: activeChar,
                        warehouseItems = gameState.inventoryItems,
                        allCharacters = gameState.characters,
                        onEquipItem = onEquipItem,
                        onUnequipItem = onUnequipItem,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// UI Subcomponents
// -------------------------------------------------------------

@Composable
fun DestinationIntelCard(destination: Location) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = TechCyan
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "СЕКТОР ${destination.sectorCode.ifBlank { "ALPHA" }} • ${destination.type.titleRu.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = destination.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                }
                DangerBadge(dangerLevel = destination.dangerLevel)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = destination.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Дистанция", style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp))
                        Text("${destination.distanceKm} км", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextWhite))
                    }
                }
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Ландшафт", style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp))
                        Text(destination.terrainType.titleRu, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = WarningAmber))
                    }
                }
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Ожидаемый лут", style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp))
                        Text("+${destination.estimatedLootMaterials}м, +${destination.estimatedLootCredits}кр", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = FoodGreen))
                    }
                }
            }
        }
    }
}

@Composable
fun SquadSectionCard(
    participantChars: List<Character>,
    leaderId: String?,
    maxCapacity: Int,
    allCharacters: List<Character>,
    inventoryItems: List<WarehouseItem>,
    onOpenSquadPicker: () -> Unit,
    onSetLeader: (String) -> Unit,
    onRemoveMember: (String) -> Unit,
    onOpenGear: (Character) -> Unit
) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = if (participantChars.isEmpty()) WarningAmber else SafeEmerald
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = TechCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Экспедиционный отряд",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                }

                Surface(
                    color = if (participantChars.size > maxCapacity) DangerCrimson.copy(alpha = 0.2f) else TechCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (participantChars.size > maxCapacity) DangerCrimson else TechCyan)
                ) {
                    Text(
                        text = "${participantChars.size} / $maxCapacity бойцов",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (participantChars.size > maxCapacity) DangerCrimson else TechCyan,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (participantChars.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FrontierDarkSurfaceHighlight, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "В отряде нет участников. Нажмите «Сформировать отряд».",
                        style = MaterialTheme.typography.bodySmall.copy(color = WarningAmber)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    participantChars.forEach { char ->
                        val isLeader = char.id == leaderId
                        val effectiveStats = char.getEffectiveStats(inventoryItems)
                        val carryCap = char.getEffectiveCarryCapacityKg(inventoryItems)

                        Surface(
                            color = FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isLeader) WarningAmber else Color.Transparent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle with role icon
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isLeader) WarningAmber.copy(alpha = 0.2f) else TechCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLeader) WarningAmber else TechCyan,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = char.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                        )
                                        if (isLeader) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = WarningAmber.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "👑 КОМАНДИР",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = WarningAmber,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${char.role.titleRu} • Ур. ${char.level} • HP ${char.health}/${char.getEffectiveMaxHealth(inventoryItems)} • Груз: $carryCap кг",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSubtle,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                // Action icons
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isLeader) {
                                        IconButton(
                                            onClick = { onSetLeader(char.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.StarBorder,
                                                contentDescription = "Назначить командиром",
                                                tint = WarningAmber,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onOpenGear(char) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backpack,
                                            contentDescription = "Экипировка",
                                            tint = TechCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onRemoveMember(char.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RemoveCircleOutline,
                                            contentDescription = "Убрать из отряда",
                                            tint = DangerCrimson,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onOpenSquadPicker,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("btn_change_squad_members"),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = TechCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Изменить состав отряда", color = TechCyan, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LeaderHighlightCard(leader: Character, onOpenGear: () -> Unit) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = WarningAmber
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(WarningAmber.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Командир: ${leader.name}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
                Text(
                    text = "Бонус лидерства: ${leader.rolePerkSummary}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WarningAmber,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
fun GroupStatsCard(participants: List<Character>, inventoryItems: List<WarehouseItem>) {
    val totalAtk = participants.sumOf { it.getEffectiveStats(inventoryItems).attack }
    val totalDef = participants.sumOf { it.getEffectiveStats(inventoryItems).defense }
    val totalScav = participants.sumOf { it.getEffectiveStats(inventoryItems).scavengingSkill }
    val totalEng = participants.sumOf { it.getEffectiveStats(inventoryItems).engineeringSkill }
    val totalMed = participants.sumOf { it.getEffectiveStats(inventoryItems).medicalSkill }

    val rolesCount = participants.groupingBy { it.role }.eachCount()

    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = TechCyan.copy(alpha = 0.4f)
    ) {
        Column {
            Text(
                text = "Сводные характеристики группы",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBadge(label = "Атака", value = "$totalAtk", color = DangerCrimson, icon = Icons.Default.SportsMma)
                StatBadge(label = "Защита", value = "$totalDef", color = TechCyan, icon = Icons.Default.Shield)
                StatBadge(label = "Поиск", value = "$totalScav", color = WarningAmber, icon = Icons.Default.Search)
                StatBadge(label = "Инженерия", value = "$totalEng", color = StoragePurple, icon = Icons.Default.Build)
                StatBadge(label = "Медицина", value = "$totalMed", color = SafeEmerald, icon = Icons.Default.MedicalServices)
            }

            if (rolesCount.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(rolesCount.entries.toList()) { (role, count) ->
                        Surface(
                            color = FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${role.titleRu} × $count",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBadge(label: String, value: String, color: Color, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextWhite))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 9.sp))
    }
}

@Composable
fun TransportSectionCard(
    selectedMode: TravelTransportMode,
    selectedVehicle: Vehicle?,
    availableVehicles: List<Vehicle>,
    squadCount: Int,
    onSelectMode: (TravelTransportMode) -> Unit,
    onSelectVehicle: (String) -> Unit
) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = if (selectedMode.requiresVehicle && selectedVehicle == null) DangerCrimson else TechCyan
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TechCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Транспорт и передвижение",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                }
                Text(
                    text = selectedVehicle?.name ?: selectedMode.titleRu,
                    style = MaterialTheme.typography.labelSmall.copy(color = TechCyan, fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Modes Selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TravelTransportMode.entries.toTypedArray()) { mode ->
                    val isSelected = mode == selectedMode
                    val matchingVeh = availableVehicles.find { it.type == mode.vehicleType }
                    val isAvailable = !mode.requiresVehicle || (matchingVeh != null && matchingVeh.isAvailable)

                    Surface(
                        color = if (isSelected) TechCyan.copy(alpha = 0.2f) else FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) TechCyan else if (isAvailable) Color.Transparent else DangerCrimson.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.clickable { onSelectMode(mode) }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = mode.titleRu,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TechCyan else if (isAvailable) TextWhite else TextMuted
                                )
                            )
                            Text(
                                text = if (!isAvailable) "Нет в гараже" else "${mode.baseSpeedKmH.toInt()} км/ч",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (!isAvailable) DangerCrimson else TextSubtle,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }

            if (selectedMode.requiresVehicle) {
                Spacer(modifier = Modifier.height(10.dp))
                val matchingVehicles = availableVehicles.filter { it.type == selectedMode.vehicleType }

                if (matchingVehicles.isEmpty()) {
                    Text(
                        text = "⚠ В гараже нет транспорта типа «${selectedMode.titleRu}». Соберите его в мастерской.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DangerCrimson, fontSize = 11.sp)
                    )
                } else {
                    Text(
                        text = "Выберите машину из автопарка:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        matchingVehicles.forEach { veh ->
                            val isVehSelected = veh.id == selectedVehicle?.id
                            Surface(
                                color = if (isVehSelected) TechCyan.copy(alpha = 0.25f) else FrontierDarkSurfaceHighlight,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isVehSelected) TechCyan else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectVehicle(veh.id) }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = veh.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isVehSelected) TechCyan else TextWhite
                                        )
                                    )
                                    Text(
                                        text = "Мест: ${veh.maxPassengers} • Груз: ${veh.capacityKg}кг • Сост: ${veh.durabilityPercent}%",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 9.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Passenger Capacity Warning
            val maxPass = selectedVehicle?.maxPassengers ?: 4
            if (squadCount > maxPass) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = DangerCrimson.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, DangerCrimson)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DangerCrimson, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Отряд ($squadCount чел.) превышает вместимость транспорта ($maxPass мест)!",
                            style = MaterialTheme.typography.bodySmall.copy(color = DangerCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterEquipmentSummarySection(
    participants: List<Character>,
    inventoryItems: List<WarehouseItem>,
    onOpenGearDialog: (Character) -> Unit
) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = TechCyan.copy(alpha = 0.4f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = TechCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Экипировка и слоты бойцов",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (participants.isEmpty()) {
                Text("Нет участников", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    participants.forEach { char ->
                        val equippedMap = char.getEquippedItemsMap(inventoryItems)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FrontierDarkSurfaceHighlight, RoundedCornerShape(6.dp))
                                .clickable { onOpenGearDialog(char) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = char.name,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                                )
                                Text(
                                    text = if (equippedMap.isEmpty()) "Без экипировки" else equippedMap.values.joinToString { it.name },
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp),
                                    maxLines = 1
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                EquipmentSlotType.entries.forEach { slot ->
                                    val isEquipped = equippedMap.containsKey(slot)
                                    Surface(
                                        color = if (isEquipped) SafeEmerald.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, if (isEquipped) SafeEmerald else Color.DarkGray)
                                    ) {
                                        Text(
                                            text = slot.emoji,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(2.dp)
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
}

@Composable
fun ExpeditionSuppliesSection(
    supplies: Map<ResourceType, Int>,
    minSupplies: Map<ResourceType, Int>,
    availableResources: GameResources,
    requiresFuel: Boolean,
    onSupplyChanged: (ResourceType, Int) -> Unit,
    onTakeRecommended: () -> Unit
) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = TechCyan
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = TechCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Экспедиционные припасы",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                }

                Button(
                    onClick = onTakeRecommended,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechCyan.copy(alpha = 0.2f),
                        contentColor = TechCyan
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("btn_take_recommended_supplies")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Взять рекомендуемое", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Steppers for Water, Food, Fuel (if needed), Medicine, Ammo
            val displayTypes = buildList {
                add(ResourceType.WATER)
                add(ResourceType.FOOD)
                if (requiresFuel) add(ResourceType.FUEL)
                add(ResourceType.MEDICINE)
                add(ResourceType.AMMO)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                displayTypes.forEach { type ->
                    val currentAmount = supplies[type] ?: 0
                    val minAmount = minSupplies[type] ?: 0
                    val warehouseStock = availableResources[type]
                    val isBelowMin = currentAmount < minAmount

                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isBelowMin) DangerCrimson else Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(type.symbol, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = type.titleRu,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    )
                                    Text(
                                        text = "На складе: $warehouseStock" + if (minAmount > 0) " • Мин: $minAmount" else "",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isBelowMin) DangerCrimson else TextSubtle,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            // Stepper [-] Amount [+]
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onSupplyChanged(type, (currentAmount - 1).coerceAtLeast(0)) },
                                    enabled = currentAmount > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Меньше", tint = if (currentAmount > 0) TextWhite else TextMuted)
                                }

                                Text(
                                    text = "$currentAmount",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBelowMin) DangerCrimson else SafeEmerald
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                IconButton(
                                    onClick = { onSupplyChanged(type, (currentAmount + 1).coerceAtMost(warehouseStock)) },
                                    enabled = currentAmount < warehouseStock,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Больше", tint = if (currentAmount < warehouseStock) TextWhite else TextMuted)
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
fun CargoWeightSummaryCard(cargoSummary: ExpeditionCargoSummary) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = cargoSummary.loadStatusColor
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = cargoSummary.loadStatusColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Грузоподъёмность и вес",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                }

                Text(
                    text = "${String.format("%.1f", cargoSummary.totalCurrentWeightKg)} / ${cargoSummary.totalCapacityKg} кг",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = cargoSummary.loadStatusColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (cargoSummary.totalCurrentWeightKg / cargoSummary.totalCapacityKg).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = cargoSummary.loadStatusColor,
                trackColor = FrontierDarkSurfaceHighlight
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Припасы: ${String.format("%.1f", cargoSummary.suppliesWeightKg)} кг",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 11.sp)
                )
                Text(
                    text = "Свободно для добычи: ${String.format("%.1f", cargoSummary.freeLootCapacityKg)} кг",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (cargoSummary.freeLootCapacityKg > 10f) SafeEmerald else WarningAmber,
                        fontSize = 11.sp
                    )
                )
            }

            if (cargoSummary.isOverloaded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "⚠ ПЕРЕГРУЗ! Снизьте количество припасов, чтобы отправиться в путь.",
                    style = MaterialTheme.typography.bodySmall.copy(color = DangerCrimson, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                )
            }
        }
    }
}

@Composable
fun TravelRouteCard(
    destination: Location,
    travelCost: TravelCost,
    transportMode: TravelTransportMode
) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = TechCyan.copy(alpha = 0.4f)
    ) {
        Column {
            Text(
                text = "Расчёт маршрута и времени",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Дистанция", style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp))
                    Text("${travelCost.distanceKm} км", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextWhite))
                }
                Column {
                    Text("Скорость", style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp))
                    Text("${String.format("%.1f", travelCost.rawSpeedKmH)} км/ч", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TechCyan))
                }
                Column {
                    Text("Оценка времени", style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp))
                    val hours = travelCost.estimatedDurationHours
                    val formatted = if (hours < 1f) {
                        "${(hours * 60).roundToInt()} мин"
                    } else {
                        val h = hours.toInt()
                        val m = ((hours - h) * 60).roundToInt()
                        if (m > 0) "$h ч $m мин" else "$h ч"
                    }
                    Text("~$formatted", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = SafeEmerald))
                }
            }
        }
    }
}

@Composable
fun ReadinessAndWarningsCard(validationResult: ExpeditionValidationResult) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = if (validationResult.canDepart) SafeEmerald else WarningAmber
    ) {
        Column {
            Text(
                text = "Чеклист готовности к рейду",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                validationResult.checkItems.forEach { check ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (check.isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (check.isPassed) SafeEmerald else DangerCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = check.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (check.isPassed) TextWhite else DangerCrimson
                                )
                            )
                            Text(
                                text = check.detail,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp)
                            )
                        }
                    }
                }
            }

            if (validationResult.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Советы и предупреждения:",
                    style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    validationResult.warnings.forEach { warning ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text("⚠ ", color = WarningAmber, fontSize = 11.sp)
                            Text(warning, style = MaterialTheme.typography.bodySmall.copy(color = WarningAmber, fontSize = 11.sp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DepartConfirmationDialog(
    destination: Location,
    participants: List<Character>,
    leader: Character?,
    vehicleName: String,
    supplies: Map<ResourceType, Int>,
    travelCost: TravelCost,
    cargoSummary: ExpeditionCargoSummary,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = SafeEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Отправление экспедиции",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Подтвердите отправление отряда в «${destination.name}»:",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite)
                )

                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🎯 Цель: ${destination.name} (${destination.distanceKm} км)", style = MaterialTheme.typography.bodySmall.copy(color = TechCyan))
                        Text("👥 Состав: ${participants.size} бойцов (Командир: ${leader?.name ?: "—"})", style = MaterialTheme.typography.bodySmall.copy(color = TextWhite))
                        Text("🚗 Транспорт: $vehicleName", style = MaterialTheme.typography.bodySmall.copy(color = TextWhite))
                        Text(
                            text = "🍞 Припасы: " + supplies.entries.filter { it.value > 0 }.joinToString { "${it.key.symbol}${it.value}" },
                            style = MaterialTheme.typography.bodySmall.copy(color = FoodGreen)
                        )
                        Text(
                            text = "📦 Свободно для добычи: ${String.format("%.1f", cargoSummary.freeLootCapacityKg)} кг",
                            style = MaterialTheme.typography.bodySmall.copy(color = SafeEmerald, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Text(
                    text = "Ресурсы будут списаны со склада, а участники отправятся на маршрут.",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Подтвердить отправление", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextMuted)
            }
        },
        containerColor = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun SquadSelectionDialog(
    allCharacters: List<Character>,
    selectedIds: List<String>,
    leaderId: String?,
    maxCapacity: Int,
    onToggleCharacter: (String) -> Unit,
    onSetLeader: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(16.dp)),
            color = FrontierDarkSurfaceElevated,
            border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Выбор бойцов в отряд",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = "Выбрано: ${selectedIds.size} / $maxCapacity",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selectedIds.size > maxCapacity) DangerCrimson else TechCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allCharacters) { char ->
                        val isSelected = selectedIds.contains(char.id)
                        val isLeader = char.id == leaderId
                        val isInjured = char.status == CharacterStatus.INJURED || char.health <= 15
                        val isAway = char.status == CharacterStatus.ON_EXPEDITION

                        Surface(
                            color = if (isSelected) TechCyan.copy(alpha = 0.15f) else FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) TechCyan else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isInjured && !isAway) { onToggleCharacter(char.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { onToggleCharacter(char.id) },
                                        enabled = !isInjured && !isAway,
                                        colors = CheckboxDefaults.colors(checkedColor = TechCyan)
                                    )

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = char.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isInjured || isAway) TextMuted else TextWhite
                                                )
                                            )
                                            if (isLeader) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("👑", fontSize = 12.sp)
                                            }
                                        }
                                        Text(
                                            text = "${char.role.titleRu} • Ур. ${char.level} • HP ${char.health}/${char.maxHealth}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp)
                                        )
                                        if (isInjured) {
                                            Text("В лазарете (ранен)", style = MaterialTheme.typography.labelSmall.copy(color = DangerCrimson, fontSize = 9.sp))
                                        } else if (isAway) {
                                            Text("В другой экспедиции", style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontSize = 9.sp))
                                        }
                                    }
                                }

                                if (isSelected) {
                                    IconButton(
                                        onClick = { onSetLeader(char.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isLeader) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Лидер",
                                            tint = WarningAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = Color.Black)
                ) {
                    Text("Готово", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
