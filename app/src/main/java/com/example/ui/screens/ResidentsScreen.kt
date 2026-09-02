package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Character
import com.example.domain.model.CharacterRole
import com.example.domain.model.CharacterStatType
import com.example.domain.model.CharacterStatus
import com.example.domain.model.EquipmentSlotType
import com.example.domain.model.GameState
import com.example.domain.model.ResourceOperationResult
import com.example.domain.model.ResourceType
import com.example.ui.components.CharacterDetailDialog
import com.example.ui.components.RecruitSurvivorDialog
import com.example.ui.theme.*

private enum class ResidentSortOption(val titleRu: String) {
    LEVEL("По уровню"),
    HEALTH("По здоровью"),
    ATTACK("По атаке"),
    TOTAL_SKILLS("По навыкам")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentsScreen(
    gameState: GameState,
    onToggleSquadMember: (String) -> Unit,
    onRecruitSurvivor: (CharacterRole?) -> Unit,
    onRetireResident: (String) -> Unit,
    onHealResident: (String) -> Unit,
    onDebugAddSurvivor: (CharacterRole?) -> Unit,
    onAllocateSkillPoint: (String, CharacterStatType) -> Unit = { _, _ -> },
    onAwardExperience: (String, Int) -> Unit = { _, _ -> },
    onEquipItem: (String, EquipmentSlotType, String) -> Unit = { _, _, _ -> },
    onUnequipItem: (String, EquipmentSlotType) -> Unit = { _, _ -> },
    lastOperation: ResourceOperationResult? = null,
    onDismissOperation: () -> Unit = {},
    onBack: () -> Unit
) {
    var selectedRoleFilter by remember { mutableStateOf<CharacterRole?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<CharacterStatus?>(null) }
    var currentSortOption by remember { mutableStateOf(ResidentSortOption.LEVEL) }
    var showRecruitDialog by remember { mutableStateOf(false) }
    var selectedCharacterForDetail by remember { mutableStateOf<Character?>(null) }

    val medicineCount = gameState.resources.extraResources[ResourceType.MEDICINE] ?: 0

    // Filter & Sort characters
    val filteredCharacters = remember(gameState.characters, selectedRoleFilter, selectedStatusFilter, currentSortOption) {
        var list = gameState.characters

        if (selectedRoleFilter != null) {
            list = list.filter { it.role == selectedRoleFilter }
        }

        if (selectedStatusFilter != null) {
            list = list.filter { it.status == selectedStatusFilter }
        }

        when (currentSortOption) {
            ResidentSortOption.LEVEL -> list.sortedWith(compareByDescending<Character> { it.level }.thenByDescending { it.experience })
            ResidentSortOption.HEALTH -> list.sortedByDescending { it.healthFraction }
            ResidentSortOption.ATTACK -> list.sortedByDescending { it.effectiveStats.attack }
            ResidentSortOption.TOTAL_SKILLS -> list.sortedByDescending { it.effectiveStats.totalSkillPower }
        }
    }

    // Role breakdown counts
    val scoutCount = gameState.characters.count { it.role == CharacterRole.SCOUT }
    val soldierCount = gameState.characters.count { it.role == CharacterRole.SOLDIER }
    val engineerCount = gameState.characters.count { it.role == CharacterRole.ENGINEER }
    val medicCount = gameState.characters.count { it.role == CharacterRole.MEDIC }
    val scavengerCount = gameState.characters.count { it.role == CharacterRole.SCAVENGER }

    if (showRecruitDialog) {
        RecruitSurvivorDialog(
            gameState = gameState,
            onRecruit = { role -> onRecruitSurvivor(role) },
            onDismiss = { showRecruitDialog = false }
        )
    }

    val detailChar = selectedCharacterForDetail?.let { selected ->
        gameState.characters.find { it.id == selected.id }
    }

    if (detailChar != null) {
        CharacterDetailDialog(
            character = detailChar,
            isInSquad = gameState.selectedSquadIds.contains(detailChar.id),
            medicineAvailable = medicineCount,
            onToggleSquad = { onToggleSquadMember(it) },
            onHealInClinic = { onHealResident(it) },
            onRetireResident = { onRetireResident(it) },
            onAllocateSkillPoint = onAllocateSkillPoint,
            onAwardExperience = onAwardExperience,
            onDismiss = { selectedCharacterForDetail = null },
            warehouseItems = gameState.inventoryItems,
            allCharacters = gameState.characters,
            onEquipItem = { charId, slot, itemId ->
                onEquipItem(charId, slot, itemId)
                selectedCharacterForDetail = gameState.characters.find { it.id == charId }
            },
            onUnequipItem = { charId, slot ->
                onUnequipItem(charId, slot)
                selectedCharacterForDetail = gameState.characters.find { it.id == charId }
            }
        )
    }

    Scaffold(
        containerColor = FrontierDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Жители поселения",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            ),
                            color = TextWhite
                        )
                        Text(
                            text = "${gameState.currentPopulation} из ${gameState.maxPopulation} жителей аванпоста",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_residents")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onDebugAddSurvivor(null) },
                        modifier = Modifier.testTag("btn_debug_add_survivor")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = "Тест: добавить жителя",
                            tint = WarningAmber
                        )
                    }

