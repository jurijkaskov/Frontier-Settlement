package com.example.domain.service

import com.example.domain.model.Character
import com.example.domain.model.CharacterRole
import com.example.domain.model.CharacterStatType
import com.example.domain.model.CharacterStats
import com.example.domain.model.CharacterTrait
import com.example.domain.model.TraitCatalog
import com.example.domain.model.TraitCategory

data class LevelUpOutcome(
    val updatedCharacter: Character,
    val leveledUp: Boolean,
    val oldLevel: Int,
    val newLevel: Int,
    val gainedSkillPoints: Int,
    val newTraitUnlocked: CharacterTrait? = null
)

/**
 * Service encapsulating RPG leveling math, XP progression, skill point allocation,
 * and trait assignment for characters.
 */
object CharacterProgressionService {

    /**
     * Calculates XP needed to reach next level from current level.
     * Level 1 -> 100 XP
     * Level 2 -> 220 XP
     * Level 3 -> 380 XP
     * Level 4 -> 580 XP
     * Level 5 -> 820 XP
     */
    fun calculateMaxXpForLevel(level: Int): Int {
        val base = 100
        val growth = (level - 1) * 60 + (level * level * 10)
        return base + growth
    }

    /**
     * Grants experience to a character and processes potential level-ups.
     */
    fun addExperience(character: Character, amount: Int): LevelUpOutcome {
        var currentExp = character.experience + amount
        var currentLevel = character.level
        var maxExp = character.maxExperience
        var skillPoints = character.unspentSkillPoints
        var baseStats = character.stats
        var newMaxHp = character.maxHealth
        var currentHp = character.health
        var newTrait: CharacterTrait? = null
        var leveledUp = false
        val initialLevel = character.level

        // Check for multiple level ups if huge XP was awarded
        while (currentExp >= maxExp) {
            currentExp -= maxExp
            currentLevel += 1
            maxExp = calculateMaxXpForLevel(currentLevel)
            skillPoints += 2 // 2 skill points per level
            leveledUp = true

            // Automatic stat progression based on primary role
            baseStats = when (character.role) {
                CharacterRole.SOLDIER -> baseStats.copy(
                    attack = baseStats.attack + 2,
                    defense = baseStats.defense + 1
                )
                CharacterRole.SCOUT -> baseStats.copy(
                    scavengingSkill = baseStats.scavengingSkill + 2,
                    attack = baseStats.attack + 1
                )
                CharacterRole.ENGINEER -> baseStats.copy(
                    engineeringSkill = baseStats.engineeringSkill + 2,
                    defense = baseStats.defense + 1
                )
                CharacterRole.MEDIC -> baseStats.copy(
                    medicalSkill = baseStats.medicalSkill + 2,
                    defense = baseStats.defense + 1
                )
                CharacterRole.SCAVENGER -> baseStats.copy(
                    scavengingSkill = baseStats.scavengingSkill + 2,
                    defense = baseStats.defense + 1
                )
            }

            newMaxHp += 5
            currentHp = (currentHp + 15).coerceAtMost(newMaxHp + character.traits.sumOf { it.bonusMaxHealth })

            // Unlock a new trait at level 3 or level 5 if space allows (max 4 traits)
            if ((currentLevel == 3 || currentLevel == 5) && character.traits.size < 4) {
                val existingIds = character.traits.map { it.id }.toSet()
                val availableTraits = TraitCatalog.ALL_TRAITS.filter { it.id !in existingIds }
                if (availableTraits.isNotEmpty()) {
                    newTrait = availableTraits.random()
                }
            }
        }

        val updatedTraits = if (newTrait != null) {
            character.traits + newTrait
        } else {
            character.traits
        }

        val updatedCharacter = character.copy(
            level = currentLevel,
            experience = currentExp,
            maxExperience = maxExp,
            unspentSkillPoints = skillPoints,
            stats = baseStats,
            health = currentHp,
            maxHealth = newMaxHp,
            traits = updatedTraits
        )

        return LevelUpOutcome(
            updatedCharacter = updatedCharacter,
            leveledUp = leveledUp,
            oldLevel = initialLevel,
            newLevel = currentLevel,
            gainedSkillPoints = if (leveledUp) (currentLevel - initialLevel) * 2 else 0,
            newTraitUnlocked = newTrait
        )
    }

    /**
     * Allocates a skill point to a chosen attribute.
     */
    fun allocateSkillPoint(character: Character, statType: CharacterStatType): Character {
        if (character.unspentSkillPoints <= 0) return character

        val currentStats = character.stats
        val updatedStats = when (statType) {
            CharacterStatType.ATTACK -> currentStats.copy(attack = currentStats.attack + 2)
            CharacterStatType.DEFENSE -> currentStats.copy(defense = currentStats.defense + 2)
            CharacterStatType.SCAVENGING -> currentStats.copy(scavengingSkill = currentStats.scavengingSkill + 3)
            CharacterStatType.ENGINEERING -> currentStats.copy(engineeringSkill = currentStats.engineeringSkill + 3)
            CharacterStatType.MEDICAL -> currentStats.copy(medicalSkill = currentStats.medicalSkill + 3)
            CharacterStatType.MAX_HEALTH -> currentStats // handled via maxHealth
        }

        val updatedMaxHealth = if (statType == CharacterStatType.MAX_HEALTH) {
            character.maxHealth + 15
        } else {
            character.maxHealth
        }

        val updatedCurrentHealth = if (statType == CharacterStatType.MAX_HEALTH) {
            character.health + 15
        } else {
            character.health
        }

        return character.copy(
            unspentSkillPoints = character.unspentSkillPoints - 1,
            stats = updatedStats,
            maxHealth = updatedMaxHealth,
            health = updatedCurrentHealth
        )
    }

    /**
     * Records an expedition completion, grants XP, and updates stats.
     */
    fun recordExpeditionCompletion(
        character: Character,
        xpReward: Int = 45,
        damageTaken: Int = 0,
        threatNeutralized: Boolean = false
    ): Character {
        val damagedHp = (character.health - damageTaken).coerceAtLeast(1)
        val withHp = character.copy(
            health = damagedHp,
            expeditionsCount = character.expeditionsCount + 1,
            threatsNeutralizedCount = if (threatNeutralized) character.threatsNeutralizedCount + 1 else character.threatsNeutralizedCount,
            energy = (character.energy - 25).coerceAtLeast(10),
            morale = (character.morale + 5).coerceAtMost(100)
        )
        return addExperience(withHp, xpReward).updatedCharacter
    }

    /**
     * Restores energy and natural health recovery in settlement.
     */
    fun restInSettlement(character: Character, hasHospitalBonus: Boolean = false): Character {
        val regenAmount = if (hasHospitalBonus) 20 else 10
        val maxHp = character.effectiveMaxHealth
        val newHp = (character.health + regenAmount).coerceAtMost(maxHp)
        val newEnergy = (character.energy + 30).coerceAtMost(100)
        val newMorale = (character.morale + 5).coerceAtMost(100)

        return character.copy(
            health = newHp,
            energy = newEnergy,
            morale = newMorale,
            daysInSettlement = character.daysInSettlement + 1
        )
    }
}
