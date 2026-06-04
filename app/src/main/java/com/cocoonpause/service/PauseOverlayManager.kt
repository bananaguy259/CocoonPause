package com.cocoonpause.service

import android.content.Intent
import com.cocoonpause.PauseMenuActivity

class PauseOverlayManager(private val service: PauseOverlayService) {

    var isShowing = false
        private set

    var currentForegroundPackage: String = ""

    fun show() {
        if (isShowing) return
        val intent = Intent(service, PauseMenuActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            putExtra(PauseMenuActivity.EXTRA_PKG, currentForegroundPackage)
        }
        service.startActivity(intent)
        isShowing = true
    }

    fun hide() {
        PauseMenuActivity.instance?.finish()
        isShowing = false
    }

    fun onActivityDismissed() {
        isShowing = false
    }
}
