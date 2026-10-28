package com.example.landconverter.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

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
            DropdownField(
                label = strings.countryLabel,
                selectedText = country.displayName,
                options = viewModel.countries,
                optionLabel = { it.displayName },
                onOptionSelected = viewModel::onCountryChange
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

            DropdownField(
                label = strings.fromUnitLabel,
                selectedText = fromUnit?.displayName ?: "",
                options = availableUnits,
                optionLabel = { it.displayName },
                onOptionSelected = viewModel::onFromUnitChange
            )

            DropdownField(
                label = strings.toUnitLabel,
                selectedText = toUnit?.displayName ?: "",
                options = availableUnits,
                optionLabel = { it.displayName },
                onOptionSelected = viewModel::onToUnitChange
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
                Text("Upravljaj merama")
            }
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
private fun <T> DropdownField(
    label: String,
    selectedText: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
