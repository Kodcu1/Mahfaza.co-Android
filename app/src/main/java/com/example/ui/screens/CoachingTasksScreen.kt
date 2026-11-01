package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.AvatarUtils
import com.example.util.CurriculumDictionary
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachingTasksScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCoach = user?.role == UserRole.COACH

    if (isCoach) {
        CoachTasksView(viewModel)
    } else {
        StudentStudySubmissionView(viewModel)
    }
}

@Composable
fun CoachTasksView(viewModel: MainViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val assignedStudents = allStudents.filter { user?.assignedStudents?.contains(it.id) == true }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedStudentId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        SectionHeader(title = "Koçluk Görev Atama Merkezi")

        if (assignedStudents.isEmpty()) {
            EmptyState(
                message = "Henüz size atanmış bir öğrenci bulunmuyor. Öğrenci eklemek için Koç Kodunuzu öğrencilerinizle paylaşın.",
                icon = Icons.Default.People
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(assignedStudents) { student ->
                    StudentTaskSummaryCard(
                        student = student,
                        onAddTask = {
                            selectedStudentId = student.id
                            showAddTaskDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog && selectedStudentId != null) {
        AddTaskDialog(
            studentName = assignedStudents.find { it.id == selectedStudentId }?.name ?: "",
            level = assignedStudents.find { it.id == selectedStudentId }?.educationLevel,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, subject, topic, qCount, priority, day ->
                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    subject = subject,
                    topic = topic,
                    targetQuestionCount = qCount,
                    priority = priority,
                    dayOfWeek = day,
                    xpReward = (qCount / 2).coerceAtLeast(10)
                )
                viewModel.addTask(selectedStudentId!!, newTask)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun StudentStudySubmissionView(viewModel: MainViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    var showAddSubmissionDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSubmissionDialog = true },
                containerColor = PrimaryNavy,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null) },
                text = { Text("Yeni Çalışma Ekle", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            SectionHeader(title = "Çalışmalarım ve Fotoğraflı Kanıtlar")

            if (tasks.isEmpty()) {
                EmptyState(
                    message = "Henüz bir çalışma veya görev kaydınız bulunmuyor. Yeni bir çalışma ekleyip fotoğraflı kanıt yükleyebilirsiniz.",
                    icon = Icons.Default.Assignment
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tasks.sortedByDescending { it.id }, key = { it.id }) { task ->
                        StudentStudyCard(task = task)
                    }
                }
            }
        }
    }

    if (showAddSubmissionDialog && user != null) {
        AddStudySubmissionDialog(
            user = user!!,
            onDismiss = { showAddSubmissionDialog = false },
            onSubmit = { examCat, subject, topic, subtopic, bookName, workType, qCount, cCount, wCount, photoUri, note ->
                val fullTopic = listOfNotNull(
                    topic,
                    subtopic.takeIf { it.isNotBlank() },
                    bookName.takeIf { it.isNotBlank() }?.let { "Kitap: $it" }
                ).joinToString(" / ")

                val detailNote = listOfNotNull(
                    if (cCount != null || wCount != null) "D: ${cCount ?: 0} / Y: ${wCount ?: 0}" else null,
                    note.takeIf { it.isNotBlank() }
                ).joinToString(" • ")

                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = "[$examCat] $subject: $topic ($workType)",
                    subject = subject,
                    topic = if (detailNote.isNotBlank()) "$fullTopic ($detailNote)" else fullTopic,
                    targetQuestionCount = qCount,
                    completedQuestionCount = qCount,
                    isCompleted = true,
                    priority = TaskPriority.HIGH,
                    xpReward = (qCount / 2).coerceAtLeast(15),
                    dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
                    photoProofUri = photoUri,
                    isApproved = false
                )
                viewModel.addTask(user!!.id, newTask)
                Toast.makeText(context, "Çalışmanız ve fotoğraflı kanıtınız koç onayına gönderildi!", Toast.LENGTH_LONG).show()
                showAddSubmissionDialog = false
            }
        )
    }
}

@Composable
fun StudentStudyCard(task: Task) {
    PremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = if (task.isApproved) SoftSuccessSurface else SoftGoldSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (task.isApproved) Icons.Default.Verified else Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (task.isApproved) Success else Gold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${task.subject} • ${task.targetQuestionCount} Soru Çözümü",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (task.isApproved) {
                    PremiumStatusChip(text = "Onaylandı (+${task.xpReward} XP)", color = Success, surfaceColor = SoftSuccessSurface)
                } else if (task.photoProofUri != null) {
                    PremiumStatusChip(text = "Onay Bekliyor", color = Gold, surfaceColor = SoftGoldSurface)
                } else {
                    PremiumStatusChip(text = "Görev", color = PrimaryNavy, surfaceColor = SoftGoldSurface)
                }
            }

            if (task.topic.isNotBlank()) {
                Text(
                    text = "Konu Detayı: ${task.topic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryText
                )
            }

            if (task.photoProofUri != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(task.photoProofUri),
                        contentDescription = "Fotoğraf Kanıtı",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun StudentTaskSummaryCard(student: User, onAddTask: () -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(student.name.take(1), fontWeight = FontWeight.Bold, color = PrimaryNavy)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(student.educationLevel?.displayName ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onAddTask) {
                Icon(Icons.Default.AddCircle, contentDescription = "Görev Ekle", tint = PrimaryNavy, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddStudySubmissionDialog(
    user: User,
    onDismiss: () -> Unit,
    onSubmit: (
        examCat: String,
        subject: String,
        topic: String,
        subtopic: String,
        bookName: String,
        workType: String,
        qCount: Int,
        cCount: Int?,
        wCount: Int?,
        photoUri: String,
        note: String
    ) -> Unit
) {
    val context = LocalContext.current
    val examCategories = remember(user.educationLevel, user.examTracks) { CurriculumDictionary.getExamCategoriesForUser(user.educationLevel, user.examTracks) }

    var selectedExamCategory by remember { mutableStateOf(examCategories.firstOrNull() ?: "TYT") }
    val subjects = remember(selectedExamCategory) { CurriculumDictionary.getSubjectsForExamCategory(selectedExamCategory) }

    var selectedSubjectName by remember { mutableStateOf(subjects.firstOrNull()?.subject ?: "Türkçe") }
    val topics = remember(selectedExamCategory, selectedSubjectName) { CurriculumDictionary.getTopicsForSubject(selectedExamCategory, selectedSubjectName) }

    var selectedTopicName by remember { mutableStateOf(topics.firstOrNull()?.topicName ?: "Genel Konu") }
    var freeTopicText by remember { mutableStateOf("") }
    var subtopicText by remember { mutableStateOf("") }
    var bookNameText by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("Soru Çözümü") }
    var questionCountText by remember { mutableStateOf("50") }
    var correctCountText by remember { mutableStateOf("") }
    var wrongCountText by remember { mutableStateOf("") }
    var studentNoteText by remember { mutableStateOf("") }
    var photoProofUri by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = AvatarUtils.saveBitmapToInternalStorage(context, bitmap, "proof_cam_${UUID.randomUUID()}")
            if (savedPath != null) {
                photoProofUri = savedPath
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = AvatarUtils.saveAvatarToInternalStorage(context, uri, "proof_gal_${UUID.randomUUID()}")
            if (savedPath != null) {
                photoProofUri = savedPath
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Yeni Çalışma Gönderimi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryNavy)

                // 1. Sınav Kategorisi Seçimi
                Text("1. Sınav Türü", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                val allowedCategories = CurriculumDictionary.getExamCategoriesForUser(user.educationLevel, user.examTracks)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allowedCategories.forEach { category ->
                        FilterChip(
                            selected = selectedExamCategory == category,
                            onClick = {
                                selectedExamCategory = category
                                val newSubjects = CurriculumDictionary.getSubjectsForExamCategory(category)
                                selectedSubjectName = newSubjects.firstOrNull()?.subject ?: ""
                                val newTopics = CurriculumDictionary.getTopicsForSubject(category, selectedSubjectName)
                                selectedTopicName = newTopics.firstOrNull()?.topicName ?: ""
                            },
                            label = { Text(category, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 2. Ders Seçimi
                Text("2. Ders Seçimi ($selectedExamCategory)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { item ->
                        FilterChip(
                            selected = selectedSubjectName == item.subject,
                            onClick = {
                                selectedSubjectName = item.subject
                                val newTopics = CurriculumDictionary.getTopicsForSubject(selectedExamCategory, item.subject)
                                selectedTopicName = newTopics.firstOrNull()?.topicName ?: ""
                            },
                            label = { Text(item.subject, fontSize = 12.sp) }
                        )
                    }
                }

                // 3. Konu & Alt Konu Seçimi
                Text("3. Konu Seçimi", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    topics.take(8).forEach { item ->
                        FilterChip(
                            selected = selectedTopicName == item.topicName,
                            onClick = { selectedTopicName = item.topicName },
                            label = { Text(item.topicName, fontSize = 12.sp) }
                        )
                    }
                }

                if (selectedTopicName == "Diğer / Serbest Konu") {
                    OutlinedTextField(
                        value = freeTopicText,
                        onValueChange = { freeTopicText = it },
                        label = { Text("Özel Konu Adı *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = subtopicText,
                    onValueChange = { subtopicText = it },
                    label = { Text("Alt Konu / Detay (İsteğe Bağlı)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bookNameText,
                    onValueChange = { bookNameText = it },
                    label = { Text("Kaynak Kitap Adı (İsteğe Bağlı)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Çalışma Türü & Çözülen Soru Miktarı
                Text("4. Çalışma Türü & Miktarı", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Soru Çözümü", "Konu Anlatımı", "Tekrar", "Deneme Analizi", "Yanlış Analizi").forEach { type ->
                        FilterChip(
                            selected = workType == type,
                            onClick = { workType = type },
                            label = { Text(type, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = questionCountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) questionCountText = it },
                    label = { Text("Çözülen Soru Sayısı *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Live XP Preview Card
                val qCountInt = questionCountText.toIntOrNull() ?: 0
                val estimatedStudyXp = com.example.util.XpPolicy.calculateStudyXp(qCountInt)
                com.example.ui.components.XpPreviewCard(
                    proposedXp = estimatedStudyXp,
                    requiresCoachApproval = true,
                    subtitle = "Koçun çalışmanı ve fotoğraf kanıtını onayladığında aktarılır."
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = correctCountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) correctCountText = it },
                        label = { Text("Doğru Soru") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wrongCountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) wrongCountText = it },
                        label = { Text("Yanlış Soru") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = studentNoteText,
                    onValueChange = { studentNoteText = it },
                    label = { Text("Öğrenci Notu / Açıklama (İsteğe Bağlı)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Fotoğraflı Kanıt (Zorunlu)
                Text("5. Fotoğraflı Kanıt (Zorunlu)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                if (photoProofUri != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp))) {
                        Image(
                            painter = rememberAsyncImagePainter(photoProofUri),
                            contentDescription = "Kanıt Önizleme",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                        ) {
                            IconButton(
                                onClick = { cameraLauncher.launch(null) },
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Değiştir", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { photoProofUri = null },
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Kaldır", tint = Color.White)
                            }
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fotoğraf Çek", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galeriden Seç", fontSize = 12.sp)
                        }
                    }
                }

                // Validation logic
                val finalTopic = if (selectedTopicName == "Diğer / Serbest Konu") freeTopicText else selectedTopicName
                val qCount = questionCountText.toIntOrNull() ?: 0
                val cCount = correctCountText.toIntOrNull()
                val wCount = wrongCountText.toIntOrNull()

                val isCorrectWrongValid = if (cCount != null && wCount != null) {
                    (cCount + wCount) <= qCount
                } else true

                val isValid = selectedSubjectName.isNotBlank() &&
                        finalTopic.isNotBlank() &&
                        qCount > 0 &&
                        photoProofUri != null &&
                        isCorrectWrongValid

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!isCorrectWrongValid) {
                                Toast.makeText(context, "Doğru ve yanlış soru sayısı toplamı çözülen soru sayısını geçemez!", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            if (isValid) {
                                isSubmitting = true
                                onSubmit(
                                    selectedExamCategory,
                                    selectedSubjectName,
                                    finalTopic,
                                    subtopicText,
                                    bookNameText,
                                    workType,
                                    qCount,
                                    cCount,
                                    wCount,
                                    photoProofUri!!,
                                    studentNoteText
                                )
                            } else {
                                Toast.makeText(context, "Lütfen tüm zorunlu alanları ve fotoğraflı kanıtı tamamlayın.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = isValid && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Koç Onayına Gönder", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(
    studentName: String,
    level: EducationLevel?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, TaskPriority, Int) -> Unit
) {
    val subjects = remember(level) { CurriculumDictionary.getSubjectsForLevel(level) }
    var selectedSubjectName by remember { mutableStateOf(subjects.firstOrNull()?.subject ?: "Matematik") }
    val topics = remember(level, selectedSubjectName) { CurriculumDictionary.getTopicsForSubject(if (level == EducationLevel.LGS_8TH) "LGS" else "TYT", selectedSubjectName) }

    var selectedTopicName by remember { mutableStateOf(topics.firstOrNull()?.topicName ?: "Genel Konu") }
    var title by remember { mutableStateOf("") }
    var questionCount by remember { mutableStateOf("50") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var dayOfWeek by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "$studentName İçin Görev Atama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryNavy)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Görev Tanımı") },
                    placeholder = { Text("Örn: 2 Branş Denemesi + Analiz") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Ders Seçimi", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    subjects.forEach { item ->
                        FilterChip(
                            selected = selectedSubjectName == item.subject,
                            onClick = {
                                selectedSubjectName = item.subject
                                selectedTopicName = CurriculumDictionary.getTopicsForSubject(if (level == EducationLevel.LGS_8TH) "LGS" else "TYT", item.subject).firstOrNull()?.topicName ?: ""
                            },
                            label = { Text(item.subject, fontSize = 12.sp) }
                        )
                    }
                }

                Text("Konu Seçimi", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    topics.take(6).forEach { item ->
                        FilterChip(
                            selected = selectedTopicName == item.topicName,
                            onClick = { selectedTopicName = item.topicName },
                            label = { Text(item.topicName, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = questionCount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) questionCount = it },
                    label = { Text("Soru Hedefi") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Öncelik", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.name) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalTitle = if (title.isNotBlank()) title else "$selectedSubjectName $selectedTopicName Soru Çözümü"
                            onConfirm(finalTitle, selectedSubjectName, selectedTopicName, questionCount.toIntOrNull() ?: 50, priority, dayOfWeek)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                    ) {
                        Text("Görev Ata")
                    }
                }
            }
        }
    }
}
