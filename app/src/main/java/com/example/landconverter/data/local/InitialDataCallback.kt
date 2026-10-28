package com.example.landconverter.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Puni bazu početnim merama.
 *
 * Radi se u `onOpen`, sinhrono i u jednoj transakciji, pa:
 *  - nijedan upit (ni Flow) ne može da pročita bazu pre nego što je popunjena,
 *  - prekid procesa usred upisa poništava celu transakciju, a sledeće otvaranje baze je ponavlja,
 *  - bazu koja je ranije ostala prazna popravlja sledeće pokretanje.
 *
 * Država se smatra popunjenom ako ima sistemsku meru "sqm" (ona se ne može obrisati),
 * pa obrisane tradicionalne mere ne bivaju vraćene pri svakom pokretanju.
 */
class InitialDataCallback : RoomDatabase.Callback() {

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        if (db.isReadOnly) return
        seedMissingCountries(db)
    }

    private fun seedMissingCountries(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            InitialUnits.byCountry.forEach { (countryCode, units) ->
                if (!isCountrySeeded(db, countryCode)) {
                    units.forEach { unit ->
                        db.insert(TABLE, SQLiteDatabase.CONFLICT_IGNORE, unit.toContentValues(countryCode))
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun isCountrySeeded(db: SupportSQLiteDatabase, countryCode: String): Boolean =
        db.query(
            "SELECT COUNT(*) FROM $TABLE WHERE countryCode = ? AND unitId = ?",
            arrayOf(countryCode, "sqm")
        ).use { cursor -> cursor.moveToFirst() && cursor.getInt(0) > 0 }

    private companion object {
        const val TABLE = "land_units"
    }
}