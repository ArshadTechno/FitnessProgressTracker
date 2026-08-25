package com.example.data.repository

import com.example.data.gemini.AnalysisType
import com.example.data.gemini.FitnessAiAnalyzer
import com.example.data.gemini.FitnessAnalysisResult
import com.example.data.local.BodyMeasurementEntity
import com.example.data.local.FitnessDao
import com.example.data.local.FitnessScanEntity
import com.example.data.local.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitnessRepository(private val fitnessDao: FitnessDao) {

    val allScans: Flow<List<FitnessScanEntity>> = fitnessDao.getAllScans()
    val allMeasurements: Flow<List<BodyMeasurementEntity>> = fitnessDao.getAllMeasurements()
    val allWorkoutLogs: Flow<List<WorkoutLogEntity>> = fitnessDao.getAllWorkoutLogs()

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
}
