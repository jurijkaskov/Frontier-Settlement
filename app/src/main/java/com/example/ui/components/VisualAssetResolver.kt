package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.content.visual.AssetPriority
import com.example.domain.content.visual.AssetStatus
import com.example.domain.content.visual.VisualAssetCategory
import com.example.domain.content.visual.VisualAssetDefinition
import com.example.domain.content.visual.VisualAssetRegistry
import com.example.domain.model.Building
import com.example.domain.model.BuildingStatus
import com.example.domain.model.CharacterRole
import com.example.domain.model.ItemRarity
import com.example.domain.model.LocationType
import com.example.domain.model.ResourceType
import com.example.domain.model.Vehicle
import com.example.domain.model.WarehouseItem
import com.example.ui.theme.GameTheme

/**
 * UI-Layer Resolver and Renderer for Game Visual Assets.
 *
 * Provides safe fallback rendering, procedural canvas graphics, gradient scrims,
 * and debug inspection tools without ever crashing if an asset is not yet added.
 */
object VisualAssetResolver {

    /**
     * Development setting to force fallback rendering across all screens to test visual placeholders.
     */
    var forceMissingMode: Boolean by mutableStateOf(false)

    /**
     * Safely retrieves asset definition, mapping legacy avatar tags or location IDs if needed.
     */
    fun resolve(assetId: String?, category: VisualAssetCategory): VisualAssetDefinition {
        if (forceMissingMode) {
            return VisualAssetRegistry.getCategoryFallback(category)
        }

        if (assetId.isNullOrBlank()) {
            return VisualAssetRegistry.getCategoryFallback(category)
        }

        // Direct Registry Lookup
        val exact = VisualAssetRegistry.getDefinition(assetId)
        if (exact != null) return exact

        // Legacy / Alias Mapping
        val mappedId = when (assetId.lowercase()) {
            "scout" -> "char_portrait_scout_01"
            "soldier" -> "char_portrait_soldier_01"
            "engineer" -> "char_portrait_engineer_01"
            "medic" -> "char_portrait_medic_01"
            "scavenger" -> "char_portrait_scavenger_01"
            "sniper" -> "char_portrait_sniper_01"
            "mechanic" -> "char_portrait_mechanic_01"
            "guard" -> "char_portrait_soldier_01"
            "leader" -> "char_portrait_scout_01"
            "boss" -> "enemy_boss"
            "enemy_deserter", "enemy_guard", "enemy_medic" -> "enemy_raider"
            "enemy_beast" -> "enemy_mutant"
            "veh_default" -> "veh_foot"
            "loc_outpost" -> "loc_base"
            "loc_factory" -> "loc_industrial"
            "loc_lab" -> "loc_anomaly"
            "loc_tower" -> "loc_station"
            else -> null
        }

        if (mappedId != null) {
            val mappedDef = VisualAssetRegistry.getDefinition(mappedId)
            if (mappedDef != null) return mappedDef
        }

        return VisualAssetRegistry.getCategoryFallback(category)
    }

    fun getAssetInfo(assetId: String?): VisualAssetDefinition? =
        VisualAssetRegistry.getDefinition(assetId)

    fun getAllPriorityAssets(): List<VisualAssetDefinition> =
        VisualAssetRegistry.getDefinitionsByPriority(AssetPriority.A)

    fun getAllAssets(): List<VisualAssetDefinition> =
        VisualAssetRegistry.getAllDefinitions()

    fun getFallbackIconForLocationType(type: LocationType): ImageVector = when (type) {
        LocationType.SETTLEMENT, LocationType.TRADING_POST, LocationType.VILLAGE -> Icons.Default.Storefront
        LocationType.INDUSTRIAL_PLANT, LocationType.WAREHOUSE_COMPLEX -> Icons.Default.Factory
        LocationType.FOREST -> Icons.Default.Forest
        LocationType.FARM -> Icons.Default.Agriculture
        LocationType.ABANDONED_STATION -> Icons.Default.DirectionsTransit
        LocationType.CITY_RUINS -> Icons.Default.Apartment
        LocationType.MILITARY_BUNKER -> Icons.Default.Security
        LocationType.ANOMALY_ZONE -> Icons.Default.Diamond
    }
}

// ==========================================
// REUSABLE VISUAL COMPOSABLES
// ==========================================

/**
 * Atmospheric Hero Artwork Component for Locations, Events, and Screen Headers.
 */
@Composable
fun GameHeroImage(
    assetId: String?,
    modifier: Modifier = Modifier,
    locationType: LocationType? = null,
    aspectRatio: Float = 16f / 9f,
    height: Dp? = null,
    showOverlay: Boolean = true,
    overlayHeight: Dp = 80.dp,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    val def = VisualAssetResolver.resolve(assetId, VisualAssetCategory.LOCATION_HERO)
    val fallbackIcon = if (locationType != null) {
        VisualAssetResolver.getFallbackIconForLocationType(locationType)
    } else {
        def.fallbackIcon
    }
    val accentColor = def.fallbackColor

    val boxModifier = if (height != null) {
        modifier
            .fillMaxWidth()
            .height(height)
    } else {
        modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
    }

    Box(
        modifier = boxModifier
            .clip(RoundedCornerShape(14.dp))
            .background(GameTheme.colors.surfaceElevated)
            .border(1.dp, GameTheme.colors.borderLight, RoundedCornerShape(14.dp))
    ) {
        // Procedural Atmospheric Horizon
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.25f),
                        Color(0xFF0F172A),
                        Color(0xFF090D14)
                    )
                )
            )

            val strokeColor = accentColor.copy(alpha = 0.12f)
            for (i in 0..6) {
                val y = size.height * (0.4f + i * 0.1f)
                drawLine(
                    color = strokeColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
        }

        // Center Hero Landmark Graphic
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GameTheme.colors.surface.copy(alpha = 0.85f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Bottom Readability Gradient Scrim
        if (showOverlay) {
            HeroImageOverlay(
                modifier = Modifier.align(Alignment.BottomCenter),
                scrimHeight = overlayHeight
            )
        }

        // Custom Overlay Content (e.g. Title, Threat Badge)
        if (content != null) {
            content()
        }
    }
}

