package com.djes021.landconverter.presentation.manage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djes021.landconverter.data.model.BalkanCountry
import com.djes021.landconverter.data.repository.ApplyChangesResult
import com.djes021.landconverter.data.repository.LandUnitRepository
import com.djes021.landconverter.data.repository.NewUnitSpec
import com.djes021.landconverter.data.repository.UpdateUnitSpec
import com.djes021.landconverter.domain.model.LandUnit
import com.djes021.landconverter.domain.util.MAX_UNIT_NAME_LENGTH
import com.djes021.landconverter.domain.util.isValidUnitValue
import com.djes021.landconverter.domain.util.normalizeUnitName
import com.djes021.landconverter.domain.util.parsePositiveNumber
import com.djes021.landconverter.domain.util.parseUnitValue
import com.djes021.landconverter.presentation.CountryStringProvider
import com.djes021.landconverter.presentation.ManageUnitsUiStrings
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

    val uiStrings: ManageUnitsUiStrings
        get() = CountryStringProvider.getManageUnitsStrings(selectedCountry)

    val countries = BalkanCountry.all

    private val _selectedCountryFlow = MutableStateFlow(selectedCountry)

    /** Nove i obrisane mere se pamte kao Compose stanje radi direktnog praćenja u UI-ju. */
    var pendingNewUnits: List<LandUnit> by mutableStateOf(emptyList())
        private set

    var pendingDeletions: Set<Long> by mutableStateOf(emptySet())
        private set

    private var nextTempId = -1L

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dbUnits: StateFlow<List<LandUnit>> = _selectedCountryFlow
        .flatMapLatest { country -> repository.observeUnitsForCountry(country.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Lista za prikaz u UI-ju (baza + nove - obrisane). */
    val units: StateFlow<List<LandUnit>> = combine(
        dbUnits,
        snapshotFlow { pendingNewUnits },
        snapshotFlow { pendingDeletions }
    ) { db, pendingNew, pendingDel ->
        composeVisibleUnits(db, pendingNew, pendingDel, selectedCountry.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Ista lista, ali izračunata odmah iz trenutnog stanja. Sva logika (provere duplikata,
     * validacija, čuvanje) koristi ovu funkciju, a ne `units.value`, koji kasni za jedan ciklus.
     */
    private fun visibleUnits(): List<LandUnit> =
        composeVisibleUnits(dbUnits.value, pendingNewUnits, pendingDeletions, selectedCountry.id)

    private fun composeVisibleUnits(
        db: List<LandUnit>,
        pendingNew: List<LandUnit>,
        pendingDel: Set<Long>,
        countryId: String
    ): List<LandUnit> =
        (db + pendingNew).filter { it.countryCode == countryId && it.id !in pendingDel }

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
                pendingNewUnits.isNotEmpty() ||
                pendingDeletions.isNotEmpty() ||
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
    fun onDraftChange(unit: LandUnit, name: String, value: String) {
        val unchanged = name == unit.displayName && value == formatUnitValue(unit.valueInSqMeters)
        drafts = if (unchanged) drafts - unit.id else drafts + (unit.id to UnitDraft(name, value))
        editErrors = validateDrafts()
    }

    fun discardChanges() {
        drafts = emptyMap()
        editErrors = emptyMap()
        pendingNewUnits = emptyList()
        pendingDeletions = emptySet()
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

        val currentPendingDeletions = pendingDeletions
        val currentPendingNew = pendingNewUnits.filter { it.id !in currentPendingDeletions }
        val currentDbUnits = visibleUnits().filter { it.id > 0 }.associateBy { it.id }
        val pendingDrafts = drafts

        val additions = currentPendingNew.map { pending ->
            val draft = pendingDrafts[pending.id]
            NewUnitSpec(
                countryCode = pending.countryCode,
                unitId = pending.unitId,
                displayName = (draft?.name ?: pending.displayName).trim(),
                valueInSqMeters = draft?.let { parseUnitValue(it.value) } ?: pending.valueInSqMeters
            )
        }

        val updates = pendingDrafts.mapNotNull { (id, draft) ->
            if (id < 0 || id in currentPendingDeletions || id !in currentDbUnits) return@mapNotNull null
            val value = parseUnitValue(draft.value) ?: return@mapNotNull null
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
                    errorMessage = uiStrings.errDuplicateNameInState
                }
                ApplyChangesResult.Invalid -> {
                    errorMessage = uiStrings.errInvalidData
                }
                ApplyChangesResult.Error -> {
                    errorMessage = uiStrings.errSaveFailed
                }
            }
        }
    }

    /** Uklanja meru iz lokalnog prikaza. Brisanje iz baze vrši se tek na "Sačuvaj". */
    fun deleteUnit(unitId: Long) {
        drafts = drafts - unitId
        if (unitId < 0) {
            pendingNewUnits = pendingNewUnits.filter { it.id != unitId }
        } else {
            pendingDeletions = pendingDeletions + unitId
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
        val name = normalizeUnitName(newUnitName)

        val existingNames = visibleUnits().map { normalizeUnitName(drafts[it.id]?.name ?: it.displayName) }
        if (existingNames.any { it.equals(name, ignoreCase = true) }) {
            errorMessage = uiStrings.errDuplicateName
            return false
        }

        val tempId = nextTempId--
        val newPendingUnit = LandUnit(
            id = tempId,
            countryCode = selectedCountry.id,
            unitId = "custom_" + UUID.randomUUID(),
            displayName = name,
            valueInSqMeters = value,
            isSystem = false
        )

        pendingNewUnits = pendingNewUnits + newPendingUnit
        newUnitName = ""
        newUnitValue = ""
        errorMessage = null
        return true
    }

    /** Validira ulazna polja u formi za novu meru i postavlja `errorMessage` ako je neispravno. */
    private fun validateNewUnitInput(): Double? {
        val name = normalizeUnitName(newUnitName)
        if (name.isEmpty()) {
            errorMessage = uiStrings.errNameRequired
            return null
        }
        if (name.length > MAX_UNIT_NAME_LENGTH) {
            errorMessage = uiStrings.errNameTooLong
            return null
        }
        val valueError = valueErrorFor(newUnitValue)
        if (valueError != null) {
            errorMessage = valueError
            return null
        }
        return parseUnitValue(newUnitValue)
    }

    /** Poruka greške za unos vrednosti (nevalidan broj ili van dozvoljenog opsega), ili null. */
    private fun valueErrorFor(text: String): String? {
        val number = parsePositiveNumber(text) ?: return uiStrings.errValueInvalid
        return if (isValidUnitValue(number)) null else uiStrings.errValueOutOfRange
    }

    /** Proverava sve nacrte, uključujući duplikate naziva među merama izabrane države. */
    private fun validateDrafts(): Map<Long, UnitEditErrors> {
        val effectiveNames = visibleUnits().associate { unit ->
            unit.id to normalizeUnitName(drafts[unit.id]?.name ?: unit.displayName)
        }
        return drafts.mapNotNull { (id, draft) ->
            val name = normalizeUnitName(draft.name)
            val nameError = when {
                name.isEmpty() -> uiStrings.errNameRequired
                name.length > MAX_UNIT_NAME_LENGTH -> uiStrings.errNameTooLong
                effectiveNames.any { (otherId, other) ->
                    otherId != id && other.equals(name, ignoreCase = true)
                } -> uiStrings.errDuplicateName
                else -> null
            }
            val valueError = valueErrorFor(draft.value)
            if (nameError != null || valueError != null) id to UnitEditErrors(nameError, valueError) else null
        }.toMap()
    }

}