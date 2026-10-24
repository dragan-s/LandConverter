package com.example.landconverter.presentation

import com.example.landconverter.data.model.BalkanCountry

data class AppUiStrings(
    val appTitle: String,
    val amountLabel: String,
    val fromUnitLabel: String,
    val toUnitLabel: String,
    val countryLabel: String,
    val resultTitle: String,
    val errorInvalidNumber: String
)

object CountryStringProvider {
    fun getStrings(country: BalkanCountry): AppUiStrings {
        return when (country) {
            /*is BalkanCountry.StandardSI -> AppUiStrings(
                appTitle = "Land Area Converter",
                amountLabel = "Amount",
                fromUnitLabel = "From unit",
                toUnitLabel = "To unit",
                countryLabel = "Country / system",
                resultTitle = "Result",
                errorInvalidNumber = "Enter a valid positive number"
            )*/
            is BalkanCountry.BosniaAndHerzegovina -> AppUiStrings(
                appTitle = "Konvertor površine zemljišta",
                amountLabel = "Količina",
                fromUnitLabel = "Iz jedinice",
                toUnitLabel = "U jedinicu",
                countryLabel = "Država",
                resultTitle = "Rezultat",
                errorInvalidNumber = "Unesite ispravan pozitivan broj"
            )
            is BalkanCountry.Serbia -> AppUiStrings(
                appTitle = "Pretvarač površine zemljišta",
                amountLabel = "Količina",
                fromUnitLabel = "Iz jedinice",
                toUnitLabel = "U jedinicu",
                countryLabel = "Država",
                resultTitle = "Rezultat",
                errorInvalidNumber = "Unesite ispravan pozitivan broj"
            )
            is BalkanCountry.Montenegro -> AppUiStrings(
                appTitle = "Konvertor površine zemljišta",
                amountLabel = "Količina",
                fromUnitLabel = "Iz jedinice",
                toUnitLabel = "U jedinicu",
                countryLabel = "Država",
                resultTitle = "Rezultat",
                errorInvalidNumber = "Unesite važeći pozitivan broj"
            )
            is BalkanCountry.NorthMacedonia -> AppUiStrings(
                appTitle = "Конвертор на површина на земјиште",
                amountLabel = "Количина",
                fromUnitLabel = "Од единица",
                toUnitLabel = "До единица",
                countryLabel = "Држава",
                resultTitle = "Резултат",
                errorInvalidNumber = "Внесете валиден позитивен број"
            )
            is BalkanCountry.Croatia -> AppUiStrings(
                appTitle = "Pretvornik površine zemljišta",
                amountLabel = "Količina",
                fromUnitLabel = "Iz jedinice",
                toUnitLabel = "U jedinicu",
                countryLabel = "Država",
                resultTitle = "Rezultat",
                errorInvalidNumber = "Unesite ispravan pozitivan broj"
            )
        }
    }
}
