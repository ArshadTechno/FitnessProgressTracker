package com.awscubetech.fitnesstracker.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awscubetech.fitnesstracker.data.gemini.AnalysisType
import com.awscubetech.fitnesstracker.data.gemini.FitnessAnalysisResult
import com.awscubetech.fitnesstracker.data.local.FitnessScanEntity
import com.awscubetech.fitnesstracker.ui.AiAnalysisUiState
import com.awscubetech.fitnesstracker.ui.FitnessViewModel
import com.awscubetech.fitnesstracker.ui.components.shareAppIntent
import com.awscubetech.fitnesstracker.ui.theme.EmeraldContainerLight
import com.awscubetech.fitnesstracker.ui.theme.EmeraldDark
import com.awscubetech.fitnesstracker.ui.theme.EmeraldPrimary
import com.awscubetech.fitnesstracker.ui.theme.GoldAccent

@Composable
fun CameraAnalysisScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Analyze, 1: History
    val analysisState by viewModel.analysisState.collectAsState()
    val selectedBitmap by viewModel.selectedAnalysisBitmap.collectAsState()
    val currentType by viewModel.currentAnalysisType.collectAsState()
    val historyScans by viewModel.scans.collectAsState()

    var userNotes by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.setSelectedBitmap(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.loadBitmapFromUri(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = EmeraldPrimary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live AI Scan", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saved Scans (${historyScans.size})", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // Live AI Scan Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Analysis Mode Switcher
                item {
                    Text(
                        text = "Select Analysis Target",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalysisModeChip(
                            label = "Physique & Posture",
                            isSelected = currentType == AnalysisType.PHYSIQUE_PROGRESS,
                            icon = Icons.Default.Person,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setAnalysisType(AnalysisType.PHYSIQUE_PROGRESS) }
                        )
                        AnalysisModeChip(
                            label = "Workout Form",
                            isSelected = currentType == AnalysisType.EXERCISE_FORM,
                            icon = Icons.Default.FitnessCenter,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setAnalysisType(AnalysisType.EXERCISE_FORM) }
                        )
                        AnalysisModeChip(
                            label = "Meal & Nutrition",
                            isSelected = currentType == AnalysisType.MEAL_NUTRITION,
                            icon = Icons.Default.Restaurant,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setAnalysisType(AnalysisType.MEAL_NUTRITION) }
                        )
                    }
                }

                // Dynamic Mode Header & Guidance Banner
                item {
                    val modeColor = when (currentType) {
                        AnalysisType.PHYSIQUE_PROGRESS -> Color(0xFF00A86B)
                        AnalysisType.EXERCISE_FORM -> Color(0xFF8B5CF6)
                        AnalysisType.MEAL_NUTRITION -> Color(0xFFF97316)
                    }
                    val modeTitle = when (currentType) {
                        AnalysisType.PHYSIQUE_PROGRESS -> "AI Body & Physique Progress"
                        AnalysisType.EXERCISE_FORM -> "AI Workout Form & Pose Checker"
                        AnalysisType.MEAL_NUTRITION -> "AI Meal & Nutrition Scanner"
                    }
                    val modeDesc = when (currentType) {
                        AnalysisType.PHYSIQUE_PROGRESS -> "Evaluates posture symmetry, muscular definition, and visual transformation milestones."
                        AnalysisType.EXERCISE_FORM -> "Evaluates joint biomechanics, back curvature, squat depth, and lifting safety."
                        AnalysisType.MEAL_NUTRITION -> "Estimates calories, protein, carbohydrates, and healthy fats from your food plate."
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = modeColor.copy(alpha = 0.08f)),
                        border = BorderStroke(1.2.dp, modeColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(modeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (currentType) {
                                        AnalysisType.PHYSIQUE_PROGRESS -> Icons.Default.Person
                                        AnalysisType.EXERCISE_FORM -> Icons.Default.FitnessCenter
                                        AnalysisType.MEAL_NUTRITION -> Icons.Default.Restaurant
                                    },
                                    contentDescription = null,
                                    tint = modeColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = modeTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = modeDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Camera Preview / Photo Frame
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .testTag("camera_frame_card"),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (selectedBitmap != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = "Selected Frame",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Geometric Telemetry Overlay
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFB3261E))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI VISION ACTIVE",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.setSelectedBitmap(null) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = Color.White)
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (currentType) {
                                            AnalysisType.PHYSIQUE_PROGRESS -> Icons.Default.CameraAlt
                                            AnalysisType.EXERCISE_FORM -> Icons.Default.FitnessCenter
                                            AnalysisType.MEAL_NUTRITION -> Icons.Default.Restaurant
                                        },
                                        contentDescription = "Camera",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = when (currentType) {
                                        AnalysisType.PHYSIQUE_PROGRESS -> "Capture or upload physique progress photo"
                                        AnalysisType.EXERCISE_FORM -> "Record or upload exercise posture frame"
                                        AnalysisType.MEAL_NUTRITION -> "Take or upload photo of your meal plate"
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = when (currentType) {
                                        AnalysisType.PHYSIQUE_PROGRESS -> "Front, side or back pose in good lighting"
                                        AnalysisType.EXERCISE_FORM -> "Side-angle view of squat, deadlift, bench or press"
                                        AnalysisType.MEAL_NUTRITION -> "Top-down view showing protein, carbs & sides"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = {
                                            try {
                                                cameraLauncher.launch(null)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Opening camera...", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("open_camera_button")
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Camera", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("open_gallery_button")
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Gallery", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Demo Preset Samples for instant preview
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Quick Demo Test Samples:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        when (currentType) {
                            AnalysisType.EXERCISE_FORM -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PresetSampleChip(label = "Squat Pose", modifier = Modifier.weight(1f)) {
                                        viewModel.setSelectedBitmap(createPresetBitmap("SQUAT POSE", 0xFF8B5CF6.toInt()))
                                        userNotes = "Barbell Back Squat - checking depth and spine angle"
                                    }
                                    PresetSampleChip(label = "Deadlift Form", modifier = Modifier.weight(1f)) {
                                        viewModel.setSelectedBitmap(createPresetBitmap("DEADLIFT FORM", 0xFF8B5CF6.toInt()))
                                        userNotes = "Conventional Deadlift - checking lower back curvature"
                                    }
                                    PresetSampleChip(label = "Pushup Angle", modifier = Modifier.weight(1f)) {
                                        viewModel.setSelectedBitmap(createPresetBitmap("PUSHUP ALIGNMENT", 0xFF8B5CF6.toInt()))
                                        userNotes = "Pushup - checking elbow flare and core engagement"
                                    }
                                }
                            }
                            AnalysisType.PHYSIQUE_PROGRESS -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PresetSampleChip(label = "Front Relaxed", modifier = Modifier.weight(1f)) {
                                        viewModel.setSelectedBitmap(createPresetBitmap("FRONT RELAXED", 0xFF00A86B.toInt()))
                                        userNotes = "Front relaxed pose - symmetry & chest/shoulder balance"
                                    }
                                    PresetSampleChip(label = "Side Profile", modifier = Modifier.weight(1f)) {
                                        viewModel.setSelectedBitmap(createPresetBitmap("SIDE PROFILE", 0xFF00A86B.toInt()))
                                        userNotes = "Side profile - posture and abdominal definition"
                                    }
                                    PresetSampleChip(label = "Back Lat Spread", modifier = Modifier.weight(1f)) {
                                        viewModel.setSelectedBitmap(createPresetBitmap("BACK LAT SPREAD", 0xFF00A86B.toInt()))
                                        userNotes = "Back pose - lat width & lower back development"
                                    }
                                }
                            }
                            AnalysisType.MEAL_NUTRITION -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PresetSampleChip(label = "Protein Bowl", modifier = Modifier.weight(1f)) {
                                        viewModel.setSelectedBitmap(createPresetBitmap("PROTEIN BOWL", 0xFFF97316.toInt()))
                                        userNotes = "Post-workout grilled chicken, rice, avocado and greens"
                                    }
                                    PresetSampleChip(label = "Steak & Eggs", modifier = Modifier.weight(1f)) {
                                        viewModel.setSelectedBitmap(createPresetBitmap("STEAK & EGGS", 0xFFF97316.toInt()))
                                        userNotes = "Sirloin steak, 3 eggs, sweet potato"
                                    }
                                    PresetSampleChip(label = "Oatmeal Shake", modifier = Modifier.weight(1f)) {
                                        viewModel.setSelectedBitmap(createPresetBitmap("OATMEAL SHAKE", 0xFFF97316.toInt()))
                                        userNotes = "Whey protein, rolled oats, peanut butter, banana"
                                    }
                                }
                            }
                        }
                    }
                }

                // User Notes
                item {
                    OutlinedTextField(
                        value = userNotes,
                        onValueChange = { userNotes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analysis_notes_input"),
                        label = { Text("Add notes or exercise context (optional)") },
                        placeholder = {
                            Text(
                                when (currentType) {
                                    AnalysisType.EXERCISE_FORM -> "e.g., Back squat 100kg, checking depth & knee tracking..."
                                    AnalysisType.PHYSIQUE_PROGRESS -> "e.g., 6-week cutting phase, 78kg bodyweight..."
                                    AnalysisType.MEAL_NUTRITION -> "e.g., Post-workout meal: grilled salmon, rice, broccoli..."
                                }
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                // Run Analysis Button
                item {
                    Button(
                        onClick = { viewModel.runAiAnalysis(userNotes) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("run_analysis_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        enabled = analysisState !is AiAnalysisUiState.Analyzing
                    ) {
                        if (analysisState is AiAnalysisUiState.Analyzing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Analyzing Frame with Gemini AI...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ANALYZE WITH AI VISION",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Results Card
                if (analysisState is AiAnalysisUiState.Success) {
                    val result = (analysisState as AiAnalysisUiState.Success).result
                    item {
                        AnalysisResultView(
                            result = result,
                            onShare = { shareAppIntent(context) },
                            onNewScan = { viewModel.resetAnalysis() }
                        )
                    }
                } else if (analysisState is AiAnalysisUiState.Error) {
                    val errorMsg = (analysisState as AiAnalysisUiState.Error).message
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFF87171)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = errorMsg, color = Color(0xFF991B1B), fontSize = 13.sp)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            // History Tab
            if (historyScans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = EmeraldPrimary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No saved scans yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Run an AI body or workout analysis to see your logs here.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyScans, key = { it.id }) { scan ->
                        ScanHistoryCard(
                            scan = scan,
                            onDelete = { viewModel.deleteScan(scan) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisModeChip(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.2.dp,
            if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PresetSampleChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(EmeraldPrimary.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnalysisResultView(
    result: FitnessAnalysisResult,
    onShare: () -> Unit,
    onNewScan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analysis_result_card"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "AI Score",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Score Badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${result.score}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary & Secondary Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = result.primaryMetricLabel,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = result.primaryMetricValue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = EmeraldPrimary
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = result.secondaryMetricLabel,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = result.secondaryMetricValue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary
            Text(
                text = result.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Detailed Insights
            Text(
                text = "Key Observations & Biomarkers",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            result.detailedInsights.forEach { insight ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = insight,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recommendations
            Text(
                text = "Coaching Recommendations",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            result.recommendations.forEach { rec ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rec,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Safety Notes Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.safetyOrPostureNotes,
                        fontSize = 12.sp,
                        color = Color(0xFF92400E),
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }

                Button(
                    onClick = onNewScan,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Scan")
                }
            }
        }
    }
}

@Composable
private fun ScanHistoryCard(
    scan: FitnessScanEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${scan.score}",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = scan.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${scan.dateFormatted} • ${scan.scanType.replace("_", " ")}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scan.summary,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${scan.primaryMetricLabel}: ${scan.primaryMetricValue}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldPrimary
                )
                Text(
                    text = "${scan.secondaryMetricLabel}: ${scan.secondaryMetricValue}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun createPresetBitmap(label: String, colorInt: Int): Bitmap {
    val width = 450
    val height = 450
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    paint.color = colorInt
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    paint.color = android.graphics.Color.WHITE
    paint.strokeWidth = 5f
    paint.style = Paint.Style.STROKE
    canvas.drawRoundRect(20f, 20f, 430f, 430f, 30f, 30f, paint)

    paint.style = Paint.Style.FILL
    paint.textSize = 28f
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText(label, 225f, 235f, paint)
    return bitmap
}
