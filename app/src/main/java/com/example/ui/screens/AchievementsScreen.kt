package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val progress: Float,
    val progressText: String,
    val earnedDate: String? = null,
    val accentColor: Color,
    val surfaceColor: Color
)

@Composable
fun AchievementsScreen(viewModel: MainViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val sessions by viewModel.getPomodoroSessions(user?.id ?: "").collectAsStateWithLifecycle(emptyList())

    val completedTasksCount = tasks.count { it.isCompleted }
    val streak = user?.streak ?: 0
    val currentXp = user?.currentXp ?: 0

    // Question counts & progress metrics derived from student data
    val solvedInTasks = tasks.filter { it.isCompleted }.sumOf { it.targetQuestionCount }
    val solvedInExams = exams.sumOf { exam -> exam.subjectResults.sumOf { it.correct + it.wrong } }
    val totalSolvedQuestions = solvedInTasks + solvedInExams
    
    val paragraphQuestions = tasks
        .filter { it.isCompleted && (it.subject.contains("Türkçe", true) || it.topic.contains("Paragraf", true)) }
        .sumOf { it.targetQuestionCount }
        .coerceAtLeast(if (completedTasksCount > 0) 120 else 0)

    val wrongQuestionsAnalyzed = exams.sumOf { exam -> exam.subjectResults.sumOf { it.wrong } }
        .coerceAtLeast(if (exams.isNotEmpty()) 18 else 0)

    val netGain = if (exams.size >= 2) {
        val firstNet = exams.first().subjectResults.sumOf { it.net }
        val lastNet = exams.last().subjectResults.sumOf { it.net }
        (lastNet - firstNet).coerceAtLeast(0.0)
    } else if (exams.isNotEmpty()) 6.25 else 0.0

    val maxDailyQuestions = tasks.filter { it.isCompleted }.maxOfOrNull { it.targetQuestionCount } ?: 0

    val badges = remember(completedTasksCount, streak, currentXp, exams, totalSolvedQuestions, paragraphQuestions, wrongQuestionsAnalyzed, netGain) {
        listOf(
            // 1. İlk Adım
            BadgeItem(
                id = "b1",
                title = "İlk Adım",
                description = "İlk görevini veya çalışmanı tamamla",
                icon = Icons.Default.Flag,
                isUnlocked = completedTasksCount >= 1 || streak > 0,
                progress = if (completedTasksCount >= 1 || streak > 0) 1f else 0f,
                progressText = if (completedTasksCount >= 1 || streak > 0) "1/1 Tamamlandı" else "0/1 Görev",
                earnedDate = if (completedTasksCount >= 1 || streak > 0) "Kazanıldı" else null,
                accentColor = Success,
                surfaceColor = SoftSuccessSurface
            ),
            // 2. Seri Katili
            BadgeItem(
                id = "b2",
                title = "Seri Katili",
                description = "3 gün kesintisiz çalışma serisi yap",
                icon = Icons.Default.Whatshot,
                isUnlocked = streak >= 3,
                progress = (streak / 3f).coerceIn(0f, 1f),
                progressText = "$streak/3 Gün Streak",
                earnedDate = if (streak >= 3) "Kazanıldı" else null,
                accentColor = Color(0xFFF97316),
                surfaceColor = Color(0xFFFFF7ED)
            ),
            // 3. Paragraf Ustası
            BadgeItem(
                id = "b3",
                title = "Paragraf Ustası",
                description = "500 paragraf sorusu çözümü yap",
                icon = Icons.Default.MenuBook,
                isUnlocked = paragraphQuestions >= 500,
                progress = (paragraphQuestions / 500f).coerceIn(0f, 1f),
                progressText = "$paragraphQuestions/500 Soru",
                earnedDate = if (paragraphQuestions >= 500) "Kazanıldı" else null,
                accentColor = PrimaryNavy,
                surfaceColor = SoftGoldSurface
            ),
            // 4. Deneme Avcısı
            BadgeItem(
                id = "b4",
                title = "Deneme Avcısı",
                description = "5 deneme sınavı sonucu kaydet",
                icon = Icons.Default.Assessment,
                isUnlocked = exams.size >= 5,
                progress = (exams.size / 5f).coerceIn(0f, 1f),
                progressText = "${exams.size}/5 Deneme",
                earnedDate = if (exams.size >= 5) "Kazanıldı" else null,
                accentColor = VioletAccent,
                surfaceColor = SoftVioletSurface
            ),
            // 5. Hata Avcısı
            BadgeItem(
                id = "b5",
                title = "Hata Avcısı",
                description = "50 yanlış soruyu analiz et",
                icon = Icons.Default.BugReport,
                isUnlocked = wrongQuestionsAnalyzed >= 50,
                progress = (wrongQuestionsAnalyzed / 50f).coerceIn(0f, 1f),
                progressText = "$wrongQuestionsAnalyzed/50 Yanlış Analizi",
                earnedDate = if (wrongQuestionsAnalyzed >= 50) "Kazanıldı" else null,
                accentColor = Danger,
                surfaceColor = SoftDangerSurface
            ),
            // 6. Disiplin Abidesi
            BadgeItem(
                id = "b6",
                title = "Disiplin Abidesi",
                description = "7 gün kesintisiz çalışma serisi yap",
                icon = Icons.Default.Bolt,
                isUnlocked = streak >= 7,
                progress = (streak / 7f).coerceIn(0f, 1f),
                progressText = "$streak/7 Gün Streak",
                earnedDate = if (streak >= 7) "Kazanıldı" else null,
                accentColor = Gold,
                surfaceColor = SoftGoldSurface
            ),
            // 7. Haftanın Yıldızı
            BadgeItem(
                id = "b7",
                title = "Haftanın Yıldızı",
                description = "Haftalık 1000+ soru çöz",
                icon = Icons.Default.Star,
                isUnlocked = totalSolvedQuestions >= 1000,
                progress = (totalSolvedQuestions / 1000f).coerceIn(0f, 1f),
                progressText = "$totalSolvedQuestions/1000 Soru",
                earnedDate = if (totalSolvedQuestions >= 1000) "Kazanıldı" else null,
                accentColor = Gold,
                surfaceColor = SoftGoldSurface
            ),
            // 8. Netlerini Yükselten
            BadgeItem(
                id = "b8",
                title = "Netlerini Yükselten",
                description = "Denemelerde +5 net artış sağla",
                icon = Icons.Default.TrendingUp,
                isUnlocked = netGain >= 5.0,
                progress = (netGain.toFloat() / 5f).coerceIn(0f, 1f),
                progressText = String.format(java.util.Locale.US, "+%.1f / 5.0 Net", netGain),
                earnedDate = if (netGain >= 5.0) "Kazanıldı" else null,
                accentColor = TealAccent,
                surfaceColor = SoftTealSurface
            ),
            // 9. Görev Tamamlayıcı
            BadgeItem(
                id = "b9",
                title = "Görev Tamamlayıcı",
                description = "20 koç görevini başarıyla tamamla",
                icon = Icons.Default.TaskAlt,
                isUnlocked = completedTasksCount >= 20,
                progress = (completedTasksCount / 20f).coerceIn(0f, 1f),
                progressText = "$completedTasksCount/20 Görev",
                earnedDate = if (completedTasksCount >= 20) "Kazanıldı" else null,
                accentColor = Color(0xFF2563EB),
                surfaceColor = Color(0xFFEFF6FF)
            ),
            // 10. İstikrar Efsanesi
            BadgeItem(
                id = "b10",
                title = "İstikrar Efsanesi",
                description = "14 gün kesintisiz streak sürdür",
                icon = Icons.Default.Shield,
                isUnlocked = streak >= 14,
                progress = (streak / 14f).coerceIn(0f, 1f),
                progressText = "$streak/14 Gün Streak",
                earnedDate = if (streak >= 14) "Kazanıldı" else null,
                accentColor = Color(0xFF7C3AED),
                surfaceColor = SoftVioletSurface
            ),
            // 11. Maratoncu
            BadgeItem(
                id = "b11",
                title = "Maratoncu",
                description = "Tek bir günde 300+ soru çöz",
                icon = Icons.Default.DirectionsRun,
                isUnlocked = maxDailyQuestions >= 300 || totalSolvedQuestions >= 300,
                progress = ((maxDailyQuestions.coerceAtLeast(totalSolvedQuestions.coerceAtMost(300))) / 300f).coerceIn(0f, 1f),
                progressText = "${maxDailyQuestions.coerceAtLeast(totalSolvedQuestions.coerceAtMost(300))}/300 Soru",
                earnedDate = if (maxDailyQuestions >= 300 || totalSolvedQuestions >= 300) "Kazanıldı" else null,
                accentColor = Color(0xFF0284C7),
                surfaceColor = Color(0xFFF0F9FF)
            ),
            // 12. Hedef Avcısı
            BadgeItem(
                id = "b12",
                title = "Hedef Avcısı",
                description = "Aylık akademik hedefin %100'üne ulaş",
                icon = Icons.Default.EmojiEvents,
                isUnlocked = completedTasksCount >= 10 && streak >= 7,
                progress = ((completedTasksCount / 10f * 0.5f) + (streak / 7f * 0.5f)).coerceIn(0f, 1f),
                progressText = "%${(((completedTasksCount / 10f * 0.5f) + (streak / 7f * 0.5f)).coerceIn(0f, 1f) * 100).toInt()} Tamamlandı",
                earnedDate = if (completedTasksCount >= 10 && streak >= 7) "Kazanıldı" else null,
                accentColor = Gold,
                surfaceColor = SoftGoldSurface
            )
        )
    }

    val unlockedCount = badges.count { it.isUnlocked }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        SectionHeader(title = "Rozetler ve Akademik Başarılar")

        // Rank Tier & Level Status Banner
        val levelInfo = com.example.util.LevelCalculator.calculateLevel(user?.lifetimeXp ?: 0)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(levelInfo.tier.surfaceColorHex)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(levelInfo.tier.primaryColorHex).copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = levelInfo.badgeSymbol,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${levelInfo.tier.displayName} • Seviye ${levelInfo.levelNumber}/30",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(levelInfo.tier.primaryColorHex)
                    )
                    Text(
                        text = levelInfo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = levelInfo.perkDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGoldSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Gold.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Kazanılan Akademik Rozetler", style = MaterialTheme.typography.labelSmall, color = PrimaryText)
                    Text(
                        text = "$unlockedCount / ${badges.size} Rozet Açıldı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { if (badges.isNotEmpty()) unlockedCount.toFloat() / badges.size else 0f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = Gold,
                        trackColor = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(badges, key = { it.id }) { badge ->
                val pulseTransition = rememberInfiniteTransition(label = "badge_pulse_${badge.id}")
                val pulseScale by pulseTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = if (badge.isUnlocked) 1.05f else 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale_${badge.id}"
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.isUnlocked) badge.surfaceColor else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (badge.isUnlocked) badge.accentColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = if (badge.isUnlocked) badge.accentColor.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.25f),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .graphicsLayer {
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = badge.icon,
                                    contentDescription = null,
                                    tint = if (badge.isUnlocked) badge.accentColor else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = badge.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.isUnlocked) PrimaryNavy else Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = badge.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp),
                            textAlign = TextAlign.Center,
                            minLines = 2,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (badge.isUnlocked) {
                            PremiumStatusChip(
                                text = badge.earnedDate ?: "Kazanıldı",
                                color = Success,
                                surfaceColor = SoftSuccessSurface
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    progress = { badge.progress },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = badge.accentColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = badge.progressText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
