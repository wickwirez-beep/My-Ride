package com.wickwirez.myride.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.model.FuelLog
import kotlinx.coroutines.launch

class AddFuelLogViewModel(private val repository: VehicleRepository) : ViewModel() {

    fun saveLog(log: FuelLog, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.addFuelLog(log)
            onSaved()
        }
    }

    companion object {
        fun factory(repository: VehicleRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                AddFuelLogViewModel(repository) as T
        }
    }
}
