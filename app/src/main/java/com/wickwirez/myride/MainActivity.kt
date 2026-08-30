package com.wickwirez.myride

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wickwirez.myride.data.OnboardingPrefs
import com.wickwirez.myride.data.MascotVoice
import com.wickwirez.myride.data.ReviewPromptManager
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.ui.AboutScreen
import com.wickwirez.myride.ui.AddServiceRecordScreen
import com.wickwirez.myride.ui.AddServiceRecordViewModel
import com.wickwirez.myride.ui.AddFuelLogScreen
import com.wickwirez.myride.ui.AddFuelLogViewModel
import com.wickwirez.myride.ui.AddVehicleScreen
import com.wickwirez.myride.ui.AddVehicleViewModel
import com.wickwirez.myride.ui.AiAssistantScreen
import com.wickwirez.myride.ui.AiMechanicScreen
import com.wickwirez.myride.ui.PhotoServiceRecordScreen
import com.wickwirez.myride.ui.EditServiceRecordScreen
import com.wickwirez.myride.ui.EditServiceRecordViewModel
import com.wickwirez.myride.ui.EditFuelLogScreen
import com.wickwirez.myride.ui.EditFuelLogViewModel
import com.wickwirez.myride.ui.EditVehicleScreen
import com.wickwirez.myride.ui.EditVehicleViewModel
import com.wickwirez.myride.ui.FuelLogScreen
import com.wickwirez.myride.ui.FuelLogViewModel
import com.wickwirez.myride.ui.GarageScreen
import com.wickwirez.myride.ui.GarageViewModel
import com.wickwirez.myride.ui.HelpScreen
import com.wickwirez.myride.ui.OnboardingScreen
import com.wickwirez.myride.ui.RecallScreen
import com.wickwirez.myride.ui.SettingsScreen
import com.wickwirez.myride.ui.SplashScreen
import com.wickwirez.myride.ui.VehicleDetailScreen
import com.wickwirez.myride.ui.DocumentsScreen
import com.wickwirez.myride.ui.VehicleDetailViewModel
import com.wickwirez.myride.ui.VehicleSpecsScreen
import com.wickwirez.myride.ui.VinScannerScreen
import com.wickwirez.myride.ui.theme.MyRideTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { false }
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val repository = (application as MyRideApplication).repository

        setContent {
            MyRideTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MyRideNavHost(repository)
                }
            }
        }
    }
}

