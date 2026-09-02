package com.example.domain.service.events

import com.example.domain.model.*

/**
 * Result of evaluating a set of event or choice requirements.
 */
data class RequirementEvaluationResult(
    val isMet: Boolean,
    val reasons: List<String> = emptyList(),
    val lockDescription: String? = null
)

/**
 * Centralized evaluator to match and validate requirements for events and choices against
 * the current Expedition, Squad, Location, GameState, and World Flags.
 */
object EventRequirementEvaluator {

    /**
     * Checks if all conditions in a requirements list are satisfied.
     */
    fun evaluate(
        requirements: List<EventRequirement>,
        gameState: GameState,
        expedition: Expedition,
        currentLocation: Location = expedition.location,
        currentAreaId: String? = expedition.currentAreaId
    ): RequirementEvaluationResult {
        if (requirements.isEmpty()) {
            return RequirementEvaluationResult(isMet = true)
        }

        val failedReasons = mutableListOf<String>()
        val lockDescriptions = mutableListOf<String>()

        for (req in requirements) {
            when (req) {
                is EventRequirement.LocationTypeRequirement -> {
                    if (!req.types.contains(currentLocation.type)) {
                        failedReasons.add("Тип локации «${currentLocation.type.titleRu}» не подходит.")
                        lockDescriptions.add("🔒 Недоступно в данной локации")
                    }
                }
                is EventRequirement.MinDangerRequirement -> {
                    if (currentLocation.dangerLevel.rating < req.min.rating) {
                        failedReasons.add("Уровень опасности ниже требуемого (${req.min.titleRu}).")
                        lockDescriptions.add("🔒 Требуется угроза: ${req.min.titleRu}")
                    }
                }
                is EventRequirement.MaxDangerRequirement -> {
                    if (currentLocation.dangerLevel.rating > req.max.rating) {
                        failedReasons.add("Уровень опасности выше допустимого (${req.max.titleRu}).")
                        lockDescriptions.add("🔒 Зона слишком опасна")
                    }
                }
                is EventRequirement.RequiresRole -> {
                    val hasRole = expedition.squad.any { it.role == req.role }
                    if (!hasRole) {
                        failedReasons.add("В отряде отсутствует ${req.role.titleRu}.")
                        lockDescriptions.add("🔒 Требуется: ${req.role.titleRu}")
                    }
                }
                is EventRequirement.RequiresSpecialization -> {
                    val hasSpec = expedition.squad.any { it.specialization.equals(req.specialization, ignoreCase = true) }
                    if (!hasSpec) {
                        failedReasons.add("В отряде отсутствует специалист «${req.specialization}».")
                        lockDescriptions.add("🔒 Нужна специализация: ${req.specialization}")
                    }
                }
                is EventRequirement.RequiresTrait -> {
                    val hasTrait = expedition.squad.any { c -> c.traits.any { it.id == req.traitId } }
                    if (!hasTrait) {
                        failedReasons.add("Ни один боец не обладает чертой «${req.traitId}».")
                        lockDescriptions.add("🔒 Требуется особая черта характера")
                    }
                }
                is EventRequirement.RequiresItem -> {
                    // Check if carried in expedition supplies, equipped by a squad member, or in settlement inventory
                    val carriedInExpedition = expedition.carriedItemIds.contains(req.itemId)
                    val equippedInSquad = expedition.squad.any { char ->
                        char.equipment.slots.values.contains(req.itemId)
                    }
                    val inSettlement = gameState.inventoryItems.any { it.id == req.itemId && it.quantity >= req.minCount }

                    if (!carriedInExpedition && !equippedInSquad && !inSettlement) {
                        val itemDisplayName = req.itemNameHint ?: gameState.inventoryItems.find { it.id == req.itemId }?.name ?: req.itemId
                        failedReasons.add("Отсутствует необходимый предмет «$itemDisplayName».")
                        lockDescriptions.add("🔒 Требуется: $itemDisplayName")
                    }
                }
                is EventRequirement.RequiresResource -> {
                    val inSupplies = expedition.supplies[req.type] ?: 0
                    if (inSupplies < req.minAmount) {
                        failedReasons.add("Недостаточно ресурса «${req.type.nameRu}» в припасах экспедиции (нужно ${req.minAmount}).")
                        lockDescriptions.add("🔒 Требуется: ${req.minAmount} ${req.type.nameRu}")
                    }
                }
                is EventRequirement.RequiresVehicle -> {
                    val isVehiclePresent = expedition.travelMode != TravelTransportMode.FOOT && expedition.vehicle.id != "veh_foot"
                    if (req.hasVehicle && !isVehiclePresent) {
                        failedReasons.add("Экспедиция выдвинулась пешком, транспорт отсутствует.")
                        lockDescriptions.add("🔒 Требуется транспорт")
                    } else if (!req.hasVehicle && isVehiclePresent) {
                        failedReasons.add("Транспорт мешает пройти по узкому проходу.")
                        lockDescriptions.add("🔒 Только для пешего отряда")
                    }
                }
                is EventRequirement.RequiresWorldFlag -> {
                    val flagValue = gameState.worldFlags[req.flag] ?: false
                    if (flagValue != req.expectedValue) {
                        failedReasons.add("Сюжетное условие [${req.flag}] не выполнено.")
                        lockDescriptions.add("🔒 Недоступно")
                    }
                }
                is EventRequirement.RequiresTech -> {
                    val isResearched = gameState.technologies.any { it.id == req.techId && it.isResearched }
                    if (!isResearched) {
                        val techName = gameState.technologies.find { it.id == req.techId }?.title ?: req.techId
                        failedReasons.add("Технология «$techName» ещё не исследована.")
                        lockDescriptions.add("🔒 Нужна технология: $techName")
                    }
                }
                is EventRequirement.RequiresMinStat -> {
                    val meetsStat = expedition.squad.any { char ->
                        val effective = char.getEffectiveStats(gameState.inventoryItems)
                        val statVal = when (req.statType) {
                            CharacterStatType.ATTACK -> effective.attack
                            CharacterStatType.DEFENSE -> effective.defense
                            CharacterStatType.SCAVENGING -> effective.scavengingSkill
                            CharacterStatType.ENGINEERING -> effective.engineeringSkill
                            CharacterStatType.MEDICAL -> effective.medicalSkill
                            CharacterStatType.MAX_HEALTH -> char.getEffectiveMaxHealth(gameState.inventoryItems) / 10
                        }
                        statVal >= req.minValue
                    }
                    if (!meetsStat) {
                        failedReasons.add("Ни у одного участника нет требуемого значения ${req.statType.titleRu} (>= ${req.minValue}).")
                        lockDescriptions.add("🔒 Требуется ${req.statType.titleRu} >= ${req.minValue}")
                    }
                }
                is EventRequirement.RequiresArea -> {
                    if (currentAreaId != null && currentAreaId != req.areaId) {
                        failedReasons.add("Действие возможно только в зоне «${req.areaId}».")
                        lockDescriptions.add("🔒 Доступно в другой зоне сектора")
                    }
                }
                is EventRequirement.RequiresMinExplorationProgress -> {
                    if (expedition.explorationProgress < req.minPercent && currentLocation.explorationProgressPercent < req.minPercent) {
                        failedReasons.add("Прогресс исследования локации ниже ${req.minPercent}%.")
                        lockDescriptions.add("🔒 Требуется исследование >= ${req.minPercent}%")
                    }
                }
                is EventRequirement.RequiresMaxExplorationProgress -> {
                    if (expedition.explorationProgress > req.maxPercent || currentLocation.explorationProgressPercent > req.maxPercent) {
                        failedReasons.add("Прогресс исследования выше ${req.maxPercent}%.")
                        lockDescriptions.add("🔒 Сектор уже слишком исследован")
                    }
                }
                is EventRequirement.RequiresMinVisits -> {
                    if (currentLocation.visitCount < req.minVisits) {
                        failedReasons.add("Локация должна быть посещена не менее ${req.minVisits} раз.")
                        lockDescriptions.add("🔒 Требуется визитов >= ${req.minVisits}")
                    }
                }
                is EventRequirement.RequiresDayPeriod -> {
                    val currentPeriod = gameState.gameDateTime.dayPeriod
                    if (!req.periods.contains(currentPeriod)) {
                        val validNames = req.periods.joinToString(", ") { it.titleRu }
                        failedReasons.add("Событие доступно только в период: $validNames (сейчас ${currentPeriod.titleRu}).")
                        lockDescriptions.add("🔒 Доступно: $validNames")
                    }
                }
                is EventRequirement.RequiresTimeRange -> {
                    val currentHour = gameState.gameDateTime.hour
                    val inRange = if (req.startHour <= req.endHour) {
                        currentHour in req.startHour..req.endHour
                    } else {
                        currentHour >= req.startHour || currentHour <= req.endHour
                    }
                    if (!inRange) {
                        failedReasons.add("Доступно только в промежуток %02d:00 - %02d:00.".format(req.startHour, req.endHour))
                        lockDescriptions.add("🔒 Доступно: %02d:00-%02d:00".format(req.startHour, req.endHour))
                    }
                }
                is EventRequirement.RequiresMinReputation -> {
                    val currentRep = gameState.settlement.reputation
                    if (currentRep < req.minReputation) {
                        failedReasons.add("Репутация поселения слишком низкая ($currentRep, требуется >= ${req.minReputation}).")
                        lockDescriptions.add("🔒 Требуется репутация >= ${req.minReputation}")
                    }
                }
                is EventRequirement.RequiresFactionRelation -> {
                    val currentPoints = gameState.factionRelations[req.factionId]?.points ?: 0
                    if (currentPoints < req.minRelationPoints) {
                        val factionName = com.example.data.ReputationBalanceConfig.getFaction(req.factionId)?.nameRu ?: req.factionId
                        failedReasons.add("Отношения с фракцией «$factionName» недостаточны ($currentPoints, требуется >= ${req.minRelationPoints}).")
                        lockDescriptions.add("🔒 Отношения с «$factionName» >= ${req.minRelationPoints}")
                    }
                }
            }
        }

        val isMet = failedReasons.isEmpty()
        val lockDesc = if (isMet) null else lockDescriptions.firstOrNull() ?: "🔒 Условия не выполнены"

        return RequirementEvaluationResult(
            isMet = isMet,
            reasons = failedReasons,
            lockDescription = lockDesc
        )
    }
}
