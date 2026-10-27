package com.example.landconverter.presentation.manage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.landconverter.data.local.LandUnitEntity
import com.example.landconverter.data.model.BalkanCountry
import com.example.landconverter.data.repository.LandUnitRepository
import com.example.landconverter.data.repository.SaveUnitResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageUnitsViewModel @Inject constructor(
    private val repository: LandUnitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val initialCountryCode: String = savedStateHandle.get<String>("countryCode") ?: "rs"

    var selectedCountry: BalkanCountry by mutableStateOf(
        BalkanCountry.all.find { it.id == initialCountryCode } ?: BalkanCountry.Serbia
    )
        private set

    val countries = BalkanCountry.all

    private val _selectedCountryFlow = MutableStateFlow(selectedCountry)

    val units: StateFlow<List<LandUnitEntity>> = _selectedCountryFlow.flatMapLatest { country ->
        repository.observeUnitsForCountry(country.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var newUnitName by mutableStateOf("")
        private set
    var newUnitValue by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onCountrySelected(country: BalkanCountry) {
        selectedCountry = country
        _selectedCountryFlow.value = country
    }

    fun onNewUnitNameChange(name: String) {
        newUnitName = name
        errorMessage = null
    }

    fun onNewUnitValueChange(value: String) {
        newUnitValue = value
        errorMessage = null
    }

    fun updateExistingUnit(unit: LandUnitEntity, newName: String, newValueStr: String) {
        val value = newValueStr.replace(',', '.').toDoubleOrNull()
        if (newName.isBlank() || value == null || value <= 0) return
        viewModelScope.launch {
            val updated = unit.copy(displayName = newName.trim(), valueInSqMeters = value)
            repository.updateUserUnit(updated)
        }
    }

    fun deleteUnit(unitId: Long) {
        viewModelScope.launch {
            repository.deleteUnit(unitId)
        }
    }

    fun addNewUnit(): Boolean {
        val name = newUnitName.trim()
        val value = newUnitValue.replace(',', '.').toDoubleOrNull()

        if (name.isEmpty()) {
            errorMessage = "Naziv mere je obavezan."
            return false
        }
        if (value == null || value <= 0) {
            errorMessage = "Unesite ispravnu pozitivnu vrednost u m²."
            return false
        }

        viewModelScope.launch {
            val unitId = "custom_" + System.currentTimeMillis()
            val result = repository.saveUserUnit(
                countryCode = selectedCountry.id,
                unitId = unitId,
                displayName = name,
                valueInSqMeters = value
            )
            when (result) {
                SaveUnitResult.Saved -> {
                    newUnitName = ""
                    newUnitValue = ""
                    errorMessage = null
                }
                SaveUnitResult.Duplicate -> {
                    errorMessage = "Mera sa ovim ID-om ili nazivom već postoji."
                }
                SaveUnitResult.Invalid -> {
                    errorMessage = "Unesite ispravne podatke za novu meru."
                }
            }
        }
        return true
    }
}
