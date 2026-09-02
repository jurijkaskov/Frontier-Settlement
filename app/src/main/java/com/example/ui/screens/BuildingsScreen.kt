package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.*
import com.example.ui.components.GameCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

private enum class BuildingFilterTab(val title: String) {
    ALL("Все объекты"),
    PRODUCTION("Производство"),
    SURVIVAL("Оборона и медицина"),
    MANAGEMENT("Логистика и штаб"),
    UNBUILT("К постройке")
}

/**
 * Screen for Settlement Infrastructure and Development (Пункт 4: Развитие поселения).
 */
@Composable
fun BuildingsScreen(
    gameState: GameState,
    onBuildBuilding: (String) -> Unit,
    onUpgradeBuilding: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val res = gameState.resources
    val settlement = gameState.settlement
    val buildings = settlement.buildings

    var selectedFilter by remember { mutableStateOf(BuildingFilterTab.ALL) }
    var inspectBuilding by remember { mutableStateOf<Building?>(null) }

    // Blueprint Inspection Dialog
    if (inspectBuilding != null) {
        val bld = inspectBuilding!!
        BuildingBlueprintDialog(
            building = bld,
            resources = res,
            settlementLevel = settlement.level,
            onBuild = {
                onBuildBuilding(bld.id)
                inspectBuilding = null
            },
            onUpgrade = {
                onUpgradeBuilding(bld.id)
                inspectBuilding = null
            },
            onDismiss = { inspectBuilding = null }
        )
    }

    val filteredBuildings = remember(buildings, selectedFilter) {
        when (selectedFilter) {
            BuildingFilterTab.ALL -> buildings
            BuildingFilterTab.PRODUCTION -> buildings.filter { it.category == BuildingCategory.PRODUCTION }
            BuildingFilterTab.SURVIVAL -> buildings.filter { it.category == BuildingCategory.SURVIVAL_DEFENSE }
            BuildingFilterTab.MANAGEMENT -> buildings.filter { it.category == BuildingCategory.MANAGEMENT_LOGISTICS }
            BuildingFilterTab.UNBUILT -> buildings.filter { !it.isConstructed }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
    ) {
        // 1. Top Header Row with Back Button and Quick Balance
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_buildings_back")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Развитие поселения",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = "Строительство и улучшение инфраструктуры",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Balance summary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = FrontierDarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialsOrange.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.SquareFoot, contentDescription = null, tint = MaterialsOrange, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "${res.materials}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Surface(
                        color = FrontierDarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreditsYellow.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = CreditsYellow, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "${res.money}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 2. Settlement Level & Progress Overview Banner
        item {
            SettlementProgressBanner(settlement = settlement)
        }

        // 3. Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BuildingFilterTab.values()) { tab ->
                    val isSelected = selectedFilter == tab
                    val count = when (tab) {
                        BuildingFilterTab.ALL -> buildings.size
                        BuildingFilterTab.PRODUCTION -> buildings.count { it.category == BuildingCategory.PRODUCTION }
                        BuildingFilterTab.SURVIVAL -> buildings.count { it.category == BuildingCategory.SURVIVAL_DEFENSE }
                        BuildingFilterTab.MANAGEMENT -> buildings.count { it.category == BuildingCategory.MANAGEMENT_LOGISTICS }
                        BuildingFilterTab.UNBUILT -> buildings.count { !it.isConstructed }
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = tab },
                        label = {
                            Text(
                                text = "${tab.title} ($count)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = FrontierDarkSurfaceElevated,
                            labelColor = TextMuted,
                            selectedContainerColor = FrontierPrimaryContainer,
                            selectedLabelColor = SafeEmerald
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = FrontierBorder,
                            selectedBorderColor = SafeEmerald
                        ),
                        modifier = Modifier.testTag("filter_chip_${tab.name}")
                    )
                }
            }
        }

        // 4. Section Subheader
        item {
            SectionHeader(
                title = "Инфраструктурные объекты (${filteredBuildings.size})",
                accentColor = TechCyan
            )
        }

        // 5. Building Cards List
        items(filteredBuildings, key = { it.id }) { building ->
            BuildingDevelopmentCard(
                building = building,
                resources = res,
                settlementLevel = settlement.level,
                onBuild = { onBuildBuilding(building.id) },
                onUpgrade = { onUpgradeBuilding(building.id) },
                onInspect = { inspectBuilding = building }
            )
        }
    }
}

/**
 * Top Settlement Progression Banner with level, XP progress bar, tier badge, and stats.
 */
