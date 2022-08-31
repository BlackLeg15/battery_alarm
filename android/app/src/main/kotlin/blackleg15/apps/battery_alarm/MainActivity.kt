package blackleg15.apps.battery_alarm

import android.content.Intent
import android.os.*
import androidx.annotation.NonNull
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel

class NotificationServiceResultReceiver(val callback: (Float) -> Unit, handler: Handler?) :
    ResultReceiver(handler) {
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
    private val methodChannel = "adbysantos.apps.battery_alarm/call"
    private lateinit var onNewBatteryValueCallback: (Float) -> Unit

    private fun initService(callback: (Float) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceIntent = Intent(this, BatteryNotificationService::class.java)
            serviceIntent.putExtra(
                "notification_callback",
                NotificationServiceResultReceiver(callback, null)
            )
            startService(serviceIntent)
        }
    }

    private fun stopService() {
        stopService(Intent(this, BatteryNotificationService::class.java))
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            methodChannel
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "initService" -> {
                    try {
                        initService(onNewBatteryValueCallback)
                        result.success(null)
                    } catch (e: Error) {
                        result.error("START", "Couldn't start the battery service", e.message)
                    }
                }
                "stopService" -> {
                    try {
                        stopService()
                        result.success(null)
                    } catch (e: Error) {
                        result.error("STOP", "Couldn't stop the battery service", e.message)
                    }
                }
                else -> {
                    result.notImplemented()
                }
            }
        }

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, channelStream)
            .setStreamHandler(
                object : EventChannel.StreamHandler {
                    override fun onListen(args: Any?, events: EventChannel.EventSink) {
                        onNewBatteryValueCallback = fun(value: Float) { events.success(value) }
                        println("registering on new battery value callback")
                    }

                    override fun onCancel(arguments: Any?) {
                        println("cancelling event channel")
                    }

                }
            )
    }
}

