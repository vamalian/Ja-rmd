package com.example.data.model

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING,
    ERROR
}

enum class InterfaceMode {
    AUTOMATIC,
    MOBILE,
    TABLET
}

enum class CapabilityStatus {
    AVAILABLE,
    PERMISSION_REQUIRED,
    SPECIAL_ACCESS_REQUIRED,
    UNSUPPORTED
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionSummary: String? = null,
    val actionSuccess: Boolean? = null
)

sealed class DeviceAction {
    data class LaunchApp(val appName: String, val packageName: String?) : DeviceAction()
    data class PrepareCall(val contactName: String?, val phoneNumber: String) : DeviceAction()
    data class PrepareSms(val recipient: String, val messageBody: String) : DeviceAction()
    data class ToggleTorch(val enable: Boolean) : DeviceAction()
    data class AdjustVolume(val direction: Int) : DeviceAction() // +1 for up, -1 for down, 0 for mute
    data class OpenSettings(val settingsType: String) : DeviceAction() // wifi, bluetooth, display, sound, apps, accessibility
    data class Scroll(val direction: String) : DeviceAction() // up, down
    data class MediaControl(val command: String) : DeviceAction() // play, pause, next, youtube
}

data class DeviceCapability(
    val id: String,
    val title: String,
    val description: String,
    val status: CapabilityStatus,
    val permissionOrRequirement: String,
    val actionCategory: String
)

data class JarvisTelemetry(
    val timeFormatted: String = "",
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val networkStatus: String = "Online",
    val isWifi: Boolean = true,
    val availableMemoryMb: Long = 0,
    val volumeLevelPercent: Int = 50,
    val torchActive: Boolean = false,
    val activeInterface: InterfaceMode = InterfaceMode.AUTOMATIC
)

data class JarvisSettings(
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-3.5-flash",
    val voicePitch: Float = 0.95f,
    val voiceSpeed: Float = 1.05f,
    val spokenResponsesEnabled: Boolean = true,
    val interfaceMode: InterfaceMode = InterfaceMode.AUTOMATIC,
    val requireConfirmationBeforeAction: Boolean = false,
    val animationQuality: String = "High" // High, Balanced, Minimal
)
