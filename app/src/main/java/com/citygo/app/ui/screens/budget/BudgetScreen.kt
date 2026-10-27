package com.citygo.app.ui.screens.budget

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citygo.app.data.local.entity.TripPlanEntity
import kotlin.math.min

// ─── Design tokens ────────────────────────────────────────────────────────────
private val Purple   = Color(0xFF7C5CFC)
private val Orange   = Color(0xFFF97316)
private val BgPurple = Color(0xFFFAF8FF)
private val donutColors = listOf(
    Color(0xFF7C5CFC), Color(0xFFF97316), Color(0xFF22C55E), Color(0xFF38BDF8)
)

@Composable
fun BudgetScreen(
    innerPadding: PaddingValues,
    prefillDestination: String? = null,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedPlans by viewModel.savedPlans.collectAsStateWithLifecycle()

    LaunchedEffect(prefillDestination) {
        if (!prefillDestination.isNullOrBlank()) viewModel.prefillDestination(prefillDestination)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPurple)
            .padding(bottom = innerPadding.calculateBottomPadding()),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Purple, Orange)))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column {
                    Text(
                        "Budget Planner",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Plan your trip costs before you go",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Input form card
        item { Spacer(Modifier.height(16.dp)) }
        item { InputFormCard(state, viewModel) }

        // Error snackbar inline
        if (state.error != null) {
            item {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            state.error!!, style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        IconButton(onClick = viewModel::clearError, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.Close, null, Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Calculate button
        item {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = viewModel::calculate,
                enabled = !state.isCalculating,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                if (state.isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White, strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (state.isCalculating) "Calculating…" else "Calculate Budget",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Results
        if (state.calculationDone) {
            item { Spacer(Modifier.height(20.dp)) }
            item { TotalCostCard(state, onSave = viewModel::savePlan) }
            item { Spacer(Modifier.height(16.dp)) }
            item { DonutChartCard(state) }
            item { Spacer(Modifier.height(16.dp)) }
            item { CompareStripCard(state) }
        }

        // Saved plans
        if (savedPlans.isNotEmpty()) {
            item {
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Saved Trip Plans",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${savedPlans.size} plans",
                        style = MaterialTheme.typography.labelSmall,
                        color = Purple
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            items(savedPlans, key = { it.id }) { plan ->
                SavedPlanCard(
                    plan = plan,
                    onDelete = { viewModel.deletePlan(plan.id) },
                    shareText = viewModel.sharePlanText(plan)
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Input Form ───────────────────────────────────────────────────────────────

@Composable
private fun InputFormCard(state: BudgetUiState, vm: BudgetViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // From / To row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.fromCity,
                    onValueChange = vm::onFromCity,
                    label = { Text("From city") },
                    leadingIcon = { Icon(Icons.Rounded.TripOrigin, null, tint = Purple, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = purpleTextFieldColors()
                )
                OutlinedTextField(
                    value = state.toCity,
                    onValueChange = vm::onToCity,
                    label = { Text("Destination") },
                    leadingIcon = { Icon(Icons.Rounded.LocationOn, null, tint = Orange, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = purpleTextFieldColors()
                )
            }

            // Travellers & Nights
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StepperField(
                    label = "Travellers",
                    value = state.travellers,
                    onDecrement = { vm.onTravellers(state.travellers - 1) },
                    onIncrement = { vm.onTravellers(state.travellers + 1) },
                    modifier = Modifier.weight(1f)
                )
                StepperField(
                    label = "Nights",
                    value = state.nights,
                    onDecrement = { vm.onNights(state.nights - 1) },
                    onIncrement = { vm.onNights(state.nights + 1) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Travel mode
            LabeledSection("Travel Mode") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TravelMode.values().forEach { mode ->
                        val selected = state.travelMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { vm.onTravelMode(mode) },
                            label = {
                                Text(
                                    mode.toLabel(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            // Own vehicle fuel cost
            AnimatedVisibility(state.travelMode == TravelMode.OWN_VEHICLE) {
                OutlinedTextField(
                    value = state.fuelCostPerKm.toString(),
                    onValueChange = { vm.onFuelCost(it.toDoubleOrNull() ?: state.fuelCostPerKm) },
                    label = { Text("Fuel cost (₹/km)") },
                    leadingIcon = { Icon(Icons.Rounded.LocalGasStation, null, tint = Orange, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = purpleTextFieldColors()
                )
            }

            // Room type
            LabeledSection("Room Type") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoomType.values().forEach { rt ->
                        val selected = state.roomType == rt
                        FilterChip(
                            selected = selected,
                            onClick = { vm.onRoomType(rt) },
                            label = { Text(rt.toLabel()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Orange,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            // Rooms count & daily food
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StepperField(
                    label = "Rooms",
                    value = state.roomCount,
                    onDecrement = { vm.onRoomCount(state.roomCount - 1) },
                    onIncrement = { vm.onRoomCount(state.roomCount + 1) },
                    modifier = Modifier.weight(1f)
                )
            }

            LabeledSection("Daily Food Budget: ₹${state.dailyFoodBudget.toInt()}/person") {
                Slider(
                    value = state.dailyFoodBudget.toFloat(),
                    onValueChange = { vm.onDailyFood(it.toDouble()) },
                    valueRange = 100f..3000f,
                    steps = 28,
                    colors = SliderDefaults.colors(thumbColor = Purple, activeTrackColor = Purple)
                )
            }

            // Return trip toggle
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.SwapHoriz, null, tint = Purple, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Return Trip", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = state.returnTrip,
                    onCheckedChange = vm::onReturnTrip,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple)
                )
            }
        }
    }
}

// ─── Total Cost Gradient Card ─────────────────────────────────────────────────

@Composable
private fun TotalCostCard(state: BudgetUiState, onSave: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Purple, Color(0xFFFB923C))))
            .padding(20.dp)
    ) {
        Column {
            Text("Total Trip Cost", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
            Text(
                "₹${"%.0f".format(state.totalCost)}",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "₹${"%.0f".format(state.perPersonCost)} per person",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CostPill("✈ ₹${"%.0f".format(state.travelCost)}", "Travel")
                CostPill("🏨 ₹${"%.0f".format(state.stayCost)}", "Stay")
                CostPill("🍽 ₹${"%.0f".format(state.foodCost)}", "Food")
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onSave,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Rounded.BookmarkAdd, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save Plan", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CostPill(amount: String, label: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(amount, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
    }
}

// ─── Donut Chart (Canvas, no library) ────────────────────────────────────────

@Composable
private fun DonutChartCard(state: BudgetUiState) {
    val total = state.travelCost + state.stayCost + state.foodCost
    if (total == 0.0) return

    val segments = listOf(
        "Travel" to state.travelCost,
        "Stay"   to state.stayCost,
        "Food"   to state.foodCost
    )
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, tween(900, easing = EaseOutCubic))
    }
    val progress by animProgress.asState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Expense Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut
                Canvas(modifier = Modifier.size(140.dp)) {
                    val strokeWidth = 32f
                    val diameter = min(size.width, size.height) - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)
                    var startAngle = -90f

                    segments.forEachIndexed { i, (_, value) ->
                        val sweep = (value / total * 360f * progress)
                        drawArc(
                            color = donutColors[i],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweep + 3f
                    }
                }

                Spacer(Modifier.width(20.dp))

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    segments.forEachIndexed { i, (label, value) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(donutColors[i])
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    "₹${"%.0f".format(value)} (${"%.0f".format(value / total * 100)}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Compare Strip ────────────────────────────────────────────────────────────

@Composable
private fun CompareStripCard(state: BudgetUiState) {
    if (state.compareCosts.isEmpty()) return
    val cheapest = state.compareCosts.minByOrNull { it.value }?.key

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Transport Comparison", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            val modeLabels = mapOf(
                TravelMode.METRO       to "Metro",
                TravelMode.GOVT_BUS    to "Govt Bus",
                TravelMode.PRIVATE_BUS to "Private Bus"
            )
            val modeIcons = mapOf(
                TravelMode.METRO       to Icons.Rounded.Train,
                TravelMode.GOVT_BUS    to Icons.Rounded.DirectionsBus,
                TravelMode.PRIVATE_BUS to Icons.Rounded.DirectionsBus
            )

            state.compareCosts.forEach { (mode, cost) ->
                val isCheapest = mode == cheapest
                val isCurrent  = mode == state.travelMode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isCheapest) Orange.copy(alpha = 0.08f)
                            else if (isCurrent) Purple.copy(alpha = 0.06f)
                            else Color.Transparent
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modeIcons[mode] ?: Icons.Rounded.DirectionsBus,
                        null,
                        tint = if (isCheapest) Orange else Purple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        modeLabels[mode] ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (isCheapest) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Orange
                        ) {
                            Text(
                                "Cheapest",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        "₹${"%.0f".format(cost)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCheapest) Orange else Purple
                    )
                }
            }
        }
    }
}

// ─── Saved Plan Card ──────────────────────────────────────────────────────────

@Composable
private fun SavedPlanCard(plan: TripPlanEntity, onDelete: () -> Unit, shareText: String) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${plan.fromCity} → ${plan.toCity}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${plan.travellers} travellers · ${plan.nights} nights · ${plan.travelMode.replace("_", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    "₹${"%.0f".format(plan.totalCost)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Purple
                )
            }

            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                    Spacer(Modifier.height(4.dp))
                    PlanDetailRow("Travel", "₹${"%.0f".format(plan.travelCost)}", donutColors[0])
                    PlanDetailRow("Stay", "₹${"%.0f".format(plan.stayCost)}", donutColors[1])
                    PlanDetailRow("Food", "₹${"%.0f".format(plan.foodCost)}", donutColors[2])
                    PlanDetailRow("Per person", "₹${"%.0f".format(plan.perPersonCost)}", Purple)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share budget"))
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple),
                            border = BorderStroke(1.dp, Purple),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Share, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Share", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delete", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanDetailRow(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun LabeledSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        content()
    }
}

@Composable
private fun StepperField(label: String, value: Int, onDecrement: () -> Unit, onIncrement: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                .padding(horizontal = 4.dp)
        ) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Remove, null, modifier = Modifier.size(16.dp), tint = Purple)
            }
            Text(
                "$value",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp), tint = Purple)
            }
        }
    }
}

@Composable
private fun purpleTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Purple,
    unfocusedBorderColor = Color(0xFFE5E7EB),
    focusedLabelColor = Purple
)

private fun TravelMode.toLabel() = when (this) {
    TravelMode.METRO        -> "Metro"
    TravelMode.GOVT_BUS     -> "Govt Bus"
    TravelMode.PRIVATE_BUS  -> "Pvt Bus"
    TravelMode.OWN_VEHICLE  -> "Own Car"
}

private fun RoomType.toLabel() = when (this) {
    RoomType.BUDGET  -> "Budget"
    RoomType.MID     -> "Mid"
    RoomType.PREMIUM -> "Premium"
}
