package com.example.landconverter.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.example.landconverter.data.local.AppDatabase
import com.example.landconverter.data.local.LandUnitDao
import com.example.landconverter.data.local.LandUnitEntity
import com.example.landconverter.data.local.toDomain
import com.example.landconverter.domain.model.LandUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class NewUnitSpec(
    val countryCode: String,
    val unitId: String,
    val displayName: String,
    val valueInSqMeters: Double
)

data class UpdateUnitSpec(
    val id: Long,
    val displayName: String,
    val valueInSqMeters: Double
)

sealed interface ApplyChangesResult {
    data object Success : ApplyChangesResult
    data object DuplicateName : ApplyChangesResult
    data object Invalid : ApplyChangesResult
    data object Error : ApplyChangesResult
}

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
    private val landUnitDao: LandUnitDao,
    private val appDatabase: AppDatabase
) {
    fun observeUnitsForCountry(countryCode: String): Flow<List<LandUnit>> {
        return landUnitDao.observeUnitsForCountry(countryCode).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Atomično primenjuje sve izmene (brisanja, dodavanja i ažuriranja) u jednoj bazičnoj transakciji.
     * Proverava konfilterisano KONAČNO stanje naziva pre zapisa.
     */
    suspend fun applyChanges(
        countryCode: String,
        deletions: Set<Long>,
        additions: List<NewUnitSpec>,
        updates: List<UpdateUnitSpec>
    ): ApplyChangesResult {
        val normalizedCountryCode = countryCode.trim().lowercase()

        // 1. Učitaj trenutne mere iz baze za državu
        val dbUnits = landUnitDao.getUnitsForCountry(normalizedCountryCode)
        val dbUnitsMap = dbUnits.associateBy { it.id }

        // 2. Validacija za obrisane i izmenjene mere
        for (id in deletions) {
            val unit = dbUnitsMap[id]
            if (unit != null && unit.isSystem) {
                return ApplyChangesResult.Invalid
            }
        }

        for (update in updates) {
            val unit = dbUnitsMap[update.id]
            if (unit == null || unit.isSystem) {
                return ApplyChangesResult.Invalid
            }
            if (normalizeName(update.displayName).isBlank() || !update.valueInSqMeters.isFinite() || update.valueInSqMeters <= 0.0) {
                return ApplyChangesResult.Invalid
            }
        }

        for (addition in additions) {
            if (
                normalizeName(addition.displayName).isBlank() ||
                addition.unitId.isBlank() ||
                !addition.valueInSqMeters.isFinite() ||
                addition.valueInSqMeters <= 0.0
            ) {
                return ApplyChangesResult.Invalid
            }
        }

        // 3. Proverava KONAČNO stanje naziva za državu nakon svih brisanja, izmena i dodavanja
        val updatesMap = updates.associateBy { it.id }
        val remainingUnits = dbUnits.filter { it.id !in deletions }

        val finalNames = remainingUnits.map { unit ->
            val update = updatesMap[unit.id]
            normalizeName(update?.displayName ?: unit.displayName)
        } + additions.map { normalizeName(it.displayName) }

        val lowerNames = finalNames.map { it.lowercase() }
        if (lowerNames.distinct().size != lowerNames.size) {
            return ApplyChangesResult.DuplicateName
        }

        // 4. Izvrši sve promene u jednoj atomičnoj transakciji
        return try {
            appDatabase.withTransaction {
                // a) Brisanje
                for (id in deletions) {
                    if (id > 0) {
                        landUnitDao.deleteNonSystemUnit(id)
                    }
                }

                // b) Ažuriranje
                for (update in updates) {
                    landUnitDao.updateUserUnit(
                        id = update.id,
                        displayName = normalizeName(update.displayName),
                        valueInSqMeters = update.valueInSqMeters
                    )
                }

                // c) Dodavanje
                for (addition in additions) {
                    landUnitDao.insertUnit(
                        LandUnitEntity(
                            countryCode = normalizedCountryCode,
                            unitId = addition.unitId.trim().lowercase(),
                            displayName = normalizeName(addition.displayName),
                            valueInSqMeters = addition.valueInSqMeters,
                            isSystem = false
                        )
                    )
                }
            }
            ApplyChangesResult.Success
        } catch (e: Exception) {
            ApplyChangesResult.Error
        }
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
                    isSystem = false
                )
            )
            SaveUnitResult.Saved
        } catch (e: SQLiteConstraintException) {
            SaveUnitResult.Duplicate
        }
    }

    suspend fun updateUserUnit(entity: LandUnit): UpdateUnitResult {
        if (entity.isSystem) {
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

    private suspend fun nameExists(countryCode: String, name: String, excludeId: Long?): Boolean =
        landUnitDao.getUnitsForCountry(countryCode).any { existing ->
            existing.id != excludeId &&
                    normalizeName(existing.displayName).equals(name, ignoreCase = true)
        }

    private fun normalizeName(name: String): String =
        name.trim().replace(Regex("\\s+"), " ")
}
