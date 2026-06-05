package com.cocoonpause.service

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.cocoonpause.models.ControllerStyle
import com.cocoonpause.models.FanMode
import com.cocoonpause.models.L2R2Style
import com.cocoonpause.models.PerfMode
import com.cocoonpause.tools.ShellExecutor
import com.cocoonpause.ui.PauseMenuContent
import com.cocoonpause.ui.theme.CocoonPauseTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PauseOverlayManager(
    private val service: PauseOverlayService,
    private val executor: ShellExecutor,
) {
    private val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    var isShowing = false
        private set
    var currentForegroundPackage = ""

    // D-pad selection (0=controller,1=l2r2,2=perf,3=fan,4=exit)
    val itemCount = 5
    var selectedIndex by mutableStateOf(0)
        private set

    // Mode states — Compose observes these directly
    var controllerStyle by mutableStateOf<ControllerStyle>(ControllerStyle.Unknown)
        private set
    var l2r2Style by mutableStateOf<L2R2Style>(L2R2Style.Unknown)
        private set
    var perfMode by mutableStateOf<PerfMode>(PerfMode.Unknown)
        private set
    var fanMode by mutableStateOf<FanMode>(FanMode.Unknown)
        private set
    var modesLoaded by mutableStateOf(false)
        private set

    fun show() {
        if (isShowing) return
        selectedIndex = 0
        modesLoaded = false
        scope.launch {
            controllerStyle = ControllerStyle.getStyle(executor)
            l2r2Style       = L2R2Style.getStyle(executor)
            perfMode        = PerfMode.getMode(executor)
            fanMode         = FanMode.getMode(executor)
            modesLoaded     = true
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.TOP or Gravity.START }

        val owner = OverlayLifecycleOwner().also { it.start(); lifecycleOwner = it }

        val view = ComposeView(service).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                CocoonPauseTheme {
                    PauseMenuContent(
                        overlayManager = this@PauseOverlayManager,
                        onDismiss  = { hide() },
                        onExitGame = { service.exitGame() },
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

    fun destroy() { runCatching { hide() } }

    // ── Navigation (called from onKeyEvent on the main thread) ────────────
    fun navigateUp()   { selectedIndex = (selectedIndex - 1 + itemCount) % itemCount }
    fun navigateDown() { selectedIndex = (selectedIndex + 1) % itemCount }

    fun navigatePrev() { scope.launch { applyChange(selectedIndex, prev = true) } }
    fun navigateNext() { scope.launch { applyChange(selectedIndex, prev = false) } }

    fun activate() { if (selectedIndex == 4) service.exitGame() }

    // ── Touch arrow buttons call these directly with the row index ─────────
    fun prevForRow(row: Int) { scope.launch { applyChange(row, prev = true) } }
    fun nextForRow(row: Int) { scope.launch { applyChange(row, prev = false) } }

    private fun applyChange(row: Int, prev: Boolean) {
        when (row) {
            0 -> { val n = if (prev) ControllerStyle.prev(controllerStyle) else ControllerStyle.next(controllerStyle); n.enable(executor); controllerStyle = n }
            1 -> { val n = if (prev) L2R2Style.prev(l2r2Style)             else L2R2Style.next(l2r2Style);             n.enable(executor); l2r2Style = n }
            2 -> { val n = if (prev) PerfMode.prev(perfMode)               else PerfMode.next(perfMode);               n.enable(executor); perfMode = n }
            3 -> { val n = if (prev) FanMode.prev(fanMode)                 else FanMode.next(fanMode);                 n.enable(executor); fanMode = n }
        }
    }
}
