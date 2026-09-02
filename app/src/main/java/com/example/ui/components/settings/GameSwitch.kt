package com.example.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Tactical styled switch matching the Frontier Settlement wasteland theme.
 */
@Composable
fun GameSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = SafeEmerald,
    testTag: String = "game_switch"
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = TextWhite,
            checkedTrackColor = activeColor,
            checkedBorderColor = activeColor,
            uncheckedThumbColor = TextMuted,
            uncheckedTrackColor = FrontierDarkSurfaceHighlight,
            uncheckedBorderColor = FrontierBorder,
            disabledCheckedThumbColor = TextMuted.copy(alpha = 0.5f),
            disabledCheckedTrackColor = activeColor.copy(alpha = 0.3f),
            disabledUncheckedThumbColor = TextMuted.copy(alpha = 0.3f),
            disabledUncheckedTrackColor = FrontierDarkSurface
        ),
        modifier = modifier.testTag(testTag)
    )
}
