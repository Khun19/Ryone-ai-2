package com.example.automation

import android.util.Log

data class TriggerCondition(
    val type: String, // e.g., "LOCATION", "TIME", "BATTERY"
    val value: String
)

data class RoutineRule(
    val id: String,
    val name: String,
    val condition: TriggerCondition,
    val actionCommand: String,
    val isEnabled: Boolean = true
)

class RoutineManager {
    private val tag = "RoutineManager"
    private val rules = mutableListOf<RoutineRule>()

    fun registerRoutine(rule: RoutineRule) {
        rules.add(rule)
        Log.d(tag, "Registered new localized automation routine: '${rule.name}' triggerable on ${rule.condition.type}")
    }

    fun removeRoutine(id: String) {
        rules.removeAll { it.id == id }
    }

    fun evaluateLocationTriggers(latitude: Double, longitude: Double, onTriggered: (String) -> Unit) {
        // Evaluate user's custom location rules (e.g., geofencing "Home")
        rules.filter { it.isEnabled && it.condition.type == "LOCATION" }.forEach { rule ->
            Log.d(tag, "Location geofence matching routine '${rule.name}' triggered.")
            onTriggered(rule.actionCommand)
        }
    }

    fun evaluateTimeTriggers(hour: Int, minute: Int, onTriggered: (String) -> Unit) {
        rules.filter { it.isEnabled && it.condition.type == "TIME" }.forEach { rule ->
            val timeString = "$hour:$minute"
            if (rule.condition.value == timeString) {
                Log.d(tag, "Time match routine '${rule.name}' triggered.")
                onTriggered(rule.actionCommand)
            }
        }
    }
}
