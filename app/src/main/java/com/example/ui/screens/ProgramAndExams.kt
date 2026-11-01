package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ExamResult
import com.example.viewmodel.MainViewModel
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyProgramScreen(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val isCoach = currentUser?.role == com.example.model.UserRole.COACH

    val assignedStudents = remember(allStudents, currentUser) {
        if (isCoach) {
            allStudents.filter { currentUser?.assignedStudents?.contains(it.id) == true }.ifEmpty { allStudents }
        } else {
            emptyList()
        }
    }

    var selectedStudentId by remember {
        mutableStateOf(if (isCoach) assignedStudents.firstOrNull()?.id ?: "s2" else currentUser?.id ?: "")
    }

    // Keep selectedStudentId valid
    LaunchedEffect(assignedStudents) {
        if (isCoach && (selectedStudentId.isEmpty() || assignedStudents.none { it.id == selectedStudentId })) {
            assignedStudents.firstOrNull()?.let { selectedStudentId = it.id }
        }
    }

    val targetStudent = if (isCoach) {
        allStudents.find { it.id == selectedStudentId } ?: currentUser
    } else {
        currentUser
    }

    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val studentTasks = remember(allTasks, selectedStudentId, currentUser) {
        val targetId = if (isCoach) selectedStudentId else (currentUser?.id ?: "")
        allTasks.filter { task ->
            // In entities, tasks belong to userId
            // We can check through repository or match tasks
            true
        }
    }

    // Direct student tasks stream from ViewModel
    var directTasks by remember { mutableStateOf<List<com.example.model.Task>>(emptyList()) }
    val effectiveUserId = if (isCoach) selectedStudentId else (currentUser?.id ?: "")
    LaunchedEffect(effectiveUserId) {
        if (effectiveUserId.isNotBlank()) {
            viewModel.getStudentTasks(effectiveUserId).collect { tasks ->
                directTasks = tasks
            }
        }
    }

    val days = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")
    var selectedDay by remember { mutableStateOf(1) } // 1 to 7

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<com.example.model.Task?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    taskToEdit = null
                    showAddTaskDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Görev Ekle") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header with PDF Export Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Haftalık Program",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Pazartesi - Pazar akademik çalışma çizelgesi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        targetStudent?.let { student ->
                            com.example.util.PdfExporter.exportWeeklyPlan(context, student, directTasks)
                        } ?: run {
                            Toast.makeText(context, "Öğrenci bilgisi bulunamadı.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF İndir", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coach Student Selector
            if (isCoach && assignedStudents.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Öğrenci Seçimi:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ScrollableTabRow(
                            selectedTabIndex = assignedStudents.indexOfFirst { it.id == selectedStudentId }.coerceAtLeast(0),
                            edgePadding = 0.dp,
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            divider = {}
                        ) {
                            assignedStudents.forEach { student ->
                                Tab(
                                    selected = selectedStudentId == student.id,
                                    onClick = { selectedStudentId = student.id },
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            com.example.util.StudentAvatar(name = student.name, avatarUri = student.avatarUri, size = 20.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(student.name, fontWeight = if (selectedStudentId == student.id) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Days Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedDay - 1,
                edgePadding = 0.dp,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                divider = {}
            ) {
                days.forEachIndexed { index, day ->
                    val dayTaskCount = directTasks.count { it.dayOfWeek == index + 1 }
                    Tab(
                        selected = selectedDay == index + 1,
                        onClick = { selectedDay = index + 1 },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(day, fontWeight = if (selectedDay == index + 1) FontWeight.Bold else FontWeight.Normal)
                                if (dayTaskCount > 0) {
                                    Text(
                                        "$dayTaskCount görev",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selectedDay == index + 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val dayTasks = directTasks.filter { it.dayOfWeek == selectedDay }

            if (dayTasks.isEmpty()) {
                EmptyState(
                    message = "${days[selectedDay - 1]} günü için planlanmış bir görev bulunmuyor. Yeni bir görev eklemek için '+' butonuna basabilirsiniz.",
                    icon = Icons.Default.EventNote
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(dayTasks, key = { it.id }) { task ->
                        WeeklyTaskItemCard(
                            task = task,
                            onToggle = { viewModel.toggleTask(task.id) },
                            onEdit = {
                                taskToEdit = task
                                showAddTaskDialog = true
                            },
                            onDelete = {
                                viewModel.deleteTask(task.id)
                                Toast.makeText(context, "${task.title} silindi.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        WeeklyTaskDialog(
            initialTask = taskToEdit,
            initialDay = selectedDay,
            studentName = targetStudent?.name ?: "Öğrenci",
            onDismiss = {
                showAddTaskDialog = false
                taskToEdit = null
            },
            onConfirm = { task ->
                if (taskToEdit == null) {
                    viewModel.addTask(effectiveUserId, task)
                    Toast.makeText(context, "Görev programa eklendi.", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateTask(effectiveUserId, task)
                    Toast.makeText(context, "Görev güncellendi.", Toast.LENGTH_SHORT).show()
                }
                showAddTaskDialog = false
                taskToEdit = null
            }
        )
    }
}

@Composable
fun WeeklyTaskItemCard(
    task: com.example.model.Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    PremiumCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (task.isApproved) {
                        Surface(
                            color = androidx.compose.ui.graphics.Color(0xFF059669).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Onaylandı",
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color(0xFF059669),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${task.subject} • ${task.topic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Hedef: ${task.targetQuestionCount} Soru | Ödül: ${task.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyTaskDialog(
    initialTask: com.example.model.Task?,
    initialDay: Int,
    studentName: String,
    onDismiss: () -> Unit,
    onConfirm: (com.example.model.Task) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var subject by remember { mutableStateOf(initialTask?.subject ?: "Matematik") }
    var topic by remember { mutableStateOf(initialTask?.topic ?: "") }
    var questionCount by remember { mutableStateOf(initialTask?.targetQuestionCount?.toString() ?: "40") }
    var xpReward by remember { mutableStateOf(initialTask?.xpReward?.toString() ?: "20") }
    var dayOfWeek by remember { mutableStateOf(initialTask?.dayOfWeek ?: initialDay) }
    var priority by remember { mutableStateOf(initialTask?.priority ?: com.example.model.TaskPriority.MEDIUM) }

    val days = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTask == null) "$studentName İçin Görev Ekle" else "Görevi Düzenle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Görev Başlığı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Ders") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Konu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = questionCount,
                        onValueChange = { questionCount = it },
                        label = { Text("Soru Hedefi") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = xpReward,
                        onValueChange = { xpReward = it },
                        label = { Text("XP Ödülü") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Live XP Preview Card for Task Creation
                val taskXpVal = xpReward.toIntOrNull() ?: 20
                val estimatedTaskXp = com.example.util.XpPolicy.calculateTaskXp(taskXpVal)
                com.example.ui.components.XpPreviewCard(
                    proposedXp = estimatedTaskXp,
                    requiresCoachApproval = true,
                    subtitle = "Öğrenci görevi tamamlayıp onay aldığında hesabına eklenecektir."
                )

                Text("Haftanın Günü", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                ScrollableTabRow(
                    selectedTabIndex = dayOfWeek - 1,
                    edgePadding = 0.dp,
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    divider = {}
                ) {
                    days.forEachIndexed { idx, d ->
                        Tab(
                            selected = dayOfWeek == idx + 1,
                            onClick = { dayOfWeek = idx + 1 },
                            text = { Text(d.take(3)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && subject.isNotBlank()) {
                        val qCount = questionCount.toIntOrNull()?.coerceAtLeast(0) ?: 40
                        val xp = xpReward.toIntOrNull()?.coerceAtLeast(0) ?: (qCount / 2)
                        onConfirm(
                            com.example.model.Task(
                                id = initialTask?.id ?: java.util.UUID.randomUUID().toString(),
                                title = title.trim(),
                                subject = subject.trim(),
                                topic = topic.trim().ifEmpty { "Genel Tekrar" },
                                targetQuestionCount = qCount,
                                completedQuestionCount = initialTask?.completedQuestionCount ?: 0,
                                isCompleted = initialTask?.isCompleted ?: false,
                                priority = priority,
                                xpReward = xp,
                                dayOfWeek = dayOfWeek,
                                photoProofUri = initialTask?.photoProofUri,
                                isApproved = initialTask?.isApproved ?: false
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && subject.isNotBlank()
            ) {
                Text(if (initialTask == null) "Ekle" else "Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun ExamResultsScreen(viewModel: MainViewModel) {
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var showAddExamDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExamDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Deneme")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            SectionHeader(title = "Deneme Takibi")
            
            if (exams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henüz deneme kaydı bulunmuyor. Yeni deneme eklemek için '+' butonuna basabilirsiniz.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(exams.reversed()) { exam ->
                        ExamResultItem(exam = exam)
                    }
                }
            }
        }
    }

    if (showAddExamDialog) {
        val level = currentUser?.let { u ->
            when {
                u.examTracks.contains("YKS") -> com.example.model.EducationLevel.YKS_TYT_AYT
                u.examTracks.contains("LGS") -> com.example.model.EducationLevel.LGS_8TH
                u.examTracks.contains("KPSS") -> com.example.model.EducationLevel.KPSS
                u.educationLevel != null -> u.educationLevel
                else -> com.example.model.EducationLevel.YKS_TYT_AYT
            }
        } ?: com.example.model.EducationLevel.YKS_TYT_AYT
        AddExamDialog(
            level = level,
            onDismiss = { showAddExamDialog = false },
            onConfirm = { newExam ->
                currentUser?.let { user ->
                    viewModel.addExam(user.id, newExam)
                }
                showAddExamDialog = false
            }
        )
    }
}

@Composable
fun ExamResultItem(exam: ExamResult) {
    PremiumCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = exam.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = exam.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = "${exam.subjectResults.sumOf { it.net }.let { String.format("%.2f", it) }} Net",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                exam.subjectResults.forEach { subject ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = subject.subjectName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(
                            text = "D: ${subject.correct} Y: ${subject.wrong} B: ${subject.blank} Net: ${subject.net}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
