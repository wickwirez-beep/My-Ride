package com.wickwirez.myride.work

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wickwirez.myride.MyRideApplication
import com.wickwirez.myride.ui.DueStatus
import com.wickwirez.myride.ui.computeDueStatus
import kotlinx.coroutines.flow.first

class MaintenanceCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as MyRideApplication).repository

        val vehicles = repository.getAllVehicles().first()
        val reminderRecords = repository.getLatestReminderRecords().first()
        val recordsByVehicle = reminderRecords.associateBy { it.vehicleId }
        val now = System.currentTimeMillis()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        vehicles.forEach { vehicle ->
            val status = computeDueStatus(vehicle, recordsByVehicle[vehicle.id], now)
            if (status == DueStatus.OVERDUE || status == DueStatus.DUE_SOON) {
                val title = if (status == DueStatus.OVERDUE) "Maintenance overdue" else "Maintenance due soon"
                val vehicleName = vehicle.nickname.ifBlank {
                    "${vehicle.year} ${vehicle.make} ${vehicle.model}"
                }

                val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(vehicleName)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(vehicle.id.toInt(), notification)
            }
        }

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "maintenance_reminders"
    }
}
