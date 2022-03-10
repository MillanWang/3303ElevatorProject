package app.Scheduler.SchedulerThreads;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

import app.Scheduler.Scheduler;
import app.UDP.PacketReceiver;

/**
 * Class for receiving packets from elevator subsystem and then replying with responses
 * 
 * @author Millan Wang
 *
 */
public class ElevatorSubsystemPacketReceiver extends PacketReceiver {

	/**
	 * The scheduler to get next instructions
	 */
	private Scheduler scheduler;
	
	/**
	 * Constructor for the ElevaorSubsystemPacketReceiver class
	 * @param port Port to be used as the receive port
	 * @param scheduler to contact to get instructions
	 */
	public ElevatorSubsystemPacketReceiver(int port, Scheduler scheduler) {
		super("ElevatorSubsystemPacketReceiver", port);
		this.scheduler = scheduler;
	}

	/**
	 * Creates a reply packet given a request packet
	 */
	@Override
	protected DatagramPacket createReplyPacketGivenRequestPacket(DatagramPacket requestPacket) {
        //Deserialize packet contents to become input for scheduler's next floors to visit
        Object elevatorSubsystemComms = new String(requestPacket.getData()); //TODO: Plan out a comms object and then Deserialize 
        
        //Get the next toVisitList from scheduler
        Object schedulerUpdate = this.scheduler.getNextFloorsToVisit(1, true);
        
        //Create byte array to build reply packet contents more easily
        ByteArrayOutputStream packetMessageOutputStream = new ByteArrayOutputStream();
        
        //Write serialized response object to packet
        try {
        	//TODO: Replace toString with a serialization process
			packetMessageOutputStream.write(schedulerUpdate.toString().getBytes());
		} catch (IOException e) {e.printStackTrace();}
        

        
        //Create packet to reply with. Then send
        byte[] replyData = packetMessageOutputStream.toByteArray();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, requestPacket.getAddress(), requestPacket.getPort());
		return replyPacket;
	}

}
