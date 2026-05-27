package cz.macak.weatherhk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class WeatherWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    companion object {
        const val CHANNEL_ID = "weather_hk_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun doWork(): Result {
        val forecasts = WeatherApi.fetchForecast()
        val text = if (forecasts != null) buildNotificationText(forecasts)
                   else "Nepodařilo se načíst počasí. Zkontroluj připojení."

        showNotification(text)
        return Result.success()
    }

    private fun buildNotificationText(forecasts: List<DayForecast>): String {
        return forecasts.joinToString("\n") { f ->
            val day = f.date.substring(8).trimStart('0') // jen číslo dne, bez leading zero
            if (f.available) {
                val rain = if (f.precipitation > 0) " 💧" else ""
                "$day  ${f.icon}  ${f.tempMax.toInt()}°/${f.tempMin.toInt()}°$rain"
            } else {
                "$day  ?"
            }
        }
    }

    private fun showNotification(text: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Počasí HK",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Denní předpověď počasí pro Hradec Králové"
        }
        nm.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Počasí HK • 8.6. – 14.6.")
            .setContentText(text.lines().firstOrNull() ?: "")
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }
}
