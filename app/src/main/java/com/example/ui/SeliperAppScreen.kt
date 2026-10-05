package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ConversationEntity
import com.example.data.local.UserSettings
import com.example.data.model.FtaTopic
import com.example.data.model.SeliperMode
import com.example.data.remote.MultiModelGateway
import com.example.ui.components.SeliperHorizontalRibbonBanner
import com.example.ui.components.SeliperMicroMicDialog
import com.example.ui.components.SeliperNeonCircleLogo
import com.example.ui.components.SeliperOpeningSplashOverlay
import com.example.ui.components.SeliperWordmarkText
import com.example.ui.screens.AboutSeliperScreen
import com.example.ui.screens.BuildGuideScreen
import com.example.ui.screens.ChatWorkspaceScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.modeIconFor
import com.example.ui.theme.LocalSeliperTokens
import com.example.ui.theme.SeliperAiTheme
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeliperAppScreen(viewModel: SeliperViewModel) {
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()
    val destination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val streamingPartialText by viewModel.streamingPartialText.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val pendingAttachment by viewModel.pendingAttachment.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    var inputPrompt by rememberSaveable { mutableStateOf("") }
    var renameDialogTarget by remember { mutableStateOf<ConversationEntity?>(null) }
    var renameDraft by remember { mutableStateOf("") }
    var showMicroMicDialog by remember { mutableStateOf(false) }

    // Remember across configuration changes so startup animation only runs once per session
    var hasShownSplashThisSession by rememberSaveable { mutableStateOf(false) }
    val shouldShowSplash = !hasShownSplashThisSession && settings.startupAnimationEnabled

    // Zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.attachImageFromUri(uri)
        }
    }

    // Document Picker for text/code/markdown files
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            var displayName = "attached_document.txt"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    displayName = cursor.getString(nameIndex) ?: displayName
                }
            }
            viewModel.attachDocumentFromUri(uri, displayName)
        }
    }

    // Real Android Speech-to-Text Voice Input Launcher (Fallback)
    val voiceRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenList = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenList?.firstOrNull()?.trim()
            if (!spokenText.isNullOrEmpty()) {
                inputPrompt = if (inputPrompt.isBlank()) spokenText else "$inputPrompt $spokenText"
            }
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showMicroMicDialog = true
        } else {
            // Fallback to system RecognizerIntent if runtime mic permission was declined
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to SELIPER AI…")
                }
                voiceRecognitionLauncher.launch(intent)
            } catch (_: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Microphone permission is needed for Micro-Mic voice input.")
                }
            }
        }
    }

    LaunchedEffect(statusBanner) {
        statusBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    BackHandler(enabled = destination != SeliperDestination.CHAT || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            viewModel.navigateTo(SeliperDestination.CHAT)
        }
    }

    SeliperAiTheme(seliperTheme = settings.activeTheme) {
        val tokens = LocalSeliperTokens.current

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SeliperSidebarDrawer(
                    settings = settings,
                    conversations = conversations,
                    activeConversationId = activeConversationId,
                    searchQuery = searchQuery,
                    onSearchChange = viewModel::updateSearchQuery,
                    onNewChat = {
                        viewModel.startNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onSelectMode = { mode ->
                        viewModel.selectMode(mode)
                    },
                    onSelectFtaTopic = { topic ->
                        viewModel.selectFtaTopic(topic)
                    },
                    onSelectConversation = { convId ->
                        viewModel.selectConversation(convId)
                        scope.launch { drawerState.close() }
                    },
                    onRequestRename = { conv ->
                        renameDialogTarget = conv
                        renameDraft = conv.title
                    },
                    onDeleteConversation = viewModel::deleteConversation,
                    onNavigateDestination = { dest ->
                        viewModel.navigateTo(dest)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tokens.backgroundBrush)
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        SeliperTopAppBar(
                            settings = settings,
                            hasActiveMessages = activeMessages.isNotEmpty(),
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onSelectMode = viewModel::selectMode,
                            onSelectFtaTopic = viewModel::selectFtaTopic,
                            onNewChat = viewModel::startNewChat,
                            onClearCurrentChat = viewModel::clearCurrentChat,
                            onOpenHistory = { scope.launch { drawerState.open() } },
                            onOpenGuide = { viewModel.navigateTo(SeliperDestination.BUILD_GUIDE) },
                            onOpenSettings = { viewModel.navigateTo(SeliperDestination.SETTINGS) },
                            onOpenAbout = { viewModel.navigateTo(SeliperDestination.ABOUT) }
                        )
                    },
                    bottomBar = {
                        if (destination == SeliperDestination.CHAT) {
                            SeliperMessageComposer(
                                inputPrompt = inputPrompt,
                                onInputChange = { inputPrompt = it },
                                isGenerating = isGenerating,
                                pendingAttachment = pendingAttachment,
                                onClearAttachment = viewModel::clearPendingAttachment,
                                onPickImage = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onPickDocument = {
                                    documentPickerLauncher.launch(arrayOf("text/*", "application/json", "application/xml"))
                                },
                                onStartVoiceInput = {
                                    val hasPerm = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPerm) {
                                        showMicroMicDialog = true
                                    } else {
                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                onSend = {
                                    val toSend = inputPrompt
                                    inputPrompt = ""
                                    viewModel.sendMessage(toSend)
                                },
                                onStop = viewModel::stopGeneration
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (destination) {
                            SeliperDestination.CHAT -> {
                                ChatWorkspaceScreen(
                                    settings = settings,
                                    messages = activeMessages,
                                    isGenerating = isGenerating,
                                    streamingPartialText = streamingPartialText,
                                    decodeToolLogs = viewModel::decodeToolLogs,
                                    onSendStarterPrompt = { prompt ->
                                        viewModel.sendMessage(prompt)
                                    },
                                    onRegenerateLast = viewModel::regenerateLastResponse,
                                    onEditUserMessage = viewModel::editUserMessageAndResubmit,
                                    onOpenToolsMenu = { scope.launch { drawerState.open() } },
                                    onOpenSettings = { viewModel.navigateTo(SeliperDestination.SETTINGS) }
                                )
                            }

                            SeliperDestination.BUILD_GUIDE -> {
                                BuildGuideScreen(
                                    settings = settings,
                                    onSelectStep = viewModel::setArchitectureGuideStep,
                                    onBackToChat = { viewModel.navigateTo(SeliperDestination.CHAT) }
                                )
                            }

                            SeliperDestination.SETTINGS -> {
                                SettingsScreen(
                                    settings = settings,
                                    onSelectTheme = viewModel::selectTheme,
                                    onSelectProviderAndModel = viewModel::selectProviderAndModel,
                                    onUpdateCustomModel = viewModel::updateCustomModelOverride,
                                    onUpdateLanguage = viewModel::updateLanguage,
                                    onToggleStreaming = viewModel::updateStreamingEnabled,
                                    onToggleKingThinking = viewModel::updateKingThinkingEnabled,
                                    onToggleAgentTools = viewModel::updateAgentToolsEnabled,
                                    onToggleStrictSafety = viewModel::updateStrictSafetyEnabled,
                                    onToggleStartupAnimation = viewModel::updateStartupAnimationEnabled,
                                    onClearAllHistory = viewModel::clearAllConversations,
                                    onNavigateAbout = { viewModel.navigateTo(SeliperDestination.ABOUT) },
                                    onBackToChat = { viewModel.navigateTo(SeliperDestination.CHAT) }
                                )
                            }

                            SeliperDestination.ABOUT -> {
                                AboutSeliperScreen(
                                    settings = settings,
                                    onBackToChat = { viewModel.navigateTo(SeliperDestination.CHAT) },
                                    onOpenGuide = { viewModel.navigateTo(SeliperDestination.BUILD_GUIDE) }
                                )
                            }
                        }
                    }
                }

                renameDialogTarget?.let { targetConv ->
                    AlertDialog(
                        onDismissRequest = { renameDialogTarget = null },
                        title = { Text("Rename Conversation") },
                        text = {
                            OutlinedTextField(
                                value = renameDraft,
                                onValueChange = { renameDraft = it },
                                singleLine = true,
                                label = { Text("Conversation Title") }
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.renameConversation(targetConv.id, renameDraft)
                                    renameDialogTarget = null
                                }
                            ) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { renameDialogTarget = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showMicroMicDialog) {
                    SeliperMicroMicDialog(
                        onDismiss = { showMicroMicDialog = false },
                        onResultTranscribed = { spokenText, sendImmediately ->
                            if (sendImmediately) {
                                viewModel.sendMessage(spokenText)
                            } else {
                                inputPrompt = if (inputPrompt.isBlank()) spokenText else "$inputPrompt $spokenText"
                            }
                        },
                        onFallbackSystemVoice = {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to SELIPER AI…")
                                }
                                voiceRecognitionLauncher.launch(intent)
                            } catch (_: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Voice input is not supported on this device.")
                                }
                            }
                        }
                    )
                }

                // Short, smooth 1.2s startup animation (respects user preference & session state)
                AnimatedVisibility(
                    visible = shouldShowSplash,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(380))
                ) {
                    SeliperOpeningSplashOverlay(
                        onFinished = { hasShownSplashThisSession = true }
                    )
                }
            }
        }
    }
}

