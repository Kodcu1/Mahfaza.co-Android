package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.model.*
import com.example.ui.animation.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.LevelCalculator
import com.example.util.RankTier
import com.example.util.StudentAvatar
import com.example.viewmodel.MainViewModel
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    viewModel: MainViewModel,
    onFeatureLocked: (String, String) -> Unit,
    onNavigateToWeeklyProgram: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val exams by viewModel.exams.collectAsStateWithLifecycle()

    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasksCount = tasks.size
    val completionRatio = if (totalTasksCount > 0) completedTasksCount.toFloat() / totalTasksCount else 0f
    val currentStreak = user?.streak ?: 0
    val currentXp = user?.currentXp ?: 0
    val lifetimeXp = user?.lifetimeXp ?: 0

    val levelInfo = LevelCalculator.calculateLevel(lifetimeXp)

    var celebratedLevel by remember { mutableStateOf(levelInfo.levelNumber) }
    var showLevelUpDialog by remember { mutableStateOf(false) }
    var showRanksSheet by remember { mutableStateOf(false) }

    // Floating XP effect state
    var triggeredXpAmount by remember { mutableStateOf(0) }
    var showFloatingXp by remember { mutableStateOf(false) }

    LaunchedEffect(levelInfo.levelNumber) {
        if (levelInfo.levelNumber > celebratedLevel) {
            showLevelUpDialog = true
            celebratedLevel = levelInfo.levelNumber
        }
    }

    // Upgraded Celebration Dialog with Confetti and Glowing Badge
    if (showLevelUpDialog) {
        AlertDialog(
            onDismissRequest = { showLevelUpDialog = false },
            icon = {
                GlowingRankBadge(
                    symbol = levelInfo.badgeSymbol,
                    badgeLevel = levelInfo.levelNumber,
                    primaryColor = Color(levelInfo.tier.primaryColorHex),
                    surfaceColor = Color(levelInfo.tier.surfaceColorHex),
                    badgeSize = 72.dp
                )
            },
            title = {
                Text(
                    text = "TEBRİKLER! SEVİYE ATLADIN! 🎉",
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryNavy,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Seviye ${levelInfo.levelNumber}: ${levelInfo.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(levelInfo.tier.primaryColorHex)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(levelInfo.tier.surfaceColorHex),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(levelInfo.tier.borderHex))
                    ) {
                        Text(
                            text = "${levelInfo.tier.displayName} • Unvan Açıldı!",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(levelInfo.tier.primaryColorHex),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Açılan Perk: ${levelInfo.perkDescription}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Çalışmalarına aynı kararlılıkla devam et, hedefine her geçen gün daha da yaklaşıyorsun!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = SecondaryText
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLevelUpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(levelInfo.tier.primaryColorHex)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Harika! Yolculuğa Devam Et 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PremiumAnimatedBackground()
        
        ConfettiParticleOverlay(visible = showLevelUpDialog)

        // Floating XP Gain Visual Effect Overlay
        if (showFloatingXp) {
            FloatingXpEffect(
                xpAmount = triggeredXpAmount,
                trigger = showFloatingXp,
                modifier = Modifier.align(Alignment.Center),
                onAnimationEnd = { showFloatingXp = false }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Premium Hero Welcome Banner
            item {
                AnimatedEntry {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFFFDF9), Color(0xFFFEF3C7))
                                    )
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(Gold)
                                    .align(Alignment.TopCenter)
                            )
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StudentAvatar(
                                        name = user?.name ?: "Öğrenci",
                                        avatarUri = user?.avatarUri,
                                        size = 56.dp
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = Gold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        ) {
                                            Text(
                                                text = "AKADEMİK ÖĞRENCİ PANELİ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryNavy,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "Merhaba, ${user?.name ?: "Öğrenci"}!",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNavy
                                        )
                                        Text(
                                            text = "${user?.educationLevel?.displayName ?: "LGS & YKS"} • Planını Kur. Disiplinini Koru.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    XPBadge(xp = currentXp)
                                }
                            }
                        }
                    }
                }
            }

            // 1.1 Günün Motivasyonu Card
            item {
                val todayQuote = remember { com.example.util.MotivationQuotes.getTodayStudentQuote(context) }
                AnimatedEntry(delayMillis = 30) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.35f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFFFDF9), Color(0xFFFFF8E7))
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Gold.copy(alpha = 0.2f),
                                            shape = CircleShape,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = Gold,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Günün Motivasyonu",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNavy
                                        )
                                    }
                                    Text(
                                        text = "✨ Günlük İlham",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "“$todayQuote”",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryNavy,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (onNavigateToWeeklyProgram != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = onNavigateToWeeklyProgram,
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Bugünün Hedefini Belirle", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1.15 Günün Vur-Kaç Görevi Card with Animated Morphing States & Mandatory Proof
            item {
                val vurKacTask = tasks.find { it.title.contains("Vur-Kaç", ignoreCase = true) || it.topic.contains("Vur-Kaç", ignoreCase = true) }
                val state = when {
                    vurKacTask == null -> "ACTIVE"
                    !vurKacTask.isApproved -> "PENDING"
                    else -> "COMPLETED"
                }

                var photoUri by remember { mutableStateOf<String?>(null) }

                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri: Uri? ->
                    if (uri != null && user != null) {
                        val savedPath = com.example.util.AvatarUtils.saveAvatarToInternalStorage(context, uri, "vurkac_${user!!.id}_${System.currentTimeMillis()}")
                        if (savedPath != null) {
                            photoUri = savedPath
                        }
                    }
                }

                val infiniteTransition = rememberInfiniteTransition(label = "vur_kac_glow")
                val borderColor by infiniteTransition.animateColor(
                    initialValue = Gold,
                    targetValue = VioletAccent,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "border_color"
                )

                AnimatedEntry(delayMillis = 35) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (state == "ACTIVE") {
                                    Modifier.bouncyClick {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                } else Modifier
                            ),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (state) {
                                "ACTIVE" -> SoftGoldSurface
                                "PENDING" -> Color(0xFFFEF3C7)
                                else -> Color(0xFFECFDF5)
                            }
                        ),
                        border = BorderStroke(
                            width = if (state == "ACTIVE") 2.dp else 1.dp,
                            color = when (state) {
                                "ACTIVE" -> borderColor
                                "PENDING" -> Color(0xFFF59E0B)
                                else -> Success
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        AnimatedContent(
                            targetState = state,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f) togetherWith
                                        fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.95f)
                            },
                            label = "VurKacMorph"
                        ) { targetState ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                when (targetState) {
                                    "ACTIVE" -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = Gold,
                                                    shape = CircleShape,
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            Icons.Default.FlashOn,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "Günün Vur-Kaç Görevi ⚡",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryNavy
                                                    )
                                                    Text(
                                                        text = "Hızlı Soru Serisi • +35 XP Ödüllü",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = SecondaryText
                                                    )
                                                }
                                            }
                                            Surface(
                                                color = Gold.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "⚡ AKTİF",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Gold,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "Matematik Türev & Parabol Hızlı Tekrarı (15 Soru). Kanıt yükle, koç onayından sonra XP'yi kap!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PrimaryText,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Kanıt Yükle & XP Kap 📸", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    "PENDING" -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                                    shape = CircleShape,
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            Icons.Default.HourglassTop,
                                                            contentDescription = null,
                                                            tint = Color(0xFFD97706),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "⏳ Koç Onayı Bekleniyor...",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF92400E)
                                                    )
                                                    Text(
                                                        text = "Vur-Kaç kanıtınız başarıyla iletildi",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFFB45309)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Koçunuz fotoğrafı inceleyip onayladığında +35 XP hesabınıza eklenecektir.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF78350F)
                                        )
                                    }

                                    "COMPLETED" -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = Success.copy(alpha = 0.2f),
                                                    shape = CircleShape,
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            Icons.Default.Verified,
                                                            contentDescription = null,
                                                            tint = Success,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "✅ Görev Başarıyla Tamamlandı!",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Success
                                                    )
                                                    Text(
                                                        text = "+35 XP Onaylandı ve Eklendi",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = PrimaryText
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Tebrikler! Vur-Kaç görevini başarıyla tamamladın ve ödülünü aldın.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SecondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (photoUri != null && user != null) {
                    AlertDialog(
                        onDismissRequest = { photoUri = null },
                        title = { Text("Vur-Kaç Kanıtını Gönder", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Çalışma kanıtınız eklendi. Koç onayına göndermek istiyor musunuz?")
                                Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp))) {
                                    Image(
                                        painter = rememberAsyncImagePainter(photoUri),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val newTask = Task(
                                        id = UUID.randomUUID().toString(),
                                        title = "Günün Vur-Kaç Görevi: Matematik Türev & Parabol",
                                        subject = "Matematik",
                                        topic = "Vur-Kaç Hızlı Soru Çözümü (15 Soru)",
                                        targetQuestionCount = 15,
                                        completedQuestionCount = 15,
                                        isCompleted = true,
                                        priority = TaskPriority.HIGH,
                                        xpReward = 35,
                                        dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
                                        photoProofUri = photoUri,
                                        isApproved = false
                                    )
                                    viewModel.addTask(user!!.id, newTask)
                                    triggeredXpAmount = 35
                                    showFloatingXp = true
                                    Toast.makeText(context, "Vur-Kaç kanıtı başarıyla koç onayına gönderildi!", Toast.LENGTH_SHORT).show()
                                    photoUri = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                            ) {
                                Text("Onaya Gönder", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { photoUri = null }) {
                                Text("İptal")
                            }
                        }
                    )
                }
            }

            // 1.2 Upgraded 30-Level Progression & Rank Roadmap Card
            item {
                val tierPrimaryColor = Color(levelInfo.tier.primaryColorHex)
                val tierSurfaceColor = Color(levelInfo.tier.surfaceColorHex)

                AnimatedEntry(delayMillis = 40) {
                    PremiumCard(
                        containerColor = tierSurfaceColor,
                        border = BorderStroke(1.dp, tierPrimaryColor.copy(alpha = 0.4f)),
                        onClick = { showRanksSheet = true }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GlowingRankBadge(
                                        symbol = levelInfo.badgeSymbol,
                                        badgeLevel = levelInfo.levelNumber,
                                        primaryColor = tierPrimaryColor,
                                        surfaceColor = Color.White,
                                        badgeSize = 48.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Surface(
                                            color = tierPrimaryColor.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "${levelInfo.tier.displayName.uppercase()} • SEVİYE ${levelInfo.levelNumber}/30",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = tierPrimaryColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = levelInfo.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNavy
                                        )
                                        Text(
                                            text = "Toplam Kazanılan: $lifetimeXp XP",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = if (!levelInfo.isMaxLevel) "%${(levelInfo.progress * 100).toInt()}" else "MAX",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = tierPrimaryColor
                                )
                            }

                            AnimatedProgressIndicator(
                                progress = levelInfo.progress,
                                color = tierPrimaryColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (!levelInfo.isMaxLevel) "Sonraki Seviye (${levelInfo.maxXp} XP) için ${levelInfo.remainingXpToNextLevel} XP kaldı"
                                    else "Tebrikler! En Yüksek Akademik Unvandasın! 🏆",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = { showRanksSheet = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = tierPrimaryColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.MilitaryTech, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Harita 👑", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Daily Task Progress Card
            item {
                AnimatedEntry(delayMillis = 50) {
                    PremiumCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Günün Çalışma İlerlemesi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$completedTasksCount / $totalTasksCount Görev (%${(completionRatio * 100).toInt()})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (completionRatio >= 1f && totalTasksCount > 0) Success else TealAccent
                                )
                            }

                            AnimatedProgressIndicator(
                                progress = completionRatio,
                                color = if (completionRatio >= 1f && totalTasksCount > 0) Success else TealAccent
                            )
                        }
                    }
                }
            }

            // 3. Streak, Level, and Exam Stats Row
            item {
                AnimatedEntry(delayMillis = 100) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            label = "Çalışma Serisi",
                            value = "$currentStreak Gün",
                            icon = Icons.Default.Whatshot,
                            color = Color(0xFFEA580C),
                            containerColor = SoftGoldSurface,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Rütbe / Seviye",
                            value = "Lvl ${levelInfo.levelNumber}",
                            icon = Icons.Default.MilitaryTech,
                            color = Color(levelInfo.tier.primaryColorHex),
                            containerColor = Color(levelInfo.tier.surfaceColorHex),
                            modifier = Modifier.weight(1f),
                            onClick = { showRanksSheet = true }
                        )
                        StatCard(
                            label = "Toplam Deneme",
                            value = "${exams.size} Sınav",
                            icon = Icons.Default.Assessment,
                            color = VioletAccent,
                            containerColor = SoftVioletSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Real Academic Milestones & Achievements Area
            item {
                AnimatedEntry(delayMillis = 150) {
                    PremiumCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Akademik Başarı Rozetleri",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Rozet Koleksiyonu",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AchievementBadge(
                                    title = "İlk Adım",
                                    desc = "İlk Görev",
                                    icon = Icons.Default.Flag,
                                    isUnlocked = completedTasksCount > 0 || currentStreak > 0,
                                    accentColor = Success,
                                    surfaceColor = SoftSuccessSurface
                                )
                                AchievementBadge(
                                    title = "Disiplin",
                                    desc = "3+ Gün Seri",
                                    icon = Icons.Default.Whatshot,
                                    isUnlocked = currentStreak >= 3,
                                    accentColor = Gold,
                                    surfaceColor = SoftGoldSurface
                                )
                                AchievementBadge(
                                    title = "Denemeci",
                                    desc = "Deneme Çözümü",
                                    icon = Icons.Default.Assessment,
                                    isUnlocked = exams.isNotEmpty(),
                                    accentColor = VioletAccent,
                                    surfaceColor = SoftVioletSurface
                                )
                                AchievementBadge(
                                    title = "XP Ustası",
                                    desc = "500+ XP",
                                    icon = Icons.Default.WorkspacePremium,
                                    isUnlocked = currentXp >= 500,
                                    accentColor = TealAccent,
                                    surfaceColor = SoftTealSurface
                                )
                            }
                        }
                    }
                }
            }

            // 5. Today's Tasks Header
            item {
                SectionHeader(
                    title = "Bugünkü Görevler",
                    actionText = "Tüm Programı Gör",
                    onActionClick = onNavigateToWeeklyProgram
                )
                if (tasks.isEmpty()) {
                    EmptyState(message = "Bugün için planlanmış görev bulunmuyor. Haftalık programından yeni görev ekleyebilirsin.")
                }
            }

            items(tasks.take(4), key = { it.id }) { task ->
                AnimatedEntry {
                    TaskItem(
                        task = task,
                        onToggle = {
                            viewModel.toggleTask(task.id)
                            if (!task.isCompleted) {
                                triggeredXpAmount = task.xpReward
                                showFloatingXp = true
                            }
                        }
                    )
                }
            }

            // 6. Last Exam Result Card
            item {
                SectionHeader(title = "Son Deneme Sonucu")
                val lastExam = exams.lastOrNull()
                if (lastExam != null) {
                    AnimatedEntry {
                        PremiumCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = lastExam.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = lastExam.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${lastExam.subjectResults.sumOf { it.net }.let { String.format("%.2f", it) }} Net",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = lastExam.educationLevel.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    EmptyState(message = "Henüz deneme kaydı bulunmuyor.")
                }
            }

            // 7. Coach Motivation Message
            item {
                SectionHeader(title = "Koçundan Mesaj")
                PremiumCard(containerColor = SoftGoldSurface, border = BorderStroke(1.dp, Gold.copy(alpha = 0.25f))) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Gold.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Harika bir tempo yakaladın! Günlük soru hedeflerini tamamlayıp kanıt yüklemeyi unutma. Hedefine giden yol her gün atılan bu adımlarla örülüyor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Interactive 30-Level & Rank Roadmap Sheet
    if (showRanksSheet) {
        RankHierarchyBottomSheet(
            currentLifetimeXp = lifetimeXp,
            onDismiss = { showRanksSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankHierarchyBottomSheet(
    currentLifetimeXp: Int,
    onDismiss: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTierFilter by remember { mutableStateOf<RankTier?>(null) }
    val allLevels = remember(currentLifetimeXp) { LevelCalculator.getAllLevels(currentLifetimeXp) }
    val currentLevelInfo = remember(currentLifetimeXp) { LevelCalculator.calculateLevel(currentLifetimeXp) }

    val filteredLevels = if (selectedTierFilter == null) allLevels else allLevels.filter { it.tier == selectedTierFilter }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxHeight(0.88f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Gold.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Gold, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Akademik Rütbe & Seviye Haritası",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavy,
                textAlign = TextAlign.Center
            )

            Text(
                text = "30 Akademik Seviye • 6 Rütbe Ligi • Özel Unvanlar & XP Bonusları",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Current user rank quick stats
            Surface(
                color = Color(currentLevelInfo.tier.surfaceColorHex),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(currentLevelInfo.tier.primaryColorHex).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentLevelInfo.badgeSymbol,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Lvl ${currentLevelInfo.levelNumber}: ${currentLevelInfo.title}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(currentLevelInfo.tier.primaryColorHex)
                            )
                        }
                        Text(
                            text = "${currentLevelInfo.tier.displayName} • ${currentLevelInfo.perkDescription}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = Color(currentLevelInfo.tier.primaryColorHex),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "MEVCUT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Tier Filters Row
            ScrollableTabRow(
                selectedTabIndex = if (selectedTierFilter == null) 0 else RankTier.values().indexOf(selectedTierFilter) + 1,
                edgePadding = 0.dp,
                divider = {},
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = selectedTierFilter == null,
                    onClick = { selectedTierFilter = null },
                    text = { Text("Tümü (30)", fontWeight = FontWeight.Bold) }
                )
                RankTier.values().forEach { tier ->
                    Tab(
                        selected = selectedTierFilter == tier,
                        onClick = { selectedTierFilter = tier },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tier.badgeSymbol)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tier.displayName)
                            }
                        }
                    )
                }
            }

            // Levels List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(filteredLevels, key = { it.levelNumber }) { lvl ->
                    val isCurrent = lvl.levelNumber == currentLevelInfo.levelNumber
                    val isUnlocked = currentLifetimeXp >= lvl.minXp
                    val tierColor = Color(lvl.tier.primaryColorHex)
                    val tierSurface = Color(lvl.tier.surfaceColorHex)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) SoftGoldSurface else if (isUnlocked) tierSurface else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) Gold else if (isUnlocked) tierColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (isUnlocked) tierColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = lvl.badgeSymbol,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Lvl ${lvl.levelNumber}: ${lvl.title}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUnlocked) PrimaryNavy else Color.Gray
                                            )
                                        }
                                        Text(
                                            text = "${lvl.tier.displayName} • ${lvl.minXp} XP Gereksinimi",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                when {
                                    isCurrent -> {
                                        Surface(color = Gold, shape = RoundedCornerShape(8.dp)) {
                                            Text(
                                                text = "SEVİYENİZ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    isUnlocked -> {
                                        Surface(color = Success.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                                            Text(
                                                text = "AÇILDI ✅",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Success,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    else -> {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Kilitli",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Unvan & Perk: ${lvl.perkDescription}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUnlocked) tierColor else Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (isCurrent && !lvl.isMaxLevel) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { lvl.progress },
                                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                    color = Gold,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementBadge(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isUnlocked: Boolean,
    accentColor: Color,
    surfaceColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Surface(
            color = if (isUnlocked) surfaceColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = CircleShape,
            modifier = Modifier.size(46.dp),
            border = if (isUnlocked) BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)) else null
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isUnlocked) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) PrimaryText else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            maxLines = 1
        )
    }
}
