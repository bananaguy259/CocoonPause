package com.cocoonpause.models

import com.cocoonpause.tools.ShellExecutor

sealed class FanMode(
    val id: String,
    val displayName: String,
    val settingsValue: Int,
) {
    data object Off : FanMode("fanOff", "Off", 0)
    data object Quiet : FanMode("quiet", "Quiet", 1)
    data object Smart : FanMode("smart", "Smart", 4)
    data object Sport : FanMode("sport", "Sport", 5)
    data object Unknown : FanMode("unknown", "Unknown", -1)

    fun enable(executor: ShellExecutor) {
        if (this != Unknown) executor.setIntSystemSetting("fan_mode", settingsValue)
    }

    companion object {
        val cycle = listOf(Off, Quiet, Smart, Sport)

        fun getMode(executor: ShellExecutor): FanMode =
            when (executor.getIntSystemSetting("fan_mode", Quiet.settingsValue)) {
                Off.settingsValue -> Off
                Quiet.settingsValue -> Quiet
                Smart.settingsValue -> Smart
                Sport.settingsValue -> Sport
                else -> Unknown
            }

        fun next(current: FanMode): FanMode {
            val idx = cycle.indexOf(current)
            return cycle[(idx + 1) % cycle.size]
        }

        fun prev(current: FanMode): FanMode {
            val idx = cycle.indexOf(current)
            return cycle[(idx - 1 + cycle.size) % cycle.size]
        }
    }
}
