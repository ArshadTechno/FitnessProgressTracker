package com.awscubetech.fitnesstracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awscubetech.fitnesstracker.R
import com.awscubetech.fitnesstracker.data.gemini.AnalysisType
import com.awscubetech.fitnesstracker.ui.FitnessViewModel
import com.awscubetech.fitnesstracker.ui.ScreenDestination
import com.awscubetech.fitnesstracker.ui.components.HomeHabitsWidget
import com.awscubetech.fitnesstracker.ui.components.shareOnWhatsApp
import com.awscubetech.fitnesstracker.ui.theme.EmeraldContainerDark
import com.awscubetech.fitnesstracker.ui.theme.EmeraldDark
import com.awscubetech.fitnesstracker.ui.theme.EmeraldPrimary
import com.awscubetech.fitnesstracker.ui.theme.GoldAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigate: (ScreenDestination) -> Unit,
    onRateUsClicked: () -> Unit,
    viewModel: FitnessViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayFormatted = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    }
    val todayDateString = remember { FitnessViewModel.getTodayDateString() }

    val habits = viewModel?.habits?.collectAsState()?.value ?: emptyList()
    val habitLogs = viewModel?.habitLogs?.collectAsState()?.value ?: emptyList()
    val measurements = viewModel?.measurements?.collectAsState()?.value ?: emptyList()
    val workoutLogs = viewModel?.workoutLogs?.collectAsState()?.value ?: emptyList()
    val primaryGoal = viewModel?.primaryAthleteGoal?.collectAsState()?.value

    val weeklySummary = remember(habitLogs, habits, todayDateString) {
        viewModel?.calculateWeeklyConsistencySummary(habitLogs, habits, todayDateString)
    }

    val greeting = remember { viewModel?.getGreetingMessage() ?: "Good Day, Athlete" }
    val (dailyQuote, quoteTag) = remember { viewModel?.getDynamicDailyQuote() ?: ("Progress is built through daily execution." to "Habit Principle") }

    val latestMeasurement = measurements.firstOrNull()
    val initialMeasurement = measurements.lastOrNull()
    val totalWorkouts = workoutLogs.size
    val totalCalories = workoutLogs.sumOf { it.caloriesBurned }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Dynamic Hero Vibes Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .testTag("hero_banner_card"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Today's Fitness Vibes",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Subtle overlay gradient for high text contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF21005D).copy(alpha = 0.45f),
                                        Color(0xFF141218).copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$greeting • $todayFormatted",
                                color = Color(0xFFD0BCFF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = quoteTag.uppercase(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "\"$dailyQuote\"",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onNavigate(ScreenDestination.CameraAnalysis(AnalysisType.PHYSIQUE_PROGRESS)) },
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("hero_explore_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, Color(0xFFD0BCFF)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFF6750A4).copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = "START AI PROGRESS SCAN",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Bottom dynamic indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${habits.size} Daily Habits • ${totalWorkouts} Workouts Logged",
                                color = Color(0xFFEADDFF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "AI ACTIVE",
                                color = EmeraldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Real Live KPI Dashboard Summary Card
        if (latestMeasurement != null || workoutLogs.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Weight Metric
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(ScreenDestination.Measurements) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Current Weight", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (latestMeasurement != null) "${latestMeasurement.weightKg} kg" else "--",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = EmeraldPrimary
                            )
                            if (latestMeasurement != null && initialMeasurement != null && latestMeasurement.id != initialMeasurement.id) {
                                val diff = latestMeasurement.weightKg - initialMeasurement.weightKg
                                val sign = if (diff > 0) "+" else ""
                                Text("$sign${String.format(Locale.US, "%.1f", diff)} kg net", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        )

                        // Workouts & Calories
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(ScreenDestination.WorkoutLogs) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total Burned", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$totalCalories kcal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFFE11D48)
                            )
                            Text("$totalWorkouts sessions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        )

                        // Active PR Bench Target
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(ScreenDestination.AthleteBenchmarks) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Bench PR Target", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            val benchStr = if (primaryGoal != null) "${primaryGoal.userCurrentBenchKg.toInt()}/${primaryGoal.targetBenchKg.toInt()}k" else "85/140k"
                            Text(
                                text = benchStr,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GoldAccent
                            )
                            Text("PR Goals", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Today's Fitness Goals & 7-Day Consistency Quick Widget
        if (habits.isNotEmpty() && weeklySummary != null) {
            item {
                HomeHabitsWidget(
                    habits = habits,
                    habitLogs = habitLogs,
                    todayDateString = todayDateString,
                    weeklySummary = weeklySummary,
                    onToggleHabit = { habitId ->
                        viewModel?.toggleHabit(habitId, todayDateString)
                    },
                    onOpenFullHabits = {
                        onNavigate(ScreenDestination.DailyHabits)
                    }
                )
            }
        }

        // 2. Menu Navigation Cards (Matching Screenshot 1 style with thick rounded borders & icons)
        item {
            FitnessMenuCard(
                icon = Icons.Default.CameraAlt,
                iconBgColor = Color(0xFF00A86B),
                title = "AI Camera Body & Progress Analysis",
                subtitle = "Posture, body composition & muscle symmetry",
                onClick = { onNavigate(ScreenDestination.CameraAnalysis(AnalysisType.PHYSIQUE_PROGRESS)) }
            )
        }

        item {
            FitnessMenuCard(
                icon = Icons.Default.FitnessCenter,
                iconBgColor = Color(0xFF8B5CF6),
                title = "AI Workout Form & Pose Checker",
                subtitle = "Real-time posture angle & lifting technique",
                onClick = { onNavigate(ScreenDestination.CameraAnalysis(AnalysisType.EXERCISE_FORM)) }
            )
        }

        item {
            FitnessMenuCard(
                icon = Icons.Default.FitnessCenter,
                iconBgColor = Color(0xFFE11D48),
                title = "Workout & PR Logs",
                subtitle = "Log daily sets, reps, weight & personal records",
                onClick = { onNavigate(ScreenDestination.WorkoutLogs) }
            )
        }

        item {
            FitnessMenuCard(
                icon = Icons.Default.MonitorWeight,
                iconBgColor = Color(0xFF06B6D4),
                title = "Body Measurements & Transformation",
                subtitle = "Log weight, body fat %, waist & arm trends",
                onClick = { onNavigate(ScreenDestination.Measurements) }
            )
        }

        item {
            FitnessMenuCard(
                icon = Icons.Default.Whatshot,
                iconBgColor = Color(0xFFEA580C),
                title = "Daily Habit Tracker & Streaks",
                subtitle = "Track water, cardio, protein & visual streaks",
                onClick = { onNavigate(ScreenDestination.DailyHabits) }
            )
        }

        item {
            FitnessMenuCard(
                icon = Icons.Default.Calculate,
                iconBgColor = Color(0xFF10B981),
                title = "Fitness Calculators Suite",
                subtitle = "BMI, TDEE, Navy Body Fat, 1RM, Heart Zones",
                onClick = { onNavigate(ScreenDestination.Calculators) }
            )
        }

        item {
            FitnessMenuCard(
                icon = Icons.Default.Restaurant,
                iconBgColor = Color(0xFFF97316),
                title = "AI Meal & Calorie Scanner",
                subtitle = "Scan post-workout meals for protein & macros",
                onClick = { onNavigate(ScreenDestination.CameraAnalysis(AnalysisType.MEAL_NUTRITION)) }
            )
        }

        item {
            FitnessMenuCard(
                icon = Icons.Default.LocationOn,
                iconBgColor = Color(0xFFEC4899),
                title = "Fitness Centers Near Me",
                subtitle = "Gyms, CrossFit boxes & Olympic studios",
                onClick = { onNavigate(ScreenDestination.GymsNearMe) }
            )
        }

        item {
            FitnessMenuCard(
                icon = Icons.Default.EmojiEvents,
                iconBgColor = Color(0xFFF59E0B),
                title = "Athlete & Celebrity Benchmarks",
                subtitle = "Champion workout splits, macros & PRs",
                onClick = { onNavigate(ScreenDestination.AthleteBenchmarks) }
            )
        }

        item {
            FitnessMenuCard(
                icon = Icons.Default.CalendarMonth,
                iconBgColor = Color(0xFF3B82F6),
                title = "Goal Countdown & Active Days",
                subtitle = "Working days vs rest days between dates",
                onClick = { onNavigate(ScreenDestination.GoalCountdown) }
            )
        }

        // 3. Trusted By 5 Million+ Users Card (Matching Screenshot 1)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRateUsClicked() }
                    .testTag("rate_us_card"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Trusted by 5 million+ users",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star",
                                tint = GoldAccent,
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(horizontal = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Rate us on Google play store",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = EmeraldPrimary
                    )
                }
            }
        }

        // 4. Connect with Fitness Buddies / WhatsApp Share Card (Matching Screenshot 1)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_whatsapp_card"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_community_share),
                        contentDescription = "Share with friends",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Connect with Your Fitness Buddies",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Share Fitness Progress app with your friends, training partners, and family.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { shareOnWhatsApp(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("share_whatsapp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366) // WhatsApp Brand Green
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "WhatsApp Share",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Share on Whatsapp",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FitnessMenuCard(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("menu_card_${title.take(10).lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Square Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
