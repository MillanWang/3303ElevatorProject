package app.ElevatorSubsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;

import app.UDP.PacketReceiver;
import app.UDP.Util;

public class ElevatorSubsystem_SchedulerPacketReceiver extends PacketReceiver {

	ElevatorSubsystem ess;
	
	protected ElevatorSubsystem_SchedulerPacketReceiver(String name, int port, ElevatorSubsystem ess) {
		super(name, port);
		this.ess = ess;
	}

	@Override
	protected DatagramPacket createReplyPacketGivenRequestPacket(DatagramPacket requestPacket) {
		System.out.println("\n\n\n***************MADE IT INTO THE ELEVATOR SCHEDULER RECEIVER****************\n\n\n");
		HashMap<Integer, Integer> nextFloorHashMap  = null;
		try {
			Object obj = Util.deserialize(requestPacket.getData());
			nextFloorHashMap  = (HashMap<Integer, Integer>) obj;
		}catch(IOException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		//TODO : Call something in the ESS areas to handle this new incoming Hashmap
		System.out.println(nextFloorHashMap);

		
		
		//Create packet to reply with. Then send
        byte[] replyData = "200 OK".getBytes();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, requestPacket.getAddress(), requestPacket.getPort());
		return replyPacket;
	}

}
