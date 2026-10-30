package com.djes021.landconverter.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.djes021.landconverter.data.model.BalkanCountry
import com.djes021.landconverter.domain.model.LandUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    onNavigateToManageUnits: (String) -> Unit,
    viewModel: ConverterViewModel = hiltViewModel()
) {
    val strings = viewModel.uiStrings
    val country = viewModel.selectedCountry
    val error = viewModel.inputError
    val availableUnits by viewModel.availableUnits.collectAsState()

    val fromUnit = viewModel.fromUnit
    val toUnit = viewModel.toUnit

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { CenterAlignedTopAppBar(title = { Text(strings.appTitle) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CountryDropdownField(
                label = strings.countryLabel,
                selectedCountry = country,
                countries = viewModel.countries,
                onCountrySelected = viewModel::onCountryChange
            )

            OutlinedTextField(
                value = viewModel.inputAmount,
                onValueChange = viewModel::onInputChange,
                label = { Text(strings.amountLabel) },
                isError = error != null,
                supportingText = { error?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            UnitDropdownField(
                label = strings.fromUnitLabel,
                selectedUnit = fromUnit,
                units = availableUnits,
                onUnitSelected = viewModel::onFromUnitChange
            )

            UnitDropdownField(
                label = strings.toUnitLabel,
                selectedUnit = toUnit,
                units = availableUnits,
                onUnitSelected = viewModel::onToUnitChange
            )

            ResultCard(
                title = strings.resultTitle,
                expression = if (error == null && fromUnit != null) {
                    "${viewModel.inputAmount.ifBlank { "0" }} ${fromUnit.displayName} ="
                } else "—",
                result = if (toUnit != null) "${viewModel.resultAmount} ${toUnit.displayName}" else viewModel.resultAmount
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onNavigateToManageUnits(country.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.manageUnitsButton)
            }
        }

        // Welcome / How it works Dialog (shown only on first run)
        if (viewModel.showWelcomeDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissWelcomeDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { Text("O aplikaciji LandConverter") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "LandConverter vam omogućava brzo i jednostavno preračunavanje površina zemljišta između standardnih i regionalnih mera.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• Izbor države: Izaberite državu/region za pristup specifičnim lokalnim merama (npr. Dulum, Motika, Ralo, Lanac, Jutro).",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Preračun: Unesite vrednost i izaberite početnu i ciljnu meru za automatski preračun u realnom vremenu.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Upravljanje merama: Klikom na \"Upravljaj merama\" možete prilagoditi vrednosti postojećih mera ili dodati sopstvene mere.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.dismissWelcomeDialog() }) {
                        Text("Razumem")
                    }
                }
            )
        }
    }
}

@Composable
private fun ResultCard(title: String, expression: String, result: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = expression,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = result,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdownField(
    label: String,
    selectedCountry: BalkanCountry,
    countries: List<BalkanCountry>,
    onCountrySelected: (BalkanCountry) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = "${selectedCountry.flagEmoji}  ${selectedCountry.displayName}",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            countries.forEach { item ->
                val isSelected = item.id == selectedCountry.id
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${item.flagEmoji}  ${item.displayName}",
                            style = if (isSelected) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge
                        )
                    },
                    trailingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = "Izabrano", tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    onClick = {
                        onCountrySelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdownField(
    label: String,
    selectedUnit: LandUnit?,
    units: List<LandUnit>,
    onUnitSelected: (LandUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedUnit?.displayName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                val isSelected = unit.id == selectedUnit?.id
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = unit.displayName,
                                style = if (isSelected) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = formatSqmValue(unit.valueInSqMeters),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    trailingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = "Izabrano", tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatSqmValue(value: Double): String =
    value.toBigDecimal().stripTrailingZeros().toPlainString() + " m²"
