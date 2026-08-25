package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    // Scans
    @Query("SELECT * FROM fitness_scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<FitnessScanEntity>>

    @Query("SELECT * FROM fitness_scans WHERE scanType = :type ORDER BY timestamp DESC")
    fun getScansByType(type: String): Flow<List<FitnessScanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: FitnessScanEntity): Long

    @Delete
    suspend fun deleteScan(scan: FitnessScanEntity)

    @Query("DELETE FROM fitness_scans")
    suspend fun clearAllScans()

    // Measurements
    @Query("SELECT * FROM body_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long

    @Delete
    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity)

    // Workout Logs
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(workoutLog: WorkoutLogEntity): Long

    @Delete
    suspend fun deleteWorkoutLog(workoutLog: WorkoutLogEntity)
}
