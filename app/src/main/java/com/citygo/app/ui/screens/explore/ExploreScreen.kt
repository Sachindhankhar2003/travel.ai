package com.citygo.app.ui.screens.explore

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citygo.app.domain.model.LatLng
import com.citygo.app.ui.components.EmptyState
import com.citygo.app.ui.components.ErrorState
import com.citygo.app.ui.components.ShimmerRow

// ── Category tint colours ─────────────────────────────────────────────────────
private fun ExploreCategory.tint(): Color = when (this) {
    ExploreCategory.RESTAURANT -> Color(0xFFEF4444)
    ExploreCategory.HOTEL      -> Color(0xFF7C5CFC)
    ExploreCategory.CAFE       -> Color(0xFFD97706)
    ExploreCategory.HOSPITAL   -> Color(0xFF10B981)
    ExploreCategory.PHARMACY   -> Color(0xFF06B6D4)
    ExploreCategory.ATM        -> Color(0xFF0F766E)
    ExploreCategory.PETROL     -> Color(0xFFF59E0B)
    ExploreCategory.PARK       -> Color(0xFF22C55E)
    ExploreCategory.SHOPPING   -> Color(0xFFEC4899)
    ExploreCategory.METRO      -> Color(0xFF3B82F6)
    ExploreCategory.MUSEUM     -> Color(0xFF8B5CF6)
    ExploreCategory.TEMPLE     -> Color(0xFFF97316)
}

@Composable
fun ExploreScreen(
    innerPadding: PaddingValues,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F766E), Color(0xFF059669))
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Explore",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        uiState.currentLocation?.let { "Places near you" } ?: "Discovering location…",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                // Radius badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        "${uiState.searchRadius / 1000.0}km radius",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // ── Category chips ────────────────────────────────────────────────────
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ExploreCategory.values()) { cat ->
                val selected = uiState.selectedCategory == cat
                val tint     = cat.tint()
                FilterChip(
                    selected  = selected,
                    onClick   = { viewModel.onCategorySelected(cat) },
                    label     = { Text("${cat.emoji} ${cat.label}", fontSize = 12.sp) },
                    colors    = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = tint,
                        selectedLabelColor     = Color.White,
                        containerColor         = Color.White,
                        labelColor             = Color(0xFF374151)
                    ),
                    border    = FilterChipDefaults.filterChipBorder(
                        enabled          = true,
                        selected         = selected,
                        borderColor      = if (selected) tint else Color(0xFFE5E7EB),
                        selectedBorderColor = tint
                    ),
                    shape     = RoundedCornerShape(50)
                )
            }
        }

        // ── Radius slider ─────────────────────────────────────────────────────
        RadiusControl(
            radius    = uiState.searchRadius,
            onChange  = viewModel::onRadiusChanged
        )

        // ── Main content ──────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    ShimmerRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        count    = 5,
                        height   = 110
                    )
                }
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = viewModel::retry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.hasSearched && uiState.places.isEmpty() -> {
                    EmptyState(
                        icon     = Icons.Rounded.SearchOff,
                        title    = "No ${uiState.selectedCategory.label} found",
                        subtitle = "Try increasing the search radius or pick another category",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.places.isNotEmpty() -> {
                    LazyColumn(
                        contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement   = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                "${uiState.places.size} places found · ${uiState.selectedCategory.label}",
                                style  = MaterialTheme.typography.labelMedium,
                                color  = Color(0xFF6B7280),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(uiState.places, key = { it.id }) { place ->
                            PlaceCard(
                                place        = place,
                                userLocation = uiState.currentLocation,
                                distanceText = uiState.currentLocation?.let {
                                    viewModel.distanceText(it, place.latLng)
                                },
                                categoryTint = uiState.selectedCategory.tint(),
                                categoryEmoji = uiState.selectedCategory.emoji,
                                onNavigate   = {
                                    val uri    = Uri.parse("geo:${place.latLng.lat},${place.latLng.lng}?q=${Uri.encode(place.name)}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    }
                                },
                                onCall = place.phone?.let { phone ->
                                    {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        context.startActivity(intent)
                                    }
                                },
                                onWeb = place.website?.let { web ->
                                    {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(web))
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Radius control ────────────────────────────────────────────────────────────
@Composable
private fun RadiusControl(radius: Int, onChange: (Int) -> Unit) {
    var sliderValue by remember(radius) { mutableFloatStateOf(radius / 500f) }
    val steps = listOf(500, 1000, 2000, 3000, 5000)

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Search radius", style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF374151))
                Text(
                    if (radius < 1000) "${radius}m" else "${"%.0f".format(radius / 1000.0)}km",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF0F766E)
                )
            }
            Slider(
                value         = sliderValue,
                onValueChange = {
                    sliderValue = it
                    // Snap to nearest step
                    val raw  = (it * 500).toInt().coerceIn(500, 5000)
                    val snap = steps.minByOrNull { s -> Math.abs(s - raw) } ?: 2000
                    onChange(snap)
                },
                valueRange    = 1f..10f,
                steps         = 3,
                colors        = SliderDefaults.colors(
                    thumbColor       = Color(0xFF0F766E),
                    activeTrackColor = Color(0xFF0F766E)
                ),
                modifier      = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Place Card ────────────────────────────────────────────────────────────────
@Composable
private fun PlaceCard(
    place: Place,
    userLocation: LatLng?,
    distanceText: String?,
    categoryTint: Color,
    categoryEmoji: String,
    onNavigate: () -> Unit,
    onCall: (() -> Unit)?,
    onWeb: (() -> Unit)?
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(categoryTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(categoryEmoji, fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        place.name,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    if (place.address.isNotBlank()) {
                        Text(
                            place.address,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    if (distanceText != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = categoryTint.copy(alpha = 0.10f)
                        ) {
                            Text(
                                distanceText,
                                style      = MaterialTheme.typography.labelSmall,
                                color      = categoryTint,
                                fontWeight = FontWeight.SemiBold,
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (place.openingHours != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "⏰ " + place.openingHours.take(20),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6B7280),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Expanded actions
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Navigate
                        OutlinedButton(
                            onClick = onNavigate,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F766E)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0F766E))
                        ) {
                            Icon(Icons.Rounded.Navigation, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Navigate", style = MaterialTheme.typography.labelSmall)
                        }

                        // Call
                        if (onCall != null) {
                            OutlinedButton(
                                onClick = onCall,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF059669))
                            ) {
                                Icon(Icons.Rounded.Call, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Call", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Website
                        if (onWeb != null) {
                            OutlinedButton(
                                onClick = onWeb,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = categoryTint),
                                border = androidx.compose.foundation.BorderStroke(1.dp, categoryTint)
                            ) {
                                Icon(Icons.Rounded.OpenInNew, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Web", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
