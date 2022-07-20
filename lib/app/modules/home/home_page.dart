import 'package:battery_indicator/battery_indicator.dart';
import 'package:flutter/material.dart';
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

  var buildCounter = 0;

  var previousBatteryValue = 0.0;
  var currentBatteryValue = 0.0;

  @override
  void initState() {
    super.initState();
    startListeningToTheBatteryValue();
  }

  void startListeningToTheBatteryValue() {
    final batteryStream = store.channel.batteryChannel.receiveBroadcastStream();
    batteryStream.listen(onNewBatteryValueCallback);
  }

  void onNewBatteryValueCallback(dynamic possibleValue) {
    final newBatteryValue = possibleValue as double?;
    if (newBatteryValue != null && newBatteryValue != currentBatteryValue) {
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
