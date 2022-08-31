import 'package:flutter/services.dart';

class BatteryPlatformChannelRepository {
  final batteryEventChannel = const EventChannel('adbysantos.apps.battery_alarm/stream');
  final _batteryPlatformChannel = const MethodChannel('adbysantos.apps.battery_alarm/call');

  Future<void> initService() async {
    await _batteryPlatformChannel.invokeMethod('initService');
  }

  Future<void> stopService() async {
    await _batteryPlatformChannel.invokeMethod('stopService');
  }

  const BatteryPlatformChannelRepository();
}
