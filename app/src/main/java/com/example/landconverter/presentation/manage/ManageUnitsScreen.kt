package com.example.landconverter.presentation.manage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upravljaj merama") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                },
                actions = {
                    Button(
                        onClick = { onNavigateBack() },
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
                                    viewModel.onCountrySelected(country)
                                    countryMenuExpanded = false
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
                    UnitEditRow(
                        unit = unit,
                        onUpdate = { name, value ->
                            viewModel.updateExistingUnit(unit, name, value)
                        },
                        onDelete = {
                            unitToDelete = unit
                        }
                    )
                }
            }
        }

        // Confirmation Dialog for Unit Deletion
        unitToDelete?.let { unit ->
            AlertDialog(
                onDismissRequest = { unitToDelete = null },
                title = { Text("Potvrda brisanja") },
                text = {
                    Text("Da li ste sigurni da želite da obrišete meru \"${unit.displayName}\"? Izmena će biti vidljiva nakon što kliknete na dugme Sačuvaj.")
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

@Composable
private fun UnitEditRow(
    unit: LandUnitEntity,
    onUpdate: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    var nameText by remember(unit.id, unit.displayName) { mutableStateOf(unit.displayName) }
    var valueText by remember(unit.id, unit.valueInSqMeters) { mutableStateOf(unit.valueInSqMeters.toString()) }

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nameText,
                onValueChange = {
                    nameText = it
                    onUpdate(it, valueText)
                },
                label = { Text("Naziv") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = valueText,
                onValueChange = {
                    valueText = it
                    onUpdate(nameText, it)
                },
                label = { Text("m²") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(110.dp)
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Obriši meru",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
