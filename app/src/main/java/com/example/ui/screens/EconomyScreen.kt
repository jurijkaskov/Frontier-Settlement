package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EconomyBalanceConfig
import com.example.domain.model.*
import com.example.domain.service.economy.SettlementEconomyProcessor
import com.example.ui.theme.*

/**
 * Full Tactical Settlement Economy & Accounting Screen (Экран экономики поселения).
 *
 * Implements:
 * - Real-time non-mutating Daily Economic Forecast (Production, Consumption, Treasury balance);
 * - Itemized Income vs Upkeep Expenses breakdown;
 * - Daily Economy Reports history & last tick retrospective;
 * - Deficit / Shortage alerts and warehouse overflow losses;
 * - Interactive Debug/Balancing dashboard for economy tuning.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EconomyScreen(
    gameState: GameState,
    onBack: () -> Unit,
    onNavigateToWarehouse: () -> Unit,
    onNavigateToBuildings: () -> Unit,
    onDebugAddCredits: (Int) -> Unit = {},
    onDebugDrainResource: (ResourceType) -> Unit = {},
    onDebugClearDeficits: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val forecast = remember(gameState) {
        SettlementEconomyProcessor.calculateDailyEconomyForecast(gameState)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ЭКОНОМИКА АВАНПОСТА",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "День ${gameState.day} • Финансовый и ресурсный баланс",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TechCyan,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("economy_btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    Surface(
                        color = FrontierDarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreditsYellow.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Казна",
                                tint = CreditsYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${gameState.resources.money} Кр.",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FrontierDarkSurface
                )
            )
        },
        containerColor = FrontierDarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Treasury & Net Daily Trend Header Summary Card
            TreasuryHeaderSummaryCard(
                gameState = gameState,
                forecast = forecast,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )

            // Critical Shortage Alert Banner
            if (forecast.criticalWarnings.isNotEmpty()) {
                CriticalWarningBanner(
                    warnings = forecast.criticalWarnings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // Navigation Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = FrontierDarkSurface,
                contentColor = TechCyan,
                divider = { HorizontalDivider(color = FrontierBorder) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "Прогноз",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_economy_forecast")
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "Отчёт дня",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_economy_daily_report")
                )

                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            text = "История",
                            fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_economy_history")
                )

                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = {
                        Text(
                            text = "Debug",
                            fontWeight = if (selectedTabIndex == 3) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_economy_debug")
                )
            }

            // Tab Content Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> ForecastTabContent(
                        gameState = gameState,
                        forecast = forecast,
                        onNavigateToBuildings = onNavigateToBuildings
                    )
                    1 -> LastReportTabContent(
                        report = gameState.lastEconomyReport,
                        gameState = gameState,
                        onNavigateToWarehouse = onNavigateToWarehouse
                    )
                    2 -> HistoryTabContent(
                        reports = gameState.economyReports,
                        unpaidDeficits = gameState.unpaidDeficits,
                        onClearDeficits = onDebugClearDeficits
                    )
                    3 -> DebugEconomyTabContent(
                        gameState = gameState,
                        forecast = forecast,
                        onDebugAddCredits = onDebugAddCredits,
                        onDebugDrainResource = onDebugDrainResource
                    )
                }
            }
        }
    }
}

/**
 * Top Summary Card showing Treasury Balance, Net Daily Delta, and Storage Occupancy.
 */
