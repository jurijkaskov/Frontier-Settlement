package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DayPeriod
import com.example.domain.model.Settlement
import com.example.ui.theme.*

/**
 * Atmospheric 2D Multi-layered Settlement Scene.
 *
 * Visually communicates:
 * - Dynamic sky gradient and lighting reflecting current [DayPeriod] (Morning, Day, Evening, Night);
 * - Distant wasteland mountain ridges and radio silhouettes;
 * - Guard observation tower with an animated searchlight beacon;
 * - Primary command habitat and residential modular domes;
 * - Storage hangar facility with supply bay;
 * - Engineering workshop with warm glowing windows & forge smoke;
 * - Wind turbine generating power;
 * - Dust/sand wasteland ground with perimeter fence, access road and light poles;
 * - Interactive tap to inspect outpost status.
 */
@Composable
fun SettlementScene(
    settlement: Settlement,
    onSceneClick: () -> Unit,
    dayPeriod: DayPeriod = DayPeriod.DAY,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "settlement_ambient")

    // Beacon / Searchlight rotation animation
    val searchlightAngle by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "searchlight"
    )

    // Tower beacon red blinking pulse
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_pulse"
    )

    // Workshop smoke and sparks drift
    val smokeDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smoke"
    )

    // Wind turbine blade rotation
    val turbineRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "turbine"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FrontierDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, SafeEmerald.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable { onSceneClick() }
            .testTag("settlement_scene_canvas")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 1: Atmospheric Canvas Scene
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawSettlementLandscape(
                    searchlightAngle = searchlightAngle,
                    beaconPulse = beaconPulse,
                    smokeDrift = smokeDrift,
                    turbineRotation = turbineRotation,
                    dayPeriod = dayPeriod
                )
            }

            // Layer 2: Top Tactical Overlay Badges (Status pills)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Outpost Name Pill
                Surface(
                    color = Color(0xDD090D14),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.6f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SafeEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "СЕКТОР: ${settlement.name.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                letterSpacing = 0.8.sp,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Interactive Hint Badge
                Surface(
                    color = Color(0xDD090D14),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Осмотр базы",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Layer 3: Bottom Building Identification Markers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xEE090D14))
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BuildingMiniMarker(name = "Штаб и Жильё", color = FoodGreen)
                BuildingMiniMarker(name = "Склад", color = StoragePurple)
                BuildingMiniMarker(name = "Мастерская", color = MaterialsOrange)
                BuildingMiniMarker(name = "Вышка", color = DangerCrimson)
            }
        }
    }
}

@Composable
private fun BuildingMiniMarker(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp
            )
        )
    }
}

/**
 * Draws the layered 2D post-apocalyptic settlement landscape with canvas primitives.
 */
