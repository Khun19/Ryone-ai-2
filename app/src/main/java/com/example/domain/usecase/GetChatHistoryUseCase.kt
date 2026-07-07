package com.example.domain.usecase

import com.example.domain.model.Conversation
import com.example.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetChatHistoryUseCase(private val chatRepository: ChatRepository) {
    operator fun invoke(): Flow<List<Conversation>> {
        return chatRepository.getConversations()
    }
}
