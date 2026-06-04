package com.cocoonpause

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.cocoonpause.service.PauseOverlayService
import com.cocoonpause.tools.PrefsManager
import com.cocoonpause.ui.SettingsScreen
import com.cocoonpause.ui.theme.CocoonPauseTheme

class MainActivity : ComponentActivity() {

    // State for the button-capture flow, hoisted here so dispatchKeyEvent can feed it
    private var isCapturing by mutableStateOf(false)
    private var capturedKeys by mutableStateOf(emptySet<Int>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CocoonPauseTheme {
                SettingsScreen(
                    capturedKeys = capturedKeys,
                    isCapturing  = isCapturing,
                    onStartCapture = {
                        capturedKeys = emptySet()
                        isCapturing  = true
                    },
                    onClearBinding = {
                        // If capturing → cancel; if not → clear saved binding
                        if (isCapturing) {
                            capturedKeys = emptySet()
                            isCapturing  = false
                        } else {
                            PrefsManager.clearTriggerKeycodes(this)
                            PauseOverlayService.instance?.reloadTrigger()
                        }
                    },
                    onSaveBinding = {
                        if (capturedKeys.isNotEmpty()) {
                            PrefsManager.setTriggerKeycodes(this, capturedKeys)
                            PauseOverlayService.instance?.reloadTrigger()
                        }
                        isCapturing  = false
                        capturedKeys = emptySet()
                    },
                )
            }
        }
    }

    /**
     * Intercepts ALL key events so the user can press any controller button
     * (A, B, M1, M2, Start, Select, etc.) during the capture flow.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!isCapturing) return super.dispatchKeyEvent(event)

        // Ignore repeats and volume/power so we don't capture unintended keys
        if (event.repeatCount > 0) return true
        if (event.keyCode in listOf(
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_POWER,
            )
        ) return super.dispatchKeyEvent(event)

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                capturedKeys = capturedKeys + event.keyCode
                return true
            }
            KeyEvent.ACTION_UP -> {
                // We only ADD on DOWN so the set stays stable while held
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
