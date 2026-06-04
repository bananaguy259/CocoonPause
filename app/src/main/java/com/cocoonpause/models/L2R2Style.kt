package com.cocoonpause.models

import com.cocoonpause.tools.ShellExecutor

sealed class L2R2Style(
    val id: String,
    val displayName: String,
    val settingsValue: Int,
) {
    data object Analog : L2R2Style("analog", "Analog", 0)
    data object Digital : L2R2Style("digital", "Digital", 1)
    data object Both : L2R2Style("both", "Both", 2)
    data object Unknown : L2R2Style("unknown", "Unknown", -1)

    fun enable(executor: ShellExecutor) {
        if (this != Unknown) executor.setIntSystemSetting("trigger_input_mode", settingsValue)
    }

    companion object {
        val cycle = listOf(Analog, Digital, Both)

        fun getStyle(executor: ShellExecutor): L2R2Style =
            when (executor.getIntSystemSetting("trigger_input_mode", Analog.settingsValue)) {
                Analog.settingsValue -> Analog
                Digital.settingsValue -> Digital
                Both.settingsValue -> Both
                else -> Unknown
            }

        fun next(current: L2R2Style): L2R2Style {
            val idx = cycle.indexOf(current)
            return cycle[(idx + 1) % cycle.size]
        }

        fun prev(current: L2R2Style): L2R2Style {
            val idx = cycle.indexOf(current)
            return cycle[(idx - 1 + cycle.size) % cycle.size]
        }
    }
}
