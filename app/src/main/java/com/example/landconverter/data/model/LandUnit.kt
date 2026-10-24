package com.example.landconverter.data.model

import com.example.landconverter.data.model.BalkanCountry.BosniaAndHerzegovina
import com.example.landconverter.data.model.BalkanCountry.Croatia
import com.example.landconverter.data.model.BalkanCountry.Montenegro
import com.example.landconverter.data.model.BalkanCountry.NorthMacedonia
import com.example.landconverter.data.model.BalkanCountry.Serbia

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
            BosniaAndHerzegovina -> "Hektar (ha)"
            Serbia -> "Hektar (ha)"
            Montenegro -> "Hektar (ha)"
            NorthMacedonia -> "Хектар (ha)"
            Croatia -> "Hektar (ha)"
        }
    }

    data object Dulum : LandUnit("dulum") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = when (country) {
            BosniaAndHerzegovina -> 1000.0
            Serbia -> 1000.0
            Montenegro -> 1000.0
            NorthMacedonia -> 1000.0
            Croatia -> 1000.0
        }

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            BosniaAndHerzegovina -> "Dulum"
            Serbia -> "Dulum"
            Montenegro -> "Dulum"
            NorthMacedonia -> "Дулум"
            Croatia -> "Dulum"
        }
    }

    data object Motika : LandUnit("motika") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = when (country) {
            BosniaAndHerzegovina -> 300.0
            Serbia -> 576.0
            Montenegro -> 400.0
            NorthMacedonia -> 800.0
            Croatia -> 360.0
        }

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            BosniaAndHerzegovina -> "Motika"
            Serbia -> "Motika"
            Montenegro -> "Motika"
            NorthMacedonia -> "Мотика"
            Croatia -> "Motika"
        }
    }

    data object Ralo : LandUnit("ralo") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = when (country) {
            BosniaAndHerzegovina -> 3600.0
            Serbia -> 2878.0
            Montenegro -> 3000.0
            NorthMacedonia -> 1600.0
            Croatia -> 2878.0
        }

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            BosniaAndHerzegovina -> "Ralo"
            Serbia -> "Ralo"
            Montenegro -> "Ralo"
            NorthMacedonia -> "Рало"
            Croatia -> "Ralo"
        }
    }

    data object RaloDanOranja : LandUnit("ralo_dan_oranja") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = 4000.0

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            NorthMacedonia -> "Рало (ден орање)"
            else -> "Ralo (dan oranja)"
        }
    }

    data object Lanac : LandUnit("lanac") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = when (country) {
            BosniaAndHerzegovina -> 7193.3
            Serbia -> 7192.0
            Montenegro -> 7193.3
            NorthMacedonia -> 5000.0
            Croatia -> 7193.3
        }

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            BosniaAndHerzegovina -> "Lanac"
            Serbia -> "Lanac"
            Montenegro -> "Lanac"
            NorthMacedonia -> "Ланац"
            Croatia -> "Lanac"
        }
    }

    data object JutroRal : LandUnit("jutro_ral") {
        override fun getValueInSqMeters(country: BalkanCountry): Double = CADASTRAL_JUTRO_SQM

        override fun getLocalizedName(country: BalkanCountry): String = when (country) {
            BosniaAndHerzegovina -> "Jutro (ral)"
            Serbia -> "Jutro (ral)"
            Montenegro -> "Jutro (ral)"
            NorthMacedonia -> "Јутро (рало)"
            Croatia -> "Jutro (ral)"
        }
    }

    companion object {
        val all: List<LandUnit>
            get() = listOf(KvadratniMetar, Ar, Hektar, Dulum, Motika, Ralo, RaloDanOranja, Lanac, JutroRal)

        fun getUnitsForCountry(country: BalkanCountry): List<LandUnit> {
            return when (country) {
                is BosniaAndHerzegovina -> listOf(KvadratniMetar, Ar, Hektar, Dulum, Motika, Ralo, Lanac, JutroRal)
                is Serbia -> listOf(KvadratniMetar, Ar, Hektar, Dulum, Motika, Ralo, Lanac, JutroRal)
                is Montenegro -> listOf(KvadratniMetar, Ar, Hektar, Dulum, Motika, Ralo, Lanac, JutroRal)
                is NorthMacedonia -> listOf(KvadratniMetar, Ar, Hektar, Dulum, Motika, Ralo, RaloDanOranja)
                is Croatia -> listOf(KvadratniMetar, Ar, Hektar, JutroRal, Lanac, Motika, Ralo)
            }
        }
    }
}