/**
 * Clean Top App Bar with:
 * - Left: ☰ Three-line Menu Drawer trigger
 * - Center/Left: Seliper AI Logo + Active Mode subtitle
 * - Right: New Chat + ⋮ Three-dot Corner Menu containing all Modes, FTA Topics, Tools, Settings, Themes & About
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeliperTopAppBar(
    settings: UserSettings,
    hasActiveMessages: Boolean,
    onOpenDrawer: () -> Unit,
    onSelectMode: (SeliperMode) -> Unit,
    onSelectFtaTopic: (FtaTopic) -> Unit,
    onNewChat: () -> Unit,
    onClearCurrentChat: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val tokens = LocalSeliperTokens.current
    val isConnected = MultiModelGateway.isProviderConnected(settings.activeProvider)
    var showOverflowMenu by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = tokens.cardBackground.copy(alpha = 0.90f)
        ),
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("open_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open menu (☰)",
                    tint = tokens.aiBubbleTextColor
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable { showOverflowMenu = true }
            ) {
                SeliperNeonCircleLogo(
                    size = 34.dp,
                    showInnerText = false
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SeliperWordmarkText(
                            fontSize = 18.sp,
                            seliperColor = tokens.aiBubbleTextColor
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) Color(0xFF00E676) else Color(0xFFFFB74D))
                        )
                    }
                    Text(
                        text = if (settings.activeMode == SeliperMode.FTA) {
                            "${settings.activeMode.shortTitle} • ${settings.activeFtaTopic.displayName}"
                        } else {
                            settings.activeMode.shortTitle
                        },
                        style = MaterialTheme.typography.labelMedium.copy(color = tokens.accentCyan),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onNewChat,
                modifier = Modifier.testTag("top_new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = tokens.accentCyan
                )
            }

            Box {
                IconButton(
                    onClick = { showOverflowMenu = true },
                    modifier = Modifier.testTag("top_more_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tools & Modes Menu (⋮)",
                        tint = tokens.aiBubbleTextColor
                    )
                }

                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false },
                    modifier = Modifier
                        .width(265.dp)
                        .background(tokens.cardBackground)
                ) {
                    Text(
                        text = "SELIPER AI MODES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = tokens.accentCyan,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    SeliperMode.entries.forEach { mode ->
                        val isSelected = settings.activeMode == mode
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = mode.shortTitle,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) tokens.accentCyan else tokens.aiBubbleTextColor
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = modeIconFor(mode),
                                    contentDescription = null,
                                    tint = if (isSelected) tokens.accentCyan else tokens.subtleText,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = tokens.accentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            onClick = {
                                onSelectMode(mode)
                                showOverflowMenu = false
                            }
                        )
                    }

                    if (settings.activeMode == SeliperMode.FTA) {
                        HorizontalDivider(color = tokens.cardBorder)
                        Text(
                            text = "ACTIVE FTA TOPIC",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = tokens.accentCyan,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        FtaTopic.entries.forEach { topic ->
                            val isTopicSelected = settings.activeFtaTopic == topic
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${topic.displayName} [${topic.badge}]",
                                        color = if (isTopicSelected) tokens.accentCyan else tokens.aiBubbleTextColor,
                                        fontWeight = if (isTopicSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSelectFtaTopic(topic)
                                    showOverflowMenu = false
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = tokens.cardBorder)

                    DropdownMenuItem(
                        text = { Text("New Chat", color = tokens.aiBubbleTextColor) },
                        leadingIcon = { Icon(Icons.Default.Add, null, tint = tokens.accentCyan) },
                        onClick = {
                            showOverflowMenu = false
                            onNewChat()
                        }
                    )
                    if (hasActiveMessages) {
                        DropdownMenuItem(
                            text = { Text("Clear Current Chat", color = tokens.aiBubbleTextColor) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = tokens.subtleText) },
                            onClick = {
                                showOverflowMenu = false
                                onClearCurrentChat()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Chat History & Tools Drawer", color = tokens.aiBubbleTextColor) },
                        leadingIcon = { Icon(Icons.Default.Menu, null, tint = tokens.subtleText) },
                        onClick = {
                            showOverflowMenu = false
                            onOpenHistory()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings & Themes", color = tokens.aiBubbleTextColor) },
                        leadingIcon = { Icon(Icons.Default.Palette, null, tint = tokens.accentCyan) },
                        onClick = {
                            showOverflowMenu = false
                            onOpenSettings()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("10-Step Architecture Guide", color = tokens.aiBubbleTextColor) },
                        leadingIcon = { Icon(Icons.Default.School, null, tint = tokens.subtleText) },
                        onClick = {
                            showOverflowMenu = false
                            onOpenGuide()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("About SELIPER AI", color = tokens.aiBubbleTextColor) },
                        leadingIcon = { Icon(Icons.Default.Info, null, tint = tokens.subtleText) },
                        onClick = {
                            showOverflowMenu = false
                            onOpenAbout()
                        }
                    )
                }
            }
        }
    )
}

/**
 * ☰ Three-Line Sidebar Drawer housing all Modes, FTA Topics, Chat History, Settings, Themes & About
 * so the main chat screen remains clean and uncluttered.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeliperSidebarDrawer(
    settings: UserSettings,
    conversations: List<ConversationEntity>,
    activeConversationId: Long?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNewChat: () -> Unit,
    onSelectMode: (SeliperMode) -> Unit,
    onSelectFtaTopic: (FtaTopic) -> Unit,
    onSelectConversation: (Long) -> Unit,
    onRequestRename: (ConversationEntity) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onNavigateDestination: (SeliperDestination) -> Unit
) {
    val tokens = LocalSeliperTokens.current

    ModalDrawerSheet(
        drawerContainerColor = tokens.cardBackground,
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Brand Header
            SeliperHorizontalRibbonBanner(
                iconSize = 44.dp,
                fontSize = 20.sp,
                seliperTextColor = tokens.aiBubbleTextColor,
                subtitle = "Owned by BISWARUP MONDAL (BRM)",
                subtitleColor = tokens.accentCyan
            )

            // New Chat Button
            Button(
                onClick = onNewChat,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.accentCyan,
                    contentColor = Color(0xFF04141C)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drawer_new_chat_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Chat", fontWeight = FontWeight.Bold)
            }

            // Modes & FTA Selector inside the ☰ Menu
            Text(
                text = "AI MODES & TOOLS",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = tokens.accentCyan,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SeliperMode.entries.forEach { mode ->
                    val isSelected = settings.activeMode == mode
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) tokens.accentCyan.copy(alpha = 0.16f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMode(mode) }
                            .testTag("drawer_mode_${mode.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = modeIconFor(mode),
                                    contentDescription = null,
                                    tint = if (isSelected) tokens.accentCyan else tokens.subtleText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = mode.shortTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) tokens.accentCyan else tokens.aiBubbleTextColor,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = tokens.accentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // FTA Topic Chips inside drawer when FTA mode is active
            if (settings.activeMode == SeliperMode.FTA) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FtaTopic.entries.forEach { topic ->
                        val selected = settings.activeFtaTopic == topic
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) tokens.accentCyan else tokens.aiBubbleColor,
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (selected) tokens.accentCyan else tokens.cardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectFtaTopic(topic) }
                        ) {
                            Text(
                                text = topic.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (selected) Color(0xFF04141C) else tokens.aiBubbleTextColor,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = tokens.cardBorder)

            // Search Conversations
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search chat history…") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = tokens.subtleText)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drawer_search_input")
            )

            Text(
                text = "CHAT HISTORY (${conversations.size})",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = tokens.subtleText,
                    fontWeight = FontWeight.Bold
                )
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (conversations.isEmpty()) {
                    item {
                        Text(
                            text = "No saved chats yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = tokens.subtleText),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(conversations, key = { it.id }) { conv ->
                        val isSelected = conv.id == activeConversationId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) tokens.accentCyan.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) tokens.accentCyan else tokens.cardBorder.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectConversation(conv.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = conv.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = tokens.aiBubbleTextColor
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = conv.modeId.replace("_", " ").uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(color = tokens.accentCyan)
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = { onRequestRename(conv) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Rename chat",
                                            tint = tokens.subtleText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteConversation(conv.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete chat",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = tokens.cardBorder)

            DrawerNavRow(
                icon = Icons.Default.Settings,
                label = "Settings & Themes",
                onClick = { onNavigateDestination(SeliperDestination.SETTINGS) },
                tag = "drawer_nav_settings"
            )
            DrawerNavRow(
                icon = Icons.Default.School,
                label = "10-Step Build Guide",
                onClick = { onNavigateDestination(SeliperDestination.BUILD_GUIDE) },
                tag = "drawer_nav_guide"
            )
            DrawerNavRow(
                icon = Icons.Default.Info,
                label = "About SELIPER AI",
                onClick = { onNavigateDestination(SeliperDestination.ABOUT) },
                tag = "drawer_nav_about"
            )
        }
    }
}

@Composable
private fun DrawerNavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    val tokens = LocalSeliperTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tokens.accentCyan, modifier = Modifier.size(19.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(color = tokens.aiBubbleTextColor)
        )
    }
}

/**
 * Sections 4 & 5 — Premium Floating Pill Message Box anchored at the bottom of the screen.
 * - Large comfortable auto-growing typing area (1 to 6 lines)
 * - Rounded 28.dp corners, smooth luminous border, subtle shadow/glow
 * - Exact placeholder: "Message SELIPER AI..."
 * - Attachment menu (+ button for Image & Document), Voice Speech-to-Text button, and Send/Stop button
 * - Smooth mobile keyboard behavior via navigationBarsPadding() + imePadding()
 */
