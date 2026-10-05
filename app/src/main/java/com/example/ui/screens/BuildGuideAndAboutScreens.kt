package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.UserSettings
import com.example.data.remote.MultiModelGateway
import com.example.domain.SeliperBuildGuideRepository
import com.example.ui.components.SeliperNeonCircleLogo
import com.example.ui.components.SeliperWordmarkText
import com.example.ui.theme.LocalSeliperTokens

@Composable
fun BuildGuideScreen(
    settings: UserSettings,
    onSelectStep: (Int) -> Unit,
    onBackToChat: () -> Unit
) {
    val tokens = LocalSeliperTokens.current
    val steps = SeliperBuildGuideRepository.allSteps
    val currentStepIndex = (settings.currentArchitectureStep - 1).coerceIn(0, steps.lastIndex)
    val currentStep = steps[currentStepIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackToChat,
                    modifier = Modifier.testTag("guide_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Chat",
                        tint = tokens.aiBubbleTextColor
                    )
                }
                Column {
                    Text(
                        text = "10-Step Beginner Build Guide",
                        style = MaterialTheme.typography.titleLarge.copy(color = tokens.aiBubbleTextColor)
                    )
                    Text(
                        text = "SELIPER AI • Owned by BISWARUP MONDAL (BRM)",
                        style = MaterialTheme.typography.labelMedium.copy(color = tokens.accentCyan)
                    )
                }
            }
        }

        // Step Selector Pills (1..10)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(steps) { stepItem ->
                val isSelected = stepItem.stepNumber == currentStep.stepNumber
                val isCompleted = stepItem.stepNumber < currentStep.stepNumber
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isSelected -> tokens.accentCyan
                        isCompleted -> tokens.accentCyan.copy(alpha = 0.18f)
                        else -> tokens.cardBackground
                    },
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (isSelected) tokens.accentCyan else tokens.cardBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelectStep(stepItem.stepNumber) }
                        .testTag("guide_step_pill_${stepItem.stepNumber}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = tokens.accentCyan,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "Step ${stepItem.stepNumber}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF04141C) else tokens.aiBubbleTextColor
                            )
                        )
                    }
                }
            }
        }

        // Main Step Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = tokens.cardBackground,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, tokens.cardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = currentStep.title,
                    style = MaterialTheme.typography.headlineMedium.copy(color = tokens.accentCyan)
                )
                Text(
                    text = currentStep.subtitle,
                    style = MaterialTheme.typography.titleMedium.copy(color = tokens.aiBubbleTextColor)
                )
                Text(
                    text = currentStep.overview,
                    style = MaterialTheme.typography.bodyLarge.copy(color = tokens.subtleText)
                )

                HorizontalDivider(color = tokens.cardBorder)

                GuideDetailSection(
                    icon = Icons.Outlined.TouchApp,
                    label = "1. What to Click",
                    body = currentStep.whatToClick
                )
                GuideDetailSection(
                    icon = Icons.Outlined.Architecture,
                    label = "2. What to Create",
                    body = currentStep.whatToCreate
                )
                GuideDetailSection(
                    icon = Icons.Default.Code,
                    label = "3. Where to Put It",
                    body = currentStep.whereToPutIt
                )

                // Code / Config Box
                Text(
                    text = "4. Code / Configuration Added",
                    style = MaterialTheme.typography.titleMedium.copy(color = tokens.accentCyan)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = tokens.codeBlockBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, tokens.cardBorder, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = currentStep.codeOrConfigToAdd,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                GuideDetailSection(
                    icon = Icons.Outlined.BugReport,
                    label = "5. How to Test It",
                    body = currentStep.howToTestIt
                )
                GuideDetailSection(
                    icon = Icons.Default.Verified,
                    label = "6. What Result You Should See",
                    body = currentStep.expectedResult
                )
            }
        }

        // Prev / Confirm & Next Step controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { if (currentStep.stepNumber > 1) onSelectStep(currentStep.stepNumber - 1) },
                enabled = currentStep.stepNumber > 1,
                modifier = Modifier.testTag("guide_prev_step_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Previous Step")
            }

            if (currentStep.stepNumber < 10) {
                Button(
                    onClick = { onSelectStep(currentStep.stepNumber + 1) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.accentCyan,
                        contentColor = Color(0xFF04141C)
                    ),
                    modifier = Modifier.testTag("guide_next_step_button")
                ) {
                    Text("Confirm & Step ${currentStep.stepNumber + 1}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            } else {
                Button(
                    onClick = onBackToChat,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.accentCyan,
                        contentColor = Color(0xFF04141C)
                    ),
                    modifier = Modifier.testTag("guide_launch_chat_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch SELIPER AI Chat", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GuideDetailSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    body: String
) {
    val tokens = LocalSeliperTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tokens.accentCyan,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(color = tokens.accentCyan)
            )
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge.copy(color = tokens.aiBubbleTextColor)
        )
    }
}

