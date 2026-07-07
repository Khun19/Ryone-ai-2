package com.example.repository

import com.example.domain.model.ChatMessage
import com.example.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getConversations(): Flow<List<Conversation>>
    fun getConversationById(id: String): Flow<Conversation?>
    suspend fun createNewConversation(title: String): String
    suspend fun saveMessage(conversationId: String, message: ChatMessage)
    suspend fun searchConversations(query: String): List<Conversation>
    suspend fun deleteConversation(id: String)
    suspend fun exportConversationAsText(id: String): String
}
