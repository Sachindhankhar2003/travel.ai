package com.citygo.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.citygo.app.domain.model.*
import com.citygo.app.ui.components.*
import com.citygo.app.ui.navigation.Screen
import com.google.accompanist.permissions.*
import kotlin.math.roundToInt

// ── Helper: convert Celsius to the display unit ───────────────────────────────
private fun Double.toDisplay(unit: String): String =
    if (unit == "F") "${((this * 9.0 / 5.0) + 32.0).roundToInt()}°F"
    else "${this.roundToInt()}°C"

private fun Double.toDisplayShort(unit: String): String =
    if (unit == "F") "${((this * 9.0 / 5.0) + 32.0).roundToInt()}°"
    else "${this.roundToInt()}°"

// ── Static travel tips ────────────────────────────────────────────────────────
private val travelTips = listOf(
    Triple(Icons.Rounded.Train,       "Use metro off-peak (before 9am / after 8pm) to avoid overcrowding.", Color(0xFF0F766E)),
    Triple(Icons.Rounded.WbSunny,     "Summers in Delhi NCR can exceed 45°C. Carry water and avoid afternoon outdoor trips.", Color(0xFFD97706)),
    Triple(Icons.Rounded.Umbrella,    "Monsoon (July–Sep) brings heavy traffic delays. Allow extra travel time.", Color(0xFF3B82F6)),
    Triple(Icons.Rounded.CurrencyRupee, "Negotiate auto-rickshaw fares before boarding in areas without app-cabs.", Color(0xFF7C5CFC)),
    Triple(Icons.Rounded.DirectionsBus, "DTC buses cover areas metros don't. Great for short hops within Delhi.", Color(0xFF059669)),
    Triple(Icons.Rounded.Air,         "AQI in Delhi-NCR can spike in winter (Oct–Jan). Keep N95 masks handy.", Color(0xFFDC2626)),
    Triple(Icons.Rounded.AccountBalanceWallet, "Book hotels 48h in advance in Jaipur/Agra — prices spike near weekends.", Color(0xFFF97316)),
    Triple(Icons.Rounded.Map,         "Agra's Taj Mahal entry is free on Fridays for Indian nationals.", Color(0xFF0EA5E9)),
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val locationPermission = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) { results ->
        if (results.any { it.value }) viewModel.onPermissionGranted()
        else viewModel.onPermissionDenied()
    }

    LaunchedEffect(Unit) {
        if (!uiState.locationPermissionGranted) {
            locationPermission.launchMultiplePermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            AnimatedVisibility(visible = uiState.isOffline) { OfflineBanner() }

            HomeHeader(
                cityName   = uiState.currentCity?.name ?: uiState.weather?.cityName ?: "Delhi NCR",
                tempUnit   = uiState.tempUnit,
                onCityPickerClick = { viewModel.onShowCityPicker() },
                onRefresh  = { viewModel.refresh() },
                onSettings = { navController.navigate("settings") }
            )

            when {
                uiState.isLoading -> {
                    Spacer(Modifier.height(16.dp))
                    ShimmerRow(modifier = Modifier.padding(horizontal = 16.dp), count = 4, height = 120)
                }
                uiState.weatherError != null && uiState.weather == null -> {
                    ErrorState(
                        message  = uiState.weatherError!!,
                        onRetry  = { viewModel.refresh() },
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
                uiState.weather != null -> {
                    val weather = uiState.weather!!
                    val unit    = uiState.tempUnit

                    WeatherHeroCard(weather = weather, unit = unit)
                    Spacer(Modifier.height(16.dp))
                    WeatherStatsRow(weather = weather)
                    Spacer(Modifier.height(24.dp))

                    SectionHeader(title = "Hourly Forecast", modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                    HourlyForecastRow(hourly = weather.hourlyForecast, unit = unit)
                    Spacer(Modifier.height(24.dp))

                    SectionHeader(title = "7-Day Forecast", modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                    DailyForecastList(daily = weather.dailyForecast, unit = unit)
                    Spacer(Modifier.height(24.dp))
                }
            }

            // Quick Access
            SectionHeader(title = "Quick Access", modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(12.dp))
            QuickAccessGrid(navController = navController)
            Spacer(Modifier.height(24.dp))

            // Travel Tips
            SectionHeader(title = "Travel Tips", modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(12.dp))
            TravelTipsSection()
            Spacer(Modifier.height(16.dp))
        }

        if (uiState.showCityPicker) {
            CityPickerBottomSheet(
                onDismiss     = { /* non-dismissible until city chosen */ },
                onCitySelected = { viewModel.onCitySelected(it) },
                canDismiss    = uiState.currentCity != null || uiState.weather != null
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(
    cityName: String,
    tempUnit: String,
    onCityPickerClick: () -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.LocationCity, null,
                    tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Travel.ai", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
                TextButton(
                    onClick            = onCityPickerClick,
                    contentPadding     = PaddingValues(0.dp),
                    modifier           = Modifier.height(28.dp)
                ) {
                    Text(cityName, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Rounded.KeyboardArrowDown, "Change city",
                        tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                }
            }
            // temp unit badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
            ) {
                Text(
                    "°$tempUnit",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.Refresh, "Refresh", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.Settings, "Settings", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

// ── Weather Hero Card ─────────────────────────────────────────────────────────
@Composable
private fun WeatherHeroCard(weather: Weather, unit: String) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = weather.temperature.toDisplay(unit),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text  = weather.weatherCode.toWeatherDescription(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Rain ${weather.precipProbability}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                        Text("Feels ${weather.feelsLike.toDisplayShort(unit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = weather.weatherCode.toWeatherDescription(),
                        modifier = Modifier.size(44.dp),
                        tint     = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// ── Stats Row ─────────────────────────────────────────────────────────────────
@Composable
private fun WeatherStatsRow(weather: Weather) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        WeatherStatChip("Humidity", "${weather.humidity}%", Icons.Rounded.WaterDrop, Modifier.weight(1f))
        WeatherStatChip("Wind",     "${weather.windSpeed.roundToInt()} km/h", Icons.Rounded.Air, Modifier.weight(1f))
        WeatherStatChip("Rain",     "${weather.precipProbability}%", Icons.Rounded.Umbrella, Modifier.weight(1f))
    }
}

@Composable
private fun WeatherStatChip(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Hourly Forecast ───────────────────────────────────────────────────────────
@Composable
private fun HourlyForecastRow(hourly: List<HourlyForecast>, unit: String) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(hourly.take(24)) { item -> HourlyCard(item = item, unit = unit) }
    }
}

@Composable
private fun HourlyCard(item: HourlyForecast, unit: String) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier              = Modifier.width(62.dp).padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(6.dp)
        ) {
            val hour = item.time.substringAfterLast("T").take(5)
            Text(hour, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Rounded.WbSunny, item.weatherCode.toWeatherDescription(),
                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
            Text(item.temperature.toDisplayShort(unit),
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            if (item.precipProbability > 0) {
                Text("${item.precipProbability}%", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
            }
        }
    }
}

// ── Daily Forecast ────────────────────────────────────────────────────────────
@Composable
private fun DailyForecastList(daily: List<DailyForecast>, unit: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        daily.forEach { item -> DailyForecastRow(item = item, unit = unit) }
    }
}

@Composable
private fun DailyForecastRow(item: DailyForecast, unit: String) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment   = Alignment.CenterVertically
        ) {
            val dayDisplay = try {
                val parts  = item.date.split("-")
                val months = listOf("","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                "${months[parts[1].toInt()]} ${parts[2]}"
            } catch (e: Exception) { item.date }
            Text(dayDisplay, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.WbSunny, item.weatherCode.toWeatherDescription(),
                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("${item.precipProbability}%", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(32.dp))
            Text(
                "${item.tempMax.toDisplayShort(unit)} / ${item.tempMin.toDisplayShort(unit)}",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.width(88.dp)
            )
        }
    }
}

// ── Quick Access Grid ─────────────────────────────────────────────────────────
@Composable
private fun QuickAccessGrid(navController: NavController) {
    val items = listOf(
        Triple("Metro Routes",  Icons.Rounded.Train,          Screen.Transit.route),
        Triple("Bus Search",    Icons.Rounded.DirectionsBus,  Screen.Transit.route),
        Triple("Explore Places",Icons.Rounded.Explore,        Screen.Explore.route),
        Triple("Map & Routes",  Icons.Rounded.Map,            Screen.Map.route)
    )
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items.take(2).forEach { (label, icon, route) ->
                QuickAccessCard(label, icon, { navController.navigate(route) }, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items.drop(2).forEach { (label, icon, route) ->
                QuickAccessCard(label, icon, { navController.navigate(route) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickAccessCard(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick   = onClick,
        modifier  = modifier.height(80.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, label,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── Travel Tips ───────────────────────────────────────────────────────────────
@Composable
private fun TravelTipsSection() {
    val tipIndex = remember { (travelTips.indices).random() }
    val tip      = travelTips[tipIndex]

    Column(
        modifier              = Modifier.padding(horizontal = 16.dp),
        verticalArrangement   = Arrangement.spacedBy(10.dp)
    ) {
        // Featured tip card (larger)
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = tip.third.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border    = BorderStroke(1.dp, tip.third.copy(alpha = 0.25f))
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tip.third.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(tip.first, null, tint = tip.third, modifier = Modifier.size(24.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tip of the Day", style = MaterialTheme.typography.labelSmall,
                        color = tip.third, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(tip.second, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                }
            }
        }

        // Two more smaller tips side by side
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val t1 = travelTips[(tipIndex + 1) % travelTips.size]
            val t2 = travelTips[(tipIndex + 2) % travelTips.size]
            listOf(t1, t2).forEach { t ->
                Card(
                    modifier  = Modifier.weight(1f),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(t.third.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(t.first, null, tint = t.third, modifier = Modifier.size(20.dp))
                        }
                        Text(t.second, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp,
                            maxLines = 4)
                    }
                }
            }
        }
    }
}

// ── City Picker Bottom Sheet ──────────────────────────────────────────────────
@Composable
private fun CityPickerBottomSheet(
    onDismiss: () -> Unit,
    onCitySelected: (City) -> Unit,
    canDismiss: Boolean
) {
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    val allCities   = CityData.cities
    val filtered    = if (searchQuery.isEmpty()) allCities
    else allCities.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.state.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = { if (canDismiss) onDismiss() },
        sheetState       = sheetState,
        dragHandle       = { BottomSheetDefaults.DragHandle() },
        containerColor   = Color.White
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Select City", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder   = { Text("Search cities…") },
                leadingIcon   = { Icon(Icons.Rounded.Search, null) },
                trailingIcon  = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Rounded.Close, "Clear") }
                    }
                },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(14.dp),
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.height(8.dp))

            if (searchQuery.isEmpty()) {
                Text("Popular Cities", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CityData.popularCities) { city ->
                        FilterChip(
                            selected = false,
                            onClick  = { onCitySelected(city) },
                            label    = { Text(city.name) },
                            leadingIcon = { Icon(Icons.Rounded.LocationOn, null, Modifier.size(16.dp)) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("All Cities", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 6.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.heightIn(max = 420.dp)
            ) {
                items(filtered) { city ->
                    ListItem(
                        headlineContent  = { Text(city.name) },
                        supportingContent = { Text(city.state) },
                        leadingContent   = {
                            Icon(Icons.Rounded.LocationOn, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCitySelected(city) }
                    )
                }
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}
