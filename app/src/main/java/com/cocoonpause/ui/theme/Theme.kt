package com.cocoonpause.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.cocoonpause.R

val MaterialSymbolsRounded = FontFamily(Font(R.font.material_symbols_rounded))

val CocoonBlack         = Color(0xFF000000)
val CocoonSurface       = Color(0xFF1D1D1D)
val CocoonSurface2      = Color(0xFF282828)
val CocoonDivider       = Color(0xFF333333)
val CocoonTextPrimary   = Color(0xFFFFFFFF)
val CocoonTextSecondary = Color(0xFF9E9E9E)
val CocoonAccent        = Color(0xFFFFFFFF)
val CocoonDanger        = Color(0xFFFF5555)

private val ColorScheme = darkColorScheme(
    background   = CocoonBlack,
    surface      = CocoonSurface,
    onBackground = CocoonTextPrimary,
    onSurface    = CocoonTextPrimary,
    primary      = CocoonAccent,
    onPrimary    = CocoonBlack,
    error        = CocoonDanger,
)

@Composable
fun CocoonPauseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, content = content)
}
