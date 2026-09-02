package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.*
import com.example.ui.theme.*

/**
 * Mapping helper for warehouse item icons and colors.
 */
fun getWarehouseItemIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "money" -> Icons.Default.MonetizationOn
        "food" -> Icons.Default.Restaurant
        "water" -> Icons.Default.WaterDrop
        "fuel" -> Icons.Default.LocalGasStation
        "materials" -> Icons.Default.Build
        "medicine" -> Icons.Default.Medication
        "ammo" -> Icons.Default.Shield
        "components" -> Icons.Default.Memory
        "rare_alloy" -> Icons.Default.Diamond
        else -> Icons.Default.Inventory2
    }
}

fun getWarehouseItemColor(iconKey: String, rarity: ItemRarity): Color {
    return when (iconKey) {
        "money" -> CreditsYellow
        "food" -> FoodGreen
        "water" -> WaterCyan
        "fuel" -> FuelAmber
        "materials" -> MaterialsOrange
        "medicine" -> SafeEmerald
        "ammo" -> DangerCrimson
        "components" -> TechCyan
        "rare_alloy" -> StoragePurple
        else -> rarity.color
    }
}

/**
 * Main capacity summary card for the warehouse.
 */
@Composable
fun WarehouseCapacityHeaderCard(
    currentVolume: Int,
    maxCapacity: Int,
    storageBuilding: Building?,
    canAffordUpgrade: Boolean,
    onUpgradeClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usageFraction = if (maxCapacity > 0) (currentVolume.toFloat() / maxCapacity.toFloat()).coerceIn(0f, 1f) else 0f
    val capacityStatus = WarehouseCapacityStatus.fromUsageFraction(usageFraction)
    val availableSpace = (maxCapacity - currentVolume).coerceAtLeast(0)

    val animatedFraction by animateFloatAsState(
        targetValue = usageFraction,
        animationSpec = tween(durationMillis = 600),
        label = "capacity_bar"
    )

    val animatedColor by animateColorAsState(
        targetValue = capacityStatus.accentColor,
        animationSpec = tween(durationMillis = 400),
        label = "capacity_color"
    )

    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = animatedColor.copy(alpha = 0.8f),
        modifier = modifier.testTag("warehouse_capacity_card")
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(animatedColor.copy(alpha = 0.15f))
                            .border(1.dp, animatedColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = animatedColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Заполненность склада",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            if (storageBuilding != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = StoragePurple.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, StoragePurple.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "Ур. ${storageBuilding.level}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = StoragePurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = capacityStatus.subtitleRu,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (usageFraction >= 0.75f) animatedColor else TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onHelpClick,
                    modifier = Modifier.size(32.dp).testTag("btn_warehouse_help")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Справка по складу",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Volume Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Занято места",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 11.sp)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentVolume",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = animatedColor
                            )
                        )
                        Text(
                            text = " / $maxCapacity ед.",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                        )
                    }
                }

                Surface(
                    color = animatedColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, animatedColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${(usageFraction * 100).toInt()}% загрузки",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = animatedColor,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Storage Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(FrontierDarkBackground)
                    .border(0.5.dp, FrontierBorder, RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedFraction)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    animatedColor.copy(alpha = 0.7f),
                                    animatedColor
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats Sub-row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Свободно: $availableSpace ед. объема",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (availableSpace > 0) TechCyan else DangerCrimson,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = "Статус: ${capacityStatus.titleRu}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = animatedColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }

            // Upgrade Button if storage depot exists and can be upgraded
            if (storageBuilding != null && !storageBuilding.isMaxLevel) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = FrontierBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onUpgradeClick,
                    enabled = canAffordUpgrade,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StoragePurple,
                        disabledContainerColor = StoragePurple.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_upgrade_storage")
                ) {
                    Icon(
                        imageVector = Icons.Default.Upgrade,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Расширить склад (Ур. ${storageBuilding.level} → ${storageBuilding.level + 1}) • +300 ед.",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Filter chips for warehouse categories with dynamic item counts.
 */
@Composable
fun WarehouseCategoryFilterChips(
    selectedCategory: WarehouseFilterCategory,
    categoryCounts: Map<WarehouseFilterCategory, Int>,
    onCategorySelected: (WarehouseFilterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WarehouseFilterCategory.entries.forEach { category ->
            val isSelected = selectedCategory == category
            val count = categoryCounts[category] ?: 0

            val chipBorderColor = if (isSelected) TechCyan else FrontierBorder
            val chipBackgroundColor = if (isSelected) TechCyan.copy(alpha = 0.18f) else FrontierDarkSurfaceElevated
            val textColor = if (isSelected) TextWhite else TextMuted

            Surface(
                color = chipBackgroundColor,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, chipBorderColor),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCategorySelected(category) }
                    .testTag("warehouse_tab_${category.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = category.titleRu,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = if (isSelected) TechCyan else FrontierDarkSurfaceHighlight,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) FrontierDarkBackground else TextSubtle,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Search bar and sort option dropdown for warehouse.
 */
@Composable
fun WarehouseSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedSort: WarehouseSortOption,
    onSortSelected: (WarehouseSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Поиск припасов...",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 12.sp)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Очистить",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FrontierDarkSurfaceElevated,
                unfocusedContainerColor = FrontierDarkSurfaceElevated,
                focusedBorderColor = TechCyan,
                unfocusedBorderColor = FrontierBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = TechCyan
            ),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("warehouse_search_input")
        )

        // Sort button with dropdown menu
        Box {
            IconButton(
                onClick = { sortMenuExpanded = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(FrontierDarkSurfaceElevated)
                    .border(1.dp, FrontierBorder, RoundedCornerShape(10.dp))
                    .testTag("warehouse_sort_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Сортировка",
                    tint = if (selectedSort != WarehouseSortOption.DEFAULT) TechCyan else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false },
                modifier = Modifier
                    .background(FrontierDarkSurfaceElevated)
                    .border(1.dp, FrontierBorder, RoundedCornerShape(8.dp))
            ) {
                WarehouseSortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.titleRu,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (selectedSort == option) TechCyan else TextWhite,
                                    fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            )
                        },
                        leadingIcon = {
                            if (selectedSort == option) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = TechCyan, modifier = Modifier.size(16.dp))
                            }
                        },
                        onClick = {
                            onSortSelected(option)
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Visual card for an individual resource or warehouse item.
 */
@Composable
fun WarehouseResourceCard(
    item: WarehouseItemDisplay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = getWarehouseItemIcon(item.iconKey)
    val itemColor = getWarehouseItemColor(item.iconKey, item.rarity)

    val (statusText, statusColor) = when (item.stateLevel) {
        ResourceStateLevel.NORMAL -> "В норме" to SafeEmerald
        ResourceStateLevel.LOW -> "Мало" to WarningAmber
        ResourceStateLevel.CRITICAL -> "Критический!" to DangerCrimson
    }

    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = if (item.stateLevel == ResourceStateLevel.CRITICAL) DangerCrimson.copy(alpha = 0.7f) else FrontierBorder,
        onClick = onClick,
        modifier = modifier.testTag("warehouse_item_${item.id}")
    ) {
        Column {
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(itemColor.copy(alpha = 0.15f))
                            .border(1.dp, itemColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = itemColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (item.rarity != ItemRarity.COMMON) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = item.rarity.color.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, item.rarity.color.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = item.rarity.titleRu,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = item.rarity.color,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = item.purposeRu.ifEmpty { item.categoryLabel },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Amount & Unit Suffix
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (item.isPhysical) "${item.quantity} ед." else "${item.quantity} кр.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = itemColor
                        )
                    )

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, statusColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Storage Occupancy Bar / Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.isPhysical) {
                        "Занимает: ${item.totalStorageVolume} ед. склада (${item.unitSize} ед./шт)"
                    } else {
                        "Электронный счёт (0 ед. склада)"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (item.isPhysical) TechCyan else CreditsYellow,
                        fontSize = 10.sp
                    )
                )

                Text(
                    text = "Цена: ~${item.baseMarketValueCredits} кр./ед.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSubtle,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

/**
 * Empty state for warehouse categories or search results.
 */
@Composable
fun WarehouseEmptyState(
    title: String = "В этой категории пока ничего нет",
    subtitle: String = "Отправляйте экспедиции в Пустошь, стройте производственные здания и торгуйте с караванами, чтобы пополнить запасы.",
    onResetSearch: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    GameCard(
        backgroundColor = FrontierDarkSurface,
        borderColor = FrontierBorder,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FrontierDarkSurfaceHighlight)
                    .border(1.dp, FrontierBorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = null,
                    tint = TextSubtle,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if (onResetSearch != null) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onResetSearch,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan)
                ) {
                    Text(text = "Сбросить поиск", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * Detailed tactical dialog showing in-depth resource information, lore, sources, uses, and market price.
 */
@Composable
fun WarehouseResourceDetailDialog(
    item: WarehouseItemDisplay,
    totalWarehouseCapacity: Int,
    onDismiss: () -> Unit
) {
    val icon = getWarehouseItemIcon(item.iconKey)
    val itemColor = getWarehouseItemColor(item.iconKey, item.rarity)
    val volumeSharePercent = if (totalWarehouseCapacity > 0 && item.isPhysical) {
        ((item.totalStorageVolume.toFloat() / totalWarehouseCapacity.toFloat()) * 100).toInt()
    } else 0

    Dialog(onDismissRequest = onDismiss) {
        GameCard(
            backgroundColor = FrontierDarkSurfaceElevated,
            borderColor = itemColor.copy(alpha = 0.8f),
            shapeRadius = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("dialog_warehouse_resource_detail")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(itemColor.copy(alpha = 0.15f))
                                .border(1.dp, itemColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = itemColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = TechCyan.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = item.categoryLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TechCyan,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (item.rarity != ItemRarity.COMMON) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = item.rarity.color.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = item.rarity.titleRu,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = item.rarity.color,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_detail")) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = FrontierBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, FrontierBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "В наличии",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp)
                            )
                            Text(
                                text = if (item.isPhysical) "${item.quantity} ед." else "${item.quantity} кр.",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = itemColor
                                )
                            )
                        }
                    }

                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, FrontierBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Объем на складе",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp)
                            )
                            Text(
                                text = if (item.isPhysical) "${item.totalStorageVolume} ед. ($volumeSharePercent%)" else "0 ед.",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isPhysical) TechCyan else CreditsYellow
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, FrontierBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Вес 1 единицы",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp)
                            )
                            Text(
                                text = if (item.isPhysical) "${item.unitSize} ед. склада" else "Не занимает место",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                            )
                        }
                    }

                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, FrontierBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Базовая стоимость",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp)
                            )
                            Text(
                                text = "~${item.baseMarketValueCredits} кр. / ед.",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = CreditsYellow
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description & Lore
                Text(
                    text = "Описание и назначение:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )

                // Sources
                if (item.sourcesRu.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Источники получения:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SafeEmerald
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    item.sourcesRu.forEach { source ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(SafeEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = source,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextWhite,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Uses
                if (item.usesRu.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Применение и расходы:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TechCyan
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    item.usesRu.forEach { use ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(TechCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = use,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextWhite,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Status Evaluation Box
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = when (item.stateLevel) {
                        ResourceStateLevel.NORMAL -> SafeEmerald.copy(alpha = 0.12f)
                        ResourceStateLevel.LOW -> WarningAmber.copy(alpha = 0.12f)
                        ResourceStateLevel.CRITICAL -> DangerCrimson.copy(alpha = 0.12f)
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (item.stateLevel) {
                            ResourceStateLevel.NORMAL -> SafeEmerald.copy(alpha = 0.4f)
                            ResourceStateLevel.LOW -> WarningAmber.copy(alpha = 0.4f)
                            ResourceStateLevel.CRITICAL -> DangerCrimson.copy(alpha = 0.4f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = when (item.stateLevel) {
                                ResourceStateLevel.NORMAL -> "✅ Статус: Нормальные запасы"
                                ResourceStateLevel.LOW -> "⚠️ Статус: Запасы истощаются (порог: < ${item.lowThreshold} ед.)"
                                ResourceStateLevel.CRITICAL -> "🚨 Статус: Критический дефицит! (порог: < ${item.criticalThreshold} ед.)"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (item.stateLevel) {
                                    ResourceStateLevel.NORMAL -> SafeEmerald
                                    ResourceStateLevel.LOW -> WarningAmber
                                    ResourceStateLevel.CRITICAL -> DangerCrimson
                                },
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Понятно",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * Tactical help dialog explaining warehouse mechanics.
 */
@Composable
fun WarehouseHelpDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GameCard(
            backgroundColor = FrontierDarkSurfaceElevated,
            borderColor = StoragePurple.copy(alpha = 0.8f),
            shapeRadius = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("dialog_warehouse_help")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StoragePurple.copy(alpha = 0.15f))
                                .border(1.dp, StoragePurple.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = StoragePurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Справочник Склада",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = FrontierBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "1. Физические припасы и объем:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TechCyan
                    )
                )
                Text(
                    text = "Еда, вода, топливо, стройматериалы, медикаменты и патроны занимают физическое место (1 ед. объема за 1 шт.). Редкие сплавы имеют повышенный вес (2 ед. склада).",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "2. Электронные кредиты (деньги):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CreditsYellow
                    )
                )
                Text(
                    text = "Кредиты хранятся на цифровых счетах и не занимают объема в хранилище.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "3. Защита от переполнения:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DangerCrimson
                    )
                )
                Text(
                    text = "Когда склад заполнен на 100%, новые ресурсы из экспедиций и производства не смогут поместиться и будут утеряны.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "4. Расширение вместимости:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = StoragePurple
                    )
                )
                Text(
                    text = "Каждый уровень Склада даёт +300 единиц максимального объема. Улучшайте склад своевременно!",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = StoragePurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Закрыть", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
