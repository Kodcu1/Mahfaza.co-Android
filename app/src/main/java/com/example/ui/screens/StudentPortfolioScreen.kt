package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.EducationLevel
import com.example.model.PackageCatalog
import com.example.model.User
import com.example.ui.components.EmptyState
import com.example.ui.components.PremiumButton
import com.example.ui.components.PremiumCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldAccent
import com.example.util.StudentAvatar
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPortfolioScreen(
    viewModel: MainViewModel,
    onStudentClick: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    
    val assignedStudentIds = currentUser?.assignedStudents ?: emptyList()
    val assignedStudents = allStudents.filter { assignedStudentIds.contains(it.id) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevelFilter by remember { mutableStateOf<EducationLevel?>(null) }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    val filteredStudents = assignedStudents.filter { student ->
        val matchesQuery = student.name.contains(searchQuery, ignoreCase = true) ||
                student.email.contains(searchQuery, ignoreCase = true)
        val matchesLevel = selectedLevelFilter == null || student.educationLevel == selectedLevelFilter
        matchesQuery && matchesLevel
    }

    val studentCapacity = currentUser?.studentCapacity ?: 0
    val canAdd = assignedStudents.size < studentCapacity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Öğrenci Portföyü",
            actionText = "+ Yeni Öğrenci",
            onActionClick = {
                if (canAdd) {
                    showAddStudentDialog = true
                } else {
                    Toast.makeText(
                        context,
                        "Kapasiteniz doldu ($studentCapacity/$studentCapacity). Lütfen paketinizi yükseltin.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )

        // Capacity and Status Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Aktif Portföy Kapasitesi",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${assignedStudents.size} / $studentCapacity Öğrenci Kayıtlı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (canAdd) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
                com.example.ui.components.AnimatedProgressIndicator(
                    progress = if (studentCapacity > 0) (assignedStudents.size.toFloat() / studentCapacity) else 0f,
                    modifier = Modifier.width(100.dp),
                    color = if (canAdd) com.example.ui.theme.TealAccent else com.example.ui.theme.Danger
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Öğrenci adı veya e-posta ile ara...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Temizle")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Level Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedLevelFilter == null,
                onClick = { selectedLevelFilter = null },
                label = { Text("Tümü (${assignedStudents.size})") }
            )
            FilterChip(
                selected = selectedLevelFilter == EducationLevel.LGS_8TH,
                onClick = { selectedLevelFilter = EducationLevel.LGS_8TH },
                label = { Text("LGS") }
            )
            FilterChip(
                selected = selectedLevelFilter == EducationLevel.YKS_TYT_AYT,
                onClick = { selectedLevelFilter = EducationLevel.YKS_TYT_AYT },
                label = { Text("YKS") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Students List
        if (filteredStudents.isEmpty()) {
            EmptyState(
                message = if (assignedStudents.isEmpty())
                    "Henüz atanmış bir öğrenciniz bulunmuyor. Sağ üstteki '+ Yeni Öğrenci' butonundan ekleyebilirsiniz."
                else
                    "Arama kriterlerine uygun öğrenci bulunamadı.",
                icon = Icons.Default.PersonSearch
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredStudents, key = { it.id }) { student ->
                    StudentCard(
                        student = student,
                        viewModel = viewModel,
                        onClick = { onStudentClick(student.id) },
                        onChatClick = { onNavigateToChat(student.id) }
                    )
                }
            }
        }
    }

    if (showAddStudentDialog) {
        AddStudentDialog(
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { name, email, level, packageId ->
                viewModel.createAndAssignStudent(name, email, level, packageId) { success ->
                    if (success) {
                        Toast.makeText(context, "$name başarıyla portföye eklendi.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Öğrenci eklenemedi. Kapasite sınırını kontrol edin.", Toast.LENGTH_LONG).show()
                    }
                }
                showAddStudentDialog = false
            }
        )
    }
}

@Composable
fun StudentCard(
    student: User,
    viewModel: MainViewModel,
    onClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val studentTasks by viewModel.getStudentTasks(student.id).collectAsStateWithLifecycle(emptyList())
    val studentExams by viewModel.getStudentExams(student.id).collectAsStateWithLifecycle(emptyList())

    val completedTasksCount = studentTasks.count { it.isCompleted }
    val totalTasksCount = studentTasks.size
    val lastExam = studentExams.firstOrNull()
    val lastExamNet = lastExam?.subjectResults?.sumOf { it.net }

    PremiumCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StudentAvatar(name = student.name, avatarUri = student.avatarUri, size = 48.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = student.educationLevel?.displayName ?: "Seviye Belirtilmedi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onChatClick) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = "Mesaj Gönder",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricSnippet(
                    label = "Görevler",
                    value = "$completedTasksCount/$totalTasksCount",
                    color = MaterialTheme.colorScheme.primary
                )
                MetricSnippet(
                    label = "Son Deneme",
                    value = if (lastExamNet != null) "%.2f Net".format(lastExamNet) else "Yok",
                    color = if (lastExamNet != null) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                MetricSnippet(
                    label = "XP",
                    value = "${student.currentXp}",
                    color = GoldAccent
                )
                MetricSnippet(
                    label = "Seri",
                    value = "${student.streak} Gün",
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "360° Profil Detayı",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MetricSnippet(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, level: EducationLevel, packageId: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(EducationLevel.LGS_8TH) }
    var selectedPackageId by remember { mutableStateOf(PackageCatalog.STUDENT_STANDART.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Öğrenci Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Öğrenci Adı Soyadı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-posta Adresi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Eğitim Seviyesi", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedLevel == EducationLevel.LGS_8TH,
                        onClick = { selectedLevel = EducationLevel.LGS_8TH },
                        label = { Text("LGS (8. Sınıf)") }
                    )
                    FilterChip(
                        selected = selectedLevel == EducationLevel.YKS_TYT_AYT,
                        onClick = { selectedLevel = EducationLevel.YKS_TYT_AYT },
                        label = { Text("YKS (TYT-AYT)") }
                    )
                }
                Text("Öğrenci Paketi", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedPackageId == PackageCatalog.STUDENT_STANDART.id,
                        onClick = { selectedPackageId = PackageCatalog.STUDENT_STANDART.id },
                        label = { Text("Standart") }
                    )
                    FilterChip(
                        selected = selectedPackageId == PackageCatalog.STUDENT_PLUS.id,
                        onClick = { selectedPackageId = PackageCatalog.STUDENT_PLUS.id },
                        label = { Text("Plus") }
                    )
                    FilterChip(
                        selected = selectedPackageId == PackageCatalog.STUDENT_PRO.id,
                        onClick = { selectedPackageId = PackageCatalog.STUDENT_PRO.id },
                        label = { Text("Pro") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        onConfirm(name, email, selectedLevel, selectedPackageId)
                    }
                },
                enabled = name.isNotBlank() && email.isNotBlank()
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
