package com.quint.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Pronóstico de las próximas horas, en una fila debajo de la barra. */
@Composable
fun HourlyStrip(weather: Weather?) {
    val p = LocalPalette.current
    if (weather == null || weather.hourly.isEmpty()) return

    Row(
        modifier = Modifier.padding(start = 8.dp, top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        weather.hourly.forEach { h ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${h.time}h", fontSize = 15.sp, color = p.muted)
                Image(
                    painter = painterResource(WeatherRepository.iconFor(h.code, h.isDay)),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(p.ink),
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .size(30.dp),
                )
                Text(text = "${h.temp}°", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = p.ink)
            }
        }
    }
}
