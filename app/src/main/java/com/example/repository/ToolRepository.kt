package com.example.repository

interface ToolRepository {
    suspend fun toggleWifi(enable: Boolean): Boolean
    suspend fun toggleBluetooth(enable: Boolean): Boolean
    suspend fun toggleFlashlight(enable: Boolean): Boolean
    suspend fun makePhoneCall(number: String): Boolean
    suspend fun sendSms(number: String, message: String): Boolean
    suspend fun setAlarm(hour: Int, minute: Int, label: String): Boolean
    suspend fun getContacts(query: String): List<String>
    suspend fun getLocalWeather(location: String): String
    suspend fun performCalculator(expression: String): String
    suspend fun requireUserConfirmation(actionDescription: String): Boolean
}
