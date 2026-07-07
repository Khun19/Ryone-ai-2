package com.example.repository

import com.example.domain.model.MemoryRecord
import com.example.domain.model.UserPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MemoryRepositoryImpl : MemoryRepository {
    private val _preferences = MutableStateFlow<List<UserPreference>>(
        listOf(
            UserPreference(key = "DEFAULT_LANGUAGE", value = "my"),
            UserPreference(key = "TIMEZONE", value = "Asia/Yangon"),
            UserPreference(key = "ENABLE_MEMORY", value = "true")
        )
    )
    private val _memories = MutableStateFlow<List<MemoryRecord>>(emptyList())

    override fun getPreferences(): Flow<List<UserPreference>> = _preferences.asStateFlow()

    override fun getMemories(): Flow<List<MemoryRecord>> = _memories.asStateFlow()

    override suspend fun savePreference(key: String, value: String) {
        val currentList = _preferences.value.toMutableList()
        val index = currentList.indexOfFirst { it.key == key }
        if (index != -1) {
            currentList[index] = currentList[index].copy(value = value)
        } else {
            currentList.add(UserPreference(key = key, value = value))
        }
        _preferences.value = currentList
    }

    override suspend fun addMemory(content: String, category: String) {
        val currentList = _memories.value.toMutableList()
        currentList.add(MemoryRecord(content = content, category = category))
        _memories.value = currentList
    }

    override suspend fun deleteMemory(id: String) {
        val currentList = _memories.value.toMutableList()
        currentList.removeAll { it.id == id }
        _memories.value = currentList
    }

    override suspend fun clearAllMemory() {
        _memories.value = emptyList()
    }

    override suspend fun querySemanticMemory(prompt: String): List<MemoryRecord> {
        // Mock semantic query based on keyword matching
        val normalizedPrompt = prompt.lowercase()
        return _memories.value.filter {
            normalizedPrompt.contains(it.content.lowercase()) ||
                    it.content.lowercase().contains(normalizedPrompt)
        }
    }
}
