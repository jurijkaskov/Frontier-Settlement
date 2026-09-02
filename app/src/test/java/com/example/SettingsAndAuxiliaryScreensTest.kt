package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.audio.manager.AudioSettingsRepository
import com.example.core.settings.model.AppLanguage
import com.example.data.repository.AppSettingsRepository
import com.example.domain.model.help.HelpCategory
import com.example.domain.service.help.HelpCatalog
import com.example.viewmodel.SettingsViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsAndAuxiliaryScreensTest {

    private lateinit var application: Application
    private lateinit var appSettingsRepository: AppSettingsRepository
    private lateinit var audioSettingsRepository: AudioSettingsRepository

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        appSettingsRepository = AppSettingsRepository.getInstance(application)
        appSettingsRepository.resetToDefaults()
        audioSettingsRepository = AudioSettingsRepository(application)
    }

    @Test
    fun testAppSettingsDefaultsAndIndividualUpdates() {
        val initial = appSettingsRepository.getSettings()
        assertEquals(0.85f, initial.masterVolume, 0.01f)
        assertEquals(0.70f, initial.musicVolume, 0.01f)
        assertEquals(0.75f, initial.ambientVolume, 0.01f)
        assertEquals(0.85f, initial.sfxVolume, 0.01f)
        assertFalse(initial.isMuted)
        assertFalse(initial.isReducedMotion)
        assertEquals(AppLanguage.SYSTEM, initial.language)
        assertTrue(initial.showTutorialHints)
        assertFalse(initial.confirmDayEnd)
        assertTrue(initial.confirmDangerActions)

        // Modify settings
        appSettingsRepository.setMasterVolume(0.5f)
        appSettingsRepository.setMusicVolume(0.4f)
        appSettingsRepository.setAmbientVolume(0.3f)
        appSettingsRepository.setSfxVolume(0.6f)
        appSettingsRepository.setMuted(true)
        appSettingsRepository.setReducedMotion(true)
        appSettingsRepository.setLanguage(AppLanguage.RUSSIAN)
        appSettingsRepository.setShowTutorialHints(false)
        appSettingsRepository.setConfirmDayEnd(true)
        appSettingsRepository.setConfirmDangerActions(false)

        val updated = appSettingsRepository.getSettings()
        assertEquals(0.5f, updated.masterVolume, 0.01f)
        assertEquals(0.4f, updated.musicVolume, 0.01f)
        assertEquals(0.3f, updated.ambientVolume, 0.01f)
        assertEquals(0.6f, updated.sfxVolume, 0.01f)
        assertTrue(updated.isMuted)
        assertTrue(updated.isReducedMotion)
        assertEquals(AppLanguage.RUSSIAN, updated.language)
        assertFalse(updated.showTutorialHints)
        assertTrue(updated.confirmDayEnd)
        assertFalse(updated.confirmDangerActions)
    }

    @Test
    fun testAudioSettingsRepositoryProxiesToAppSettingsRepository() {
        // Test that AudioSettingsRepository correctly delegates to AppSettingsRepository
        audioSettingsRepository.setMasterVolume(0.92f)
        audioSettingsRepository.setMusicVolume(0.62f)
        audioSettingsRepository.setAmbientVolume(0.82f)
        audioSettingsRepository.setSfxVolume(0.72f)
        audioSettingsRepository.setMuted(true)

        val appSettings = appSettingsRepository.getSettings()
        assertEquals(0.92f, appSettings.masterVolume, 0.01f)
        assertEquals(0.62f, appSettings.musicVolume, 0.01f)
        assertEquals(0.82f, appSettings.ambientVolume, 0.01f)
        assertEquals(0.72f, appSettings.sfxVolume, 0.01f)
        assertTrue(appSettings.isMuted)

        val audioModel = audioSettingsRepository.getSettings()
        assertEquals(0.92f, audioModel.masterVolume, 0.01f)
        assertTrue(audioModel.isMuted)
    }

    @Test
    fun testAudioResetAndFullReset() {
        appSettingsRepository.setMasterVolume(0.1f)
        appSettingsRepository.setMusicVolume(0.1f)
        appSettingsRepository.setReducedMotion(true)
        appSettingsRepository.setLanguage(AppLanguage.ENGLISH)

        appSettingsRepository.resetAudioToDefaults()
        val afterAudioReset = appSettingsRepository.getSettings()
        assertEquals(0.85f, afterAudioReset.masterVolume, 0.01f)
        assertEquals(0.70f, afterAudioReset.musicVolume, 0.01f)
        assertTrue(afterAudioReset.isReducedMotion)
        assertEquals(AppLanguage.ENGLISH, afterAudioReset.language)

        appSettingsRepository.resetToDefaults()
        val afterFullReset = appSettingsRepository.getSettings()
        assertFalse(afterFullReset.isReducedMotion)
        assertEquals(AppLanguage.SYSTEM, afterFullReset.language)
    }

    @Test
    fun testHelpCatalogCoversAllCategories() {
        val categories = HelpCategory.entries
        assertEquals(9, categories.size)

        for (category in categories) {
            val articles = HelpCatalog.getArticlesForCategory(category)
            assertTrue("Category ${category.name} must have at least 1 article", articles.isNotEmpty())
            for (article in articles) {
                assertFalse("Article ID should not be blank", article.id.isBlank())
                assertFalse("Article title should not be blank", article.title.isBlank())
                assertFalse("Article summary should not be blank", article.summary.isBlank())
                assertTrue("Article key points must not be empty", article.keyPoints.isNotEmpty())
            }
        }

        // Test retrieval by ID
        val settlementArticle = HelpCatalog.getArticleById("settlement_basics")
        assertNotNull(settlementArticle)
        assertEquals(HelpCategory.SETTLEMENT, settlementArticle?.category)

        val combatArticle = HelpCatalog.getArticleById("combat_tactics")
        assertNotNull(combatArticle)
        assertEquals(HelpCategory.COMBAT, combatArticle?.category)
    }

    @Test
    fun testSettingsViewModelInitializationAndState() {
        val viewModel = SettingsViewModel(application)
        val state = viewModel.uiState.value
        assertNotNull(state)
        assertEquals(0.85f, state.masterVolume, 0.01f)
        assertFalse(state.isMuted)
        assertFalse(state.isReducedMotion)
        assertEquals(AppLanguage.SYSTEM, state.language)
        assertNotNull(state.appVersionName)
    }
}
