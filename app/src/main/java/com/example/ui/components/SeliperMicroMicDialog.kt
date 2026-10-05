package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Locale
import kotlin.math.sin

/**
 * Interactive Micro-Mic Live Voice System for SELIPER AI.
 * - Uses Android SpeechRecognizer for live real-time speech transcription with RMS audio wave bars.
 * - Styled in the Violet-Pink-Red Instagram-inspired gradient palette with deep bold white text.
 * - Automatically falls back to the system voice intent if on-device SpeechRecognizer service is unavailable.
 */
@Composable
fun SeliperMicroMicDialog(
    onDismiss: () -> Unit,
    onResultTranscribed: (String, Boolean) -> Unit,
    onFallbackSystemVoice: () -> Unit
) {
    val context = LocalContext.current
    var liveTranscript by remember { mutableStateOf("") }
    var statusLabel by remember { mutableStateOf("Listening… Speak to SELIPER AI") }
    var rmsLevel by remember { mutableFloatStateOf(0.3f) }
    var isListening by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onDismiss()
            onFallbackSystemVoice()
            return@DisposableEffect onDispose { }
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                statusLabel = "Micro-Mic Active • Speak now…"
                isListening = true
            }

            override fun onBeginningOfSpeech() {
                statusLabel = "Capturing your voice…"
            }

            override fun onRmsChanged(rmsdB: Float) {
                rmsLevel = ((rmsdB + 2f) / 12f).coerceIn(0.15f, 1.0f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                statusLabel = "Processing speech…"
            }

            override fun onError(error: Int) {
                isListening = false
                if (liveTranscript.isNotBlank()) {
                    onResultTranscribed(liveTranscript, false)
                    onDismiss()
                } else {
                    statusLabel = "Tap 'Speak Again' or type your message."
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val best = matches?.firstOrNull()?.trim().orEmpty()
                if (best.isNotEmpty()) {
                    liveTranscript = best
                    onResultTranscribed(best, false)
                    onDismiss()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()?.trim().orEmpty()
                if (partial.isNotEmpty()) {
                    liveTranscript = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        recognizer.setRecognitionListener(listener)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            recognizer.startListening(intent)
        } catch (_: Exception) {
            onDismiss()
            onFallbackSystemVoice()
        }

        onDispose {
            try {
                recognizer.stopListening()
                recognizer.destroy()
            } catch (_: Exception) {}
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val instaGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF833AB4), // Violet
            Color(0xFFE1306C), // Pink
            Color(0xFFFD1D1D)  // Red
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1E0830),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, instaGradient, RoundedCornerShape(28.dp))
                .testTag("micro_mic_dialog")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELIPER AI • MICRO-MIC",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFFFF4081),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Micro-Mic",
                            tint = Color.White
                        )
                    }
                }

                // Pulsing Violet-Pink-Red Micro-Mic Orb
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(if (isListening) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFE1306C).copy(alpha = 0.45f),
                                        Color(0xFF833AB4).copy(alpha = 0.20f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(instaGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Microphone active",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Live Audio Wave Equalizer Bars
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    val barCount = 19
                    val barWidth = 5.dp.toPx()
                    val spacing = (size.width - (barCount * barWidth)) / (barCount + 1)
                    for (i in 0 until barCount) {
                        val factor = if (isListening) {
                            (0.35f + 0.65f * kotlin.math.abs(sin(wavePhase + i * 0.55f)) * rmsLevel)
                        } else {
                            0.2f
                        }
                        val barH = (size.height * factor).coerceAtLeast(6.dp.toPx())
                        val x = spacing + i * (barWidth + spacing)
                        val y = (size.height - barH) / 2f
                        drawRoundRect(
                            brush = instaGradient,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }

                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center
                )

                // Live Deep White Transcript Preview
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF12041E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFF4081).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = liveTranscript.ifBlank { "Speak clearly into your microphone…" },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (liveTranscript.isNotBlank()) {
                                onResultTranscribed(liveTranscript, true)
                                onDismiss()
                            } else {
                                onDismiss()
                                onFallbackSystemVoice()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE1306C),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (liveTranscript.isNotBlank()) "Send Now" else "Use System Mic",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
