package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.*
import com.example.ui.theme.*
import kotlin.math.sqrt

enum class MapFilterCategory(val titleRu: String) {
    ALL("Все точки"),
    SAFE("Безопасные"),
    RESOURCES("Ресурсы"),
    DANGEROUS("Опасные зоны"),
    SPECIAL("Бункеры и спец.")
}

/**
 * Interactive 2D Tactical World Map rendered with Jetpack Compose Canvas.
 * Supports smooth Pan, Pinch-to-Zoom, Tap Selection, Procedural Post-Apoc Terrain,
 * Road Networks, Radar Rings, Dynamic Trajectories, and Custom Markers.
 */
@Composable
fun InteractiveWorldMap(
    locations: List<Location>,
    selectedLocationId: String?,
    onSelectLocation: (Location) -> Unit,
    scale: Float,
    panOffset: Offset,
    onTransform: (zoomChange: Float, panChange: Offset) -> Unit,
    activeTravel: TravelState? = null,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Pulse animation for selected target and player base beacon
    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_progress"
    )
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash_phase"
    )

    val playerBase = locations.find { it.isPlayerBase } ?: locations.firstOrNull()
    val selectedLoc = locations.find { it.id == selectedLocationId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(FrontierDarkBackground)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onTransform(zoom, pan)
                }
            }
            .pointerInput(locations, scale, panOffset) {
                detectTapGestures { tapOffset ->
                    val w = size.width
                    val h = size.height
                    val worldWidth = w * scale
                    val worldHeight = h * scale
                    val originX = panOffset.x + (w - worldWidth) / 2f
                    val originY = panOffset.y + (h - worldHeight) / 2f

                    // Hit test locations
                    var clickedLocation: Location? = null
                    val hitRadiusPx = 36.dp.toPx()

                    for (loc in locations) {
                        val nodeScreenX = originX + loc.coordinateX * worldWidth
                        val nodeScreenY = originY + loc.coordinateY * worldHeight
                        val dx = tapOffset.x - nodeScreenX
                        val dy = tapOffset.y - nodeScreenY
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist <= hitRadiusPx) {
                            clickedLocation = loc
                            break
                        }
                    }

                    if (clickedLocation != null) {
                        onSelectLocation(clickedLocation)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val worldWidth = w * scale
            val worldHeight = h * scale
            val originX = panOffset.x + (w - worldWidth) / 2f
            val originY = panOffset.y + (h - worldHeight) / 2f

            // 1. Draw World Background & Atmospheric Post-Apoc Terrain
            drawWorldTerrain(
                originX = originX,
                originY = originY,
                worldWidth = worldWidth,
                worldHeight = worldHeight
            )

            // 2. Draw Sector Tactical Grid & Coordinate Marks
            drawSectorGrid(
                originX = originX,
                originY = originY,
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                textMeasurer = textMeasurer
            )

            // 3. Draw Road / Track Network between Outpost and surrounding nodes
            drawRoadNetwork(
                locations = locations,
                originX = originX,
                originY = originY,
                worldWidth = worldWidth,
                worldHeight = worldHeight
            )

            // 4. Draw Radar Range Rings from Player Base
            if (playerBase != null) {
                val baseX = originX + playerBase.coordinateX * worldWidth
                val baseY = originY + playerBase.coordinateY * worldHeight
                drawRadarRings(
                    centerX = baseX,
                    centerY = baseY,
                    worldHeight = worldHeight,
                    pulseProgress = pulseProgress,
                    textMeasurer = textMeasurer
                )
            }

            // 5. Draw Active Route Trajectory to Selected Location
            if (playerBase != null && selectedLoc != null && selectedLoc.id != playerBase.id) {
                val startX = originX + playerBase.coordinateX * worldWidth
                val startY = originY + playerBase.coordinateY * worldHeight
                val endX = originX + selectedLoc.coordinateX * worldWidth
                val endY = originY + selectedLoc.coordinateY * worldHeight

                drawRouteTrajectory(
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    selectedLoc = selectedLoc,
                    dashPhase = dashPhase,
                    textMeasurer = textMeasurer
                )
            }

            // 5b. Draw Active Travel Party moving along route
            if (activeTravel != null && activeTravel.isActiveTravel) {
                val fromLoc = locations.find { it.id == activeTravel.fromLocationId } ?: playerBase
                val toLoc = locations.find { it.id == activeTravel.toLocationId }
                if (fromLoc != null && toLoc != null) {
                    val sX = originX + fromLoc.coordinateX * worldWidth
                    val sY = originY + fromLoc.coordinateY * worldHeight
                    val eX = originX + toLoc.coordinateX * worldWidth
                    val eY = originY + toLoc.coordinateY * worldHeight

                    drawActiveTravelingParty(
                        start = Offset(sX, sY),
                        end = Offset(eX, eY),
                        travel = activeTravel,
                        pulseProgress = pulseProgress,
                        textMeasurer = textMeasurer
                    )
                }
            }

            // 6. Draw Location Nodes & Markers
            locations.forEach { loc ->
                val nodeX = originX + loc.coordinateX * worldWidth
                val nodeY = originY + loc.coordinateY * worldHeight
                val isSelected = loc.id == selectedLocationId

                drawLocationNode(
                    location = loc,
                    center = Offset(nodeX, nodeY),
                    isSelected = isSelected,
                    pulseProgress = pulseProgress,
                    textMeasurer = textMeasurer,
                    scale = scale
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Canvas Drawing Helper Functions
// -------------------------------------------------------------

private fun DrawScope.drawWorldTerrain(
    originX: Float,
    originY: Float,
    worldWidth: Float,
    worldHeight: Float
) {
    // Base dark wasteland ground
    drawRect(
        color = Color(0xFF0C111A),
        topLeft = Offset(originX, originY),
        size = Size(worldWidth, worldHeight)
    )

    // Wasteland terrain gradient blotches
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF162232), Color.Transparent),
            center = Offset(originX + worldWidth * 0.5f, originY + worldHeight * 0.5f),
            radius = worldWidth * 0.55f
        ),
        center = Offset(originX + worldWidth * 0.5f, originY + worldHeight * 0.5f),
        radius = worldWidth * 0.55f
    )

    // Forest Biome Patch 1 (North-East: Pine Valley)
    val forestPath1 = Path().apply {
        moveTo(originX + worldWidth * 0.58f, originY + worldHeight * 0.22f)
        cubicTo(
            originX + worldWidth * 0.70f, originY + worldHeight * 0.15f,
            originX + worldWidth * 0.85f, originY + worldHeight * 0.35f,
            originX + worldWidth * 0.75f, originY + worldHeight * 0.48f
        )
        cubicTo(
            originX + worldWidth * 0.65f, originY + worldHeight * 0.52f,
            originX + worldWidth * 0.55f, originY + worldHeight * 0.35f,
            originX + worldWidth * 0.58f, originY + worldHeight * 0.22f
        )
        close()
    }
    drawPath(
        path = forestPath1,
        color = Color(0xFF0D2818).copy(alpha = 0.65f)
    )
    drawPath(
        path = forestPath1,
        color = Color(0xFF1B4332).copy(alpha = 0.4f),
        style = Stroke(width = 2f)
    )

    // River / Lake system (winding from North-West to South-East)
    val riverPath = Path().apply {
        moveTo(originX + worldWidth * 0.30f, originY + worldHeight * 0.05f)
        cubicTo(
            originX + worldWidth * 0.38f, originY + worldHeight * 0.20f,
            originX + worldWidth * 0.32f, originY + worldHeight * 0.40f,
            originX + worldWidth * 0.48f, originY + worldHeight * 0.56f
        )
        cubicTo(
            originX + worldWidth * 0.56f, originY + worldHeight * 0.68f,
            originX + worldWidth * 0.50f, originY + worldHeight * 0.85f,
            originX + worldWidth * 0.62f, originY + worldHeight * 0.98f
        )
    }
    // River Shoreline glow
    drawPath(
        path = riverPath,
        color = WaterCyan.copy(alpha = 0.15f),
        style = Stroke(width = 18f, cap = StrokeCap.Round)
    )
    // River Main water body
    drawPath(
        path = riverPath,
        color = Color(0xFF0E4A5C),
        style = Stroke(width = 7f, cap = StrokeCap.Round)
    )

    // Mountain Ridge (North-East corner near Bunker 42)
    val mountainPath = Path().apply {
        moveTo(originX + worldWidth * 0.78f, originY + worldHeight * 0.08f)
        lineTo(originX + worldWidth * 0.83f, originY + worldHeight * 0.16f)
        lineTo(originX + worldWidth * 0.88f, originY + worldHeight * 0.11f)
        lineTo(originX + worldWidth * 0.94f, originY + worldHeight * 0.22f)
        lineTo(originX + worldWidth * 0.97f, originY + worldHeight * 0.14f)
    }
    drawPath(
        path = mountainPath,
        color = Color(0xFF2C3E50).copy(alpha = 0.7f),
        style = Stroke(width = 3f, join = StrokeJoin.Miter)
    )

    // Industrial Ruins Ground Patch (South-West)
    drawRect(
        color = Color(0xFF221A1A).copy(alpha = 0.4f),
        topLeft = Offset(originX + worldWidth * 0.12f, originY + worldHeight * 0.65f),
        size = Size(worldWidth * 0.22f, worldHeight * 0.20f)
    )

    // Anomaly Zone Hazard Glow (West Sector)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(StoragePurple.copy(alpha = 0.25f), Color.Transparent),
            center = Offset(originX + worldWidth * 0.14f, originY + worldHeight * 0.44f),
            radius = worldWidth * 0.18f
        ),
        center = Offset(originX + worldWidth * 0.14f, originY + worldHeight * 0.44f),
        radius = worldWidth * 0.18f
    )
}

