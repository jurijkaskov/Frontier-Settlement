package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CraftConfig
import com.example.domain.model.*
import com.example.ui.theme.*

/**
 * Tactical Header Card for Settlement Workshop & Manufacturing Terminal.
 */
@Composable
fun WorkshopHeaderCard(
    workshopBuilding: Building?,
    settlementLevel: Int,
    usedStorage: Int,
    maxStorage: Int,
    resources: GameResources,
    onUpgradeWorkshop: (() -> Unit)? = null,
    onDebugSupplies: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isConstructed = workshopBuilding != null && workshopBuilding.isConstructed
    val level = workshopBuilding?.level ?: 0

    val storageFraction = if (maxStorage > 0) (usedStorage.toFloat() / maxStorage.toFloat()).coerceIn(0f, 1f) else 0f
    val storageStatus = WarehouseCapacityStatus.fromUsageFraction(storageFraction)

    Card(
        colors = CardDefaults.cardColors(containerColor = FrontierCardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_workshop_header")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            TechCyan.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Title, Status and Level Badge
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TechCyan.copy(alpha = 0.2f))
                            .border(1.dp, TechCyan, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Мастерская",
                            tint = TechCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Мастерская поселения",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 17.sp
                                )
                            )
                        }
                        Text(
                            text = if (isConstructed) "Производственный комплекс Ур. $level" else "Объект не построен",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isConstructed) TechCyan else DangerCrimson,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                if (isConstructed) {
                    Surface(
                        color = TechCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "Ур. $level",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TechCyan,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Description / Lore
            Text(
                text = if (isConstructed) {
                    "«Сборка снаряжения, очистка топлива и создание высокотехнологичных приборов из найденных компонентов пустошей»."
                } else {
                    "Мастерская не возведена. Для открытия чертежей постройте Мастерскую во вкладке объектов поселения."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )

            // Storage and settlement info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FrontierDarkSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Склад",
                        tint = storageStatus.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Склад: $usedStorage / $maxStorage ед.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Surface(
                    color = storageStatus.accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = storageStatus.titleRu,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = storageStatus.accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Key Crafting Supplies Quick HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CraftSuppliesChip(name = "Стройматериалы", amount = resources.materials, color = WarningAmber, icon = Icons.Default.HomeRepairService)
                CraftSuppliesChip(name = "Компоненты", amount = resources[ResourceType.COMPONENTS], color = TechCyan, icon = Icons.Default.Memory)
                CraftSuppliesChip(name = "Сплавы", amount = resources[ResourceType.RARE_ALLOY], color = StoragePurple, icon = Icons.Default.Diamond)
                CraftSuppliesChip(name = "Кредиты", amount = resources.money, color = CreditsYellow, icon = Icons.Default.MonetizationOn)
            }

            // Quick Debug Action (testing convenience)
            if (onDebugSupplies != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDebugSupplies,
                        modifier = Modifier.testTag("btn_debug_craft_supplies")
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = TechCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Тест: Пополнить сырьё (+компоненты)", style = MaterialTheme.typography.labelSmall.copy(color = TechCyan))
                    }
                }
            }
        }
    }
}

@Composable
private fun CraftSuppliesChip(
    name: String,
    amount: Int,
    color: Color,
    icon: ImageVector
) {
    Surface(
        color = FrontierDarkSurface.copy(alpha = 0.8f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = name, tint = color, modifier = Modifier.size(14.dp))
            Text(
                text = "$amount",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 11.sp
                )
            )
        }
    }
}

/**
 * Filter tabs for Crafting Blueprints categories.
 */
