package com.citygo.app.ui.screens.transit

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citygo.app.domain.model.*
import com.citygo.app.ui.components.*

@Composable
fun TransitScreen(
    innerPadding: PaddingValues,
    viewModel: TransitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    "Transit",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Metro, Bus & more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(16.dp))

                // Tab Row
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    indicator = { tabPositions ->
                        if (uiState.selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.onTabSelected(0) },
                        text = { Text("Metro") },
                        icon = { Icon(Icons.Rounded.Train, null, Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.onTabSelected(1) },
                        text = { Text("Bus") },
                        icon = { Icon(Icons.Rounded.DirectionsBus, null, Modifier.size(18.dp)) }
                    )
                }
            }
        }

        // Content
        AnimatedContent(
            targetState = uiState.selectedTab,
            transitionSpec = {
                fadeIn() + slideInHorizontally { if (targetState > initialState) it else -it } togetherWith
                fadeOut() + slideOutHorizontally { if (targetState > initialState) -it else it }
            }
        ) { tab ->
            when (tab) {
                0 -> MetroTab(uiState = uiState.metro, viewModel = viewModel)
                1 -> BusTab(uiState = uiState.bus, viewModel = viewModel)
            }
        }
    }
}

// ─── Metro Tab ────────────────────────────────────────────────────────────────
@Composable
private fun MetroTab(
    uiState: MetroUiState,
    viewModel: TransitViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // From/To Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Find Metro Route",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))

                    // From
                    OutlinedTextField(
                        value = uiState.fromStation,
                        onValueChange = viewModel::onMetroFromChange,
                        label = { Text("From Station") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.RadioButtonChecked,
                                    null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Station suggestions for From
                    if (uiState.fromResults.isNotEmpty()) {
                        StationSuggestionsDropdown(
                            stations = uiState.fromResults,
                            onSelect = viewModel::onMetroFromSelected
                        )
                    }

                    // Swap button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(
                            onClick = viewModel::swapMetroStations,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                Icons.Rounded.SwapVert,
                                contentDescription = "Swap stations",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // To
                    OutlinedTextField(
                        value = uiState.toStation,
                        onValueChange = viewModel::onMetroToChange,
                        label = { Text("To Station") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.LocationOn,
                                    null,
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Station suggestions for To
                    if (uiState.toResults.isNotEmpty()) {
                        StationSuggestionsDropdown(
                            stations = uiState.toResults,
                            onSelect = viewModel::onMetroToSelected
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = viewModel::searchMetroRoute,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState.fromStation.isNotBlank() && uiState.toStation.isNotBlank()
                    ) {
                        Icon(Icons.Rounded.Search, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Find Route")
                    }
                }
            }
        }

        // Nearest Stations
        if (uiState.nearestStations.isNotEmpty()) {
            item {
                SectionHeader(title = "Nearby Stations")
            }
            items(uiState.nearestStations) { station ->
                NearestStationCard(station = station)
            }
        }

        // Loading
        if (uiState.isLoading) {
            item { ShimmerCard(height = 200) }
        }

        // Error
        if (uiState.error != null && !uiState.isLoading) {
            item {
                ErrorState(
                    message = uiState.error!!,
                    onRetry = viewModel::searchMetroRoute
                )
            }
        }

        // Route Result
        if (uiState.route != null && !uiState.isLoading) {
            item { MetroRouteCard(route = uiState.route!!, isFromCache = uiState.isFromCache) }
        }
    }
}

@Composable
private fun StationSuggestionsDropdown(
    stations: List<MetroStation>,
    onSelect: (MetroStation) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        stations.take(5).forEach { station ->
            ListItem(
                headlineContent = { Text(station.name) },
                supportingContent = { Text(station.line) },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                try { Color(android.graphics.Color.parseColor(station.lineColor)) }
                                catch (e: Exception) { MaterialTheme.colorScheme.primary }
                            )
                    )
                },
                modifier = Modifier.clickable { onSelect(station) }
            )
        }
    }
}

