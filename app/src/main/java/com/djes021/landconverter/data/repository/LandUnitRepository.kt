package com.djes021.landconverter.data.repository

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

        // 3. Proverava KONAČNO stanje naziva za državu nakon svih brisanja, izmena i dodavanja
        val updatesMap = updates.associateBy { it.id }
        val remainingUnits = dbUnits.filter { it.id !in deletions }

        val finalNames = remainingUnits.map { unit ->
            val update = updatesMap[unit.id]
            normalizeUnitName(update?.displayName ?: unit.displayName)
        } + additions.map { normalizeUnitName(it.displayName) }

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
                        displayName = normalizeUnitName(update.displayName),
                        valueInSqMeters = update.valueInSqMeters
                    )
                }

                // c) Dodavanje
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
            }
            ApplyChangesResult.Success
        } catch (e: CancellationException) {
            // Otkazivanje korutine mora da se propagira (transakcija je već poništena).
            throw e
        } catch (e: Exception) {
            ApplyChangesResult.Error
        }
    }
}