package com.example.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Animated Number Counter for XP, Scores, and Questions
 */
@Composable
fun AnimatedCounterText(
    targetValue: Int,
    suffix: String = "",
    prefix: String = "",
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.primary,
    fontWeight: FontWeight = FontWeight.Bold,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "AnimatedCounter"
    )

    Text(
        text = "$prefix$animatedValue$suffix",
        style = style,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier
    )
}

/**
 * Animated Double Counter for Net Scores
 */
@Composable
fun AnimatedNetScoreText(
    targetValue: Double,
    suffix: String = " Net",
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.primary,
    fontWeight: FontWeight = FontWeight.Bold,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "AnimatedNetScore"
    )

    Text(
        text = "%.2f%s".format(animatedValue, suffix),
        style = style,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier
    )
}

/**
 * Celebratory Particle / Confetti Burst Overlay
 */
@Composable
fun ConfettiParticleOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 30
) {
    if (!visible) return

    val particles = remember {
        List(particleCount) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 250f + 100f
            val radius = Random.nextFloat() * 6f + 4f
            val color = listOf(
                Color(0xFFF59E0B),
                Color(0xFFD97706),
                Color(0xFF10B981),
                Color(0xFF3B82F6),
                Color(0xFF8B5CF6),
                Color(0xFFEC4899)
            ).random()
            ParticleData(angle, speed, radius, color)
        }
    }

    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            progressAnim.snapTo(0f)
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerPoint = Offset(size.width / 2f, size.height / 3f)
        val p = progressAnim.value

        particles.forEach { particle ->
            val dist = particle.speed * p
            val x = centerPoint.x + Math.cos(particle.angle.toDouble()).toFloat() * dist
            val y = centerPoint.y + Math.sin(particle.angle.toDouble()).toFloat() * dist + (p * p * 120f)
            val alpha = (1f - p).coerceIn(0f, 1f)

            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.radius * (1f - p * 0.3f),
                center = Offset(x, y)
            )
        }
    }
}

private data class ParticleData(
    val angle: Float,
    val speed: Float,
    val radius: Float,
    val color: Color
)

/**
 * Animated Glowing Rank Badge Container with Rotating Light Sweep
 */
@Composable
fun GlowingRankBadge(
    symbol: String,
    badgeLevel: Int,
    primaryColor: Color,
    surfaceColor: Color,
    badgeSize: androidx.compose.ui.unit.Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(badgeSize)
            .scale(scalePulse)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().drawWithContent {
            drawContent()
        }) {
            val sweepBrush = Brush.sweepGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.8f),
                    primaryColor.copy(alpha = 0.1f),
                    primaryColor.copy(alpha = 0.9f)
                )
            )
            drawCircle(
                brush = sweepBrush,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
        }

        Box(
            modifier = Modifier
                .size(badgeSize - 8.dp)
                .background(surfaceColor, shape = CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

/**
 * Floating XP Gain Animation Effect (+25 XP floating up & fading)
 */
@Composable
fun FloatingXpEffect(
    xpAmount: Int,
    trigger: Boolean,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    if (xpAmount <= 0) return

    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            offsetY.snapTo(0f)
            alpha.snapTo(1f)
            scale.snapTo(0.6f)

            coroutineScope {
                launch {
                    scale.animateTo(1.2f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                }
                launch {
                    offsetY.animateTo(-80f, animationSpec = tween(700, easing = FastOutSlowInEasing))
                }
                launch {
                    delay(400)
                    alpha.animateTo(0f, animationSpec = tween(300))
                    onAnimationEnd()
                }
            }
        }
    }

    if (alpha.value > 0f) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    translationY = offsetY.value
                    this.alpha = alpha.value
                    scaleX = scale.value
                    scaleY = scale.value
                }
        ) {
            Surface(
                color = com.example.ui.theme.Gold,
                shape = CircleShape,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "+$xpAmount XP ✨",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}


