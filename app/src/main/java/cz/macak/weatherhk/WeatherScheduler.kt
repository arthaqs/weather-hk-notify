package cz.macak.weatherhk

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WeatherScheduler {

    fun scheduleDailyAt8AM(context: Context) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val firstRun = OneTimeWorkRequestBuilder<WeatherWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag("weather_first")
            .build()

        val daily = PeriodicWorkRequestBuilder<WeatherWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay + TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag("weather_daily")
            .build()

        val wm = WorkManager.getInstance(context)
        wm.enqueueUniqueWork(
            "weather_first_run",
            androidx.work.ExistingWorkPolicy.REPLACE,
            firstRun
        )
        wm.enqueueUniquePeriodicWork(
            "weather_daily",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            daily
        )
    }
}
