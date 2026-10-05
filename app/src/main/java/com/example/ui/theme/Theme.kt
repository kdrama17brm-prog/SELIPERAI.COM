package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.model.SeliperTheme

@Immutable
data class SeliperVisualTokens(
    val theme: SeliperTheme,
    val backgroundBrush: Brush,
    val lobbyAccentBrush: Brush,
    val cardBackground: Color,
    val cardBorder: Color,
    val userBubbleColor: Color,
    val userBubbleTextColor: Color,
    val aiBubbleColor: Color,
    val aiBubbleTextColor: Color,
    val accentCyan: Color,
    val accentSecondary: Color,
    val subtleText: Color,
    val codeBlockBackground: Color
)

val LocalSeliperTokens = staticCompositionLocalOf {
    buildVisualTokens(SeliperTheme.DARK_FUTURISTIC)
}

/**
 * Builds visual tokens with a rich mixture of Violet, Pink, and Red (Instagram-inspired gradient lobby)
 * while guaranteeing that AI Answers & Messages are rendered in DEEP BOLD WHITE (#FFFFFF) on a high-contrast dark surface.
 */
fun buildVisualTokens(theme: SeliperTheme): SeliperVisualTokens {
    val instaLobbyGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF833AB4), // Deep Violet
            Color(0xFFE1306C), // Vibrant Pink-Magenta
            Color(0xFFFD1D1D)  // Crimson Red
        ),
        start = Offset(0f, 0f),
        end = Offset(900f, 900f)
    )

    return when (theme) {
        SeliperTheme.DARK_FUTURISTIC -> SeliperVisualTokens(
            theme = theme,
            backgroundBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1F0A3D), // Deep Royal Violet
                    Color(0xFF4A0E3A), // Rich Berry Magenta
                    Color(0xFF6B0F24), // Deep Crimson Red
                    Color(0xFF160729)  // Dark Velvet Night
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1800f)
            ),
            lobbyAccentBrush = instaLobbyGradient,
            cardBackground = Color(0xFF230B36).copy(alpha = 0.92f),
            cardBorder = Color(0xFFFF4081).copy(alpha = 0.48f),
            userBubbleColor = Color(0xFFD81B60),
            userBubbleTextColor = Color(0xFFFFFFFF),
            aiBubbleColor = Color(0xFF180726).copy(alpha = 0.96f),
            aiBubbleTextColor = Color(0xFFFFFFFF), // Deep Pure White
            accentCyan = Color(0xFFFF4081),        // Vibrant Rose Pink highlight
            accentSecondary = Color(0xFFFF1744),   // Vivid Red accent
            subtleText = Color(0xFFF8BBD0),
            codeBlockBackground = Color(0xFF0E0417)
        )

        SeliperTheme.LIGHT_MINIMAL -> SeliperVisualTokens(
            theme = theme,
            backgroundBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF3B1053),
                    Color(0xFFAD1457),
                    Color(0xFFC62828)
                )
            ),
            lobbyAccentBrush = instaLobbyGradient,
            cardBackground = Color(0xFF280B3A).copy(alpha = 0.90f),
            cardBorder = Color(0xFFFF80AB).copy(alpha = 0.55f),
            userBubbleColor = Color(0xFFE91E63),
            userBubbleTextColor = Color(0xFFFFFFFF),
            aiBubbleColor = Color(0xFF1A0729).copy(alpha = 0.96f),
            aiBubbleTextColor = Color(0xFFFFFFFF), // Deep Pure White
            accentCyan = Color(0xFFFF80AB),
            accentSecondary = Color(0xFFFF5252),
            subtleText = Color(0xFFF48FB1),
            codeBlockBackground = Color(0xFF10041C)
        )

        SeliperTheme.DEEP_SPACE -> SeliperVisualTokens(
            theme = theme,
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF14052B),
                    Color(0xFF380939),
                    Color(0xFF540A22),
                    Color(0xFF0E031F)
                )
            ),
            lobbyAccentBrush = instaLobbyGradient,
            cardBackground = Color(0xFF220A3B).copy(alpha = 0.93f),
            cardBorder = Color(0xFFE040FB).copy(alpha = 0.45f),
            userBubbleColor = Color(0xFF8E24AA),
            userBubbleTextColor = Color(0xFFFFFFFF),
            aiBubbleColor = Color(0xFF150626),
            aiBubbleTextColor = Color(0xFFFFFFFF),
            accentCyan = Color(0xFFFF4081),
            accentSecondary = Color(0xFFE040FB),
            subtleText = Color(0xFFE1BEE7),
            codeBlockBackground = Color(0xFF0A0214)
        )

        SeliperTheme.GLASSMORPHISM -> SeliperVisualTokens(
            theme = theme,
            backgroundBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF51127F), // Violet
                    Color(0xFFB81B5A), // Pink-Magenta
                    Color(0xFFD31A2A), // Red
                    Color(0xFF2A0845)
                ),
                start = Offset(0f, 0f),
                end = Offset(1100f, 1600f)
            ),
            lobbyAccentBrush = instaLobbyGradient,
            cardBackground = Color(0xFF2A0C3D).copy(alpha = 0.85f),
            cardBorder = Color(0xFFFF80AB).copy(alpha = 0.55f),
            userBubbleColor = Color(0xFFC2185B),
            userBubbleTextColor = Color(0xFFFFFFFF),
            aiBubbleColor = Color(0xFF1A072B).copy(alpha = 0.94f),
            aiBubbleTextColor = Color(0xFFFFFFFF),
            accentCyan = Color(0xFFFF80AB),
            accentSecondary = Color(0xFFFF5252),
            subtleText = Color(0xFFF8BBD0),
            codeBlockBackground = Color(0xFF0F0419)
        )

        SeliperTheme.AURORA -> SeliperVisualTokens(
            theme = theme,
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2B0945),
                    Color(0xFF7B114B),
                    Color(0xFF9E1428),
                    Color(0xFF1A052B)
                )
            ),
            lobbyAccentBrush = instaLobbyGradient,
            cardBackground = Color(0xFF290B36).copy(alpha = 0.92f),
            cardBorder = Color(0xFFFF5252).copy(alpha = 0.45f),
            userBubbleColor = Color(0xFFD81B60),
            userBubbleTextColor = Color(0xFFFFFFFF),
            aiBubbleColor = Color(0xFF1A0624),
            aiBubbleTextColor = Color(0xFFFFFFFF),
            accentCyan = Color(0xFFFF5252),
            accentSecondary = Color(0xFFFF4081),
            subtleText = Color(0xFFFFCDD2),
            codeBlockBackground = Color(0xFF0E0314)
        )

        SeliperTheme.PROFESSIONAL -> SeliperVisualTokens(
            theme = theme,
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A0B2E),
                    Color(0xFF2E0F38),
                    Color(0xFF160824)
                )
            ),
            lobbyAccentBrush = instaLobbyGradient,
            cardBackground = Color(0xFF240F38),
            cardBorder = Color(0xFFF06292).copy(alpha = 0.4f),
            userBubbleColor = Color(0xFFAD1457),
            userBubbleTextColor = Color(0xFFFFFFFF),
            aiBubbleColor = Color(0xFF160826),
            aiBubbleTextColor = Color(0xFFFFFFFF),
            accentCyan = Color(0xFFF06292),
            accentSecondary = Color(0xFFE91E63),
            subtleText = Color(0xFFF8BBD0),
            codeBlockBackground = Color(0xFF0C0414)
        )

        SeliperTheme.AMOLED_DARK -> SeliperVisualTokens(
            theme = theme,
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF000000),
                    Color(0xFF12021A),
                    Color(0xFF000000)
                )
            ),
            lobbyAccentBrush = instaLobbyGradient,
            cardBackground = Color(0xFF14051E),
            cardBorder = Color(0xFFFF1744).copy(alpha = 0.5f),
            userBubbleColor = Color(0xFFC51162),
            userBubbleTextColor = Color(0xFFFFFFFF),
            aiBubbleColor = Color(0xFF0D0314),
            aiBubbleTextColor = Color(0xFFFFFFFF),
            accentCyan = Color(0xFFFF4081),
            accentSecondary = Color(0xFFFF1744),
            subtleText = Color(0xFFF48FB1),
            codeBlockBackground = Color(0xFF07020C)
        )
    }
}

