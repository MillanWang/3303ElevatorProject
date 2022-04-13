package app.FloorSubsystem.FloorSubsystemThreads;

import app.UDP.PacketReceiver;
import app.UDP.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.LinkedList;

import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.FloorSubsystem.*;
import app.Scheduler.ElevatorSpecificScheduler;

public class FloorSubsystem_SchedulerPacketReceiver extends PacketReceiver {
	
	private FloorSubsystem FloorSubsystem;
	
	public FloorSubsystem_SchedulerPacketReceiver(FloorSubsystem FloorSubsystem, int port) {
		super("SchedulerPacketReceiver", port);
		this.FloorSubsystem = FloorSubsystem;
	}

	@Override
	protected DatagramPacket createReplyPacketGivenRequestPacket(DatagramPacket requestPacket) {
		//Deserialize packet contents to become input for scheduler's next floors to visit
		//Object SchedulerSubsystemcomms
    	LinkedList<ElevatorInfo> schedulerSubsystemcomms = null;
		try {
			schedulerSubsystemcomms = (LinkedList<ElevatorInfo>) Util.deserialize(requestPacket.getData());
			
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		} //this is going to be the list of elevatorInfo
		this.FloorSubsystem.addElevatorInfo(schedulerSubsystemcomms);
        
        
        //Create byte array to build reply packet contents more easily
        ByteArrayOutputStream packetMessageOutputStream = new ByteArrayOutputStream();
        
        try {
			packetMessageOutputStream.write("200 OK".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
        

        
        //Create packet to reply with. Then send
        byte[] replyData = packetMessageOutputStream.toByteArray();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, requestPacket.getAddress(), requestPacket.getPort());
		return replyPacket;
	}

}
