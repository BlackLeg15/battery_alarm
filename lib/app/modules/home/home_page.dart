import 'package:battery_indicator/battery_indicator.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_modular/flutter_modular.dart';

import 'home_store.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final store = Modular.get<HomeStore>();

  late Future<bool> futureInitializeNotificationsChannel;

  @override
  void initState() {
    super.initState();
    futureInitializeNotificationsChannel = initializeNotificationsChannel();
    startListeningToTheBatteryValue();
  }

  late FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin;
  late NotificationDetails platformChannelSpecifics;

  Future<bool> initializeNotificationsChannel() async {
    try {
      flutterLocalNotificationsPlugin = FlutterLocalNotificationsPlugin();

      const androidInitializationSettings = AndroidInitializationSettings('app_icon');
      const initializationSettings = InitializationSettings(android: androidInitializationSettings);

      await flutterLocalNotificationsPlugin.initialize(initializationSettings);

      const androidPlatformChannelSpecifics = AndroidNotificationDetails(
        '493de931f3c92df6084ac2b3238ed309',
        'BATTERY_NOTIFICATION',
        importance: Importance.max,
        priority: Priority.high,
      );
      platformChannelSpecifics = const NotificationDetails(android: androidPlatformChannelSpecifics);
      return true;
    } catch (error) {
      onErrorWhenInitializingNotificationsChannel();
      return false;
    }
  }

  var notificationsChannelInitializationError = '';

  void onErrorWhenInitializingNotificationsChannel() {
    notificationsChannelInitializationError = 'Não foi possível inicializar o canal de notificações';
  }

  void startListeningToTheBatteryValue() {
    final batteryStream = store.channel.batteryChannel.receiveBroadcastStream();
    batteryStream.listen(onNewBatteryValueCallback);
  }

  var previousBatteryValue = 0.0;
  var currentBatteryValue = 0.0;
  var buildCounter = 0;

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

  Future<void> showNotification() {
    return flutterLocalNotificationsPlugin.show(
      0,
      'Seu celular agradece:',
      'Preserve a bateria dele tirando-o da tomada',
      platformChannelSpecifics,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Battery Listener'),
      ),
      body: Center(
        child: FutureBuilder(
          initialData: false,
          future: futureInitializeNotificationsChannel,
          builder: (context, snapshot) {
            final isNotificationsChannelInitialized = snapshot.data;
            if (isNotificationsChannelInitialized == false) {
              if (notificationsChannelInitializationError.isNotEmpty) {
                return Text(notificationsChannelInitializationError);
              }
              return const CircularProgressIndicator();
            }
            return Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                //Text('Builds: $buildCounter'),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Text(
                      'Battery: ',
                      style: TextStyle(fontSize: 40),
                    ),
                    BatteryIndicator(
                      batteryFromPhone: false,
                      batteryLevel: currentBatteryValue.toInt(),
                      size: 36,
                      percentNumSize: 27,
                    ),
                  ],
                ),
                TextButton(
                  onPressed: () {
                    showNotification();
                  },
                  child: const Text('Show notification'),
                )
              ],
            );
          },
        ),
      ),
    );
  }
}
