package com.example.landconverter.data.local

import com.example.landconverter.domain.model.LandUnit

fun LandUnitEntity.toDomain(): LandUnit = LandUnit(
    id = id,
    countryCode = countryCode,
    unitId = unitId,
    displayName = displayName,
    valueInSqMeters = valueInSqMeters,
    isSystem = isSystem,
    sourceType = sourceType
)

fun LandUnit.toEntity(): LandUnitEntity = LandUnitEntity(
    id = id,
    countryCode = countryCode,
    unitId = unitId,
    displayName = displayName,
    valueInSqMeters = valueInSqMeters,
    isSystem = isSystem,
    sourceType = sourceType
)
