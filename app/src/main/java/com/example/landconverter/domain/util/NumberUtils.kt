package com.example.landconverter.domain.util

private val POSITIVE_NUMBER_REGEX = Regex("""^(\d+([.,]\d*)?|[.,]\d+)$""")

/**
 * Parsira tekstualni unos u pozitivan konačan broj (prihvata i ',' i '.' kao decimalni separator).
 * Vraća `null` ako unos nije ispravan broj ili ako je <= 0.
 */
fun parsePositiveNumber(input: String): Double? {
    val trimmed = input.trim()
    if (!POSITIVE_NUMBER_REGEX.matches(trimmed)) return null
    val value = trimmed.replace(',', '.').toDoubleOrNull() ?: return null
    return if (value.isFinite() && value > 0.0) value else null
}
