package com.wickwirez.myride.data

import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(
    private val vehicleDao: VehicleDao,
    private val serviceRecordDao: ServiceRecordDao
) {
    fun getAllVehicles(): Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    suspend fun getAllVehiclesOnce(): List<Vehicle> = vehicleDao.getAllVehiclesOnce()

    fun getVehicleById(id: Long): Flow<Vehicle?> = vehicleDao.getVehicleById(id)

    suspend fun addVehicle(vehicle: Vehicle): Long = vehicleDao.insertVehicle(vehicle)

    suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.updateVehicle(vehicle)

    suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.deleteVehicle(vehicle)

    suspend fun updateMileage(vehicleId: Long, mileage: Int) =
        vehicleDao.updateMileage(vehicleId, mileage)

    fun getRecordsForVehicle(vehicleId: Long): Flow<List<ServiceRecord>> =
        serviceRecordDao.getRecordsForVehicle(vehicleId)

    suspend fun getAllRecordsOnce(): List<ServiceRecord> = serviceRecordDao.getAllRecordsOnce()

    fun getRecordById(recordId: Long): Flow<ServiceRecord?> =
        serviceRecordDao.getRecordById(recordId)

    fun getTotalCostForVehicle(vehicleId: Long): Flow<Double> =
        serviceRecordDao.getTotalCostForVehicle(vehicleId)

    fun getReminderRecords(): Flow<List<ServiceRecord>> =
        serviceRecordDao.getReminderRecords()

    suspend fun addServiceRecord(record: ServiceRecord): Long =
        serviceRecordDao.insertRecord(record)

    suspend fun updateServiceRecord(record: ServiceRecord) =
        serviceRecordDao.updateRecord(record)

    suspend fun deleteServiceRecord(record: ServiceRecord) =
        serviceRecordDao.deleteRecord(record)
}
