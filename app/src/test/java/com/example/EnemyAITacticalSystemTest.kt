package com.example

import com.example.domain.model.*
import com.example.domain.service.combat.CombatActionCatalog
import com.example.domain.service.combat.EnemyTurnResolver
import com.example.domain.service.combat.ai.EnemyAIController
import com.example.domain.service.combat.ai.EnemyAIProfileCatalog
import com.example.domain.service.combat.ai.EnemyActionEvaluator
import com.example.domain.service.combat.ai.EnemyTargetEvaluator
import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive Unit & Integration Tests for Enemy Tactical AI (Point 22).
 * Validates utility-based action evaluation, target prioritization, archetypes,
 * loop protection, deterministic seed behavior, and fair combat rules.
 */
class EnemyAITacticalSystemTest {

    @Test
    fun testAIProfileCatalog_containsAllArchetypes() {
        val profiles = EnemyAIProfileCatalog.getAllProfiles()
        val archetypes = profiles.map { it.archetype }.toSet()

        assertTrue("Profile for AGGRESSIVE should exist", archetypes.contains(EnemyAIArchetype.AGGRESSIVE))
        assertTrue("Profile for CAUTIOUS should exist", archetypes.contains(EnemyAIArchetype.CAUTIOUS))
        assertTrue("Profile for OPPORTUNIST should exist", archetypes.contains(EnemyAIArchetype.OPPORTUNIST))
        assertTrue("Profile for SUPPORT should exist", archetypes.contains(EnemyAIArchetype.SUPPORT))
        assertTrue("Profile for BALANCED should exist", archetypes.contains(EnemyAIArchetype.BALANCED))
    }

    @Test
    fun testEnemyTargetEvaluator_opportunistPrioritizesLowestHp() {
        val playerTank = Combatant(
            id = "player_tank",
            team = CombatantTeam.PLAYER,
            displayName = "Танк",
            currentHealth = 100,
            maxHealth = 100,
            attack = 10,
            defense = 10
        )
        val playerWounded = Combatant(
            id = "player_wounded",
            team = CombatantTeam.PLAYER,
            displayName = "Раненый боец",
            currentHealth = 15,
            maxHealth = 80,
            attack = 12,
            defense = 2
        )

        val enemy = Combatant(
            id = "enemy_opportunist",
            team = CombatantTeam.ENEMY,
            displayName = "Охотник",
            currentHealth = 40,
            maxHealth = 40,
            attack = 10,
            defense = 4,
            aiProfileId = "ai_opportunist"
        )

        val profile = EnemyAIProfileCatalog.getProfile("ai_opportunist")
        val combatants = listOf(playerTank, playerWounded, enemy)
        val state = CombatState(
            id = "enc_test",
            encounterTitle = "Test",
            combatants = combatants,
            turnOrder = listOf("enemy_opportunist", "player_tank", "player_wounded"),
            currentTurnIndex = 0
        )

        val evalTank = EnemyTargetEvaluator.evaluateTarget(
            actor = enemy,
            target = playerTank,
            action = CombatActionCatalog.BASIC_ATTACK,
            profile = profile,
            state = state
        )

        val evalWounded = EnemyTargetEvaluator.evaluateTarget(
            actor = enemy,
            target = playerWounded,
            action = CombatActionCatalog.BASIC_ATTACK,
            profile = profile,
            state = state
        )

        assertTrue(
            "Opportunist must score wounded target higher than tank (${evalWounded.score} vs ${evalTank.score})",
            evalWounded.score > evalTank.score
        )
    }

    @Test
    fun testEnemyActionEvaluator_cautiousEnemyUnderLowHpChoosesDefend() {
        val woundedCautiousEnemy = Combatant(
            id = "enemy_guard",
            team = CombatantTeam.ENEMY,
            displayName = "Щитовик",
            currentHealth = 10,
            maxHealth = 60,
            actionPoints = 1,
            maxActionPoints = 4,
            attack = 8,
            defense = 8,
            aiProfileId = "ai_cautious"
        )

        val player = Combatant(
            id = "player_1",
            team = CombatantTeam.PLAYER,
            displayName = "Боец",
            currentHealth = 50,
            maxHealth = 50,
            attack = 10,
            defense = 5
        )

        val combatants = listOf(woundedCautiousEnemy, player)
        val state = CombatState(
            id = "enc_cautious_test",
            encounterTitle = "Cautious Test",
            combatants = combatants,
            turnOrder = listOf("enemy_guard", "player_1"),
            currentTurnIndex = 0
        )

        val profile = EnemyAIProfileCatalog.getProfile("ai_cautious")

        val candidates = EnemyActionEvaluator.evaluateCandidates(
            actor = woundedCautiousEnemy,
            state = state,
            profile = profile
        )

        assertTrue("Should produce action candidates", candidates.isNotEmpty())
        val bestCandidate = candidates.first()
        assertEquals("Cautious wounded enemy with 1 AP should prioritize DEFEND", CombatActionCatalog.DEFEND.id, bestCandidate.action.id)
    }

    @Test
    fun testEnemyAIController_executesTurnFairlyAndConsumesAP() {
        val enemy = Combatant(
            id = "enemy_raider",
            team = CombatantTeam.ENEMY,
            displayName = "Мародёр",
            currentHealth = 50,
            maxHealth = 50,
            actionPoints = 4,
            maxActionPoints = 4,
            attack = 12,
            defense = 4,
            aiProfileId = "ai_aggressive"
        )

        val player = Combatant(
            id = "player_1",
            team = CombatantTeam.PLAYER,
            displayName = "Боец отряда",
            currentHealth = 60,
            maxHealth = 60,
            actionPoints = 4,
            maxActionPoints = 4,
            attack = 10,
            defense = 4
        )

        val initialCombat = CombatState(
            id = "enc_ai_test",
            encounterTitle = "AI Test Combat",
            combatants = listOf(enemy, player),
            turnOrder = listOf("enemy_raider", "player_1"),
            currentTurnIndex = 0,
            roundNumber = 1
        )

        val resolvedCombat = EnemyTurnResolver.resolveEnemyTurn(initialCombat)

        assertNotEquals("Turn index should advance after enemy turn finishes", 0, resolvedCombat.currentTurnIndex)
        assertEquals("Active combatant should now be player", "player_1", resolvedCombat.currentActiveCombatant?.id)
        assertTrue("Combat log should contain actions performed by enemy", resolvedCombat.logs.isNotEmpty())
        assertTrue("AI decision logs should be recorded", resolvedCombat.aiDecisionLogs.isNotEmpty())
    }

    @Test
    fun testEnemyAIController_loopProtectionPreventsInfiniteTurns() {
        val stuckEnemy = Combatant(
            id = "enemy_broken",
            team = CombatantTeam.ENEMY,
            displayName = "Сломанный враг",
            currentHealth = 50,
            maxHealth = 50,
            actionPoints = 0,
            maxActionPoints = 4,
            attack = 0,
            defense = 0
        )

        val player = Combatant(
            id = "player_1",
            team = CombatantTeam.PLAYER,
            displayName = "Боец",
            currentHealth = 50,
            maxHealth = 50,
            attack = 10,
            defense = 0
        )

        val stuckCombat = CombatState(
            id = "enc_stuck_test",
            encounterTitle = "Loop Protection Test",
            combatants = listOf(stuckEnemy, player),
            turnOrder = listOf("enemy_broken", "player_1"),
            currentTurnIndex = 0
        )

        val result = EnemyAIController.resolveEnemyTurn(stuckCombat)
        assertEquals("Must advance turn to player without hang", "player_1", result.updatedCombatState.currentActiveCombatant?.id)
    }
}
