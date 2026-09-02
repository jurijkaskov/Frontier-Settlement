package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.SurvivorGenerator
import com.example.domain.model.CharacterRole
import com.example.domain.model.GameState
import com.example.domain.model.ResourceType
import com.example.ui.theme.*

@Composable
fun RecruitSurvivorDialog(
    gameState: GameState,
    onRecruit: (CharacterRole?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRole by remember { mutableStateOf<CharacterRole?>(null) }

    val cost = SurvivorGenerator.RECRUITMENT_COST
    val moneyCost = cost[ResourceType.MONEY] ?: 60
    val foodCost = cost[ResourceType.FOOD] ?: 15
    val waterCost = cost[ResourceType.WATER] ?: 15

    val hasEnoughMoney = gameState.resources.money >= moneyCost
    val hasEnoughFood = gameState.resources.food >= foodCost
    val hasEnoughWater = gameState.resources.water >= waterCost
    val hasResources = hasEnoughMoney && hasEnoughFood && hasEnoughWater
    val hasHousingSpace = gameState.freeHousingSlots > 0
    val canRecruit = hasResources && hasHousingSpace

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_recruit_survivor"),
            shape = RoundedCornerShape(20.dp),
            color = FrontierDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .background(SafeEmerald.copy(alpha = 0.15f))
                                .border(1.dp, SafeEmerald, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = SafeEmerald,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Приём выжившего",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                            Text(
                                text = "Радиосигнал и распределение жилья",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextMuted
                        )
                    }
                }

                HorizontalDivider(color = FrontierBorder, thickness = 1.dp)

                // Housing capacity status banner
                Surface(
                    color = if (hasHousingSpace) SafeEmerald.copy(alpha = 0.10f) else CriticalRed.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (hasHousingSpace) SafeEmerald.copy(alpha = 0.35f) else CriticalRed.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Вместимость жилья",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextMuted
                            )
                            Text(
                                text = if (hasHousingSpace) "Свободно мест: ${gameState.freeHousingSlots}" else "Жилой фонд заполнен!",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (hasHousingSpace) SafeEmerald else CriticalRed
                            )
                        }

                        Text(
                            text = "${gameState.currentPopulation} / ${gameState.maxPopulation} чел.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                    }
                }

                // Preferred role selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Специализация кандидата:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RoleChip(
                            title = "Любая",
                            isSelected = selectedRole == null,
                            onClick = { selectedRole = null },
                            modifier = Modifier.weight(1f)
                        )
                        RoleChip(
                            title = "Разведка",
                            isSelected = selectedRole == CharacterRole.SCOUT,
                            onClick = { selectedRole = CharacterRole.SCOUT },
                            modifier = Modifier.weight(1f)
                        )
                        RoleChip(
                            title = "Штурм",
                            isSelected = selectedRole == CharacterRole.SOLDIER,
                            onClick = { selectedRole = CharacterRole.SOLDIER },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RoleChip(
                            title = "Инженер",
                            isSelected = selectedRole == CharacterRole.ENGINEER,
                            onClick = { selectedRole = CharacterRole.ENGINEER },
                            modifier = Modifier.weight(1f)
                        )
                        RoleChip(
                            title = "Медик",
                            isSelected = selectedRole == CharacterRole.MEDIC,
                            onClick = { selectedRole = CharacterRole.MEDIC },
                            modifier = Modifier.weight(1f)
                        )
                        RoleChip(
                            title = "Следопыт",
                            isSelected = selectedRole == CharacterRole.SCAVENGER,
                            onClick = { selectedRole = CharacterRole.SCAVENGER },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Recruitment Resource Costs
                Surface(
                    color = FrontierDarkBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Стоимость приёма и экипировки:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CostBadge(
                                label = "🪙 $moneyCost Кр",
                                isAffordable = hasEnoughMoney,
                                availableText = "${gameState.resources.money} в кассе"
                            )
                            CostBadge(
                                label = "🍖 $foodCost Еды",
                                isAffordable = hasEnoughFood,
                                availableText = "${gameState.resources.food} на складе"
                            )
                            CostBadge(
                                label = "💧 $waterCost Воды",
                                isAffordable = hasEnoughWater,
                                availableText = "${gameState.resources.water} на складе"
                            )
                        }
                    }
                }

                // Info note
                Text(
                    text = "ℹ️ Новый житель будет потреблять 1 ед. Еды и 1 ед. Воды в сутки, но сможет выходить в рейды пустоши или усиливать оборону базы.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Отмена", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = {
                            onRecruit(selectedRole)
                            onDismiss()
                        },
                        enabled = canRecruit,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("btn_confirm_recruit_survivor"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafeEmerald,
                            disabledContainerColor = SafeEmerald.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!hasHousingSpace) "Нет мест" else if (!hasResources) "Мало ресурсов" else "Принять",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) SafeEmerald.copy(alpha = 0.2f) else FrontierDarkBackground,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) SafeEmerald else FrontierBorder
        ),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) SafeEmerald else TextMuted
            )
        }
    }
}

@Composable
private fun CostBadge(
    label: String,
    isAffordable: Boolean,
    availableText: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Surface(
            color = if (isAffordable) SafeEmerald.copy(alpha = 0.15f) else CriticalRed.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isAffordable) SafeEmerald.copy(alpha = 0.4f) else CriticalRed.copy(alpha = 0.4f)
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isAffordable) SafeEmerald else CriticalRed,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Text(
            text = availableText,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextMuted
        )
    }
}
