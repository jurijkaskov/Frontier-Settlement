package com.example

import com.example.domain.model.BuildingCategory
import com.example.domain.model.BuildingStatus
import com.example.domain.model.BuildingType
import com.example.domain.model.ResourceType
import com.example.domain.model.SettlementTier
import com.example.viewmodel.GameViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SettlementDevelopmentSystemTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        viewModel = GameViewModel()
    }

    @Test
    fun `initial settlement state has valid level tier and initial buildings`() {
        val settlement = viewModel.gameState.value.settlement
        assertEquals(1, settlement.level)
        assertTrue(settlement.xp >= 0)
        assertEquals(SettlementTier.SURVIVOR_CAMP, settlement.tier)
        assertTrue(settlement.buildings.isNotEmpty())
        assertTrue(settlement.constructedBuildingsCount > 0)
    }

    @Test
    fun `building construction fails when settlement level is too low`() {
        // Find a high tier locked building (e.g. Armory Lab requires level 3)
        val lockedBuilding = viewModel.gameState.value.settlement.buildings.find {
            it.type == BuildingType.ARMORY_LAB
        }
        assertNotNull(lockedBuilding)
        assertTrue(lockedBuilding!!.requiredSettlementLevel > viewModel.gameState.value.settlement.level)

        // Give plenty of resources
        viewModel.debugModifyResource(ResourceType.MATERIALS, 5000)
        viewModel.debugModifyResource(ResourceType.MONEY, 5000)

        viewModel.buildBuilding(lockedBuilding.id)

        val updatedBuilding = viewModel.gameState.value.settlement.buildings.find { it.id == lockedBuilding.id }
        assertFalse(updatedBuilding!!.isConstructed)
    }

    @Test
    fun `building construction succeeds when available and affordable, awarding XP and recalculating stats`() {
        // Radio tower requires level 2 and is unbuilt initially
        // First raise settlement level to 2 so radio tower unlocks to AVAILABLE_TO_BUILD
        viewModel.debugLevelUpSettlement()
        val stateAfterLevelUp = viewModel.gameState.value
        assertEquals(2, stateAfterLevelUp.settlement.level)

        val radioTower = stateAfterLevelUp.settlement.buildings.find {
            it.type == BuildingType.RADIO_TOWER
        }
        assertNotNull(radioTower)
        assertFalse(radioTower!!.isConstructed)
        assertEquals(BuildingStatus.AVAILABLE_TO_BUILD, radioTower.status)

        val initialMaterials = stateAfterLevelUp.resources.materials
        val initialMoney = stateAfterLevelUp.resources.money
        val initialXp = stateAfterLevelUp.settlement.xp

        // Make sure we have enough resources
        viewModel.debugModifyResource(ResourceType.MATERIALS, radioTower.buildCostMaterials)
        viewModel.debugModifyResource(ResourceType.MONEY, radioTower.buildCostMoney)

        val currentMat = viewModel.gameState.value.resources.materials
        val currentMoney = viewModel.gameState.value.resources.money

        viewModel.buildBuilding(radioTower.id)

        val updatedState = viewModel.gameState.value
        val updatedRadio = updatedState.settlement.buildings.find { it.id == radioTower.id }!!

        assertTrue(updatedRadio.isConstructed)
        assertEquals(1, updatedRadio.level)
        assertEquals(BuildingStatus.OPERATIONAL, updatedRadio.status)

        // Verify resource deduction
        assertEquals(currentMat - radioTower.buildCostMaterials, updatedState.resources.materials)
        assertEquals(currentMoney - radioTower.buildCostMoney, updatedState.resources.money)

        // Verify XP gain
        assertEquals(initialXp + radioTower.xpRewardOnBuild, updatedState.settlement.xp)
    }

    @Test
    fun `building upgrade increases level and updates capacity and awards XP`() {
        val storageBuilding = viewModel.gameState.value.settlement.buildings.find {
            it.type == BuildingType.STORAGE_DEPOT
        }!!
        val initialStorageLevel = storageBuilding.level
        val initialCapacity = viewModel.gameState.value.resources.warehouseMaxCapacity
        val initialXp = viewModel.gameState.value.settlement.xp

        // Give sufficient materials & money
        viewModel.debugModifyResource(ResourceType.MATERIALS, 1000)
        viewModel.debugModifyResource(ResourceType.MONEY, 1000)

        val upgradeCostMat = storageBuilding.upgradeCostMaterials
        val upgradeCostMoney = storageBuilding.upgradeCostMoney
        val matBefore = viewModel.gameState.value.resources.materials

        viewModel.upgradeBuilding(storageBuilding.id)

        val updatedState = viewModel.gameState.value
        val updatedStorage = updatedState.settlement.buildings.find { it.id == storageBuilding.id }!!

        assertEquals(initialStorageLevel + 1, updatedStorage.level)
        assertEquals(matBefore - upgradeCostMat, updatedState.resources.materials)
        assertTrue(updatedState.resources.warehouseMaxCapacity > initialCapacity)
        assertEquals(initialXp + storageBuilding.xpRewardOnUpgrade, updatedState.settlement.xp)
    }

    @Test
    fun `xp accumulation triggers settlement level up and unlocks higher tier buildings`() {
        val initialLevel = viewModel.gameState.value.settlement.level
        val armory = viewModel.gameState.value.settlement.buildings.find { it.type == BuildingType.ARMORY_LAB }!!

        assertEquals(BuildingStatus.LOCKED, armory.status)

        // Add enough XP to level up settlement multiple times to level 3
        viewModel.debugAddSettlementXp(1000)

        val updatedState = viewModel.gameState.value
        assertTrue(updatedState.settlement.level >= 3)

        // Now armory should be AVAILABLE_TO_BUILD
        val updatedArmory = updatedState.settlement.buildings.find { it.id == armory.id }!!
        assertEquals(BuildingStatus.AVAILABLE_TO_BUILD, updatedArmory.status)
    }

    @Test
    fun `debugConstructAllBuildings unlocks and builds every building`() {
        viewModel.debugConstructAllBuildings()

        val state = viewModel.gameState.value
        val allConstructed = state.settlement.buildings.all { it.isConstructed && it.level >= 1 }
        assertTrue(allConstructed)
        assertTrue(state.settlement.level >= 4)
        assertEquals(SettlementTier.FRONTIER_SETTLEMENT, state.settlement.tier)
    }
}
