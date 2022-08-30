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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

class BatteryNotificationService : Service() {
    private var batteryBroadcastReceiver: BatteryBroadcastReceiver? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun buildNotification() {
        val stop = "stop"
        val broadcastIntent = PendingIntent.getBroadcast(
            this, 0, Intent(stop), PendingIntent.FLAG_IMMUTABLE
        )

        // Create the persistent notification
        val builder = NotificationCompat.Builder(this, "1234")
            .setContentTitle("Battery Alarm")
            .setContentText("Battery tracking is working")
            .setOngoing(true)
            .setContentIntent(broadcastIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "1234", "Battery Alarm",
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
        buildNotification()
        //createNotificationChannel()

        val callback : ResultReceiver = if (Build.VERSION.SDK_INT >= 33) {
            intent!!.getParcelableExtra("notification_callback", ResultReceiver::class.java)!!
        } else {
            intent!!.getParcelableExtra("notification_callback")!!
        }

        val onNewBatteryValueCallback = fun(value: Float) {
            val bundle = Bundle()
            bundle.putFloat("battery", value)
            callback.send(0, bundle)
            showToast(value)}
        batteryBroadcastReceiver = BatteryBroadcastReceiver(onNewBatteryValueCallback)
        registerReceiver(batteryBroadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        println("Notifying")
        return START_STICKY
    }

    private fun showToast(value : Float){
        Toast.makeText(this@BatteryNotificationService, "Battery Value: $value", Toast.LENGTH_LONG).show()
    }

    private fun showNotification(value : Float){
        val builder = NotificationCompat.Builder(this, "123123123")
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle("Hello, World")
            .setContentText(value.toString())
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(Random.nextInt(), builder.build())
        }
        println("Notifying")
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
