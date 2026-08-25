package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.roundToInt

object FitnessCalculators {

    data class BmiResult(
        val bmi: Double,
        val category: String,
        val healthyMinWeight: Double,
        val healthyMaxWeight: Double,
        val primeScore: Double,
        val advice: String
    )

    fun calculateBmi(heightCm: Double, weightKg: Double): BmiResult {
        if (heightCm <= 0 || weightKg <= 0) {
            return BmiResult(0.0, "Invalid Input", 0.0, 0.0, 0.0, "Please enter positive numbers.")
        }
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        val roundedBmi = (bmi * 10).roundToInt() / 10.0

        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 24.9 -> "Normal / Athletic"
            bmi < 29.9 -> "Overweight"
            else -> "Obese"
        }

        val healthyMin = (18.5 * heightM * heightM * 10).roundToInt() / 10.0
        val healthyMax = (24.9 * heightM * heightM * 10).roundToInt() / 10.0
        val primeScore = (bmi / 25.0 * 100).roundToInt() / 100.0

        val advice = when (category) {
            "Underweight" -> "Focus on progressive resistance training with a nutrient-rich caloric surplus."
            "Normal / Athletic" -> "Excellent! Maintain regular physical training and balanced macronutrient fueling."
            "Overweight" -> "Implement moderate daily cardio and a modest caloric deficit (300-500 kcal) with high protein."
            else -> "Prioritize structured physical activity, daily step targets (8,000+), and whole food nutrition."
        }

