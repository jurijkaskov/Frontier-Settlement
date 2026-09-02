package com.example

import com.example.data.InitialGameData
import com.example.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class GlobalMapSystemTest {

    @Test
    fun testInitialLocationsConfiguration() {
        val locations = InitialGameData.createInitialGameState().locations
        assertTrue("Map should contain multiple points of interest", locations.size >= 8)

        val playerBase = locations.find { it.isPlayerBase }
        assertNotNull("Player base must be present on the world map", playerBase)
        assertEquals("Player base should be at center (0.50, 0.50)", 0.50f, playerBase!!.coordinateX, 0.05f)
        assertEquals("Player base should be at center (0.50, 0.50)", 0.50f, playerBase.coordinateY, 0.05f)
        assertEquals(DangerLevel.SAFE, playerBase.dangerLevel)
        assertEquals(0, playerBase.distanceKm)
        assertTrue(playerBase.isUnlocked)
    }

    @Test
    fun testLocationCoordinatesValidity() {
        val locations = InitialGameData.createInitialGameState().locations
        for (loc in locations) {
            assertTrue("Location ${loc.name} X coordinate must be between 0 and 1", loc.coordinateX in 0f..1f)
            assertTrue("Location ${loc.name} Y coordinate must be between 0 and 1", loc.coordinateY in 0f..1f)
            assertTrue("Location distance must be non-negative", loc.distanceKm >= 0)
        }
    }

    @Test
    fun testLocationTypesAndStatuses() {
        val locations = InitialGameData.createInitialGameState().locations

        val bunker = locations.find { it.type == LocationType.MILITARY_BUNKER }
        assertNotNull("Military bunker should exist", bunker)
        assertFalse("Military bunker should start locked", bunker!!.isUnlocked)
        assertEquals(LocationStatus.LOCKED, bunker.status)
        assertEquals(DangerLevel.EXTREME, bunker.dangerLevel)

        val tradingPost = locations.find { it.type == LocationType.TRADING_POST }
        assertNotNull("Trading post should exist", tradingPost)
        assertTrue("Trading post should be available", tradingPost!!.isUnlocked)
        assertEquals(DangerLevel.SAFE, tradingPost.dangerLevel)

        val station = locations.find { it.type == LocationType.ABANDONED_STATION }
        assertNotNull("Station should exist", station)
        assertEquals(DangerLevel.LOW, station!!.dangerLevel)
    }

    @Test
    fun testTechUnlocksLocation() {
        val initialLocations = InitialGameData.createInitialGameState().locations
        val bunker = initialLocations.find { it.id == "loc_6" }!!
        assertFalse(bunker.isUnlocked)

        // Simulate tech unlock effect
        val unlockEffect = TechEffect.LocationUnlock(locationId = "loc_6", locationNameRu = bunker.name)
        val updatedLocations = initialLocations.map { loc ->
            if (loc.id == unlockEffect.locationId) loc.copy(isUnlocked = true, status = LocationStatus.AVAILABLE) else loc
        }

        val unlockedBunker = updatedLocations.find { it.id == "loc_6" }!!
        assertTrue("Bunker should be unlocked after research", unlockedBunker.isUnlocked)
        assertEquals(LocationStatus.AVAILABLE, unlockedBunker.status)
    }

    @Test
    fun testLocationFilters() {
        val locations = InitialGameData.createInitialGameState().locations

        val safeLocations = locations.filter { it.isPlayerBase || it.dangerLevel == DangerLevel.SAFE }
        assertTrue("Safe locations should include Base and Trading Post", safeLocations.size >= 2)

        val resourceLocations = locations.filter {
            it.type in listOf(
                LocationType.FARM,
                LocationType.FOREST,
                LocationType.WAREHOUSE_COMPLEX,
                LocationType.ABANDONED_STATION,
                LocationType.VILLAGE
            )
        }
        assertTrue("Resource locations should be populated", resourceLocations.size >= 4)

        val dangerousLocations = locations.filter {
            it.dangerLevel in listOf(DangerLevel.HIGH, DangerLevel.EXTREME, DangerLevel.UNKNOWN)
        }
        assertTrue("Dangerous locations should exist", dangerousLocations.isNotEmpty())
    }
}
