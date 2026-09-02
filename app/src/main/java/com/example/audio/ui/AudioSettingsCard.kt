package com.example.audio.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.model.AudioSettings
import com.example.audio.model.GameSoundId
import com.example.audio.player.GameAudioEngine
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun AudioSettingsCard(
    audioEngine: GameAudioEngine,
    modifier: Modifier = Modifier
) {
    val settings by audioEngine.audioSettings.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, FrontierBorderLight, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = FrontierDarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Mute Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (settings.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Audio",
                        tint = if (settings.isMuted) DangerCrimson else TechCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "ЗВУК И АТМОСФЕРА",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (settings.isMuted) "Звук полностью отключен" else "Акустический профиль активен",
                            color = if (settings.isMuted) DangerCrimson else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (settings.isMuted) "ВЫКЛ" else "ВКЛ",
                        color = if (settings.isMuted) TextMuted else TechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = !settings.isMuted,
                        onCheckedChange = { active ->
                            val newMuted = !active
                            audioEngine.setMuted(newMuted)
                            audioEngine.playSfx(GameSoundId.UI_TOGGLE)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TechCyan,
                            checkedTrackColor = FrontierDarkSurfaceHighlight,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = FrontierDarkSurface
                        )
                    )
                }
            }

            HorizontalDivider(color = FrontierBorder)

            // Sliders list
            VolumeSliderRow(
                title = "Общая громкость (Master)",
                icon = Icons.Default.Tune,
                value = settings.masterVolume,
                enabled = !settings.isMuted,
                accentColor = TechCyan,
                onValueChange = { newVol ->
                    audioEngine.updateSettings(settings.copy(masterVolume = newVol))
                },
                onValueChangeFinished = {
                    audioEngine.playSfx(GameSoundId.UI_CLICK)
                }
            )

            VolumeSliderRow(
                title = "Музыка (Music)",
                icon = Icons.Default.MusicNote,
                value = settings.musicVolume,
                enabled = !settings.isMuted,
                accentColor = StoragePurple,
                onValueChange = { newVol ->
                    audioEngine.updateSettings(settings.copy(musicVolume = newVol))
                }
            )

            VolumeSliderRow(
                title = "Окружение (Ambient)",
                icon = Icons.Default.Air,
                value = settings.ambientVolume,
                enabled = !settings.isMuted,
                accentColor = FoodGreen,
                onValueChange = { newVol ->
                    audioEngine.updateSettings(settings.copy(ambientVolume = newVol))
                }
            )

            VolumeSliderRow(
                title = "Эффекты и интерфейс (SFX & UI)",
                icon = Icons.Default.GraphicEq,
                value = settings.sfxVolume,
                enabled = !settings.isMuted,
                accentColor = WarningAmber,
                onValueChange = { newVol ->
                    audioEngine.updateSettings(settings.copy(sfxVolume = newVol))
                },
                onValueChangeFinished = {
                    audioEngine.playSfx(GameSoundId.UI_CONFIRM)
                }
            )
        }
    }
}

@Composable
private fun VolumeSliderRow(
    title: String,
    icon: ImageVector,
    value: Float,
    enabled: Boolean,
    accentColor: Color,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val percent = (value * 100f).roundToInt()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) accentColor else TextSubtle,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    color = if (enabled) TextWhite else TextSubtle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = if (enabled) "$percent%" else "—",
                color = if (enabled) accentColor else TextSubtle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            enabled = enabled,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = FrontierBorder,
                disabledThumbColor = TextSubtle,
                disabledActiveTrackColor = TextSubtle,
                disabledInactiveTrackColor = FrontierDarkSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
    }
}
