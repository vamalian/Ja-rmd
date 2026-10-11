package com.example.service

import com.example.data.model.DeviceAction
import java.util.Locale

class ActionRouter(
    private val deviceControlManager: DeviceControlManager
) {

    // Offline heuristic parser: handles commands immediately even if no API key is entered
    fun parseOfflineCommand(input: String): Pair<String, DeviceAction?>? {
        val lower = input.lowercase(Locale.ROOT).trim()

        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ")) {
            val app = lower.removePrefix("open ").removePrefix("launch ").removePrefix("start ").trim()
            if (app.contains("setting")) {
                return Pair("Accessing system configuration, Sir.", DeviceAction.OpenSettings("general"))
            }
            if (app.contains("camera")) {
                return Pair("Activating optical sensor, Sir.", DeviceAction.LaunchApp("Camera", null))
            }
            if (app.contains("youtube")) {
                return Pair("Launching YouTube, Sir.", DeviceAction.LaunchApp("YouTube", null))
            }
            if (app.contains("chrome") || app.contains("browser")) {
                return Pair("Launching Chrome browser, Sir.", DeviceAction.LaunchApp("Chrome", null))
            }
            if (app.contains("dialer") || app.contains("phone")) {
                return Pair("Opening communications dialer, Sir.", DeviceAction.LaunchApp("Dialer", null))
            }
            if (app.contains("map")) {
                return Pair("Loading tactical maps, Sir.", DeviceAction.LaunchApp("Maps", null))
            }
            return Pair("Locating and launching $app, Sir.", DeviceAction.LaunchApp(app, null))
        }

        if (lower.contains("torch") || lower.contains("flashlight")) {
            val enable = !lower.contains("off") && !lower.contains("disable")
            val msg = if (enable) "Activating illumination beam, Sir." else "Deactivating illumination beam, Sir."
            return Pair(msg, DeviceAction.ToggleTorch(enable))
        }

        if (lower.contains("volume up") || lower.contains("louder") || lower.contains("increase volume")) {
            return Pair("Increasing acoustic output, Sir.", DeviceAction.AdjustVolume(1))
        }

        if (lower.contains("volume down") || lower.contains("quieter") || lower.contains("lower volume")) {
            return Pair("Lowering acoustic output, Sir.", DeviceAction.AdjustVolume(-1))
        }

        if (lower.startsWith("call ") || lower.startsWith("dial ")) {
            val target = lower.removePrefix("call ").removePrefix("dial ").trim()
            return Pair("Preparing communications for $target, Sir.", DeviceAction.PrepareCall(target, target))
        }

        if (lower.startsWith("message ") || lower.startsWith("text ") || lower.startsWith("sms ")) {
            val parts = lower.split(" that ", " saying ", limit = 2)
            val recipient = parts[0].removePrefix("message ").removePrefix("text ").removePrefix("sms ").trim()
            val body = if (parts.size > 1) parts[1].trim() else ""
            return Pair("Drafting dispatch for $recipient, Sir.", DeviceAction.PrepareSms(recipient, body))
        }

        if (lower.contains("wifi") || lower.contains("wi-fi")) {
            return Pair("Opening Wi-Fi network configuration, Sir.", DeviceAction.OpenSettings("wifi"))
        }

        if (lower.contains("bluetooth")) {
            return Pair("Opening Bluetooth terminal, Sir.", DeviceAction.OpenSettings("bluetooth"))
        }

        if (lower.contains("display") || lower.contains("brightness")) {
            return Pair("Opening display calibration, Sir.", DeviceAction.OpenSettings("display"))
        }

        if (lower.contains("battery") || lower.contains("power")) {
            return Pair("Opening power diagnostic settings, Sir.", DeviceAction.OpenSettings("battery"))
        }

        if (lower.contains("scroll down")) {
            return Pair("Scrolling down, Sir.", DeviceAction.Scroll("down"))
        }

        if (lower.contains("scroll up")) {
            return Pair("Scrolling up, Sir.", DeviceAction.Scroll("up"))
        }

        return null
    }

    // Executes validated actions and returns (success: Boolean, reportMessage: String)
    fun executeAction(action: DeviceAction): Pair<Boolean, String> {
        return when (action) {
            is DeviceAction.LaunchApp -> {
                deviceControlManager.launchAppByName(action.appName)
            }
            is DeviceAction.PrepareCall -> {
                var phoneToUse = action.phoneNumber
                var contactDisplayName = action.contactName ?: action.phoneNumber
                if (action.contactName != null) {
                    val (resolvedName, resolvedNum) = deviceControlManager.resolveContactPhone(action.contactName)
                    if (resolvedNum != null) {
                        phoneToUse = resolvedNum
                        contactDisplayName = resolvedName ?: action.contactName
                    }
                }
                deviceControlManager.openDialer(phoneToUse)
            }
            is DeviceAction.PrepareSms -> {
                var recipientToUse = action.recipient
                val (resolvedName, resolvedNum) = deviceControlManager.resolveContactPhone(action.recipient)
                if (resolvedNum != null) {
                    recipientToUse = resolvedNum
                }
                deviceControlManager.prepareSms(recipientToUse, action.messageBody)
            }
            is DeviceAction.ToggleTorch -> {
                deviceControlManager.toggleFlashlight(action.enable)
            }
            is DeviceAction.AdjustVolume -> {
                deviceControlManager.adjustVolume(action.direction)
            }
            is DeviceAction.OpenSettings -> {
                deviceControlManager.openSettings(action.settingsType)
            }
            is DeviceAction.Scroll -> {
                val forward = action.direction.lowercase(Locale.ROOT) == "down"
                if (JarvisAccessibilityService.isRunning()) {
                    val scrolled = JarvisAccessibilityService.scrollCurrentWindow(forward)
                    if (scrolled) Pair(true, "Window scrolled, Sir.")
                    else Pair(false, "Window cannot be scrolled or target view is not interactive.")
                } else {
                    Pair(false, "Accessibility Service is not active. Please enable it in Settings for automated scrolling.")
                }
            }
            is DeviceAction.MediaControl -> {
                deviceControlManager.launchAppByName("YouTube")
            }
        }
    }
}
