package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.PomodoroSession
import com.example.ui.animation.ConfettiParticleOverlay
import com.example.ui.animation.FloatingXpEffect
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.XpPolicy
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class PomodoroMode(
    val displayName: String,
    val defaultMinutes: Int,
    val color: Color,
    val surfaceColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    FOCUS("Odaklanma", 25, PrimaryNavy, SoftTealSurface, Icons.Default.Timer),
    SHORT_BREAK("Kısa Mola", 5, TealAccent, SoftTealSurface, Icons.Default.FreeBreakfast),
    LONG_BREAK("Uzun Mola", 15, VioletAccent, SoftVioletSurface, Icons.Default.SelfImprovement)
}

enum class AmbientSound(
    val displayName: String,
    val iconSymbol: String
) {
    OFF("Sessiz", "🔇"),
    RAIN("Yağmur Sesi", "🌧️"),
    WHITE_NOISE("Beyaz Gürültü", "🎧"),
    LIBRARY("Kütüphane", "📚"),
    CAFE("Kafe Vibe", "☕")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val sessions by viewModel.getPomodoroSessions(user?.id ?: "").collectAsStateWithLifecycle(emptyList())

    // Customizable Duration States
    var customFocusMinutes by remember { mutableStateOf(25) }
    var customShortBreakMinutes by remember { mutableStateOf(5) }
    var customLongBreakMinutes by remember { mutableStateOf(15) }
    var dailyTargetPomodoros by remember { mutableStateOf(5) }

    var mode by remember { mutableStateOf(PomodoroMode.FOCUS) }
    var selectedAmbientSound by remember { mutableStateOf(AmbientSound.OFF) }

    val currentModeMinutes = when (mode) {
        PomodoroMode.FOCUS -> customFocusMinutes
        PomodoroMode.SHORT_BREAK -> customShortBreakMinutes
        PomodoroMode.LONG_BREAK -> customLongBreakMinutes
    }

    var totalSeconds by remember { mutableStateOf(currentModeMinutes * 60) }
    var secondsLeft by remember { mutableStateOf(currentModeMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var subjectText by remember { mutableStateOf("Genel Odaklanma") }

    var showDurationDialog by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    // Completion animations
    var showConfetti by remember { mutableStateOf(false) }
    var showFloatingXp by remember { mutableStateOf(false) }
    var earnedXpAmount by remember { mutableStateOf(0) }

    // Sync mode change or custom minutes change
    LaunchedEffect(mode, customFocusMinutes, customShortBreakMinutes, customLongBreakMinutes) {
        if (!isRunning) {
            val minutes = when (mode) {
                PomodoroMode.FOCUS -> customFocusMinutes
                PomodoroMode.SHORT_BREAK -> customShortBreakMinutes
                PomodoroMode.LONG_BREAK -> customLongBreakMinutes
            }
            totalSeconds = minutes * 60
            secondsLeft = minutes * 60
        }
    }

    // Timer Countdown Coroutine
    LaunchedEffect(isRunning, secondsLeft) {
        if (isRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        } else if (isRunning && secondsLeft == 0) {
            isRunning = false
            showConfetti = true

            val earnedXp = if (mode == PomodoroMode.FOCUS) {
                XpPolicy.calculatePomodoroXp(currentModeMinutes).coerceAtLeast(3)
            } else 0

            earnedXpAmount = earnedXp
            if (earnedXp > 0) {
                showFloatingXp = true
            }

            viewModel.savePomodoroSession(currentModeMinutes, mode.displayName, subjectText)
            showCompletionDialog = true

            // Reset for next round
            totalSeconds = currentModeMinutes * 60
            secondsLeft = currentModeMinutes * 60
        }
    }

    // Calculate today's discipline statistics
    val todayCalendar = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val todayTimestamp = todayCalendar.timeInMillis

    val todaySessions = sessions.filter { it.timestamp >= todayTimestamp }
    val todayCompletedFocusCount = todaySessions.count { it.mode.contains("Odaklanma", ignoreCase = true) || it.mode.equals("FOCUS", ignoreCase = true) }
    val todayFocusMinutesSum = todaySessions.filter { it.mode.contains("Odaklanma", ignoreCase = true) || it.mode.equals("FOCUS", ignoreCase = true) }.sumOf { it.durationMinutes }

    val targetProgress = if (dailyTargetPomodoros > 0) (todayCompletedFocusCount.toFloat() / dailyTargetPomodoros).coerceIn(0f, 1f) else 0f

    val progressRatio = if (totalSeconds > 0) secondsLeft.toFloat() / totalSeconds else 0f
    val minutesFormatted = secondsLeft / 60
    val secondsFormatted = secondsLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutesFormatted, secondsFormatted)

    Box(modifier = Modifier.fillMaxSize()) {
        ConfettiParticleOverlay(visible = showConfetti)

        if (showFloatingXp) {
            FloatingXpEffect(
                xpAmount = earnedXpAmount,
                trigger = showFloatingXp,
                modifier = Modifier.align(Alignment.Center),
                onAnimationEnd = { showFloatingXp = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionHeader(
                title = "Pomodoro Odaklanma Odası",
                actionText = "Süre Ayarla ⚙️",
                onActionClick = { showDurationDialog = true }
            )

            // 1. Mode Selector Tabs
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                PomodoroMode.values().forEachIndexed { index, pMode ->
                    SegmentedButton(
                        selected = mode == pMode,
                        onClick = { mode = pMode },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = PomodoroMode.values().size)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(pMode.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(pMode.displayName, fontWeight = if (mode == pMode) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // 2. Preset Interval Quick Selection Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Süre Aralığı:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )

                val presetOptions = when (mode) {
                    PomodoroMode.FOCUS -> listOf(15, 25, 35, 45, 60)
                    PomodoroMode.SHORT_BREAK -> listOf(3, 5, 8, 10)
                    PomodoroMode.LONG_BREAK -> listOf(10, 15, 20, 30)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetOptions.forEach { mins ->
                        FilterChip(
                            selected = currentModeMinutes == mins,
                            onClick = {
                                when (mode) {
                                    PomodoroMode.FOCUS -> customFocusMinutes = mins
                                    PomodoroMode.SHORT_BREAK -> customShortBreakMinutes = mins
                                    PomodoroMode.LONG_BREAK -> customLongBreakMinutes = mins
                                }
                            },
                            label = { Text("${mins}dk", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = mode.color,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // 3. Daily Discipline Tracker Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGoldSurface),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Gold.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Günlük Disiplin İlerlemesi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                                Text(
                                    text = "Bugün Toplam: ${todayFocusMinutesSum / 60}s ${todayFocusMinutesSum % 60}dk Odaklanma",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                            }
                        }

                        Surface(
                            color = Gold,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$todayCompletedFocusCount / $dailyTargetPomodoros Oturum",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tomato / Session Progress Indicators Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(dailyTargetPomodoros) { idx ->
                                val isDone = idx < todayCompletedFocusCount
                                Surface(
                                    color = if (isDone) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (isDone) "🍅" else "⚪",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "%${(targetProgress * 100).toInt()} Disiplin",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    AnimatedProgressIndicator(
                        progress = targetProgress,
                        color = Gold,
                        trackColor = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Main Circular Timer Display Card with Pulse Animation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = mode.surfaceColor),
                border = BorderStroke(1.dp, mode.color.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.98f,
                        targetValue = if (isRunning) 1.03f else 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "timer_pulse"
                    )

                    val animatedProgress by animateFloatAsState(
                        targetValue = progressRatio,
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                        label = "progressRatio"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(210.dp)
                            .scale(pulseScale)
                    ) {
                        val trackColor = mode.color.copy(alpha = 0.15f)
                        val activeColor = mode.color

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            drawCircle(color = trackColor, style = Stroke(width = strokeWidth))
                            drawArc(
                                color = activeColor,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = mode.color.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = mode.displayName.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = mode.color,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ambient Sound Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedAmbientSound.iconSymbol, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedAmbientSound.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AmbientSound.values().forEach { sound ->
                                Surface(
                                    color = if (selectedAmbientSound == sound) mode.color else Color.Transparent,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable { selectedAmbientSound = sound }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(sound.iconSymbol, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subjectText,
                        onValueChange = { subjectText = it },
                        label = { Text("Çalışılan Ders / Konu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Timer Control Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isRunning = false
                                secondsLeft = totalSeconds
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .bouncyClick {
                                    isRunning = false
                                    secondsLeft = totalSeconds
                                }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sıfırla", tint = PrimaryNavy)
                        }

                        Button(
                            onClick = { isRunning = !isRunning },
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = mode.color),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Duraklat" else "Başlat"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRunning) "Duraklat" else "Başlat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                isRunning = false
                                secondsLeft = 0
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .bouncyClick {
                                    isRunning = false
                                    secondsLeft = 0
                                }
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Atla", tint = PrimaryNavy)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Completed Sessions Log Section
            SectionHeader(title = "Tamamlanan Oturumlar (${todaySessions.size} Bugün)")

            if (sessions.isEmpty()) {
                EmptyState(
                    message = "Henüz kaydedilmiş bir Pomodoro oturumu bulunmuyor. Zamanlayıcıyı başlatıp ilk oturumunu tamamla!",
                    icon = Icons.Default.Timer
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(sessions.sortedByDescending { it.timestamp }, key = { it.id }) { session ->
                        PremiumCard {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    color = SoftTealSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(session.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${session.mode} • ${session.durationMinutes} Dakika",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(session.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Duration Settings Dialog
    if (showDurationDialog) {
        AlertDialog(
            onDismissRequest = { showDurationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = PrimaryNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Özel Pomodoro Ayarları", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Süreleri ve günlük hedefini kişiselleştir:")

                    // Focus duration stepper
                    DurationStepperRow(
                        label = "Odaklanma Süresi:",
                        valueMinutes = customFocusMinutes,
                        onValueChange = { customFocusMinutes = it.coerceIn(5, 120) }
                    )

                    // Short break stepper
                    DurationStepperRow(
                        label = "Kısa Mola Süresi:",
                        valueMinutes = customShortBreakMinutes,
                        onValueChange = { customShortBreakMinutes = it.coerceIn(1, 30) }
                    )

                    // Long break stepper
                    DurationStepperRow(
                        label = "Uzun Mola Süresi:",
                        valueMinutes = customLongBreakMinutes,
                        onValueChange = { customLongBreakMinutes = it.coerceIn(5, 60) }
                    )

                    HorizontalDivider()

                    // Daily Target Pomodoros Stepper
                    DurationStepperRow(
                        label = "Günlük Hedef Oturum:",
                        valueMinutes = dailyTargetPomodoros,
                        suffix = "Oturum",
                        onValueChange = { dailyTargetPomodoros = it.coerceIn(1, 15) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDurationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Text("Kaydet & Uygula", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Completion Celebration Dialog
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = {
                showCompletionDialog = false
                showConfetti = false
            },
            icon = {
                Surface(
                    color = Gold.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(32.dp))
                    }
                }
            },
            title = {
                Text(
                    text = "Tebrikler! Oturum Tamamlandı 🎉",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${mode.displayName} ($currentModeMinutes Dk) oturumunu başarıyla tamamladın.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    if (earnedXpAmount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = SoftGoldSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "+$earnedXpAmount XP Hesabına Eklendi ✨",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Gold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompletionDialog = false
                        showConfetti = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text("Devam Et", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun DurationStepperRow(
    label: String,
    valueMinutes: Int,
    suffix: String = "Dakika",
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onValueChange(valueMinutes - 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Azalt")
            }

            Text(
                text = "$valueMinutes $suffix",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = { onValueChange(valueMinutes + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Artır")
            }
        }
    }
}
