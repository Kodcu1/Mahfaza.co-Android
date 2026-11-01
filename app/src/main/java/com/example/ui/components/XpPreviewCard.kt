package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Gold
import com.example.ui.theme.SoftGoldSurface

@Composable
fun XpPreviewCard(
    proposedXp: Int,
    earnedToday: Int = 0,
    requiresCoachApproval: Boolean = false,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val remainingCap = (100 - earnedToday).coerceAtLeast(0)
    val actualEstimate = minOf(proposedXp, remainingCap)
    val isCappedOut = remainingCap == 0

    Surface(
        color = if (isCappedOut) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else SoftGoldSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isCappedOut) Color.Gray.copy(alpha = 0.3f) else Gold.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = if (isCappedOut) Color.Gray.copy(alpha = 0.2f) else Gold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "XP",
                            tint = if (isCappedOut) Color.Gray else Gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (requiresCoachApproval) "Tahmini Onay XP'si:" else "Kazandırması Beklenen XP:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+$actualEstimate XP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCappedOut) Color.Gray else Gold
                        )
                    }

                    val infoText = when {
                        isCappedOut -> "Günlük XP sınırına ulaşıldı (100/100 XP)."
                        requiresCoachApproval -> "Koç onayından sonra hesabına eklenecektir."
                        subtitle != null -> subtitle
                        else -> "İşlem tamamlandığında anında hesabına yansır."
                    }

                    Text(
                        text = infoText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCappedOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (requiresCoachApproval) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Koç Onaylı",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
