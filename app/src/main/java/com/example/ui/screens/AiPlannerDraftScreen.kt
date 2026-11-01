package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.EducationLevel
import com.example.model.Task
import com.example.service.LocalPlannerService
import com.example.ui.components.PremiumButton
import com.example.ui.components.PremiumCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.bouncyClick

@Composable
fun AiPlannerDraftScreen(onPublish: (List<Task>) -> Unit) {
    var draftTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader(
            title = "Yapay Zeka Plan Taslağı",
            actionText = if (draftTasks.isNotEmpty()) "Yeniden Oluştur" else null,
            onActionClick = { 
                draftTasks = LocalPlannerService.generateDraft(EducationLevel.YKS_TYT_AYT, listOf("Matematik"))
            }
        )
        
        if (draftTasks.isEmpty() && !isGenerating) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Öğrenci verilerine göre haftalık plan taslağı oluşturun.")
                    Spacer(modifier = Modifier.height(24.dp))
                    PremiumButton(
                        text = "Taslak Oluştur",
                        onClick = {
                            isGenerating = true
                            draftTasks = LocalPlannerService.generateDraft(EducationLevel.YKS_TYT_AYT, listOf("Matematik"))
                            isGenerating = false
                        },
                        icon = Icons.Default.AutoAwesome
                    )
                }
            }
        } else if (isGenerating) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(draftTasks, key = { it.id }) { task ->
                    DraftTaskItem(
                        task = task,
                        onEdit = { taskToEdit = task }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PremiumButton(
                text = "Planı Onayla ve Yayınla",
                onClick = { onPublish(draftTasks) },
                modifier = Modifier.fillMaxWidth().bouncyClick { }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Yayınlamadan önce kalem simgesine dokunarak görevleri özelleştirebilirsiniz. Yayınladığınızda öğrencinin haftalık programına eklenecektir.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (taskToEdit != null) {
        EditDraftTaskDialog(
            task = taskToEdit!!,
            onDismiss = { taskToEdit = null },
            onSave = { updatedTask ->
                draftTasks = draftTasks.map { if (it.id == updatedTask.id) updatedTask else it }
                taskToEdit = null
            }
        )
    }
}

@Composable
fun DraftTaskItem(task: Task, onEdit: () -> Unit) {
    val days = listOf("", "Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    val dayLabel = days.getOrElse(task.dayOfWeek) { "Pzt" }

    PremiumCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = dayLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${task.subject} • ${task.targetQuestionCount} Soru", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Görevi Düzenle",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EditDraftTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var subject by remember { mutableStateOf(task.subject) }
    var questionCountText by remember { mutableStateOf(task.targetQuestionCount.toString()) }
    var selectedDay by remember { mutableStateOf(task.dayOfWeek) }

    val days = listOf(
        1 to "Pazartesi",
        2 to "Salı",
        3 to "Çarşamba",
        4 to "Perşembe",
        5 to "Cuma",
        6 to "Cumartesi",
        7 to "Pazar"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Görevi Düzenle", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                    label = { Text("Ders / Branş") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = questionCountText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) questionCountText = it },
                    label = { Text("Hedef Soru Sayısı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Gün Seçimi:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.take(4).forEach { (dayNum, name) ->
                        FilterChip(
                            selected = selectedDay == dayNum,
                            onClick = { selectedDay = dayNum },
                            label = { Text(name.take(3)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.drop(4).forEach { (dayNum, name) ->
                        FilterChip(
                            selected = selectedDay == dayNum,
                            onClick = { selectedDay = dayNum },
                            label = { Text(name.take(3)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qCount = questionCountText.toIntOrNull() ?: task.targetQuestionCount
                    onSave(
                        task.copy(
                            title = title.trim(),
                            subject = subject.trim(),
                            targetQuestionCount = qCount,
                            dayOfWeek = selectedDay
                        )
                    )
                },
                enabled = title.isNotBlank() && subject.isNotBlank()
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}
