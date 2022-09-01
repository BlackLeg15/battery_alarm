package blackleg15.apps.battery_alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.core.app.NotificationCompat

class BatteryNotificationService : Service() {
    private var batteryBroadcastReceiver: BatteryBroadcastReceiver? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun buildNotificationBadge() {
        val stop = "stop"
        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        val broadcastIntent =
            PendingIntent.getBroadcast(
                this, 0, Intent(stop), flag
            )

        val builder = NotificationCompat.Builder(this, "ee2fe36d4e0f04404109ad5e45bbe2ed")
            .setContentTitle("Battery Alarm")
            .setContentText("Battery tracking is working")
            .setOngoing(true)
            .setContentIntent(broadcastIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ee2fe36d4e0f04404109ad5e45bbe2ed", "Battery Alarm",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setShowBadge(false)
            channel.description = "Listening to battery values"
            channel.setSound(null, null)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        startForeground(1, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        buildNotificationBadge()

        val resultReceiver: ResultReceiver = if (Build.VERSION.SDK_INT >= 33) {
            intent!!.getParcelableExtra("notification_callback", ResultReceiver::class.java)!!
        } else {
            intent!!.getParcelableExtra("notification_callback")!!
        }

        val onNewBatteryValueCallback = fun(value: Float) {
            val bundle = Bundle()
            bundle.putFloat("battery", value)
            resultReceiver.send(0, bundle)
        }
        if (batteryBroadcastReceiver == null) {
            batteryBroadcastReceiver = BatteryBroadcastReceiver(onNewBatteryValueCallback)
        }
        registerReceiver(batteryBroadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        println("Notifying")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryBroadcastReceiver)
    }
}

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
