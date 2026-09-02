package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.audio.manager.GameAudioManager
import com.example.audio.model.GameSoundId
import com.example.audio.ui.LocalGameAudio
import com.example.domain.model.ExpeditionStatus
import com.example.domain.model.QuestStatus
import com.example.ui.components.GameBottomNav
import com.example.ui.components.GameTopHUD
import com.example.ui.components.SettlementEventsDialog
import com.example.ui.components.ExpeditionReturnSummaryDialog
import com.example.ui.components.TimeManagementDialog
import com.example.ui.motion.GameScreenTransitions
import com.example.ui.motion.GameVisualEffectHost
import com.example.ui.motion.LocalVisualNotificationController
import com.example.ui.motion.NewDayBannerOverlay
import com.example.ui.motion.VisualNotificationController
import com.example.ui.screens.*
import com.example.ui.screens.debug.AudioGalleryScreen
import com.example.ui.screens.debug.ContentBrowserScreen
import com.example.ui.screens.debug.DebugSaveScreen
import com.example.ui.screens.debug.GeneratorDebugScreen
import com.example.ui.screens.debug.UiGalleryScreen
import com.example.ui.screens.debug.VisualAssetBrowserScreen
import com.example.ui.screens.save.LoadGameScreen
import com.example.ui.screens.save.SaveGameScreen
import com.example.ui.theme.FrontierDarkBackground
import com.example.viewmodel.GameViewModel

