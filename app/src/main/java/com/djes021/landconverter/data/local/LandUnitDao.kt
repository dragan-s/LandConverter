package com.djes021.landconverter.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LandUnitDao {

    // ORDER BY id: redosled unosa (m², ar, ha, tradicionalne mere, pa korisničke).
    // Bez njega SQLite može da vrati redove po indeksu (countryCode, unitId), tj. po abecedi unitId.
    @Query("SELECT * FROM land_units WHERE countryCode = :countryCode ORDER BY id")
    fun observeUnitsForCountry(countryCode: String): Flow<List<LandUnitEntity>>

    @Query("SELECT * FROM land_units WHERE countryCode = :countryCode ORDER BY id")
    suspend fun getUnitsForCountry(countryCode: String): List<LandUnitEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUnit(unit: LandUnitEntity): Long

    @Query("""
    UPDATE land_units
    SET displayName = :displayName,
        valueInSqMeters = :valueInSqMeters
    WHERE id = :id
      AND isSystem = 0
    """)
    suspend fun updateUserUnit(
        id: Long,
        displayName: String,
        valueInSqMeters: Double
    ): Int

    @Query("DELETE FROM land_units WHERE id = :id AND isSystem = 0")
    suspend fun deleteNonSystemUnit(id: Long): Int
}