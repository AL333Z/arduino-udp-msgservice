# arduino-udp-msgservice
A message-passing library, to bridge serial port data to UDP messages.

> Most of this code is taken and adapted from the course `Embedded Systems Programming`, `Unibo 2014/2015`

`ArduinoMsgBridgeService` is an example of a message passing bridge for that supports multiple serial ports -> Multicast UDP.

##Up and running
Launch `ArduinoMsgBridgeService`, with a list of serial ports as arguments.

`java -Djava.net.preferIPv4Stack=true -jar bridge.jar /dev/cu.usbmodem1421`

For a concrete running example, see [play-parking-pi](https://github.com/AL333Z/play-parking-pi) and [arduino-parking](https://github.com/AL333Z/arduino-parking).
