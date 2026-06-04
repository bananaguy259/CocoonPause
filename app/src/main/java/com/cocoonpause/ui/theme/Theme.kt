package com.cocoonpause.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Cocoon colour palette ─────────────────────────────────────────────────
val CocoonBlack      = Color(0xFF000000)
val CocoonSurface    = Color(0xFF1C1C1C)   // card / panel background
val CocoonSurface2   = Color(0xFF252525)   // slightly lighter panels / pressed states
val CocoonDivider    = Color(0xFF333333)
val CocoonTextPrimary   = Color(0xFFFFFFFF)
val CocoonTextSecondary = Color(0xFF9E9E9E)
val CocoonAccent     = Color(0xFFFFFFFF)   // arrows, active icons
val CocoonDanger     = Color(0xFFFF5555)   // Exit Game row

private val ColorScheme = darkColorScheme(
    background    = CocoonBlack,
    surface       = CocoonSurface,
    onBackground  = CocoonTextPrimary,
    onSurface     = CocoonTextPrimary,
    primary       = CocoonAccent,
    onPrimary     = CocoonBlack,
    error         = CocoonDanger,
)

@Composable
fun CocoonPauseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content,
    )
}
