package com.cocoonpause.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.cocoonpause.tools.PrefsManager
import com.cocoonpause.tools.ShellExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PauseOverlayService : AccessibilityService() {

    private lateinit var executor: ShellExecutor
    lateinit var overlayManager: PauseOverlayManager
        private set

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val pressedKeys = mutableSetOf<Int>()
    private var triggerKeycodes = emptySet<Int>()
    private var currentForegroundPackage = ""

    override fun onServiceConnected() {
        executor = ShellExecutor()
        overlayManager = PauseOverlayManager(this, executor)
        reloadTrigger()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        runCatching { overlayManager.destroy() }
        instance = null
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg in IGNORED_PACKAGES) return
        currentForegroundPackage = pkg
        overlayManager.currentForegroundPackage = pkg
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (overlayManager.isShowing) {
            // Consume all key-ups and repeats silently
            if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount > 0) return true

            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP     -> overlayManager.navigateUp()
                KeyEvent.KEYCODE_DPAD_DOWN   -> overlayManager.navigateDown()
                KeyEvent.KEYCODE_DPAD_LEFT   -> overlayManager.navigatePrev()
                KeyEvent.KEYCODE_DPAD_RIGHT  -> overlayManager.navigateNext()
                KeyEvent.KEYCODE_BUTTON_A,
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER       -> overlayManager.activate()
                // B or Back always closes the overlay
                KeyEvent.KEYCODE_BUTTON_B,
                KeyEvent.KEYCODE_BACK        -> Handler(Looper.getMainLooper()).post { overlayManager.hide() }
                else -> {
                    // Allow the configured trigger combo to close it too
                    pressedKeys.add(event.keyCode)
                    if (triggerKeycodes.isNotEmpty() && pressedKeys.containsAll(triggerKeycodes))
                        Handler(Looper.getMainLooper()).post { overlayManager.hide() }
                }
            }
            return true // consume EVERYTHING while overlay is up
        }

        // Overlay not showing — watch for trigger combo to open it
        if (triggerKeycodes.isEmpty()) return false
        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount > 0) return false
                pressedKeys.add(event.keyCode)
                if (pressedKeys.containsAll(triggerKeycodes)) {
                    Handler(Looper.getMainLooper()).post { overlayManager.show() }
                    return true
                }
            }
            KeyEvent.ACTION_UP -> pressedKeys.remove(event.keyCode)
        }
        return false
    }

    fun exitGame() {
        val pkg = currentForegroundPackage
        overlayManager.hide()
        scope.launch {
            if (pkg.isNotBlank()) executor.executeAsRoot("am force-stop $pkg")
            withContext(Dispatchers.Main) { performGlobalAction(GLOBAL_ACTION_HOME) }
        }
    }

    fun getExecutor(): ShellExecutor = executor

    fun reloadTrigger() {
        triggerKeycodes = PrefsManager.getTriggerKeycodes(this)
    }

    companion object {
        var instance: PauseOverlayService? = null
            private set
        private val IGNORED_PACKAGES = setOf("com.android.systemui", "android", "com.android.launcher3")
    }
}
