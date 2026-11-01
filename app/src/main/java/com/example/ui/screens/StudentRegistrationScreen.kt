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
import com.example.model.EducationLevel
import com.example.model.UserRole
import com.example.ui.components.PremiumButton
import com.example.ui.components.bouncyClick
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegistrationScreen(
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

    var selectedStudentTrack by remember { mutableStateOf("YKS") }
    var coachCodeInput by remember { mutableStateOf("") }

    var termsAccepted by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Öğrenci Kaydı",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("student_register_back_button")) {
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
            // Header information badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Öğrenci Hesabı Oluştur",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = "Kaydolduktan sonra size en uygun koçluk paketini seçeceksiniz.",
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
                        placeholder = { Text("Örn: Zeynep Kaya") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_student_name")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta Adresi") },
                        placeholder = { Text("Örn: zeynep@mahfaza.co") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_student_email")
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
                            .testTag("input_student_phone")
                    )
                }
            }

            // Segment 2: Hedef Sınav Alanı
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
                                Icon(Icons.Default.AutoGraph, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "2. Hedef Sınav Alanı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }

                    Text(
                        text = "Hazırlandığınız sınavı seçin. Müfredat, ödevler ve denemeleriniz bu alana göre planlanacaktır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("LGS", "YKS", "KPSS").forEach { exam ->
                            val isSelected = selectedStudentTrack == exam
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStudentTrack = exam },
                                label = { Text(exam, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_student_track_$exam")
                            )
                        }
                    }
                }
            }

            // Segment 3: Davet Kodu
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
                        Surface(color = SoftSuccessSurface, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = Success, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "3. Davet Kodu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }

                    Text(
                        text = "Koçunuzdan veya kurumunuzdan aldığınız davet kodunu giriniz (MHFZ-XXXX veya KOC-XXXX).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = coachCodeInput,
                        onValueChange = { coachCodeInput = it.uppercase() },
                        label = { Text("Davet Kodu") },
                        placeholder = { Text("Örn: MHFZ-8921") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_student_invite_code")
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
                            .testTag("input_student_password")
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
                            .testTag("input_student_password_confirm")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            modifier = Modifier.testTag("checkbox_student_terms")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kullanım koşullarını ve KVKK aydınlatma metnini onaylıyorum.",
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
                        errorText = "Devam etmek için kullanım koşullarını onaylamalısınız."
                    } else {
                        errorText = null
                        isLoading = true
                        scope.launch {
                            val level = when (selectedStudentTrack) {
                                "LGS" -> EducationLevel.LGS_8TH
                                "KPSS" -> EducationLevel.KPSS
                                else -> EducationLevel.YKS_TYT_AYT
                            }

                            val success = viewModel.register(
                                name = name.trim(),
                                email = email.trim(),
                                role = UserRole.STUDENT,
                                level = level,
                                packageId = "std_free", // Default baseline; user will choose and pay in subsequent screens
                                examTracks = listOf(selectedStudentTrack),
                                subscriptionPlan = "PENDING_PAYMENT",
                                coachCodeInput = coachCodeInput.trim().takeIf { it.isNotBlank() }
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
                    .testTag("btn_student_register_submit")
                    .bouncyClick { }
            )

            Text(
                text = "Mahfaza.co • Güvenli ve Disiplinli Akademik Koçluk",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
