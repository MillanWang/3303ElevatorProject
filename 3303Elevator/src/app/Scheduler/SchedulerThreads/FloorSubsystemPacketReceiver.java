package app.Scheduler.SchedulerThreads;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

import app.FloorSubsystem.ScheduledElevatorRequest;
import app.Scheduler.Scheduler;
import app.UDP.PacketReceiver;

/**
 * Class for receiving packets from the floor subsystem and replying with acknowledgments
 * 
 * @author Millan Wang
 *
 */
public class FloorSubsystemPacketReceiver extends PacketReceiver {
	private Scheduler scheduler;

	public FloorSubsystemPacketReceiver(int port, Scheduler scheduler) {
		super("FloorSubsystemPacketReceiver", port);
		this.scheduler = scheduler;
	}

	@Override
	protected void receiveNextPacket_sendReply() {
		//Receive packet. Happens in PacketReceiver
        DatagramPacket receivedPacket = this.receiveNextPacket();
        
        
        
        
        //Create byte array to build reply packet contents more easily
        ByteArrayOutputStream packetMessageOutputStream = new ByteArrayOutputStream();
        
        //Get contents of packet. Attempt to deserialize to an object and call scheduler
        String packetDataString = new String(receivedPacket.getData());

        //Set the appropriate reply message in the packet
        if (packetDataString.equals("PLACEHOLDER HERE NEED TO DESERIALIZE AND CHECK IF OK")) { //TODO: NEED TO ATTEMPT TO DESERIALIZE
        	//Deserialization successful. Add to scheduler
        	this.scheduler.floorSystemScheduleRequest(new ScheduledElevatorRequest(null,1,true,2)); //SWAP WITH DESERIALIZED VERSION ASAP
        	try {
				packetMessageOutputStream.write("200 OK".getBytes());
			} catch (IOException e) {e.printStackTrace();}
        	
        } else {
        	//Deserialization unsuccessful. Reply indicating this
        	try {
				packetMessageOutputStream.write("500 Cannot deserialize ScheduledElevatorRequest".getBytes());
			} catch (IOException e) {e.printStackTrace();}
        }
        

        
        
        //Create packet to reply with. Then send
        byte[] replyData = packetMessageOutputStream.toByteArray();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, receivedPacket.getAddress(), receivedPacket.getPort());
        this.sendReply(replyPacket);

	}

}
