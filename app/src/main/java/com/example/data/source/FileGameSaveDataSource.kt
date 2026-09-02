package com.example.data.source

import com.example.core.log.GameLogger
import com.example.data.InitialGameData
import com.example.data.save.*
import com.example.data.save.migration.GameSaveMigrator
import com.example.data.save.serializer.GameSaveSerializer
import com.example.domain.model.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * File-based implementation of [GameSaveDataSource] using atomic staged writes and backup rotation.
 */
class FileGameSaveDataSource(
    private val saveDirectory: File,
    private val serializer: GameSaveSerializer = GameSaveSerializer(),
    private val migrator: GameSaveMigrator = GameSaveMigrator(),
    private val initialProvider: () -> GameState = { InitialGameData.createInitialGameState() }
) : GameSaveDataSource {

    init {
        if (!saveDirectory.exists()) {
            saveDirectory.mkdirs()
        }
    }

    private fun getFileForSlot(slotId: String): File {
        return File(saveDirectory, "save_${slotId}.json")
    }

    private fun getTempFileForSlot(slotId: String): File {
        return File(saveDirectory, "save_${slotId}.json.tmp")
    }

    private fun getBackupFileForSlot(slotId: String): File {
        return File(saveDirectory, "save_${slotId}_backup.json")
    }

    override suspend fun saveSlot(slotId: String, saveFile: GameSaveFile): SaveOperationResult = withContext(Dispatchers.IO) {
        try {
            val mainFile = getFileForSlot(slotId)
            val tempFile = getTempFileForSlot(slotId)
            val backupFile = getBackupFileForSlot(slotId)

            // 1. Serialize to JSON string
            val json = serializer.serialize(saveFile)

            // 2. Write to temporary staging file and flush to disk
            FileOutputStream(tempFile).use { fos ->
                fos.write(json.toByteArray(Charsets.UTF_8))
                fos.flush()
                try {
                    fos.fd.sync()
                } catch (e: Exception) {
                    // Ignored on file systems without physical sync support
                }
            }

            // 3. Validate staging file size and readability
            if (!tempFile.exists() || tempFile.length() == 0L) {
                tempFile.delete()
                return@withContext SaveOperationResult.Failure(slotId, "Staged temp file was empty")
            }

            // 4. If saving autosave and existing valid main autosave exists, backup previous save
            if (slotId == SaveSlotId.AUTOSAVE.id && mainFile.exists()) {
                try {
                    mainFile.copyTo(backupFile, overwrite = true)
                } catch (e: Exception) {
                    GameLogger.w("FileGameSaveDataSource", "Failed to update backup file for $slotId", e)
                }
            }

            // 5. Atomically replace main file with temp file
            if (mainFile.exists()) {
                mainFile.delete()
            }
            val renamed = tempFile.renameTo(mainFile)
            if (!renamed) {
                // Fallback copy if atomic rename fails across mounts
                tempFile.copyTo(mainFile, overwrite = true)
                tempFile.delete()
            }

            val finalMetadata = saveFile.metadata.copy(fileSizeBytes = mainFile.length())
            GameLogger.i("FileGameSaveDataSource", "Save slot '$slotId' written (${mainFile.length()} bytes)")
            SaveOperationResult.Success(finalMetadata)
        } catch (e: Exception) {
            GameLogger.e("FileGameSaveDataSource", "Failed to save slot '$slotId'", e)
            SaveOperationResult.Failure(slotId, e.message ?: "Failed to save slot", e)
        }
    }

    override suspend fun loadSlot(slotId: String): SaveLoadResult = withContext(Dispatchers.IO) {
        val file = getFileForSlot(slotId)
        val backupFile = getBackupFileForSlot(slotId)

        if (!file.exists()) {
            // Check if backup exists as fallback
            if (backupFile.exists()) {
                GameLogger.w("FileGameSaveDataSource", "Main save for $slotId missing, attempting backup")
                return@withContext loadFromFile(backupFile, slotId, isBackup = true)
            }
            if (slotId == SaveSlotId.AUTOSAVE.id) {
                val initial = initialProvider()
                val newFile = GameSaveFile.create(SaveSlotId.AUTOSAVE.id, SaveSlotId.AUTOSAVE.displayName, initial)
                saveSlot(SaveSlotId.AUTOSAVE.id, newFile)
                return@withContext SaveLoadResult.Success(newFile)
            }
            return@withContext SaveLoadResult.NotFound(slotId)
        }

        val result = loadFromFile(file, slotId, isBackup = false)
        if (result is SaveLoadResult.Corrupted && result.canTryBackup && backupFile.exists()) {
            GameLogger.w("FileGameSaveDataSource", "Main save for $slotId corrupted, falling back to backup")
            val backupResult = loadFromFile(backupFile, slotId, isBackup = true)
            if (backupResult is SaveLoadResult.Success) {
                return@withContext backupResult
            }
        }

        result
    }

    private fun loadFromFile(file: File, slotId: String, isBackup: Boolean): SaveLoadResult {
        return try {
            val rawJson = file.readText(Charsets.UTF_8)
            if (rawJson.isBlank()) {
                return SaveLoadResult.Corrupted(slotId, "Save file is empty", canTryBackup = !isBackup)
            }

            val schemaVer = migrator.inspectSchemaVersion(rawJson)
            if (schemaVer > GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION) {
                return SaveLoadResult.UnsupportedNewerVersion(
                    slotId = slotId,
                    saveVersion = schemaVer,
                    appMaxVersion = GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION
                )
            }

            val migratedJson = if (schemaVer < GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION) {
                migrator.migrateToVersion(rawJson, GameSaveConstants.CURRENT_SAVE_SCHEMA_VERSION)
            } else {
                rawJson
            }

            val saveFile = serializer.deserialize(migratedJson)
            val enriched = saveFile.copy(
                metadata = saveFile.metadata.copy(fileSizeBytes = file.length())
            )
            SaveLoadResult.Success(enriched, loadedFromBackup = isBackup)
        } catch (e: Exception) {
            GameLogger.e("FileGameSaveDataSource", "Error parsing save file for slot $slotId", e)
            SaveLoadResult.Corrupted(slotId, e.message ?: "Failed to parse save file", canTryBackup = !isBackup)
        }
    }

    override suspend fun hasSlot(slotId: String): Boolean = withContext(Dispatchers.IO) {
        val file = getFileForSlot(slotId)
        file.exists() && file.length() > 0L
    }

    override suspend fun deleteSlot(slotId: String): Boolean = withContext(Dispatchers.IO) {
        val file = getFileForSlot(slotId)
        val temp = getTempFileForSlot(slotId)
        val backup = getBackupFileForSlot(slotId)
        var deleted = true
        if (file.exists()) deleted = deleted && file.delete()
        if (temp.exists()) temp.delete()
        if (backup.exists()) backup.delete()
        deleted
    }

    override suspend fun getSlotMetadata(slotId: String): SaveMetadata? = withContext(Dispatchers.IO) {
        when (val res = loadSlot(slotId)) {
            is SaveLoadResult.Success -> res.saveFile.metadata
            is SaveLoadResult.Corrupted -> SaveMetadata(
                saveId = "corrupt_$slotId",
                slotId = slotId,
                displayName = SaveSlotId.fromId(slotId).displayName,
                gameDay = 0,
                gameTimeFormatted = "--:--",
                settlementName = "Повреждено",
                settlementLevel = 0,
                playthroughId = "",
                isCorrupted = true,
                fileSizeBytes = getFileForSlot(slotId).length()
            )
            else -> null
        }
    }

    override suspend fun getAllMetadata(): Map<String, SaveMetadata> = withContext(Dispatchers.IO) {
        val map = mutableMapOf<String, SaveMetadata>()
        for (slot in SaveSlotId.allPlayerVisibleSlots()) {
            val meta = getSlotMetadata(slot.id)
            if (meta != null) {
                map[slot.id] = meta
            }
        }
        map
    }

    override suspend fun createBackup(sourceSlotId: String, backupSlotId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = getFileForSlot(sourceSlotId)
            val backupFile = getBackupFileForSlot(backupSlotId)
            if (sourceFile.exists()) {
                sourceFile.copyTo(backupFile, overwrite = true)
                true
            } else false
        } catch (e: Exception) {
            GameLogger.e("FileGameSaveDataSource", "Failed to create backup", e)
            false
        }
    }

    override suspend fun restoreFromBackup(backupSlotId: String, targetSlotId: String): SaveLoadResult = withContext(Dispatchers.IO) {
        try {
            val backupFile = getBackupFileForSlot(backupSlotId)
            if (!backupFile.exists()) {
                return@withContext SaveLoadResult.NotFound(backupSlotId)
            }
            val targetFile = getFileForSlot(targetSlotId)
            backupFile.copyTo(targetFile, overwrite = true)
            loadSlot(targetSlotId)
        } catch (e: Exception) {
            SaveLoadResult.StorageError(targetSlotId, "Failed to restore backup: ${e.message}", e)
        }
    }

    override suspend fun corruptSlotForTesting(slotId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getFileForSlot(slotId)
            if (file.exists()) {
                file.writeText("{ \"corrupted\": true, \"broken_json\": [ ")
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }
}
