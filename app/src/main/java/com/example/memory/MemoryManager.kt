package com.example.memory

import com.example.domain.model.MemoryRecord
import com.example.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow

class MemoryManager(private val memoryRepository: MemoryRepository) {

    fun observeAllMemories(): Flow<List<MemoryRecord>> {
        return memoryRepository.getMemories()
    }

    suspend fun savePersonalDetail(detail: String, category: String = "UserDetail") {
        memoryRepository.addMemory(detail, category)
    }

    suspend fun forgetDetail(id: String) {
        memoryRepository.deleteMemory(id)
    }

    suspend fun forgetAllDetails() {
        memoryRepository.clearAllMemory()
    }

    suspend fun retrieveContextForPrompt(userPrompt: String): String {
        val matches = memoryRepository.querySemanticMemory(userPrompt)
        return if (matches.isNotEmpty()) {
            "မိတ်ဆက်အချက်အလက်:\n" + matches.joinToString("\n") { "- " + it.content }
        } else {
            ""
        }
    }
}
