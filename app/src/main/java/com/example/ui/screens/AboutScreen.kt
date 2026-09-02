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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.components.GameCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = FrontierDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Об игре",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_about_back")
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Hero Title Card
            item {
                GameCard(
                    backgroundColor = FrontierDarkSurfaceElevated,
                    borderColor = TechCyan
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TechCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = TechCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "FRONTIER SETTLEMENT",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                fontSize = 20.sp
                            )
                        )

                        Text(
                            text = "Постапокалиптическая 2D-стратегия выживания аванпоста с пошаговыми экспедициями, добычей и развитием поселения.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = FrontierDarkSurfaceHighlight,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Версия ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TechCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                color = if (BuildConfig.DEBUG) WarningAmber.copy(alpha = 0.2f) else SafeEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (BuildConfig.DEBUG) "DEBUG" else "RELEASE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (BuildConfig.DEBUG) WarningAmber else SafeEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Credits Section
            item {
                GameCard(
                    backgroundColor = FrontierDarkSurfaceElevated,
                    borderColor = FrontierBorder
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Команда и разработка",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Divider(color = FrontierBorder, thickness = 1.dp)

                        CreditItem(role = "Архитектура и геймдизайн", name = "Frontier Core Engine Team")
                        CreditItem(role = "Разработка на Kotlin & Jetpack Compose", name = "Android Engineering Guild")
                        CreditItem(role = "Визуальный дизайн и система ассетов", name = "Tactical Wasteland Art Division")
                        CreditItem(role = "Атмосферный саундтрек и звуковой движок", name = "Frontier Audio Labs")
                    }
                }
            }

            // Engine & Tech Stack
            item {
                GameCard(
                    backgroundColor = FrontierDarkSurfaceElevated,
                    borderColor = FrontierBorder
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = TechCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Технологический стек",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Divider(color = FrontierBorder, thickness = 1.dp)

                        Text(
                            text = "• 100% Kotlin & Jetpack Compose Material 3\n• Room Database & Moshi Serialization\n• Custom Procedural Synthesizer & SoundPool Audio Engine\n• Roborazzi & Robolectric Quality Assurance",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            // Third-Party Licenses Navigation Button
            item {
                Button(
                    onClick = onNavigateToLicenses,
                    colors = ButtonDefaults.buttonColors(containerColor = FrontierDarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_about_licenses")
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = TechCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Сторонние лицензии и библиотеки (Open Source)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditItem(role: String, name: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TechCyan,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        )
    }
}
