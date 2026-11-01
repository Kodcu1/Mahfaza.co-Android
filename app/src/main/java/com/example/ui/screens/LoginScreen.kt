package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.*
import com.example.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        PremiumAnimatedBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StaggeredVerticalEntrance(index = 0) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground_asset_1793110713689),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            StaggeredVerticalEntrance(index = 1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Mahfaza.co",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Planını Kur. Disiplinini Koru. Hedefine Ulaş.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(36.dp))
            
            StaggeredVerticalEntrance(index = 2) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorText = null },
                    label = { Text("E-posta") },
                    placeholder = { Text("yks@mahfaza.co veya demokoc@mahfaza.co") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            StaggeredVerticalEntrance(index = 3) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorText = null },
                    label = { Text("Şifre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Şifreyi Gizle" else "Şifreyi Göster")
                        }
                    }
                )
            }

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            StaggeredVerticalEntrance(index = 4) {
                PremiumButton(
                    text = if (isLoading) "Giriş Yapılıyor..." else "Giriş Yap",
                    onClick = { 
                        if (email.isBlank()) {
                            errorText = "Lütfen e-posta adresinizi girin."
                        } else {
                            isLoading = true
                            viewModel.login(email) { success ->
                                isLoading = false
                                if (!success) {
                                    errorText = "Kullanıcı bulunamadı. Lütfen hızlı demo butonlarını deneyin."
                                } else {
                                    onLoginSuccess()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().premiumClickableScale()
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            StaggeredVerticalEntrance(index = 5) {
                GoogleSignInButton(
                    modifier = Modifier.fillMaxWidth(),
                    onAuthSuccess = {
                        viewModel.handleGoogleAuthSuccess {
                            onLoginSuccess()
                        }
                    },
                    onAuthError = { error ->
                        errorText = error
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            StaggeredVerticalEntrance(index = 6) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hesabın yok mu?", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = onRegisterClick) {
                        Text("Yeni Hesap Oluştur", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            // Fast Demo Shortcuts Section
            StaggeredVerticalEntrance(index = 7) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ HIZLI DEMO GİRİŞİ",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tek tıkla simülasyon hesabıyla hemen giriş yapın:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { email = "yks@mahfaza.co" },
                                modifier = Modifier.weight(1f).premiumClickableScale(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Öğrenci", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { email = "demokoc@mahfaza.co" },
                                modifier = Modifier.weight(1f).premiumClickableScale(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Koç", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { email = "serkankocak551@gmail.com" },
                                modifier = Modifier.weight(1.3f).premiumClickableScale(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Yönetici (Serkan)", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
