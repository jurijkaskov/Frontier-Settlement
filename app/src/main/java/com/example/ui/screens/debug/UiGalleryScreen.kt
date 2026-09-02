package com.example.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CharacterRole
import com.example.domain.model.DangerLevel
import com.example.domain.model.ResourceType
import com.example.ui.components.*
import com.example.ui.motion.*
import com.example.ui.theme.*

/**
 * UI Component Gallery & Style Guide Inspector.
 * Demonstrates all Design System tokens, interactive components, cards, buttons and states.
 */
@Composable
fun UiGalleryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialogDemo by remember { mutableStateOf(false) }
    var selectedTabChip by remember { mutableStateOf("Все") }
    var isCardSelected by remember { mutableStateOf(true) }

    // Motion Section Demo States
    var motionSpeed by remember { mutableStateOf(MotionSpeed.NORMAL) }
    var demoResourceValue by remember { mutableIntStateOf(120) }
    var demoResourceDelta by remember { mutableIntStateOf(0) }
    var isOverloadedCapacity by remember { mutableStateOf(false) }
    var combatDemoHp by remember { mutableIntStateOf(75) }
    var combatDemoAp by remember { mutableIntStateOf(3) }
    var combatFloatingDelta by remember { mutableStateOf<Int?>(null) }
    var isTargetSelected by remember { mutableStateOf(true) }
    var showNewDayDemo by remember { mutableStateOf(false) }
    val notificationController = LocalVisualNotificationController.current

    if (showDialogDemo) {
        GameConfirmationDialog(
            title = "Подтверждение эвакуации",
            message = "Вы уверены, что хотите принудительно эвакуировать отряд? Несобранная добыча будет потеряна.",
            confirmText = "Эвакуировать",
            cancelText = "Остаться",
            isDanger = true,
            onConfirm = { showDialogDemo = false },
            onDismiss = { showDialogDemo = false }
        )
    }

    Scaffold(
        containerColor = GameTheme.colors.background,
        topBar = {
            GameTopBar(
                title = "UI Design System Gallery",
                subtitle = "Frontier Settlement Visual Audit & Reference",
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = GameTheme.spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. BUTTONS HIERARCHY
            item {
                SectionHeader(title = "1. Button Hierarchy", accentColor = GameTheme.colors.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryActionButton(
                        text = "Главное действие (Primary)",
                        icon = Icons.Default.PlayArrow,
                        onClick = {}
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecondaryActionButton(
                            text = "Вторичное (Secondary)",
                            icon = Icons.Default.Tune,
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                        DangerActionButton(
                            text = "Опасность (Danger)",
                            icon = Icons.Default.Warning,
                            onClick = { showDialogDemo = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactActionButton(
                            text = "Compact Action",
                            icon = Icons.Default.Check,
                            onClick = {}
                        )
                        PrimaryActionButton(
                            text = "Disabled State",
                            isEnabled = false,
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            height = 40.dp
                        )
                        IconGameButton(
                            icon = Icons.Default.Notifications,
                            contentDescription = "Уведомления",
                            badgeColor = GameTheme.colors.warning,
                            onClick = {}
                        )
                    }
                }
            }

            // 2. CARD HIERARCHY
            item {
                SectionHeader(title = "2. Card System", accentColor = GameTheme.colors.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InteractiveCard(
                        onClick = { isCardSelected = !isCardSelected },
                        isSelected = isCardSelected
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCardSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isCardSelected) GameTheme.colors.primary else GameTheme.colors.textMuted
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Интерактивная карточка (InteractiveCard)",
                                    style = GameTheme.typography.cardTitle
                                )
                                Text(
                                    text = if (isCardSelected) "Состояние: ВЫБРАНА (Active)" else "Нажмите для выбора",
                                    style = GameTheme.typography.bodySecondary
                                )
                            }
                        }
                    }

                    WarningCard(
                        title = "Критический дефицит провизии",
                        message = "Запасов продовольствия осталось менее чем на 1 сутки. Производительность жителей снижена.",
                        isCritical = true,
                        actionText = "На склад",
                        onActionClick = {}
                    )

                    CompactCard {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GameTheme.colors.info,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Компактная информационная карточка (CompactCard)",
                            style = GameTheme.typography.bodySecondary
                        )
                    }
                }
            }

            // 3. RESOURCE & ECONOMY ICONS
            item {
                SectionHeader(title = "3. Resource & Tactical Icons", accentColor = GameTheme.colors.accentWarm)
                Spacer(modifier = Modifier.height(8.dp))
                GameCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ResourceAmount(type = ResourceType.WATER, amount = 140, delta = -12)
                        ResourceAmount(type = ResourceType.FOOD, amount = 95, delta = +8)
                        ResourceAmount(type = ResourceType.FUEL, amount = 42)
                        ResourceAmount(type = ResourceType.MATERIALS, amount = 230)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ResourceAmount(type = ResourceType.MEDICINE, amount = 15)
                        ResourceAmount(type = ResourceType.AMMO, amount = 60)
                        ResourceAmount(type = ResourceType.COMPONENTS, amount = 18)
                        CreditsAmount(amount = 1450)
                    }
                }
            }

            // 4. DANGER SYSTEM
            item {
                SectionHeader(title = "4. Unified Danger Levels", accentColor = GameTheme.colors.danger)
                Spacer(modifier = Modifier.height(8.dp))
                GameCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DangerBadge(dangerLevel = DangerLevel.SAFE)
                        DangerBadge(dangerLevel = DangerLevel.LOW)
                        DangerBadge(dangerLevel = DangerLevel.MODERATE)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DangerBadge(dangerLevel = DangerLevel.HIGH)
                        DangerBadge(dangerLevel = DangerLevel.EXTREME)
                        DangerBadge(dangerLevel = DangerLevel.UNKNOWN)
                    }
                }
            }

            // 5. CHARACTER PORTRAITS & STATS
            item {
                SectionHeader(title = "5. Character Portraits & Roles", accentColor = GameTheme.colors.primary)
                Spacer(modifier = Modifier.height(8.dp))
                GameCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CharacterPortrait(role = CharacterRole.SCOUT, isLeader = true)
                        CharacterPortrait(role = CharacterRole.SOLDIER, isSelected = true)
                        CharacterPortrait(role = CharacterRole.ENGINEER)
                        CharacterPortrait(role = CharacterRole.SCAVENGER, isInExpedition = true)
                        CharacterPortrait(role = CharacterRole.MEDIC, isWounded = true)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    CharacterStatRow(combat = 8, scavenging = 12, engineering = 6, medical = 4)
                }
            }

            // 6. PROGRESS BARS & AP INDICATORS
            item {
                SectionHeader(title = "6. Combat Indicators & Progress", accentColor = GameTheme.colors.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                GameCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Очки Действий (AP):",
                            style = GameTheme.typography.caption
                        )
                        ActionPointsBar(currentAP = 3, maxAP = 4, pipSize = 14.dp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    StatProgressBar(
                        label = "Здоровье бойца",
                        current = 75,
                        max = 100,
                        barColor = GameTheme.colors.danger
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatProgressBar(
                        label = "Вместимость склада",
                        current = 420,
                        max = 500,
                        barColor = GameTheme.colors.resStorage,
                        suffix = " ед."
                    )
                }
            }

            // 7. CHIPS & FILTERS
            item {
                SectionHeader(title = "7. Tactical Filter Chips", accentColor = GameTheme.colors.accentOrange)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Все", "Оружие", "Броня", "Расходники").forEach { tag ->
                        GameChip(
                            text = tag,
                            isSelected = selectedTabChip == tag,
                            onClick = { selectedTabChip = tag }
                        )
                    }
                }
            }

            // 8. EMPTY & LOADING STATES
            item {
                SectionHeader(title = "8. Feedback & Empty States", accentColor = GameTheme.colors.textMuted)
                Spacer(modifier = Modifier.height(8.dp))
                GameCard {
                    GameEmptyState(
                        title = "Караваны не обнаружены",
                        description = "В данный момент на торговом посту нет прибывших караванов. Ожидайте следующий игровой день.",
                        icon = Icons.Default.Storefront,
                        actionText = "Обновить эфир",
                        onActionClick = {}
                    )
                }
            }

            // 9. VISUAL ASSET SYSTEM & IMAGE COMPONENTS (Point 32)
            item {
                SectionHeader(title = "9. Visual Asset System Components (Пункт 32)", accentColor = GameTheme.colors.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                GameCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Location & Event Hero Art (16:9):", style = GameTheme.typography.cardTitle)
                        GameHeroImage(
                            assetId = "loc_base",
                            height = 120.dp
                        )

                        Text("Characters & Portraits (1:1):", style = GameTheme.typography.cardTitle)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CharacterPortrait(portraitAssetId = "char_portrait_scout_01", size = 52.dp, isLeader = true)
                            CharacterPortrait(portraitAssetId = "char_portrait_soldier_01", size = 52.dp)
                            CharacterPortrait(portraitAssetId = "char_portrait_engineer_01", size = 52.dp)
                            CharacterPortrait(portraitAssetId = "char_portrait_medic_01", size = 52.dp, isWounded = true)
                            CharacterPortrait(portraitAssetId = "char_portrait_scavenger_01", size = 52.dp)
                        }

                        Text("Items & Equipment (1:1 with Rarity):", style = GameTheme.typography.cardTitle)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GameItemImage(assetId = "item_backpack_basic", rarity = com.example.domain.model.ItemRarity.COMMON, size = 48.dp)
                            GameItemImage(assetId = "item_backpack_tactical", rarity = com.example.domain.model.ItemRarity.UNCOMMON, size = 48.dp)
                            GameItemImage(assetId = "item_armor_heavy", rarity = com.example.domain.model.ItemRarity.RARE, size = 48.dp)
                            GameItemImage(assetId = "item_multitool", rarity = com.example.domain.model.ItemRarity.EPIC, size = 48.dp)
                            GameItemImage(assetId = "item_medkit", rarity = com.example.domain.model.ItemRarity.LEGENDARY, size = 48.dp)
                        }

                        Text("Vehicles & Enemies:", style = GameTheme.typography.cardTitle)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            VehicleArtwork(assetId = "veh_foot", size = 54.dp)
                            VehicleArtwork(assetId = "veh_bike", size = 54.dp)
                            VehicleArtwork(assetId = "veh_buggy", size = 54.dp, isSelected = true)
                            EnemyArtwork(assetId = "enemy_raider", size = 54.dp)
                            EnemyArtwork(assetId = "enemy_drone", size = 54.dp)
                            EnemyArtwork(assetId = "enemy_boss", size = 54.dp, isBoss = true)
                        }
                    }
                }
            }

            // 10. MOTION & VISUAL EFFECTS SYSTEM (Point 36)
            item {
                SectionHeader(title = "10. Motion & Visual Effects System (Пункт 36)", accentColor = TechCyan)
                Spacer(modifier = Modifier.height(8.dp))

                GameCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Конфигурация скорости и доступности:",
                            style = GameTheme.typography.cardTitle
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MotionSpeed.entries.forEach { speed ->
                                CompactActionButton(
                                    text = speed.label,
                                    onClick = { motionSpeed = speed },
                                    containerColor = if (motionSpeed == speed) TechCyan.copy(alpha = 0.2f) else GameTheme.colors.surfaceHighlight,
                                    contentColor = if (motionSpeed == speed) TechCyan else GameTheme.colors.textSecondary,
                                    borderColor = if (motionSpeed == speed) TechCyan else GameTheme.colors.borderLight,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Divider(color = GameTheme.colors.borderLight)

                        Text("1. Динамическое изменение ресурсов & всплывающие дельты:", style = GameTheme.typography.cardTitle)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Топливо: ", style = GameTheme.typography.bodySecondary)
                                AnimatedResourceValue(
                                    targetValue = demoResourceValue,
                                    style = GameTheme.typography.screenTitle.copy(fontSize = 18.sp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                ResourceDeltaBadge(
                                    delta = demoResourceDelta,
                                    onDismiss = { demoResourceDelta = 0 }
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            CompactActionButton(
                                text = "+10",
                                onClick = {
                                    demoResourceValue += 10
                                    demoResourceDelta = 10
                                },
                                containerColor = SafeEmerald.copy(alpha = 0.15f),
                                contentColor = SafeEmerald,
                                borderColor = SafeEmerald
                            )

                            CompactActionButton(
                                text = "-5",
                                onClick = {
                                    demoResourceValue = (demoResourceValue - 5).coerceAtLeast(0)
                                    demoResourceDelta = -5
                                },
                                containerColor = DangerRed.copy(alpha = 0.15f),
                                contentColor = DangerRed,
                                borderColor = DangerRed
                            )
                        }

                        Divider(color = GameTheme.colors.borderLight)

                        Text("2. Вместимость хранилища с предупреждением переполнения:", style = GameTheme.typography.cardTitle)
                        AnimatedCapacityBar(
                            current = if (isOverloadedCapacity) 950 else 600,
                            max = 1000,
                            height = 10.dp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOverloadedCapacity) "Критическое заполнение: 950 / 1000 (95%)" else "Штатное хранение: 600 / 1000 (60%)",
                                style = GameTheme.typography.caption.copy(
                                    color = if (isOverloadedCapacity) DangerRed else GameTheme.colors.textMuted
                                )
                            )
                            CompactActionButton(
                                text = if (isOverloadedCapacity) "Сбросить нагрузку" else "Симулировать перегруз (>90%)",
                                onClick = { isOverloadedCapacity = !isOverloadedCapacity }
                            )
                        }

                        Divider(color = GameTheme.colors.borderLight)

                        Text("3. Боевые индикаторы (HP, AP, Таргетинг, Дельты):", style = GameTheme.typography.cardTitle)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Здоровье бойца ($combatDemoHp / 100):", style = GameTheme.typography.caption)
                                Spacer(modifier = Modifier.height(4.dp))
                                CombatHpBar(currentHp = combatDemoHp, maxHp = 100, height = 10.dp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Очки действий:", style = GameTheme.typography.caption)
                                Spacer(modifier = Modifier.height(4.dp))
                                CombatApPips(currentAp = combatDemoAp, maxAp = 4, pipSize = 12.dp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompactActionButton(
                                text = "Урон (-25)",
                                onClick = {
                                    combatDemoHp = (combatDemoHp - 25).coerceAtLeast(0)
                                    combatFloatingDelta = -25
                                    combatDemoAp = (combatDemoAp - 1).coerceAtLeast(0)
                                },
                                containerColor = DangerRed.copy(alpha = 0.15f),
                                contentColor = DangerRed,
                                borderColor = DangerRed,
                                modifier = Modifier.weight(1f)
                            )

                            CompactActionButton(
                                text = "Лечение (+20)",
                                onClick = {
                                    combatDemoHp = (combatDemoHp + 20).coerceAtMost(100)
                                    combatFloatingDelta = 20
                                    combatDemoAp = (combatDemoAp + 1).coerceAtMost(4)
                                },
                                containerColor = SafeEmerald.copy(alpha = 0.15f),
                                contentColor = SafeEmerald,
                                borderColor = SafeEmerald,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        combatFloatingDelta?.let { delta ->
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CombatFloatingDelta(
                                    delta = delta,
                                    onFinished = { combatFloatingDelta = null }
                                )
                            }
                        }

                        Divider(color = GameTheme.colors.borderLight)

                        Text("4. Очередь визуальных уведомлений (Notification Host):", style = GameTheme.typography.cardTitle)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompactActionButton(
                                text = "Успех",
                                onClick = {
                                    notificationController.show(
                                        VisualNotification(
                                            title = "Задание выполнено!",
                                            message = "Получено +150 кредитов и +50 опыта",
                                            type = VisualNotificationType.SUCCESS
                                        )
                                    )
                                },
                                containerColor = SafeEmerald.copy(alpha = 0.15f),
                                contentColor = SafeEmerald,
                                borderColor = SafeEmerald,
                                modifier = Modifier.weight(1f)
                            )

                            CompactActionButton(
                                text = "Тревога",
                                onClick = {
                                    notificationController.show(
                                        VisualNotification(
                                            title = "Нехватка провизии",
                                            message = "Запас пайков на исходе (<20 ед.)",
                                            type = VisualNotificationType.WARNING
                                        )
                                    )
                                },
                                containerColor = WarningAmber.copy(alpha = 0.15f),
                                contentColor = WarningAmber,
                                borderColor = WarningAmber,
                                modifier = Modifier.weight(1f)
                            )

                            CompactActionButton(
                                text = "Уровень",
                                onClick = {
                                    notificationController.show(
                                        VisualNotification(
                                            title = "Поселение повысило уровень!",
                                            message = "Разблокированы новые чертежи мастерской",
                                            type = VisualNotificationType.LEVEL_UP
                                        )
                                    )
                                },
                                containerColor = CreditsYellow.copy(alpha = 0.15f),
                                contentColor = CreditsYellow,
                                borderColor = CreditsYellow,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Divider(color = GameTheme.colors.borderLight)

                        Text("5. Атмосферный экран смены дня (New Day Banner):", style = GameTheme.typography.cardTitle)
                        SecondaryActionButton(
                            text = "Показать оверлей смены дня (День 14)",
                            icon = Icons.Default.WbSunny,
                            onClick = { showNewDayDemo = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showNewDayDemo) {
        NewDayBannerOverlay(
            dayNumber = 14,
            isVisible = showNewDayDemo,
            onDismiss = { showNewDayDemo = false }
        )
    }
}
