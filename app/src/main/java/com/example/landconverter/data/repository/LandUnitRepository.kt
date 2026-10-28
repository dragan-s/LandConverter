package com.example.landconverter.data.repository

import com.example.landconverter.data.local.LandUnitDao
import com.example.landconverter.data.local.LandUnitEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SaveUnitResult {
    data object Saved : SaveUnitResult
    /** Isti unitId već postoji u toj državi. */
    data object Duplicate : SaveUnitResult
    /** Mera sa istim nazivom već postoji u toj državi. */
    data object DuplicateName : SaveUnitResult
    data object Invalid : SaveUnitResult
}

sealed interface UpdateUnitResult {
    data object Updated : UpdateUnitResult
    data object DuplicateName : UpdateUnitResult
    data object Invalid : UpdateUnitResult
    /** Sistemska mera, ili mera koja je u međuvremenu obrisana. */
    data object NotEditable : UpdateUnitResult
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
        val normalizedDisplayName = normalizeName(displayName)

        if (
            normalizedCountryCode.isBlank() ||
            normalizedUnitId.isBlank() ||
            normalizedDisplayName.isBlank() ||
            !valueInSqMeters.isFinite() ||
            valueInSqMeters <= 0.0
        ) {
            return SaveUnitResult.Invalid
        }

        if (nameExists(normalizedCountryCode, normalizedDisplayName, excludeId = null)) {
            return SaveUnitResult.DuplicateName
        }

        if (landUnitDao.findByCountryAndUnitId(normalizedCountryCode, normalizedUnitId) != null) {
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

    suspend fun updateUserUnit(entity: LandUnitEntity): UpdateUnitResult {
        if (entity.sourceType != "USER_DEFINED" || entity.isSystem) {
            return UpdateUnitResult.NotEditable
        }

        val name = normalizeName(entity.displayName)
        if (
            name.isBlank() ||
            !entity.valueInSqMeters.isFinite() ||
            entity.valueInSqMeters <= 0.0
        ) {
            return UpdateUnitResult.Invalid
        }

        if (nameExists(entity.countryCode, name, excludeId = entity.id)) {
            return UpdateUnitResult.DuplicateName
        }

        val updatedRows = landUnitDao.updateUserUnit(
            id = entity.id,
            displayName = name,
            valueInSqMeters = entity.valueInSqMeters
        )
        return if (updatedRows > 0) UpdateUnitResult.Updated else UpdateUnitResult.NotEditable
    }

    suspend fun deleteUnit(id: Long) {
        landUnitDao.deleteNonSystemUnit(id)
    }

    /** Poredi se u Kotlinu jer SQLite LOWER() ne radi sa ćirilicom i slovima č, ć, š, ž, đ. */
    private suspend fun nameExists(countryCode: String, name: String, excludeId: Long?): Boolean =
        landUnitDao.getUnitsForCountry(countryCode).any { existing ->
            existing.id != excludeId &&
                    normalizeName(existing.displayName).equals(name, ignoreCase = true)
        }

    private fun normalizeName(name: String): String =
        name.trim().replace(Regex("\\s+"), " ")
}