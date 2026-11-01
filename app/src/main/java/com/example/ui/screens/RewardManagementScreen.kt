package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardManagementScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val requests by viewModel.rewardRequests.collectAsStateWithLifecycle()
    val rewards by viewModel.rewards.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    val pendingCount = requests.count { it.status == RewardRequestStatus.PENDING }
    val tabs = listOf(
        if (pendingCount > 0) "Ödül Talepleri ($pendingCount)" else "Ödül Talepleri",
        "Ödüller (${rewards.size})"
    )

    var showAddRewardDialog by remember { mutableStateOf(false) }
    var rewardToEdit by remember { mutableStateOf<Reward?>(null) }
    var rewardToRemove by remember { mutableStateOf<Reward?>(null) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = {
                        rewardToEdit = null
                        showAddRewardDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Yeni Ödül Ekle") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionHeader(title = "Ödül ve Mağaza Yönetimi")

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize().animateContentSize()) {
                when (selectedTab) {
                    0 -> RewardRequestsList(
                        requests = requests,
                        onApprove = { request ->
                            viewModel.updateRewardRequestStatus(request.id, RewardRequestStatus.APPROVED)
                            Toast.makeText(context, "${request.rewardName} talebi onaylandı.", Toast.LENGTH_SHORT).show()
                        },
                        onReject = { request ->
                            viewModel.updateRewardRequestStatus(request.id, RewardRequestStatus.REJECTED)
                            Toast.makeText(context, "${request.rewardName} talebi reddedildi, ${request.xpCost} XP öğrenciye iade edildi.", Toast.LENGTH_SHORT).show()
                        }
                    )
                    1 -> RewardInventoryList(
                        rewards = rewards,
                        onEdit = { reward ->
                            rewardToEdit = reward
                            showAddRewardDialog = true
                        },
                        onRemove = { reward ->
                            rewardToRemove = reward
                        }
                    )
                }
            }
        }
    }

    if (showAddRewardDialog) {
        AddEditRewardDialog(
            initialReward = rewardToEdit,
            onDismiss = {
                showAddRewardDialog = false
                rewardToEdit = null
            },
            onConfirm = { name, desc, xp, stock ->
                if (rewardToEdit == null) {
                    viewModel.addReward(name, desc, xp, stock)
                    Toast.makeText(context, "Ödül mağazaya eklendi.", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateReward(rewardToEdit!!.copy(name = name, description = desc, xpCost = xp, stock = stock))
                    Toast.makeText(context, "Ödül güncellendi.", Toast.LENGTH_SHORT).show()
                }
                showAddRewardDialog = false
                rewardToEdit = null
            }
        )
    }

    if (rewardToRemove != null) {
        AlertDialog(
            onDismissRequest = { rewardToRemove = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Danger)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ödülü Kaldır")
                }
            },
            text = {
                Text(
                    text = "${rewardToRemove!!.name} adlı ödülü mağazadan kaldırmak istediğinize emin misiniz? Öğrencilerin geçmiş kazanım kayıtları korunacaktır.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReward(rewardToRemove!!.id)
                        Toast.makeText(context, "${rewardToRemove!!.name} kaldırıldı.", Toast.LENGTH_SHORT).show()
                        rewardToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) {
                    Text("Ödülü Kaldır")
                }
            },
            dismissButton = {
                TextButton(onClick = { rewardToRemove = null }) { Text("Vazgeç") }
            }
        )
    }
}

@Composable
fun RewardRequestsList(
    requests: List<RewardRequest>,
    onApprove: (RewardRequest) -> Unit,
    onReject: (RewardRequest) -> Unit
) {
    if (requests.isEmpty()) {
        EmptyState(
            message = "Şu anda onay bekleyen herhangi bir ödül talebi bulunmuyor.",
            icon = Icons.Default.CardGiftcard
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(requests, key = { it.id }) { request ->
                AnimatedEntry {
                    RewardRequestCard(
                        request = request,
                        onApprove = { onApprove(request) },
                        onReject = { onReject(request) }
                    )
                }
            }
        }
    }
}

