package com.wickwirez.myride.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.model.ServiceRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditServiceRecordViewModel(
    private val repository: VehicleRepository,
    recordId: Long
) : ViewModel() {

    val record: StateFlow<ServiceRecord?> = repository.getRecordById(recordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveRecord(record: ServiceRecord, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.updateServiceRecord(record)
            onSaved()
        }
    }

    fun deleteRecord(record: ServiceRecord, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteServiceRecord(record)
            onDeleted()
        }
    }

    companion object {
        fun factory(repository: VehicleRepository, recordId: Long) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                EditServiceRecordViewModel(repository, recordId) as T
        }
    }
}
