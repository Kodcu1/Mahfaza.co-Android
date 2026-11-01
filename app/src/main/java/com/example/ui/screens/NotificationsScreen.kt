package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppNotification
import com.example.model.NotificationType
import com.example.ui.components.EmptyState
import com.example.ui.components.PremiumCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.bouncyClick
import com.example.ui.theme.*
import androidx.compose.ui.graphics.Brush
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    viewModel: MainViewModel,
    onNavigateToRoute: ((String) -> Unit)? = null
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) } // 0: Tümü, 1: Okunmamışlar

    val filteredList = remember(notifications, selectedTab) {
        if (selectedTab == 1) notifications.filter { !it.isRead } else notifications
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bildirim Merkezi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
                if (unreadCount > 0) {
                    Text(
                        text = "$unreadCount okunmamış bildiriminiz var",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gold
                    )
                }
            }

            if (unreadCount > 0) {
                OutlinedButton(
                    onClick = { viewModel.markAllNotificationsAsRead() },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryNavy)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tümünü Oku", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Tüm Bildirimler (${notifications.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Okunmamış ($unreadCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (filteredList.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        color = Gold.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Şu an buralar çok sessiz…",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedTab == 1) "Okunmamış bildirimin yok." else "Yeni maceralar ve güzel haberler burada seni bekliyor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        NotificationCard(
                            notification = item,
                            onClick = {
                                viewModel.markNotificationAsRead(item.id)
                                if (!item.navigationRoute.isNullOrBlank()) {
                                    onNavigateToRoute?.invoke(item.navigationRoute)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val (icon, iconColor, iconBg) = when (notification.type) {
        NotificationType.TASK_ASSIGNED -> Triple(Icons.Default.Assignment, PrimaryNavy, SoftGoldSurface)
        NotificationType.STUDY_SUBMITTED -> Triple(Icons.Default.Task, Gold, SoftGoldSurface)
        NotificationType.STUDY_APPROVED -> Triple(Icons.Default.Verified, Success, SoftSuccessSurface)
        NotificationType.STUDY_REJECTED -> Triple(Icons.Default.Error, Danger, SoftDangerSurface)
        NotificationType.LEVEL_UP -> Triple(Icons.Default.EmojiEvents, Gold, SoftGoldSurface)
        NotificationType.NEW_MESSAGE -> Triple(Icons.Default.Chat, TealAccent, SoftTealSurface)
        NotificationType.REWARD_REQUESTED, NotificationType.REWARD_STATUS -> Triple(Icons.Default.CardGiftcard, VioletAccent, SoftVioletSurface)
        else -> Triple(Icons.Default.Notifications, PrimaryNavy, SoftGoldSurface)
    }

    val timeFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("tr")) }
    val formattedTime = remember(notification.timestamp) { timeFormat.format(Date(notification.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 3.dp else 1.dp),
        border = if (!notification.isRead) androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.5f)) else androidx.compose.foundation.BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (!notification.isRead) {
                        Brush.linearGradient(
                            colors = listOf(SoftGoldSurface, AlternateBackground)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(CardSurface, CardSurface)
                        )
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = iconBg,
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryText
                    )
                }

                if (!notification.isRead) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Gold)
                    )
                }
            }
        }
    }
}
