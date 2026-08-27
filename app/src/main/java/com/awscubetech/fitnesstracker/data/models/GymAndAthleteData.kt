package com.awscubetech.fitnesstracker.data.models

data class FitnessCenter(
    val id: String,
    val name: String,
    val category: String,
    val rating: Double,
    val reviewCount: Int,
    val distanceKm: Double,
    val address: String,
    val openingHours: String,
    val isOpenNow: Boolean,
    val facilities: List<String>,
    val phoneNumber: String
)

data class AthleteBenchmark(
    val id: String,
    val name: String,
    val sport: String,
    val physiqueType: String,
    val benchPressKg: Double,
    val squatKg: Double,
    val deadliftKg: Double,
    val bodyFatRange: String,
    val dailyCalories: Int,
    val proteinGrams: Int,
    val weeklySplit: List<String>,
    val signatureQuote: String
)

object FitnessStaticData {
    val fitnessCenters = listOf(
        FitnessCenter(
            id = "gym_1",
            name = "Iron Peak Strength & Conditioning",
            category = "Powerlifting & Olympic Gym",
            rating = 4.9,
            reviewCount = 384,
            distanceKm = 0.6,
            address = "742 Broadway Ave, Metro District",
            openingHours = "24 / 7 Access",
            isOpenNow = true,
            facilities = listOf("Eleiko Plates", "Squat Racks", "Chalk Allowed", "Deadlift Platforms", "Sauna"),
            phoneNumber = "+1 (555) 234-8901"
        ),
        FitnessCenter(
            id = "gym_2",
            name = "Apex Performance Athletic Club",
            category = "Full Fitness & Health Club",
            rating = 4.8,
            reviewCount = 612,
            distanceKm = 1.2,
            address = "1200 Highland Park Blvd",
            openingHours = "5:00 AM - 11:00 PM",
            isOpenNow = true,
            facilities = listOf("Olympic Pool", "Recovery Ice Baths", "Cardio Deck", "Dumbbells to 150lbs", "Smoothie Bar"),
            phoneNumber = "+1 (555) 345-6789"
        ),
        FitnessCenter(
            id = "gym_3",
            name = "Velocity Functional Cross Training",
            category = "CrossFit & Functional Box",
            rating = 4.9,
            reviewCount = 210,
            distanceKm = 1.8,
            address = "450 Industrial Parkway",
            openingHours = "6:00 AM - 9:00 PM",
            isOpenNow = true,
            facilities = listOf("Rowers & SkiErgs", "Rope Climbs", "Kettlebells", "Mobility Corner", "Certified Coaches"),
            phoneNumber = "+1 (555) 890-1234"
        ),
        FitnessCenter(
            id = "gym_4",
            name = "Zenith Calisthenics & Movement Studio",
            category = "Bodyweight & Gymnastics",
            rating = 4.7,
            reviewCount = 145,
            distanceKm = 2.4,
            address = "88 West End Boulevard",
            openingHours = "7:00 AM - 10:00 PM",
            isOpenNow = true,
            facilities = listOf("Parallel Bars", "Gymnastic Rings", "Pegboards", "Yoga & Flexibility"),
            phoneNumber = "+1 (555) 678-9012"
        ),
        FitnessCenter(
            id = "gym_5",
            name = "Titan 24/7 Express Gym",
            category = "24/7 Fitness Center",
            rating = 4.6,
            reviewCount = 490,
            distanceKm = 3.1,
            address = "310 Commerce Street",
            openingHours = "24 Hours Daily",
            isOpenNow = true,
            facilities = listOf("Keycard Access", "Matrix Machines", "Free Weights", "Shower Suites"),
            phoneNumber = "+1 (555) 432-1098"
        )
    )

    val athleteBenchmarks = listOf(
        AthleteBenchmark(
            id = "ath_1",
            name = "Classic Bodybuilding Standard",
            sport = "Aesthetics & Hypertrophy",
            physiqueType = "V-Taper & High Muscle Mass",
            benchPressKg = 140.0,
            squatKg = 180.0,
            deadliftKg = 220.0,
            bodyFatRange = "8% - 12%",
            dailyCalories = 3200,
            proteinGrams = 210,
            weeklySplit = listOf(
                "Monday: Chest & Triceps (Hypertrophy)",
                "Tuesday: Back & Biceps (Heavy Lat Pulls)",
                "Wednesday: Active Mobility & Core",
                "Thursday: Shoulders & Traps",
                "Friday: Legs & Calves (Quad Focus)",
                "Saturday: Arms & Weak Point Focus",
                "Sunday: Rest & High Protein Meal Prep"
            ),
            signatureQuote = "Consistency and progressive overload beat intensity without discipline."
        ),
        AthleteBenchmark(
            id = "ath_2",
            name = "Championship CrossFit Competitor",
            sport = "Work Capacity & Conditioning",
            physiqueType = "Functional Athletic & Dense",
            benchPressKg = 130.0,
            squatKg = 200.0,
            deadliftKg = 235.0,
            bodyFatRange = "10% - 14%",
            dailyCalories = 3800,
            proteinGrams = 220,
            weeklySplit = listOf(
                "Monday: Clean & Jerk + EMOM Conditioning",
                "Tuesday: Heavy Squats + Gymnastic Handstands",
                "Wednesday: Zone 2 Aerobic Rowing 10km",
                "Thursday: Snatch Technique + High Rep Burpees",
                "Friday: Deadlift Strength + 500m Sprint Intervals",
                "Saturday: Team Hero WOD",
                "Sunday: Contrast Therapy & Sauna"
            ),
            signatureQuote = "Train your engine to run when everyone else is out of gas."
        ),
        AthleteBenchmark(
            id = "ath_3",
            name = "Elite Hybrid Athlete",
            sport = "Endurance + Heavy Lifting",
            physiqueType = "Lean, Resilient & Stamina Optimized",
            benchPressKg = 120.0,
            squatKg = 160.0,
            deadliftKg = 200.0,
            bodyFatRange = "11% - 15%",
            dailyCalories = 3500,
            proteinGrams = 190,
            weeklySplit = listOf(
                "Monday: 10km Tempo Run + Upper Body Push",
                "Tuesday: Heavy Barbell Squats + 5km Easy Recovery",
                "Wednesday: 45-min Zone 2 Cycling + Core Vacuums",
                "Thursday: Deadlifts + Weighted Pull-Ups",
                "Friday: 800m Track Repeats + Shoulder Mobility",
                "Saturday: 21km Long Trail Run",
                "Sunday: Deep Stretching & Nutrition Reload"
            ),
            signatureQuote = "Lift heavy weights, run long distances, and build an unbreakable mindset."
        ),
        AthleteBenchmark(
            id = "ath_4",
            name = "Olympic Track & Field Sprinter",
            sport = "Explosive Power & Speed",
            physiqueType = "Low Body Fat & Explosive Posterior Chain",
            benchPressKg = 110.0,
            squatKg = 190.0,
            deadliftKg = 210.0,
            bodyFatRange = "7% - 10%",
            dailyCalories = 3100,
            proteinGrams = 180,
            weeklySplit = listOf(
                "Monday: Acceleration Block Starts + Power Cleans",
                "Tuesday: Plyometrics + Depth Jumps + Core",
                "Wednesday: Maximum Velocity 60m Flys",
                "Thursday: Trap Bar Deadlifts + Hip Thrusts",
                "Friday: Speed Endurance (150m repeats)",
                "Saturday: Low Impact Pool Drills",
                "Sunday: Central Nervous System Recovery"
            ),
            signatureQuote = "Power is nothing without precision and relaxation under speed."
        )
    )
}