/**
 * Backward compatibility alias for existing code.
 */
@Composable
fun LocationHeroArt(
    visualAssetId: String?,
    locationType: LocationType,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp
) {
    GameHeroImage(
        assetId = visualAssetId,
        locationType = locationType,
        modifier = modifier,
        height = height
    )
}

/**
 * Standard Item Artwork Component for Warehouse, Workshop, Equipment, Loot, and Trade.
 */
@Composable
fun GameItemImage(
    item: WarehouseItem? = null,
    assetId: String? = null,
    rarity: ItemRarity = ItemRarity.COMMON,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showRarityBorder: Boolean = true
) {
    val resolvedId = assetId ?: item?.iconKey ?: "item_backpack_basic"
    val resolvedRarity = item?.rarity ?: rarity
    val def = VisualAssetResolver.resolve(resolvedId, VisualAssetCategory.ITEM)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(GameTheme.colors.surfaceHighlight)
            .border(
                width = if (showRarityBorder) 1.5.dp else 1.dp,
                color = if (showRarityBorder) resolvedRarity.color else GameTheme.colors.border,
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = def.fallbackIcon,
            contentDescription = def.titleRu,
            tint = resolvedRarity.color,
            modifier = Modifier.size(size * 0.58f)
        )
    }
}

/**
 * Standard Vehicle Artwork Component for Transport, Expedition Prep, and Details.
 */
@Composable
fun VehicleArtwork(
    vehicle: Vehicle? = null,
    assetId: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    isSelected: Boolean = false
) {
    val resolvedId = assetId ?: vehicle?.visualAssetId ?: "veh_foot"
    val def = VisualAssetResolver.resolve(resolvedId, VisualAssetCategory.VEHICLE)

    val borderColor = when {
        isSelected -> GameTheme.colors.primary
        else -> GameTheme.colors.borderLight
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(GameTheme.colors.surfaceHighlight)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = def.fallbackIcon,
            contentDescription = def.titleRu,
            tint = def.fallbackColor,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

/**
 * Standard Enemy Artwork Component for Combat, Encounter Preview, and Logs.
 */
@Composable
fun EnemyArtwork(
    assetId: String?,
    modifier: Modifier = Modifier,
    isBoss: Boolean = false,
    size: Dp = 64.dp
) {
    val def = VisualAssetResolver.resolve(assetId, VisualAssetCategory.ENEMY)
    val borderColor = if (isBoss) GameTheme.colors.accentMilitary else GameTheme.colors.danger

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(GameTheme.colors.surface)
            .border(if (isBoss) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = def.fallbackIcon,
            contentDescription = def.titleRu,
            tint = def.fallbackColor,
            modifier = Modifier.size(size * 0.55f)
        )

        if (isBoss) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(GameTheme.colors.accentMilitary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(9.dp)
                )
            }
        }
    }
}

/**
 * Standard Building Artwork Component for Settlement Grid and Building Info.
 */
@Composable
fun BuildingArtwork(
    building: Building,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val def = VisualAssetResolver.resolve(building.iconKey, VisualAssetCategory.BUILDING)
    val isLocked = building.status == BuildingStatus.LOCKED || building.status == BuildingStatus.AVAILABLE_TO_BUILD

    val borderColor = when {
        isSelected -> GameTheme.colors.primary
        isLocked -> GameTheme.colors.border
        building.isMaxLevel -> GameTheme.colors.accentWarm
        else -> GameTheme.colors.borderLight
    }

    val boxModifier = modifier
        .size(size)
        .clip(RoundedCornerShape(12.dp))
        .background(
            if (isLocked) GameTheme.colors.surface.copy(alpha = 0.5f)
            else GameTheme.colors.surfaceElevated
        )
        .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isLocked) Icons.Default.Lock else def.fallbackIcon,
            contentDescription = building.name,
            tint = if (isLocked) GameTheme.colors.textDisabled else def.fallbackColor,
            modifier = Modifier.size(size * 0.5f)
        )

        // Level badge in bottom right
        if (building.level > 0 && !isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GameTheme.colors.surface)
                    .border(1.dp, GameTheme.colors.border, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "L${building.level}",
                    style = GameTheme.typography.badgeText.copy(fontSize = 9.sp),
                    color = if (building.isMaxLevel) GameTheme.colors.accentWarm else GameTheme.colors.textPrimary
                )
            }
        }
    }
}

/**
 * Standard Faction Icon component.
 */
@Composable
fun FactionIcon(
    factionId: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color? = null
) {
    val def = VisualAssetResolver.resolve(factionId, VisualAssetCategory.FACTION_ICON)
    Icon(
        imageVector = def.fallbackIcon,
        contentDescription = def.titleRu,
        tint = tint ?: def.fallbackColor,
        modifier = modifier.size(size)
    )
}
