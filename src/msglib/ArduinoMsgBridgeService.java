package msglib;

import gnu.io.CommPortIdentifier;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Enumeration;
import java.util.Random;

/**
 * Message passing bridge for that supports multiple serial ports -> UDP
 * 
 * To be used with one or multiple Arduinos connected via Serial Port
 *
 */
class MsgReceiver extends Thread {

	private MsgServiceUDPMulticast multicast;
	private CommChannel channel;

	public MsgReceiver(MsgServiceUDPMulticast multicast, CommChannel channel) {
		this.channel = channel;
		this.multicast = multicast;
	}

	public void run() {
		try {
			while (true) {
				String msg = channel.receiveMsg();
				// check if a dest is specified
				int index = msg.indexOf(":");
				if (index == -1) {
					log("[BRIDGE] new msg on Arduino: " + msg);
					multicast.sendMsg(msg);
				} else {
					String target = msg.substring(0, index);
					msg = msg.substring(index + 1);
					log("[BRIDGE] new msg on Arduino to " + target + ": " + msg);
					multicast.sendMsgTo(target, msg);

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void log(String msg) {
		System.out.println(msg);
	}

}

class MsgForwarder extends Thread {

	private MsgServiceUDPMulticast multicast;
	private CommChannel channel;

	public MsgForwarder(MsgServiceUDPMulticast multicast, CommChannel channel) {
		this.channel = channel;
		this.multicast = multicast;
	}

	public void run() {
		try {
			while (true) {
				Msg msg = multicast.receiveMsg();
				log("[BRIDGE] new msg on Arduino from " + msg.getSender()
						+ ": " + msg.getContent());
				channel.sendMsg(msg.getSender(), msg.getContent());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void log(String msg) {
		System.out.println(msg);
	}
}

public class ArduinoMsgBridgeService {

	public static void main(String[] args) throws Exception {

		if (args.length == 0) {
			System.out.println("args: <CommPortName> <BoudRate>");
			System.out.println("Available serial ports:");

			Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
			while (portEnum.hasMoreElements()) {
				CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
						.nextElement();
				System.out.println(currPortId.getName());
			}
		} else {
			BroadcastSerialCommChannel channel = new BroadcastSerialCommChannel(args, 9600);
			// SerialCommChannel channel = new SerialCommChannel(args[0], 9600);

			System.out.println("[BRIDGE] Waiting for arduino... ");

			String who = "";

			int nJoins = 0;
			while (nJoins < args.length) {
				String msg = channel.receiveMsg();
				if (msg.startsWith("join:")) {
					who = msg.substring(5);
					nJoins++;

					System.out.println("[BRIDGE] Arduino joined with name: "
							+ who);
				}
			}

			MsgServiceUDPMulticast multicast = new MsgServiceUDPMulticast();
			multicast.init(who);

			new MsgReceiver(multicast, channel).start();
			new MsgForwarder(multicast, channel).start();

			System.out.println("Ready.");
		}
	}
}