@Composable
private fun MyRideNavHost(repository: VehicleRepository) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = tween(280)) +
                fadeIn(animationSpec = tween(280))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(280)) +
                fadeOut(animationSpec = tween(280))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(280)) +
                fadeIn(animationSpec = tween(280))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = tween(280)) +
                fadeOut(animationSpec = tween(280))
        }
    ) {
        composable("splash") {
            val context = LocalContext.current
            SplashScreen(
                onFinished = {
                    val destination = if (OnboardingPrefs.hasSeenOnboarding(context)) "garage" else "onboarding"
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            val context = LocalContext.current
            OnboardingScreen(
                onFinished = {
                    OnboardingPrefs.setSeenOnboarding(context)
                    navController.navigate("garage") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding_replay") {
            OnboardingScreen(
                onFinished = { navController.popBackStack() }
            )
        }

        composable("garage") {
            val viewModel: GarageViewModel =
                viewModel(factory = GarageViewModel.factory(repository))
            val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()

            GarageScreen(
                vehicles = vehicles,
                onAddVehicle = { navController.navigate("add_vehicle") },
                onVehicleClick = { vehicle ->
                    navController.navigate("vehicle_detail/${vehicle.id}")
                },
                onDeleteVehicle = { vehicle -> viewModel.deleteVehicle(vehicle) },
                onEditVehicle = { vehicle ->
                    navController.navigate("edit_vehicle/${vehicle.id}")
                },
                onOpenSettings = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            SettingsScreen(
                repository = repository,
                onOpenHelp = { navController.navigate("help") },
                onReplayOnboarding = { navController.navigate("onboarding_replay") },
                onOpenAbout = { navController.navigate("about") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("help") {
            HelpScreen(
                onReplayWelcomeTour = {
                    navController.navigate("onboarding_replay")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable("add_vehicle") {
            val viewModel: AddVehicleViewModel =
                viewModel(factory = AddVehicleViewModel.factory(repository))
            val scannedVin by navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getStateFlow<String?>("scanned_vin", null)
                ?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }

            AddVehicleScreen(
                scannedVin = scannedVin,
                onScanVinRequest = { navController.navigate("vin_scanner") },
                onScannedVinConsumed = {
                    navController.currentBackStackEntry?.savedStateHandle?.set<String?>("scanned_vin", null)
                },
                onSave = { vehicle ->
                    viewModel.saveVehicle(vehicle) {
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("vin_scanner") {
            VinScannerScreen(
                onVinFound = { vin ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scanned_vin", vin)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = "vehicle_detail/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val viewModel: VehicleDetailViewModel =
                viewModel(factory = VehicleDetailViewModel.factory(repository, vehicleId))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            VehicleDetailScreen(
                vehicle = uiState.vehicle,
                records = uiState.records,
                totalCost = uiState.totalCost,
                dueStatus = uiState.dueStatus,
                onAddService = { navController.navigate("add_service_record/$vehicleId") },
                onRecordClick = { record ->
                    navController.navigate("edit_service_record/${record.id}")
                },
                onDuplicateRecord = { record ->
                    viewModel.duplicateRecord(record, uiState.vehicle?.currentMileage ?: record.mileage)
                },
                onOpenAssistant = { navController.navigate("ai_assistant/$vehicleId") },
                onOpenRecalls = { navController.navigate("recalls/$vehicleId") },
                onOpenFuelLog = { navController.navigate("fuel_log/$vehicleId") },
                onOpenSpecs = { navController.navigate("vehicle_specs/$vehicleId") },
                onOpenDocuments = { navController.navigate("documents/$vehicleId") },
                onOpenAiMechanic = { navController.navigate("ai_mechanic/$vehicleId") },
                onOpenPhotoServiceLog = { navController.navigate("photo_service_record/$vehicleId") },
                onMarkParkedSpot = { lat, lng, timestamp ->
                    viewModel.markParkedLocation(lat, lng, timestamp)
                },
                onClearParkedSpot = { viewModel.clearParkedLocation() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "documents/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            DocumentsScreen(
                vehicleId = vehicleId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "vehicle_specs/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val viewModel: VehicleDetailViewModel =
                viewModel(factory = VehicleDetailViewModel.factory(repository, vehicleId))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val coroutineScope = rememberCoroutineScope()

            VehicleSpecsScreen(
                vehicle = uiState.vehicle,
                onSave = { updated ->
                    coroutineScope.launch { repository.updateVehicle(updated) }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "fuel_log/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val viewModel: FuelLogViewModel =
                viewModel(factory = FuelLogViewModel.factory(repository, vehicleId))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            FuelLogScreen(
                uiState = uiState,
                onAddLog = { navController.navigate("add_fuel_log/$vehicleId") },
                onLogClick = { entry -> navController.navigate("edit_fuel_log/${entry.log.id}") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "add_fuel_log/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val viewModel: AddFuelLogViewModel =
                viewModel(factory = AddFuelLogViewModel.factory(repository))
            val detailViewModel: FuelLogViewModel =
                viewModel(factory = FuelLogViewModel.factory(repository, vehicleId))
            val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()

            val fuelLogContext = LocalContext.current

            AddFuelLogScreen(
                vehicleId = vehicleId,
                currentMileage = uiState.vehicle?.currentMileage ?: 0,
                onSave = { log ->
                    viewModel.saveLog(log) {
                        MascotVoice.play(fuelLogContext, MascotVoice.Clip.FILLUP_LOGGED)
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_fuel_log/{logId}",
            arguments = listOf(navArgument("logId") { type = NavType.LongType })
        ) { backStackEntry ->
            val logId = backStackEntry.arguments?.getLong("logId") ?: return@composable
            val viewModel: EditFuelLogViewModel =
                viewModel(factory = EditFuelLogViewModel.factory(repository, logId))
            val log by viewModel.log.collectAsStateWithLifecycle()

            EditFuelLogScreen(
                log = log,
                onSave = { updated ->
                    viewModel.saveLog(updated) {
                        navController.popBackStack()
                    }
                },
                onDelete = {
                    log?.let {
                        viewModel.deleteLog(it) {
                            navController.popBackStack()
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "recalls/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val viewModel: VehicleDetailViewModel =
                viewModel(factory = VehicleDetailViewModel.factory(repository, vehicleId))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            RecallScreen(
                vehicle = uiState.vehicle,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "ai_assistant/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val viewModel: VehicleDetailViewModel =
                viewModel(factory = VehicleDetailViewModel.factory(repository, vehicleId))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AiAssistantScreen(
                vehicle = uiState.vehicle,
                records = uiState.records,
                onOpenSettings = { navController.navigate("settings") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "ai_mechanic/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val viewModel: VehicleDetailViewModel =
                viewModel(factory = VehicleDetailViewModel.factory(repository, vehicleId))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AiMechanicScreen(
                vehicle = uiState.vehicle,
                records = uiState.records,
                onOpenSettings = { navController.navigate("settings") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "photo_service_record/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val viewModel: AddServiceRecordViewModel =
                viewModel(factory = AddServiceRecordViewModel.factory(repository))
            val detailViewModel: VehicleDetailViewModel =
                viewModel(factory = VehicleDetailViewModel.factory(repository, vehicleId))
            val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            PhotoServiceRecordScreen(
                vehicleId = vehicleId,
                currentMileage = uiState.vehicle?.currentMileage ?: 0,
                onOpenSettings = { navController.navigate("settings") },
                onSave = { record ->
                    viewModel.saveRecord(record) {
                        ReviewPromptManager.recordSuccessfulAction(context)
                        MascotVoice.play(context, MascotVoice.Clip.SERVICE_LOGGED)
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "add_service_record/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val viewModel: AddServiceRecordViewModel =
                viewModel(factory = AddServiceRecordViewModel.factory(repository))
            val detailViewModel: VehicleDetailViewModel =
                viewModel(factory = VehicleDetailViewModel.factory(repository, vehicleId))
            val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            AddServiceRecordScreen(
                vehicleId = vehicleId,
                currentMileage = uiState.vehicle?.currentMileage ?: 0,
                onSave = { record ->
                    viewModel.saveRecord(record) {
                        ReviewPromptManager.recordSuccessfulAction(context)
                        MascotVoice.play(context, MascotVoice.Clip.SERVICE_LOGGED)
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_service_record/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId") ?: return@composable
            val viewModel: EditServiceRecordViewModel =
                viewModel(factory = EditServiceRecordViewModel.factory(repository, recordId))
            val record by viewModel.record.collectAsStateWithLifecycle()

            EditServiceRecordScreen(
                record = record,
                onSave = { updated ->
                    viewModel.saveRecord(updated) {
                        navController.popBackStack()
                    }
                },
                onDelete = {
                    record?.let {
                        viewModel.deleteRecord(it) {
                            navController.popBackStack()
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_vehicle/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
            val viewModel: EditVehicleViewModel =
                viewModel(factory = EditVehicleViewModel.factory(repository, vehicleId))
            val vehicle by viewModel.vehicle.collectAsStateWithLifecycle()
            val scannedVin by backStackEntry.savedStateHandle
                .getStateFlow<String?>("scanned_vin", null)
                .collectAsStateWithLifecycle()

            EditVehicleScreen(
                vehicle = vehicle,
                scannedVin = scannedVin,
                onScanVinRequest = { navController.navigate("vin_scanner") },
                onScannedVinConsumed = {
                    backStackEntry.savedStateHandle.set<String?>("scanned_vin", null)
                },
                onSave = { updated ->
                    viewModel.saveVehicle(updated) {
                        navController.popBackStack()
                    }
                },
                onDelete = {
                    vehicle?.let {
                        viewModel.deleteVehicle(it) {
                            navController.popBackStack()
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
