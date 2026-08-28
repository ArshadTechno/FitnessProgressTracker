package com.awscubetech.fitnesstracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awscubetech.fitnesstracker.data.local.AthleteGoalEntity
import com.awscubetech.fitnesstracker.data.models.AthleteBenchmark
import com.awscubetech.fitnesstracker.data.models.FitnessStaticData
import com.awscubetech.fitnesstracker.ui.FitnessViewModel
import com.awscubetech.fitnesstracker.ui.theme.EmeraldPrimary
import com.awscubetech.fitnesstracker.ui.theme.GoldAccent

@Composable
fun AthleteBenchmarksScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val athletes = FitnessStaticData.athleteBenchmarks
    val currentGoal by viewModel.primaryAthleteGoal.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    var showEditPrDialog by remember { mutableStateOf(false) }

    val userBench = currentGoal?.userCurrentBenchKg ?: 85.0
    val userSquat = currentGoal?.userCurrentSquatKg ?: 110.0
    val userDeadlift = currentGoal?.userCurrentDeadliftKg ?: 135.0
    val activeGoalId = currentGoal?.benchmarkId ?: "ath_1"

    val filters = listOf("All", "Bodybuilding", "Powerlifting", "CrossFit", "Athletic")

    val filteredAthletes = athletes.filter { athlete ->
        when (selectedFilter) {
            "All" -> true
            "Bodybuilding" -> athlete.sport.contains("Bodybuilding", ignoreCase = true) || athlete.physiqueType.contains("Hypertrophy", ignoreCase = true)
            "Powerlifting" -> athlete.sport.contains("Powerlifting", ignoreCase = true) || athlete.physiqueType.contains("Strength", ignoreCase = true)
            "CrossFit" -> athlete.sport.contains("CrossFit", ignoreCase = true) || athlete.physiqueType.contains("Hybrid", ignoreCase = true)
            "Athletic" -> athlete.sport.contains("Olympic", ignoreCase = true) || athlete.physiqueType.contains("Athletic", ignoreCase = true)
            else -> true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. User's Real Strength Baseline Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_strength_baseline_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, GoldAccent.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "My Personal Best PRs (1RM)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Active Target: ${currentGoal?.benchmarkName ?: "Classic Standard"}",
                                        fontSize = 12.sp,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showEditPrDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit PRs", modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StrengthStatBox("Bench Press", "$userBench kg", Modifier.weight(1f))
                            StrengthStatBox("Barbell Squat", "$userSquat kg", Modifier.weight(1f))
                            StrengthStatBox("Deadlift", "$userDeadlift kg", Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val totalLifted = userBench + userSquat + userDeadlift
                        Text(
                            text = "3-Lift Total: ${totalLifted.toInt()} kg • Strength Ratio: ${(totalLifted / 75.0).formatOneDecimal()}x Bodyweight",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = Color(0xFF1E1E1E)
                            )
                        )
                    }
                }
            }

            // 3. Athletes Comparison Cards
            items(filteredAthletes, key = { it.id }) { athlete ->
                DynamicAthleteCard(
                    athlete = athlete,
                    userBench = userBench,
                    userSquat = userSquat,
                    userDeadlift = userDeadlift,
                    isActiveGoal = activeGoalId == athlete.id,
                    onSetAsGoal = {
                        viewModel.updateAthleteGoal(
                            benchmarkId = athlete.id,
                            benchmarkName = athlete.name,
                            targetBenchKg = athlete.benchPressKg,
                            targetSquatKg = athlete.squatKg,
                            targetDeadliftKg = athlete.deadliftKg,
                            userCurrentBenchKg = userBench,
                            userCurrentSquatKg = userSquat,
                            userCurrentDeadliftKg = userDeadlift
                        )
                    }
                )
            }
        }
    }

    if (showEditPrDialog) {
        EditPrsDialog(
            currentBench = userBench,
            currentSquat = userSquat,
            currentDeadlift = userDeadlift,
            onDismiss = { showEditPrDialog = false },
            onSave = { bench, squat, deadlift ->
                viewModel.updateUserCurrentPrs(bench, squat, deadlift)
                showEditPrDialog = false
            }
        )
    }
}

