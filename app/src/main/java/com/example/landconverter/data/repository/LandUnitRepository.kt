package com.example.landconverter.data.repository

import com.example.landconverter.data.local.LandUnitDao
import com.example.landconverter.data.local.LandUnitEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

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

    suspend fun saveUnit(
        countryCode: String,
        unitId: String,
        displayName: String,
        valueInSqMeters: Double,
        isSystem: Boolean = false
    ) {
        val entity = LandUnitEntity(
            countryCode = countryCode,
            unitId = unitId,
            displayName = displayName,
            valueInSqMeters = valueInSqMeters,
            isSystem = isSystem
        )
        landUnitDao.insertUnit(entity)
    }

    suspend fun updateUnit(entity: LandUnitEntity) {
        if (!entity.isSystem) {
            landUnitDao.updateUnit(entity)
        }
    }

    suspend fun deleteUnit(id: Long) {
        landUnitDao.deleteNonSystemUnit(id)
    }
}
