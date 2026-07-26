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

data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val records: List<ServiceRecord> = emptyList(),
    val totalCost: Double = 0.0,
    val dueStatus: DueStatus = DueStatus.NONE
)

class VehicleDetailViewModel(
    private val repository: VehicleRepository,
    private val vehicleId: Long
) : ViewModel() {

    val uiState: StateFlow<VehicleDetailUiState> = combine(
        repository.getVehicleById(vehicleId),
        repository.getRecordsForVehicle(vehicleId),
        repository.getTotalCostForVehicle(vehicleId)
    ) { vehicle, records, totalCost ->
        val reminderRecords = records.filter {
            it.reminderIntervalMiles != null || it.reminderIntervalDays != null
        }
        val dueStatus = if (vehicle != null) {
            computeWorstDueStatus(vehicle, reminderRecords, System.currentTimeMillis())
        } else {
            DueStatus.NONE
        }
        VehicleDetailUiState(vehicle, records, totalCost, dueStatus)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VehicleDetailUiState())

    fun deleteRecord(record: ServiceRecord) {
        viewModelScope.launch { repository.deleteServiceRecord(record) }
    }

    fun duplicateRecord(record: ServiceRecord, currentMileage: Int) {
        viewModelScope.launch {
            val copy = record.copy(
                id = 0L,
                date = System.currentTimeMillis(),
                mileage = currentMileage
            )
            repository.addServiceRecord(copy)
        }
    }

    companion object {
        fun factory(repository: VehicleRepository, vehicleId: Long) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                VehicleDetailViewModel(repository, vehicleId) as T
        }
    }
}
