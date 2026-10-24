package com.example.landconverter.data.model

/** Skup opcija dostupnih u dropdown menijima (isporučuje ga Hilt). */
data class ConverterOptions(
    val countries: List<BalkanCountry>,
    val units: List<LandUnit>
)