/**
 * Deep Bold Letter White Typography so all AI answers and messages are crisp, deep bold white (#FFFFFF).
 */
private val SeliperTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        color = Color.White
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = Color.White
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = Color.White
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.5.sp,
        lineHeight = 22.sp,
        color = Color.White
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold, // Deep letter weight for maximum white clarity
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = Color.White
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        lineHeight = 21.sp,
        color = Color.White
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = Color.White
    )
)

@Composable
fun SeliperAiTheme(
    seliperTheme: SeliperTheme = SeliperTheme.DARK_FUTURISTIC,
    content: @Composable () -> Unit
) {
    val tokens = buildVisualTokens(seliperTheme)
    val colorScheme = darkColorScheme(
        primary = tokens.accentCyan,
        onPrimary = Color.White,
        secondary = tokens.accentSecondary,
        onSecondary = Color.White,
        background = Color(0xFF1F0A3D),
        onBackground = Color.White,
        surface = tokens.cardBackground,
        onSurface = Color.White,
        surfaceVariant = tokens.aiBubbleColor,
        onSurfaceVariant = tokens.subtleText,
        outline = tokens.cardBorder
    )

    CompositionLocalProvider(LocalSeliperTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SeliperTypography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    SeliperAiTheme(
        seliperTheme = SeliperTheme.DARK_FUTURISTIC,
        content = content
    )
}