private fun DrawScope.drawSectorGrid(
    originX: Float,
    originY: Float,
    worldWidth: Float,
    worldHeight: Float,
    textMeasurer: TextMeasurer
) {
    val gridStroke = Color(0x1838BDF8)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)

    // 4x4 Grid lines
    val cols = 4
    val rows = 4
    val cellW = worldWidth / cols
    val cellH = worldHeight / rows

    for (c in 1 until cols) {
        val x = originX + c * cellW
        drawLine(
            color = gridStroke,
            start = Offset(x, originY),
            end = Offset(x, originY + worldHeight),
            strokeWidth = 1f,
            pathEffect = dashEffect
        )
    }
    for (r in 1 until rows) {
        val y = originY + r * cellH
        drawLine(
            color = gridStroke,
            start = Offset(originX, y),
            end = Offset(originX + worldWidth, y),
            strokeWidth = 1f,
            pathEffect = dashEffect
        )
    }

    // Outer tactical map border
    drawRect(
        color = FrontierBorderLight.copy(alpha = 0.5f),
        topLeft = Offset(originX, originY),
        size = Size(worldWidth, worldHeight),
        style = Stroke(width = 2f)
    )

    // Sector labels in cell corners (A1, A2, B1, B2...)
    val rowLetters = listOf("A", "B", "C", "D")
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val label = "${rowLetters[r]}-${c + 1}"
            val textLayout = textMeasurer.measure(
                text = label,
                style = TextStyle(
                    color = Color(0x3564748B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(originX + c * cellW + 8f, originY + r * cellH + 8f)
            )
        }
    }
}

