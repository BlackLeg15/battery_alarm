import 'package:flutter/services.dart';

class BatteryPlatformChannel {
  final batteryChannel = const EventChannel('blackleg15.apps.battery_alarm/stream');
}
