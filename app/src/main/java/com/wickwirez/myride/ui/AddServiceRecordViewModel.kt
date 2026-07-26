package com.wickwirez.myride.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.model.ServiceRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddServiceRecordViewModel(private val repository: VehicleRepository) : ViewModel() {

    fun saveRecord(record: ServiceRecord, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.addServiceRecord(record)

            // Keep the vehicle's odometer current if this entry reports higher mileage.
            val vehicle = repository.getVehicleById(record.vehicleId).first()
            if (vehicle != null && record.mileage > vehicle.currentMileage) {
                repository.updateMileage(record.vehicleId, record.mileage)
            }

            onSaved()
        }
    }

    companion object {
        fun factory(repository: VehicleRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                AddServiceRecordViewModel(repository) as T
        }
    }
}
