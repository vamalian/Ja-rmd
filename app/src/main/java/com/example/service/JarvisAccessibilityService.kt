package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Passive observation when authorized; strictly no covert surveillance or unauthorized recording
    }

    override fun onInterrupt() {
        // Clean release
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        var instance: JarvisAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null

        fun scrollCurrentWindow(forward: Boolean): Boolean {
            val service = instance ?: return false
            val root = service.rootInActiveWindow ?: return false
            val action = if (forward) {
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            } else {
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            }
            return findAndPerformScroll(root, action)
        }

        private fun findAndPerformScroll(node: AccessibilityNodeInfo, action: Int): Boolean {
            if (node.actionList.any { it.id == action }) {
                return node.performAction(action)
            }
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                if (findAndPerformScroll(child, action)) {
                    return true
                }
            }
            return false
        }
    }
}
