package com.djes021.landconverter.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djes021.landconverter.data.model.BalkanCountry
import com.djes021.landconverter.data.preferences.AppPreferences
import com.djes021.landconverter.data.repository.LandUnitRepository
import com.djes021.landconverter.domain.model.LandUnit
import com.djes021.landconverter.domain.usecase.ConversionResult
import com.djes021.landconverter.domain.usecase.ConvertAreaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: LandUnitRepository,
    private val convertAreaUseCase: ConvertAreaUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    var showWelcomeDialog by mutableStateOf(appPreferences.isFirstRun)
        private set

    fun dismissWelcomeDialog() {
        showWelcomeDialog = false
        appPreferences.setFirstRunCompleted()
    }

    // Uneta količina, država i izabrane mere čuvaju se u SavedStateHandle,
    // pa se vraćaju i kada Android ubije proces aplikacije u pozadini.
    var inputAmount by mutableStateOf(savedStateHandle.get<String>(KEY_AMOUNT) ?: "1")
        private set
    var selectedCountry: BalkanCountry by mutableStateOf(
        BalkanCountry.all.find { it.id == savedStateHandle.get<String>(KEY_COUNTRY) }
            ?: BalkanCountry.Serbia
    )
        private set

    val countries = BalkanCountry.all

    val uiStrings: AppUiStrings
        get() = CountryStringProvider.getStrings(selectedCountry)

    private val _countryFlow = MutableStateFlow(selectedCountry)

    @OptIn(ExperimentalCoroutinesApi::class)
    val availableUnits: StateFlow<List<LandUnit>> =
        _countryFlow
            .flatMapLatest { country ->
                repository.observeUnitsForCountry(country.id)
                    .onStart { emit(emptyList()) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    var fromUnit: LandUnit? by mutableStateOf(null)
        private set
    var toUnit: LandUnit? by mutableStateOf(null)
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
    private fun syncSelection(units: List<LandUnit>) {
        if (units.isEmpty()) return

        // Nikada ne prihvataj jedinice druge države.
        val currentUnits = units.filter {
            it.countryCode == selectedCountry.id
        }

        if (currentUnits.isEmpty()) {
            fromUnit = null
            toUnit = null
            inputError = null
            resultAmount = "0"
            return
        }

        // Zadrži izbor ako jedinica i dalje postoji.
        // U suprotnom izaberi podrazumevanu jedinicu.
        // Posle ubijanja procesa fromUnit/toUnit su null, pa se koriste sačuvani ID-evi.
        val wantedFromId = fromUnit?.id ?: savedStateHandle.get<Long>(KEY_FROM_UNIT)
        val wantedToId = toUnit?.id ?: savedStateHandle.get<Long>(KEY_TO_UNIT)

        val newFrom = currentUnits.firstOrNull { it.id == wantedFromId } ?: currentUnits.first()
        val newTo = currentUnits.firstOrNull { it.id == wantedToId }
            ?: currentUnits.getOrNull(1) ?: currentUnits.first()

        fromUnit = newFrom
        toUnit = newTo
        savedStateHandle[KEY_FROM_UNIT] = newFrom.id
        savedStateHandle[KEY_TO_UNIT] = newTo.id
        recalculate()
    }

    fun onInputChange(value: String) {
        inputAmount = value
        // Ograničeno da stanje ne preraste limit Bundle-a pri velikom nalepljenom tekstu.
        savedStateHandle[KEY_AMOUNT] = value.take(MAX_SAVED_INPUT_LENGTH)
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(INPUT_DEBOUNCE_MS.milliseconds)
            recalculate()
        }
    }

    fun onFromUnitChange(unit: LandUnit) {
        if (unit.countryCode != selectedCountry.id) return
        if (availableUnits.value.none { it.id == unit.id }) return
        fromUnit = unit
        savedStateHandle[KEY_FROM_UNIT] = unit.id
        recalculateNow()
    }

    fun onToUnitChange(unit: LandUnit) {
        if (unit.countryCode != selectedCountry.id) return
        if (availableUnits.value.none { it.id == unit.id }) return
        toUnit = unit
        savedStateHandle[KEY_TO_UNIT] = unit.id
        recalculateNow()
    }

    fun onCountryChange(country: BalkanCountry) {
        if (selectedCountry.id == country.id) return

        debounceJob?.cancel()
        selectedCountry = country
        savedStateHandle[KEY_COUNTRY] = country.id

        // Ukloni izbore prethodne države odmah (uključujući sačuvane ID-eve mera).
        fromUnit = null
        toUnit = null
        savedStateHandle.remove<Long>(KEY_FROM_UNIT)
        savedStateHandle.remove<Long>(KEY_TO_UNIT)
        inputError = null
        resultAmount = "0"

        // Pokreni učitavanje jedinica nove države.
        _countryFlow.value = country
    }

    private fun recalculateNow() {
        debounceJob?.cancel()
        recalculate()
    }

    private fun recalculate() {
        val from = fromUnit
        val to = toUnit
        if (from == null || to == null || from.countryCode != selectedCountry.id ||
            to.countryCode != selectedCountry.id
        ) {
            inputError = null
            resultAmount = "0"
            return
        }

        // Koristi samo jedinice koje su još u aktuelnoj listi.
        val currentUnits = availableUnits.value
        if (
            currentUnits.none { it.id == from.id } ||
            currentUnits.none { it.id == to.id }
        ) {
            inputError = null
            resultAmount = "0"
            return
        }

        when (val result =
            convertAreaUseCase(inputAmount, from.valueInSqMeters, to.valueInSqMeters)) {
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
        const val MAX_SAVED_INPUT_LENGTH = 100

        const val KEY_AMOUNT = "converter_amount"
        const val KEY_COUNTRY = "converter_country"
        const val KEY_FROM_UNIT = "converter_from_unit"
        const val KEY_TO_UNIT = "converter_to_unit"
    }
}