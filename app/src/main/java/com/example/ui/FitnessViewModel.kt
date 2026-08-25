package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gemini.AnalysisType
import com.example.data.gemini.FitnessAiAnalyzer
import com.example.data.gemini.FitnessAnalysisResult
import com.example.data.local.BodyMeasurementEntity
import com.example.data.local.FitnessDatabase
import com.example.data.local.FitnessScanEntity
import com.example.data.local.WorkoutLogEntity
import com.example.data.repository.FitnessRepository
import com.example.domain.FitnessCalculators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class ScreenDestination {
    object Home : ScreenDestination()
    object CameraAnalysis : ScreenDestination()
    object Measurements : ScreenDestination()
    object WorkoutLogs : ScreenDestination()
    object Calculators : ScreenDestination()
    object GymsNearMe : ScreenDestination()
    object AthleteBenchmarks : ScreenDestination()
    object GoalCountdown : ScreenDestination()
}

sealed class AiAnalysisUiState {
    object Idle : AiAnalysisUiState()
    object Analyzing : AiAnalysisUiState()
    data class Success(val result: FitnessAnalysisResult) : AiAnalysisUiState()
    data class Error(val message: String) : AiAnalysisUiState()
}

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitnessRepository

    val scans: StateFlow<List<FitnessScanEntity>>
    val measurements: StateFlow<List<BodyMeasurementEntity>>
    val workoutLogs: StateFlow<List<WorkoutLogEntity>>

    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Home)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _analysisState = MutableStateFlow<AiAnalysisUiState>(AiAnalysisUiState.Idle)
    val analysisState: StateFlow<AiAnalysisUiState> = _analysisState.asStateFlow()

    private val _selectedAnalysisBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedAnalysisBitmap: StateFlow<Bitmap?> = _selectedAnalysisBitmap.asStateFlow()

    private val _currentAnalysisType = MutableStateFlow(AnalysisType.PHYSIQUE_PROGRESS)
    val currentAnalysisType: StateFlow<AnalysisType> = _currentAnalysisType.asStateFlow()

    // Calculator inputs & states
    val bmiHeightInput = MutableStateFlow("178")
    val bmiWeightInput = MutableStateFlow("75")
    val bmiResult = MutableStateFlow<FitnessCalculators.BmiResult?>(null)

    val tdeeGender = MutableStateFlow("Male")
    val tdeeAge = MutableStateFlow("26")
    val tdeeHeight = MutableStateFlow("178")
    val tdeeWeight = MutableStateFlow("75")
    val tdeeActivityMultiplier = MutableStateFlow(1.55) // Moderate
    val tdeeResult = MutableStateFlow<FitnessCalculators.TdeeResult?>(null)

    val bfIsMale = MutableStateFlow(true)
    val bfHeight = MutableStateFlow("178")
    val bfNeck = MutableStateFlow("39")
    val bfWaist = MutableStateFlow("84")
    val bfHip = MutableStateFlow("96")
    val bfWeight = MutableStateFlow("75")
    val bfResult = MutableStateFlow<FitnessCalculators.BodyFatResult?>(null)

    val rmExercise = MutableStateFlow("Bench Press")
    val rmWeight = MutableStateFlow("85")
    val rmReps = MutableStateFlow("6")
    val rmResult = MutableStateFlow<FitnessCalculators.OneRepMaxResult?>(null)

    val hrAge = MutableStateFlow("26")
    val hrResting = MutableStateFlow("62")
    val hrResult = MutableStateFlow<FitnessCalculators.HeartRateResult?>(null)

    val targetGoalStartDate = MutableStateFlow(Date())
    val targetGoalEndDate = MutableStateFlow(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 90) }.time)
    val goalDateResult = MutableStateFlow<FitnessCalculators.DateDifferenceResult?>(null)

    init {
        val db = FitnessDatabase.getDatabase(application)
        repository = FitnessRepository(db.fitnessDao())

        scans = repository.allScans.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        measurements = repository.allMeasurements.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        workoutLogs = repository.allWorkoutLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Populate initial demo measurements and logs if empty
        viewModelScope.launch {
            repository.allMeasurements.collect { list ->
                if (list.isEmpty()) {
                    repository.addMeasurement(79.4, 19.0, 87.0, 101.5, 35.5, "Starting transformation phase", "15 - 06 - 2026")
                    repository.addMeasurement(78.5, 18.2, 86.0, 102.0, 36.0, "Initial assessment baseline", "01 - 07 - 2026")
                    repository.addMeasurement(77.2, 17.1, 84.5, 103.0, 36.5, "Week 4 progress update", "22 - 07 - 2026")
                    repository.addMeasurement(76.4, 16.4, 83.5, 103.5, 36.8, "Mid-cycle check-in", "08 - 08 - 2026")
                    repository.addMeasurement(75.8, 15.8, 82.8, 104.0, 37.2, "Current active phase", "25 - 08 - 2026")
                }
            }
        }

        viewModelScope.launch {
            repository.allWorkoutLogs.collect { list ->
                if (list.isEmpty()) {
                    repository.addWorkoutLog("Upper Body Push & Core", 55, 480, 7, "High", "Hit new bench PR of 85kg for 6 reps")
                    repository.addWorkoutLog("Legs & Posterior Chain", 60, 560, 6, "High", "Barbell squat focus with 3-sec eccentric pause")
                    repository.addWorkoutLog("HIIT Cardio & Mobility", 40, 390, 8, "Moderate", "Zone 4 intervals + thoracic spine mobility")
                }
            }
        }

        // Initial calculations
        calculateAllDefaults()
    }

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setAnalysisType(type: AnalysisType) {
        _currentAnalysisType.value = type
    }

    fun setSelectedBitmap(bitmap: Bitmap?) {
        _selectedAnalysisBitmap.value = bitmap
    }

    fun loadBitmapFromUri(uri: Uri) {
        try {
            val context = getApplication<Application>()
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            _selectedAnalysisBitmap.value = bitmap
        } catch (e: Exception) {
            _analysisState.value = AiAnalysisUiState.Error("Could not load image: ${e.message}")
        }
    }

    fun runAiAnalysis(notes: String = "") {
        val bitmap = _selectedAnalysisBitmap.value
        if (bitmap == null) {
            // Generate a sample synthetic frame if user doesn't have an image ready
            val sampleBitmap = createSampleAthleticFrame()
            _selectedAnalysisBitmap.value = sampleBitmap
        }

        val targetBitmap = _selectedAnalysisBitmap.value ?: createSampleAthleticFrame()

        viewModelScope.launch {
            _analysisState.value = AiAnalysisUiState.Analyzing
            try {
                val result = FitnessAiAnalyzer.analyzeImage(
                    bitmap = targetBitmap,
                    analysisType = _currentAnalysisType.value,
                    additionalUserNotes = notes
                )
                _analysisState.value = AiAnalysisUiState.Success(result)
                // Automatically persist scan result in Room
                repository.saveScanResult(result, notes)
            } catch (e: Exception) {
                _analysisState.value = AiAnalysisUiState.Error("Analysis failed: ${e.message}")
            }
        }
    }

    fun resetAnalysis() {
        _analysisState.value = AiAnalysisUiState.Idle
        _selectedAnalysisBitmap.value = null
    }

    fun deleteScan(scan: FitnessScanEntity) {
        viewModelScope.launch {
            repository.deleteScan(scan)
        }
    }

    fun addMeasurement(weight: Double, bodyFat: Double, waist: Double, chest: Double, arm: Double, notes: String) {
        viewModelScope.launch {
            repository.addMeasurement(weight, bodyFat, waist, chest, arm, notes)
        }
    }

    fun deleteMeasurement(measurement: BodyMeasurementEntity) {
        viewModelScope.launch {
            repository.deleteMeasurement(measurement)
        }
    }

    fun addWorkoutLog(name: String, duration: Int, calories: Int, exercises: Int, intensity: String, notes: String) {
        viewModelScope.launch {
            repository.addWorkoutLog(name, duration, calories, exercises, intensity, notes)
        }
    }

    fun deleteWorkoutLog(workoutLog: WorkoutLogEntity) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(workoutLog)
        }
    }

    // Calculators
    fun calculateBmi() {
        val h = bmiHeightInput.value.toDoubleOrNull() ?: 178.0
        val w = bmiWeightInput.value.toDoubleOrNull() ?: 75.0
        bmiResult.value = FitnessCalculators.calculateBmi(h, w)
    }

    fun clearBmi() {
        bmiHeightInput.value = ""
        bmiWeightInput.value = ""
        bmiResult.value = null
    }

    fun calculateTdee() {
        val age = tdeeAge.value.toIntOrNull() ?: 26
        val h = tdeeHeight.value.toDoubleOrNull() ?: 178.0
        val w = tdeeWeight.value.toDoubleOrNull() ?: 75.0
        tdeeResult.value = FitnessCalculators.calculateTdee(
            gender = tdeeGender.value,
            age = age,
            heightCm = h,
            weightKg = w,
            activityLevelMultiplier = tdeeActivityMultiplier.value
        )
    }

    fun clearTdee() {
        tdeeAge.value = ""
        tdeeHeight.value = ""
        tdeeWeight.value = ""
        tdeeResult.value = null
    }

    fun calculateNavyBodyFat() {
        val h = bfHeight.value.toDoubleOrNull() ?: 178.0
        val neck = bfNeck.value.toDoubleOrNull() ?: 39.0
        val waist = bfWaist.value.toDoubleOrNull() ?: 84.0
        val hip = bfHip.value.toDoubleOrNull() ?: 96.0
        val w = bfWeight.value.toDoubleOrNull() ?: 75.0
        bfResult.value = FitnessCalculators.calculateNavyBodyFat(
            isMale = bfIsMale.value,
            heightCm = h,
            neckCm = neck,
            waistCm = waist,
            hipCm = hip,
            weightKg = w
        )
    }

    fun clearNavyBodyFat() {
        bfHeight.value = ""
        bfNeck.value = ""
        bfWaist.value = ""
        bfHip.value = ""
        bfWeight.value = ""
        bfResult.value = null
    }

    fun calculateOneRepMax() {
        val w = rmWeight.value.toDoubleOrNull() ?: 85.0
        val r = rmReps.value.toIntOrNull() ?: 6
        rmResult.value = FitnessCalculators.calculateOneRepMax(rmExercise.value, w, r)
    }

    fun clearOneRepMax() {
        rmWeight.value = ""
        rmReps.value = ""
        rmResult.value = null
    }

    fun calculateHeartRate() {
        val age = hrAge.value.toIntOrNull() ?: 26
        val rest = hrResting.value.toIntOrNull() ?: 62
        hrResult.value = FitnessCalculators.calculateHeartRateZones(age, rest)
    }

    fun calculateGoalDates() {
        goalDateResult.value = FitnessCalculators.calculateDateDifference(
            startDate = targetGoalStartDate.value,
            endDate = targetGoalEndDate.value,
            workoutDaysPerWeek = 4
        )
    }

    private fun calculateAllDefaults() {
        calculateBmi()
        calculateTdee()
        calculateNavyBodyFat()
        calculateOneRepMax()
        calculateHeartRate()
        calculateGoalDates()
    }

    private fun createSampleAthleticFrame(): Bitmap {
        val width = 400
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint()
        paint.color = android.graphics.Color.rgb(18, 48, 38)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw center athletic icon badge
        paint.color = android.graphics.Color.rgb(0, 168, 107)
        paint.strokeWidth = 6f
        paint.style = android.graphics.Paint.Style.STROKE
        canvas.drawCircle(200f, 200f, 120f, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 28f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText("FITNESS SCAN", 200f, 210f, paint)
        return bitmap
    }
}
