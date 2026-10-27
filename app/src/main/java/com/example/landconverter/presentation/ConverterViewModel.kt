package com.example.landconverter.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.landconverter.data.local.LandUnitEntity
import com.example.landconverter.data.model.BalkanCountry
import com.example.landconverter.data.repository.LandUnitRepository
import com.example.landconverter.domain.usecase.ConversionResult
import com.example.landconverter.domain.usecase.ConvertAreaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val repository: LandUnitRepository,
    private val convertAreaUseCase: ConvertAreaUseCase
) : ViewModel() {

    var inputAmount by mutableStateOf("1")
        private set
    var selectedCountry: BalkanCountry by mutableStateOf(BalkanCountry.Serbia)
        private set

    val countries = BalkanCountry.all

    val uiStrings: AppUiStrings
        get() = CountryStringProvider.getStrings(selectedCountry)

    private val _countryFlow = MutableStateFlow(selectedCountry)

    val availableUnits: StateFlow<List<LandUnitEntity>> = _countryFlow.flatMapLatest { country ->
        repository.observeUnitsForCountry(country.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var fromUnit: LandUnitEntity? by mutableStateOf(null)
        private set
    var toUnit: LandUnitEntity? by mutableStateOf(null)
        private set

    var resultAmount by mutableStateOf("0")
        private set
    var inputError: String? by mutableStateOf(null)
        private set

    private var debounceJob: Job? = null

    init {
        // Jedini izvor istine za listu mera. Svaka promena u bazi (izmena, brisanje,
        // dodavanje, promena države) osvežava i izabrane mere.
        viewModelScope.launch {
            availableUnits.collect { units -> syncSelection(units) }
        }
    }

    /**
     * Izabrane mere se uvek zamenjuju svežim entitetima iz liste (po ID-u), da konverzija
     * ne bi koristila staru vrednost ili naziv nakon izmene na ekranu "Upravljaj merama".
     * Ako izabrane mere više nema (obrisana je ili je promenjena država), bira se podrazumevana.
     */
    private fun syncSelection(units: List<LandUnitEntity>) {
        if (units.isEmpty()) return
        fromUnit = units.firstOrNull { it.id == fromUnit?.id } ?: units.first()
        toUnit = units.firstOrNull { it.id == toUnit?.id } ?: units.getOrNull(1) ?: units.first()
        recalculate()
    }

    fun onInputChange(value: String) {
        inputAmount = value
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(INPUT_DEBOUNCE_MS.milliseconds)
            recalculate()
        }
    }

    fun onFromUnitChange(unit: LandUnitEntity) {
        fromUnit = unit
        recalculateNow()
    }

    fun onToUnitChange(unit: LandUnitEntity) {
        toUnit = unit
        recalculateNow()
    }

    fun onCountryChange(country: BalkanCountry) {
        selectedCountry = country
        _countryFlow.value = country
        recalculateNow()
    }

    private fun recalculateNow() {
        debounceJob?.cancel()
        recalculate()
    }

    private fun recalculate() {
        val from = fromUnit
        val to = toUnit
        if (from == null || to == null) {
            inputError = null
            resultAmount = "0"
            return
        }

        when (val result = convertAreaUseCase(inputAmount, from.valueInSqMeters, to.valueInSqMeters)) {
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