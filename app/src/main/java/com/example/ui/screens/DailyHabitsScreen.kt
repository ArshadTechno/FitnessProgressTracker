package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitDayInfo
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.ui.FitnessViewModel
import com.example.ui.components.ConsistencyMilestonesCard
import com.example.ui.components.FitnessGoalPresetsStrip
import com.example.ui.components.WeeklyConsistencyMatrixCard
import com.example.ui.components.getHabitIconFromKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DailyHabitsScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val habits by viewModel.habits.collectAsState()
    val habitLogs by viewModel.habitLogs.collectAsState()
    val selectedDate by viewModel.selectedHabitDate.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Daily Goals Checklist, 1: Weekly Consistency Matrix
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Pending", "Completed"
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val todayDate = remember { FitnessViewModel.getTodayDateString() }
    val isToday = selectedDate == todayDate

    // Parse date for readable header
    val displayDateHeader = remember(selectedDate) {
        try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfOutput = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            val date = sdfInput.parse(selectedDate)
            if (date != null) sdfOutput.format(date) else selectedDate
        } catch (e: Exception) {
            selectedDate
        }
    }

    // Weekly consistency computation
    val weeklySummary = remember(habitLogs, habits, selectedDate) {
        viewModel.calculateWeeklyConsistencySummary(habitLogs, habits, selectedDate)
    }

    // Stats calculations for selected date
    val completedCountToday = habits.count { habit ->
        habitLogs.any { it.habitId == habit.id && it.dateFormatted == selectedDate && it.isCompleted }
    }
    val totalCount = habits.size
    val completionRatio = if (totalCount > 0) completedCountToday.toFloat() / totalCount else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = completionRatio,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    // Total active streaks
    val totalActiveStreaks = habits.sumOf { habit ->
        val streak = viewModel.calculateCurrentStreak(habit.id, habitLogs)
        if (streak > 0) 1.toInt() else 0.toInt()
    }

    val filteredHabits = habits.filter { habit ->
        val isCompleted = habitLogs.any { it.habitId == habit.id && it.dateFormatted == selectedDate && it.isCompleted }
        val matchesStatus = when (selectedFilter) {
            "Completed" -> isCompleted
            "Pending" -> !isCompleted
            else -> true
        }
        val matchesCategory = if (selectedCategoryFilter == "All") true else habit.category.equals(selectedCategoryFilter, ignoreCase = true)
        matchesStatus && matchesCategory
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Habit")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // View Mode Tab Switcher: Daily Checklist vs Weekly Consistency Matrix
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color(0xFF6750A4),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF6750A4).copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Daily Goals Log",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_daily_goals")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Weekly Consistency",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_weekly_consistency")
                    )
                }
            }

            if (selectedTab == 0) {
                // ==================== TAB 0: DAILY GOAL LOG ====================
                // Hero Geometric Balance Card
                item {
                    HabitsHeroBanner(
                        displayDateHeader = displayDateHeader,
                        isToday = isToday,
                        selectedDate = selectedDate,
                        completedCount = completedCountToday,
                        totalCount = totalCount,
                        progress = animatedProgress,
                        totalActiveStreaks = totalActiveStreaks,
                        onPreviousDay = {
                            changeDay(selectedDate, -1) { viewModel.setSelectedHabitDate(it) }
                        },
                        onNextDay = {
                            changeDay(selectedDate, 1) { viewModel.setSelectedHabitDate(it) }
                        },
                        onTodayClicked = {
                            viewModel.setSelectedHabitDate(todayDate)
                        }
                    )
                }

                // 7-Day Day Selector Strip
                item {
                    SevenDayTimelineStrip(
                        selectedDate = selectedDate,
                        onDateSelected = { viewModel.setSelectedHabitDate(it) }
                    )
                }

                // Fast Action Bar: Mark All Completed & Reset All
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.markAllHabitsForDate(selectedDate, isCompleted = true)
                                Toast.makeText(context, "All goals marked complete for $selectedDate! 🔥", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("btn_mark_all_complete"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mark All Complete",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.markAllHabitsForDate(selectedDate, isCompleted = false)
                                Toast.makeText(context, "Reset goals for $selectedDate", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("btn_reset_all_habits"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reset",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Preset Fitness Goals Strip (Quick 1-Tap Add)
                item {
                    FitnessGoalPresetsStrip(
                        onSelectPreset = { preset ->
                            viewModel.addCustomHabit(
                                title = preset.title,
                                category = preset.category,
                                iconKey = preset.iconKey,
                                colorHex = preset.colorHex,
                                frequency = "Daily"
                            )
                            Toast.makeText(context, "Added goal: ${preset.title}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // 8:00 PM Habit Reminder Status & Test Card
                item {
                    HabitReminderCard(
                        onTestReminder = {
                            viewModel.triggerTest8PmReminder(context)
                            Toast.makeText(
                                context,
                                "🔥 8:00 PM Check-in reminder & notification triggered!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                // Filter Chips Bar (Status Filters)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("All", "Pending", "Completed").forEach { filter ->
                            val isSelected = selectedFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(
                                        text = when (filter) {
                                            "All" -> "All ($totalCount)"
                                            "Pending" -> "Pending (${totalCount - completedCountToday})"
                                            else -> "Completed ($completedCountToday)"
                                        },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6750A4),
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF6750A4) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("filter_chip_$filter")
                            )
                        }
                    }
                }

                // Habit Items List
                if (filteredHabits.isEmpty()) {
                    item {
                        EmptyHabitsCard(filter = selectedFilter, onAddClick = { showAddDialog = true })
                    }
                } else {
                    items(filteredHabits, key = { it.id }) { habit ->
                        val isDone = habitLogs.any { it.habitId == habit.id && it.dateFormatted == selectedDate && it.isCompleted }
                        val currentStreak = viewModel.calculateCurrentStreak(habit.id, habitLogs)
                        val bestStreak = viewModel.calculateBestStreak(habit.id, habitLogs)
                        val heatmap7Days = viewModel.get7DayHeatmap(habit.id, habitLogs, selectedDate)

                        GeometricHabitCard(
                            habit = habit,
                            isCompleted = isDone,
                            currentStreak = currentStreak,
                            bestStreak = bestStreak,
                            heatmap7Days = heatmap7Days,
                            onToggle = {
                                viewModel.toggleHabit(habit.id, selectedDate)
                            },
                            onDelete = {
                                viewModel.deleteHabit(habit)
                                Toast.makeText(context, "Deleted ${habit.title}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            } else {
                // ==================== TAB 1: WEEKLY CONSISTENCY MATRIX ====================
                // 1. Full 7-Day Consistency Matrix Card
                item {
                    WeeklyConsistencyMatrixCard(
                        summary = weeklySummary,
                        habits = habits,
                        habitLogs = habitLogs,
                        selectedDate = selectedDate,
                        onDateSelected = { viewModel.setSelectedHabitDate(it) },
                        onToggleHabitForDate = { habitId, dateStr ->
                            viewModel.toggleHabit(habitId, dateStr)
                        },
                        onMarkAllTodayComplete = {
                            viewModel.markAllHabitsForDate(todayDate, isCompleted = true)
                            Toast.makeText(context, "Marked all goals complete for today! 🔥", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // 2. Consistency Badges & Milestones Card
                item {
                    ConsistencyMilestonesCard(
                        weeklyConsistencyPercent = weeklySummary.averageConsistencyPercent,
                        activeStreaks = weeklySummary.activeStreaksCount,
                        perfectDays = weeklySummary.perfectDaysCount
                    )
                }

                // 3. Quick Goal Templates
                item {
                    FitnessGoalPresetsStrip(
                        onSelectPreset = { preset ->
                            viewModel.addCustomHabit(
                                title = preset.title,
                                category = preset.category,
                                iconKey = preset.iconKey,
                                colorHex = preset.colorHex,
                                frequency = "Daily"
                            )
                            Toast.makeText(context, "Added goal: ${preset.title}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, iconKey, colorHex, frequency ->
                viewModel.addCustomHabit(title, category, iconKey, colorHex, frequency)
                showAddDialog = false
                Toast.makeText(context, "Created custom habit: $title", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun HabitsHeroBanner(
    displayDateHeader: String,
    isToday: Boolean,
    selectedDate: String,
    completedCount: Int,
    totalCount: Int,
    progress: Float,
    totalActiveStreaks: Int,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onTodayClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habits_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, Color(0xFF6750A4).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6750A4).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Date switcher & Today tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPreviousDay,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_prev_day")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column {
                        Text(
                            text = "HABIT TRACKING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            text = displayDateHeader,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onNextDay,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_next_day")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (!isToday) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF6750A4).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF6750A4).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { onTodayClicked() }
                            .testTag("btn_jump_today")
                    ) {
                        Text(
                            text = "JUMP TO TODAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF16A34A).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "TODAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF16A34A),
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Completion Progress Bar & Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "COMPLETED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0xFF6750A4),
                        trackColor = Color(0xFF6750A4).copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$completedCount of $totalCount daily habits completed",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Streak Flame Badge
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFFF7ED),
                    border = BorderStroke(1.2.dp, Color(0xFFF97316).copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("habits_active_streak_badge")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Streak",
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "$totalActiveStreaks",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFC2410C)
                        )
                        Text(
                            text = "STREAKS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFFEA580C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SevenDayTimelineStrip(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfDay = SimpleDateFormat("EE", Locale.getDefault())
    val sdfNum = SimpleDateFormat("d", Locale.getDefault())
    val today = remember { FitnessViewModel.getTodayDateString() }

    val days = remember {
        val list = mutableListOf<Triple<String, String, String>>() // dateStr, dayLabel, dayNum
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = sdfDate.format(cal.time)
            val dayLabel = sdfDay.format(cal.time).take(3).uppercase()
            val dayNum = sdfNum.format(cal.time)
            list.add(Triple(dateStr, dayLabel, dayNum))
        }
        list
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items(days) { (dateStr, dayLabel, dayNum) ->
            val isSelected = dateStr == selectedDate
            val isDayToday = dateStr == today

            val bgCol = if (isSelected) Color(0xFF6750A4) else MaterialTheme.colorScheme.surface
            val textCol = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            val borderCol = if (isSelected) Color(0xFF6750A4) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDateSelected(dateStr) }
                    .testTag("timeline_day_$dateStr"),
                shape = RoundedCornerShape(16.dp),
                color = bgCol,
                border = BorderStroke(1.2.dp, borderCol)
            ) {
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = dayLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dayNum,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textCol
                    )
                    if (isDayToday) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color(0xFF6750A4))
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitReminderCard(
    onTestReminder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_reminder_info_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEA580C).copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, Color(0xFFEA580C).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEA580C).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFFEA580C),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Daily 8:00 PM Check-in Alert",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Reminds you if no habits are completed by 8 PM",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onTestReminder,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEA580C)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_test_8pm_reminder")
            ) {
                Text("Test Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GeometricHabitCard(
    habit: HabitEntity,
    isCompleted: Boolean,
    currentStreak: Int,
    bestStreak: Int,
    heatmap7Days: List<HabitDayInfo>,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val habitColor = Color(habit.colorHex)
    val cardBorderColor = if (isCompleted) habitColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Top Row: Icon, Title & Category, Complete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Habit Icon Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(habitColor.copy(alpha = 0.15f))
                        .border(1.2.dp, habitColor.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getHabitIconFromKey(habit.iconKey),
                        contentDescription = habit.category,
                        tint = habitColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Title & Category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = habitColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = habit.category.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = habitColor,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (currentStreak > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFF7ED),
                                border = BorderStroke(0.8.dp, Color(0xFFF97316).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Whatshot,
                                        contentDescription = "Streak",
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "$currentStreak d streak",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC2410C)
                                    )
                                }
                            }
                        }
                    }
                }

                // Geometric Checkbox / Toggle Button
                val checkBg by animateColorAsState(
                    targetValue = if (isCompleted) habitColor else Color.Transparent,
                    animationSpec = tween(300),
                    label = "checkBg"
                )
                val checkBorder by animateColorAsState(
                    targetValue = if (isCompleted) habitColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    animationSpec = tween(300),
                    label = "checkBorder"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(checkBg)
                        .border(1.5.dp, checkBorder, RoundedCornerShape(14.dp))
                        .clickable { onToggle() }
                        .testTag("toggle_habit_${habit.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 7-Day Visual Completion Heatmap / Dots Trail
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LAST 7 DAYS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (dayInfo in heatmap7Days) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = dayInfo.dayLabel,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dayInfo.isSelected) habitColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (dayInfo.isCompleted) habitColor else habitColor.copy(alpha = 0.12f)
                                        )
                                        .border(
                                            width = if (dayInfo.isSelected) 1.2.dp else 0.dp,
                                            color = if (dayInfo.isSelected) habitColor else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayInfo.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom metadata & Best streak record
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Best Streak",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Best record: $bestStreak days",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_habit_${habit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHabitsCard(filter: String, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6750A4).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = null,
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = if (filter == "All") "No Habits Found" else "No $filter Habits",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Build discipline with daily fitness habits and maintain visual streaks.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Habit")
            }
        }
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, iconKey: String, colorHex: Long, frequency: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Hydration") }
    var selectedIconKey by remember { mutableStateOf("water") }
    var selectedColorHex by remember { mutableStateOf(0xFF0284C7) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        Triple("Hydration", "water", 0xFF0284C7),
        Triple("Cardio", "cardio", 0xFFE11D48),
        Triple("Nutrition", "protein", 0xFF16A34A),
        Triple("Steps", "steps", 0xFF6750A4),
        Triple("Recovery", "stretch", 0xFFD97706),
        Triple("Sleep", "sleep", 0xFF7C3AED),
        Triple("Supplements", "pill", 0xFF0D9488),
        Triple("Strength", "strength", 0xFF475569)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Daily Fitness Habit",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Habit Title (e.g. 100 Pushups, 3L Water)") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_habit_title")
                )

                Text(
                    text = "SELECT CATEGORY & ICON",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6750A4),
                    letterSpacing = 1.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { (cat, iconKey, colorHex) ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(colorHex) else Color(colorHex).copy(alpha = 0.1f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(colorHex) else Color(colorHex).copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedCategory = cat
                                    selectedIconKey = iconKey
                                    selectedColorHex = colorHex
                                }
                                .testTag("select_category_$cat")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = getHabitIconFromKey(iconKey),
                                    contentDescription = cat,
                                    tint = if (isSelected) Color.White else Color(colorHex),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(colorHex)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a habit title"
                    } else {
                        onConfirm(title, selectedCategory, selectedIconKey, selectedColorHex, "Daily")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_save_habit")
            ) {
                Text("Save Habit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun changeDay(currentDateStr: String, delta: Int, onResult: (String) -> Unit) {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(currentDateStr)
        if (date != null) {
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, delta)
            }
            onResult(sdf.format(cal.time))
        }
    } catch (e: Exception) {
        // ignore
    }
}
