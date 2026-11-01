package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * A wrapper that animates children in a staggered vertical sequence.
 */
@Composable
fun StaggeredVerticalEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    delayMillis: Int = 100,
    durationMillis: Int = 500,
    content: @Composable () -> Unit
) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * delayMillis).toLong())
        visible.value = true
    }

    AnimatedVisibility(
        visible = visible.value,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(durationMillis)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
                ),
        content = { content() }
    )
}

/**
 * Animates a float value (like progress or XP) smoothly when it changes.
 */
@Composable
fun animateIntAsState(targetValue: Int): State<Int> {
    return animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "int_count_up"
    )
}

/**
 * A decorative animated background for premium screens.
 */
@Composable
fun PremiumAnimatedBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_glow")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFD97706).copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = center.copy(x = center.x + (offset % 200f) - 100f),
            radius = size.minDimension
        )
        drawRect(brush = brush)
    }
}

/**
 * Scale animation for buttons and interactive cards.
 */
@Composable
fun Modifier.premiumClickableScale(): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

private suspend fun delay(time: Long) {
    kotlinx.coroutines.delay(time)
}