private fun DrawScope.drawRoadNetwork(
    locations: List<Location>,
    originX: Float,
    originY: Float,
    worldWidth: Float,
    worldHeight: Float
) {
    val base = locations.find { it.isPlayerBase } ?: return
    val baseX = originX + base.coordinateX * worldWidth
    val baseY = originY + base.coordinateY * worldHeight

    val roadColor = Color(0x3594A3B8)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)

    locations.filter { !it.isPlayerBase && it.isUnlocked }.forEach { loc ->
        val locX = originX + loc.coordinateX * worldWidth
        val locY = originY + loc.coordinateY * worldHeight

        // Connected road track
        drawLine(
            color = roadColor,
            start = Offset(baseX, baseY),
            end = Offset(locX, locY),
            strokeWidth = 1.5f,
            pathEffect = dashEffect
        )
    }
}

private fun DrawScope.drawRadarRings(
    centerX: Float,
    centerY: Float,
    worldHeight: Float,
    pulseProgress: Float,
    textMeasurer: TextMeasurer
) {
    val radarStroke = Color(0x1F10B981)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

    val radius15km = worldHeight * 0.18f
    val radius30km = worldHeight * 0.34f
    val radius50km = worldHeight * 0.50f

    drawCircle(
        color = radarStroke,
        radius = radius15km,
        center = Offset(centerX, centerY),
        style = Stroke(width = 1f)
    )
    drawCircle(
        color = radarStroke,
        radius = radius30km,
        center = Offset(centerX, centerY),
        style = Stroke(width = 1f, pathEffect = dashEffect)
    )
    drawCircle(
        color = radarStroke,
        radius = radius50km,
        center = Offset(centerX, centerY),
        style = Stroke(width = 1f, pathEffect = dashEffect)
    )

    // Animated beacon pulse wave
    val animatedRadius = radius50km * pulseProgress
    drawCircle(
        color = SafeEmerald.copy(alpha = (1f - pulseProgress) * 0.25f),
        radius = animatedRadius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 1.5f)
    )

    // Range distance tags
    val tag15 = textMeasurer.measure(
        text = "15 км",
        style = TextStyle(color = Color(0x4010B981), fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
    )
    drawText(
        textLayoutResult = tag15,
        topLeft = Offset(centerX + 6f, centerY - radius15km - 10f)
    )

    val tag30 = textMeasurer.measure(
        text = "30 км",
        style = TextStyle(color = Color(0x4010B981), fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
    )
    drawText(
        textLayoutResult = tag30,
        topLeft = Offset(centerX + 6f, centerY - radius30km - 10f)
    )
}

private fun DrawScope.drawRouteTrajectory(
    start: Offset,
    end: Offset,
    selectedLoc: Location,
    dashPhase: Float,
    textMeasurer: TextMeasurer
) {
    val routeColor = when (selectedLoc.dangerLevel) {
        DangerLevel.SAFE -> SafeEmerald
        DangerLevel.LOW -> TechCyan
        DangerLevel.MODERATE -> WarningAmber
        DangerLevel.HIGH -> DangerCrimson
        DangerLevel.EXTREME -> MilitaryRed
        DangerLevel.UNKNOWN -> StoragePurple
    }

    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), -dashPhase)

    // Outer glow path
    drawLine(
        color = routeColor.copy(alpha = 0.2f),
        start = start,
        end = end,
        strokeWidth = 6f
    )

    // Animated dashed core path
    drawLine(
        color = routeColor,
        start = start,
        end = end,
        strokeWidth = 2.5f,
        pathEffect = dashEffect
    )

    // Midpoint distance waypoint tag
    val midX = (start.x + end.x) / 2f
    val midY = (start.y + end.y) / 2f

    drawCircle(
        color = FrontierDarkSurfaceElevated,
        radius = 16f,
        center = Offset(midX, midY)
    )
    drawCircle(
        color = routeColor,
        radius = 16f,
        center = Offset(midX, midY),
        style = Stroke(width = 1.5f)
    )

    val distText = textMeasurer.measure(
        text = "${selectedLoc.distanceKm}k",
        style = TextStyle(
            color = TextWhite,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    )
    drawText(
        textLayoutResult = distText,
        topLeft = Offset(midX - distText.size.width / 2f, midY - distText.size.height / 2f)
    )
}

