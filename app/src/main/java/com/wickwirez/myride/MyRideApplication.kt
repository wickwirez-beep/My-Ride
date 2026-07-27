package com.wickwirez.myride

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wickwirez.myride.data.AppDatabase
import com.wickwirez.myride.data.VehicleRepository
import com.wickwirez.myride.work.MaintenanceCheckWorker
import java.util.concurrent.TimeUnit

class MyRideApplication : Application() {

    private val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: VehicleRepository by lazy {
        VehicleRepository(database.vehicleDao(), database.serviceRecordDao(), database.fuelLogDao())
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleMaintenanceChecks()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MaintenanceCheckWorker.CHANNEL_ID,
                "Maintenance Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when a vehicle's maintenance is due soon or overdue"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun scheduleMaintenanceChecks() {
        val request = PeriodicWorkRequestBuilder<MaintenanceCheckWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "maintenance_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
