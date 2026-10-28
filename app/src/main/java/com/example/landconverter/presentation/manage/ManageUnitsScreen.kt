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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.landconverter.data.local.LandUnitEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUnitsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageUnitsViewModel = hiltViewModel()
) {
    val selectedCountry = viewModel.selectedCountry
    val units by viewModel.units.collectAsState()
    val editableUnits = remember(units) { units.filter { !it.isSystem } }

    var countryMenuExpanded by remember { mutableStateOf(false) }
    var unitToDelete by remember { mutableStateOf<LandUnitEntity?>(null) }

    // Ako postoje nesačuvane izmene, napuštanje ekrana ili promena države traži potvrdu.
    var pendingDiscard by remember { mutableStateOf<(() -> Unit)?>(null) }
    val requestLeave: (() -> Unit) -> Unit = { action ->
        if (viewModel.hasUnsavedChanges) pendingDiscard = action else action()
    }
    BackHandler(enabled = viewModel.hasUnsavedChanges) { requestLeave(onNavigateBack) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upravljaj merama") },
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
                        Text("Sačuvaj")
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
                        value = selectedCountry.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Država / sistem") },
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
                            DropdownMenuItem(
                                text = { Text(country.displayName) },
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
                            "Dodaj novu meru za ${selectedCountry.displayName}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = viewModel.newUnitName,
                            onValueChange = viewModel::onNewUnitNameChange,
                            label = { Text("Naziv mere (npr. Motika)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = viewModel.newUnitValue,
                            onValueChange = viewModel::onNewUnitValueChange,
                            label = { Text("Vrednost u m²") },
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
                            Text("Dodaj meru")
                        }
                    }
                }
            }

            // Section Header
            item {
                Text("Postojeće mere", style = MaterialTheme.typography.titleMedium)
            }

            // Existing Units List (excluding system units m2, ar, ha)
            if (editableUnits.isEmpty()) {
                item {
                    Text(
                        "Nema prilagođenih mera za ovu državu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(editableUnits, key = { it.id }) { unit ->
                    val errors = viewModel.editErrors[unit.id]
                    UnitEditRow(
                        unit = unit,
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
                title = { Text("Nesačuvane izmene") },
                text = { Text("Imate izmene koje nisu sačuvane. Da li želite da ih odbacite?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.discardChanges()
                            pendingDiscard = null
                            action()
                        }
                    ) {
                        Text("Odbaci", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDiscard = null }) {
                        Text("Nastavi uređivanje")
                    }
                }
            )
        }

        // Confirmation Dialog for Unit Deletion
        unitToDelete?.let { unit ->
            AlertDialog(
                onDismissRequest = { unitToDelete = null },
                title = { Text("Potvrda brisanja") },
                text = {
                    Text("Da li ste sigurni da želite da obrišete meru \"${unit.displayName}\"? Izmena će biti sačuvana tek kada pritisnete \"Sačuvaj\".")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteUnit(unit.id)
                            unitToDelete = null
                        }
                    ) {
                        Text("Obriši", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { unitToDelete = null }) {
                        Text("Otkaži")
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
    unit: LandUnitEntity,
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
                label = { Text("Naziv") },
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
                    contentDescription = "Obriši meru",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}