package com.example.domain.model

import java.util.UUID

data class UserPreference(
    val id: String = UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val isUserControlled: Boolean = true
)

data class MemoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val content: String, // e.g. "User name is Sayargyi"
    val category: String, // e.g. "Identity", "Preference", "Routine"
    val createdAt: Long = System.currentTimeMillis()
)
