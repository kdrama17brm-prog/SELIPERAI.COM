package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.local.MessageEntity
import com.example.data.model.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

// --- Gemini REST Data Classes ---

@Serializable
data class GenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

// --- OpenAI / xAI Compatible REST Data Classes ---

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatCompletionMessage>,
    val temperature: Float = 0.7f,
    val stream: Boolean = false
)

@Serializable
data class ChatCompletionMessage(
    val role: String,
    val content: String
)

interface GeminiRetrofitService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:streamGenerateContent?alt=sse")
    @Streaming
    suspend fun generateContentStream(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): ResponseBody
}

sealed class AiServiceResult {
    data class Success(val fullText: String) : AiServiceResult()
    data class NotConnected(
        val provider: AiProvider,
        val message: String = "AI service is not connected. Please configure the API."
    ) : AiServiceResult()
    data class Error(
        val userFriendlyMessage: String,
        val technicalDetail: String
    ) : AiServiceResult()
}

/**
 * Multi-Model Provider Gateway (Section 6, 7, 14, 18)
 * Provider -> Model -> SELIPER AI Backend -> Selected Mode -> User Response
 */
object MultiModelGateway {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val geminiService: GeminiRetrofitService by lazy {
        Retrofit.Builder()
            .baseUrl(AiProvider.GOOGLE_GEMINI.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiRetrofitService::class.java)
    }

    /**
     * Checks whether the required API key in BuildConfig is genuinely configured
     * and not empty or the placeholder default in .env.example.
     */
    fun isProviderConnected(provider: AiProvider): Boolean {
        val rawKey = getRawApiKey(provider).trim()
        if (rawKey.isEmpty()) return false
        val placeholders = setOf(
            "MY_GEMINI_API_KEY",
            "MY_OPENAI_API_KEY",
            "MY_XAI_API_KEY",
            "YOUR_API_KEY",
            "null"
        )
        return rawKey !in placeholders
    }

