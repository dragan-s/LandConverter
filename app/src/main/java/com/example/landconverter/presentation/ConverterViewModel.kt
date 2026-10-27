package com.example.landconverter.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.landconverter.data.local.LandUnitEntity
import com.example.landconverter.data.model.BalkanCountry
import com.example.landconverter.data.repository.LandUnitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val repository: LandUnitRepository
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
    private val validNumber = Regex("""^(\d+([.,]\d*)?|[.,]\d+)$""")

    init {
        viewModelScope.launch {
            _countryFlow.collectLatest { country ->
                val units = repository.getUnitsForCountry(country.id)
                if (units.isNotEmpty()) {
                    if (fromUnit == null || units.none { it.id == fromUnit?.id }) {
                        fromUnit = units.firstOrNull()
                    }
                    if (toUnit == null || units.none { it.id == toUnit?.id }) {
                        toUnit = units.getOrNull(1) ?: units.firstOrNull()
                    }
                }
                recalculate()
            }
        }

        viewModelScope.launch {
            availableUnits.collect { units ->
                if (units.isNotEmpty()) {
                    if (fromUnit == null || units.none { it.id == fromUnit?.id }) {
                        fromUnit = units.firstOrNull()
                    }
                    if (toUnit == null || units.none { it.id == toUnit?.id }) {
                        toUnit = units.getOrNull(1) ?: units.firstOrNull()
                    }
                }
                recalculate()
            }
        }
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
        val trimmed = inputAmount.trim()
        if (trimmed.isEmpty()) {
            inputError = null
            resultAmount = "0"
            return
        }
        if (!validNumber.matches(trimmed)) {
            inputError = uiStrings.errorInvalidNumber
            resultAmount = "0"
            return
        }

        val value = trimmed.replace(',', '.').toDoubleOrNull()
        if (value == null || !value.isFinite()) {
            inputError = uiStrings.errorInvalidNumber
            resultAmount = "0"
            return
        }

        val from = fromUnit
        val to = toUnit
        if (from == null || to == null) {
            inputError = null
            resultAmount = "0"
            return
        }

        val valueInSqMeters = value * from.valueInSqMeters
        val res = valueInSqMeters / to.valueInSqMeters
        if (!res.isFinite()) {
            inputError = uiStrings.errorInvalidNumber
            resultAmount = "0"
            return
        }

        inputError = null
        resultAmount = format(res)
    }

    private fun format(value: Double): String =
        BigDecimal.valueOf(value)
            .setScale(4, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()

    private companion object {
        const val INPUT_DEBOUNCE_MS = 300L
    }
}
