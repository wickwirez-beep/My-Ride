package com.wickwirez.myride.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wickwirez.myride.model.VehicleDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDocumentDao {

    @Query("SELECT * FROM vehicle_documents WHERE vehicleId = :vehicleId ORDER BY category, dateAdded DESC")
    fun getDocumentsForVehicle(vehicleId: Long): Flow<List<VehicleDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: VehicleDocument): Long

    @Delete
    suspend fun deleteDocument(document: VehicleDocument)
}
