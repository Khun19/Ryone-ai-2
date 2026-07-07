package com.example.tools

import com.example.repository.ToolRepository
import android.util.Log

class SystemToolDispatcher(private val toolRepository: ToolRepository) {
    private val tag = "SystemToolDispatcher"

    suspend fun executeToolCall(toolName: String, parameters: Map<String, String>): String {
        Log.d(tag, "Dispatching system tool: $toolName with parameters: $parameters")
        
        return when (toolName) {
            "toggleWifi" -> {
                val state = parameters["enabled"]?.toBoolean() ?: false
                val result = toolRepository.toggleWifi(state)
                if (result) "WiFi ${if (state) "opened" else "closed"} successfully." else "WiFi operation failed."
            }
            "toggleBluetooth" -> {
                val state = parameters["enabled"]?.toBoolean() ?: false
                val result = toolRepository.toggleBluetooth(state)
                if (result) "Bluetooth ${if (state) "opened" else "closed"} successfully." else "Bluetooth operation failed."
            }
            "toggleFlashlight" -> {
                val state = parameters["enabled"]?.toBoolean() ?: false
                val result = toolRepository.toggleFlashlight(state)
                if (result) "Flashlight ${if (state) "opened" else "closed"} successfully." else "Flashlight operation failed."
            }
            "setAlarm" -> {
                val hour = parameters["hour"]?.toIntOrNull() ?: 0
                val minute = parameters["minute"]?.toIntOrNull() ?: 0
                val label = parameters["label"] ?: "Alarm"
                val result = toolRepository.setAlarm(hour, minute, label)
                if (result) "Alarm set for $hour:$minute successfully." else "Set alarm failed."
            }
            "performCalculator" -> {
                val expression = parameters["expression"] ?: "0+0"
                toolRepository.performCalculator(expression)
            }
            "getLocalWeather" -> {
                val location = parameters["location"] ?: "Yangon"
                toolRepository.getLocalWeather(location)
            }
            else -> "သတ်မှတ်ထားသော ကိရိယာ မရှိပါခင်ဗျာ။"
        }
    }
}
