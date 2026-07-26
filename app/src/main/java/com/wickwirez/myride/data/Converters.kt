package com.wickwirez.myride.data

import androidx.room.TypeConverter
import com.wickwirez.myride.model.ServiceType

class Converters {

    @TypeConverter
    fun fromServiceType(type: ServiceType): String = type.name

    @TypeConverter
    fun toServiceType(value: String): ServiceType =
        runCatching { ServiceType.valueOf(value) }.getOrDefault(ServiceType.OTHER)
}
