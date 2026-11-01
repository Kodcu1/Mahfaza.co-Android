package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.util.PriceFormatter
import com.example.viewmodel.MainViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onNavigateToUsers: () -> Unit,
    onNavigateToPackages: () -> Unit,
    onNavigateToCoupons: () -> Unit = {},
    onNavigateToFinancials: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val coaches by viewModel.allCoaches.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val packages by viewModel.allPackageConfigs.collectAsStateWithLifecycle()
    val coupons by viewModel.allCoupons.collectAsStateWithLifecycle()
    
    val totalExpenseValue = expenses.sumOf { it.amount }
    val netBalance = (totalIncome ?: 0.0) - totalExpenseValue

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        PremiumAnimatedBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StaggeredVerticalEntrance(index = 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Süper Admin Komuta Merkezi",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF233B5D)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Akademik platform ve finansal yönetim paneli",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
            
            // Financial Summary Banner
            StaggeredVerticalEntrance(index = 1) {
                Card(
                    modifier = Modifier.fillMaxWidth().premiumClickableScale(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF233B5D))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text("Net Gerçekleşen Bakiye", style = MaterialTheme.typography.labelLarge, color = Color(0xFFF5C451))
                            Surface(
                                color = Color(0xFF059669).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "CANLI SİSTEM",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Text(
                            text = PriceFormatter.format(netBalance),
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Toplam Gelir", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                Text(PriceFormatter.format(totalIncome ?: 0.0), style = MaterialTheme.typography.titleMedium, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text("Toplam Gider", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                Text(PriceFormatter.format(totalExpenseValue), style = MaterialTheme.typography.titleMedium, color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        PremiumButton(
                            text = "Detaylı Finansal Raporlar",
                            onClick = onNavigateToFinancials,
                            containerColor = Color(0xFFD97706),
                            modifier = Modifier.fillMaxWidth().premiumClickableScale()
                        )
                    }
                }
            }
            
            // Stats Grid
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StaggeredVerticalEntrance(index = 2, modifier = Modifier.weight(1f)) {
                    StatItemAnimated("Öğrenciler", students.size, Icons.Default.People, Color(0xFF233B5D))
                }
                StaggeredVerticalEntrance(index = 3, modifier = Modifier.weight(1f)) {
                    StatItemAnimated("Koçlar", coaches.size, Icons.Default.SupervisorAccount, Color(0xFFD97706))
                }
                StaggeredVerticalEntrance(index = 4, modifier = Modifier.weight(1f)) {
                    StatItemAnimated("Kuponlar", coupons.size, Icons.Default.ConfirmationNumber, Color(0xFF059669))
                }
            }
    
            // Action Cards
            StaggeredVerticalEntrance(index = 5) {
                SectionHeader(title = "Yönetim Modülleri")
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StaggeredVerticalEntrance(index = 6) {
                    AdminActionCard("Kullanıcı Yönetimi", "Öğrenci ve koç hesaplarını inceleyin, yetkilendirin.", Icons.Default.People, onNavigateToUsers)
                }
                StaggeredVerticalEntrance(index = 7) {
                    AdminActionCard("Paket ve Fiyat Yönetimi", "Abonelik paketlerini, kapasiteleri ve fiyatları düzenleyin.", Icons.Default.WorkspacePremium, onNavigateToPackages)
                }
                StaggeredVerticalEntrance(index = 8) {
                    AdminActionCard("İndirim Kodu Yönetimi", "İndirim kodları, kullanım limitleri ve kampanyaları yönetin.", Icons.Default.ConfirmationNumber, onNavigateToCoupons)
                }
                StaggeredVerticalEntrance(index = 9) {
                    AdminActionCard("Finansal Raporlar", "Gelir, gider ve bakiye dökümlerini inceleyin.", Icons.Default.Assessment, onNavigateToFinancials)
                }
                StaggeredVerticalEntrance(index = 10) {
                    AdminActionCard("Veritabanı Yedekleme", "Sistem verilerini güvenli JSON formatında yedekleyin veya geri yükleyin.", Icons.Default.Backup, onNavigateToBackup)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
fun StatItemAnimated(label: String, value: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    PremiumCard {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedCounter(targetValue = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF233B5D))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AdminActionCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    PremiumCard(modifier = Modifier.fillMaxWidth().premiumClickableScale(), onClick = onClick) {
        ListItem(
            headlineContent = { Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF233B5D)) },
            supportingContent = { Text(description, color = Color(0xFF64748B)) },
            leadingContent = { Icon(icon, contentDescription = null, tint = Color(0xFF233B5D), modifier = Modifier.size(28.dp)) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
