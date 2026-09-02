package com.example.domain.content.registry

/**
 * Diagnostic validation severity level.
 */
enum class ValidationSeverity {
    ERROR,
    WARNING,
    INFO
}

/**
 * Diagnostic issue identified by content validator.
 */
data class ContentValidationIssue(
    val severity: ValidationSeverity,
    val domain: String,
    val contentId: String,
    val message: String
)

/**
 * Complete report produced by [GameContentValidator].
 */
data class ContentValidationReport(
    val issues: List<ContentValidationIssue>,
    val totalPacksChecked: Int,
    val totalLocationsChecked: Int,
    val totalEventsChecked: Int,
    val totalEnemiesChecked: Int,
    val totalEncountersChecked: Int,
    val totalLootTablesChecked: Int,
    val totalArchetypesChecked: Int,
    val totalQuestsChecked: Int
) {
    val errorCount: Int get() = issues.count { it.severity == ValidationSeverity.ERROR }
    val warningCount: Int get() = issues.count { it.severity == ValidationSeverity.WARNING }
    val isValid: Boolean get() = errorCount == 0
}

/**
 * Development-time integrity validator that verifies all data-driven definitions,
 * cross-references, ID uniqueness, and balance bounds.
 */
object GameContentValidator {

    fun validateRegistry(registry: GameContentRegistry = GameContentRegistry): ContentValidationReport {
        val issues = mutableListOf<ContentValidationIssue>()

        val locations = registry.locationTemplates.values.toList()
        val events = registry.events.values.toList()
        val enemies = registry.enemyTemplates.values.toList()
        val encounters = registry.encounterTemplates.values.toList()
        val lootTables = registry.lootTables.values.toList()
        val archetypes = registry.characterArchetypes.values.toList()
        val quests = registry.repeatableQuestTemplates.values.toList()

        // 1. Check ID Duplicates
        checkIdUniqueness("Locations", locations.map { it.id }, issues)
        checkIdUniqueness("Events", events.map { it.id }, issues)
        checkIdUniqueness("Enemies", enemies.map { it.id }, issues)
        checkIdUniqueness("Encounters", encounters.map { it.id }, issues)
        checkIdUniqueness("LootTables", lootTables.map { it.id }, issues)
        checkIdUniqueness("Archetypes", archetypes.map { it.id }, issues)
        checkIdUniqueness("Quests", quests.map { it.id }, issues)

        // 2. Validate Location Templates and Area references
        for (loc in locations) {
            if (loc.minDangerLevel.rating > loc.maxDangerLevel.rating) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.ERROR,
                        "Location",
                        loc.id,
                        "minDangerLevel (${loc.minDangerLevel}) > maxDangerLevel (${loc.maxDangerLevel})"
                    )
                )
            }
            val allAreas = loc.mandatoryAreas + loc.optionalAreaPool
            for (area in allAreas) {
                if (area.lootTableId != null && !registry.lootTables.containsKey(area.lootTableId)) {
                    issues.add(
                        ContentValidationIssue(
                            ValidationSeverity.ERROR,
                            "Location/Area",
                            loc.id,
                            "Area '${area.id}' references non-existent lootTableId '${area.lootTableId}'"
                        )
                    )
                }
            }
        }

        // 3. Validate Encounters and Enemy References
        for (enc in encounters) {
            if (enc.minEnemies > enc.maxEnemies) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.ERROR,
                        "Encounter",
                        enc.id,
                        "minEnemies (${enc.minEnemies}) > maxEnemies (${enc.maxEnemies})"
                    )
                )
            }
            if (enc.enemyPool.isEmpty()) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.ERROR,
                        "Encounter",
                        enc.id,
                        "Encounter has an empty enemyPool"
                    )
                )
            }
            for (entry in enc.enemyPool) {
                if (!registry.enemyTemplates.containsKey(entry.enemyTemplateId)) {
                    issues.add(
                        ContentValidationIssue(
                            ValidationSeverity.ERROR,
                            "Encounter",
                            enc.id,
                            "Encounter references missing enemyTemplateId '${entry.enemyTemplateId}'"
                        )
                    )
                }
            }
            if (enc.lootTableId != null && !registry.lootTables.containsKey(enc.lootTableId)) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.ERROR,
                        "Encounter",
                        enc.id,
                        "Encounter references missing lootTableId '${enc.lootTableId}'"
                    )
                )
            }
        }

        // 4. Validate Enemy Templates
        for (enemy in enemies) {
            if (enemy.baseHp <= 0) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.ERROR,
                        "Enemy",
                        enemy.id,
                        "baseHp must be > 0 (found ${enemy.baseHp})"
                    )
                )
            }
            if (enemy.baseAttack <= 0) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.ERROR,
                        "Enemy",
                        enemy.id,
                        "baseAttack must be > 0 (found ${enemy.baseAttack})"
                    )
                )
            }
        }

        // 5. Validate Loot Tables
        for (loot in lootTables) {
            if (loot.minCredits > loot.maxCredits) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.ERROR,
                        "LootTable",
                        loot.id,
                        "minCredits (${loot.minCredits}) > maxCredits (${loot.maxCredits})"
                    )
                )
            }
            if (loot.resourceEntries.isEmpty() && loot.itemEntries.isEmpty() && loot.guaranteedItemIds.isEmpty() && loot.maxCredits == 0) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.WARNING,
                        "LootTable",
                        loot.id,
                        "Loot table has no rewards or resources configured"
                    )
                )
            }
        }

        // 6. Validate Character Archetypes
        for (arch in archetypes) {
            if (arch.minStatBudget > arch.maxStatBudget) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.ERROR,
                        "Archetype",
                        arch.id,
                        "minStatBudget (${arch.minStatBudget}) > maxStatBudget (${arch.maxStatBudget})"
                    )
                )
            }
            if (arch.bioTemplates.isEmpty()) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.WARNING,
                        "Archetype",
                        arch.id,
                        "Archetype has no bio templates"
                    )
                )
            }
        }

        // 7. Validate Repeatable Quest Templates
        for (q in quests) {
            if (q.minRequiredAmount > q.maxRequiredAmount) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.ERROR,
                        "RepeatableQuest",
                        q.id,
                        "minRequiredAmount (${q.minRequiredAmount}) > maxRequiredAmount (${q.maxRequiredAmount})"
                    )
                )
            }
        }

        // 8. Validate Visual Assets
        val assetReport = com.example.domain.content.visual.VisualAssetValidator.validate()
        issues.addAll(assetReport.issues)

        return ContentValidationReport(
            issues = issues,
            totalPacksChecked = registry.allPacks.size,
            totalLocationsChecked = locations.size,
            totalEventsChecked = events.size,
            totalEnemiesChecked = enemies.size,
            totalEncountersChecked = encounters.size,
            totalLootTablesChecked = lootTables.size,
            totalArchetypesChecked = archetypes.size,
            totalQuestsChecked = quests.size
        )
    }

    private fun checkIdUniqueness(domain: String, ids: List<String>, issues: MutableList<ContentValidationIssue>) {
        val seen = mutableSetOf<String>()
        for (id in ids) {
            if (!seen.add(id)) {
                issues.add(
                    ContentValidationIssue(
                        ValidationSeverity.ERROR,
                        domain,
                        id,
                        "Duplicate ID '$id' detected in domain $domain"
                    )
                )
            }
        }
    }
}
