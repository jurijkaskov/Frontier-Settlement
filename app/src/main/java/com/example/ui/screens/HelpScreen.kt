package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.help.HelpArticleDefinition
import com.example.domain.model.help.HelpCategory
import com.example.domain.service.help.HelpCatalog
import com.example.ui.components.GameCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    initialCategoryId: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember {
        mutableStateOf(
            if (!initialCategoryId.isNullOrBlank()) {
                HelpCategory.fromId(initialCategoryId)
            } else {
                null
            }
        )
    }

    val articles = remember(selectedCategory) {
        if (selectedCategory == null) {
            HelpCatalog.ARTICLES
        } else {
            HelpCatalog.getArticlesForCategory(selectedCategory!!)
        }
    }

    Scaffold(
        containerColor = FrontierDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Справочник выживания",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_help_back")
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Scrollable Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FrontierDarkSurface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" filter chip
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Все разделы (${HelpCatalog.ARTICLES.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TechCyan,
                        selectedLabelColor = FrontierOnPrimary,
                        containerColor = FrontierDarkSurfaceHighlight,
                        labelColor = TextWhite
                    ),
                    modifier = Modifier.testTag("chip_help_category_all")
                )

                HelpCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category.titleRu, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TechCyan,
                            selectedLabelColor = FrontierOnPrimary,
                            containerColor = FrontierDarkSurfaceHighlight,
                            labelColor = TextWhite
                        ),
                        modifier = Modifier.testTag("chip_help_category_${category.id}")
                    )
                }
            }

            Divider(color = FrontierBorder, thickness = 1.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
            ) {
                items(articles, key = { it.id }) { article ->
                    HelpArticleCard(article = article)
                }
            }
        }
    }
}

@Composable
fun HelpArticleCard(
    article: HelpArticleDefinition,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    GameCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_help_article_${article.id}"),
        backgroundColor = FrontierDarkSurfaceElevated,
        borderColor = TechCyan.copy(alpha = 0.4f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = TechCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = article.category.titleRu.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Свернуть" else "Развернуть",
                        tint = TextMuted
                    )
                }
            }

            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = FrontierBorder.copy(alpha = 0.5f), thickness = 1.dp)

                    Text(
                        text = "Ключевые правила:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )

                    article.keyPoints.forEach { point ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }

                    if (article.proTips.isNotEmpty()) {
                        Surface(
                            color = WarningAmber.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Совет выживания:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = WarningAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                article.proTips.forEach { tip ->
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextWhite,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