private fun DrawScope.drawSettlementLandscape(
    searchlightAngle: Float,
    beaconPulse: Float,
    smokeDrift: Float,
    turbineRotation: Float,
    dayPeriod: DayPeriod = DayPeriod.DAY
) {
    val width = size.width
    val height = size.height

    // 1. Wasteland Sky Gradient tailored by DayPeriod
    val skyColors = when (dayPeriod) {
        DayPeriod.MORNING -> listOf(
            Color(0xFF1E293B), // Indigo dawn
            Color(0xFF473335), // Rose haze
            Color(0xFF7A4E3A), // Amber horizon
            Color(0xFF8F6849)  // Warm morning sand
        )
        DayPeriod.DAY -> listOf(
            Color(0xFF1E3A5F), // Clear wasteland blue
            Color(0xFF2C5364), // Dusty teal
            Color(0xFF486581), // Light blue grey
            Color(0xFF6B5E4E)  // Sandy horizon
        )
        DayPeriod.EVENING -> listOf(
            Color(0xFF1A1423), // Deep violet dusk
            Color(0xFF3D1E3A), // Crimson haze
            Color(0xFF6A2E35), // Fiery sunset
            Color(0xFF8D4A38)  // Burning desert horizon
        )
        DayPeriod.NIGHT -> listOf(
            Color(0xFF060913), // Pitch obsidian
            Color(0xFF0C1322), // Deep navy
            Color(0xFF152238), // Night sky
            Color(0xFF1C2836)  // Cold horizon
        )
    }

    drawRect(
        brush = Brush.verticalGradient(
            colors = skyColors,
            startY = 0f,
            endY = height * 0.65f
        ),
        size = Size(width, height)
    )

    // 2. Distant Wasteland Mountain Ridges (Layer 1 - Far)
    val farMountainPath = Path().apply {
        moveTo(0f, height * 0.45f)
        lineTo(width * 0.2f, height * 0.35f)
        lineTo(width * 0.35f, height * 0.42f)
        lineTo(width * 0.55f, height * 0.32f)
        lineTo(width * 0.75f, height * 0.40f)
        lineTo(width, height * 0.34f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(
        path = farMountainPath,
        color = Color(0xFF162032).copy(alpha = 0.7f)
    )

    // 3. Middle Mountain Ridge (Layer 2)
    val midMountainPath = Path().apply {
        moveTo(0f, height * 0.52f)
        lineTo(width * 0.15f, height * 0.44f)
        lineTo(width * 0.4f, height * 0.50f)
        lineTo(width * 0.65f, height * 0.42f)
        lineTo(width * 0.88f, height * 0.48f)
        lineTo(width, height * 0.45f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(
        path = midMountainPath,
        color = Color(0xFF1B263B)
    )

    // 4. Wasteland Ground Terrain (Layer 3)
    val groundY = height * 0.62f
    val groundPath = Path().apply {
        moveTo(0f, groundY)
        quadraticBezierTo(width * 0.5f, groundY - 10f, width, groundY)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(
        path = groundPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF262D38), // Dusty soil
                Color(0xFF1A202C), // Dark perimeter ground
                Color(0xFF111722)
            ),
            startY = groundY,
            endY = height
        )
    )

    // 5. Dirt Road / Access Path through the settlement
    val roadPath = Path().apply {
        moveTo(width * 0.45f, groundY + 10f)
        lineTo(width * 0.52f, groundY + 10f)
        lineTo(width * 0.75f, height)
        lineTo(width * 0.35f, height)
        close()
    }
    drawPath(
        path = roadPath,
        color = Color(0xFF202936).copy(alpha = 0.9f)
    )

    // 6. Perimeter Security Fence & Barbed Wire
    for (i in 0..10) {
        val poleX = i * (width / 10f)
        val poleTopY = groundY - 14f
        // Fence pole
        drawLine(
            color = Color(0xFF475569),
            start = Offset(poleX, groundY + 4f),
            end = Offset(poleX, poleTopY),
            strokeWidth = 2f
        )
        // Horizontal wire
        if (i < 10) {
            drawLine(
                color = Color(0xFF334155),
                start = Offset(poleX, poleTopY + 3f),
                end = Offset(poleX + (width / 10f), poleTopY + 3f),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0xFF334155),
                start = Offset(poleX, poleTopY + 8f),
                end = Offset(poleX + (width / 10f), poleTopY + 8f),
                strokeWidth = 1f
            )
        }
    }

    // ----------------------------------------------------------------
    // 7. BUILDINGS & INFRASTRUCTURE
    // ----------------------------------------------------------------

    // 7.1 Observation Guard Tower (Left Side: X = 12% width)
    val towerBaseX = width * 0.12f
    val towerBaseY = groundY + 15f
    val towerHeight = 75f

    // Tower structural legs
    drawLine(
        color = Color(0xFF475569),
        start = Offset(towerBaseX - 14f, towerBaseY),
        end = Offset(towerBaseX - 6f, towerBaseY - towerHeight),
        strokeWidth = 3f
    )
    drawLine(
        color = Color(0xFF475569),
        start = Offset(towerBaseX + 14f, towerBaseY),
        end = Offset(towerBaseX + 6f, towerBaseY - towerHeight),
        strokeWidth = 3f
    )
    // Cross braces
    drawLine(
        color = Color(0xFF334155),
        start = Offset(towerBaseX - 12f, towerBaseY - 20f),
        end = Offset(towerBaseX + 8f, towerBaseY - 45f),
        strokeWidth = 1.5f
    )
    drawLine(
        color = Color(0xFF334155),
        start = Offset(towerBaseX + 12f, towerBaseY - 20f),
        end = Offset(towerBaseX - 8f, towerBaseY - 45f),
        strokeWidth = 1.5f
    )

    // Tower Observation Cabin
    val cabinY = towerBaseY - towerHeight - 16f
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(towerBaseX - 16f, cabinY),
        size = Size(32f, 18f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
    )
    // Cabin illuminated window
    drawRect(
        color = Color(0xFF38BDF8).copy(alpha = 0.8f),
        topLeft = Offset(towerBaseX - 10f, cabinY + 4f),
        size = Size(20f, 6f)
    )
    // Tower Roof
    val roofPath = Path().apply {
        moveTo(towerBaseX - 18f, cabinY)
        lineTo(towerBaseX, cabinY - 8f)
        lineTo(towerBaseX + 18f, cabinY)
        close()
    }
    drawPath(roofPath, Color(0xFF0F172A))

    // Red warning beacon on top of the tower
    val beaconY = cabinY - 9f
    drawCircle(
        color = DangerCrimson.copy(alpha = beaconPulse),
        radius = 4f,
        center = Offset(towerBaseX, beaconY)
    )
    drawCircle(
        color = DangerCrimson.copy(alpha = beaconPulse * 0.4f),
        radius = 10f,
        center = Offset(towerBaseX, beaconY)
    )

    // Animated Searchlight Cone from tower
    val lightStart = Offset(towerBaseX, cabinY + 8f)
    val lightLength = 120f
    val radAngle = Math.toRadians(searchlightAngle.toDouble() + 50.0)
    val lightEndCenter = Offset(
        (lightStart.x + Math.cos(radAngle) * lightLength).toFloat(),
        (lightStart.y + Math.sin(radAngle) * lightLength).toFloat()
    )

    val lightConePath = Path().apply {
        moveTo(lightStart.x, lightStart.y)
        lineTo(lightEndCenter.x - 25f, lightEndCenter.y)
        lineTo(lightEndCenter.x + 25f, lightEndCenter.y)
        close()
    }
    drawPath(
        path = lightConePath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x66FACC15),
                Color(0x22FACC15),
                Color.Transparent
            ),
            center = lightStart,
            radius = lightLength * 1.1f
        )
    )

    // 7.2 Main Command Habitat & Living Domes (Center: X = 32% width)
    val hqX = width * 0.32f
    val hqY = groundY + 18f

    // Habitat Dome (Left)
    drawArc(
        color = Color(0xFF1E293B),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(hqX - 35f, hqY - 32f),
        size = Size(40f, 38f)
    )
    // Habitat illuminated windows
    drawCircle(
        color = FoodGreen.copy(alpha = 0.85f),
        radius = 3.5f,
        center = Offset(hqX - 15f, hqY - 14f)
    )

    // Command Center Main Building (Right)
    drawRoundRect(
        color = Color(0xFF283548),
        topLeft = Offset(hqX - 8f, hqY - 42f),
        size = Size(48f, 44f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    // Command HQ windows
    drawRect(
        color = TechCyan.copy(alpha = 0.9f),
        topLeft = Offset(hqX - 2f, hqY - 36f),
        size = Size(36f, 10f)
    )
    drawRect(
        color = WarningAmber.copy(alpha = 0.8f),
        topLeft = Offset(hqX + 4f, hqY - 20f),
        size = Size(12f, 18f)
    )
    // Communications Dish & Antenna on HQ
    drawLine(
        color = Color(0xFF94A3B8),
        start = Offset(hqX + 16f, hqY - 42f),
        end = Offset(hqX + 16f, hqY - 60f),
        strokeWidth = 2f
    )
    drawCircle(
        color = TechCyan.copy(alpha = 0.7f),
        radius = 2.5f,
        center = Offset(hqX + 16f, hqY - 60f)
    )

    // 7.3 Storage Hangar Facility (X = 60% width)
    val hangarX = width * 0.60f
    val hangarY = groundY + 22f

    // Curved Hangar Structure
    val hangarPath = Path().apply {
        moveTo(hangarX - 28f, hangarY)
        quadraticBezierTo(hangarX, hangarY - 48f, hangarX + 28f, hangarY)
        close()
    }
    drawPath(hangarPath, Color(0xFF1A2333))
    drawPath(
        path = hangarPath,
        color = StoragePurple.copy(alpha = 0.4f),
        style = Stroke(width = 1.5f)
    )
    // Hangar Rolling Shutter Gate
    drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(hangarX - 14f, hangarY - 22f),
        size = Size(28f, 22f)
    )
    // Small purple marker light over hangar
    drawCircle(
        color = StoragePurple,
        radius = 3f,
        center = Offset(hangarX, hangarY - 26f)
    )

    // 7.4 Engineering Workshop & Forge (Right Side: X = 82% width)
    val wsX = width * 0.82f
    val wsY = groundY + 20f

    // Workshop block building
    drawRoundRect(
        color = Color(0xFF243247),
        topLeft = Offset(wsX - 22f, wsY - 34f),
        size = Size(44f, 36f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
    )
    // Warm glowing orange forge window
    drawRect(
        color = MaterialsOrange.copy(alpha = 0.9f),
        topLeft = Offset(wsX - 14f, wsY - 24f),
        size = Size(16f, 14f)
    )
    // Workshop Chimney
    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(wsX + 8f, wsY - 48f),
        size = Size(8f, 16f)
    )

    // Animated Smoke / Sparks from Chimney
    val chimneyTop = Offset(wsX + 12f, wsY - 50f)
    for (s in 0..2) {
        val sProgress = (smokeDrift + (s * 0.33f)) % 1f
        val smokeY = chimneyTop.y - (sProgress * 24f)
        val smokeX = chimneyTop.x + (Math.sin(sProgress * Math.PI * 2).toFloat() * 6f) - (sProgress * 8f)
        val smokeAlpha = (1f - sProgress) * 0.5f
        drawCircle(
            color = Color(0xFFCBD5E1).copy(alpha = smokeAlpha),
            radius = 3f + (sProgress * 5f),
            center = Offset(smokeX, smokeY)
        )
    }

    // 7.5 Wind Turbine Generator (Behind Workshop: X = 93% width)
    val turbX = width * 0.93f
    val turbY = groundY + 8f
    val turbPoleHeight = 65f

    // Turbine Pole
    drawLine(
        color = Color(0xFF64748B),
        start = Offset(turbX, turbY),
        end = Offset(turbX, turbY - turbPoleHeight),
        strokeWidth = 2.5f
    )
    // Turbine Rotor Center Hub
    val hubCenter = Offset(turbX, turbY - turbPoleHeight)
    drawCircle(
        color = Color(0xFF94A3B8),
        radius = 3.5f,
        center = hubCenter
    )
    // 3 Animated Rotating Blades
    val bladeLength = 22f
    for (b in 0..2) {
        val bladeAngleRad = Math.toRadians((turbineRotation + (b * 120f)).toDouble())
        val bladeEnd = Offset(
            (hubCenter.x + Math.cos(bladeAngleRad) * bladeLength).toFloat(),
            (hubCenter.y + Math.sin(bladeAngleRad) * bladeLength).toFloat()
        )
        drawLine(
            color = Color(0xFFE2E8F0).copy(alpha = 0.85f),
            start = hubCenter,
            end = bladeEnd,
            strokeWidth = 2f
        )
    }

    // 8. Ground Perimeter Floodlight Poles (Lighting the path)
    drawPerimeterLamp(lampX = width * 0.28f, groundY = groundY + 12f)
    drawPerimeterLamp(lampX = width * 0.72f, groundY = groundY + 14f)
}

/**
 * Draws a small defensive floodlight pole with a subtle ground glow.
 */
private fun DrawScope.drawPerimeterLamp(lampX: Float, groundY: Float) {
    val lampHeight = 22f
    drawLine(
        color = Color(0xFF475569),
        start = Offset(lampX, groundY),
        end = Offset(lampX, groundY - lampHeight),
        strokeWidth = 1.5f
    )
    drawCircle(
        color = SafeEmerald.copy(alpha = 0.8f),
        radius = 2.5f,
        center = Offset(lampX, groundY - lampHeight)
    )
    // Ground pool of light
    drawOval(
        color = SafeEmerald.copy(alpha = 0.12f),
        topLeft = Offset(lampX - 16f, groundY - 2f),
        size = Size(32f, 10f)
    )
}
