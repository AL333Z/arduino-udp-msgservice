package msglib;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

public class Sender extends Thread {
	
	public void run(){
		try {
			MulticastSocket ms = new MulticastSocket(20001);
			InetAddress group = InetAddress.getByName("224.111.112.113");
			ms.setReuseAddress(true);
            ms.setLoopbackMode(false);
            ms.setTimeToLive(2);
            Random gen = new Random(System.currentTimeMillis());
            String tag = ""+gen.nextLong();
            ms.joinGroup(group);
            int count = 0;
            System.out.println("SENDER READY.");
			while (true){
				String msg = tag+": hello "+count++;
                DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), group, 20001);
                ms.send(dp);				
                Thread.sleep(1000);
    		}
		} catch (Exception ex){
			ex.printStackTrace();
			System.err.println("Network exception.");
		}
	}

	public static void main(String[] args){
		new Sender().start();		
	}
	
}
