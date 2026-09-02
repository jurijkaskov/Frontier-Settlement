package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.InitialGameData
import com.example.data.ReputationBalanceConfig
import com.example.data.ResearchConfig
import com.example.data.SurvivorGenerator
import com.example.data.TradeConfig
import com.example.data.TravelCalculator
import com.example.domain.model.*
import com.example.domain.model.quest.*
import com.example.domain.model.reputation.*
import com.example.domain.service.ExpeditionPreparationValidator
import com.example.domain.service.ExpeditionSupplyCalculator
import com.example.domain.service.ExpeditionReturnService
import com.example.domain.service.events.EventCatalog
import com.example.domain.service.events.EventOutcomeResolver
import com.example.domain.service.events.EventRequirementEvaluator
import com.example.domain.service.events.EventSelector
import com.example.domain.service.events.SkillCheckResolver
import com.example.domain.service.combat.*
import com.example.domain.service.quest.*
import com.example.domain.service.reputation.ReputationLevelResolver
import com.example.domain.service.reputation.ReputationManager
import com.example.domain.service.time.DailyTickProcessor
import com.example.domain.service.time.GameClock
import com.example.data.repository.DefaultGameStateRepository
import com.example.data.repository.GameStateRepository
import com.example.data.save.*
import com.example.domain.service.resolver.GameResumeDestinationResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(
    val repository: GameStateRepository = DefaultGameStateRepository.getInstance()
) : ViewModel() {

    private val _gameState = MutableStateFlow(repository.currentGameState)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _saveSlotsMetadata = MutableStateFlow<Map<String, SaveMetadata>>(emptyMap())
    val saveSlotsMetadata: StateFlow<Map<String, SaveMetadata>> = _saveSlotsMetadata.asStateFlow()

    val isSaving: StateFlow<Boolean> = repository.coordinator.isSaving
    val lastSaveOperationResult: StateFlow<SaveOperationResult?> = repository.coordinator.lastSaveResult

    private val _lastLoadResult = MutableStateFlow<SaveLoadResult?>(null)
    val lastLoadResult: StateFlow<SaveLoadResult?> = _lastLoadResult.asStateFlow()

    init {
        refreshSaveMetadata()
    }

    fun refreshSaveMetadata() {
        viewModelScope.launch {
            _saveSlotsMetadata.value = repository.getAllMetadata()
        }
    }

    fun saveAutosave(isCritical: Boolean = false) {
        viewModelScope.launch {
            repository.updateGameStateSync { _gameState.value }
            repository.saveAutosave(isCritical = isCritical)
            refreshSaveMetadata()
        }
    }

    fun saveToSlot(slotId: String, displayName: String = "") {
        viewModelScope.launch {
            repository.updateGameStateSync { _gameState.value }
            val name = displayName.ifBlank { SaveSlotId.fromId(slotId).displayName }
            repository.saveSlot(slotId, name)
            refreshSaveMetadata()
        }
    }

    fun loadFromSlot(slotId: String, onNavigateToRoute: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.loadSlot(slotId)
            _lastLoadResult.value = result
            if (result is SaveLoadResult.Success) {
                _gameState.value = result.saveFile.gameState
                val destination = GameResumeDestinationResolver.resolve(result.saveFile.gameState)
                onNavigateToRoute?.invoke(destination.route)
            }
            refreshSaveMetadata()
        }
    }

    fun deleteSaveSlot(slotId: String) {
        viewModelScope.launch {
            repository.deleteSlot(slotId)
            refreshSaveMetadata()
        }
    }

    fun restoreFromBackup(
        backupSlotId: String = SaveSlotId.AUTOSAVE_BACKUP.id,
        targetSlotId: String = SaveSlotId.AUTOSAVE.id,
        onNavigateToRoute: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.restoreFromBackup(backupSlotId, targetSlotId)
            _lastLoadResult.value = result
            if (result is SaveLoadResult.Success) {
                _gameState.value = result.saveFile.gameState
                val destination = GameResumeDestinationResolver.resolve(result.saveFile.gameState)
                onNavigateToRoute?.invoke(destination.route)
            }
            refreshSaveMetadata()
        }
    }

    fun corruptSlotForTesting(slotId: String) {
        viewModelScope.launch {
            repository.corruptSlotForTesting(slotId)
            refreshSaveMetadata()
        }
    }

    fun getResumeDestinationRoute(): String {
        return GameResumeDestinationResolver.resolve(_gameState.value).route
    }

    fun clearLastLoadResult() {
        _lastLoadResult.value = null
    }

    private val _expeditionDraft = MutableStateFlow<ExpeditionPreparationState?>(null)
    val expeditionDraft: StateFlow<ExpeditionPreparationState?> = _expeditionDraft.asStateFlow()

    private val _lastExpeditionTransactionResult = MutableStateFlow<ExpeditionTransactionResult?>(null)
    val lastExpeditionTransactionResult: StateFlow<ExpeditionTransactionResult?> = _lastExpeditionTransactionResult.asStateFlow()

    private val _lastResourceOperation = MutableStateFlow<ResourceOperationResult?>(null)
    val lastResourceOperation: StateFlow<ResourceOperationResult?> = _lastResourceOperation.asStateFlow()

    private val _lastTradeResult = MutableStateFlow<TradeTransactionResult?>(null)
    val lastTradeResult: StateFlow<TradeTransactionResult?> = _lastTradeResult.asStateFlow()

    private val _lastCraftResult = MutableStateFlow<CraftTransactionResult?>(null)
    val lastCraftResult: StateFlow<CraftTransactionResult?> = _lastCraftResult.asStateFlow()

    private val _selectedTravelMode = MutableStateFlow(TravelTransportMode.FOOT)
    val selectedTravelMode: StateFlow<TravelTransportMode> = _selectedTravelMode.asStateFlow()

    private val _lastTravelResult = MutableStateFlow<TravelTransactionResult?>(null)
    val lastTravelResult: StateFlow<TravelTransactionResult?> = _lastTravelResult.asStateFlow()

    private val _lastSquadOperation = MutableStateFlow<SquadOperationResult?>(null)
    val lastSquadOperation: StateFlow<SquadOperationResult?> = _lastSquadOperation.asStateFlow()

    private val _lastEquipmentResult = MutableStateFlow<EquipmentOperationResult?>(null)
    val lastEquipmentResult: StateFlow<EquipmentOperationResult?> = _lastEquipmentResult.asStateFlow()

    private val _selectedActorIdForEvent = MutableStateFlow<String?>(null)
    val selectedActorIdForEvent: StateFlow<String?> = _selectedActorIdForEvent.asStateFlow()

    fun selectActorForEventCheck(actorId: String) {
        _selectedActorIdForEvent.value = actorId
    }

    fun clearLastTradeResult() {
        _lastTradeResult.value = null
    }

    fun clearLastCraftResult() {
        _lastCraftResult.value = null
    }

    fun clearLastTravelResult() {
        _lastTravelResult.value = null
    }

    fun clearLastSquadOperation() {
        _lastSquadOperation.value = null
    }

    fun clearLastEquipmentResult() {
        _lastEquipmentResult.value = null
    }

    fun selectTravelMode(mode: TravelTransportMode) {
        _selectedTravelMode.value = mode
    }

    // -------------------------------------------------------------
    // Centralized Resource Management System
    // -------------------------------------------------------------

    /**
     * Safely adds a resource to the settlement inventory with warehouse storage checks.
     */
    fun addResource(type: ResourceType, amount: Int, allowPartial: Boolean = true): ResourceOperationResult {
        var opResult: ResourceOperationResult = ResourceOperationResult.Failure(
            ResourceOperationResult.FailureReason.INVALID_AMOUNT,
            "Некорректная операция"
        )
        _gameState.update { state ->
            val (updatedRes, result) = state.resources.addResourceSafe(type, amount, allowPartial)
            opResult = result
            if (result.isSuccess) {
                state.copy(
                    resources = updatedRes,
                    dayLogs = listOf("Склад: ${result.message}") + state.dayLogs.take(19)
                )
            } else {
                state
            }
        }
        _lastResourceOperation.value = opResult
        return opResult
    }

    /**
     * Safely consumes a resource from the settlement inventory.
     */
    fun consumeResource(type: ResourceType, amount: Int): ResourceOperationResult {
        var opResult: ResourceOperationResult = ResourceOperationResult.Failure(
            ResourceOperationResult.FailureReason.INVALID_AMOUNT,
            "Некорректная операция"
        )
        _gameState.update { state ->
            val (updatedRes, result) = state.resources.consumeResourceSafe(type, amount)
            opResult = result
            if (result.isSuccess) {
                state.copy(
                    resources = updatedRes,
                    dayLogs = listOf("Склад: ${result.message}") + state.dayLogs.take(19)
                )
            } else {
                state
            }
        }
        _lastResourceOperation.value = opResult
        return opResult
    }

    /**
     * Safely adds a bundle of resources (e.g. from an expedition or market).
     */
    fun addResourceBundle(bundle: Map<ResourceType, Int>, allowPartial: Boolean = true): ResourceOperationResult {
        var opResult: ResourceOperationResult = ResourceOperationResult.Failure(
            ResourceOperationResult.FailureReason.INVALID_AMOUNT,
            "Некорректный набор ресурсов"
        )
        _gameState.update { state ->
            val (updatedRes, result) = state.resources.addBundleSafe(bundle, allowPartial)
            opResult = result
            if (result.isSuccess) {
                state.copy(
                    resources = updatedRes,
                    dayLogs = listOf("Склад: ${result.message}") + state.dayLogs.take(19)
                )
            } else {
                state
            }
        }
        _lastResourceOperation.value = opResult
        return opResult
    }

    /**
     * Safely consumes a bundle of resources atomically.
     */
    fun consumeResourceBundle(bundle: Map<ResourceType, Int>): ResourceOperationResult {
        var opResult: ResourceOperationResult = ResourceOperationResult.Failure(
            ResourceOperationResult.FailureReason.INVALID_AMOUNT,
            "Некорректный набор ресурсов"
        )
        _gameState.update { state ->
            val (updatedRes, result) = state.resources.consumeBundleSafe(bundle)
            opResult = result
            if (result.isSuccess) {
                state.copy(
                    resources = updatedRes,
                    dayLogs = listOf("Склад: ${result.message}") + state.dayLogs.take(19)
                )
            } else {
                state
            }
        }
        _lastResourceOperation.value = opResult
        return opResult
    }

    fun clearLastResourceOperation() {
        _lastResourceOperation.value = null
    }

    // -------------------------------------------------------------
    // Resource Debug & Testing Tools (For verifying rules & limits)
    // -------------------------------------------------------------

    /**
     * Debug: Quickly add or subtract an amount of a specific resource.
     */
    fun debugModifyResource(type: ResourceType, delta: Int) {
        if (delta > 0) {
            addResource(type, delta, allowPartial = true)
        } else if (delta < 0) {
            consumeResource(type, -delta)
        }
    }

    /**
     * Debug Scenario: Fill warehouse capacity to specified target percentage (e.g. 95%).
     * Tests capacity limits, warning indicators in HUD, and rejected storage overflow.
     */
    fun debugFillWarehouseTo(targetPercent: Float = 0.95f) {
        _gameState.update { state ->
            val maxCap = state.resources.warehouseMaxCapacity
            val targetVolume = (maxCap * targetPercent).toInt()
            val currentVolume = state.resources.totalStoredVolume
            val neededVolume = (targetVolume - currentVolume).coerceAtLeast(0)

            val updatedRes = state.resources.copy(
                materials = state.resources.materials + neededVolume
            )
            val msg = "Тест склада: Вместимость заполнена до ${(targetPercent * 100).toInt()}% ($targetVolume / $maxCap ед.)"
            _lastResourceOperation.value = ResourceOperationResult.Success(
                type = ResourceType.MATERIALS,
                amountChanged = neededVolume,
                message = msg
            )
            state.copy(
                resources = updatedRes,
                dayLogs = listOf(msg) + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Debug Scenario: Empty survival resources (food and water) to 0.
     * Tests critical deficit alerts, HUD red flashing chips, and next day survival warnings.
     */
    fun debugDrainSupplies() {
        _gameState.update { state ->
            val updatedRes = state.resources.copy(food = 0, water = 0, fuel = 2)
            val msg = "Тест дефицита: Провизия и вода сброшены до 0!"
            _lastResourceOperation.value = ResourceOperationResult.Success(
                message = msg
            )
            state.copy(
                resources = updatedRes,
                dayLogs = listOf(msg) + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Debug Scenario: Attempt to add a large batch (+500 units) when storage is almost full.
     * Demonstrates partial addition and overflow calculation.
     */
    fun debugTestPartialOverflow(type: ResourceType = ResourceType.FOOD, requestedAmount: Int = 500) {
        addResource(type, requestedAmount, allowPartial = true)
    }

    /**
     * Debug Scenario: Reset resources to balanced initial default values.
     */
    fun debugResetToDefaultResources() {
        _gameState.update { state ->
            val defaultRes = GameResources(
                money = 850,
                food = 160,
                water = 190,
                fuel = 95,
                materials = 240,
                warehouseMaxCapacity = 800
            )
            val msg = "Тест: Ресурсы сброшены к исходным нормальным запасам."
            _lastResourceOperation.value = ResourceOperationResult.Success(message = msg)
            state.copy(
                resources = defaultRes,
                dayLogs = listOf(msg) + state.dayLogs.take(19)
            )
        }
    }

    // -------------------------------------------------------------
    // Settlement & Time Progression (GameClock & Daily Pipeline)
    // -------------------------------------------------------------

    /**
     * Advances the authoritative game time by the specified duration.
     * If a day boundary is crossed, triggers DailyTickProcessor automatically.
     */
    fun advanceGameTime(duration: GameDuration, actionLog: String? = null) {
        _gameState.update { state ->
            val timeAdvance = GameClock.advance(state.gameDateTime, duration)
            val baseState = state.copy(
                gameDateTime = timeAdvance.newTime
            )

            val finalState = if (timeAdvance.isNewDayCrossed) {
                val (processedState, summaries) = DailyTickProcessor.processMultipleDays(baseState, timeAdvance.crossedDays)
                processedState.copy(
                    lastDailySummary = summaries.lastOrNull() ?: processedState.lastDailySummary
                )
            } else {
                baseState
            }

            if (actionLog != null) {
                finalState.copy(dayLogs = listOf(actionLog) + finalState.dayLogs.take(19))
            } else {
                finalState
            }
        }
    }

    /**
     * Manually advances game time by a number of hours.
     */
    fun advanceTimeHours(hours: Int) {
        if (hours <= 0) return
        val currentStr = _gameState.value.gameDateTime.formattedTime
        advanceGameTime(
            duration = GameDuration.ofHours(hours),
            actionLog = "⏳ Прошло $hours ч. (Время: $currentStr)."
        )
    }

    /**
     * Manually advances game time by a number of minutes.
     */
    fun advanceTimeMinutes(minutes: Int) {
        if (minutes <= 0) return
        advanceGameTime(duration = GameDuration.ofMinutes(minutes))
    }

    /**
     * Advance to the next game day (08:00 morning):
     * - Advances clock to 08:00 next morning.
     * - Executes DailyTickProcessor for consumption, building production, healing, trade restock, quest progress.
     */
    fun nextDay() {
        _gameState.update { state ->
            val nextMorning = GameClock.nextMorning(state.gameDateTime)
            val crossedDays = GameClock.calculateCrossedDays(state.gameDateTime, nextMorning)
            val targetDays = if (crossedDays.isNotEmpty()) crossedDays else listOf(nextMorning.day)

            val baseState = state.copy(
                gameDateTime = nextMorning
            )

            val (processedState, summaries) = DailyTickProcessor.processMultipleDays(baseState, targetDays)
            val latestSummary = summaries.lastOrNull() ?: processedState.lastDailySummary

            val logMsg = "🌅 Наступило утро (День ${nextMorning.day}, 08:00). Сводка за сутки сохранена."
            processedState.copy(
                lastDailySummary = latestSummary,
                dayLogs = listOf(logMsg) + processedState.dayLogs.take(19)
            )
        }
    }

    /**
     * Fast-forwards to the start of the next game day.
     */
    fun endCurrentDay() {
        nextDay()
    }

    /**
     * Clears or acknowledges the last daily summary.
     */
    fun dismissDailySummary() {
        _gameState.update { it.copy(lastDailySummary = null) }
    }

    // -------------------------------------------------------------
    // Settlement Development & Building Infrastructure
    // -------------------------------------------------------------

    /**
     * Constructs a new unbuilt building in the settlement.
     */
    fun buildBuilding(buildingId: String) {
        _gameState.update { state ->
            val bld = state.settlement.buildings.find { it.id == buildingId } ?: return@update state
            if (bld.isConstructed) return@update state

            // Check requirements
            if (state.settlement.level < bld.requiredSettlementLevel) {
                val errorMsg = "Ошибка постройки: Требуется уровень поселения ${bld.requiredSettlementLevel}!"
                _lastResourceOperation.value = ResourceOperationResult.Failure(
                    reason = ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE,
                    message = errorMsg
                )
                return@update state
            }

            if (state.resources.materials < bld.buildCostMaterials || state.resources.money < bld.buildCostMoney) {
                val errorMsg = "Недостаточно ресурсов для постройки «${bld.name}» (нужно ${bld.buildCostMaterials} материалов, ${bld.buildCostMoney} кр.)"
                val deficitMat = (bld.buildCostMaterials - state.resources.materials).coerceAtLeast(0)
                _lastResourceOperation.value = ResourceOperationResult.Failure(
                    reason = ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE,
                    message = errorMsg,
                    deficitType = ResourceType.MATERIALS,
                    deficitAmount = deficitMat
                )
                return@update state
            }

            // Deduct resources
            val updatedResources = state.resources.copy(
                materials = state.resources.materials - bld.buildCostMaterials,
                money = state.resources.money - bld.buildCostMoney
            )

            // Update building to Level 1 Operational
            val constructedBuilding = bld.copy(
                level = 1,
                status = BuildingStatus.OPERATIONAL,
                upgradeCostMaterials = bld.calculateNextUpgradeMaterials(1),
                upgradeCostMoney = bld.calculateNextUpgradeMoney(1)
            )

            val updatedBuildingList = state.settlement.buildings.map {
                if (it.id == buildingId) constructedBuilding else it
            }

            // Award Settlement XP & evaluate level-up
            val (settlementAfterXp, leveledUp) = state.settlement.copy(buildings = updatedBuildingList)
                .addXp(bld.xpRewardOnBuild)

            // Synchronize settlement limits & unlocked buildings
            val finalizedSettlement = recalculateSettlementState(settlementAfterXp)
            val finalizedResources = updatedResources.copy(
                warehouseMaxCapacity = calculateWarehouseCapacity(finalizedSettlement.buildings)
            )

            val successMsg = buildString {
                append("Возведено новое здание: «${bld.name}»! (+${bld.xpRewardOnBuild} XP поселения)")
                if (leveledUp) {
                    append(" 🌟 ПОСЕЛЕНИЕ ДОСТИГЛО УРОВНЯ ${finalizedSettlement.level}! Новые технологии и чертежи доступны!")
                }
            }

            _lastResourceOperation.value = ResourceOperationResult.Success(message = successMsg)

            state.copy(
                settlement = finalizedSettlement,
                resources = finalizedResources,
                dayLogs = listOf(successMsg) + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Upgrade an existing settlement building.
     */
    fun upgradeBuilding(buildingId: String) {
        _gameState.update { state ->
            val bld = state.settlement.buildings.find { it.id == buildingId } ?: return@update state
            if (bld.isMaxLevel || !bld.isConstructed) return@update state

            val nextLevel = bld.level + 1
            val matCost = bld.upgradeCostMaterials
            val moneyCost = bld.upgradeCostMoney

            if (state.resources.materials < matCost || state.resources.money < moneyCost) {
                val errorMsg = "Недостаточно ресурсов для улучшения «${bld.name}» (нужно $matCost материалов, $moneyCost кр.)"
                val deficitMat = (matCost - state.resources.materials).coerceAtLeast(0)
                _lastResourceOperation.value = ResourceOperationResult.Failure(
                    reason = ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE,
                    message = errorMsg,
                    deficitType = ResourceType.MATERIALS,
                    deficitAmount = deficitMat
                )
                return@update state
            }

            // Deduct resources
            val updatedResources = state.resources.copy(
                materials = state.resources.materials - matCost,
                money = state.resources.money - moneyCost
            )

            val updatedBuilding = bld.copy(
                level = nextLevel,
                upgradeCostMaterials = bld.calculateNextUpgradeMaterials(nextLevel),
                upgradeCostMoney = bld.calculateNextUpgradeMoney(nextLevel)
            )

            val updatedBuildings = state.settlement.buildings.map {
                if (it.id == buildingId) updatedBuilding else it
            }

            // Award Settlement XP & evaluate level-up
            val (settlementAfterXp, leveledUp) = state.settlement.copy(buildings = updatedBuildings)
                .addXp(bld.xpRewardOnUpgrade)

            // Synchronize settlement limits & unlocked buildings
            val finalizedSettlement = recalculateSettlementState(settlementAfterXp)
            val finalizedResources = updatedResources.copy(
                warehouseMaxCapacity = calculateWarehouseCapacity(finalizedSettlement.buildings)
            )

            val updatedQuests = state.quests.map { q ->
                if (q.id == "quest_3" && q.status == QuestStatus.IN_PROGRESS) {
                    q.copy(progress = 1, status = QuestStatus.READY_TO_CLAIM)
                } else q
            }

            val successMsg = buildString {
                append("Завершена модернизация «${bld.name}» до уровня $nextLevel! (+${bld.xpRewardOnUpgrade} XP)")
                if (leveledUp) {
                    append(" 🌟 ПОСЕЛЕНИЕ ПОВЫСИЛО УРОВЕНЬ ДО ${finalizedSettlement.level}! Статус: ${finalizedSettlement.tier.titleRu}!")
                }
            }

            _lastResourceOperation.value = ResourceOperationResult.Success(message = successMsg)

            state.copy(
                settlement = finalizedSettlement,
                resources = finalizedResources,
                quests = updatedQuests,
                dayLogs = listOf(successMsg) + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Adds settlement XP, triggering potential level-ups and building unlocks.
     */
    fun addSettlementXp(amount: Int) {
        _gameState.update { state ->
            val (updatedSettlement, leveledUp) = state.settlement.addXp(amount)
            val finalizedSettlement = recalculateSettlementState(updatedSettlement)

            val log = if (leveledUp) {
                "🌟 Развитие поселения: Достигнут уровень ${finalizedSettlement.level}! Новые возможности разблокированы."
            } else {
                "Получено +$amount XP прогресса развития поселения."
            }

            state.copy(
                settlement = finalizedSettlement,
                dayLogs = listOf(log) + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Changes global settlement reputation using ReputationManager.
     */
    fun changeSettlementReputation(delta: Int, reason: String, sourceTitle: String = "Совет Аванпоста") {
        _gameState.update { state ->
            val (newState, _) = ReputationManager.changeSettlementReputation(
                state = state,
                delta = delta,
                sourceTitle = sourceTitle,
                reason = reason,
                type = ReputationChangeType.DEBUG_MOD
            )
            newState
        }
    }

    /**
     * Changes diplomatic relation with a faction using ReputationManager.
     */
    fun changeFactionRelation(factionId: String, delta: Int, reason: String, sourceTitle: String = "Дипломатический контакт") {
        _gameState.update { state ->
            val (newState, _) = ReputationManager.changeFactionRelation(
                state = state,
                factionId = factionId,
                delta = delta,
                sourceTitle = sourceTitle,
                reason = reason,
                type = ReputationChangeType.DEBUG_MOD
            )
            newState
        }
    }

    /**
     * Resets reputation and faction relations to initial defaults for debugging.
     */
    fun resetReputationDebug() {
        _gameState.update { state ->
            state.copy(
                settlement = state.settlement.copy(reputation = 50),
                factionRelations = ReputationBalanceConfig.createInitialFactionRelations(),
                reputationHistory = listOf(
                    ReputationHistoryEntry(
                        id = "rep_reset_${System.currentTimeMillis()}",
                        day = state.day,
                        gameDateTime = state.gameDateTime,
                        sourceTitle = "Сброс дипломатии",
                        reasonDescription = "Параметры репутации и фракций сброшены к начальным значениям (Debug)",
                        delta = 0,
                        type = ReputationChangeType.DEBUG_MOD
                    )
                ),
                dayLogs = listOf("Дипломатия и репутация поселения сброшены к базовым значениям.") + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Recalculates settlement metrics (limits, building unlocked states, defense, population, daily consumption).
     */
    private fun recalculateSettlementState(
        settlement: Settlement,
        characterCount: Int = _gameState.value.characters.size
    ): Settlement {
        val hqLevel = settlement.buildings.find { it.type == BuildingType.HQ_COMMAND && it.isConstructed }?.level ?: 1
        val defenseLevel = settlement.buildings.find { it.type == BuildingType.DEFENSE_PERIMETER && it.isConstructed }?.level ?: 1
        val armoryLevel = settlement.buildings.find { it.type == BuildingType.ARMORY_LAB && it.isConstructed }?.level ?: 0

        // Unlocked buildings check
        val updatedBuildings = settlement.buildings.map { bld ->
            if (bld.status == BuildingStatus.LOCKED && settlement.level >= bld.requiredSettlementLevel) {
                bld.copy(status = BuildingStatus.AVAILABLE_TO_BUILD)
            } else bld
        }

        val calculatedMaxPop = 15 + (hqLevel * 5) + (settlement.level * 3)
        val calculatedDefense = 30 + (defenseLevel * 20) + (armoryLevel * 15) + (settlement.level * 5)
        val calculatedReputation = settlement.reputation.coerceIn(ReputationBalanceConfig.MIN_POINTS, ReputationBalanceConfig.MAX_POINTS)

        return settlement.copy(
            buildings = updatedBuildings,
            population = characterCount,
            maxPopulation = calculatedMaxPop,
            defenseRating = calculatedDefense,
            reputation = calculatedReputation,
            tier = SettlementTier.fromLevel(settlement.level),
            dailyFoodConsumption = characterCount,
            dailyWaterConsumption = characterCount
        )
    }

    private fun calculateWarehouseCapacity(
        buildings: List<Building>,
        technologies: List<ResearchTech> = _gameState.value.technologies
    ): Int {
        val storageLevel = buildings.find { it.type == BuildingType.STORAGE_DEPOT && it.isConstructed }?.level ?: 1
        val baseCapacity = 500 + (storageLevel * 300)
        val techBonus = ResearchConfig.getStorageBonus(technologies)
        return baseCapacity + techBonus
    }

    // -------------------------------------------------------------
    // Population & Resident Management (Point 8)
    // -------------------------------------------------------------

    /**
     * Recruits a new survivor into the settlement with strict capacity & resource checks.
     */
    fun recruitSurvivor(forcedRole: CharacterRole? = null): ResourceOperationResult {
        var opResult: ResourceOperationResult = ResourceOperationResult.Failure(
            ResourceOperationResult.FailureReason.INVALID_AMOUNT,
            "Ошибка приёма выжившего"
        )

        _gameState.update { state ->
            // 1. Capacity check against derived maxPopulation
            if (state.characters.size >= state.settlement.maxPopulation) {
                val errorMsg = "В поселении нет свободных мест для жилья! Достигнут лимит (${state.settlement.maxPopulation} чел.). Улучшите Штаб или жилые постройки!"
                opResult = ResourceOperationResult.Failure(
                    reason = ResourceOperationResult.FailureReason.STORAGE_FULL,
                    message = errorMsg
                )
                _lastResourceOperation.value = opResult
                return@update state
            }

            // 2. Resource cost verification
            val cost = SurvivorGenerator.RECRUITMENT_COST
            val moneyCost = cost[ResourceType.MONEY] ?: 60
            val foodCost = cost[ResourceType.FOOD] ?: 15
            val waterCost = cost[ResourceType.WATER] ?: 15

            if (state.resources.money < moneyCost || state.resources.food < foodCost || state.resources.water < waterCost) {
                val deficitType = when {
                    state.resources.money < moneyCost -> ResourceType.MONEY
                    state.resources.food < foodCost -> ResourceType.FOOD
                    else -> ResourceType.WATER
                }
                val deficitAmount = when (deficitType) {
                    ResourceType.MONEY -> moneyCost - state.resources.money
                    ResourceType.FOOD -> foodCost - state.resources.food
                    else -> waterCost - state.resources.water
                }
                val errorMsg = "Недостаточно ресурсов для приёма выжившего! Нужно: $moneyCost Кр, $foodCost Еды, $waterCost Воды."
                opResult = ResourceOperationResult.Failure(
                    reason = ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE,
                    message = errorMsg,
                    deficitType = deficitType,
                    deficitAmount = deficitAmount
                )
                _lastResourceOperation.value = opResult
                return@update state
            }

            // 3. Deduct resources safely
            val updatedResources = state.resources.copy(
                money = state.resources.money - moneyCost,
                food = state.resources.food - foodCost,
                water = state.resources.water - waterCost
            )

            // 4. Generate new authentic survivor
            val newSurvivor = SurvivorGenerator.generateSurvivor(forcedRole = forcedRole)
            val updatedCharacters = state.characters + newSurvivor

            // 5. Recalculate settlement population & daily consumption
            val updatedSettlement = recalculateSettlementState(state.settlement, updatedCharacters.size)

            val successMsg = "Новый житель прибыл в аванпост: ${newSurvivor.name} (${newSurvivor.role.titleRu}, спец: ${newSurvivor.specialization}). Население: ${updatedCharacters.size}/${updatedSettlement.maxPopulation}."
            opResult = ResourceOperationResult.Success(
                message = successMsg
            )
            _lastResourceOperation.value = opResult

            state.copy(
                resources = updatedResources,
                characters = updatedCharacters,
                settlement = updatedSettlement,
                dayLogs = listOf(successMsg) + state.dayLogs.take(19)
            )
        }

        return opResult
    }

    /**
     * Safely retires or dismisses a resident from the settlement.
     */
    fun retireResident(characterId: String): ResourceOperationResult {
        var opResult: ResourceOperationResult = ResourceOperationResult.Failure(
            ResourceOperationResult.FailureReason.INVALID_AMOUNT,
            "Ошибка увольнения жителя"
        )

        _gameState.update { state ->
            val char = state.characters.find { it.id == characterId }
            if (char == null) {
                opResult = ResourceOperationResult.Failure(
                    ResourceOperationResult.FailureReason.INVALID_AMOUNT,
                    "Житель не найден"
                )
                return@update state
            }

            if (char.status == CharacterStatus.ON_EXPEDITION) {
                opResult = ResourceOperationResult.Failure(
                    ResourceOperationResult.FailureReason.INVALID_AMOUNT,
                    "Нельзя исключить жителя, находящегося в активной экспедиции!"
                )
                _lastResourceOperation.value = opResult
                return@update state
            }

            if (state.characters.size <= 1) {
                opResult = ResourceOperationResult.Failure(
                    ResourceOperationResult.FailureReason.INVALID_AMOUNT,
                    "Нельзя распустить последнего жителя аванпоста!"
                )
                _lastResourceOperation.value = opResult
                return@update state
            }

            val updatedChars = state.characters.filterNot { it.id == characterId }
            val updatedSquadMemberIds = state.squad.memberIds.filterNot { it == characterId }
            val updatedLeaderId = if (state.squad.leaderId == characterId) updatedSquadMemberIds.firstOrNull() else state.squad.leaderId
            val updatedSquad = state.squad.copy(
                memberIds = updatedSquadMemberIds,
                leaderId = updatedLeaderId,
                status = if (updatedSquadMemberIds.isEmpty()) SquadStatus.EMPTY else state.squad.status
            )
            val updatedSettlement = recalculateSettlementState(state.settlement, updatedChars.size)

            val msg = "Житель ${char.name} покинул аванпост. Население: ${updatedChars.size}/${updatedSettlement.maxPopulation}."
            opResult = ResourceOperationResult.Success(message = msg)
            _lastResourceOperation.value = opResult

            state.copy(
                characters = updatedChars,
                squad = updatedSquad,
                selectedSquadIds = updatedSquadMemberIds.toSet(),
                settlement = updatedSettlement,
                dayLogs = listOf(msg) + state.dayLogs.take(19)
            )
        }

        return opResult
    }

    /**
     * Heals an injured resident in the medical clinic using 1 medicine unit.
     */
    fun healResidentInClinic(characterId: String): ResourceOperationResult {
        var opResult: ResourceOperationResult = ResourceOperationResult.Failure(
            ResourceOperationResult.FailureReason.INVALID_AMOUNT,
            "Ошибка лечения"
        )

        _gameState.update { state ->
            val char = state.characters.find { it.id == characterId }
            if (char == null) return@update state

            if (char.health >= char.maxHealth) {
                opResult = ResourceOperationResult.Failure(
                    ResourceOperationResult.FailureReason.INVALID_AMOUNT,
                    "${char.name} полностью здоров!"
                )
                _lastResourceOperation.value = opResult
                return@update state
            }

            val medicineAvailable = state.resources.extraResources[ResourceType.MEDICINE] ?: 0
            if (medicineAvailable < 1) {
                opResult = ResourceOperationResult.Failure(
                    reason = ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE,
                    message = "В медпункте закончились медикаменты! Требуется 1 ед. Медикаментов.",
                    deficitType = ResourceType.MEDICINE,
                    deficitAmount = 1
                )
                _lastResourceOperation.value = opResult
                return@update state
            }

            val updatedExtraRes = state.resources.extraResources.toMutableMap()
            updatedExtraRes[ResourceType.MEDICINE] = medicineAvailable - 1

            val updatedChars = state.characters.map {
                if (it.id == characterId) {
                    it.copy(
                        health = it.maxHealth,
                        status = if (it.status == CharacterStatus.INJURED) {
                            if (state.selectedSquadIds.contains(it.id)) CharacterStatus.IN_SQUAD else CharacterStatus.READY
                        } else it.status
                    )
                } else it
            }

            val msg = "Медпункт: ${char.name} полностью вылечен (израсходована 1 ед. медикаментов)."
            opResult = ResourceOperationResult.Success(
                type = ResourceType.MEDICINE,
                amountChanged = -1,
                message = msg
            )
            _lastResourceOperation.value = opResult

            state.copy(
                characters = updatedChars,
                resources = state.resources.copy(extraResources = updatedExtraRes),
                dayLogs = listOf(msg) + state.dayLogs.take(19)
            )
        }

        return opResult
    }

    /**
     * Debug shortcut: Instantly add a procedural survivor bypassing costs (respecting capacity).
     */
    fun debugAddSurvivor(forcedRole: CharacterRole? = null) = debugAddRandomSurvivor(forcedRole)

    fun debugAddRandomSurvivor(forcedRole: CharacterRole? = null) {
        _gameState.update { state ->
            val newSurvivor = SurvivorGenerator.generateSurvivor(forcedRole = forcedRole)
            val updatedChars = state.characters + newSurvivor
            val updatedSettlement = recalculateSettlementState(state.settlement, updatedChars.size)
            val msg = "Тест: Добавлен выживший ${newSurvivor.name} (${newSurvivor.role.titleRu})."
            _lastResourceOperation.value = ResourceOperationResult.Success(message = msg)
            state.copy(
                characters = updatedChars,
                settlement = updatedSettlement,
                dayLogs = listOf(msg) + state.dayLogs.take(19)
            )
        }
    }

    // -------------------------------------------------------------
    // Debug & Development Cheat Tools
    // -------------------------------------------------------------

    fun debugAddSettlementXp(amount: Int = 150) {
        addSettlementXp(amount)
    }

    fun debugLevelUpSettlement() {
        val needed = _gameState.value.settlement.xpToNextLevel - _gameState.value.settlement.xp
        addSettlementXp(needed.coerceAtLeast(10))
    }

    fun debugConstructAllBuildings() {
        _gameState.update { state ->
            val upgradedBuildings = state.settlement.buildings.map { bld ->
                bld.copy(
                    level = if (bld.level == 0) 1 else bld.level,
                    status = BuildingStatus.OPERATIONAL
                )
            }
            val sett = state.settlement.copy(
                buildings = upgradedBuildings,
                level = (state.settlement.level + 1).coerceAtLeast(4)
            )
            val finalSett = recalculateSettlementState(sett, state.characters.size)
            state.copy(
                settlement = finalSett,
                resources = state.resources.copy(
                    warehouseMaxCapacity = calculateWarehouseCapacity(finalSett.buildings)
                ),
                dayLogs = listOf("Тест: Все здания поселения разблокированы и построены!") + state.dayLogs.take(19)
            )
        }
    }

    // -------------------------------------------------------------
    // Squad & Vehicle Management
    // -------------------------------------------------------------

    /**
     * Adds a resident to the expedition squad with comprehensive validation.
     */
    fun addSquadMember(characterId: String): SquadOperationResult {
        var opResult: SquadOperationResult = SquadOperationResult.Failure("Неизвестная ошибка формирования отряда.")
        _gameState.update { state ->
            val char = state.characters.find { it.id == characterId }
            if (char == null) {
                opResult = SquadOperationResult.Failure("Житель не найден в поселении.")
                return@update state
            }
            if (char.status == CharacterStatus.ON_EXPEDITION) {
                opResult = SquadOperationResult.Failure("Житель уже находится на задании в пустоши!")
                return@update state
            }
            if (char.status == CharacterStatus.INJURED) {
                opResult = SquadOperationResult.Failure("Раненый житель не может отправиться в отряд. Требуется лечение в медпункте.")
                return@update state
            }
            if (state.squad.memberIds.contains(characterId)) {
                opResult = SquadOperationResult.Failure("${char.name} уже входит в состав отряда.")
                return@update state
            }

            val maxCap = SquadLimits.getMaxCapacity(state.selectedVehicle)
            if (state.squad.memberIds.size >= maxCap) {
                val vehName = state.selectedVehicle?.name ?: "Пеший ход"
                opResult = SquadOperationResult.Failure("Отряд укомплектован ($maxCap/$maxCap мест для «$vehName»).")
                return@update state
            }

            val updatedMemberIds = state.squad.memberIds + characterId
            val newLeaderId = if (state.squad.leaderId == null || !updatedMemberIds.contains(state.squad.leaderId)) {
                characterId
            } else {
                state.squad.leaderId
            }

            val updatedSquad = state.squad.copy(
                memberIds = updatedMemberIds,
                leaderId = newLeaderId,
                status = SquadStatus.READY
            )

            val updatedChars = state.characters.map { c ->
                if (c.id == characterId) c.copy(status = CharacterStatus.IN_SQUAD) else c
            }

            val msg = "Житель ${char.name} зачислен в экспедиционный отряд."
            opResult = SquadOperationResult.Success(msg)

            state.copy(
                squad = updatedSquad,
                selectedSquadIds = updatedMemberIds.toSet(),
                characters = updatedChars,
                dayLogs = listOf("👥 Отряд: $msg") + state.dayLogs.take(19)
            )
        }
        _lastSquadOperation.value = opResult
        return opResult
    }

    /**
     * Removes a resident from the expedition squad and returns them to base.
     */
    fun removeSquadMember(characterId: String): SquadOperationResult {
        var opResult: SquadOperationResult = SquadOperationResult.Failure("Житель не состоит в отряде.")
        _gameState.update { state ->
            val char = state.characters.find { it.id == characterId }
            if (char == null) {
                opResult = SquadOperationResult.Failure("Житель не найден.")
                return@update state
            }
            if (char.status == CharacterStatus.ON_EXPEDITION) {
                opResult = SquadOperationResult.Failure("Нельзя исключить участника во время активной экспедиции!")
                return@update state
            }
            if (!state.squad.memberIds.contains(characterId)) {
                opResult = SquadOperationResult.Failure("${char.name} не входит в состав отряда.")
                return@update state
            }

            val updatedMemberIds = state.squad.memberIds.filterNot { it == characterId }
            val newLeaderId = if (state.squad.leaderId == characterId) {
                updatedMemberIds.firstOrNull()
            } else {
                state.squad.leaderId
            }

            val updatedSquad = state.squad.copy(
                memberIds = updatedMemberIds,
                leaderId = newLeaderId,
                status = if (updatedMemberIds.isEmpty()) SquadStatus.EMPTY else SquadStatus.READY
            )

            val updatedChars = state.characters.map { c ->
                if (c.id == characterId) c.copy(status = CharacterStatus.READY) else c
            }

            val msg = "${char.name} возвращён в гарнизон аванпоста."
            opResult = SquadOperationResult.Success(msg)

            state.copy(
                squad = updatedSquad,
                selectedSquadIds = updatedMemberIds.toSet(),
                characters = updatedChars,
                dayLogs = listOf("👥 Отряд: $msg") + state.dayLogs.take(19)
            )
        }
        _lastSquadOperation.value = opResult
        return opResult
    }

    /**
     * Toggles a resident in or out of the squad.
     */
    fun toggleSquadMember(characterId: String): SquadOperationResult {
        val currentState = _gameState.value
        return if (currentState.squad.memberIds.contains(characterId)) {
            removeSquadMember(characterId)
        } else {
            addSquadMember(characterId)
        }
    }

    /**
     * Designates a specific squad member as the leader/commander.
     */
    fun setSquadLeader(characterId: String): SquadOperationResult {
        var opResult: SquadOperationResult = SquadOperationResult.Failure("Не удалось назначить командира.")
        _gameState.update { state ->
            if (!state.squad.memberIds.contains(characterId)) {
                opResult = SquadOperationResult.Failure("Командиром может быть назначен только действующий боец отряда.")
                return@update state
            }
            val char = state.characters.find { it.id == characterId }
            val updatedSquad = state.squad.copy(leaderId = characterId)
            val msg = "${char?.name ?: "Боец"} назначен командиром отряда «${state.squad.name}»."
            opResult = SquadOperationResult.Success(msg)

            state.copy(
                squad = updatedSquad,
                dayLogs = listOf("⭐ Командир: $msg") + state.dayLogs.take(19)
            )
        }
        _lastSquadOperation.value = opResult
        return opResult
    }

    /**
     * Clears all members from the current squad.
     */
    fun clearSquad(): SquadOperationResult {
        var opResult: SquadOperationResult = SquadOperationResult.Success("Отряд распущен.")
        _gameState.update { state ->
            if (state.isCurrentlyTraveling) {
                opResult = SquadOperationResult.Failure("Нельзя расформировать отряд в пути!")
                return@update state
            }
            val squadSet = state.squad.memberIds.toSet()
            val updatedChars = state.characters.map { c ->
                if (squadSet.contains(c.id) && c.status != CharacterStatus.ON_EXPEDITION) {
                    c.copy(status = CharacterStatus.READY)
                } else c
            }

            state.copy(
                squad = Squad(
                    id = state.squad.id,
                    name = state.squad.name,
                    memberIds = emptyList(),
                    leaderId = null,
                    assignedVehicleId = state.squad.assignedVehicleId,
                    status = SquadStatus.EMPTY
                ),
                selectedSquadIds = emptySet(),
                characters = updatedChars,
                dayLogs = listOf("👥 Отряд распущен. Все бойцы вернулись в гарнизон.") + state.dayLogs.take(19)
            )
        }
        _lastSquadOperation.value = opResult
        return opResult
    }

    fun selectVehicle(vehicleId: String) {
        _gameState.update { state ->
            val veh = state.vehicles.find { it.id == vehicleId } ?: return@update state
            if (veh.isUnlocked && veh.isAvailable) {
                state.copy(
                    selectedVehicleId = vehicleId,
                    squad = state.squad.copy(assignedVehicleId = vehicleId)
                )
            } else state
        }
    }

    // -------------------------------------------------------------
    // System 16: Expedition Preparation Draft & Lifecycle System
    // -------------------------------------------------------------

    fun clearLastExpeditionResult() {
        _lastExpeditionTransactionResult.value = null
    }

    /**
     * Initializes or updates the preparation draft state for the target destination.
     */
    fun initExpeditionDraft(locationId: String) {
        val state = _gameState.value
        val destLoc = state.locations.find { it.id == locationId } ?: return

        val currentDraft = _expeditionDraft.value
        if (currentDraft != null && currentDraft.destinationLocationId == locationId) {
            return // Keep existing draft if already working on same destination
        }

        val initialParticipantIds = if (state.squad.memberIds.isNotEmpty()) {
            state.squad.memberIds.filter { id ->
                val c = state.characters.find { it.id == id }
                c != null && c.status != CharacterStatus.INJURED && c.status != CharacterStatus.ON_EXPEDITION
            }
        } else {
            listOfNotNull(state.characters.firstOrNull { it.status == CharacterStatus.READY }?.id)
        }

        val initialLeaderId = if (initialParticipantIds.contains(state.squad.leaderId)) {
            state.squad.leaderId
        } else {
            initialParticipantIds.firstOrNull()
        }

        val preferredVehicleId = state.selectedVehicleId ?: state.vehicles.firstOrNull { it.isAvailable }?.id
        val travelMode = _selectedTravelMode.value

        val recSupplies = ExpeditionSupplyCalculator.calculateRecommendedSupplies(
            destination = destLoc,
            transportMode = travelMode,
            participantCount = initialParticipantIds.size,
            technologies = state.technologies,
            origin = state.locations.find { it.id == state.currentLocationId },
            vehicle = state.vehicles.find { it.id == preferredVehicleId },
            availableResources = state.resources
        )

        _expeditionDraft.value = ExpeditionPreparationState(
            destinationLocationId = locationId,
            originLocationId = state.currentLocationId,
            participantIds = initialParticipantIds,
            leaderId = initialLeaderId,
            travelMode = travelMode,
            selectedVehicleId = preferredVehicleId,
            supplies = recSupplies
        )
    }

    fun updateDraftParticipants(participantIds: List<String>) {
        _expeditionDraft.update { draft ->
            draft?.copy(
                participantIds = participantIds,
                leaderId = if (participantIds.contains(draft.leaderId)) draft.leaderId else participantIds.firstOrNull()
            )
        }
    }

    fun toggleDraftParticipant(characterId: String) {
        _expeditionDraft.update { draft ->
            draft?.withToggledParticipant(characterId)
        }
    }

    fun setDraftLeader(leaderId: String) {
        _expeditionDraft.update { draft ->
            draft?.withLeader(leaderId)
        }
    }

    fun setDraftTravelMode(mode: TravelTransportMode) {
        _selectedTravelMode.value = mode
        _expeditionDraft.update { draft ->
            draft?.copy(travelMode = mode)
        }
    }

    fun setDraftVehicle(vehicleId: String?) {
        _expeditionDraft.update { draft ->
            draft?.copy(selectedVehicleId = vehicleId)
        }
        if (vehicleId != null) {
            selectVehicle(vehicleId)
        }
    }

    fun setDraftSupply(resourceType: ResourceType, amount: Int) {
        val state = _gameState.value
        val availableInWarehouse = state.resources[resourceType]
        val clampedAmount = amount.coerceIn(0, availableInWarehouse)

        _expeditionDraft.update { draft ->
            draft?.withSupply(resourceType, clampedAmount)
        }
    }

    fun applyRecommendedSupplies() {
        val state = _gameState.value
        val draft = _expeditionDraft.value ?: return
        val destLoc = state.locations.find { it.id == draft.destinationLocationId } ?: return
        val vehicle = state.vehicles.find { it.id == draft.selectedVehicleId }

        val recSupplies = ExpeditionSupplyCalculator.calculateRecommendedSupplies(
            destination = destLoc,
            transportMode = draft.travelMode,
            participantCount = draft.participantCount,
            technologies = state.technologies,
            origin = state.locations.find { it.id == draft.originLocationId },
            vehicle = vehicle,
            availableResources = state.resources
        )

        _expeditionDraft.update { it?.copy(supplies = recSupplies) }
    }

    fun clearExpeditionDraft() {
        _expeditionDraft.value = null
    }

    /**
     * Validates current draft against the active game state.
     */
    fun validateExpeditionDraft(): ExpeditionValidationResult {
        val state = _gameState.value
        val draft = _expeditionDraft.value ?: ExpeditionPreparationState(destinationLocationId = state.locations.first().id)
        return ExpeditionPreparationValidator.validate(draft, state)
    }

    /**
     * Atomically validates, deducts resources, marks participants/vehicle as active,
     * and dispatches the prepared expedition.
     */
    fun startPreparedExpedition(targetLocationId: String? = null): ExpeditionTransactionResult {
        var result: ExpeditionTransactionResult = ExpeditionTransactionResult.Failure(
            blockingIssues = listOf("Подготовка не завершена."),
            message = "Невозможно начать экспедицию."
        )

        _gameState.update { state ->
            val draft = _expeditionDraft.value
            val destLocId = targetLocationId ?: draft?.destinationLocationId ?: state.locations.first().id
            val destLoc = state.locations.find { it.id == destLocId }
            if (destLoc == null) {
                result = ExpeditionTransactionResult.Failure(
                    blockingIssues = listOf("Локация не найдена на карте."),
                    message = "Локация не существует."
                )
                return@update state
            }

            val effectiveDraft = draft?.copy(destinationLocationId = destLoc.id) ?: run {
                val pIds = if (state.squad.memberIds.isNotEmpty()) state.squad.memberIds else listOfNotNull(state.characters.firstOrNull()?.id)
                val vehId = state.selectedVehicleId ?: state.vehicles.firstOrNull()?.id
                val recSupplies = ExpeditionSupplyCalculator.calculateRecommendedSupplies(
                    destination = destLoc,
                    transportMode = _selectedTravelMode.value,
                    participantCount = pIds.size,
                    technologies = state.technologies,
                    origin = state.locations.find { it.id == state.currentLocationId },
                    vehicle = state.vehicles.find { it.id == vehId },
                    availableResources = state.resources
                )
                ExpeditionPreparationState(
                    destinationLocationId = destLoc.id,
                    originLocationId = state.currentLocationId,
                    participantIds = pIds,
                    leaderId = state.squad.leaderId ?: pIds.firstOrNull(),
                    travelMode = _selectedTravelMode.value,
                    selectedVehicleId = vehId,
                    supplies = recSupplies
                )
            }

            val validation = ExpeditionPreparationValidator.validate(effectiveDraft, state)
            if (!validation.canDepart) {
                result = ExpeditionTransactionResult.Failure(
                    blockingIssues = validation.blockingIssues,
                    message = "Экспедиция не готова: ${validation.blockingIssues.firstOrNull() ?: "Проверьте условия"}"
                )
                return@update state
            }

            val targetVehicle = if (effectiveDraft.travelMode.requiresVehicle) {
                state.vehicles.find { it.id == effectiveDraft.selectedVehicleId }
                    ?: TravelCalculator.resolveVehicle(effectiveDraft.travelMode, state)
            } else null

            // Safe atomic resource deduction
            var updatedResources = state.resources
            effectiveDraft.supplies.forEach { (resType, qty) ->
                if (qty > 0) {
                    val currentVal = updatedResources[resType]
                    updatedResources = updatedResources.withResource(resType, (currentVal - qty).coerceAtLeast(0))
                }
            }

            // Mark participants as ON_EXPEDITION
            val participantSet = effectiveDraft.participantIds.toSet()
            val squadCharacters = state.characters.filter { participantSet.contains(it.id) }
            val updatedCharacters = state.characters.map { char ->
                if (participantSet.contains(char.id)) {
                    char.copy(status = CharacterStatus.ON_EXPEDITION)
                } else char
            }

            // Mark vehicle as IN_USE
            val updatedVehicles = if (targetVehicle != null && effectiveDraft.travelMode.requiresVehicle) {
                state.vehicles.map { veh ->
                    if (veh.id == targetVehicle.id) {
                        veh.copy(status = VehicleStatus.IN_USE)
                    } else veh
                }
            } else {
                state.vehicles
            }

            val travelId = "travel_${System.currentTimeMillis()}"
            val vehicleName = targetVehicle?.name ?: effectiveDraft.travelMode.titleRu
            val originLoc = state.locations.find { it.id == effectiveDraft.originLocationId }

            val travelState = TravelState(
                id = travelId,
                fromLocationId = effectiveDraft.originLocationId,
                toLocationId = destLoc.id,
                transportMode = effectiveDraft.travelMode,
                vehicleId = targetVehicle?.id,
                vehicleName = vehicleName,
                participantIds = effectiveDraft.participantIds,
                leaderId = effectiveDraft.leaderId,
                cargoCapacityKg = validation.cargoSummary.totalCapacityKg,
                distanceKm = validation.travelCost.distanceKm,
                traveledKm = 0f,
                progressFraction = 0f,
                status = TravelStatus.TRAVELING,
                isReturning = destLoc.isPlayerBase,
                startTimestamp = System.currentTimeMillis(),
                estimatedHours = validation.travelCost.estimatedDurationHours,
                costPaid = validation.travelCost,
                statusMessage = "Отряд выдвинулся к точке «${destLoc.name}» на $vehicleName (${validation.travelCost.distanceKm} км).",
                currentSectorName = destLoc.sectorCode,
                travelLogs = listOf(
                    "Снаряжение укомплектовано. Провизия: ${effectiveDraft.supplies.entries.joinToString { "${it.key.titleRu} ${it.value}" }}.",
                    "Группа под командованием ${squadCharacters.find { it.id == effectiveDraft.leaderId }?.name ?: "Лидера"} покинула [${originLoc?.name ?: "Базу"}]."
                )
            )

            val expId = "exp_${System.currentTimeMillis()}"
            val initialEvent = createEventForLocation(destLoc)
            val newExpedition = Expedition(
                id = expId,
                location = destLoc,
                squad = squadCharacters.ifEmpty { listOf(state.characters.first()) },
                vehicle = targetVehicle ?: state.vehicles.first(),
                status = ExpeditionStatus.TRAVELING,
                phase = ExpeditionPhase.TRAVELING_TO_LOCATION,
                currentStep = 0,
                currentEvent = initialEvent,
                logs = listOf("Отряд выдвинулся в путь к сектору «${destLoc.name}»."),
                leaderId = effectiveDraft.leaderId,
                travelMode = effectiveDraft.travelMode,
                travelId = travelId,
                supplies = effectiveDraft.supplies,
                carriedItemIds = effectiveDraft.carriedItemIds,
                cargoCapacityKg = validation.cargoSummary.totalCapacityKg,
                cargoWeightKg = validation.cargoSummary.totalCurrentWeightKg,
                startTimestamp = System.currentTimeMillis(),
                startDateTime = state.gameDateTime
            )

            val logMsg = "🚀 Экспедиция: Отряд (${effectiveDraft.participantCount} чел.) отправился в «${destLoc.name}» ($vehicleName). Запас: ${effectiveDraft.supplies.entries.filter { it.value > 0 }.joinToString { "${it.key.symbol}${it.value}" }}."

            result = ExpeditionTransactionResult.Success(
                expedition = newExpedition,
                travelState = travelState,
                consumedSupplies = effectiveDraft.supplies,
                message = logMsg
            )

            _lastExpeditionTransactionResult.value = result
            _expeditionDraft.value = null

            state.copy(
                resources = updatedResources,
                characters = updatedCharacters,
                vehicles = updatedVehicles,
                activeExpedition = newExpedition,
                activeTravel = travelState,
                dayLogs = listOf(logMsg) + state.dayLogs.take(19)
            )
        }

        return result
    }

    /**
     * Legacy helper for preparing an expedition (backward-compatible).
     */
    fun prepareExpedition(locationId: String) {
        initExpeditionDraft(locationId)
    }

    /**
     * Legacy helper for launching expedition.
     */
    fun launchExpedition() {
        startPreparedExpedition()
    }

    private fun createEventForLocation(location: Location): ExpeditionEvent {
        return EventCatalog.ALL_EVENTS.firstOrNull { it.allowedLocationTypes.isEmpty() || it.allowedLocationTypes.contains(location.type) }
            ?: EventCatalog.EVT_SCAVENGE_SUPPLIES
    }

    /**
     * Resolve a choice during an expedition exploration phase.
     */
    fun resolveExpeditionChoice(chooseOptionA: Boolean) {
        val exp = _gameState.value.activeExpedition ?: return
        val activeEvt = exp.activeEventState
        if (activeEvt != null) {
            val choice = if (chooseOptionA) {
                activeEvt.event.choices.firstOrNull()
            } else {
                activeEvt.event.choices.getOrNull(1) ?: activeEvt.event.choices.firstOrNull()
            }
            if (choice != null) {
                executeEventChoice(choice.id)
                return
            }
        }

        // Fallback to legacy resolution if activeEventState is missing
        val currentEvent = exp.currentEvent ?: return
        if (currentEvent.choices.isNotEmpty()) {
            val choice = if (chooseOptionA) currentEvent.choices.first() else currentEvent.choices.getOrNull(1) ?: currentEvent.choices.first()
            val (updatedState, _) = EventOutcomeResolver.resolve(
                event = currentEvent,
                choice = choice,
                gameState = _gameState.value,
                expedition = exp
            )
            _gameState.value = updatedState
        }
    }

    /**
     * Executes a specific chosen action in the current active event.
     */
    fun executeEventChoice(choiceId: String, actorId: String? = null) {
        _gameState.update { state ->
            val exp = state.activeExpedition ?: return@update state
            val activeEvt = exp.activeEventState ?: return@update state
            val event = activeEvt.event
            val choice = event.choices.find { it.id == choiceId } ?: return@update state

            val effectiveActorId = actorId
                ?: _selectedActorIdForEvent.value
                ?: activeEvt.selectedActorId
                ?: exp.leader?.id
                ?: exp.squad.first().id

            // Check if choice requires combat
            if (choice.successOutcome.requiresCombat) {
                val outcome = choice.successOutcome
                val updatedExp = exp.copy(
                    activeEventState = activeEvt.copy(
                        selectedChoiceId = choiceId,
                        resolvedOutcome = outcome,
                        selectedActorId = effectiveActorId,
                        isResolved = true
                    ),
                    logs = exp.logs + listOf(outcome.narrativeText)
                )
                startCombatEncounter(updatedExp)
                return@update state
            }

            // Resolve outcome using deterministic EventOutcomeResolver
            val (updatedState, _) = EventOutcomeResolver.resolve(
                event = event,
                choice = choice,
                gameState = state,
                expedition = exp,
                actorId = effectiveActorId,
                seed = activeEvt.instanceSeed
            )

            updatedState
        }
    }

    /**
     * Advances the exploration step to trigger the next random event or conclude exploration.
     */
    fun advanceExpeditionExplorationStep() {
        _gameState.update { state ->
            val exp = state.activeExpedition ?: return@update state
            val nextStep = exp.currentStep + 1
            if (nextStep > exp.maxSteps) {
                val updatedExp = exp.copy(
                    phase = ExpeditionPhase.RETURNING,
                    status = ExpeditionStatus.RETURNING,
                    activeEventState = null,
                    currentEvent = null,
                    logs = exp.logs + "Все сектора исследованы. Отряд формирует колонну для возвращения в поселение."
                )
                return@update state.copy(
                    activeExpedition = updatedExp,
                    dayLogs = listOf("🚩 Разведка завершена: Отряд возвращается из «${exp.location.name}».") + state.dayLogs.take(19)
                )
            }

            val nextSeed = (exp.seed) + (nextStep * 31L)
            val nextEvent = EventSelector.selectNextEvent(
                catalog = EventCatalog.ALL_EVENTS,
                gameState = state,
                expedition = exp,
                seed = nextSeed
            ) ?: EventCatalog.EVT_SCAVENGE_SUPPLIES

            val newEventState = ActiveEventState(
                eventId = nextEvent.id,
                event = nextEvent,
                instanceSeed = nextSeed
            )

            val updatedExp = exp.copy(
                currentStep = nextStep,
                currentEvent = nextEvent,
                activeEventState = newEventState,
                logs = exp.logs + "Продвижение в глубь сектора (${nextStep}/${exp.maxSteps}). Событие: «${nextEvent.title}»."
            )

            state.copy(
                activeExpedition = updatedExp,
                dayLogs = listOf("🧭 Продвижение: ${exp.location.name} (шаг $nextStep/${exp.maxSteps}).") + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Debug helper to trigger a specific event by ID.
     */
    fun debugTriggerEvent(eventId: String) {
        val targetEvent = EventCatalog.ALL_EVENTS.find { it.id == eventId } ?: return
        _gameState.update { state ->
            val exp = state.activeExpedition ?: return@update state
            val eventState = ActiveEventState(
                eventId = targetEvent.id,
                event = targetEvent,
                instanceSeed = System.currentTimeMillis()
            )
            state.copy(
                activeExpedition = exp.copy(
                    currentEvent = targetEvent,
                    activeEventState = eventState,
                    logs = exp.logs + "Тест события: [${targetEvent.title}]."
                )
            )
        }
    }

    /**
     * Initialize tactical turn-based combat with live squad characters and deterministic enemies.
     */
    fun startCombatEncounter(
        expedition: Expedition? = null,
        sourceEventId: String? = null,
        sourceChoiceId: String? = null
    ) {
        val state = _gameState.value
        val exp = expedition ?: state.activeExpedition ?: return

        val combat = CombatInitiator.createCombatEncounter(
            expedition = exp,
            gameState = state,
            sourceEventId = sourceEventId,
            sourceChoiceId = sourceChoiceId
        )

        _gameState.update { s ->
            s.copy(
                activeCombat = combat,
                activeExpedition = exp.copy(
                    status = ExpeditionStatus.COMBAT,
                    phase = ExpeditionPhase.EXPLORING
                )
            )
        }

        // If enemy won the initiative roll, execute enemy turn(s)
        executeEnemyTurnsIfNeeded()
    }

    /**
     * Sets currently selected target in tactical combat. If an action is awaiting target, executes it immediately.
     */
    fun selectCombatTarget(targetId: String) {
        _gameState.update { state ->
            val combat = state.activeCombat ?: return@update state
            val targetingAct = combat.targetingAction
            if (targetingAct != null) {
                val result = CombatResolver.resolveAction(
                    state = combat.copy(selectedTargetId = targetId, targetingAction = null),
                    action = targetingAct,
                    targetId = targetId,
                    expedition = state.activeExpedition,
                    gameState = state
                )
                var nextState = result.updatedGameState ?: state
                nextState = nextState.copy(
                    activeCombat = result.updatedCombatState,
                    activeExpedition = result.updatedExpedition ?: nextState.activeExpedition
                )
                nextState
            } else {
                state.copy(activeCombat = combat.copy(selectedTargetId = targetId))
            }
        }
        executeEnemyTurnsIfNeeded()
    }

    /**
     * Sets or clears the active targeting action for SELECTING_TARGET mode.
     */
    fun setCombatTargetingAction(action: CombatAction?) {
        _gameState.update { state ->
            val combat = state.activeCombat ?: return@update state
            state.copy(activeCombat = combat.copy(targetingAction = action))
        }
    }

    /**
     * Cancels target selection mode.
     */
    fun cancelCombatTargeting() {
        setCombatTargetingAction(null)
    }

    /**
     * Executes a data-driven CombatAction (e.g. Basic Attack, Defend, Skill, Item, Pass).
     */
    fun executeCombatAction(action: CombatAction, targetId: String? = null) {
        val currentCombat = _gameState.value.activeCombat
        val actor = currentCombat?.currentActiveCombatant
        if (currentCombat != null && actor != null && targetId == null && (action.targetType == TargetType.ENEMY || action.targetType == TargetType.ALLY)) {
            val validTargets = com.example.domain.service.combat.CombatTargetValidator.getValidTargets(action, actor, currentCombat.combatants)
            if (validTargets.size > 1 && currentCombat.targetingAction == null) {
                setCombatTargetingAction(action)
                return
            }
        }

        _gameState.update { state ->
            val combat = state.activeCombat ?: return@update state
            val exp = state.activeExpedition

            val effectiveTarget = targetId ?: combat.selectedTargetId

            val result = CombatResolver.resolveAction(
                state = combat,
                action = action,
                targetId = effectiveTarget,
                expedition = exp,
                gameState = state
            )

            var nextState = result.updatedGameState ?: state
            nextState = nextState.copy(
                activeCombat = result.updatedCombatState,
                activeExpedition = result.updatedExpedition ?: nextState.activeExpedition
            )

            nextState
        }

        // Execute enemy turns if turn transitioned to an enemy
        executeEnemyTurnsIfNeeded()
    }

    /**
     * Legacy string-based combat turn execution for backwards compatibility.
     */
    fun executeCombatTurn(action: String) {
        val combat = _gameState.value.activeCombat ?: return
        val activeCombatant = combat.currentActiveCombatant

        val combatAction = when (action) {
            "ATTACK" -> CombatActionCatalog.BASIC_ATTACK
            "TACTICAL_SHOT" -> {
                if (activeCombatant?.role == CharacterRole.SOLDIER) CombatActionCatalog.SOLDIER_SNIPE
                else CombatActionCatalog.getSkillForRole(activeCombatant?.role)
            }
            "DEFEND" -> CombatActionCatalog.DEFEND
            "PASS" -> CombatActionCatalog.END_TURN
            else -> CombatActionCatalog.BASIC_ATTACK
        }

        executeCombatAction(combatAction)
    }

    /**
     * Uses a consumable item from expedition inventory in tactical combat.
     */
    fun useCombatItem(itemId: String, targetId: String? = null) {
        _gameState.update { state ->
            val combat = state.activeCombat ?: return@update state
            val exp = state.activeExpedition

            val result = CombatResolver.resolveItemUsage(
                state = combat,
                itemId = itemId,
                targetId = targetId ?: combat.selectedTargetId,
                expedition = exp,
                gameState = state
            )

            var nextState = result.updatedGameState ?: state
            nextState = nextState.copy(
                activeCombat = result.updatedCombatState,
                activeExpedition = result.updatedExpedition ?: nextState.activeExpedition
            )

            nextState
        }

        executeEnemyTurnsIfNeeded()
    }

    /**
     * Passes current turn for the active player combatant.
     */
    fun endCombatTurn() {
        executeCombatAction(CombatActionCatalog.END_TURN)
    }

    /**
     * Executes consecutive enemy turns until either a player turn is reached or combat ends.
     */
    private fun executeEnemyTurnsIfNeeded() {
        var currentCombat = _gameState.value.activeCombat ?: return
        var loopSafety = 0

        while (currentCombat.currentActiveCombatant?.team == CombatantTeam.ENEMY && !currentCombat.isEnded && loopSafety < 10) {
            val updatedCombat = EnemyTurnResolver.resolveEnemyTurn(currentCombat)
            currentCombat = updatedCombat
            _gameState.update { s -> s.copy(activeCombat = currentCombat) }
            loopSafety++
        }
    }

    /**
     * Claims tactical victory rewards, syncs combatant HP back to settlement and squad characters,
     * applies experience via CombatExperienceCalculator, and resumes normal expedition exploration.
     */
    fun finishCombatVictory() {
        _gameState.update { state ->
            val combat = state.activeCombat ?: return@update state
            val exp = state.activeExpedition ?: return@update state

            // Map current HP from combatants back to live characters
            val combatHpMap = combat.combatants
                .filter { it.team == CombatantTeam.PLAYER }
                .associate { it.id to it.currentHealth }

            val xpGain = combat.xpReward
            val leveledSquad = com.example.domain.service.combat.CombatExperienceCalculator.applyExperienceToSquad(
                squadCharacters = exp.squad,
                xpAmount = xpGain
            )

            val updatedSquad = leveledSquad.map { char ->
                val newHp = combatHpMap[char.id] ?: char.health
                char.copy(
                    health = newHp.coerceIn(0, char.maxHealth),
                    threatsNeutralizedCount = char.threatsNeutralizedCount + 1
                )
            }

            val squadIdSet = exp.squad.map { it.id }.toSet()
            val squadMap = updatedSquad.associateBy { it.id }

            val updatedAllCharacters = state.characters.map { char ->
                if (squadIdSet.contains(char.id)) {
                    squadMap[char.id] ?: char
                } else char
            }

            val updatedExp = exp.copy(
                status = ExpeditionStatus.EXPLORING,
                phase = ExpeditionPhase.EXPLORING,
                squad = updatedSquad,
                gatheredLoot = exp.gatheredLoot.add(combat.bonusLoot),
                xpReward = exp.xpReward + xpGain,
                logs = exp.logs + listOf(
                    "⚔️ Победа в бою! Захвачены трофеи (+${combat.bonusLoot.money} Кр, +${combat.bonusLoot.materials} Матер.) и получено +$xpGain опыта."
                )
            )

            state.copy(
                characters = updatedAllCharacters,
                activeExpedition = updatedExp,
                activeCombat = null,
                dayLogs = listOf("⚔️ Победа в бою: Отряд разгромил противников в «${exp.location.name}».") + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Retreats squad from tactical combat and begins emergency evacuation back to base.
     */
    fun retreatFromCombat() {
        _gameState.update { state ->
            val combat = state.activeCombat
            val exp = state.activeExpedition ?: return@update state

            val combatHpMap = combat?.combatants
                ?.filter { it.team == CombatantTeam.PLAYER }
                ?.associate { it.id to it.currentHealth } ?: emptyMap()

            val updatedSquad = exp.squad.map { char ->
                val newHp = (combatHpMap[char.id] ?: char.health).coerceIn(0, char.maxHealth)
                char.copy(health = newHp)
            }

            val squadIdSet = exp.squad.map { it.id }.toSet()
            val updatedAllCharacters = state.characters.map { char ->
                if (squadIdSet.contains(char.id)) {
                    val newHp = (combatHpMap[char.id] ?: char.health).coerceIn(0, char.maxHealth)
                    char.copy(health = newHp)
                } else char
            }

            val updatedExp = exp.copy(
                status = ExpeditionStatus.RETURNING,
                phase = ExpeditionPhase.RETURNING,
                squad = updatedSquad,
                logs = exp.logs + listOf("⚠️ Отряд вышел из боя и начал экстренную эвакуацию в поселение.")
            )

            state.copy(
                characters = updatedAllCharacters,
                activeExpedition = updatedExp,
                activeCombat = null,
                dayLogs = listOf("⚠️ Эвакуация: Отряд отступил из «${exp.location.name}» с ранениями.") + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Restores action points to max for all player combatants (Debug / Sandbox).
     */
    fun debugCombatRestoreAP() {
        _gameState.update { state ->
            val combat = state.activeCombat ?: return@update state
            val updatedCombatants = combat.combatants.map {
                if (it.team == CombatantTeam.PLAYER) it.copy(actionPoints = it.maxActionPoints) else it
            }
            state.copy(activeCombat = combat.copy(combatants = updatedCombatants))
        }
    }

    /**
     * Forces the combat encounter into Victory phase (Debug).
     */
    fun debugCombatForceVictory() {
        _gameState.update { state ->
            val combat = state.activeCombat ?: return@update state
            val defeatedEnemies = combat.combatants.map {
                if (it.team == CombatantTeam.ENEMY) it.copy(currentHealth = 0, status = CombatantStatus.DEFEATED) else it
            }
            val victoriousState = combat.copy(
                combatants = defeatedEnemies,
                currentPhase = CombatPhase.VICTORY
            )
            state.copy(activeCombat = victoriousState)
        }
    }

    /**
     * Forces the combat encounter into Defeat phase (Debug).
     */
    fun debugCombatForceDefeat() {
        _gameState.update { state ->
            val combat = state.activeCombat ?: return@update state
            val defeatedPlayers = combat.combatants.map {
                if (it.team == CombatantTeam.PLAYER) it.copy(currentHealth = 0, status = CombatantStatus.INCAPACITATED) else it
            }
            val defeatState = combat.copy(
                combatants = defeatedPlayers,
                currentPhase = CombatPhase.DEFEAT
            )
            state.copy(activeCombat = defeatState)
        }
    }

    /**
     * Skips the current turn in combat (Debug).
     */
    fun debugCombatSkipTurn() {
        _gameState.update { state ->
            val combat = state.activeCombat ?: return@update state
            state.copy(activeCombat = com.example.domain.service.combat.CombatTurnManager.advanceTurn(combat))
        }
        executeEnemyTurnsIfNeeded()
    }

    /**
     * Conclude expedition, transfer all collected loot safely into settlement warehouse,
     * award XP to survivors, update vehicle/quests, and generate return summary.
     */
    fun completeExpeditionAndReturn() {
        _gameState.update { state ->
            val (finalizedState, _) = ExpeditionReturnService.completeExpeditionReturn(state)
            finalizedState
        }
    }

    /**
     * Completes expedition return atomically and returns the generated ExpeditionReturnSummary.
     */
    fun completeExpeditionReturn(): ExpeditionReturnSummary? {
        var summary: ExpeditionReturnSummary? = null
        _gameState.update { state ->
            val (finalizedState, generatedSummary) = ExpeditionReturnService.completeExpeditionReturn(state)
            summary = generatedSummary
            finalizedState
        }
        return summary
    }

    /**
     * Dismisses the expedition return summary dialog/card.
     */
    fun dismissReturnSummary() {
        _gameState.update { state ->
            state.copy(lastReturnSummary = null)
        }
    }

    /**
     * Unloads pending excess cargo from the staging area into the warehouse when space is available.
     */
    fun claimPendingUnloadCargo(): ResourceOperationResult {
        var opResult: ResourceOperationResult = ResourceOperationResult.Failure(
            reason = ResourceOperationResult.FailureReason.STORAGE_FULL,
            message = "Недостаточно места на складе."
        )
        _gameState.update { state ->
            val (updatedState, result) = ExpeditionReturnService.unloadPendingSettlementCargo(state)
            opResult = result
            updatedState
        }
        _lastResourceOperation.value = opResult
        return opResult
    }

    // -------------------------------------------------------------
    // Market, Barter & Safe Atomic Trading System (Point 6)
    // -------------------------------------------------------------

    /**
     * Executes an atomic trade transaction (Buy or Sell) with comprehensive validation.
     * Guarantees all-or-nothing execution with zero partial state corruption.
     */
    fun executeTrade(offerId: String, quantity: Int, mode: TradeMode): TradeTransactionResult {
        var txResult: TradeTransactionResult = TradeTransactionResult.Failure(
            reason = TradeFailureReason.INVALID_QUANTITY,
            message = "Некорректное количество товаров."
        )

        _gameState.update { state ->
            if (quantity <= 0) {
                txResult = TradeTransactionResult.Failure(
                    reason = TradeFailureReason.INVALID_QUANTITY,
                    message = "Количество для обмена должно быть больше нуля."
                )
                return@update state
            }

            // Locate offer or fallback by ResourceType
            val currentOffers = if (state.merchantState.offers.isNotEmpty()) {
                state.merchantState.offers
            } else {
                TradeConfig.createDefaultTradeOffers()
            }

            val offer = currentOffers.find { it.id == offerId || it.resourceType.id.equals(offerId, ignoreCase = true) }
            if (offer == null) {
                txResult = TradeTransactionResult.Failure(
                    reason = TradeFailureReason.INVALID_QUANTITY,
                    message = "Товар не найден в каталоге каравана."
                )
                return@update state
            }

            val tradingPostBuilding = state.settlement.buildings.find {
                it.type == BuildingType.TRADING_POST && it.isConstructed
            }
            val tradingPostLevel = tradingPostBuilding?.level ?: 0

            // Check unlock requirements
            if (!offer.isAvailable(tradingPostLevel, state.settlement.level)) {
                txResult = TradeTransactionResult.Failure(
                    reason = TradeFailureReason.LOCKED_ITEM,
                    message = "Товар «${offer.nameRu}» заблокирован! Требуется Торговый Пост уровня ${offer.minTradingPostLevel}."
                )
                return@update state
            }

            val discountPercent = tradingPostLevel * 5
            val unitBuyPrice = offer.getEffectiveBuyPrice(discountPercent)
            val unitSellPrice = offer.getEffectiveSellPrice(discountPercent)

            when (mode) {
                TradeMode.BUY -> {
                    // Check merchant stock
                    if (offer.merchantStock < quantity) {
                        txResult = TradeTransactionResult.Failure(
                            reason = TradeFailureReason.INSUFFICIENT_MERCHANT_STOCK,
                            message = "У торговца в наличии только ${offer.merchantStock} ед. «${offer.nameRu}».",
                            availableQuantity = offer.merchantStock
                        )
                        return@update state
                    }

                    val totalCost = quantity * unitBuyPrice

                    // Check player credits
                    if (state.resources.money < totalCost) {
                        txResult = TradeTransactionResult.Failure(
                            reason = TradeFailureReason.INSUFFICIENT_CREDITS,
                            message = "Недостаточно кредитов! Нужно: $totalCost кр., в наличии: ${state.resources.money} кр.",
                            requiredCredits = totalCost
                        )
                        return@update state
                    }

                    // Check warehouse capacity
                    val spaceNeeded = quantity * offer.unitSize
                    if (state.resources.availableCapacity < spaceNeeded) {
                        txResult = TradeTransactionResult.Failure(
                            reason = TradeFailureReason.INSUFFICIENT_STORAGE,
                            message = "Недостаточно места на складе! Требуется $spaceNeeded ед. места, свободно ${state.resources.availableCapacity} ед.",
                            requiredCapacity = spaceNeeded
                        )
                        return@update state
                    }

                    // All checks passed -> ATOMIC UPDATE
                    val currentAmount = state.resources[offer.resourceType]
                    val updatedResources = state.resources
                        .copy(money = state.resources.money - totalCost)
                        .withResource(offer.resourceType, currentAmount + quantity)

                    val updatedOffersList = currentOffers.map {
                        if (it.id == offer.id) it.copy(merchantStock = it.merchantStock - quantity) else it
                    }

                    val successMessage = "Приобретено: +$quantity ${offer.nameRu} за $totalCost кр."
                    val logEntry = "Рынок: Куплено $quantity ед. «${offer.nameRu}» (-$totalCost кр.)."

                    txResult = TradeTransactionResult.Success(
                        mode = TradeMode.BUY,
                        resourceType = offer.resourceType,
                        quantity = quantity,
                        totalCredits = totalCost,
                        message = successMessage
                    )

                    _lastResourceOperation.value = ResourceOperationResult.Success(
                        type = offer.resourceType,
                        amountChanged = quantity,
                        message = successMessage
                    )

                    state.copy(
                        resources = updatedResources,
                        merchantState = state.merchantState.copy(
                            offers = updatedOffersList,
                            totalCreditsTurnover = state.merchantState.totalCreditsTurnover + totalCost,
                            totalDealsCompleted = state.merchantState.totalDealsCompleted + 1,
                            lastDealMessage = successMessage
                        ),
                        dayLogs = listOf(logEntry) + state.dayLogs.take(19)
                    )
                }

                TradeMode.SELL -> {
                    val playerStock = state.resources[offer.resourceType]
                    if (playerStock < quantity) {
                        txResult = TradeTransactionResult.Failure(
                            reason = TradeFailureReason.INSUFFICIENT_PLAYER_STOCK,
                            message = "Недостаточно «${offer.nameRu}» на складе! В наличии: $playerStock ед., выбрано: $quantity ед.",
                            availableQuantity = playerStock
                        )
                        return@update state
                    }

                    val totalGain = quantity * unitSellPrice

                    // All checks passed -> ATOMIC UPDATE
                    val updatedResources = state.resources
                        .withResource(offer.resourceType, playerStock - quantity)
                        .let { it.copy(money = it.money + totalGain) }

                    val updatedOffersList = currentOffers.map {
                        if (it.id == offer.id) {
                            it.copy(merchantStock = (it.merchantStock + quantity).coerceAtMost(it.maxMerchantStock))
                        } else it
                    }

                    val successMessage = "Продано: -$quantity ${offer.nameRu} (+ $totalGain кр.)"
                    val logEntry = "Рынок: Продано $quantity ед. «${offer.nameRu}» (+ $totalGain кр.)."

                    txResult = TradeTransactionResult.Success(
                        mode = TradeMode.SELL,
                        resourceType = offer.resourceType,
                        quantity = quantity,
                        totalCredits = totalGain,
                        message = successMessage
                    )

                    _lastResourceOperation.value = ResourceOperationResult.Success(
                        type = offer.resourceType,
                        amountChanged = -quantity,
                        message = successMessage
                    )

                    state.copy(
                        resources = updatedResources,
                        merchantState = state.merchantState.copy(
                            offers = updatedOffersList,
                            totalCreditsTurnover = state.merchantState.totalCreditsTurnover + totalGain,
                            totalDealsCompleted = state.merchantState.totalDealsCompleted + 1,
                            lastDealMessage = successMessage
                        ),
                        dayLogs = listOf(logEntry) + state.dayLogs.take(19)
                    )
                }
            }
        }

        _lastTradeResult.value = txResult
        return txResult
    }

    /**
     * Backward-compatible buy helper.
     */
    fun buyResource(resourceName: String, amount: Int, creditCost: Int) {
        val type = ResourceType.fromId(resourceName) ?: ResourceType.FOOD
        val offer = _gameState.value.merchantState.offers.find { it.resourceType == type }
        val offerId = offer?.id ?: "offer_${type.id}"
        executeTrade(offerId, amount, TradeMode.BUY)
    }

    /**
     * Backward-compatible sell helper.
     */
    fun sellResource(resourceName: String, amount: Int, creditGain: Int) {
        val type = ResourceType.fromId(resourceName) ?: ResourceType.FOOD
        val offer = _gameState.value.merchantState.offers.find { it.resourceType == type }
        val offerId = offer?.id ?: "offer_${type.id}"
        executeTrade(offerId, amount, TradeMode.SELL)
    }

    /**
     * Debug action to immediately refresh merchant stock.
     */
    fun debugRestockMarket() {
        _gameState.update { state ->
            val restocked = TradeConfig.createDefaultTradeOffers().map {
                it.copy(merchantStock = it.maxMerchantStock)
            }
            state.copy(
                merchantState = state.merchantState.copy(
                    offers = restocked,
                    lastDealMessage = "Караван полностью обновил запасы товаров!"
                ),
                dayLogs = listOf("Торговля: Прибыл новый караван с полными запасами припасов.") + state.dayLogs.take(19)
            )
        }
    }

    // -------------------------------------------------------------
    // Workshop, Manufacturing & Safe Atomic Crafting System (Point 7)
    // -------------------------------------------------------------

    /**
     * Executes an atomic crafting operation with strict verification.
     * Deducts necessary resources and adds newly manufactured items to the settlement warehouse.
     */
    fun craftItem(recipeId: String, craftCount: Int = 1): CraftTransactionResult {
        var craftRes: CraftTransactionResult = CraftTransactionResult.Failure(
            reason = CraftFailureReason.INVALID_QUANTITY,
            message = "Некорректное количество предметов для создания."
        )

        _gameState.update { state ->
            if (craftCount <= 0) {
                craftRes = CraftTransactionResult.Failure(
                    reason = CraftFailureReason.INVALID_QUANTITY,
                    message = "Количество для создания должно быть больше нуля."
                )
                return@update state
            }

            val allRecipes = com.example.data.CraftConfig.createDefaultRecipes()
            val recipe = allRecipes.find { it.id == recipeId }
            if (recipe == null) {
                craftRes = CraftTransactionResult.Failure(
                    reason = CraftFailureReason.INVALID_QUANTITY,
                    message = "Чертёж не найден в каталоге Мастерской."
                )
                return@update state
            }

            val workshopBuilding = state.settlement.buildings.find {
                it.type == BuildingType.WORKSHOP && it.isConstructed
            }

            val validationFailure = com.example.data.CraftConfig.validateCraft(
                recipe = recipe,
                workshopBuilding = workshopBuilding,
                settlementLevel = state.settlement.level,
                resources = state.resources,
                currentInventory = state.inventoryItems,
                craftCount = craftCount
            )

            if (validationFailure != null) {
                val missingResources = recipe.requiredResources.filter { (type, costPerBatch) ->
                    state.resources[type] < (costPerBatch * craftCount)
                }.mapValues { (type, costPerBatch) ->
                    (costPerBatch * craftCount) - state.resources[type]
                }

                val netVolumeNeeded = (recipe.totalOutputVolume * craftCount) -
                        recipe.requiredResources.entries.sumOf { (type, amt) ->
                            if (type.isPhysical) amt * type.unitSize * craftCount else 0
                        }

                val failureMessage = when (validationFailure) {
                    CraftFailureReason.WORKSHOP_NOT_BUILT ->
                        "Мастерская ещё не возведена! Постройте её во вкладке объектов поселения."
                    CraftFailureReason.INSUFFICIENT_WORKSHOP_LEVEL ->
                        "Недостаточный уровень Мастерской! Требуется Ур. ${recipe.minWorkshopLevel}, текущий: Ур. ${workshopBuilding?.level ?: 0}."
                    CraftFailureReason.INSUFFICIENT_SETTLEMENT_LEVEL ->
                        "Требуется уровень поселения ${recipe.requiredSettlementLevel}."
                    CraftFailureReason.INSUFFICIENT_RESOURCES ->
                        "Недостаточно сырья на складе для изготовления «${recipe.nameRu}»!"
                    CraftFailureReason.INSUFFICIENT_STORAGE ->
                        "Недостаточно места на складе! Требуется ещё $netVolumeNeeded ед. свободного объёма."
                    CraftFailureReason.INVALID_QUANTITY ->
                        "Количество для создания должно быть больше нуля."
                    CraftFailureReason.TECH_NOT_RESEARCHED ->
                        "Требуется изучить чертежи в научной лаборатории."
                }

                craftRes = CraftTransactionResult.Failure(
                    reason = validationFailure,
                    message = failureMessage,
                    missingResources = missingResources,
                    requiredCapacity = netVolumeNeeded.coerceAtLeast(0),
                    availableCapacity = state.freeWarehouseCapacity
                )
                return@update state
            }

            // ATOMIC DEDUCTION & INVENTORY ADDITION:
            val totalConsumedMap = mutableMapOf<ResourceType, Int>()
            recipe.requiredResources.forEach { (type, amountPerBatch) ->
                val totalAmount = amountPerBatch * craftCount
                if (totalAmount > 0) {
                    totalConsumedMap[type] = totalAmount
                }
            }

            var updatedResources = state.resources
            totalConsumedMap.forEach { (type, amount) ->
                val currentStock = updatedResources[type]
                updatedResources = updatedResources.withResource(type, currentStock - amount)
            }

            val itemsProduced = recipe.outputQuantity * craftCount
            val existingItemIndex = state.inventoryItems.indexOfFirst { it.id == recipe.outputItem.id }

            val updatedInventory = if (existingItemIndex >= 0) {
                state.inventoryItems.mapIndexed { index, item ->
                    if (index == existingItemIndex) {
                        item.copy(quantity = item.quantity + itemsProduced)
                    } else item
                }
            } else {
                state.inventoryItems + recipe.outputItem.copy(quantity = itemsProduced)
            }

            val successMsg = "Изготовлено: +$itemsProduced «${recipe.nameRu}» (Мастерская Ур. ${workshopBuilding?.level ?: 1})"
            val logEntry = "Мастерская: Завершено производство $itemsProduced ед. «${recipe.nameRu}»."

            craftRes = CraftTransactionResult.Success(
                recipe = recipe,
                craftCount = craftCount,
                totalItemsCreated = itemsProduced,
                consumedResources = totalConsumedMap,
                message = successMsg
            )

            _lastResourceOperation.value = ResourceOperationResult.Success(
                message = successMsg
            )

            state.copy(
                resources = updatedResources,
                inventoryItems = updatedInventory,
                dayLogs = listOf(logEntry) + state.dayLogs.take(19)
            )
        }

        _lastCraftResult.value = craftRes
        return craftRes
    }

    /**
     * Debug: Add resources and components specifically for testing crafting recipes.
     */
    fun debugAddCraftingSupplies() {
        _gameState.update { state ->
            val updatedRes = state.resources.copy(
                materials = state.resources.materials + 80,
                food = state.resources.food + 50,
                water = state.resources.water + 40,
                fuel = state.resources.fuel + 30,
                warehouseMaxCapacity = state.resources.warehouseMaxCapacity + 400,
                extraResources = state.resources.extraResources.toMutableMap().apply {
                    put(ResourceType.MEDICINE, (get(ResourceType.MEDICINE) ?: 0) + 15)
                    put(ResourceType.COMPONENTS, (get(ResourceType.COMPONENTS) ?: 0) + 15)
                    put(ResourceType.RARE_ALLOY, (get(ResourceType.RARE_ALLOY) ?: 0) + 5)
                    put(ResourceType.AMMO, (get(ResourceType.AMMO) ?: 0) + 50)
                }
            )
            val msg = "Тест: Выданы компоненты и сырьё для тестирования Мастерской."
            _lastResourceOperation.value = ResourceOperationResult.Success(message = msg)
            state.copy(
                resources = updatedRes,
                dayLogs = listOf(msg) + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Debug: Add research funds & materials.
     */
    fun debugAddResearchSupplies() {
        _gameState.update { state ->
            val updatedRes = state.resources.copy(
                money = state.resources.money + 1000,
                materials = (state.resources.materials + 400).coerceAtMost(state.resources.warehouseMaxCapacity),
                extraResources = state.resources.extraResources.toMutableMap().apply {
                    put(ResourceType.COMPONENTS, (get(ResourceType.COMPONENTS) ?: 0) + 10)
                    put(ResourceType.RARE_ALLOY, (get(ResourceType.RARE_ALLOY) ?: 0) + 5)
                    put(ResourceType.MEDICINE, (get(ResourceType.MEDICINE) ?: 0) + 10)
                }
            )
            val msg = "Тест: Выданы средства и материалы для проведения исследований."
            _lastResourceOperation.value = ResourceOperationResult.Success(message = msg)
            state.copy(
                resources = updatedRes,
                dayLogs = listOf(msg) + state.dayLogs.take(19)
            )
        }
    }

    // -------------------------------------------------------------
    // Research & Quests
    // -------------------------------------------------------------

    fun researchTech(techId: String): ResearchTransactionResult {
        var result: ResearchTransactionResult = ResearchTransactionResult.Failure(
            tech = null,
            status = TechStatus.INSUFFICIENT_RESOURCES,
            message = "Ошибка при проведении исследований"
        )

        _gameState.update { state ->
            val tech = state.technologies.find { it.id == techId }
            if (tech == null) {
                result = ResearchTransactionResult.Failure(
                    tech = null,
                    status = TechStatus.LOCKED_DEPENDENCY,
                    message = "Технология с идентификатором $techId не найдена."
                )
                return@update state
            }

            val labBuilding = state.settlement.buildings.find {
                (it.type == BuildingType.RESEARCH_LAB || it.type == BuildingType.ARMORY_LAB) && it.isConstructed
            }
            val validation = ResearchConfig.validateTech(
                tech = tech,
                allTechs = state.technologies,
                labBuilding = labBuilding,
                settlementLevel = state.settlement.level,
                resources = state.resources
            )

            if (!validation.canBeResearched) {
                val failReason = when (validation.status) {
                    TechStatus.RESEARCHED -> "Технология «${tech.title}» уже изучена!"
                    TechStatus.LOCKED_LAB_UNBUILT -> "Для исследований необходимо построить Исследовательский центр!"
                    TechStatus.LOCKED_LAB_LEVEL -> "Требуется повысить уровень Исследовательского центра до ${tech.requirements.minLabLevel}!"
                    TechStatus.LOCKED_SETTLEMENT_LEVEL -> "Требуется уровень поселения ${tech.requirements.minSettlementLevel}!"
                    TechStatus.LOCKED_DEPENDENCY -> "Сначала необходимо изучить предшествующие технологии: ${validation.unsatisfiedPrerequisites.joinToString { it.title }}!"
                    TechStatus.INSUFFICIENT_RESOURCES -> "Недостаточно ресурсов для исследования! Не хватает: ${validation.missingResources.entries.joinToString { "${it.key.titleRu} (${it.value})" }}."
                    else -> "Условия для изучения технологии не выполнены."
                }
                _lastResourceOperation.value = ResourceOperationResult.Failure(
                    reason = ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE,
                    message = failReason
                )
                result = ResearchTransactionResult.Failure(
                    tech = tech,
                    status = validation.status,
                    message = failReason,
                    missingResources = validation.missingResources
                )
                return@update state
            }

            // Deduct required resources safely
            val (currentRes, deductionResult) = state.resources.consumeBundleSafe(tech.requirements.resourceCosts)
            if (deductionResult !is ResourceOperationResult.Success) {
                val failMsg = "Не удалось списать ресурсы для исследования: ${deductionResult.message}"
                _lastResourceOperation.value = deductionResult
                result = ResearchTransactionResult.Failure(
                    tech = tech,
                    status = TechStatus.INSUFFICIENT_RESOURCES,
                    message = failMsg
                )
                return@update state
            }

            // Mark tech as researched
            val updatedTechs = state.technologies.map {
                if (it.id == techId) it.copy(isResearched = true) else it
            }

            // Recalculate storage capacity immediately
            val recalculatedStorageCap = calculateWarehouseCapacity(state.settlement.buildings, updatedTechs)
            val finalizedResources = currentRes.copy(warehouseMaxCapacity = recalculatedStorageCap)

            // Handle location unlocks
            val locationUnlockEffects = tech.effects.filterIsInstance<TechEffect.LocationUnlock>()
            val updatedLocations = if (locationUnlockEffects.isNotEmpty()) {
                val unlockedLocationIds = locationUnlockEffects.map { it.locationId }.toSet()
                state.locations.map { loc ->
                    if (unlockedLocationIds.contains(loc.id)) loc.copy(isUnlocked = true) else loc
                }
            } else state.locations

            // Handle squad stat bonuses if any
            val squadBonusEffects = tech.effects.filterIsInstance<TechEffect.SquadStatBonus>()
            val updatedChars = if (squadBonusEffects.isNotEmpty()) {
                val totalAtk = squadBonusEffects.sumOf { it.attackBonus }
                val totalDef = squadBonusEffects.sumOf { it.defenseBonus }
                val totalHp = squadBonusEffects.sumOf { it.healthBonus }
                state.characters.map { c ->
                    c.copy(
                        maxHealth = c.maxHealth + totalHp,
                        health = (c.health + totalHp).coerceAtMost(c.maxHealth + totalHp),
                        stats = c.stats.copy(
                            attack = c.stats.attack + totalAtk,
                            defense = c.stats.defense + totalDef
                        )
                    )
                }
            } else state.characters

            val logMsg = "🔬 Исследования: Успешно изучена технология «${tech.title}»! Эффект: ${tech.effectSummary}"
            _lastResourceOperation.value = ResourceOperationResult.Success(
                message = "Изучено: «${tech.title}»! ${tech.effectSummary}"
            )

            result = ResearchTransactionResult.Success(
                tech = tech.copy(isResearched = true),
                consumedResources = tech.requirements.resourceCosts,
                appliedEffects = tech.effects,
                message = logMsg
            )

            state.copy(
                resources = finalizedResources,
                technologies = updatedTechs,
                locations = updatedLocations,
                characters = updatedChars,
                dayLogs = listOf(logMsg) + state.dayLogs.take(19)
            )
        }

        return result
    }

    // -------------------------------------------------------------
    // Quest & Mission System (Point 29)
    // -------------------------------------------------------------

    /**
     * Accepts an AVAILABLE quest.
     */
    fun acceptQuest(questId: String): QuestOperationResult {
        var opResult: QuestOperationResult = QuestOperationResult(_gameState.value, false, "Ошибка принятия задания")
        _gameState.update { state ->
            val result = QuestManager.acceptQuest(state, questId)
            opResult = result
            if (result.isSuccess) {
                _lastResourceOperation.value = ResourceOperationResult.Success(message = result.messageRu)
                result.updatedGameState
            } else {
                _lastResourceOperation.value = ResourceOperationResult.Failure(
                    ResourceOperationResult.FailureReason.INVALID_AMOUNT,
                    result.messageRu
                )
                state
            }
        }
        return opResult
    }

    /**
     * Declines an AVAILABLE quest.
     */
    fun declineQuest(questId: String): QuestOperationResult {
        var opResult: QuestOperationResult = QuestOperationResult(_gameState.value, false, "Ошибка отклонения задания")
        _gameState.update { state ->
            val result = QuestManager.declineQuest(state, questId)
            opResult = result
            if (result.isSuccess) {
                _lastResourceOperation.value = ResourceOperationResult.Success(message = result.messageRu)
                result.updatedGameState
            } else {
                _lastResourceOperation.value = ResourceOperationResult.Failure(
                    ResourceOperationResult.FailureReason.INVALID_AMOUNT,
                    result.messageRu
                )
                state
            }
        }
        return opResult
    }

    /**
     * Turns in a READY_TO_CLAIM quest, granting rewards idempotently.
     */
    fun turnInQuest(questId: String): QuestOperationResult {
        var opResult: QuestOperationResult = QuestOperationResult(_gameState.value, false, "Ошибка сдачи задания")
        _gameState.update { state ->
            val result = QuestManager.turnInQuest(state, questId)
            opResult = result
            if (result.isSuccess) {
                _lastResourceOperation.value = ResourceOperationResult.Success(message = result.messageRu)
                result.updatedGameState
            } else {
                _lastResourceOperation.value = ResourceOperationResult.Failure(
                    ResourceOperationResult.FailureReason.INVALID_AMOUNT,
                    result.messageRu
                )
                state
            }
        }
        return opResult
    }

    /**
     * Delivers resources to a quest objective from the settlement warehouse.
     */
    fun deliverQuestResource(questId: String, objectiveId: String, amount: Int): QuestOperationResult {
        var opResult: QuestOperationResult = QuestOperationResult(_gameState.value, false, "Ошибка передачи ресурсов")
        _gameState.update { state ->
            val result = QuestManager.deliverResource(state, questId, objectiveId, amount)
            opResult = result
            if (result.isSuccess) {
                _lastResourceOperation.value = ResourceOperationResult.Success(message = result.messageRu)
                result.updatedGameState
            } else {
                _lastResourceOperation.value = ResourceOperationResult.Failure(
                    ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE,
                    result.messageRu
                )
                state
            }
        }
        return opResult
    }

    /**
     * Delivers an inventory quest item.
     */
    fun deliverQuestItem(questId: String, objectiveId: String, itemId: String): QuestOperationResult {
        var opResult: QuestOperationResult = QuestOperationResult(_gameState.value, false, "Ошибка передачи предмета")
        _gameState.update { state ->
            val result = QuestManager.deliverItem(state, questId, objectiveId, itemId)
            opResult = result
            if (result.isSuccess) {
                _lastResourceOperation.value = ResourceOperationResult.Success(message = result.messageRu)
                result.updatedGameState
            } else {
                _lastResourceOperation.value = ResourceOperationResult.Failure(
                    ResourceOperationResult.FailureReason.INSUFFICIENT_RESOURCE,
                    result.messageRu
                )
                state
            }
        }
        return opResult
    }

    /**
     * Sets the active HUD tracked quest.
     */
    fun setTrackedQuest(questId: String?) {
        _gameState.update { state ->
            QuestManager.setTrackedQuest(state, questId)
        }
    }

    /**
     * Unified claim quest method supporting both new and legacy quest representations.
     */
    fun claimQuest(questId: String) {
        _gameState.update { state ->
            if (state.questStates.containsKey(questId)) {
                val res = QuestManager.turnInQuest(state, questId)
                if (res.isSuccess) {
                    _lastResourceOperation.value = ResourceOperationResult.Success(message = res.messageRu)
                    return@update res.updatedGameState
                }
            }

            val quest = state.quests.find { it.id == questId } ?: return@update state
            if (quest.status != QuestStatus.READY_TO_CLAIM) return@update state

            val updatedQuests = state.quests.map {
                if (it.id == questId) it.copy(status = QuestStatus.COMPLETED) else it
            }

            val updatedResources = state.resources.copy(
                money = state.resources.money + quest.rewardCredits,
                materials = (state.resources.materials + quest.rewardMaterials).coerceAtMost(state.resources.warehouseMaxCapacity)
            )

            val updatedSettlement = state.settlement.copy(
                reputation = state.settlement.reputation + quest.rewardReputation
            )

            val successMsg = "Задание выполнено: «${quest.title}»! Награда: +${quest.rewardCredits} Кр, +${quest.rewardMaterials} Мат."
            _lastResourceOperation.value = ResourceOperationResult.Success(message = successMsg)

            state.copy(
                quests = updatedQuests,
                resources = updatedResources,
                settlement = updatedSettlement,
                dayLogs = listOf(successMsg) + state.dayLogs.take(19)
            )
        }
    }

    // -------------------------------------------------------------
    // Global Map Travel & Movement System (Point 11)
    // -------------------------------------------------------------

    /**
     * Estimates the resource requirements and travel time for the selected destination.
     */
    fun calculateTravelCost(
        destinationId: String,
        mode: TravelTransportMode = _selectedTravelMode.value,
        participantIds: List<String> = emptyList()
    ): TravelCost {
        val state = _gameState.value
        val destLoc = state.locations.find { it.id == destinationId } ?: return TravelCost()
        val originLoc = state.locations.find { it.id == state.currentLocationId }
        val squadCount = if (participantIds.isNotEmpty()) participantIds.size else state.selectedSquadIds.size.coerceAtLeast(1)

        return TravelCalculator.calculateTravelCost(
            destination = destLoc,
            transportMode = mode,
            participantCount = squadCount,
            technologies = state.technologies,
            origin = originLoc
        )
    }

    /**
     * Validates whether the player can dispatch a travel party.
     */
    fun validateTravel(
        destinationId: String,
        mode: TravelTransportMode = _selectedTravelMode.value,
        participantIds: List<String> = emptyList()
    ): TravelValidationResult {
        val state = _gameState.value
        val destLoc = state.locations.find { it.id == destinationId }
            ?: return TravelValidationResult.Invalid(TravelFailureReason.LOCATION_LOCKED, "Локация не найдена.")

        return TravelCalculator.validateTravel(
            destination = destLoc,
            transportMode = mode,
            gameState = state,
            participantIds = participantIds,
            originLocationId = state.currentLocationId
        )
    }

    /**
     * Dispatches an expedition/travel party towards the destination location.
     * Executes atomic resource deduction and generates the active TravelState.
     */
    fun startTravel(
        destinationId: String,
        mode: TravelTransportMode = _selectedTravelMode.value,
        participantIds: List<String> = emptyList(),
        vehicleId: String? = null
    ): TravelTransactionResult {
        var finalResult: TravelTransactionResult = TravelTransactionResult.Failure(
            reason = TravelFailureReason.LOCATION_LOCKED,
            message = "Невозможно начать путешествие."
        )

        _gameState.update { state ->
            val destLoc = state.locations.find { it.id == destinationId }
            if (destLoc == null) {
                finalResult = TravelTransactionResult.Failure(
                    reason = TravelFailureReason.LOCATION_LOCKED,
                    message = "Локация не найдена."
                )
                return@update state
            }

            val targetVehicle = if (vehicleId != null) {
                state.vehicles.find { it.id == vehicleId }
            } else if (mode.requiresVehicle) {
                state.vehicles.find { it.id == state.selectedVehicleId && it.isReadyForTrip }
                    ?: TravelCalculator.resolveVehicle(mode, state)
            } else null

            val transaction = TravelCalculator.startTravelTransaction(
                destination = destLoc,
                transportMode = mode,
                gameState = state,
                participantIds = participantIds,
                vehicle = targetVehicle,
                originLocationId = state.currentLocationId
            )

            finalResult = transaction
            _lastTravelResult.value = transaction

            if (transaction is TravelTransactionResult.Success) {
                val participantSet = transaction.travelState.participantIds.toSet()
                val updatedCharacters = state.characters.map { char ->
                    if (participantSet.contains(char.id)) {
                        char.copy(status = CharacterStatus.ON_EXPEDITION)
                    } else char
                }

                val vehName = targetVehicle?.name ?: mode.titleRu
                val log = "🗺️ Путешествие: Отряд выдвинулся в сторону «${destLoc.name}» (${destLoc.distanceKm} км) на «$vehName»."
                state.copy(
                    resources = transaction.updatedResources,
                    characters = updatedCharacters,
                    vehicles = transaction.updatedVehicles,
                    activeTravel = transaction.travelState,
                    dayLogs = listOf(log) + state.dayLogs.take(19)
                )
            } else {
                state
            }
        }

        return finalResult
    }

    /**
     * Advances the traveling party by a step along the route trajectory.
     */
    fun advanceTravelStep(stepFraction: Float = 0.25f) {
        _gameState.update { state ->
            val currentTravel = state.activeTravel ?: return@update state
            val updatedTravel = TravelCalculator.advanceStep(
                current = currentTravel,
                locations = state.locations,
                stepFraction = stepFraction
            )

            // Calculate step duration in game minutes
            val rawMinutes = if (currentTravel.totalDuration.totalMinutes > 0L) {
                currentTravel.totalDuration.totalMinutes.toInt()
            } else {
                (currentTravel.costPaid.estimatedDurationHours * 60f).toInt()
            }
            val totalMinutes = rawMinutes.coerceAtLeast(15)
            val stepMinutes = (totalMinutes * stepFraction).toInt().coerceAtLeast(5)
            val stepDuration = GameDuration.ofMinutes(stepMinutes)
            val timeAdvance = GameClock.advance(state.gameDateTime, stepDuration)
            val baseDateTime = timeAdvance.newTime

            val stateWithTime = if (timeAdvance.isNewDayCrossed) {
                val (processedState, summaries) = DailyTickProcessor.processMultipleDays(
                    state.copy(gameDateTime = baseDateTime),
                    timeAdvance.crossedDays
                )
                processedState.copy(
                    lastDailySummary = summaries.lastOrNull() ?: processedState.lastDailySummary
                )
            } else {
                state.copy(gameDateTime = baseDateTime)
            }

            val isArrivedNow = updatedTravel.status == TravelStatus.ARRIVED && currentTravel.status != TravelStatus.ARRIVED
            val isReturningToBase = isArrivedNow && (updatedTravel.isReturning || updatedTravel.toLocationId == "loc_base")

            if (isReturningToBase && stateWithTime.activeExpedition != null) {
                // Return journey reached base! Safely and automatically complete the expedition
                val intermediateState = stateWithTime.copy(activeTravel = updatedTravel, currentLocationId = "loc_base")
                val (completedState, _) = ExpeditionReturnService.completeExpeditionReturn(intermediateState)
                return@update completedState
            }

            val updatedCurrentLocId = if (isArrivedNow) updatedTravel.toLocationId else stateWithTime.currentLocationId

            val updatedLocations = if (isArrivedNow) {
                stateWithTime.locations.map { loc ->
                    if (loc.id == updatedTravel.toLocationId) {
                        loc.copy(
                            status = if (loc.status == LocationStatus.EXPLORED) LocationStatus.EXPLORED else LocationStatus.VISITED,
                            visitCount = loc.visitCount + 1,
                            firstVisitedDay = loc.firstVisitedDay ?: stateWithTime.day,
                            lastVisitedDay = stateWithTime.day
                        )
                    } else loc
                }
            } else {
                stateWithTime.locations
            }

            val updatedActiveExp = if (isArrivedNow && stateWithTime.activeExpedition != null) {
                val destLoc = updatedLocations.find { it.id == updatedTravel.toLocationId } ?: stateWithTime.activeExpedition.location
                stateWithTime.activeExpedition.copy(
                    phase = ExpeditionPhase.AT_LOCATION,
                    location = destLoc,
                    logs = stateWithTime.activeExpedition.logs + "Отряд прибыл на точку «${destLoc.name}». Ожидание приказа командира."
                )
            } else {
                stateWithTime.activeExpedition
            }

            val newLogs = if (isArrivedNow) {
                val destName = updatedLocations.find { it.id == updatedTravel.toLocationId }?.name ?: "Пункт назначения"
                listOf("🎯 Прибытие: Отряд благополучно достиг точки «$destName»!") + stateWithTime.dayLogs.take(19)
            } else {
                stateWithTime.dayLogs
            }

            stateWithTime.copy(
                activeTravel = updatedTravel,
                activeExpedition = updatedActiveExp,
                locations = updatedLocations,
                currentLocationId = updatedCurrentLocId,
                dayLogs = newLogs
            )
        }
    }

    /**
     * Instantly completes current travel and places the party at the destination.
     */
    fun instantArriveTravel() {
        advanceTravelStep(stepFraction = 1.0f)
    }

    /**
     * Initiates return journey from the current remote destination back to the settlement base.
     */
    fun startReturnTravel(mode: TravelTransportMode = _selectedTravelMode.value): TravelTransactionResult {
        var txResult: TravelTransactionResult = TravelTransactionResult.Failure(
            reason = TravelFailureReason.LOCATION_IS_CURRENT,
            message = "Ошибка при возвращении."
        )

        _gameState.update { state ->
            val (updatedState, result) = ExpeditionReturnService.startReturnJourney(state, mode)
            txResult = result
            updatedState
        }

        _lastTravelResult.value = txResult
        return txResult
    }

    /**
     * Initiates return journey directly from arrival screen back to settlement base.
     */
    fun returnExpeditionFromArrival(mode: TravelTransportMode = _selectedTravelMode.value): TravelTransactionResult {
        return startReturnTravel(mode)
    }

    /**
     * Transitions arrived expedition into detailed exploration phase.
     */
    fun startExplorationFromArrival() {
        _gameState.update { state ->
            val activeExp = state.activeExpedition
            val currentLoc = state.locations.find { it.id == state.currentLocationId }
                ?: activeExp?.location
                ?: return@update state

            val squadChars = activeExp?.squad
                ?: state.characters.filter { state.activeTravel?.participantIds?.contains(it.id) == true }
                    .ifEmpty { state.characters.filter { state.selectedSquadIds.contains(it.id) } }
                    .ifEmpty { listOf(state.characters.first()) }

            val vehicle = activeExp?.vehicle
                ?: state.vehicles.find { it.id == state.activeTravel?.vehicleId }
                ?: state.vehicles.find { it.id == state.selectedVehicleId }
                ?: state.vehicles.first()

            val effectiveSeed = activeExp?.seed ?: System.currentTimeMillis()

            val dummyExp = activeExp ?: Expedition(
                id = "exp_${System.currentTimeMillis()}",
                location = currentLoc,
                squad = squadChars,
                vehicle = vehicle,
                seed = effectiveSeed
            )

            // Select event from catalog using deterministic EventSelector
            val chosenEvent = EventSelector.selectNextEvent(
                catalog = EventCatalog.ALL_EVENTS,
                gameState = state,
                expedition = dummyExp,
                seed = effectiveSeed + (activeExp?.currentStep ?: 0)
            ) ?: EventCatalog.EVT_SCAVENGE_SUPPLIES

            val activeEventState = ActiveEventState(
                eventId = chosenEvent.id,
                event = chosenEvent,
                instanceSeed = effectiveSeed + (activeExp?.currentStep ?: 0)
            )

            val updatedExpedition = dummyExp.copy(
                phase = ExpeditionPhase.EXPLORING,
                status = ExpeditionStatus.EXPLORING,
                currentStep = 1,
                currentEvent = chosenEvent,
                activeEventState = activeEventState,
                logs = (activeExp?.logs ?: emptyList()) + "Отряд приступил к детальному осмотру сектора «${currentLoc.name}». Событие: «${chosenEvent.title}»."
            )

            // Mark location as explored
            val updatedLocations = state.locations.map {
                if (it.id == currentLoc.id) {
                    it.copy(
                        status = LocationStatus.EXPLORED,
                        timesExplored = it.timesExplored + 1,
                        explorationProgressPercent = (it.explorationProgressPercent + 35).coerceAtMost(100)
                    )
                } else it
            }

            state.copy(
                locations = updatedLocations,
                activeExpedition = updatedExpedition,
                dayLogs = listOf("🔎 Исследование: Отряд начал прочесывать сектор «${currentLoc.name}».") + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Conducts recon of surroundings without triggering combat or collecting loot yet.
     */
    fun scoutSurroundings(locationId: String) {
        _gameState.update { state ->
            val loc = state.locations.find { it.id == locationId } ?: return@update state
            val scoutChar = state.activeExpedition?.squad?.maxByOrNull { it.stats.scavengingSkill }
                ?: state.characters.firstOrNull()
            val scoutName = scoutChar?.name ?: "Разведчик"
            val logMsg = "🔭 Разведка: $scoutName провел(а) осмотр окрестностей «${loc.name}». Обнаружены ключевые ориентиры и подъездные пути."

            state.copy(
                dayLogs = listOf(logMsg) + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Explores the arrived location and launches expedition investigation.
     */
    fun exploreArrivedLocation() {
        startExplorationFromArrival()
    }

    /**
     * Safely resets travel state back to settlement base and frees squad members and vehicles.
     */
    fun cancelOrReturnToSettlement() {
        _gameState.update { state ->
            val usedVehId = state.activeTravel?.vehicleId
            val updatedCharacters = state.characters.map { char ->
                if (char.status == CharacterStatus.ON_EXPEDITION) {
                    char.copy(status = CharacterStatus.READY)
                } else char
            }

            val updatedVehicles = state.vehicles.map { veh ->
                if (veh.id == usedVehId && veh.status == VehicleStatus.IN_USE) {
                    veh.copy(
                        status = VehicleStatus.AVAILABLE,
                        tripsCompleted = veh.tripsCompleted + 1,
                        totalDistanceTraveledKm = veh.totalDistanceTraveledKm + (state.activeTravel?.distanceKm ?: 0)
                    )
                } else veh
            }

            state.copy(
                activeTravel = null,
                currentLocationId = "loc_base",
                characters = updatedCharacters,
                vehicles = updatedVehicles,
                dayLogs = listOf("Отряд вернулся в расположение Аванпоста «Фронтир».") + state.dayLogs.take(19)
            )
        }
    }

    // -------------------------------------------------------------
    // Vehicle Fleet Management
    // -------------------------------------------------------------

    /**
     * Adds a newly acquired or constructed vehicle to the settlement fleet.
     */
    fun addVehicleToFleet(vehicle: Vehicle) {
        _gameState.update { state ->
            state.copy(
                vehicles = state.vehicles + vehicle,
                dayLogs = listOf("Гараж: В автопарк базы добавлен новый транспорт «${vehicle.name}».") + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Repairs damaged vehicle back to 100% durability and AVAILABLE status.
     */
    fun repairVehicle(vehicleId: String, materialsCost: Int = 15): Boolean {
        var success = false
        _gameState.update { state ->
            val veh = state.vehicles.find { it.id == vehicleId } ?: return@update state
            if (state.resources.materials < materialsCost) return@update state

            val updatedVehicles = state.vehicles.map { v ->
                if (v.id == vehicleId) {
                    v.copy(
                        durabilityPercent = 100,
                        status = VehicleStatus.AVAILABLE
                    )
                } else v
            }

            val updatedRes = state.resources.copy(
                materials = state.resources.materials - materialsCost
            )

            success = true
            state.copy(
                vehicles = updatedVehicles,
                resources = updatedRes,
                dayLogs = listOf("Гараж: Транспорт «${veh.name}» полностью отремонтирован и готов к рейдам.") + state.dayLogs.take(19)
            )
        }
        return success
    }

    /**
     * Crafts and deploys a new vehicle from workshop materials.
     */
    fun craftVehicle(type: VehicleType, name: String, materialsCost: Int = 60, creditsCost: Int = 120): Boolean {
        var success = false
        _gameState.update { state ->
            if (state.resources.materials < materialsCost || state.resources.money < creditsCost) return@update state

            val newId = "veh_${type.name.lowercase()}_${System.currentTimeMillis()}"
            val newVehicle = Vehicle(
                id = newId,
                name = name.ifBlank { "${type.titleRu} №${state.vehicles.count { it.type == type } + 1}" },
                type = type,
                capacityKg = type.defaultCapacityKg,
                fuelConsumptionPerKm = type.defaultFuelPerKm,
                speedKmH = type.defaultSpeedKmH,
                maxPassengers = type.defaultMaxPassengers,
                status = VehicleStatus.AVAILABLE,
                durabilityPercent = 100,
                description = "Собран в мастерской аванпоста из высокопрочных сплавов.",
                visualAssetId = "veh_${type.name.lowercase()}"
            )

            val updatedRes = state.resources.copy(
                materials = state.resources.materials - materialsCost,
                money = state.resources.money - creditsCost
            )

            success = true
            state.copy(
                vehicles = state.vehicles + newVehicle,
                resources = updatedRes,
                dayLogs = listOf("Мастерская: Завершена сборка «${newVehicle.name}». Транспорт поступил в гараж!") + state.dayLogs.take(19)
            )
        }
        return success
    }

    fun resetGame() {
        _gameState.value = InitialGameData.createInitialGameState()
    }

    // -------------------------------------------------------------
    // Character RPG Progression & Attributes
    // -------------------------------------------------------------

    /**
     * Allocates an unspent skill point to a character attribute.
     */
    fun allocateCharacterSkillPoint(characterId: String, statType: CharacterStatType): Boolean {
        var success = false
        _gameState.update { state ->
            val char = state.characters.find { it.id == characterId } ?: return@update state
            if (char.unspentSkillPoints <= 0) return@update state

            val updatedChar = com.example.domain.service.CharacterProgressionService.allocateSkillPoint(char, statType)
            val updatedCharacters = state.characters.map {
                if (it.id == characterId) updatedChar else it
            }

            val statName = when (statType) {
                CharacterStatType.ATTACK -> "Атаку (+2)"
                CharacterStatType.DEFENSE -> "Защиту (+2)"
                CharacterStatType.SCAVENGING -> "Поиск (+3)"
                CharacterStatType.ENGINEERING -> "Инженерию (+3)"
                CharacterStatType.MEDICAL -> "Медицину (+3)"
                CharacterStatType.MAX_HEALTH -> "Макс. Здоровье (+15)"
            }
            val logMsg = "Прокачка: ${char.name} улучшил навык: $statName."
            _lastResourceOperation.value = ResourceOperationResult.Success(message = logMsg)
            success = true

            state.copy(
                characters = updatedCharacters,
                dayLogs = listOf(logMsg) + state.dayLogs.take(19)
            )
        }
        return success
    }

    /**
     * Awards experience points to a specific character.
     */
    fun awardCharacterExperience(characterId: String, amount: Int): com.example.domain.service.LevelUpOutcome? {
        var outcome: com.example.domain.service.LevelUpOutcome? = null
        _gameState.update { state ->
            val char = state.characters.find { it.id == characterId } ?: return@update state
            val res = com.example.domain.service.CharacterProgressionService.addExperience(char, amount)
            outcome = res

            val updatedCharacters = state.characters.map {
                if (it.id == characterId) res.updatedCharacter else it
            }

            val logMsg = if (res.leveledUp) {
                "🌟 ПОВЫШЕНИЕ: ${char.name} достиг УРОВНЯ ${res.newLevel}! Получено очков навыков: ${res.gainedSkillPoints}."
            } else {
                "${char.name} получил +$amount XP."
            }

            _lastResourceOperation.value = ResourceOperationResult.Success(message = logMsg)

            state.copy(
                characters = updatedCharacters,
                dayLogs = listOf(logMsg) + state.dayLogs.take(19)
            )
        }
        return outcome
    }

    /**
     * Awards experience to all active expedition squad members.
     */
    fun awardSquadExperience(amount: Int) {
        _gameState.update { state ->
            val squadIds = state.squad.memberIds.toSet()
            val newLogs = mutableListOf<String>()

            val updatedCharacters = state.characters.map { char ->
                if (squadIds.contains(char.id)) {
                    val outcome = com.example.domain.service.CharacterProgressionService.addExperience(char, amount)
                    if (outcome.leveledUp) {
                        newLogs.add("🌟 ПОВЫШЕНИЕ: ${char.name} достиг Ур. ${outcome.newLevel} (+${outcome.gainedSkillPoints} ОН)!")
                    }
                    outcome.updatedCharacter
                } else char
            }

            if (newLogs.isEmpty()) {
                newLogs.add("Отряд получил +$amount XP за экспедицию.")
            }

            state.copy(
                characters = updatedCharacters,
                dayLogs = newLogs + state.dayLogs.take(19 - newLogs.size)
            )
        }
    }

    // -------------------------------------------------------------
    // System 15: Character Equipment & Gear Loadouts
    // -------------------------------------------------------------

    /**
     * Equips an item to a character in the specified slot.
     */
    fun equipCharacterItem(
        characterId: String,
        slot: EquipmentSlotType,
        itemId: String
    ): EquipmentOperationResult {
        val currentState = _gameState.value
        val outcome = com.example.domain.service.EquipmentService.equipItem(
            characters = currentState.characters,
            allItems = currentState.inventoryItems,
            characterId = characterId,
            slot = slot,
            itemId = itemId
        )

        _lastEquipmentResult.value = outcome.result

        if (outcome.result is EquipmentOperationResult.Success) {
            _gameState.update { state ->
                state.copy(
                    characters = outcome.updatedCharacters,
                    dayLogs = listOf("⚔️ ЭКИПИРОВКА: ${outcome.result.message}") + state.dayLogs.take(19)
                )
            }
        }
        return outcome.result
    }

    /**
     * Unequips an item from the character's slot and returns it to the settlement warehouse.
     */
    fun unequipCharacterItem(
        characterId: String,
        slot: EquipmentSlotType
    ): EquipmentOperationResult {
        val currentState = _gameState.value
        val outcome = com.example.domain.service.EquipmentService.unequipItem(
            characters = currentState.characters,
            allItems = currentState.inventoryItems,
            characterId = characterId,
            slot = slot
        )

        _lastEquipmentResult.value = outcome.result

        if (outcome.result is EquipmentOperationResult.Success) {
            _gameState.update { state ->
                state.copy(
                    characters = outcome.updatedCharacters,
                    dayLogs = listOf("🎒 ЭКИПИРОВКА: ${outcome.result.message}") + state.dayLogs.take(19)
                )
            }
        }
        return outcome.result
    }

    fun equipItem(characterId: String, slot: EquipmentSlotType, itemId: String): EquipmentOperationResult =
        equipCharacterItem(characterId, slot, itemId)

    fun unequipItem(characterId: String, slot: EquipmentSlotType): EquipmentOperationResult =
        unequipCharacterItem(characterId, slot)

    /**
     * Retrieves warehouse items available for the given equipment slot.
     */
    fun getAvailableWarehouseItemsForSlot(
        slot: EquipmentSlotType,
        currentCharacterId: String? = null
    ): List<WarehouseItem> {
        val currentState = _gameState.value
        return com.example.domain.service.EquipmentService.getAvailableItemsForSlot(
            allItems = currentState.inventoryItems,
            characters = currentState.characters,
            slot = slot,
            excludeCurrentCharacterId = currentCharacterId
        )
    }

    // -------------------------------------------------------------
    // System 27: Settlement Economy & Accounting
    // -------------------------------------------------------------

    /**
     * Obtains the pure calculated economic forecast for the upcoming day.
     */
    fun getEconomyForecast(): com.example.domain.model.EconomyForecast {
        return com.example.domain.service.economy.SettlementEconomyProcessor.calculateDailyEconomyForecast(_gameState.value)
    }

    /**
     * Debug: Adds treasury credits directly.
     */
    fun debugAddTreasuryCredits(amount: Int = 100) {
        _gameState.update { state ->
            val updatedRes = state.resources.copy(money = (state.resources.money + amount).coerceAtLeast(0))
            val log = "🛠️ DEBUG: В казну добавлено +$amount Кредитов."
            state.copy(
                resources = updatedRes,
                dayLogs = listOf(log) + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Debug: Simulates economic shortages (sets resource to 0 to test alerts and deficits).
     */
    fun debugDrainResourceForDeficitTest(resourceType: ResourceType) {
        _gameState.update { state ->
            val res = state.resources
            val updatedRes = when (resourceType) {
                ResourceType.FOOD -> res.copy(food = 0)
                ResourceType.WATER -> res.copy(water = 0)
                ResourceType.FUEL -> res.copy(fuel = 0)
                ResourceType.MATERIALS -> res.copy(materials = 0)
                ResourceType.MONEY -> res.copy(money = 0)
                ResourceType.MEDICINE -> {
                    val extra = res.extraResources.toMutableMap()
                    extra[ResourceType.MEDICINE] = 0
                    res.copy(extraResources = extra)
                }
                else -> res
            }
            val log = "🛠️ DEBUG: Запас «${resourceType.titleRu}» обнулен для тестирования дефицита."
            state.copy(
                resources = updatedRes,
                dayLogs = listOf(log) + state.dayLogs.take(19)
            )
        }
    }

    /**
     * Clears historical unpaid deficits list.
     */
    fun clearUnpaidDeficits() {
        _gameState.update { state ->
            state.copy(unpaidDeficits = emptyList())
        }
    }
}


