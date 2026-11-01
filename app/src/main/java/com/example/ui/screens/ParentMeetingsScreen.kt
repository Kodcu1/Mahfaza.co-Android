package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ParentMeeting
import com.example.ui.components.EmptyState
import com.example.ui.components.PremiumCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.util.StudentAvatar
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentMeetingsScreen(
    viewModel: MainViewModel,
    initialStudentId: String? = null
) {
    val context = LocalContext.current
    val allMeetings by viewModel.allMeetings.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val assignedStudents = allStudents.filter { currentUser?.assignedStudents?.contains(it.id) == true }

    var selectedStudentId by remember { mutableStateOf(initialStudentId) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Tümü, 1: Planlandı, 2: Tamamlandı
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredMeetings = allMeetings.filter { meeting ->
        val matchesStudent = selectedStudentId == null || meeting.studentId == selectedStudentId
        val matchesTab = when (selectedTab) {
            1 -> !meeting.isCompleted
            2 -> meeting.isCompleted
            else -> true
        }
        matchesStudent && matchesTab
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Yeni Görüşme") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionHeader(title = "Veli Görüşmeleri")

            var studentDropdownExpanded by remember { mutableStateOf(false) }
            val selectedStudentObj = assignedStudents.find { it.id == selectedStudentId }

            // Section 7: Searchable "Öğrenci Seç" Dropdown (No horizontal tab row)
            ExposedDropdownMenuBox(
                expanded = studentDropdownExpanded,
                onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                OutlinedTextField(
                    value = selectedStudentObj?.name ?: "Tüm Yetkili Öğrenciler",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Öğrenci Seç") },
                    leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null, tint = PrimaryNavy) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                ExposedDropdownMenu(
                    expanded = studentDropdownExpanded,
                    onDismissRequest = { studentDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tüm Yetkili Öğrenciler", fontWeight = FontWeight.Bold) },
                        onClick = {
                            selectedStudentId = null
                            studentDropdownExpanded = false
                        }
                    )
                    HorizontalDivider()
                    assignedStudents.forEach { student ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StudentAvatar(name = student.name, avatarUri = student.avatarUri, size = 28.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(student.name, fontWeight = FontWeight.SemiBold)
                                        Text(student.educationLevel?.displayName ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            onClick = {
                                selectedStudentId = student.id
                                studentDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Status Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Tümü (${allMeetings.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Planlandı (${allMeetings.count { !it.isCompleted }})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Tamamlandı (${allMeetings.count { it.isCompleted }})") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredMeetings.isEmpty()) {
                EmptyState(
                    message = "Kayıtlı veli görüşmesi bulunmuyor. Yeni görüşme kaydetmek için aşağıdaki butonu kullanabilirsiniz.",
                    icon = Icons.Default.ContactPhone
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMeetings, key = { it.id }) { meeting ->
                        val student = allStudents.find { it.id == meeting.studentId }
                        MeetingCard(
                            meeting = meeting,
                            studentAvatarUri = student?.avatarUri,
                            onExportPdf = {
                                com.example.util.PdfExporter.exportParentMeeting(context, meeting, student)
                            },
                            onToggleStatus = {
                                viewModel.updateMeeting(meeting.copy(isCompleted = !meeting.isCompleted))
                                Toast.makeText(
                                    context,
                                    if (!meeting.isCompleted) "Görüşme tamamlandı olarak işaretlendi." else "Görüşme planlandı durumuna alındı.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onDelete = {
                                viewModel.deleteMeeting(meeting)
                                Toast.makeText(context, "Görüşme kaydı silindi.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMeetingDialog(
            assignedStudents = assignedStudents,
            initialStudentId = selectedStudentId,
            onDismiss = { showAddDialog = false },
            onConfirm = { newMeeting ->
                viewModel.addMeeting(newMeeting)
                Toast.makeText(context, "Veli görüşmesi kaydedildi.", Toast.LENGTH_SHORT).show()
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MeetingCard(
    meeting: ParentMeeting,
    studentAvatarUri: String?,
    onExportPdf: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    PremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(name = meeting.studentName, avatarUri = studentAvatarUri, size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meeting.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tarih: ${meeting.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (meeting.isCompleted) Color(0xFF059669).copy(alpha = 0.12f) else Color(0xFF2563EB).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (meeting.isCompleted) "Tamamlandı" else "Planlandı",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (meeting.isCompleted) Color(0xFF059669) else Color(0xFF2563EB),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text(
                text = "Konu: ${meeting.topic}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (meeting.notes.isNotBlank()) {
                Text(
                    text = "Görüşme Notları:\n${meeting.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (meeting.decisions.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Alınan Kararlar:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = meeting.decisions,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (meeting.nextMeetingDate.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bir Sonraki Görüşme: ${meeting.nextMeetingDate}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF Raporu", style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = onToggleStatus,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (meeting.isCompleted) MaterialTheme.colorScheme.error else Color(0xFF059669)
                        )
                    ) {
                        Icon(
                            if (meeting.isCompleted) Icons.Default.Replay else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (meeting.isCompleted) "Yeniden Aç" else "Tamamla")
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeetingDialog(
    assignedStudents: List<com.example.model.User>,
    initialStudentId: String?,
    onDismiss: () -> Unit,
    onConfirm: (ParentMeeting) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())

    var studentId by remember { mutableStateOf(initialStudentId ?: assignedStudents.firstOrNull()?.id ?: "") }
    var date by remember { mutableStateOf(today) }
    var topic by remember { mutableStateOf("Genel Akademik Gelişim & Deneme Analizi") }
    var notes by remember { mutableStateOf("") }
    var decisions by remember { mutableStateOf("") }
    var nextMeetingDate by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }

    var expandedDropdown by remember { mutableStateOf(false) }
    val selectedStudent = assignedStudents.find { it.id == studentId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Veli Görüşmesi Kaydı") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Student Picker
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedStudent?.name ?: "Öğrenci Seçin",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Öğrenci") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        assignedStudents.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.name) },
                                onClick = {
                                    studentId = s.id
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Görüşme Tarihi (YYYY-AA-GG)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Görüşme Konusu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Görüşme Notları") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = decisions,
                    onValueChange = { decisions = it },
                    label = { Text("Alınan Kararlar / Eylem Planı") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nextMeetingDate,
                    onValueChange = { nextMeetingDate = it },
                    label = { Text("Sonraki Görüşme Tarihi (İsteğe Bağlı)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isCompleted, onCheckedChange = { isCompleted = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Görüşme Tamamlandı", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (studentId.isNotBlank() && selectedStudent != null) {
                        onConfirm(
                            ParentMeeting(
                                id = UUID.randomUUID().toString(),
                                studentId = studentId,
                                studentName = selectedStudent.name,
                                date = date,
                                notes = notes,
                                decisions = decisions,
                                topic = topic,
                                nextMeetingDate = nextMeetingDate,
                                isCompleted = isCompleted
                            )
                        )
                    }
                },
                enabled = studentId.isNotBlank() && selectedStudent != null
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