@Composable
private fun TreasuryHeaderSummaryCard(
    gameState: GameState,
    forecast: EconomyForecast,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Treasury & Net Trend
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "КАЗНА АВАНПОСТА",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${gameState.resources.money} Кр.",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = CreditsYellow
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val net = forecast.netMoneyForecast
                    val netColor = if (net >= 0) SafeEmerald else DangerCrimson
                    Surface(
                        color = netColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, netColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (net >= 0) "+$net / дн." else "$net / дн.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = netColor,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Population breakdown (Home vs Field)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "ПОТРЕБЛЕНИЕ ЖИТЕЛЕЙ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "${forecast.residentsInSettlement} на базе • ${forecast.residentsOnExpedition} в походе",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = "Склад: ${gameState.totalWarehouseOccupiedVolume} / ${gameState.resources.warehouseMaxCapacity}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (gameState.isWarehouseFull) DangerCrimson else TextSubtle,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

/**
 * Critical warning banner for shortages or deficits.
 */
@Composable
private fun CriticalWarningBanner(
    warnings: List<String>,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DangerCrimson.copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.7f)),
        modifier = modifier.testTag("banner_economy_critical_warning")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = DangerCrimson,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ЭКОНОМИЧЕСКАЯ УГРОЗА",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DangerCrimson,
                        letterSpacing = 0.8.sp
                    )
                )
                warnings.forEach { warning ->
                    Text(
                        text = "• $warning",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextWhite,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Tab 0: Forecast Tab Content (Full resource-by-resource projection + Treasury Breakdown).
 */
@Composable
private fun ForecastTabContent(
    gameState: GameState,
    forecast: EconomyForecast,
    onNavigateToBuildings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        // Section 1: Financial Balance Sheet
        item {
            FinancialBalanceSheetCard(forecast = forecast)
        }

        // Section 2: Resource Supply Health Projections
        item {
            Text(
                text = "БАЛАНС РЕСУРСОВ (ПРОГНОЗ НА СЛЕДУЮЩИЙ ДЕНЬ)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TechCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        items(forecast.resourceDetails.values.toList()) { detail ->
            ResourceForecastCard(detail = detail)
        }

        // Action shortcut to buildings management
        item {
            OutlinedButton(
                onClick = onNavigateToBuildings,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TechCyan
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .testTag("btn_economy_to_buildings")
            ) {
                Icon(
                    imageVector = Icons.Default.Apartment,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Управление производственными объектами базы")
            }
        }
    }
}

/**
 * Card detailing income sources vs maintenance expenses.
 */
@Composable
private fun FinancialBalanceSheetCard(forecast: EconomyForecast) {
    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ФИНАНСОВЫЙ БЮДЖЕТ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CreditsYellow,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Итог: ${if (forecast.netMoneyForecast >= 0) "+${forecast.netMoneyForecast}" else "${forecast.netMoneyForecast}"} Кр.",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (forecast.netMoneyForecast >= 0) SafeEmerald else DangerCrimson
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Incomes column
            Text(
                text = "Доходы (+${forecast.expectedIncome} Кр./день):",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SafeEmerald,
                    fontWeight = FontWeight.SemiBold
                )
            )
            forecast.incomeSources.forEach { (src, amt) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "• $src", style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp))
                    Text(text = "+$amt Кр.", style = MaterialTheme.typography.bodySmall.copy(color = SafeEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expenses column
            Text(
                text = "Расходы на содержание (-${forecast.expectedExpenses} Кр./день):",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = DangerCrimson,
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (forecast.expenseSources.isEmpty()) {
                Text(
                    text = "• Нет активных расходов",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp),
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                forecast.expenseSources.forEach { (src, amt) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• $src", style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp))
                        Text(text = "-$amt Кр.", style = MaterialTheme.typography.bodySmall.copy(color = DangerCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

/**
 * Card for individual resource forecast (Food, Water, Fuel, etc.).
 */
@Composable
private fun ResourceForecastCard(detail: ResourceForecastDetail) {
    val statusColor = when (detail.status) {
        ResourceEconomicStatus.SURPLUS -> SafeEmerald
        ResourceEconomicStatus.STABLE -> TechCyan
        ResourceEconomicStatus.DEFICIT -> WarningAmber
        ResourceEconomicStatus.CRITICAL -> DangerCrimson
    }

    val resIcon: ImageVector = when (detail.resourceType) {
        ResourceType.FOOD -> Icons.Default.Restaurant
        ResourceType.WATER -> Icons.Default.WaterDrop
        ResourceType.FUEL -> Icons.Default.LocalGasStation
        ResourceType.MATERIALS -> Icons.Default.Handyman
        ResourceType.MEDICINE -> Icons.Default.MedicalServices
        ResourceType.MONEY -> Icons.Default.MonetizationOn
        ResourceType.AMMO -> Icons.Default.Shield
        ResourceType.COMPONENTS -> Icons.Default.Build
        ResourceType.RARE_ALLOY -> Icons.Default.Diamond
    }

    Surface(
        color = FrontierDarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_economy_res_${detail.resourceType.name.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = resIcon,
                        contentDescription = detail.resourceType.titleRu,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = detail.resourceType.titleRu,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                }

                // Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = detail.status.titleRu,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Core Numbers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("В наличии", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    Text("${detail.currentStock} ед.", style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite, fontWeight = FontWeight.Bold))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Производство", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    Text("+${detail.dailyProduction}/дн.", style = MaterialTheme.typography.bodyMedium.copy(color = SafeEmerald, fontWeight = FontWeight.Bold))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Расход", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    Text("-${detail.dailyConsumption}/дн.", style = MaterialTheme.typography.bodyMedium.copy(color = if (detail.dailyConsumption > 0) DangerCrimson else TextMuted, fontWeight = FontWeight.Bold))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Запас на", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    val daysText = if (detail.daysOfSupply >= 90f) "∞" else String.format("%.1f дн.", detail.daysOfSupply)
                    Text(daysText, style = MaterialTheme.typography.bodyMedium.copy(color = statusColor, fontWeight = FontWeight.Bold))
                }
            }

            // Breakdown details
            if (detail.productionSources.isNotEmpty() || detail.consumptionSources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = FrontierBorderLight.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(6.dp))

                if (detail.productionSources.isNotEmpty()) {
                    Text(
                        text = "Источники: " + detail.productionSources.joinToString(", ") { "${it.first} (+${it.second})" },
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 10.sp)
                    )
                }
                if (detail.consumptionSources.isNotEmpty()) {
                    Text(
                        text = "Потребители: " + detail.consumptionSources.joinToString(", ") { "${it.first} (-${it.second})" },
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}

/**
 * Tab 1: Last Daily Economy Report Retrospective.
 */
@Composable
private fun LastReportTabContent(
    report: DailyEconomyReport?,
    gameState: GameState,
    onNavigateToWarehouse: () -> Unit
) {
    if (report == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Отчёт ещё не сформирован",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextMuted)
                )
                Text(
                    text = "Завершите текущий день для получения полной сводки.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        item {
            Surface(
                color = FrontierDarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ИТОГИ ЭКОНОМИЧЕСКОГО ТИКА (ДЕНЬ ${report.day})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Production summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Произведено ресурсов:", style = MaterialTheme.typography.bodySmall.copy(color = TextWhite))
                        Text("+${report.totalProducedUnits} ед.", style = MaterialTheme.typography.bodySmall.copy(color = SafeEmerald, fontWeight = FontWeight.Bold))
                    }

                    // Consumption summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Потреблено ресурсов:", style = MaterialTheme.typography.bodySmall.copy(color = TextWhite))
                        Text("-${report.totalConsumedUnits} ед.", style = MaterialTheme.typography.bodySmall.copy(color = DangerCrimson, fontWeight = FontWeight.Bold))
                    }

                    // Net money
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Финансовое сальдо:", style = MaterialTheme.typography.bodySmall.copy(color = TextWhite))
                        val net = report.netMoneyChange
                        Text(
                            text = if (net >= 0) "+$net Кр." else "$net Кр.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (net >= 0) SafeEmerald else DangerCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Warehouse state
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Заполненность склада:", style = MaterialTheme.typography.bodySmall.copy(color = TextWhite))
                        Text("${report.storageOccupancyAfter} / ${report.storageCapacity}", style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle))
                    }
                }
            }
        }

        // Overflow Lost Notice
        if (report.overflowLost.isNotEmpty()) {
            item {
                Surface(
                    color = WarningAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ИЗЛИШКИ НЕ ПОМЕСТИЛИСЬ НА СКЛАД!",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber
                                )
                            )
                            val lost = report.overflowLost.entries.joinToString(", ") { "${it.key.titleRu}: -${it.value}" }
                            Text(
                                text = "Утеряно из-за нехватки места: $lost",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }

        // Shortages list
        if (report.shortages.isNotEmpty()) {
            item {
                Text(
                    text = "ЗАРЕГИСТРИРОВАННЫЕ ДЕФИЦИТЫ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DangerCrimson,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            items(report.shortages) { def ->
                Surface(
                    color = DangerCrimson.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = def.type.titleRu,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DangerCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Источник: ${def.sourceRu} • Запрошено: ${def.requestedAmount}, доступно: ${def.availableAmount}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 10.sp)
                            )
                        }
                        Text(
                            text = "-${def.deficitAmount}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DangerCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Summary Logs
        item {
            Text(
                text = "ХРОНИКА ЭКОНОМИКИ",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TechCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        items(report.summaryLogs) { log ->
            Text(
                text = "• $log",
                style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Tab 2: Historical Reports List.
 */
@Composable
private fun HistoryTabContent(
    reports: List<DailyEconomyReport>,
    unpaidDeficits: List<EconomicDeficit>,
    onClearDeficits: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        if (unpaidDeficits.isNotEmpty()) {
            item {
                Surface(
                    color = FrontierDarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Непогашенные дефициты: ${unpaidDeficits.size}",
                                style = MaterialTheme.typography.labelMedium.copy(color = WarningAmber, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Зафиксированы в прошлых циклах",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 10.sp)
                            )
                        }
                        TextButton(onClick = onClearDeficits) {
                            Text("Очистить историю", color = TechCyan, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        if (reports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "История отчётов пуста. Проживите хотя бы один день.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                }
            }
        } else {
            items(reports) { rep ->
                Surface(
                    color = FrontierDarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrontierBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "День ${rep.day}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            )
                            val net = rep.netMoneyChange
                            Text(
                                text = if (net >= 0) "+$net Кр." else "$net Кр.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (net >= 0) SafeEmerald else DangerCrimson
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Произведено: +${rep.totalProducedUnits} ед. • Потреблено: -${rep.totalConsumedUnits} ед.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSubtle, fontSize = 11.sp)
                        )

                        if (rep.shortages.isNotEmpty()) {
                            Text(
                                text = "⚠️ Дефициты: ${rep.shortages.joinToString { it.type.titleRu }}",
                                style = MaterialTheme.typography.bodySmall.copy(color = DangerCrimson, fontSize = 10.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Debug & Economy Tuning Dashboard.
 */
@Composable
private fun DebugEconomyTabContent(
    gameState: GameState,
    forecast: EconomyForecast,
    onDebugAddCredits: (Int) -> Unit,
    onDebugDrainResource: (ResourceType) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        item {
            Surface(
                color = FrontierDarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🛠️ ДИАГНОСТИКА ЭКОНОМИКИ БАЗЫ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TechCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Базовая провизия: ${EconomyBalanceConfig.BASE_FOOD_PER_RESIDENT_DAY} ед./житель/день",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp)
                    )
                    Text(
                        text = "• Базовая вода: ${EconomyBalanceConfig.BASE_WATER_PER_RESIDENT_DAY} ед./житель/день",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp)
                    )
                    Text(
                        text = "• Базовый коммерческий оборот: +${EconomyBalanceConfig.BASE_SETTLEMENT_COMMERCE_CREDITS} Кр./день",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp)
                    )
                    Text(
                        text = "• Склад макс.: ${gameState.resources.warehouseMaxCapacity} ед.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextWhite, fontSize = 11.sp)
                    )
                }
            }
        }

        item {
            Text(
                text = "ТЕСТИРОВАНИЕ КАЗНЫ И ДЕФИЦИТОВ",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = WarningAmber,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        // Add credits button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDebugAddCredits(100) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CreditsYellow, contentColor = FrontierDarkBackground),
                    modifier = Modifier.weight(1f).testTag("debug_btn_add_credits")
                ) {
                    Text("+100 Кредитов", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { onDebugAddCredits(500) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald, contentColor = FrontierDarkBackground),
                    modifier = Modifier.weight(1f).testTag("debug_btn_add_credits_500")
                ) {
                    Text("+500 Кредитов", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Deficit test triggers
        item {
            Text(
                text = "СИМУЛЯЦИЯ ИСЧЕРПАНИЯ РЕСУРСА:",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onDebugDrainResource(ResourceType.FOOD) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCrimson),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson),
                    modifier = Modifier.weight(1f).testTag("debug_drain_food")
                ) {
                    Text("Обнулить Еду", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { onDebugDrainResource(ResourceType.WATER) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCrimson),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson),
                    modifier = Modifier.weight(1f).testTag("debug_drain_water")
                ) {
                    Text("Обнулить Воду", fontSize = 11.sp)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onDebugDrainResource(ResourceType.FUEL) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                    modifier = Modifier.weight(1f).testTag("debug_drain_fuel")
                ) {
                    Text("Обнулить Топливо", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { onDebugDrainResource(ResourceType.MONEY) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                    modifier = Modifier.weight(1f).testTag("debug_drain_money")
                ) {
                    Text("Обнулить Казну", fontSize = 11.sp)
                }
            }
        }
    }
}
