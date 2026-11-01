package com.djes021.landconverter.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.djes021.landconverter.data.local.AppDatabase
import com.djes021.landconverter.data.local.LandUnitDao
import com.djes021.landconverter.data.local.LandUnitEntity
import com.djes021.landconverter.data.local.toDomain
import com.djes021.landconverter.domain.model.LandUnit
import com.djes021.landconverter.domain.util.isValidUnitName
import com.djes021.landconverter.domain.util.isValidUnitValue
import com.djes021.landconverter.domain.util.normalizeUnitName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Nova mera; država se prosleđuje jednom, kao parametar `applyChanges`. */
data class NewUnitSpec(
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
     * Atomično primenjuje sve izmene (brisanja, dodavanja i ažuriranja) u jednoj transakciji.
     * Stanje se čita, proverava (uključujući KONAČNE nazive) i upisuje unutar iste transakcije,
     * pa između provere i upisa ne može da se desi druga izmena.
     */
    suspend fun applyChanges(
        countryCode: String,
        deletions: Set<Long>,
        additions: List<NewUnitSpec>,
        updates: List<UpdateUnitSpec>
    ): ApplyChangesResult {
        val normalizedCountryCode = countryCode.trim().lowercase()

        return try {
            appDatabase.withTransaction {
                val dbUnits = landUnitDao.getUnitsForCountry(normalizedCountryCode)

                // Ako nešto nije u redu, ništa se ne upisuje (transakcija se završava bez izmena).
                validateChanges(dbUnits, deletions, additions, updates)
                    ?.let { return@withTransaction it }

                for (id in deletions) {
                    if (id > 0) landUnitDao.deleteNonSystemUnit(id)
                }
                for (update in updates) {
                    landUnitDao.updateUserUnit(
                        id = update.id,
                        displayName = normalizeUnitName(update.displayName),
                        valueInSqMeters = update.valueInSqMeters
                    )
                }
                for (addition in additions) {
                    landUnitDao.insertUnit(
                        LandUnitEntity(
                            countryCode = normalizedCountryCode,
                            unitId = addition.unitId.trim().lowercase(),
                            displayName = normalizeUnitName(addition.displayName),
                            valueInSqMeters = addition.valueInSqMeters,
                            isSystem = false
                        )
                    )
                }
                ApplyChangesResult.Success
            }
        } catch (e: CancellationException) {
            // Otkazivanje korutine mora da se propagira (transakcija je već poništena).
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "applyChanges failed", e)
            ApplyChangesResult.Error
        }
    }

    /** Vraća razlog odbijanja ili `null` ako su sve promene ispravne. */
    private fun validateChanges(
        dbUnits: List<LandUnitEntity>,
        deletions: Set<Long>,
        additions: List<NewUnitSpec>,
        updates: List<UpdateUnitSpec>
    ): ApplyChangesResult? {
        val dbUnitsById = dbUnits.associateBy { it.id }

        for (id in deletions) {
            val unit = dbUnitsById[id]
            if (unit != null && unit.isSystem) return ApplyChangesResult.Invalid
        }

        for (update in updates) {
            val unit = dbUnitsById[update.id]
            if (unit == null || unit.isSystem) return ApplyChangesResult.Invalid
            if (!isValidUnitName(update.displayName) || !isValidUnitValue(update.valueInSqMeters)) {
                return ApplyChangesResult.Invalid
            }
        }

        for (addition in additions) {
            if (
                !isValidUnitName(addition.displayName) ||
                addition.unitId.isBlank() ||
                !isValidUnitValue(addition.valueInSqMeters)
            ) {
                return ApplyChangesResult.Invalid
            }
        }

        // KONAČNO stanje naziva nakon svih brisanja, izmena i dodavanja mora biti jedinstveno.
        val updatesById = updates.associateBy { it.id }
        val finalNames = dbUnits
            .filter { it.id !in deletions }
            .map { unit -> normalizeUnitName(updatesById[unit.id]?.displayName ?: unit.displayName) } +
                additions.map { normalizeUnitName(it.displayName) }

        val lowerNames = finalNames.map { it.lowercase() }
        if (lowerNames.distinct().size != lowerNames.size) return ApplyChangesResult.DuplicateName

        return null
    }

    private companion object {
        const val TAG = "LandUnitRepository"
    }
}