package com.wickwirez.myride.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Categories of maintenance work. Stored as its string name in the DB
 * (see [com.wickwirez.myride.data.Converters]), so reordering is safe but
 * renaming an entry will orphan any rows already saved under the old name.
 */
enum class ServiceType {
    OIL_CHANGE,
    TIRE_ROTATION,
    TIRE_REPLACEMENT,
    BRAKES,
    BATTERY,
    INSPECTION,
    REGISTRATION,
    FLUID_CHANGE,
    FILTER_CHANGE,
    REPAIR,
    OTHER
}

@Entity(
    tableName = "service_records",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehicleId")]
)
data class ServiceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val vehicleId: Long,
    val type: ServiceType,
    val date: Long,
    val mileage: Int,
    val cost: Double = 0.0,
    val shopName: String = "",
    val notes: String = "",
    // Mileage/day interval to the next occurrence of this service, if the
    // user wants a reminder. Null means "one-off, no reminder."
    val reminderIntervalMiles: Int? = null,
    val reminderIntervalDays: Int? = null
)
