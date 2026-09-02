package com.example.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.content.visual.AssetPriority
import com.example.domain.content.visual.AssetStatus
import com.example.domain.content.visual.VisualAssetCategory
import com.example.domain.content.visual.VisualAssetDefinition
import com.example.domain.content.visual.VisualAssetRegistry
import com.example.domain.content.visual.VisualAssetValidator
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Interactive Visual Asset Browser and Testing Dashboard (Point 32).
 */
@Composable
fun VisualAssetBrowserScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<VisualAssetCategory?>(null) }
    var selectedStatus by remember { mutableStateOf<AssetStatus?>(null) }
    var selectedPriority by remember { mutableStateOf<AssetPriority?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var inspectingAsset by remember { mutableStateOf<VisualAssetDefinition?>(null) }

    val coverageReport = remember(VisualAssetResolver.forceMissingMode) {
        VisualAssetValidator.validate()
    }

    val allAssets = remember { VisualAssetRegistry.getAllDefinitions() }

    val filteredAssets = remember(selectedCategory, selectedStatus, selectedPriority, searchQuery) {
        allAssets.filter { asset ->
            (selectedCategory == null || asset.category == selectedCategory) &&
            (selectedStatus == null || asset.status == selectedStatus) &&
            (selectedPriority == null || asset.priority == selectedPriority) &&
            (searchQuery.isBlank() ||
                asset.assetId.contains(searchQuery, ignoreCase = true) ||
                asset.titleRu.contains(searchQuery, ignoreCase = true) ||
                asset.fileName.contains(searchQuery, ignoreCase = true) ||
                asset.tags.any { it.contains(searchQuery, ignoreCase = true) })
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WastelandDark)
            .padding(14.dp)
    ) {
        // Top Header with Navigation & Force Missing Fallback Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF24262E), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Реестр игровых ассетов (Пункт 32)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = "Всего: ${coverageReport.totalAssets} • Утверждено: ${coverageReport.approvedCount} (${coverageReport.approvedPercentage.toInt()}%)",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            // Force Fallback toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (VisualAssetResolver.forceMissingMode) "Тест заглушек ВКЛ" else "Заглушки",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (VisualAssetResolver.forceMissingMode) WarningAmber else TextMuted,
                        fontSize = 10.sp
                    )
                )
                Switch(
                    checked = VisualAssetResolver.forceMissingMode,
                    onCheckedChange = { VisualAssetResolver.forceMissingMode = it },
                    modifier = Modifier.scale(0.75f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск по ID, названию или тегу...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TechCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить", tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FrontierDarkSurfaceElevated,
                unfocusedContainerColor = FrontierDarkSurface,
                focusedBorderColor = TechCyan,
                unfocusedBorderColor = FrontierBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Все категории (${allAssets.size})", fontSize = 11.sp) }
                )
            }
            items(VisualAssetCategory.values()) { category ->
                val count = allAssets.count { it.category == category }
                if (count > 0) {
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = if (selectedCategory == category) null else category },
                        label = { Text("${category.titleRu} ($count)", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Priority & Status Sub-filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Text(
                        text = "Приоритет:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp),
                        modifier = Modifier.align(Alignment.CenterVertically).padding(end = 2.dp)
                    )
                }
                items(AssetPriority.values()) { prio ->
                    val isSel = selectedPriority == prio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) TechCyan.copy(alpha = 0.25f) else Color(0xFF1E2028))
                            .border(1.dp, if (isSel) TechCyan else Color(0xFF333A48), RoundedCornerShape(6.dp))
                            .clickable { selectedPriority = if (isSel) null else prio }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(prio.titleRu, style = MaterialTheme.typography.labelSmall.copy(color = if (isSel) TechCyan else TextSecondary, fontSize = 10.sp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Asset List
        if (filteredAssets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ассеты по заданным фильтрам не найдены",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredAssets, key = { it.assetId }) { asset ->
                    AssetCardRow(
                        asset = asset,
                        onClick = { inspectingAsset = asset }
                    )
                }
            }
        }
    }

    // Fullscreen Asset Inspector Dialog
    inspectingAsset?.let { asset ->
        AssetInspectorDialog(
            asset = asset,
            onDismiss = { inspectingAsset = null }
        )
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.padding(0.dp) // placeholder or standard scaling modifier
)

