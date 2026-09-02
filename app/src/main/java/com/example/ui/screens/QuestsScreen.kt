package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.domain.model.GameState
import com.example.domain.model.QuestStatus
import com.example.domain.model.quest.*
import com.example.domain.service.quest.QuestCatalog
import com.example.domain.service.quest.QuestCompletionEvaluator
import com.example.ui.components.quest.DeliverResourceDialog
import com.example.ui.components.quest.QuestCard
import com.example.ui.theme.*

enum class QuestFilterTab(val titleRu: String) {
    ACTIVE("Активные"),
    AVAILABLE("Доступные"),
    ALL("Все"),
    COMPLETED("Завершённые"),
    FACTIONS("Фракционные")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestsScreen(
    gameState: GameState,
    onBack: () -> Unit,
    onAcceptQuest: (questId: String) -> Unit,
    onDeclineQuest: (questId: String) -> Unit,
    onTurnInQuest: (questId: String) -> Unit,
    onTrackQuest: (questId: String?) -> Unit,
    onDeliverResource: (questId: String, objectiveId: String, amount: Int) -> Unit,
    onDeliverItem: (questId: String, objectiveId: String, itemId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(QuestFilterTab.ACTIVE) }
    var selectedCategoryFilter by remember { mutableStateOf<QuestCategory?>(null) }

    var deliveringDialogData by remember {
        mutableStateOf<Pair<QuestDefinition, Pair<QuestObjectiveDefinition, QuestObjectiveProgress?>>?>(null)
    }

    // Deliver Resource Dialog
    deliveringDialogData?.let { (questDef, objData) ->
        val (objDef, objProg) = objData
        DeliverResourceDialog(
            questDef = questDef,
            objDef = objDef,
            objProg = objProg,
            gameState = gameState,
            onDeliver = { amount ->
                onDeliverResource(questDef.id, objDef.id, amount)
            },
            onDismiss = { deliveringDialogData = null }
        )
    }

    // Compute stats
    val allCatalogQuests = QuestCatalog.ALL_QUESTS
    val activeCount = gameState.questStates.values.count { it.isActive }
    val readyToClaimCount = gameState.questStates.values.count { it.status == QuestStatus.READY_TO_CLAIM }
    val completedCount = gameState.questStates.values.count { it.status == QuestStatus.COMPLETED }
    val availableCount = allCatalogQuests.count { def ->
        val st = gameState.questStates[def.id]
        st?.status == QuestStatus.AVAILABLE || (st == null && def.requirements.isEmpty())
    }

    // Filter quests
    val filteredQuests = remember(selectedTab, selectedCategoryFilter, gameState.questStates, gameState.trackedQuestId) {
        allCatalogQuests.filter { def ->
            val qState = gameState.questStates[def.id]
            val status = qState?.status ?: QuestStatus.LOCKED

            val matchesTab = when (selectedTab) {
                QuestFilterTab.ALL -> true
                QuestFilterTab.ACTIVE -> status == QuestStatus.ACTIVE || status == QuestStatus.IN_PROGRESS || status == QuestStatus.READY_TO_CLAIM
                QuestFilterTab.AVAILABLE -> status == QuestStatus.AVAILABLE
                QuestFilterTab.COMPLETED -> status == QuestStatus.COMPLETED
                QuestFilterTab.FACTIONS -> def.category == QuestCategory.FACTION || def.factionId != null
            }

            val matchesCategory = selectedCategoryFilter == null || def.category == selectedCategoryFilter

            matchesTab && matchesCategory
        }.sortedWith(
            compareByDescending<QuestDefinition> { def ->
                val st = gameState.questStates[def.id]?.status
                st == QuestStatus.READY_TO_CLAIM
            }.thenByDescending { def ->
                gameState.trackedQuestId == def.id
            }.thenByDescending { def ->
                val st = gameState.questStates[def.id]?.status
                st == QuestStatus.ACTIVE || st == QuestStatus.IN_PROGRESS
            }.thenByDescending { it.priority }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Журнал заданий",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Активных: $activeCount | К сдаче: $readyToClaimCount | Завершено: $completedCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("quests_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FrontierDarkSurface
                )
            )
        },
        containerColor = FrontierDarkBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stats Summary Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestStatCard(
                    title = "Активно",
                    value = "$activeCount",
                    color = TechCyan,
                    modifier = Modifier.weight(1f)
                )
                QuestStatCard(
                    title = "К сдаче",
                    value = "$readyToClaimCount",
                    color = if (readyToClaimCount > 0) SafeEmerald else TextMuted,
                    isGlowing = readyToClaimCount > 0,
                    modifier = Modifier.weight(1f)
                )
                QuestStatCard(
                    title = "Доступно",
                    value = "$availableCount",
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
                QuestStatCard(
                    title = "Завершено",
                    value = "$completedCount",
                    color = TextMuted,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = FrontierDarkSurface,
                contentColor = FrontierPrimary,
                divider = { HorizontalDivider(color = FrontierBorder) }
            ) {
                QuestFilterTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = tab.titleRu,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                if (tab == QuestFilterTab.ACTIVE && readyToClaimCount > 0) {
                                    Surface(
                                        color = SafeEmerald,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "$readyToClaimCount",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("Все категории") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FrontierPrimaryContainer,
                            selectedLabelColor = FrontierOnPrimaryContainer
                        )
                    )
                }

                items(QuestCategory.values()) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        label = { Text(cat.titleRu) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(cat.badgeColorHex).copy(alpha = 0.3f),
                            selectedLabelColor = Color(cat.badgeColorHex)
                        )
                    )
                }
            }

            // Quests List
            if (filteredQuests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentLate,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Нет заданий в этой категории",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Исследуйте новые территории, развивайте поселение и улучшайте репутацию, чтобы открыть новые контракты и миссии.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredQuests, key = { it.id }) { def ->
                        val qState = gameState.questStates[def.id]
                        val isTracked = gameState.trackedQuestId == def.id

                        QuestCard(
                            definition = def,
                            questState = qState,
                            gameState = gameState,
                            isTracked = isTracked,
                            onAccept = { onAcceptQuest(def.id) },
                            onDecline = { onDeclineQuest(def.id) },
                            onTurnIn = { onTurnInQuest(def.id) },
                            onTrackToggle = {
                                onTrackQuest(if (isTracked) null else def.id)
                            },
                            onOpenDeliverResource = { objDef, objProg ->
                                deliveringDialogData = Pair(def, Pair(objDef, objProg))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestStatCard(
    title: String,
    value: String,
    color: Color,
    isGlowing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (isGlowing) color else FrontierBorder,
                RoundedCornerShape(8.dp)
            ),
        color = if (isGlowing) color.copy(alpha = 0.15f) else FrontierDarkSurface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}
