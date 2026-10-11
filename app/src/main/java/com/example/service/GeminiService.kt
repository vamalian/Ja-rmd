package com.example.service

import com.example.data.model.ChatMessage
import com.example.data.model.DeviceAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val systemInstructionText = """
        You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the advanced AI assistant inspired by Tony Stark's personal AI from Iron Man.
        Personality traits:
        - Intelligent, calm, polite, distinguished, confident, and professional.
        - Address the user as 'Sir' or respectfully in an English/British cadence when natural.
        - Be concise, helpful, and articulate. Avoid childish humor or excessive chatter.
        - You have direct integration with Android device controls via structured JSON actions.
        
        When the user asks to control the device or open an application, you MUST output a JSON response in the following schema:
        {
          "reply": "Spoken text response for the user, Sir.",
          "action": {
            "type": "LAUNCH_APP" | "DIAL_CALL" | "SEND_SMS" | "TOGGLE_TORCH" | "ADJUST_VOLUME" | "OPEN_SETTINGS" | "SCROLL" | "MEDIA_CONTROL",
            "appName": "string (for LAUNCH_APP, e.g. 'YouTube', 'Chrome', 'Camera', 'Dialer', 'Settings')",
            "contact": "string (for DIAL_CALL / SEND_SMS)",
            "phoneNumber": "string (optional phone number)",
            "messageBody": "string (for SEND_SMS)",
            "enable": boolean (for TOGGLE_TORCH),
            "direction": integer (1 for up, -1 for down) or string ("up" / "down" for SCROLL),
            "setting": "string (for OPEN_SETTINGS, e.g. 'wifi', 'bluetooth', 'display', 'sound', 'battery', 'accessibility')",
            "mediaCommand": "string (for MEDIA_CONTROL, e.g. 'play', 'pause')"
          }
        }
        
        If the user asks an ordinary question or conversational query without asking to manipulate the device, reply with:
        {
          "reply": "Your intelligent answer here, Sir.",
          "action": null
        }
        
        Always return valid JSON only. Do not wrap in markdown code blocks.
    """.trimIndent()

    suspend fun testConnection(apiKey: String, model: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Pair(false, "API key is not configured.")
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "System diagnostic check. Respond with 'ONLINE'.") })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Pair(true, "Core neural uplink active. Model '$model' responding normally.")
            } else {
                val errBody = response.body?.string() ?: response.message
                Pair(false, "Gemini rejected transmission (HTTP ${response.code}): $errBody")
            }
        } catch (e: Exception) {
            Pair(false, "Connection failure: ${e.localizedMessage}")
        }
    }

    suspend fun generateAssistantResponse(
        apiKey: String,
        model: String,
        userPrompt: String,
        history: List<ChatMessage>
    ): Pair<String, DeviceAction?> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Pair(
                "Gemini API key is not configured, Sir. Please enter your API key in Settings to activate full neural conversation.",
                null
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // Include last 6 turns for context
            val recentHistory = history.takeLast(6)
            for (msg in recentHistory) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (msg.isUser) "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    })
                })
            }

            // Current prompt
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", userPrompt) })
                })
            })

            val payload = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstructionText) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = response.body?.string() ?: response.message
                return@withContext Pair("Apologies Sir, the Gemini server returned an error: $err", null)
            }

            val respBody = response.body?.string() ?: ""
            val jsonResp = JSONObject(respBody)
            val candidates = jsonResp.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseJarvisStructuredOutput(rawText)
        } catch (e: Exception) {
            Pair("Neural synthesis interrupted, Sir: ${e.localizedMessage}", null)
        }
    }

    private fun parseJarvisStructuredOutput(rawText: String): Pair<String, DeviceAction?> {
        val clean = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        return try {
            val json = JSONObject(clean)
            val reply = json.optString("reply", "At your service, Sir.")
            val actionObj = json.optJSONObject("action")
            val action = if (actionObj != null) {
                when (actionObj.optString("type")) {
                    "LAUNCH_APP" -> {
                        val appName = actionObj.optString("appName")
                        if (appName.isNotBlank()) DeviceAction.LaunchApp(appName, null) else null
                    }
                    "DIAL_CALL" -> {
                        val contact = actionObj.optString("contact")
                        val number = actionObj.optString("phoneNumber")
                        DeviceAction.PrepareCall(contact.ifBlank { null }, number)
                    }
                    "SEND_SMS" -> {
                        val recipient = actionObj.optString("contact").ifBlank { actionObj.optString("recipient") }
                        val body = actionObj.optString("messageBody")
                        DeviceAction.PrepareSms(recipient, body)
                    }
                    "TOGGLE_TORCH" -> {
                        val enable = actionObj.optBoolean("enable", true)
                        DeviceAction.ToggleTorch(enable)
                    }
                    "ADJUST_VOLUME" -> {
                        val dir = actionObj.optInt("direction", 1)
                        DeviceAction.AdjustVolume(dir)
                    }
                    "OPEN_SETTINGS" -> {
                        val setting = actionObj.optString("setting", "general")
                        DeviceAction.OpenSettings(setting)
                    }
                    "SCROLL" -> {
                        val dir = actionObj.optString("direction", "down")
                        DeviceAction.Scroll(dir)
                    }
                    "MEDIA_CONTROL" -> {
                        val cmd = actionObj.optString("mediaCommand", "play")
                        DeviceAction.MediaControl(cmd)
                    }
                    else -> null
                }
            } else null

            Pair(reply, action)
        } catch (_: Exception) {
            // In case model returned plain text despite json instructions
            Pair(clean.ifBlank { "Acknowledged, Sir." }, null)
        }
    }
}
