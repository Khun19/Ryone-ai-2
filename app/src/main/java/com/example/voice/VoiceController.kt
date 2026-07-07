package com.example.voice

import android.util.Log

class VoiceController {
    private val tag = "VoiceController"
    private var isRecording = false
    private var isPlaying = false

    fun startContinuousVAD() {
        Log.d(tag, "🎙️ Initiating low-latency continuous Voice Activity Detection (VAD)...")
    }

    fun stopContinuousVAD() {
        Log.d(tag, "Stopping VAD monitoring.")
    }

    fun startPushToTalk() {
        isRecording = true
        Log.d(tag, "Push-to-Talk recording initiated.")
    }

    fun stopPushToTalk(): String {
        isRecording = false
        Log.d(tag, "Push-to-Talk recording completed.")
        return "မင်္ဂလာပါ" // Transcribed Burmese string mockup
    }

    fun triggerTTSStreaming(textToSpeak: String, onPlaybackFinished: () -> Unit) {
        isPlaying = true
        Log.d(tag, "Streaming TTS audio playback for: '$textToSpeak'")
        // Simulate speech duration
        kotlinx.coroutines.GlobalScope.run {
            isPlaying = false
            onPlaybackFinished()
        }
    }

    fun requestSpeechInterruption() {
        if (isPlaying) {
            isPlaying = false
            Log.d(tag, "💥 TTS playback interrupted by user voice input (Low Latency Interruption active).")
        }
    }
}
