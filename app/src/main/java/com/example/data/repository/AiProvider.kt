package com.example.data.repository

import com.example.data.model.MessageEntity
import com.example.data.remote.GeminiClient
import org.json.JSONArray
import org.json.JSONObject

interface AiProvider {
    val providerName: String
    fun isReady(): Boolean
    fun setApiKeyOverride(key: String?)

    suspend fun streamMessage(
        modelName: String,
        history: List<MessageEntity>,
        newMessage: String,
        projectInstructions: String?,
        imageBase64: String?,
        imageMimeType: String?,
        onChunk: (String) -> Unit
    ): Result<String>

    suspend fun generateTitle(firstUserMessage: String): String
}

class GeminiAiProvider(
    private val geminiClient: GeminiClient = GeminiClient()
) : AiProvider {

    override val providerName: String = "Google Gemini"

    override fun isReady(): Boolean = geminiClient.isApiKeyConfigured()

    override fun setApiKeyOverride(key: String?) {
        geminiClient.setCustomApiKey(key)
    }

    override suspend fun streamMessage(
        modelName: String,
        history: List<MessageEntity>,
        newMessage: String,
        projectInstructions: String?,
        imageBase64: String?,
        imageMimeType: String?,
        onChunk: (String) -> Unit
    ): Result<String> {
        // Build system instructions combining Global Workspace prompt and Project instructions
        val systemPromptBuilder = StringBuilder()
        systemPromptBuilder.append(
            "You are an intelligent, proactive AI Workspace assistant. " +
            "Your user interface and default response language is Thai (ภาษาไทย). " +
            "Respond naturally, professionally, and clearly in Thai unless the user requests otherwise. " +
            "Format your answers with structured Markdown: use headers, bullet points, checklists, code blocks with language identifiers, and tables where suitable."
        )

        if (!projectInstructions.isNullOrBlank()) {
            systemPromptBuilder.append("\n\n[PROJECT CONTEXT & INSTRUCTIONS]:\n")
            systemPromptBuilder.append(projectInstructions)
            systemPromptBuilder.append("\nStrictly adhere to the project instructions above.")
        }

        // Build contents array for Gemini
        val contentsArray = JSONArray()

        // Include last 10 messages for conversation context
        val contextHistory = history.takeLast(10)
        for (msg in contextHistory) {
            val contentObj = JSONObject()
            val role = if (msg.role == "user") "user" else "model"
            contentObj.put("role", role)

            val partsArray = JSONArray()
            if (!msg.imageBase64.isNullOrEmpty()) {
                val inlineData = JSONObject().apply {
                    put("mimeType", msg.imageMimeType ?: "image/jpeg")
                    put("data", msg.imageBase64)
                }
                partsArray.put(JSONObject().apply { put("inlineData", inlineData) })
            }
            if (msg.content.isNotEmpty()) {
                partsArray.put(JSONObject().apply { put("text", msg.content) })
            }
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
        }

        // Add the current incoming message
        val currentContentObj = JSONObject().apply {
            put("role", "user")
            val currentParts = JSONArray()
            if (!imageBase64.isNullOrEmpty()) {
                val inlineData = JSONObject().apply {
                    put("mimeType", imageMimeType ?: "image/jpeg")
                    put("data", imageBase64)
                }
                currentParts.put(JSONObject().apply { put("inlineData", inlineData) })
            }
            currentParts.put(JSONObject().apply { put("text", newMessage) })
            put("parts", currentParts)
        }
        contentsArray.put(currentContentObj)

        val resolvedModel = when (modelName) {
            "fast" -> "gemini-3.5-flash"
            "advanced" -> "gemini-3.1-pro-preview"
            else -> if (modelName.contains("gemini")) modelName else "gemini-3.5-flash"
        }

        return geminiClient.streamGenerateContent(
            modelName = resolvedModel,
            systemInstruction = systemPromptBuilder.toString(),
            contentsPayload = contentsArray,
            onChunkReceived = onChunk
        )
    }

    override suspend fun generateTitle(firstUserMessage: String): String {
        val trimmed = firstUserMessage.trim()
        if (trimmed.length <= 30) return trimmed
        val words = trimmed.take(28)
        return "$words..."
    }
}
