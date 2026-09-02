package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GameResources
import com.example.domain.model.Settlement
import com.example.ui.theme.*

@Composable
fun ResourceTopBar(
    resources: GameResources,
    settlement: Settlement,
    day: Int,
    onNextDayClick: () -> Unit,
    onWarehouseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        color = FrontierDarkSurface,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = FrontierBorder,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Header Row: Settlement Name & Day Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(FrontierPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SafeEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = settlement.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "Ур. ${settlement.level} • Репутация: ${settlement.reputation}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TechCyan,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Day Counter & Next Day Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorderLight)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "День $day",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                        }
                    }

                    Button(
                        onClick = onNextDayClick,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_next_day"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafeEmerald,
                            contentColor = FrontierOnPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Следующий день",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "День +1",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontally Scrollable Resource Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ResourceChip(
                    icon = Icons.Default.MonetizationOn,
                    label = "Кредиты",
                    value = "${resources.money}",
                    color = CreditsYellow
                )
                ResourceChip(
                    icon = Icons.Default.Restaurant,
                    label = "Еда",
                    value = "${resources.food}",
                    color = FoodGreen
                )
                ResourceChip(
                    icon = Icons.Default.WaterDrop,
                    label = "Вода",
                    value = "${resources.water}",
                    color = WaterCyan
                )
                ResourceChip(
                    icon = Icons.Default.LocalGasStation,
                    label = "Топливо",
                    value = "${resources.fuel}",
                    color = FuelAmber
                )
                ResourceChip(
                    icon = Icons.Default.Build,
                    label = "Материалы",
                    value = "${resources.materials}",
                    color = MaterialsOrange
                )

                // Storage Capacity Mini-Bar (Clickable)
                StorageUsageChip(
                    currentVolume = resources.totalStoredVolume,
                    maxVolume = resources.warehouseMaxCapacity,
                    onClick = onWarehouseClick
                )
            }
        }
    }
}

@Composable
fun ResourceChip(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun StorageUsageChip(
    currentVolume: Int,
    maxVolume: Int,
    onClick: () -> Unit
) {
    val fraction = if (maxVolume > 0) (currentVolume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f) else 0f
    val isNearFull = fraction > 0.85f

    Surface(
        color = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isNearFull) DangerCrimson else FrontierBorder
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = "Склад",
                tint = if (isNearFull) DangerCrimson else StoragePurple,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "$currentVolume / $maxVolume",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNearFull) DangerCrimson else TextWhite,
                        fontSize = 11.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .width(45.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(FrontierBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .background(if (isNearFull) DangerCrimson else StoragePurple)
                    )
                }
            }
        }
    }
}
