package com.example.domain.usecase

import com.example.repository.ToolRepository

class ProcessVoiceCommandUseCase(
    private val toolRepository: ToolRepository
) {
    suspend operator fun invoke(spokenText: String): String {
        val trimmed = spokenText.lowercase().trim()
        
        return when {
            trimmed.contains("wifi") || trimmed.contains("ဝိုင်ဖိုင်") -> {
                val turnOn = trimmed.contains("ဖွင့်") || trimmed.contains("on")
                val success = toolRepository.toggleWifi(turnOn)
                if (success) {
                    if (turnOn) "ဝိုင်ဖိုင်ကို အောင်မြင်စွာ ဖွင့်လိုက်ပါပြီခင်ဗျာ။" else "ဝိုင်ဖိုင်ကို အောင်မြင်စွာ ပိတ်လိုက်ပါပြီခင်ဗျာ။"
                } else {
                    "ဝိုင်ဖိုင် လုပ်ဆောင်ချက်ကို မလုပ်ဆောင်နိုင်ပါခင်ဗျာ။"
                }
            }
            trimmed.contains("bluetooth") || trimmed.contains("ဘလူးတု") -> {
                val turnOn = trimmed.contains("ဖွင့်") || trimmed.contains("on")
                val success = toolRepository.toggleBluetooth(turnOn)
                if (success) {
                    if (turnOn) "ဘလူးတုကို အောင်မြင်စွာ ဖွင့်လိုက်ပါပြီခင်ဗျာ။" else "ဘလူးတုကို အောင်မြင်စွာ ပိတ်လိုက်ပါပြီခင်ဗျာ။"
                } else {
                    "ဘလူးတု လုပ်ဆောင်ချက်ကို မလုပ်ဆောင်နိုင်ပါခင်ဗျာ။"
                }
            }
            trimmed.contains("flashlight") || trimmed.contains("ဓာတ်မီး") -> {
                val turnOn = trimmed.contains("ဖွင့်") || trimmed.contains("on")
                val success = toolRepository.toggleFlashlight(turnOn)
                if (success) {
                    if (turnOn) "ဓာတ်မီးကို အောင်မြင်စွာ ဖွင့်လိုက်ပါပြီခင်ဗျာ။" else "ဓာတ်မီးကို အောင်မြင်စွာ ပိတ်လိုက်ပါပြီခင်ဗျာ။"
                } else {
                    "ဓာတ်မီး လုပ်ဆောင်ချက်ကို မလုပ်ဆောင်နိုင်ပါခင်ဗျာ။"
                }
            }
            trimmed.contains("ဖုန်းခေါ်") || trimmed.contains("call") -> {
                val digits = trimmed.filter { it.isDigit() }
                if (digits.isNotEmpty()) {
                    val success = toolRepository.makePhoneCall(digits)
                    if (success) "ဖုန်းနံပါတ် $digits သို့ ခေါ်ဆိုနေပါပြီခင်ဗျာ။" else "ဖုန်းခေါ်ဆိုမှု မအောင်မြင်ပါခင်ဗျာ။"
                } else {
                    "ဖုန်းခေါ်ဆိုရန် ဖုန်းနံပါတ် သတ်သတ်မှတ်မှတ် ပြောပေးပါခင်ဗျာ။"
                }
            }
            else -> ""
        }
    }
}
