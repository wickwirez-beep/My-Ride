package com.wickwirez.myride.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DueStatus { NONE, OK, DUE_SOON, OVERDUE }

data class VehicleWithStatus(
    val vehicle: Vehicle,
    val dueStatus: DueStatus
)

private const val DUE_SOON_MILES_WINDOW = 500
private const val DUE_SOON_DAYS_WINDOW = 14
private const val MS_PER_DAY = 24L * 60 * 60 * 1000

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

private fun computeDueStatus(vehicle: Vehicle, record: ServiceRecord?, nowMs: Long): DueStatus {
    if (record == null) return DueStatus.NONE

    var overdue = false
    var dueSoon = false

    record.reminderIntervalMiles?.let { interval ->
        val remaining = (record.mileage + interval) - vehicle.currentMileage
        when {
            remaining <= 0 -> overdue = true
            remaining <= DUE_SOON_MILES_WINDOW -> dueSoon = true
        }
    }

    record.reminderIntervalDays?.let { interval ->
        val dueAtMs = record.date + interval * MS_PER_DAY
        val remainingDays = (dueAtMs - nowMs) / MS_PER_DAY
        when {
            remainingDays <= 0 -> overdue = true
            remainingDays <= DUE_SOON_DAYS_WINDOW -> dueSoon = true
        }
    }

    return when {
        overdue -> DueStatus.OVERDUE
        dueSoon -> DueStatus.DUE_SOON
        else -> DueStatus.OK
    }
}
