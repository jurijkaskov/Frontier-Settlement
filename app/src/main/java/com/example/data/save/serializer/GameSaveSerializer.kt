package com.example.data.save.serializer

import com.example.core.log.GameLogger
import com.example.data.save.GameSaveFile
import com.example.domain.model.*
import com.example.domain.model.quest.QuestFailureCondition
import com.example.domain.model.quest.QuestRequirement
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.zip.CRC32

/**
 * Dedicated serializer converting [GameSaveFile] to and from JSON format.
 * Includes polymorphic adapters for game rule effects and CRC32 integrity checksums.
 */
class GameSaveSerializer {

    private val moshi: Moshi = Moshi.Builder()
        .add(TechEffectJsonAdapter())
        .add(EventRequirementJsonAdapter())
        .add(QuestRequirementJsonAdapter())
        .add(QuestFailureConditionJsonAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val saveFileAdapter: JsonAdapter<GameSaveFile> = moshi.adapter(GameSaveFile::class.java).indent("  ")

    /**
     * Serializes a [GameSaveFile] into an indented JSON string with integrity checksum.
     */
    fun serialize(saveFile: GameSaveFile): String {
        val fileWithPlaceholder = saveFile.copy(checksum = "")
        val rawJson = saveFileAdapter.toJson(fileWithPlaceholder)
        val checksum = computeChecksum(rawJson)
        val finalFile = saveFile.copy(checksum = checksum)
        return saveFileAdapter.toJson(finalFile)
    }

    /**
     * Deserializes JSON string into [GameSaveFile] and validates checksum integrity.
     */
    fun deserialize(json: String, verifyChecksum: Boolean = true): GameSaveFile {
        val saveFile = saveFileAdapter.fromJson(json)
            ?: throw JsonDataException("Deserialized GameSaveFile is null")

        if (verifyChecksum && saveFile.checksum.isNotBlank()) {
            val fileWithoutChecksum = saveFile.copy(checksum = "")
            val rawWithoutChecksum = saveFileAdapter.toJson(fileWithoutChecksum)
            val computed = computeChecksum(rawWithoutChecksum)
            if (computed != saveFile.checksum) {
                GameLogger.w("GameSaveSerializer", "Checksum mismatch! Expected: ${saveFile.checksum}, Calculated: $computed")
            }
        }

        return saveFile
    }

    /**
     * Computes 8-character hex CRC32 checksum of content.
     */
    fun computeChecksum(content: String): String {
        val crc = CRC32()
        crc.update(content.toByteArray(Charsets.UTF_8))
        return java.lang.Long.toHexString(crc.value).padStart(8, '0')
    }
}

// -------------------------------------------------------------
// Custom Moshi JSON Adapters for Polymorphic Domain Interfaces
// -------------------------------------------------------------

class TechEffectJsonAdapter {
    @ToJson
    fun toJson(writer: JsonWriter, effect: TechEffect?) {
        if (effect == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        when (effect) {
            is TechEffect.StorageCapacityBoost -> {
                writer.name("type").value("StorageCapacityBoost")
                writer.name("additionalCapacity").value(effect.additionalCapacity)
                writer.name("summaryRu").value(effect.summaryRu)
            }
            is TechEffect.ResourceProductionMultiplier -> {
                writer.name("type").value("ResourceProductionMultiplier")
                writer.name("resourceType").value(effect.resourceType.name)
                writer.name("multiplierPercent").value(effect.multiplierPercent)
                writer.name("summaryRu").value(effect.summaryRu)
            }
            is TechEffect.RecipeUnlock -> {
                writer.name("type").value("RecipeUnlock")
                writer.name("recipeId").value(effect.recipeId)
                writer.name("recipeNameRu").value(effect.recipeNameRu)
                writer.name("summaryRu").value(effect.summaryRu)
            }
            is TechEffect.TradeBonus -> {
                writer.name("type").value("TradeBonus")
                writer.name("discountPercent").value(effect.discountPercent)
                writer.name("unlocksRareGoods").value(effect.unlocksRareGoods)
                writer.name("summaryRu").value(effect.summaryRu)
            }
            is TechEffect.SquadStatBonus -> {
                writer.name("type").value("SquadStatBonus")
                writer.name("attackBonus").value(effect.attackBonus)
                writer.name("defenseBonus").value(effect.defenseBonus)
                writer.name("healthBonus").value(effect.healthBonus)
                writer.name("summaryRu").value(effect.summaryRu)
            }
            is TechEffect.VehicleCargoMultiplier -> {
                writer.name("type").value("VehicleCargoMultiplier")
                writer.name("multiplierPercent").value(effect.multiplierPercent)
                writer.name("summaryRu").value(effect.summaryRu)
            }
            is TechEffect.LocationUnlock -> {
                writer.name("type").value("LocationUnlock")
                writer.name("locationId").value(effect.locationId)
                writer.name("locationNameRu").value(effect.locationNameRu)
                writer.name("summaryRu").value(effect.summaryRu)
            }
            is TechEffect.FuelEfficiency -> {
                writer.name("type").value("FuelEfficiency")
                writer.name("reductionPercent").value(effect.reductionPercent)
                writer.name("summaryRu").value(effect.summaryRu)
            }
            is TechEffect.MedicalEfficiency -> {
                writer.name("type").value("MedicalEfficiency")
                writer.name("regenBonusHp").value(effect.regenBonusHp)
                writer.name("summaryRu").value(effect.summaryRu)
            }
        }
        writer.endObject()
    }

    @FromJson
    fun fromJson(reader: JsonReader): TechEffect? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        reader.beginObject()
        var type = ""
        var additionalCapacity = 0
        var resourceType = ResourceType.MATERIALS
        var multiplierPercent = 0
        var recipeId = ""
        var recipeNameRu = ""
        var discountPercent = 0
        var unlocksRareGoods = false
        var attackBonus = 0
        var defenseBonus = 0
        var healthBonus = 0
        var locationId = ""
        var locationNameRu = ""
        var reductionPercent = 0
        var regenBonusHp = 0
        var summaryRu = ""

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> type = reader.nextString()
                "additionalCapacity" -> additionalCapacity = reader.nextInt()
                "resourceType" -> resourceType = try { ResourceType.valueOf(reader.nextString()) } catch (e: Exception) { ResourceType.MATERIALS }
                "multiplierPercent" -> multiplierPercent = reader.nextInt()
                "recipeId" -> recipeId = reader.nextString()
                "recipeNameRu" -> recipeNameRu = reader.nextString()
                "discountPercent" -> discountPercent = reader.nextInt()
                "unlocksRareGoods" -> unlocksRareGoods = reader.nextBoolean()
                "attackBonus" -> attackBonus = reader.nextInt()
                "defenseBonus" -> defenseBonus = reader.nextInt()
                "healthBonus" -> healthBonus = reader.nextInt()
                "locationId" -> locationId = reader.nextString()
                "locationNameRu" -> locationNameRu = reader.nextString()
                "reductionPercent" -> reductionPercent = reader.nextInt()
                "regenBonusHp" -> regenBonusHp = reader.nextInt()
                "summaryRu" -> summaryRu = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return when (type) {
            "StorageCapacityBoost" -> TechEffect.StorageCapacityBoost(additionalCapacity, summaryRu.ifBlank { "+$additionalCapacity к складу" })
            "ResourceProductionMultiplier" -> TechEffect.ResourceProductionMultiplier(resourceType, multiplierPercent, summaryRu)
            "RecipeUnlock" -> TechEffect.RecipeUnlock(recipeId, recipeNameRu, summaryRu)
            "TradeBonus" -> TechEffect.TradeBonus(discountPercent, unlocksRareGoods, summaryRu)
            "SquadStatBonus" -> TechEffect.SquadStatBonus(attackBonus, defenseBonus, healthBonus, summaryRu)
            "VehicleCargoMultiplier" -> TechEffect.VehicleCargoMultiplier(multiplierPercent, summaryRu)
            "LocationUnlock" -> TechEffect.LocationUnlock(locationId, locationNameRu, summaryRu)
            "FuelEfficiency" -> TechEffect.FuelEfficiency(reductionPercent, summaryRu)
            "MedicalEfficiency" -> TechEffect.MedicalEfficiency(regenBonusHp, summaryRu)
            else -> TechEffect.StorageCapacityBoost(100, "+100 к складу")
        }
    }
}

class EventRequirementJsonAdapter {
    @ToJson
    fun toJson(writer: JsonWriter, req: EventRequirement?) {
        if (req == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        when (req) {
            is EventRequirement.RequiresRole -> {
                writer.name("type").value("RequiresRole")
                writer.name("role").value(req.role.name)
            }
            is EventRequirement.RequiresSpecialization -> {
                writer.name("type").value("RequiresSpecialization")
                writer.name("specialization").value(req.specialization)
            }
            is EventRequirement.RequiresTrait -> {
                writer.name("type").value("RequiresTrait")
                writer.name("traitId").value(req.traitId)
            }
            is EventRequirement.RequiresItem -> {
                writer.name("type").value("RequiresItem")
                writer.name("itemId").value(req.itemId)
                writer.name("minCount").value(req.minCount)
            }
            is EventRequirement.RequiresResource -> {
                writer.name("type").value("RequiresResource")
                writer.name("resType").value(req.type.name)
                writer.name("minAmount").value(req.minAmount)
            }
            is EventRequirement.RequiresVehicle -> {
                writer.name("type").value("RequiresVehicle")
                writer.name("hasVehicle").value(req.hasVehicle)
            }
            is EventRequirement.RequiresWorldFlag -> {
                writer.name("type").value("RequiresWorldFlag")
                writer.name("flag").value(req.flag)
                writer.name("expectedValue").value(req.expectedValue)
            }
            is EventRequirement.RequiresTech -> {
                writer.name("type").value("RequiresTech")
                writer.name("techId").value(req.techId)
            }
            is EventRequirement.RequiresMinStat -> {
                writer.name("type").value("RequiresMinStat")
                writer.name("statType").value(req.statType.name)
                writer.name("minValue").value(req.minValue)
            }
            is EventRequirement.RequiresArea -> {
                writer.name("type").value("RequiresArea")
                writer.name("areaId").value(req.areaId)
            }
            is EventRequirement.RequiresMinExplorationProgress -> {
                writer.name("type").value("RequiresMinExplorationProgress")
                writer.name("minPercent").value(req.minPercent)
            }
            is EventRequirement.RequiresMaxExplorationProgress -> {
                writer.name("type").value("RequiresMaxExplorationProgress")
                writer.name("maxPercent").value(req.maxPercent)
            }
            is EventRequirement.RequiresMinVisits -> {
                writer.name("type").value("RequiresMinVisits")
                writer.name("minVisits").value(req.minVisits)
            }
            is EventRequirement.RequiresTimeRange -> {
                writer.name("type").value("RequiresTimeRange")
                writer.name("startHour").value(req.startHour)
                writer.name("endHour").value(req.endHour)
            }
            is EventRequirement.RequiresMinReputation -> {
                writer.name("type").value("RequiresMinReputation")
                writer.name("minReputation").value(req.minReputation)
            }
            is EventRequirement.RequiresFactionRelation -> {
                writer.name("type").value("RequiresFactionRelation")
                writer.name("factionId").value(req.factionId)
                writer.name("minRelationPoints").value(req.minRelationPoints)
            }
            is EventRequirement.LocationTypeRequirement -> {
                writer.name("type").value("LocationTypeRequirement")
            }
            is EventRequirement.MinDangerRequirement -> {
                writer.name("type").value("MinDangerRequirement")
                writer.name("min").value(req.min.name)
            }
            is EventRequirement.MaxDangerRequirement -> {
                writer.name("type").value("MaxDangerRequirement")
                writer.name("max").value(req.max.name)
            }
            is EventRequirement.RequiresDayPeriod -> {
                writer.name("type").value("RequiresDayPeriod")
            }
        }
        writer.endObject()
    }

    @FromJson
    fun fromJson(reader: JsonReader): EventRequirement? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        reader.beginObject()
        var type = ""
        var role = CharacterRole.SCAVENGER
        var specialization = ""
        var traitId = ""
        var itemId = ""
        var minCount = 1
        var resType = ResourceType.FOOD
        var minAmount = 1
        var hasVehicle = true
        var flag = ""
        var expectedValue = true
        var techId = ""
        var statType = CharacterStatType.ATTACK
        var minValue = 1
        var areaId = ""
        var minPercent = 0
        var maxPercent = 100
        var minVisits = 1
        var startHour = 0
        var endHour = 24
        var minReputation = 0
        var factionId = ""
        var minRelationPoints = 0
        var minDanger = DangerLevel.SAFE
        var maxDanger = DangerLevel.EXTREME

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> type = reader.nextString()
                "role" -> role = try { CharacterRole.valueOf(reader.nextString()) } catch (e: Exception) { CharacterRole.SCAVENGER }
                "specialization" -> specialization = reader.nextString()
                "traitId" -> traitId = reader.nextString()
                "itemId" -> itemId = reader.nextString()
                "minCount" -> minCount = reader.nextInt()
                "resType" -> resType = try { ResourceType.valueOf(reader.nextString()) } catch (e: Exception) { ResourceType.FOOD }
                "minAmount" -> minAmount = reader.nextInt()
                "hasVehicle" -> hasVehicle = reader.nextBoolean()
                "flag" -> flag = reader.nextString()
                "expectedValue" -> expectedValue = reader.nextBoolean()
                "techId" -> techId = reader.nextString()
                "statType" -> statType = try { CharacterStatType.valueOf(reader.nextString()) } catch (e: Exception) { CharacterStatType.ATTACK }
                "minValue" -> minValue = reader.nextInt()
                "areaId" -> areaId = reader.nextString()
                "minPercent" -> minPercent = reader.nextInt()
                "maxPercent" -> maxPercent = reader.nextInt()
                "minVisits" -> minVisits = reader.nextInt()
                "startHour" -> startHour = reader.nextInt()
                "endHour" -> endHour = reader.nextInt()
                "minReputation" -> minReputation = reader.nextInt()
                "factionId" -> factionId = reader.nextString()
                "minRelationPoints" -> minRelationPoints = reader.nextInt()
                "min" -> minDanger = try { DangerLevel.valueOf(reader.nextString()) } catch (e: Exception) { DangerLevel.SAFE }
                "max" -> maxDanger = try { DangerLevel.valueOf(reader.nextString()) } catch (e: Exception) { DangerLevel.EXTREME }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return when (type) {
            "RequiresRole" -> EventRequirement.RequiresRole(role)
            "RequiresSpecialization" -> EventRequirement.RequiresSpecialization(specialization)
            "RequiresTrait" -> EventRequirement.RequiresTrait(traitId)
            "RequiresItem" -> EventRequirement.RequiresItem(itemId, minCount)
            "RequiresResource" -> EventRequirement.RequiresResource(resType, minAmount)
            "RequiresVehicle" -> EventRequirement.RequiresVehicle(hasVehicle)
            "RequiresWorldFlag" -> EventRequirement.RequiresWorldFlag(flag, expectedValue)
            "RequiresTech" -> EventRequirement.RequiresTech(techId)
            "RequiresMinStat" -> EventRequirement.RequiresMinStat(statType, minValue)
            "RequiresArea" -> EventRequirement.RequiresArea(areaId)
            "RequiresMinExplorationProgress" -> EventRequirement.RequiresMinExplorationProgress(minPercent)
            "RequiresMaxExplorationProgress" -> EventRequirement.RequiresMaxExplorationProgress(maxPercent)
            "RequiresMinVisits" -> EventRequirement.RequiresMinVisits(minVisits)
            "RequiresTimeRange" -> EventRequirement.RequiresTimeRange(startHour, endHour)
            "RequiresMinReputation" -> EventRequirement.RequiresMinReputation(minReputation)
            "RequiresFactionRelation" -> EventRequirement.RequiresFactionRelation(factionId, minRelationPoints)
            "MinDangerRequirement" -> EventRequirement.MinDangerRequirement(minDanger)
            "MaxDangerRequirement" -> EventRequirement.MaxDangerRequirement(maxDanger)
            else -> EventRequirement.RequiresVehicle(false)
        }
    }
}

class QuestRequirementJsonAdapter {
    @ToJson
    fun toJson(writer: JsonWriter, req: QuestRequirement?) {
        if (req == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        when (req) {
            is QuestRequirement.MinSettlementLevel -> {
                writer.name("type").value("MinSettlementLevel")
                writer.name("level").value(req.level)
            }
            is QuestRequirement.MinReputation -> {
                writer.name("type").value("MinReputation")
                writer.name("minPoints").value(req.minPoints)
            }
            is QuestRequirement.MinFactionRelation -> {
                writer.name("type").value("MinFactionRelation")
                writer.name("factionId").value(req.factionId)
                writer.name("minPoints").value(req.minPoints)
            }
            is QuestRequirement.WorldFlag -> {
                writer.name("type").value("WorldFlag")
                writer.name("flag").value(req.flag)
                writer.name("expectedValue").value(req.expectedValue)
            }
            is QuestRequirement.CompletedQuest -> {
                writer.name("type").value("CompletedQuest")
                writer.name("questId").value(req.questId)
            }
            is QuestRequirement.LocationDiscovered -> {
                writer.name("type").value("LocationDiscovered")
                writer.name("locationId").value(req.locationId)
            }
            is QuestRequirement.TechnologyResearched -> {
                writer.name("type").value("TechnologyResearched")
                writer.name("techId").value(req.techId)
            }
            is QuestRequirement.BuildingConstructed -> {
                writer.name("type").value("BuildingConstructed")
                writer.name("buildingType").value(req.buildingType.name)
                writer.name("minLevel").value(req.minLevel)
            }
            is QuestRequirement.MinDay -> {
                writer.name("type").value("MinDay")
                writer.name("day").value(req.day)
            }
        }
        writer.endObject()
    }

    @FromJson
    fun fromJson(reader: JsonReader): QuestRequirement? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        reader.beginObject()
        var type = ""
        var level = 1
        var minPoints = 0
        var factionId = ""
        var flag = ""
        var expectedValue = true
        var questId = ""
        var locationId = ""
        var techId = ""
        var buildingType = BuildingType.HQ_COMMAND
        var minLevel = 1
        var day = 1

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> type = reader.nextString()
                "level" -> level = reader.nextInt()
                "minPoints" -> minPoints = reader.nextInt()
                "factionId" -> factionId = reader.nextString()
                "flag" -> flag = reader.nextString()
                "expectedValue" -> expectedValue = reader.nextBoolean()
                "questId" -> questId = reader.nextString()
                "locationId" -> locationId = reader.nextString()
                "techId" -> techId = reader.nextString()
                "buildingType" -> buildingType = try { BuildingType.valueOf(reader.nextString()) } catch (e: Exception) { BuildingType.HQ_COMMAND }
                "minLevel" -> minLevel = reader.nextInt()
                "day" -> day = reader.nextInt()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return when (type) {
            "MinSettlementLevel" -> QuestRequirement.MinSettlementLevel(level)
            "MinReputation" -> QuestRequirement.MinReputation(minPoints)
            "MinFactionRelation" -> QuestRequirement.MinFactionRelation(factionId, minPoints)
            "WorldFlag" -> QuestRequirement.WorldFlag(flag, expectedValue)
            "CompletedQuest" -> QuestRequirement.CompletedQuest(questId)
            "LocationDiscovered" -> QuestRequirement.LocationDiscovered(locationId)
            "TechnologyResearched" -> QuestRequirement.TechnologyResearched(techId)
            "BuildingConstructed" -> QuestRequirement.BuildingConstructed(buildingType, minLevel)
            "MinDay" -> QuestRequirement.MinDay(day)
            else -> QuestRequirement.MinSettlementLevel(1)
        }
    }
}

class QuestFailureConditionJsonAdapter {
    @ToJson
    fun toJson(writer: JsonWriter, cond: QuestFailureCondition?) {
        if (cond == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        when (cond) {
            is QuestFailureCondition.IncompatibleWorldFlag -> {
                writer.name("type").value("IncompatibleWorldFlag")
                writer.name("flag").value(cond.flag)
                writer.name("failureValue").value(cond.failureValue)
                writer.name("reasonRu").value(cond.reasonRu)
            }
            is QuestFailureCondition.FactionRelationBelow -> {
                writer.name("type").value("FactionRelationBelow")
                writer.name("factionId").value(cond.factionId)
                writer.name("thresholdPoints").value(cond.thresholdPoints)
                writer.name("reasonRu").value(cond.reasonRu)
            }
            is QuestFailureCondition.TimeLimitExpired -> {
                writer.name("type").value("TimeLimitExpired")
                writer.name("reasonRu").value(cond.reasonRu)
            }
            is QuestFailureCondition.TargetDestroyed -> {
                writer.name("type").value("TargetDestroyed")
                writer.name("targetId").value(cond.targetId)
                writer.name("reasonRu").value(cond.reasonRu)
            }
            is QuestFailureCondition.CustomCondition -> {
                writer.name("type").value("CustomCondition")
                writer.name("conditionId").value(cond.conditionId)
                writer.name("reasonRu").value(cond.reasonRu)
            }
        }
        writer.endObject()
    }

    @FromJson
    fun fromJson(reader: JsonReader): QuestFailureCondition? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        reader.beginObject()
        var type = ""
        var flag = ""
        var failureValue = true
        var factionId = ""
        var thresholdPoints = 0
        var targetId = ""
        var conditionId = ""
        var reasonRu = ""

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> type = reader.nextString()
                "flag" -> flag = reader.nextString()
                "failureValue" -> failureValue = reader.nextBoolean()
                "factionId" -> factionId = reader.nextString()
                "thresholdPoints" -> thresholdPoints = reader.nextInt()
                "targetId" -> targetId = reader.nextString()
                "conditionId" -> conditionId = reader.nextString()
                "reasonRu" -> reasonRu = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return when (type) {
            "IncompatibleWorldFlag" -> QuestFailureCondition.IncompatibleWorldFlag(flag, failureValue, reasonRu)
            "FactionRelationBelow" -> QuestFailureCondition.FactionRelationBelow(factionId, thresholdPoints, reasonRu)
            "TimeLimitExpired" -> QuestFailureCondition.TimeLimitExpired(reasonRu.ifBlank { "Истёк срок выполнения контракта" })
            "TargetDestroyed" -> QuestFailureCondition.TargetDestroyed(targetId, reasonRu)
            else -> QuestFailureCondition.CustomCondition(conditionId, reasonRu)
        }
    }
}
