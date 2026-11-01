package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.CouponValidationResult
import com.example.model.PackageCatalog
import com.example.model.UserRole
import com.example.ui.components.PremiumButton
import com.example.ui.components.bouncyClick
import com.example.ui.theme.*
import com.example.util.PriceFormatter
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    packageId: String,
    role: UserRole,
    viewModel: MainViewModel,
    onPaymentSuccess: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val dynamicConfigs by viewModel.allPackageConfigs.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Package details from Room DB
    val dynamicConfig = dynamicConfigs.find { it.id == packageId }
    val staticPkg = PackageCatalog.getPackageById(packageId)

    val packageName = dynamicConfig?.name?.takeIf { it.isNotBlank() } ?: staticPkg?.name ?: "Mahfaza Standart Paket"
    
    // Billing Period State: "MONTHLY" or "YEARLY"
    var billingPeriod by remember { mutableStateOf("MONTHLY") }

    val basePriceMonthlyStr = dynamicConfig?.price?.takeIf { it.isNotBlank() } ?: staticPkg?.price ?: "₺1.200 / Ay"
    val basePriceYearlyStr = dynamicConfig?.yearlyPrice?.takeIf { it.isNotBlank() } ?: staticPkg?.yearlyPrice ?: "₺2.400 / Yıl"

    // Numeric parsing helper
    fun parsePrice(priceStr: String): Double {
        val clean = priceStr.replace("₺", "").replace(".", "").replace(",", ".").trim()
        val digitsAndDot = clean.takeWhile { it.isDigit() || it == '.' }
        return digitsAndDot.toDoubleOrNull() ?: 1200.0
    }

    val basePriceAmount = remember(billingPeriod, basePriceMonthlyStr, basePriceYearlyStr) {
        if (billingPeriod == "YEARLY") parsePrice(basePriceYearlyStr) else parsePrice(basePriceMonthlyStr)
    }

    // Coupon State
    var couponInput by remember { mutableStateOf("") }
    var appliedCouponResult by remember { mutableStateOf<CouponValidationResult.Success?>(null) }
    var couponErrorText by remember { mutableStateOf<String?>(null) }
    var isValidatingCoupon by remember { mutableStateOf(false) }

    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentErrorText by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Calculated prices
    val discountAmount = appliedCouponResult?.discountAmount ?: 0.0
    val finalPrice = (basePriceAmount - discountAmount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paket Satın Al & Test Ödemesi",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("payment_back_button")) {
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Test Mode Banner Notice
            Surface(
                color = Gold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Science,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Test Ödeme Modu Aktif",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = "Gerçek kredi kartı bilgisi istenmez. 'Test Ödemesini Tamamla' butonuna basarak paketi anında aktifleştirebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Billing Period Toggle Selector
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Abonelik Periyodu Seçin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = billingPeriod == "MONTHLY",
                            onClick = {
                                billingPeriod = "MONTHLY"
                                appliedCouponResult = null
                                couponErrorText = null
                            },
                            label = {
                                Text("Aylık Abonelik", fontWeight = FontWeight.Bold)
                            },
                            leadingIcon = {
                                if (billingPeriod == "MONTHLY") Icon(Icons.Default.Check, contentDescription = null)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_billing_monthly")
                        )

                        FilterChip(
                            selected = billingPeriod == "YEARLY",
                            onClick = {
                                billingPeriod = "YEARLY"
                                appliedCouponResult = null
                                couponErrorText = null
                            },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Yıllık Abonelik", fontWeight = FontWeight.Bold)
                                    Text("Avantajlı", style = MaterialTheme.typography.labelSmall, color = Gold)
                                }
                            },
                            leadingIcon = {
                                if (billingPeriod == "YEARLY") Icon(Icons.Default.Star, contentDescription = null, tint = Gold)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_billing_yearly")
                        )
                    }
                }
            }

            // Coupon Code Section Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Gold)
                        Text(
                            text = "İndirim Kodun Var Mı?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }

                    if (appliedCouponResult == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = {
                                    couponInput = it.uppercase()
                                    couponErrorText = null
                                },
                                placeholder = { Text("Örn: SERKAN20") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_coupon_code")
                            )

                            Button(
                                onClick = {
                                    if (couponInput.isBlank()) {
                                        couponErrorText = "Lütfen geçerli bir indirim kodu giriniz."
                                        return@Button
                                    }
                                    isValidatingCoupon = true
                                    couponErrorText = null
                                    scope.launch {
                                        val result = viewModel.validateCoupon(
                                            code = couponInput,
                                            packageId = packageId,
                                            billingPeriod = billingPeriod,
                                            originalPrice = basePriceAmount
                                        )
                                        isValidatingCoupon = false
                                        when (result) {
                                            is CouponValidationResult.Success -> {
                                                appliedCouponResult = result
                                                couponErrorText = null
                                            }
                                            is CouponValidationResult.Error -> {
                                                appliedCouponResult = null
                                                couponErrorText = result.message
                                            }
                                        }
                                    }
                                },
                                enabled = !isValidatingCoupon && couponInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("btn_apply_coupon")
                            ) {
                                if (isValidatingCoupon) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Uygula", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (couponErrorText != null) {
                            Text(
                                text = couponErrorText!!,
                                color = Danger,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        // Applied Coupon Success Banner
                        Surface(
                            color = SoftSuccessSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Success.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success)
                                    Column {
                                        Text(
                                            text = "Kupon Uygulandı: ${appliedCouponResult?.coupon?.code}",
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNavy,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = appliedCouponResult?.description ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Success
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        appliedCouponResult = null
                                        couponInput = ""
                                    }
                                ) {
                                    Text("Kaldır", color = Danger, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Order Summary Card Breakdown
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sipariş Özet Tablosu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$packageName (${if (billingPeriod == "YEARLY") "Yıllık" else "Aylık"})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryNavy
                        )
                        Text(
                            text = PriceFormatter.format(basePriceAmount),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }

                    if (discountAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "İndirim (${appliedCouponResult?.coupon?.code})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "-${PriceFormatter.format(discountAmount)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ödenecek Toplam Tutar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = PriceFormatter.format(finalPrice),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                    }
                }
            }

            if (paymentErrorText != null) {
                Surface(
                    color = SoftDangerSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Danger.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = paymentErrorText!!,
                        color = Danger,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Test Payment Button
            PremiumButton(
                text = if (isProcessingPayment) "İşleniyor..." else "Test Ödemesini Tamamla (${PriceFormatter.format(finalPrice)})",
                onClick = {
                    isProcessingPayment = true
                    paymentErrorText = null
                    scope.launch {
                        val success = viewModel.processTestPayment(
                            packageId = packageId,
                            billingPeriod = billingPeriod,
                            appliedCouponCode = appliedCouponResult?.coupon?.code,
                            originalPrice = basePriceAmount
                        )
                        isProcessingPayment = false
                        if (success) {
                            showSuccessDialog = true
                        } else {
                            paymentErrorText = "Ödeme işlemi gerçekleştirilemedi. Lütfen tekrar deneyiniz."
                        }
                    }
                },
                containerColor = EmeraldGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .testTag("btn_complete_test_payment")
                    .bouncyClick { }
            )

            Text(
                text = "Mahfaza.co Test Altyapısı • Gerçek kart çekimi ve banka işlemi yapılmaz.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onPaymentSuccess()
            },
            icon = {
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Abonelik Başarıyla Aktifleşti!",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$packageName (${if (billingPeriod == "YEARLY") "Yıllık" else "Aylık"})",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tebrikler! Test ödemeniz onaylandı. Paket lisansınız ve haklarınız hesabınıza tanımlandı. Yönetim paneline yönlendiriliyorsunuz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPaymentSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_payment_success_continue")
                ) {
                    Text("Ana Panele Devam Et", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
