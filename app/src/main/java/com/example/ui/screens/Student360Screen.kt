package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.GoldAccent
import com.example.util.AvatarUtils
import com.example.util.CurriculumDictionary
import com.example.util.ExamUtils
import com.example.util.PdfExporter
import com.example.util.StudentAvatar
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Student360Screen(
    viewModel: MainViewModel,
    studentId: String,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    var student by remember { mutableStateOf<User?>(null) }
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()

    LaunchedEffect(studentId, allStudents) {
        student = allStudents.find { it.id == studentId } ?: viewModel.getStudentById(studentId)
    }

    val studentExams by viewModel.getStudentExams(studentId).collectAsStateWithLifecycle(emptyList())
    val studentTasks by viewModel.getStudentTasks(studentId).collectAsStateWithLifecycle(emptyList())
    val studentXpTransactions by viewModel.getStudentXPTransactions(studentId).collectAsStateWithLifecycle(emptyList())
    val studentMeetings by viewModel.getStudentMeetings(studentId).collectAsStateWithLifecycle(emptyList())

    var selectedSection by remember { mutableStateOf(0) } // 0: Denemeler, 1: Görevler, 2: XP, 3: Veli Görüşmeleri
    var showXpDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddMeetingDialog by remember { mutableStateOf(false) }

    // Photo picker for student avatar
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = AvatarUtils.saveAvatarToInternalStorage(context, uri, studentId)
            if (savedPath != null) {
                viewModel.updateUserAvatar(studentId, savedPath)
                Toast.makeText(context, "Profil fotoğrafı başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val studentVal = student!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Card with Photo and Identity
        PremiumCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        StudentAvatar(
                            name = studentVal.name,
                            avatarUri = studentVal.avatarUri,
                            size = 72.dp
                        )
                        SmallFloatingActionButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Fotoğraf Yükle", modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = studentVal.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = studentVal.educationLevel?.displayName ?: "Seviye Belirtilmedi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Paket: " + (PackageCatalog.getPackageById(studentVal.packageId)?.name ?: "Standart"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { onNavigateToChat(studentId) }) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Mesaj Gönder", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ScoreStat("Mevcut XP", "${studentVal.currentXp}", GoldAccent)
                    ScoreStat("Lifetime XP", "${studentVal.lifetimeXp}", MaterialTheme.colorScheme.primary)
                    ScoreStat("Seri", "${studentVal.streak} Gün", MaterialTheme.colorScheme.secondary)
                    ScoreStat(
                        "Görev Oranı",
                        "${studentTasks.count { it.isCompleted }}/${studentTasks.size}",
                        Color(0xFF059669)
                    )
                }
            }
        }

        // Action Buttons Row: PDF Export & Chat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { PdfExporter.exportStudent360(context, studentVal, studentExams) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("360° PDF Raporu")
            }

            OutlinedButton(
                onClick = { showXpDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AddModerator, contentDescription = null, modifier = Modifier.size(18.dp), tint = GoldAccent)
                Spacer(modifier = Modifier.width(6.dp))
                Text("XP Yönet")
            }
        }

        // Module Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(selected = selectedSection == 0, onClick = { selectedSection = 0 }, text = { Text("Denemeler (${studentExams.size})") })
            Tab(selected = selectedSection == 1, onClick = { selectedSection = 1 }, text = { Text("Görevler (${studentTasks.size})") })
            Tab(selected = selectedSection == 2, onClick = { selectedSection = 2 }, text = { Text("XP Geçmişi") })
            Tab(selected = selectedSection == 3, onClick = { selectedSection = 3 }, text = { Text("Veli (${studentMeetings.size})") })
        }

        // Content according to section
        when (selectedSection) {
            0 -> {
                // Denemeler Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Akademik Deneme Takibi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { showAddExamDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deneme Ekle")
                    }
                }

                if (studentExams.isNotEmpty()) {
                    PremiumCard {
                        AcademicTrendChart(studentExams)
                    }
                }

                if (studentExams.isEmpty()) {
                    EmptyState(message = "Öğrenciye ait kayıtlı deneme sonucu bulunmuyor.", icon = Icons.Default.Assessment)
                } else {
                    studentExams.forEach { exam ->
                        ExamResultItem(exam = exam)
                    }
                }
            }

            1 -> {
                // Görevler Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Atanan Haftalık Görevler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { showAddTaskDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Görev Ata")
                    }
                }

                if (studentTasks.isEmpty()) {
                    EmptyState(message = "Henüz planlanmış bir görev bulunmuyor.", icon = Icons.Default.Task)
                } else {
                    studentTasks.forEach { task ->
                        TaskItem(task = task, onToggle = { viewModel.toggleTask(task.id) })
                    }
                }
            }

            2 -> {
                // XP History Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "XP & Puan Hareketleri",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { showXpDialog = true }) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("XP Ekle/Çıkar")
                    }
                }

                if (studentXpTransactions.isEmpty()) {
                    EmptyState(message = "Henüz bir XP hareketi bulunmuyor.", icon = Icons.Default.Stars)
                } else {
                    PremiumCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            studentXpTransactions.forEach { tx ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (tx.amount > 0) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                                        contentDescription = null,
                                        tint = if (tx.amount > 0) Color(0xFF059669) else Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tx.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${if (tx.amount > 0) "+" else ""}${tx.amount} XP",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tx.amount > 0) Color(0xFF059669) else Color(0xFFDC2626)
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }

            3 -> {
                // Veli Görüşmeleri Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Veli Görüşme Kayıtları",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { showAddMeetingDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Görüşme Ekle")
                    }
                }

                if (studentMeetings.isEmpty()) {
                    EmptyState(message = "Bu öğrenci için kayıtlı veli görüşmesi bulunmuyor.", icon = Icons.Default.ContactPhone)
                } else {
                    studentMeetings.forEach { meeting ->
                        MeetingCard(
                            meeting = meeting,
                            studentAvatarUri = studentVal.avatarUri,
                            onExportPdf = {
                                PdfExporter.exportParentMeeting(context, meeting, studentVal)
                            },
                            onToggleStatus = {
                                viewModel.updateMeeting(meeting.copy(isCompleted = !meeting.isCompleted))
                            },
                            onDelete = {
                                viewModel.deleteMeeting(meeting)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showXpDialog) {
        XpManagementDialog(
            studentName = studentVal.name,
            onDismiss = { showXpDialog = false },
            onConfirm = { amount, reason ->
                viewModel.adjustXP(studentId, amount, reason)
                showXpDialog = false
            }
        )
    }

    if (showAddExamDialog) {
        AddExamDialog(
            level = studentVal.educationLevel ?: EducationLevel.LGS_8TH,
            onDismiss = { showAddExamDialog = false },
            onConfirm = { newExam ->
                viewModel.addExam(studentId, newExam)
                Toast.makeText(context, "${newExam.name} kaydedildi.", Toast.LENGTH_SHORT).show()
                showAddExamDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            studentId = studentId,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { newTask ->
                viewModel.addTask(studentId, newTask)
                Toast.makeText(context, "${newTask.title} görevi atandı.", Toast.LENGTH_SHORT).show()
                showAddTaskDialog = false
            }
        )
    }

    if (showAddMeetingDialog) {
        AddMeetingDialog(
            assignedStudents = listOf(studentVal),
            initialStudentId = studentId,
            onDismiss = { showAddMeetingDialog = false },
            onConfirm = { meeting ->
                viewModel.addMeeting(meeting)
                Toast.makeText(context, "Veli görüşmesi kaydedildi.", Toast.LENGTH_SHORT).show()
                showAddMeetingDialog = false
            }
        )
    }
}

@Composable
fun AddExamDialog(
    level: EducationLevel,
    onDismiss: () -> Unit,
    onConfirm: (ExamResult) -> Unit
) {
    var examName by remember { mutableStateOf("") }
    var yksSubCategory by remember { mutableStateOf("TYT") } // "TYT" or "AYT"
    var isAlternatifFelsefe by remember { mutableStateOf(false) }
    var aytTrackFilter by remember { mutableStateOf("ALL") } // "ALL", "SAY", "EA", "SOZ"

    val allCategorySubjects = remember(level, yksSubCategory, isAlternatifFelsefe) {
        when (level) {
            EducationLevel.LGS_8TH -> ExamUtils.getLgsSubjects()
            EducationLevel.KPSS -> ExamUtils.getKpssSubjects()
            EducationLevel.YKS_TYT_AYT -> {
                if (yksSubCategory == "AYT") {
                    ExamUtils.getAytSubjects()
                } else {
                    ExamUtils.getTytSubjects(isAlternatifFelsefe)
                }
            }
        }
    }

    // Filter displayed subjects based on track filter (preserves scores map state across filter changes)
    val displayedSubjects = remember(allCategorySubjects, aytTrackFilter, level, yksSubCategory) {
        if (level == EducationLevel.YKS_TYT_AYT && yksSubCategory == "AYT") {
            when (aytTrackFilter) {
                "SAY" -> allCategorySubjects.filter { ExamUtils.AYT_SAYISAL_SUBJECTS.contains(it.first) }
                "EA" -> allCategorySubjects.filter { ExamUtils.AYT_ESIT_AGIRLIK_SUBJECTS.contains(it.first) }
                "SOZ" -> allCategorySubjects.filter { ExamUtils.AYT_SOZEL_SUBJECTS.contains(it.first) }
                else -> allCategorySubjects
            }
        } else {
            allCategorySubjects
        }
    }

    // Map: subjectName -> Triple(correct, wrong, blank)
    val scoresState = remember(level, yksSubCategory, isAlternatifFelsefe) {
        mutableStateMapOf<String, Triple<String, String, String>>().apply {
            allCategorySubjects.forEach { (sub, maxQ) ->
                val initC = (maxQ * 0.6).toInt()
                val initW = 2
                val initB = ExamUtils.autoCalculateBlank(maxQ, initC, initW)
                put(sub, Triple("$initC", "$initW", "$initB"))
            }
        }
    }

    val hasValidationError = displayedSubjects.any { (sub, maxQ) ->
        val score = scoresState[sub] ?: Triple("0", "0", "0")
        val c = score.first.toIntOrNull() ?: -1
        val w = score.second.toIntOrNull() ?: -1
        val b = score.third.toIntOrNull() ?: -1
        !ExamUtils.validateInputs(c, w, b, maxQ)
    }

    // Net Calculations
    val totalNet = displayedSubjects.sumOf { (sub, _) ->
        val score = scoresState[sub] ?: Triple("0", "0", "0")
        val c = score.first.toIntOrNull() ?: 0
        val w = score.second.toIntOrNull() ?: 0
        ExamUtils.calculateNet(c, w, level)
    }

    val lgsSozelNet = remember(scoresState, displayedSubjects) {
        if (level == EducationLevel.LGS_8TH) {
            ExamUtils.LGS_SOZEL_SUBJECTS.sumOf { (sub, _) ->
                val score = scoresState[sub] ?: Triple("0", "0", "0")
                val c = score.first.toIntOrNull() ?: 0
                val w = score.second.toIntOrNull() ?: 0
                ExamUtils.calculateNet(c, w, level)
            }
        } else 0.0
    }

    val lgsSayisalNet = remember(scoresState, displayedSubjects) {
        if (level == EducationLevel.LGS_8TH) {
            ExamUtils.LGS_SAYISAL_SUBJECTS.sumOf { (sub, _) ->
                val score = scoresState[sub] ?: Triple("0", "0", "0")
                val c = score.first.toIntOrNull() ?: 0
                val w = score.second.toIntOrNull() ?: 0
                ExamUtils.calculateNet(c, w, level)
            }
        } else 0.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Yeni Deneme Sınavı Ekle", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PrimaryNavy)
                Text(
                    text = "Sınav Türü: ${if (level == EducationLevel.YKS_TYT_AYT) "YKS ($yksSubCategory)" else level.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("Deneme Sınavı Adı (Örn: Özdebir 1)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // YKS Sınavlarında TYT / AYT Seçimi
                if (level == EducationLevel.YKS_TYT_AYT) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = yksSubCategory == "TYT",
                            onClick = { yksSubCategory = "TYT" },
                            label = { Text("TYT Sınavı (120 Soru)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = yksSubCategory == "AYT",
                            onClick = { yksSubCategory = "AYT" },
                            label = { Text("AYT Sınavı (80 Soru)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (yksSubCategory == "TYT") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Din Kültürü yerine Alternatif Felsefe çözüldü",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = isAlternatifFelsefe,
                                onCheckedChange = { isAlternatifFelsefe = it }
                            )
                        }
                    } else {
                        // AYT Track Filters
                        Column {
                            Text("AYT Puan Türü / Test Filtresi:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = aytTrackFilter == "ALL",
                                    onClick = { aytTrackFilter = "ALL" },
                                    label = { Text("Tüm Testler") }
                                )
                                FilterChip(
                                    selected = aytTrackFilter == "SAY",
                                    onClick = { aytTrackFilter = "SAY" },
                                    label = { Text("Sayısal") }
                                )
                                FilterChip(
                                    selected = aytTrackFilter == "EA",
                                    onClick = { aytTrackFilter = "EA" },
                                    label = { Text("Eşit Ağırlık") }
                                )
                                FilterChip(
                                    selected = aytTrackFilter == "SOZ",
                                    onClick = { aytTrackFilter = "SOZ" },
                                    label = { Text("Sözel") }
                                )
                            }
                        }
                    }
                }

                // XP Preview Card
                com.example.ui.components.XpPreviewCard(
                    proposedXp = com.example.util.XpPolicy.EXAM_XP,
                    requiresCoachApproval = false,
                    subtitle = "Sınav kaydedildiğinde anında +15 XP hesabına aktarılır."
                )

                // Toplam Canlı Net Göstergesi
                Surface(
                    color = com.example.ui.theme.SoftGoldSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, com.example.ui.theme.Gold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Hesaplanan Toplam Net:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            com.example.ui.animation.AnimatedNetScoreText(
                                targetValue = totalNet,
                                style = MaterialTheme.typography.titleMedium,
                                color = com.example.ui.theme.Gold
                            )
                        }

                        if (level == EducationLevel.LGS_8TH) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = com.example.ui.theme.Gold.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Sözel Bölüm (50 Soru): %.2f Net".format(lgsSozelNet), style = MaterialTheme.typography.labelSmall, color = com.example.ui.theme.PrimaryNavy)
                                Text("Sayısal Bölüm (40 Soru): %.2f Net".format(lgsSayisalNet), style = MaterialTheme.typography.labelSmall, color = com.example.ui.theme.PrimaryNavy)
                            }
                        }
                    }
                }

                if (hasValidationError) {
                    Text(
                        text = "Uyarı: Bir veya daha fazla branşta doğru, yanlış ve boş toplamı soru sınırını aşıyor veya negatif değer içeriyor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                displayedSubjects.forEach { (subjectName, maxQ) ->
                    val score = scoresState[subjectName] ?: Triple("0", "0", "0")
                    val correct = score.first.toIntOrNull() ?: 0
                    val wrong = score.second.toIntOrNull() ?: 0
                    val blank = score.third.toIntOrNull() ?: 0
                    val net = ExamUtils.calculateNet(correct, wrong, level)
                    val isValid = ExamUtils.validateInputs(correct, wrong, blank, maxQ)

                    Surface(
                        color = if (isValid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else com.example.ui.theme.SoftDangerSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(subjectName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PrimaryNavy)
                                Text("Net: %.2f ($maxQ Soru)".format(net), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = score.first,
                                    onValueChange = { input ->
                                        if (input.all { ch -> ch.isDigit() }) {
                                            val newC = input.toIntOrNull() ?: 0
                                            val currentW = score.second.toIntOrNull() ?: 0
                                            val autoB = ExamUtils.autoCalculateBlank(maxQ, newC, currentW)
                                            scoresState[subjectName] = Triple(input, score.second, autoB.toString())
                                        }
                                    },
                                    label = { Text("Doğru") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = score.second,
                                    onValueChange = { input ->
                                        if (input.all { ch -> ch.isDigit() }) {
                                            val currentC = score.first.toIntOrNull() ?: 0
                                            val newW = input.toIntOrNull() ?: 0
                                            val autoB = ExamUtils.autoCalculateBlank(maxQ, currentC, newW)
                                            scoresState[subjectName] = Triple(score.first, input, autoB.toString())
                                        }
                                    },
                                    label = { Text("Yanlış") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = score.third,
                                    onValueChange = { input ->
                                        if (input.all { ch -> ch.isDigit() }) {
                                            scoresState[subjectName] = score.copy(third = input)
                                        }
                                    },
                                    label = { Text("Boş") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (examName.isNotBlank() && !hasValidationError) {
                        val results = allCategorySubjects.map { (sub, _) ->

                            val score = scoresState[sub] ?: Triple("0", "0", "0")
                            val c = score.first.toIntOrNull() ?: 0
                            val w = score.second.toIntOrNull() ?: 0
                            val b = score.third.toIntOrNull() ?: 0
                            val net = ExamUtils.calculateNet(c, w, level)
                            SubjectResult(sub, c, w, b, net)
                        }
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val finalExamName = if (level == EducationLevel.YKS_TYT_AYT) "[$yksSubCategory] ${examName.trim()}" else examName.trim()
                        onConfirm(
                            ExamResult(
                                id = UUID.randomUUID().toString(),
                                name = finalExamName,
                                date = today,
                                educationLevel = level,
                                subjectResults = results
                            )
                        )
                    }
                },
                enabled = examName.isNotBlank() && !hasValidationError
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    studentId: String,
    studentLevel: EducationLevel? = null,
    studentExamTracks: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit
) {
    val allowedCategories = remember(studentLevel, studentExamTracks) {
        CurriculumDictionary.getExamCategoriesForUser(studentLevel, studentExamTracks)
    }
    var selectedCategory by remember { mutableStateOf(allowedCategories.firstOrNull() ?: "TYT") }
    val subjects = remember(selectedCategory) {
        CurriculumDictionary.getSubjectsForExamCategory(selectedCategory)
    }

    var title by remember { mutableStateOf("") }
    var selectedSubjectName by remember { mutableStateOf(subjects.firstOrNull()?.subject ?: "Matematik") }
    val topics = remember(selectedCategory, selectedSubjectName) {
        CurriculumDictionary.getTopicsForSubject(selectedCategory, selectedSubjectName)
    }
    var selectedTopicName by remember { mutableStateOf(topics.firstOrNull()?.topicName ?: "Genel Konu") }
    var targetQuestions by remember { mutableStateOf("30") }
    var selectedDay by remember { mutableStateOf(1) } // 1: Pazartesi
    var xpReward by remember { mutableStateOf("25") }

    val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Görev Ata", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = com.example.ui.theme.PrimaryNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Görev Başlığı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Exam Category Chips
                Text("Sınav Kategorisi:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    allowedCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                val newSubjects = CurriculumDictionary.getSubjectsForExamCategory(cat)
                                selectedSubjectName = newSubjects.firstOrNull()?.subject ?: ""
                                val newTopics = CurriculumDictionary.getTopicsForSubject(cat, selectedSubjectName)
                                selectedTopicName = newTopics.firstOrNull()?.topicName ?: ""
                            },
                            label = { Text(cat) }
                        )
                    }
                }

                // Subject Chips
                Text("Ders Seçimi:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                ScrollableTabRow(
                    selectedTabIndex = subjects.indexOfFirst { it.subject == selectedSubjectName }.coerceAtLeast(0),
                    edgePadding = 0.dp,
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    divider = {}
                ) {
                    subjects.forEach { item ->
                        Tab(
                            selected = selectedSubjectName == item.subject,
                            onClick = {
                                selectedSubjectName = item.subject
                                val newTopics = CurriculumDictionary.getTopicsForSubject(selectedCategory, item.subject)
                                selectedTopicName = newTopics.firstOrNull()?.topicName ?: ""
                            },
                            text = { Text(item.subject, fontSize = 12.sp) }
                        )
                    }
                }

                // Topic Chips
                Text("Konu Seçimi:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    topics.take(6).forEach { t ->
                        FilterChip(
                            selected = selectedTopicName == t.topicName,
                            onClick = { selectedTopicName = t.topicName },
                            label = { Text(t.topicName, fontSize = 11.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetQuestions,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) targetQuestions = it },
                        label = { Text("Soru Hedefi") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = xpReward,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) xpReward = it },
                        label = { Text("XP Ödülü") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Live XP Preview Card
                val taskXpVal = xpReward.toIntOrNull() ?: 25
                val estimatedTaskXp = com.example.util.XpPolicy.calculateTaskXp(taskXpVal)
                com.example.ui.components.XpPreviewCard(
                    proposedXp = estimatedTaskXp,
                    requiresCoachApproval = true,
                    subtitle = "Öğrenci görevi tamamlayıp onay aldığında hesabına aktarılır."
                )

                Text("Haftanın Günü", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.forEachIndexed { index, day ->
                        FilterChip(
                            selected = selectedDay == index + 1,
                            onClick = { selectedDay = index + 1 },
                            label = { Text(day, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedSubjectName.isNotBlank()) {
                        val targetQ = targetQuestions.toIntOrNull() ?: 30
                        val xp = xpReward.toIntOrNull() ?: 25
                        onConfirm(
                            Task(
                                id = UUID.randomUUID().toString(),
                                title = title.trim(),
                                subject = selectedSubjectName,
                                topic = selectedTopicName,
                                targetQuestionCount = targetQ,
                                completedQuestionCount = 0,
                                isCompleted = false,
                                priority = TaskPriority.MEDIUM,
                                xpReward = xp,
                                dayOfWeek = selectedDay,
                                photoProofUri = null,
                                isApproved = false
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && selectedSubjectName.isNotBlank()
            ) {
                Text("Görev Ata")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
