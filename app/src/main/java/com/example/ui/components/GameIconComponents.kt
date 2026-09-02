package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CharacterRole
import com.example.domain.model.ResourceType
import com.example.ui.theme.GameTheme

/**
 * Type-safe resolver for Resource icons and semantic colors.
 */
object ResourceVisuals {
    fun iconFor(type: ResourceType): ImageVector = when (type) {
        ResourceType.MONEY -> Icons.Default.MonetizationOn
        ResourceType.FOOD -> Icons.Default.Restaurant
        ResourceType.WATER -> Icons.Default.WaterDrop
        ResourceType.FUEL -> Icons.Default.LocalGasStation
        ResourceType.MATERIALS -> Icons.Default.Build
        ResourceType.MEDICINE -> Icons.Default.MedicalServices
        ResourceType.AMMO -> Icons.Default.Shield
        ResourceType.COMPONENTS -> Icons.Default.Memory
        ResourceType.RARE_ALLOY -> Icons.Default.Diamond
    }

    fun colorFor(type: ResourceType): Color = when (type) {
        ResourceType.MONEY -> Color(0xFFFACC15)
        ResourceType.FOOD -> Color(0xFF22C55E)
        ResourceType.WATER -> Color(0xFF06B6D4)
        ResourceType.FUEL -> Color(0xFFF97316)
        ResourceType.MATERIALS -> Color(0xFFFB923C)
        ResourceType.MEDICINE -> Color(0xFF14B8A6)
        ResourceType.AMMO -> Color(0xFFE11D48)
        ResourceType.COMPONENTS -> Color(0xFF818CF8)
        ResourceType.RARE_ALLOY -> Color(0xFFA855F7)
    }

    val creditsIcon: ImageVector = Icons.Default.MonetizationOn
    val creditsColor: Color = Color(0xFFFACC15)
}

/**
 * Unified Resource Icon with standard sizing and semantic tinting.
 */
@Composable
fun ResourceIcon(
    type: ResourceType,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color? = null
) {
    Icon(
        imageVector = ResourceVisuals.iconFor(type),
        contentDescription = type.titleRu,
        tint = tint ?: ResourceVisuals.colorFor(type),
        modifier = modifier.size(size)
    )
}

/**
 * Unified Resource Amount row (icon + amount + optional deficit badge or label).
 */
@Composable
fun ResourceAmount(
    type: ResourceType,
    amount: Int,
    modifier: Modifier = Modifier,
    delta: Int? = null,
    isDeficit: Boolean = false,
    showLabel: Boolean = false,
    size: Dp = 16.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ResourceIcon(type = type, size = size)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$amount",
                    style = GameTheme.typography.numericValue.copy(
                        color = if (isDeficit) GameTheme.colors.danger else GameTheme.colors.textPrimary,
                        fontSize = 13.sp
                    )
                )
                if (delta != null && delta != 0) {
                    val isPositive = delta > 0
                    Text(
                        text = if (isPositive) " (+$delta)" else " ($delta)",
                        style = GameTheme.typography.caption.copy(
                            color = if (isPositive) GameTheme.colors.success else GameTheme.colors.danger,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            if (showLabel) {
                Text(
                    text = type.titleRu,
                    style = GameTheme.typography.caption.copy(color = GameTheme.colors.textMuted)
                )
            }
        }
    }
}

/**
 * Unified Credits / Money Amount row.
 */
@Composable
fun CreditsAmount(
    amount: Int,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ResourceVisuals.creditsIcon,
            contentDescription = "Кредиты",
            tint = ResourceVisuals.creditsColor,
            modifier = Modifier.size(size)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$amount",
            style = GameTheme.typography.numericValue.copy(
                color = ResourceVisuals.creditsColor,
                fontSize = 13.sp
            )
        )
    }
}

/**
 * Unified Character Portrait with tactical frame, role icon, leader star, and status indicators.
 */
