package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

sealed class NavTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Settlement : NavTab(
        route = "settlement",
        title = "Поселение",
        selectedIcon = Icons.Filled.Castle,
        unselectedIcon = Icons.Outlined.Castle
    )

    data object Map : NavTab(
        route = "map",
        title = "Карта",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore
    )

    data object Squad : NavTab(
        route = "squad",
        title = "Отряд",
        selectedIcon = Icons.Filled.Groups,
        unselectedIcon = Icons.Outlined.Groups
    )

    data object Market : NavTab(
        route = "market",
        title = "Торговля",
        selectedIcon = Icons.Filled.Storefront,
        unselectedIcon = Icons.Outlined.Storefront
    )

    data object Menu : NavTab(
        route = "menu",
        title = "Штаб",
        selectedIcon = Icons.Filled.Assignment,
        unselectedIcon = Icons.Outlined.Assignment
    )
}

@Composable
fun GameBottomNav(
    currentRoute: String,
    onTabSelected: (String) -> Unit,
    hasActiveExpedition: Boolean = false,
    hasClaimableQuest: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        NavTab.Settlement,
        NavTab.Map,
        NavTab.Squad,
        NavTab.Market,
        NavTab.Menu
    )

    Surface(
        color = FrontierDarkSurface,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = FrontierBorder,
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
            )
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
    ) {
        NavigationBar(
            containerColor = FrontierDarkSurface,
            contentColor = TextWhite,
            windowInsets = WindowInsets.navigationBars,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEach { tab ->
                val isSelected = currentRoute == tab.route

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab.route) },
                    modifier = Modifier.testTag("nav_tab_${tab.route}"),
                    icon = {
                        BadgedBox(
                            badge = {
                                if (tab == NavTab.Map && hasActiveExpedition) {
                                    Badge(
                                        containerColor = WarningAmber,
                                        modifier = Modifier.size(8.dp)
                                    )
                                } else if (tab == NavTab.Menu && hasClaimableQuest) {
                                    Badge(
                                        containerColor = SafeEmerald,
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SafeEmerald,
                        selectedTextColor = SafeEmerald,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextSubtle,
                        indicatorColor = FrontierPrimaryContainer
                    )
                )
            }
        }
    }
}
