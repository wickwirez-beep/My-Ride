package com.wickwirez.myride.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.launch

class AddVehicleViewModel(private val repository: VehicleRepository) : ViewModel() {

    fun saveVehicle(vehicle: Vehicle, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.addVehicle(vehicle)
            onSaved()
        }
    }

    companion object {
        fun factory(repository: VehicleRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                AddVehicleViewModel(repository) as T
        }
    }
}
