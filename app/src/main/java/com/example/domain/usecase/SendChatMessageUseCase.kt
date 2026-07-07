package com.example.domain.usecase

import com.example.domain.model.ChatMessage
import com.example.domain.model.SenderType
import com.example.repository.ChatRepository
import com.example.repository.MemoryRepository

class SendChatMessageUseCase(
    private val chatRepository: ChatRepository,
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(conversationId: String, text: String, onResponseChunk: (String) -> Unit): String {
        // Retrieve memory contexts to augment the generation (RAG-ready semantic memory)
        val memories = memoryRepository.querySemanticMemory(text)
        val contextPrompt = if (memories.isNotEmpty()) {
            "အသုံးပြုသူအကြောင်း အချက်အလက်များ:\n" + memories.joinToString("\n") { "- " + it.content } + "\n\n"
        } else {
            ""
        }

        // Save user message in the repository
        val userMsg = ChatMessage(sender = SenderType.USER, text = text)
        chatRepository.saveMessage(conversationId, userMsg)

        // Mock response pipeline or trigger actual API client streaming
        val rawAiResponse = "မင်္ဂလာပါခင်ဗျာ။ လူကြီးမင်း မေးမြန်းထားသော '$text' ကို ဆောင်ရွက်ပေးရန် အဆင်သင့်ရှိပါသည်။"
        
        // Simulating Chunk Streaming response to the presentation layer
        rawAiResponse.split(" ").forEach { chunk ->
            onResponseChunk("$chunk ")
            kotlinx.coroutines.delay(100)
        }

        val aiMsg = ChatMessage(sender = SenderType.ASSISTANT, text = rawAiResponse)
        chatRepository.saveMessage(conversationId, aiMsg)
        
        return rawAiResponse
    }
}
