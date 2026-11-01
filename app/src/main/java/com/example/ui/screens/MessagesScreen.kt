package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.UserRole
import com.example.ui.components.EmptyState
import com.example.ui.components.PremiumCard
import com.example.ui.components.SectionHeader
import com.example.util.StudentAvatar
import com.example.viewmodel.MainViewModel

@Composable
fun MessagesScreen(
    viewModel: MainViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val allCoaches by viewModel.allCoaches.collectAsStateWithLifecycle()

    val conversationPartners = if (currentUser?.role == UserRole.COACH) {
        val assigned = currentUser?.assignedStudents ?: emptyList()
        allStudents.filter { assigned.contains(it.id) }
    } else {
        allCoaches
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        SectionHeader(title = "Mesajlar")

        if (conversationPartners.isEmpty()) {
            EmptyState(
                message = if (currentUser?.role == UserRole.COACH)
                    "Henüz portföyünüzde atanmış öğrenci bulunmuyor. Mesajlaşmak için önce öğrenci ekleyin."
                else
                    "Henüz atanmış bir koçunuz bulunmuyor.",
                icon = Icons.Default.ChatBubbleOutline
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(conversationPartners, key = { it.id }) { partner ->
                    PremiumCard(onClick = { onNavigateToChat(partner.id) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StudentAvatar(name = partner.name, avatarUri = partner.avatarUri, size = 48.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = partner.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = partner.educationLevel?.displayName ?: partner.title ?: "Sohbeti Başlat",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
