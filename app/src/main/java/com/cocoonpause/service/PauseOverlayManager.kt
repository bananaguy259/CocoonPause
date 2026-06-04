package com.cocoonpause.service

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.cocoonpause.tools.ShellExecutor
import com.cocoonpause.ui.PauseMenuContent
import com.cocoonpause.ui.theme.CocoonPauseTheme

class PauseOverlayManager(
    private val service: PauseOverlayService,
    private val executor: ShellExecutor,
) {
    private val windowManager =
        service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    var isShowing = false
        private set

    var currentForegroundPackage: String = ""

    fun show() {
        if (isShowing) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val owner = OverlayLifecycleOwner().also {
            it.start()
            lifecycleOwner = it
        }

        val view = ComposeView(service).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                CocoonPauseTheme {
                    PauseMenuContent(
                        executor = executor,
                        foregroundPackage = currentForegroundPackage,
                        onDismiss = { hide() },
                        onExitGame = { service.exitGame() },
                        onScreenshot = { service.captureScreenshot() },
                    )
                }
            }
        }

        windowManager.addView(view, params)
        overlayView = view
        isShowing = true
    }

    fun hide() {
        val view = overlayView ?: return
        runCatching { windowManager.removeView(view) }
        lifecycleOwner?.stop()
        lifecycleOwner = null
        overlayView = null
        isShowing = false
    }
}
