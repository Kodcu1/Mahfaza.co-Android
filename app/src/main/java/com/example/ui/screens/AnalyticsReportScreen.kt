package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ExamResult
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.components.EmptyState
import com.example.ui.components.PremiumButton
import com.example.ui.components.PremiumCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldAccent
import com.example.util.PdfExporter
import com.example.util.StudentAvatar
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsReportScreen(
    viewModel: MainViewModel,
    initialStudentId: String? = null
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()

    val assignedStudents = allStudents.filter { currentUser?.assignedStudents?.contains(it.id) == true }

    var selectedStudentId by remember {
        mutableStateOf(
            if (currentUser?.role == UserRole.COACH) {
                initialStudentId ?: assignedStudents.firstOrNull()?.id ?: ""
            } else {
                currentUser?.id ?: ""
            }
        )
    }

    val selectedStudent = if (currentUser?.role == UserRole.COACH) {
        allStudents.find { it.id == selectedStudentId } ?: assignedStudents.firstOrNull()
    } else {
        currentUser
    }

    val studentExams by viewModel.getStudentExams(selectedStudent?.id ?: "").collectAsStateWithLifecycle(emptyList())
    val studentTasks by viewModel.getStudentTasks(selectedStudent?.id ?: "").collectAsStateWithLifecycle(emptyList())

    var expandedStudentDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = "Yerel Veri Analizi & AI Raporlama")

        // Coach Student Picker
        if (currentUser?.role == UserRole.COACH && assignedStudents.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = expandedStudentDropdown,
                onExpandedChange = { expandedStudentDropdown = !expandedStudentDropdown }
            ) {
                OutlinedTextField(
                    value = selectedStudent?.name ?: "Öğrenci Seçin",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Analiz Edilen Öğrenci") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStudentDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expandedStudentDropdown,
                    onDismissRequest = { expandedStudentDropdown = false }
                ) {
                    assignedStudents.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.name) },
                            onClick = {
                                selectedStudentId = s.id
                                expandedStudentDropdown = false
                            }
                        )
                    }
                }
            }
        }

        if (selectedStudent == null) {
            EmptyState(message = "Lütfen analiz edilecek bir öğrenci seçin.", icon = Icons.Default.PersonSearch)
            return@Column
        }

        // Student Header Card
        PremiumCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(name = selectedStudent.name, avatarUri = selectedStudent.avatarUri, size = 52.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedStudent.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedStudent.educationLevel?.displayName ?: "Seviye Belirtilmedi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Yerel Motor",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Exam Analysis Logic
        when {
            studentExams.isEmpty() -> {
                PremiumCard(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Henüz Deneme Verisi Bulunmuyor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bu öğrenci için sisteme girilmiş deneme sonucu bulunmuyor. Akademik trend hesaplaması ve net gelişimi için lütfen en az 1 deneme kaydedin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            studentExams.size == 1 -> {
                val singleExam = studentExams.first()
                val net = singleExam.subjectResults.sumOf { it.net }
                PremiumCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "İlk Deneme Analizi Kaydedildi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Sistemde yalnızca 1 adet deneme (${singleExam.name}: %.2f Net) bulunmaktadır. Güvenilir bir gelişim eğrisi ve trend tahmini oluşturmak için ikinci denemenin tamamlanması beklenmektedir.".format(net),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            else -> {
                // 2 or more exams -> Complete Analysis
                val sortedExams = studentExams.sortedBy { it.date }
                val examNets = sortedExams.map { it.subjectResults.sumOf { s -> s.net } }
                val avgNet = examNets.average()
                val minNet = examNets.minOrNull() ?: 0.0
                val maxNet = examNets.maxOrNull() ?: 0.0
                val trend = examNets.last() - examNets.first()

                // Net Stat Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnalysisStatBox("Ortalama Net", "%.2f".format(avgNet), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    AnalysisStatBox("En Yüksek Net", "%.2f".format(maxNet), Color(0xFF059669), Modifier.weight(1f))
                    AnalysisStatBox(
                        "Net Trendi",
                        "${if (trend >= 0) "+" else ""}${String.format("%.2f", trend)}",
                        if (trend >= 0) Color(0xFF059669) else Color(0xFFDC2626),
                        Modifier.weight(1f)
                    )
                }

                // Trend Canvas Chart
                SectionHeader(title = "Kronolojik Net Gelişimi")
                PremiumCard {
                    AnalyticsTrendChart(examNets, sortedExams.map { it.name })
                }

                // Weak Subject Identification
                SectionHeader(title = "Ders Bazlı Yanlış & Boş Yoğunluğu")
                val subjectMistakes = mutableMapOf<String, Pair<Int, Int>>() // subject -> (wrong, blank)
                sortedExams.forEach { exam ->
                    exam.subjectResults.forEach { sub ->
                        val current = subjectMistakes[sub.subjectName] ?: Pair(0, 0)
                        subjectMistakes[sub.subjectName] = Pair(current.first + sub.wrong, current.second + sub.blank)
                    }
                }
                val mostWrongSubjects = subjectMistakes.toList().sortedByDescending { it.second.first }.take(4)

                PremiumCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        mostWrongSubjects.forEach { (subName, counts) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(subName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${counts.first} Yanlış", color = Color(0xFFDC2626), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text("•", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                                    Text("${counts.second} Boş", color = Color(0xFFD97706), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }

                // Academic Coaching Recommendations
                SectionHeader(title = "Akademik Tavsiyeler & Eylem Planı")
                PremiumCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RecommendationItem(
                            icon = if (trend >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            iconTint = if (trend >= 0) Color(0xFF059669) else Color(0xFFDC2626),
                            title = if (trend >= 0) "Pozitif Net İvmesi" else "Netlerde Dalgalanma Tespiti",
                            description = if (trend >= 0)
                                "Öğrenci denemeler arasında %.2f netlik bir artış sağladı. Çalışma temposu korunmalı.".format(trend)
                            else
                                "Son denemelerde net kaybı gözleniyor. Sınav süresi yönetimi ve konu eksikleri gözden geçirilmeli."
                        )
                        if (mostWrongSubjects.isNotEmpty()) {
                            val weakest = mostWrongSubjects.first().first
                            RecommendationItem(
                                icon = Icons.Default.PriorityHigh,
                                iconTint = GoldAccent,
                                title = "Odak Dersi: $weakest",
                                description = "$weakest dersinde toplam ${mostWrongSubjects.first().second.first} yanlış tespit edildi. Haftalık programa bu dersten en az 50 soru eklenmesi önerilir."
                            )
                        }
                    }
                }
            }
        }

        // Task Completion Metric
        SectionHeader(title = "Çalışma Disiplini & Görev Analizi")
        val completedCount = studentTasks.count { it.isCompleted }
        val totalCount = studentTasks.size
        val completionRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

        PremiumCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tamamlanma Oranı", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("%${(completionRatio * 100).toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                LinearProgressIndicator(
                    progress = { completionRatio },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "$totalCount görevden $completedCount adedi tamamlandı.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Export PDF Action
        PremiumButton(
            text = "360° PDF Analiz Raporunu İndir",
            onClick = {
                PdfExporter.exportStudent360(context, selectedStudent, studentExams)
            },
            icon = Icons.Default.PictureAsPdf,
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AnalysisStatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AnalyticsTrendChart(nets: List<Double>, examNames: List<String>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val maxNet = (nets.maxOrNull() ?: 100.0).coerceAtLeast(80.0).toFloat()

    Column {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).padding(top = 16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spacing = width / (nets.size - 1).coerceAtLeast(1)

                val points = nets.mapIndexed { index, net ->
                    val x = index * spacing
                    val y = height - (net.toFloat() / maxNet * (height - 30.dp.toPx())) - 10.dp.toPx()
                    Offset(x, y)
                }

                // Grid lines
                for (i in 0..3) {
                    val y = height - (i * height / 3)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Curve line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = primaryColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Point markers
                points.forEach { point ->
                    drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = point)
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            examNames.forEach { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecommendationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
