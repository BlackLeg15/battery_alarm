import 'package:flutter/services.dart';

class BatteryPlatformChannel {
  final batteryChannel = const EventChannel('com.example.battery_alarm/stream');
}
