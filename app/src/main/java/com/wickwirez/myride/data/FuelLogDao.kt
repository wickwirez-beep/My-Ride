package com.wickwirez.myride.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wickwirez.myride.model.FuelLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {

    @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId ORDER BY mileage DESC")
    fun getLogsForVehicle(vehicleId: Long): Flow<List<FuelLog>>

    @Query("SELECT * FROM fuel_logs WHERE id = :logId")
    fun getLogById(logId: Long): Flow<FuelLog?>

    @Query("SELECT * FROM fuel_logs")
    suspend fun getAllLogsOnce(): List<FuelLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FuelLog): Long

    @Update
    suspend fun updateLog(log: FuelLog)

    @Delete
    suspend fun deleteLog(log: FuelLog)
}
