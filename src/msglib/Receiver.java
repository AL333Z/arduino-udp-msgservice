package msglib;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Receiver extends Thread {
	
	public void run(){
		try {
			MulticastSocket ms = new MulticastSocket(20001);
			InetAddress group = InetAddress.getByName("224.111.112.113");
			ms.setReuseAddress(true);
            ms.setLoopbackMode(false);
            ms.setTimeToLive(2);
			ms.joinGroup(group);
            byte[] buf = new byte[1000];
            System.out.println("RECEIVER READY.");
			while (true){
				DatagramPacket recv = new DatagramPacket(buf, buf.length);
				ms.receive(recv);
                String tmp = new String(recv.getData(),0,recv.getLength());
                System.out.println("\n\nReceived: \""+ tmp + "\"\nMessage Length is: " + tmp.length());				
    		}
		} catch (Exception ex){
			ex.printStackTrace();
			System.err.println("Network exception.");
		}
	}
	
	
	public static void main(String[] args){
		new Receiver().start();		
	}

}
