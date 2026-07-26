package com.wickwirez.myride.data

import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.ServiceType
import com.wickwirez.myride.model.Vehicle
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    data class BackupData(val vehicles: List<Vehicle>, val records: List<ServiceRecord>)

    fun buildBackupJson(vehicles: List<Vehicle>, records: List<ServiceRecord>): String {
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

        val root = JSONObject().apply {
            put("version", 1)
            put("vehicles", vehiclesJson)
            put("serviceRecords", recordsJson)
        }

        return root.toString(2)
    }

    fun parseBackupJson(json: String): BackupData? {
        return try {
            val root = JSONObject(json)
            val vehiclesJson = root.getJSONArray("vehicles")
            val recordsJson = root.getJSONArray("serviceRecords")

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
                    photoUri = if (o.isNull("photoUri")) null else o.getString("photoUri")
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

            BackupData(vehicles, records)
        } catch (e: Exception) {
            null
        }
    }
}
