package com.example.battery_alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.annotation.NonNull
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import java.util.*
import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity() {
    private val CHANNEL_STREAM = "com.example.battery_alarm/stream"
    private var batteryBroadcastReceiver: BatteryBroadcastReceiver? = null

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_STREAM)
            .setStreamHandler(
                object : EventChannel.StreamHandler {
                    override fun onListen(args: Any?, events: EventChannel.EventSink) {
                        val onNewBatteryValueCallback = fun(value: Float) { events.success(value)}
                        batteryBroadcastReceiver = BatteryBroadcastReceiver(onNewBatteryValueCallback)
                        registerReceiver(batteryBroadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    }

                    override fun onCancel(arguments: Any?) {
                        println("cancelling listener")
                    }
                }
            )
    }

    override fun onDestroy() {
        batteryBroadcastReceiver?.abortBroadcast()
        super.onDestroy()
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