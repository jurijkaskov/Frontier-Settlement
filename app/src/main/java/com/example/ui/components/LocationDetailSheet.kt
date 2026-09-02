package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.domain.model.*
import com.example.ui.theme.*

/**
 * Bottom inspection card / sheet displaying tactical intel, loot potential,
 * distance, hazard rating, and expedition launch actions for a selected POI or Base.
 */
@Composable
fun LocationDetailSheet(
    location: Location,
    gameState: GameState,
    onDismiss: () -> Unit,
    onPrepareExpedition: (String) -> Unit,
    onNavigateToSettlement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorderLight),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("location_detail_sheet_${location.id}")
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

            // Header row: Icon + Title + Danger/Base Badge + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (location.isPlayerBase) SafeEmerald.copy(alpha = 0.2f)
                                else if (!location.isUnlocked || location.isHiddenOrUnknown) TextSubtle.copy(alpha = 0.2f)
                                else when (location.dangerLevel) {
                                    DangerLevel.SAFE -> SafeEmerald.copy(alpha = 0.2f)
                                    DangerLevel.LOW -> TechCyan.copy(alpha = 0.2f)
                                    DangerLevel.MODERATE -> WarningAmber.copy(alpha = 0.2f)
                                    DangerLevel.HIGH -> DangerCrimson.copy(alpha = 0.2f)
                                    DangerLevel.EXTREME -> MilitaryRed.copy(alpha = 0.2f)
                                    DangerLevel.UNKNOWN -> StoragePurple.copy(alpha = 0.2f)
                                }
                            )
                            .border(
                                1.dp,
                                if (location.isPlayerBase) SafeEmerald
                                else if (!location.isUnlocked) FrontierBorder
                                else TechCyan,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                location.isPlayerBase -> Icons.Default.Home
                                location.isHiddenOrUnknown -> Icons.Default.HelpOutline
                                location.type == LocationType.ABANDONED_STATION -> Icons.Default.Train
                                location.type == LocationType.FARM -> Icons.Default.Grass
                                location.type == LocationType.FOREST -> Icons.Default.Park
                                location.type == LocationType.VILLAGE -> Icons.Default.HolidayVillage
                                location.type == LocationType.TRADING_POST -> Icons.Default.Storefront
                                location.type == LocationType.WAREHOUSE_COMPLEX -> Icons.Default.Inventory2
                                location.type == LocationType.INDUSTRIAL_PLANT -> Icons.Default.Factory
                                location.type == LocationType.CITY_RUINS -> Icons.Default.LocationCity
                                location.type == LocationType.MILITARY_BUNKER -> Icons.Default.Shield
                                location.type == LocationType.ANOMALY_ZONE -> Icons.Default.Sensors
                                else -> Icons.Default.Place
                            },
                            contentDescription = null,
                            tint = if (location.isPlayerBase) SafeEmerald
                            else if (location.isHiddenOrUnknown) StoragePurple
                            else if (!location.isUnlocked) TextSubtle
                            else TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = location.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (location.isPlayerBase) "Главная база • Сектор 00"
                                else "${location.type.titleRu} • ${location.terrainType.titleRu}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (location.isPlayerBase) SafeEmerald else TechCyan,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (location.isPlayerBase) {
                        Surface(
                            color = SafeEmerald.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "ВАША БАЗА",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SafeEmerald,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else if (location.isHiddenOrUnknown) {
                        Surface(
                            color = StoragePurple.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StoragePurple)
                        ) {
                            Text(
                                text = "НЕИЗВЕДАНО",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StoragePurple,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else if (!location.isUnlocked) {
                        Surface(
                            color = FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "ЗАКРЫТО",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    } else {
                        DangerBadge(dangerLevel = location.dangerLevel)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

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
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Atmospheric Visual Hero Banner
            if (!location.isHiddenOrUnknown) {
                GameHeroImage(
                    assetId = location.visualAssetId,
                    locationType = location.type,
                    height = 110.dp,
                    showOverlay = true,
                    overlayHeight = 40.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Description
            Text(
                text = location.displayDescription,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats / Intel Grid
            if (location.isPlayerBase) {
                // Base Info Card Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IntelStatChip(
                        label = "Жители базы",
                        value = "${gameState.currentPopulation} / ${gameState.maxPopulation}",
                        accentColor = FoodGreen,
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )
                    IntelStatChip(
                        label = "Уровень аванпоста",
                        value = "Ур. ${gameState.settlement.level}",
                        accentColor = TechCyan,
                        icon = Icons.Default.Apartment,
                        modifier = Modifier.weight(1f)
                    )
                    IntelStatChip(
                        label = "Защита периметра",
                        value = "${gameState.settlement.defenseRating} ед.",
                        accentColor = SafeEmerald,
                        icon = Icons.Default.Shield,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Location Travel & Loot Intel Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IntelStatChip(
                        label = "Дистанция",
                        value = "${location.distanceKm} км",
                        accentColor = TechCyan,
                        icon = Icons.Default.Explore,
                        modifier = Modifier.weight(1f)
                    )
                    IntelStatChip(
                        label = "Сложность",
                        value = if (location.isHiddenOrUnknown) "Неизвестна" else location.dangerLevel.titleRu,
                        accentColor = when (location.dangerLevel) {
                            DangerLevel.SAFE -> SafeEmerald
                            DangerLevel.LOW -> TechCyan
                            DangerLevel.MODERATE -> WarningAmber
                            DangerLevel.HIGH -> DangerCrimson
                            DangerLevel.EXTREME -> MilitaryRed
                            DangerLevel.UNKNOWN -> StoragePurple
                        },
                        icon = Icons.Default.WarningAmber,
                        modifier = Modifier.weight(1.2f)
                    )
                    IntelStatChip(
                        label = "Реком. отряд",
                        value = "${location.recommendedSquadSize}+ бойцов",
                        accentColor = WarningAmber,
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Potential loot forecast
                Text(
                    text = "Потенциальные находки и ресурсы:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    location.potentialLoot.forEach { lootTag ->
                        Surface(
                            color = FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialsOrange)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = lootTag,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Linked Quest Objectives at this Location
            val locationMarkers = com.example.domain.service.quest.QuestMarkerHelper.getMarkersForLocation(location.id, gameState)
            if (locationMarkers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = TechCyan.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = TechCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ЦЕЛИ АКТИВНЫХ ЗАДАНИЙ:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TechCyan
                            )
                        }
                        locationMarkers.forEach { marker ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = marker.questTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextWhite
                                    )
                                    if (marker.objectiveDescription != null) {
                                        Text(
                                            text = marker.objectiveDescription,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                                if (marker.markerType == com.example.domain.service.quest.QuestMarkerType.TURN_IN) {
                                    Surface(
                                        color = SafeEmerald.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "К сдаче",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SafeEmerald,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Lock requirements info if blocked
            if (!location.isPlayerBase && !location.isUnlocked && !location.isHiddenOrUnknown) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = WarningAmber.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (location.requiredTechId != null)
                                "Требуется исследование в Научном центре (Биосканирование / Радиомачта)."
                            else "Требуется уровень поселения ${location.requiredSettlementLevel}.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = WarningAmber,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Action Buttons
            if (location.isPlayerBase) {
                Button(
                    onClick = onNavigateToSettlement,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafeEmerald,
                        contentColor = FrontierOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_sheet_to_settlement")
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Управление аванпостом",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Button(
                    onClick = { onPrepareExpedition(location.id) },
                    enabled = location.isUnlocked && !location.isHiddenOrUnknown,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafeEmerald,
                        disabledContainerColor = FrontierBorder,
                        contentColor = FrontierOnPrimary,
                        disabledContentColor = TextSubtle
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_sheet_prepare_expedition")
                ) {
                    Icon(
                        imageVector = if (!location.isUnlocked) Icons.Default.Lock else Icons.Default.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (location.isHiddenOrUnknown) "Необходимо разведать сектор"
                        else if (!location.isUnlocked) "Сектор заблокирован"
                        else "Подготовка к вылазке (${location.distanceKm} км)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun IntelStatChip(
    label: String,
    value: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FrontierDarkSurfaceHighlight,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSubtle,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}
