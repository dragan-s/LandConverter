package com.example.landconverter.domain.usecase

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

    // Pozitivan broj: "12", "12.5", "12,5", "12." ili ".5". Bez znaka, slova i više separatora.
    private val validNumber = Regex("""^(\d+([.,]\d*)?|[.,]\d+)$""")

    operator fun invoke(
        input: String,
        fromUnitSqMeters: Double,
        toUnitSqMeters: Double
    ): ConversionResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ConversionResult.Empty
        if (!validNumber.matches(trimmed)) return ConversionResult.InvalidInput

        val value = trimmed.replace(',', '.').toDoubleOrNull()
            ?: return ConversionResult.InvalidInput
        if (!value.isFinite()) return ConversionResult.InvalidInput

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