package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.model.AssistantState
import com.example.data.model.ChatMessage
import com.example.data.model.DeviceAction
import com.example.data.model.DeviceCapability
import com.example.data.model.InterfaceMode
import com.example.data.model.JarvisSettings
import com.example.data.model.JarvisTelemetry
import com.example.data.repository.JarvisPreferencesRepository
import com.example.service.ActionRouter
import com.example.service.DeviceControlManager
import com.example.service.GeminiService
import com.example.service.SpeechRecognitionManager
import com.example.service.TtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JarvisPreferencesRepository(application)
    val settings: StateFlow<JarvisSettings> = repository.settings

    val deviceControlManager = DeviceControlManager(application)
    private val actionRouter = ActionRouter(deviceControlManager)
    private val geminiService = GeminiService()

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _telemetry = MutableStateFlow(deviceControlManager.gatherTelemetry())
    val telemetry: StateFlow<JarvisTelemetry> = _telemetry.asStateFlow()

    private val _capabilities = MutableStateFlow<List<DeviceCapability>>(emptyList())
    val capabilities: StateFlow<List<DeviceCapability>> = _capabilities.asStateFlow()

    private val _pendingAction = MutableStateFlow<Pair<DeviceAction, String>?>(null)
    val pendingAction: StateFlow<Pair<DeviceAction, String>?> = _pendingAction.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>("JARVIS Core Initialized. Systems Nominal.")
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _geminiTestResult = MutableStateFlow<Pair<Boolean, String>?>(null)
    val geminiTestResult: StateFlow<Pair<Boolean, String>?> = _geminiTestResult.asStateFlow()

    // J.A.R.V.I.S. Native Speech & Audio Services
    private val ttsManager = TtsManager(application) { speaking ->
        if (speaking) {
            _assistantState.value = AssistantState.SPEAKING
        } else if (_assistantState.value == AssistantState.SPEAKING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    private val speechRecognitionManager = SpeechRecognitionManager(
        context = application,
        onResult = { text ->
            processUserInput(text)
        },
        onError = { errorMsg ->
            _assistantState.value = AssistantState.ERROR
            _statusBanner.value = errorMsg
            viewModelScope.launch {
                delay(3000)
                if (_assistantState.value == AssistantState.ERROR) {
                    _assistantState.value = AssistantState.IDLE
                }
            }
        },
        onReady = {
            _assistantState.value = AssistantState.LISTENING
            _statusBanner.value = "Awaiting voice command, Sir..."
        }
    )

    private var telemetryJob: Job? = null

    init {
        // Welcome message
        _messages.value = listOf(
            ChatMessage(
                isUser = false,
                text = "Good day, Sir. J.A.R.V.I.S. is online and standing by. All core subsystems and device integrations are operational."
            )
        )

        // Telemetry loop
        telemetryJob = viewModelScope.launch {
            while (isActive) {
                _telemetry.value = deviceControlManager.gatherTelemetry()
                refreshCapabilities()
                delay(2000)
            }
        }

        // Apply voice parameters from settings
        viewModelScope.launch {
            settings.collect { s ->
                ttsManager.setVoiceParameters(s.voicePitch, s.voiceSpeed)
            }
        }

        // Collect mic amplitude
        viewModelScope.launch {
            speechRecognitionManager.audioRmsDb.collect { rms ->
                if (_assistantState.value == AssistantState.LISTENING) {
                    _audioAmplitude.value = rms
                }
            }
        }
    }

    fun refreshCapabilities() {
        _capabilities.value = deviceControlManager.getCapabilitiesList()
    }

    fun startListening() {
        stopSpeaking()
        _assistantState.value = AssistantState.LISTENING
        speechRecognitionManager.startListening()
    }

    fun stopListening() {
        speechRecognitionManager.stopListening()
        if (_assistantState.value == AssistantState.LISTENING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
        if (_assistantState.value == AssistantState.SPEAKING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun testVoicePlayback() {
        stopSpeaking()
        speakAssistantResponse("At your service, Sir. Audio synthesis systems are functioning at optimal capacity.")
    }

    fun processUserInput(prompt: String) {
        if (prompt.isBlank()) return
        stopListening()
        stopSpeaking()

        val userMsg = ChatMessage(isUser = true, text = prompt)
        _messages.value = _messages.value + userMsg

        _assistantState.value = AssistantState.THINKING
        _statusBanner.value = "Analyzing query: \"$prompt\""

        viewModelScope.launch {
            // First check offline heuristic parser for quick and direct device commands
            val offlineParsed = actionRouter.parseOfflineCommand(prompt)
            val currentSettings = settings.value

            val effectiveApiKey = if (currentSettings.geminiApiKey.isNotBlank()) {
                currentSettings.geminiApiKey
            } else if (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
                BuildConfig.GEMINI_API_KEY
            } else {
                ""
            }

            if (offlineParsed != null) {
                val (reply, action) = offlineParsed
                handleExecutionAndReply(reply, action)
            } else if (effectiveApiKey.isNotBlank()) {
                // Call Gemini API with structured output
                val (reply, action) = geminiService.generateAssistantResponse(
                    apiKey = effectiveApiKey,
                    model = currentSettings.geminiModel,
                    userPrompt = prompt,
                    history = _messages.value.dropLast(1)
                )
                handleExecutionAndReply(reply, action)
            } else {
                // No API key and not a recognized offline command
                val reply = "Gemini neural uplink is not configured, Sir. Please enter your Gemini API Key in Settings to enable natural language reasoning. Meanwhile, device commands such as 'Open YouTube', 'Turn on flashlight', or 'Volume up' remain operational."
                handleExecutionAndReply(reply, null)
            }
        }
    }

    private suspend fun handleExecutionAndReply(replyText: String, action: DeviceAction?) {
        var actionReport: String? = null
        var actionSuccess: Boolean? = null

        if (action != null) {
            if (settings.value.requireConfirmationBeforeAction) {
                _pendingAction.value = Pair(action, replyText)
                _statusBanner.value = "Action confirmation required: $replyText"
                speakAssistantResponse("$replyText Shall I proceed with execution, Sir?")
                _assistantState.value = AssistantState.IDLE
                return
            } else {
                _assistantState.value = AssistantState.EXECUTING
                val (success, report) = actionRouter.executeAction(action)
                actionReport = report
                actionSuccess = success
                _statusBanner.value = report
                delay(600)
            }
        }

        val fullResponse = if (actionReport != null && actionReport != replyText) {
            "$replyText $actionReport"
        } else {
            replyText
        }

        val assistantMsg = ChatMessage(
            isUser = false,
            text = fullResponse,
            actionSummary = actionReport,
            actionSuccess = actionSuccess
        )
        _messages.value = _messages.value + assistantMsg

        speakAssistantResponse(fullResponse)
    }

    fun confirmPendingAction() {
        val pending = _pendingAction.value ?: return
        _pendingAction.value = null
        val (action, originalReply) = pending

        viewModelScope.launch {
            _assistantState.value = AssistantState.EXECUTING
            val (success, report) = actionRouter.executeAction(action)
            _statusBanner.value = report

            val finalMsg = ChatMessage(
                isUser = false,
                text = "Confirmed, Sir. $report",
                actionSummary = report,
                actionSuccess = success
            )
            _messages.value = _messages.value + finalMsg
            speakAssistantResponse(report)
        }
    }

    fun dismissPendingAction() {
        _pendingAction.value = null
        val msg = ChatMessage(isUser = false, text = "Action canceled, Sir. Standing by.")
        _messages.value = _messages.value + msg
        speakAssistantResponse("Action canceled, Sir.")
    }

    private fun speakAssistantResponse(text: String) {
        val currentSettings = settings.value
        if (!currentSettings.spokenResponsesEnabled) {
            _assistantState.value = AssistantState.IDLE
            return
        }

        _assistantState.value = AssistantState.SPEAKING
        ttsManager.speak(text)
    }

    fun clearConversation() {
        stopSpeaking()
        _messages.value = listOf(
            ChatMessage(
                isUser = false,
                text = "Conversation memory cleared, Sir. J.A.R.V.I.S. telemetry and subsystems remain nominal."
            )
        )
        _statusBanner.value = "Memory cleared."
    }

    // Settings actions
    fun setGeminiApiKey(key: String) = repository.updateGeminiApiKey(key)
    fun setGeminiModel(model: String) = repository.updateGeminiModel(model)
    fun setVoicePitch(pitch: Float) = repository.updateVoicePitch(pitch)
    fun setVoiceSpeed(speed: Float) = repository.updateVoiceSpeed(speed)
    fun setSpokenResponses(enabled: Boolean) = repository.updateSpokenResponses(enabled)
    fun setInterfaceMode(mode: InterfaceMode) = repository.updateInterfaceMode(mode)
    fun setRequireConfirmation(required: Boolean) = repository.updateConfirmationRequired(required)
    fun setAnimationQuality(quality: String) = repository.updateAnimationQuality(quality)
    fun clearCredentials() = repository.clearAllCredentials()

    fun testGeminiConnection() {
        val key = if (settings.value.geminiApiKey.isNotBlank()) {
            settings.value.geminiApiKey
        } else if (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
            BuildConfig.GEMINI_API_KEY
        } else {
            ""
        }
        val model = settings.value.geminiModel
        viewModelScope.launch {
            _geminiTestResult.value = Pair(false, "Testing connection to $model...")
            val result = geminiService.testConnection(key, model)
            _geminiTestResult.value = result
        }
    }

    override fun onCleared() {
        super.onCleared()
        telemetryJob?.cancel()
        speechRecognitionManager.stopListening()
        ttsManager.shutdown()
    }
}
