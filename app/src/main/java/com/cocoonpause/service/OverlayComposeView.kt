package com.cocoonpause.service

import android.content.Context
import android.view.InputDevice
import android.view.MotionEvent
import androidx.compose.ui.platform.ComposeView
import kotlin.math.abs

class OverlayComposeView(
    context: Context,
    private val manager: PauseOverlayManager,
) : ComposeView(context) {

    // Track last discrete direction so we fire once per tilt, not every frame
    private var lastNavX = 0
    private var lastNavY = 0

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        val src = event.source
        val isJoystick = (src and InputDevice.SOURCE_JOYSTICK != 0)
                      || (src and InputDevice.SOURCE_GAMEPAD != 0)

        if (isJoystick && event.action == MotionEvent.ACTION_MOVE) {
            // Check both D-pad hat axes and left analog stick;
            // whichever has the bigger deflection wins
            val hatX   = event.getAxisValue(MotionEvent.AXIS_HAT_X)
            val hatY   = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
            val stickX = event.getAxisValue(MotionEvent.AXIS_X)
            val stickY = event.getAxisValue(MotionEvent.AXIS_Y)

            val x = if (abs(hatX) >= abs(stickX)) hatX else stickX
            val y = if (abs(hatY) >= abs(stickY)) hatY else stickY

            val nx = if (x < -0.5f) -1 else if (x > 0.5f) 1 else 0
            val ny = if (y < -0.5f) -1 else if (y > 0.5f) 1 else 0

            if (nx != lastNavX) {
                lastNavX = nx
                when {
                    nx < 0 -> manager.navigatePrev()
                    nx > 0 -> manager.navigateNext()
                }
            }
            if (ny != lastNavY) {
                lastNavY = ny
                when {
                    ny < 0 -> manager.navigateUp()
                    ny > 0 -> manager.navigateDown()
                }
            }
            return true
        }
        return super.dispatchGenericMotionEvent(event)
    }
}