@Composable
private fun SeliperMessageComposer(
    inputPrompt: String,
    onInputChange: (String) -> Unit,
    isGenerating: Boolean,
    pendingAttachment: PendingAttachmentState,
    onClearAttachment: () -> Unit,
    onPickImage: () -> Unit,
    onPickDocument: () -> Unit,
    onStartVoiceInput: () -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    val tokens = LocalSeliperTokens.current
    var isFocused by remember { mutableStateOf(false) }
    var showAttachPopup by remember { mutableStateOf(false) }

    val canSend = inputPrompt.isNotBlank() ||
        pendingAttachment.imageBase64 != null ||
        pendingAttachment.fileContent != null

    val borderBrush = if (isFocused || canSend) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF833AB4), // Deep Violet
                Color(0xFFE1306C), // Pink-Magenta
                Color(0xFFFD1D1D)  // Red
            )
        )
    } else {
        SolidColor(tokens.cardBorder)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = tokens.cardBackground,
            shadowElevation = 10.dp,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .shadow(
                    elevation = if (isFocused) 14.dp else 8.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = tokens.accentCyan.copy(alpha = 0.28f),
                    spotColor = tokens.accentCyan.copy(alpha = 0.35f)
                )
                .border(
                    width = if (isFocused) 1.6.dp else 1.dp,
                    brush = borderBrush,
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pending Attachment Preview Pill
                if (pendingAttachment.imageUri != null || pendingAttachment.fileName != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = tokens.accentCyan.copy(alpha = 0.14f),
                        modifier = Modifier.border(
                            1.dp,
                            tokens.accentCyan.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = buildString {
                                    if (pendingAttachment.imageUri != null) append("🖼️ Image attached ")
                                    if (pendingAttachment.fileName != null) append("📄 ${pendingAttachment.fileName}")
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = tokens.accentCyan,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onClearAttachment,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove attachment",
                                    tint = tokens.accentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Main Composer Row (Attachment + Auto-Growing Input + Voice + Send/Stop)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left Attachment (+) Button with popup for Photo or Document
                    Box {
                        IconButton(
                            onClick = { showAttachPopup = true },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(tokens.aiBubbleColor)
                                .testTag("attach_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Attach image or file",
                                tint = tokens.accentCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showAttachPopup,
                            onDismissRequest = { showAttachPopup = false },
                            modifier = Modifier.background(tokens.cardBackground)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Attach Photo / Image", color = tokens.aiBubbleTextColor) },
                                leadingIcon = {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = tokens.accentCyan)
                                },
                                onClick = {
                                    showAttachPopup = false
                                    onPickImage()
                                },
                                modifier = Modifier.testTag("attach_image_button")
                            )
                            DropdownMenuItem(
                                text = { Text("Attach Document / Text File", color = tokens.aiBubbleTextColor) },
                                leadingIcon = {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = tokens.accentCyan)
                                },
                                onClick = {
                                    showAttachPopup = false
                                    onPickDocument()
                                },
                                modifier = Modifier.testTag("attach_document_button")
                            )
                        }
                    }

                    // Comfortable Auto-Growing Text Input Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 42.dp, max = 140.dp)
                            .padding(horizontal = 4.dp, vertical = 9.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputPrompt.isEmpty()) {
                            Text(
                                text = "Message SELIPER AI...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = tokens.subtleText.copy(alpha = 0.85f),
                                    fontSize = 16.sp
                                )
                            )
                        }
                        BasicTextField(
                            value = inputPrompt,
                            onValueChange = onInputChange,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = tokens.aiBubbleTextColor,
                                fontSize = 16.sp,
                                lineHeight = 22.sp
                            ),
                            cursorBrush = SolidColor(tokens.accentCyan),
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isFocused = it.isFocused }
                                .testTag("chat_message_input")
                        )
                    }

                    // Always-Available Micro-Mic Voice Button (Speech-to-Text)
                    if (!isGenerating) {
                        IconButton(
                            onClick = onStartVoiceInput,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF833AB4), // Violet
                                            Color(0xFFE1306C), // Pink
                                            Color(0xFFFD1D1D)  // Red
                                        )
                                    )
                                )
                                .testTag("voice_input_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Micro-Mic Voice Input",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Right Action Button: Stop Generation OR Send Message
                    if (isGenerating) {
                        IconButton(
                            onClick = onStop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF1744))
                                .testTag("stop_generation_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop generation",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onSend,
                            enabled = canSend,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = if (canSend) {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF833AB4),
                                                Color(0xFFE1306C),
                                                Color(0xFFFD1D1D)
                                            )
                                        )
                                    } else {
                                        SolidColor(tokens.cardBorder.copy(alpha = 0.6f))
                                    }
                                )
                                .testTag("send_message_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send message",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
