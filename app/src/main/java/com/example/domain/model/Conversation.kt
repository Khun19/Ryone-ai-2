package com.example.domain.model

import java.util.UUID

enum class SenderType {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: SenderType,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpeaking: Boolean = false,
    val hasVoiceFeedback: Boolean = false
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val messages: List<ChatMessage> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)
