package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.ScreenDestination
import com.example.ui.theme.EmeraldPrimary

@Composable
fun AppDrawer(
    currentScreen: ScreenDestination,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNavigate: (ScreenDestination) -> Unit,
    onRateUsClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // Emerald Header (matching screenshot 2)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldPrimary)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_fitness_icon),
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Fitness Progress",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                        Text(
                            text = "AI Tracker & Tools",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Scrollable Menu items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp)
            ) {
                DrawerMenuItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = currentScreen is ScreenDestination.Home,
                    iconColor = EmeraldPrimary,
                    onClick = { onNavigate(ScreenDestination.Home) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.PhotoCamera,
                    label = "Camera AI Analysis",
                    isSelected = currentScreen is ScreenDestination.CameraAnalysis,
                    iconColor = Color(0xFF06B6D4),
                    onClick = { onNavigate(ScreenDestination.CameraAnalysis) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.MonitorWeight,
                    label = "Body Measurements",
                    isSelected = currentScreen is ScreenDestination.Measurements,
                    iconColor = Color(0xFF8B5CF6),
                    onClick = { onNavigate(ScreenDestination.Measurements) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.FitnessCenter,
                    label = "Workout & Habit Log",
                    isSelected = currentScreen is ScreenDestination.WorkoutLogs,
                    iconColor = Color(0xFFF97316),
                    onClick = { onNavigate(ScreenDestination.WorkoutLogs) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Calculate,
                    label = "Fitness Calculators",
                    isSelected = currentScreen is ScreenDestination.Calculators,
                    iconColor = Color(0xFF10B981),
                    onClick = { onNavigate(ScreenDestination.Calculators) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.LocationOn,
                    label = "Fitness Centers Near Me",
                    isSelected = currentScreen is ScreenDestination.GymsNearMe,
                    iconColor = Color(0xFFEC4899),
                    onClick = { onNavigate(ScreenDestination.GymsNearMe) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Athlete Benchmarks",
                    isSelected = currentScreen is ScreenDestination.AthleteBenchmarks,
                    iconColor = Color(0xFFF59E0B),
                    onClick = { onNavigate(ScreenDestination.AthleteBenchmarks) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.CalendarMonth,
                    label = "Goal & Active Days",
                    isSelected = currentScreen is ScreenDestination.GoalCountdown,
                    iconColor = Color(0xFF3B82F6),
                    onClick = { onNavigate(ScreenDestination.GoalCountdown) }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )

                DrawerMenuItem(
                    icon = Icons.Default.Star,
                    label = "Rate Us",
                    isSelected = false,
                    iconColor = Color(0xFFF59E0B),
                    onClick = onRateUsClicked
                )

                DrawerMenuItem(
                    icon = Icons.Default.Share,
                    label = "Share this app",
                    isSelected = false,
                    iconColor = EmeraldPrimary,
                    onClick = onShareClicked
                )

                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    isSelected = false,
                    iconColor = Color(0xFF6B7280),
                    onClick = onSettingsClicked
                )
            }

            // Bottom Dark/Light Mode Row (matching screenshot 2)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Dark Mode",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Dark/Light Mode",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("dark_mode_switch")
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    iconColor: Color,
    onClick: () -> Unit
) {
    val bg = if (isSelected) EmeraldPrimary.copy(alpha = 0.12f) else Color.Transparent
    val textStyle = if (isSelected) {
        MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary,
            fontSize = 15.sp
        )
    } else {
        MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(bg)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) EmeraldPrimary else iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = textStyle
        )
    }
}
