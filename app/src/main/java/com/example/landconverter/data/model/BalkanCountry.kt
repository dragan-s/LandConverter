package com.example.landconverter.data.model

private val stringsStandardSI = AppStrings(
    appTitle = "Land Area Converter",
    amountLabel = "Amount",
    fromUnitLabel = "From unit",
    toUnitLabel = "To unit",
    countryLabel = "Country / system",
    resultTitle = "Result",
    errorInvalidNumber = "Enter a valid positive number"
)

private val stringsBosnia = AppStrings(
    appTitle = "Konvertor površine zemljišta",
    amountLabel = "Količina",
    fromUnitLabel = "Iz jedinice",
    toUnitLabel = "U jedinicu",
    countryLabel = "Država",
    resultTitle = "Rezultat",
    errorInvalidNumber = "Unesite ispravan pozitivan broj"
)

private val stringsSerbia = AppStrings(
    appTitle = "Pretvarač površine zemljišta",
    amountLabel = "Količina",
    fromUnitLabel = "Iz jedinice",
    toUnitLabel = "U jedinicu",
    countryLabel = "Država",
    resultTitle = "Rezultat",
    errorInvalidNumber = "Unesite ispravan pozitivan broj"
)

private val stringsMontenegro = AppStrings(
    appTitle = "Konvertor površine zemljišta",
    amountLabel = "Količina",
    fromUnitLabel = "Iz jedinice",
    toUnitLabel = "U jedinicu",
    countryLabel = "Država",
    resultTitle = "Rezultat",
    errorInvalidNumber = "Unesite važeći pozitivan broj"
)

private val stringsNorthMacedonia = AppStrings(
    appTitle = "Конвертор на површина на земјиште",
    amountLabel = "Количина",
    fromUnitLabel = "Од единица",
    toUnitLabel = "До единица",
    countryLabel = "Држава",
    resultTitle = "Резултат",
    errorInvalidNumber = "Внесете валиден позитивен број"
)

private val stringsCroatia = AppStrings(
    appTitle = "Pretvornik površine zemljišta",
    amountLabel = "Količina",
    fromUnitLabel = "Iz jedinice",
    toUnitLabel = "U jedinicu",
    countryLabel = "Država",
    resultTitle = "Rezultat",
    errorInvalidNumber = "Unesite ispravan pozitivan broj"
)

sealed class BalkanCountry(
    val id: String,
    val displayName: String,
    val uiStrings: AppStrings
) {
    data object StandardSI : BalkanCountry("si", "SI standard", stringsStandardSI)
    data object BosniaAndHerzegovina : BalkanCountry("ba", "Bosna i Hercegovina", stringsBosnia)
    data object Serbia : BalkanCountry("rs", "Srbija", stringsSerbia)
    data object Montenegro : BalkanCountry("me", "Crna Gora", stringsMontenegro)
    data object NorthMacedonia : BalkanCountry("mk", "Северна Македонија", stringsNorthMacedonia)
    data object Croatia : BalkanCountry("hr", "Hrvatska", stringsCroatia)

    companion object {
        // Getter (a ne polje) da se izbegne problem redosleda inicijalizacije sealed objekata.
        val all: List<BalkanCountry>
            get() = listOf(BosniaAndHerzegovina, Serbia, Montenegro, NorthMacedonia, Croatia) //StandardSI,
    }
}
