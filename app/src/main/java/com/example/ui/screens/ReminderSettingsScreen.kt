package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.data.local.ReminderSettingsEntity
import com.example.ui.components.*
import com.example.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.testTag
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val settings by viewModel.reminderSettings.collectAsStateWithLifecycle()
    
    val notificationPermissionState = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionState?.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bildirim ve Hatırlatmalar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("reminder_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PremiumAnimatedBackground()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Permission Status Card
                if (notificationPermissionState != null && !notificationPermissionState.status.isGranted) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bildirim İzni Gerekli", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("Hatırlatıcı alabilmek için sistem ayarlarından bildirim izni vermelisiniz.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                            Button(
                                onClick = { notificationPermissionState.launchPermissionRequest() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("İzin Ver")
                            }
                        }
                    }
                }

                val currentSettings = settings ?: ReminderSettingsEntity(userId = user?.id ?: "")

                // Master Switch
                ReminderToggleCard(
                    title = "Genel Bildirimler",
                    description = "Uygulama genelindeki tüm hatırlatıcıları açar veya kapatır.",
                    checked = currentSettings.notificationsEnabled,
                    onCheckedChange = { viewModel.updateReminderSettings(currentSettings.copy(notificationsEnabled = it)) }
                )

                if (currentSettings.notificationsEnabled) {
                    SectionHeader(title = "Otomatik Hatırlatıcılar")
                    
                    // Daily Reminder
                    ReminderToggleCard(
                        title = "Günlük Çalışma Hatırlatıcısı",
                        description = "Her gün belirlediğiniz saatte size hedeflerinizi hatırlatır.",
                        checked = currentSettings.dailyReminderEnabled,
                        onCheckedChange = { viewModel.updateReminderSettings(currentSettings.copy(dailyReminderEnabled = it)) },
                        trailing = {
                            if (currentSettings.dailyReminderEnabled) {
                                TimePickerButton(time = currentSettings.dailyReminderTime) { newTime ->
                                    viewModel.updateReminderSettings(currentSettings.copy(dailyReminderTime = newTime))
                                }
                            }
                        }
                    )

                    // Second Daily Reminder
                    ReminderToggleCard(
                        title = "İkinci Günlük Hatırlatıcı",
                        description = "Günün sonunda durum değerlendirmesi için ek bir hatırlatıcı.",
                        checked = currentSettings.secondDailyReminderEnabled,
                        onCheckedChange = { viewModel.updateReminderSettings(currentSettings.copy(secondDailyReminderEnabled = it)) },
                        trailing = {
                            if (currentSettings.secondDailyReminderEnabled) {
                                TimePickerButton(time = currentSettings.secondDailyReminderTime) { newTime ->
                                    viewModel.updateReminderSettings(currentSettings.copy(secondDailyReminderTime = newTime))
                                }
                            }
                        }
                    )

                    // Weekly Plan Reminder
                    ReminderToggleCard(
                        title = "Haftalık Planlama Hatırlatıcısı",
                        description = "Yeni haftaya hazırlanmanız için haftalık program hatırlatması.",
                        checked = currentSettings.weeklyPlanReminderEnabled,
                        onCheckedChange = { viewModel.updateReminderSettings(currentSettings.copy(weeklyPlanReminderEnabled = it)) },
                        trailing = {
                            if (currentSettings.weeklyPlanReminderEnabled) {
                                Column(horizontalAlignment = Alignment.End) {
                                    DayPickerButton(day = currentSettings.weeklyPlanReminderDay) { newDay ->
                                        viewModel.updateReminderSettings(currentSettings.copy(weeklyPlanReminderDay = newDay))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TimePickerButton(time = currentSettings.weeklyPlanReminderTime) { newTime ->
                                        viewModel.updateReminderSettings(currentSettings.copy(weeklyPlanReminderTime = newTime))
                                    }
                                }
                            }
                        }
                    )

                    SectionHeader(title = "Görev Bazlı Hatırlatıcılar")

                    ReminderToggleCard(
                        title = "Ders ve Görev Hatırlatıcıları",
                        description = "Programa eklediğiniz spesifik görevlerin saatinde bildirim alırsınız.",
                        checked = currentSettings.taskRemindersEnabled,
                        onCheckedChange = { viewModel.updateReminderSettings(currentSettings.copy(taskRemindersEnabled = it)) }
                    )

                    ReminderToggleCard(
                        title = "Eksik Görev Hatırlatması",
                        description = "Günün sonunda tamamlanmamış görevleriniz varsa sizi uyarır.",
                        checked = currentSettings.incompleteTaskReminderEnabled,
                        onCheckedChange = { viewModel.updateReminderSettings(currentSettings.copy(incompleteTaskReminderEnabled = it)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumButton(
                        text = "Test Bildirimi Gönder",
                        onClick = { viewModel.sendTestNotification() },
                        modifier = Modifier.fillMaxWidth().testTag("btn_send_test_notification"),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        icon = Icons.Default.Send
                    )

                    OutlinedButton(
                        onClick = { viewModel.resetReminderSettings() },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_reset_reminders"),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Varsayılanlara Dön")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ReminderToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    PremiumCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
        if (trailing != null && checked) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                trailing()
            }
        }
    }
}

@Composable
fun TimePickerButton(time: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    val (hour, minute) = time.split(":").map { it.toInt() }

    OutlinedButton(
        onClick = {
            TimePickerDialog(context, { _, h, m ->
                val formattedTime = String.format("%02d:%02d", h, m)
                onTimeSelected(formattedTime)
            }, hour, minute, true).show()
        },
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(time, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DayPickerButton(day: Int, onDaySelected: (Int) -> Unit) {
    val days = listOf("Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi")
    var showMenu by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { showMenu = true },
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(days[day - 1], fontWeight = FontWeight.Bold)
        }

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            days.forEachIndexed { index, name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onDaySelected(index + 1)
                        showMenu = false
                    }
                )
            }
        }
    }
}