                    Button(
                        onClick = { showRecruitDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gameState.isPopulationAtMax) FrontierCardElevated else SafeEmerald
                        ),
                        enabled = !gameState.isPopulationAtMax,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("btn_open_recruit_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (gameState.isPopulationAtMax) TextMuted else TextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Принять",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (gameState.isPopulationAtMax) TextMuted else TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FrontierDarkSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("residents_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Settlement Population Overview Card
            item {
                SettlementPopulationHeaderCard(
                    currentPop = gameState.currentPopulation,
                    maxPop = gameState.maxPopulation,
                    freeSlots = gameState.freeHousingSlots,
                    fraction = gameState.populationOccupancyFraction,
                    inSquadCount = gameState.selectedSquadIds.size,
                    scoutCount = scoutCount,
                    soldierCount = soldierCount,
                    engineerCount = engineerCount,
                    medicCount = medicCount,
                    scavengerCount = scavengerCount,
                    onOpenRecruit = { showRecruitDialog = true }
                )
            }

            // 2. Feedback notification banner if any
            if (lastOperation != null) {
                item {
                    val isSuccess = lastOperation is ResourceOperationResult.Success
                    Surface(
                        color = if (isSuccess) SafeEmerald.copy(alpha = 0.15f) else CriticalRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSuccess) SafeEmerald.copy(alpha = 0.4f) else CriticalRed.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isSuccess) SafeEmerald else CriticalRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isSuccess) (lastOperation as ResourceOperationResult.Success).message
                                    else (lastOperation as ResourceOperationResult.Failure).message,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = TextWhite
                                )
                            }
                            IconButton(
                                onClick = onDismissOperation,
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
            }

