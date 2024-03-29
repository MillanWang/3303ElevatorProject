package app.ElevatorSubsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;

import app.Scheduler.SchedulerInfo;
import app.UDP.PacketReceiver;
import app.UDP.Util;

/***
 * This class is used to receive packets from scheduler as to
 * determine the next floors and errors each elevator has to deal 
 * with as reply to the scheduler with updates about each elevator.
 * 
 * @author benki
 *
 */
public class ElevatorSubsystem_SchedulerPacketReceiver extends PacketReceiver {

	ElevatorSubsystem ess;
	SchedulerInfo nextFloorHashMap;
	boolean exit; 

	protected ElevatorSubsystem_SchedulerPacketReceiver(String name, int port, ElevatorSubsystem ess) {
		super(name, port);
		this.ess = ess;
		exit = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected DatagramPacket createReplyPacketGivenRequestPacket(DatagramPacket requestPacket) {
		//System.out.println("\n\n\n***************MADE IT INTO THE ELEVATOR SCHEDULER RECEIVER****************\n\n\n");
		try {
			Object obj = Util.deserialize(requestPacket.getData());
			nextFloorHashMap  = (SchedulerInfo) obj;
		}catch(IOException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		//Create packet to reply with. Then send
        byte[] replyData = "200 OK".getBytes();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, requestPacket.getAddress(), requestPacket.getPort());
		return replyPacket;
	}

	
	public void exit() {
		exit = true;
	}
	
	@Override
	public void run(){
		System.out.println("Starting " + this.name + "...");
		while (!exit) {
			this.ess.log("waiting for next request");
			this.sendReply(this.createReplyPacketGivenRequestPacket(this.receiveNextPacket()));
			this.ess.updateElevators(this.nextFloorHashMap);
			nextFloorHashMap = null;
			this.ess.sendUpdateToScheduler();
		}
		this.closeSocket();
	}
}
