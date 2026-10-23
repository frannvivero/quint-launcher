package com.quint.launcher

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Weather(
    val temp: Int,
    val max: Int?,
    val min: Int?,
    val code: Int,
    val isDay: Boolean,
) {
    val description: String get() = WeatherRepository.describe(code)
    val iconRes: Int get() = WeatherRepository.iconFor(code, isDay)
}

/**
 * Clima actual de Federal, Entre Ríos, desde Open-Meteo (sin clave de API).
 * La idea de pedir is_day y el pronóstico diario está inspirada en Free My TV (MIT).
 */
object WeatherRepository {
    private const val URL_STR =
        "https://api.open-meteo.com/v1/forecast" +
            "?latitude=-30.950&longitude=-58.800" +
            "&current=temperature_2m,weather_code,is_day" +
            "&daily=temperature_2m_max,temperature_2m_min" +
            "&timezone=America%2FArgentina%2FCordoba&forecast_days=1"

    /** Devuelve null si no hay conexión. Llamar fuera del hilo principal. */
    fun fetch(): Weather? = runCatching {
        val conn = URL(URL_STR).openConnection() as HttpURLConnection
        conn.connectTimeout = 8_000
        conn.readTimeout = 8_000
        try {
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val current = json.getJSONObject("current")
            val daily = json.getJSONObject("daily")
            Weather(
                temp = Math.round(current.getDouble("temperature_2m")).toInt(),
                max = daily.getJSONArray("temperature_2m_max").optDouble(0, Double.NaN)
                    .takeIf { !it.isNaN() }?.let { Math.round(it).toInt() },
                min = daily.getJSONArray("temperature_2m_min").optDouble(0, Double.NaN)
                    .takeIf { !it.isNaN() }?.let { Math.round(it).toInt() },
                code = current.getInt("weather_code"),
                isDay = current.optInt("is_day", 1) == 1,
            )
        } finally {
            conn.disconnect()
        }
    }.getOrNull()

    /** Códigos WMO que usa Open-Meteo. */
    fun describe(code: Int): String = when (code) {
        0 -> "Despejado"
        1 -> "Mayormente despejado"
        2 -> "Parcialmente nublado"
        3 -> "Nublado"
        45, 48 -> "Niebla"
        in 51..57 -> "Llovizna"
        in 61..67, in 80..82 -> "Lluvia"
        in 71..77, 85, 86 -> "Nieve"
        in 95..99 -> "Tormenta"
        else -> ""
    }

    fun iconFor(code: Int, isDay: Boolean): Int = when (code) {
        0 -> if (isDay) R.drawable.wx_sunny else R.drawable.wx_clear_night
        1, 2 -> if (isDay) R.drawable.wx_partly_cloudy_day else R.drawable.wx_partly_cloudy_night
        3 -> R.drawable.wx_cloud
        45, 48 -> R.drawable.wx_foggy
        in 51..67, in 80..82 -> R.drawable.wx_rainy
        in 71..77, 85, 86 -> R.drawable.wx_snowflake
        in 95..99 -> R.drawable.wx_thunderstorm
        else -> R.drawable.wx_cloud
    }
}
