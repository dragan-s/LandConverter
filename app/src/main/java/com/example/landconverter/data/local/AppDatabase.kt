package com.example.landconverter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LandUnitEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun landUnitDao(): LandUnitDao

    companion object {
        const val DATABASE_NAME = "land_converter_database"
    }
}