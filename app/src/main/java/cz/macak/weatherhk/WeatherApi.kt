package cz.macak.weatherhk

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request

data class WeatherResponse(
    val daily: DailyData
)

data class DailyData(
    val time: List<String>,
    @SerializedName("temperature_2m_max") val tempMax: List<Double>,
    @SerializedName("temperature_2m_min") val tempMin: List<Double>,
    @SerializedName("weathercode") val weatherCode: List<Int>,
    @SerializedName("precipitation_sum") val precipitation: List<Double>
)

data class DayForecast(
    val date: String,
    val tempMax: Double,
    val tempMin: Double,
    val description: String,
    val icon: String,
    val precipitation: Double,
    val available: Boolean = true
)

object WeatherApi {

    private const val LAT = 50.2092
    private const val LON = 15.8328

    // Target week — hardcoded, app is single-purpose
    private val TARGET_DATES = listOf(
        "2026-06-08", "2026-06-09", "2026-06-10",
        "2026-06-11", "2026-06-12", "2026-06-13", "2026-06-14"
    )

    private val client = OkHttpClient()
    private val gson = Gson()

    fun fetchForecast(): List<DayForecast>? {
        // Use forecast_days=16 (max) — avoids 400 when end date out of range
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$LAT&longitude=$LON" +
            "&daily=temperature_2m_max,temperature_2m_min,weathercode,precipitation_sum" +
            "&timezone=Europe%2FPrague" +
            "&forecast_days=16"

        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return fallbackUnavailable()
            val body = response.body?.string() ?: return fallbackUnavailable()
            val data = gson.fromJson(body, WeatherResponse::class.java)

            // Index available dates
            val byDate = data.daily.time.mapIndexed { i, date -> date to i }.toMap()

            TARGET_DATES.map { date ->
                val i = byDate[date]
                if (i != null) {
                    val code = data.daily.weatherCode[i]
                    DayForecast(
                        date = date,
                        tempMax = data.daily.tempMax[i],
                        tempMin = data.daily.tempMin[i],
                        description = weatherCodeToText(code),
                        icon = weatherCodeToIcon(code),
                        precipitation = data.daily.precipitation[i],
                        available = true
                    )
                } else {
                    // Date not yet in forecast window
                    DayForecast(
                        date = date,
                        tempMax = 0.0,
                        tempMin = 0.0,
                        description = "brzy k dispozici",
                        icon = "?",
                        precipitation = 0.0,
                        available = false
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun fallbackUnavailable(): List<DayForecast> =
        TARGET_DATES.map { date ->
            DayForecast(date, 0.0, 0.0, "brzy k dispozici", "?", 0.0, false)
        }

    private fun weatherCodeToIcon(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2 -> "🌤️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        80, 81, 82 -> "🌦️"
        95 -> "⛈️"
        96, 99 -> "⛈️"
        else -> "🌥️"
    }

    private fun weatherCodeToText(code: Int): String = when (code) {
        0 -> "Jasno"
        1, 2 -> "Skoro jasno"
        3 -> "Oblačno"
        45, 48 -> "Mlha"
        51, 53, 55 -> "Mrholení"
        61, 63, 65 -> "Déšť"
        71, 73, 75 -> "Sněžení"
        80, 81, 82 -> "Přeháňky"
        95 -> "Bouřka"
        96, 99 -> "Bouřka s kroupami"
        else -> "Proměnlivě"
    }
}
