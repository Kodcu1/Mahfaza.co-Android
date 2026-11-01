package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PremiumAnimatedBackground
import com.example.ui.components.StaggeredVerticalEntrance
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: MainViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Sequence states
    var startTextAnimation by remember { mutableStateOf(false) }
    
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // Step 1: Logo enters
        logoAlpha.animateTo(1f, tween(1000))
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        // Step 2: Text begins
        startTextAnimation = true
        
        delay(2500) // Total cinematic time
        
        if (currentUser != null) {
            onNavigateToDashboard()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        PremiumAnimatedBackground()
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Branded Emblem with Pulse/Glow
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                shape = CircleShape,
                color = Color(0xFF233B5D),
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "M",
                        color = Color(0xFFD97706),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (startTextAnimation) {
                StaggeredVerticalEntrance(index = 0) {
                    Text(
                        text = "MAHFAZA",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color(0xFF233B5D),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 6.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                StaggeredVerticalEntrance(index = 1) {
                    Text(
                        text = "PLANINI KUR. DİSİPLİNİNİ KORU.",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                StaggeredVerticalEntrance(index = 2) {
                    Text(
                        text = "HEDEFİNE ULAŞ.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            AnimatedVisibility(
                visible = startTextAnimation,
                enter = fadeIn(tween(1000, 500))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFFD97706),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
