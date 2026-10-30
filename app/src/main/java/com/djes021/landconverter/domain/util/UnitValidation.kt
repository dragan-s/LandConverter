package com.djes021.landconverter.domain.util

/** Dozvoljeni opseg vrednosti jedne mere: od 0,0001 m² do 1.000.000 km². */
const val MIN_UNIT_SQM = 0.0001
const val MAX_UNIT_SQM = 1_000_000_000_000.0

/** Najveća dužina naziva mere (posle uklanjanja viška razmaka). */
const val MAX_UNIT_NAME_LENGTH = 40

private val WHITESPACE = Regex("\\s+")

/** Uklanja razmake sa krajeva i sažima uzastopne razmake u jedan. */
fun normalizeUnitName(name: String): String = name.trim().replace(WHITESPACE, " ")

fun isValidUnitName(name: String): Boolean {
    val normalized = normalizeUnitName(name)
    return normalized.isNotEmpty() && normalized.length <= MAX_UNIT_NAME_LENGTH
}

fun isValidUnitValue(value: Double): Boolean =
    value.isFinite() && value in MIN_UNIT_SQM..MAX_UNIT_SQM

/** Parsira unos i vraća vrednost samo ako je u dozvoljenom opsegu za meru. */
fun parseUnitValue(input: String): Double? =
    parsePositiveNumber(input)?.takeIf { isValidUnitValue(it) }
