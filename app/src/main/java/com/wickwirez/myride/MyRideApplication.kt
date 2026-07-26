package com.wickwirez.myride

import android.app.Application
import com.wickwirez.myride.data.AppDatabase
import com.wickwirez.myride.data.VehicleRepository

class MyRideApplication : Application() {

    private val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: VehicleRepository by lazy {
        VehicleRepository(database.vehicleDao(), database.serviceRecordDao())
    }
}
