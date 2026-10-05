package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettings
import com.example.data.model.ConnectedToolExecution
import com.example.data.model.FtaTopic
import com.example.data.model.SeliperMode
import com.example.data.remote.MultiModelGateway
import com.example.ui.components.AnimatedSparkleThinkingOrb
import com.example.ui.components.MarkdownMessageRenderer
import com.example.ui.components.SeliperNeonCircleLogo
import com.example.ui.components.SeliperShimmerWaveBar
import com.example.ui.components.SeliperWordmarkText
import com.example.ui.theme.LocalSeliperTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun modeIconFor(mode: SeliperMode): ImageVector {
    return when (mode) {
        SeliperMode.HOMEWORK_FRIEND -> Icons.Default.School
        SeliperMode.HELPER -> Icons.Default.Handyman
        SeliperMode.HUMANS -> Icons.Default.Favorite
        SeliperMode.SUPER_AGENT -> Icons.Default.AutoAwesome
        SeliperMode.FTA -> Icons.Default.TrackChanges
    }
}

/**
 * Clean, Uncluttered Main AI Chat Screen (Sections 2 & 3).
 * - No cluttered mode bars or tool grids on the main screen.
 * - Immediately communicates: "Type your message here and talk to SELIPER AI."
 * - Modes, tools, history, settings, and themes live cleanly inside the ☰ and ⋮ menus.
 */