@Composable
private fun NearestStationCard(station: MetroStation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        try { Color(android.graphics.Color.parseColor(station.lineColor)).copy(alpha = 0.15f) }
                        catch (e: Exception) { MaterialTheme.colorScheme.primaryContainer }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Train,
                    contentDescription = null,
                    tint = try { Color(android.graphics.Color.parseColor(station.lineColor)) }
                           catch (e: Exception) { MaterialTheme.colorScheme.primary },
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(station.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(station.line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "First: ${station.firstTrain}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Last: ${station.lastTrain}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun MetroRouteCard(route: MetroRoute, isFromCache: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Metro Route",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                if (isFromCache) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "Cached",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // From → To
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.RadioButtonChecked, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(route.fromStation, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .padding(start = 7.dp)
                    .width(2.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(route.toStation, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetroStat("Stations", "${route.stationCount}")
                MetroStat("Fare", "₹${route.fare.toInt()}")
                MetroStat("Time", "${route.estimatedTimeMinutes} min")
            }

            Spacer(Modifier.height(12.dp))

            // Interchange info
            if (route.interchangeStations.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Rounded.SwapHoriz,
                        null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Change at: ${route.interchangeStations.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // Train times
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "First Train: ${route.firstTrain}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
                Text(
                    "Last Train: ${route.lastTrain}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun MetroStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

// ─── Bus Tab ──────────────────────────────────────────────────────────────────
@Composable
private fun BusTab(
    uiState: BusUiState,
    viewModel: TransitViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Search Bus Routes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.fromQuery,
                    onValueChange = viewModel::onBusFromChange,
                    label = { Text("From") },
                    leadingIcon = { Icon(Icons.Rounded.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.toQuery,
                    onValueChange = viewModel::onBusToChange,
                    label = { Text("To") },
                    leadingIcon = { Icon(Icons.Rounded.LocationOn, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = viewModel::searchBuses,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.fromQuery.isNotBlank() && uiState.toQuery.isNotBlank()
                ) {
                    Icon(Icons.Rounded.Search, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Search Buses")
                }
            }
        }

        // Filter chips (show after search)
        if (uiState.hasSearched) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All" to OperatorType.ALL,
                    "Government" to OperatorType.GOVERNMENT,
                    "Private" to OperatorType.PRIVATE
                ).forEach { (label, type) ->
                    FilterChip(
                        selected = uiState.selectedFilter == type,
                        onClick = { viewModel.onBusFilterChange(type) },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Content
        when {
            uiState.isLoading -> {
                ShimmerRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    count = 3,
                    height = 130
                )
            }
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error!!,
                    onRetry = viewModel::searchBuses
                )
            }
            uiState.hasSearched && uiState.filteredBuses.isEmpty() -> {
                EmptyState(
                    title = "No buses found",
                    subtitle = "Try different source/destination or filter",
                    icon = Icons.Rounded.DirectionsBusFilledRound
                )
            }
            uiState.filteredBuses.isNotEmpty() -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "${uiState.filteredBuses.size} buses found",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(uiState.filteredBuses, key = { it.id }) { bus ->
                        BusCard(bus = bus)
                    }
                }
            }
        }
    }
}

@Composable
private fun BusCard(bus: Bus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Route number
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        bus.routeNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${bus.from} → ${bus.to}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        bus.operatorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OperatorBadge(isGovernment = bus.operatorType == OperatorType.GOVERNMENT)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BusStat(Icons.Rounded.CurrencyRupee, "₹${bus.fare.toInt()}", "Fare")
                BusStat(Icons.Rounded.Schedule, "Every ${bus.frequencyMinutes}m", "Frequency")
                BusStat(Icons.Rounded.Place, "${bus.stops.size} stops", "Stops")
            }

            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "First: ${bus.firstBus}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Last: ${bus.lastBus}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
                if (bus.isAC) {
                    Text(
                        "AC",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BusStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        Column {
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
