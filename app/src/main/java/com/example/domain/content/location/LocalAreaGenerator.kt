package com.example.domain.content.location

import com.example.domain.content.core.WeightedSelector
import com.example.domain.model.LocalArea
import kotlin.random.Random

/**
 * Generator for local areas within a location, combining mandatory areas with a weighted optional pool.
 */
object LocalAreaGenerator {

    fun generateAreas(
        locationId: String,
        template: LocationTemplate,
        random: Random
    ): List<LocalArea> {
        val result = mutableListOf<LocalArea>()

        // 1. Instantiate mandatory areas
        template.mandatoryAreas.forEachIndexed { index, areaTmpl ->
            result.add(
                LocalArea(
                    id = "${locationId}_a${index + 1}",
                    name = areaTmpl.namePatternRu,
                    typeRu = areaTmpl.typeNameRu,
                    isDiscovered = true,
                    isExplored = false
                )
            )
        }

        // 2. Select optional areas from pool
        val optionalPool = template.optionalAreaPool
        if (optionalPool.isNotEmpty()) {
            val countToPick = if (template.minOptionalAreas >= template.maxOptionalAreas) {
                template.minOptionalAreas
            } else {
                random.nextInt(template.minOptionalAreas, template.maxOptionalAreas + 1)
            }

            val picked = WeightedSelector.selectMultipleWithoutReplacement(
                candidates = optionalPool,
                count = countToPick,
                weightExtractor = { it.weight },
                random = random
            )

            picked.forEachIndexed { index, areaTmpl ->
                val nextIdx = result.size + 1
                result.add(
                    LocalArea(
                        id = "${locationId}_a$nextIdx",
                        name = areaTmpl.namePatternRu,
                        typeRu = areaTmpl.typeNameRu,
                        isDiscovered = true,
                        isExplored = false
                    )
                )
            }
        }

        return result
    }
}
