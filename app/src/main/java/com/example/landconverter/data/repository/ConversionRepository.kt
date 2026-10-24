package com.example.landconverter.data.repository

import com.example.landconverter.data.model.BalkanCountry
import com.example.landconverter.data.model.LandUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversionRepository @Inject constructor() {

    /** Konverzija ide preko kvadratnog metra kao zajedničke osnove. */
    fun convert(
        value: Double,
        from: LandUnit,
        to: LandUnit,
        country: BalkanCountry
    ): Double {
        val valueInSqMeters = value * from.getValueInSqMeters(country)
        return valueInSqMeters / to.getValueInSqMeters(country)
    }
}
