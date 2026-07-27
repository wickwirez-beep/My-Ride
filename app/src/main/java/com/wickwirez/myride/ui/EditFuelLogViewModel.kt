package com.wickwirez.myride.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.model.FuelLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditFuelLogViewModel(
    private val repository: VehicleRepository,
    logId: Long
) : ViewModel() {

    val log: StateFlow<FuelLog?> = repository.getFuelLogById(logId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveLog(log: FuelLog, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.updateFuelLog(log)
            onSaved()
        }
    }

    fun deleteLog(log: FuelLog, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteFuelLog(log)
            onDeleted()
        }
    }

    companion object {
        fun factory(repository: VehicleRepository, logId: Long) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                EditFuelLogViewModel(repository, logId) as T
        }
    }
}
