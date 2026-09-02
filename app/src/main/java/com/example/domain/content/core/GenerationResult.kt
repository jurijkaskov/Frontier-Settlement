package com.example.domain.content.core

/**
 * Diagnostic trace entry generated during procedural content selection and generation.
 * Enables in-depth inspection and debugging of weights, candidate filters, and roll outcomes.
 */
data class GenerationTrace(
    val generatorName: String,
    val masterSeed: Long,
    val derivedSeed: Long,
    val candidateCountBeforeFilter: Int,
    val candidateCountAfterFilter: Int,
    val candidateBreakdown: List<CandidateScore> = emptyList(),
    val selectedId: String? = null,
    val notes: List<String> = emptyList()
)

data class CandidateScore(
    val id: String,
    val baseWeight: Float,
    val finalWeight: Float,
    val modifiersApplied: List<String> = emptyList()
)

/**
 * Unified generation outcome wrapper.
 */
sealed interface GenerationResult<out T> {
    data class Success<T>(
        val data: T,
        val trace: GenerationTrace? = null
    ) : GenerationResult<T>

    data class NoEligibleContent(
        val reason: String,
        val trace: GenerationTrace? = null
    ) : GenerationResult<Nothing>

    data class InvalidContext(
        val reason: String
    ) : GenerationResult<Nothing>

    data class ContentError(
        val message: String,
        val exception: Throwable? = null
    ) : GenerationResult<Nothing>

    val isSuccess: Boolean
        get() = this is Success

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> defaultValue
    }
}
