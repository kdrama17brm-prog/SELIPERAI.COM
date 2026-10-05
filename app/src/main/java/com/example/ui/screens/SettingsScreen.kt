package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.UserSettings
import com.example.data.model.AiProvider
import com.example.data.model.SeliperTheme
import com.example.data.remote.MultiModelGateway
import com.example.ui.theme.LocalSeliperTokens
import com.example.ui.theme.buildVisualTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onSelectTheme: (SeliperTheme) -> Unit,
    onSelectProviderAndModel: (AiProvider, String) -> Unit,
    onUpdateCustomModel: (String) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onToggleStreaming: (Boolean) -> Unit,
    onToggleKingThinking: (Boolean) -> Unit,
    onToggleAgentTools: (Boolean) -> Unit,
    onToggleStrictSafety: (Boolean) -> Unit,
    onToggleStartupAnimation: (Boolean) -> Unit,
    onClearAllHistory: () -> Unit,
    onNavigateAbout: () -> Unit,
    onBackToChat: () -> Unit
) {
    val tokens = LocalSeliperTokens.current
    var customModelDraft by remember(settings.customModelOverride) {
        mutableStateOf(settings.customModelOverride)
    }
    val languages = listOf("English", "Bengali", "Hindi", "Spanish", "French", "German", "Japanese")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToChat,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to chat",
                    tint = tokens.aiBubbleTextColor
                )
            }
            Column {
                Text(
                    text = "SELIPER AI Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(color = tokens.aiBubbleTextColor)
                )
                Text(
                    text = "Owned by BISWARUP MONDAL (BRM)",
                    style = MaterialTheme.typography.labelMedium.copy(color = tokens.accentCyan)
                )
            }
        }

        // 1. Premium Background Themes (7 Themes + Dark/Light Mode)
        SettingsSectionCard(
            title = "1. Premium Background Themes (Dark & Light)",
            icon = Icons.Default.Palette
        ) {
            Text(
                text = "Choose from 7 contrast-tested SELIPER AI themes:",
                style = MaterialTheme.typography.bodyMedium.copy(color = tokens.subtleText)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SeliperTheme.entries.forEach { themeOption ->
                    val isSelected = settings.activeTheme == themeOption
                    val previewTokens = buildVisualTokens(themeOption)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tokens.cardBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) tokens.accentCyan else tokens.cardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectTheme(themeOption) }
                            .testTag("theme_option_${themeOption.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(previewTokens.backgroundBrush)
                                        .border(1.5.dp, previewTokens.accentCyan, CircleShape)
                                )
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = themeOption.displayName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = tokens.aiBubbleTextColor
                                            )
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = tokens.accentCyan.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (themeOption.isDark) "DARK" else "LIGHT",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = tokens.accentCyan,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = themeOption.subtitle,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = tokens.subtleText)
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected theme",
                                    tint = tokens.accentCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Multi-Model & Provider Selection
        SettingsSectionCard(
            title = "2. Multi-Model & AI Provider Architecture",
            icon = Icons.Default.Memory
        ) {
            Text(
                text = "Provider → Model → SELIPER AI Backend → Selected Mode → User Response",
                style = MaterialTheme.typography.labelMedium.copy(color = tokens.accentCyan)
            )

            AiProvider.entries.forEach { provider ->
                val isProviderSelected = settings.activeProvider == provider
                val isConnected = MultiModelGateway.isProviderConnected(provider)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tokens.aiBubbleColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isProviderSelected) 1.5.dp else 1.dp,
                            color = if (isProviderSelected) tokens.accentCyan else tokens.cardBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = provider.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(color = tokens.aiBubbleTextColor)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isConnected) Color(0xFF00E676).copy(alpha = 0.16f)
                                else Color(0xFFFFB74D).copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = if (isConnected) "API Key Configured (${provider.secretKeyName})"
                                    else "Key Not Set (${provider.secretKeyName})",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isConnected) Color(0xFF00E676) else Color(0xFFFFB74D),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        provider.defaultModels.forEach { modelOpt ->
                            val isModelActive = isProviderSelected && settings.selectedModelId == modelOpt.modelId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isModelActive) tokens.accentCyan.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        onSelectProviderAndModel(provider, modelOpt.modelId)
                                    }
                                    .padding(10.dp)
                                    .testTag("model_option_${modelOpt.modelId}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${modelOpt.displayName} (${modelOpt.modelId})",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = if (isModelActive) tokens.accentCyan else tokens.aiBubbleTextColor
                                        )
                                    )
                                    Text(
                                        text = modelOpt.badgeDescription,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = tokens.subtleText)
                                    )
                                }
                                if (isModelActive) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active model",
                                        tint = tokens.accentCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = customModelDraft,
                onValueChange = {
                    customModelDraft = it
                    onUpdateCustomModel(it)
                },
                label = { Text("Optional Custom Model ID Override") },
                placeholder = { Text("Leave blank to use selected preset model above") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_model_input")
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = tokens.accentCyan, modifier = Modifier.size(16.dp))
                Text(
                    text = "Configure your API keys securely in the AI Studio Secrets panel (`GEMINI_API_KEY`, `OPENAI_API_KEY`, `XAI_API_KEY`). Keys are never hardcoded.",
                    style = MaterialTheme.typography.labelMedium.copy(color = tokens.subtleText)
                )
            }
        }

        // 3. Language Preferences
        SettingsSectionCard(
            title = "3. Response Language",
            icon = Icons.Default.Language
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { lang ->
                    val selected = settings.language == lang
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (selected) tokens.accentCyan else tokens.aiBubbleColor,
                        modifier = Modifier
                            .border(1.dp, if (selected) tokens.accentCyan else tokens.cardBorder, RoundedCornerShape(18.dp))
                            .clickable { onUpdateLanguage(lang) }
                            .testTag("lang_chip_$lang")
                    ) {
                        Text(
                            text = lang,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (selected) Color(0xFF04141C) else tokens.aiBubbleTextColor,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 4. Chat, Speed & Reasoning Settings
        SettingsSectionCard(
            title = "4. Chat, Speed & 'King of Best Thinking' Settings",
            icon = Icons.Default.Security
        ) {
            SettingToggleRow(
                title = "Real-Time Streaming Responses (SSE)",
                subtitle = "Stream words live as SELIPER AI generates them for maximum speed",
                checked = settings.streamingEnabled,
                onCheckedChange = onToggleStreaming,
                tag = "toggle_streaming"
            )
            HorizontalDivider(color = tokens.cardBorder)
            SettingToggleRow(
                title = "'King of Best Thinking' Reasoning Summary",
                subtitle = "Breaks complicated tasks into verified internal steps and provides a concise summary",
                checked = settings.kingThinkingSummaryEnabled,
                onCheckedChange = onToggleKingThinking,
                tag = "toggle_king_thinking"
            )
            HorizontalDivider(color = tokens.cardBorder)
            SettingToggleRow(
                title = "Connected Local Agent Tools",
                subtitle = "Allow SELIPER AI to run verified local Math, Financial, Document & Time tools",
                checked = settings.agentToolsEnabled,
                onCheckedChange = onToggleAgentTools,
                tag = "toggle_agent_tools"
            )
            HorizontalDivider(color = tokens.cardBorder)
            SettingToggleRow(
                title = "Strict Safety & Policy Protections",
                subtitle = "Always block illegal, malware, credential theft, fraud, or harmful assistance",
                checked = settings.strictSafetyEnabled,
                onCheckedChange = onToggleStrictSafety,
                tag = "toggle_strict_safety"
            )
            HorizontalDivider(color = tokens.cardBorder)
            SettingToggleRow(
                title = "Startup Opening Animation",
                subtitle = "Show the short 1.2s SELIPER AI logo pulse when opening the app",
                checked = settings.startupAnimationEnabled,
                onCheckedChange = onToggleStartupAnimation,
                tag = "toggle_startup_animation"
            )
        }

        // 5. Conversation History & About
        SettingsSectionCard(
            title = "5. Conversation History & About SELIPER AI",
            icon = Icons.Default.Info
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClearAllHistory,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("clear_all_history_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All Chats")
                }

                OutlinedButton(
                    onClick = onNavigateAbout,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("settings_open_about_button")
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("About SELIPER AI")
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    val tokens = LocalSeliperTokens.current
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = tokens.accentCyan, modifier = Modifier.size(20.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(color = tokens.aiBubbleTextColor)
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    val tokens = LocalSeliperTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(color = tokens.aiBubbleTextColor)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = tokens.subtleText)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag)
        )
    }
}
