package app.Scheduler.SchedulerThreads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.LinkedList;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
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
	@SuppressWarnings("unchecked")
	@Override
	protected DatagramPacket createReplyPacketGivenRequestPacket(DatagramPacket requestPacket) {
		System.out.println("[Scheduler_ElevatorSubsystemPacketReceiver] : Received packet from elevator subsystem");
        //De-serialize packet contents to become input for scheduler's next floors to visit
        try {
        	mostRecentElevatorInfo = (LinkedList<ElevatorInfo>) Util.deserialize(requestPacket.getData());
		} catch (ClassNotFoundException | IOException e1) {e1.printStackTrace();}

        //Create packet to reply with. Then send
        byte[] replyData = "200 OK".getBytes();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, requestPacket.getAddress(), requestPacket.getPort());
		return replyPacket;
	}
	
	/**
	 * Runs the Scheduler_ElevatorSubsystemPacketReceiver thread
	 */
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
