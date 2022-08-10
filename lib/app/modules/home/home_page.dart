import 'package:battery_indicator/battery_indicator.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_modular/flutter_modular.dart';
import 'home_store.dart';

class HomePage extends StatefulWidget {
  final String title;
  const HomePage({Key? key, this.title = "Home"}) : super(key: key);

  @override
  createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final store = Modular.get<HomeStore>();
  late FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin;
  late AndroidInitializationSettings androidInitializationSettings;
  late InitializationSettings initializationSettings;

  var buildCounter = 0;

  var previousBatteryValue = 0.0;
  var currentBatteryValue = 0.0;

  @override
  void initState() {
    super.initState();
    initializeNotifications();
    startListeningToTheBatteryValue();
  }

  void initializeNotifications() {
    flutterLocalNotificationsPlugin = FlutterLocalNotificationsPlugin();
    androidInitializationSettings = const AndroidInitializationSettings('app_icon');
    initializationSettings = InitializationSettings(
      android: androidInitializationSettings,
    );
    flutterLocalNotificationsPlugin.initialize(initializationSettings);
  }

  Future<void> showNotification() async {
    const AndroidNotificationDetails androidPlatformChannelSpecifics = AndroidNotificationDetails(
      'your channel id',
      'your channel name',
      channelDescription: 'your channel description',
      importance: Importance.max,
      priority: Priority.high,
      ticker: 'ticker',
      fullScreenIntent: true,
    );
    const NotificationDetails platformChannelSpecifics = NotificationDetails(android: androidPlatformChannelSpecifics);
    await flutterLocalNotificationsPlugin.show(
      0,
      'Seu celular agradece:',
      'Preserve a bateria dele tirando-o da tomada',
      platformChannelSpecifics,
      payload: 'item x',
    );
  }

  void startListeningToTheBatteryValue() {
    final batteryStream = store.channel.batteryChannel.receiveBroadcastStream();
    batteryStream.listen(onNewBatteryValueCallback);
  }

  void onNewBatteryValueCallback(dynamic possibleValue) {
    final newBatteryValue = possibleValue as double?;
    if (newBatteryValue != null && newBatteryValue != currentBatteryValue) {
      if (newBatteryValue == 80 && previousBatteryValue < 80) {
        showNotification();
      }
      setState(() {
        previousBatteryValue = currentBatteryValue;
        currentBatteryValue = newBatteryValue;
        buildCounter++;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Battery Listener'),
      ),
      body: Center(
        child: Builder(
          builder: (context) {
            return Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text('Builds: $buildCounter'),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Text(
                      'Battery: ',
                      style: TextStyle(fontSize: 40),
                    ),
                    BatteryIndicator(batteryFromPhone: false, batteryLevel: currentBatteryValue.toInt(), size: 36, percentNumSize: 27),
                    TextButton(
                        onPressed: () {
                          showNotification();
                        },
                        child: const Text('Show notification'))
                  ],
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}