        return BmiResult(roundedBmi, category, healthyMin, healthyMax, primeScore, advice)
    }

    data class TdeeResult(
        val bmr: Int,
        val tdeeMaintenance: Int,
        val cuttingCalories: Int,
        val aggressiveCutCalories: Int,
        val cleanBulkingCalories: Int,
        val proteinGrams: Int,
        val carbsGrams: Int,
        val fatsGrams: Int
    )

    fun calculateTdee(
        gender: String, // "Male" or "Female"
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevelMultiplier: Double // 1.2, 1.375, 1.55, 1.725, 1.9
    ): TdeeResult {
        // Mifflin-St Jeor Equation
        val bmr = if (gender.equals("Male", ignoreCase = true)) {
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
        } else {
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
        }

        val tdee = (bmr * activityLevelMultiplier).roundToInt()
        val cut = (tdee - 500).coerceAtLeast(1200)
        val aggressiveCut = (tdee - 750).coerceAtLeast(1200)
        val bulk = tdee + 350

        // Macro split for maintenance (2g protein per kg, 25% fats, remainder carbs)
        val proteinGrams = (weightKg * 2.0).roundToInt()
        val fatCalories = tdee * 0.25
        val fatsGrams = (fatCalories / 9.0).roundToInt()
        val carbCalories = tdee - (proteinGrams * 4) - fatCalories
        val carbsGrams = (carbCalories / 4.0).coerceAtLeast(50.0).roundToInt()

        return TdeeResult(
            bmr = bmr.roundToInt(),
            tdeeMaintenance = tdee,
            cuttingCalories = cut,
            aggressiveCutCalories = aggressiveCut,
            cleanBulkingCalories = bulk,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatsGrams = fatsGrams
        )
    }

    data class BodyFatResult(
        val bodyFatPercent: Double,
        val fatMassKg: Double,
        val leanMassKg: Double,
        val category: String,
        val idealBodyFatRange: String
    )

    fun calculateNavyBodyFat(
        isMale: Boolean,
        heightCm: Double,
        neckCm: Double,
        waistCm: Double,
        hipCm: Double = 0.0,
        weightKg: Double
    ): BodyFatResult {
        if (heightCm <= 0 || neckCm <= 0 || waistCm <= 0 || weightKg <= 0) {
            return BodyFatResult(0.0, 0.0, 0.0, "Invalid Input", "")
        }

        val bodyFat: Double = if (isMale) {
            val diff = (waistCm - neckCm).coerceAtLeast(1.0)
            495.0 / (1.0324 - 0.19077 * log10(diff) + 0.15456 * log10(heightCm)) - 450.0
        } else {
            val total = (waistCm + hipCm - neckCm).coerceAtLeast(1.0)
            495.0 / (1.29579 - 0.35004 * log10(total) + 0.22100 * log10(heightCm)) - 450.0
        }

        val clampedBf = ((bodyFat.coerceIn(3.0, 60.0)) * 10).roundToInt() / 10.0
        val fatMass = (weightKg * (clampedBf / 100.0) * 10).roundToInt() / 10.0
        val leanMass = ((weightKg - fatMass) * 10).roundToInt() / 10.0

        val category = if (isMale) {
            when {
                clampedBf < 6.0 -> "Essential Fat"
                clampedBf < 14.0 -> "Athletes"
                clampedBf < 18.0 -> "Fitness"
                clampedBf < 25.0 -> "Average"
                else -> "Above Average"
            }
        } else {
            when {
                clampedBf < 14.0 -> "Essential Fat"
                clampedBf < 21.0 -> "Athletes"
                clampedBf < 25.0 -> "Fitness"
                clampedBf < 32.0 -> "Average"
                else -> "Above Average"
            }
        }

        val idealRange = if (isMale) "10% - 17%" else "18% - 24%"
        return BodyFatResult(clampedBf, fatMass, leanMass, category, idealRange)
    }

    data class OneRepMaxResult(
        val estimatedOneRepMaxKg: Double,
        val ninetyPercentKg: Double,
        val eightyPercentKg: Double,
        val seventyPercentKg: Double,
        val sixtyPercentKg: Double,
        val repBreakdown: List<Pair<Int, Double>>,
        val strengthLevel: String
    )

    fun calculateOneRepMax(lift: String, weightLiftedKg: Double, reps: Int): OneRepMaxResult {
        if (weightLiftedKg <= 0 || reps <= 0) {
            return OneRepMaxResult(0.0, 0.0, 0.0, 0.0, 0.0, emptyList(), "Novice")
        }

        // Brzycki & Epley averaged
        val epley = weightLiftedKg * (1.0 + reps / 30.0)
        val brzycki = if (reps < 37) weightLiftedKg * (36.0 / (37.0 - reps)) else epley
        val oneRepMax = ((epley + brzycki) / 2.0 * 10).roundToInt() / 10.0

        val breakdown = (1..10).map { r ->
            val w = (oneRepMax * (1.0 - (r - 1) * 0.025) * 10).roundToInt() / 10.0
            Pair(r, w)
        }

        val strengthLevel = when {
            oneRepMax > 140 -> "Elite"
            oneRepMax > 100 -> "Advanced"
            oneRepMax > 70 -> "Intermediate"
            else -> "Novice"
        }

        return OneRepMaxResult(
            estimatedOneRepMaxKg = oneRepMax,
            ninetyPercentKg = (oneRepMax * 0.9 * 10).roundToInt() / 10.0,
            eightyPercentKg = (oneRepMax * 0.8 * 10).roundToInt() / 10.0,
            seventyPercentKg = (oneRepMax * 0.7 * 10).roundToInt() / 10.0,
            sixtyPercentKg = (oneRepMax * 0.6 * 10).roundToInt() / 10.0,
            repBreakdown = breakdown,
            strengthLevel = strengthLevel
        )
    }

    data class HeartRateResult(
        val maxHeartRate: Int,
        val zone1Warmup: String,
        val zone2FatBurn: String,
        val zone3Aerobic: String,
        val zone4Anaerobic: String,
        val zone5MaxEffort: String
    )

    fun calculateHeartRateZones(age: Int, restingHr: Int = 60): HeartRateResult {
        val maxHr = (208 - (0.7 * age)).roundToInt()
        val hrr = maxHr - restingHr

        fun zone(lowerPct: Double, upperPct: Double): String {
            val min = (restingHr + (hrr * lowerPct)).roundToInt()
            val max = (restingHr + (hrr * upperPct)).roundToInt()
            return "$min - $max bpm"
        }

        return HeartRateResult(
            maxHeartRate = maxHr,
            zone1Warmup = zone(0.50, 0.60),
            zone2FatBurn = zone(0.60, 0.70),
            zone3Aerobic = zone(0.70, 0.80),
            zone4Anaerobic = zone(0.80, 0.90),
            zone5MaxEffort = zone(0.90, 1.00)
        )
    }

    data class DateDifferenceResult(
        val years: Int,
        val months: Int,
        val days: Int,
        val totalDays: Long,
        val totalWeeks: Long,
        val workoutDays: Long,
        val restDays: Long,
        val daysToNextAnniversary: Int,
        val nextMilestoneMonths: Int,
        val nextMilestoneDays: Int
    )

    fun calculateDateDifference(
        startDate: Date,
        endDate: Date,
        workoutDaysPerWeek: Int = 4
    ): DateDifferenceResult {
        val startCal = Calendar.getInstance().apply { time = startDate }
        val endCal = Calendar.getInstance().apply { time = endDate }

        // Swap if end is before start
        val (first, second) = if (startCal.before(endCal)) {
            startCal to endCal
        } else {
            endCal to startCal
        }

        val diffMillis = second.timeInMillis - first.timeInMillis
        val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val totalWeeks = totalDays / 7

        var years = second.get(Calendar.YEAR) - first.get(Calendar.YEAR)
        var months = second.get(Calendar.MONTH) - first.get(Calendar.MONTH)
        var days = second.get(Calendar.DAY_OF_MONTH) - first.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months -= 1
            val prevMonthCal = (first.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.DAY_OF_MONTH, -1)
            }
            days += prevMonthCal.get(Calendar.DAY_OF_MONTH)
        }

        if (months < 0) {
            years -= 1
            months += 12
        }

        val workoutDays = (totalDays * (workoutDaysPerWeek.toDouble() / 7.0)).roundToInt().toLong()
        val restDays = totalDays - workoutDays

        // Next milestone/anniversary
        val nextMilestoneDays = (30 - (totalDays % 30)).toInt()
        val nextMilestoneMonths = (12 - months % 12).coerceAtLeast(1)

        return DateDifferenceResult(
            years = years.coerceAtLeast(0),
            months = months.coerceAtLeast(0),
            days = days.coerceAtLeast(0),
            totalDays = totalDays,
            totalWeeks = totalWeeks,
            workoutDays = workoutDays,
            restDays = restDays,
            daysToNextAnniversary = nextMilestoneDays,
            nextMilestoneMonths = nextMilestoneMonths,
            nextMilestoneDays = nextMilestoneDays
        )
    }
}