    fun getRawApiKey(provider: AiProvider): String {
        return when (provider) {
            AiProvider.GOOGLE_GEMINI -> BuildConfig.GEMINI_API_KEY
            AiProvider.OPENAI_COMPATIBLE -> BuildConfig.OPENAI_API_KEY
            AiProvider.XAI_GROK -> BuildConfig.XAI_API_KEY
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 82, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun generateResponse(
        provider: AiProvider,
        modelId: String,
        systemInstruction: String,
        history: List<MessageEntity>,
        currentUserPrompt: String,
        attachedImageBase64: String?,
        attachedFileName: String?,
        attachedFileContent: String?,
        stream: Boolean,
        onStreamChunk: (String) -> Unit
    ): AiServiceResult = withContext(Dispatchers.IO) {
        // Section 6: If the AI API is not connected, clearly show:
        // "AI service is not connected. Please configure the API."
        if (!isProviderConnected(provider)) {
            return@withContext AiServiceResult.NotConnected(
                provider = provider,
                message = "AI service is not connected. Please configure the API."
            )
        }

        val apiKey = getRawApiKey(provider).trim()
        val effectivePrompt = buildPromptWithAttachments(
            userPrompt = currentUserPrompt,
            fileName = attachedFileName,
            fileContent = attachedFileContent
        )

        try {
            when (provider) {
                AiProvider.GOOGLE_GEMINI -> executeGeminiCall(
                    apiKey = apiKey,
                    modelId = modelId,
                    systemInstruction = systemInstruction,
                    history = history,
                    effectivePrompt = effectivePrompt,
                    attachedImageBase64 = attachedImageBase64,
                    stream = stream,
                    onStreamChunk = onStreamChunk
                )
                AiProvider.OPENAI_COMPATIBLE, AiProvider.XAI_GROK -> executeOpenAiCompatibleCall(
                    provider = provider,
                    apiKey = apiKey,
                    modelId = modelId,
                    systemInstruction = systemInstruction,
                    history = history,
                    effectivePrompt = effectivePrompt
                )
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            mapExceptionToBeginnerFriendlyError(e)
        }
    }

    private suspend fun executeGeminiCall(
        apiKey: String,
        modelId: String,
        systemInstruction: String,
        history: List<MessageEntity>,
        effectivePrompt: String,
        attachedImageBase64: String?,
        stream: Boolean,
        onStreamChunk: (String) -> Unit
    ): AiServiceResult {
        val contents = mutableListOf<GeminiContent>()

        // Include up to last 14 messages from conversation history for context window efficiency
        val recentHistory = history.filter { !it.isError && it.content.isNotBlank() }.takeLast(14)
        for (msg in recentHistory) {
            val geminiRole = if (msg.role == "user") "user" else "model"
            contents.add(
                GeminiContent(
                    role = geminiRole,
                    parts = listOf(GeminiPart(text = msg.content))
                )
            )
        }

        // Current turn user parts
        val currentParts = mutableListOf<GeminiPart>()
        currentParts.add(GeminiPart(text = effectivePrompt))
        if (!attachedImageBase64.isNullOrBlank()) {
            currentParts.add(
                GeminiPart(
                    inlineData = GeminiInlineData(
                        mimeType = "image/jpeg",
                        data = attachedImageBase64
                    )
                )
            )
        }
        contents.add(GeminiContent(role = "user", parts = currentParts))

        val request = GenerateContentRequest(
            contents = contents,
            generationConfig = GeminiGenerationConfig(temperature = 0.7f, topP = 0.95f, topK = 40),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
        )

        if (stream) {
            val accumulated = StringBuilder()
            val responseBody = geminiService.generateContentStream(
                model = modelId,
                apiKey = apiKey,
                request = request
            )
            responseBody.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    currentCoroutineContext().ensureActive()
                    val rawLine = line?.trim() ?: continue
                    if (!rawLine.startsWith("data:")) continue
                    val jsonPayload = rawLine.removePrefix("data:").trim()
                    if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue
                    try {
                        val chunkObj = json.parseToJsonElement(jsonPayload).jsonObject
                        val partsArray = chunkObj["candidates"]?.jsonArray
                            ?.getOrNull(0)?.jsonObject
                            ?.get("content")?.jsonObject
                            ?.get("parts")?.jsonArray
                        if (partsArray != null) {
                            for (partElem in partsArray) {
                                val textPiece = partElem.jsonObject["text"]?.jsonPrimitive?.content
                                if (!textPiece.isNullOrEmpty()) {
                                    accumulated.append(textPiece)
                                    onStreamChunk(accumulated.toString())
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Ignore malformed partial SSE keep-alive lines
                    }
                }
            }
            val finalStr = accumulated.toString().trim()
            if (finalStr.isEmpty()) {
                // Fallback to non-streaming if SSE returned empty
                val nonStreamResp = geminiService.generateContent(modelId, apiKey, request)
                val fallbackText = nonStreamResp.candidates
                    ?.firstOrNull()?.content?.parts
                    ?.mapNotNull { it.text }
                    ?.joinToString("")
                    ?.trim()
                return if (!fallbackText.isNullOrEmpty()) {
                    onStreamChunk(fallbackText)
                    AiServiceResult.Success(fallbackText)
                } else {
                    AiServiceResult.Error(
                        userFriendlyMessage = "The AI model returned an empty response. Please try rephrasing your question.",
                        technicalDetail = "Empty candidate parts from $modelId"
                    )
                }
            }
            return AiServiceResult.Success(finalStr)
        } else {
            val response = geminiService.generateContent(
                model = modelId,
                apiKey = apiKey,
                request = request
            )
            val text = response.candidates
                ?.firstOrNull()?.content?.parts
                ?.mapNotNull { it.text }
                ?.joinToString("")
                ?.trim()
            return if (!text.isNullOrEmpty()) {
                AiServiceResult.Success(text)
            } else {
                AiServiceResult.Error(
                    userFriendlyMessage = "The AI model returned an empty response. Please try rephrasing your question.",
                    technicalDetail = "No candidates in response from $modelId"
                )
            }
        }
    }

    private fun executeOpenAiCompatibleCall(
        provider: AiProvider,
        apiKey: String,
        modelId: String,
        systemInstruction: String,
        history: List<MessageEntity>,
        effectivePrompt: String
    ): AiServiceResult {
        val messages = mutableListOf<ChatCompletionMessage>()
        messages.add(ChatCompletionMessage(role = "system", content = systemInstruction))
        history.filter { !it.isError && it.content.isNotBlank() }.takeLast(12).forEach { msg ->
            messages.add(ChatCompletionMessage(role = msg.role, content = msg.content))
        }
        messages.add(ChatCompletionMessage(role = "user", content = effectivePrompt))

        val payload = json.encodeToString(
            ChatCompletionRequest.serializer(),
            ChatCompletionRequest(model = modelId, messages = messages, temperature = 0.7f, stream = false)
        )
        val endpoint = "${provider.baseUrl.trimEnd('/')}/v1/chat/completions"
        val httpRequest = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        okHttpClient.newCall(httpRequest).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return mapHttpStatusToFriendlyError(response.code, bodyStr, provider.displayName)
            }
            val root = json.parseToJsonElement(bodyStr).jsonObject
            val content = root["choices"]?.jsonArray
                ?.getOrNull(0)?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content
                ?.trim()
            return if (!content.isNullOrEmpty()) {
                AiServiceResult.Success(content)
            } else {
                AiServiceResult.Error(
                    userFriendlyMessage = "The AI service returned an empty response. Please try again.",
                    technicalDetail = "Empty choices in ${provider.displayName} response"
                )
            }
        }
    }

    private fun buildPromptWithAttachments(
        userPrompt: String,
        fileName: String?,
        fileContent: String?
    ): String {
        if (fileContent.isNullOrBlank()) return userPrompt
        val truncatedContent = if (fileContent.length > 12000) {
            fileContent.take(12000) + "\n...[Document truncated at 12,000 characters for context window]"
        } else {
            fileContent
        }
        return """
            [ATTACHED DOCUMENT FILE: ${fileName ?: "document.txt"}]
            ```
            $truncatedContent
            ```
            
            USER REQUEST:
            $userPrompt
        """.trimIndent()
    }

    private fun mapHttpStatusToFriendlyError(
        code: Int,
        body: String,
        providerName: String
    ): AiServiceResult.Error {
        val friendly = when (code) {
            400 -> "The AI service could not process this request (HTTP 400). Please check that the selected model name is valid."
            401, 403 -> "Invalid or unauthorized API key for $providerName (HTTP $code). Please check your API key in the AI Studio Secrets panel and try again."
            404 -> "The selected AI model is currently unavailable (HTTP 404). Try switching to 'Gemini 3.5 Flash' in Settings."
            429 -> "Rate limit reached (HTTP 429). The AI service is receiving too many requests right now. Please wait a few seconds and tap Retry."
            500, 502, 503, 504 -> "The AI provider ($providerName) is temporarily busy or unavailable (HTTP $code). Please wait a moment and try again."
            else -> "Something went wrong while connecting to the AI service. Please check your API configuration and try again."
        }
        return AiServiceResult.Error(
            userFriendlyMessage = friendly,
            technicalDetail = "HTTP $code: ${body.take(220)}"
        )
    }

    private fun mapExceptionToBeginnerFriendlyError(e: Exception): AiServiceResult.Error {
        return when (e) {
            is HttpException -> {
                val errBody = try { e.response()?.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                mapHttpStatusToFriendlyError(e.code(), errBody, "AI Service")
            }
            is SocketTimeoutException -> AiServiceResult.Error(
                userFriendlyMessage = "The connection to the AI service timed out. Please check your internet connection or try a shorter prompt.",
                technicalDetail = "SocketTimeoutException: ${e.message}"
            )
            is UnknownHostException -> AiServiceResult.Error(
                userFriendlyMessage = "No internet connection detected. Please check your network and try again.",
                technicalDetail = "UnknownHostException: ${e.message}"
            )
            is IOException -> AiServiceResult.Error(
                userFriendlyMessage = "Something went wrong while connecting to the AI service. Please check your API configuration and try again.",
                technicalDetail = "Network IOException: ${e.message}"
            )
            else -> AiServiceResult.Error(
                userFriendlyMessage = "Something went wrong while connecting to the AI service. Please check your API configuration and try again.",
                technicalDetail = "${e.javaClass.simpleName}: ${e.message}"
            )
        }
    }
}
