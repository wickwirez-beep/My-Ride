package com.wickwirez.myride.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.model.FuelLog
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// mpg is null for the earliest fill-up shown (no prior mileage to compare against).
data class FuelLogWithMpg(val log: FuelLog, val mpg: Double?)

data class FuelLogUiState(
    val vehicle: Vehicle? = null,
    val logs: List<FuelLogWithMpg> = emptyList(),
    val averageMpg: Double? = null
)

class FuelLogViewModel(
    private val repository: VehicleRepository,
    private val vehicleId: Long
) : ViewModel() {

    val uiState: StateFlow<FuelLogUiState> = combine(
        repository.getVehicleById(vehicleId),
        repository.getFuelLogsForVehicle(vehicleId)
    ) { vehicle, logs ->
        // DAO returns newest-mileage-first; walk oldest-first to compute MPG
        // between each fill-up and the one before it.
        val ascending = logs.sortedBy { it.mileage }
        val mpgByLogId = HashMap<Long, Double>()
        for (i in 1 until ascending.size) {
            val prev = ascending[i - 1]
            val curr = ascending[i]
            val milesDriven = curr.mileage - prev.mileage
            if (milesDriven > 0 && curr.gallons > 0) {
                mpgByLogId[curr.id] = milesDriven / curr.gallons
            }
        }

        val withMpg = logs.map { FuelLogWithMpg(it, mpgByLogId[it.id]) }
        val validMpgs = mpgByLogId.values
        val average = if (validMpgs.isNotEmpty()) validMpgs.average() else null

        FuelLogUiState(vehicle, withMpg, average)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FuelLogUiState())

    fun deleteLog(log: FuelLog) {
        viewModelScope.launch { repository.deleteFuelLog(log) }
    }

    companion object {
        fun factory(repository: VehicleRepository, vehicleId: Long) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                FuelLogViewModel(repository, vehicleId) as T
        }
    }
}
