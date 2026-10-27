package com.citygo.app.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private val Purple = Color(0xFF7C5CFC)
private val Orange = Color(0xFFF97316)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }

    val locationPermission = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    when (page) {
        0 -> OnboardPage1(onNext = { page = 1 })
        1 -> OnboardPage2(
            onRequest = { locationPermission.launchMultiplePermissionRequest() },
            onFinish = onFinish
        )
    }
}

@Composable
private fun OnboardPage1(onNext: () -> Unit) {
    val pulse = rememberInfiniteTransition("pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFAF8FF), Color(0xFFEDE8FF))))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(40.dp))

        // Animated logo
        Box(
            Modifier
                .size(140.dp)
                .scale(scale)
                .clip(RoundedCornerShape(32.dp))
                .background(Brush.linearGradient(listOf(Purple, Orange))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.TravelExplore,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                tint = Color.White
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Travel.ai",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Purple
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Your AI-powered travel companion for Delhi NCR, Haryana & Rajasthan.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(Modifier.height(28.dp))
            OnboardFeatureRow(Icons.Rounded.WbSunny, "Live weather & forecasts")
            Spacer(Modifier.height(10.dp))
            OnboardFeatureRow(Icons.Rounded.Train, "Metro & bus routes")
            Spacer(Modifier.height(10.dp))
            OnboardFeatureRow(Icons.Rounded.AccountBalanceWallet, "Trip budget planner")
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Rounded.ArrowForward, null)
        }
    }
}

@Composable
private fun OnboardPage2(onRequest: () -> Unit, onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFAF8FF), Color(0xFFFFF7ED))))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(40.dp))

        Box(
            Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Orange.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.LocationOn, null, Modifier.size(70.dp), tint = Orange)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Enable Location",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Purple
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Travel.ai uses your location to show weather, nearest metro stations, and bus stops in real time. Your location is never stored or shared.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(Modifier.height(20.dp))
            PermissionInfoRow(Icons.Rounded.WbSunny, "Real-time local weather")
            Spacer(Modifier.height(8.dp))
            PermissionInfoRow(Icons.Rounded.Train, "Nearest metro & bus stops")
            Spacer(Modifier.height(8.dp))
            PermissionInfoRow(Icons.Rounded.Route, "Better route suggestions")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = { onRequest(); onFinish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Icon(Icons.Rounded.LocationOn, null)
                Spacer(Modifier.width(8.dp))
                Text("Allow Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onFinish) {
                Text("Skip for now", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun OnboardFeatureRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Purple.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = Purple)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF374151))
    }
}

@Composable
private fun PermissionInfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF7ED))
            .padding(12.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = Orange)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
