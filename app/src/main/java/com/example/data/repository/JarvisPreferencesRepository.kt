package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.InterfaceMode
import com.example.data.model.JarvisSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JarvisPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<JarvisSettings> = _settings.asStateFlow()

    private fun loadSettings(): JarvisSettings {
        val geminiKey = prefs.getString("gemini_api_key", "") ?: ""
        val geminiModel = prefs.getString("gemini_model", "gemini-3.5-flash") ?: "gemini-3.5-flash"
        val pitch = prefs.getFloat("voice_pitch", 0.95f)
        val speed = prefs.getFloat("voice_speed", 1.05f)
        val spokenResponses = prefs.getBoolean("spoken_responses", true)
        val modeStr = prefs.getString("interface_mode", InterfaceMode.AUTOMATIC.name) ?: InterfaceMode.AUTOMATIC.name
        val mode = try { InterfaceMode.valueOf(modeStr) } catch (_: Exception) { InterfaceMode.AUTOMATIC }
        val confirm = prefs.getBoolean("require_confirm_action", false)
        val animQuality = prefs.getString("anim_quality", "High") ?: "High"

        return JarvisSettings(
            geminiApiKey = geminiKey,
            geminiModel = geminiModel,
            voicePitch = pitch,
            voiceSpeed = speed,
            spokenResponsesEnabled = spokenResponses,
            interfaceMode = mode,
            requireConfirmationBeforeAction = confirm,
            animationQuality = animQuality
        )
    }

    fun updateGeminiApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key.trim()).apply()
        _settings.value = _settings.value.copy(geminiApiKey = key.trim())
    }

    fun updateGeminiModel(model: String) {
        prefs.edit().putString("gemini_model", model).apply()
        _settings.value = _settings.value.copy(geminiModel = model)
    }

    fun updateVoicePitch(pitch: Float) {
        prefs.edit().putFloat("voice_pitch", pitch).apply()
        _settings.value = _settings.value.copy(voicePitch = pitch)
    }

    fun updateVoiceSpeed(speed: Float) {
        prefs.edit().putFloat("voice_speed", speed).apply()
        _settings.value = _settings.value.copy(voiceSpeed = speed)
    }

    fun updateSpokenResponses(enabled: Boolean) {
        prefs.edit().putBoolean("spoken_responses", enabled).apply()
        _settings.value = _settings.value.copy(spokenResponsesEnabled = enabled)
    }

    fun updateInterfaceMode(mode: InterfaceMode) {
        prefs.edit().putString("interface_mode", mode.name).apply()
        _settings.value = _settings.value.copy(interfaceMode = mode)
    }

    fun updateConfirmationRequired(required: Boolean) {
        prefs.edit().putBoolean("require_confirm_action", required).apply()
        _settings.value = _settings.value.copy(requireConfirmationBeforeAction = required)
    }

    fun updateAnimationQuality(quality: String) {
        prefs.edit().putString("anim_quality", quality).apply()
        _settings.value = _settings.value.copy(animationQuality = quality)
    }

    fun clearAllCredentials() {
        prefs.edit()
            .remove("gemini_api_key")
            .apply()
        _settings.value = _settings.value.copy(
            geminiApiKey = ""
        )
    }
}
