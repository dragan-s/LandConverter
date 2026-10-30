package com.djes021.landconverter.data.local

import com.djes021.landconverter.domain.model.LandUnit

fun LandUnitEntity.toDomain(): LandUnit = LandUnit(
    id = id,
    countryCode = countryCode,
    unitId = unitId,
    displayName = displayName,
    valueInSqMeters = valueInSqMeters,
    isSystem = isSystem
)