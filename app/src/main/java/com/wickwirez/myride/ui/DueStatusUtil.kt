package com.wickwirez.myride.ui

import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.Vehicle

enum class DueStatus { NONE, OK, DUE_SOON, OVERDUE }

private const val MS_PER_DAY = 24L * 60 * 60 * 1000
private const val DUE_SOON_FRACTION = 0.1

fun computeDueStatus(vehicle: Vehicle, record: ServiceRecord?, nowMs: Long): DueStatus {
    if (record == null) return DueStatus.NONE

    var overdue = false
    var dueSoon = false

    record.reminderIntervalMiles?.let { interval ->
        if (interval > 0) {
            val remaining = (record.mileage + interval) - vehicle.currentMileage
            val threshold = (interval * DUE_SOON_FRACTION).coerceAtLeast(1.0)
            when {
                remaining <= 0 -> overdue = true
                remaining <= threshold -> dueSoon = true
            }
        }
    }

    record.reminderIntervalDays?.let { interval ->
        if (interval > 0) {
            val dueAtMs = record.date + interval * MS_PER_DAY
            val remainingDays = (dueAtMs - nowMs) / MS_PER_DAY
            val threshold = (interval * DUE_SOON_FRACTION).coerceAtLeast(1.0)
            when {
                remainingDays <= 0 -> overdue = true
                remainingDays <= threshold -> dueSoon = true
            }
        }
    }

    return when {
        overdue -> DueStatus.OVERDUE
        dueSoon -> DueStatus.DUE_SOON
        else -> DueStatus.OK
    }
}
