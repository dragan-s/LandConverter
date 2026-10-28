package com.example.landconverter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LandUnitEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun landUnitDao(): LandUnitDao

    companion object {
        const val DATABASE_NAME = "land_converter_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE land_units
                    ADD COLUMN sourceType TEXT NOT NULL
                    DEFAULT 'USER_DEFINED'
                    """.trimIndent()
                )

                // Fiksne sistemske mere za sve podržane države.
                db.execSQL(
                    """
                    UPDATE land_units
                    SET sourceType = 'BUILT_IN'
                    WHERE unitId IN ('sqm', 'ar', 'ha')
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_land_units_countryCode_unitId
                    ON land_units (countryCode, unitId)
                    """.trimIndent()
                )
            }
        }
    }
}