@Composable
fun MainGameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val gameAudioManager = remember { GameAudioManager.getInstance(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> gameAudioManager.onAppBackground()
                Lifecycle.Event.ON_START -> gameAudioManager.onAppForeground()
                Lifecycle.Event.ON_DESTROY -> gameAudioManager.release()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val lastResourceOp by viewModel.lastResourceOperation.collectAsStateWithLifecycle()
    val lastTradeResult by viewModel.lastTradeResult.collectAsStateWithLifecycle()
    val lastCraftResult by viewModel.lastCraftResult.collectAsStateWithLifecycle()
    val lastSquadOp by viewModel.lastSquadOperation.collectAsStateWithLifecycle()
    val selectedTravelMode by viewModel.selectedTravelMode.collectAsStateWithLifecycle()
    val expeditionDraft by viewModel.expeditionDraft.collectAsStateWithLifecycle()
    val selectedActorIdForEvent by viewModel.selectedActorIdForEvent.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "settlement"

    val baseRoute = currentRoute.substringBefore("/")

    val isTopLevelRoute = baseRoute in listOf("settlement", "map", "squad", "market", "menu")

    val screensWithDedicatedHeader = setOf(
        "settings", "help", "about", "licenses", "save", "load", "game_menu", "quests",
        "combat", "reputation", "residents", "economy", "arrival", "expedition_prep",
        "debug_save", "content_browser", "generator_debug", "ui_gallery",
        "visual_asset_browser", "audio_gallery"
    )
    val shouldShowTopHUD = baseRoute !in screensWithDedicatedHeader

    var showEventsDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    val notificationController = remember { VisualNotificationController() }
    var showNewDayBanner by remember { mutableStateOf(false) }
    var previousDay by remember { mutableIntStateOf(gameState.day) }

    LaunchedEffect(currentRoute, gameState) {
        gameAudioManager.syncWithGameState(currentRoute, gameState)
    }

    LaunchedEffect(gameState.day) {
        if (gameState.day > previousDay) {
            showNewDayBanner = true
            previousDay = gameState.day
            gameAudioManager.playSfx(GameSoundId.NEW_DAY)
        }
    }

    if (showEventsDialog) {
        SettlementEventsDialog(
            logs = gameState.dayLogs,
            onDismiss = { showEventsDialog = false }
        )
    }

    if (showTimeDialog) {
        TimeManagementDialog(
            gameState = gameState,
            onAdvanceHours = { hours -> viewModel.advanceTimeHours(hours) },
            onNextDayClick = {
                viewModel.nextDay()
                showTimeDialog = false
            },
            onDismiss = { showTimeDialog = false }
        )
    }

    gameState.lastReturnSummary?.let { returnSummary ->
        ExpeditionReturnSummaryDialog(
            summary = returnSummary,
            onDismiss = { viewModel.dismissReturnSummary() },
            onNavigateToWarehouse = {
                viewModel.dismissReturnSummary()
                navController.navigate("warehouse")
            }
        )
    }

    val appSettingsRepo = remember { com.example.data.repository.AppSettingsRepository.getInstance(context) }
    val appSettings by appSettingsRepo.settingsFlow.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalVisualNotificationController provides notificationController,
        LocalGameAudio provides gameAudioManager,
        com.example.ui.theme.LocalFrontierMotion provides com.example.ui.motion.FrontierGameMotion(isReducedMotion = appSettings.isReducedMotion)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = FrontierDarkBackground,
                contentWindowInsets = WindowInsets.navigationBars,
                topBar = {
                    if (shouldShowTopHUD) {
                        GameTopHUD(
                            day = gameState.day,
                            gameDateTime = gameState.gameDateTime,
                            resources = gameState.resources,
                            settlement = gameState.settlement,
                            onNextDayClick = { viewModel.nextDay() },
                            onTimeClick = { showTimeDialog = true },
                            onEconomyClick = { navController.navigate("economy") },
                            onWarehouseClick = { navController.navigate("warehouse") },
                            onEventsClick = { showEventsDialog = true },
                            onMenuClick = { navController.navigate("game_menu") },
                            hasUnreadEvents = gameState.quests.any { it.status == QuestStatus.READY_TO_CLAIM }
                        )
                    }
                },
                bottomBar = {
                    if (isTopLevelRoute) {
                        GameBottomNav(
                            currentRoute = baseRoute,
                            onTabSelected = { route ->
                                navController.navigate(route) {
                                    popUpTo("settlement") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            hasActiveExpedition = gameState.activeExpedition != null,
                            hasClaimableQuest = gameState.quests.any { it.status == QuestStatus.READY_TO_CLAIM }
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "settlement",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(FrontierDarkBackground),
                    enterTransition = { GameScreenTransitions.enter() },
                    exitTransition = { GameScreenTransitions.exit() },
                    popEnterTransition = { GameScreenTransitions.popEnter() },
                    popExitTransition = { GameScreenTransitions.popExit() }
                ) {
            // Main Hub & Tabs
            composable("settlement") {
                SettlementScreen(
                    gameState = gameState,
                    onNavigateToWarehouse = { navController.navigate("warehouse") },
                    onNavigateToWorkshop = { navController.navigate("workshop") },
                    onNavigateToBuildings = { navController.navigate("buildings") },
                    onNavigateToResearch = { navController.navigate("research") },
                    onNavigateToMap = { navController.navigate("map") },
                    onNavigateToMarket = { navController.navigate("market") },
                    onNavigateToSquad = { navController.navigate("squad") },
                    onNavigateToResidents = { navController.navigate("residents") },
                    onNavigateToQuests = { navController.navigate("menu") },
                    onNavigateToVehicles = { navController.navigate("vehicles") },
                    onNavigateToEconomy = { navController.navigate("economy") },
                    onNavigateToReputation = { navController.navigate("reputation") },
                    onNavigateToExpeditionLive = {
                        if (gameState.activeCombat != null) {
                            navController.navigate("combat")
                        } else if (gameState.activeExpedition?.phase == com.example.domain.model.ExpeditionPhase.AT_LOCATION) {
                            navController.navigate("arrival")
                        } else {
                            navController.navigate("expedition_live")
                        }
                    }
                )
            }

            composable("map") {
                MapScreen(
                    gameState = gameState,
                    onSelectLocation = { locationId ->
                        navController.navigate("expedition_prep/$locationId")
                    },
                    onNavigateToExpeditionLive = {
                        if (gameState.activeCombat != null) {
                            navController.navigate("combat")
                        } else if (gameState.activeExpedition?.phase == com.example.domain.model.ExpeditionPhase.AT_LOCATION) {
                            navController.navigate("arrival")
                        } else {
                            navController.navigate("expedition_live")
                        }
                    },
                    onNavigateToSettlement = {
                        navController.navigate("settlement")
                    },
                    onStartTravel = { destId, mode ->
                        viewModel.startTravel(destId, mode)
                    },
                    onAdvanceTravelStep = {
                        viewModel.advanceTravelStep()
                    },
                    onInstantArrive = {
                        viewModel.instantArriveTravel()
                    },
                    onReturnToBase = {
                        viewModel.startReturnTravel()
                    },
                    onExploreArrived = {
                        navController.navigate("arrival")
                    },
                    selectedTravelMode = selectedTravelMode,
                    onSelectTravelMode = { mode ->
                        viewModel.selectTravelMode(mode)
                    }
                )
            }

            composable("squad") {
                SquadScreen(
                    gameState = gameState,
                    onToggleSquadMember = { charId -> viewModel.toggleSquadMember(charId) },
                    onAddSquadMember = { charId -> viewModel.addSquadMember(charId) },
                    onRemoveSquadMember = { charId -> viewModel.removeSquadMember(charId) },
                    onSetSquadLeader = { charId -> viewModel.setSquadLeader(charId) },
                    onClearSquad = { viewModel.clearSquad() },
                    onNavigateToResidents = { navController.navigate("residents") },
                    onNavigateToVehicles = { navController.navigate("vehicles") },
                    onNavigateToMap = { navController.navigate("map") },
                    onHealResident = { charId -> viewModel.healResidentInClinic(charId) },
                    onAllocateSkillPoint = { charId, stat -> viewModel.allocateCharacterSkillPoint(charId, stat) },
                    onAwardExperience = { charId, xp -> viewModel.awardCharacterExperience(charId, xp) },
                    onEquipItem = { charId, slot, itemId -> viewModel.equipCharacterItem(charId, slot, itemId) },
                    onUnequipItem = { charId, slot -> viewModel.unequipCharacterItem(charId, slot) },
                    lastSquadOperation = lastSquadOp,
                    onDismissOperationResult = { viewModel.clearLastSquadOperation() }
                )
            }

            composable("residents") {
                ResidentsScreen(
                    gameState = gameState,
                    onToggleSquadMember = { charId -> viewModel.toggleSquadMember(charId) },
                    onRecruitSurvivor = { role -> viewModel.recruitSurvivor(role) },
                    onRetireResident = { charId -> viewModel.retireResident(charId) },
                    onHealResident = { charId -> viewModel.healResidentInClinic(charId) },
                    onDebugAddSurvivor = { role -> viewModel.debugAddSurvivor(role) },
                    onAllocateSkillPoint = { charId, stat -> viewModel.allocateCharacterSkillPoint(charId, stat) },
                    onAwardExperience = { charId, xp -> viewModel.awardCharacterExperience(charId, xp) },
                    onEquipItem = { charId, slot, itemId -> viewModel.equipCharacterItem(charId, slot, itemId) },
                    onUnequipItem = { charId, slot -> viewModel.unequipCharacterItem(charId, slot) },
                    lastOperation = lastResourceOp,
                    onDismissOperation = { viewModel.clearLastResourceOperation() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("market") {
                MarketScreen(
                    gameState = gameState,
                    tradeResult = lastTradeResult,
                    onTrade = { offerId, quantity, mode ->
                        viewModel.executeTrade(offerId, quantity, mode)
                    },
                    onDismissTradeResult = { viewModel.clearLastTradeResult() },
                    onRestockMarket = { viewModel.debugRestockMarket() }
                )
            }

            composable("menu") {
                MenuScreen(
                    gameState = gameState,
                    lastResourceOp = lastResourceOp,
                    onClaimQuest = { qId -> viewModel.claimQuest(qId) },
                    onModifyResource = { type, delta -> viewModel.debugModifyResource(type, delta) },
                    onTestFillWarehouse = { viewModel.debugFillWarehouseTo(0.95f) },
                    onTestDrainSupplies = { viewModel.debugDrainSupplies() },
                    onTestPartialAdd = { viewModel.debugTestPartialOverflow() },
                    onTestResetResources = { viewModel.debugResetToDefaultResources() },
                    onAddSettlementXp = { xp -> viewModel.debugAddSettlementXp(xp) },
                    onLevelUpSettlement = { viewModel.debugLevelUpSettlement() },
                    onConstructAllBuildings = { viewModel.debugConstructAllBuildings() },
                    onResetGame = { viewModel.resetGame() },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToHelp = { navController.navigate("help") },
                    onNavigateToAbout = { navController.navigate("about") },
                    onNavigateToSaveGame = { navController.navigate("save") },
                    onNavigateToLoadGame = { navController.navigate("load") },
                    onNavigateToDebugSave = { navController.navigate("debug_save") },
                    onNavigateToContentBrowser = { navController.navigate("content_browser") },
                    onNavigateToGeneratorDebug = { navController.navigate("generator_debug") },
                    onNavigateToUiGallery = { navController.navigate("ui_gallery") },
                    onNavigateToVisualAssetBrowser = { navController.navigate("visual_asset_browser") },
                    onNavigateToAudioGallery = { navController.navigate("audio_gallery") }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToSaveGame = { navController.navigate("save") },
                    onNavigateToLoadGame = { navController.navigate("load") },
                    onNavigateToHelp = { navController.navigate("help") },
                    onNavigateToAbout = { navController.navigate("about") },
                    onNavigateToContentBrowser = { navController.navigate("content_browser") },
                    onNavigateToGeneratorDebug = { navController.navigate("generator_debug") },
                    onNavigateToUiGallery = { navController.navigate("ui_gallery") },
                    onNavigateToVisualAssetBrowser = { navController.navigate("visual_asset_browser") },
                    onNavigateToAudioGallery = { navController.navigate("audio_gallery") },
                    onNavigateToDebugSave = { navController.navigate("debug_save") }
                )
            }

            composable("game_menu") {
                GameMenuScreen(
                    gameState = gameState,
                    onResume = { navController.popBackStack() },
                    onNavigateToSave = { navController.navigate("save") },
                    onNavigateToLoad = { navController.navigate("load") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToHelp = { navController.navigate("help") },
                    onNavigateToAbout = { navController.navigate("about") },
                    onResetGame = {
                        viewModel.resetGame()
                        navController.navigate("settlement") {
                            popUpTo("settlement") { inclusive = true }
                        }
                    }
                )
            }

            composable("help") {
                HelpScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "help/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId")
                HelpScreen(
                    initialCategoryId = categoryId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("about") {
                AboutScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLicenses = { navController.navigate("licenses") }
                )
            }

            composable("licenses") {
                LicensesScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("recovery") {
                RecoveryScreen(
                    slotName = "Автосохранение",
                    errorMessage = "Не удалось прочитать файл сохранения.",
                    hasBackupAvailable = true,
                    onRestoreBackup = {
                        viewModel.restoreFromBackup(
                            backupSlotId = com.example.data.save.SaveSlotId.AUTOSAVE_BACKUP.id,
                            targetSlotId = com.example.data.save.SaveSlotId.AUTOSAVE.id,
                            onNavigateToRoute = { route ->
                                navController.navigate(route) {
                                    popUpTo("settlement") { inclusive = true }
                                }
                            }
                        )
                    },
                    onOpenLoadScreen = { navController.navigate("load") },
                    onStartNewGame = {
                        viewModel.resetGame()
                        navController.navigate("settlement") {
                            popUpTo("settlement") { inclusive = true }
                        }
                    }
                )
            }

            composable("save") {
                SaveGameScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("load") {
                LoadGameScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGame = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo("settlement") { inclusive = false }
                        }
                    }
                )
            }

            composable("debug_save") {
                DebugSaveScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sub-Screens
            composable("warehouse") {
                WarehouseScreen(
                    gameState = gameState,
                    onUpgradeStorage = {
                        val storageBld = gameState.settlement.buildings.find { it.type == com.example.domain.model.BuildingType.STORAGE_DEPOT }
                        if (storageBld != null) viewModel.upgradeBuilding(storageBld.id)
                    },
                    onBack = { navController.popBackStack() },
                    onClaimPendingCargo = { viewModel.claimPendingUnloadCargo() }
                )
            }

            composable("workshop") {
                WorkshopScreen(
                    gameState = gameState,
                    onCraft = { recipeId, count ->
                        viewModel.craftItem(recipeId, count)
                    },
                    craftResult = lastCraftResult,
                    onDismissCraftResult = { viewModel.clearLastCraftResult() },
                    onUpgradeWorkshop = {
                        val workshopBld = gameState.settlement.buildings.find { it.type == com.example.domain.model.BuildingType.WORKSHOP }
                        if (workshopBld != null) viewModel.upgradeBuilding(workshopBld.id)
                    },
                    onDebugSupplies = { viewModel.debugAddCraftingSupplies() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("buildings") {
                BuildingsScreen(
                    gameState = gameState,
                    onBuildBuilding = { bldId -> viewModel.buildBuilding(bldId) },
                    onUpgradeBuilding = { bldId -> viewModel.upgradeBuilding(bldId) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("research") {
                ResearchScreen(
                    gameState = gameState,
                    onResearchTech = { techId -> viewModel.researchTech(techId) },
                    onBack = { navController.popBackStack() },
                    onNavigateToBuildings = { navController.navigate("buildings") },
                    onDebugAddSupplies = { viewModel.debugAddResearchSupplies() }
                )
            }

            composable("vehicles") {
                VehiclesScreen(
                    gameState = gameState,
                    onSelectVehicle = { vehId -> viewModel.selectVehicle(vehId) },
                    onRepairVehicle = { vehId -> viewModel.repairVehicle(vehId) },
                    onCraftVehicle = { type, name, mat, cred ->
                        viewModel.craftVehicle(type, name, mat, cred)
                    },
                    onNavigateToMap = { navController.navigate("map") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("economy") {
                EconomyScreen(
                    gameState = gameState,
                    onBack = { navController.popBackStack() },
                    onNavigateToWarehouse = { navController.navigate("warehouse") },
                    onNavigateToBuildings = { navController.navigate("buildings") },
                    onDebugAddCredits = { amt -> viewModel.debugAddTreasuryCredits(amt) },
                    onDebugDrainResource = { type -> viewModel.debugDrainResourceForDeficitTest(type) },
                    onDebugClearDeficits = { viewModel.clearUnpaidDeficits() }
                )
            }

            composable(
                route = "expedition_prep/{locationId}",
                arguments = listOf(navArgument("locationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val locationId = backStackEntry.arguments?.getString("locationId") ?: ""
                ExpeditionPrepScreen(
                    locationId = locationId,
                    gameState = gameState,
                    expeditionDraft = expeditionDraft,
                    onInitDraft = { locId -> viewModel.initExpeditionDraft(locId) },
                    onToggleParticipant = { charId -> viewModel.toggleDraftParticipant(charId) },
                    onSetLeader = { leaderId -> viewModel.setDraftLeader(leaderId) },
                    onSelectTravelMode = { mode -> viewModel.setDraftTravelMode(mode) },
                    onSelectVehicle = { vehId -> viewModel.setDraftVehicle(vehId) },
                    onSetSupply = { type, amt -> viewModel.setDraftSupply(type, amt) },
                    onApplyRecommendedSupplies = { viewModel.applyRecommendedSupplies() },
                    onEquipItem = { charId, slot, itemId -> viewModel.equipItem(charId, slot, itemId) },
                    onUnequipItem = { charId, slot -> viewModel.unequipItem(charId, slot) },
                    onStartExpedition = {
                        val result = viewModel.startPreparedExpedition(locationId)
                        if (result.isSuccess) {
                            navController.navigate("expedition_live") {
                                popUpTo("map") { inclusive = false }
                            }
                        }
                    },
                    onBackToMap = { navController.popBackStack() }
                )
            }

            composable("arrival") {
                ArrivalScreen(
                    gameState = gameState,
                    onStartExploration = {
                        viewModel.startExplorationFromArrival()
                        navController.navigate("expedition_live") {
                            popUpTo("map") { inclusive = false }
                        }
                    },
                    onReturnToBase = {
                        val result = viewModel.returnExpeditionFromArrival()
                        if (result is com.example.domain.model.TravelTransactionResult.Success) {
                            navController.navigate("map") {
                                popUpTo("map") { inclusive = true }
                            }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    onScoutSurroundings = { locId ->
                        viewModel.scoutSurroundings(locId)
                    }
                )
            }

            composable("expedition_live") {
                if (gameState.activeCombat != null) {
                    CombatScreen(
                        gameState = gameState,
                        onCombatAction = { action -> viewModel.executeCombatAction(action) },
                        onSelectTarget = { targetId -> viewModel.selectCombatTarget(targetId) },
                        onUseItem = { itemId -> viewModel.useCombatItem(itemId) },
                        onFinishCombatVictory = { viewModel.finishCombatVictory() },
                        onRetreat = { viewModel.retreatFromCombat() },
                        onCancelTargeting = { viewModel.cancelCombatTargeting() },
                        onDebugRestoreAP = { viewModel.debugCombatRestoreAP() },
                        onDebugSkipTurn = { viewModel.debugCombatSkipTurn() },
                        onDebugForceVictory = { viewModel.debugCombatForceVictory() },
                        onDebugForceDefeat = { viewModel.debugCombatForceDefeat() }
                    )
                } else if (gameState.activeExpedition?.phase == com.example.domain.model.ExpeditionPhase.AT_LOCATION) {
                    ArrivalScreen(
                        gameState = gameState,
                        onStartExploration = {
                            viewModel.startExplorationFromArrival()
                        },
                        onReturnToBase = {
                            val result = viewModel.returnExpeditionFromArrival()
                            if (result is com.example.domain.model.TravelTransactionResult.Success) {
                                navController.navigate("map") {
                                    popUpTo("map") { inclusive = true }
                                }
                            }
                        },
                        onBack = {
                            navController.navigate("map") {
                                popUpTo("map") { inclusive = true }
                            }
                        },
                        onScoutSurroundings = { locId ->
                            viewModel.scoutSurroundings(locId)
                        }
                    )
                } else {
                    ExpeditionLiveScreen(
                        gameState = gameState,
                        onChoiceA = { viewModel.resolveExpeditionChoice(chooseOptionA = true) },
                        onChoiceB = { viewModel.resolveExpeditionChoice(chooseOptionA = false) },
                        onExecuteChoice = { choiceId -> viewModel.executeEventChoice(choiceId) },
                        onSelectActor = { actorId -> viewModel.selectActorForEventCheck(actorId) },
                        onContinueExploration = { viewModel.advanceExpeditionExplorationStep() },
                        selectedActorId = selectedActorIdForEvent,
                        onStartCombat = {
                            viewModel.startCombatEncounter()
                            navController.navigate("combat")
                        },
                        onFinishAndReturn = {
                            viewModel.completeExpeditionAndReturn()
                            navController.navigate("settlement") {
                                popUpTo("settlement") { inclusive = true }
                            }
                        },
                        onBackToSettlement = {
                            navController.navigate("settlement") {
                                popUpTo("settlement") { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable("combat") {
                CombatScreen(
                    gameState = gameState,
                    onCombatAction = { action -> viewModel.executeCombatAction(action) },
                    onSelectTarget = { targetId -> viewModel.selectCombatTarget(targetId) },
                    onUseItem = { itemId -> viewModel.useCombatItem(itemId) },
                    onFinishCombatVictory = {
                        viewModel.finishCombatVictory()
                        navController.navigate("expedition_live") {
                            popUpTo("expedition_live") { inclusive = true }
                        }
                    },
                    onRetreat = {
                        viewModel.retreatFromCombat()
                        navController.navigate("settlement") {
                            popUpTo("settlement") { inclusive = true }
                        }
                    }
                )
            }

            composable("reputation") {
                ReputationScreen(
                    gameState = gameState,
                    onBack = { navController.popBackStack() },
                    onSettlementReputationChange = { delta, reason ->
                        viewModel.changeSettlementReputation(delta, reason)
                    },
                    onFactionRelationChange = { factionId, delta, reason ->
                        viewModel.changeFactionRelation(factionId, delta, reason)
                    },
                    onResetReputationDebug = {
                        viewModel.resetReputationDebug()
                    }
                )
            }

            composable("quests") {
                QuestsScreen(
                    gameState = gameState,
                    onBack = { navController.popBackStack() },
                    onAcceptQuest = { qId -> viewModel.acceptQuest(qId) },
                    onDeclineQuest = { qId -> viewModel.declineQuest(qId) },
                    onTurnInQuest = { qId -> viewModel.turnInQuest(qId) },
                    onTrackQuest = { qId -> viewModel.setTrackedQuest(qId) },
                    onDeliverResource = { qId, objId, amt -> viewModel.deliverQuestResource(qId, objId, amt) },
                    onDeliverItem = { qId, objId, itemId -> viewModel.deliverQuestItem(qId, objId, itemId) }
                )
            }

            composable("content_browser") {
                ContentBrowserScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("generator_debug") {
                GeneratorDebugScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("ui_gallery") {
                UiGalleryScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("visual_asset_browser") {
                VisualAssetBrowserScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("audio_gallery") {
                AudioGalleryScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    GameVisualEffectHost(controller = notificationController)

    NewDayBannerOverlay(
        dayNumber = gameState.day,
        isVisible = showNewDayBanner,
        onDismiss = { showNewDayBanner = false }
    )
}
}
}
