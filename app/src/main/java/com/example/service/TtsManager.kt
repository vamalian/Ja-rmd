package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(
    private val context: Context,
    private val onSpeakingStateChanged: (Boolean) -> Unit
) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var currentPitch: Float = 0.95f
    private var currentSpeed: Float = 1.05f

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                configureJarvisVoice()

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        onSpeakingStateChanged(true)
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        onSpeakingStateChanged(false)
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        onSpeakingStateChanged(false)
                    }
                })
            }
        }
    }

    private fun configureJarvisVoice() {
        val t = tts ?: return
        // Prioritize UK English for J.A.R.V.I.S.
        var selectedLocale = Locale.UK
        if (t.isLanguageAvailable(Locale.UK) != TextToSpeech.LANG_AVAILABLE &&
            t.isLanguageAvailable(Locale.UK) != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            selectedLocale = Locale.US
        }
        t.language = selectedLocale

        // Select dignified male vocal profile if available on this device
        try {
            val voices = t.voices
            if (!voices.isNullOrEmpty()) {
                val bestVoice = voices.firstOrNull { voice ->
                    val name = voice.name.lowercase(Locale.ROOT)
                    (voice.locale == Locale.UK || voice.locale.language == "en") &&
                            (name.contains("male") || name.contains("en-gb")) &&
                            !name.contains("female")
                } ?: voices.firstOrNull { voice ->
                    voice.locale.language == "en" && !voice.name.lowercase(Locale.ROOT).contains("female")
                }
                if (bestVoice != null) {
                    t.voice = bestVoice
                }
            }
        } catch (_: Exception) {}

        t.setPitch(currentPitch)
        t.setSpeechRate(currentSpeed)
    }

    fun setVoiceParameters(pitch: Float, speed: Float) {
        currentPitch = pitch
        currentSpeed = speed
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speed)
    }

    fun speak(text: String, utteranceId: String = "jarvis_${System.currentTimeMillis()}") {
        if (!isInitialized || tts == null) return
        stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        onSpeakingStateChanged(false)
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }
}
