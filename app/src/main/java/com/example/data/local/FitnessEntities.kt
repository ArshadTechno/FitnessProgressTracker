package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_scans")
data class FitnessScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scanType: String, // "PHYSIQUE", "EXERCISE_FORM", "MEAL"
    val title: String,
    val dateFormatted: String,
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int,
    val primaryMetricLabel: String,
    val primaryMetricValue: String,
    val secondaryMetricLabel: String,
    val secondaryMetricValue: String,
    val summary: String,
    val insightsCsv: String,
    val recommendationsCsv: String,
    val notes: String = ""
)

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateFormatted: String,
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Double,
    val bodyFatPercent: Double = 0.0,
    val waistCm: Double = 0.0,
    val chestCm: Double = 0.0,
    val armCm: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateFormatted: String,
    val timestamp: Long = System.currentTimeMillis(),
    val routineName: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val exercisesCompleted: Int,
    val intensity: String = "Moderate",
    val notes: String = ""
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Hydration", "Cardio", "Nutrition", "Steps", "Recovery", "Sleep", "Strength"
    val frequency: String = "Daily",
    val iconKey: String = "water", // "water", "cardio", "protein", "steps", "stretch", "sleep", "strength", "pill"
    val colorHex: Long = 0xFF6750A4,
    val targetDaily: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateFormatted: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true,
    val note: String = ""
)

data class HabitDayInfo(
    val dateString: String,
    val dayLabel: String,
    val dayNumber: String,
    val isCompleted: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean
)

data class WeeklyDayConsistency(
    val dateString: String,
    val dayOfWeekName: String, // "Mon", "Tue", etc.
    val dayNumber: String,     // "26"
    val completedHabitsCount: Int,
    val totalHabitsCount: Int,
    val completionPercentage: Float, // 0.0f - 1.0f
    val isToday: Boolean,
    val isSelected: Boolean
)

data class WeeklyConsistencySummary(
    val days: List<WeeklyDayConsistency>,
    val averageConsistencyPercent: Int,
    val totalHabitsCompletedThisWeek: Int,
    val perfectDaysCount: Int,
    val consistencyStatusText: String,
    val activeStreaksCount: Int
)
