package com.example.domain.model

/**
 * High-level behavioral archetype for enemy AI.
 */
enum class EnemyAIArchetype(val titleRu: String, val descriptionRu: String) {
    AGGRESSIVE("Агрессор", "Фокусируется на атаках и максимальном расходе ОД на урон."),
    CAUTIOUS("Осторожный", "Занимает укрытия и укрепляет оборону при ранениях."),
    OPPORTUNIST("Охотник за слабыми", "Атакует цели с наименьшим здоровьем и накладывает уязвимости."),
    SUPPORT("Поддержка", "Приоритетно лечит и усиливает раненых союзников."),
    BALANCED("Тактик", "Взвешенно оценивает атаки, оборону и спецнавыки.")
}

/**
 * Target selection bias for AI decision making.
 */
enum class TargetPreference(val titleRu: String) {
    LOWEST_HP("Минимальное HP"),
    HIGHEST_THREAT("Наибольшая угроза"),
    SUPPORT_ROLE("Союзники в беде"),
    RANDOM_VALID("Случайная доступная цель"),
    BALANCED("Сбалансированная оценка")
}

/**
 * Consumable item utilization frequency.
 */
enum class ItemUsagePreference {
    NEVER,
    RARELY,
    NORMAL,
    FREQUENT
}

/**
 * Behavioral trait modifiers for enemy AI.
 */
enum class EnemyAITrait(val titleRu: String) {
    BRAVE("Бесстрашный"),
    COWARDLY("Осторожный / Трусливый"),
    TACTICAL("Тактичный"),
    RELENTLESS("Неумолимый"),
    PROTECTIVE("Защитник союзников")
}

/**
 * AI Difficulty level adjusting scoring precision and randomness.
 */
enum class AIDifficulty(val titleRu: String, val randomnessScale: Float) {
    EASY("Легко", 1.5f),
    NORMAL("Нормально", 1.0f),
    HARD("Сложно", 0.3f)
}

/**
 * Data-driven AI profile describing tactical preferences and weights.
 */
data class EnemyAIProfile(
    val profileId: String,
    val name: String,
    val archetype: EnemyAIArchetype = EnemyAIArchetype.BALANCED,
    val aggression: Float = 0.5f,
    val defensePreference: Float = 0.5f,
    val targetPreference: TargetPreference = TargetPreference.BALANCED,
    val abilityUsageWeight: Float = 1.0f,
    val itemUsage: ItemUsagePreference = ItemUsagePreference.NORMAL,
    val riskTolerance: Float = 0.5f,
    val randomnessWeight: Float = 0.1f,
    val minScoreThreshold: Float = 5.0f,
    val traits: List<EnemyAITrait> = emptyList()
)

/**
 * Data-driven template defining an enemy unit type, stats, role, and assigned AI profile.
 */
data class EnemyTemplate(
    val templateId: String,
    val name: String,
    val baseHealth: Int,
    val baseAttack: Int,
    val baseDefense: Int,
    val baseInitiative: Int,
    val avatarTag: String,
    val aiProfileId: String,
    val role: CharacterRole? = null,
    val abilityIds: List<String> = emptyList(),
    val consumables: List<String> = emptyList(),
    val dangerTier: Int = 1
)

/**
 * Scored tactical action candidate generated during an AI decision step.
 */
data class AIActionCandidate(
    val action: CombatAction,
    val targetId: String?,
    val targetName: String?,
    val baseScore: Float,
    val modifiers: Map<String, Float> = emptyMap(),
    val finalScore: Float,
    val explanation: String
)

/**
 * Structured debug log capturing full AI decision context and candidate evaluations.
 */
data class AIDecisionLog(
    val logId: String = "ai_dec_${System.currentTimeMillis()}_${(0..9999).random()}",
    val actorId: String,
    val actorName: String,
    val round: Int,
    val turnIndex: Int,
    val actionStep: Int,
    val profileId: String,
    val candidates: List<AIActionCandidate> = emptyList(),
    val chosenCandidate: AIActionCandidate?,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
