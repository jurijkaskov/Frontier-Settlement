package com.example.domain.model

enum class QuestStatus {
    LOCKED,
    AVAILABLE,
    ACTIVE,
    IN_PROGRESS,
    READY_TO_CLAIM,
    COMPLETED,
    FAILED,
    DECLINED,
    EXPIRED
}

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val requirementDescription: String,
    val progress: Int,
    val target: Int,
    val rewardCredits: Int = 100,
    val rewardReputation: Int = 10,
    val rewardMaterials: Int = 50,
    val status: QuestStatus = QuestStatus.IN_PROGRESS
) {
    val progressFraction: Float
        get() = if (target > 0) (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
}