private fun DrawScope.drawLocationNode(
    location: Location,
    center: Offset,
    isSelected: Boolean,
    pulseProgress: Float,
    textMeasurer: TextMeasurer,
    scale: Float
) {
    val isBase = location.isPlayerBase
    val isUnknown = location.isHiddenOrUnknown
    val isLocked = !location.isUnlocked

    val accentColor = when {
        isBase -> SafeEmerald
        isUnknown -> StoragePurple
        isLocked -> TextSubtle
        location.dangerLevel == DangerLevel.SAFE -> SafeEmerald
        location.dangerLevel == DangerLevel.LOW -> TechCyan
        location.dangerLevel == DangerLevel.MODERATE -> WarningAmber
        location.dangerLevel == DangerLevel.HIGH -> DangerCrimson
        location.dangerLevel == DangerLevel.EXTREME -> MilitaryRed
        else -> TechCyan
    }

    val baseRadius = if (isBase) 14f else if (isSelected) 12f else 9f

    // 1. Selection Brackets / Target Ring
    if (isSelected) {
        val targetRadius = baseRadius + 10f + 4f * pulseProgress
        drawCircle(
            color = accentColor.copy(alpha = 0.4f * (1f - pulseProgress * 0.5f)),
            radius = targetRadius,
            center = center,
            style = Stroke(width = 2f)
        )
        // Corner tactical brackets
        val bracketSize = 7f
        val bracketDist = baseRadius + 8f
        // Top-left
        drawLine(accentColor, Offset(center.x - bracketDist, center.y - bracketDist), Offset(center.x - bracketDist + bracketSize, center.y - bracketDist), 2f)
        drawLine(accentColor, Offset(center.x - bracketDist, center.y - bracketDist), Offset(center.x - bracketDist, center.y - bracketDist + bracketSize), 2f)
        // Bottom-right
        drawLine(accentColor, Offset(center.x + bracketDist, center.y + bracketDist), Offset(center.x + bracketDist - bracketSize, center.y + bracketDist), 2f)
        drawLine(accentColor, Offset(center.x + bracketDist, center.y + bracketDist), Offset(center.x + bracketDist, center.y + bracketDist - bracketSize), 2f)
    }

    // 2. Base / Node Outer Glow
    if (isBase) {
        drawCircle(
            color = SafeEmerald.copy(alpha = 0.25f),
            radius = baseRadius + 6f,
            center = center
        )
    }

    // 3. Main Marker Body
    if (isBase) {
        // Hexagonal / Diamond Outpost Shield
        val hexPath = Path().apply {
            moveTo(center.x, center.y - baseRadius)
            lineTo(center.x + baseRadius, center.y - baseRadius * 0.5f)
            lineTo(center.x + baseRadius, center.y + baseRadius * 0.5f)
            lineTo(center.x, center.y + baseRadius)
            lineTo(center.x - baseRadius, center.y + baseRadius * 0.5f)
            lineTo(center.x - baseRadius, center.y - baseRadius * 0.5f)
            close()
        }
        drawPath(path = hexPath, color = FrontierDarkSurfaceElevated)
        drawPath(path = hexPath, color = SafeEmerald, style = Stroke(width = 2.5f))
        // Center emerald beacon point
        drawCircle(color = SafeEmerald, radius = 4f, center = center)
    } else {
        // Circular POI node
        drawCircle(
            color = FrontierDarkSurfaceElevated,
            radius = baseRadius,
            center = center
        )
        drawCircle(
            color = accentColor,
            radius = baseRadius,
            center = center,
            style = Stroke(width = if (isSelected) 2.5f else 1.5f)
        )
        // Inner core
        drawCircle(
            color = if (isUnknown) StoragePurple else if (isLocked) FrontierBorder else accentColor,
            radius = baseRadius * 0.45f,
            center = center
        )
    }

    // 4. Status Indicator Badge (Lock icon dot or Hazard dot)
    if (isLocked && !isUnknown) {
        drawCircle(
            color = WarningAmber,
            radius = 3.5f,
            center = Offset(center.x + baseRadius * 0.7f, center.y - baseRadius * 0.7f)
        )
    }

    // 5. Text Label below marker
    val nameText = if (isBase) "АВАНПОСТ"
    else if (isUnknown) "? Зона"
    else location.name.take(14)

    val labelLayout = textMeasurer.measure(
        text = nameText,
        style = TextStyle(
            color = if (isSelected) TextWhite else if (isBase) SafeEmerald else TextMuted,
            fontSize = if (isBase) 10.sp else 9.sp,
            fontWeight = if (isSelected || isBase) FontWeight.Bold else FontWeight.Medium
        )
    )

    // Pill background for label
    val labelWidth = labelLayout.size.width + 12f
    val labelHeight = labelLayout.size.height + 4f
    val labelTopLeft = Offset(center.x - labelWidth / 2f, center.y + baseRadius + 4f)

    drawRoundRect(
        color = FrontierDarkBackground.copy(alpha = 0.85f),
        topLeft = labelTopLeft,
        size = Size(labelWidth, labelHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = if (isSelected) accentColor.copy(alpha = 0.6f) else FrontierBorder.copy(alpha = 0.5f),
        topLeft = labelTopLeft,
        size = Size(labelWidth, labelHeight),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 0.8f)
    )
    drawText(
        textLayoutResult = labelLayout,
        topLeft = Offset(center.x - labelLayout.size.width / 2f, center.y + baseRadius + 6f)
    )
}

