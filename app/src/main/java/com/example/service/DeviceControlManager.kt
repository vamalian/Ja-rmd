package com.example.service

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.data.model.CapabilityStatus
import com.example.data.model.DeviceCapability
import com.example.data.model.JarvisTelemetry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeviceControlManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    private var isTorchOn: Boolean = false

    // App Launchers
    fun launchAppByName(name: String): Pair<Boolean, String> {
        val lower = name.lowercase(Locale.ROOT)
        val packageMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "camera" to "camera_intent",
            "dialer" to "dialer_intent",
            "phone" to "dialer_intent",
            "maps" to "com.google.android.apps.maps",
            "gmail" to "com.google.android.gm",
            "email" to "com.google.android.gm",
            "spotify" to "com.spotify.music",
            "settings" to "settings_intent"
        )

        for ((key, target) in packageMap) {
            if (lower.contains(key)) {
                return when (target) {
                    "camera_intent" -> openCamera()
                    "dialer_intent" -> openDialer()
                    "settings_intent" -> openSettings("general")
                    else -> launchPackage(target, key.replaceFirstChar { it.uppercase() })
                }
            }
        }

        // Try searching installed applications
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (appInfo in packages) {
                val label = pm.getApplicationLabel(appInfo).toString()
                if (label.lowercase(Locale.ROOT).contains(lower)) {
                    val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return Pair(true, "Launching $label, Sir.")
                    }
                }
            }
        } catch (e: Exception) {
            return Pair(false, "Failed to resolve application: ${e.localizedMessage}")
        }

        return Pair(false, "Application '$name' was not found on this terminal, Sir.")
    }

    private fun launchPackage(packageName: String, friendlyName: String): Pair<Boolean, String> {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Pair(true, "Launching $friendlyName, Sir.")
            } else {
                // Fallback to web if applicable (e.g. YouTube / Chrome)
                if (packageName.contains("youtube")) {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    Pair(true, "Launching YouTube portal in browser, Sir.")
                } else {
                    Pair(false, "$friendlyName is not installed on this device, Sir.")
                }
            }
        } catch (e: Exception) {
            Pair(false, "Could not open $friendlyName: ${e.localizedMessage}")
        }
    }

    fun openCamera(): Pair<Boolean, String> {
        return try {
            val intent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Pair(true, "Camera optical systems activated, Sir.")
            } else {
                val fallback = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_GALLERY)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
                Pair(true, "Accessing visual subsystem, Sir.")
            }
        } catch (e: Exception) {
            Pair(false, "Unable to launch optical sensor: ${e.localizedMessage}")
        }
    }

    fun openDialer(phoneNumber: String? = null): Pair<Boolean, String> {
        return try {
            val uri = if (!phoneNumber.isNullOrBlank()) {
                Uri.parse("tel:${Uri.encode(phoneNumber)}")
            } else {
                Uri.parse("tel:")
            }
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val msg = if (!phoneNumber.isNullOrBlank()) "Dialer prepared with $phoneNumber. Awaiting your confirmation to initiate call, Sir."
                      else "Communications dialer open, Sir."
            Pair(true, msg)
        } catch (e: Exception) {
            Pair(false, "Failed to launch communications dialer: ${e.localizedMessage}")
        }
    }

    fun prepareSms(recipient: String, messageText: String): Pair<Boolean, String> {
        return try {
            val uri = Uri.parse("smsto:${Uri.encode(recipient)}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Pair(true, "Transmission drafted for $recipient. Ready for your review and dispatch, Sir.")
        } catch (e: Exception) {
            Pair(false, "Failed to prepare message draft: ${e.localizedMessage}")
        }
    }

    fun resolveContactPhone(nameQuery: String): Pair<String?, String?> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return Pair(null, null)
        }
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$nameQuery%"),
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val resolvedName = it.getString(nameIndex)
                    val resolvedNum = it.getString(numIndex)
                    return Pair(resolvedName, resolvedNum)
                }
            }
        } catch (_: Exception) { }
        return Pair(null, null)
    }

    fun toggleFlashlight(forceState: Boolean? = null): Pair<Boolean, String> {
        val cm = cameraManager ?: return Pair(false, "Optical illumination module is unavailable.")
        return try {
            val cameraId = cm.cameraIdList.firstOrNull { id ->
                val chars = cm.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return Pair(false, "No flashlight hardware detected on this chassis, Sir.")

            val newState = forceState ?: !isTorchOn
            cm.setTorchMode(cameraId, newState)
            isTorchOn = newState
            val statusText = if (newState) "Illumination beam active, Sir." else "Illumination beam extinguished, Sir."
            Pair(true, statusText)
        } catch (e: Exception) {
            Pair(false, "Flashlight control encountered an anomaly: ${e.localizedMessage}")
        }
    }

    fun isFlashlightOn(): Boolean = isTorchOn

    fun adjustVolume(direction: Int): Pair<Boolean, String> {
        val am = audioManager ?: return Pair(false, "Acoustic controller unavailable.")
        return try {
            val adjust = when {
                direction > 0 -> AudioManager.ADJUST_RAISE
                direction < 0 -> AudioManager.ADJUST_LOWER
                else -> AudioManager.ADJUST_SAME
            }
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, adjust, AudioManager.FLAG_SHOW_UI)
            val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val pct = (current * 100) / max
            Pair(true, "Acoustic output calibrated to $pct%, Sir.")
        } catch (e: Exception) {
            Pair(false, "Volume adjustment failed: ${e.localizedMessage}")
        }
    }

    fun openSettings(type: String): Pair<Boolean, String> {
        val action = when (type.lowercase(Locale.ROOT)) {
            "wifi", "wi-fi", "internet" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "display", "brightness", "screen" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound", "audio", "volume" -> Settings.ACTION_SOUND_SETTINGS
            "battery", "power" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "apps", "application" -> Settings.ACTION_APPLICATION_SETTINGS
            "accessibility" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "location", "gps" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Pair(true, "Opening system settings for $type, Sir.")
        } catch (e: Exception) {
            // Fallback to main settings
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Pair(true, "Opening general system settings, Sir.")
            } catch (ex: Exception) {
                Pair(false, "Unable to access system settings: ${ex.localizedMessage}")
            }
        }
    }

    fun openAppDetailsSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    // Telemetry Collection
    fun gatherTelemetry(): JarvisTelemetry {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        var batteryPct = 100
        var isCharging = false
        batteryManager?.let {
            batteryPct = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val status = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        }

        var netStatus = "Offline"
        var isWifi = false
        connectivityManager?.let { cm ->
            val activeNetwork = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(activeNetwork)
            if (caps != null) {
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    netStatus = "Wi-Fi Connected"
                    isWifi = true
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    netStatus = "Cellular Online"
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    netStatus = "Ethernet Link"
                } else {
                    netStatus = "Online"
                }
            }
        }

        var availMemMb = 0L
        activityManager?.let { am ->
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)
            availMemMb = memInfo.availMem / (1024 * 1024)
        }

        var volPct = 50
        audioManager?.let { am ->
            val curr = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (max > 0) volPct = (curr * 100) / max
        }

        return JarvisTelemetry(
            timeFormatted = timeStr,
            batteryPercent = batteryPct,
            isCharging = isCharging,
            networkStatus = netStatus,
            isWifi = isWifi,
            availableMemoryMb = availMemMb,
            volumeLevelPercent = volPct,
            torchActive = isTorchOn
        )
    }

    // Device Capabilities Audit
    fun getCapabilitiesList(): List<DeviceCapability> {
        val micStatus = if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            CapabilityStatus.AVAILABLE else CapabilityStatus.PERMISSION_REQUIRED

        val contactStatus = if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            CapabilityStatus.AVAILABLE else CapabilityStatus.PERMISSION_REQUIRED

        val cameraStatus = if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            CapabilityStatus.AVAILABLE else CapabilityStatus.PERMISSION_REQUIRED

        val accessibilityStatus = if (JarvisAccessibilityService.isRunning())
            CapabilityStatus.AVAILABLE else CapabilityStatus.SPECIAL_ACCESS_REQUIRED

        return listOf(
            DeviceCapability(
                id = "vocal_capture",
                title = "Vocal Audio Capture",
                description = "Speech-to-text input via native Android SpeechRecognizer.",
                status = micStatus,
                permissionOrRequirement = "RECORD_AUDIO",
                actionCategory = "Voice Interface"
            ),
            DeviceCapability(
                id = "app_launch",
                title = "Application Launcher",
                description = "Direct package launching (YouTube, Chrome, Camera, Dialer, etc.).",
                status = CapabilityStatus.AVAILABLE,
                permissionOrRequirement = "Supported via Intent / Package Queries",
                actionCategory = "System Control"
            ),
            DeviceCapability(
                id = "telecom_dialer",
                title = "Communications Dialer",
                description = "Initiating phone call preparation with user confirmation.",
                status = CapabilityStatus.AVAILABLE,
                permissionOrRequirement = "ACTION_DIAL (Safe confirmation model)",
                actionCategory = "Communications"
            ),
            DeviceCapability(
                id = "contact_resolution",
                title = "Contact Resolution",
                description = "Resolving names to phone numbers before preparing calls/messages.",
                status = contactStatus,
                permissionOrRequirement = "READ_CONTACTS",
                actionCategory = "Communications"
            ),
            DeviceCapability(
                id = "messaging_draft",
                title = "Message Dispatch & Draft",
                description = "Drafting SMS messages for user review and sending.",
                status = CapabilityStatus.AVAILABLE,
                permissionOrRequirement = "ACTION_SENDTO",
                actionCategory = "Communications"
            ),
            DeviceCapability(
                id = "torch_subsystem",
                title = "Flashlight / Illumination",
                description = "Activating device LED torch via CameraManager.",
                status = cameraStatus,
                permissionOrRequirement = "CAMERA / Flashlight Hardware",
                actionCategory = "Hardware Control"
            ),
            DeviceCapability(
                id = "acoustic_output",
                title = "Audio Output Calibration",
                description = "Dynamic system stream volume adjustment.",
                status = CapabilityStatus.AVAILABLE,
                permissionOrRequirement = "AudioManager API",
                actionCategory = "System Control"
            ),
            DeviceCapability(
                id = "system_settings",
                title = "System Control Panels",
                description = "Navigating to Wi-Fi, Bluetooth, Display, and Battery settings.",
                status = CapabilityStatus.AVAILABLE,
                permissionOrRequirement = "Android Settings Intents",
                actionCategory = "System Control"
            ),
            DeviceCapability(
                id = "accessibility_gestures",
                title = "Accessibility Gestures & Scrolling",
                description = "Hands-free scrolling of supported windows.",
                status = accessibilityStatus,
                permissionOrRequirement = "Accessibility Service Setting",
                actionCategory = "Accessibility"
            )
        )
    }
}
