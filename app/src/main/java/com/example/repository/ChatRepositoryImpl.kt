package com.example.repository

import com.example.database.ChatDao
import com.example.database.ChatMessageEntity
import com.example.database.ConversationEntity
import com.example.domain.model.ChatMessage
import com.example.domain.model.Conversation
import com.example.domain.model.SenderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(private val chatDao: ChatDao) : ChatRepository {

    override fun getConversations(): Flow<List<Conversation>> {
        return chatDao.getConversations().map { entityList ->
            entityList.map { entity ->
                Conversation(
                    id = entity.id,
                    title = entity.title,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    override fun getConversationById(id: String): Flow<Conversation?> {
        // Return a flow representing a specific conversation
        return flow {
            emit(Conversation(id = id, title = "ဆွေးနွေးမှုအသစ်"))
        }
    }

    override suspend fun createNewConversation(title: String): String {
        val id = java.util.UUID.randomUUID().toString()
        chatDao.insertConversation(
            ConversationEntity(id = id, title = title, updatedAt = System.currentTimeMillis())
        )
        return id
    }

    override suspend fun saveMessage(conversationId: String, message: ChatMessage) {
        chatDao.insertMessage(
            ChatMessageEntity(
                id = message.id,
                conversationId = conversationId,
                sender = message.sender.name,
                text = message.text,
                timestamp = message.timestamp
            )
        )
    }

    override suspend fun searchConversations(query: String): List<Conversation> {
        return chatDao.searchConversations(query).map { entity ->
            Conversation(id = entity.id, title = entity.title, updatedAt = entity.updatedAt)
        }
    }

    override suspend fun deleteConversation(id: String) {
        chatDao.deleteConversation(id)
    }

    override suspend fun exportConversationAsText(id: String): String {
        return "Conversation $id Exported at ${System.currentTimeMillis()}"
    }
}
