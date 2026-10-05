package com.example.ui

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.SeliperDatabase
import com.example.data.local.SettingsPreferences
import com.example.data.local.UserSettings
import com.example.data.model.AiProvider
import com.example.data.model.ConnectedToolExecution
import com.example.data.model.FtaTopic
import com.example.data.model.SeliperMode
import com.example.data.model.SeliperTheme
import com.example.data.remote.AiServiceResult
import com.example.data.remote.MultiModelGateway
import com.example.domain.SafetyPolicyEngine
import com.example.domain.SeliperAgentTools
import com.example.domain.SeliperPromptArchitect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

enum class SeliperDestination {
    CHAT,
    BUILD_GUIDE,
    SETTINGS,
    ABOUT
}

data class PendingAttachmentState(
    val imageUri: String? = null,
    val imageBase64: String? = null,
    val fileName: String? = null,
    val fileContent: String? = null
)

class SeliperViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SeliperDatabase.getDatabase(application).seliperDao()
    private val prefs = SettingsPreferences(application)
    private val json = Json { ignoreUnknownKeys = true }

    val userSettings: StateFlow<UserSettings> = prefs.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    private val _currentDestination = MutableStateFlow(SeliperDestination.CHAT)
    val currentDestination: StateFlow<SeliperDestination> = _currentDestination.asStateFlow()

    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingPartialText = MutableStateFlow("")
    val streamingPartialText: StateFlow<String> = _streamingPartialText.asStateFlow()

    private val _pendingAttachment = MutableStateFlow(PendingAttachmentState())
    val pendingAttachment: StateFlow<PendingAttachmentState> = _pendingAttachment.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private var activeGenerationJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val conversations: StateFlow<List<ConversationEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) dao.getAllConversations()
            else dao.searchConversations(query.trim())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { convId ->
            if (convId == null) flowOf(emptyList())
            else dao.getMessagesForConversation(convId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun navigateTo(destination: SeliperDestination) {
        _currentDestination.value = destination
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun selectConversation(conversationId: Long) {
        _activeConversationId.value = conversationId
        _currentDestination.value = SeliperDestination.CHAT
        viewModelScope.launch {
            val conv = dao.getConversationById(conversationId) ?: return@launch
            SeliperMode.entries.find { it.id == conv.modeId }?.let { prefs.setMode(it) }
            FtaTopic.entries.find { it.id == conv.ftaTopicId }?.let { prefs.setFtaTopic(it) }
        }
    }

    fun startNewChat() {
        stopGeneration()
        _activeConversationId.value = null
        _pendingAttachment.value = PendingAttachmentState()
        _currentDestination.value = SeliperDestination.CHAT
    }

    fun clearCurrentChat() {
        val convId = _activeConversationId.value ?: return
        stopGeneration()
        viewModelScope.launch {
            dao.clearMessagesInConversation(convId)
            _statusBannerMessage.value = "Chat history cleared for this conversation."
        }
    }

    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            if (_activeConversationId.value == conversationId) {
                stopGeneration()
                _activeConversationId.value = null
            }
            dao.deleteConversation(conversationId)
        }
    }

    fun renameConversation(conversationId: Long, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            dao.renameConversation(conversationId, trimmed)
        }
    }

    fun clearAllConversations() {
        stopGeneration()
        viewModelScope.launch {
            dao.deleteAllConversations()
            _activeConversationId.value = null
            _statusBannerMessage.value = "All conversations cleared."
        }
    }

    fun selectMode(mode: SeliperMode) {
        viewModelScope.launch { prefs.setMode(mode) }
    }

    fun selectFtaTopic(topic: FtaTopic) {
        viewModelScope.launch {
            prefs.setFtaTopic(topic)
            if (userSettings.value.activeMode != SeliperMode.FTA) {
                prefs.setMode(SeliperMode.FTA)
            }
        }
    }

    fun selectTheme(theme: SeliperTheme) {
        viewModelScope.launch { prefs.setTheme(theme) }
    }

    fun selectProviderAndModel(provider: AiProvider, modelId: String) {
        viewModelScope.launch { prefs.setProviderAndModel(provider, modelId) }
    }

    fun updateCustomModelOverride(customModel: String) {
        viewModelScope.launch { prefs.setCustomModelOverride(customModel) }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch { prefs.setLanguage(language) }
    }

    fun updateStreamingEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setStreamingEnabled(enabled) }
    }

    fun updateStrictSafetyEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setStrictSafetyEnabled(enabled) }
    }

    fun updateKingThinkingEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setKingThinkingEnabled(enabled) }
    }

    fun updateAgentToolsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setAgentToolsEnabled(enabled) }
    }

    fun updateStartupAnimationEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setStartupAnimationEnabled(enabled) }
    }

    fun setArchitectureGuideStep(step: Int) {
        viewModelScope.launch { prefs.setArchitectureStep(step) }
    }

    fun attachImageFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    if (bitmap != null) {
                        val b64 = MultiModelGateway.bitmapToBase64(bitmap)
                        _pendingAttachment.value = _pendingAttachment.value.copy(
                            imageUri = uri.toString(),
                            imageBase64 = b64
                        )
                    }
                }
            } catch (e: Exception) {
                _statusBannerMessage.value = "Could not load selected image: ${e.message}"
            }
        }
    }

    fun attachDocumentFromUri(uri: Uri, displayName: String) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                    it.readText()
                }
                if (!content.isNullOrBlank()) {
                    _pendingAttachment.value = _pendingAttachment.value.copy(
                        fileName = displayName,
                        fileContent = content
                    )
                } else {
                    _statusBannerMessage.value = "The selected file appears to be empty or binary."
                }
            } catch (e: Exception) {
                _statusBannerMessage.value = "Could not read selected file: ${e.message}"
            }
        }
    }

    fun clearPendingAttachment() {
        _pendingAttachment.value = PendingAttachmentState()
    }

    fun stopGeneration() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        _isGenerating.value = false
        _streamingPartialText.value = ""
    }

    fun sendMessage(rawInput: String) {
        val prompt = rawInput.trim()
        val attachment = _pendingAttachment.value
        if (prompt.isEmpty() && attachment.imageBase64 == null && attachment.fileContent == null) return

        val effectivePrompt = prompt.ifEmpty {
            if (attachment.imageBase64 != null) "Please analyze this image in detail."
            else "Please analyze and summarize the attached document."
        }

        _pendingAttachment.value = PendingAttachmentState()
        executeTurn(
            userPrompt = effectivePrompt,
            attachedImageUri = attachment.imageUri,
            attachedImageBase64 = attachment.imageBase64,
            attachedFileName = attachment.fileName,
            attachedFileContent = attachment.fileContent
        )
    }

    fun regenerateLastResponse() {
        val convId = _activeConversationId.value ?: return
        if (_isGenerating.value) return

        viewModelScope.launch {
            val currentList = dao.getMessagesListSync(convId)
            val lastUserMessage = currentList.lastOrNull { it.role == "user" } ?: return@launch
            val lastMessage = currentList.lastOrNull()
            if (lastMessage != null && lastMessage.role == "assistant") {
                dao.deleteMessageById(lastMessage.id)
            }
            executeTurn(
                userPrompt = lastUserMessage.content,
                attachedImageUri = lastUserMessage.attachedImageUri,
                attachedImageBase64 = null,
                attachedFileName = lastUserMessage.attachedFileName,
                attachedFileContent = lastUserMessage.attachedFileContent,
                skipInsertingUserMessage = true
            )
        }
    }

    fun editUserMessageAndResubmit(message: MessageEntity, updatedPrompt: String) {
        val cleanPrompt = updatedPrompt.trim()
        if (cleanPrompt.isEmpty()) return
        stopGeneration()

        viewModelScope.launch {
            dao.deleteMessagesFromTimestamp(message.conversationId, message.timestamp)
            executeTurn(
                userPrompt = cleanPrompt,
                attachedImageUri = message.attachedImageUri,
                attachedImageBase64 = null,
                attachedFileName = message.attachedFileName,
                attachedFileContent = message.attachedFileContent
            )
        }
    }

    private fun executeTurn(
        userPrompt: String,
        attachedImageUri: String?,
        attachedImageBase64: String?,
        attachedFileName: String?,
        attachedFileContent: String?,
        skipInsertingUserMessage: Boolean = false
    ) {
        stopGeneration()
        val settings = userSettings.value
        val mode = settings.activeMode
        val ftaTopic = settings.activeFtaTopic
        val provider = settings.activeProvider
        val modelId = settings.effectiveModelId

        activeGenerationJob = viewModelScope.launch {
            _isGenerating.value = true
            _streamingPartialText.value = ""

            val convId = _activeConversationId.value ?: run {
                val autoTitle = userPrompt.lineSequence().firstOrNull()?.take(42)?.trim()
                    .orEmpty().ifEmpty { "${mode.shortTitle} Chat" }
                val newId = dao.insertConversation(
                    ConversationEntity(
                        title = autoTitle,
                        modeId = mode.id,
                        ftaTopicId = ftaTopic.id,
                        providerId = provider.id,
                        modelId = modelId
                    )
                )
                _activeConversationId.value = newId
                newId
            }

            val priorMessages = dao.getMessagesListSync(convId)

            if (!skipInsertingUserMessage) {
                dao.insertMessage(
                    MessageEntity(
                        conversationId = convId,
                        role = "user",
                        content = userPrompt,
                        modeId = mode.id,
                        ftaTopicId = if (mode == SeliperMode.FTA) ftaTopic.id else null,
                        attachedImageUri = attachedImageUri,
                        attachedFileName = attachedFileName,
                        attachedFileContent = attachedFileContent
                    )
                )
            }

            val safetyResult = SafetyPolicyEngine.evaluateUserPrompt(userPrompt)
            if (!safetyResult.isSafe && safetyResult.refusalMessage != null) {
                dao.insertMessage(
                    MessageEntity(
                        conversationId = convId,
                        role = "assistant",
                        content = safetyResult.refusalMessage,
                        modeId = mode.id,
                        ftaTopicId = if (mode == SeliperMode.FTA) ftaTopic.id else null,
                        modelUsed = "SELIPER Safety Guard",
                        isError = false
                    )
                )
                _isGenerating.value = false
                return@launch
            }

            val toolExecutions = SeliperAgentTools.runConnectedToolsIfNeeded(
                prompt = userPrompt,
                attachedFileName = attachedFileName,
                attachedFileContent = attachedFileContent,
                hasAttachedImage = !attachedImageBase64.isNullOrBlank(),
                agentToolsEnabled = settings.agentToolsEnabled
            )
            val toolLogsSerialized = if (toolExecutions.isNotEmpty()) {
                json.encodeToString(ListSerializer(ConnectedToolExecution.serializer()), toolExecutions)
            } else null

            val systemPrompt = SeliperPromptArchitect.buildSystemPrompt(
                mode = mode,
                ftaTopic = ftaTopic,
                language = settings.language,
                kingThinkingEnabled = settings.kingThinkingSummaryEnabled,
                toolExecutions = toolExecutions,
                hasAttachedFile = !attachedFileContent.isNullOrBlank(),
                hasAttachedImage = !attachedImageBase64.isNullOrBlank()
            )

            val apiResult = MultiModelGateway.generateResponse(
                provider = provider,
                modelId = modelId,
                systemInstruction = systemPrompt,
                history = priorMessages,
                currentUserPrompt = userPrompt,
                attachedImageBase64 = attachedImageBase64,
                attachedFileName = attachedFileName,
                attachedFileContent = attachedFileContent,
                stream = settings.streamingEnabled,
                onStreamChunk = { partial ->
                    _streamingPartialText.value = partial
                }
            )

            when (apiResult) {
                is AiServiceResult.Success -> {
                    dao.insertMessage(
                        MessageEntity(
                            conversationId = convId,
                            role = "assistant",
                            content = apiResult.fullText,
                            modeId = mode.id,
                            ftaTopicId = if (mode == SeliperMode.FTA) ftaTopic.id else null,
                            modelUsed = "${provider.displayName} • $modelId",
                            toolLogsJson = toolLogsSerialized,
                            reasoningSummary = if (settings.kingThinkingSummaryEnabled) {
                                "King of Best Thinking • ${mode.shortTitle}${if (mode == SeliperMode.FTA) " (${ftaTopic.displayName})" else ""}"
                            } else null,
                            isError = false
                        )
                    )
                }

                is AiServiceResult.NotConnected -> {
                    val disconnectedNotice = buildString {
                        appendLine("**AI service is not connected. Please configure the API.**")
                        appendLine()
                        appendLine("To connect **${apiResult.provider.displayName}** in AI Studio:")
                        appendLine("1. Open the **Secrets** panel in Google AI Studio.")
                        appendLine("2. Set **`${apiResult.provider.secretKeyName}`** to your valid API key.")
                        appendLine("3. Return here and tap **Retry**.")
                        if (toolExecutions.isNotEmpty()) {
                            appendLine()
                            appendLine("---")
                            appendLine("*Local connected SELIPER tools executed:*")
                            toolExecutions.forEach { t ->
                                appendLine("- **${t.toolName}**: `${t.toolOutput}`")
                            }
                        }
                    }
                    dao.insertMessage(
                        MessageEntity(
                            conversationId = convId,
                            role = "assistant",
                            content = disconnectedNotice.trim(),
                            modeId = mode.id,
                            ftaTopicId = if (mode == SeliperMode.FTA) ftaTopic.id else null,
                            modelUsed = "${provider.displayName} (Not Connected)",
                            toolLogsJson = toolLogsSerialized,
                            isError = true
                        )
                    )
                }

                is AiServiceResult.Error -> {
                    val errorBlock = buildString {
                        appendLine("**${apiResult.userFriendlyMessage}**")
                        appendLine()
                        appendLine("`Diagnostic: ${apiResult.technicalDetail}`")
                        if (toolExecutions.isNotEmpty()) {
                            appendLine()
                            appendLine("*Verified Local Connected Tool Output:*")
                            toolExecutions.forEach { t ->
                                appendLine("- **${t.toolName}**: `${t.toolOutput}`")
                            }
                        }
                    }
                    dao.insertMessage(
                        MessageEntity(
                            conversationId = convId,
                            role = "assistant",
                            content = errorBlock.trim(),
                            modeId = mode.id,
                            ftaTopicId = if (mode == SeliperMode.FTA) ftaTopic.id else null,
                            modelUsed = "$modelId (Connection Error)",
                            toolLogsJson = toolLogsSerialized,
                            isError = true
                        )
                    )
                }
            }

            dao.getConversationById(convId)?.let { existing ->
                dao.updateConversation(existing.copy(updatedAt = System.currentTimeMillis()))
            }

            _streamingPartialText.value = ""
            _isGenerating.value = false
        }
    }

    fun decodeToolLogs(toolLogsJson: String?): List<ConnectedToolExecution> {
        if (toolLogsJson.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(ConnectedToolExecution.serializer()), toolLogsJson)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
