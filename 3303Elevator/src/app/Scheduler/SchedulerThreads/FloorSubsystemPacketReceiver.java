package app.Scheduler.SchedulerThreads;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

import app.FloorSubsystem.ScheduledElevatorRequest;
import app.Scheduler.Scheduler;
import app.UDP.PacketReceiver;
import app.UDP.Util;

/**
 * Class for receiving packets from the floor subsystem and replying with acknowledgments
 * 
 * @author Millan Wang
 *
 */
public class FloorSubsystemPacketReceiver extends PacketReceiver {
	/**
	 * The scheduler to get next instructions
	 */
	private Scheduler scheduler;

	/**
	 * Constructor for the FloorSubsystemPacketReceiver class
	 * @param port The port to be used to receive on the DatagramSocket
	 * @param scheduler The scheduler to send requests to 
	 */
	public FloorSubsystemPacketReceiver(int port, Scheduler scheduler) {
		super("FloorSubsystemPacketReceiver", port);
		this.scheduler = scheduler;
	}

	/**
	 * Creates a reply packet given a request packet
	 */
	@Override
	protected DatagramPacket createReplyPacketGivenRequestPacket(DatagramPacket requestPacket) {
        //Create byte array to build reply packet contents more easily
        ByteArrayOutputStream packetMessageOutputStream = new ByteArrayOutputStream();

        //Set the appropriate reply message in the packet based on the deserialization attempt
        try {
			ScheduledElevatorRequest requestObj = (ScheduledElevatorRequest) Util.deserialize(requestPacket.getData());
			//Deserialization successful. Add to scheduler
        	this.scheduler.floorSystemScheduleRequest(new ScheduledElevatorRequest(null,1,true,2)); //SWAP WITH DESERIALIZED VERSION ASAP
        	try {
				packetMessageOutputStream.write("200 OK".getBytes());
			} catch (IOException e) {e.printStackTrace();}
			
        	
        	
		} catch (ClassNotFoundException | IOException e1) {
			//DESERIALIZATION FAILED - Reply indicating this
        	try {
				packetMessageOutputStream.write("500 Cannot deserialize ScheduledElevatorRequest".getBytes());
			} catch (IOException e) {e.printStackTrace();}
			e1.printStackTrace();
		}
        
        
        //Create packet to reply with. Then send
        byte[] replyData = packetMessageOutputStream.toByteArray();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, requestPacket.getAddress(), requestPacket.getPort());
		return replyPacket;
	}

}
