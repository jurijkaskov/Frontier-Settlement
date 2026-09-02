package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.CraftConfig
import com.example.domain.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Atmospheric Workshop & Manufacturing Screen (Point 7).
 *
 * Features:
 * - Recipe catalogue with categories (Survival, Medicine, Tools, Equipment, Components)
 * - Material costs breakdown with real-time stock indicators
 * - Multi-batch manufacturing quantity dialog with MAX calculations and warehouse load preview
 * - Atomic safe crafting transactions integrated with central GameViewModel and GameResources
 * - Workshop building level requirement gating
 * - Instant feedback banner upon craft success/failure
 */
@Composable
fun WorkshopScreen(
    gameState: GameState,
    onCraft: (recipeId: String, craftCount: Int) -> Unit,
    craftResult: CraftTransactionResult? = null,
    onDismissCraftResult: () -> Unit = {},
    onUpgradeWorkshop: (() -> Unit)? = null,
    onDebugSupplies: (() -> Unit)? = null,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val res = gameState.resources
    val sett = gameState.settlement

    val workshopBuilding = sett.buildings.find {
        it.type == BuildingType.WORKSHOP && it.isConstructed
    }
    val workshopLevel = workshopBuilding?.level ?: 0
    val isWorkshopBuilt = workshopBuilding != null && workshopBuilding.isConstructed

    // Local UI State
    var selectedCategory by remember { mutableStateOf(CraftRecipeCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var dialogRecipe by remember { mutableStateOf<CraftRecipe?>(null) }

    val allRecipes = remember { CraftConfig.createDefaultRecipes() }

    // Category counts calculation
    val categoryCounts = remember(allRecipes) {
        mapOf(
            CraftRecipeCategory.ALL to allRecipes.size,
            CraftRecipeCategory.SURVIVAL to allRecipes.count { it.category == CraftRecipeCategory.SURVIVAL },
            CraftRecipeCategory.MEDICINE to allRecipes.count { it.category == CraftRecipeCategory.MEDICINE },
            CraftRecipeCategory.TOOLS to allRecipes.count { it.category == CraftRecipeCategory.TOOLS },
            CraftRecipeCategory.EQUIPMENT to allRecipes.count { it.category == CraftRecipeCategory.EQUIPMENT },
            CraftRecipeCategory.COMPONENTS to allRecipes.count { it.category == CraftRecipeCategory.COMPONENTS }
        )
    }

    // Filter recipes based on active category and search
    val filteredRecipes = remember(selectedCategory, searchQuery, allRecipes) {
        allRecipes.filter { recipe ->
            val matchesCategory = (selectedCategory == CraftRecipeCategory.ALL) || (recipe.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                    recipe.nameRu.contains(searchQuery, ignoreCase = true) ||
                    recipe.descriptionRu.contains(searchQuery, ignoreCase = true) ||
                    recipe.outputItem.category.titleRu.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
        ) {
            // 1. Top Header Bar
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("btn_workshop_back")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = TextWhite)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Мастерская базы",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                )
                                if (isWorkshopBuilt) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = TechCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "Ур. $workshopLevel",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TechCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Производство снаряжения, боеприпасов и медикаментов",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // 2. Workshop Status and Supplies HUD Card
            item {
                WorkshopHeaderCard(
                    workshopBuilding = workshopBuilding,
                    settlementLevel = sett.level,
                    usedStorage = gameState.totalWarehouseOccupiedVolume,
                    maxStorage = res.warehouseMaxCapacity,
                    resources = res,
                    onUpgradeWorkshop = onUpgradeWorkshop,
                    onDebugSupplies = onDebugSupplies
                )
            }

            // 3. Transaction Result Feedback Banner
            if (craftResult != null) {
                item {
                    CraftResultBanner(
                        result = craftResult,
                        onDismiss = onDismissCraftResult
                    )
                }
            }

            // 4. Workshop Not Built Warning Alert
            if (!isWorkshopBuilt) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DangerCrimson.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = DangerCrimson,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Мастерская не построена!",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = DangerCrimson,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Чтобы производить медикаменты, боеприпасы и детали, возведите Мастерскую в меню объектов поселения.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextWhite,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 5. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск чертежей, снаряжения, припасов...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск", tint = TechCyan) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить", tint = TextMuted)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FrontierCardBg,
                        unfocusedContainerColor = FrontierCardBg,
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_workshop_search")
                )
            }

            // 6. Category Filter Tabs
            item {
                CraftCategoryFilterTabs(
                    selectedCategory = selectedCategory,
                    categoryCounts = categoryCounts,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // 7. Blueprints Catalog Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ЧЕРТЕЖИ И ПРОИЗВОДСТВО (${filteredRecipes.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            letterSpacing = 1.sp
                        )
                    )

                    Text(
                        text = "Вместимость: ${gameState.freeWarehouseCapacity} ед. свободно",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // 8. Recipe Cards List
            if (filteredRecipes.isEmpty()) {
                item {
                    Surface(
                        color = FrontierCardBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Text("Чертежи не найдены", color = TextWhite, fontWeight = FontWeight.Bold)
                            Text("Попробуйте изменить категорию или поисковый запрос.", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filteredRecipes, key = { it.id }) { recipe ->
                    CraftRecipeCard(
                        recipe = recipe,
                        gameState = gameState,
                        workshopLevel = workshopLevel,
                        isWorkshopBuilt = isWorkshopBuilt,
                        onCraftClick = { dialogRecipe = it }
                    )
                }
            }
        }

        // Active Craft Quantity Dialog Modal
        if (dialogRecipe != null) {
            CraftQuantityDialog(
                recipe = dialogRecipe!!,
                gameState = gameState,
                workshopLevel = workshopLevel,
                onConfirm = { quantity ->
                    onCraft(dialogRecipe!!.id, quantity)
                },
                onDismiss = { dialogRecipe = null }
            )
        }
    }
}
