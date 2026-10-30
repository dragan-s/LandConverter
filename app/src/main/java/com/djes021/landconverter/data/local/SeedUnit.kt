package com.djes021.landconverter.data.local

import android.content.ContentValues
import androidx.core.content.contentValuesOf

/** Jedna početna mera koja se upisuje u bazu pri prvom pokretanju. */
data class SeedUnit(
    val unitId: String,
    val displayName: String,
    val valueInSqMeters: Double,
    val isSystem: Boolean
) {
    fun toContentValues(countryCode: String): ContentValues = contentValuesOf(
        "countryCode" to countryCode,
        "unitId" to unitId,
        "displayName" to displayName,
        "valueInSqMeters" to valueInSqMeters,
        "isSystem" to (if (isSystem) 1 else 0)
    )
}

/** Početne mere po državama. Jedini izvor podrazumevanih podataka. */
object InitialUnits {

    private const val DULUM_SQM = 1000.0
    private const val MOTIKA_SQM = 800.0
    private const val JUTRO_RAL_SQM = 5754.64

    private fun systemUnits(sqmName: String = "Kvadratni metar (m²)") = listOf(
        SeedUnit("sqm", sqmName, 1.0, isSystem = true),
        SeedUnit("ar", "Ar (a)", 100.0, isSystem = true),
        SeedUnit("ha", "Hektar (ha)", 10_000.0, isSystem = true)
    )

    private fun traditional(unitId: String, name: String, value: Double) =
        SeedUnit(unitId, name, value, isSystem = false)

    val byCountry: Map<String, List<SeedUnit>> = mapOf(
        "ba" to systemUnits() + listOf(
            traditional("dulum", "Dulum", DULUM_SQM),
            traditional("motika", "Motika", MOTIKA_SQM),
            traditional("lanac", "Lanac", 7193.3),
            traditional("jutro_ral", "Jutro (ral)", JUTRO_RAL_SQM)
        ),
        "rs" to systemUnits() + listOf(
            traditional("dulum", "Dulum", DULUM_SQM),
            traditional("motika", "Motika", MOTIKA_SQM),
            traditional("ralo", "Ralo", 2878.0),
            traditional("ralo_dan_oranja", "Ralo (dan oranja)", 4000.0),
            traditional("lanac", "Lanac", 7192.0),
            traditional("jutro_ral", "Jutro (ral)", JUTRO_RAL_SQM)
        ),
        "me" to systemUnits() + listOf(
            traditional("dulum", "Dulum", DULUM_SQM),
            traditional("motika", "Motika", MOTIKA_SQM),
            traditional("ralo", "Ralo", 1820.0),
            traditional("lanac", "Lanac", 7193.3),
            traditional("jutro_ral", "Jutro (ral)", JUTRO_RAL_SQM)
        ),
        "mk" to systemUnits() + listOf(
            traditional("dulum", "Дулум", DULUM_SQM),
            traditional("motika", "Мотика", MOTIKA_SQM),
            traditional("ralo", "Рало", 1600.0),
            traditional("ralo_dan_oranja", "Рало (ден орање)", 4000.0)
        ),
        "hr" to systemUnits(sqmName = "Četvorni metar (m²)") + listOf(
            traditional("dulum", "Dulum", DULUM_SQM),
            traditional("motika", "Motika", MOTIKA_SQM),
            traditional("jutro_ral", "Jutro (ral)", JUTRO_RAL_SQM)
        )
    )
}
