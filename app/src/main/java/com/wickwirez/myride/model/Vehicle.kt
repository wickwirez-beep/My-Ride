package com.wickwirez.myride.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nickname: String = "",
    val year: Int,
    val make: String,
    val model: String,
    val trim: String = "",
    val vin: String = "",
    val currentMileage: Int = 0,
    val photoUri: String? = null,
    val oilType: String = "",
    val oilCapacity: String = "",
    val airFilterPartNumber: String = "",
    val sparkPlugType: String = "",
    val sparkPlugGap: String = "",
    val specNotes: String = ""
)
