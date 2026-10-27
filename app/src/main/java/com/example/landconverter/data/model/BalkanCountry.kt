package com.example.landconverter.data.model

sealed class BalkanCountry(
    val id: String,
    val displayName: String
) {
    data object BosniaAndHerzegovina : BalkanCountry("ba", "Bosna i Hercegovina")
    data object Serbia : BalkanCountry("rs", "Srbija")
    data object Montenegro : BalkanCountry("me", "Crna Gora")
    data object NorthMacedonia : BalkanCountry("mk", "Северна Македонија")
    data object Croatia : BalkanCountry("hr", "Hrvatska")

    companion object {
        val all: List<BalkanCountry>
            get() = listOf(BosniaAndHerzegovina, Serbia, Montenegro, NorthMacedonia, Croatia)
    }
}