package com.quint.launcher

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Weather(val temp: Int, val description: String)

/** Clima actual de Federal, Entre Ríos, desde Open-Meteo (sin clave de API). */
object WeatherRepository {
    private const val CURRENT_URL =
        "https://api.open-meteo.com/v1/forecast" +
            "?latitude=-30.950&longitude=-58.800" +
            "&current=temperature_2m,weather_code" +
            "&timezone=America%2FArgentina%2FCordoba"

    /** Devuelve null si no hay conexión. Llamar fuera del hilo principal. */
    fun fetch(): Weather? = runCatching {
        val conn = URL(CURRENT_URL).openConnection() as HttpURLConnection
        conn.connectTimeout = 8_000
        conn.readTimeout = 8_000
        try {
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")
            Weather(
                temp = Math.round(current.getDouble("temperature_2m")).toInt(),
                description = describe(current.getInt("weather_code")),
            )
        } finally {
            conn.disconnect()
        }
    }.getOrNull()

    /** Códigos WMO que usa Open-Meteo. */
    private fun describe(code: Int): String = when (code) {
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
}
