package com.cocoonpause.tools

import android.content.Context
import android.view.KeyEvent

object PrefsManager {
    private const val PREFS_NAME = "cocoon_pause_prefs"
    private const val KEY_TRIGGER_KEYCODES = "trigger_keycodes"

    fun getTriggerKeycodes(context: Context): Set<Int> {
        val str = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TRIGGER_KEYCODES, "") ?: ""
        if (str.isBlank()) return emptySet()
        return str.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    fun setTriggerKeycodes(context: Context, keycodes: Set<Int>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TRIGGER_KEYCODES, keycodes.joinToString(","))
            .apply()
    }

    fun clearTriggerKeycodes(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_TRIGGER_KEYCODES)
            .apply()
    }

    /** Returns a human-readable label for a set of keycodes, e.g. "M1 + Start" */
    fun formatKeycodes(keycodes: Set<Int>): String {
        if (keycodes.isEmpty()) return "Not configured"
        return keycodes.sorted().joinToString(" + ") { keyCodeToName(it) }
    }

    fun keyCodeToName(keyCode: Int): String {
        return KeyEvent.keyCodeToString(keyCode)
            .removePrefix("KEYCODE_")
            .replace("_", " ")
            .lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercaseChar() }
            }
    }
}
