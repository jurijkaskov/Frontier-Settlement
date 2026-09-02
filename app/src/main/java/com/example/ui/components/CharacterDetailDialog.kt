package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.Character
import com.example.domain.model.CharacterRole
import com.example.domain.model.CharacterStatType
import com.example.domain.model.CharacterStatus
import com.example.domain.model.CharacterTrait
import com.example.domain.model.EquipmentSlotType
import com.example.domain.model.TraitCategory
import com.example.domain.model.WarehouseItem
import com.example.ui.theme.*

@Composable
fun CharacterDetailDialog(
    character: Character,
    isInSquad: Boolean,
    medicineAvailable: Int,
    onToggleSquad: (String) -> Unit,
    onHealInClinic: (String) -> Unit,
    onRetireResident: (String) -> Unit,
    onAllocateSkillPoint: (String, CharacterStatType) -> Unit = { _, _ -> },
    onAwardExperience: (String, Int) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    isLeader: Boolean = false,
    onSetLeader: ((String) -> Unit)? = null,
    warehouseItems: List<WarehouseItem> = emptyList(),
    allCharacters: List<Character> = emptyList(),
    onEquipItem: (characterId: String, slot: EquipmentSlotType, itemId: String) -> Unit = { _, _, _ -> },
    onUnequipItem: (characterId: String, slot: EquipmentSlotType) -> Unit = { _, _ -> }
) {
    var showRetireConfirm by remember { mutableStateOf(false) }

    val effectiveStats = character.getEffectiveStats(warehouseItems)
    val effectiveMaxHealth = character.getEffectiveMaxHealth(warehouseItems)
    val effectiveCarryCapacity = character.getEffectiveCarryCapacityKg(warehouseItems)

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("dialog_character_detail"),
            shape = RoundedCornerShape(20.dp),
            color = FrontierDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(roleColor.copy(alpha = 0.15f))
                                .border(1.5.dp, roleColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = roleIcon,
                                contentDescription = null,
                                tint = roleColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = character.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = TextWhite
                                )
                                if (isLeader) {
                                    Surface(
                                        color = WarningAmber.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "⭐ Командир",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = WarningAmber,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = roleColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = character.role.titleRu,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        color = roleColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "• ${character.specialization}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_close_character_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = FrontierBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Level, XP & Health Card
                    Surface(
                        color = FrontierDarkBackground,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Уровень ${character.level}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AccentCyan
                                    )
                                    if (character.unspentSkillPoints > 0) {
                                        Surface(
                                            color = SafeEmerald,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "+${character.unspentSkillPoints} ОН",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = FrontierDarkBackground,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    color = statusColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = character.status.titleRu,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Health Bar
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Здоровье (HP)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${character.health} / $effectiveMaxHealth",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (character.healthFraction < 0.35f) CriticalRed else SafeEmerald
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (character.health.toFloat() / effectiveMaxHealth.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (character.healthFraction < 0.35f) CriticalRed else SafeEmerald,
                                    trackColor = FrontierCardElevated
                                )
                            }

                            // XP Bar
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Прогресс опыта",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "${character.experience} / ${character.maxExperience} XP",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = AccentCyan
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { character.xpFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = AccentCyan,
                                    trackColor = FrontierCardElevated
                                )
                            }

                            // Morale & Energy Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Morale
                                Surface(
                                    color = FrontierDarkSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "😊 Мораль",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                            Text(
                                                text = "${character.morale}% (${character.moraleStatusLabel})",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (character.morale >= 70) SafeEmerald else if (character.morale >= 40) WarningAmber else CriticalRed
                                                )
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { character.morale / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = if (character.morale >= 70) SafeEmerald else if (character.morale >= 40) WarningAmber else CriticalRed,
                                            trackColor = FrontierCardElevated
                                        )
                                    }
                                }

                                // Energy
                                Surface(
                                    color = FrontierDarkSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "⚡ Энергия",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                            Text(
                                                text = "${character.energy}%",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = WarningAmber
                                                )
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { character.energy / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = WarningAmber,
                                            trackColor = FrontierCardElevated
                                        )
                                    }
                                }
                            }

                            // Heal button
                            if (character.health < character.effectiveMaxHealth) {
                                Button(
                                    onClick = { onHealInClinic(character.id) },
                                    enabled = medicineAvailable >= 1,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SafeEmerald.copy(alpha = 0.2f),
                                        contentColor = SafeEmerald
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_heal_resident_${character.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Healing,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (medicineAvailable >= 1) "Лечить в лазарете (-1 Медикамент)" else "Недостаточно медикаментов в медпункте",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    // RPG Stats & Skill Points Allocation
                    Surface(
                        color = FrontierDarkBackground,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Боевые и технические навыки",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextWhite
                                    )
                                    if (character.unspentSkillPoints > 0) {
                                        Text(
                                            text = "Доступно очков для распределения: ${character.unspentSkillPoints}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SafeEmerald
                                        )
                                    }
                                }
                                Text(
                                    text = "Сила: ${effectiveStats.totalSkillPower}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = WarningAmber
                                )
                            }

                            StatUpgradeRow(
                                label = "⚔️ Атака",
                                baseValue = character.stats.attack,
                                effectiveValue = effectiveStats.attack,
                                maxValue = 40,
                                color = WarningOrange,
                                canUpgrade = character.unspentSkillPoints > 0,
                                onUpgrade = { onAllocateSkillPoint(character.id, CharacterStatType.ATTACK) }
                            )

                            StatUpgradeRow(
                                label = "🛡️ Защита",
                                baseValue = character.stats.defense,
                                effectiveValue = effectiveStats.defense,
                                maxValue = 35,
                                color = AccentCyan,
                                canUpgrade = character.unspentSkillPoints > 0,
                                onUpgrade = { onAllocateSkillPoint(character.id, CharacterStatType.DEFENSE) }
                            )

                            StatUpgradeRow(
                                label = "🎒 Поиск и лут",
                                baseValue = character.stats.scavengingSkill,
                                effectiveValue = effectiveStats.scavengingSkill,
                                maxValue = 40,
                                color = Color(0xFFB388FF),
                                canUpgrade = character.unspentSkillPoints > 0,
                                onUpgrade = { onAllocateSkillPoint(character.id, CharacterStatType.SCAVENGING) }
                            )

                            StatUpgradeRow(
                                label = "🔧 Инженерия",
                                baseValue = character.stats.engineeringSkill,
                                effectiveValue = effectiveStats.engineeringSkill,
                                maxValue = 40,
                                color = WarningAmber,
                                canUpgrade = character.unspentSkillPoints > 0,
                                onUpgrade = { onAllocateSkillPoint(character.id, CharacterStatType.ENGINEERING) }
                            )

                            StatUpgradeRow(
                                label = "💉 Медицина",
                                baseValue = character.stats.medicalSkill,
                                effectiveValue = effectiveStats.medicalSkill,
                                maxValue = 40,
                                color = SafeEmerald,
                                canUpgrade = character.unspentSkillPoints > 0,
                                onUpgrade = { onAllocateSkillPoint(character.id, CharacterStatType.MEDICAL) }
                            )

                            StatUpgradeRow(
                                label = "❤️ Живучесть (HP)",
                                baseValue = character.maxHealth,
                                effectiveValue = effectiveMaxHealth,
                                maxValue = 160,
                                color = CriticalRed,
                                canUpgrade = character.unspentSkillPoints > 0,
                                onUpgrade = { onAllocateSkillPoint(character.id, CharacterStatType.MAX_HEALTH) }
                            )
                        }
                    }

                    // Interactive Equipment and Gear Loadout Section
                    EquipmentManagementSection(
                        character = character,
                        warehouseItems = warehouseItems,
                        allCharacters = allCharacters,
                        onEquipItem = onEquipItem,
                        onUnequipItem = onUnequipItem
                    )

                    // Character Unique Traits List
                    Surface(
                        color = FrontierDarkBackground,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Уникальные черты характера (${character.traits.size}/4)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhite
                                )
                                Text(
                                    text = "Пассивные бонусы",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }

                            if (character.traits.isEmpty()) {
                                Text(
                                    text = "У этого жителя пока нет открытых черт. Черты открываются при получении 3 и 5 уровней.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    character.traits.forEach { trait ->
                                        TraitCardItem(trait = trait)
                                    }
                                }
                            }
                        }
                    }

                    // Service Record & Lore
                    Surface(
                        color = FrontierDarkBackground,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Послужной список и вылазки",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                RecordChip("🚶 Вылазок", "${character.expeditionsCount}")
                                RecordChip("📅 Дней на базе", "${character.daysInSettlement}")
                                RecordChip("🎯 Угроз устранено", "${character.threatsNeutralizedCount}")
                            }

                            HorizontalDivider(color = FrontierBorder, thickness = 0.5.dp)

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Экипировка",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextMuted
                                )
                                Text(
                                    text = character.equipmentSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                            }

                            HorizontalDivider(color = FrontierBorder, thickness = 0.5.dp)

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Биография",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextMuted
                                )
                                Text(
                                    text = character.bio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (character.status != CharacterStatus.ON_EXPEDITION) {
                        Button(
                            onClick = { onToggleSquad(character.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInSquad) CriticalRed.copy(alpha = 0.85f) else SafeEmerald
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_toggle_squad_modal_${character.id}")
                        ) {
                            Icon(
                                imageVector = if (isInSquad) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isInSquad) "Убрать из отряда" else "Назначить в отряд",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (isInSquad && onSetLeader != null) {
                            Button(
                                onClick = { onSetLeader(character.id) },
                                enabled = !isLeader,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLeader) WarningAmber.copy(alpha = 0.4f) else WarningAmber,
                                    contentColor = if (isLeader) WarningAmber else FrontierDarkBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("btn_set_leader_modal_${character.id}")
                            ) {
                                Icon(
                                    imageVector = if (isLeader) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isLeader) "Командир" else "В лидеры",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (!showRetireConfirm) {
                                showRetireConfirm = true
                            } else {
                                onRetireResident(character.id)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (showRetireConfirm) CriticalRed else TextMuted
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (showRetireConfirm) CriticalRed else FrontierBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_retire_resident_${character.id}")
                    ) {
                        Text(
                            text = if (showRetireConfirm) "Подтвердить роспуск" else "Распустить",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatUpgradeRow(
    label: String,
    baseValue: Int,
    effectiveValue: Int,
    maxValue: Int,
    color: Color,
    canUpgrade: Boolean,
    onUpgrade: () -> Unit
) {
    val fraction = (effectiveValue.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
    val hasBonus = effectiveValue > baseValue

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextWhite,
            modifier = Modifier.width(115.dp)
        )

        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = FrontierCardElevated
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.width(42.dp)
        ) {
            Text(
                text = effectiveValue.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            if (hasBonus) {
                Text(
                    text = "↑",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SafeEmerald
                )
            }
        }

        if (canUpgrade) {
            IconButton(
                onClick = onUpgrade,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(SafeEmerald)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Улучшить",
                    tint = FrontierDarkBackground,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TraitCardItem(trait: CharacterTrait) {
    val catColor = trait.categoryColor

    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(catColor.copy(alpha = 0.15f))
                    .border(1.dp, catColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = trait.category.icon,
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = trait.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Surface(
                        color = catColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = trait.category.titleRu,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = catColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = trait.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
                if (trait.effectSummary.isNotBlank()) {
                    Text(
                        text = trait.effectSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SafeEmerald
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordChip(
    title: String,
    value: String
) {
    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, FrontierBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = AccentCyan
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMuted
            )
        }
    }
}

