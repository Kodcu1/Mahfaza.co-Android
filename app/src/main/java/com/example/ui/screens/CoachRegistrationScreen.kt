package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.model.UserRole
import com.example.ui.components.PremiumButton
import com.example.ui.components.bouncyClick
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachRegistrationScreen(
    viewModel: MainViewModel,
    onSuccess: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var selectedCoachTracks by remember { mutableStateOf(listOf("LGS", "YKS")) }
    var invitationCode by remember { mutableStateOf("") }

    var termsAccepted by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Koç Kaydı",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("coach_register_back_button")) {
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
            // Header information banner
            Surface(
                color = SoftGoldSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Profesyonel Koç Hesabı",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = "Profilinizi oluşturun; öğrenci kapasitenize uygun koçluk paketini bir sonraki adımda belirleyeceksiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Segment 1: Kimlik Bilgileri (Identity: Name, Email, Phone)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("1. Kimlik Bilgileri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ad Soyad") },
                        placeholder = { Text("Örn: Serkan Koçak") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_coach_name")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta Adresi") },
                        placeholder = { Text("Örn: serkan@mahfaza.co") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_coach_email")
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefon Numarası") },
                        placeholder = { Text("05XX XXX XX XX") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_coach_phone")
                    )
                }
            }

            // Segment 2: Uzmanlık Sınav Grupları
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = SoftVioletSurface, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "2. Uzmanlık Sınav Grupları",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }

                    Text(
                        text = "Öğrenci koçluğu vereceğiniz alanları seçin (birden fazla seçilebilir).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("LGS", "YKS", "KPSS").forEach { exam ->
                            val isChecked = selectedCoachTracks.contains(exam)
                            FilterChip(
                                selected = isChecked,
                                onClick = {
                                    selectedCoachTracks = if (isChecked) {
                                        if (selectedCoachTracks.size > 1) selectedCoachTracks - exam else selectedCoachTracks
                                    } else {
                                        selectedCoachTracks + exam
                                    }
                                },
                                label = { Text(exam, fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (isChecked) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_coach_track_$exam")
                            )
                        }
                    }
                }
            }

            // Segment 3: Kurum / Yetkili Davet Kodu
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = SoftGoldSurface, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "3. Kurum / Yetkili Davet Kodu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }

                    Text(
                        text = "Mahfaza.co kurumsal koçluk lisans davet kodunuzu girin (MHFZ-XXXX).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = invitationCode,
                        onValueChange = { invitationCode = it.uppercase() },
                        label = { Text("Koç Davet Kodu") },
                        placeholder = { Text("Örn: MHFZ-KOC9") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_coach_invite_code")
                    )
                }
            }

            // Segment 4: Hesap Güvenliği
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "4. Hesap Güvenliği",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Şifre") },
                        placeholder = { Text("En az 8 karakter") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_coach_password")
                    )

                    OutlinedTextField(
                        value = passwordConfirm,
                        onValueChange = { passwordConfirm = it },
                        label = { Text("Şifre Tekrar") },
                        placeholder = { Text("Şifrenizi doğrulayın") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_coach_password_confirm")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            modifier = Modifier.testTag("checkbox_coach_terms")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Koçluk hizmet sözleşmesini ve KVKK aydınlatma metnini onaylıyorum.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (errorText != null) {
                Surface(
                    color = SoftDangerSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Danger.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorText!!,
                        color = Danger,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            PremiumButton(
                text = if (isLoading) "Hesap Oluşturuluyor..." else "Devam Et (Paket Seçimi)",
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        errorText = "Lütfen zorunlu alanların tümünü doldurun."
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorText = "Lütfen geçerli bir e-posta adresi girin."
                    } else if (password.length < 8) {
                        errorText = "Şifre güvenliğiniz için en az 8 karakter olmalıdır."
                    } else if (password != passwordConfirm) {
                        errorText = "Girdiğiniz şifreler birbiriyle eşleşmiyor."
                    } else if (!termsAccepted) {
                        errorText = "Devam etmek için sözleşme ve koşulları onaylamalısınız."
                    } else {
                        errorText = null
                        isLoading = true
                        scope.launch {
                            val success = viewModel.register(
                                name = name.trim(),
                                email = email.trim(),
                                role = UserRole.COACH,
                                level = null,
                                packageId = "c_start", // Baseline; updated upon package selection & payment
                                examTracks = selectedCoachTracks,
                                subscriptionPlan = "PENDING_PAYMENT",
                                coachCodeInput = null
                            )

                            isLoading = false
                            if (success) {
                                onSuccess()
                            } else {
                                errorText = "Bu e-posta adresiyle kayıtlı bir hesap zaten mevcut."
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_coach_register_submit")
                    .bouncyClick { }
            )

            Text(
                text = "Mahfaza.co • Profesyonel Koçluk Yönetim Merkezi",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
