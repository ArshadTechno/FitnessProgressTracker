package com.awscubetech.fitnesstracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.awscubetech.fitnesstracker.ads.AdMobManager
import com.awscubetech.fitnesstracker.data.gemini.AnalysisType
import com.awscubetech.fitnesstracker.ui.FitnessViewModel
import com.awscubetech.fitnesstracker.ui.ScreenDestination
import com.awscubetech.fitnesstracker.ui.components.AdMobBanner
import com.awscubetech.fitnesstracker.ui.components.AppDrawer
import com.awscubetech.fitnesstracker.ui.components.AppTopBar
import com.awscubetech.fitnesstracker.ui.components.HabitReminderToastBanner
import com.awscubetech.fitnesstracker.ui.components.RateUsDialog
import com.awscubetech.fitnesstracker.ui.components.SettingsDialog
import com.awscubetech.fitnesstracker.ui.components.shareAppIntent
import com.awscubetech.fitnesstracker.ui.screens.AthleteBenchmarksScreen
import com.awscubetech.fitnesstracker.ui.screens.CalculatorSuiteScreen
import com.awscubetech.fitnesstracker.ui.screens.CameraAnalysisScreen
import com.awscubetech.fitnesstracker.ui.screens.DailyHabitsScreen
import com.awscubetech.fitnesstracker.ui.screens.GymsNearMeScreen
import com.awscubetech.fitnesstracker.ui.screens.HomeScreen
import com.awscubetech.fitnesstracker.ui.screens.MeasurementTrackerScreen
import com.awscubetech.fitnesstracker.ui.screens.WorkoutLogsScreen
import com.awscubetech.fitnesstracker.ui.theme.FitnessTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FitnessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdMobManager.initialize(this)
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
    val show8PmReminder by viewModel.show8PmHabitReminder.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showRateUsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // 8:00 PM Habit Check-in notification & toast trigger
    LaunchedEffect(show8PmReminder) {
        if (show8PmReminder) {
            Toast.makeText(
                context,
                "🔥 8:00 PM Check-in: You haven't logged your daily habits yet!",
                Toast.LENGTH_LONG
            ).show()
            viewModel.checkAndTrigger8PmNotification(context)
        }
    }

    // Intercept back button if drawer is open or if on sub-screen
    BackHandler(enabled = drawerState.isOpen || currentScreen !is ScreenDestination.Home) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen !is ScreenDestination.Home) {
            viewModel.navigateTo(ScreenDestination.Home)
        }
    }

    val screenTitle = when (val dest = currentScreen) {
        is ScreenDestination.Home -> "Fitness Progress"
        is ScreenDestination.CameraAnalysis -> when (dest.initialType) {
            AnalysisType.PHYSIQUE_PROGRESS -> "AI Body & Progress Scan"
            AnalysisType.EXERCISE_FORM -> "AI Workout & Pose Checker"
            AnalysisType.MEAL_NUTRITION -> "AI Meal & Calorie Scanner"
        }
        is ScreenDestination.Measurements -> "Body Measurements"
        is ScreenDestination.DailyHabits -> "Daily Habit Tracker"
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
                    },
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() }
                )
            },
            bottomBar = {
                AdMobBanner()
            }
        ) { innerPadding ->
            val screenModifier = Modifier.padding(innerPadding)

            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    is ScreenDestination.Home -> {
                        HomeScreen(
                            onNavigate = { viewModel.navigateTo(it) },
                            onRateUsClicked = { showRateUsDialog = true },
                            viewModel = viewModel,
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

                    is ScreenDestination.DailyHabits -> {
                        DailyHabitsScreen(
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

                // 8:00 PM Habit Reminder Toast Banner
                HabitReminderToastBanner(
                    isVisible = show8PmReminder,
                    onLogHabitsClick = {
                        viewModel.navigateTo(ScreenDestination.DailyHabits)
                        viewModel.dismissHabitReminder()
                    },
                    onDismiss = {
                        viewModel.dismissHabitReminder()
                        Toast.makeText(context, "Reminder dismissed for today", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(innerPadding)
                )
            }
        }
    }

    if (showRateUsDialog) {
        RateUsDialog(onDismiss = { showRateUsDialog = false })
    }

    if (showSettingsDialog) {
        SettingsDialog(
            isDarkMode = isDarkMode,
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
