package blackleg15.apps.battery_alarm

import android.content.Intent
import android.os.*
import androidx.annotation.NonNull
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.embedding.android.FlutterActivity

class NotificationServiceResultReceiver(val callback: (Float) -> Unit, handler: Handler?) : ResultReceiver(handler) {
    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        super.onReceiveResult(resultCode, resultData)
        val data: Float? = resultData?.getFloat("battery")
        if (data != null) {
            callback(data)
        }
    }
}

class MainActivity : FlutterActivity() {
    private val channelStream = "adbysantos.apps.battery_alarm/stream"

    fun initService(callback: ((Float) -> Unit)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceIntent = Intent(this, BatteryNotificationService::class.java)
            serviceIntent.putExtra("notification_callback", NotificationServiceResultReceiver(callback, null))
            startService(serviceIntent)
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)


        EventChannel(flutterEngine.dartExecutor.binaryMessenger, channelStream)
            .setStreamHandler(
                object : EventChannel.StreamHandler {
                    override fun onListen(args: Any?, events: EventChannel.EventSink) {
                        val onNewBatteryValueCallback = fun(value: Float) { events.success(value) }
                        initService(onNewBatteryValueCallback)
                        println("init service")
                    }

                    override fun onCancel(arguments: Any?) {
                        println("cancelling listener")
                    }

                }
            )
    }
}