/**
 * Section 17 — About SELIPER AI Screen
 */
@Composable
fun AboutSeliperScreen(
    settings: UserSettings,
    onBackToChat: () -> Unit,
    onOpenGuide: () -> Unit
) {
    val tokens = LocalSeliperTokens.current
    val isGeminiConnected = MultiModelGateway.isProviderConnected(settings.activeProvider)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBackToChat,
                modifier = Modifier.testTag("about_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = tokens.aiBubbleTextColor
                )
            }
            Text(
                text = "About SELIPER AI",
                style = MaterialTheme.typography.headlineMedium.copy(color = tokens.aiBubbleTextColor)
            )
        }

        // Hero Banner Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = tokens.cardBackground,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, tokens.cardBorder, RoundedCornerShape(20.dp))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner_1791149422903),
                        contentDescription = "SELIPER AI Neural Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SeliperNeonCircleLogo(
                            size = 68.dp,
                            showInnerText = true
                        )
                        Column {
                            SeliperWordmarkText(
                                fontSize = 28.sp,
                                seliperColor = Color.White
                            )
                            Text(
                                text = "Owned by BISWARUP MONDAL (BRM)",
                                style = MaterialTheme.typography.titleMedium.copy(color = tokens.accentCyan)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tokens.accentCyan.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "OWNER: BISWARUP MONDAL (BRM)  •  ORIGINAL AI ASSISTANT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = tokens.accentCyan,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        text = "SELIPER AI is an original, fast, safe, and modern multi-mode AI assistant and agent owned by BISWARUP MONDAL (BRM). It is designed from the ground up with a dedicated 'King of Best Thinking' reasoning architecture, five specialized operating modes, a 9-domain Focus Topic Agent (FTA), and strict tool honesty.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = tokens.aiBubbleTextColor)
                    )

                    HorizontalDivider(color = tokens.cardBorder)

                    Text(
                        text = "Core Pillars of SELIPER AI",
                        style = MaterialTheme.typography.titleLarge.copy(color = tokens.accentCyan)
                    )
                    Text(
                        text = "• Five Specialized Modes: Homework Friend Mode, Helper Mode, Humans Mode, Super AI Agent Mode, and FTA (Focus Topic Agent) Mode.\n" +
                            "• Multi-Model Backend Abstraction: Connects seamlessly to Google Gemini, OpenAI, or xAI Grok without fake demo responses.\n" +
                            "• Verified Tool Honesty: Executes real local calculations, compound interest models, document analysis, and image vision — never fabricating tool usage.\n" +
                            "• Strong Safety Guardrails: Refuses harmful or illegal instructions clearly and offers educational alternatives.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = tokens.subtleText)
                    )

                    HorizontalDivider(color = tokens.cardBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active Provider Status",
                                style = MaterialTheme.typography.labelMedium.copy(color = tokens.subtleText)
                            )
                            Text(
                                text = if (isGeminiConnected) {
                                    "${settings.activeProvider.displayName} Connected (${settings.effectiveModelId})"
                                } else {
                                    "AI service is not connected. Configure ${settings.activeProvider.secretKeyName}."
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (isGeminiConnected) Color(0xFF00E676) else Color(0xFFFFB74D)
                                )
                            )
                        }
                    }

                    Button(
                        onClick = onOpenGuide,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.accentCyan,
                            contentColor = Color(0xFF04141C)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("about_open_guide_button")
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open 10-Step Beginner Architecture Guide", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
