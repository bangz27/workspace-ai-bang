package com.example.data.repository

import com.example.data.local.WorkspaceDao
import com.example.data.local.WorkspaceDatabase
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class WorkspaceRepository(
    private val dao: WorkspaceDao,
    val aiProvider: AiProvider = GeminiAiProvider()
) {
    val allConversations: Flow<List<ConversationEntity>> = dao.getAllConversations()
    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    fun getMessagesForConversation(convId: String): Flow<List<MessageEntity>> {
        return dao.getMessagesForConversation(convId)
    }

    suspend fun getConversationById(id: String): ConversationEntity? {
        return dao.getConversationById(id)
    }

    fun observeConversation(id: String): Flow<ConversationEntity?> {
        return dao.observeConversationById(id)
    }

    suspend fun getProjectById(id: String): ProjectEntity? {
        return dao.getProjectById(id)
    }

    suspend fun createConversation(
        title: String = "การสนทนาใหม่",
        projectId: String? = null,
        modelId: String = "gemini-3.5-flash"
    ): ConversationEntity {
        val conv = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            projectId = projectId,
            modelId = modelId
        )
        dao.insertConversation(conv)
        return conv
    }

    suspend fun updateConversationTitle(id: String, title: String) {
        dao.updateConversationTitle(id, title)
    }

    suspend fun deleteConversation(id: String) {
        dao.deleteMessagesForConversation(id)
        dao.deleteConversation(id)
    }

    suspend fun createProject(
        name: String,
        description: String,
        instructions: String,
        colorHex: String = "#6366F1"
    ): ProjectEntity {
        val project = ProjectEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            instructions = instructions,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            colorHex = colorHex
        )
        dao.insertProject(project)
        return project
    }

    suspend fun updateProject(project: ProjectEntity) {
        dao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(id: String) {
        dao.deleteProject(id)
    }

    suspend fun insertMessage(message: MessageEntity) {
        dao.insertMessage(message)
        // Also bump the conversation updatedAt
        dao.getConversationById(message.conversationId)?.let { conv ->
            dao.updateConversation(conv.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun updateMessageContent(id: String, content: String, status: String) {
        dao.updateMessageContent(id, content, status)
    }

    suspend fun search(query: String): List<ConversationEntity> {
        if (query.isBlank()) return emptyList()
        return dao.searchConversations(query.trim())
    }

    suspend fun clearAllData() {
        dao.clearAllMessages()
        dao.clearAllConversations()
        dao.clearAllProjects()
    }

    suspend fun resetDemoData() {
        clearAllData()
        WorkspaceDatabase.populateDemoData(dao)
    }
}
