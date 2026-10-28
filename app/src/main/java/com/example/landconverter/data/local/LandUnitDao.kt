package com.example.landconverter.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LandUnitDao {

    @Query("SELECT * FROM land_units WHERE countryCode = :countryCode")
    fun observeUnitsForCountry(countryCode: String): Flow<List<LandUnitEntity>>

    @Query("SELECT * FROM land_units WHERE countryCode = :countryCode")
    suspend fun getUnitsForCountry(countryCode: String): List<LandUnitEntity>

    @Query("""
    SELECT * FROM land_units
    WHERE countryCode = :countryCode
      AND unitId = :unitId
    LIMIT 1
    """)
    suspend fun findByCountryAndUnitId(
        countryCode: String,
        unitId: String
    ): LandUnitEntity?


    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUnit(unit: LandUnitEntity): Long

    @Query("""
    UPDATE land_units
    SET displayName = :displayName,
        valueInSqMeters = :valueInSqMeters
    WHERE id = :id
      AND sourceType = 'USER_DEFINED'
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
