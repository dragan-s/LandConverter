package com.djes021.landconverter.domain.model

/**
 * Čist domenski model za jedinicu površine (bez Room/database anotacija).
 */
data class LandUnit(
    val id: Long = 0L,
    val countryCode: String,
    val unitId: String,
    val displayName: String,
    val valueInSqMeters: Double,
    val isSystem: Boolean
)
