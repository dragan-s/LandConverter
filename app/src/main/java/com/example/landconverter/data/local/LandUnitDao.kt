package com.example.landconverter.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LandUnitDao {

    @Query("SELECT * FROM land_units WHERE countryCode = :countryCode")
    fun observeUnitsForCountry(countryCode: String): Flow<List<LandUnitEntity>>

    @Query("SELECT * FROM land_units WHERE countryCode = :countryCode")
    suspend fun getUnitsForCountry(countryCode: String): List<LandUnitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: LandUnitEntity): Long

    @Update
    suspend fun updateUnit(unit: LandUnitEntity)

    @Query("DELETE FROM land_units WHERE id = :id AND isSystem = 0")
    suspend fun deleteNonSystemUnit(id: Long): Int

    @Query("SELECT COUNT(*) FROM land_units")
    suspend fun getCount(): Int
}
