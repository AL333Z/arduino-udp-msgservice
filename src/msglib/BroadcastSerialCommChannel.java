package msglib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * Broadcast comm channel implementation based on serial port. The semantic of
 * this implementation support multiple ports:
 * 
 * `receiveMsg` and `isMsgAvailable` msg returns a msg from one of the ports.
 * `sendMsg` sends a message to all ports.
 *
 */
public class BroadcastSerialCommChannel implements CommChannel,
		SerialPortEventListener {

	private List<SerialPort> serialPorts;
	private List<BufferedReader> inputs;
	private List<OutputStream> outputs;
	private BlockingQueue<String> queue;

	public BroadcastSerialCommChannel(String[] ports, int rate) throws Exception {
		queue = new ArrayBlockingQueue<String>(100);

		serialPorts = new LinkedList<>();
		inputs = new LinkedList<>();
		outputs = new LinkedList<>();

		for (String port : ports) {
			System.out.println(port);

			CommPortIdentifier portId = CommPortIdentifier
					.getPortIdentifier(port);

			// open serial port, and use class name for the appName.
			SerialPort serialPort = (SerialPort) portId.open(this.getClass()
					.getName(), 2000);

			// set port parameters
			serialPort.setSerialPortParams(rate, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// open the streams
			inputs.add(new BufferedReader(new InputStreamReader(serialPort
					.getInputStream())));
			outputs.add(serialPort.getOutputStream());
			serialPorts.add(serialPort);

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		}
	}

	@Override
	public void sendMsg(String sender, String msg) {
		String fullString = (sender + ":" + msg + "$");
		// System.out.println("COMCHANNEL: sending "+fullString);
		char[] array = fullString.toCharArray();
		byte[] bytes = new byte[array.length];
		for (int i = 0; i < array.length; i++) {
			bytes[i] = (byte) array[i];
		}
		try {
			for (OutputStream output : outputs) {
				output.write(bytes);
				output.flush();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public String receiveMsg() throws InterruptedException {
		return queue.take();
	}

	@Override
	public boolean isMsgAvailable() {
		return !queue.isEmpty();
	}

	/**
	 * This should be called when you stop using the port. This will prevent
	 * port locking on platforms like Linux.
	 */
	public synchronized void close() {
		for (SerialPort serialPort : serialPorts) {
			if (serialPort != null) {
				serialPort.removeEventListener();
				serialPort.close();
			}
		}
	}

	/**
	 * Handle an events on the serial ports. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				for (int i = 0; i < serialPorts.size(); i++) {
					if (oEvent.getSource() == (SerialPort)serialPorts.get(i)) {
						String msg = inputs.get(i).readLine();
						queue.put(msg);

						break;
					}
				}
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other
		// ones.
	}
}
