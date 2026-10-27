package com.citygo.app.ui.screens.map

import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citygo.app.domain.model.*
import com.citygo.app.ui.components.ErrorState
import com.citygo.app.ui.components.ShimmerCard
import com.citygo.app.ui.theme.Teal60
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapScreen(
    innerPadding: PaddingValues,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Init osmdroid config
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osm_prefs", 0))
            userAgentValue = "CityGo/1.0"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // OSMDroid Map
        var mapView by remember { mutableStateOf<MapView?>(null) }

        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(14.0)
                    controller.setCenter(GeoPoint(28.6139, 77.2090))
                    isVerticalMapRepetitionEnabled = false
                    setScrollableAreaLimitLatitude(
                        MapView.getTileSystem().maxLatitude,
                        MapView.getTileSystem().minLatitude,
                        0
                    )
                }.also { mapView = it }
            },
            update = { map ->
                // Update camera to current location
                uiState.currentLocation?.let { loc ->
                    map.controller.animateTo(GeoPoint(loc.lat, loc.lng))
                }

                // Clear overlays and redraw
                map.overlays.clear()

                // Current location marker
                uiState.currentLocation?.let { loc ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(loc.lat, loc.lng)
                        title = "Your Location"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(marker)
                }

                // Route polyline
                uiState.route?.let { route ->
                    if (route.coordinates.isNotEmpty()) {
                        val polyline = Polyline(map).apply {
                            setPoints(route.coordinates.map { GeoPoint(it.lat, it.lng) })
                            outlinePaint.color = Teal60.toArgb()
                            outlinePaint.strokeWidth = 10f
                            outlinePaint.style = Paint.Style.STROKE
                        }
                        map.overlays.add(polyline)

                        // Destination marker
                        uiState.destination?.let { dest ->
                            val marker = Marker(map).apply {
                                position = GeoPoint(dest.latLng.lat, dest.latLng.lng)
                                title = dest.name
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            map.overlays.add(marker)
                        }

                        // Zoom to route bounds
                        val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(
                            route.coordinates.map { GeoPoint(it.lat, it.lng) }
                        )
                        map.zoomToBoundingBox(boundingBox.increaseByScale(1.3f), true)
                    }
                }

                map.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Search overlay at top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            MapSearchBar(
                uiState = uiState,
                onQueryChange = viewModel::onSearchQueryChange,
                onPlaceSelected = viewModel::onPlaceSelected,
                onSetMode = viewModel::setSearchMode,
                onClearRoute = viewModel::clearRoute
            )
        }

        // Route info card at bottom
        AnimatedVisibility(
            visible = uiState.route != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = innerPadding.calculateBottomPadding() + 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            uiState.route?.let { route ->
                RouteInfoCard(
                    route = route,
                    destination = uiState.destination?.name ?: "",
                    formatDistance = viewModel::formatDistance,
                    formatDuration = viewModel::formatDuration,
                    onDismiss = viewModel::clearRoute
                )
            }
        }

        // Routing loading
        if (uiState.isRouting) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    Text("Calculating route...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Route error
        if (uiState.routeError != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                action = {
                    TextButton(onClick = viewModel::clearRoute) { Text("Dismiss") }
                }
            ) {
                Text(uiState.routeError!!)
            }
        }
    }
}

@Composable
private fun MapSearchBar(
    uiState: MapUiState,
    onQueryChange: (String) -> Unit,
    onPlaceSelected: (Place) -> Unit,
    onSetMode: (SearchMode) -> Unit,
    onClearRoute: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main search row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = {
                        onSetMode(SearchMode.DESTINATION)
                        onQueryChange(it)
                        isExpanded = it.isNotEmpty()
                    },
                    placeholder = {
                        Text(
                            "Search destination...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
                if (uiState.destination != null) {
                    IconButton(onClick = onClearRoute, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Rounded.Close, "Clear", modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Destination selected chip
            uiState.destination?.let {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = "Destination",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        it.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
            }

            // Search results dropdown
            AnimatedVisibility(visible = uiState.searchResults.isNotEmpty() || uiState.isSearching) {
                HorizontalDivider()
                Column {
                    if (uiState.isSearching) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            ShimmerCard(height = 48)
                        }
                    }
                    uiState.searchResults.take(6).forEach { place ->
                        ListItem(
                            headlineContent = {
                                Text(place.name, style = MaterialTheme.typography.bodyMedium)
                            },
                            supportingContent = {
                                Text(
                                    place.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Rounded.Place,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.clickable {
                                onPlaceSelected(place)
                                isExpanded = false
                            }
                        )
                    }
                }
            }

            // Recent searches when empty
            AnimatedVisibility(
                visible = uiState.searchQuery.isEmpty() &&
                        uiState.recentSearches.isNotEmpty() &&
                        uiState.destination == null
            ) {
                Column {
                    HorizontalDivider()
                    Text(
                        "Recent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    uiState.recentSearches.take(3).forEach { place ->
                        ListItem(
                            headlineContent = { Text(place.name) },
                            leadingContent = {
                                Icon(Icons.Rounded.History, null, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.clickable { onPlaceSelected(place) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteInfoCard(
    route: RouteResult,
    destination: String,
    formatDistance: (Double) -> String,
    formatDuration: (Double) -> String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        destination,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        "Route found",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Close, "Close", modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                RouteMetric(
                    icon = Icons.Rounded.Straighten,
                    value = formatDistance(route.distance),
                    label = "Distance"
                )
                RouteMetric(
                    icon = Icons.Rounded.Schedule,
                    value = formatDuration(route.duration),
                    label = "Est. Time"
                )
                RouteMetric(
                    icon = Icons.Rounded.Traffic,
                    value = "Live",
                    label = "Traffic"
                )
            }
        }
    }
}

@Composable
private fun RouteMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
