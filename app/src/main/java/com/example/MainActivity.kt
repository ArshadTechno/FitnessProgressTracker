package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.FitnessViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.AppDrawer
import com.example.ui.components.AppTopBar
import com.example.ui.components.RateUsDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.shareAppIntent
import com.example.ui.screens.AthleteBenchmarksScreen
import com.example.ui.screens.CalculatorSuiteScreen
import com.example.ui.screens.CameraAnalysisScreen
import com.example.ui.screens.GymsNearMeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MeasurementTrackerScreen
import com.example.ui.screens.WorkoutLogsScreen
import com.example.ui.theme.FitnessTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FitnessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            FitnessTrackerTheme(darkTheme = isDarkMode) {
                FitnessApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FitnessApp(viewModel: FitnessViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showRateUsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Intercept back button if drawer is open or if on sub-screen
    BackHandler(enabled = drawerState.isOpen || currentScreen !is ScreenDestination.Home) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen !is ScreenDestination.Home) {
            viewModel.navigateTo(ScreenDestination.Home)
        }
    }

    val screenTitle = when (currentScreen) {
        is ScreenDestination.Home -> "Fitness Progress"
        is ScreenDestination.CameraAnalysis -> "AI Camera Analysis"
        is ScreenDestination.Measurements -> "Body Measurements"
        is ScreenDestination.WorkoutLogs -> "Workout & PR Logs"
        is ScreenDestination.Calculators -> "Fitness Calculators"
        is ScreenDestination.GymsNearMe -> "Fitness Centers Near Me"
        is ScreenDestination.AthleteBenchmarks -> "Athlete Benchmarks"
        is ScreenDestination.GoalCountdown -> "Goal & Active Days"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                AppDrawer(
                    currentScreen = currentScreen,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onNavigate = { destination ->
                        viewModel.navigateTo(destination)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onRateUsClicked = {
                        coroutineScope.launch { drawerState.close() }
                        showRateUsDialog = true
                    },
                    onShareClicked = {
                        coroutineScope.launch { drawerState.close() }
                        shareAppIntent(context)
                    },
                    onSettingsClicked = {
                        coroutineScope.launch { drawerState.close() }
                        showSettingsDialog = true
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppTopBar(
                    title = screenTitle,
                    canNavigateBack = currentScreen !is ScreenDestination.Home,
                    onNavigateBack = { viewModel.navigateTo(ScreenDestination.Home) },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            }
        ) { innerPadding ->
            val screenModifier = Modifier.padding(innerPadding)

            when (currentScreen) {
                is ScreenDestination.Home -> {
                    HomeScreen(
                        onNavigate = { viewModel.navigateTo(it) },
                        onRateUsClicked = { showRateUsDialog = true },
                        modifier = screenModifier
                    )
                }

                is ScreenDestination.CameraAnalysis -> {
                    CameraAnalysisScreen(
                        viewModel = viewModel,
                        modifier = screenModifier
                    )
                }

                is ScreenDestination.Measurements -> {
                    MeasurementTrackerScreen(
                        viewModel = viewModel,
                        modifier = screenModifier
                    )
                }

                is ScreenDestination.WorkoutLogs -> {
                    WorkoutLogsScreen(
                        viewModel = viewModel,
                        modifier = screenModifier
                    )
                }

                is ScreenDestination.Calculators -> {
                    CalculatorSuiteScreen(
                        viewModel = viewModel,
                        initialTab = 1, // BMI & Health tab
                        modifier = screenModifier
                    )
                }

                is ScreenDestination.GoalCountdown -> {
                    CalculatorSuiteScreen(
                        viewModel = viewModel,
                        initialTab = 0, // Active Days & Goal Deadline tab
                        modifier = screenModifier
                    )
                }

                is ScreenDestination.GymsNearMe -> {
                    GymsNearMeScreen(
                        modifier = screenModifier
                    )
                }

                is ScreenDestination.AthleteBenchmarks -> {
                    AthleteBenchmarksScreen(
                        modifier = screenModifier
                    )
                }
            }
        }
    }

    if (showRateUsDialog) {
        RateUsDialog(onDismiss = { showRateUsDialog = false })
    }

    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }
}
