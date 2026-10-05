package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AiProvider
import com.example.data.model.FtaTopic
import com.example.data.model.SeliperMode
import com.example.data.model.SeliperTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "seliper_user_settings")

data class UserSettings(
    val themeId: String = SeliperTheme.DARK_FUTURISTIC.id,
    val selectedModeId: String = SeliperMode.SUPER_AGENT.id,
    val selectedFtaTopicId: String = FtaTopic.CODING.id,
    val selectedProviderId: String = AiProvider.GOOGLE_GEMINI.id,
    val selectedModelId: String = "gemini-3.5-flash",
    val customModelOverride: String = "",
    val language: String = "English",
    val streamingEnabled: Boolean = true,
    val strictSafetyEnabled: Boolean = true,
    val kingThinkingSummaryEnabled: Boolean = true,
    val agentToolsEnabled: Boolean = true,
    val startupAnimationEnabled: Boolean = true,
    val currentArchitectureStep: Int = 1 // Step 1..10 interactive walkthrough tracker
) {
    val activeTheme: SeliperTheme
        get() = SeliperTheme.entries.find { it.id == themeId } ?: SeliperTheme.DARK_FUTURISTIC

    val activeMode: SeliperMode
        get() = SeliperMode.entries.find { it.id == selectedModeId } ?: SeliperMode.SUPER_AGENT

    val activeFtaTopic: FtaTopic
        get() = FtaTopic.entries.find { it.id == selectedFtaTopicId } ?: FtaTopic.CODING

    val activeProvider: AiProvider
        get() = AiProvider.entries.find { it.id == selectedProviderId } ?: AiProvider.GOOGLE_GEMINI

    val effectiveModelId: String
        get() = customModelOverride.trim().ifEmpty { selectedModelId }
}

class SettingsPreferences(private val context: Context) {

    private object Keys {
        val THEME_ID = stringPreferencesKey("theme_id")
        val MODE_ID = stringPreferencesKey("mode_id")
        val FTA_TOPIC_ID = stringPreferencesKey("fta_topic_id")
        val PROVIDER_ID = stringPreferencesKey("provider_id")
        val MODEL_ID = stringPreferencesKey("model_id")
        val CUSTOM_MODEL = stringPreferencesKey("custom_model")
        val LANGUAGE = stringPreferencesKey("language")
        val STREAMING = booleanPreferencesKey("streaming_enabled")
        val STRICT_SAFETY = booleanPreferencesKey("strict_safety_enabled")
        val KING_THINKING = booleanPreferencesKey("king_thinking_enabled")
        val AGENT_TOOLS = booleanPreferencesKey("agent_tools_enabled")
        val STARTUP_ANIMATION = booleanPreferencesKey("startup_animation_enabled")
        val CURRENT_STEP = intPreferencesKey("current_architecture_step")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            themeId = prefs[Keys.THEME_ID] ?: SeliperTheme.DARK_FUTURISTIC.id,
            selectedModeId = prefs[Keys.MODE_ID] ?: SeliperMode.SUPER_AGENT.id,
            selectedFtaTopicId = prefs[Keys.FTA_TOPIC_ID] ?: FtaTopic.CODING.id,
            selectedProviderId = prefs[Keys.PROVIDER_ID] ?: AiProvider.GOOGLE_GEMINI.id,
            selectedModelId = prefs[Keys.MODEL_ID] ?: "gemini-3.5-flash",
            customModelOverride = prefs[Keys.CUSTOM_MODEL] ?: "",
            language = prefs[Keys.LANGUAGE] ?: "English",
            streamingEnabled = prefs[Keys.STREAMING] ?: true,
            strictSafetyEnabled = prefs[Keys.STRICT_SAFETY] ?: true,
            kingThinkingSummaryEnabled = prefs[Keys.KING_THINKING] ?: true,
            agentToolsEnabled = prefs[Keys.AGENT_TOOLS] ?: true,
            startupAnimationEnabled = prefs[Keys.STARTUP_ANIMATION] ?: true,
            currentArchitectureStep = prefs[Keys.CURRENT_STEP] ?: 1
        )
    }

    suspend fun setTheme(theme: SeliperTheme) {
        context.dataStore.edit { it[Keys.THEME_ID] = theme.id }
    }

    suspend fun setMode(mode: SeliperMode) {
        context.dataStore.edit { it[Keys.MODE_ID] = mode.id }
    }

    suspend fun setFtaTopic(topic: FtaTopic) {
        context.dataStore.edit { it[Keys.FTA_TOPIC_ID] = topic.id }
    }

    suspend fun setProviderAndModel(provider: AiProvider, modelId: String) {
        context.dataStore.edit {
            it[Keys.PROVIDER_ID] = provider.id
            it[Keys.MODEL_ID] = modelId
        }
    }

    suspend fun setCustomModelOverride(customModel: String) {
        context.dataStore.edit { it[Keys.CUSTOM_MODEL] = customModel }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun setStreamingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.STREAMING] = enabled }
    }

    suspend fun setStrictSafetyEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.STRICT_SAFETY] = enabled }
    }

    suspend fun setKingThinkingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KING_THINKING] = enabled }
    }

    suspend fun setAgentToolsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AGENT_TOOLS] = enabled }
    }

    suspend fun setStartupAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.STARTUP_ANIMATION] = enabled }
    }

    suspend fun setArchitectureStep(step: Int) {
        context.dataStore.edit { it[Keys.CURRENT_STEP] = step.coerceIn(1, 10) }
    }
}