@Composable
fun CharacterPortrait(
    role: CharacterRole = CharacterRole.SCOUT,
    portraitAssetId: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    isSelected: Boolean = false,
    isLeader: Boolean = false,
    isInExpedition: Boolean = false,
    isWounded: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String? = null
) {
    val resolvedId = portraitAssetId ?: when (role) {
        CharacterRole.SCOUT -> "char_portrait_scout_01"
        CharacterRole.SOLDIER -> "char_portrait_soldier_01"
        CharacterRole.SCAVENGER -> "char_portrait_scavenger_01"
        CharacterRole.ENGINEER -> "char_portrait_engineer_01"
        CharacterRole.MEDIC -> "char_portrait_medic_01"
    }

    val def = VisualAssetResolver.resolve(resolvedId, com.example.domain.content.visual.VisualAssetCategory.CHARACTER_PORTRAIT)

    val roleColor = when (role) {
        CharacterRole.SCOUT -> Color(0xFF38BDF8)
        CharacterRole.SOLDIER -> Color(0xFFE11D48)
        CharacterRole.SCAVENGER -> Color(0xFFFB923C)
        CharacterRole.ENGINEER -> Color(0xFF818CF8)
        CharacterRole.MEDIC -> Color(0xFF10B981)
    }

    val roleIcon = when (role) {
        CharacterRole.SCOUT -> Icons.Default.Explore
        CharacterRole.SOLDIER -> Icons.Default.Shield
        CharacterRole.SCAVENGER -> Icons.Default.ShoppingBag
        CharacterRole.ENGINEER -> Icons.Default.Build
        CharacterRole.MEDIC -> Icons.Default.MedicalServices
    }

    val borderColor = when {
        isSelected -> GameTheme.colors.primary
        isLeader -> Color(0xFFFACC15)
        isWounded -> GameTheme.colors.danger
        else -> GameTheme.colors.borderLight
    }

    Box(
        modifier = modifier
            .size(size)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clip(RoundedCornerShape(12.dp))
            .background(GameTheme.colors.surfaceHighlight)
            .border(if (isSelected || isLeader) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        // Main Avatar Silhouette / Role Icon
        Icon(
            imageVector = def.fallbackIcon,
            contentDescription = role.titleRu,
            tint = def.fallbackColor.copy(alpha = if (isInExpedition) 0.4f else 0.85f),
            modifier = Modifier.size(size * 0.6f)
        )

        // Bottom-right role badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(GameTheme.colors.surface)
                .border(1.dp, roleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = roleIcon,
                contentDescription = null,
                tint = roleColor,
                modifier = Modifier.size(10.dp)
            )
        }

        // Top-left leader star
        if (isLeader) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(2.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFACC15)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Лидер",
                    tint = Color(0xFF451A03),
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        // Expedition overlay banner
        if (isInExpedition) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x77000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "В ПОХОДЕ",
                    style = GameTheme.typography.badgeText.copy(
                        fontSize = 8.sp,
                        color = Color(0xFFF59E0B)
                    )
                )
            }
        }
    }
}

/**
 * Unified Stat Display row for character profile screens.
 */
@Composable
fun CharacterStatRow(
    combat: Int,
    scavenging: Int,
    engineering: Int,
    medical: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatBadge(label = "Бой", value = combat, color = Color(0xFFE11D48), icon = Icons.Default.Shield)
        StatBadge(label = "Сбор", value = scavenging, color = Color(0xFFFB923C), icon = Icons.Default.ShoppingBag)
        StatBadge(label = "Техника", value = engineering, color = Color(0xFF818CF8), icon = Icons.Default.Build)
        StatBadge(label = "Медицина", value = medical, color = Color(0xFF10B981), icon = Icons.Default.MedicalServices)
    }
}

/**
 * Compact Tactical Stat Badge.
 */
@Composable
fun StatBadge(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Surface(
        color = GameTheme.colors.surfaceHighlight,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = "$label: ",
                style = GameTheme.typography.caption.copy(color = GameTheme.colors.textMuted, fontSize = 9.sp)
            )
            Text(
                text = "$value",
                style = GameTheme.typography.numericSecondary.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 11.sp
                )
            )
        }
    }
}
