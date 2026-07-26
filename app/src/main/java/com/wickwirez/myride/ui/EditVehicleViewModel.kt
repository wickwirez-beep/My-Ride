package com.wickwirez.myride.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditVehicleViewModel(
    private val repository: VehicleRepository,
    vehicleId: Long
) : ViewModel() {

    val vehicle: StateFlow<Vehicle?> = repository.getVehicleById(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveVehicle(vehicle: Vehicle, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
            onSaved()
        }
    }

    fun deleteVehicle(vehicle: Vehicle, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
            onDeleted()
        }
    }

    companion object {
        fun factory(repository: VehicleRepository, vehicleId: Long) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                EditVehicleViewModel(repository, vehicleId) as T
        }
    }
}