@Composable
private fun SettlementProgressBanner(
    settlement: Settlement,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = settlement.xpProgressFraction,
        label = "settlementXpProgress"
    )

    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("settlement_progress_banner")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Level & Tier Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = SafeEmerald.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald)
                        ) {
                            Text(
                                text = "УРОВЕНЬ ПОСЕЛЕНИЯ ${settlement.level}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SafeEmerald,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = settlement.tier.titleRu,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TechCyan
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = settlement.tier.perkDescription,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Settlement XP Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Опыт развития (XP)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "${settlement.xp} / ${settlement.xpToNextLevel} XP (${(settlement.xpProgressFraction * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(FrontierBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(TechCyan.copy(alpha = 0.8f), SafeEmerald)
                                )
                            )
                    )
                }
            }

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    icon = Icons.Default.Apartment,
                    label = "Постройки",
                    value = "${settlement.constructedBuildingsCount} / ${settlement.buildings.size}",
                    tint = TechCyan,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    icon = Icons.Default.Upgrade,
                    label = "Сумм. уровни",
                    value = "${settlement.totalBuildingLevels}",
                    tint = SafeEmerald,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    icon = Icons.Default.Shield,
                    label = "Оборона",
                    value = "${settlement.defenseRating} ед.",
                    tint = DangerCrimson,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FrontierDarkSurfaceHighlight,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                Text(text = value, style = MaterialTheme.typography.labelSmall.copy(color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp))
            }
        }
    }
}

/**
 * Building Development and Construction Card.
 */
@Composable
fun BuildingDevelopmentCard(
    building: Building,
    resources: GameResources,
    settlementLevel: Int,
    onBuild: () -> Unit,
    onUpgrade: () -> Unit,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, accentColor) = getBuildingIconAndColor(building.type)

    val isConstructed = building.isConstructed
    val isLocked = building.status == BuildingStatus.LOCKED || (settlementLevel < building.requiredSettlementLevel && !isConstructed)
    val isAvailableToBuild = !isConstructed && !isLocked

    val matCost = if (isConstructed) building.upgradeCostMaterials else building.buildCostMaterials
    val moneyCost = if (isConstructed) building.upgradeCostMoney else building.buildCostMoney
    val canAfford = resources.materials >= matCost && resources.money >= moneyCost && !isLocked

    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = if (isAvailableToBuild) TechCyan.copy(alpha = 0.5f) else FrontierBorder,
        modifier = modifier
            .clickable { onInspect() }
            .testTag("card_building_${building.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Icon + Name + Status / Level Pips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = building.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = building.category.titleRu,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Status or Level Indicator
                if (isConstructed) {
                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Ур. ${building.level} / ${building.maxLevel}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TechCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                } else if (isLocked) {
                    Surface(
                        color = DangerCrimson.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = DangerCrimson, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "База ур. ${building.requiredSettlementLevel}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DangerCrimson,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                } else {
                    Surface(
                        color = SafeEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald)
                    ) {
                        Text(
                            text = "К ПОСТРОЙКЕ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SafeEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Description
            Text(
                text = building.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            )

            // Current Effect and Next Level Preview
            Surface(
                color = FrontierDarkSurfaceHighlight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    if (isConstructed) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Текущий эффект: ",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                            )
                            Text(
                                text = "${building.dailyProductionDescription} (${building.bonusSummary})",
                                style = MaterialTheme.typography.labelSmall.copy(color = FoodGreen, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                            )
                        }

                        if (!building.isMaxLevel) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Следующий уровень: ",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                                )
                                Text(
                                    text = building.getNextLevelPreview(),
                                    style = MaterialTheme.typography.labelSmall.copy(color = TechCyan, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Эффект после постройки: ",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                            )
                            Text(
                                text = "${building.dailyProductionDescription} • ${building.bonusSummary}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TechCyan, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                            )
                        }
                    }
                }
            }

            // Cost & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cost pills
                if (!building.isMaxLevel && !isLocked) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val matOk = resources.materials >= matCost
                        val moneyOk = resources.money >= moneyCost

                        // Materials cost
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SquareFoot, contentDescription = null, tint = if (matOk) MaterialsOrange else DangerCrimson, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$matCost",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (matOk) TextWhite else DangerCrimson,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Money cost
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = if (moneyOk) CreditsYellow else DangerCrimson, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$moneyCost",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (moneyOk) TextWhite else DangerCrimson,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // XP gain badge
                        val xpGain = if (isConstructed) building.xpRewardOnUpgrade else building.xpRewardOnBuild
                        Text(
                            text = "+$xpGain XP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SafeEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                } else if (building.isMaxLevel) {
                    Text(
                        text = "⭐ МАКСИМАЛЬНЫЙ УРОВЕНЬ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                } else {
                    Text(
                        text = "Требуется ${building.requiredSettlementLevel} уровень поселения",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DangerCrimson,
                            fontSize = 11.sp
                        )
                    )
                }

                // Action Button
                if (isAvailableToBuild) {
                    Button(
                        onClick = onBuild,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) SafeEmerald else FrontierBorder,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_build_${building.id}")
                    ) {
                        Icon(Icons.Default.AddHome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Построить",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                } else if (isConstructed && !building.isMaxLevel) {
                    Button(
                        onClick = onUpgrade,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) SafeEmerald else FrontierBorder,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_upgrade_${building.id}")
                    ) {
                        Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Улучшить",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Detailed Building Blueprint Modal Dialog with lore, upgrade progression roadmap, and actions.
 */
@Composable
fun BuildingBlueprintDialog(
    building: Building,
    resources: GameResources,
    settlementLevel: Int,
    onBuild: () -> Unit,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    val (icon, accentColor) = getBuildingIconAndColor(building.type)
    val isConstructed = building.isConstructed
    val isLocked = settlementLevel < building.requiredSettlementLevel && !isConstructed
    val matCost = if (isConstructed) building.upgradeCostMaterials else building.buildCostMaterials
    val moneyCost = if (isConstructed) building.upgradeCostMoney else building.buildCostMoney
    val canAfford = resources.materials >= matCost && resources.money >= moneyCost && !isLocked

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(1.dp, accentColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = building.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = building.category.titleRu,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TechCyan,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = building.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )

                Divider(color = FrontierBorder)

                // Current Stats Breakdown
                Text(
                    text = "ПАРАМЕТРЫ ОБЪЕКТА",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TechCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    )
                )

                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Статус:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp))
                            Text(
                                text = if (isConstructed) "Работает (Ур. ${building.level}/${building.maxLevel})" else if (isLocked) "Заблокировано (Требуется ур. ${building.requiredSettlementLevel})" else "Готово к постройке",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isConstructed) SafeEmerald else if (isLocked) DangerCrimson else WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Суточная выработка:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp))
                            Text(
                                text = if (isConstructed) building.dailyProductionDescription else "Не функционирует",
                                style = MaterialTheme.typography.bodySmall.copy(color = FoodGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Стратегический бонус:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp))
                            Text(text = building.bonusSummary, style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp))
                        }
                    }
                }

                if (!building.isMaxLevel && !isLocked) {
                    Text(
                        text = if (isConstructed) "СТОИМОСТЬ МОДЕРНИЗАЦИИ" else "СТОИМОСТЬ СТРОИТЕЛЬСТВА",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WarningAmber,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val matOk = resources.materials >= matCost
                        val moneyOk = resources.money >= moneyCost

                        Surface(
                            color = FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (matOk) SafeEmerald else DangerCrimson),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "Материалы", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                                Text(
                                    text = "$matCost (в наличии: ${resources.materials})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (matOk) TextWhite else DangerCrimson,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            color = FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (moneyOk) SafeEmerald else DangerCrimson),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "Кредиты", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                                Text(
                                    text = "$moneyCost (в наличии: ${resources.money})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (moneyOk) TextWhite else DangerCrimson,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!building.isMaxLevel && !isLocked) {
                Button(
                    onClick = {
                        if (isConstructed) onUpgrade() else onBuild()
                    },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) SafeEmerald else FrontierBorder,
                        contentColor = TextWhite
                    ),
                    modifier = Modifier.testTag("dialog_btn_confirm_building_action")
                ) {
                    Text(text = if (isConstructed) "Улучшить здание" else "Построить объект")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Закрыть", color = TextMuted)
            }
        }
    )
}

