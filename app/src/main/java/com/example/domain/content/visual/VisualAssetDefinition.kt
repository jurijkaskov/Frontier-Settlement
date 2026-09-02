package com.example.domain.content.visual

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * High-level category for a game visual asset.
 * Used for fallback resolution, aspect ratio validation, and UI component selection.
 */
enum class VisualAssetCategory(val titleRu: String) {
    CHARACTER_PORTRAIT("Портрет персонажа"),
    LOCATION_HERO("Панорама локации"),
    LOCATION_THUMBNAIL("Миниатюра локации"),
    BUILDING("Здание поселения"),
    ITEM("Предмет инвентаря"),
    VEHICLE("Транспорт"),
    ENEMY("Противник"),
    EVENT("Событие вылазки"),
    MAP_MARKER("Маркер карты"),
    RESOURCE_ICON("Иконка ресурса"),
    STATUS_ICON("Иконка статуса / эффекта"),
    FACTION_ICON("Эмблема фракции"),
    UI_DECORATION("UI Декорация"),
    BACKGROUND("Фоновое изображение")
}

/**
 * Development & Production status for a visual asset.
 */
enum class AssetStatus(val titleRu: String, val badgeColor: Color) {
    MISSING("Отсутствует", Color(0xFFEF4444)),
    PLACEHOLDER("Заглушка", Color(0xFFF59E0B)),
    GENERATED_UNREVIEWED("Сгенерирован (на проверке)", Color(0xFF38BDF8)),
    APPROVED("Утверждён (финал)", Color(0xFF10B981)),
    DEPRECATED("Устарел", Color(0xFF64748B))
}

/**
 * Priority tier for asset creation and AI generation.
 */
enum class AssetPriority(val titleRu: String, val descriptionRu: String) {
    A("Приоритет A", "Критичный / часто видимый контент (стартовые экраны, персонажи, транспорт)"),
    B("Приоритет B", "Вторичный контент (дополнительные локации, фракции, технологии, события)"),
    C("Приоритет C", "Редкий контент (уникальные реликвии, редкие аномалии, расширенные варианты)")
}

/**
 * Data-driven definition of a visual game asset.
 * Decoupled from Android resource IDs so domain models remain clean and serializable.
 */
data class VisualAssetDefinition(
    val assetId: String,
    val fileName: String,
    val category: VisualAssetCategory,
    val status: AssetStatus = AssetStatus.PLACEHOLDER,
    val priority: AssetPriority = AssetPriority.A,
    val titleRu: String,
    val descriptionRu: String,
    val fallbackIcon: ImageVector = Icons.Default.Image,
    val fallbackColor: Color = Color(0xFF38BDF8),
    val recommendedResolution: String = "1280x720",
    val aspectRatio: String = "16:9",
    val hasTransparency: Boolean = false,
    val supportsTint: Boolean = false,
    val focalPointX: Float = 0.5f,
    val focalPointY: Float = 0.5f,
    val tags: List<String> = emptyList(),
    val screenUsage: List<String> = emptyList(),
    val englishPrompt: String = "",
    val fallbackAssetId: String? = null
)
