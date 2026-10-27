package com.example.landconverter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val DULUM_SQM = 1000.0
private const val MOTIKA_SQM = 800.0

@Database(entities = [LandUnitEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun landUnitDao(): LandUnitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "land_converter_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.landUnitDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: LandUnitDao) {
            val countries = listOf("ba", "rs", "me", "mk", "hr")

            for (code in countries) {
                // 1. System units (common for all countries)
                dao.insertUnit(LandUnitEntity(countryCode = code, unitId = "sqm", displayName = getSystemUnitName("sqm", code), valueInSqMeters = 1.0, isSystem = true))
                dao.insertUnit(LandUnitEntity(countryCode = code, unitId = "ar", displayName = getSystemUnitName("ar", code), valueInSqMeters = 100.0, isSystem = true))
                dao.insertUnit(LandUnitEntity(countryCode = code, unitId = "ha", displayName = getSystemUnitName("ha", code), valueInSqMeters = 10000.0, isSystem = true))

                // 2. Country-specific default units
                val defaultUnits = getDefaultUnitsForCountry(code)
                for (unit in defaultUnits) {
                    dao.insertUnit(unit)
                }
            }
        }

        private fun getSystemUnitName(unitId: String, countryCode: String): String {
            return when (unitId) {
                "sqm" -> if (countryCode == "hr") "Četvorni metar (m²)" else "Kvadratni metar (m²)"
                "ar" -> "Ar (a)"
                "ha" -> "Hektar (ha)"
                else -> unitId
            }
        }

        private fun getDefaultUnitsForCountry(countryCode: String): List<LandUnitEntity> {
            return when (countryCode) {
                "ba" -> listOf(
                    LandUnitEntity(countryCode = "ba", unitId = "dulum", displayName = "Dulum", valueInSqMeters = DULUM_SQM, isSystem = false),
                    LandUnitEntity(countryCode = "ba", unitId = "motika", displayName = "Motika", valueInSqMeters = MOTIKA_SQM, isSystem = false),
                    LandUnitEntity(countryCode = "ba", unitId = "lanac", displayName = "Lanac", valueInSqMeters = 7193.3, isSystem = false),
                    LandUnitEntity(countryCode = "ba", unitId = "jutro_ral", displayName = "Jutro (ral)", valueInSqMeters = 5754.64, isSystem = false)
                )
                "rs" -> listOf(
                    LandUnitEntity(countryCode = "rs", unitId = "dulum", displayName = "Dulum", valueInSqMeters = DULUM_SQM, isSystem = false),
                    LandUnitEntity(countryCode = "rs", unitId = "motika", displayName = "Motika", valueInSqMeters = MOTIKA_SQM, isSystem = false),
                    LandUnitEntity(countryCode = "rs", unitId = "ralo", displayName = "Ralo", valueInSqMeters = 2878.0, isSystem = false),
                    LandUnitEntity(countryCode = "rs", unitId = "ralo_dan_oranja", displayName = "Ralo (dan oranja)", valueInSqMeters = 4000.0, isSystem = false),
                    LandUnitEntity(countryCode = "rs", unitId = "lanac", displayName = "Lanac", valueInSqMeters = 7192.0, isSystem = false),
                    LandUnitEntity(countryCode = "rs", unitId = "jutro_ral", displayName = "Jutro (ral)", valueInSqMeters = 5754.64, isSystem = false)
                )
                "me" -> listOf(
                    LandUnitEntity(countryCode = "me", unitId = "dulum", displayName = "Dulum", valueInSqMeters = DULUM_SQM, isSystem = false),
                    LandUnitEntity(countryCode = "me", unitId = "motika", displayName = "Motika", valueInSqMeters = MOTIKA_SQM, isSystem = false),
                    LandUnitEntity(countryCode = "me", unitId = "ralo", displayName = "Ralo", valueInSqMeters = 1820.0, isSystem = false),
                    LandUnitEntity(countryCode = "me", unitId = "lanac", displayName = "Lanac", valueInSqMeters = 7193.3, isSystem = false),
                    LandUnitEntity(countryCode = "me", unitId = "jutro_ral", displayName = "Jutro (ral)", valueInSqMeters = 5754.64, isSystem = false)
                )
                "mk" -> listOf(
                    LandUnitEntity(countryCode = "mk", unitId = "dulum", displayName = "Дулум", valueInSqMeters = DULUM_SQM, isSystem = false),
                    LandUnitEntity(countryCode = "mk", unitId = "motika", displayName = "Мотика", valueInSqMeters = MOTIKA_SQM, isSystem = false),
                    LandUnitEntity(countryCode = "mk", unitId = "ralo", displayName = "Рало", valueInSqMeters = 1600.0, isSystem = false),
                    LandUnitEntity(countryCode = "mk", unitId = "ralo_dan_oranja", displayName = "Рало (ден орање)", valueInSqMeters = 4000.0, isSystem = false)
                )
                "hr" -> listOf(
                    LandUnitEntity(countryCode = "hr", unitId = "dulum", displayName = "Dulum", valueInSqMeters = DULUM_SQM, isSystem = false),
                    LandUnitEntity(countryCode = "hr", unitId = "motika", displayName = "Motika", valueInSqMeters = MOTIKA_SQM, isSystem = false),
                    LandUnitEntity(countryCode = "hr", unitId = "jutro_ral", displayName = "Jutro (ral)", valueInSqMeters = 5754.64, isSystem = false)
                )
                else -> emptyList()
            }
        }
    }
}
