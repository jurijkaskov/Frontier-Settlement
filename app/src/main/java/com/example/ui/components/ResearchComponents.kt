package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ResearchConfig
import com.example.domain.model.*
import com.example.ui.theme.*

/**
 * Filter mode for Research Screen.
 */
enum class TechFilterStatus(val titleRu: String) {
    ALL("Все"),
    AVAILABLE("Доступные"),
    RESEARCHED("Изученные"),
    LOCKED("Заблокированные")
}

/**
 * Tactical Header for the Research Screen displaying Research Lab building status,
 * overall technology progression, and global active tech bonuses.
 */
@Composable
fun ResearchLabOverviewHeader(
    labBuilding: Building?,
    allTechs: List<ResearchTech>,
    settlementLevel: Int,
    onNavigateToBuildings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLabBuilt = labBuilding?.isConstructed == true
    val labLevel = if (isLabBuilt) labBuilding?.level ?: 0 else 0
    val researchedCount = allTechs.count { it.isResearched }
    val totalCount = allTechs.size
    val progressFraction = if (totalCount > 0) (researchedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "tech_progress")

    // Collect high-level active bonuses for quick summary
    val storageBonus = ResearchConfig.getStorageBonus(allTechs)
    val tradeDiscount = ResearchConfig.getTradeDiscount(allTechs)
    val (squadAtk, squadDef) = ResearchConfig.getSquadCombatBonus(allTechs)
    val waterMultiplier = ResearchConfig.getProductionMultiplier(allTechs, ResourceType.WATER)
    val foodMultiplier = ResearchConfig.getProductionMultiplier(allTechs, ResourceType.FOOD)
    val matMultiplier = ResearchConfig.getProductionMultiplier(allTechs, ResourceType.MATERIALS)
    val fuelReduction = ResearchConfig.getFuelEfficiencyPercent(allTechs)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_research_overview"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FrontierDarkSurfaceElevated),
        border = BorderStroke(1.dp, if (isLabBuilt) TechCyan.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Lab Status & Upgrade button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLabBuilt) TechCyan.copy(alpha = 0.18f) else WarningAmber.copy(alpha = 0.18f))
                            .border(1.dp, if (isLabBuilt) TechCyan else WarningAmber, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLabBuilt) Icons.Default.Science else Icons.Default.Engineering,
                            contentDescription = null,
                            tint = if (isLabBuilt) TechCyan else WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isLabBuilt) "Исследовательский центр (Ур. $labLevel)" else "Центр исследований не построен",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = if (isLabBuilt) {
                                "Поселение: Ур. $settlementLevel • Доступны схемы до Ур. $labLevel"
                            } else {
                                "Постройте центр для изучения технологий"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isLabBuilt) TextMuted else WarningAmber,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                if (!isLabBuilt) {
                    FilledTonalButton(
                        onClick = onNavigateToBuildings,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WarningAmber.copy(alpha = 0.25f),
                            contentColor = WarningAmber
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_build_lab_prompt")
                    ) {
                        Text(
                            text = "Построить",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else if (labLevel < 3) {
                    OutlinedButton(
                        onClick = onNavigateToBuildings,
                        border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_upgrade_lab_prompt")
                    ) {
                        Text(
                            text = "Улучшить базу",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TechCyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Progression Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Научный прогресс аванпоста",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "$researchedCount из $totalCount (${(progressFraction * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TechCyan,
                    trackColor = FrontierBorder
                )
            }

            // Active Bonuses Strip
            if (researchedCount > 0) {
                Divider(color = FrontierBorder.copy(alpha = 0.6f), thickness = 1.dp)

                Text(
                    text = "АКТИВНЫЕ ЭФФЕКТЫ ИССЛЕДОВАНИЙ:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 2.dp)
                ) {
                    if (storageBonus > 0) {
                        item {
                            ActiveBonusChip(
                                icon = Icons.Default.Inventory2,
                                text = "+$storageBonus склад",
                                color = StoragePurple
                            )
                        }
                    }
                    if (tradeDiscount > 0) {
                        item {
                            ActiveBonusChip(
                                icon = Icons.Default.Storefront,
                                text = "-$tradeDiscount% цены рынка",
                                color = CreditsYellow
                            )
                        }
                    }
                    if (waterMultiplier > 1.0f) {
                        val pct = ((waterMultiplier - 1f) * 100).toInt()
                        item {
                            ActiveBonusChip(
                                icon = Icons.Default.WaterDrop,
                                text = "+$pct% добыча воды",
                                color = WaterCyan
                            )
                        }
                    }
                    if (foodMultiplier > 1.0f) {
                        val pct = ((foodMultiplier - 1f) * 100).toInt()
                        item {
                            ActiveBonusChip(
                                icon = Icons.Default.Grass,
                                text = "+$pct% урожай ферм",
                                color = FoodGreen
                            )
                        }
                    }
                    if (matMultiplier > 1.0f) {
                        val pct = ((matMultiplier - 1f) * 100).toInt()
                        item {
                            ActiveBonusChip(
                                icon = Icons.Default.PrecisionManufacturing,
                                text = "+$pct% переработка лома",
                                color = MaterialsOrange
                            )
                        }
                    }
                    if (fuelReduction > 0) {
                        item {
                            ActiveBonusChip(
                                icon = Icons.Default.Bolt,
                                text = "-$fuelReduction% расход топлива",
                                color = WarningAmber
                            )
                        }
                    }
                    if (squadDef > 0) {
                        item {
                            ActiveBonusChip(
                                icon = Icons.Default.Shield,
                                text = "+$squadDef защита бойцов",
                                color = SafeEmerald
                            )
                        }
                    }
                    if (squadAtk > 0) {
                        item {
                            ActiveBonusChip(
                                icon = Icons.Default.Gavel,
                                text = "+$squadAtk урон бойцов",
                                color = DangerCrimson
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveBonusChip(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

/**
 * Category filter tabs with count badges for the 4 core categories + "All".
 */
@Composable
fun ResearchCategorySelector(
    selectedCategory: TechCategory?,
    allTechs: List<ResearchTech>,
    onSelectCategory: (TechCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        null to ("Все" to Icons.Default.Apps),
        TechCategory.SETTLEMENT to ("Поселение" to Icons.Default.HomeWork),
        TechCategory.PRODUCTION to ("Производство" to Icons.Default.PrecisionManufacturing),
        TechCategory.ECONOMY to ("Экономика" to Icons.Default.Storefront),
        TechCategory.SURVIVAL to ("Выживание" to Icons.Default.Shield)
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(categories) { (cat, info) ->
            val (title, icon) = info
            val isSelected = selectedCategory == cat
            val count = if (cat == null) allTechs.size else allTechs.count { it.category == cat }
            val researched = if (cat == null) allTechs.count { it.isResearched } else allTechs.count { it.category == cat && it.isResearched }

            val accentColor = when (cat) {
                TechCategory.SETTLEMENT -> WaterCyan
                TechCategory.PRODUCTION -> MaterialsOrange
                TechCategory.ECONOMY -> CreditsYellow
                TechCategory.SURVIVAL -> SafeEmerald
                null -> TechCyan
                else -> TechCyan
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectCategory(cat) }
                    .testTag("tab_tech_category_${cat?.id ?: "all"}"),
                color = if (isSelected) accentColor.copy(alpha = 0.2f) else FrontierDarkSurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) accentColor else FrontierBorder
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) accentColor else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isSelected) TextWhite else TextMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) accentColor.copy(alpha = 0.3f) else FrontierSurface)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "$researched/$count",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) accentColor else TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Secondary status filter chips (All, Available, Researched, Locked).
 */
@Composable
fun ResearchStatusFilterSelector(
    selectedFilter: TechFilterStatus,
    onSelectFilter: (TechFilterStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TechFilterStatus.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onSelectFilter(filter) },
                label = {
                    Text(
                        text = filter.titleRu,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FrontierPrimaryContainer,
                    selectedLabelColor = TechCyan,
                    containerColor = FrontierDarkSurface,
                    labelColor = TextMuted
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = FrontierBorder,
                    selectedBorderColor = TechCyan,
                    enabled = true,
                    selected = isSelected
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(30.dp)
                    .testTag("filter_status_${filter.name.lowercase()}")
            )
        }
    }
}

/**
 * Rich Interactive Card for an individual Research Technology.
 */
@Composable
fun TechCardView(
    validationInfo: TechValidationInfo,
    resources: GameResources,
    onResearch: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tech = validationInfo.tech
    val status = validationInfo.status
    val isResearched = tech.isResearched
    val canBeResearched = validationInfo.canBeResearched

    val categoryColor = when (tech.category) {
        TechCategory.SETTLEMENT -> WaterCyan
        TechCategory.PRODUCTION -> MaterialsOrange
        TechCategory.ECONOMY -> CreditsYellow
        TechCategory.SURVIVAL -> SafeEmerald
        else -> TechCyan
    }

    val cardBg = when {
        isResearched -> Color(0xFF0F241C)
        canBeResearched -> Color(0xFF14202B)
        else -> FrontierDarkSurfaceElevated
    }

    val cardBorderColor = when {
        isResearched -> SafeEmerald.copy(alpha = 0.7f)
        canBeResearched -> TechCyan.copy(alpha = 0.8f)
        else -> FrontierBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpenDetails() }
            .testTag("card_tech_${tech.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category, Tier badge & Status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    Surface(
                        color = categoryColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = tech.category.titleRu.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = categoryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Tier Pill
                    Surface(
                        color = FrontierSurface,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, FrontierBorder)
                    ) {
                        Text(
                            text = "УР. ${tech.tier}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Status Badge
                TechStatusBadge(status = status)
            }

            // Tech Title
            Text(
                text = tech.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isResearched) SafeEmerald else TextWhite,
                    fontSize = 15.sp
                )
            )

            // Short Description
            Text(
                text = tech.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Primary Effect Highlight Box
            Surface(
                color = if (isResearched) SafeEmerald.copy(alpha = 0.12f) else TechCyan.copy(alpha = 0.10f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    if (isResearched) SafeEmerald.copy(alpha = 0.35f) else TechCyan.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isResearched) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isResearched) SafeEmerald else TechCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Эффект: ${tech.effectSummary}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isResearched) SafeEmerald else TechCyan,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Prerequisites row (if any)
            if (validationInfo.unsatisfiedPrerequisites.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = DangerCrimson,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Требуется: ${validationInfo.unsatisfiedPrerequisites.joinToString { it.title }}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DangerCrimson,
                            fontSize = 10.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Resource Costs and Action Button Bar
            if (!isResearched) {
                Divider(color = FrontierBorder.copy(alpha = 0.5f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Resource Cost Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        tech.requirements.resourceCosts.forEach { (resType, cost) ->
                            val currentStock = resources[resType]
                            val isAffordable = currentStock >= cost
                            item {
                                ResourceCostPill(
                                    resourceType = resType,
                                    cost = cost,
                                    currentStock = currentStock,
                                    isAffordable = isAffordable
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Direct Research Action Button
                    Button(
                        onClick = onResearch,
                        enabled = canBeResearched,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafeEmerald,
                            disabledContainerColor = FrontierBorder.copy(alpha = 0.5f),
                            contentColor = TextWhite,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_research_${tech.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (canBeResearched) Icons.Default.Biotech else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Изучить",
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
}

/**
 * Status badge with distinctive coloring and icon.
 */
@Composable
fun TechStatusBadge(status: TechStatus) {
    val (label, color, icon) = when (status) {
        TechStatus.RESEARCHED -> Triple("Изучено", SafeEmerald, Icons.Default.Check)
        TechStatus.AVAILABLE -> Triple("Готово к изучению", TechCyan, Icons.Default.PlayArrow)
        TechStatus.INSUFFICIENT_RESOURCES -> Triple("Нехватка ресурсов", WarningAmber, Icons.Default.Warning)
        TechStatus.LOCKED_DEPENDENCY -> Triple("Требуется чертёж", DangerCrimson, Icons.Default.Lock)
        TechStatus.LOCKED_LAB_LEVEL -> Triple("Лаборатория Ур. 2+", TechCyan, Icons.Default.Upgrade)
        TechStatus.LOCKED_SETTLEMENT_LEVEL -> Triple("Требуется уровень базы", WarningAmber, Icons.Default.Lock)
        TechStatus.LOCKED_LAB_UNBUILT -> Triple("Лаборатория не построена", DangerCrimson, Icons.Default.BuildCircle)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Resource cost badge showing cost and dynamic green/red affordability color coding.
 */
@Composable
fun ResourceCostPill(
    resourceType: ResourceType,
    cost: Int,
    currentStock: Int,
    isAffordable: Boolean
) {
    val pillColor = if (isAffordable) SafeEmerald else DangerCrimson

    Surface(
        color = if (isAffordable) FrontierDarkSurface else DangerCrimson.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, pillColor.copy(alpha = 0.4f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = resourceType.symbol,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
            )
            Text(
                text = "$cost",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isAffordable) TextWhite else DangerCrimson,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Unbuilt Lab Card shown when the Research Lab building has not been constructed.
 */
@Composable
fun UnbuiltResearchCenterCard(
    onNavigateToBuildings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_unbuilt_lab_warning"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF23180F)),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(WarningAmber.copy(alpha = 0.2f))
                    .border(1.dp, WarningAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Construction,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Исследовательский центр не построен",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = "Для изучения передовых чертежей, технологий укрепления базы и экипировки необходимо возвести Исследовательский центр в меню «Постройки».",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            )

            Button(
                onClick = onNavigateToBuildings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningAmber,
                    contentColor = FrontierDarkSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_go_to_build_lab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Перейти к строительству",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
