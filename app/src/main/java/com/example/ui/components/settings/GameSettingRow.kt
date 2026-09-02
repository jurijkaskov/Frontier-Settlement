package com.example.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Clean setting item row with leading icon, labels, and trailing control component.
 */
@Composable
fun GameSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = TechCyan,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    testTag: String = "setting_row",
    trailingContent: @Composable () -> Unit
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .then(
            if (onClick != null && enabled) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )
        .padding(vertical = 6.dp, horizontal = 4.dp)
        .testTag(testTag)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (enabled) FrontierDarkSurfaceHighlight else FrontierDarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) iconTint else TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (enabled) TextWhite else TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                )

                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(contentAlignment = Alignment.CenterEnd) {
            trailingContent()
        }
    }
}