// -------------------------------------------------------------
// Floating Map HUD & Controls Overlay
// -------------------------------------------------------------

@Composable
fun MapControlsOverlay(
    activeFilter: MapFilterCategory,
    onFilterChange: (MapFilterCategory) -> Unit,
    totalLocationsCount: Int,
    unlockedLocationsCount: Int,
    onCenterOnBase: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onShowLegend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top HUD: Sector Header & Filter Row
        Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = FrontierDarkSurfaceElevated.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorderLight),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(TechCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = TechCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Сектор 7: Долина Ветров",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            Text(
                                text = "Радар активен • $unlockedLocationsCount/$totalLocationsCount точек разведано",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSubtle,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onShowLegend,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_map_legend")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Легенда карты",
                            tint = TechCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MapFilterCategory.values()) { category ->
                    val isSelected = category == activeFilter
                    Surface(
                        color = if (isSelected) TechCyan else FrontierDarkSurfaceElevated.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) TechCyan else FrontierBorder
                        ),
                        modifier = Modifier.clickable { onFilterChange(category) }
                    ) {
                        Text(
                            text = category.titleRu,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FrontierDarkBackground else TextWhite,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Floating Action Buttons on Right Edge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Zoom In
                SmallFloatingActionButton(
                    onClick = onZoomIn,
                    containerColor = FrontierDarkSurfaceElevated,
                    contentColor = TextWhite,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, FrontierBorder, CircleShape)
                        .testTag("btn_map_zoom_in")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Приблизить", modifier = Modifier.size(18.dp))
                }

                // Zoom Out
                SmallFloatingActionButton(
                    onClick = onZoomOut,
                    containerColor = FrontierDarkSurfaceElevated,
                    contentColor = TextWhite,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, FrontierBorder, CircleShape)
                        .testTag("btn_map_zoom_out")
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Отдалить", modifier = Modifier.size(18.dp))
                }

                // Center on Player Base
                FloatingActionButton(
                    onClick = onCenterOnBase,
                    containerColor = SafeEmerald,
                    contentColor = FrontierOnPrimary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("btn_map_center_base")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "К поселению",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Map Legend Dialog
// -------------------------------------------------------------

@Composable
fun MapLegendDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FrontierDarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorderLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Обозначения на карте",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextSubtle, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Уровни опасности зон:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSubtle,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                LegendRow(color = SafeEmerald, title = "Безопасно", desc = "Аванпост и торговые караваны")
                LegendRow(color = TechCyan, title = "Низкая угроза", desc = "Фермы, старые станции (до 15 км)")
                LegendRow(color = WarningAmber, title = "Умеренная", desc = "Посёлки и чащи (15–25 км)")
                LegendRow(color = DangerCrimson, title = "Высокая", desc = "Склады, промзоны, банды рейдеров")
                LegendRow(color = MilitaryRed, title = "Смертельная", desc = "Запечатанные военные бункеры")
                LegendRow(color = StoragePurple, title = "Неизведанно", desc = "Аномалии и туман войны (?)")

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Подсказка по управлению:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSubtle,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Перемещение пальцем по карте.\n• Масштабирование щипком (двумя пальцами) или кнопками +/-.\n• Нажмите на точку интереса для изучения разведданных и маршрута.\n• Кнопка с прицелом быстро вернёт камеру к вашей базе.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = FrontierDarkBackground),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Понятно", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$title — ",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 11.sp
            )
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontSize = 11.sp
            )
        )
    }
}

