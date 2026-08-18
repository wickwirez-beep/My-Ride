package com.wickwirez.myride.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wickwirez.myride.model.FuelLog
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.Vehicle
import com.wickwirez.myride.model.VehicleDocument
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE fuel_logs ADD COLUMN fuelType TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [Vehicle::class, ServiceRecord::class, FuelLog::class, VehicleDocument::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun fuelLogDao(): FuelLogDao
    abstract fun vehicleDocumentDao(): VehicleDocumentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myride.db"
                )
                    .addMigrations(MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
