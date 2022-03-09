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

	private Scheduler scheduler;
	
	public ElevatorSubsystemPacketReceiver(int port, Scheduler scheduler) {
		super("ElevatorSubsystemPacketReceiver", port);
		this.scheduler = scheduler;
	}

	@Override
	protected void receiveNextPacket_sendReply() {
		//Receive packet. Happens in PacketReceiver
        DatagramPacket receivedPacket = this.receiveNextPacket();
        
        
        
        //Deserialize packet contents to become input for scheduler's next floors to visit
        Object elevatorSubsystemComms = new String(receivedPacket.getData()); //TODO: Plan out a comms object and then Deserialize 
        
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
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, receivedPacket.getAddress(), receivedPacket.getPort());
        this.sendReply(replyPacket);
	}

}
