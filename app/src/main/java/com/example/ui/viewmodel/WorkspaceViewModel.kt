package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WorkspaceDatabase
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.ProjectEntity
import com.example.data.repository.WorkspaceRepository
import com.example.i18n.Language
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WorkspaceDatabase.getInstance(application)
    val repository = WorkspaceRepository(db.workspaceDao())

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _currentProject = MutableStateFlow<ProjectEntity?>(null)
    val currentProject: StateFlow<ProjectEntity?> = _currentProject.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _composerText = MutableStateFlow("")
    val composerText: StateFlow<String> = _composerText.asStateFlow()

    private val _isListeningVoice = MutableStateFlow(false)
    val isListeningVoice: StateFlow<Boolean> = _isListeningVoice.asStateFlow()

    // Settings
    private val _selectedTheme = MutableStateFlow("dark") // Default to premium dark workspace
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(Language.TH) // Default Thai
    val selectedLanguage: StateFlow<Language> = _selectedLanguage.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ConversationEntity>>(emptyList())
    val searchResults: StateFlow<List<ConversationEntity>> = _searchResults.asStateFlow()

    private var messageObserverJob: Job? = null
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        // Observe conversations and pick first or create default
        viewModelScope.launch {
            conversations.collect { list ->
                if (_currentConversationId.value == null && list.isNotEmpty()) {
                    selectConversation(list.first().id)
                }
            }
        }
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
        messageObserverJob?.cancel()
        messageObserverJob = viewModelScope.launch {
            repository.getMessagesForConversation(id).collect { msgs ->
                _messages.value = msgs
            }
        }
        viewModelScope.launch {
            val conv = repository.getConversationById(id)
            if (conv?.projectId != null) {
                _currentProject.value = repository.getProjectById(conv.projectId)
            } else {
                _currentProject.value = null
            }
        }
    }

    fun startNewChat(projectId: String? = null) {
        viewModelScope.launch {
            val newConv = repository.createConversation(
                title = "แชทใหม่",
                projectId = projectId,
                modelId = _selectedModel.value
            )
            selectConversation(newConv.id)
            if (projectId != null) {
                _currentProject.value = repository.getProjectById(projectId)
            } else {
                _currentProject.value = null
            }
        }
    }

    fun setComposerText(text: String) {
        _composerText.value = text
    }

    fun sendMessage(content: String, imageBase64: String? = null, imageMimeType: String? = "image/jpeg") {
        if (content.isBlank() && imageBase64 == null) return
        val currentConvId = _currentConversationId.value ?: return
        val activeConv = conversations.value.find { it.id == currentConvId }

        viewModelScope.launch {
            // Auto update title if still default "แชทใหม่" or "New Chat"
            if (activeConv?.title == "แชทใหม่" || activeConv?.title == "New Chat" || activeConv?.title == "การสนทนาใหม่") {
                val newTitle = repository.aiProvider.generateTitle(content)
                repository.updateConversationTitle(currentConvId, newTitle)
            }

            // 1. Add user message
            val userMsg = MessageEntity(
                conversationId = currentConvId,
                role = "user",
                content = content,
                imageBase64 = imageBase64,
                imageMimeType = imageMimeType,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(userMsg)
            _composerText.value = ""

            // 2. Prepare AI streaming message placeholder
            val aiMsgId = UUID.randomUUID().toString()
            val aiMsg = MessageEntity(
                id = aiMsgId,
                conversationId = currentConvId,
                role = "model",
                content = "",
                timestamp = System.currentTimeMillis() + 10L,
                status = "streaming"
            )
            repository.insertMessage(aiMsg)
            _isGenerating.value = true

            // 3. Stream from AI Provider
            val projectInstructions = _currentProject.value?.instructions
            val currentHistory = _messages.value

            val streamBuffer = StringBuilder()
            val result = repository.aiProvider.streamMessage(
                modelName = _selectedModel.value,
                history = currentHistory,
                newMessage = content,
                projectInstructions = projectInstructions,
                imageBase64 = imageBase64,
                imageMimeType = imageMimeType,
                onChunk = { chunk ->
                    streamBuffer.append(chunk)
                    viewModelScope.launch {
                        repository.updateMessageContent(aiMsgId, streamBuffer.toString(), "streaming")
                    }
                }
            )

            _isGenerating.value = false
            if (result.isSuccess) {
                repository.updateMessageContent(aiMsgId, streamBuffer.toString(), "sent")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "ERROR"
                repository.updateMessageContent(aiMsgId, errorMsg, "error")
            }
        }
    }

    fun regenerateMessage(messageId: String) {
        val currentMsgs = _messages.value
        val targetIdx = currentMsgs.indexOfFirst { it.id == messageId }
        if (targetIdx == -1) return

        // Find last user message before this
        val prevUserMsg = currentMsgs.subList(0, targetIdx).lastOrNull { it.role == "user" }
        if (prevUserMsg == null) return

        val currentConvId = _currentConversationId.value ?: return

        viewModelScope.launch {
            // Delete previous model answer
            db.workspaceDao().deleteMessage(messageId)

            // Prepare new AI message
            val newAiMsgId = UUID.randomUUID().toString()
            val newAiMsg = MessageEntity(
                id = newAiMsgId,
                conversationId = currentConvId,
                role = "model",
                content = "",
                timestamp = System.currentTimeMillis(),
                status = "streaming"
            )
            repository.insertMessage(newAiMsg)
            _isGenerating.value = true

            val projectInstructions = _currentProject.value?.instructions
            val history = currentMsgs.filter { it.id != messageId && it.timestamp < prevUserMsg.timestamp }
            val streamBuffer = StringBuilder()

            val result = repository.aiProvider.streamMessage(
                modelName = _selectedModel.value,
                history = history,
                newMessage = prevUserMsg.content,
                projectInstructions = projectInstructions,
                imageBase64 = prevUserMsg.imageBase64,
                imageMimeType = prevUserMsg.imageMimeType,
                onChunk = { chunk ->
                    streamBuffer.append(chunk)
                    viewModelScope.launch {
                        repository.updateMessageContent(newAiMsgId, streamBuffer.toString(), "streaming")
                    }
                }
            )

            _isGenerating.value = false
            if (result.isSuccess) {
                repository.updateMessageContent(newAiMsgId, streamBuffer.toString(), "sent")
            } else {
                repository.updateMessageContent(newAiMsgId, result.exceptionOrNull()?.message ?: "ERROR", "error")
            }
        }
    }

    fun selectProject(project: ProjectEntity?) {
        _currentProject.value = project
        if (project != null) {
            startNewChat(projectId = project.id)
        }
    }

    fun createProject(name: String, description: String, instructions: String, colorHex: String = "#6366F1") {
        viewModelScope.launch {
            val proj = repository.createProject(name, description, instructions, colorHex)
            selectProject(proj)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_currentProject.value?.id == projectId) {
                _currentProject.value = null
            }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                val remaining = conversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    selectConversation(remaining.first().id)
                } else {
                    startNewChat()
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                _searchResults.value = repository.search(query)
            }
        }
    }

    fun setTheme(theme: String) {
        _selectedTheme.value = theme
    }

    fun setLanguage(language: Language) {
        _selectedLanguage.value = language
    }

    fun setModel(model: String) {
        _selectedModel.value = model
    }

    fun saveCustomApiKey(key: String) {
        _customApiKey.value = key
        repository.aiProvider.setApiKeyOverride(key)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _messages.value = emptyList()
            _currentProject.value = null
            _currentConversationId.value = null
            startNewChat()
        }
    }

    fun resetToDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
        }
    }

    fun exportCurrentChat(asMarkdown: Boolean): String {
        val conv = conversations.value.find { it.id == _currentConversationId.value }
        val msgs = _messages.value
        val sb = StringBuilder()

        val title = conv?.title ?: "AI Workspace Chat"
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        if (asMarkdown) {
            sb.append("# $title\n\n")
            sb.append("*Exported from AI Workspace on $date*\n\n")
            _currentProject.value?.let { proj ->
                sb.append("> **Project:** ${proj.name}\n\n")
            }
            sb.append("---\n\n")
            msgs.forEach { m ->
                val sender = if (m.role == "user") "### User" else "### AI Assistant"
                sb.append("$sender\n\n${m.content}\n\n")
            }
        } else {
            sb.append("$title\n")
            sb.append("Date: $date\n\n")
            msgs.forEach { m ->
                val sender = if (m.role == "user") "User:" else "AI:"
                sb.append("$sender\n${m.content}\n\n")
            }
        }
        return sb.toString()
    }

    fun startVoiceRecognition(context: Context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Voice recognition not available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListeningVoice.value = true
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListeningVoice.value = false
                    }
                    override fun onError(error: Int) {
                        _isListeningVoice.value = false
                    }
                    override fun onResults(results: Bundle?) {
                        _isListeningVoice.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognized = matches[0]
                            val current = _composerText.value
                            _composerText.value = if (current.isBlank()) recognized else "$current $recognized"
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (_selectedLanguage.value == Language.TH) "th-TH" else "en-US")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "พูดข้อความ...")
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListeningVoice.value = false
        }
    }

    fun stopVoiceRecognition() {
        speechRecognizer?.stopListening()
        _isListeningVoice.value = false
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
