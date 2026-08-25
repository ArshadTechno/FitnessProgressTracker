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