/**
 * Returns icon and thematic accent color for each building type.
 */
fun getBuildingIconAndColor(type: BuildingType): Pair<ImageVector, Color> {
    return when (type) {
        BuildingType.HQ_COMMAND -> Icons.Default.Apartment to TechCyan
        BuildingType.HYDROPONICS_FARM -> Icons.Default.Grass to FoodGreen
        BuildingType.WATER_EXTRACTOR -> Icons.Default.WaterDrop to WaterCyan
        BuildingType.GENERATOR_STATION -> Icons.Default.Bolt to WarningAmber
        BuildingType.WORKSHOP -> Icons.Default.PrecisionManufacturing to MaterialsOrange
        BuildingType.STORAGE_DEPOT -> Icons.Default.Inventory2 to StoragePurple
        BuildingType.MEDICAL_CLINIC -> Icons.Default.LocalHospital to SafeEmerald
        BuildingType.DEFENSE_PERIMETER -> Icons.Default.Shield to DangerCrimson
        BuildingType.RADIO_TOWER -> Icons.Default.Podcasts to TechCyan
        BuildingType.TRADING_POST -> Icons.Default.Storefront to CreditsYellow
        BuildingType.RESEARCH_LAB -> Icons.Default.Science to TechCyan
        BuildingType.ARMORY_LAB -> Icons.Default.Gavel to DangerCrimson
        BuildingType.GREENHOUSE_COMPLEX -> Icons.Default.Eco to FoodGreen
    }
}
