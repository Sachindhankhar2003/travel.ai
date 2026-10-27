package com.citygo.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.citygo.app.ui.screens.budget.BudgetScreen
import com.citygo.app.ui.screens.explore.ExploreScreen
import com.citygo.app.ui.screens.home.HomeScreen
import com.citygo.app.ui.screens.map.MapScreen
import com.citygo.app.ui.screens.onboarding.OnboardingScreen
import com.citygo.app.ui.screens.onboarding.OnboardingViewModel
import com.citygo.app.ui.screens.settings.SettingsScreen
import com.citygo.app.ui.screens.transit.TransitScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home     : Screen("home",     "Home",    Icons.Rounded.Home)
    object Map      : Screen("map",      "Map",     Icons.Rounded.Map)
    object Transit  : Screen("transit",  "Transit", Icons.Rounded.Train)
    object Explore  : Screen("explore",  "Explore", Icons.Rounded.Explore)
    object Budget   : Screen("budget",   "Budget",  Icons.Rounded.AccountBalanceWallet)
    object Settings : Screen("settings", "Settings",Icons.Rounded.Settings)
}

val bottomNavItems = listOf(
    Screen.Home, Screen.Map, Screen.Transit, Screen.Explore, Screen.Budget
)

// ── Warm light-theme nav bar colours ─────────────────────────────────────────
private val NavBarBg         = Color.White
private val NavSelectedColor = Color(0xFF0F766E)          // teal-primary
private val NavIndicator     = Color(0xFFCCF0EE)          // very light teal
private val NavUnselected    = Color(0xFF6B8080)

@Composable
fun CityGoNavHost() {
    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val onboardingDone by onboardingVm.onboardingDone.collectAsStateWithLifecycle()

    if (onboardingDone == null) return          // wait for DataStore
    if (onboardingDone == false) {
        OnboardingScreen(onFinish = { onboardingVm.completeOnboarding() })
        return
    }

    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        bottomBar = {
            NavigationBar(
                containerColor   = NavBarBg,
                tonalElevation   = 0.dp,
                windowInsets     = WindowInsets.navigationBars,
                modifier         = Modifier.height(72.dp)
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentRoute?.startsWith(screen.route) == true
                    NavigationBarItem(
                        selected = selected,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                modifier = Modifier.size(if (selected) 24.dp else 22.dp)
                            )
                        },
                        label = {
                            Text(
                                screen.label,
                                style      = MaterialTheme.typography.labelSmall,
                                fontSize   = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = NavSelectedColor,
                            selectedTextColor   = NavSelectedColor,
                            indicatorColor      = NavIndicator,
                            unselectedIconColor = NavUnselected,
                            unselectedTextColor = NavUnselected
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController      = navController,
            startDestination   = Screen.Home.route,
            enterTransition    = { fadeIn(tween(180)) + slideInHorizontally(tween(180))  { it / 10 } },
            exitTransition     = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { -it / 10 } },
            popEnterTransition = { fadeIn(tween(180)) + slideInHorizontally(tween(180))  { -it / 10 } },
            popExitTransition  = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 10 } }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(innerPadding = innerPadding, navController = navController)
            }
            composable(Screen.Map.route) {
                MapScreen(innerPadding = innerPadding)
            }
            composable(Screen.Transit.route) {
                TransitScreen(innerPadding = innerPadding)
            }
            composable(Screen.Explore.route) {
                ExploreScreen(innerPadding = innerPadding)
            }
            composable(
                route     = "${Screen.Budget.route}?destination={destination}",
                arguments = listOf(navArgument("destination") {
                    type         = NavType.StringType
                    defaultValue = ""
                })
            ) { backStack ->
                val dest = backStack.arguments?.getString("destination") ?: ""
                BudgetScreen(
                    innerPadding        = innerPadding,
                    prefillDestination  = dest.ifBlank { null }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(innerPadding = innerPadding)
            }
        }
    }
}
