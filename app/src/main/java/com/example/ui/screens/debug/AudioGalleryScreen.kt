package com.example.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.model.*
import com.example.audio.registry.GameAudioAssetValidator
import com.example.audio.registry.GameAudioRegistry
import com.example.audio.ui.AudioSettingsCard
import com.example.audio.ui.LocalGameAudio
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioGalleryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audioEngine = LocalGameAudio.current
    val context = LocalContext.current

    val currentContext by audioEngine.currentContext.collectAsState()
    val currentMusic by audioEngine.currentMusicId.collectAsState()
    val currentAmbient by audioEngine.currentAmbientId.collectAsState()
    val settings by audioEngine.audioSettings.collectAsState()
    val audioFocusState by audioEngine.audioFocusState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val validationReport = remember(context) { GameAudioAssetValidator.validate(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Аудиогалерея и атмосфера",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Пункт 37 — Звуковая архитектура & тестер",
                            fontSize = 12.sp,
                            color = TechCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (audioFocusState == "FOCUSED") SafeEmerald.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f))
                            .border(1.dp, if (audioFocusState == "FOCUSED") SafeEmerald else WarningAmber, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "FOCUS: $audioFocusState",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (audioFocusState == "FOCUSED") SafeEmerald else WarningAmber
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FrontierDarkSurface)
            )
        },
        containerColor = FrontierDarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Live Status Card
            item {
                LiveAudioStatusCard(
                    currentContext = currentContext,
                    currentMusic = currentMusic,
                    currentAmbient = currentAmbient,
                    settings = settings,
                    onStopMusic = { audioEngine.stopMusic() },
                    onStopAmbient = { audioEngine.stopAmbient() }
                )
            }

            // Quick Context Presets
            item {
                ContextPresetsCard(
                    activeContext = currentContext,
                    onSelectContext = { ctx ->
                        audioEngine.setContext(ctx)
                        audioEngine.playSfx(GameSoundId.UI_TAB)
                    }
                )
            }

            // Audio Settings Card
            item {
                AudioSettingsCard(audioEngine = audioEngine)
            }

            // Navigation Tabs (SFX Gallery vs Asset Validator)
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = FrontierDarkSurfaceElevated,
                    contentColor = TechCyan,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            audioEngine.playSfx(GameSoundId.UI_TAB)
                        },
                        text = { Text("Звуковые эффекты (SFX)", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            audioEngine.playSfx(GameSoundId.UI_TAB)
                        },
                        text = { Text("Валидатор ассетов", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // SFX Category Groups
                item {
                    SfxCategorySection(
                        title = "1. Пользовательский интерфейс (UI)",
                        sounds = listOf(
                            GameSoundId.UI_CLICK,
                            GameSoundId.UI_CONFIRM,
                            GameSoundId.UI_CANCEL,
                            GameSoundId.UI_TAB,
                            GameSoundId.UI_TOGGLE,
                            GameSoundId.UI_ERROR,
                            GameSoundId.UI_WARNING
                        ),
                        onPlay = { audioEngine.playSfx(it) }
                    )
                }

                item {
                    SfxCategorySection(
                        title = "2. Поселение, крафт и прогрессия",
                        sounds = listOf(
                            GameSoundId.RESOURCE_GAIN,
                            GameSoundId.DAILY_REPORT,
                            GameSoundId.NEW_DAY,
                            GameSoundId.BUILDING_CONSTRUCT,
                            GameSoundId.BUILDING_UPGRADE,
                            GameSoundId.WORKSHOP_CRAFT,
                            GameSoundId.WORKSHOP_REPAIR,
                            GameSoundId.RESEARCH_COMPLETE
                        ),
                        onPlay = { audioEngine.playSfx(it) }
                    )
                }

                item {
                    SfxCategorySection(
                        title = "3. Торговля, квесты и дипломатия",
                        sounds = listOf(
                            GameSoundId.TRADE_BUY,
                            GameSoundId.TRADE_SELL,
                            GameSoundId.REPUTATION_INCREASE,
                            GameSoundId.QUEST_ACCEPTED,
                            GameSoundId.QUEST_OBJECTIVE_COMPLETE,
                            GameSoundId.QUEST_COMPLETED,
                            GameSoundId.QUEST_FAILED
                        ),
                        onPlay = { audioEngine.playSfx(it) }
                    )
                }

                item {
                    SfxCategorySection(
                        title = "4. Карта, переход и события",
                        sounds = listOf(
                            GameSoundId.LOCATION_DISCOVERED,
                            GameSoundId.TRAVEL_STEP,
                            GameSoundId.VEHICLE_ENGINE,
                            GameSoundId.EVENT_REVEAL,
                            GameSoundId.EVENT_POSITIVE_RESULT,
                            GameSoundId.EVENT_NEGATIVE_RESULT
                        ),
                        onPlay = { audioEngine.playSfx(it) }
                    )
                }

                item {
                    SfxCategorySection(
                        title = "5. Добыча и тайники (Loot)",
                        sounds = listOf(
                            GameSoundId.LOOT_REVEAL,
                            GameSoundId.LOOT_PICK,
                            GameSoundId.LOOT_RARE,
                            GameSoundId.LOOT_TAKE_ALL
                        ),
                        onPlay = { audioEngine.playSfx(it) }
                    )
                }

                item {
                    SfxCategorySection(
                        title = "6. Тактический бой (Combat SFX)",
                        sounds = listOf(
                            GameSoundId.COMBAT_ATTACK_MELEE,
                            GameSoundId.COMBAT_SHOT_PISTOL,
                            GameSoundId.COMBAT_SHOT_RIFLE,
                            GameSoundId.COMBAT_SHOT_SHOTGUN,
                            GameSoundId.COMBAT_SHOT_HEAVY,
                            GameSoundId.COMBAT_HIT,
                            GameSoundId.COMBAT_MISS,
                            GameSoundId.COMBAT_ARMOR_BLOCK,
                            GameSoundId.COMBAT_HEAL,
                            GameSoundId.COMBAT_BUFF,
                            GameSoundId.COMBAT_DEBUFF,
                            GameSoundId.COMBAT_STATUS_EXPIRE,
                            GameSoundId.COMBAT_TURN_PLAYER,
                            GameSoundId.COMBAT_TURN_ENEMY,
                            GameSoundId.COMBAT_VICTORY_STING,
                            GameSoundId.COMBAT_DEFEAT_STING
                        ),
                        onPlay = { audioEngine.playSfx(it) }
                    )
                }
            } else {
                // Asset Validator Tab
                item {
                    AssetValidatorSummaryCard(report = validationReport)
                }

                items(validationReport.items) { item ->
                    AssetValidationRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun LiveAudioStatusCard(
    currentContext: GameAudioContext,
    currentMusic: GameMusicId,
    currentAmbient: GameAmbientId,
    settings: AudioSettings,
    onStopMusic: () -> Unit,
    onStopAmbient: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TechCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = FrontierDarkSurfaceHighlight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (!settings.isMuted) SafeEmerald else DangerCrimson)
                    )
                    Text(
                        text = "АКТИВНЫЙ АКУСТИЧЕСКИЙ ПРОФИЛЬ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechCyan,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = currentContext.titleRu,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            HorizontalDivider(color = FrontierBorder)

            // Music track status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.MusicNote, null, tint = StoragePurple, modifier = Modifier.size(18.dp))
                    Column {
                        Text("Музыка:", fontSize = 11.sp, color = TextMuted)
                        Text(currentMusic.titleRu, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                    }
                }
                if (currentMusic != GameMusicId.SILENT) {
                    IconButton(onClick = onStopMusic, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Stop, "Stop", tint = WarningAmber, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Ambient loop status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Air, null, tint = FoodGreen, modifier = Modifier.size(18.dp))
                    Column {
                        Text("Эмбиент:", fontSize = 11.sp, color = TextMuted)
                        Text(currentAmbient.titleRu, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                    }
                }
                if (currentAmbient != GameAmbientId.SILENT) {
                    IconButton(onClick = onStopAmbient, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Stop, "Stop", tint = WarningAmber, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextPresetsCard(
    activeContext: GameAudioContext,
    onSelectContext: (GameAudioContext) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FrontierBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = FrontierDarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "ТЕСТИРОВАНИЕ КОНТЕКСТОВ И ПЕРЕХОДОВ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TechCyan,
                letterSpacing = 0.8.sp
            )

            val contexts = listOf(
                GameAudioContext.SETTLEMENT,
                GameAudioContext.WORLD_MAP,
                GameAudioContext.TRAVEL,
                GameAudioContext.LOCATION,
                GameAudioContext.COMBAT,
                GameAudioContext.EVENT,
                GameAudioContext.LOOT,
                GameAudioContext.RETURN_SUMMARY
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                contexts.take(4).forEach { ctx ->
                    val isSelected = activeContext == ctx
                    Button(
                        onClick = { onSelectContext(ctx) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) TechCyan else FrontierDarkSurfaceHighlight,
                            contentColor = if (isSelected) FrontierDarkBackground else TextWhite
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(ctx.titleRu.take(8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                contexts.drop(4).forEach { ctx ->
                    val isSelected = activeContext == ctx
                    Button(
                        onClick = { onSelectContext(ctx) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) TechCyan else FrontierDarkSurfaceHighlight,
                            contentColor = if (isSelected) FrontierDarkBackground else TextWhite
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(ctx.titleRu.take(8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SfxCategorySection(
    title: String,
    sounds: List<GameSoundId>,
    onPlay: (GameSoundId) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FrontierBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = FrontierDarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WarningAmber
            )

            sounds.forEach { soundId ->
                val entry = remember(soundId) { GameAudioRegistry.getSoundEntry(soundId) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FrontierDarkSurface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = soundId.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${soundId.descriptionRu} (${entry.resourceName})",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    FilledTonalButton(
                        onClick = { onPlay(soundId) },
                        modifier = Modifier
                            .height(32.dp)
                            .padding(start = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Тест", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetValidatorSummaryCard(report: com.example.audio.registry.AudioValidationReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SafeEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = FrontierDarkSurfaceHighlight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ОТЧЁТ ВАЛИДАТОРА АУДИОРЕСУРСОВ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeEmerald,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (report.allSoundEnumsCovered && report.allMusicEnumsCovered) "100% ПОКРЫТИЕ ENUM" else "ТРЕБУЕТСЯ ВНИМАНИЕ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (report.allSoundEnumsCovered) SafeEmerald else WarningAmber
                )
            }

            HorizontalDivider(color = FrontierBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Всего звуков SFX: ${report.totalSounds}", fontSize = 12.sp, color = TextWhite)
                    Text("Музыкальных тем: ${report.totalMusic}", fontSize = 12.sp, color = TextWhite)
                }
                Column {
                    Text("Эмбиент профилей: ${report.totalAmbient}", fontSize = 12.sp, color = TextWhite)
                    Text("Найдено в res/raw: ${report.presentResourceCount}", fontSize = 12.sp, color = TechCyan)
                }
            }
        }
    }
}

@Composable
private fun AssetValidationRow(item: com.example.audio.registry.AudioValidationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(FrontierDarkSurfaceElevated)
            .border(1.dp, FrontierBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "[${item.category}]",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TechCyan
                )
                Text(
                    text = item.id,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "Res: ${item.resourceName} | Loop: ${item.loop} | DefVol: ${item.defaultVolume}",
                fontSize = 10.sp,
                color = TextMuted
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (item.isPresentInRaw) SafeEmerald.copy(alpha = 0.2f) else TechCyan.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = item.notes,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.isPresentInRaw) SafeEmerald else TechCyan
            )
        }
    }
}
