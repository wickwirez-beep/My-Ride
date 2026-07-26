package com.wickwirez.myride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.ui.AddServiceRecordScreen
import com.wickwirez.myride.ui.AddServiceRecordViewModel
import com.wickwirez.myride.ui.AddVehicleScreen
import com.wickwirez.myride.ui.AddVehicleViewModel
import com.wickwirez.myride.ui.EditServiceRecordScreen
import com.wickwirez.myride.ui.EditServiceRecordViewModel
import com.wickwirez.myride.ui.EditVehicleScreen
import com.wickwirez.myride.ui.EditVehicleViewModel
import com.wickwirez.myride.ui.GarageScreen
import com.wickwirez.myride.ui.GarageViewModel
import com.wickwirez.myride.ui.VehicleDetailScreen
import com.wickwirez.myride.ui.VehicleDetailViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as MyRideApplication).repository

        setContent {
            MaterialTheme {
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

    NavHost(navController = navController, startDestination = "garage") {

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
                }
            )
        }

        composable("add_vehicle") {
            val viewModel: AddVehicleViewModel =
                viewModel(factory = AddVehicleViewModel.factory(repository))

            AddVehicleScreen(
                onSave = { vehicle ->
                    viewModel.saveVehicle(vehicle) {
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() }
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
                onAddService = { navController.navigate("add_service_record/$vehicleId") },
                onRecordClick = { record ->
                    navController.navigate("edit_service_record/${record.id}")
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

            AddServiceRecordScreen(
                vehicleId = vehicleId,
                currentMileage = uiState.vehicle?.currentMileage ?: 0,
                onSave = { record ->
                    viewModel.saveRecord(record) {
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

            EditVehicleScreen(
                vehicle = vehicle,
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
