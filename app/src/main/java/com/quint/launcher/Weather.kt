package com.quint.launcher

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class City(val name: String, val lat: Double, val lon: Double)

/** Ciudades que se pueden elegir desde el launcher. */
val Cities = listOf(
    City("Federal", -30.950, -58.800),
    City("Concordia", -31.393, -58.021),
    City("Concepción del Uruguay", -32.483, -58.237),
    City("Gualeguaychú", -33.010, -58.517),
    City("Paraná", -31.733, -60.524),
    City("Rosario", -32.944, -60.650),
    City("Córdoba", -31.420, -64.189),
    City("Buenos Aires", -34.604, -58.382),
    City("Mendoza", -32.889, -68.846),
)

data class HourForecast(val time: String, val temp: Int, val code: Int, val isDay: Boolean)

data class Weather(
    val temp: Int,
    val max: Int?,
    val min: Int?,
    val code: Int,
    val isDay: Boolean,
    val hourly: List<HourForecast>,
) {
    val description: String get() = WeatherRepository.describe(code)
    val iconRes: Int get() = WeatherRepository.iconFor(code, isDay)
}

/**
 * Clima desde Open-Meteo (sin clave de API).
 * La idea de pedir is_day, el pronóstico diario y por hora está inspirada en Free My TV (MIT).
 */
object WeatherRepository {
    private const val TZ = "America/Argentina/Cordoba"

    /** Devuelve null si no hay conexión. Llamar fuera del hilo principal. */
    fun fetch(city: City): Weather? = runCatching {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${city.lat}&longitude=${city.lon}" +
                "&current=temperature_2m,weather_code,is_day" +
                "&daily=temperature_2m_max,temperature_2m_min" +
                "&hourly=temperature_2m,weather_code,is_day" +
                "&timezone=America%2FArgentina%2FCordoba&forecast_days=2",
        )
        val conn = url.openConnection() as HttpURLConnection
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
                hourly = parseHourly(json.getJSONObject("hourly")),
            )
        } finally {
            conn.disconnect()
        }
    }.getOrNull()

    /** Próximas 6 horas a partir de la hora actual en Córdoba. */
    private fun parseHourly(h: JSONObject): List<HourForecast> {
        val times = h.getJSONArray("time")
        val temps = h.getJSONArray("temperature_2m")
        val codes = h.getJSONArray("weather_code")
        val days = h.getJSONArray("is_day")
        val key = SimpleDateFormat("yyyy-MM-dd'T'HH':00'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone(TZ) }
            .format(Date())
        var start = 0
        while (start < times.length() && times.getString(start) < key) start++
        return (start until minOf(start + 6, times.length())).map { i ->
            HourForecast(
                time = times.getString(i).substring(11, 13),
                temp = Math.round(temps.getDouble(i)).toInt(),
                code = codes.getInt(i),
                isDay = days.getInt(i) == 1,
            )
        }
    }

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
