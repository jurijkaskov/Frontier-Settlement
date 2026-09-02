package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GameCard
import com.example.ui.theme.*

data class OpenSourceLicense(
    val name: String,
    val author: String,
    val licenseType: String,
    val licenseText: String
)

private val LICENSES_LIST = listOf(
    OpenSourceLicense(
        name = "Jetpack Compose & AndroidX Libraries",
        author = "The Android Open Source Project (Google LLC)",
        licenseType = "Apache License 2.0",
        licenseText = """
            Licensed under the Apache License, Version 2.0 (the "License");
            you may not use this file except in compliance with the License.
            You may obtain a copy of the License at

                http://www.apache.org/licenses/LICENSE-2.0

            Unless required by applicable law or agreed to in writing, software
            distributed under the License is distributed on an "AS IS" BASIS,
            WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        """.trimIndent()
    ),
    OpenSourceLicense(
        name = "Kotlin Standard Library & Coroutines",
        author = "JetBrains s.r.o. and Kotlin Programming Language contributors",
        licenseType = "Apache License 2.0",
        licenseText = """
            Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
            Licensed under the Apache License, Version 2.0.
        """.trimIndent()
    ),
    OpenSourceLicense(
        name = "Moshi JSON Library",
        author = "Square, Inc.",
        licenseType = "Apache License 2.0",
        licenseText = """
            Copyright 2015 Square, Inc.
            Licensed under the Apache License, Version 2.0 (the "License").
        """.trimIndent()
    ),
    OpenSourceLicense(
        name = "Material Design Icons",
        author = "Google LLC",
        licenseType = "Apache License 2.0",
        licenseText = """
            Copyright (c) Google LLC.
            Licensed under the Apache License, Version 2.0.
        """.trimIndent()
    ),
    OpenSourceLicense(
        name = "Roborazzi & Robolectric",
        author = "Roborazzi / Robolectric Contributors",
        licenseType = "Apache License 2.0 / MIT License",
        licenseText = """
            Copyright (c) 2020-2024 Contributors.
            Licensed under the Apache License, Version 2.0.
        """.trimIndent()
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = FrontierDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Сторонние лицензии",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_licenses_back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
        ) {
            items(LICENSES_LIST, key = { it.name }) { item ->
                GameCard(
                    backgroundColor = FrontierDarkSurfaceElevated,
                    borderColor = FrontierBorder
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.author,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TechCyan,
                                    fontSize = 11.sp
                                )
                            )
                            Surface(
                                color = FrontierDarkSurfaceHighlight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.licenseType,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = WarningAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Divider(color = FrontierBorder.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))

                        Text(
                            text = item.licenseText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
