package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.components.PremiumButton
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.Gold
import com.example.ui.theme.SoftGoldSurface

@Composable
fun SelectLevelScreen(
    onSelectRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkNavy,
                        Color(0xFF1E293B),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Hero Header Badge
        Surface(
            color = Gold.copy(alpha = 0.2f),
            shape = CircleShape,
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MAHFAZA.CO DİJİTAL AKADEMİ",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mahfaza'ya Hoş Geldin!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sınav maratonunda en güçlü yardımcın. Başlamak için üyelik türünü seç:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Role Cards
        RoleSelectionCard(
            title = "Öğrenci Hesabı",
            subtitle = "LGS, YKS & KPSS sınavlarına hazırlanan öğrenciler için",
            icon = Icons.Default.School,
            benefits = listOf(
                "Kişiselleştirilmiş Haftalık Çalışma Programı",
                "Branş Bazlı Ayrıntılı Net ve Konu Analizi",
                "Oyunlaştırılmış XP, Seviye ve Ödül Mağazası",
                "Pomodoro Sayaç & Fotoğraflı Görev Onayı"
            ),
            isSelected = selectedRole == UserRole.STUDENT,
            onClick = { selectedRole = UserRole.STUDENT }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoleSelectionCard(
            title = "Koç Hesabı",
            subtitle = "Öğrencilerine yön veren öğretmen ve eğitim koçları için",
            icon = Icons.Default.Psychology,
            benefits = listOf(
                "Çoklu Öğrenci Takip Çizelgesi & Portföy",
                "Tek Tıkla Görev Atama & Kanıt Onay Merkezi",
                "Gelişmiş Öğrenci 360° Karne Raporları",
                "Veli Görüşmeleri & PDF Dışa Aktarma"
            ),
            isSelected = selectedRole == UserRole.COACH,
            onClick = { selectedRole = UserRole.COACH }
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Action Button
        Button(
            onClick = { onSelectRole(selectedRole) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = DarkNavy
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (selectedRole == UserRole.STUDENT) "Öğrenci Olarak Devam Et" else "Koç Olarak Devam Et",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    benefits: List<String>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF1E293B) else DarkNavy.copy(alpha = 0.8f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Gold else Color.Gray.copy(alpha = 0.3f)
        ),
        shadowElevation = if (isSelected) 8.dp else 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isSelected) Gold else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) DarkNavy else Gold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray.copy(alpha = 0.8f)
                        )
                    }
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Gold,
                        unselectedColor = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(14.dp))

            benefits.forEach { benefit ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isSelected) Gold else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}
