package tests.ElevatorSubsystemTests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

import app.UDP.Util;


/***
 * 
 * Used to test elevator
 * 
 * @author benki
 *
 */

public class tmp {

	public static void main(String[] args) {
		int port = 3000;
		byte[] data = new byte[1032];
		
		DatagramPacket packet = new DatagramPacket(data,data.length);
		
		try {
			DatagramSocket socket = new DatagramSocket(port);
			socket.receive(packet);
			
			HashMap<Integer, Integer> floors = new HashMap<Integer,Integer>();
			
			floors.put(1, 5);
			floors.put(2, 6);
			
			data = Util.serialize(floors);
			packet = new DatagramPacket(data, data.length,packet.getAddress(), packet.getPort());
			socket.send(packet);
		}catch(IOException e) {
			e.printStackTrace();
		}		
		
		
	}
	
	
}
