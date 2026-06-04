package com.cocoonpause.models

import com.cocoonpause.tools.ShellExecutor

sealed class PerfMode(
    val id: String,
    val displayName: String,
    val settingsValue: Int,
) {
    data object Standard : PerfMode("standard", "Standard", 0)
    data object Performance : PerfMode("performance", "Performance", 1)
    data object HighPerformance : PerfMode("highPerformance", "High Perf", 2)
    data object Unknown : PerfMode("unknown", "Unknown", -1)

    fun enable(executor: ShellExecutor) {
        if (this != Unknown) executor.setIntSystemSetting("performance_mode", settingsValue)
    }

    companion object {
        val cycle = listOf(Standard, Performance, HighPerformance)

        fun getMode(executor: ShellExecutor): PerfMode =
            when (executor.getIntSystemSetting("performance_mode", Standard.settingsValue)) {
                Standard.settingsValue -> Standard
                Performance.settingsValue -> Performance
                HighPerformance.settingsValue -> HighPerformance
                else -> Unknown
            }

        fun next(current: PerfMode): PerfMode {
            val idx = cycle.indexOf(current)
            return cycle[(idx + 1) % cycle.size]
        }

        fun prev(current: PerfMode): PerfMode {
            val idx = cycle.indexOf(current)
            return cycle[(idx - 1 + cycle.size) % cycle.size]
        }
    }
}
