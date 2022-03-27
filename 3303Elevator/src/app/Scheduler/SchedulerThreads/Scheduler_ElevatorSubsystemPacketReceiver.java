package app.Scheduler.SchedulerThreads;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.Scheduler.ElevatorSpecificScheduler;
import app.Scheduler.Scheduler;
import app.UDP.PacketReceiver;
import app.UDP.Util;

/**
 * Class for receiving packets from elevator subsystem and then replying with responses
 * 
 * @author Millan Wang
 *
 */
public class Scheduler_ElevatorSubsystemPacketReceiver extends PacketReceiver {

	/**
	 * The scheduler to get next instructions
	 */
	private Scheduler scheduler;
	private LinkedList<ElevatorInfo> mostRecentElevatorInfo;
	
	/**
	 * Constructor for the ElevaorSubsystemPacketReceiver class
	 * @param port Port to be used as the receive port
	 * @param scheduler to contact to get instructions
	 */
	public Scheduler_ElevatorSubsystemPacketReceiver(int port, Scheduler scheduler) {
		super("ElevatorSubsystemPacketReceiver", port);
		this.scheduler = scheduler;
	}

	/**
	 * Creates a reply packet given a request packet
	 */
	@Override
	protected DatagramPacket createReplyPacketGivenRequestPacket(DatagramPacket requestPacket) {
		System.out.println("[Scheduler_ElevatorSubsystemPacketReceiver] : Received packet from elevator subsystem");
        //De-serialize packet contents to become input for scheduler's next floors to visit
		LinkedList<ElevatorInfo> elevatorSubsystemComms = null;
        try {
        	mostRecentElevatorInfo = (LinkedList<ElevatorInfo>) Util.deserialize(requestPacket.getData());
		} catch (ClassNotFoundException | IOException e1) {e1.printStackTrace();}
       
        

        
//        //Create byte array to build reply packet contents more easily
//        ByteArrayOutputStream packetMessageOutputStream = new ByteArrayOutputStream();
//        
//        //Write serialized response object to packet
//        try {
//			packetMessageOutputStream.write(Util.serialize(allElevatorsAllFloorsToVisit));
//		} catch (IOException e) {e.printStackTrace();}
//        

        //Create packet to reply with. Then send
        byte[] replyData = "200 OK".getBytes();//packetMessageOutputStream.toByteArray();//
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, requestPacket.getAddress(), requestPacket.getPort());
		return replyPacket;
	}
	
	@Override
	public void run(){
		System.out.println("Starting " + this.name + "...");
		while (true) {
			this.sendReply(this.createReplyPacketGivenRequestPacket(this.receiveNextPacket()));
			//Get the next floor to visit for each elevator from scheduler
	        this.scheduler.sendNextPacket_elevatorSpecificNextFloor(mostRecentElevatorInfo);
		}
	}

}
