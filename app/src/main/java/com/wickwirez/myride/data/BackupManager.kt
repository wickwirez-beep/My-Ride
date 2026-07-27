package com.wickwirez.myride.data

import com.wickwirez.myride.model.FuelLog
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.ServiceType
import com.wickwirez.myride.model.Vehicle
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    data class BackupData(
        val vehicles: List<Vehicle>,
        val records: List<ServiceRecord>,
        val fuelLogs: List<FuelLog>
    )

    fun buildBackupJson(
        vehicles: List<Vehicle>,
        records: List<ServiceRecord>,
        fuelLogs: List<FuelLog>
    ): String {
        val vehiclesJson = JSONArray()
        vehicles.forEach { v ->
            vehiclesJson.put(
                JSONObject().apply {
                    put("id", v.id)
                    put("nickname", v.nickname)
                    put("year", v.year)
                    put("make", v.make)
                    put("model", v.model)
                    put("trim", v.trim)
                    put("vin", v.vin)
                    put("currentMileage", v.currentMileage)
                    put("photoUri", v.photoUri ?: JSONObject.NULL)
                    put("oilType", v.oilType)
                    put("oilCapacity", v.oilCapacity)
                    put("airFilterPartNumber", v.airFilterPartNumber)
                    put("sparkPlugType", v.sparkPlugType)
                    put("sparkPlugGap", v.sparkPlugGap)
                    put("specNotes", v.specNotes)
                }
            )
        }

        val recordsJson = JSONArray()
        records.forEach { r ->
            recordsJson.put(
                JSONObject().apply {
                    put("id", r.id)
                    put("vehicleId", r.vehicleId)
                    put("type", r.type.name)
                    put("date", r.date)
                    put("mileage", r.mileage)
                    put("cost", r.cost)
                    put("shopName", r.shopName)
                    put("notes", r.notes)
                    put("reminderIntervalMiles", r.reminderIntervalMiles ?: JSONObject.NULL)
                    put("reminderIntervalDays", r.reminderIntervalDays ?: JSONObject.NULL)
                }
            )
        }

        val fuelLogsJson = JSONArray()
        fuelLogs.forEach { f ->
            fuelLogsJson.put(
                JSONObject().apply {
                    put("id", f.id)
                    put("vehicleId", f.vehicleId)
                    put("date", f.date)
                    put("mileage", f.mileage)
                    put("gallons", f.gallons)
                    put("cost", f.cost)
                    put("notes", f.notes)
                }
            )
        }

        val root = JSONObject().apply {
            put("version", 2)
            put("vehicles", vehiclesJson)
            put("serviceRecords", recordsJson)
            put("fuelLogs", fuelLogsJson)
        }

        return root.toString(2)
    }

    fun parseBackupJson(json: String): BackupData? {
        return try {
            val root = JSONObject(json)
            val vehiclesJson = root.getJSONArray("vehicles")
            val recordsJson = root.getJSONArray("serviceRecords")
            val fuelLogsJson = root.optJSONArray("fuelLogs") ?: JSONArray()

            val vehicles = (0 until vehiclesJson.length()).map { i ->
                val o = vehiclesJson.getJSONObject(i)
                Vehicle(
                    id = o.getLong("id"),
                    nickname = o.optString("nickname", ""),
                    year = o.getInt("year"),
                    make = o.getString("make"),
                    model = o.getString("model"),
                    trim = o.optString("trim", ""),
                    vin = o.optString("vin", ""),
                    currentMileage = o.optInt("currentMileage", 0),
                    photoUri = if (o.isNull("photoUri")) null else o.getString("photoUri"),
                    oilType = o.optString("oilType", ""),
                    oilCapacity = o.optString("oilCapacity", ""),
                    airFilterPartNumber = o.optString("airFilterPartNumber", ""),
                    sparkPlugType = o.optString("sparkPlugType", ""),
                    sparkPlugGap = o.optString("sparkPlugGap", ""),
                    specNotes = o.optString("specNotes", "")
                )
            }

            val records = (0 until recordsJson.length()).map { i ->
                val o = recordsJson.getJSONObject(i)
                ServiceRecord(
                    id = o.getLong("id"),
                    vehicleId = o.getLong("vehicleId"),
                    type = ServiceType.valueOf(o.getString("type")),
                    date = o.getLong("date"),
                    mileage = o.getInt("mileage"),
                    cost = o.optDouble("cost", 0.0),
                    shopName = o.optString("shopName", ""),
                    notes = o.optString("notes", ""),
                    reminderIntervalMiles = if (o.isNull("reminderIntervalMiles")) {
                        null
                    } else {
                        o.getInt("reminderIntervalMiles")
                    },
                    reminderIntervalDays = if (o.isNull("reminderIntervalDays")) {
                        null
                    } else {
                        o.getInt("reminderIntervalDays")
                    }
                )
            }

            val fuelLogs = (0 until fuelLogsJson.length()).map { i ->
                val o = fuelLogsJson.getJSONObject(i)
                FuelLog(
                    id = o.getLong("id"),
                    vehicleId = o.getLong("vehicleId"),
                    date = o.getLong("date"),
                    mileage = o.getInt("mileage"),
                    gallons = o.optDouble("gallons", 0.0),
                    cost = o.optDouble("cost", 0.0),
                    notes = o.optString("notes", "")
                )
            }

            BackupData(vehicles, records, fuelLogs)
        } catch (e: Exception) {
            null
        }
    }
}
