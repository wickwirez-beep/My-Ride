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

// Checks every reminder-bearing record for a vehicle (not just the most
// recent one) and returns the single most urgent status across all of them.
fun computeWorstDueStatus(vehicle: Vehicle, records: List<ServiceRecord>, nowMs: Long): DueStatus {
    if (records.isEmpty()) return DueStatus.NONE

    var sawDueSoon = false
    var sawOk = false

    for (record in records) {
        when (computeDueStatus(vehicle, record, nowMs)) {
            DueStatus.OVERDUE -> return DueStatus.OVERDUE
            DueStatus.DUE_SOON -> sawDueSoon = true
            DueStatus.OK -> sawOk = true
            DueStatus.NONE -> {}
        }
    }

    return when {
        sawDueSoon -> DueStatus.DUE_SOON
        sawOk -> DueStatus.OK
        else -> DueStatus.NONE
    }
}

// Continuous 0-100 version of the same interval math used above: 100 = nothing
// due, drops toward 0 as the nearest mileage/date reminder approaches or passes.
fun computeHealthScore(vehicle: Vehicle, records: List<ServiceRecord>, nowMs: Long): Int {
    var worstFraction = 1.0

    records.forEach { record ->
        record.reminderIntervalMiles?.let { interval ->
            if (interval > 0) {
                val remaining = (record.mileage + interval) - vehicle.currentMileage
                val fraction = (remaining.toDouble() / interval).coerceIn(0.0, 1.0)
                worstFraction = minOf(worstFraction, fraction)
            }
        }
        record.reminderIntervalDays?.let { interval ->
            if (interval > 0) {
                val dueAtMs = record.date + interval * MS_PER_DAY
                val remainingDays = (dueAtMs - nowMs) / MS_PER_DAY
                val fraction = (remainingDays.toDouble() / interval).coerceIn(0.0, 1.0)
                worstFraction = minOf(worstFraction, fraction)
            }
        }
    }

    return (worstFraction * 100).toInt().coerceIn(0, 100)
}
