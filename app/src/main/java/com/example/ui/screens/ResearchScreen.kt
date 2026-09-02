package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ResearchConfig
import com.example.domain.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ResearchScreen(
    gameState: GameState,
    onResearchTech: (String) -> Unit,
    onBack: () -> Unit,
    onNavigateToBuildings: () -> Unit = {},
    onDebugAddSupplies: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val res = gameState.resources
    val techs = gameState.technologies
    val settlement = gameState.settlement

    val labBuilding = settlement.buildings.find {
        (it.type == BuildingType.RESEARCH_LAB || it.type == BuildingType.ARMORY_LAB) && it.isConstructed
    }

    var selectedCategory by remember { mutableStateOf<TechCategory?>(null) }
    var selectedFilter by remember { mutableStateOf(TechFilterStatus.ALL) }
    var selectedTechForDialog by remember { mutableStateOf<TechValidationInfo?>(null) }

    // Validate all technologies against current game state
    val validatedTechs = remember(techs, labBuilding, settlement.level, res) {
        techs.map { tech ->
            ResearchConfig.validateTech(
                tech = tech,
                allTechs = techs,
                labBuilding = labBuilding,
                settlementLevel = settlement.level,
                resources = res
            )
        }
    }

    // Filter by category and status
    val filteredTechs = remember(validatedTechs, selectedCategory, selectedFilter) {
        validatedTechs.filter { item ->
            val matchCategory = selectedCategory == null || item.tech.category == selectedCategory
            val matchStatus = when (selectedFilter) {
                TechFilterStatus.ALL -> true
                TechFilterStatus.AVAILABLE -> item.canBeResearched
                TechFilterStatus.RESEARCHED -> item.tech.isResearched
                TechFilterStatus.LOCKED -> !item.canBeResearched && !item.tech.isResearched
            }
            matchCategory && matchStatus
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FrontierDarkBackground)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // 1. Top Bar
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_research_back")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = TextWhite)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Исследования и технологии",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Научно-исследовательский комплекс поселения",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // 2. Resource Quick Bar
        item {
            ResearchResourceBar(resources = res)
        }

        // 3. Research Lab Overview Header & Active Bonuses
        item {
            ResearchLabOverviewHeader(
                labBuilding = labBuilding,
                allTechs = techs,
                settlementLevel = settlement.level,
                onNavigateToBuildings = onNavigateToBuildings
            )
        }

        // 4. If Lab is not constructed, show tactical alert
        if (labBuilding == null) {
            item {
                UnbuiltResearchCenterCard(onNavigateToBuildings = onNavigateToBuildings)
            }
        }

        // 5. Category Tabs
        item {
            ResearchCategorySelector(
                selectedCategory = selectedCategory,
                allTechs = techs,
                onSelectCategory = { selectedCategory = it }
            )
        }

        // 6. Status Filter Chips
        item {
            ResearchStatusFilterSelector(
                selectedFilter = selectedFilter,
                onSelectFilter = { selectedFilter = it }
            )
        }

        // 7. Technology Cards List
        if (filteredTechs.isEmpty()) {
            item {
                EmptyTechsCard(selectedFilter = selectedFilter)
            }
        } else {
            items(
                items = filteredTechs,
                key = { it.tech.id }
            ) { validationInfo ->
                TechCardView(
                    validationInfo = validationInfo,
                    resources = res,
                    onResearch = { onResearchTech(validationInfo.tech.id) },
                    onOpenDetails = { selectedTechForDialog = validationInfo }
                )
            }
        }
    }

    // Modal Details Dialog
    selectedTechForDialog?.let { currentValidation ->
        // Keep validation up-to-date with current state if dialog is open
        val latestValidation = validatedTechs.find { it.tech.id == currentValidation.tech.id } ?: currentValidation
        ResearchDetailDialog(
            validationInfo = latestValidation,
            resources = res,
            onResearch = { onResearchTech(latestValidation.tech.id) },
            onDismiss = { selectedTechForDialog = null }
        )
    }
}

/**
 * Tactical Resource Strip for the Research Screen.
 */
@Composable
private fun ResearchResourceBar(
    resources: GameResources,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
    ) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                MiniResourceItem(
                    symbol = "💰",
                    amount = resources.money,
                    label = "Кредиты",
                    color = CreditsYellow
                )
            }
            item {
                MiniResourceItem(
                    symbol = "📦",
                    amount = resources.materials,
                    label = "Материалы",
                    color = MaterialsOrange
                )
            }
            item {
                MiniResourceItem(
                    symbol = "⚙️",
                    amount = resources[ResourceType.COMPONENTS],
                    label = "Детали",
                    color = TechCyan
                )
            }
            item {
                MiniResourceItem(
                    symbol = "💎",
                    amount = resources[ResourceType.RARE_ALLOY],
                    label = "Сплавы",
                    color = StoragePurple
                )
            }
            item {
                MiniResourceItem(
                    symbol = "💊",
                    amount = resources[ResourceType.MEDICINE],
                    label = "Медикаменты",
                    color = SafeEmerald
                )
            }
        }
    }
}

@Composable
private fun MiniResourceItem(
    symbol: String,
    amount: Int,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = symbol, fontSize = 12.sp)
        Text(
            text = "$amount",
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun EmptyTechsCard(selectedFilter: TechFilterStatus) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "В данной категории нет технологий со статусом «${selectedFilter.titleRu}»",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
