package com.cocoonpause

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cocoonpause.service.PauseOverlayService
import com.cocoonpause.ui.PauseMenuContent
import com.cocoonpause.ui.theme.CocoonPauseTheme

class PauseMenuActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        val svc = PauseOverlayService.instance
        if (svc == null) { finish(); return }
        val pkg = intent.getStringExtra(EXTRA_PKG) ?: ""
        setContent {
            CocoonPauseTheme {
                PauseMenuContent(
                    executor = svc.getExecutor(),
                    foregroundPackage = pkg,
                    onDismiss = ::finish,
                    onExitGame = { finish(); svc.exitGame() },
                    onScreenshot = { finish(); svc.captureScreenshot() },
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            finish()
        }
        return true
    }

    companion object {
        var instance: PauseMenuActivity? = null
            private set
        const val EXTRA_PKG = "extra_pkg"
    }
}
