package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.viewmodel.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.GoldAccent
import com.example.util.AvatarUtils
import com.example.util.StudentAvatar
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CoachProfileScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    var showEditSheet by remember { mutableStateOf(false) }
    var showUpgradeSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && user != null) {
            val savedPath = AvatarUtils.saveAvatarToInternalStorage(context, uri, user!!.id)
            if (savedPath != null) {
                viewModel.updateUserAvatar(user!!.id, savedPath)
                Toast.makeText(context, "Profil fotoğrafı başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (user == null || user?.role != UserRole.COACH) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bu ekran yalnızca koçlar içindir.")
        }
        return
    }

    val coach = user!!
    val userPkg = coach.packageId.let { PackageCatalog.getPackageById(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // A. Profile Header
        PremiumCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    StudentAvatar(
                        name = coach.name,
                        avatarUri = coach.avatarUri,
                        size = 100.dp,
                        textSize = 28.sp
                    )
                    SmallFloatingActionButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Fotoğrafı Değiştir", modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = coach.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = coach.title ?: "Eğitim Koçu", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Profile Completion (Simulated)
                LinearProgressIndicator(
                    progress = { 0.85f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = GoldAccent,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "Profil Tamamlama: %85",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumButton(
                        text = "Profili Düzenle",
                        onClick = { showEditSheet = true },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Edit
                    )
                    OutlinedButton(
                        onClick = { showUpgradeSheet = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Paketi Yükselt")
                    }
                }
            }
        }

        // Bio & Approach
        SectionHeader(title = "Hakkında")
        PremiumCard {
            Text(text = coach.bio ?: "Henüz bir biyografi eklenmemiş.", style = MaterialTheme.typography.bodyMedium)
            if (coach.coachingApproach != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Koçluk Yaklaşımı:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(text = coach.coachingApproach!!, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Coach Code Card for Student Linking
        SectionHeader(title = "Öğrenci Bağlantı Kodu")
        var coachCode by remember { mutableStateOf(coach.coachCode ?: "") }
        LaunchedEffect(coach.id) {
            viewModel.getOrGenerateCoachCode(coach.id) { code ->
                coachCode = code
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.SoftGoldSurface),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Paylaşılabilir Koç Kodu", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = if (coachCode.isNotBlank()) coachCode else "Oluşturuluyor...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Öğrenciler profillerinden bu kodu girerek doğrudan size bağlanır.", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Coach Code", coachCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Koç kodu kopyalandı: $coachCode", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // B. Expertise Tags
        SectionHeader(title = "Uzmanlık Alanları")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            coach.expertiseTags.forEach { tag ->
                InputChip(
                    selected = true,
                    onClick = { 
                        val newTags = coach.expertiseTags.toMutableList().apply { remove(tag) }
                        viewModel.updateUser(coach.copy(expertiseTags = newTags))
                    },
                    label = { Text(tag) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
            InputChip(
                selected = false,
                onClick = { 
                    val newTags = coach.expertiseTags.toMutableList().apply { 
                        if (!contains("Yeni Uzmanlık")) add("Yeni Uzmanlık") 
                    }
                    viewModel.updateUser(coach.copy(expertiseTags = newTags))
                },
                label = { Text("+ Ekle") },
                trailingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        // C. Education & Experience
        SectionHeader(title = "Eğitim ve Deneyim")
        PremiumCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                coach.education.forEach { edu ->
                    EntryItem(
                        icon = Icons.Default.School,
                        title = "${edu.university} - ${edu.department}",
                        subtitle = "${edu.degree} • ${edu.graduationYear}"
                    )
                }
                coach.experience.forEach { exp ->
                    EntryItem(
                        icon = Icons.Default.Work,
                        title = exp.title,
                        subtitle = "${exp.company} • ${exp.period}"
                    )
                }
                coach.certificates.forEach { cert ->
                    EntryItem(
                        icon = Icons.Default.CardMembership,
                        title = cert.name,
                        subtitle = "${cert.organization} • ${cert.year}"
                    )
                }
            }
        }

        // D. Work Schedule
        SectionHeader(title = "Çalışma Düzeni")
        PremiumCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Çalışma Günleri: ${coach.workingDays.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Çalışma Saatleri: ${coach.workingHours ?: "Belirtilmemiş"}", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = coach.acceptingStudents, onCheckedChange = { viewModel.updateUser(coach.copy(acceptingStudents = it)) })
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = if (coach.acceptingStudents) "Yeni Öğrenci Kabul Ediliyor" else "Yeni Öğrenci Kabul Edilmiyor", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // E. Performance Cards (Simulated from coach data)
        SectionHeader(title = "Koçluk Performansı")
        val studentCount = coach.assignedStudents.size
        val capacity = coach.studentCapacity
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "Aktif Öğrenci",
                value = "$studentCount",
                icon = Icons.Default.People,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Görev Tamamlama",
                value = "%78",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF059669),
                modifier = Modifier.weight(1f)
            )
        }

        // F. Package Info
        SectionHeader(title = "Paket Bilgisi")
        PremiumCard(containerColor = MaterialTheme.colorScheme.surfaceVariant, border = null) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = userPkg?.name ?: "Koç Başlangıç", fontWeight = FontWeight.Bold)
                    Text(text = "$studentCount / $capacity Öğrenci", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (capacity > 0) studentCount.toFloat() / capacity else 0f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { showUpgradeSheet = true }, modifier = Modifier.align(Alignment.End)) {
                    Text("Paketi Yükselt", color = GoldAccent)
                }
            }
        }

        // G. Account Actions (Çıkış Yap)
        SectionHeader(title = "Hesap İşlemleri")
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Çıkış Yap", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
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

    if (showEditSheet) {
        EditProfileBottomSheet(
            coach = coach,
            onDismiss = { showEditSheet = false },
            onSave = { updatedCoach ->
                viewModel.updateUser(updatedCoach)
                showEditSheet = false
            }
        )
    }

    if (showUpgradeSheet) {
        com.example.ui.components.UpgradePackageSheet(
            currentUser = coach,
            onDismiss = { showUpgradeSheet = false },
            onSelectPackage = { pkg, isYearly ->
                viewModel.updateUserSubscriptionPlan(pkg.id, if (isYearly) "YEARLY" else "MONTHLY")
                Toast.makeText(context, "${pkg.name} paketine geçildi! Öğrenci kapasitesi güncellendi.", Toast.LENGTH_SHORT).show()
                showUpgradeSheet = false
            }
        )
    }
}

@Composable
fun EntryItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(coach: User, onDismiss: () -> Unit, onSave: (User) -> Unit) {
    var title by remember { mutableStateOf(coach.title ?: "") }
    var bio by remember { mutableStateOf(coach.bio ?: "") }
    var approach by remember { mutableStateOf(coach.coachingApproach ?: "") }
    var workingHours by remember { mutableStateOf(coach.workingHours ?: "") }
    var workingDaysStr by remember { mutableStateOf(coach.workingDays.joinToString(", ")) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Koç Profili & Çalışma Düzeni", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Mesleki Unvan") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Biyografi") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            OutlinedTextField(
                value = approach,
                onValueChange = { approach = it },
                label = { Text("Koçluk Yaklaşımı") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = workingHours,
                onValueChange = { workingHours = it },
                label = { Text("Çalışma Saatleri (Örn: 09:00 - 18:00)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = workingDaysStr,
                onValueChange = { workingDaysStr = it },
                label = { Text("Çalışma Günleri (Örn: Pazartesi, Salı, Çarşamba)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("İptal")
                }
                Button(
                    onClick = {
                        val parsedDays = workingDaysStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onSave(coach.copy(
                            title = title,
                            bio = bio,
                            coachingApproach = approach,
                            workingHours = workingHours,
                            workingDays = if (parsedDays.isNotEmpty()) parsedDays else coach.workingDays
                        ))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Kaydet")
                }
            }
        }
    }
}