private fun DrawScope.drawActiveTravelingParty(
    start: Offset,
    end: Offset,
    travel: TravelState,
    pulseProgress: Float,
    textMeasurer: TextMeasurer
) {
    val t = travel.progressFraction.coerceIn(0f, 1f)
    val currentX = start.x + (end.x - start.x) * t
    val currentY = start.y + (end.y - start.y) * t
    val partyPos = Offset(currentX, currentY)

    // 1. Draw solid completed trail behind the party
    drawLine(
        color = TechCyan.copy(alpha = 0.8f),
        start = start,
        end = partyPos,
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // 2. Draw dashed remaining path ahead
    drawLine(
        color = WarningAmber.copy(alpha = 0.5f),
        start = partyPos,
        end = end,
        strokeWidth = 2.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
        cap = StrokeCap.Round
    )

    // 3. Pulsing radar ring around the traveling party
    val pulseRadius = 14f + pulseProgress * 22f
    val pulseAlpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.7f
    drawCircle(
        color = TechCyan.copy(alpha = pulseAlpha),
        radius = pulseRadius,
        center = partyPos,
        style = Stroke(width = 2f)
    )

    // 4. Party token circle
    drawCircle(
        color = FrontierDarkSurfaceElevated,
        radius = 12f,
        center = partyPos
    )
    drawCircle(
        color = TechCyan,
        radius = 12f,
        center = partyPos,
        style = Stroke(width = 2.5f)
    )

    // Inner bright core
    drawCircle(
        color = TechCyan,
        radius = 5f,
        center = partyPos
    )

    // 5. Party progress tag label
    val tagText = textMeasurer.measure(
        text = "${travel.progressPercent}%",
        style = TextStyle(
            color = TechCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    )
    drawText(
        textLayoutResult = tagText,
        topLeft = Offset(currentX - tagText.size.width / 2f, currentY + 16f)
    )
}

