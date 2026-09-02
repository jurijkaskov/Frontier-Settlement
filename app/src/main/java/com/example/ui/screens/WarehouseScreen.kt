package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun WarehouseScreen(
    gameState: GameState,
    onUpgradeStorage: () -> Unit,
    onBack: () -> Unit,
    onClaimPendingCargo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val res = gameState.resources
    val sett = gameState.settlement
    val storageBuilding = sett.buildings.find { it.type == BuildingType.STORAGE_DEPOT }
    val pendingUnload = gameState.pendingSettlementUnload

    var selectedCategory by remember { mutableStateOf(WarehouseFilterCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(WarehouseSortOption.DEFAULT) }

    var selectedItemDetail by remember { mutableStateOf<WarehouseItemDisplay?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Build all items without filtering to compute category counts
    val allItems = remember(res, gameState.inventoryItems) {
        WarehouseDisplayHelper.buildDisplayEntries(
            resources = res,
            futureItems = gameState.inventoryItems,
            selectedCategory = WarehouseFilterCategory.ALL,
            searchQuery = "",
            sortOption = WarehouseSortOption.DEFAULT
        )
    }

    // Dynamic category counts
    val categoryCounts = remember(allItems) {
        mapOf(
            WarehouseFilterCategory.ALL to allItems.size,
            WarehouseFilterCategory.PRIMARY to allItems.count { it.category == WarehouseFilterCategory.PRIMARY },
            WarehouseFilterCategory.MATERIALS to allItems.count { it.category == WarehouseFilterCategory.MATERIALS },
            WarehouseFilterCategory.ITEMS to allItems.count { it.category == WarehouseFilterCategory.ITEMS },
            WarehouseFilterCategory.VALUABLES to allItems.count { it.category == WarehouseFilterCategory.VALUABLES }
        )
    }

    // Filtered and sorted items for active display
    val displayedItems = remember(res, gameState.inventoryItems, selectedCategory, searchQuery, selectedSort) {
        WarehouseDisplayHelper.buildDisplayEntries(
            resources = res,
            futureItems = gameState.inventoryItems,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery,
            sortOption = selectedSort
        )
    }

    val canAffordUpgrade = if (storageBuilding != null && !storageBuilding.isMaxLevel) {
        res.materials >= storageBuilding.upgradeCostMaterials && res.money >= storageBuilding.upgradeCostMoney
    } else false

    // Modals
    if (selectedItemDetail != null) {
        WarehouseResourceDetailDialog(
            item = selectedItemDetail!!,
            totalWarehouseCapacity = res.warehouseMaxCapacity,
            onDismiss = { selectedItemDetail = null }
        )
    }

    if (showHelpDialog) {
        WarehouseHelpDialog(
            onDismiss = { showHelpDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
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
                        modifier = Modifier.testTag("btn_warehouse_back")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = TextWhite)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Склад аванпоста",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            if (storageBuilding != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = StoragePurple.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StoragePurple.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "Ур. ${storageBuilding.level}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = StoragePurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Управление физическими припасами и ценностями базы",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // 2. Capacity & Load Status Card
        item {
            WarehouseCapacityHeaderCard(
                currentVolume = gameState.totalWarehouseOccupiedVolume,
                maxCapacity = res.warehouseMaxCapacity,
                storageBuilding = storageBuilding,
                canAffordUpgrade = canAffordUpgrade,
                onUpgradeClick = onUpgradeStorage,
                onHelpClick = { showHelpDialog = true }
            )
        }

        // 2.1 Temporary Staging Area for Cargo Overflow (if any)
        if (pendingUnload.hasPendingCargo) {
            item {
                Surface(
                    color = WarningAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
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
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Зона разгрузки у ворот",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = WarningAmber
                                        )
                                    )
                                    Text(
                                        text = "Излишки из «${pendingUnload.sourceLocationName.ifEmpty { "экспедиции" }}» (${pendingUnload.totalVolume} ед. объема)",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 10.sp)
                                    )
                                }
                            }

                            Button(
                                onClick = onClaimPendingCargo,
                                enabled = gameState.freeWarehouseCapacity > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarningAmber,
                                    contentColor = FrontierDarkBackground
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("btn_claim_pending_cargo")
                            ) {
                                Text(
                                    text = if (gameState.freeWarehouseCapacity > 0) "Разгрузить" else "Склад полон",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Search Bar & Sort Dropdown
        item {
            WarehouseSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedSort = selectedSort,
                onSortSelected = { selectedSort = it }
            )
        }

        // 4. Category Filter Chips
        item {
            WarehouseCategoryFilterChips(
                selectedCategory = selectedCategory,
                categoryCounts = categoryCounts,
                onCategorySelected = { selectedCategory = it }
            )
        }

        // 5. Section Header with count
        item {
            SectionHeader(
                title = "${selectedCategory.titleRu} (${displayedItems.size})",
                accentColor = when (selectedCategory) {
                    WarehouseFilterCategory.ALL -> SafeEmerald
                    WarehouseFilterCategory.PRIMARY -> FoodGreen
                    WarehouseFilterCategory.MATERIALS -> MaterialsOrange
                    WarehouseFilterCategory.ITEMS -> TechCyan
                    WarehouseFilterCategory.VALUABLES -> CreditsYellow
                }
            )
        }

        // 6. Items or Empty State
        if (displayedItems.isEmpty()) {
            item {
                WarehouseEmptyState(
                    title = if (searchQuery.isNotEmpty()) "По запросу «$searchQuery» ничего не найдено" else "В категории «${selectedCategory.titleRu}» пока пусто",
                    subtitle = if (searchQuery.isNotEmpty()) "Попробуйте изменить поисковый запрос или выбрать другую категорию." else "Отправляйте экспедиции в Пустошь и развивайте базу, чтобы добыть новые предметы.",
                    onResetSearch = if (searchQuery.isNotEmpty()) {
                        { searchQuery = "" }
                    } else null
                )
            }
        } else {
            items(displayedItems, key = { it.id }) { itemDisplay ->
                WarehouseResourceCard(
                    item = itemDisplay,
                    onClick = { selectedItemDetail = itemDisplay }
                )
            }
        }
    }
}
