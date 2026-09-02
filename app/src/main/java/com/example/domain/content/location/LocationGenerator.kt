package com.example.domain.content.location

import com.example.domain.content.core.ContentGenerationContext
import com.example.domain.content.core.GameRandomProvider
import com.example.domain.content.core.GenerationResult
import com.example.domain.content.core.WeightedSelector
import com.example.domain.content.registry.GameContentRegistry
import com.example.domain.model.*
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Procedural generator for world map locations.
 * Places locations with non-overlapping coordinates, generated sub-areas,
 * calculated danger levels, and thematic descriptions.
 */
object LocationGenerator {

    private const val MIN_COORDINATE = 0.12f
    private const val MAX_COORDINATE = 0.88f
    private const val BASE_COORDINATE_X = 0.50f
    private const val BASE_COORDINATE_Y = 0.50f
    private const val MIN_SPACING_BETWEEN_LOCATIONS = 0.10f

    /**
     * Generates a new Location instance from a template and context.
     */
    fun generateLocation(
        template: LocationTemplate,
        context: ContentGenerationContext,
        existingLocations: List<Location> = emptyList(),
        customIndex: Int? = null
    ): GenerationResult<Location> {
        val index = customIndex ?: context.generationIndex
        val seed = GameRandomProvider.deriveSeed(context.gameSeed, "location", template.id, index)
        val random = Random(seed)

        val locId = "loc_gen_${template.type.name.lowercase()}_$index"

        // 1. Generate Coordinates avoiding existing locations
        val (coordX, coordY) = generateCoordinates(random, existingLocations)

        // 2. Calculate Distance in Km from base (0.5, 0.5)
        val distRatio = hypot(coordX - BASE_COORDINATE_X, coordY - BASE_COORDINATE_Y)
        val distanceKm = (template.minDistanceKm + (distRatio * (template.maxDistanceKm - template.minDistanceKm) * 2f).toInt())
            .coerceIn(template.minDistanceKm, template.maxDistanceKm)

        // 3. Danger Level
        val dangerOptions = DangerLevel.values().filter {
            it.rating >= template.minDangerLevel.rating && it.rating <= template.maxDangerLevel.rating
        }
        val chosenDanger = if (dangerOptions.isNotEmpty()) {
            dangerOptions[random.nextInt(dangerOptions.size)]
        } else {
            template.minDangerLevel
        }

        // 4. Terrain Type
        val chosenTerrain = if (template.allowedTerrains.isNotEmpty()) {
            template.allowedTerrains.toList()[random.nextInt(template.allowedTerrains.size)]
        } else {
            TerrainType.WASTELAND
        }

        // 5. Name and Description
        val locName = LocationNameGenerator.generateName(template, random)
        val desc = if (template.descriptionTemplates.isNotEmpty()) {
            template.descriptionTemplates[random.nextInt(template.descriptionTemplates.size)]
        } else {
            "Заброшенный сектор пустоши с сохранившимися постройками."
        }

        // 6. Sub-areas
        val localAreas = LocalAreaGenerator.generateAreas(locId, template, random)

        // 7. Estimated Loot
        val dangerFactor = chosenDanger.rating + 1
        val estMaterials = (15 * dangerFactor) + random.nextInt(20)
        val estCredits = (25 * dangerFactor) + random.nextInt(35)
        val estFood = (10 * dangerFactor) + random.nextInt(15)
        val estFuel = (8 * dangerFactor) + random.nextInt(12)

        // 8. Sector code
        val sectorCode = "SEC-${(index + 10).toString().padStart(2, '0')}"

        val visualAsset = if (template.visualAssetPool.isNotEmpty()) {
            template.visualAssetPool[random.nextInt(template.visualAssetPool.size)]
        } else {
            "loc_station"
        }

        val location = Location(
            id = locId,
            name = locName,
            type = template.type,
            dangerLevel = chosenDanger,
            isUnlocked = true,
            status = LocationStatus.AVAILABLE,
            distanceKm = distanceKm,
            potentialLoot = if (template.potentialLootKeywordsRu.isNotEmpty()) template.potentialLootKeywordsRu else listOf("Припасы", "Утиль"),
            description = desc,
            estimatedLootMaterials = estMaterials,
            estimatedLootCredits = estCredits,
            estimatedLootFood = estFood,
            estimatedLootFuel = estFuel,
            coordinateX = coordX,
            coordinateY = coordY,
            terrainType = chosenTerrain,
            isPlayerBase = false,
            sectorCode = sectorCode,
            visualAssetId = visualAsset,
            observations = if (template.observationTemplatesRu.isNotEmpty()) template.observationTemplatesRu else listOf("Признаков активного наблюдения не обнаружено"),
            threats = if (template.threatTemplatesRu.isNotEmpty()) template.threatTemplatesRu else listOf("Нестабильная радиационная обстановка"),
            localAreas = localAreas
        )

        return GenerationResult.Success(location)
    }

    /**
     * Selects a template using weights and generates a new location.
     */
    fun generateRandomLocation(
        context: ContentGenerationContext,
        existingLocations: List<Location> = emptyList(),
        registry: GameContentRegistry = GameContentRegistry
    ): GenerationResult<Location> {
        val candidates = registry.locationTemplates.values.filter { tmpl ->
            // Filter by context danger or tags if specified
            tmpl.minDangerLevel.rating <= context.dangerLevel.rating + 1 &&
                    tmpl.maxDangerLevel.rating >= context.dangerLevel.rating - 1
        }

        if (candidates.isEmpty()) {
            return GenerationResult.NoEligibleContent("No location templates match context")
        }

        val random = GameRandomProvider.createRandom(context.gameSeed, "location_select", context.generationIndex)
        val selectedTemplate = WeightedSelector.select(
            candidates = candidates,
            weightExtractor = { it.baseWeight },
            random = random,
            context = context
        ) ?: candidates.first()

        return generateLocation(selectedTemplate, context, existingLocations)
    }

    private fun generateCoordinates(random: Random, existingLocations: List<Location>): Pair<Float, Float> {
        var bestX = random.nextFloat() * (MAX_COORDINATE - MIN_COORDINATE) + MIN_COORDINATE
        var bestY = random.nextFloat() * (MAX_COORDINATE - MIN_COORDINATE) + MIN_COORDINATE
        var maxMinDist = 0f

        // Try up to 15 candidates to maximize distance from other locations
        for (attempt in 0 until 15) {
            val candX = random.nextFloat() * (MAX_COORDINATE - MIN_COORDINATE) + MIN_COORDINATE
            val candY = random.nextFloat() * (MAX_COORDINATE - MIN_COORDINATE) + MIN_COORDINATE

            val distFromBase = hypot(candX - BASE_COORDINATE_X, candY - BASE_COORDINATE_Y)
            if (distFromBase < 0.12f) continue // Too close to base

            val minDistToOthers = existingLocations.minOfOrNull {
                hypot(candX - it.coordinateX, candY - it.coordinateY)
            } ?: Float.MAX_VALUE

            if (minDistToOthers > MIN_SPACING_BETWEEN_LOCATIONS) {
                return Pair(candX, candY)
            }

            if (minDistToOthers > maxMinDist) {
                maxMinDist = minDistToOthers
                bestX = candX
                bestY = candY
            }
        }

        return Pair(bestX, bestY)
    }
}
