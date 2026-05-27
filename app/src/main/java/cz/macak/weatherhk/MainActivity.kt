package cz.macak.weatherhk

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationPermission()
        WeatherScheduler.scheduleDailyAt8AM(this)

        findViewById<TextView>(R.id.tvStatus).text =
            "Notifikace naplánována na každý den v 8:00\nPočasí: Hradec Králové • 8.6.–14.6."

        findViewById<Button>(R.id.btnTestNow).setOnClickListener {
            val req = OneTimeWorkRequestBuilder<WeatherWorker>().build()
            WorkManager.getInstance(this).enqueue(req)
            Toast.makeText(this, "Načítám počasí…", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
}
