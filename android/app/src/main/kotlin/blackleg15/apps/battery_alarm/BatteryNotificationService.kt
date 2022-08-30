package blackleg15.apps.battery_alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
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
            .setContentText("Location tracking is working")
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


//    private fun createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "Battery Value Channel"
//            val descriptionText = "That's my battery value notification channel"
//            val importance = NotificationManager.IMPORTANCE_HIGH
//            val channel = NotificationChannel("123123123", name, importance).apply {
//                description = descriptionText
//            }
//            // Register the channel with the system
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        buildNotification()
        //createNotificationChannel()

        //val onNewBatteryValueCallback = fun(value: Float) { showNotification(value)}
        val onNewBatteryValueCallback = fun(value: Float) { showToast(value)}
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

    override fun onDestroy() {

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
