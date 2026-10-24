package com.example.landconverter.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.landconverter.data.model.BalkanCountry
import com.example.landconverter.data.model.ConverterOptions
import com.example.landconverter.data.model.LandUnit
import com.example.landconverter.domain.usecase.ConversionResult
import com.example.landconverter.domain.usecase.ConvertAreaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val convertAreaUseCase: ConvertAreaUseCase,
    val options: ConverterOptions
) : ViewModel() {

    var inputAmount by mutableStateOf("1")
        private set
    var fromUnit: LandUnit by mutableStateOf(LandUnit.Dulum)
        private set
    var toUnit: LandUnit by mutableStateOf(LandUnit.KvadratniMetar)
        private set
    var selectedCountry: BalkanCountry by mutableStateOf(BalkanCountry.Serbia)
        private set
    var resultAmount by mutableStateOf("0")
        private set
    var inputError: String? by mutableStateOf(null)
        private set

    /** Tekstovi prate trenutno izabranu državu preko prezentacionog sloja. */
    val uiStrings: AppUiStrings
        get() = CountryStringProvider.getStrings(selectedCountry)

    private var debounceJob: Job? = null

    init {
        recalculate()
    }

    /** Tekst u polju se ažurira odmah, a preračunavanje čeka kratku pauzu nakon poslednjeg unosa. */
    fun onInputChange(value: String) {
        inputAmount = value
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(INPUT_DEBOUNCE_MS.milliseconds)
            recalculate()
        }
    }

    fun onFromUnitChange(unit: LandUnit) {
        fromUnit = unit
        recalculateNow()
    }

    fun onToUnitChange(unit: LandUnit) {
        toUnit = unit
        recalculateNow()
    }

    fun onCountryChange(country: BalkanCountry) {
        selectedCountry = country
        recalculateNow() // ažurira i lokalizovanu poruku greške
    }

    /** Promena jedinice ili države preračunava odmah i otkazuje eventualno čekanje unosa. */
    private fun recalculateNow() {
        debounceJob?.cancel()
        recalculate()
    }

    private fun recalculate() {
        when (val result = convertAreaUseCase(inputAmount, fromUnit, toUnit, selectedCountry)) {
            is ConversionResult.Success -> {
                inputError = null
                resultAmount = result.formatted
            }
            ConversionResult.Empty -> {
                inputError = null
                resultAmount = "0"
            }
            ConversionResult.InvalidInput -> {
                inputError = uiStrings.errorInvalidNumber
                resultAmount = "0"
            }
        }
    }

    private companion object {
        const val INPUT_DEBOUNCE_MS = 300L
    }
}
