package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.PremiumButton
import com.example.ui.theme.GoldAccent

@Composable
fun FeatureLockedScreen(featureName: String, requiredPackageName: String, onUpgradeClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GoldAccent
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = featureName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Bu özellik şu anki paketinizde bulunmuyor. Bu özelliğe erişmek için $requiredPackageName paketine yükseltmelisiniz.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        PremiumButton(
            text = "Hemen Yükselt",
            onClick = onUpgradeClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = GoldAccent
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = { /* Go back handled by navigation */ }) {
            Text("Daha Sonra")
        }
    }
}
