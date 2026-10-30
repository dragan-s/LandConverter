package com.example.landconverter.presentation.manage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.landconverter.data.local.LandUnitEntity
import com.example.landconverter.data.model.BalkanCountry
import com.example.landconverter.data.repository.ApplyChangesResult
import com.example.landconverter.data.repository.LandUnitRepository
import com.example.landconverter.data.repository.NewUnitSpec
import com.example.landconverter.data.repository.UpdateUnitSpec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val _pendingNewUnitsFlow = MutableStateFlow<List<LandUnitEntity>>(emptyList())
    private val _pendingDeletionsFlow = MutableStateFlow<Set<Long>>(emptySet())
    private var nextTempId = -1L

    @OptIn(ExperimentalCoroutinesApi::class)
    val units: StateFlow<List<LandUnitEntity>> = combine(
        _selectedCountryFlow.flatMapLatest { country ->
            repository.observeUnitsForCountry(country.id)
        },
        _pendingNewUnitsFlow,
        _pendingDeletionsFlow
    ) { dbUnits, pendingUnits, pendingDeletions ->
        (dbUnits + pendingUnits).filter { it.id !in pendingDeletions }
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
        get() = drafts.isNotEmpty() ||
                _pendingNewUnitsFlow.value.isNotEmpty() ||
                _pendingDeletionsFlow.value.isNotEmpty() ||
                newUnitName.isNotBlank() ||
                newUnitValue.isNotBlank()

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
        _pendingNewUnitsFlow.value = emptyList()
        _pendingDeletionsFlow.value = emptySet()
        newUnitName = ""
        newUnitValue = ""
        errorMessage = null
        nextTempId = -1L
    }

    /**
     * Upisuje sve nove, izmenjene i obrisane mere u bazu u jednoj atomičnoj transakciji.
     * Ako bilo koja ima grešku, ništa se ne upisuje u bazu i nesačuvana stanja ostaju sačuvana.
     */
    fun saveChanges(onSaved: () -> Unit) {
        if (!addPendingUnitFromForm(isExplicitAdd = false)) return

        val errors = validateDrafts()
        editErrors = errors
        if (errors.isNotEmpty()) return

        val currentPendingDeletions = _pendingDeletionsFlow.value
        val currentPendingNew = _pendingNewUnitsFlow.value.filter { it.id !in currentPendingDeletions }
        val currentDbUnits = units.value.filter { it.id > 0 && it.id !in currentPendingDeletions }.associateBy { it.id }
        val pendingDrafts = drafts

        val additions = currentPendingNew.map { pending ->
            val draft = pendingDrafts[pending.id]
            NewUnitSpec(
                countryCode = pending.countryCode,
                unitId = pending.unitId,
                displayName = (draft?.name ?: pending.displayName).trim(),
                valueInSqMeters = draft?.let { parsePositiveValue(it.value) } ?: pending.valueInSqMeters
            )
        }

        val updates = pendingDrafts.mapNotNull { (id, draft) ->
            if (id < 0 || id in currentPendingDeletions || id !in currentDbUnits) return@mapNotNull null
            val value = parsePositiveValue(draft.value) ?: return@mapNotNull null
            UpdateUnitSpec(
                id = id,
                displayName = draft.name.trim(),
                valueInSqMeters = value
            )
        }

        viewModelScope.launch {
            val result = repository.applyChanges(
                countryCode = selectedCountry.id,
                deletions = currentPendingDeletions,
                additions = additions,
                updates = updates
            )

            when (result) {
                ApplyChangesResult.Success -> {
                    discardChanges()
                    onSaved()
                }
                ApplyChangesResult.DuplicateName -> {
                    errorMessage = "Postoje izmene sa istim nazivom mere u izabranoj državi."
                }
                ApplyChangesResult.Invalid -> {
                    errorMessage = "Neispravni podaci u izmenama mera."
                }
                ApplyChangesResult.Error -> {
                    errorMessage = "Greška pri čuvanju izmena. Pokušajte ponovo."
                }
            }
        }
    }

    /** Uklanja meru iz lokalnog prikaza. Brisanje iz baze vrši se tek na "Sačuvaj". */
    fun deleteUnit(unitId: Long) {
        drafts = drafts - unitId
        if (unitId < 0) {
            _pendingNewUnitsFlow.value = _pendingNewUnitsFlow.value.filter { it.id != unitId }
        } else {
            _pendingDeletionsFlow.value = _pendingDeletionsFlow.value + unitId
        }
        editErrors = validateDrafts()
    }

    /** Dodaje novu meru u privremenu listu bez upisa u bazu. Baza se ažurira tek na "Sačuvaj". */
    fun addNewUnit() {
        addPendingUnitFromForm(isExplicitAdd = true)
    }

    /**
     * Dodaje novu meru iz forme u privremenu listu bez upisa u bazu.
     * @param isExplicitAdd true ako se poziva na dugme "Dodaj meru" (mora imati unose),
     *                      false ako se poziva iz `saveChanges` (dodaje samo ako je unet bar neki tekst).
     * @return true ako je operacija bila uspešna (ili nema ničeg za dodavanje u saveChanges),
     *         false ako je došlo do greške pri validaciji.
     */
    private fun addPendingUnitFromForm(isExplicitAdd: Boolean = false): Boolean {
        val hasInput = newUnitName.isNotBlank() || newUnitValue.isNotBlank()
        if (!hasInput && !isExplicitAdd) return true

        val value = validateNewUnitInput() ?: return false
        val name = newUnitName.trim()

        val normalized = normalizeName(name)
        val existingNames = units.value.map { normalizeName(drafts[it.id]?.name ?: it.displayName) }
        if (existingNames.any { it.equals(normalized, ignoreCase = true) }) {
            errorMessage = "Mera sa ovim nazivom već postoji u izabranoj državi."
            return false
        }

        val tempId = nextTempId--
        val newPendingUnit = LandUnitEntity(
            id = tempId,
            countryCode = selectedCountry.id,
            unitId = "custom_" + UUID.randomUUID(),
            displayName = name,
            valueInSqMeters = value,
            isSystem = false,
            sourceType = "USER_DEFINED"
        )

        _pendingNewUnitsFlow.value = _pendingNewUnitsFlow.value + newPendingUnit
        newUnitName = ""
        newUnitValue = ""
        errorMessage = null
        return true
    }

    /** Validira ulazna polja u formi za novu meru i postavlja `errorMessage` ako je neispravno. */
    private fun validateNewUnitInput(): Double? {
        val name = newUnitName.trim()
        val valueText = newUnitValue.trim()

        if (name.isEmpty()) {
            errorMessage = "Naziv mere je obavezan."
            return null
        }
        val value = parsePositiveValue(valueText)
        if (value == null) {
            errorMessage = "Unesite ispravnu pozitivnu vrednost u m²."
            return null
        }
        return value
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
