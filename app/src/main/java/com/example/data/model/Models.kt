package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val projectId: String? = null,
    val modelId: String = "gemini-3.5-flash",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null,
    val imageMimeType: String? = "image/jpeg",
    val fileName: String? = null,
    val status: String = "sent" // "sent", "streaming", "error"
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val instructions: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val colorHex: String = "#6366F1",
    val iconName: String = "folder"
)

data class AiActionItem(
    val id: String,
    val titleKey: String,
    val promptTemplate: String,
    val iconType: String
)
