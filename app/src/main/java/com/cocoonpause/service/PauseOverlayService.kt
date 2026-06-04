package com.cocoonpause.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
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
        runCatching { overlayManager.hide() }
        instance = null
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg in IGNORED_PACKAGES) return
        currentForegroundPackage = pkg
        overlayManager.currentForegroundPackage = pkg
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (triggerKeycodes.isEmpty()) return false

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount > 0) return false
                pressedKeys.add(event.keyCode)
                if (pressedKeys.containsAll(triggerKeycodes)) {
                    if (overlayManager.isShowing) {
                        Handler(Looper.getMainLooper()).post { overlayManager.hide() }
                    } else {
                        Handler(Looper.getMainLooper()).post { overlayManager.show() }
                    }
                    return true
                }
            }
            KeyEvent.ACTION_UP -> {
                pressedKeys.remove(event.keyCode)
            }
        }
        // Pass everything else through — the overlay window handles its own input
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

    fun captureScreenshot() {
        overlayManager.hide()
        // Wait for the overlay window to fully clear the compositor before snapping
        Handler(Looper.getMainLooper()).postDelayed({
            scope.launch {
                val ts = System.currentTimeMillis()
                val dir = "/storage/emulated/0/Pictures/Screenshots"
                val path = "$dir/CocoonPause_$ts.png"
                executor.executeAsRoot("mkdir -p $dir")
                val result = executor.executeAsRoot("screencap -p $path")
                // Tell the media scanner about the new file so it shows in gallery
                executor.executeAsRoot(
                    "am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file://$path"
                )
                withContext(Dispatchers.Main) {
                    val msg = if (result.isSuccess) "Screenshot saved!" else "Screenshot failed"
                    Toast.makeText(this@PauseOverlayService, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }, 600)
    }

    fun getExecutor(): ShellExecutor = executor

    fun reloadTrigger() {
        triggerKeycodes = PrefsManager.getTriggerKeycodes(this)
    }

    companion object {
        var instance: PauseOverlayService? = null
            private set

        private val IGNORED_PACKAGES = setOf(
            "com.android.systemui", "android", "com.android.launcher3"
        )
    }
}
