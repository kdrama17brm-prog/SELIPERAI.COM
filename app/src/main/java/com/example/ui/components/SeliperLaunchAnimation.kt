package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Refined, Short, Premium Startup Animation (~1.3 seconds total)
 * 1. Shows the SELIPER AI logo
 * 2. Subtle glow/pulse + gentle breathing & slight smooth rotation
 * 3. Displays "SELIPER AI"
 * 4. Smoothly fades into the main chat screen without making the user wait
 */
@Composable
fun SeliperOpeningSplashOverlay(
    onFinished: () -> Unit
) {
    val contentAlpha = remember { Animatable(0f) }
    val breathingScale = remember { Animatable(0.90f) }
    val subtleRotation = remember { Animatable(-4f) }
    val glowPulse = remember { Animatable(0.6f) }
    var showTitle by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            contentAlpha.animateTo(1f, animationSpec = tween(320, easing = FastOutSlowInEasing))
        }
        launch {
            breathingScale.animateTo(1.03f, animationSpec = tween(750, easing = FastOutSlowInEasing))
            breathingScale.animateTo(1.0f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        }
        launch {
            subtleRotation.animateTo(0f, animationSpec = tween(850, easing = FastOutSlowInEasing))
        }
        launch {
            glowPulse.animateTo(1.05f, animationSpec = tween(700, easing = FastOutSlowInEasing))
            glowPulse.animateTo(0.85f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        }
        delay(140)
        showTitle = true
        delay(1050) // Total duration ~1.2 seconds — snappy, calm, and non-intrusive
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF280947), // Deep Violet
                        Color(0xFF7B104E), // Vibrant Pink-Magenta
                        Color(0xFF9E1128), // Crimson Red
                        Color(0xFF160529)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient radial glow behind the logo
        Canvas(modifier = Modifier.size(260.dp)) {
            val radius = (size.minDimension / 2f) * glowPulse.value
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF4081).copy(alpha = 0.28f),
                        Color(0xFFE1306C).copy(alpha = 0.16f),
                        Color(0xFFFD1D1D).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = radius
                ),
                radius = radius
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(contentAlpha.value)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(breathingScale.value)
                    .rotate(subtleRotation.value),
                contentAlignment = Alignment.Center
            ) {
                SeliperNeonCircleLogo(
                    size = 104.dp,
                    showInnerText = false
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(320)),
                exit = fadeOut(tween(200))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SELIPER AI",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.4.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Animated cyan-blue-purple shimmer bar (used for thinking/streaming indicator)
 */
@Composable
fun SeliperShimmerWaveBar(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer_wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_phase"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00E5FF),
                        Color(0xFF2979FF),
                        Color(0xFFAA00FF),
                        Color(0xFF00E5FF)
                    ),
                    start = Offset(phase - 400f, 0f),
                    end = Offset(phase, 0f)
                )
            )
    )
}

/**
 * Animated 4-Pointed Sparkle Orb Indicator for live AI thinking / streaming states
 */
@Composable
fun AnimatedSparkleThinkingOrb(
    size: Dp = 24.dp
) {
    val transition = rememberInfiniteTransition(label = "sparkle_orb")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing)
        ),
        label = "orb_rotation"
    )
    val scale by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_scale"
    )

    Canvas(
        modifier = Modifier
            .size(size)
            .scale(scale)
    ) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r = this.size.minDimension * 0.44f
        val inner = r * 0.28f

        rotate(degrees = rotation, pivot = Offset(cx, cy)) {
            val starPath = Path().apply {
                moveTo(cx, cy - r)
                quadraticTo(cx + inner, cy - inner, cx + r, cy)
                quadraticTo(cx + inner, cy + inner, cx, cy + r)
                quadraticTo(cx - inner, cy + inner, cx - r, cy)
                quadraticTo(cx - inner, cy - inner, cx, cy - r)
                close()
            }
            drawPath(
                path = starPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00E5FF),
                        Color(0xFF2979FF),
                        Color(0xFFD52BFF)
                    )
                )
            )
        }
    }
}
