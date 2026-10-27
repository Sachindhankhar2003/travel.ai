package com.citygo.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Single light-only colour scheme (no dark theme) ─────────────────────────
private val AppColorScheme = lightColorScheme(
    primary             = Teal60,               // #0F766E  — deep teal
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFCCF0EE),    // very light teal
    onPrimaryContainer  = Color(0xFF003331),

    secondary           = Amber70,              // #D97706  — warm amber
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFFFEDD5),    // soft orange-cream
    onSecondaryContainer = Color(0xFF411E00),

    tertiary            = Color(0xFF4B53A0),     // indigo accent
    onTertiary          = Color.White,
    tertiaryContainer   = Color(0xFFDFE0FF),
    onTertiaryContainer = Color(0xFF00006E),

    error               = Color(0xFFB3261E),
    onError             = Color.White,
    errorContainer      = Color(0xFFFFDAD6),
    onErrorContainer    = Color(0xFF410002),

    background          = Color(0xFFFAFAFA),     // near-white warm background
    onBackground        = Color(0xFF1C1C1E),

    surface             = Color.White,
    onSurface           = Color(0xFF1C1C1E),
    surfaceVariant      = Color(0xFFF2F7F7),     // very light teal-tinted surface
    onSurfaceVariant    = Color(0xFF3F5151),

    outline             = Color(0xFF6B8080),
    outlineVariant      = Color(0xFFCADEDE),

    inverseSurface      = Color(0xFF2D3131),
    inverseOnSurface    = Color(0xFFECF4F4),
    inversePrimary      = Teal80,
    surfaceTint         = Teal60,
)

@Composable
fun CityGoTheme(
    // always light — ignore system dark setting
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true   // always light status bar icons
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = CityGoTypography,
        content     = content
    )
}
