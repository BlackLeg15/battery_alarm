import 'package:flutter/services.dart';

class BatteryPlatformChannelRepository {
  final batteryEventChannel = const EventChannel('adbysantos.apps.battery_alarm/stream');

  const BatteryPlatformChannelRepository();
}
