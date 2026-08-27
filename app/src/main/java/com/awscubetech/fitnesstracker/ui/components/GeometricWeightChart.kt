package com.awscubetech.fitnesstracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awscubetech.fitnesstracker.data.local.BodyMeasurementEntity
import com.awscubetech.fitnesstracker.ui.theme.GeoLiveIndicator
import com.awscubetech.fitnesstracker.ui.theme.GeoPrimary
import com.awscubetech.fitnesstracker.ui.theme.GeoPrimaryDark
import kotlin.math.max
import kotlin.math.min

enum class ChartMetricType(val label: String, val unit: String) {
    WEIGHT("Weight", "kg"),
    BODY_FAT("Body Fat", "%"),
    WAIST("Waist", "cm")
}

@Composable
fun GeometricWeightChart(
    measurements: List<BodyMeasurementEntity>,
    modifier: Modifier = Modifier,
    targetGoalValue: Double = 72.0
) {
    var selectedMetric by remember { mutableStateOf(ChartMetricType.WEIGHT) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    // Reverse list so chronological order is left-to-right (oldest -> newest)
    val chronologicalList = remember(measurements) {
        measurements.reversed()
    }

    val dataPoints: List<Pair<String, Double>> = remember(chronologicalList, selectedMetric) {
        chronologicalList.map { item ->
            val value = when (selectedMetric) {
                ChartMetricType.WEIGHT -> item.weightKg
                ChartMetricType.BODY_FAT -> item.bodyFatPercent
                ChartMetricType.WAIST -> item.waistCm
            }
            Pair(item.dateFormatted, value)
        }
    }

    val firstVal = dataPoints.firstOrNull()?.second ?: 0.0
    val lastVal = dataPoints.lastOrNull()?.second ?: 0.0
    val totalDelta = if (dataPoints.size >= 2) lastVal - firstVal else 0.0

    // Smooth numerical transitions for stats badges
    val animatedLastVal by animateFloatAsState(
        targetValue = lastVal.toFloat(),
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "animated_last_val"
    )
    val animatedFirstVal by animateFloatAsState(
        targetValue = firstVal.toFloat(),
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "animated_first_val"
    )
    val animatedDelta by animateFloatAsState(
        targetValue = totalDelta.toFloat(),
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "animated_delta"
    )

    // Entrance and Update Sweep animation (triggers on metric or data count change)
    val sweepProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedMetric, dataPoints.size, dataPoints.map { it.second }) {
        sweepProgress.snapTo(0f)
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 750,
                easing = FastOutSlowInEasing
            )
        )
    }

    // Infinite radar beacon pulse for the live endpoint and header
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseRadiusRatio by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("geometric_weight_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Bar in Geometric Balance styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WEIGHT & PROGRESSION",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Geometric Trend Timeline",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Geometric Live Status Pill with animated beacon
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp * pulseRadiusRatio.coerceAtMost(1.8f))
                                .clip(CircleShape)
                                .background(GeoLiveIndicator.copy(alpha = pulseAlpha))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GeoLiveIndicator)
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "LIVE SYNC",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Selector Pills with animated transitions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChartMetricType.values().forEach { metric ->
                    val isSelected = metric == selectedMetric

                    val targetBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    val targetTextColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    val targetBorderWidth by animateDpAsState(
                        targetValue = if (isSelected) 1.5.dp else 0.dp,
                        label = "pill_border_width"
                    )

                    val animatedBg by animateColorAsState(targetValue = targetBg, label = "pill_bg")
                    val animatedTextCol by animateColorAsState(targetValue = targetTextColor, label = "pill_text")

                    Surface(
                        onClick = {
                            selectedMetric = metric
                            selectedIndex = -1
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = animatedBg,
                        border = if (isSelected) BorderStroke(targetBorderWidth, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = metric.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animatedTextCol
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Stats Row (Geometric Cards with animated values)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatBadge(
                    label = "CURRENT",
                    value = String.format("%.1f %s", animatedLastVal, selectedMetric.unit),
                    highlightColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatBadge(
                    label = "STARTING",
                    value = String.format("%.1f %s", animatedFirstVal, selectedMetric.unit),
                    highlightColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                MetricDeltaBadge(
                    label = "NET CHANGE",
                    delta = animatedDelta.toDouble(),
                    unit = selectedMetric.unit,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Data Point Tooltip (if selected) with smooth enter/exit
            AnimatedVisibility(
                visible = selectedIndex >= 0 && selectedIndex < dataPoints.size,
                enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(tween(200)),
                exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(tween(150))
            ) {
                val point = dataPoints.getOrNull(selectedIndex)
                if (point != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📅 ${point.first}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${point.second} ${selectedMetric.unit}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Interactive Canvas Line Chart with entrance and update sweep
            val primaryColor = MaterialTheme.colorScheme.primary
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            val textColor = MaterialTheme.colorScheme.onSurfaceVariant
            val goalColor = Color(0xFFE5B54F)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (dataPoints.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No measurement records logged yet",
                            fontSize = 12.sp,
                            color = textColor
                        )
                    }
                } else {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(dataPoints) {
                                detectTapGestures { offset ->
                                    val count = dataPoints.size
                                    if (count > 0) {
                                        val step = size.width / max(1, count - 1)
                                        val idx = (offset.x / step).toInt().coerceIn(0, count - 1)
                                        selectedIndex = idx
                                    }
                                }
                            }
                            .pointerInput(dataPoints) {
                                detectDragGestures { change, _ ->
                                    val count = dataPoints.size
                                    if (count > 0) {
                                        val step = size.width / max(1, count - 1)
                                        val idx = (change.position.x / step).toInt().coerceIn(0, count - 1)
                                        selectedIndex = idx
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val paddingBottom = 20.dp.toPx()
                        val paddingTop = 16.dp.toPx()
                        val drawHeight = canvasHeight - paddingBottom - paddingTop
                        val currentSweep = sweepProgress.value
                        val clippedSweepWidth = canvasWidth * currentSweep

                        val values = dataPoints.map { it.second }
                        val minVal = (values.minOrNull() ?: 0.0) - 1.5
                        val maxVal = (values.maxOrNull() ?: 100.0) + 1.5
                        val valRange = max(1.0, maxVal - minVal)

                        // 1. Draw Horizontal Grid Lines
                        val gridSteps = 3
                        for (i in 0..gridSteps) {
                            val y = paddingTop + drawHeight * (i.toFloat() / gridSteps.toFloat())
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        }

                        // 2. Target Goal Reference Line (if in weight metric) with animated alpha
                        if (selectedMetric == ChartMetricType.WEIGHT && targetGoalValue in minVal..maxVal) {
                            val goalY = paddingTop + drawHeight * (1f - ((targetGoalValue - minVal) / valRange).toFloat())
                            drawLine(
                                color = goalColor.copy(alpha = 0.85f * currentSweep),
                                start = Offset(0f, goalY),
                                end = Offset(canvasWidth * currentSweep, goalY),
                                strokeWidth = 1.2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                            )
                        }

                        val pointCoordinates = mutableListOf<Offset>()
                        val count = dataPoints.size

                        for (i in dataPoints.indices) {
                            val x = if (count == 1) canvasWidth / 2f else i.toFloat() * (canvasWidth / (count - 1))
                            val normalizedY = 1f - ((dataPoints[i].second - minVal) / valRange).toFloat()
                            val y = paddingTop + (normalizedY * drawHeight)
                            pointCoordinates.add(Offset(x, y))
                        }

                        // 3. Draw Chart Line & Area Fill with animated entrance clipping
                        clipRect(
                            left = 0f,
                            top = 0f,
                            right = clippedSweepWidth,
                            bottom = canvasHeight
                        ) {
                            if (pointCoordinates.size == 1) {
                                // Single point circle
                                drawCircle(
                                    color = primaryColor,
                                    radius = 6.dp.toPx(),
                                    center = pointCoordinates[0]
                                )
                            } else if (pointCoordinates.size >= 2) {
                                // Smooth Cubic Bézier Curve Path
                                val strokePath = Path()
                                val fillPath = Path()

                                strokePath.moveTo(pointCoordinates[0].x, pointCoordinates[0].y)
                                fillPath.moveTo(pointCoordinates[0].x, canvasHeight)
                                fillPath.lineTo(pointCoordinates[0].x, pointCoordinates[0].y)

                                for (i in 0 until pointCoordinates.size - 1) {
                                    val current = pointCoordinates[i]
                                    val next = pointCoordinates[i + 1]
                                    val controlPoint1 = Offset(
                                        current.x + (next.x - current.x) / 2f,
                                        current.y
                                    )
                                    val controlPoint2 = Offset(
                                        current.x + (next.x - current.x) / 2f,
                                        next.y
                                    )
                                    strokePath.cubicTo(
                                        controlPoint1.x, controlPoint1.y,
                                        controlPoint2.x, controlPoint2.y,
                                        next.x, next.y
                                    )
                                    fillPath.cubicTo(
                                        controlPoint1.x, controlPoint1.y,
                                        controlPoint2.x, controlPoint2.y,
                                        next.x, next.y
                                    )
                                }

                                fillPath.lineTo(pointCoordinates.last().x, canvasHeight)
                                fillPath.close()

                                // Gradient Area Fill under curve
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.35f * currentSweep),
                                            primaryColor.copy(alpha = 0.02f)
                                        ),
                                        startY = paddingTop,
                                        endY = canvasHeight
                                    )
                                )

                                // Stroke Line
                                drawPath(
                                    path = strokePath,
                                    color = primaryColor,
                                    style = Stroke(
                                        width = 3.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // 4. Draw Geometric Data Points & Halos with staggered bounce pop
                        pointCoordinates.forEachIndexed { index, point ->
                            // Only render node when the sweep reaches it
                            if (point.x <= clippedSweepWidth + 8f) {
                                val isSelected = index == selectedIndex
                                val isLatest = index == pointCoordinates.lastIndex

                                // Scale factor for node entry pop
                                val nodePop = ((clippedSweepWidth - point.x) / 25.dp.toPx()).coerceIn(0.2f, 1f)

                                // Pulsing Radar Beacon for the latest (most recent) data node or selected node
                                if (isLatest || isSelected) {
                                    drawCircle(
                                        color = primaryColor.copy(alpha = pulseAlpha),
                                        radius = (14.dp.toPx() * pulseRadiusRatio) * nodePop,
                                        center = point
                                    )
                                }

                                // Outer halo
                                drawCircle(
                                    color = if (isSelected) primaryColor.copy(alpha = 0.45f) else primaryColor.copy(alpha = 0.18f),
                                    radius = (if (isSelected) 10.dp.toPx() else 6.5.dp.toPx()) * nodePop,
                                    center = point
                                )

                                // Inner core node
                                drawCircle(
                                    color = if (isSelected) Color.White else primaryColor,
                                    radius = (if (isSelected) 5.5.dp.toPx() else 3.8.dp.toPx()) * nodePop,
                                    center = point
                                )

                                if (isSelected) {
                                    drawCircle(
                                        color = primaryColor,
                                        radius = 2.5.dp.toPx() * nodePop,
                                        center = point
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-chart Guide & Goal Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap or scrub chart points to inspect records",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (selectedMetric == ChartMetricType.WEIGHT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFE5B54F))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Goal: ${targetGoalValue}kg",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatBadge(
    label: String,
    value: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = highlightColor
            )
        }
    }
}

@Composable
private fun MetricDeltaBadge(
    label: String,
    delta: Double,
    unit: String,
    modifier: Modifier = Modifier
) {
    val isDrop = delta < 0
    val isZero = delta == 0.0
    val badgeColor = if (isZero) MaterialTheme.colorScheme.onSurfaceVariant else if (isDrop) GeoPrimary else Color(0xFFEF4444)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isZero) {
                    Icon(
                        imageVector = if (isDrop) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text(
                    text = String.format("%s%.1f %s", if (delta > 0) "+" else "", delta, unit),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = badgeColor
                )
            }
        }
    }
}