@Composable
fun CraftCategoryFilterTabs(
    selectedCategory: CraftRecipeCategory,
    categoryCounts: Map<CraftRecipeCategory, Int>,
    onCategorySelected: (CraftRecipeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("row_craft_category_tabs"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(CraftRecipeCategory.entries) { category ->
            val isSelected = selectedCategory == category
            val count = categoryCounts[category] ?: 0

            val icon = when (category) {
                CraftRecipeCategory.ALL -> Icons.Default.Apps
                CraftRecipeCategory.SURVIVAL -> Icons.Default.LocalFireDepartment
                CraftRecipeCategory.MEDICINE -> Icons.Default.MedicalServices
                CraftRecipeCategory.TOOLS -> Icons.Default.Handyman
                CraftRecipeCategory.EQUIPMENT -> Icons.Default.Shield
                CraftRecipeCategory.COMPONENTS -> Icons.Default.Memory
            }

            Surface(
                color = if (isSelected) TechCyan else FrontierCardBg,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) TechCyan else BorderSubtle
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onCategorySelected(category) }
                    .testTag("tab_craft_cat_${category.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.titleRu,
                        tint = if (isSelected) FrontierDarkBackground else TechCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = category.titleRu,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) FrontierDarkBackground else TextWhite,
                            fontSize = 12.sp
                        )
                    )
                    Surface(
                        color = if (isSelected) FrontierDarkBackground.copy(alpha = 0.25f) else FrontierDarkSurface,
                        shape = CircleShape
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) FrontierDarkBackground else TextMuted,
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

/**
 * Individual Craft Recipe Blueprint Card with Ingredients, Lock Status, and Action Buttons.
 */
@Composable
fun CraftRecipeCard(
    recipe: CraftRecipe,
    gameState: GameState,
    workshopLevel: Int,
    isWorkshopBuilt: Boolean,
    onCraftClick: (CraftRecipe) -> Unit,
    modifier: Modifier = Modifier
) {
    val res = gameState.resources
    val researchedTechIds = remember(gameState.technologies) {
        gameState.technologies.filter { it.isResearched }.map { it.id }.toSet()
    }
    val isUnlocked = isWorkshopBuilt && recipe.isUnlocked(workshopLevel, gameState.settlement.level, researchedTechIds)
    val maxCraftable = if (isUnlocked) CraftConfig.calculateMaxCraftCount(recipe, res, gameState.inventoryItems) else 0

    val item = recipe.outputItem
    val rarityColor = item.rarity.color

    // Check if player has each required resource
    val resourceAvailability = recipe.requiredResources.mapValues { (type, amount) ->
        res[type] >= amount
    }
    val hasAllResources = resourceAvailability.values.all { it }
    val canCraftAtLeastOne = isUnlocked && maxCraftable > 0 && !gameState.isWarehouseFull

    Card(
        colors = CardDefaults.cardColors(containerColor = FrontierCardBg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnlocked && canCraftAtLeastOne) TechCyan.copy(alpha = 0.5f)
            else if (!isUnlocked) BorderSubtle.copy(alpha = 0.5f)
            else BorderSubtle
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_craft_recipe_${recipe.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Item Icon, Name, Output Yield Badge, Rarity Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Recipe Icon Box
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(rarityColor.copy(alpha = 0.15f))
                            .border(1.dp, rarityColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconVector = when (recipe.iconKey) {
                            "medkit" -> Icons.Default.MedicalServices
                            "toolkit" -> Icons.Default.Handyman
                            "ration" -> Icons.Default.LunchDining
                            "fuel_canister" -> Icons.Default.LocalGasStation
                            "armor_plate" -> Icons.Default.Shield
                            "ammo_crate" -> Icons.Default.Inventory
                            "scanner" -> Icons.Default.Sensors
                            "microchip" -> Icons.Default.Memory
                            else -> Icons.Default.Build
                        }

                        Icon(
                            imageVector = iconVector,
                            contentDescription = recipe.nameRu,
                            tint = rarityColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = recipe.nameRu,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (recipe.outputQuantity > 1) {
                                Surface(
                                    color = SafeEmerald.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "+${recipe.outputQuantity} шт.",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SafeEmerald,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.rarity.titleRu,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = rarityColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "•",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${item.unitSize} ед. объёма",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Level / Lock Badge
                if (!isUnlocked) {
                    val lockReason = when {
                        !isWorkshopBuilt -> "Нет Мастерской"
                        recipe.requiredTechId != null && !researchedTechIds.contains(recipe.requiredTechId) -> "Требует технологию"
                        workshopLevel < recipe.minWorkshopLevel -> "Мастерская Ур. ${recipe.minWorkshopLevel}"
                        gameState.settlement.level < recipe.requiredSettlementLevel -> "База Ур. ${recipe.requiredSettlementLevel}"
                        else -> "Заблокировано"
                    }

                    Surface(
                        color = DangerCrimson.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Заблокировано", tint = DangerCrimson, modifier = Modifier.size(12.dp))
                            Text(
                                text = lockReason,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DangerCrimson,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            // Description
            Text(
                text = recipe.descriptionRu,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Required Ingredients Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FrontierDarkSurface)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "ТРЕБУЕМЫЕ МАТЕРИАЛЫ (на 1 партию):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recipe.requiredResources.forEach { (type, reqAmount) ->
                        val playerHas = res[type]
                        val isEnough = playerHas >= reqAmount

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            val icon = when (type) {
                                ResourceType.MATERIALS -> Icons.Default.HomeRepairService
                                ResourceType.COMPONENTS -> Icons.Default.Memory
                                ResourceType.RARE_ALLOY -> Icons.Default.Diamond
                                ResourceType.MEDICINE -> Icons.Default.MedicalServices
                                ResourceType.AMMO -> Icons.Default.Inventory
                                ResourceType.FOOD -> Icons.Default.Restaurant
                                ResourceType.WATER -> Icons.Default.WaterDrop
                                ResourceType.FUEL -> Icons.Default.LocalGasStation
                                ResourceType.MONEY -> Icons.Default.MonetizationOn
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = type.nameRu,
                                tint = if (isEnough) SafeEmerald else DangerCrimson,
                                modifier = Modifier.size(13.dp)
                            )

                            Text(
                                text = "${type.nameRu}: $playerHas/$reqAmount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isEnough) TextWhite else DangerCrimson,
                                    fontWeight = if (isEnough) FontWeight.Normal else FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Actions & Craft Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Max Craftable indicator
                if (isUnlocked) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (maxCraftable > 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (maxCraftable > 0) SafeEmerald else WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (maxCraftable > 0) "Доступно для сборки: $maxCraftable" else "Недостаточно ресурсов",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (maxCraftable > 0) SafeEmerald else WarningAmber,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                } else {
                    Text(
                        text = "Требуется чертёж и модернизация базы",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DangerCrimson,
                            fontSize = 11.sp
                        )
                    )
                }

                // Craft Button
                Button(
                    onClick = { onCraftClick(recipe) },
                    enabled = isUnlocked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canCraftAtLeastOne) TechCyan else FrontierCardBg,
                        contentColor = if (canCraftAtLeastOne) FrontierDarkBackground else TextMuted,
                        disabledContainerColor = FrontierDarkSurface,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_craft_recipe_${recipe.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (!isUnlocked) "Закрыто" else "Собрать",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * Result Feedback Banner for Craft Operations.
 */
@Composable
fun CraftResultBanner(
    result: CraftTransactionResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSuccess = result.isSuccess
    val color = if (isSuccess) SafeEmerald else DangerCrimson

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("banner_craft_result")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
