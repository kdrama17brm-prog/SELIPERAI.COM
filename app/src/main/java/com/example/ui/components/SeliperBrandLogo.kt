package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders the custom Seliper AI 3D Gradient Ribbon 'S' + Two Sparkle Stars
 * inside the Neon Circle Ring (matching the uploaded Seliper AI Neon Circle Logo).
 */
@Composable
fun SeliperNeonCircleLogo(
    size: Dp = 56.dp,
    showInnerText: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0A175E), Color(0xFF050B38), Color(0xFF030724))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = w * 0.045f

            // 1. Outer Neon Gradient Circle Ring (Cyan -> Royal Blue -> Purple)
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00E5FF),
                        Color(0xFF1976FF),
                        Color(0xFF7C2AE8),
                        Color(0xFFB81DFF)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                radius = (w / 2f) - (strokeW / 2f),
                style = Stroke(width = strokeW)
            )

            val yShift = if (showInnerText) -h * 0.08f else 0f

            // 2. Bottom Ribbon Fold (Deep Indigo-Violet -> Magenta)
            val bottomFold = Path().apply {
                moveTo(w * 0.34f, h * 0.65f + yShift)
                lineTo(w * 0.57f, h * 0.53f + yShift)
                cubicTo(
                    w * 0.65f, h * 0.50f + yShift,
                    w * 0.68f, h * 0.59f + yShift,
                    w * 0.61f, h * 0.64f + yShift
                )
                lineTo(w * 0.42f, h * 0.72f + yShift)
                cubicTo(
                    w * 0.37f, h * 0.74f + yShift,
                    w * 0.32f, h * 0.68f + yShift,
                    w * 0.34f, h * 0.65f + yShift
                )
                close()
            }
            drawPath(
                path = bottomFold,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF5319E8), Color(0xFF8E24AA), Color(0xFFE028FF)),
                    start = Offset(w * 0.34f, h * 0.72f + yShift),
                    end = Offset(w * 0.66f, h * 0.53f + yShift)
                )
            )

            // 3. Middle Ribbon Spine (Royal Blue -> Electric Purple -> Magenta)
            val middleFold = Path().apply {
                moveTo(w * 0.36f, h * 0.39f + yShift)
                cubicTo(
                    w * 0.31f, h * 0.44f + yShift,
                    w * 0.35f, h * 0.52f + yShift,
                    w * 0.42f, h * 0.55f + yShift
                )
                lineTo(w * 0.60f, h * 0.63f + yShift)
                cubicTo(
                    w * 0.67f, h * 0.65f + yShift,
                    w * 0.70f, h * 0.54f + yShift,
                    w * 0.63f, h * 0.49f + yShift
                )
                lineTo(w * 0.45f, h * 0.39f + yShift)
                cubicTo(
                    w * 0.41f, h * 0.37f + yShift,
                    w * 0.38f, h * 0.37f + yShift,
                    w * 0.36f, h * 0.39f + yShift
                )
                close()
            }
            drawPath(
                path = middleFold,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0055FF), Color(0xFF4A3AFF), Color(0xFFD52BFF)),
                    start = Offset(w * 0.34f, h * 0.42f + yShift),
                    end = Offset(w * 0.68f, h * 0.62f + yShift)
                )
            )

            // 4. Top Ribbon Fold (Electric Blue -> Bright Cyan)
            val topFold = Path().apply {
                moveTo(w * 0.35f, h * 0.44f + yShift)
                cubicTo(
                    w * 0.31f, h * 0.36f + yShift,
                    w * 0.35f, h * 0.29f + yShift,
                    w * 0.42f, h * 0.26f + yShift
                )
                lineTo(w * 0.58f, h * 0.19f + yShift)
                cubicTo(
                    w * 0.60f, h * 0.18f + yShift,
                    w * 0.60f, h * 0.22f + yShift,
                    w * 0.59f, h * 0.24f + yShift
                )
                cubicTo(
                    w * 0.58f, h * 0.26f + yShift,
                    w * 0.61f, h * 0.27f + yShift,
                    w * 0.63f, h * 0.28f + yShift
                )
                lineTo(w * 0.43f, h * 0.38f + yShift)
                cubicTo(
                    w * 0.39f, h * 0.40f + yShift,
                    w * 0.37f, h * 0.45f + yShift,
                    w * 0.35f, h * 0.44f + yShift
                )
                close()
            }
            drawPath(
                path = topFold,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0077FF), Color(0xFF00C8FF), Color(0xFF18FFFF)),
                    start = Offset(w * 0.34f, h * 0.44f + yShift),
                    end = Offset(w * 0.63f, h * 0.19f + yShift)
                )
            )

            // 5. Primary 4-Pointed Sparkle Star
            fun createStarPath(cx: Float, cy: Float, r: Float): Path {
                val inner = r * 0.28f
                return Path().apply {
                    moveTo(cx, cy - r)
                    quadraticTo(cx + inner, cy - inner, cx + r, cy)
                    quadraticTo(cx + inner, cy + inner, cx, cy + r)
                    quadraticTo(cx - inner, cy + inner, cx - r, cy)
                    quadraticTo(cx - inner, cy - inner, cx, cy - r)
                    close()
                }
            }

            drawPath(
                path = createStarPath(w * 0.66f, h * 0.21f + yShift, w * 0.065f),
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF2979FF))
                )
            )
            drawPath(
                path = createStarPath(w * 0.72f, h * 0.28f + yShift, w * 0.042f),
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF448AFF), Color(0xFF7C4DFF))
                )
            )
        }

        if (showInnerText && size >= 52.dp) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = size * 0.13f)
            ) {
                SeliperWordmarkText(
                    fontSize = (size.value * 0.16f).sp,
                    seliperColor = Color.White
                )
            }
        }
    }
}

/**
 * Renders the stylized "Seliper AI" wordmark matching the uploaded horizontal ribbon logo:
 * "Seliper" in bold white/dark navy + "AI" in cyan-to-purple gradient.
 */
@Composable
fun SeliperWordmarkText(
    fontSize: TextUnit = 20.sp,
    seliperColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val aiGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00D4FF),
            Color(0xFF1E6BFF),
            Color(0xFF8B2BFF)
        )
    )
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = seliperColor,
                    fontWeight = FontWeight.ExtraBold
                )
            ) {
                append("Seliper ")
            }
            withStyle(
                SpanStyle(
                    brush = aiGradient,
                    fontWeight = FontWeight.ExtraBold
                )
            ) {
                append("AI")
            }
        },
        style = TextStyle(
            fontSize = fontSize,
            letterSpacing = (-0.3).sp
        ),
        modifier = modifier
    )
}

/**
 * Horizontal Seliper AI Gradient Ribbon Logo + Wordmark lockup
 * (matches the second uploaded image: Seliper AI Gradient Ribbon Logo.png)
 */
@Composable
fun SeliperHorizontalRibbonBanner(
    iconSize: Dp = 44.dp,
    fontSize: TextUnit = 24.sp,
    seliperTextColor: Color = Color.White,
    subtitle: String? = null,
    subtitleColor: Color = Color(0xFF00E5FF),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SeliperNeonCircleLogo(
            size = iconSize,
            showInnerText = false
        )
        Column {
            SeliperWordmarkText(
                fontSize = fontSize,
                seliperColor = seliperTextColor
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = subtitleColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