@Composable
private fun DynamicAthleteCard(
    athlete: AthleteBenchmark,
    userBench: Double,
    userSquat: Double,
    userDeadlift: Double,
    isActiveGoal: Boolean,
    onSetAsGoal: () -> Unit
) {
    val benchRatio = (userBench / athlete.benchPressKg).coerceIn(0.0, 1.5)
    val squatRatio = (userSquat / athlete.squatKg).coerceIn(0.0, 1.5)
    val deadliftRatio = (userDeadlift / athlete.deadliftKg).coerceIn(0.0, 1.5)
    val overallRatio = ((benchRatio + squatRatio + deadliftRatio) / 3.0).coerceIn(0.0, 1.5)
    val overallPercent = (overallRatio * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("athlete_card_${athlete.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.2.dp,
            if (isActiveGoal) GoldAccent else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = athlete.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isActiveGoal) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GoldAccent)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVE GOAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                            }
                        }
                    }
                    Text(
                        text = "${athlete.sport} • ${athlete.physiqueType}",
                        fontSize = 12.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (overallPercent >= 100) EmeraldPrimary.copy(alpha = 0.15f) else GoldAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$overallPercent% Matched",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (overallPercent >= 100) EmeraldPrimary else Color(0xFFB45309)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Overall Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (overallRatio.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (overallPercent >= 100) EmeraldPrimary else GoldAccent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real Comparison Matrix
            Text(
                text = "Strength Standard Benchmarks vs. Your Real Lifts",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComparisonStatBox(
                    label = "Bench",
                    userVal = userBench,
                    targetVal = athlete.benchPressKg,
                    modifier = Modifier.weight(1f)
                )
                ComparisonStatBox(
                    label = "Squat",
                    userVal = userSquat,
                    targetVal = athlete.squatKg,
                    modifier = Modifier.weight(1f)
                )
                ComparisonStatBox(
                    label = "Deadlift",
                    userVal = userDeadlift,
                    targetVal = athlete.deadliftKg,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nutrition & Body Fat Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Body Fat Target: ${athlete.bodyFatRange}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("${athlete.dailyCalories} kcal • ${athlete.proteinGrams}g Protein", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Split
            Text("Pro Workout Split:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            athlete.weeklySplit.take(3).forEach { day ->
                Text("• $day", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"${athlete.signatureQuote}\"",
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isActiveGoal) {
                OutlinedButton(
                    onClick = onSetAsGoal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GoldAccent)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set as My Primary Athletic Goal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(disabledContainerColor = EmeraldPrimary.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Currently Tracking This Goal Target", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }
        }
    }
}

@Composable
private fun ComparisonStatBox(
    label: String,
    userVal: Double,
    targetVal: Double,
    modifier: Modifier = Modifier
) {
    val diff = targetVal - userVal
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text("${targetVal.toInt()} kg", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            val diffText = if (diff <= 0) "Achieved! 🏆" else "-${diff.toInt()} kg"
            val diffColor = if (diff <= 0) EmeraldPrimary else Color(0xFFEF4444)
            Text(diffText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = diffColor)
        }
    }
}

@Composable
private fun StrengthStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldAccent)
        }
    }
}

@Composable
private fun EditPrsDialog(
    currentBench: Double,
    currentSquat: Double,
    currentDeadlift: Double,
    onDismiss: () -> Unit,
    onSave: (bench: Double, squat: Double, deadlift: Double) -> Unit
) {
    var benchText by remember { mutableStateOf(currentBench.toString()) }
    var squatText by remember { mutableStateOf(currentSquat.toString()) }
    var deadliftText by remember { mutableStateOf(currentDeadlift.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Your 1RM PR Lifts", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enter your current maximum single-rep (1RM) lifts in kilograms:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = benchText,
                    onValueChange = { benchText = it },
                    label = { Text("Bench Press (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = squatText,
                    onValueChange = { squatText = it },
                    label = { Text("Barbell Squat (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deadliftText,
                    onValueChange = { deadliftText = it },
                    label = { Text("Barbell Deadlift (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bench = benchText.toDoubleOrNull() ?: currentBench
                    val squat = squatText.toDoubleOrNull() ?: currentSquat
                    val deadlift = deadliftText.toDoubleOrNull() ?: currentDeadlift
                    onSave(bench, squat, deadlift)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Save PRs")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun Double.formatOneDecimal(): String {
    return String.format(java.util.Locale.US, "%.1f", this)
}
