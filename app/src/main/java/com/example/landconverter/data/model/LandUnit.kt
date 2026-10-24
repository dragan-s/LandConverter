package com.example.landconverter.data.model

import com.example.landconverter.data.model.BalkanCountry.BosniaAndHerzegovina
import com.example.landconverter.data.model.BalkanCountry.Croatia
import com.example.landconverter.data.model.BalkanCountry.Montenegro
import com.example.landconverter.data.model.BalkanCountry.NorthMacedonia
import com.example.landconverter.data.model.BalkanCountry.Serbia
import com.example.landconverter.data.model.BalkanCountry.StandardSI

/** Katastarsko jutro (1600 četvornih hvati) u m². */
private const val CADASTRAL_JUTRO_SQM = 5754.64

/**
 * Tradicionalne jedinice su regionalno promenljive i u praksi se razlikuju od sela do sela.
 * Vrednosti ispod su približne referentne vrednosti i lako se menjaju u okviru `when` blokova.
 */
sealed class LandUnit(val id: String) {

    abstract fun getValueInSqMeters(country: BalkanCountry): Double
    abstract fun getLocalizedName(country: BalkanCountry): String

    data object KvadratniMetar : LandUnit("sqm") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = 1.0
        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            StandardSI -> "Square meter (m²)"
            BosniaAndHerzegovina -> "Kvadratni metar (m²)"
            Serbia -> "Kvadratni metar (m²)"
            Montenegro -> "Kvadratni metar (m²)"
            NorthMacedonia -> "Квадратен метар (m²)"
            Croatia -> "Četvorni metar (m²)"
        }
    }

    data object Ar : LandUnit("ar") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = 100.0
        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            StandardSI -> "Are (a)"
            BosniaAndHerzegovina -> "Ar (a)"
            Serbia -> "Ar (a)"
            Montenegro -> "Ar (a)"
            NorthMacedonia -> "Ар (a)"
            Croatia -> "Ar (a)"
        }
    }

    data object Hektar : LandUnit("ha") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = 10_000.0
        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            StandardSI -> "Hectare (ha)"
            BosniaAndHerzegovina -> "Hektar (ha)"
            Serbia -> "Hektar (ha)"
            Montenegro -> "Hektar (ha)"
            NorthMacedonia -> "Хектар (ha)"
            Croatia -> "Hektar (ha)"
        }
    }

    data object Dulum : LandUnit("dulum") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = when (country) {
            StandardSI -> 1000.0
            BosniaAndHerzegovina -> 1000.0
            Serbia -> 1000.0
            Montenegro -> 1000.0
            NorthMacedonia -> 1000.0
            Croatia -> 1000.0
        }

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            StandardSI -> "Dunum"
            BosniaAndHerzegovina -> "Dulum"
            Serbia -> "Dulum"
            Montenegro -> "Dulum"
            NorthMacedonia -> "Дулум"
            Croatia -> "Dulum"
        }
    }

    data object Motika : LandUnit("motika") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = when (country) {
            StandardSI -> 500.0
            BosniaAndHerzegovina -> 300.0
            Serbia -> 576.0
            Montenegro -> 400.0
            NorthMacedonia -> 250.0
            Croatia -> 360.0
        }

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            StandardSI -> "Motika (hoe area)"
            BosniaAndHerzegovina -> "Motika"
            Serbia -> "Motika"
            Montenegro -> "Motika"
            NorthMacedonia -> "Мотика"
            Croatia -> "Motika"
        }
    }

    data object Ralo : LandUnit("ralo") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = when (country) {
            StandardSI -> 3000.0
            BosniaAndHerzegovina -> 3600.0
            Serbia -> 2878.0
            Montenegro -> 3000.0
            NorthMacedonia -> 3000.0
            Croatia -> 2878.0
        }

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            StandardSI -> "Ralo (plough area)"
            BosniaAndHerzegovina -> "Ralo"
            Serbia -> "Ralo"
            Montenegro -> "Ralo"
            NorthMacedonia -> "Рало"
            Croatia -> "Ralo"
        }
    }

    data object Lanac : LandUnit("lanac") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = when (country) {
            StandardSI -> 5755.0
            BosniaAndHerzegovina -> 4316.0
            Serbia -> 5755.0
            Montenegro -> 5000.0
            NorthMacedonia -> 5000.0
            Croatia -> CADASTRAL_JUTRO_SQM
        }

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            StandardSI -> "Lanac (chain area)"
            BosniaAndHerzegovina -> "Lanac"
            Serbia -> "Lanac"
            Montenegro -> "Lanac"
            NorthMacedonia -> "Ланац"
            Croatia -> "Lanac"
        }
    }

    data object JutroRal : LandUnit("jutro_ral") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = when (country) {
            StandardSI -> CADASTRAL_JUTRO_SQM
            BosniaAndHerzegovina -> CADASTRAL_JUTRO_SQM
            Serbia -> CADASTRAL_JUTRO_SQM
            Montenegro -> CADASTRAL_JUTRO_SQM
            NorthMacedonia -> CADASTRAL_JUTRO_SQM
            Croatia -> CADASTRAL_JUTRO_SQM
        }

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            StandardSI -> "Jutro / Ral (cadastral acre)"
            BosniaAndHerzegovina -> "Jutro (ral)"
            Serbia -> "Jutro (ral)"
            Montenegro -> "Jutro (ral)"
            NorthMacedonia -> "Јутро (рало)"
            Croatia -> "Jutro (ral)"
        }
    }

    companion object {
        val all: List<LandUnit>
            get() = listOf(KvadratniMetar, Ar, Hektar, Dulum, Motika, Ralo, Lanac, JutroRal)
    }
}