@Composable
fun ChatWorkspaceScreen(
    settings: UserSettings,
    messages: List<MessageEntity>,
    isGenerating: Boolean,
    streamingPartialText: String,
    decodeToolLogs: (String?) -> List<ConnectedToolExecution>,
    onSendStarterPrompt: (String) -> Unit,
    onRegenerateLast: () -> Unit,
    onEditUserMessage: (MessageEntity, String) -> Unit,
    onOpenToolsMenu: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val isProviderConnected = MultiModelGateway.isProviderConnected(settings.activeProvider)
    val tokens = LocalSeliperTokens.current

    LaunchedEffect(messages.size, streamingPartialText) {
        if (messages.isNotEmpty() || streamingPartialText.isNotEmpty()) {
            val targetIdx = (messages.size + if (isGenerating) 1 else 0 - 1).coerceAtLeast(0)
            listState.animateScrollToItem(targetIdx)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Subtle API connection notice only if not connected
        if (!isProviderConnected) {
            Surface(
                color = Color(0xFF3E2723).copy(alpha = 0.88f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSettings() }
                    .testTag("api_not_connected_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "AI service is not connected. Please configure the API.",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFFFFE0B2),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    Text(
                        text = "Settings →",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = tokens.accentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        if (messages.isEmpty() && !isGenerating) {
            CleanMinimalChatWelcome(
                settings = settings,
                onSendStarterPrompt = onSendStarterPrompt,
                onOpenToolsMenu = onOpenToolsMenu,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageItem(
                        message = msg,
                        toolLogs = decodeToolLogs(msg.toolLogsJson),
                        isLastAssistantMessage = msg.role == "assistant" && msg.id == messages.lastOrNull()?.id,
                        onRegenerate = onRegenerateLast,
                        onEditUserMessage = { newPrompt ->
                            onEditUserMessage(msg, newPrompt)
                        }
                    )
                }

                if (isGenerating) {
                    item(key = "streaming_indicator") {
                        StreamingThinkingBubble(
                            partialText = streamingPartialText,
                            activeMode = settings.activeMode,
                            activeFta = settings.activeFtaTopic
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CleanMinimalChatWelcome(
    settings: UserSettings,
    onSendStarterPrompt: (String) -> Unit,
    onOpenToolsMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalSeliperTokens.current
    val activeMode = settings.activeMode
    val activeFta = settings.activeFtaTopic
    val quickPrompts = if (activeMode == SeliperMode.FTA) {
        activeFta.samplePrompts.take(2)
    } else {
        activeMode.samplePrompts.take(2)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 480.dp)
        ) {
            // Centered Seliper AI Logo
            SeliperNeonCircleLogo(
                size = 76.dp,
                showInnerText = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeliperWordmarkText(
                fontSize = 28.sp,
                seliperColor = tokens.aiBubbleTextColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "How can I help you today?",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = tokens.aiBubbleTextColor.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtle active mode pill that opens the menu when tapped
            Surface(
                shape = RoundedCornerShape(50),
                color = tokens.accentCyan.copy(alpha = 0.12f),
                modifier = Modifier
                    .border(1.dp, tokens.accentCyan.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .clickable { onOpenToolsMenu() }
                    .testTag("active_mode_indicator_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = modeIconFor(activeMode),
                        contentDescription = null,
                        tint = tokens.accentCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (activeMode == SeliperMode.FTA) {
                            "${activeMode.shortTitle} • ${activeFta.displayName}"
                        } else {
                            activeMode.title
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = tokens.accentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2 subtle starter suggestions to begin chatting immediately
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickPrompts.forEachIndexed { idx, prompt ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = tokens.cardBackground.copy(alpha = 0.72f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, tokens.cardBorder.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            .clickable { onSendStarterPrompt(prompt) }
                            .testTag("starter_prompt_$idx")
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = tokens.aiBubbleTextColor.copy(alpha = 0.9f)
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: MessageEntity,
    toolLogs: List<ConnectedToolExecution>,
    isLastAssistantMessage: Boolean,
    onRegenerate: () -> Unit,
    onEditUserMessage: (String) -> Unit
) {
    val tokens = LocalSeliperTokens.current
    val isUser = message.role == "user"
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editDraft by remember(message.content) { mutableStateOf(message.content) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 20.dp
            ),
            color = if (isUser) tokens.userBubbleColor else tokens.aiBubbleColor,
            modifier = Modifier
                .widthIn(max = 620.dp)
                .border(
                    width = 1.dp,
                    color = when {
                        message.isError -> Color(0xFFFF5252)
                        isUser -> tokens.userBubbleColor
                        else -> tokens.cardBorder
                    },
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 6.dp,
                        bottomEnd = if (isUser) 6.dp else 20.dp
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SeliperNeonCircleLogo(
                                size = 22.dp,
                                showInnerText = false
                            )
                            SeliperWordmarkText(
                                fontSize = 13.sp,
                                seliperColor = if (message.isError) Color(0xFFFFB74D) else tokens.aiBubbleTextColor
                            )
                        }
                        if (!message.modelUsed.isNullOrBlank()) {
                            Text(
                                text = message.modelUsed,
                                style = MaterialTheme.typography.labelMedium.copy(color = tokens.subtleText)
                            )
                        }
                    }
                }

                if (!message.attachedImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = message.attachedImageUri,
                        contentDescription = "Attached image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                if (!message.attachedFileName.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.22f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = tokens.accentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Attached File: ${message.attachedFileName}",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                            )
                        }
                    }
                }

                if (toolLogs.isNotEmpty()) {
                    toolLogs.forEach { tool ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.10f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Verified Tool Used: ${tool.toolName} (${tool.executionTimeMs}ms)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color(0xFF00E676),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    text = "Output: ${tool.toolOutput}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = tokens.aiBubbleTextColor
                                    )
                                )
                            }
                        }
                    }
                }

                if (isEditing && isUser) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editDraft,
                            onValueChange = { editDraft = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { isEditing = false }) {
                                Text("Cancel", color = Color.White)
                            }
                            Button(
                                onClick = {
                                    isEditing = false
                                    onEditUserMessage(editDraft)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tokens.accentCyan,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Save & Resubmit", fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                } else {
                    MarkdownMessageRenderer(
                        markdown = message.content,
                        textColor = Color.White // Deep letter pure white (#FFFFFF) for all messages & AI answers
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(message.content))
                            copied = true
                            scope.launch {
                                delay(1600)
                                copied = false
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_message_button_${message.id}")
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Outlined.ContentCopy,
                            contentDescription = "Copy message",
                            tint = if (isUser) tokens.userBubbleTextColor.copy(alpha = 0.8f) else tokens.subtleText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (isUser) {
                        IconButton(
                            onClick = { isEditing = !isEditing },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("edit_message_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit message",
                                tint = tokens.userBubbleTextColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (!isUser && isLastAssistantMessage) {
                        OutlinedButton(
                            onClick = onRegenerate,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("regenerate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate or Retry",
                                tint = tokens.accentCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (message.isError) "Retry" else "Regenerate",
                                style = MaterialTheme.typography.labelMedium.copy(color = tokens.accentCyan)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingThinkingBubble(
    partialText: String,
    activeMode: SeliperMode,
    activeFta: FtaTopic
) {
    val tokens = LocalSeliperTokens.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tokens.aiBubbleColor,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .border(1.dp, tokens.accentCyan.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedSparkleThinkingOrb(size = 24.dp)
                Text(
                    text = if (partialText.isEmpty()) {
                        "Seliper AI is thinking (${activeMode.shortTitle}${if (activeMode == SeliperMode.FTA) " • ${activeFta.displayName}" else ""})…"
                    } else {
                        "Seliper AI • Streaming live response…"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = tokens.accentCyan,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            if (partialText.isEmpty()) {
                SeliperShimmerWaveBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )
            } else {
                MarkdownMessageRenderer(
                    markdown = partialText,
                    textColor = tokens.aiBubbleTextColor
                )
            }
        }
    }
}
