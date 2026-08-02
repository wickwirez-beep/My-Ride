package com.wickwirez.myride.data

import com.wickwirez.myride.model.FuelLog
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.ServiceType
import com.wickwirez.myride.model.Vehicle
import org.json.JSONArray
import org.json.JSONObject
import android.net.Uri
import android.util.Base64
import java.io.File
import android.content.Context

object BackupManager {

    private fun encodePhotoBase64(photoUri: String?): String? {
        if (photoUri == null) return null
        return try {
            val path = Uri.parse(photoUri).path ?: return null
            val file = File(path)
            if (!file.exists()) return null
            Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun decodePhotoAndSave(context: Context, base64: String?): String? {
        if (base64.isNullOrBlank()) return null
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
            val destFile = File(photosDir, "${java.util.UUID.randomUUID()}.jpg")
            destFile.writeBytes(bytes)
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            null
        }
    }


    data class BackupData(
        val vehicles: List<Vehicle>,
        val records: List<ServiceRecord>,
        val fuelLogs: List<FuelLog>,
        val apiKey: String?
    )

    fun buildBackupJson(
        vehicles: List<Vehicle>,
        records: List<ServiceRecord>,
        fuelLogs: List<FuelLog>,
        apiKey: String?
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
                    put("photoBase64", encodePhotoBase64(v.photoUri) ?: JSONObject.NULL)
                    put("oilType", v.oilType)
                    put("oilCapacity", v.oilCapacity)
                    put("oilFilterBrand", v.oilFilterBrand)
                    put("oilFilterPartNumber", v.oilFilterPartNumber)
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
            put("version", 3)
            put("vehicles", vehiclesJson)
            put("serviceRecords", recordsJson)
            put("fuelLogs", fuelLogsJson)
            put("apiKey", apiKey ?: JSONObject.NULL)
        }

        return root.toString(2)
    }

    fun parseBackupJson(context: Context, json: String): BackupData? {
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
                    photoUri = decodePhotoAndSave(context, if (o.has("photoBase64") && !o.isNull("photoBase64")) o.getString("photoBase64") else null) ?: (if (o.isNull("photoUri")) null else o.getString("photoUri")),
                    oilType = o.optString("oilType", ""),
                    oilCapacity = o.optString("oilCapacity", ""),
                    oilFilterBrand = o.optString("oilFilterBrand", ""),
                    oilFilterPartNumber = o.optString("oilFilterPartNumber", ""),
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

            val apiKey = if (root.has("apiKey") && !root.isNull("apiKey")) root.getString("apiKey") else null
            BackupData(vehicles, records, fuelLogs, apiKey)
        } catch (e: Exception) {
            null
        }
    }
}
