package com.awscubetech.fitnesstracker.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awscubetech.fitnesstracker.data.local.HabitEntity
import com.awscubetech.fitnesstracker.data.local.HabitLogEntity
import com.awscubetech.fitnesstracker.data.local.WeeklyConsistencySummary

@Composable
fun HomeHabitsWidget(
    habits: List<HabitEntity>,
    habitLogs: List<HabitLogEntity>,
    todayDateString: String,
    weeklySummary: WeeklyConsistencySummary,
    onToggleHabit: (Long) -> Unit,
    onOpenFullHabits: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = habits.size
    val completedToday = habits.count { habit ->
        habitLogs.any { it.habitId == habit.id && it.dateFormatted == todayDateString && it.isCompleted }
    }
    val completionRatio = if (totalCount > 0) completedToday.toFloat() / totalCount else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = completionRatio,
        animationSpec = tween(500),
        label = "home_habit_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenFullHabits() }
            .testTag("home_habits_widget_card"),
        shape = RoundedCornerShape(20.dp),
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
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title + Streaks + Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6750A4).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "TODAY'S FITNESS GOALS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$completedToday of $totalCount Completed",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF6750A4).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${(completionRatio * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF6750A4),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open Habits",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Mark-Off Scrollable Pill Buttons for Today
            if (habits.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        val isDone = habitLogs.any {
                            it.habitId == habit.id && it.dateFormatted == todayDateString && it.isCompleted
                        }
                        val habitColor = Color(habit.colorHex)

                        val pillBg by animateColorAsState(
                            targetValue = if (isDone) habitColor else habitColor.copy(alpha = 0.08f),
                            animationSpec = tween(250),
                            label = "home_habit_pill_${habit.id}"
                        )
                        val textColor = if (isDone) Color.White else MaterialTheme.colorScheme.onSurface

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleHabit(habit.id) }
                                .testTag("home_quick_toggle_${habit.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = pillBg,
                            border = BorderStroke(
                                1.dp,
                                if (isDone) habitColor else habitColor.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (isDone) Color.White else habitColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = habitColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = getHabitIconFromKey(habit.iconKey),
                                            contentDescription = null,
                                            tint = habitColor,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = habit.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 7-Day Consistency Mini Dots Strip
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = Color(0xFF6750A4),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "7-Day Consistency: ${weeklySummary.averageConsistencyPercent}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 7 dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (day in weeklySummary.days) {
                            val dotColor = when {
                                day.completionPercentage >= 0.99f -> Color(0xFF16A34A)
                                day.completionPercentage >= 0.5f -> Color(0xFF6750A4)
                                day.completionPercentage > 0f -> Color(0xFFEA580C)
                                else -> Color(0xFF94A3B8).copy(alpha = 0.4f)
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .border(
                                        width = if (day.isToday) 1.dp else 0.dp,
                                        color = if (day.isToday) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
