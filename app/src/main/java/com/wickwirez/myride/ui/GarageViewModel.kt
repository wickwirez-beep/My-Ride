package com.wickwirez.myride.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VehicleWithStatus(
    val vehicle: Vehicle,
    val dueStatus: DueStatus
)

class GarageViewModel(private val repository: VehicleRepository) : ViewModel() {

    val vehicles: StateFlow<List<VehicleWithStatus>> = combine(
        repository.getAllVehicles(),
        repository.getLatestReminderRecords()
    ) { vehicles, reminderRecords ->
        val recordsByVehicle = reminderRecords.associateBy { it.vehicleId }
        val now = System.currentTimeMillis()
        vehicles.map { vehicle ->
            VehicleWithStatus(vehicle, computeDueStatus(vehicle, recordsByVehicle[vehicle.id], now))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch { repository.deleteVehicle(vehicle) }
    }

    companion object {
        fun factory(repository: VehicleRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                GarageViewModel(repository) as T
        }
    }
}
