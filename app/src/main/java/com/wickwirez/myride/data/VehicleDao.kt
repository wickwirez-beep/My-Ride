package com.wickwirez.myride.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicles ORDER BY nickname, make, model")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles")
    suspend fun getAllVehiclesOnce(): List<Vehicle>

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId")
    fun getVehicleById(vehicleId: Long): Flow<Vehicle?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)

    @Query("UPDATE vehicles SET currentMileage = :mileage WHERE id = :vehicleId")
    suspend fun updateMileage(vehicleId: Long, mileage: Int)
}
