package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.ExpeditionReturnSummary
import com.example.domain.model.ResourceType
import com.example.domain.model.SquadMemberReturnOutcome
import com.example.domain.model.WarehouseItem
import com.example.ui.theme.*

/**
 * Polished Material 3 summary dialog presented when an expedition returns to the settlement.
 * Displays collected loot, storage allocation, squad XP progression, level-ups, injuries,
 * and settlement reputation gains.
 */
@Composable
fun ExpeditionReturnSummaryDialog(
    summary: ExpeditionReturnSummary,
    onDismiss: () -> Unit,
    onNavigateToWarehouse: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("dialog_expedition_return_summary"),
            shape = RoundedCornerShape(20.dp),
            color = FrontierDarkBackground,
            border = BorderStroke(1.5.dp, TechCyan.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with Mission Accomplished Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SafeEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Экспедиция завершена",
                                tint = SafeEmerald,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ЭКСПЕДИЦИЯ ЗАВЕРШЕНА",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = TextWhite
                                )
                            )
                            Text(
                                text = "${summary.locationName} • ${summary.locationDistanceKm} км",
                                style = MaterialTheme.typography.bodySmall.copy(color = TechCyan)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_close_return_summary")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Mission Stats Bar
                    MissionMetadataCard(summary)

                    // Gathered Resources Section
                    GatheredLootCard(summary)

                    // Storage Status & Overflow Warning (if any)
                    if (summary.hasOverflow) {
                        StorageOverflowAlertCard(summary)
                    }

                    // Squad Progression & Outcomes
                    SquadProgressionCard(summary.squadOutcomes)

                    // Quests & Settlement Perks
                    SettlementPerksCard(summary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (summary.hasOverflow && onNavigateToWarehouse != null) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onNavigateToWarehouse()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_return_summary_to_warehouse"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WarningAmber),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("На склад")
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_return_summary_confirm"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TechCyan,
                            contentColor = FrontierDarkBackground
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Принять трофеи",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionMetadataCard(summary: ExpeditionReturnSummary) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FrontierBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetaStatItem(
                icon = Icons.Default.DirectionsCar,
                label = "Транспорт",
                value = summary.vehicleName,
                tint = TechCyan
            )
            Divider(
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp),
                color = FrontierBorderLight
            )
            MetaStatItem(
                icon = Icons.Default.Star,
                label = "Опыт отряда",
                value = "+${summary.totalXpAwarded} XP",
                tint = WarningAmber
            )
            Divider(
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp),
                color = FrontierBorderLight
            )
            MetaStatItem(
                icon = Icons.Default.MilitaryTech,
                label = "Репутация",
                value = "+${summary.reputationGained}",
                tint = SafeEmerald
            )
        }
    }
}

@Composable
private fun MetaStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = TextMuted
            )
        )
    }
}

@Composable
private fun GatheredLootCard(summary: ExpeditionReturnSummary) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FrontierBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📦 ДОБЫТЫЕ РЕСУРСЫ И ТРОФЕИ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TechCyan,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = "Ценность: ~${summary.totalLootValueCredits} Кр.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resource grid chips
            val resources = summary.gatheredResources
            val resItems = listOfNotNull(
                if (resources.money > 0) ResourceType.MONEY to resources.money else null,
                if (resources.materials > 0) ResourceType.MATERIALS to resources.materials else null,
                if (resources.food > 0) ResourceType.FOOD to resources.food else null,
                if (resources.water > 0) ResourceType.WATER to resources.water else null,
                if (resources.fuel > 0) ResourceType.FUEL to resources.fuel else null,
                resources.extraResources[ResourceType.MEDICINE]?.let { if (it > 0) ResourceType.MEDICINE to it else null },
                resources.extraResources[ResourceType.AMMO]?.let { if (it > 0) ResourceType.AMMO to it else null },
                resources.extraResources[ResourceType.COMPONENTS]?.let { if (it > 0) ResourceType.COMPONENTS to it else null },
                resources.extraResources[ResourceType.RARE_ALLOY]?.let { if (it > 0) ResourceType.RARE_ALLOY to it else null }
            )

            if (resItems.isEmpty() && summary.gatheredItems.isEmpty()) {
                Text(
                    text = "Ценных ресурсов не обнаружено.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            } else {
                // Bulk Resources
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    resItems.take(4).forEach { (type, count) ->
                        ResourceLootBadge(
                            symbol = type.symbol,
                            title = type.titleRu,
                            count = count,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (resItems.size > 4) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        resItems.drop(4).take(4).forEach { (type, count) ->
                            ResourceLootBadge(
                                symbol = type.symbol,
                                title = type.titleRu,
                                count = count,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Item artifacts / components
                if (summary.gatheredItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Предметы и снаряжение (${summary.gatheredItems.size} шт.):",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    summary.gatheredItems.forEach { item ->
                        ItemLootRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceLootBadge(
    symbol: String,
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FrontierDarkSurfaceHighlight,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, FrontierBorderLight),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "+$count",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SafeEmerald
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = TextMuted
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ItemLootRow(item: WarehouseItem) {
    Surface(
        color = FrontierDarkSurfaceHighlight,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, FrontierBorderLight),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GameItemImage(
                    assetId = item.id,
                    size = 28.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextWhite
                    )
                )
            }
            Text(
                text = "x${item.quantity}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SafeEmerald
                )
            )
        }
    }
}

@Composable
private fun StorageOverflowAlertCard(summary: ExpeditionReturnSummary) {
    Surface(
        color = WarningAmber.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Предупреждение о переполнении",
                tint = WarningAmber,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Склад заполнен! Излишки трофеев сохранены",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Часть добычи помещена во временную зону разгрузки у ворот аванпоста. Освободите склад для их перемещения.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSubtle,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun SquadProgressionCard(squadOutcomes: List<SquadMemberReturnOutcome>) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FrontierBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "👥 РЕЗУЛЬТАТЫ БОЙЦОВ ОТРЯДА",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TechCyan,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                squadOutcomes.forEach { member ->
                    SquadMemberOutcomeItem(member)
                }
            }
        }
    }
}

@Composable
private fun SquadMemberOutcomeItem(member: SquadMemberReturnOutcome) {
    Surface(
        color = FrontierDarkSurfaceHighlight,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (member.leveledUp) WarningAmber.copy(alpha = 0.6f) else FrontierBorderLight
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Character Portrait with injury/leader state
                CharacterPortrait(
                    portraitAssetId = member.characterId,
                    size = 36.dp,
                    isWounded = member.isInjured
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.characterName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        if (member.leveledUp) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = WarningAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, WarningAmber)
                            ) {
                                Text(
                                    text = "🌟 LVL ${member.newLevel}!",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = WarningAmber,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ур. ${member.oldLevel} → ${member.newLevel}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle)
                        )
                        Text(
                            text = "HP: ${member.finalHealth}/${member.maxHealth}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (member.isInjured) CriticalRed else SafeEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // XP pill
            Surface(
                color = TechCyan.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "+${member.xpGained} XP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TechCyan
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SettlementPerksCard(summary: ExpeditionReturnSummary) {
    if (summary.completedQuests.isEmpty() && summary.summaryLogs.isEmpty()) return

    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FrontierBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "📜 ИТОГИ И ДОСТИЖЕНИЯ",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TechCyan,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (summary.completedQuests.isNotEmpty()) {
                summary.completedQuests.forEach { questTitle ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = SafeEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Задание готово к сдаче: $questTitle",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SafeEmerald,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            summary.summaryLogs.take(3).forEach { log ->
                Text(
                    text = "• $log",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSubtle,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
