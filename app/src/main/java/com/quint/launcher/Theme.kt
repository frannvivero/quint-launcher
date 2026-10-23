package com.quint.launcher

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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
    dockBg = Color.White,
    dockBorder = Color(0x14000000),
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
    dockBg = Color(0xFF1C1C1E),
    dockBorder = Color(0x22FFFFFF),
    shadow = Color(0x66000000),
    tileTop = Color(0xFF2C2C2E),
    tileBottom = Color(0xFF111111),
)

val LocalPalette = staticCompositionLocalOf { LightPalette }
