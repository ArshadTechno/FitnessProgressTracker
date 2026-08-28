package com.awscubetech.fitnesstracker.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.awscubetech.fitnesstracker.data.gemini.AnalysisType
import com.awscubetech.fitnesstracker.data.gemini.FitnessAiAnalyzer
import com.awscubetech.fitnesstracker.data.gemini.FitnessAnalysisResult
import com.awscubetech.fitnesstracker.data.local.BodyMeasurementEntity
import com.awscubetech.fitnesstracker.data.local.FitnessDatabase
import com.awscubetech.fitnesstracker.data.local.FitnessScanEntity
import com.awscubetech.fitnesstracker.data.local.HabitDayInfo
import com.awscubetech.fitnesstracker.data.local.HabitEntity
import com.awscubetech.fitnesstracker.data.local.HabitLogEntity
import com.awscubetech.fitnesstracker.data.local.SavedGymEntity
import com.awscubetech.fitnesstracker.data.local.AthleteGoalEntity
import com.awscubetech.fitnesstracker.data.local.WeeklyDayConsistency
import com.awscubetech.fitnesstracker.data.local.WeeklyConsistencySummary
import com.awscubetech.fitnesstracker.data.local.WorkoutLogEntity
import com.awscubetech.fitnesstracker.data.models.FitnessStaticData
import com.awscubetech.fitnesstracker.data.repository.FitnessRepository
import com.awscubetech.fitnesstracker.domain.FitnessCalculators
import com.awscubetech.fitnesstracker.util.HabitReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class ScreenDestination {
    object Home : ScreenDestination()
    data class CameraAnalysis(val initialType: AnalysisType = AnalysisType.PHYSIQUE_PROGRESS) : ScreenDestination()
    object Measurements : ScreenDestination()
    object DailyHabits : ScreenDestination()
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
    val habits: StateFlow<List<HabitEntity>>
    val habitLogs: StateFlow<List<HabitLogEntity>>
    val savedGyms: StateFlow<List<SavedGymEntity>>
    val primaryAthleteGoal: StateFlow<AthleteGoalEntity?>

    private val _selectedHabitDate = MutableStateFlow(getTodayDateString())
    val selectedHabitDate: StateFlow<String> = _selectedHabitDate.asStateFlow()

    private val _isReminderDismissed = MutableStateFlow(false)
    val isReminderDismissed: StateFlow<Boolean> = _isReminderDismissed.asStateFlow()

    private val _forceShowReminder = MutableStateFlow(false)

    val show8PmHabitReminder: StateFlow<Boolean>

    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Home)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val sharedPrefs = application.getSharedPreferences("fitness_tracker_preferences", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("pref_dark_theme", false))
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

        habits = repository.allHabits.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        habitLogs = repository.allHabitLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        savedGyms = repository.allSavedGyms.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        primaryAthleteGoal = repository.primaryAthleteGoal.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        show8PmHabitReminder = combine(
            habits,
            habitLogs,
            _isReminderDismissed,
            _forceShowReminder
        ) { habitList, logs, isDismissed, force ->
            if (isDismissed) return@combine false
            if (habitList.isEmpty()) return@combine false
            val todayStr = getTodayDateString()
            val completedToday = habitList.count { habit ->
                logs.any { it.habitId == habit.id && it.dateFormatted == todayStr && it.isCompleted }
            }
            val isTimePast8 = HabitReminderManager.isPast8Pm()
            (isTimePast8 || force) && completedToday == 0
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
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

        // Populate initial habits and streak logs
        viewModelScope.launch {
            repository.allHabits.collect { list ->
                if (list.isEmpty()) {
                    seedDefaultHabits()
                }
            }
        }

        // Populate initial gyms if database is empty
        viewModelScope.launch {
            repository.allSavedGyms.collect { list ->
                if (list.isEmpty()) {
                    FitnessStaticData.fitnessCenters.forEach { center ->
                        repository.insertGymDirect(
                            SavedGymEntity(
                                name = center.name,
                                category = center.category,
                                rating = center.rating,
                                reviewCount = center.reviewCount,
                                distanceKm = center.distanceKm,
                                address = center.address,
                                openingHours = center.openingHours,
                                isOpenNow = center.isOpenNow,
                                facilitiesCsv = center.facilities.joinToString(", "),
                                phoneNumber = center.phoneNumber,
                                isCustomUserGym = false,
                                isFavorite = center.id == "gym_1"
                            )
                        )
                    }
                }
            }
        }

        // Populate initial athlete goal if empty
        viewModelScope.launch {
            repository.primaryAthleteGoal.collect { goal ->
                if (goal == null) {
                    repository.saveAthleteGoal(
                        AthleteGoalEntity(
                            id = "primary_goal",
                            benchmarkId = "ath_1",
                            benchmarkName = "Classic Bodybuilding Standard",
                            targetBenchKg = 140.0,
                            targetSquatKg = 180.0,
                            targetDeadliftKg = 220.0,
                            userCurrentBenchKg = 85.0,
                            userCurrentSquatKg = 110.0,
                            userCurrentDeadliftKg = 135.0
                        )
                    )
                }
            }
        }

        // Initial calculations
        calculateAllDefaults()
    }

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
        if (screen is ScreenDestination.CameraAnalysis) {
            _currentAnalysisType.value = screen.initialType
        }
    }

    fun toggleDarkMode() {
        val newMode = !_isDarkMode.value
        _isDarkMode.value = newMode
        sharedPrefs.edit().putBoolean("pref_dark_theme", newMode).apply()
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        sharedPrefs.edit().putBoolean("pref_dark_theme", enabled).apply()
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

    // Daily Habit Management
    fun setSelectedHabitDate(date: String) {
        _selectedHabitDate.value = date
    }

    fun toggleHabit(habitId: Long, dateString: String? = null, note: String = "") {
        val targetDate = dateString ?: _selectedHabitDate.value
        val isCurrentlyCompleted = habitLogs.value.any { it.habitId == habitId && it.dateFormatted == targetDate }
        viewModelScope.launch {
            repository.toggleHabitLog(habitId, targetDate, isCurrentlyCompleted, note)
        }
    }

    fun addCustomHabit(
        title: String,
        category: String,
        iconKey: String,
        colorHex: Long,
        frequency: String = "Daily",
        targetDaily: Int = 1
    ) {
        viewModelScope.launch {
            repository.addHabit(
                title = title.trim(),
                category = category,
                iconKey = iconKey,
                colorHex = colorHex,
                frequency = frequency,
                targetDaily = targetDaily
            )
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Reminder Actions & Testing
    fun dismissHabitReminder() {
        _isReminderDismissed.value = true
    }

    fun triggerTest8PmReminder(context: android.content.Context? = null) {
        _isReminderDismissed.value = false
        _forceShowReminder.value = true
        if (context != null) {
            HabitReminderManager.postSystemNotification(context, habits.value.size)
        }
    }

    fun resetReminderDismissal() {
        _isReminderDismissed.value = false
        _forceShowReminder.value = false
    }

    fun checkAndTrigger8PmNotification(context: android.content.Context) {
        val totalHabits = habits.value.size
        val todayStr = getTodayDateString()
        val completedToday = habits.value.count { habit ->
            habitLogs.value.any { it.habitId == habit.id && it.dateFormatted == todayStr && it.isCompleted }
        }
        if (HabitReminderManager.shouldShow8PmReminder(totalHabits, completedToday)) {
            HabitReminderManager.postSystemNotification(context, totalHabits)
        }
    }

    private suspend fun seedDefaultHabits() {
        val habitWaterId = repository.addHabit(
            title = "Drank 3L Water",
            category = "Hydration",
            iconKey = "water",
            colorHex = 0xFF0284C7,
            frequency = "Daily"
        )
        val habitCardioId = repository.addHabit(
            title = "Cardio Session (30m)",
            category = "Cardio",
            iconKey = "cardio",
            colorHex = 0xFFE11D48,
            frequency = "Daily"
        )
        val habitProteinId = repository.addHabit(
            title = "Hit 150g Protein Target",
            category = "Nutrition",
            iconKey = "protein",
            colorHex = 0xFF16A34A,
            frequency = "Daily"
        )
        val habitStepsId = repository.addHabit(
            title = "10,000 Steps Walking",
            category = "Steps",
            iconKey = "steps",
            colorHex = 0xFF6750A4,
            frequency = "Daily"
        )
        val habitStretchId = repository.addHabit(
            title = "Post-Workout Stretching & Mobility",
            category = "Recovery",
            iconKey = "stretch",
            colorHex = 0xFFD97706,
            frequency = "Daily"
        )
        val habitSleepId = repository.addHabit(
            title = "8 Hours Restful Sleep",
            category = "Sleep",
            iconKey = "sleep",
            colorHex = 0xFF7C3AED,
            frequency = "Daily"
        )
        val habitSuppsId = repository.addHabit(
            title = "Daily Vitamins & Creatine",
            category = "Supplements",
            iconKey = "pill",
            colorHex = 0xFF0D9488,
            frequency = "Daily"
        )

        // Seed realistic past logs for visual streaks (last 5 days)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        // Days: 4 days ago, 3 days ago, 2 days ago, yesterday, today
        for (i in 4 downTo 0) {
            val logCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = sdf.format(logCal.time)

            // Seed completions for water (5 day streak)
            repository.logHabitExplicit(habitWaterId, dateStr, isCompleted = true)

            // Seed completions for cardio (4 day streak)
            if (i <= 3) {
                repository.logHabitExplicit(habitCardioId, dateStr, isCompleted = true)
            }

            // Seed completions for protein (5 day streak)
            repository.logHabitExplicit(habitProteinId, dateStr, isCompleted = true)

            // Seed completions for steps (3 day streak)
            if (i <= 2) {
                repository.logHabitExplicit(habitStepsId, dateStr, isCompleted = true)
            }

            // Seed stretching (4 day streak)
            if (i in 1..4) {
                repository.logHabitExplicit(habitStretchId, dateStr, isCompleted = true)
            }

            // Seed sleep (5 day streak)
            repository.logHabitExplicit(habitSleepId, dateStr, isCompleted = true)

            // Seed supps (5 day streak)
            repository.logHabitExplicit(habitSuppsId, dateStr, isCompleted = true)
        }
    }

    // Streak & Heatmap computation utilities
    fun calculateCurrentStreak(habitId: Long, logs: List<HabitLogEntity>): Int {
        val completedDates = logs.filter { it.habitId == habitId && it.isCompleted }.map { it.dateFormatted }.toSet()
        if (completedDates.isEmpty()) return 0

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        var streak = 0
        val isTodayDone = completedDates.contains(todayStr)

        if (isTodayDone) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
            while (completedDates.contains(sdf.format(cal.time))) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
        } else {
            // Check if streak was active through yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1)
            while (completedDates.contains(sdf.format(cal.time))) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }
        return streak
    }

    fun calculateBestStreak(habitId: Long, logs: List<HabitLogEntity>): Int {
        val completedDates = logs.filter { it.habitId == habitId && it.isCompleted }
            .map { it.dateFormatted }
            .distinct()
            .sorted()
        if (completedDates.isEmpty()) return 0

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var maxStreak = 1
        var currentStreak = 1

        for (i in 0 until completedDates.size - 1) {
            try {
                val d1 = sdf.parse(completedDates[i])
                val d2 = sdf.parse(completedDates[i + 1])
                if (d1 != null && d2 != null) {
                    val diffDays = ((d2.time - d1.time) / (1000 * 60 * 60 * 24)).toInt()
                    if (diffDays == 1) {
                        currentStreak++
                        if (currentStreak > maxStreak) maxStreak = currentStreak
                    } else if (diffDays > 1) {
                        currentStreak = 1
                    }
                }
            } catch (e: Exception) {
                // ignore parsing error
            }
        }
        return maxOf(maxStreak, calculateCurrentStreak(habitId, logs))
    }

    fun get7DayHeatmap(habitId: Long, logs: List<HabitLogEntity>, selectedDateString: String): List<HabitDayInfo> {
        val completedDates = logs.filter { it.habitId == habitId && it.isCompleted }.map { it.dateFormatted }.toSet()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayLetterFormat = SimpleDateFormat("EE", Locale.getDefault())
        val dayNumFormat = SimpleDateFormat("d", Locale.getDefault())
        val todayStr = sdf.format(Date())

        val result = mutableListOf<HabitDayInfo>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = sdf.format(cal.time)
            val dayLetter = dayLetterFormat.format(cal.time).take(1).uppercase()
            val dayNum = dayNumFormat.format(cal.time)

            result.add(
                HabitDayInfo(
                    dateString = dateStr,
                    dayLabel = dayLetter,
                    dayNumber = dayNum,
                    isCompleted = completedDates.contains(dateStr),
                    isToday = dateStr == todayStr,
                    isSelected = dateStr == selectedDateString
                )
            )
        }
        return result
    }

    fun markAllHabitsForDate(targetDate: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val allHabits = habits.value
            for (habit in allHabits) {
                repository.logHabitExplicit(habit.id, targetDate, isCompleted)
            }
        }
    }

    fun calculateWeeklyConsistencySummary(
        logs: List<HabitLogEntity>,
        allHabits: List<HabitEntity>,
        selectedDateString: String
    ): WeeklyConsistencySummary {
        val totalHabitsCount = allHabits.size
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumFormat = SimpleDateFormat("d", Locale.getDefault())
        val todayStr = getTodayDateString()

        val daysList = mutableListOf<WeeklyDayConsistency>()
        var totalCompletions = 0
        var totalOpportunities = 0
        var perfectDays = 0

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = sdf.format(cal.time)
            val dayName = dayNameFormat.format(cal.time)
            val dayNum = dayNumFormat.format(cal.time)

            val completedForDay = allHabits.count { habit ->
                logs.any { it.habitId == habit.id && it.dateFormatted == dateStr && it.isCompleted }
            }

            val percentage = if (totalHabitsCount > 0) completedForDay.toFloat() / totalHabitsCount else 0f
            if (completedForDay == totalHabitsCount && totalHabitsCount > 0) {
                perfectDays++
            }

            totalCompletions += completedForDay
            totalOpportunities += totalHabitsCount

            daysList.add(
                WeeklyDayConsistency(
                    dateString = dateStr,
                    dayOfWeekName = dayName,
                    dayNumber = dayNum,
                    completedHabitsCount = completedForDay,
                    totalHabitsCount = totalHabitsCount,
                    completionPercentage = percentage,
                    isToday = dateStr == todayStr,
                    isSelected = dateStr == selectedDateString
                )
            )
        }

        val avgPercent = if (totalOpportunities > 0) {
            ((totalCompletions.toDouble() / totalOpportunities.toDouble()) * 100).toInt()
        } else {
            0
        }

        val statusText = when {
            avgPercent >= 90 -> "🔥 Elite Discipline • Peak Consistency"
            avgPercent >= 75 -> "⚡ High Momentum • Outstanding Work"
            avgPercent >= 50 -> "🌱 Solid Progress • Keep Pushing"
            else -> "🎯 Building Habits • Daily Effort Counts"
        }

        val activeStreaks = allHabits.count { calculateCurrentStreak(it.id, logs) > 0 }

        return WeeklyConsistencySummary(
            days = daysList,
            averageConsistencyPercent = avgPercent,
            totalHabitsCompletedThisWeek = totalCompletions,
            perfectDaysCount = perfectDays,
            consistencyStatusText = statusText,
            activeStreaksCount = activeStreaks
        )
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
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

    fun addCustomGym(
        name: String,
        category: String,
        address: String,
        phoneNumber: String = "+1 (555) 000-1234",
        facilitiesCsv: String = "Weights, Cardio, Locker Rooms",
        openingHours: String = "6:00 AM - 10:00 PM"
    ) {
        viewModelScope.launch {
            repository.addGym(
                name = name,
                category = category,
                address = address,
                phoneNumber = phoneNumber,
                facilitiesCsv = facilitiesCsv,
                openingHours = openingHours,
                rating = 5.0,
                distanceKm = 0.5,
                isCustomUserGym = true
            )
        }
    }

    fun toggleGymFavorite(gym: SavedGymEntity) {
        viewModelScope.launch {
            repository.toggleGymFavorite(gym)
        }
    }

    fun deleteGym(gym: SavedGymEntity) {
        viewModelScope.launch {
            repository.deleteGym(gym)
        }
    }

    fun updateAthleteGoal(
        benchmarkId: String,
        benchmarkName: String,
        targetBenchKg: Double,
        targetSquatKg: Double,
        targetDeadliftKg: Double,
        userCurrentBenchKg: Double,
        userCurrentSquatKg: Double,
        userCurrentDeadliftKg: Double
    ) {
        viewModelScope.launch {
            repository.saveAthleteGoal(
                AthleteGoalEntity(
                    id = "primary_goal",
                    benchmarkId = benchmarkId,
                    benchmarkName = benchmarkName,
                    targetBenchKg = targetBenchKg,
                    targetSquatKg = targetSquatKg,
                    targetDeadliftKg = targetDeadliftKg,
                    userCurrentBenchKg = userCurrentBenchKg,
                    userCurrentSquatKg = userCurrentSquatKg,
                    userCurrentDeadliftKg = userCurrentDeadliftKg,
                    updatedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateUserCurrentPrs(benchKg: Double, squatKg: Double, deadliftKg: Double) {
        viewModelScope.launch {
            val current = primaryAthleteGoal.value ?: AthleteGoalEntity()
            repository.saveAthleteGoal(
                current.copy(
                    userCurrentBenchKg = benchKg,
                    userCurrentSquatKg = squatKg,
                    userCurrentDeadliftKg = deadliftKg,
                    updatedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning, Athlete"
            in 12..16 -> "Good Afternoon, Athlete"
            in 17..21 -> "Good Evening, Athlete"
            else -> "Late Night Grind, Athlete"
        }
    }

    fun getDynamicDailyQuote(): Pair<String, String> {
        val quotes = listOf(
            "Progress is built through daily relentless execution." to "Atomic Habits Rule",
            "True strength is forged when consistency meets discipline." to "Champion Mindset",
            "Small 1% improvements compound into monumental transformation." to "Hypertrophy Science",
            "Recovery is not idle time; it is where muscle protein synthesis happens." to "Physiology Insight",
            "The body achieves what the disciplined mind believes." to "Peak Athletic Form",
            "Focus on form, progressive overload, and adequate hydration." to "Biomechanics Standard",
            "Consistency over intensity: show up every single day." to "Iron Principle"
        )
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return quotes[dayOfYear % quotes.size]
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
