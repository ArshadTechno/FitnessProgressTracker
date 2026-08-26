package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FitnessGoalPreset(
    val title: String,
    val category: String,
    val iconKey: String,
    val colorHex: Long,
    val subtitle: String
)

val PRESET_FITNESS_GOALS = listOf(
    FitnessGoalPreset(
        title = "3.5L Daily Hydration",
        category = "Hydration",
        iconKey = "water",
        colorHex = 0xFF0284C7,
        subtitle = "Optimal muscle cell hydration"
    ),
    FitnessGoalPreset(
        title = "150g Protein Target",
        category = "Nutrition",
        iconKey = "protein",
        colorHex = 0xFF16A34A,
        subtitle = "Lean hypertrophy & recovery"
    ),
    FitnessGoalPreset(
        title = "10,000 Daily Steps",
        category = "Steps",
        iconKey = "steps",
        colorHex = 0xFF6750A4,
        subtitle = "Active NEAT & fat burn"
    ),
    FitnessGoalPreset(
        title = "45-Min Heavy Strength Workout",
        category = "Strength",
        iconKey = "strength",
        colorHex = 0xFF475569,
        subtitle = "Progressive overload session"
    ),
    FitnessGoalPreset(
        title = "30-Min Cardio / Zone 2",
        category = "Cardio",
        iconKey = "cardio",
        colorHex = 0xFFE11D48,
        subtitle = "Aerobic base & VO2 Max"
    ),
    FitnessGoalPreset(
        title = "15-Min Post-Workout Mobility",
        category = "Recovery",
        iconKey = "stretch",
        colorHex = 0xFFD97706,
        subtitle = "Joint health & flexibility"
    ),
    FitnessGoalPreset(
        title = "8 Hours Restful Sleep",
        category = "Sleep",
        iconKey = "sleep",
        colorHex = 0xFF7C3AED,
        subtitle = "Deep REM & CNS recovery"
    ),
    FitnessGoalPreset(
        title = "5g Creatine & Multivitamins",
        category = "Supplements",
        iconKey = "pill",
        colorHex = 0xFF0D9488,
        subtitle = "ATP replenishment & immune"
    )
)

@Composable
fun FitnessGoalPresetsStrip(
    onSelectPreset: (FitnessGoalPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUICK GOAL TEMPLATES",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Text(
                text = "Tap to add",
                fontSize = 11.sp,
                color = Color(0xFF6750A4),
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(PRESET_FITNESS_GOALS) { preset ->
                val presetColor = Color(preset.colorHex)
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectPreset(preset) }
                        .testTag("preset_goal_${preset.category}"),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.2.dp, presetColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .background(presetColor.copy(alpha = 0.06f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(presetColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getHabitIconFromKey(preset.iconKey),
                                contentDescription = preset.title,
                                tint = presetColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = preset.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = preset.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = presetColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(presetColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConsistencyMilestonesCard(
    weeklyConsistencyPercent: Int,
    activeStreaks: Int,
    perfectDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("consistency_milestones_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, Color(0xFFEAB308).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFEF08A).copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Milestones",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "CONSISTENCY BADGES & MILESTONES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB45309),
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MilestoneBadgeItem(
                    title = "7-Day Streak",
                    subtitle = if (activeStreaks >= 3) "Unlocked 🔥" else "In Progress",
                    isUnlocked = activeStreaks >= 3,
                    badgeColor = Color(0xFFEA580C)
                )
                MilestoneBadgeItem(
                    title = "85%+ Consistency",
                    subtitle = if (weeklyConsistencyPercent >= 85) "Achieved 🏆" else "$weeklyConsistencyPercent% current",
                    isUnlocked = weeklyConsistencyPercent >= 85,
                    badgeColor = Color(0xFF16A34A)
                )
                MilestoneBadgeItem(
                    title = "Perfect Day",
                    subtitle = if (perfectDays > 0) "$perfectDays Days ⭐" else "0 / 1 Day",
                    isUnlocked = perfectDays > 0,
                    badgeColor = Color(0xFF6750A4)
                )
            }
        }
    }
}

@Composable
private fun MilestoneBadgeItem(
    title: String,
    subtitle: String,
    isUnlocked: Boolean,
    badgeColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isUnlocked) badgeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) badgeColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isUnlocked) Icons.Default.Star else Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = if (isUnlocked) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (isUnlocked) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