@Composable
fun RewardRequestCard(
    request: RewardRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    PremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = SoftVioletSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = VioletAccent)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.rewardName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Öğrenci: ${request.studentName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = request.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(request.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${request.xpCost} XP",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )
            }

            if (request.status == RewardRequestStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reddet & İade")
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Success),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Onayla")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: RewardRequestStatus) {
    val (color, surfaceColor, text) = when(status) {
        RewardRequestStatus.PENDING -> Triple(Gold, SoftGoldSurface, "Onay Bekliyor")
        RewardRequestStatus.APPROVED -> Triple(Success, SoftSuccessSurface, "Onaylandı")
        RewardRequestStatus.REJECTED -> Triple(Danger, SoftDangerSurface, "Reddedildi")
    }
    PremiumStatusChip(text = text, color = color, surfaceColor = surfaceColor)
}

@Composable
fun RewardInventoryList(
    rewards: List<Reward>,
    onEdit: (Reward) -> Unit,
    onRemove: (Reward) -> Unit
) {
    val categories = listOf("Tümü", "Kırtasiye & Kitap", "Mola & Etkinlik", "Dijital", "Özel")
    var selectedCategory by remember { mutableStateOf("Tümü") }

    fun getCategory(reward: Reward): String {
        val text = (reward.name + " " + reward.description).lowercase()
        return when {
            text.contains("kitap") || text.contains("kırtasiye") || text.contains("defter") || text.contains("kalem") -> "Kırtasiye & Kitap"
            text.contains("mola") || text.contains("kahve") || text.contains("sinema") || text.contains("etkinlik") || text.contains("tatil") || text.contains("oyun") -> "Mola & Etkinlik"
            text.contains("spotify") || text.contains("netflix") || text.contains("youtube") || text.contains("dijital") || text.contains("abonelik") -> "Dijital"
            else -> "Özel"
        }
    }

    val filteredRewards = remember(rewards, selectedCategory) {
        if (selectedCategory == "Tümü") rewards else rewards.filter { getCategory(it) == selectedCategory }
    }

    if (rewards.isEmpty()) {
        EmptyState(
            message = "Henüz tanımlanmış bir ödül bulunmuyor. Yeni bir ödül tanımlamak için aşağıdaki butonu kullanabilirsiniz.",
            icon = Icons.Default.EmojiEvents
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                categories.forEach { cat ->
                    val count = if (cat == "Tümü") rewards.size else rewards.count { getCategory(it) == cat }
                    val isSelected = selectedCategory == cat
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        text = {
                            Text(
                                text = "$cat ($count)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredRewards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$selectedCategory kategorisinde henüz ödül bulunmuyor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredRewards, key = { it.id }) { reward ->
                        AnimatedEntry {
                            PremiumCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = SoftGoldSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(reward.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = getCategory(reward),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(reward.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(
                                                text = "${reward.xpCost} XP",
                                                fontWeight = FontWeight.Bold,
                                                color = Gold,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Text(
                                                text = "Stok: ${reward.stock} Adet",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (reward.stock > 0) TealAccent else Danger
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onEdit(reward) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    IconButton(onClick = { onRemove(reward) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Ödülü Kaldır", tint = Danger)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRewardDialog(
    initialReward: Reward?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialReward?.name ?: "") }
    var description by remember { mutableStateOf(initialReward?.description ?: "") }
    var xpCost by remember { mutableStateOf(initialReward?.xpCost?.toString() ?: "500") }
    var stock by remember { mutableStateOf(initialReward?.stock?.toString() ?: "10") }

    val xpVal = xpCost.toIntOrNull()
    val stockVal = stock.toIntOrNull()
    val isValid = name.isNotBlank() && description.isNotBlank() && xpVal != null && xpVal >= 0 && stockVal != null && stockVal >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialReward == null) "Yeni Ödül Ekle" else "Ödülü Düzenle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ödül Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = xpCost,
                    onValueChange = { xpCost = it },
                    label = { Text("XP Bedeli (Pozitif Sayı)") },
                    singleLine = true,
                    isError = xpVal == null || xpVal < 0,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stok Adedi (Pozitif Sayı)") },
                    singleLine = true,
                    isError = stockVal == null || stockVal < 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(name.trim(), description.trim(), xpVal!!, stockVal!!)
                    }
                },
                enabled = isValid
            ) {
                Text(if (initialReward == null) "Yeni Ödül Ekle" else "Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        }
    )
}
