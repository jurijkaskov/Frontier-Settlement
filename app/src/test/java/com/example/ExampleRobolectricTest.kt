package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.GameViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Frontier Settlement", appName)
    }

    @Test
    fun `game loop advances day and updates resources`() {
        val viewModel = GameViewModel()
        val initialDay = viewModel.gameState.value.day
        val initialFood = viewModel.gameState.value.resources.food

        viewModel.nextDay()

        assertEquals(initialDay + 1, viewModel.gameState.value.day)
        assertTrue(viewModel.gameState.value.dayLogs.isNotEmpty())
    }

    @Test
    fun `squad selection toggles members up to limit`() {
        val viewModel = GameViewModel()
        val initialSquad = viewModel.gameState.value.selectedSquadIds

        viewModel.toggleSquadMember("char_3")
        assertTrue(viewModel.gameState.value.selectedSquadIds.contains("char_3"))

        viewModel.toggleSquadMember("char_3")
        assertFalse(viewModel.gameState.value.selectedSquadIds.contains("char_3"))
    }
}

