package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardStoreScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val rewards by viewModel.rewards.collectAsStateWithLifecycle()
    val requests by viewModel.rewardRequests.collectAsStateWithLifecycle()

    val studentRequests = requests.filter { it.studentId == user?.id }
    val currentXp = user?.currentXp ?: 0

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ödül Mağazası", "Taleplerim (${studentRequests.size})")

    var rewardToPurchase by remember { mutableStateOf<Reward?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        SectionHeader(title = "Ödül Mağazası")

        // Animated XP Balance Card with Gold Surface
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGoldSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.35f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Surface(
                    color = Gold.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Kullanılabilir Bakiyen", style = MaterialTheme.typography.labelSmall, color = PrimaryText.copy(alpha = 0.7f))
                    AnimatedCounter(
                        targetValue = currentXp,
                        suffix = " XP",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Mağaza Aktif",
                        style = MaterialTheme.typography.labelSmall,
                        color = Success,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                0 -> StoreContent(
                    rewards = rewards,
                    userXp = currentXp,
                    onRequestPurchase = { reward ->
                        rewardToPurchase = reward
                    }
                )
                1 -> RequestsContent(studentRequests)
            }
        }
    }

    if (rewardToPurchase != null) {
        val reward = rewardToPurchase!!
        AlertDialog(
            onDismissRequest = { rewardToPurchase = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Gold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ödülü Talep Et")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${reward.name} ödülünü talep etmek istiyor musunuz?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Bu ödül için ${reward.xpCost} XP harcanacak. Koçunuz onayladığında ödül teslim edilecek.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.redeemReward(reward.id)
                        Toast.makeText(context, "${reward.name} talebiniz koçunuza iletildi!", Toast.LENGTH_SHORT).show()
                        rewardToPurchase = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text("Talep Et (${reward.xpCost} XP)")
                }
            },
            dismissButton = {
                TextButton(onClick = { rewardToPurchase = null }) { Text("Vazgeç") }
            }
        )
    }
}

@Composable
fun StoreContent(
    rewards: List<Reward>,
    userXp: Int,
    onRequestPurchase: (Reward) -> Unit
) {
    val categories = listOf("Tümü", "Kırtasiye ve Kitap", "Mola ve Etkinlik", "Dijital Abonelik", "Kişisel Hedefler", "Özel Ödüller")
    var selectedCategory by remember { mutableStateOf("Tümü") }

    fun getCategory(reward: Reward): String {
        val text = "${reward.name} ${reward.description}".lowercase()
        return when {
            text.contains("kitap") || text.contains("kırtasiye") || text.contains("defter") || text.contains("kalem") -> "Kırtasiye ve Kitap"
            text.contains("mola") || text.contains("oyun") || text.contains("yemek") || text.contains("etkinlik") -> "Mola ve Etkinlik"
            text.contains("abonelik") || text.contains("dijital") || text.contains("premium") -> "Dijital Abonelik"
            text.contains("hedef") || text.contains("başarı") -> "Kişisel Hedefler"
            else -> "Özel Ödüller"
        }
    }

    val filteredRewards = remember(rewards, selectedCategory) {
        if (selectedCategory == "Tümü") rewards
        else rewards.filter { getCategory(it) == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            categories.forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    text = {
                        Text(
                            text = cat,
                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategory == cat) Gold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredRewards.isEmpty()) {
            EmptyState(
                message = if (selectedCategory == "Tümü") "Mağazada şu an listelenen ödül bulunmuyor." else "$selectedCategory kategorisinde henüz ödül bulunmuyor.",
                icon = Icons.Default.Storefront
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredRewards, key = { it.id }) { reward ->
                    val canAfford = userXp >= reward.xpCost
                    val inStock = reward.stock > 0

                    AnimatedEntry {
                        PremiumCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (inStock) SoftGoldSurface else SoftDangerSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            tint = if (inStock) Gold else Danger,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reward.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "${reward.xpCost} XP",
                                            color = Gold,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = "• Kalan: ${reward.stock} Adet",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (inStock) TealAccent else Danger
                                        )
                                    }
                                }

                                if (!inStock) {
                                    PremiumStatusChip(text = "Tükendi", color = Danger, surfaceColor = SoftDangerSurface)
                                } else if (!canAfford) {
                                    PremiumStatusChip(text = "Yetersiz XP", color = Danger, surfaceColor = SoftDangerSurface)
                                } else {
                                    PremiumStatusChip(text = "Alınabilir", color = Success, surfaceColor = SoftSuccessSurface)
                                }
                            }

                            Text(
                                text = reward.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = { onRequestPurchase(reward) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = canAfford && inStock,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canAfford && inStock) Gold else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (!inStock) "Stok Tükendi" else if (canAfford) "Ödülü Talep Et" else "Yetersiz XP (${reward.xpCost - userXp} XP Gerekiyor)"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun RequestsContent(requests: List<RewardRequest>) {
    if (requests.isEmpty()) {
        EmptyState(
            message = "Henüz bir ödül talebiniz bulunmuyor. Mağazadaki ödülleri inceleyip puanlarınızla talep oluşturabilirsiniz.",
            icon = Icons.Default.ReceiptLong
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(requests, key = { it.id }) { request ->
                AnimatedEntry {
                    RewardRequestCard(request = request, onApprove = {}, onReject = {})
                }
            }
        }
    }
}
