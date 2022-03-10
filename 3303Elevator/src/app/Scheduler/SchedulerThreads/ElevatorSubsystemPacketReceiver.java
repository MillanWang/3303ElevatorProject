package app.Scheduler.SchedulerThreads;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.LinkedList;

import app.Scheduler.ElevatorSpecificFloorsToVisit;
import app.Scheduler.Scheduler;
import app.UDP.PacketReceiver;
import app.UDP.Util;

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
        try {
			Object elevatorSubsystemComms = Util.deserialize(requestPacket.getData()); //TODO : GET THE PROPER CLASS FOR THIS DESERIALIZATION RESULT
		} catch (ClassNotFoundException | IOException e1) {e1.printStackTrace();}
        
        //Get the next toVisitList from scheduler   TODO:SETUP SCHEDULER TO PROPERLY RETURN THE TO VISIT LIST
        Object eventuallyReplaceWithProperSchedulerImplementation = this.scheduler.getNextFloorsToVisit(1, true);
        LinkedList<ElevatorSpecificFloorsToVisit> allElevatorsAllFloorsToVisit = new LinkedList<ElevatorSpecificFloorsToVisit>();
        
        //Create byte array to build reply packet contents more easily
        ByteArrayOutputStream packetMessageOutputStream = new ByteArrayOutputStream();
        
        //Write serialized response object to packet
        try {
			packetMessageOutputStream.write(Util.serialize(allElevatorsAllFloorsToVisit));
		} catch (IOException e) {e.printStackTrace();}
        

        
        //Create packet to reply with. Then send
        byte[] replyData = packetMessageOutputStream.toByteArray();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, requestPacket.getAddress(), requestPacket.getPort());
		return replyPacket;
	}

}
