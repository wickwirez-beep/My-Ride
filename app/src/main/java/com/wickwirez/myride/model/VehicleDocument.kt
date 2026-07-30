package com.wickwirez.myride.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_documents")
data class VehicleDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val vehicleId: Long,
    val category: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val dateAdded: Long = System.currentTimeMillis()
)

object DocumentCategories {
    val all = listOf("Insurance", "Registration", "Title", "Warranty", "Other")
}
