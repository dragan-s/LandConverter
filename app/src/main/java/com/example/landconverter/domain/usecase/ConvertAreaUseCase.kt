package com.example.landconverter.domain.usecase

import com.example.landconverter.domain.util.parsePositiveNumber
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

sealed interface ConversionResult {
    data class Success(val formatted: String) : ConversionResult
    data object Empty : ConversionResult
    data object InvalidInput : ConversionResult
}

/**
 * Čista poslovna logika: validira unos, konvertuje preko kvadratnog metra
 * i formatira rezultat. Ne zna ništa o bazi ni o državama; prima samo vrednosti mera u m².
 */
class ConvertAreaUseCase @Inject constructor() {

    operator fun invoke(
        input: String,
        fromUnitSqMeters: Double,
        toUnitSqMeters: Double
    ): ConversionResult {
        if (input.trim().isEmpty()) return ConversionResult.Empty

        val value = parsePositiveNumber(input) ?: return ConversionResult.InvalidInput

        val result = value * fromUnitSqMeters / toUnitSqMeters
        if (!result.isFinite()) return ConversionResult.InvalidInput

        return ConversionResult.Success(format(result))
    }

    /** Maksimalno 4 decimale, bez suvišnih nula (npr. 2.5000 -> 2.5, 3.0000 -> 3). */
    private fun format(value: Double): String =
        BigDecimal.valueOf(value)
            .setScale(4, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
}
