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
    val precipitation: Double
)

object WeatherApi {

    private const val LAT = 50.2092
    private const val LON = 15.8328
    private const val START = "2026-06-08"
    private const val END = "2026-06-14"

    private val client = OkHttpClient()
    private val gson = Gson()

    fun fetchForecast(): List<DayForecast>? {
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$LAT&longitude=$LON" +
            "&daily=temperature_2m_max,temperature_2m_min,weathercode,precipitation_sum" +
            "&timezone=Europe%2FPrague" +
            "&start_date=$START&end_date=$END"

        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return null
            val data = gson.fromJson(body, WeatherResponse::class.java)
            data.daily.time.mapIndexed { i, date ->
                DayForecast(
                    date = date,
                    tempMax = data.daily.tempMax[i],
                    tempMin = data.daily.tempMin[i],
                    description = weatherCodeToText(data.daily.weatherCode[i]),
                    precipitation = data.daily.precipitation[i]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun weatherCodeToText(code: Int): String = when (code) {
        0 -> "Jasno ☀️"
        1, 2 -> "Skoro jasno 🌤️"
        3 -> "Oblačno ☁️"
        45, 48 -> "Mlha 🌫️"
        51, 53, 55 -> "Mrholení 🌦️"
        61, 63, 65 -> "Déšť 🌧️"
        71, 73, 75 -> "Sněžení ❄️"
        80, 81, 82 -> "Přeháňky 🌦️"
        95 -> "Bouřka ⛈️"
        96, 99 -> "Bouřka s kroupami ⛈️"
        else -> "Proměnlivě"
    }
}
