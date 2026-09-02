package com.example

import com.example.domain.model.*
import com.example.domain.service.combat.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TurnBasedCombatMechanicsTest {

    private lateinit var samplePlayer: Combatant
    private lateinit var sampleMedic: Combatant
    private lateinit var sampleEnemy: Combatant
    private lateinit var baseCombatState: CombatState

    @Before
    fun setup() {
        samplePlayer = Combatant(
            id = "c_soldier_1",
            team = CombatantTeam.PLAYER,
            displayName = "Штурмовик Марк",
            characterId = "char_1",
            currentHealth = 80,
            maxHealth = 80,
            actionPoints = 4,
            maxActionPoints = 4,
            initiative = 15,
            attack = 18,
            defense = 6,
            role = CharacterRole.SOLDIER
        )

        sampleMedic = Combatant(
            id = "c_medic_1",
            team = CombatantTeam.PLAYER,
            displayName = "Медик Анна",
            characterId = "char_2",
            currentHealth = 60,
            maxHealth = 60,
            actionPoints = 4,
            maxActionPoints = 4,
            initiative = 12,
            attack = 10,
            defense = 4,
            role = CharacterRole.MEDIC
        )

        sampleEnemy = Combatant(
            id = "c_raider_1",
            team = CombatantTeam.ENEMY,
            displayName = "Рейдер-громила",
            currentHealth = 50,
            maxHealth = 50,
            actionPoints = 4,
            maxActionPoints = 4,
            initiative = 10,
            attack = 14,
            defense = 4
        )

        baseCombatState = CombatState(
            id = "combat_test_1",
            encounterTitle = "Засада рейдеров",
            combatants = listOf(samplePlayer, sampleMedic, sampleEnemy),
            turnOrder = listOf(samplePlayer.id, sampleMedic.id, sampleEnemy.id),
            currentTurnIndex = 0,
            roundNumber = 1,
            selectedTargetId = sampleEnemy.id,
            currentPhase = CombatPhase.PLAYER_TURN,
            xpReward = 150,
            bonusLoot = GameResources(money = 50, materials = 25)
        )
    }

    @Test
    fun testBasicAttackConsumesAPAndDealsDamage() {
        val exec = CombatActionExecutor.executeAction(
            state = baseCombatState,
            action = CombatActionCatalog.BASIC_ATTACK,
            targetId = sampleEnemy.id
        )

        assertTrue("Attack should succeed", exec.actionResult.success)
        assertEquals(2, exec.actionResult.apSpent)

        val updatedState = exec.updatedCombatState
        val updatedPlayer = updatedState.combatants.first { it.id == samplePlayer.id }
        val updatedEnemy = updatedState.combatants.first { it.id == sampleEnemy.id }

        assertEquals("Player should have 2 AP remaining (4 - 2)", 2, updatedPlayer.actionPoints)
        assertTrue("Enemy should have taken damage", updatedEnemy.currentHealth < sampleEnemy.maxHealth)
    }

    @Test
    fun testInsufficientAPPreventsActionExecution() {
        val lowApPlayer = samplePlayer.copy(actionPoints = 1)
        val stateWithLowAp = baseCombatState.copy(
            combatants = listOf(lowApPlayer, sampleMedic, sampleEnemy)
        )

        val exec = CombatActionExecutor.executeAction(
            state = stateWithLowAp,
            action = CombatActionCatalog.BASIC_ATTACK,
            targetId = sampleEnemy.id
        )

        assertFalse("Action should fail due to insufficient AP", exec.actionResult.success)
        assertTrue(exec.actionResult.errorMessage?.contains("Недостаточно ОД") == true)
    }

    @Test
    fun testDefensiveStanceAddsBonusDefense() {
        val exec = CombatActionExecutor.executeAction(
            state = baseCombatState,
            action = CombatActionCatalog.DEFEND,
            targetId = null
        )

        assertTrue(exec.actionResult.success)
        assertEquals(1, exec.actionResult.apSpent)

        val updatedState = exec.updatedCombatState
        val updatedPlayer = updatedState.combatants.first { it.id == samplePlayer.id }

        assertEquals(CombatantStatus.DEFENDING, updatedPlayer.status)
        assertEquals(3, updatedPlayer.actionPoints)
        assertTrue("Effective defense should increase by +6", updatedPlayer.effectiveDefense >= samplePlayer.defense + 6)
    }

    @Test
    fun testRoleSkillCooldownApplication() {
        val soldierSkill = CombatActionCatalog.SOLDIER_SNIPE
        val exec = CombatActionExecutor.executeAction(
            state = baseCombatState,
            action = soldierSkill,
            targetId = sampleEnemy.id
        )

        assertTrue(exec.actionResult.success)
        val updatedState = exec.updatedCombatState
        val updatedPlayer = updatedState.combatants.first { it.id == samplePlayer.id }

        assertTrue("Ability should be on cooldown", updatedPlayer.isAbilityOnCooldown(soldierSkill.id))
        assertEquals(2, updatedPlayer.getAbilityRemainingCooldown(soldierSkill.id))

        // Attempting to use again should fail due to cooldown
        val secondExec = CombatActionExecutor.executeAction(
            state = updatedState,
            action = soldierSkill,
            targetId = sampleEnemy.id
        )
        assertFalse("Should not be able to execute skill on cooldown", secondExec.actionResult.success)
    }

    @Test
    fun testTurnAdvancementRestoresAPAndTicksCooldowns() {
        // Player spends 2 AP and uses skill
        val soldierSkill = CombatActionCatalog.SOLDIER_SNIPE
        val exec = CombatActionExecutor.executeAction(
            state = baseCombatState,
            action = soldierSkill,
            targetId = sampleEnemy.id
        )
        var state = exec.updatedCombatState

        // Pass player turn -> advances to Medic
        state = CombatTurnManager.advanceTurn(state)
        assertEquals(1, state.currentTurnIndex)
        assertEquals(sampleMedic.id, state.activeCombatantId)

        // Pass Medic turn -> advances to Enemy
        state = CombatTurnManager.advanceTurn(state)
        assertEquals(2, state.currentTurnIndex)
        assertEquals(sampleEnemy.id, state.activeCombatantId)

        // Pass Enemy turn -> advances to Round 2, Player's turn again
        state = CombatTurnManager.advanceTurn(state)
        assertEquals(2, state.roundNumber)
        assertEquals(0, state.currentTurnIndex)
        assertEquals(samplePlayer.id, state.activeCombatantId)

        val playerInRound2 = state.combatants.first { it.id == samplePlayer.id }
        assertEquals("Player should have fully restored AP in new turn", playerInRound2.maxActionPoints, playerInRound2.actionPoints)
        assertEquals("Cooldown should have decremented from 2 to 1", 1, playerInRound2.getAbilityRemainingCooldown(soldierSkill.id))
    }

    @Test
    fun testVictoryEvaluationWhenAllEnemiesDefeated() {
        val lowHpEnemy = sampleEnemy.copy(currentHealth = 5)
        val state = baseCombatState.copy(
            combatants = listOf(samplePlayer, sampleMedic, lowHpEnemy)
        )

        // Execute killing blow
        val exec = CombatActionExecutor.executeAction(
            state = state,
            action = CombatActionCatalog.BASIC_ATTACK,
            targetId = lowHpEnemy.id
        )

        val postActionState = exec.updatedCombatState
        val finalState = CombatTurnManager.evaluateBattleOutcome(postActionState)

        assertTrue("Combat should be marked as victory", finalState.isVictory)
        assertEquals(CombatPhase.VICTORY, finalState.currentPhase)
        assertNotNull("Battle result must be populated", finalState.battleResult)
        assertEquals(150, finalState.battleResult?.xpEarned)
        assertEquals(50, finalState.battleResult?.bonusLoot?.money)
    }

    @Test
    fun testTargetValidation() {
        val enemyTargets = CombatTargetValidator.getValidTargets(
            action = CombatActionCatalog.BASIC_ATTACK,
            actor = samplePlayer,
            allCombatants = baseCombatState.combatants
        )
        assertEquals(1, enemyTargets.size)
        assertEquals(sampleEnemy.id, enemyTargets.first().id)

        val allyHealTargets = CombatTargetValidator.getValidTargets(
            action = CombatActionCatalog.MEDIC_HEAL,
            actor = sampleMedic,
            allCombatants = baseCombatState.combatants
        )
        assertEquals(2, allyHealTargets.size) // Both samplePlayer and sampleMedic
    }
}
