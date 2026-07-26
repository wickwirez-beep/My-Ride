package com.wickwirez.myride.ui

import android.content.Context
import android.content.Intent
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.Vehicle
import java.text.SimpleDateFormat
import java.util.Locale

object ShareHelper {

    fun shareVehicleSummary(context: Context, vehicle: Vehicle, records: List<ServiceRecord>) {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val vehicleName = vehicle.nickname.ifBlank { "${vehicle.year} ${vehicle.make} ${vehicle.model}" }

        val summary = buildString {
            appendLine("${vehicle.year} ${vehicle.make} ${vehicle.model} ${vehicle.trim}".trim())
            appendLine("Mileage: ${vehicle.currentMileage}")
            if (vehicle.vin.isNotBlank()) appendLine("VIN: ${vehicle.vin}")
            appendLine()
            if (records.isEmpty()) {
                appendLine("No service history logged yet.")
            } else {
                appendLine("Service History:")
                records.forEach { r ->
                    val costText = if (r.cost > 0) {
                        " (" + String.format(Locale.US, "$%.2f", r.cost) + ")"
                    } else {
                        ""
                    }
                    appendLine(
                        "- ${dateFormat.format(r.date)}: ${r.type.name.replace('_', ' ')} " +
                            "at ${r.mileage} mi$costText"
                    )
                }
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$vehicleName — Service History")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        context.startActivity(Intent.createChooser(intent, "Share vehicle summary"))
    }
}
