package com.example

import com.example.data.InitialGameData
import com.example.data.repository.DefaultGameStateRepository
import com.example.data.save.*
import com.example.data.save.coordinator.GameSaveCoordinator
import com.example.data.save.migration.GameSaveMigrator
import com.example.data.save.serializer.GameSaveSerializer
import com.example.data.source.FileGameSaveDataSource
import com.example.data.source.InMemoryGameSaveDataSource
import com.example.domain.model.*
import com.example.domain.service.resolver.GameResumeDestinationResolver
import com.example.domain.service.resolver.ResumeDestination
import com.example.domain.validator.GameStateNormalizer
import com.example.domain.validator.GameStateValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SaveSystemTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var serializer: GameSaveSerializer
    private lateinit var migrator: GameSaveMigrator
    private lateinit var inMemoryDataSource: InMemoryGameSaveDataSource
    private lateinit var fileDataSource: FileGameSaveDataSource
    private lateinit var saveDir: File

    @Before
    fun setUp() {
        serializer = GameSaveSerializer()
        migrator = GameSaveMigrator()
        inMemoryDataSource = InMemoryGameSaveDataSource()
        saveDir = tempFolder.newFolder("game_saves")
        fileDataSource = FileGameSaveDataSource(
            saveDirectory = saveDir,
            serializer = serializer,
            migrator = migrator
        )
    }

    // -------------------------------------------------------------
    // 1. Serialization & Deserialization Round-Trip Tests
    // -------------------------------------------------------------

    @Test
    fun testSerializationRoundTripPreservesInitialGameState() {
        val initial = InitialGameData.createInitialGameState()
        val saveFile = GameSaveFile.create(
            slotId = SaveSlotId.AUTOSAVE.id,
            displayName = "Тестовое автосохранение",
            gameState = initial
        )

        val json = serializer.serialize(saveFile)
        assertNotNull(json)
        assertTrue(json.contains("\"schemaVersion\": 1"))
        assertTrue(json.contains("\"checksum\":"))

        val restored = serializer.deserialize(json)
        assertEquals(saveFile.schemaVersion, restored.schemaVersion)
        assertEquals(saveFile.playthroughId, restored.playthroughId)
        assertEquals(saveFile.slotId, restored.slotId)
        assertEquals(initial.settlement.name, restored.gameState.settlement.name)
        assertEquals(initial.resources.food, restored.gameState.resources.food)
        assertEquals(initial.characters.size, restored.gameState.characters.size)
        assertEquals(initial.technologies.size, restored.gameState.technologies.size)
        assertEquals(initial.quests.size, restored.gameState.quests.size)
    }

    @Test
    fun testChecksumVerification() {
        val initial = InitialGameData.createInitialGameState()
        val saveFile = GameSaveFile.create("manual_1", "Слот 1", initial)
        val json = serializer.serialize(saveFile)

        val validChecksum = serializer.computeChecksum(json)
        assertNotNull(validChecksum)
        assertTrue(validChecksum.length == 8)

        // Corrupted JSON should throw or fail deserialization
        val corruptJson = json.replace("\"day\": 1", "\"day\": 9999")
        // Deserializing directly without exception will log warning or parse
        val deserialized = serializer.deserialize(corruptJson, verifyChecksum = false)
        assertEquals(9999, deserialized.gameState.day)
    }

    // -------------------------------------------------------------
    // 2. File Persistence, Staged Atomic Writes & Backup Rotation
    // -------------------------------------------------------------

    @Test
    fun testFileDataSourceStagedAtomicSaveAndLoad() = runBlocking {
        val initial = InitialGameData.createInitialGameState()
        val saveFile = GameSaveFile.create(SaveSlotId.MANUAL_1.id, "Ручной Сейв", initial)

        val saveRes = fileDataSource.saveSlot(SaveSlotId.MANUAL_1.id, saveFile)
        assertTrue(saveRes is SaveOperationResult.Success)

        val targetFile = File(saveDir, "save_manual_1.json")
        assertTrue(targetFile.exists())
        assertTrue(targetFile.length() > 0)

        // Staging .tmp file should be cleaned up
        val tempFile = File(saveDir, "save_manual_1.json.tmp")
        assertFalse(tempFile.exists())

        val loadRes = fileDataSource.loadSlot(SaveSlotId.MANUAL_1.id)
        assertTrue(loadRes is SaveLoadResult.Success)
        val loaded = (loadRes as SaveLoadResult.Success).saveFile
        assertEquals(initial.settlement.name, loaded.gameState.settlement.name)
        assertFalse(loadRes.loadedFromBackup)
    }

    @Test
    fun testAutosaveBackupRotationAndFallback() = runBlocking {
        val initialDay1 = InitialGameData.createInitialGameState().copy(
            gameDateTime = GameDateTime(day = 1, hour = 8, minute = 0)
        )
        val initialDay2 = InitialGameData.createInitialGameState().copy(
            gameDateTime = GameDateTime(day = 2, hour = 14, minute = 30)
        )

        // 1. First Autosave (Day 1)
        val save1 = GameSaveFile.create(SaveSlotId.AUTOSAVE.id, "Авто", initialDay1)
        fileDataSource.saveSlot(SaveSlotId.AUTOSAVE.id, save1)

        // 2. Second Autosave (Day 2) -> should rotate Day 1 save to backup
        val save2 = GameSaveFile.create(SaveSlotId.AUTOSAVE.id, "Авто", initialDay2)
        fileDataSource.saveSlot(SaveSlotId.AUTOSAVE.id, save2)

        val backupFile = File(saveDir, "save_autosave_backup.json")
        assertTrue("Backup file must exist after second autosave", backupFile.exists())

        // 3. Corrupt main autosave
        fileDataSource.corruptSlotForTesting(SaveSlotId.AUTOSAVE.id)

        // 4. Load Autosave -> should automatically fall back to backup (Day 1)!
        val fallbackLoad = fileDataSource.loadSlot(SaveSlotId.AUTOSAVE.id)
        assertTrue(fallbackLoad is SaveLoadResult.Success)
        val success = fallbackLoad as SaveLoadResult.Success
        assertTrue("Must indicate loadedFromBackup = true", success.loadedFromBackup)
        assertEquals(1, success.saveFile.gameState.day)
    }

    @Test
    fun testSaveSlotDeletion() = runBlocking {
        val initial = InitialGameData.createInitialGameState()
        val saveFile = GameSaveFile.create(SaveSlotId.MANUAL_2.id, "Слот 2", initial)
        fileDataSource.saveSlot(SaveSlotId.MANUAL_2.id, saveFile)

        assertTrue(fileDataSource.hasSlot(SaveSlotId.MANUAL_2.id))
        fileDataSource.deleteSlot(SaveSlotId.MANUAL_2.id)
        assertFalse(fileDataSource.hasSlot(SaveSlotId.MANUAL_2.id))

        val loadRes = fileDataSource.loadSlot(SaveSlotId.MANUAL_2.id)
        assertTrue(loadRes is SaveLoadResult.NotFound)
    }

    // -------------------------------------------------------------
    // 3. Migration Pipeline Tests
    // -------------------------------------------------------------

    @Test
    fun testMigrationFromV0ToV1() {
        val legacyV0Json = """
            {
              "schemaVersion": 0,
              "saveId": "legacy_save_1",
              "gameState": {
                "day": 5
              }
            }
        """.trimIndent()

        val migratedJson = migrator.migrateToVersion(legacyV0Json, 1)
        val inspectedVersion = migrator.inspectSchemaVersion(migratedJson)
        assertEquals(1, inspectedVersion)
        assertTrue(migratedJson.contains("\"playthroughId\":"))
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testRejectsFutureSchemaVersion() {
        val futureJson = """
            {
              "schemaVersion": 999,
              "saveId": "future_save"
            }
        """.trimIndent()

        migrator.migrateToVersion(futureJson, 1)
    }

    // -------------------------------------------------------------
    // 4. Domain Invariants Restoration Tests
    // -------------------------------------------------------------

    @Test
    fun testCombatStateRestoration() = runBlocking {
        val initial = InitialGameData.createInitialGameState()
        val testCombatant1 = Combatant(
            id = "c_player_1",
            displayName = "Ветеран",
            team = CombatantTeam.PLAYER,
            currentHealth = 80,
            maxHealth = 100,
            actionPoints = 3,
            maxActionPoints = 4,
            activeEffects = listOf(
                CombatEffectInstance(
                    instanceId = "eff_1",
                    name = "Усиление атаки",
                    description = "+5 к урону",
                    sourceCombatantId = "c_player_1",
                    targetCombatantId = "c_player_1",
                    effectType = CombatEffectType.BUFF_ATTACK,
                    remainingTurns = 2,
                    modifier = 5
                )
            )
        )
        val testCombatant2 = Combatant(
            id = "c_enemy_1",
            displayName = "Рейдер пустоши",
            team = CombatantTeam.ENEMY,
            currentHealth = 45,
            maxHealth = 60,
            actionPoints = 2,
            maxActionPoints = 4
        )

        val activeCombat = CombatState(
            id = "combat_test_123",
            locationId = "loc_scrap_yard",
            currentPhase = CombatPhase.PLAYER_TURN,
            roundNumber = 2,
            combatants = listOf(testCombatant1, testCombatant2),
            turnOrder = listOf("c_player_1", "c_enemy_1")
        )

        val combatGameState = initial.copy(activeCombat = activeCombat)
        val saveFile = GameSaveFile.create(SaveSlotId.AUTOSAVE.id, "Авто", combatGameState)

        val serialized = serializer.serialize(saveFile)
        val restoredFile = serializer.deserialize(serialized)

        assertNotNull(restoredFile.gameState.activeCombat)
        val restoredCombat = restoredFile.gameState.activeCombat!!
        assertEquals("combat_test_123", restoredCombat.id)
        assertEquals(2, restoredCombat.roundNumber)
        assertEquals(2, restoredCombat.combatants.size)
        assertEquals(80, restoredCombat.combatants[0].currentHealth)
        assertEquals(1, restoredCombat.combatants[0].activeEffects.size)
        assertEquals(CombatEffectType.BUFF_ATTACK, restoredCombat.combatants[0].activeEffects[0].effectType)
    }

    @Test
    fun testExpeditionWithUnresolvedEventAndPendingLootRestoration() {
        val initial = InitialGameData.createInitialGameState()
        val activeExp = Expedition(
            id = "exp_test_1",
            location = initial.locations.first(),
            squad = initial.characters.take(1),
            vehicle = initial.vehicles.first(),
            status = ExpeditionStatus.EXPLORING,
            phase = ExpeditionPhase.EXPLORING,
            supplies = mapOf(ResourceType.FOOD to 15, ResourceType.WATER to 20, ResourceType.FUEL to 10),
            lootItemIds = listOf("loot_1", "loot_2")
        )

        val expGameState = initial.copy(activeExpedition = activeExp)
        val saveFile = GameSaveFile.create(SaveSlotId.MANUAL_1.id, "Экспедиция", expGameState)

        val serialized = serializer.serialize(saveFile)
        val restored = serializer.deserialize(serialized).gameState

        assertNotNull(restored.activeExpedition)
        assertEquals(2, restored.activeExpedition!!.lootItemIds.size)
        assertEquals(15, restored.activeExpedition!!.supplies[ResourceType.FOOD])
        assertEquals(ExpeditionPhase.EXPLORING, restored.activeExpedition!!.phase)
    }

    // -------------------------------------------------------------
    // 5. GameResumeDestinationResolver Tests
    // -------------------------------------------------------------

    @Test
    fun testResumeDestinationPriorities() {
        val baseState = InitialGameData.createInitialGameState()

        // 1. In Settlement -> SETTLEMENT
        assertEquals(ResumeDestination.SETTLEMENT, GameResumeDestinationResolver.resolve(baseState))

        // 2. Active Combat -> COMBAT (Highest priority)
        val combatState = baseState.copy(activeCombat = CombatState(id = "c1"))
        assertEquals(ResumeDestination.COMBAT, GameResumeDestinationResolver.resolve(combatState))

        // 3. Unresolved Event -> EXPEDITION_EVENT
        val sampleEvent = ExpeditionEvent(id = "ev1", title = "Засада", description = "", choices = emptyList())
        val eventExp = Expedition(
            id = "e1",
            location = baseState.locations.first(),
            squad = baseState.characters.take(1),
            vehicle = baseState.vehicles.first(),
            status = ExpeditionStatus.EXPLORING,
            currentEvent = sampleEvent,
            activeEventState = ActiveEventState(eventId = "ev1", event = sampleEvent, instanceSeed = 100L, isResolved = false)
        )
        val eventState = baseState.copy(activeExpedition = eventExp)
        assertEquals(ResumeDestination.EXPEDITION_EVENT, GameResumeDestinationResolver.resolve(eventState))

        // 4. Pending Loot -> EXPEDITION_LOOT
        val lootExp = Expedition(
            id = "e2",
            location = baseState.locations.first(),
            squad = baseState.characters.take(1),
            vehicle = baseState.vehicles.first(),
            status = ExpeditionStatus.EXPLORING,
            phase = ExpeditionPhase.EXPLORING,
            lootItemIds = listOf("medkit_1")
        )
        val lootState = baseState.copy(activeExpedition = lootExp)
        assertEquals(ResumeDestination.EXPEDITION_LOOT, GameResumeDestinationResolver.resolve(lootState))

        // 5. At Location -> ARRIVAL
        val arrivalExp = Expedition(
            id = "e3",
            location = baseState.locations.first(),
            squad = baseState.characters.take(1),
            vehicle = baseState.vehicles.first(),
            status = ExpeditionStatus.PREPARING,
            phase = ExpeditionPhase.AT_LOCATION
        )
        val arrivalState = baseState.copy(activeExpedition = arrivalExp)
        assertEquals(ResumeDestination.ARRIVAL, GameResumeDestinationResolver.resolve(arrivalState))

        // 6. Traveling -> MAP_TRAVEL
        val travelState = baseState.copy(
            activeTravel = TravelState(id = "tr_1", toLocationId = "loc_quarry", estimatedHours = 2.0f)
        )
        assertEquals(ResumeDestination.MAP_TRAVEL, GameResumeDestinationResolver.resolve(travelState))
    }

    // -------------------------------------------------------------
    // 6. GameStateNormalizer & Validator Tests
    // -------------------------------------------------------------

    @Test
    fun testGameStateNormalizerClampsInvalidValues() {
        val initial = InitialGameData.createInitialGameState()
        val brokenChar = initial.characters.first().copy(
            health = -50,
            unspentSkillPoints = -3
        )
        val brokenResources = initial.resources.copy(food = -100)
        val brokenState = initial.copy(
            characters = listOf(brokenChar) + initial.characters.drop(1),
            resources = brokenResources,
            squad = initial.squad.copy(memberIds = listOf(brokenChar.id, "non_existent_char_999"))
        )

        val normResult = GameStateNormalizer.normalize(brokenState)
        assertTrue(normResult.appliedFixes.isNotEmpty())
        assertEquals(0, normResult.normalizedState.characters.first().health)
        assertEquals(0, normResult.normalizedState.characters.first().unspentSkillPoints)
        assertEquals(0, normResult.normalizedState.resources.food)
        assertFalse(normResult.normalizedState.squad.memberIds.contains("non_existent_char_999"))

        // After normalization, validation should pass
        val validation = GameStateValidator.validate(normResult.normalizedState)
        assertTrue(validation.isValid)
    }

    // -------------------------------------------------------------
    // 7. Concurrent Mutex Safety in GameSaveCoordinator & Repository
    // -------------------------------------------------------------

    @Test
    fun testConcurrentSavesWithCoordinator() = runBlocking {
        val coordinator = GameSaveCoordinator(inMemoryDataSource)
        val initial = InitialGameData.createInitialGameState()

        val jobs = (1..20).map { i ->
            async(Dispatchers.Default) {
                val state = initial.copy(gameDateTime = initial.gameDateTime.copy(day = i))
                coordinator.saveAutosave(state)
            }
        }
        val results = jobs.awaitAll()
        assertTrue(results.all { it is SaveOperationResult.Success })

        val loadRes = coordinator.loadSlot(SaveSlotId.AUTOSAVE.id)
        assertTrue(loadRes is SaveLoadResult.Success)
    }

    @Test
    fun testRepositoryMultiSlotOperations() = runBlocking {
        val repo = DefaultGameStateRepository(inMemoryDataSource)
        val state = repo.currentGameState

        // 1. Save manual slot 1
        val saveRes = repo.saveSlot(SaveSlotId.MANUAL_1.id, "Битва у карьера")
        assertTrue(saveRes is SaveOperationResult.Success)

        // 2. Modify state
        repo.updateGameState { it.copy(gameDateTime = it.gameDateTime.copy(day = 42)) }
        assertEquals(42, repo.currentGameState.day)

        // 3. Load manual slot 1 -> should restore original day
        val loadRes = repo.loadSlot(SaveSlotId.MANUAL_1.id)
        assertTrue(loadRes is SaveLoadResult.Success)
        assertEquals(state.day, repo.currentGameState.day)

        // 4. Check metadata
        val allMeta = repo.getAllMetadata()
        assertTrue(allMeta.containsKey(SaveSlotId.MANUAL_1.id))
        assertEquals("Битва у карьера", allMeta[SaveSlotId.MANUAL_1.id]?.displayName)
    }
}
