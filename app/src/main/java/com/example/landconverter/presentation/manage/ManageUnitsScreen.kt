package com.example.landconverter.presentation.manage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.landconverter.domain.model.LandUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUnitsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageUnitsViewModel = hiltViewModel()
) {
    val selectedCountry = viewModel.selectedCountry
    val uiStrings = viewModel.uiStrings
    val units by viewModel.units.collectAsState()
    val editableUnits = remember(units) { units.filter { !it.isSystem } }

    var countryMenuExpanded by remember { mutableStateOf(false) }
    var unitToDelete by remember { mutableStateOf<LandUnit?>(null) }

    // Ako postoje nesačuvane izmene, napuštanje ekrana ili promena države traži potvrdu.
    var pendingDiscard by remember { mutableStateOf<(() -> Unit)?>(null) }
    val requestLeave: (() -> Unit) -> Unit = { action ->
        if (viewModel.hasUnsavedChanges) pendingDiscard = action else action()
    }
    BackHandler(enabled = viewModel.hasUnsavedChanges) { requestLeave(onNavigateBack) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiStrings.title) },
                navigationIcon = {
                    IconButton(onClick = { requestLeave(onNavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveChanges(onNavigateBack) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(uiStrings.saveButton)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Country Selection Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = countryMenuExpanded,
                    onExpandedChange = { countryMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${selectedCountry.flagEmoji}  ${selectedCountry.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(uiStrings.countryLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = countryMenuExpanded,
                        onDismissRequest = { countryMenuExpanded = false }
                    ) {
                        viewModel.countries.forEach { country ->
                            val isSelected = country.id == selectedCountry.id
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${country.flagEmoji}  ${country.displayName}",
                                        style = if (isSelected) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge
                                    )
                                },
                                trailingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = "Izabrano", tint = MaterialTheme.colorScheme.primary) }
                                } else null,
                                onClick = {
                                    countryMenuExpanded = false
                                    requestLeave { viewModel.onCountrySelected(country) }
                                }
                            )
                        }
                    }
                }
            }

            // Add New Unit Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            uiStrings.addSectionTitle(selectedCountry.displayName),
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = viewModel.newUnitName,
                            onValueChange = viewModel::onNewUnitNameChange,
                            label = { Text(uiStrings.unitNameLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = viewModel.newUnitValue,
                            onValueChange = viewModel::onNewUnitValueChange,
                            label = { Text(uiStrings.unitValueLabel) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        viewModel.errorMessage?.let { error ->
                            Text(
                                error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = { viewModel.addNewUnit() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(uiStrings.addUnitButton)
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(uiStrings.existingUnitsHeader, style = MaterialTheme.typography.titleMedium)
            }

            // Existing Units List (excluding system units m2, ar, ha)
            if (editableUnits.isEmpty()) {
                item {
                    Text(
                        uiStrings.emptyUnitsMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(editableUnits, key = { it.id }) { unit ->
                    val errors = viewModel.editErrors[unit.id]
                    UnitEditRow(
                        unit = unit,
                        nameLabel = uiStrings.unitNameFieldLabel,
                        deleteContentDescription = uiStrings.deleteUnitContentDescription,
                        nameError = errors?.nameError,
                        valueError = errors?.valueError,
                        draft = viewModel.drafts[unit.id],
                        onChange = { name, value ->
                            viewModel.onDraftChange(unit, name, value)
                        },
                        onDelete = {
                            unitToDelete = unit
                        }
                    )
                }
            }
        }

        // Potvrda odbacivanja nesačuvanih izmena
        pendingDiscard?.let { action ->
            AlertDialog(
                onDismissRequest = { pendingDiscard = null },
                title = { Text(uiStrings.unsavedChangesTitle) },
                text = { Text(uiStrings.unsavedChangesText) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.discardChanges()
                            pendingDiscard = null
                            action()
                        }
                    ) {
                        Text(uiStrings.discardButton, color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDiscard = null }) {
                        Text(uiStrings.continueEditingButton)
                    }
                }
            )
        }

        // Confirmation Dialog for Unit Deletion
        unitToDelete?.let { unit ->
            AlertDialog(
                onDismissRequest = { unitToDelete = null },
                title = { Text(uiStrings.confirmDeleteTitle) },
                text = {
                    Text(uiStrings.confirmDeleteText(unit.displayName))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteUnit(unit.id)
                            unitToDelete = null
                        }
                    ) {
                        Text(uiStrings.deleteConfirmButton, color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { unitToDelete = null }) {
                        Text(uiStrings.cancelButton)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarqueeOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    supportingText: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val enabled = true
    val colors = OutlinedTextFieldDefaults.colors()
    val textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface)
    val textMeasurer = rememberTextMeasurer()

    var containerWidthPx by remember { mutableIntStateOf(0) }

    val isOverflowing = remember(value, textStyle, containerWidthPx) {
        if (containerWidthPx <= 0 || value.isEmpty()) {
            false
        } else {
            val measuredTextWidth = textMeasurer.measure(
                text = value,
                style = textStyle,
                maxLines = 1
            ).size.width
            measuredTextWidth > containerWidthPx
        }
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        textStyle = textStyle,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = {
                    Box(
                        modifier = Modifier
                            .onSizeChanged { containerWidthPx = it.width }
                            .then(
                                if (isOverflowing) Modifier.basicMarquee(iterations = Int.MAX_VALUE) else Modifier
                            )
                    ) {
                        innerTextField()
                    }
                },
                enabled = enabled,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                isError = isError,
                label = label,
                supportingText = if (supportingText != null) {
                    { Text(supportingText) }
                } else null,
                colors = colors,
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = isError,
                        interactionSource = interactionSource,
                        colors = colors,
                        shape = OutlinedTextFieldDefaults.shape
                    )
                }
            )
        }
    )
}

@Composable
private fun UnitEditRow(
    unit: LandUnit,
    nameLabel: String,
    deleteContentDescription: String,
    draft: UnitDraft?,
    nameError: String?,
    valueError: String?,
    onChange: (name: String, value: String) -> Unit,
    onDelete: () -> Unit
) {
    // Dok traje izmena prikazuje se nacrt, inače vrednosti iz baze.
    val nameText = draft?.name ?: unit.displayName
    val valueText = draft?.value ?: formatUnitValue(unit.valueInSqMeters)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            MarqueeOutlinedTextField(
                value = nameText,
                onValueChange = { onChange(it, valueText) },
                label = { Text(nameLabel) },
                isError = nameError != null,
                supportingText = nameError,
                modifier = Modifier.weight(1f)
            )

            MarqueeOutlinedTextField(
                value = valueText,
                onValueChange = { onChange(nameText, it) },
                label = { Text("m²") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = valueError != null,
                supportingText = valueError,
                modifier = Modifier.width(110.dp)
            )

            IconButton(onClick = onDelete, modifier = Modifier.padding(top = 4.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = deleteContentDescription,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
