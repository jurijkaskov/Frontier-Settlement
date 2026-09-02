package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.*
import com.example.ui.theme.*

/**
 * Detailed Tactical Blueprint Modal for researching and inspecting a technology.
 */
@Composable
fun ResearchDetailDialog(
    validationInfo: TechValidationInfo,
    resources: GameResources,
    onResearch: () -> Unit,
    onDismiss: () -> Unit
) {
    val tech = validationInfo.tech
    val status = validationInfo.status
    val isResearched = tech.isResearched
    val canBeResearched = validationInfo.canBeResearched

    val categoryColor = when (tech.category) {
        TechCategory.SETTLEMENT -> WaterCyan
        TechCategory.PRODUCTION -> MaterialsOrange
        TechCategory.ECONOMY -> CreditsYellow
        TechCategory.SURVIVAL -> SafeEmerald
        else -> TechCyan
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .testTag("dialog_tech_details_${tech.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FrontierDarkSurfaceElevated),
            border = BorderStroke(1.dp, if (isResearched) SafeEmerald else categoryColor.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header: Category, Tier, Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = categoryColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = tech.category.titleRu.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = categoryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = FrontierSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, FrontierBorder)
                        ) {
                            Text(
                                text = "ТИР ${tech.tier}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_close_tech_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Title and Lore
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = tech.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isResearched) SafeEmerald else TextWhite
                        )
                    )

                    if (tech.loreRu.isNotBlank()) {
                        Text(
                            text = tech.loreRu,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted.copy(alpha = 0.8f),
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }

                // Full Technical Description
                Surface(
                    color = FrontierSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, FrontierBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tech.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextWhite.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // Effects Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ЭФФЕКТЫ И ВЛИЯНИЕ НА СИСТЕМЫ:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )

                    tech.effects.forEach { effect ->
                        Surface(
                            color = if (isResearched) SafeEmerald.copy(alpha = 0.12f) else TechCyan.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isResearched) SafeEmerald.copy(alpha = 0.4f) else TechCyan.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isResearched) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isResearched) SafeEmerald else TechCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = effect.summaryRu,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isResearched) SafeEmerald else TechCyan,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Requirements Checklist (if not already researched)
                if (!isResearched) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ТРЕБОВАНИЯ ДЛЯ ИЗУЧЕНИЯ:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )

                        validationInfo.allRequirements.forEach { req ->
                            RequirementCheckRow(req = req)
                        }
                    }
                }

                // Status Banner / Action Footer
                if (isResearched) {
                    Surface(
                        color = SafeEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SafeEmerald),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SafeEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Технология успешно исследована и активна",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = SafeEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, FrontierBorder)
                        ) {
                            Text(text = "Закрыть", color = TextMuted)
                        }

                        Button(
                            onClick = {
                                onResearch()
                                onDismiss()
                            },
                            enabled = canBeResearched,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SafeEmerald,
                                disabledContainerColor = FrontierBorder.copy(alpha = 0.4f),
                                contentColor = TextWhite,
                                disabledContentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                                .testTag("btn_confirm_research_${tech.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (canBeResearched) Icons.Default.Biotech else Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (canBeResearched) "Изучить технологию" else "Недоступно",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequirementCheckRow(req: TechRequirementStatus) {
    Surface(
        color = if (req.isSatisfied) SafeEmerald.copy(alpha = 0.08f) else DangerCrimson.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (req.isSatisfied) SafeEmerald.copy(alpha = 0.3f) else DangerCrimson.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (req.isSatisfied) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (req.isSatisfied) SafeEmerald else DangerCrimson,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = req.labelRu,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (req.isSatisfied) TextWhite else DangerCrimson,
                        fontSize = 12.sp
                    )
                )
            }

            if (req.currentProgressRu != null) {
                Text(
                    text = req.currentProgressRu,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (req.isSatisfied) SafeEmerald else DangerCrimson,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
