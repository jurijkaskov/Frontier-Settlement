package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReputationBalanceConfig
import com.example.domain.model.GameState
import com.example.domain.model.reputation.*
import com.example.domain.service.reputation.ReputationLevelResolver
import com.example.ui.theme.*

/**
 * Tactical Global Reputation Overview Card displaying standing, tier progress, and active world perks.
 */
@Composable
fun GlobalReputationCard(
    reputation: Int,
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val tier = ReputationLevelResolver.resolveSettlementTier(reputation)
    val (currentTier, progressFraction, pointsNeeded) = ReputationLevelResolver.calculateSettlementTierProgress(reputation)
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "repTierProgress")

    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tier.badgeColor.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("global_reputation_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(tier.badgeColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.5.dp, tier.badgeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Репутация",
                            tint = tier.badgeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "РЕПУТАЦИЯ АВАНПОСТА",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = tier.titleRu,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = tier.badgeColor
                            )
                        )
                    }
                }

                // Points Badge
                Surface(
                    color = tier.badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tier.badgeColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "$reputation / 100",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = tier.badgeColor
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Flavor Narrative Description
            Text(
                text = tier.descriptionRu,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSubtle,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar to Next Tier
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (pointsNeeded > 0) "До следующего ранга: $pointsNeeded очков" else "Максимальный ранг достигнут",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = tier.badgeColor,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(FrontierBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(tier.badgeColor.copy(alpha = 0.7f), tier.badgeColor)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Global Perks Matrix
            Text(
                text = "ДЕЙСТВУЮЩИЕ БОНУСЫ РАНГА",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TechCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Trade Bonus Chip
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Торговля",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                        )
                        Text(
                            text = if (tier.tradeBuyDiscountPercent > 0) "-${tier.tradeBuyDiscountPercent}% покупка" else if (tier.tradeBuyDiscountPercent < 0) "+${-tier.tradeBuyDiscountPercent}% наценка" else "0%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (tier.tradeBuyDiscountPercent >= 0) SafeEmerald else DangerCrimson
                            )
                        )
                    }
                }

                // Recruit Morale Chip
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Мораль рекрутов",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                        )
                        Text(
                            text = if (tier.recruitMoraleBonus > 0) "+${tier.recruitMoraleBonus}" else "${tier.recruitMoraleBonus}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (tier.recruitMoraleBonus >= 0) TechCyan else DangerCrimson
                            )
                        )
                    }
                }

                // Caravans Chip
                Surface(
                    color = FrontierDarkSurfaceHighlight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Караваны",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                        )
                        Text(
                            text = if (tier.isPositive) "Стабильно" else "Редко",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (tier.isPositive) CreditsYellow else WarningAmber
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Faction Status Card in the factions list.
 */
@Composable
fun FactionCardItem(
    faction: FactionDefinition,
    relation: FactionRelation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tier = relation.tier
    val (currentTier, progressFraction, needed) = ReputationLevelResolver.calculateFactionTierProgress(relation.points)
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "factionProgress")

    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tier.badgeColor.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("faction_card_${faction.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val factionIcon = when (faction.iconKey) {
                        "storefront" -> Icons.Default.Storefront
                        "science" -> Icons.Default.Science
                        "explore" -> Icons.Default.Explore
                        "groups" -> Icons.Default.Groups
                        "gavel" -> Icons.Default.Gavel
                        else -> Icons.Default.Flag
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(tier.badgeColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, tier.badgeColor.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = factionIcon,
                            contentDescription = faction.nameRu,
                            tint = tier.badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = faction.nameRu,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                        Text(
                            text = faction.titleRu,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Tier Badge
                Surface(
                    color = tier.badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tier.badgeColor)
                ) {
                    Text(
                        text = tier.titleRu,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = tier.badgeColor,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar and Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Отношения: ${relation.points} pts",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = tier.badgeColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = if (needed > 0) "До повышения: $needed pts" else "Максимум",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(FrontierBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(tier.badgeColor)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Unlocked Perks Summary
            val unlockedPerks = faction.perks.filter { it.isUnlocked(relation.points) }
            val nextPerk = faction.perks.firstOrNull { !it.isUnlocked(relation.points) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (unlockedPerks.isNotEmpty()) {
                    Text(
                        text = "⭐ Открыто бонусов: ${unlockedPerks.size}/${faction.perks.size}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SafeEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                } else {
                    Text(
                        text = "🔒 Бонусы закрыты (нужен статус «Дружественные»)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Подробнее",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Detailed Dialog / Sheet showing a Faction's full profile, lore, and perk tree.
 */
@Composable
fun FactionDetailDialog(
    faction: FactionDefinition,
    relation: FactionRelation,
    onDismiss: () -> Unit
) {
    val tier = relation.tier
    val factionIcon = when (faction.iconKey) {
        "storefront" -> Icons.Default.Storefront
        "science" -> Icons.Default.Science
        "explore" -> Icons.Default.Explore
        "groups" -> Icons.Default.Groups
        "gavel" -> Icons.Default.Gavel
        else -> Icons.Default.Flag
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = FrontierPrimary),
                modifier = Modifier.testTag("button_close_faction_dialog")
            ) {
                Text("Закрыть")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(tier.badgeColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, tier.badgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = factionIcon,
                        contentDescription = null,
                        tint = tier.badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = faction.nameRu,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                    )
                    Text(
                        text = "Статус: ${tier.titleRu} (${relation.points} pts)",
                        style = MaterialTheme.typography.labelSmall.copy(color = tier.badgeColor)
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = faction.taglineRu,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TechCyan,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )
                }

                item {
                    Text(
                        text = faction.descriptionRu,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSubtle,
                            lineHeight = 16.sp
                        )
                    )
                }

                if (faction.leaderNameRu.isNotEmpty()) {
                    item {
                        Surface(
                            color = FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "ЛИДЕР ФРАКЦИИ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = faction.leaderNameRu,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = faction.leaderTitleRu,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSubtle,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "ДРЕВО ФРАКЦИОННЫХ ПРИВИЛЕГИЙ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                items(faction.perks) { perk ->
                    val isUnlocked = perk.isUnlocked(relation.points)
                    Surface(
                        color = if (isUnlocked) SafeEmerald.copy(alpha = 0.1f) else FrontierDarkSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isUnlocked) SafeEmerald.copy(alpha = 0.6f) else FrontierBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) SafeEmerald else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = perk.titleRu,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnlocked) TextWhite else TextMuted
                                        )
                                    )
                                    Text(
                                        text = perk.requiredTier.titleRu,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isUnlocked) SafeEmerald else TextMuted,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Text(
                                    text = perk.descriptionRu,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isUnlocked) TextSubtle else TextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = FrontierDarkSurfaceElevated,
        modifier = Modifier.testTag("dialog_faction_detail")
    )
}

/**
 * Chronicle Entry for a Reputation or Faction change event.
 */
@Composable
fun ReputationHistoryCard(
    entry: ReputationHistoryEntry,
    modifier: Modifier = Modifier
) {
    val factionDef = entry.factionId?.let { ReputationBalanceConfig.getFaction(it) }
    val isPos = entry.isPositive
    val tagColor = if (isPos) SafeEmerald else DangerCrimson

    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("rep_history_${entry.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(tagColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, tagColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (entry.type) {
                        ReputationChangeType.QUEST_COMPLETED -> Icons.Default.Assignment
                        ReputationChangeType.EXPEDITION_SUCCESS -> Icons.Default.Explore
                        ReputationChangeType.EVENT_CHOICE -> Icons.Default.QuestionAnswer
                        ReputationChangeType.TRADE_DEAL -> Icons.Default.Storefront
                        ReputationChangeType.DEFENSE_VICTORY -> Icons.Default.Shield
                        ReputationChangeType.RESEARCH_BREAKTHROUGH -> Icons.Default.Science
                        ReputationChangeType.SURVIVOR_RESCUE -> Icons.Default.PersonAdd
                        ReputationChangeType.TRIBUTE_OR_AID -> Icons.Default.VolunteerActivism
                        ReputationChangeType.CRISIS_FAILURE -> Icons.Default.Warning
                        ReputationChangeType.DEBUG_MOD -> Icons.Default.BugReport
                    },
                    contentDescription = null,
                    tint = tagColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (factionDef != null) "${entry.sourceTitle} (${factionDef.nameRu})" else entry.sourceTitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                    Text(
                        text = "День ${entry.day}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                }

                Text(
                    text = entry.reasonDescription,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSubtle,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = tagColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, tagColor.copy(alpha = 0.6f))
            ) {
                Text(
                    text = entry.formattedDelta,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = tagColor
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
