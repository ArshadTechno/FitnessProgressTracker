package com.awscubetech.fitnesstracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awscubetech.fitnesstracker.data.local.HabitEntity
import com.awscubetech.fitnesstracker.data.local.HabitLogEntity
import com.awscubetech.fitnesstracker.data.local.WeeklyConsistencySummary

@Composable
fun WeeklyConsistencyMatrixCard(
    summary: WeeklyConsistencySummary,
    habits: List<HabitEntity>,
    habitLogs: List<HabitLogEntity>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onToggleHabitForDate: (habitId: Long, dateString: String) -> Unit,
    onMarkAllTodayComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_consistency_matrix_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title & Weekly Consistency Grade
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6750A4).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = Color(0xFF6750A4),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "WEEKLY CONSISTENCY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.3.sp
                        )
                        Text(
                            text = "7-Day Habit Matrix",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF6750A4).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF6750A4).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${summary.averageConsistencyPercent}% AVG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF6750A4),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Status Banner (e.g. Elite Discipline / Peak Consistency)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF6750A4).copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color(0xFF6750A4).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = summary.consistencyStatusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${summary.perfectDaysCount} Perfect Days",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            // Visual Weekly 7-Day Completion Bar Graph
            WeeklyConsistencyBarChart(
                days = summary.days,
                onDateSelected = onDateSelected
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            // Interactive 7-Day Habit Grid Matrix
            Text(
                text = "INTERACTIVE WEEKLY MATRIX (TAP TO CHECK OFF)",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            InteractiveMatrixGrid(
                habits = habits,
                days = summary.days,
                habitLogs = habitLogs,
                onToggleHabitForDate = onToggleHabitForDate,
                onDateSelected = onDateSelected
            )

            // Bottom Quick Action: Mark All Today Complete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${summary.totalHabitsCompletedThisWeek} habits completed this week",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onMarkAllTodayComplete,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_mark_all_today_complete")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark Today Done",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyConsistencyBarChart(
    days: List<com.awscubetech.fitnesstracker.data.local.WeeklyDayConsistency>,
    onDateSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        for (day in days) {
            val animatedHeight by animateFloatAsState(
                targetValue = maxOf(0.12f, day.completionPercentage),
                animationSpec = tween(600),
                label = "bar_height_${day.dateString}"
            )
            val barColor = when {
                day.completionPercentage >= 0.99f -> Color(0xFF16A34A) // Green for 100%
                day.completionPercentage >= 0.5f -> Color(0xFF6750A4)  // Purple for good
                day.completionPercentage > 0f -> Color(0xFFEA580C)     // Orange for partial
                else -> Color(0xFF94A3B8)                              // Slate for 0%
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onDateSelected(day.dateString) }
                    .padding(horizontal = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Percentage text label above bar
                Text(
                    text = "${(day.completionPercentage * 100).toInt()}%",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (day.isSelected) barColor else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // The dynamic vertical bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = animatedHeight * 0.65f)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            if (day.isSelected) {
                                barColor
                            } else {
                                barColor.copy(alpha = 0.75f)
                            }
                        )
                        .border(
                            width = if (day.isSelected) 1.5.dp else 0.dp,
                            color = if (day.isSelected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Day Label (e.g. Mon, Tue)
                Text(
                    text = day.dayOfWeekName.take(3),
                    fontSize = 10.sp,
                    fontWeight = if (day.isToday || day.isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (day.isSelected) Color(0xFF6750A4) else MaterialTheme.colorScheme.onSurface
                )

                // Day number (e.g. 26) with small indicator if today
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = day.dayNumber,
                        fontSize = 11.sp,
                        fontWeight = if (day.isToday) FontWeight.Black else FontWeight.Normal,
                        color = if (day.isToday) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveMatrixGrid(
    habits: List<HabitEntity>,
    days: List<com.awscubetech.fitnesstracker.data.local.WeeklyDayConsistency>,
    habitLogs: List<HabitLogEntity>,
    onToggleHabitForDate: (habitId: Long, dateString: String) -> Unit,
    onDateSelected: (String) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Table Header: Habit column title + 7 Day column labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DAILY GOAL",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1.8f)
            )

            Row(
                modifier = Modifier.weight(3.2f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (day in days) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDateSelected(day.dateString) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.dayOfWeekName.take(1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (day.isSelected) Color(0xFF6750A4) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = day.dayNumber,
                            fontSize = 8.sp,
                            color = if (day.isToday) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Matrix Rows: Each Habit + 7 Interactive Check Circles
        for (habit in habits) {
            val habitColor = Color(habit.colorHex)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Habit Name & Category Icon
                Row(
                    modifier = Modifier.weight(1.8f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(habitColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getHabitIconFromKey(habit.iconKey),
                            contentDescription = null,
                            tint = habitColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = habit.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 7 Day check dots
                Row(
                    modifier = Modifier.weight(3.2f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in days) {
                        val isDone = habitLogs.any {
                            it.habitId == habit.id && it.dateFormatted == day.dateString && it.isCompleted
                        }

                        val dotBg by animateColorAsState(
                            targetValue = if (isDone) habitColor else habitColor.copy(alpha = 0.1f),
                            animationSpec = tween(250),
                            label = "dot_bg_${habit.id}_${day.dateString}"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(dotBg)
                                    .border(
                                        width = if (day.isSelected) 1.2.dp else 0.8.dp,
                                        color = if (isDone) habitColor else habitColor.copy(alpha = 0.25f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onToggleHabitForDate(habit.id, day.dateString)
                                    }
                                    .testTag("matrix_cell_${habit.id}_${day.dateString}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getHabitIconFromKey(key: String): ImageVector {
    return when (key.lowercase()) {
        "water" -> Icons.Default.Opacity
        "cardio" -> Icons.Default.DirectionsRun
        "protein" -> Icons.Default.Restaurant
        "steps" -> Icons.Default.DirectionsWalk
        "stretch" -> Icons.Default.SelfImprovement
        "sleep" -> Icons.Default.Bedtime
        "pill" -> Icons.Default.Medication
        "strength" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Check
    }
}
