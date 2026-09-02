package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.model.GameSoundId
import com.example.audio.ui.LocalGameAudio
import com.example.core.settings.model.AppLanguage
import com.example.ui.components.GameCard
import com.example.ui.components.GameConfirmationDialog
import com.example.ui.components.settings.GameSettingRow
import com.example.ui.components.settings.GameSlider
import com.example.ui.components.settings.GameSwitch
import com.example.ui.components.settings.SettingsSection
import com.example.ui.theme.*
import com.example.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToSaveGame: () -> Unit = {},
    onNavigateToLoadGame: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToContentBrowser: () -> Unit = {},
    onNavigateToGeneratorDebug: () -> Unit = {},
    onNavigateToUiGallery: () -> Unit = {},
    onNavigateToVisualAssetBrowser: () -> Unit = {},
    onNavigateToAudioGallery: () -> Unit = {},
    onNavigateToDebugSave: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val audioEngine = LocalGameAudio.current

    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showFullResetDialog by remember { mutableStateOf(false) }
    var showResetHintsSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FrontierDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки приложения",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_settings_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FrontierDarkSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
        ) {
            // 1. AUDIO & SOUNDSCAPES
            item {
                SettingsSection(
                    title = "Звук и аудиоатмосфера",
                    icon = Icons.Default.VolumeUp,
                    accentColor = TechCyan,
                    subtitle = "Настройка уровней громкости фоновой музыки, окружения и эффектов",
                    testTag = "section_audio"
                ) {
                    GameSettingRow(
                        title = "Отключить все звуки (Mute)",
                        subtitle = if (uiState.isMuted) "Все аудиопотоки заглушены" else "Звук активен",
                        icon = if (uiState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        iconTint = if (uiState.isMuted) DangerCrimson else TechCyan,
                        onClick = { viewModel.setMuted(!uiState.isMuted) },
                        testTag = "setting_mute_toggle"
                    ) {
                        GameSwitch(
                            checked = uiState.isMuted,
                            onCheckedChange = { viewModel.setMuted(it) },
                            activeColor = DangerCrimson,
                            testTag = "switch_mute"
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    GameSlider(
                        label = "Общая громкость (Master)",
                        value = uiState.masterVolume,
                        onValueChange = { viewModel.setMasterVolume(it) },
                        enabled = !uiState.isMuted,
                        onValueChangeFinished = { audioEngine.playSfx(GameSoundId.UI_CLICK) },
                        accentColor = TechCyan,
                        testTag = "slider_master_volume"
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    GameSlider(
                        label = "Музыка пустоши (Music)",
                        value = uiState.musicVolume,
                        onValueChange = { viewModel.setMusicVolume(it) },
                        enabled = !uiState.isMuted,
                        onValueChangeFinished = { audioEngine.playSfx(GameSoundId.UI_CLICK) },
                        accentColor = CreditsYellow,
                        testTag = "slider_music_volume"
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    GameSlider(
                        label = "Атмосфера и эмбиент (Ambient)",
                        value = uiState.ambientVolume,
                        onValueChange = { viewModel.setAmbientVolume(it) },
                        enabled = !uiState.isMuted,
                        onValueChangeFinished = { audioEngine.playSfx(GameSoundId.UI_CLICK) },
                        accentColor = SafeEmerald,
                        testTag = "slider_ambient_volume"
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    GameSlider(
                        label = "Звуковые эффекты (SFX)",
                        value = uiState.sfxVolume,
                        onValueChange = { viewModel.setSfxVolume(it) },
                        enabled = !uiState.isMuted,
                        onValueChangeFinished = { audioEngine.playSfx(GameSoundId.LOOT_PICK) },
                        accentColor = WarningAmber,
                        testTag = "slider_sfx_volume"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { viewModel.resetAudioToDefaults() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_reset_audio")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Сбросить громкость по умолчанию", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. INTERFACE & ACCESSIBILITY
            item {
                SettingsSection(
                    title = "Интерфейс и доступность",
                    icon = Icons.Default.DisplaySettings,
                    accentColor = SafeEmerald,
                    subtitle = "Параметры анимаций, оформления и визуальных эффектов",
                    testTag = "section_interface"
                ) {
                    GameSettingRow(
                        title = "Уменьшение движения (Reduced Motion)",
                        subtitle = "Отключает тяжелые анимации переходов и вспышки эффектов",
                        icon = Icons.Default.MotionPhotosOff,
                        iconTint = SafeEmerald,
                        onClick = { viewModel.setReducedMotion(!uiState.isReducedMotion) },
                        testTag = "setting_reduced_motion"
                    ) {
                        GameSwitch(
                            checked = uiState.isReducedMotion,
                            onCheckedChange = { viewModel.setReducedMotion(it) },
                            activeColor = SafeEmerald,
                            testTag = "switch_reduced_motion"
                        )
                    }

                    GameSettingRow(
                        title = "Визуальная тема",
                        subtitle = "Темный тактический интерфейс аванпоста выживших",
                        icon = Icons.Default.Palette,
                        iconTint = WarningAmber,
                        testTag = "setting_theme_info"
                    ) {
                        Surface(
                            color = FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Wasteland Dark",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. GAMEPLAY PREFERENCES
            item {
                SettingsSection(
                    title = "Игровой процесс и подсказки",
                    icon = Icons.Default.SportsEsports,
                    accentColor = WarningAmber,
                    subtitle = "Диалоги подтверждения и обучающие подсказки",
                    testTag = "section_gameplay"
                ) {
                    GameSettingRow(
                        title = "Обучающие подсказки (Tutorial Hints)",
                        subtitle = "Показывать карточки с советами на экранах интерфейса",
                        icon = Icons.Default.Lightbulb,
                        iconTint = WarningAmber,
                        onClick = { viewModel.setShowTutorialHints(!uiState.showTutorialHints) },
                        testTag = "setting_hints_toggle"
                    ) {
                        GameSwitch(
                            checked = uiState.showTutorialHints,
                            onCheckedChange = { viewModel.setShowTutorialHints(it) },
                            activeColor = WarningAmber,
                            testTag = "switch_hints"
                        )
                    }

                    GameSettingRow(
                        title = "Подтверждение завершения дня",
                        subtitle = "Запрашивать подтверждение перед переходом на следующий день",
                        icon = Icons.Default.HourglassBottom,
                        iconTint = TechCyan,
                        onClick = { viewModel.setConfirmDayEnd(!uiState.confirmDayEnd) },
                        testTag = "setting_confirm_day"
                    ) {
                        GameSwitch(
                            checked = uiState.confirmDayEnd,
                            onCheckedChange = { viewModel.setConfirmDayEnd(it) },
                            activeColor = TechCyan,
                            testTag = "switch_confirm_day"
                        )
                    }

                    GameSettingRow(
                        title = "Защита от опасных действий",
                        subtitle = "Подтверждать сброс предметов, отставку выживших и отступление",
                        icon = Icons.Default.Shield,
                        iconTint = DangerCrimson,
                        onClick = { viewModel.setConfirmDangerActions(!uiState.confirmDangerActions) },
                        testTag = "setting_confirm_danger"
                    ) {
                        GameSwitch(
                            checked = uiState.confirmDangerActions,
                            onCheckedChange = { viewModel.setConfirmDangerActions(it) },
                            activeColor = DangerCrimson,
                            testTag = "switch_confirm_danger"
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.resetTutorialHints()
                            showResetHintsSnackbar = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_reset_hints")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Сбросить историю прочитанных подсказок", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (showResetHintsSnackbar) {
                        Text(
                            text = "✓ История подсказок сброшена. Подсказки снова активны.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SafeEmerald, fontSize = 11.sp),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // 4. LANGUAGE
            item {
                SettingsSection(
                    title = "Язык интерфейса",
                    icon = Icons.Default.Language,
                    accentColor = TechCyan,
                    subtitle = "Выбор языка локализации элементов игры",
                    testTag = "section_language"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = uiState.language == lang
                            FilledTonalButton(
                                onClick = { viewModel.setLanguage(lang) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) TechCyan else FrontierDarkSurfaceHighlight,
                                    contentColor = if (isSelected) FrontierOnPrimary else TextWhite
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_lang_${lang.code}")
                            ) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.SYSTEM -> "Системный"
                                        AppLanguage.RUSSIAN -> "Русский"
                                        AppLanguage.ENGLISH -> "English"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // 5. SAVES MANAGEMENT SHORTCUT
            item {
                SettingsSection(
                    title = "Сохранение и данные сессии",
                    icon = Icons.Default.Save,
                    accentColor = StoragePurple,
                    subtitle = "Управление слотами сохранения и автосохранениями",
                    testTag = "section_saves"
                ) {
                    val meta = uiState.latestAutosaveMetadata
                    if (meta != null) {
                        Surface(
                            color = FrontierDarkSurfaceHighlight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Последнее сохранение: ${meta.displayName}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "День ${meta.gameDay} • Уровень ${meta.settlementLevel} • База: ${meta.settlementName}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TechCyan,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveNow() },
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_save_settings")
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Сохранить сейчас", color = FrontierOnPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onNavigateToSaveGame,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263345)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_manage_saves_settings")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TechCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Менеджер", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 6. HELP & ABOUT NAVIGATION
            item {
                SettingsSection(
                    title = "Справка и информация",
                    icon = Icons.Default.Info,
                    accentColor = TechCyan,
                    subtitle = "Справочник выживания в пустошах и сведения об игре",
                    testTag = "section_help_about"
                ) {
                    GameCard(
                        onClick = onNavigateToHelp,
                        backgroundColor = FrontierDarkSurfaceHighlight,
                        borderColor = TechCyan.copy(alpha = 0.5f),
                        modifier = Modifier.testTag("card_open_help")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = TechCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Справочник выживания (Руководства)", style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite, fontWeight = FontWeight.Bold))
                                    Text("База, экономика, экспедиции, бой и крафт", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp))
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    GameCard(
                        onClick = onNavigateToAbout,
                        backgroundColor = FrontierDarkSurfaceHighlight,
                        borderColor = FrontierBorder,
                        modifier = Modifier.testTag("card_open_about")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = CreditsYellow, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Об игре и разработчиках", style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite, fontWeight = FontWeight.Bold))
                                    Text("Версия ${uiState.appVersionName} (${uiState.appVersionCode}) • Лицензии", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp))
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }
                }
            }

            // 7. DATA MANAGEMENT & RESETS
            item {
                SettingsSection(
                    title = "Управление данными",
                    icon = Icons.Default.Storage,
                    accentColor = DangerCrimson,
                    subtitle = "Сброс параметров приложения и очистка локальных данных",
                    testTag = "section_data_management"
                ) {
                    OutlinedButton(
                        onClick = { showResetSettingsDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_reset_settings")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Сбросить настройки к начальным", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = { showFullResetDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCrimson),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_full_reset")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Полный сброс всех данных (Стереть сохранения)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // 8. DEVELOPER TOOLS (Debug Only)
            if (uiState.isDebugBuild && uiState.enableDevToolsInMenu) {
                item {
                    SettingsSection(
                        title = "Инструменты разработчика (Debug Tools)",
                        icon = Icons.Default.BugReport,
                        accentColor = TechCyan,
                        subtitle = "Отладочные галереи, тестовые каталоги и инструменты симуляции",
                        testTag = "section_dev_tools"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToContentBrowser,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2638)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_dev_content_browser")
                            ) {
                                Text("Каталог данных", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onNavigateToGeneratorDebug,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E261E)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_dev_generator_debug")
                            ) {
                                Text("Генераторы мира", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToUiGallery,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132E27)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_dev_ui_gallery")
                            ) {
                                Text("UI Gallery (31)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onNavigateToVisualAssetBrowser,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1F38)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_dev_visual_assets")
                            ) {
                                Text("Ассеты (32)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToAudioGallery,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2B3A)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_dev_audio_gallery")
                            ) {
                                Text("Аудиогалерея (37)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onNavigateToDebugSave,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332020)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_dev_debug_save")
                            ) {
                                Text("Тест сбоев (34)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog: Reset App Settings
    if (showResetSettingsDialog) {
        GameConfirmationDialog(
            title = "Сброс настроек",
            message = "Вернуть все параметры звука, интерфейса и подсказок к значениям по умолчанию? Ваши файлы сохранений не будут затронуты.",
            confirmText = "Сбросить настройки",
            cancelText = "Отмена",
            isDanger = false,
            onConfirm = {
                viewModel.resetSettingsToDefaults()
                showResetSettingsDialog = false
            },
            onDismiss = { showResetSettingsDialog = false }
        )
    }

    // Dangerous Multi-Step Confirmation Dialog: Full Data Reset
    if (showFullResetDialog) {
        GameConfirmationDialog(
            title = "ВНИМАНИЕ: Полный сброс всех данных",
            message = "Это действие БЕЗВОЗВРАТНО удалит все ваши сохранения, прогресс поселения, персонажей и сбросит настройки приложения. Вы уверены?",
            confirmText = "Стереть всё и сбросить",
            cancelText = "Отмена",
            isDanger = true,
            onConfirm = {
                viewModel.fullResetAllData {
                    showFullResetDialog = false
                    onBack()
                }
            },
            onDismiss = { showFullResetDialog = false }
        )
    }
}
