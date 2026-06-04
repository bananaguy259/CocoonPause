package com.cocoonpause.models

import com.cocoonpause.tools.ShellExecutor

sealed class ControllerStyle(
    val id: String,
    val displayName: String,
    private val tempAbxyLayout: Int,
    private val noCreateGamepadLayout: Int,
    private val flipButtonLayout: Int,
) {
    data object Xbox : ControllerStyle("xbox", "Xbox", 0, 0, 1)
    data object Odin : ControllerStyle("odin", "Odin", 1, 0, 0)
    data object Disconnect : ControllerStyle("disconnect", "Disconnect", 2, 1, 0)
    data object Unknown : ControllerStyle("unknown", "Unknown", -1, -1, -1)

    fun enable(executor: ShellExecutor) {
        if (this != Unknown) {
            executor.setIntSystemSetting("temp_abxy_layout_mode", tempAbxyLayout)
            executor.setIntSystemSetting("no_create_gamepad_button_layout", noCreateGamepadLayout)
            executor.setIntSystemSetting("flip_button_layout", flipButtonLayout)
        }
    }

    companion object {
        val cycle = listOf(Xbox, Odin, Disconnect)

        fun getStyle(executor: ShellExecutor): ControllerStyle =
            when {
                executor.getIntSystemSetting("no_create_gamepad_button_layout", 0) == 1 -> Disconnect
                executor.getIntSystemSetting("flip_button_layout", 0) == 1 -> Xbox
                else -> Odin
            }

        fun next(current: ControllerStyle): ControllerStyle {
            val idx = cycle.indexOf(current)
            return cycle[(idx + 1) % cycle.size]
        }

        fun prev(current: ControllerStyle): ControllerStyle {
            val idx = cycle.indexOf(current)
            return cycle[(idx - 1 + cycle.size) % cycle.size]
        }
    }
}
