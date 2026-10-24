package com.example.landconverter.domain.usecase

import com.example.landconverter.data.model.BalkanCountry
import com.example.landconverter.data.model.LandUnit
import com.example.landconverter.data.repository.ConversionRepository
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

sealed interface ConversionResult {
    data class Success(val formatted: String) : ConversionResult
    data object Empty : ConversionResult
    data object InvalidInput : ConversionResult
}

class ConvertAreaUseCase @Inject constructor(
    private val repository: ConversionRepository
) {
    // Pozitivan broj: "12", "12.5", "12,5", "12." ili ".5". Bez znaka, slova i više separatora.
    private val validNumber = Regex("""^(\d+([.,]\d*)?|[.,]\d+)$""")

    operator fun invoke(
        input: String,
        from: LandUnit,
        to: LandUnit,
        country: BalkanCountry
    ): ConversionResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ConversionResult.Empty
        if (!validNumber.matches(trimmed)) return ConversionResult.InvalidInput

        val value = trimmed.replace(',', '.').toDoubleOrNull()
            ?: return ConversionResult.InvalidInput
        if (!value.isFinite()) return ConversionResult.InvalidInput

        val result = repository.convert(value, from, to, country)
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
