package blackleg15.apps.battery_alarm

import android.content.Intent
import android.os.Build
import androidx.annotation.NonNull
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity() {
    private val channelStream = "adbysantos.apps.battery_alarm/stream"

    fun initService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(Intent(this, BatteryNotificationService::class.java))
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)


        EventChannel(flutterEngine.dartExecutor.binaryMessenger, channelStream)
            .setStreamHandler(
                object : EventChannel.StreamHandler {
                    override fun onListen(args: Any?, events: EventChannel.EventSink) {
                        //val onNewBatteryValueCallback = fun(value: Float) { events.success(value)}
                        initService()
                        println("init service")
                    }

                    override fun onCancel(arguments: Any?) {
                        println("cancelling listener")
                    }

                }
            )
    }
}

