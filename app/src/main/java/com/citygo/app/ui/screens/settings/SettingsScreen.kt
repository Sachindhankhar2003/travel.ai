package com.citygo.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citygo.app.domain.model.CityData

private val Purple = Color(0xFF7C5CFC)
private val BgPurple = Color(0xFFFAF8FF)

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCitySheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPurple)
            .padding(bottom = innerPadding.calculateBottomPadding()),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Purple, Color(0xFFF97316))))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White, fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Customize your Travel.ai experience",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // City selection
        item {
            Spacer(Modifier.height(20.dp))
            SettingsGroupLabel("General")
        }
        item {
            SettingsCard {
                SettingsRow(
                    icon = Icons.Rounded.LocationCity,
                    title = "Home City",
                    subtitle = state.selectedCity,
                    onClick = { showCitySheet = true }
                )
            }
        }

        // Temperature unit
        item {
            Spacer(Modifier.height(16.dp))
            SettingsGroupLabel("Weather")
        }
        item {
            SettingsCard {
                LabeledToggleRow(
                    icon = Icons.Rounded.Thermostat,
                    title = "Temperature Unit",
                    optionA = "°C" to "C",
                    optionB = "°F" to "F",
                    selected = state.tempUnit,
                    onSelect = { viewModel.setTempUnit(it) }
                )
            }
        }

        // Language
        item {
            Spacer(Modifier.height(16.dp))
            SettingsGroupLabel("Language")
        }
        item {
            SettingsCard {
                LabeledToggleRow(
                    icon = Icons.Rounded.Language,
                    title = "App Language",
                    optionA = "English" to "en",
                    optionB = "हिन्दी" to "hi",
                    selected = state.language,
                    onSelect = { viewModel.setLanguage(it) }
                )
            }
        }

        // About
        item {
            Spacer(Modifier.height(16.dp))
            SettingsGroupLabel("About")
        }
        item {
            SettingsCard {
                SettingsInfoRow(Icons.Rounded.Info, "Version", "1.0.0")
                HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(horizontal = 4.dp))
                SettingsInfoRow(Icons.Rounded.Code, "Built with", "Kotlin + FastAPI")
            }
        }
    }

    // City picker sheet
    if (showCitySheet) {
        ModalBottomSheet(onDismissRequest = { showCitySheet = false }) {
            var query by remember { mutableStateOf("") }
            val cities = if (query.isEmpty()) CityData.cities
            else CityData.cities.filter { it.name.contains(query, ignoreCase = true) }
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Select City", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search…") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                cities.take(30).forEach { city ->
                    ListItem(
                        headlineContent = { Text(city.name) },
                        supportingContent = { Text(city.state) },
                        leadingContent = { Icon(Icons.Rounded.LocationOn, null, tint = Purple) },
                        modifier = Modifier.clickable { viewModel.setCity(city.name); showCitySheet = false }
                    )
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SettingsGroupLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = Purple,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle, color = Color.Gray) },
        leadingContent = { Icon(icon, null, tint = Purple) },
        trailingContent = { Icon(Icons.Rounded.ChevronRight, null, tint = Color.Gray) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        trailingContent = { Text(value, color = Color.Gray, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { Icon(icon, null, tint = Purple) }
    )
}

@Composable
private fun LabeledToggleRow(
    icon: ImageVector,
    title: String,
    optionA: Pair<String, String>,
    optionB: Pair<String, String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Purple)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = selected == optionA.second,
                onClick = { onSelect(optionA.second) },
                shape = SegmentedButtonDefaults.itemShape(0, 2)
            ) { Text(optionA.first) }
            SegmentedButton(
                selected = selected == optionB.second,
                onClick = { onSelect(optionB.second) },
                shape = SegmentedButtonDefaults.itemShape(1, 2)
            ) { Text(optionB.first) }
        }
    }
}

// Makes clickable available without import clash
@Composable
private fun Modifier.clickable(onClick: () -> Unit) =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
