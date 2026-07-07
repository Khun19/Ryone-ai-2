package com.example.repository

import com.example.domain.model.MemoryRecord
import com.example.domain.model.UserPreference
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun getPreferences(): Flow<List<UserPreference>>
    fun getMemories(): Flow<List<MemoryRecord>>
    suspend fun savePreference(key: String, value: String)
    suspend fun addMemory(content: String, category: String)
    suspend fun deleteMemory(id: String)
    suspend fun clearAllMemory()
    suspend fun querySemanticMemory(prompt: String): List<MemoryRecord>
}