@Composable
private fun AssetCardRow(
    asset: VisualAssetDefinition,
    onClick: () -> Unit
) {
    GameCard(
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = if (asset.status == AssetStatus.APPROVED) SafeEmerald.copy(alpha = 0.5f) else FrontierBorder,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reusable Asset Graphic Preview
            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                when (asset.category) {
                    VisualAssetCategory.CHARACTER_PORTRAIT -> {
                        CharacterPortrait(portraitAssetId = asset.assetId, size = 60.dp)
                    }
                    VisualAssetCategory.LOCATION_HERO, VisualAssetCategory.LOCATION_THUMBNAIL, VisualAssetCategory.EVENT -> {
                        GameHeroImage(assetId = asset.assetId, height = 60.dp, showOverlay = false)
                    }
                    VisualAssetCategory.ITEM -> {
                        GameItemImage(assetId = asset.assetId, size = 56.dp)
                    }
                    VisualAssetCategory.VEHICLE -> {
                        VehicleArtwork(assetId = asset.assetId, size = 60.dp)
                    }
                    VisualAssetCategory.ENEMY -> {
                        EnemyArtwork(assetId = asset.assetId, size = 60.dp)
                    }
                    VisualAssetCategory.RESOURCE_ICON -> {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GameTheme.colors.surface)
                                .border(1.dp, GameTheme.colors.border, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(asset.fallbackIcon, contentDescription = null, tint = asset.fallbackColor, modifier = Modifier.size(32.dp))
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GameTheme.colors.surface)
                                .border(1.dp, GameTheme.colors.border, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(asset.fallbackIcon, contentDescription = null, tint = asset.fallbackColor, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            // Metadata Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = asset.titleRu,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (asset.priority) {
                                    AssetPriority.A -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    AssetPriority.B -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    AssetPriority.C -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = asset.priority.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = when (asset.priority) {
                                    AssetPriority.A -> Color(0xFFEF4444)
                                    AssetPriority.B -> Color(0xFFF59E0B)
                                    AssetPriority.C -> Color(0xFF38BDF8)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Text(
                    text = "ID: ${asset.assetId} • ${asset.fileName}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TechCyan,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = "${asset.category.titleRu} • ${asset.recommendedResolution} (${asset.aspectRatio})" +
                        if (asset.hasTransparency) " • Прозрачный" else "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Подробнее",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AssetInspectorDialog(
    asset: VisualAssetDefinition,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = asset.titleRu,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(asset.status.badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = asset.status.titleRu,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = asset.status.badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                Text(
                    text = "ID: ${asset.assetId}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TechCyan, fontSize = 11.sp)
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large Visual Preview
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (asset.category) {
                            VisualAssetCategory.LOCATION_HERO, VisualAssetCategory.EVENT -> {
                                GameHeroImage(assetId = asset.assetId, height = 130.dp)
                            }
                            VisualAssetCategory.CHARACTER_PORTRAIT -> {
                                CharacterPortrait(portraitAssetId = asset.assetId, size = 96.dp)
                            }
                            VisualAssetCategory.VEHICLE -> {
                                VehicleArtwork(assetId = asset.assetId, size = 96.dp)
                            }
                            VisualAssetCategory.ENEMY -> {
                                EnemyArtwork(assetId = asset.assetId, size = 96.dp)
                            }
                            else -> {
                                GameItemImage(assetId = asset.assetId, size = 80.dp)
                            }
                        }
                    }
                }

                // Tech specs
                item {
                    GameCard(backgroundColor = Color(0xFF161B26), borderColor = FrontierBorder) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Файл: ${asset.fileName}", style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp))
                            Text("Разрешение: ${asset.recommendedResolution} (${asset.aspectRatio})", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
                            Text("Прозрачность: ${if (asset.hasTransparency) "Да (PNG/WebP Alpha)" else "Нет (Непрозрачный)"}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
                            if (asset.screenUsage.isNotEmpty()) {
                                Text("Экраны использования: ${asset.screenUsage.joinToString(", ")}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
                            }
                            if (asset.tags.isNotEmpty()) {
                                Text("Теги: ${asset.tags.joinToString(", ")}", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp))
                            }
                        }
                    }
                }

                // AI Generation English Prompt
                if (asset.englishPrompt.isNotBlank()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Промпт для генерации (English):",
                                    style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontWeight = FontWeight.Bold)
                                )
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(asset.englishPrompt))
                                        copied = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(if (copied) Icons.Default.Check else Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp), tint = TechCyan)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (copied) "Скопировано!" else "Копировать", fontSize = 11.sp, color = TechCyan)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0D1117))
                                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = asset.englishPrompt,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE6EDF3), fontSize = 11.sp, lineHeight = 15.sp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Закрыть", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = FrontierDarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp)
    )
}