            // 3. Filter & Sort Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Role filters
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedRoleFilter == null,
                                onClick = { selectedRoleFilter = null },
                                label = { Text("Все роли (${gameState.characters.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SafeEmerald,
                                    selectedLabelColor = TextWhite,
                                    containerColor = FrontierDarkSurface,
                                    labelColor = TextMuted
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedRoleFilter == CharacterRole.SCOUT,
                                onClick = { selectedRoleFilter = if (selectedRoleFilter == CharacterRole.SCOUT) null else CharacterRole.SCOUT },
                                label = { Text("🔍 Разведчики ($scoutCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentCyan,
                                    selectedLabelColor = TextWhite,
                                    containerColor = FrontierDarkSurface,
                                    labelColor = TextMuted
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedRoleFilter == CharacterRole.SOLDIER,
                                onClick = { selectedRoleFilter = if (selectedRoleFilter == CharacterRole.SOLDIER) null else CharacterRole.SOLDIER },
                                label = { Text("⚔️ Штурмовики ($soldierCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarningAmber,
                                    selectedLabelColor = TextWhite,
                                    containerColor = FrontierDarkSurface,
                                    labelColor = TextMuted
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedRoleFilter == CharacterRole.ENGINEER,
                                onClick = { selectedRoleFilter = if (selectedRoleFilter == CharacterRole.ENGINEER) null else CharacterRole.ENGINEER },
                                label = { Text("🔧 Инженеры ($engineerCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarningOrange,
                                    selectedLabelColor = TextWhite,
                                    containerColor = FrontierDarkSurface,
                                    labelColor = TextMuted
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedRoleFilter == CharacterRole.MEDIC,
                                onClick = { selectedRoleFilter = if (selectedRoleFilter == CharacterRole.MEDIC) null else CharacterRole.MEDIC },
                                label = { Text("💉 Медики ($medicCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SafeEmerald,
                                    selectedLabelColor = TextWhite,
                                    containerColor = FrontierDarkSurface,
                                    labelColor = TextMuted
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedRoleFilter == CharacterRole.SCAVENGER,
                                onClick = { selectedRoleFilter = if (selectedRoleFilter == CharacterRole.SCAVENGER) null else CharacterRole.SCAVENGER },
                                label = { Text("🎒 Следопыты ($scavengerCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFB388FF),
                                    selectedLabelColor = TextWhite,
                                    containerColor = FrontierDarkSurface,
                                    labelColor = TextMuted
                                )
                            )
                        }
                    }

                    // Sort row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Найдено: ${filteredCharacters.size} жителей",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Сортировка:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            ResidentSortOption.entries.forEach { option ->
                                val isSelected = currentSortOption == option
                                Surface(
                                    color = if (isSelected) SafeEmerald.copy(alpha = 0.2f) else FrontierDarkSurface,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        0.5.dp,
                                        if (isSelected) SafeEmerald else FrontierBorder
                                    ),
                                    modifier = Modifier.clickable { currentSortOption = option }
                                ) {
                                    Text(
                                        text = option.titleRu,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) SafeEmerald else TextMuted,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Resident Cards List
            if (filteredCharacters.isEmpty()) {
                item {
                    Surface(
                        color = FrontierDarkSurface,
                        shape = RoundedCornerShape(16.dp),
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
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Жители с выбранными фильтрами не найдены",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(filteredCharacters, key = { it.id }) { resident ->
                    ResidentCard(
                        character = resident,
                        isInSquad = gameState.selectedSquadIds.contains(resident.id),
                        medicineCount = medicineCount,
                        onToggleSquad = { onToggleSquadMember(resident.id) },
                        onHeal = { onHealResident(resident.id) },
                        onClick = { selectedCharacterForDetail = resident }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettlementPopulationHeaderCard(
    currentPop: Int,
    maxPop: Int,
    freeSlots: Int,
    fraction: Float,
    inSquadCount: Int,
    scoutCount: Int,
    soldierCount: Int,
    engineerCount: Int,
    medicCount: Int,
    scavengerCount: Int,
    onOpenRecruit: () -> Unit
) {
    val barColor = when {
        fraction >= 1.0f -> CriticalRed
        fraction >= 0.8f -> WarningAmber
        else -> SafeEmerald
    }

    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Численность аванпоста",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextMuted
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$currentPop",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Text(
                            text = "/ $maxPop чел.",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                Surface(
                    color = if (freeSlots > 0) SafeEmerald.copy(alpha = 0.15f) else CriticalRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (freeSlots > 0) SafeEmerald.copy(alpha = 0.4f) else CriticalRed.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (freeSlots > 0) SafeEmerald else CriticalRed)
                        )
                        Text(
                            text = if (freeSlots > 0) "Свободно: $freeSlots мест" else "Жильё заполнено",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (freeSlots > 0) SafeEmerald else CriticalRed
                        )
                    }
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = barColor,
                    trackColor = FrontierCardElevated
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Заполненность фонда: ${(fraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "Расход: -$currentPop Еды / -$currentPop Воды в день",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = WarningOrange
                    )
                }
            }

            HorizontalDivider(color = FrontierBorder, thickness = 0.5.dp)

            // Roles distribution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoleStatPill("🔍", scoutCount, AccentCyan)
                RoleStatPill("⚔️", soldierCount, WarningAmber)
                RoleStatPill("🔧", engineerCount, WarningOrange)
                RoleStatPill("💉", medicCount, SafeEmerald)
                RoleStatPill("🎒", scavengerCount, Color(0xFFB388FF))
            }
        }
    }
}

@Composable
private fun RoleStatPill(
    emoji: String,
    count: Int,
    tintColor: Color
) {
    Surface(
        color = tintColor.copy(alpha = 0.10f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, tintColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, fontSize = 12.sp)
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = tintColor
            )
        }
    }
}

@Composable
private fun ResidentCard(
    character: Character,
    isInSquad: Boolean,
    medicineCount: Int,
    onToggleSquad: () -> Unit,
    onHeal: () -> Unit,
    onClick: () -> Unit
) {
    val roleColor = when (character.role) {
        CharacterRole.SCOUT -> AccentCyan
        CharacterRole.SOLDIER -> WarningAmber
        CharacterRole.ENGINEER -> WarningOrange
        CharacterRole.MEDIC -> SafeEmerald
        CharacterRole.SCAVENGER -> Color(0xFFB388FF)
    }

    val roleIcon = when (character.role) {
        CharacterRole.SCOUT -> Icons.Default.Explore
        CharacterRole.SOLDIER -> Icons.Default.Shield
        CharacterRole.ENGINEER -> Icons.Default.Build
        CharacterRole.MEDIC -> Icons.Default.MedicalServices
        CharacterRole.SCAVENGER -> Icons.Default.Backpack
    }

    val statusColor = when (character.status) {
        CharacterStatus.READY -> SafeEmerald
        CharacterStatus.IN_SQUAD -> AccentCyan
        CharacterStatus.ON_EXPEDITION -> WarningAmber
        CharacterStatus.INJURED -> CriticalRed
        CharacterStatus.RESTING -> TextMuted
    }

    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isInSquad) AccentCyan.copy(alpha = 0.5f) else FrontierBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("resident_card_${character.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(roleColor.copy(alpha = 0.15f))
                            .border(1.dp, roleColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = roleIcon,
                            contentDescription = null,
                            tint = roleColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = character.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                            if (character.unspentSkillPoints > 0) {
                                Surface(
                                    color = SafeEmerald,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "+${character.unspentSkillPoints}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = FrontierDarkBackground,
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
                                text = character.role.titleRu,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = roleColor
                            )
                            Text(
                                text = "• Ур. ${character.level}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Status Pill
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = character.status.titleRu,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Traits row if present
            if (character.traits.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    character.traits.take(3).forEach { trait ->
                        Surface(
                            color = FrontierDarkBackground,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, FrontierBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = trait.category.icon, fontSize = 11.sp)
                                Text(
                                    text = trait.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextWhite.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // Health & XP Mini Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Health
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
                        Text(
                            "${character.health}/${character.effectiveMaxHealth}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = if (character.healthFraction < 0.35f) CriticalRed else SafeEmerald
                        )
                    }
                    LinearProgressIndicator(
                        progress = { character.healthFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (character.healthFraction < 0.35f) CriticalRed else SafeEmerald,
                        trackColor = FrontierCardElevated
                    )
                }

                // XP
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("XP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
                        Text(
                            "${character.experience}/${character.maxExperience}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                    }
                    LinearProgressIndicator(
                        progress = { character.xpFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AccentCyan,
                        trackColor = FrontierCardElevated
                    )
                }
            }

            // Stats Chip Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniStatBadge("⚔️", character.effectiveStats.attack, "Атака")
                MiniStatBadge("🛡️", character.effectiveStats.defense, "Защита")
                MiniStatBadge("🎒", character.effectiveStats.scavengingSkill, "Добыча")
                MiniStatBadge("🔧", character.effectiveStats.engineeringSkill, "Техника")
                MiniStatBadge("💉", character.effectiveStats.medicalSkill, "Медицина")
            }

            HorizontalDivider(color = FrontierBorder, thickness = 0.5.dp)

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Досье бойца ›",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AccentCyan
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (character.health < character.effectiveMaxHealth) {
                        OutlinedButton(
                            onClick = onHeal,
                            enabled = medicineCount >= 1,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SafeEmerald
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_heal_${character.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Healing,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Лечить", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    if (character.status != CharacterStatus.ON_EXPEDITION) {
                        Button(
                            onClick = onToggleSquad,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInSquad) CriticalRed.copy(alpha = 0.85f) else SafeEmerald
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_toggle_squad_${character.id}")
                        ) {
                            Text(
                                text = if (isInSquad) "В отряде" else "+ В отряд",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatBadge(
    icon: String,
    value: Int,
    label: String
) {
    Surface(
        color = FrontierDarkBackground,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, FrontierBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = icon, fontSize = 10.sp)
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextWhite
            )
        }
    }
}
