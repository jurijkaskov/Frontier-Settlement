package com.example.domain.content.visual

import com.example.domain.content.registry.ContentValidationIssue
import com.example.domain.content.registry.ValidationSeverity

/**
 * Diagnostic summary of visual asset system coverage and integrity.
 */
data class VisualAssetCoverageReport(
    val totalAssets: Int,
    val approvedCount: Int,
    val placeholderCount: Int,
    val missingCount: Int,
    val unreviewedCount: Int,
    val priorityACount: Int,
    val priorityBCount: Int,
    val priorityCCount: Int,
    val coverageByCategory: Map<VisualAssetCategory, Int>,
    val issues: List<ContentValidationIssue>
) {
    val isClean: Boolean get() = issues.none { it.severity == ValidationSeverity.ERROR }
    val approvedPercentage: Float get() = if (totalAssets > 0) (approvedCount.toFloat() / totalAssets) * 100f else 0f
}

/**
 * Development integrity validator for visual assets.
 */
object VisualAssetValidator {

    fun validate(registry: VisualAssetRegistry = VisualAssetRegistry): VisualAssetCoverageReport {
        val issues = mutableListOf<ContentValidationIssue>()
        val all = registry.getAllDefinitions()

        // 1. Check ID Uniqueness
        val duplicates = all.groupBy { it.assetId }.filter { it.value.size > 1 }
        for ((id, list) in duplicates) {
            issues.add(
                ContentValidationIssue(
                    severity = ValidationSeverity.ERROR,
                    domain = "VisualAssets",
                    contentId = id,
                    message = "Duplicate visual asset ID detected: found ${list.size} occurrences"
                )
            )
        }

        // 2. Check File Name Uniqueness
        val fileDuplicates = all.groupBy { it.fileName }.filter { it.value.size > 1 }
        for ((file, list) in fileDuplicates) {
            issues.add(
                ContentValidationIssue(
                    severity = ValidationSeverity.WARNING,
                    domain = "VisualAssets",
                    contentId = file,
                    message = "Multiple assets share fileName '$file': ${list.map { it.assetId }}"
                )
            )
        }

        // 3. Check Broken Fallbacks
        for (asset in all) {
            if (asset.fallbackAssetId != null && registry.getDefinition(asset.fallbackAssetId) == null) {
                issues.add(
                    ContentValidationIssue(
                        severity = ValidationSeverity.ERROR,
                        domain = "VisualAssets",
                        contentId = asset.assetId,
                        message = "Broken fallback reference '${asset.fallbackAssetId}'"
                    )
                )
            }

            // Check English generation prompts for Priority A
            if (asset.priority == AssetPriority.A && asset.englishPrompt.isBlank() && asset.category != VisualAssetCategory.RESOURCE_ICON) {
                issues.add(
                    ContentValidationIssue(
                        severity = ValidationSeverity.WARNING,
                        domain = "VisualAssets",
                        contentId = asset.assetId,
                        message = "Priority A asset is missing AI generation English prompt"
                    )
                )
            }
        }

        val categoryCounts = all.groupBy { it.category }.mapValues { it.value.size }

        return VisualAssetCoverageReport(
            totalAssets = all.size,
            approvedCount = all.count { it.status == AssetStatus.APPROVED },
            placeholderCount = all.count { it.status == AssetStatus.PLACEHOLDER },
            missingCount = all.count { it.status == AssetStatus.MISSING },
            unreviewedCount = all.count { it.status == AssetStatus.GENERATED_UNREVIEWED },
            priorityACount = all.count { it.priority == AssetPriority.A },
            priorityBCount = all.count { it.priority == AssetPriority.B },
            priorityCCount = all.count { it.priority == AssetPriority.C },
            coverageByCategory = categoryCounts,
            issues = issues
        )
    }
}
