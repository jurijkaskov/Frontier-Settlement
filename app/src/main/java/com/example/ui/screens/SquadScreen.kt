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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.*
import com.example.ui.components.CharacterDetailDialog
import com.example.ui.components.GameCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatProgressBar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadScreen(
    gameState: GameState,
    onToggleSquadMember: (String) -> Unit,
    onAddSquadMember: (String) -> Unit = onToggleSquadMember,
    onRemoveSquadMember: (String) -> Unit = onToggleSquadMember,
    onSetSquadLeader: (String) -> Unit = {},
    onClearSquad: () -> Unit = {},
    onNavigateToResidents: () -> Unit = {},
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onHealResident: (String) -> Unit = {},
    onAllocateSkillPoint: (String, CharacterStatType) -> Unit = { _, _ -> },
    onAwardExperience: (String, Int) -> Unit = { _, _ -> },
    onEquipItem: (String, EquipmentSlotType, String) -> Unit = { _, _, _ -> },
    onUnequipItem: (String, EquipmentSlotType) -> Unit = { _, _ -> },
    lastSquadOperation: SquadOperationResult? = null,
    onDismissOperationResult: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedRoleFilter by remember { mutableStateOf<CharacterRole?>(null) }
    var selectedCharacterForDetail by remember { mutableStateOf<Character?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val squad = gameState.squad
    val squadMembers = gameState.squadMembers
    val squadLeader = gameState.squadLeader
    val squadSummary = gameState.squadSummary
    val squadReadiness = gameState.squadReadiness
    val vehicle = gameState.selectedVehicle

    val availableResidents = remember(gameState.characters, squad.memberIds, selectedRoleFilter) {
        gameState.characters
            .filterNot { squad.memberIds.contains(it.id) }
            .filter { selectedRoleFilter == null || it.role == selectedRoleFilter }
    }

    val medicineAvailable = gameState.resources.extraResources[ResourceType.MEDICINE] ?: 0

    // Detail Dialog
    if (selectedCharacterForDetail != null) {
        val detailChar = gameState.characters.find { it.id == selectedCharacterForDetail!!.id } ?: selectedCharacterForDetail!!
        val isCharInSquad = squad.memberIds.contains(detailChar.id)
        val isCharLeader = squad.leaderId == detailChar.id

        CharacterDetailDialog(
            character = detailChar,
            isInSquad = isCharInSquad,
            medicineAvailable = medicineAvailable,
            onToggleSquad = { charId ->
                onToggleSquadMember(charId)
                selectedCharacterForDetail = gameState.characters.find { it.id == charId }
            },
            onHealInClinic = { charId ->
                onHealResident(charId)
                selectedCharacterForDetail = gameState.characters.find { it.id == charId }
            },
            onRetireResident = { /* Retired outside squad screen */ },
            onAllocateSkillPoint = { charId, stat ->
                onAllocateSkillPoint(charId, stat)
                selectedCharacterForDetail = gameState.characters.find { it.id == charId }
            },
            onAwardExperience = { charId, xp ->
                onAwardExperience(charId, xp)
                selectedCharacterForDetail = gameState.characters.find { it.id == charId }
            },
            onDismiss = { selectedCharacterForDetail = null },
            isLeader = isCharLeader,
            onSetLeader = { leaderId ->
                onSetSquadLeader(leaderId)
            },
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

    // Reset Squad Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Распустить отряд?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
            },
            text = {
                Text(
                    text = "Все бойцы отряда вернутся в гарнизон аванпоста. Назначенный командир будет сброшен.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearSquad()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Распустить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Отмена")
                }
            },
            containerColor = FrontierDarkSurfaceElevated
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
    ) {
        // Operation Feedback Banner (if active)
        if (lastSquadOperation != null) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        color = if (lastSquadOperation.isSuccess) SafeEmerald.copy(alpha = 0.18f) else CriticalRed.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (lastSquadOperation.isSuccess) SafeEmerald else CriticalRed
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (lastSquadOperation.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (lastSquadOperation.isSuccess) SafeEmerald else CriticalRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = when (lastSquadOperation) {
                                        is SquadOperationResult.Success -> lastSquadOperation.message
                                        is SquadOperationResult.Failure -> lastSquadOperation.message
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            IconButton(
                                onClick = onDismissOperationResult,
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
        }

        // 1. Squad Main Header & Status Card
        item {
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = FrontierBorder
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Title and Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = squad.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Тактическое управление вылазками пустоши",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Surface(
                            color = squadReadiness.statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, squadReadiness.statusColor)
                        ) {
                            Text(
                                text = squadReadiness.statusTitle,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = squadReadiness.statusColor
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Readiness Score Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Готовность к маршу:",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                            Text(
                                text = "${squadReadiness.readinessPercent}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = squadReadiness.statusColor
                                )
                            )
                        }
                        LinearProgressIndicator(
                            progress = { squadReadiness.readinessPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = squadReadiness.statusColor,
                            trackColor = FrontierDarkSurfaceHighlight
                        )
                    }

                    // Quick Navigation Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Base Residents shortcut
                        Surface(
                            color = FrontierDarkBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToResidents() }
                                .testTag("btn_squad_to_residents")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = TechCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Жители (${gameState.currentPopulation})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextWhite
                                )
                            }
                        }

                        // Transport shortcut
                        Surface(
                            color = FrontierDarkBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToVehicles() }
                                .testTag("btn_squad_to_vehicles")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Гараж",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextWhite
                                )
                            }
                        }

                        // Reset Squad button
                        if (squadMembers.isNotEmpty()) {
                            Surface(
                                color = CriticalRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CriticalRed.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clickable { showClearConfirmDialog = true }
                                    .testTag("btn_clear_squad")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = CriticalRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Сброс",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CriticalRed
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Readiness Checklist & Transport Status
        item {
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = if (squadReadiness.isFullyReady) SafeEmerald.copy(alpha = 0.6f) else FrontierBorder
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Чек-лист готовности к вылазке",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Surface(
                            color = FrontierDarkBackground,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                        ) {
                            Text(
                                text = "${squadMembers.size} / ${squadSummary.maxCapacity} мест",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (squadSummary.isOverCapacity) CriticalRed else SafeEmerald
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = FrontierBorder.copy(alpha = 0.5f), thickness = 1.dp)

                    // Checklist items
                    squadReadiness.checks.forEach { check ->
                        ReadinessCheckRow(check = check)
                    }

                    // Blockers / Warnings alert callout
                    if (squadReadiness.blockers.isNotEmpty()) {
                        Surface(
                            color = WarningAmber.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    squadReadiness.blockers.forEach { blocker ->
                                        Text(
                                            text = "• $blocker",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextWhite)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Tactical Aggregated Power Summary
        item {
            GameCard(
                backgroundColor = FrontierDarkSurfaceElevated,
                borderColor = TechCyan.copy(alpha = 0.35f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Тактическая мощь отряда",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            Text(
                                text = "Суммарные боевые и прикладные навыки группы",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Surface(
                            color = FrontierPrimaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = TechCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Сила: ${squadSummary.totalCombatPower}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TechCyan
                                    )
                                )
                            }
                        }
                    }

                    // Tactical stats grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FrontierDarkBackground, RoundedCornerShape(10.dp))
                            .border(1.dp, FrontierBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatTile(label = "Атака", value = squadSummary.totalAttack.toString(), color = DangerCrimson, icon = Icons.Default.GpsFixed)
                        StatTile(label = "Защита", value = squadSummary.totalDefense.toString(), color = TechCyan, icon = Icons.Default.Shield)
                        StatTile(label = "Добыча", value = squadSummary.totalScavenging.toString(), color = MaterialsOrange, icon = Icons.Default.Backpack)
                        StatTile(label = "Инженерия", value = squadSummary.totalEngineering.toString(), color = WarningAmber, icon = Icons.Default.Build)
                        StatTile(label = "Медицина", value = squadSummary.totalMedical.toString(), color = FoodGreen, icon = Icons.Default.MedicalServices)
                    }

                    // Roles composition chips
                    if (squadSummary.roleCounts.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Роли в группе:",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                            squadSummary.roleCounts.forEach { (role, count) ->
                                Surface(
                                    color = FrontierDarkSurfaceHighlight,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                                ) {
                                    Text(
                                        text = "$count× ${role.titleRu}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = when (role) {
                                                CharacterRole.SCOUT -> TechCyan
                                                CharacterRole.SOLDIER -> DangerCrimson
                                                CharacterRole.MEDIC -> FoodGreen
                                                CharacterRole.ENGINEER -> WarningAmber
                                                CharacterRole.SCAVENGER -> MaterialsOrange
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Section: Current Squad Members
        item {
            SectionHeader(
                title = "Бойцы в отряде (${squadMembers.size}/${squadSummary.maxCapacity})",
                accentColor = if (squadMembers.isEmpty()) WarningAmber else SafeEmerald
            )
        }

        if (squadMembers.isEmpty()) {
            // Empty State
            item {
                Surface(
                    color = FrontierDarkSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("squad_empty_state")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(FrontierDarkSurfaceHighlight)
                                .border(1.dp, FrontierBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = null,
                                tint = TechCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Отряд пока не сформирован",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )

                        Text(
                            text = "Выберите жителей из резерва базы ниже, чтобы укомплектовать отряд для исследований карты, поиска лута и выполнения заданий.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        } else {
            items(squadMembers, key = { it.id }) { character ->
                val isLeader = character.id == squad.leaderId

                SquadMemberCard(
                    character = character,
                    isLeader = isLeader,
                    onSetLeader = { onSetSquadLeader(character.id) },
                    onRemoveFromSquad = { onRemoveSquadMember(character.id) },
                    onCardClick = { selectedCharacterForDetail = character }
                )
            }
        }

        // 5. Section: Available Reserve Residents
        item {
            Spacer(modifier = Modifier.height(6.dp))
            SectionHeader(
                title = "Резерв поселения (${availableResidents.size})",
                accentColor = TechCyan
            )
        }

        // Role Filter Tabs Row
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedRoleFilter == null,
                        onClick = { selectedRoleFilter = null },
                        label = { Text("Все", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TechCyan.copy(alpha = 0.2f),
                            selectedLabelColor = TechCyan
                        )
                    )
                }
                CharacterRole.entries.forEach { role ->
                    item {
                        FilterChip(
                            selected = selectedRoleFilter == role,
                            onClick = { selectedRoleFilter = if (selectedRoleFilter == role) null else role },
                            label = { Text(role.titleRu, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TechCyan.copy(alpha = 0.2f),
                                selectedLabelColor = TechCyan
                            )
                        )
                    }
                }
            }
        }

        if (availableResidents.isEmpty()) {
            item {
                Surface(
                    color = FrontierDarkSurfaceElevated.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedRoleFilter != null) "Нет доступных жителей с выбранной специализацией." else "Все жители поселения уже зачислены в отряд.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(availableResidents, key = { it.id }) { resident ->
                val canJoin = resident.status != CharacterStatus.ON_EXPEDITION &&
                        resident.status != CharacterStatus.INJURED &&
                        squadMembers.size < squadSummary.maxCapacity

                ReserveResidentCard(
                    character = resident,
                    canJoin = canJoin,
                    isSquadFull = squadMembers.size >= squadSummary.maxCapacity,
                    onAddToSquad = { onAddSquadMember(resident.id) },
                    onCardClick = { selectedCharacterForDetail = resident }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Component Cards & Rows
// -------------------------------------------------------------

@Composable
private fun ReadinessCheckRow(check: SquadReadinessCheck) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (check.isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (check.isPassed) SafeEmerald else CriticalRed,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = check.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextWhite
                )
            )
        }

        Text(
            text = check.detail,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (check.isPassed) SafeEmerald else CriticalRed,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontSize = 9.sp
            )
        )
    }
}

@Composable
private fun SquadMemberCard(
    character: Character,
    isLeader: Boolean,
    onSetLeader: () -> Unit,
    onRemoveFromSquad: () -> Unit,
    onCardClick: () -> Unit
) {
    val roleColor = when (character.role) {
        CharacterRole.SCOUT -> TechCyan
        CharacterRole.SOLDIER -> DangerCrimson
        CharacterRole.MEDIC -> FoodGreen
        CharacterRole.ENGINEER -> WarningAmber
        CharacterRole.SCAVENGER -> MaterialsOrange
    }

    GameCard(
        backgroundColor = if (isLeader) Color(0xFF142436) else FrontierDarkSurfaceElevated,
        borderColor = if (isLeader) WarningAmber else FrontierBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCardClick() },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Leader Badge (if designated leader)
            if (isLeader) {
                Surface(
                    color = WarningAmber.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "КОМАНДИР ОТРЯДА",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Character Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(roleColor.copy(alpha = 0.15f))
                            .border(1.dp, roleColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (character.role) {
                                CharacterRole.SCOUT -> Icons.Default.Explore
                                CharacterRole.SOLDIER -> Icons.Default.Shield
                                CharacterRole.MEDIC -> Icons.Default.MedicalServices
                                CharacterRole.ENGINEER -> Icons.Default.Build
                                CharacterRole.SCAVENGER -> Icons.Default.Backpack
                            },
                            contentDescription = character.role.titleRu,
                            tint = roleColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = character.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = FrontierDarkSurfaceHighlight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Ур. ${character.level}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TechCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Text(
                            text = "${character.role.titleRu} • ${character.specialization}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = roleColor,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Action Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Set Leader button
                    if (!isLeader) {
                        IconButton(
                            onClick = onSetLeader,
                            modifier = Modifier
                                .size(34.dp)
                                .background(FrontierDarkSurfaceHighlight, CircleShape)
                                .border(1.dp, FrontierBorder, CircleShape)
                                .testTag("btn_set_leader_${character.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.StarBorder,
                                contentDescription = "Сделать командиром",
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Remove from Squad button
                    IconButton(
                        onClick = onRemoveFromSquad,
                        modifier = Modifier
                            .size(34.dp)
                            .background(CriticalRed.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, CriticalRed.copy(alpha = 0.6f), CircleShape)
                            .testTag("btn_remove_squad_${character.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = "Убрать из отряда",
                            tint = CriticalRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Health & XP bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatProgressBar(
                    label = "Здоровье",
                    current = character.health,
                    max = character.maxHealth,
                    barColor = if (character.health < 30) DangerCrimson else FoodGreen,
                    modifier = Modifier.weight(1f)
                )
                StatProgressBar(
                    label = "Опыт",
                    current = character.experience,
                    max = character.maxExperience,
                    barColor = TechCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Stats Pill Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FrontierDarkBackground, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItemCompact(label = "АТК", value = character.stats.attack.toString(), color = DangerCrimson)
                StatItemCompact(label = "ЗАЩ", value = character.stats.defense.toString(), color = TechCyan)
                StatItemCompact(label = "СБОР", value = character.stats.scavengingSkill.toString(), color = MaterialsOrange)
                StatItemCompact(label = "ИНЖ", value = character.stats.engineeringSkill.toString(), color = WarningAmber)
                StatItemCompact(label = "МЕД", value = character.stats.medicalSkill.toString(), color = FoodGreen)
            }
        }
    }
}

@Composable
private fun ReserveResidentCard(
    character: Character,
    canJoin: Boolean,
    isSquadFull: Boolean,
    onAddToSquad: () -> Unit,
    onCardClick: () -> Unit
) {
    val roleColor = when (character.role) {
        CharacterRole.SCOUT -> TechCyan
        CharacterRole.SOLDIER -> DangerCrimson
        CharacterRole.MEDIC -> FoodGreen
        CharacterRole.ENGINEER -> WarningAmber
        CharacterRole.SCAVENGER -> MaterialsOrange
    }

    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = FrontierBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCardClick() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.15f))
                        .border(1.dp, roleColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (character.role) {
                            CharacterRole.SCOUT -> Icons.Default.Explore
                            CharacterRole.SOLDIER -> Icons.Default.Shield
                            CharacterRole.MEDIC -> Icons.Default.MedicalServices
                            CharacterRole.ENGINEER -> Icons.Default.Build
                            CharacterRole.SCAVENGER -> Icons.Default.Backpack
                        },
                        contentDescription = null,
                        tint = roleColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ур.${character.level}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Text(
                        text = "${character.role.titleRu} • HP: ${character.health}/${character.maxHealth}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (character.health < 30) DangerCrimson else TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Add to Squad Button
            Button(
                onClick = onAddToSquad,
                enabled = canJoin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafeEmerald,
                    disabledContainerColor = FrontierDarkSurfaceHighlight
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("btn_add_to_squad_${character.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSquadFull) "Мест нет" else "В отряд",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun StatItemCompact(
    label: String,
    value: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label ",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontSize = 10.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 11.sp
            )
        )
    }
}
