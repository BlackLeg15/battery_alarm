import 'package:battery_indicator/battery_indicator.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_modular/flutter_modular.dart';

import 'battery_platform_channel.dart';
import 'home_store.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final store = Modular.get<HomeStore>();

  late Future<bool> futureInitializeNotificationsChannel;

  late BatteryPlatformChannelRepository batteryPlatformChannelRepository;

  @override
  void initState() {
    super.initState();
    futureInitializeNotificationsChannel = initializeNotificationsChannel();
    batteryPlatformChannelRepository = store.channel;
    registerCallbackForBatteryValues();
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

  void registerCallbackForBatteryValues() {
    final batteryStream = batteryPlatformChannelRepository.batteryEventChannel.receiveBroadcastStream();
    batteryStream.listen(onNewBatteryValueCallback);
  }

  var previousBatteryValue = 0.0;
  var currentBatteryValue = 0.0;

  void onNewBatteryValueCallback(dynamic possibleValue) {
    try {
      final newBatteryValue = possibleValue as double?;
      if (newBatteryValue != null) {
        if (newBatteryValue >= 80.0 && newBatteryValue > currentBatteryValue) {
          showNotification();
        }
        setState(() {
          previousBatteryValue = currentBatteryValue;
          currentBatteryValue = newBatteryValue;
        });
      }
    } catch (e) {
      onNewBatteryValueError();
    }
  }

  void onNewBatteryValueError() {
    const batteryDataFromChannelError = 'Não foi possível receber o nível atual de bateria';
    showSnackBar(message: batteryDataFromChannelError);
  }

  void showSnackBar({required String message}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }

  Future<void> showNotification() async {
    try {
      await flutterLocalNotificationsPlugin.show(
        0,
        'Seu celular agradece:',
        'Preserve a bateria dele tirando-o da tomada',
        platformChannelSpecifics,
      );
    } catch (e) {
      onErrorWhenRequestingNotifications();
    }
  }

  void onErrorWhenRequestingNotifications() {
    const notificationRequestError = 'Não foi possível lançar uma notificação';
    showSnackBar(message: notificationRequestError);
  }

  var notificationRequestError = '';

  var isRequestingNotification = false;

  bool isLoading = false;

  bool isBatteryServiceInitialized = false;

  Future<void> initBatteryNotificationsService() async {
    setState(() {
      isLoading = true;
    });
    try {
      await batteryPlatformChannelRepository.initService();
      isBatteryServiceInitialized = true;
    } catch (e) {
      showSnackBar(message: "Não foi possível iniciar o serviço de notificações de bateria");
    }
    setState(() {
      isLoading = false;
    });
  }

  Future<void> stopBatteryNotificationsService() async {
    setState(() {
      isLoading = true;
    });
    try {
      await batteryPlatformChannelRepository.stopService();
      isBatteryServiceInitialized = false;
    } catch (e) {
      showSnackBar(message: "Não foi possível parar o serviço de notificações de bateria");
    }
    setState(() {
      isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Sistema de notificação de bateria'),
      ),
      body: Center(
        child: FutureBuilder<bool>(
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
            if (isLoading) {
              return const CircularProgressIndicator();
            }
            return Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Text(
                      'Bateria: ',
                      style: TextStyle(fontSize: 40),
                    ),
                    BatteryIndicator(
                      batteryFromPhone: false,
                      batteryLevel: currentBatteryValue.toInt(),
                      size: 36.0,
                      percentNumSize: 27.0,
                    ),
                  ],
                ),
                StatefulBuilder(
                  builder: (context, setState) {
                    if (isRequestingNotification) {
                      return const CircularProgressIndicator();
                    }
                    final widgets = <Widget>[];
                    final buttonWidget = TextButton(
                      onPressed: () async {
                        setState(() {
                          isRequestingNotification = true;
                        });
                        await showNotification();
                        if (!mounted) return;
                        setState(() {
                          isRequestingNotification = false;
                        });
                      },
                      child: const Text('Mostrar notificação'),
                    );
                    if (notificationRequestError.isNotEmpty) {
                      final errorWidget = Text(notificationRequestError, style: TextStyle(color: Theme.of(context).errorColor));
                      widgets.add(errorWidget);
                    }
                    widgets.add(buttonWidget);
                    return Column(mainAxisSize: MainAxisSize.min, children: widgets);
                  },
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Text("Status do serviço de bateria:"),
                    Switch(
                      value: isBatteryServiceInitialized,
                      onChanged: (value) => value ? initBatteryNotificationsService() : stopBatteryNotificationsService(),
                    ),
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
