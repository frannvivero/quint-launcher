package com.quint.launcher

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/** Inter (variable, licencia OFL). Los pesos salen del mismo archivo. */
val AppFont = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal),
    Font(R.font.inter_variable, FontWeight.Medium),
    Font(R.font.inter_variable, FontWeight.SemiBold),
    Font(R.font.inter_variable, FontWeight.Bold),
)

/** Colores según el modo (día / noche). Se pasan con LocalPalette. */
data class Palette(
    val bg: Color,
    val ink: Color,
    val muted: Color,
    val buttonBg: Color,
    val focusFill: Color,
    val dockBg: Color,
    val dockBorder: Color,
    val shadow: Color,
    val tileTop: Color,
    val tileBottom: Color,
)

val LightPalette = Palette(
    bg = Color.White,
    ink = Color(0xFF1C1C1E),
    muted = Color(0xFF6E6E73),
    buttonBg = Color(0xFFE5E5EA),
    focusFill = Color.White,
    // Vidrio claro: blanco translúcido con borde suave.
    dockBg = Color(0xD9FFFFFF),
    dockBorder = Color(0x1F000000),
    shadow = Color(0x22000000),
    tileTop = Color(0xFF3A3A3C),
    tileBottom = Color(0xFF1C1C1E),
)

val DarkPalette = Palette(
    bg = Color.Black,
    ink = Color.White,
    muted = Color(0xFF98989F),
    buttonBg = Color(0xFF2C2C2E),
    focusFill = Color(0xFF48484A),
    // Vidrio oscuro: gris translúcido con borde de luz.
    dockBg = Color(0x991C1C1E),
    dockBorder = Color(0x33FFFFFF),
    shadow = Color(0x66000000),
    tileTop = Color(0xFF2C2C2E),
    tileBottom = Color(0xFF111111),
)

val LocalPalette = staticCompositionLocalOf { LightPalette }
