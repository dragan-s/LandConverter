package com.example.landconverter.data.repository

import com.example.landconverter.data.local.LandUnitDao
import com.example.landconverter.data.local.LandUnitEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SaveUnitResult {
    data object Saved : SaveUnitResult
    data object Duplicate : SaveUnitResult
    data object Invalid : SaveUnitResult
}

@Singleton
class LandUnitRepository @Inject constructor(
    private val landUnitDao: LandUnitDao
) {
    fun observeUnitsForCountry(countryCode: String): Flow<List<LandUnitEntity>> {
        return landUnitDao.observeUnitsForCountry(countryCode)
    }

    suspend fun getUnitsForCountry(countryCode: String): List<LandUnitEntity> {
        return landUnitDao.getUnitsForCountry(countryCode)
    }

    suspend fun saveUserUnit(
        countryCode: String,
        unitId: String,
        displayName: String,
        valueInSqMeters: Double
    ): SaveUnitResult {
        val normalizedCountryCode = countryCode.trim().lowercase()
        val normalizedUnitId = unitId.trim().lowercase()
        val normalizedDisplayName = displayName.trim()

        if (
            normalizedCountryCode.isBlank() ||
            normalizedUnitId.isBlank() ||
            normalizedDisplayName.isBlank() ||
            !valueInSqMeters.isFinite() ||
            valueInSqMeters <= 0.0
        ) {
            return SaveUnitResult.Invalid
        }

        if (
            landUnitDao.findByCountryAndUnitId(
                normalizedCountryCode,
                normalizedUnitId
            ) != null
        ) {
            return SaveUnitResult.Duplicate
        }

        return try {
            landUnitDao.insertUnit(
                LandUnitEntity(
                    countryCode = normalizedCountryCode,
                    unitId = normalizedUnitId,
                    displayName = normalizedDisplayName,
                    valueInSqMeters = valueInSqMeters,
                    isSystem = false,
                    sourceType = "USER_DEFINED"
                )
            )
            SaveUnitResult.Saved
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // Pokriva i slučaj kada drugi upis nastane između provere i insert-a.
            SaveUnitResult.Duplicate
        }
    }

    suspend fun updateUserUnit(entity: LandUnitEntity) {
        if (
            entity.sourceType != "USER_DEFINED" ||
            entity.isSystem ||
            entity.displayName.isBlank() ||
            !entity.valueInSqMeters.isFinite() ||
            entity.valueInSqMeters <= 0.0
        ) {
            return
        }

        landUnitDao.updateUserUnit(
            id = entity.id,
            displayName = entity.displayName.trim(),
            valueInSqMeters = entity.valueInSqMeters
        )
    }

    suspend fun deleteUnit(id: Long) {
        landUnitDao.deleteNonSystemUnit(id)
    }
}
