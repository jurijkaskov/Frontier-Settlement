package com.example.data.save.migration

import com.example.core.log.GameLogger
import com.example.data.save.GameSaveConstants
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Interface defining a single step migration between consecutive schema versions.
 */
interface SaveMigration {
    val fromVersion: Int
    val toVersion: Int

    /**
     * Transforms raw JSON string of [fromVersion] to [toVersion].
     */
    fun migrate(rawJson: String): String
}

/**
 * Sequential migration pipeline coordinating upgrades of older save files to current schema.
 */
class GameSaveMigrator(
    private val migrations: List<SaveMigration> = listOf(
        DefaultV0ToV1Migration()
    )
) {

    /**
     * Inspects schema version in the raw JSON payload.
     */
    fun inspectSchemaVersion(rawJson: String): Int {
        return try {
            val match = Regex(""""schemaVersion"\s*:\s*(\d+)""").find(rawJson)
            if (match != null) {
                match.groupValues[1].toInt()
            } else {
                1
            }
        } catch (e: Exception) {
            GameLogger.e("GameSaveMigrator", "Failed to inspect schemaVersion", e)
            1
        }
    }

    /**
     * Sequentially migrates [rawJson] from its source version to [targetVersion].
     */
    fun migrateToVersion(
        rawJson: String,
        targetVersion: Int = GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION
    ): String {
        var currentVersion = inspectSchemaVersion(rawJson)
        var currentJson = rawJson

        if (currentVersion == targetVersion) {
            return currentJson
        }

        if (currentVersion > targetVersion) {
            throw UnsupportedOperationException(
                "Save schema version $currentVersion is newer than current app supported version $targetVersion"
            )
        }

        while (currentVersion < targetVersion) {
            val migration = migrations.find { it.fromVersion == currentVersion }
                ?: throw IllegalStateException(
                    "Missing migration step from version $currentVersion to target $targetVersion"
                )

            GameLogger.i("GameSaveMigrator", "Executing migration: v${migration.fromVersion} -> v${migration.toVersion}")
            currentJson = migration.migrate(currentJson)
            currentVersion = migration.toVersion

            // Update schemaVersion property in JSON
            currentJson = if (currentJson.contains("\"schemaVersion\"")) {
                currentJson.replace(Regex(""""schemaVersion"\s*:\s*\d+"""), """"schemaVersion": $currentVersion""")
            } else {
                currentJson.replaceFirst("{", "{\n  \"schemaVersion\": $currentVersion,")
            }
        }

        return currentJson
    }
}

/**
 * Baseline migration ensuring backward compatibility for early development saves.
 */
class DefaultV0ToV1Migration : SaveMigration {
    override val fromVersion: Int = 0
    override val toVersion: Int = 1

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(mapType)

    @Suppress("UNCHECKED_CAST")
    override fun migrate(rawJson: String): String {
        return try {
            val rawMap = mapAdapter.fromJson(rawJson)?.toMutableMap() ?: mutableMapOf()
            rawMap["schemaVersion"] = 1.0

            if (!rawMap.containsKey("playthroughId") || (rawMap["playthroughId"] as? String).isNullOrBlank()) {
                rawMap["playthroughId"] = "playthrough_migrated_${System.currentTimeMillis()}"
            }

            if (!rawMap.containsKey("metadata") && rawMap.containsKey("gameState")) {
                val meta = mutableMapOf<String, Any?>()
                meta["saveId"] = rawMap["saveId"] ?: "save_migrated"
                meta["slotId"] = rawMap["slotId"] ?: "autosave"
                meta["displayName"] = "Сохранение (v1)"
                meta["schemaVersion"] = 1.0
                meta["gameVersion"] = "1.0"
                rawMap["metadata"] = meta
            }

            mapAdapter.toJson(rawMap)
        } catch (e: Exception) {
            GameLogger.e("DefaultV0ToV1Migration", "Failed to migrate v0 to v1", e)
            rawJson
        }
    }
}
