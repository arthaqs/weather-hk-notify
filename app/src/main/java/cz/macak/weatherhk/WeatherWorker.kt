package cz.macak.weatherhk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.widget.RemoteViews
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
        showNotification(forecasts)
        return Result.success()
    }

    private val dayIds = listOf(
        R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6, R.id.day7
    )
    private val iconIds = listOf(
        R.id.icon1, R.id.icon2, R.id.icon3, R.id.icon4, R.id.icon5, R.id.icon6, R.id.icon7
    )
    private val maxIds = listOf(
        R.id.max1, R.id.max2, R.id.max3, R.id.max4, R.id.max5, R.id.max6, R.id.max7
    )
    private val minIds = listOf(
        R.id.min1, R.id.min2, R.id.min3, R.id.min4, R.id.min5, R.id.min6, R.id.min7
    )

    private fun showNotification(forecasts: List<DayForecast>?) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Počasí HK",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Denní předpověď počasí pro Hradec Králové" }
        nm.createNotificationChannel(channel)

        val views = RemoteViews(applicationContext.packageName, R.layout.notification_weather)

        if (forecasts != null) {
            forecasts.forEachIndexed { i, f ->
                val day = f.date.substring(8).trimStart('0') // "8", "9", ...
                views.setTextViewText(dayIds[i], day + ".6.")
                views.setTextViewText(iconIds[i], if (f.available) f.icon else "?")
                views.setTextViewText(maxIds[i], if (f.available) "${f.tempMax.toInt()}°" else "—")
                views.setTextViewText(minIds[i], if (f.available) "${f.tempMin.toInt()}°" else "—")
            }
        } else {
            for (i in 0..6) {
                views.setTextViewText(iconIds[i], "⚠️")
                views.setTextViewText(maxIds[i], "—")
                views.setTextViewText(minIds[i], "—")
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🌤️ Počasí HK • 8.–14. června")
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomBigContentView(views)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }
}
