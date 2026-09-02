package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.domain.model.Expedition
import com.example.domain.model.GameState
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Main Game Hub Screen: Settlement (Главный экран поселения).
 *
 * Implements:
 * - Interactive multi-layered atmospheric 2D Settlement Scene with animated visual elements;
 * - Settlement Header with Outpost Name, Tier, Level, and Reputation progress bar;
 * - 2-Column Tactical Interactive Building Grid (Warehouse, Shop, Workshop, Residents, Research, Quests);
 * - Active Expedition status alert banner;
 * - Event chronicle & Inspection dialogs;
 * - Polished Material 3 dark tactical layout.
 */
@Composable
fun SettlementScreen(
    gameState: GameState,
    onNavigateToWarehouse: () -> Unit,
    onNavigateToWorkshop: () -> Unit,
    onNavigateToBuildings: () -> Unit,
    onNavigateToResearch: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToSquad: () -> Unit,
    onNavigateToResidents: () -> Unit = onNavigateToSquad,
    onNavigateToQuests: () -> Unit,
    onNavigateToExpeditionLive: () -> Unit,
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToEconomy: () -> Unit = {},
    onNavigateToReputation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showEventsDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Dialogs
    if (showEventsDialog) {
        SettlementEventsDialog(
            logs = gameState.dayLogs,
            onDismiss = { showEventsDialog = false }
        )
    }

    if (showInfoDialog) {
        SettlementInfoDialog(
            settlement = gameState.settlement,
            gameState = gameState,
            onDismiss = { showInfoDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
    ) {
        // Active Expedition Alert Banner (if squad is currently out in the wastelands)
        if (gameState.activeExpedition != null) {
            item {
                ActiveExpeditionBanner(
                    expedition = gameState.activeExpedition,
                    onClick = onNavigateToExpeditionLive
                )
            }
        }

        // Staging Area Pending Cargo Banner
        if (gameState.pendingSettlementUnload.hasPendingCargo) {
            item {
                Surface(
                    color = WarningAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToWarehouse() }
                        .testTag("banner_pending_settlement_cargo")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Трофеи ожидают места на складе!",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber
                                    )
                                )
                                Text(
                                    text = "Излишки из «${gameState.pendingSettlementUnload.sourceLocationName.ifEmpty { "экспедиции" }}» во временной зоне разгрузки.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 11.sp)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "К складу",
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Central Settlement Visual Scene (Atmospheric 2D Hub)
        item {
            SettlementScene(
                settlement = gameState.settlement,
                dayPeriod = gameState.gameDateTime.dayPeriod,
                onSceneClick = { showInfoDialog = true }
            )
        }

        // Settlement Status Header Card (Name, Level, Reputation Bar, Vital Stats)
        item {
            SettlementHeader(
                settlement = gameState.settlement,
                onInfoClick = { showInfoDialog = true },
                onReputationClick = onNavigateToReputation
            )
        }

        // Active Tracked Quest HUD
        item {
            com.example.ui.components.quest.TrackedQuestHud(
                gameState = gameState,
                onNavigateToQuests = onNavigateToQuests
            )
        }

        // Section Title: Interactive Buildings & Management
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ОБЪЕКТЫ АВАНПОСТА",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = "Выберите сектор для управления",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }

        // Interactive 2-Column Building Grid
        item {
            BuildingGrid(
                gameState = gameState,
                onNavigateToWarehouse = onNavigateToWarehouse,
                onNavigateToMarket = onNavigateToMarket,
                onNavigateToWorkshop = onNavigateToWorkshop,
                onNavigateToBuildings = onNavigateToBuildings,
                onNavigateToSquad = onNavigateToSquad,
                onNavigateToResidents = onNavigateToResidents,
                onNavigateToResearch = onNavigateToResearch,
                onNavigateToQuests = onNavigateToQuests,
                onNavigateToVehicles = onNavigateToVehicles,
                onNavigateToEconomy = onNavigateToEconomy,
                onNavigateToReputation = onNavigateToReputation
            )
        }

        // Quick Wasteland Exploration Launch Banner
        item {
            WastelandScoutBanner(
                unlockedLocationsCount = gameState.locations.count { it.isUnlocked },
                onClick = onNavigateToMap
            )
        }

        // Radio & Event Feed Chronicle Preview
        item {
            SettlementRadioChronicleCard(
                logs = gameState.dayLogs,
                onOpenFullLog = { showEventsDialog = true }
            )
        }
    }
}

/**
 * Call-to-action banner for launching expeditions from the settlement hub.
 */
@Composable
private fun WastelandScoutBanner(
    unlockedLocationsCount: Int,
    onClick: () -> Unit
) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("banner_wasteland_scout")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SafeEmerald.copy(alpha = 0.18f))
                        .border(1.dp, SafeEmerald, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = SafeEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Вылазка в пустоши",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                    Text(
                        text = "Доступно $unlockedLocationsCount секторов на карте",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SafeEmerald,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Surface(
                color = FrontierPrimaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Карта",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SafeEmerald
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SafeEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact Radio communications preview card on the settlement hub.
 */
@Composable
private fun SettlementRadioChronicleCard(
    logs: List<String>,
    onOpenFullLog: () -> Unit
) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpenFullLog() }
            .testTag("card_radio_chronicle")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Сводка радиоэфира",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 12.sp
                        )
                    )
                }

                Text(
                    text = "Открыть журнал",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TechCyan,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            logs.take(3).forEach { log ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "› ",
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveExpeditionBanner(
    expedition: Expedition,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF1C1917),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("banner_active_expedition")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WarningAmber.copy(alpha = 0.2f))
                        .border(1.dp, WarningAmber, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Вылазка в процессе: ${expedition.location.name}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                    Text(
                        text = "Статус: ${expedition.status.titleRu} • Отряд: ${expedition.squad.size} чел.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WarningAmber,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Surface(
                color = WarningAmber.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "К отряду",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
