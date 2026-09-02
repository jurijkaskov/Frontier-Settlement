package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Tactical Global Map Screen for Frontier Settlement.
 * Offers an interactive 2D post-apocalyptic world map with smooth multi-touch gestures,
 * sector filtering, POI reconnaissance, real-time travel navigation, active expedition alerts,
 * and comprehensive travel preparation.
 */
@Composable
fun MapScreen(
    gameState: GameState,
    onSelectLocation: (String) -> Unit,
    onNavigateToExpeditionLive: () -> Unit,
    onNavigateToSettlement: () -> Unit = {},
    onStartTravel: (destinationId: String, mode: TravelTransportMode) -> Unit = { _, _ -> },
    onAdvanceTravelStep: () -> Unit = {},
    onInstantArrive: () -> Unit = {},
    onReturnToBase: () -> Unit = {},
    onExploreArrived: () -> Unit = {},
    selectedTravelMode: TravelTransportMode = TravelTransportMode.FOOT,
    onSelectTravelMode: (TravelTransportMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Interactive Zoom and Pan transformation state
    var rawScale by remember { mutableFloatStateOf(1.0f) }
    var rawPanOffset by remember { mutableStateOf(Offset.Zero) }

    // Filter & Selection State
    var activeFilter by remember { mutableStateOf(MapFilterCategory.ALL) }
    var selectedLocationId by remember { mutableStateOf<String?>("loc_base") }
    var showLegendDialog by remember { mutableStateOf(false) }
    var showTravelPrepSheet by remember { mutableStateOf(false) }

    // Filter locations based on active filter
    val filteredLocations = remember(gameState.locations, activeFilter) {
        when (activeFilter) {
            MapFilterCategory.ALL -> gameState.locations
            MapFilterCategory.SAFE -> gameState.locations.filter {
                it.isPlayerBase || it.dangerLevel == DangerLevel.SAFE
            }
            MapFilterCategory.RESOURCES -> gameState.locations.filter {
                it.type in listOf(
                    LocationType.FARM,
                    LocationType.FOREST,
                    LocationType.WAREHOUSE_COMPLEX,
                    LocationType.ABANDONED_STATION,
                    LocationType.VILLAGE
                )
            }
            MapFilterCategory.DANGEROUS -> gameState.locations.filter {
                it.dangerLevel in listOf(DangerLevel.HIGH, DangerLevel.EXTREME, DangerLevel.UNKNOWN)
            }
            MapFilterCategory.SPECIAL -> gameState.locations.filter {
                it.type in listOf(LocationType.MILITARY_BUNKER, LocationType.ANOMALY_ZONE)
            }
        }
    }

    val selectedLocation = remember(gameState.locations, selectedLocationId) {
        gameState.locations.find { it.id == selectedLocationId }
    }

    val activeTravel = gameState.activeTravel
    val travelDestination = remember(gameState.locations, activeTravel) {
        if (activeTravel != null) gameState.locations.find { it.id == activeTravel.toLocationId } else null
    }

    val totalLocations = gameState.locations.size
    val unlockedLocations = gameState.locations.count { it.isUnlocked || it.isPlayerBase }

    // Center on player base action
    val centerOnBase: () -> Unit = {
        coroutineScope.launch {
            rawScale = 1.15f
            rawPanOffset = Offset.Zero
            selectedLocationId = "loc_base"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FrontierDarkBackground)
            .testTag("screen_global_map")
    ) {
        // 1. Fullscreen Interactive Canvas World Map with animated travel party
        InteractiveWorldMap(
            locations = filteredLocations,
            selectedLocationId = selectedLocationId,
            onSelectLocation = { location ->
                selectedLocationId = location.id
            },
            scale = rawScale,
            panOffset = rawPanOffset,
            onTransform = { zoomChange, panChange ->
                val newScale = (rawScale * zoomChange).coerceIn(0.75f, 2.6f)
                val maxPan = 400f * newScale
                val newPan = Offset(
                    x = (rawPanOffset.x + panChange.x).coerceIn(-maxPan, maxPan),
                    y = (rawPanOffset.y + panChange.y).coerceIn(-maxPan, maxPan)
                )
                rawScale = newScale
                rawPanOffset = newPan
            },
            activeTravel = activeTravel,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top-Floating Active Expedition Banner (if squad is in expedition mode)
        if (gameState.activeExpedition != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter)
            ) {
                ActiveExpeditionBanner(
                    expedition = gameState.activeExpedition,
                    onClick = onNavigateToExpeditionLive
                )
            }
        }

        // 3. Floating HUD Controls (Header, Filters, Zoom buttons, Centering)
        MapControlsOverlay(
            activeFilter = activeFilter,
            onFilterChange = { category ->
                activeFilter = category
            },
            totalLocationsCount = totalLocations,
            unlockedLocationsCount = unlockedLocations,
            onCenterOnBase = centerOnBase,
            onZoomIn = {
                rawScale = (rawScale * 1.25f).coerceIn(0.75f, 2.6f)
            },
            onZoomOut = {
                rawScale = (rawScale / 1.25f).coerceIn(0.75f, 2.6f)
            },
            onShowLegend = { showLegendDialog = true },
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (gameState.activeExpedition != null) 64.dp else 6.dp,
                    bottom = if (activeTravel?.isActiveTravel == true) 180.dp
                    else if (selectedLocation != null) 260.dp else 12.dp
                )
        )

        // 4. Floating Active Travel Real-Time HUD (during movement)
        if (activeTravel != null && activeTravel.isActiveTravel) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                TravelLiveHUD(
                    travel = activeTravel,
                    destination = travelDestination,
                    onAdvanceStep = onAdvanceTravelStep,
                    onInstantArrive = onInstantArrive,
                    onReturnToBase = onReturnToBase
                )
            }
        }

        // 5. Sliding Bottom Sheet / Inspection Panel for Selected Location
        AnimatedVisibility(
            visible = selectedLocation != null && (activeTravel == null || !activeTravel.isActiveTravel),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (selectedLocation != null) {
                LocationDetailSheet(
                    location = selectedLocation,
                    gameState = gameState,
                    onDismiss = { selectedLocationId = null },
                    onPrepareExpedition = { locId ->
                        // Open travel preparation sheet
                        showTravelPrepSheet = true
                    },
                    onNavigateToSettlement = onNavigateToSettlement
                )
            }
        }

        // 6. Travel Preparation Bottom Sheet
        if (showTravelPrepSheet && selectedLocation != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FrontierDarkBackground.copy(alpha = 0.6f))
                    .align(Alignment.BottomCenter)
            ) {
                TravelPrepSheet(
                    destination = selectedLocation,
                    gameState = gameState,
                    selectedMode = selectedTravelMode,
                    onSelectMode = onSelectTravelMode,
                    onStartTravel = { destId, mode ->
                        onStartTravel(destId, mode)
                        showTravelPrepSheet = false
                        selectedLocationId = null
                    },
                    onDismiss = { showTravelPrepSheet = false },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        // 7. Travel Arrival Modal Dialog (When party reaches target destination)
        if (activeTravel != null && activeTravel.status == TravelStatus.ARRIVED && travelDestination != null) {
            TravelArrivalDialog(
                location = travelDestination,
                travel = activeTravel,
                onExploreLocation = onExploreArrived,
                onReturnToBase = onReturnToBase,
                onDismiss = onExploreArrived
            )
        }

        // 8. Map Legend & Guide Dialog
        if (showLegendDialog) {
            MapLegendDialog(
                onDismiss = { showLegendDialog = false }
            )
        }
    }
}
