package com.wickwirez.myride.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wickwirez.myride.model.ServiceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRecordDao {

    @Query("SELECT * FROM service_records WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getRecordsForVehicle(vehicleId: Long): Flow<List<ServiceRecord>>

    @Query("SELECT * FROM service_records")
    suspend fun getAllRecordsOnce(): List<ServiceRecord>

    @Query("SELECT * FROM service_records WHERE id = :recordId")
    fun getRecordById(recordId: Long): Flow<ServiceRecord?>

    @Query("SELECT COALESCE(SUM(cost), 0.0) FROM service_records WHERE vehicleId = :vehicleId")
    fun getTotalCostForVehicle(vehicleId: Long): Flow<Double>

    @Query(
        "SELECT * FROM service_records WHERE reminderIntervalMiles IS NOT NULL OR reminderIntervalDays IS NOT NULL"
    )
    fun getReminderRecords(): Flow<List<ServiceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ServiceRecord): Long

    @Update
    suspend fun updateRecord(record: ServiceRecord)

    @Delete
    suspend fun deleteRecord(record: ServiceRecord)
}
