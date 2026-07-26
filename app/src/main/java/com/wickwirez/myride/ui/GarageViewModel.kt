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

private const val MS_PER_DAY = 24L * 60 * 60 * 1000
private const val DUE_SOON_FRACTION = 0.1

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
        if (interval > 0) {
            val remaining = (record.mileage + interval) - vehicle.currentMileage
            val threshold = (interval * DUE_SOON_FRACTION).coerceAtLeast(1.0)
            when {
                remaining <= 0 -> overdue = true
                remaining <= threshold -> dueSoon = true
            }
        }
    }

    record.reminderIntervalDays?.let { interval ->
        if (interval > 0) {
            val dueAtMs = record.date + interval * MS_PER_DAY
            val remainingDays = (dueAtMs - nowMs) / MS_PER_DAY
            val threshold = (interval * DUE_SOON_FRACTION).coerceAtLeast(1.0)
            when {
                remainingDays <= 0 -> overdue = true
                remainingDays <= threshold -> dueSoon = true
            }
        }
    }

    return when {
        overdue -> DueStatus.OVERDUE
        dueSoon -> DueStatus.DUE_SOON
        else -> DueStatus.OK
    }
}
