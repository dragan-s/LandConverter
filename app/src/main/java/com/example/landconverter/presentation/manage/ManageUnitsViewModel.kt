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
import com.example.landconverter.data.repository.UpdateUnitResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** Poruke o greškama za jedan red u listi postojećih mera. */
data class UnitEditErrors(
    val nameError: String? = null,
    val valueError: String? = null
)

/** Nesačuvana izmena jedne mere (tekst onako kako ga je korisnik uneo). */
data class UnitDraft(val name: String, val value: String)

/** 1000.0 -> "1000", 7193.3 -> "7193.3", 1.0E7 -> "10000000" (bez eksponenta). */
internal fun formatUnitValue(value: Double): String =
    value.toBigDecimal().stripTrailingZeros().toPlainString()

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

    /** Nesačuvane izmene po ID-u mere. Baza se ne dira dok korisnik ne pritisne "Sačuvaj". */
    var drafts: Map<Long, UnitDraft> by mutableStateOf(emptyMap())
        private set

    /** Greške po ID-u mere; red bez greške nema unos u mapi. */
    var editErrors: Map<Long, UnitEditErrors> by mutableStateOf(emptyMap())
        private set

    val hasUnsavedChanges: Boolean
        get() = drafts.isNotEmpty()

    fun onCountrySelected(country: BalkanCountry) {
        discardChanges()
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

    /** Samo pamti nacrt i prikazuje greške; ništa se ne upisuje u bazu. */
    fun onDraftChange(unit: LandUnitEntity, name: String, value: String) {
        val unchanged = name == unit.displayName && value == formatUnitValue(unit.valueInSqMeters)
        drafts = if (unchanged) drafts - unit.id else drafts + (unit.id to UnitDraft(name, value))
        editErrors = validateDrafts()
    }

    fun discardChanges() {
        drafts = emptyMap()
        editErrors = emptyMap()
    }

    /**
     * Upisuje sve nacrte. Ako bilo koji ima grešku, ništa se ne upisuje, greške ostaju
     * prikazane i `onSaved` se ne poziva (ekran ostaje otvoren).
     */
    fun saveChanges(onSaved: () -> Unit) {
        val errors = validateDrafts()
        editErrors = errors
        if (errors.isNotEmpty()) return

        val currentUnits = units.value.associateBy { it.id }
        val pending = drafts
        viewModelScope.launch {
            for ((id, draft) in pending) {
                val unit = currentUnits[id] ?: continue
                val value = parsePositiveValue(draft.value) ?: continue
                when (repository.updateUserUnit(unit.copy(displayName = draft.name, valueInSqMeters = value))) {
                    UpdateUnitResult.Updated, UpdateUnitResult.NotEditable -> Unit
                    UpdateUnitResult.DuplicateName -> {
                        editErrors = editErrors + (id to UnitEditErrors(nameError = "Naziv već postoji."))
                        return@launch
                    }
                    UpdateUnitResult.Invalid -> {
                        editErrors = editErrors + (id to UnitEditErrors(valueError = "Unesite broj > 0"))
                        return@launch
                    }
                }
            }
            discardChanges()
            onSaved()
        }
    }

    fun deleteUnit(unitId: Long) {
        drafts = drafts - unitId
        editErrors = validateDrafts()
        viewModelScope.launch {
            repository.deleteUnit(unitId)
        }
    }

    fun addNewUnit() {
        val name = newUnitName.trim()
        val value = parsePositiveValue(newUnitValue)

        if (name.isEmpty()) {
            errorMessage = "Naziv mere je obavezan."
            return
        }
        if (value == null) {
            errorMessage = "Unesite ispravnu pozitivnu vrednost u m²."
            return
        }

        viewModelScope.launch {
            val result = repository.saveUserUnit(
                countryCode = selectedCountry.id,
                unitId = "custom_" + UUID.randomUUID(),
                displayName = name,
                valueInSqMeters = value
            )
            when (result) {
                SaveUnitResult.Saved -> {
                    newUnitName = ""
                    newUnitValue = ""
                    errorMessage = null
                }
                SaveUnitResult.DuplicateName -> {
                    errorMessage = "Mera sa ovim nazivom već postoji u izabranoj državi."
                }
                SaveUnitResult.Duplicate -> {
                    errorMessage = "Mera nije sačuvana. Pokušajte ponovo."
                }
                SaveUnitResult.Invalid -> {
                    errorMessage = "Unesite ispravne podatke za novu meru."
                }
            }
        }
    }

    /** Proverava sve nacrte, uključujući duplikate naziva među merama izabrane države. */
    private fun validateDrafts(): Map<Long, UnitEditErrors> {
        val effectiveNames = units.value.associate { unit ->
            unit.id to normalizeName(drafts[unit.id]?.name ?: unit.displayName)
        }
        return drafts.mapNotNull { (id, draft) ->
            val name = normalizeName(draft.name)
            val nameError = when {
                name.isEmpty() -> "Naziv je obavezan."
                effectiveNames.any { (otherId, other) ->
                    otherId != id && other.equals(name, ignoreCase = true)
                } -> "Naziv već postoji."
                else -> null
            }
            val valueError = if (parsePositiveValue(draft.value) == null) "Unesite broj > 0" else null
            if (nameError != null || valueError != null) id to UnitEditErrors(nameError, valueError) else null
        }.toMap()
    }

    private fun normalizeName(name: String): String = name.trim().replace(Regex("\\s+"), " ")

    /** Pozitivan broj ("12", "12.5", "12,5", "12." ili ".5"); sve ostalo je nevalidno. */
    private fun parsePositiveValue(text: String): Double? {
        val trimmed = text.trim()
        if (!numberRegex.matches(trimmed)) return null
        return trimmed.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
    }

    private companion object {
        val numberRegex = Regex("""^(\d+([.,]\d*)?|[.,]\d+)$""")
    }
}