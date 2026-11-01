package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PackageConfigEntity
import com.example.model.Package
import com.example.model.PackageCatalog
import com.example.model.UserRole
import com.example.ui.components.PremiumButton
import com.example.ui.components.bouncyClick
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

data class DisplayPackage(
    val id: String,
    val name: String,
    val price: String,
    val yearlyPrice: String,
    val studentCapacity: Int,
    val isFree: Boolean,
    val isFeatured: Boolean,
    val badgeText: String?,
    val features: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageSelectionScreen(
    role: UserRole,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit = {},
    onPackageSelected: (String) -> Unit
) {
    val dynamicConfigs by viewModel.allPackageConfigs.collectAsStateWithLifecycle()
    var isYearlyBilling by remember { mutableStateOf(false) }

    // Merge static entitlements with dynamic prices/names/capacities from Room DB
    val baseCatalog = if (role == UserRole.STUDENT) {
        PackageCatalog.getAllStudentPackages()
    } else {
        PackageCatalog.getAllCoachPackages()
    }

    val displayPackages = remember(dynamicConfigs, role, isYearlyBilling) {
        baseCatalog.map { staticPkg ->
            val dynamicMatch = dynamicConfigs.find { it.id == staticPkg.id }
            val name = dynamicMatch?.name?.takeIf { it.isNotBlank() } ?: staticPkg.name
            val monthlyPrice = dynamicMatch?.price?.takeIf { it.isNotBlank() } ?: staticPkg.price
            val yearlyPrice = dynamicMatch?.yearlyPrice?.takeIf { it.isNotBlank() } ?: "₺${(staticPkg.price.filter { it.isDigit() }.toIntOrNull() ?: 0) * 10}/yıl"
            val capacity = if (dynamicMatch != null && dynamicMatch.studentCapacity > 0) dynamicMatch.studentCapacity else staticPkg.studentCapacity
            val isFree = staticPkg.isFree || monthlyPrice.contains("0") || monthlyPrice.contains("Ücretsiz", ignoreCase = true)
            val isFeatured = staticPkg.id.contains("pro", ignoreCase = true) || staticPkg.id.contains("plus", ignoreCase = true)
            val badge = when {
                staticPkg.id.contains("pro", ignoreCase = true) -> "EN POPÜLER"
                staticPkg.id.contains("plus", ignoreCase = true) -> "ÖNERİLEN"
                staticPkg.id.contains("kurum", ignoreCase = true) -> "VIP KURUMSAL"
                isFree -> "BAŞLANGIÇ"
                else -> null
            }

            DisplayPackage(
                id = staticPkg.id,
                name = name,
                price = if (isYearlyBilling && !isFree) yearlyPrice else monthlyPrice,
                yearlyPrice = yearlyPrice,
                studentCapacity = capacity,
                isFree = isFree,
                isFeatured = isFeatured,
                badgeText = badge,
                features = staticPkg.entitlements.map { it.displayName }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (role == UserRole.STUDENT) "Öğrenci Paketleri" else "Koç Paketleri",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("package_selection_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri Dön"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = PrimaryNavy,
                    navigationIconContentColor = PrimaryNavy
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = if (role == UserRole.STUDENT)
                    "Akademik hedeflerinize ve deneme takip ihtiyaçlarınıza en uygun paketi seçin."
                else
                    "Öğrenci kontenjanınıza ve koçluk operasyonlarınıza en uygun lisansı belirleyin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Billing Period Toggle (Monthly vs Yearly)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = if (!isYearlyBilling) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        shadowElevation = if (!isYearlyBilling) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isYearlyBilling = false }
                            .testTag("btn_billing_monthly")
                    ) {
                        Text(
                            text = "Aylık Ödeme",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (!isYearlyBilling) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isYearlyBilling) PrimaryNavy else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    Surface(
                        color = if (isYearlyBilling) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        shadowElevation = if (isYearlyBilling) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isYearlyBilling = true }
                            .testTag("btn_billing_yearly")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Yıllık",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isYearlyBilling) FontWeight.Bold else FontWeight.Medium,
                                color = if (isYearlyBilling) PrimaryNavy else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = EmeraldGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "%25 İndirim",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Package Cards List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayPackages, key = { it.id }) { item ->
                    PremiumPackageCard(
                        item = item,
                        isYearly = isYearlyBilling,
                        onSelect = { onPackageSelected(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumPackageCard(
    item: DisplayPackage,
    isYearly: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = when {
        item.isFeatured -> Gold
        !item.isFree -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val containerBackground = when {
        item.isFeatured -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("package_card_${item.id}")
            .bouncyClick(onClick = onSelect),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isFeatured) 4.dp else 1.dp),
        border = BorderStroke(if (item.isFeatured) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Title and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (item.isFeatured) SoftGoldSurface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (item.isFeatured) Icons.Default.WorkspacePremium else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (item.isFeatured) Gold else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                }

                if (item.badgeText != null) {
                    Surface(
                        color = if (item.isFeatured) Gold else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Price Row
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = item.price,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryNavy
                )
                if (!item.isFree && !item.price.contains("/")) {
                    Text(
                        text = if (isYearly) " /yıl" else " /ay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                    )
                }
            }

            // Capacity indicator for coaches
            if (item.studentCapacity > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = SoftGoldSurface,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${item.studentCapacity} Öğrenci Kapasitesi",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // Features list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.features.take(5).forEach { feat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feat,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Selection Button
            PremiumButton(
                text = "Bu Paketi Seç",
                onClick = onSelect,
                containerColor = if (item.isFeatured) Gold else PrimaryNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_select_package_${item.id}")
            )
        }
    }
}
