package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CouponEntity
import com.example.model.UserRole
import com.example.ui.components.PremiumAnimatedBackground
import com.example.ui.components.PremiumButton
import com.example.ui.components.PremiumCard
import com.example.ui.components.premiumClickableScale
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponManagementScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val coupons by viewModel.allCoupons.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var couponToEdit by remember { mutableStateOf<CouponEntity?>(null) }
    var couponToDelete by remember { mutableStateOf<CouponEntity?>(null) }

    // Role Enforcement check
    if (currentUser?.role != UserRole.ADMIN) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Danger,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Erişim Engellendi",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
                Text(
                    text = "Bu alan yalnızca Sistem Yöneticileri (Admin) tarafından erişilebilir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Text("Geri Dön")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "İndirim Kodu Yönetimi",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("admin_coupon_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri Dön",
                            tint = PrimaryNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Yeni Kod Oluştur", fontWeight = FontWeight.Bold) },
                containerColor = Gold,
                contentColor = PrimaryNavy,
                modifier = Modifier.testTag("btn_add_new_coupon")
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            PremiumAnimatedBackground()

            if (coupons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = PrimaryNavy.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Henüz Kupon Kod Oluşturulmadı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = "Kampanyalarınız ve özel indirimleriniz için sağ alttaki butondan yeni kod ekleyebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Aktif Kampanyalar",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold
                                    )
                                    Text(
                                        text = "Toplam ${coupons.size} tanımlı indirim kodu",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                Surface(
                                    color = EmeraldGreen,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "${coupons.count { it.isActive }} Aktif",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    items(coupons, key = { it.code }) { coupon ->
                        CouponItemCard(
                            coupon = coupon,
                            onToggleActive = { viewModel.toggleCouponActiveStatus(coupon.code) },
                            onEdit = { couponToEdit = coupon },
                            onDelete = { couponToDelete = coupon }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add or Edit Coupon Dialog
    if (showAddDialog || couponToEdit != null) {
        AddEditCouponDialog(
            initialCoupon = couponToEdit,
            onDismiss = {
                showAddDialog = false
                couponToEdit = null
            },
            onSave = { newCoupon ->
                if (couponToEdit != null) {
                    viewModel.updateCoupon(newCoupon)
                } else {
                    viewModel.addCoupon(newCoupon)
                }
                showAddDialog = false
                couponToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (couponToDelete != null) {
        AlertDialog(
            onDismissRequest = { couponToDelete = null },
            title = { Text("Kodu Sil", fontWeight = FontWeight.Bold) },
            text = { Text("'${couponToDelete?.code}' kodunu silmek istediğinizden emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        couponToDelete?.let { viewModel.deleteCoupon(it.code) }
                        couponToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { couponToDelete = null }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun CouponItemCard(
    coupon: CouponEntity,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val now = System.currentTimeMillis()
    val isExpired = now > coupon.endDate
    val isLimitReached = coupon.usedCount >= coupon.totalUsageLimit

    val statusText = when {
        !coupon.isActive -> "PASİF"
        isExpired -> "SÜRESİ DOLMUŞ"
        isLimitReached -> "LİMİT DOLDU"
        else -> "AKTİF"
    }

    val statusBg = when {
        !coupon.isActive -> Color.Gray.copy(alpha = 0.2f)
        isExpired || isLimitReached -> SoftDangerSurface
        else -> SoftSuccessSurface
    }

    val statusColor = when {
        !coupon.isActive -> Color.DarkGray
        isExpired || isLimitReached -> Danger
        else -> Success
    }

    PremiumCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("coupon_item_${coupon.code}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Gold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = coupon.code,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = coupon.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldGreen
                        ),
                        modifier = Modifier.testTag("switch_active_${coupon.code}")
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = PrimaryNavy)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Danger)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("İndirim Tutarı", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    val discountStr = if (coupon.discountType == "PERCENTAGE") "%${coupon.discountValue.toInt()}" else "${coupon.discountValue.toInt()} TL"
                    Text(discountStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Kullanım Durumu", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${coupon.usedCount} / ${coupon.totalUsageLimit}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Geçerlilik Bitiş", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(dateFormat.format(Date(coupon.endDate)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
                }
            }

            if (!coupon.applicableBillingPeriod.isNullOrBlank() || !coupon.applicablePackageId.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val restriction = listOfNotNull(
                            coupon.applicableBillingPeriod?.let { "Kapsam: $it Ödeme" },
                            coupon.applicablePackageId?.let { "Paket: $it" }
                        ).joinToString(" • ")
                        Text(restriction, style = MaterialTheme.typography.labelSmall, color = PrimaryNavy)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCouponDialog(
    initialCoupon: CouponEntity?,
    onDismiss: () -> Unit,
    onSave: (CouponEntity) -> Unit
) {
    var code by remember { mutableStateOf(initialCoupon?.code ?: "") }
    var discountType by remember { mutableStateOf(initialCoupon?.discountType ?: "PERCENTAGE") }
    var discountValueStr by remember { mutableStateOf(initialCoupon?.discountValue?.toInt()?.toString() ?: "20") }
    var totalLimitStr by remember { mutableStateOf(initialCoupon?.totalUsageLimit?.toString() ?: "100") }
    var perUserLimitStr by remember { mutableStateOf(initialCoupon?.perUserLimit?.toString() ?: "1") }
    var durationDaysStr by remember { mutableStateOf("90") }
    var billingPeriodRestriction by remember { mutableStateOf(initialCoupon?.applicableBillingPeriod ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialCoupon == null) "Yeni İndirim Kodu Oluştur" else "İndirim Kodunu Düzenle",
                fontWeight = FontWeight.Bold,
                color = PrimaryNavy
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Kupon Kodu") },
                    placeholder = { Text("Örn: SERKAN20") },
                    singleLine = true,
                    enabled = initialCoupon == null, // Code is PrimaryKey
                    modifier = Modifier.fillMaxWidth().testTag("input_admin_coupon_code")
                )

                Text("İndirim Tipi", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = discountType == "PERCENTAGE",
                        onClick = { discountType = "PERCENTAGE" },
                        label = { Text("Yüzde (%)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = discountType == "FIXED_TRY",
                        onClick = { discountType = "FIXED_TRY" },
                        label = { Text("Sabit TL (₺)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = discountValueStr,
                    onValueChange = { discountValueStr = it.filter { char -> char.isDigit() } },
                    label = { Text(if (discountType == "PERCENTAGE") "İndirim Yüzdesi (%)" else "İndirim Miktarı (TL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_admin_coupon_value")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalLimitStr,
                        onValueChange = { totalLimitStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Toplam Limit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = perUserLimitStr,
                        onValueChange = { perUserLimitStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Kişi Başı Limit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = durationDaysStr,
                    onValueChange = { durationDaysStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Geçerlilik Süresi (Gün)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Ödeme Periyodu Kapsamı", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = billingPeriodRestriction.isEmpty(),
                        onClick = { billingPeriodRestriction = "" },
                        label = { Text("Tümü") }
                    )
                    FilterChip(
                        selected = billingPeriodRestriction == "MONTHLY",
                        onClick = { billingPeriodRestriction = "MONTHLY" },
                        label = { Text("Aylık") }
                    )
                    FilterChip(
                        selected = billingPeriodRestriction == "YEARLY",
                        onClick = { billingPeriodRestriction = "YEARLY" },
                        label = { Text("Yıllık") }
                    )
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Danger, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanCode = code.trim().uppercase()
                    val dVal = discountValueStr.toDoubleOrNull()
                    val tLimit = totalLimitStr.toIntOrNull()
                    val pLimit = perUserLimitStr.toIntOrNull()
                    val days = durationDaysStr.toLongOrNull() ?: 90L

                    if (cleanCode.isBlank()) {
                        errorMsg = "Kupon kodu boş olamaz."
                    } else if (dVal == null || dVal <= 0) {
                        errorMsg = "Geçerli bir indirim değeri giriniz."
                    } else if (tLimit == null || tLimit <= 0) {
                        errorMsg = "Geçerli bir kullanım limiti giriniz."
                    } else {
                        val now = System.currentTimeMillis()
                        val endDate = if (initialCoupon != null) initialCoupon.endDate else now + (days * 86400000L)

                        val newCoupon = CouponEntity(
                            code = cleanCode,
                            discountType = discountType,
                            discountValue = dVal,
                            startDate = initialCoupon?.startDate ?: now - 86400000L,
                            endDate = endDate,
                            totalUsageLimit = tLimit,
                            perUserLimit = pLimit ?: 1,
                            usedCount = initialCoupon?.usedCount ?: 0,
                            isActive = initialCoupon?.isActive ?: true,
                            applicablePackageId = initialCoupon?.applicablePackageId,
                            applicableBillingPeriod = billingPeriodRestriction.ifBlank { null }
                        )
                        onSave(newCoupon)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                modifier = Modifier.testTag("btn_save_coupon")
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
