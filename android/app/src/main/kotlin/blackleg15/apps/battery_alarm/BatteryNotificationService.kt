package blackleg15.apps.battery_alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

class BatteryNotificationService : Service() {
    private var batteryBroadcastReceiver: BatteryBroadcastReceiver? = null

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Battery Value Channel"
            val descriptionText = "That's my battery value notification channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("123123123", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val onNewBatteryValueCallback = fun(value: Float) { showNotification(value)}
        batteryBroadcastReceiver = BatteryBroadcastReceiver(onNewBatteryValueCallback)
        registerReceiver(batteryBroadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        println("Notifying")
        return START_STICKY;
    }

    fun showNotification(value : Float){
        val builder = NotificationCompat.Builder(this, "123123123")
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle("Hello, World")
            .setContentText("Description")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(Random.nextInt(), builder.build())
        }
        println("Notifying")
    }
}

    private const val TAG = "BatteryBroadcastReceiver"

    private class BatteryBroadcastReceiver(val callback: ((Float) -> Unit)) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val battery: Float? = intent?.let { value ->
                val level: Int = value.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = value.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                level * 100 / scale.toFloat()
            }
            println("New battery level: $battery")
            callback((battery ?: 0.0) as Float)

        }
    }
