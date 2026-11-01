package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.RewardRequestStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.StudentAvatar
import com.example.viewmodel.MainViewModel

@Composable
fun CoachDashboardScreen(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit,
    onStudentClick: (String) -> Unit,
    onNavigateToPortfolio: (() -> Unit)? = null,
    onNavigateToApprovals: (() -> Unit)? = null,
    onNavigateToMeetings: (() -> Unit)? = null,
    onNavigateToAnalytics: (() -> Unit)? = null
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val rewardRequests by viewModel.rewardRequests.collectAsStateWithLifecycle()
    val pendingTasks by viewModel.pendingApprovalTasks.collectAsStateWithLifecycle()
    val allMeetings by viewModel.allMeetings.collectAsStateWithLifecycle()

    val assignedStudents = allStudents.filter { user?.assignedStudents?.contains(it.id) == true }
    val pendingRewardCount = rewardRequests.count { it.status == RewardRequestStatus.PENDING }
    val totalPendingApprovals = pendingRewardCount + pendingTasks.size
    val studentCapacity = user?.studentCapacity ?: 0

    Box(modifier = Modifier.fillMaxSize()) {
        PremiumAnimatedBackground()
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // 1. Deep Navy Hero Banner with Slogan and Gold Accent
        item {
            AnimatedEntry {
                PremiumHeroBanner(
                    title = "Koç ${user?.name ?: ""}",
                    subtitle = user?.title ?: "Eğitim Koçu & Mentor • Planını Kur. Disiplinini Koru. Hedefine Ulaş.",
                    badgeText = "KOMUTA MERKEZİ",
                    avatar = {
                        StudentAvatar(
                            name = user?.name ?: "Koç",
                            avatarUri = user?.avatarUri,
                            size = 56.dp
                        )
                    },
                    actions = {
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Aktif Koçluk Sezonu",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                )
            }
        }

        // 1.1 Bugünün Koçluk Notu Card
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val todayCoachNote = remember { com.example.util.MotivationQuotes.getTodayCoachNote(context) }
            AnimatedEntry(delayMillis = 30) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftGoldSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
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
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bugünün Koçluk Notu",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )
                            Text(
                                text = todayCoachNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 2. Capacity Progress Card with Animated Progress
        item {
            AnimatedEntry(delayMillis = 50) {
                PremiumCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Öğrenci Kontenjan Durumu",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${assignedStudents.size} / $studentCapacity Öğrenci",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (assignedStudents.size >= studentCapacity && studentCapacity > 0) Danger else TealAccent
                            )
                        }

                        val progress = if (studentCapacity > 0) assignedStudents.size.toFloat() / studentCapacity else 0f
                        AnimatedProgressIndicator(
                            progress = progress,
                            color = if (progress >= 1f) Danger else TealAccent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Operational 2x2 Metric Grid with Palette Accents
        item {
            AnimatedEntry(delayMillis = 100) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            label = "Öğrenci Portföyü",
                            value = "${assignedStudents.size} Aktif",
                            icon = Icons.Default.Groups,
                            color = PrimaryNavy,
                            containerColor = SoftTealSurface.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToPortfolio
                        )
                        StatCard(
                            label = "Bekleyen Onay",
                            value = "$totalPendingApprovals İşlem",
                            icon = Icons.Default.PendingActions,
                            color = if (totalPendingApprovals > 0) Gold else Success,
                            containerColor = if (totalPendingApprovals > 0) SoftGoldSurface else SoftSuccessSurface,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToApprovals
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            label = "Veli Görüşmeleri",
                            value = "${allMeetings.size} Kayıt",
                            icon = Icons.Default.ContactPhone,
                            color = VioletAccent,
                            containerColor = SoftVioletSurface.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToMeetings
                        )
                        StatCard(
                            label = "Akademik Analiz",
                            value = "Yerel Motor",
                            icon = Icons.Default.AutoGraph,
                            color = Success,
                            containerColor = SoftSuccessSurface.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAnalytics
                        )
                    }
                }
            }
        }

        // 4. Quick Action Navigation Row
        item {
            AnimatedEntry(delayMillis = 150) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onNavigateToPortfolio != null) {
                        OutlinedButton(
                            onClick = onNavigateToPortfolio,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Portföy", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    if (onNavigateToPlanner != null) {
                        Button(
                            onClick = onNavigateToPlanner,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = Gold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Planlayıcı", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // 5. Assigned Students Section Header
        item {
            SectionHeader(
                title = "Öğrencilerim & 360° Takip",
                actionText = "Tümünü Gör",
                onActionClick = onNavigateToPortfolio
            )
        }

        if (assignedStudents.isEmpty()) {
            item {
                EmptyState(
                    message = "Henüz atanmış bir öğrenci bulunmuyor. Sol menüden 'Öğrenci Portföyüm' sekmesine geçerek öğrenci ekleyebilirsiniz.",
                    icon = Icons.Default.PersonAdd
                )
            }
        } else {
            items(assignedStudents, key = { it.id }) { student ->
                AnimatedEntry {
                    PremiumCard(onClick = { onStudentClick(student.id) }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StudentAvatar(name = student.name, avatarUri = student.avatarUri, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = student.educationLevel?.displayName ?: "Seviye Belirtilmedi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                XPBadge(xp = student.currentXp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "360° İncele",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}
