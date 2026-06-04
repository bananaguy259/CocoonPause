package com.cocoonpause.service

import android.accessibilityservice.AccessibilityService
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
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

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onServiceConnected() {
        executor = ShellExecutor()
        overlayManager = PauseOverlayManager(this)
        reloadTrigger()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        runCatching { overlayManager.hide() }
        instance = null
    }

    override fun onInterrupt() { /* required */ }

    // ── Foreground app tracking ────────────────────────────────────────────

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg in IGNORED_PACKAGES) return
        currentForegroundPackage = pkg
        overlayManager.currentForegroundPackage = pkg
    }

    // ── Key event interception ─────────────────────────────────────────────

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (triggerKeycodes.isEmpty()) return false

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount > 0) return overlayManager.isShowing

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

        // Consume ALL keys while the overlay is visible so nothing leaks to the game
        return overlayManager.isShowing
    }

    // ── Actions called from the overlay UI ────────────────────────────────

    fun exitGame() {
        val pkg = currentForegroundPackage
        overlayManager.hide()
        scope.launch {
            if (pkg.isNotBlank()) {
                executor.executeAsRoot("am force-stop $pkg")
            }
            withContext(Dispatchers.Main) {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }

    fun captureScreenshot() {
        overlayManager.hide()
        // Wait a couple frames for the overlay to fully disappear before snapping
        Handler(Looper.getMainLooper()).postDelayed({
            takeScreenshot(
                android.view.Display.DEFAULT_DISPLAY,
                mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshot: ScreenshotResult) {
                        val bitmap = Bitmap.wrapHardwareBuffer(
                            screenshot.hardwareBuffer,
                            screenshot.colorSpace,
                        )
                        screenshot.hardwareBuffer.close()
                        scope.launch {
                            val saved = saveBitmapToGallery(bitmap)
                            bitmap?.recycle()
                            withContext(Dispatchers.Main) {
                                toast(if (saved) "Screenshot saved!" else "Screenshot failed")
                            }
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        // Fallback to shell screencap
                        scope.launch {
                            val ts = System.currentTimeMillis()
                            executor.executeAsRoot(
                                "screencap -p /storage/emulated/0/Pictures/Screenshots/CocoonPause_$ts.png",
                            )
                            withContext(Dispatchers.Main) { toast("Screenshot saved!") }
                        }
                    }
                },
            )
        }, 350)
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Re-reads the trigger binding from SharedPreferences (called from settings screen on save). */
    fun getExecutor(): ShellExecutor = executor

    fun reloadTrigger() {
        triggerKeycodes = PrefsManager.getTriggerKeycodes(this)
    }

    private fun saveBitmapToGallery(bitmap: Bitmap?): Boolean {
        bitmap ?: return false
        return try {
            val cv = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "CocoonPause_${System.currentTimeMillis()}.png",
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/Screenshots",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                cv,
            ) ?: return false
            contentResolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            cv.clear()
            cv.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, cv, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // ── Companion ─────────────────────────────────────────────────────────

    companion object {
        /** Nullable live reference to the running service (null if not enabled). */
        var instance: PauseOverlayService? = null
            private set

        private val IGNORED_PACKAGES = setOf(
            "com.android.systemui",
            "android",
            "com.android.launcher3",
        )
    }
}
