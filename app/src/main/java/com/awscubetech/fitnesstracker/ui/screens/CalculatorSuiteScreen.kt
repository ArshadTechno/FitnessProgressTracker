package com.awscubetech.fitnesstracker.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awscubetech.fitnesstracker.ui.FitnessViewModel
import com.awscubetech.fitnesstracker.ui.theme.EmeraldDark
import com.awscubetech.fitnesstracker.ui.theme.EmeraldPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalculatorSuiteScreen(
    viewModel: FitnessViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf(
        "Active Days / Goal",
        "BMI & Health",
        "TDEE & Calories",
        "Navy Body Fat",
        "1-Rep Max",
        "Heart Zones"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = EmeraldPrimary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> GoalAndDaysCalculatorView(viewModel)
            1 -> BmiCalculatorView(viewModel)
            2 -> TdeeCalculatorView(viewModel)
            3 -> NavyBodyFatCalculatorView(viewModel)
            4 -> OneRepMaxCalculatorView(viewModel)
            5 -> HeartRateZonesCalculatorView(viewModel)
        }
    }
}

// -----------------------------------------------------------------------------------------
// 1. Goal & Active Days Between Dates (Matching Screenshot 3!)
// -----------------------------------------------------------------------------------------
@Composable
private fun GoalAndDaysCalculatorView(viewModel: FitnessViewModel) {
    val context = LocalContext.current
    val startDate by viewModel.targetGoalStartDate.collectAsState()
    val endDate by viewModel.targetGoalEndDate.collectAsState()
    val result by viewModel.goalDateResult.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd - MM - yyyy", Locale.getDefault()) }

    fun showDatePicker(initialDate: Date, onDateSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance().apply { time = initialDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(newCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Today's Date / Start Date
        item {
            Column {
                Text(
                    text = "Today's Date / Baseline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker(startDate) {
                                viewModel.targetGoalStartDate.value = it
                            }
                        }
                        .testTag("start_date_card"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dateFormat.format(startDate),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Target Date / Goal Deadline
        item {
            Column {
                Text(
                    text = "Goal Target Date / Transformation Deadline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker(endDate) {
                                viewModel.targetGoalEndDate.value = it
                            }
                        }
                        .testTag("end_date_card"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dateFormat.format(endDate),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Action Buttons: Clear (Dark) & Calculate (Emerald) (Matching Screenshot 3!)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.targetGoalStartDate.value = Date()
                        viewModel.targetGoalEndDate.value = Date()
                        viewModel.calculateGoalDates()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("clear_date_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)) // Dark slate
                ) {
                    Text("Clear", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Button(
                    onClick = { viewModel.calculateGoalDates() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("calculate_date_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Calculate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        // Prominent Result Card (Matching Screenshot 3 with big numbers in green box)
        if (result != null) {
            val r = result!!
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_result_card"),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Timeline & Age Output",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3 Big Green Metric Boxes (Years, Months, Days) (Matching Screenshot 3)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BigMetricBox(
                                value = "${r.years}",
                                label = "Years",
                                modifier = Modifier.weight(1f)
                            )
                            BigMetricBox(
                                value = "${r.months}",
                                label = "Months",
                                modifier = Modifier.weight(1f)
                            )
                            BigMetricBox(
                                value = "${r.days}",
                                label = "Days",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Extras Section (Matching Screenshot 3)
                        Text(
                            text = "Extras & Workout Planning",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Days Duration:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${r.totalDays} Days (${r.totalWeeks} Weeks)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Est. Active Workout Days (4x/wk):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${r.workoutDays} Workouts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dedicated Rest & Recovery Days:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${r.restDays} Days", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Next Month Milestone:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${r.nextMilestoneMonths} Month ${r.nextMilestoneDays} Days", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = EmeraldPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BigMetricBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// 2. BMI & Healthy Weight Calculator
// -----------------------------------------------------------------------------------------
@Composable
private fun BmiCalculatorView(viewModel: FitnessViewModel) {
    val height by viewModel.bmiHeightInput.collectAsState()
    val weight by viewModel.bmiWeightInput.collectAsState()
    val result by viewModel.bmiResult.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedTextField(
                value = height,
                onValueChange = { viewModel.bmiHeightInput.value = it },
                label = { Text("Height (cm)") },
                placeholder = { Text("e.g. 178") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bmi_height_input"),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) }
            )
        }

        item {
            OutlinedTextField(
                value = weight,
                onValueChange = { viewModel.bmiWeightInput.value = it },
                label = { Text("Weight (kg)") },
                placeholder = { Text("e.g. 75") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bmi_weight_input"),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = { Icon(Icons.Default.Scale, contentDescription = null) }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.clearBmi() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.calculateBmi() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("calculate_bmi_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Calculate", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (result != null) {
            val r = result!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("BMI Score Result", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${r.bmi}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 36.sp,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "Category: ${r.category}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Healthy Weight Range", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${r.healthyMinWeight} - ${r.healthyMaxWeight} kg", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = r.advice,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 3. TDEE & Daily Calorie Calculator
// -----------------------------------------------------------------------------------------
@Composable
private fun TdeeCalculatorView(viewModel: FitnessViewModel) {
    val gender by viewModel.tdeeGender.collectAsState()
    val age by viewModel.tdeeAge.collectAsState()
    val height by viewModel.tdeeHeight.collectAsState()
    val weight by viewModel.tdeeWeight.collectAsState()
    val result by viewModel.tdeeResult.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.tdeeGender.value = "Male" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (gender == "Male") EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent
                    ),
                    border = BorderStroke(1.2.dp, if (gender == "Male") EmeraldPrimary else MaterialTheme.colorScheme.outline)
                ) {
                    Text("Male", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.tdeeGender.value = "Female" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (gender == "Female") EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent
                    ),
                    border = BorderStroke(1.2.dp, if (gender == "Female") EmeraldPrimary else MaterialTheme.colorScheme.outline)
                ) {
                    Text("Female", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { viewModel.tdeeAge.value = it },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { viewModel.tdeeHeight.value = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { viewModel.tdeeWeight.value = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.clearTdee() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.calculateTdee() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Calculate", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (result != null) {
            val r = result!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Daily Energy Expenditure (TDEE)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BigMetricBox(value = "${r.tdeeMaintenance}", label = "Maintenance kcal", modifier = Modifier.weight(1f))
                            BigMetricBox(value = "${r.cuttingCalories}", label = "Fat Loss kcal", modifier = Modifier.weight(1f))
                            BigMetricBox(value = "${r.cleanBulkingCalories}", label = "Muscle Gain kcal", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Recommended Daily Macronutrient Split", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Protein: ${r.proteinGrams}g", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            Text("Carbohydrates: ${r.carbsGrams}g", fontWeight = FontWeight.Medium)
                            Text("Fats: ${r.fatsGrams}g", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 4. Navy Body Fat Calculator
// -----------------------------------------------------------------------------------------
@Composable
private fun NavyBodyFatCalculatorView(viewModel: FitnessViewModel) {
    val isMale by viewModel.bfIsMale.collectAsState()
    val height by viewModel.bfHeight.collectAsState()
    val neck by viewModel.bfNeck.collectAsState()
    val waist by viewModel.bfWaist.collectAsState()
    val hip by viewModel.bfHip.collectAsState()
    val weight by viewModel.bfWeight.collectAsState()
    val result by viewModel.bfResult.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.bfIsMale.value = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isMale) EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent
                    ),
                    border = BorderStroke(1.2.dp, if (isMale) EmeraldPrimary else MaterialTheme.colorScheme.outline)
                ) {
                    Text("Male", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.bfIsMale.value = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (!isMale) EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent
                    ),
                    border = BorderStroke(1.2.dp, if (!isMale) EmeraldPrimary else MaterialTheme.colorScheme.outline)
                ) {
                    Text("Female", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { viewModel.bfHeight.value = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { viewModel.bfWeight.value = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = neck,
                    onValueChange = { viewModel.bfNeck.value = it },
                    label = { Text("Neck (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = waist,
                    onValueChange = { viewModel.bfWaist.value = it },
                    label = { Text("Waist (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                if (!isMale) {
                    OutlinedTextField(
                        value = hip,
                        onValueChange = { viewModel.bfHip.value = it },
                        label = { Text("Hip (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.clearNavyBodyFat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.calculateNavyBodyFat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Calculate", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (result != null) {
            val r = result!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Body Fat % Estimation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BigMetricBox(value = "${r.bodyFatPercent}%", label = "Body Fat", modifier = Modifier.weight(1f))
                            BigMetricBox(value = "${r.leanMassKg} kg", label = "Lean Muscle", modifier = Modifier.weight(1f))
                            BigMetricBox(value = "${r.fatMassKg} kg", label = "Fat Mass", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Classification: ${r.category} (Ideal: ${r.idealBodyFatRange})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 5. 1-Rep Max & Strength Calculator
// -----------------------------------------------------------------------------------------
@Composable
private fun OneRepMaxCalculatorView(viewModel: FitnessViewModel) {
    val exercise by viewModel.rmExercise.collectAsState()
    val weight by viewModel.rmWeight.collectAsState()
    val reps by viewModel.rmReps.collectAsState()
    val result by viewModel.rmResult.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedTextField(
                value = exercise,
                onValueChange = { viewModel.rmExercise.value = it },
                label = { Text("Exercise Lift (e.g. Bench, Squat, Deadlift)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { viewModel.rmWeight.value = it },
                    label = { Text("Weight Lifted (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { viewModel.rmReps.value = it },
                    label = { Text("Reps Completed") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.clearOneRepMax() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.calculateOneRepMax() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Calculate", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (result != null) {
            val r = result!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Estimated 1-Rep Max (1RM)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${r.estimatedOneRepMaxKg} kg",
                                fontWeight = FontWeight.Black,
                                fontSize = 36.sp,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "Tier: ${r.strengthLevel}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Percentage Training Zones:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("90%: ${r.ninetyPercentKg}kg", fontSize = 12.sp)
                            Text("80%: ${r.eightyPercentKg}kg", fontSize = 12.sp)
                            Text("70%: ${r.seventyPercentKg}kg", fontSize = 12.sp)
                            Text("60%: ${r.sixtyPercentKg}kg", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 6. Target Heart Rate (Karvonen) Zones
// -----------------------------------------------------------------------------------------
@Composable
private fun HeartRateZonesCalculatorView(viewModel: FitnessViewModel) {
    val age by viewModel.hrAge.collectAsState()
    val rest by viewModel.hrResting.collectAsState()
    val result by viewModel.hrResult.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { viewModel.hrAge.value = it },
                    label = { Text("Age (years)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = rest,
                    onValueChange = { viewModel.hrResting.value = it },
                    label = { Text("Resting HR (bpm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        item {
            Button(
                onClick = { viewModel.calculateHeartRate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Calculate Training Zones", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (result != null) {
            val r = result!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Cardiovascular Training Zones (Max: ${r.maxHeartRate} bpm)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        HeartZoneRow("Zone 1 (Warm Up 50-60%)", r.zone1Warmup, Color(0xFF10B981))
                        HeartZoneRow("Zone 2 (Fat Burn & Base 60-70%)", r.zone2FatBurn, Color(0xFF06B6D4))
                        HeartZoneRow("Zone 3 (Aerobic Cardio 70-80%)", r.zone3Aerobic, Color(0xFF3B82F6))
                        HeartZoneRow("Zone 4 (Anaerobic / Threshold 80-90%)", r.zone4Anaerobic, Color(0xFFF59E0B))
                        HeartZoneRow("Zone 5 (VO2 Max 90-100%)", r.zone5MaxEffort, Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeartZoneRow(label: String, value: String, dotColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
    }
}
