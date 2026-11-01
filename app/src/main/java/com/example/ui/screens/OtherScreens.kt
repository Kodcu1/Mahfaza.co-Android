package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.viewmodel.MainViewModel
import com.example.ui.components.*
import com.example.navigation.Chat
import com.example.util.PdfExporter
import kotlinx.coroutines.flow.Flow

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    var targetUniversity by remember(user?.targetUniversity) { mutableStateOf(user?.targetUniversity ?: "") }
    var targetDepartment by remember(user?.targetDepartment) { mutableStateOf(user?.targetDepartment ?: "") }
    var targetRanking by remember(user?.targetRanking) { mutableStateOf(user?.targetRanking ?: "") }
    var goals by remember(user?.goals) { mutableStateOf(user?.goals ?: "") }
    var coachCodeInput by remember { mutableStateOf("") }

    var assignedCoachName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user?.assignedCoachId) {
        if (!user?.assignedCoachId.isNullOrBlank()) {
            val coach = viewModel.getStudentById(user!!.assignedCoachId!!)
            assignedCoachName = coach?.name
        } else {
            assignedCoachName = null
        }
    }

    var showUpgradeSheet by remember { mutableStateOf(false) }

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri: android.net.Uri? ->
        if (uri != null && user != null) {
            val savedPath = com.example.util.AvatarUtils.saveAvatarToInternalStorage(context, uri, user!!.id)
            if (savedPath != null) {
                viewModel.updateUserAvatar(user!!.id, savedPath)
                Toast.makeText(context, "Profil fotoğrafı başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        PremiumCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    com.example.util.StudentAvatar(
                        name = user?.name ?: "Öğrenci",
                        avatarUri = user?.avatarUri,
                        size = 90.dp,
                        textSize = 28.sp
                    )
                    SmallFloatingActionButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Fotoğrafı Değiştir", modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = user?.name ?: "Öğrenci", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                val userPkg = user?.packageId?.let { PackageCatalog.getPackageById(it) }
                Text(
                    text = "${user?.educationLevel?.displayName ?: "LGS & YKS"} • ${userPkg?.name ?: "Standart"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "Mevcut: ${user?.currentXp ?: 0} XP", style = MaterialTheme.typography.labelLarge, color = com.example.ui.theme.Gold)
                    Text(text = "Seri: ${user?.streak ?: 0} Gün 🔥", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                PremiumButton(
                    text = "Paketi Yükselt",
                    onClick = { showUpgradeSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.WorkspacePremium
                )
            }
        }

        // Section: Academic Targets
        SectionHeader(title = "Akademik Hedeflerim")
        PremiumCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = targetUniversity,
                    onValueChange = { targetUniversity = it },
                    label = { Text("Hedef Üniversite / Lise") },
                    placeholder = { Text("Örn: Boğaziçi Üniversitesi / Kabataş Erkek") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetDepartment,
                    onValueChange = { targetDepartment = it },
                    label = { Text("Hedef Bölüm / Alan") },
                    placeholder = { Text("Örn: Bilgisayar Mühendisliği / Fen Lisesi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetRanking,
                    onValueChange = { targetRanking = it },
                    label = { Text("Hedef Sıralama") },
                    placeholder = { Text("Örn: İlk 1.000 / %0.5'lik Dilim") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = goals,
                    onValueChange = { goals = it },
                    label = { Text("Kişisel Çalışma Hedefleri") },
                    placeholder = { Text("Günde 250 soru, haftalık 3 deneme analizi...") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                PremiumButton(
                    text = "Hedefleri Güncelle",
                    onClick = {
                        viewModel.updateStudentAcademicProfile(targetUniversity, targetDepartment, targetRanking, goals)
                        Toast.makeText(context, "Akademik hedefleriniz başarıyla kaydedildi.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Save
                )
            }
        }

        // Section: Coach Connection
        SectionHeader(title = "Eğitim Koçum")
        PremiumCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (assignedCoachName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = com.example.ui.theme.SoftTealSurface,
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = com.example.ui.theme.TealAccent)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Bağlı Koçunuz", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(assignedCoachName!!, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "Henüz bir eğitim koçuna bağlı değilsiniz. Koçunuzun size verdiği bağlantı kodunu girerek eşleşebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = coachCodeInput,
                        onValueChange = { coachCodeInput = it.uppercase() },
                        label = { Text("Koç Kodu (Örn: KOC-AB12CD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (coachCodeInput.isNotBlank()) {
                                viewModel.linkStudentToCoachByCode(coachCodeInput) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Koçunuza başarıyla bağlandınız!", Toast.LENGTH_SHORT).show()
                                        coachCodeInput = ""
                                    } else {
                                        Toast.makeText(context, "Geçersiz koç kodu. Lütfen tekrar kontrol edin.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.height(54.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Bağlan")
                    }
                }
            }
        }

        // Account Section
        SectionHeader(title = "Hesap İşlemleri")

        var showLogoutDialog by remember { mutableStateOf(false) }

        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Çıkış Yap", fontWeight = FontWeight.Bold)
        }

        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    viewModel.logout()
                }
            )
        }

        if (showUpgradeSheet) {
            com.example.ui.components.UpgradePackageSheet(
                currentUser = user,
                onDismiss = { showUpgradeSheet = false },
                onSelectPackage = { pkg, isYearly ->
                    viewModel.updateUserSubscriptionPlan(pkg.id, if (isYearly) "YEARLY" else "MONTHLY")
                    Toast.makeText(context, "${pkg.name} paketine geçildi!", Toast.LENGTH_SHORT).show()
                    showUpgradeSheet = false
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun CoachPanelScreen(viewModel: MainViewModel, onNavigateToPlanner: () -> Unit, onStudentClick: (String) -> Unit) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val assignedStudents = allStudents.filter { user?.assignedStudents?.contains(it.id) == true }
    val canAdd = viewModel.canAddStudent()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader(
            title = "Öğrenci Portfolyosu",
            actionText = "Yapay Zeka Planlayıcı",
            onActionClick = onNavigateToPlanner
        )
        
        if (!canAdd) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Öğrenci kapasiteniz dolmuştur (${user?.assignedStudents?.size ?: 0}/${user?.studentCapacity ?: 0}). Yeni öğrenci eklemek için paketinizi yükseltin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        PremiumButton(
            text = "Yeni Öğrenci Ekle",
            onClick = { /* Add student logic simulation */ },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            containerColor = if (canAdd) MaterialTheme.colorScheme.primary else Color.Gray,
            icon = Icons.Default.Add
        )
        
        if (assignedStudents.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Henüz size atanmış bir öğrenci bulunmuyor.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(assignedStudents) { student ->
                    StudentPortfolioCard(student = student, onClick = { onStudentClick(student.id) })
                }
            }
        }
    }
}

@Composable
fun StudentPortfolioCard(student: User, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Text(student.name.take(1))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(student.educationLevel?.displayName ?: "", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${student.currentXp} XP", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text("${student.streak} Gün Seri", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun StudentDetailScreen(viewModel: MainViewModel, studentId: String, onNavigateToChat: (String) -> Unit) {
    var student by remember { mutableStateOf<User?>(null) }
    val studentExams by viewModel.getStudentExams(studentId).collectAsStateWithLifecycle(emptyList())
    val studentTasks by viewModel.getStudentTasks(studentId).collectAsStateWithLifecycle(emptyList())
    val studentXpTransactions by viewModel.getStudentXPTransactions(studentId).collectAsStateWithLifecycle(emptyList())
    
    var showXpDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(studentId) {
        student = viewModel.getStudentById(studentId)
    }
    
    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Öğrenci bulunamadı.")
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
        PremiumCard {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = studentVal.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = studentVal.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = studentVal.educationLevel?.displayName ?: "Seviye Belirtilmemiş", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = { showXpDialog = true }) {
                    Icon(Icons.Default.AddModerator, contentDescription = "XP Yönet", tint = com.example.ui.theme.GoldAccent)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "Mevcut XP",
                value = "${studentVal.currentXp}",
                icon = Icons.Default.Star,
                color = com.example.ui.theme.GoldAccent,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Lifetime XP",
                value = "${studentVal.lifetimeXp}",
                icon = Icons.Default.TrendingUp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        SectionHeader(title = "Akademik Gelişim (Son 5 Deneme)")
        PremiumCard {
            AcademicTrendChart(studentExams)
        }

        SectionHeader(title = "Son XP Hareketleri")
        PremiumCard {
            if (studentXpTransactions.isEmpty()) {
                Text("Henüz bir işlem bulunmuyor.", style = MaterialTheme.typography.bodySmall)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    studentXpTransactions.take(5).forEach { tx ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (tx.amount > 0) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                                contentDescription = null,
                                tint = if (tx.amount > 0) Color(0xFF059669) else Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tx.reason, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text(
                                "${if (tx.amount > 0) "+" else ""}${tx.amount} XP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.amount > 0) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }

        SectionHeader(title = "Akademik Detaylar")
        PremiumCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(label = "E-posta", value = studentVal.email, icon = Icons.Default.Email)
                InfoRow(label = "Kayıtlı Paket", value = PackageCatalog.getPackageById(studentVal.packageId)?.name ?: "Standart", icon = Icons.Default.CardMembership)
                InfoRow(label = "Tamamlanan Görev", value = "${studentTasks.count { it.isCompleted }}/${studentTasks.size}", icon = Icons.Default.TaskAlt)
            }
        }

        if (studentExams.isNotEmpty()) {
            val lastExam = studentExams.first()
            SectionHeader(title = "Son Deneme Analizi")
            PremiumCard {
                Column {
                    Text(text = lastExam.name, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val totalNet = lastExam.subjectResults.sumOf { it.net }
                        val totalCorrect = lastExam.subjectResults.sumOf { it.correct }
                        val totalWrong = lastExam.subjectResults.sumOf { it.wrong }
                        
                        ScoreStat("Doğru", "$totalCorrect", Color(0xFF059669))
                        ScoreStat("Yanlış", "$totalWrong", Color(0xFFDC2626))
                        ScoreStat("Net", "%.2f".format(totalNet), MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        val context = LocalContext.current
        PremiumButton(
            text = "PDF Raporu Oluştur (360° Analiz)",
            onClick = { PdfExporter.exportStudent360(context, studentVal, studentExams) },
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.secondary,
            icon = Icons.Default.PictureAsPdf
        )
        Spacer(modifier = Modifier.height(8.dp))
        PremiumButton(
            text = "Haftalık Program Hazırla",
            onClick = { /* Navigation logic */ },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = { onNavigateToChat(studentId) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Message, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Öğrenciye Mesaj Gönder")
        }
        
        Spacer(modifier = Modifier.height(40.dp))
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
}

@Composable
fun XpManagementDialog(studentName: String, onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$studentName XP Yönetimi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("XP Miktarı (Örn: 50 veya -50)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("İşlem Nedeni") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(amount.toIntOrNull() ?: 0, reason) }) {
                Text("Uygula")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun AcademicTrendChart(exams: List<ExamResult>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val chartData = if (exams.isEmpty()) {
        listOf(45.0, 52.0, 48.0, 60.0, 65.0)
    } else {
        exams.takeLast(5).map { it.subjectResults.sumOf { s -> s.net } }
    }

    Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 16.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val spacing = width / (chartData.size - 1).coerceAtLeast(1)
            val maxNet = (chartData.maxOrNull() ?: 100.0).coerceAtLeast(100.0).toFloat()
            
            val points = chartData.mapIndexed { index, net ->
                val x = index * spacing
                val y = height - (net.toFloat() / maxNet * height)
                Offset(x, y)
            }

            for (i in 0..4) {
                val y = height - (i * height / 4)
                drawLine(color = Color.LightGray.copy(alpha = 0.5f), start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1.dp.toPx())
            }

            for (i in 0 until points.size - 1) {
                drawLine(color = primaryColor, start = points[i], end = points[i + 1], strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            }

            points.forEach { point ->
                drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = point)
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ScoreStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "Bu özellik yakında eklenecektir.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AdminPanelScreen(viewModel: MainViewModel) {
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val coaches by viewModel.allCoaches.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader(title = "Sistem Özeti")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem("Öğrenci", students.size.toString(), Icons.Default.People, Modifier.weight(1f))
            StatItem("Koç", coaches.size.toString(), Icons.Default.SupervisorAccount, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
