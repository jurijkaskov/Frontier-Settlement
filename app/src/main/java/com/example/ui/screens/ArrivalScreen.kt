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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.*
import com.example.ui.components.DangerBadge
import com.example.ui.components.GameCard
import com.example.ui.components.GameHeroImage
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

/**
 * Arrival Screen (Point 17) - Displays the arrived location state, atmospheric illustration,
 * reconnaissance observations, squad readiness, cargo status, and primary action choices.
 */
@Composable
fun ArrivalScreen(
    gameState: GameState,
    onStartExploration: () -> Unit,
    onReturnToBase: () -> Unit,
    onBack: () -> Unit,
    onScoutSurroundings: (locationId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeExpedition = gameState.activeExpedition
    val currentLocation = gameState.locations.find { it.id == gameState.currentLocationId }
        ?: activeExpedition?.location
        ?: gameState.locations.firstOrNull { it.status == LocationStatus.VISITED }

    var showReturnConfirmDialog by remember { mutableStateOf(false) }
    var showReconSheet by remember { mutableStateOf(false) }

    // Fallback if no valid location or expedition is found
    if (currentLocation == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(FrontierDarkBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = WarningAmber
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Локация не определена",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Экспедиционный отряд не находится в активной точке прибытия.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber, contentColor = FrontierDarkBackground),
                        modifier = Modifier.testTag("btn_arrival_fallback_back")
                    ) {
                        Text("Вернуться к карте")
                    }
                }
            }
        }
        return
    }

    val squadMembers = activeExpedition?.squad
        ?: gameState.characters.filter { gameState.selectedSquadIds.contains(it.id) }
            .ifEmpty { listOfNotNull(gameState.characters.firstOrNull()) }

    val vehicle = activeExpedition?.vehicle
        ?: gameState.vehicles.find { it.id == gameState.selectedVehicleId }
        ?: gameState.vehicles.firstOrNull()

    val scoutCharacter = squadMembers.maxByOrNull { it.stats.scavengingSkill }

    Scaffold(
        containerColor = FrontierDarkBackground,
        bottomBar = {
            ArrivalBottomBar(
                onStartExploration = onStartExploration,
                onInspectSurroundings = {
                    onScoutSurroundings(currentLocation.id)
                    showReconSheet = true
                },
                onRequestReturn = { showReturnConfirmDialog = true }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // 1. Hero Banner with stylized wasteland visual art & overlays
            item {
                ArrivalHeroBanner(
                    location = currentLocation,
                    onBack = onBack
                )
            }

            // 2. Location Title, Status & Atmospheric Narrative
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                color = SafeEmerald.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(SafeEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ПРИБЫТИЕ • ${currentLocation.sectorCode}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SafeEmerald,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${currentLocation.distanceKm} км от базы",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentLocation.displayName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentLocation.displayDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMuted,
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            // 3. Scout Reconnaissance & Observations ("Первичный осмотр сектора")
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(
                        title = "Первичный осмотр периметра",
                        accentColor = TechCyan
                    )

                    GameCard(
                        backgroundColor = FrontierDarkSurfaceElevated,
                        borderColor = FrontierBorder
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val observations = if (currentLocation.observations.isNotEmpty()) {
                                currentLocation.observations
                            } else {
                                listOf(
                                    "Внешний периметр локации кажется заброшенным, но следы активности недавние.",
                                    "Подъездные пути свободны для транспорта экспедиции.",
                                    "Здания сохранили базовую целостность конструкций."
                                )
                            }

                            observations.forEach { obs ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = TechCyan,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = obs,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextWhite,
                                            lineHeight = 18.sp
                                        )
                                    )
                                }
                            }

                            // Scout Specialist Commentary
                            if (scoutCharacter != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = TechCyan.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = TechCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Доклад разведчика (${scoutCharacter.name})",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TechCyan
                                                )
                                            )
                                            Text(
                                                text = getScoutCommentary(currentLocation, scoutCharacter),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = TextSubtle,
                                                    fontSize = 11.sp
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

            // 4. Discovered Local Areas / Sub-Sectors
            if (currentLocation.localAreas.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader(
                            title = "Ориентиры и секторы локации",
                            accentColor = WarningAmber
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentLocation.localAreas.forEach { area ->
                                Surface(
                                    color = FrontierDarkSurfaceHighlight,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, FrontierBorderLight),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(WarningAmber.copy(alpha = 0.15f))
                                                    .border(1.dp, WarningAmber.copy(alpha = 0.4f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationSearching,
                                                    contentDescription = null,
                                                    tint = WarningAmber,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = area.name,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextWhite
                                                    )
                                                )
                                                Text(
                                                    text = area.typeRu,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = TextSubtle,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }

                                        Surface(
                                            color = FrontierDarkSurfaceElevated,
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, FrontierBorder)
                                        ) {
                                            Text(
                                                text = if (area.isExplored) "Исследовано" else "Не исследовано",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (area.isExplored) SafeEmerald else WarningAmber,
                                                    fontSize = 10.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Potential Loot & Hazards Overview
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Potential Loot Box
                        Surface(
                            color = FrontierDarkSurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, FrontierBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Возможная добыча",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = WarningAmber
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                currentLocation.potentialLoot.forEach { loot ->
                                    Text(
                                        text = "• $loot",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextWhite,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Threats & Hazards Box
                        Surface(
                            color = FrontierDarkSurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, FrontierBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = DangerCrimson,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Факторы угрозы",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DangerCrimson
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val threats = if (currentLocation.threats.isNotEmpty()) {
                                    currentLocation.threats
                                } else {
                                    listOf(currentLocation.dangerLevel.titleRu)
                                }
                                threats.forEach { threat ->
                                    Text(
                                        text = "⚠ $threat",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextWhite,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Squad & Logistics Readiness Summary
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(
                        title = "Экспедиционная группа и готовность",
                        accentColor = FoodGreen
                    )

                    // Squad Members Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(squadMembers) { character ->
                            SquadArrivalMemberCard(character = character)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Transport & Cargo Status Card
                    Surface(
                        color = FrontierDarkSurfaceElevated,
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = TechCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = vehicle?.name ?: "Пеший переход",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    )
                                }

                                Text(
                                    text = "В готовности",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SafeEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Supplies breakdown
                            val supplies = activeExpedition?.supplies ?: emptyMap()
                            if (supplies.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    supplies.entries.filter { it.value > 0 }.forEach { (type, qty) ->
                                        Surface(
                                            color = FrontierDarkSurfaceHighlight,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, FrontierBorderLight),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "${type.symbol} $qty",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = TextWhite,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Cargo Capacity Bar
                            val maxCap = activeExpedition?.cargoCapacityKg?.toInt() ?: vehicle?.capacityKg ?: 60
                            val curWeight = activeExpedition?.cargoWeightKg?.toInt() ?: 15
                            val freeCap = (maxCap - curWeight).coerceAtLeast(0)
                            val fraction = (curWeight.toFloat() / maxCap.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Свободный грузовой отсек:",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle)
                                    )
                                    Text(
                                        text = "$freeCap кг свободно (из $maxCap кг)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TechCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(FrontierBorder)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(TechCyan, WarningAmber)
                                                )
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

    // Return confirmation dialog
    if (showReturnConfirmDialog) {
        ReturnConfirmationDialog(
            locationName = currentLocation.displayName,
            onConfirm = {
                showReturnConfirmDialog = false
                onReturnToBase()
            },
            onDismiss = { showReturnConfirmDialog = false }
        )
    }

    // Scout Reconnaissance Modal Sheet
    if (showReconSheet) {
        ScoutReconModal(
            location = currentLocation,
            scout = scoutCharacter,
            onDismiss = { showReconSheet = false }
        )
    }
}

/**
 * Top atmospheric illustration banner with dynamic vector art matching location type.
 */
@Composable
private fun ArrivalHeroBanner(
    location: Location,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFF0D1117))
            .testTag("arrival_hero_banner")
    ) {
        // Visual Asset System integration with local WebP asset or atmospheric procedural fallback
        GameHeroImage(
            assetId = location.visualAssetId,
            locationType = location.type,
            height = 200.dp,
            showOverlay = true,
            overlayHeight = 70.dp
        )

        // Top bar overlay with Back Button & Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FrontierDarkSurfaceElevated.copy(alpha = 0.85f))
                    .border(1.dp, FrontierBorderLight, CircleShape)
                    .testTag("btn_arrival_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад к карте",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location Type Badge
                Surface(
                    color = FrontierDarkSurfaceElevated.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, FrontierBorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location.type.titleRu,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Danger Badge
                DangerBadge(dangerLevel = location.dangerLevel)
            }
        }
    }
}

/**
 * Squad member card in arrival screen.
 */
@Composable
private fun SquadArrivalMemberCard(
    character: Character
) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, FrontierBorder),
        modifier = Modifier.width(130.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = character.name.split(" ").firstOrNull() ?: character.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ур.${character.level}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TechCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = character.role.titleRu,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSubtle,
                    fontSize = 10.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // HP Bar
            val healthFrac = (character.health.toFloat() / character.maxHealth.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(FrontierBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(healthFrac)
                        .background(if (character.health < 35) DangerCrimson else FoodGreen)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${character.health}/${character.maxHealth} HP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                )
                Text(
                    text = "Поиск: ${character.stats.scavengingSkill}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TechCyan,
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}

/**
 * Fixed bottom action controls for the arrival screen.
 */
@Composable
private fun ArrivalBottomBar(
    onStartExploration: () -> Unit,
    onInspectSurroundings: () -> Unit,
    onRequestReturn: () -> Unit
) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        border = BorderStroke(1.dp, FrontierBorderLight),
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Primary Action: Start Exploration
            Button(
                onClick = onStartExploration,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningAmber,
                    contentColor = FrontierDarkBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_arrival_start_exploration")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Начать исследование",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Secondary & Tertiary Actions in a Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onInspectSurroundings,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                    border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_arrival_inspect_surroundings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Осмотреть окрестности",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onRequestReturn,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                    border = BorderStroke(1.dp, FrontierBorderLight),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_arrival_return_to_base")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Вернуться на базу",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Return to Settlement confirmation dialog to prevent accidental clicks.
 */
@Composable
private fun ReturnConfirmationDialog(
    locationName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FrontierDarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, FrontierBorderLight),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_return_confirmation")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WarningAmber.copy(alpha = 0.15f))
                        .border(1.dp, WarningAmber, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Вернуться в поселение?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Локация «$locationName» останется неисследованной. Отряд и транспорт отправятся в обратный путь на аванпост.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        border = BorderStroke(1.dp, FrontierBorderLight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber, contentColor = FrontierDarkBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_confirm_return")
                    ) {
                        Text("Подтвердить", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

/**
 * Scout Reconnaissance bottom sheet / dialog with detailed environmental readings and tactical assessment.
 */
@Composable
private fun ScoutReconModal(
    location: Location,
    scout: Character?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FrontierDarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_scout_recon")
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
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Тактическая разведка",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TechCyan
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
                            tint = TextSubtle,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tactical telemetry readings
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, FrontierBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Радиационный фон:", style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle))
                            Text(
                                text = when (location.dangerLevel) {
                                    DangerLevel.SAFE -> "0.08 мЗв/ч (Фон в норме)"
                                    DangerLevel.LOW -> "0.18 мЗв/ч (Слабый)"
                                    DangerLevel.MODERATE -> "0.45 мЗв/ч (Умеренный)"
                                    DangerLevel.HIGH -> "1.20 мЗв/ч (Опасно)"
                                    DangerLevel.EXTREME -> "3.50 мЗв/ч (Смертельно)"
                                    DangerLevel.UNKNOWN -> "Помехи дозиметра"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (location.dangerLevel == DangerLevel.SAFE || location.dangerLevel == DangerLevel.LOW) SafeEmerald else WarningAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Устойчивость зданий:", style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle))
                            Text(
                                text = "72% (Удовлетворительно)",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextWhite)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Оценка активности врагов:", style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle))
                            Text(
                                text = location.dangerLevel.titleRu,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TechCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Заключение разведчика (${scout?.name ?: "Дозорный"}):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "«Периметр осмотрен с возвышенности. Локация пригодна для аккуратного проникновения. Рекомендуется двигаться цепочкой и держать оружие наготове.»",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = FrontierDarkBackground),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text("Принято", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

/**
 * Returns dynamic scout commentary based on location type and scavenging skill.
 */
private fun getScoutCommentary(location: Location, scout: Character): String {
    return when (location.type) {
        LocationType.ABANDONED_STATION -> "«В железнодорожных тупиках целы топливные цистерны. Главное — не шуметь возле стрелочного поста.»"
        LocationType.FARM -> "«В силосных башнях сухо, признаков крупных хищников нет. Можно собрать неплохой запас зерна.»"
        LocationType.VILLAGE -> "«Заколоченные ставни в медпункте не взломаны. Там наверняка остались медикаменты первой помощи.»"
        LocationType.FOREST -> "«Хвойная чаща глушит звуки. Нужно держаться вместе, чтобы не попасть в логово мутантов.»"
        LocationType.WAREHOUSE_COMPLEX -> "«Охрана складов держит прожекторы включенными. Проникновение потребует максимальной скрытности.»"
        LocationType.INDUSTRIAL_PLANT -> "«В литейном цехе много цветного лома и уцелевшей проводки. Остерегайтесь обрушений потолка.»"
        LocationType.MILITARY_BUNKER -> "«Гермозатвор под напряжением. Автоматические датчики тревоги всё ещё активны.»"
        LocationType.ANOMALY_ZONE -> "«Электромагнитные разряды бьют каждые несколько секунд. Нужно следить за приборами.»"
        LocationType.TRADING_POST -> "«Караваны торговцев только что прибыли. На рынке хороший выбор редких компонентов.»"
        LocationType.SETTLEMENT -> "«Родная база. Полная безопасность и доступ ко всем мастерским.»"
        LocationType.CITY_RUINS -> "«Обрушенные высотки и провалы в метро. Не упускайте из виду верхние этажи.»"
    }
}
