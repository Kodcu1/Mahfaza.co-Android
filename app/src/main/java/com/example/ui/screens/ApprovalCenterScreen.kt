package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Task
import com.example.model.User
import com.example.ui.components.*
import com.example.util.StudentAvatar
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalCenterScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val pendingTasks by viewModel.pendingApprovalTasks.collectAsStateWithLifecycle()
    val approvedTasks by viewModel.approvedTasks.collectAsStateWithLifecycle()
    val rejectedTasks by viewModel.rejectedTasks.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    var previewPhotoUri by remember { mutableStateOf<String?>(null) }
    var actionLoadingId by remember { mutableStateOf<String?>(null) }

    var taskToArchive by remember { mutableStateOf<Task?>(null) }
    var showBulkArchiveDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Bekleyenler (${pendingTasks.size})",
        "Onaylananlar (${approvedTasks.size})",
        "Reddedilenler (${rejectedTasks.size})"
    )

    // Single Archive Confirmation Dialog
    if (taskToArchive != null) {
        AlertDialog(
            onDismissRequest = { taskToArchive = null },
            title = { Text("Görevi Arşivle / Temizle", fontWeight = FontWeight.Bold) },
            text = { Text("'${taskToArchive?.title}' görevini aktif onay listesinden kaldırmak istediğinize emin misiniz? Öğrencinin kazanıldığı XP ve başarı geçmişi sistemde güvenle korunacaktır.") },
            confirmButton = {
                Button(
                    onClick = {
                        val taskId = taskToArchive!!.id
                        taskToArchive = null
                        viewModel.archiveApprovedTask(taskId)
                        Toast.makeText(context, "Görev koç görünümünden arşivlendi.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF233B5D))
                ) {
                    Text("Arşivle", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToArchive = null }) { Text("İptal") }
            }
        )
    }

    // Bulk Archive Confirmation Dialog
    if (showBulkArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showBulkArchiveDialog = false },
            title = { Text("Tüm Onaylananları Temizle", fontWeight = FontWeight.Bold) },
            text = { Text("Onaylananlar listesindeki ${approvedTasks.size} görevin tamamını arşivlemek istediğinize emin misiniz? Öğrencilerin toplam XP, seviye ve seri bilgileri kesinlikle etkilenmeyecektir.") },
            confirmButton = {
                Button(
                    onClick = {
                        showBulkArchiveDialog = false
                        viewModel.archiveAllApprovedTasks()
                        Toast.makeText(context, "Tüm onaylanan görevler başarıyla arşivlendi.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("Tümünü Temizle", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkArchiveDialog = false }) { Text("İptal") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Onay Merkezi",
            actionText = "${pendingTasks.size} Bekleyen"
        )

        Text(
            text = "Öğrencilerin tamamladığı ve kanıt yüklediği görevleri inceleyip onaylayabilir veya düzeltme için reddedebilirsiniz.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bulk Clean Button for Approved Tab
        if (selectedTab == 1 && approvedTasks.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { showBulkArchiveDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tüm Onaylananları Temizle (${approvedTasks.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        val currentList = when (selectedTab) {
            0 -> pendingTasks
            1 -> approvedTasks
            else -> rejectedTasks
        }

        if (currentList.isEmpty()) {
            val emptyMsg = when (selectedTab) {
                0 -> "Şu an onay bekleyen herhangi bir görev bulunmuyor."
                1 -> "Henüz onaylanmış ve aktif görev bulunmuyor."
                else -> "Reddedilen görev bulunmuyor."
            }
            EmptyState(
                message = emptyMsg,
                icon = when (selectedTab) {
                    0 -> Icons.Default.CheckCircle
                    1 -> Icons.Default.Verified
                    else -> Icons.Default.ErrorOutline
                }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentList, key = { it.second.id }) { (student, task) ->
                    AnimatedEntry {
                        ApprovalTaskCard(
                            student = student,
                            task = task,
                            tabType = selectedTab,
                            isLoading = actionLoadingId == task.id,
                            onPhotoClick = { uri -> previewPhotoUri = uri },
                            onApprove = {
                                actionLoadingId = task.id
                                viewModel.approveTask(task.id)
                                Toast.makeText(context, "${task.title} onaylandı (+${task.xpReward} XP)", Toast.LENGTH_SHORT).show()
                                actionLoadingId = null
                            },
                            onReject = {
                                actionLoadingId = task.id
                                viewModel.rejectTask(task.id)
                                Toast.makeText(context, "${task.title} reddedildi ve düzeltme için geri gönderildi.", Toast.LENGTH_SHORT).show()
                                actionLoadingId = null
                            },
                            onArchive = {
                                taskToArchive = task
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Image Preview Dialog
    if (previewPhotoUri != null) {
        Dialog(onDismissRequest = { previewPhotoUri = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fotoğraf Kanıtı Önizleme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { previewPhotoUri = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(previewPhotoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Görev Kanıt Fotoğrafı",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 360.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth().bouncyClick { previewPhotoUri = null }) {
                        PremiumButton(
                            text = "Kapat",
                            onClick = { previewPhotoUri = null },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovalTaskCard(
    student: User?,
    task: Task,
    tabType: Int, // 0: Pending, 1: Approved, 2: Rejected
    isLoading: Boolean,
    onPhotoClick: (String) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onArchive: () -> Unit
) {
    PremiumCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Student Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    StudentAvatar(
                        name = student?.name ?: "Öğrenci",
                        avatarUri = student?.avatarUri,
                        size = 38.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = student?.name ?: "Öğrenci",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = student?.educationLevel?.displayName ?: "LGS & YKS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (tabType) {
                        0 -> MaterialTheme.colorScheme.primaryContainer
                        1 -> Color(0xFF059669).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = when (tabType) {
                            0 -> "İnceleme Bekliyor"
                            1 -> "Onaylandı (+${task.xpReward} XP)"
                            else -> "Düzeltme Bekliyor"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (tabType) {
                            0 -> MaterialTheme.colorScheme.primary
                            1 -> Color(0xFF059669)
                            else -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Task Details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${task.subject} • ${task.topic}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Text(
                    text = "Hedef Soru: ${task.targetQuestionCount} | Çözülen: ${task.completedQuestionCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Photo Proof Thumbnail if available
            if (!task.photoProofUri.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onPhotoClick(task.photoProofUri!!) }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(task.photoProofUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Kanıt Fotoğrafı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(bottomStart = 10.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Büyüt", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fotoğraf kanıtı bulunmuyor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action Buttons for Pending Tab (0) or Approved Tab (1)
            if (tabType == 0) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).bouncyClick(onReject)) {
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reddet")
                            }
                        }

                        Box(modifier = Modifier.weight(1f).bouncyClick(onApprove)) {
                            Button(
                                onClick = onApprove,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Onayla & XP Ver")
                            }
                        }
                    }
                }
            } else if (tabType == 1) {
                // Single Archive / Clean Button for Approved Tasks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onArchive,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Listeden Kaldır / Arşivle", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
