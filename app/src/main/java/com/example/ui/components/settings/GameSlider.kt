package com.example.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.roundToInt

/**
 * Tactical post-apocalyptic custom slider with percentage readout and disabled state support.
 */
@Composable
fun GameSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    accentColor: Color = TechCyan,
    testTag: String = "game_slider"
) {
    val percentage = (value.coerceIn(valueRange.start, valueRange.endInclusive) * 100f).roundToInt()
    val effectiveColor = if (enabled) accentColor else TextMuted.copy(alpha = 0.5f)

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (enabled) TextWhite else TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )

                Surface(
                    color = if (enabled) FrontierDarkSurfaceHighlight else FrontierDarkSurface,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (enabled) effectiveColor.copy(alpha = 0.4f) else FrontierBorder
                    )
                ) {
                    Text(
                        text = if (enabled) "$percentage%" else "ВЫКЛ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (enabled) effectiveColor else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            colors = SliderDefaults.colors(
                thumbColor = effectiveColor,
                activeTrackColor = effectiveColor,
                inactiveTrackColor = FrontierDarkSurfaceHighlight,
                disabledThumbColor = TextMuted.copy(alpha = 0.4f),
                disabledActiveTrackColor = TextMuted.copy(alpha = 0.3f),
                disabledInactiveTrackColor = FrontierDarkSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}
