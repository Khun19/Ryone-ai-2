package com.example.chat

import com.example.domain.model.ChatMessage
import com.example.domain.model.Conversation
import com.example.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ChatManager(private val chatRepository: ChatRepository) {
    
    fun loadActiveChats(): Flow<List<Conversation>> {
        return chatRepository.getConversations()
    }

    suspend fun createNewSession(initialTitle: String): String {
        return chatRepository.createNewConversation(initialTitle)
    }

    suspend fun postUserText(conversationId: String, text: String) {
        val userMsg = ChatMessage(
            sender = com.example.domain.model.SenderType.USER,
            text = text
        )
        chatRepository.saveMessage(conversationId, userMsg)
    }

    suspend fun postAssistantText(conversationId: String, responseText: String) {
        val assistantMsg = ChatMessage(
            sender = com.example.domain.model.SenderType.ASSISTANT,
            text = responseText
        )
        chatRepository.saveMessage(conversationId, assistantMsg)
    }

    suspend fun queryConversations(query: String): List<Conversation> {
        return chatRepository.searchConversations(query)
    }

    suspend fun removeConversation(id: String) {
        chatRepository.deleteConversation(id)
    }

    suspend fun createExportFileString(id: String): String {
        return chatRepository.exportConversationAsText(id)
    }
}
