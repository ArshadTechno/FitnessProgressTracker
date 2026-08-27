package com.awscubetech.fitnesstracker.data.repository

import com.awscubetech.fitnesstracker.data.gemini.AnalysisType
import com.awscubetech.fitnesstracker.data.gemini.FitnessAiAnalyzer
import com.awscubetech.fitnesstracker.data.gemini.FitnessAnalysisResult
import com.awscubetech.fitnesstracker.data.local.BodyMeasurementEntity
import com.awscubetech.fitnesstracker.data.local.FitnessDao
import com.awscubetech.fitnesstracker.data.local.FitnessScanEntity
import com.awscubetech.fitnesstracker.data.local.HabitEntity
import com.awscubetech.fitnesstracker.data.local.HabitLogEntity
import com.awscubetech.fitnesstracker.data.local.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitnessRepository(private val fitnessDao: FitnessDao) {

    val allScans: Flow<List<FitnessScanEntity>> = fitnessDao.getAllScans()
    val allMeasurements: Flow<List<BodyMeasurementEntity>> = fitnessDao.getAllMeasurements()
    val allWorkoutLogs: Flow<List<WorkoutLogEntity>> = fitnessDao.getAllWorkoutLogs()
    val allHabits: Flow<List<HabitEntity>> = fitnessDao.getAllHabits()
    val allHabitLogs: Flow<List<HabitLogEntity>> = fitnessDao.getAllHabitLogs()

    suspend fun saveScanResult(result: FitnessAnalysisResult, notes: String = ""): Long {
        val dateFormat = SimpleDateFormat("dd - MM - yyyy", Locale.getDefault())
        val entity = FitnessScanEntity(
            scanType = result.type.name,
            title = result.title,
            dateFormatted = dateFormat.format(Date()),
            timestamp = System.currentTimeMillis(),
            score = result.score,
            primaryMetricLabel = result.primaryMetricLabel,
            primaryMetricValue = result.primaryMetricValue,
            secondaryMetricLabel = result.secondaryMetricLabel,
            secondaryMetricValue = result.secondaryMetricValue,
            summary = result.summary,
            insightsCsv = result.detailedInsights.joinToString(";;"),
            recommendationsCsv = result.recommendations.joinToString(";;"),
            notes = notes
        )
        return fitnessDao.insertScan(entity)
    }

    suspend fun deleteScan(scan: FitnessScanEntity) {
        fitnessDao.deleteScan(scan)
    }

    suspend fun clearAllScans() {
        fitnessDao.clearAllScans()
    }

    suspend fun addMeasurement(
        weightKg: Double,
        bodyFatPercent: Double = 0.0,
        waistCm: Double = 0.0,
        chestCm: Double = 0.0,
        armCm: Double = 0.0,
        notes: String = "",
        dateString: String? = null
    ): Long {
        val dateFormat = SimpleDateFormat("dd - MM - yyyy", Locale.getDefault())
        val entity = BodyMeasurementEntity(
            dateFormatted = dateString ?: dateFormat.format(Date()),
            weightKg = weightKg,
            bodyFatPercent = bodyFatPercent,
            waistCm = waistCm,
            chestCm = chestCm,
            armCm = armCm,
            notes = notes
        )
        return fitnessDao.insertMeasurement(entity)
    }

    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity) {
        fitnessDao.deleteMeasurement(measurement)
    }

    suspend fun addWorkoutLog(
        routineName: String,
        durationMinutes: Int,
        caloriesBurned: Int,
        exercisesCompleted: Int,
        intensity: String,
        notes: String = ""
    ): Long {
        val dateFormat = SimpleDateFormat("dd - MM - yyyy", Locale.getDefault())
        val entity = WorkoutLogEntity(
            dateFormatted = dateFormat.format(Date()),
            routineName = routineName,
            durationMinutes = durationMinutes,
            caloriesBurned = caloriesBurned,
            exercisesCompleted = exercisesCompleted,
            intensity = intensity,
            notes = notes
        )
        return fitnessDao.insertWorkoutLog(entity)
    }

    suspend fun deleteWorkoutLog(workoutLog: WorkoutLogEntity) {
        fitnessDao.deleteWorkoutLog(workoutLog)
    }

    // Habits API
    suspend fun addHabit(
        title: String,
        category: String,
        iconKey: String,
        colorHex: Long,
        frequency: String = "Daily",
        targetDaily: Int = 1
    ): Long {
        val habit = HabitEntity(
            title = title,
            category = category,
            frequency = frequency,
            iconKey = iconKey,
            colorHex = colorHex,
            targetDaily = targetDaily
        )
        return fitnessDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        fitnessDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        fitnessDao.clearLogsForHabit(habit.id)
        fitnessDao.deleteHabit(habit)
    }

    suspend fun toggleHabitLog(habitId: Long, dateFormatted: String, isCurrentlyCompleted: Boolean, note: String = "") {
        if (isCurrentlyCompleted) {
            fitnessDao.deleteHabitLog(habitId, dateFormatted)
        } else {
            val log = HabitLogEntity(
                habitId = habitId,
                dateFormatted = dateFormatted,
                timestamp = System.currentTimeMillis(),
                isCompleted = true,
                note = note
            )
            fitnessDao.insertHabitLog(log)
        }
    }

    suspend fun logHabitExplicit(habitId: Long, dateFormatted: String, isCompleted: Boolean, note: String = "") {
        if (isCompleted) {
            val log = HabitLogEntity(
                habitId = habitId,
                dateFormatted = dateFormatted,
                timestamp = System.currentTimeMillis(),
                isCompleted = true,
                note = note
            )
            fitnessDao.insertHabitLog(log)
        } else {
            fitnessDao.deleteHabitLog(habitId, dateFormatted)
        }
    }
}
