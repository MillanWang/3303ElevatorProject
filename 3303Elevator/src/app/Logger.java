/**
 * Elevator project 
 * The logger class is the object that tracks the system time and the elevator status throughout the system operations 
 * 
 * @author petertanyous
 * #ID 101127203 
 */
package app;

import java.time.LocalTime;
import java.util.LinkedList;

import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.FloorSubsystem.ScheduledElevatorRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import app.UDP.PacketReceiver;
import app.UDP.Util;

public class Logger extends Util{

	private LocalTime dateTime; 
	private boolean printElevatorEvents;
	private boolean printSchedulerEvents; 
	private boolean printFloorEvents; 
	private boolean printTimeManagementSystemEvent; 
	protected Config config;
	private InetSocketAddress sLoggerAddr;
	/**
	 * Constructor selects the events types to be printed through the boolean parameters  
	 * 
	 * @param c Config object to configure all boolean flags for class and get addresses
	 * 
	 */
	public Logger(Config c) {
		this.printElevatorEvents = c.getBoolean("elevator.log");
		this.printSchedulerEvents = c.getBoolean("scheduler.log"); 
		this.printFloorEvents = c.getBoolean("floor.log"); 
		this.printTimeManagementSystemEvent = c.getBoolean("time.log");
		this.config = c;
		try {
			this.sLoggerAddr = new InetSocketAddress(config.getString("logger.address"), config.getInt("logger.port"));
		}catch(Exception e) {
			System.exit(1);
		}
	}
	
	/**
	 * logElevatorEvents is used to print the elevator events logged
	 * 
	 * @param message message to be logged by system
	 */
	public void logElevatorEvents(String message) {
		if(printElevatorEvents == true) {
			String m = "[" + dateTime.now() + "]" + message;
			System.out.println(m);
			if(!sLoggerAddr.getAddress().toString().contains("localhost")) { //if live, send to server logger
				DatagramPacket packet = buildLoggerPacket(m);
				Util.sendRequest_ReturnReply(packet);
			}
		}
	}
	
	/**
	 * logFloorEvent is used to print the floorsubsystem events logged
	 * 
	 * @param request: the request logged at the floor subsystem 
	 * 
	 */
	public void logFloorEvent(ScheduledElevatorRequest request) {
		if(printFloorEvents == true) {
			String m = (dateTime.now()+ " floor number " +  request.getStartFloor() + " logged a floor request at time " +request.getTime() + "  to floor " +request.getDestinationFloor());
			System.out.println(m);
			if(!sLoggerAddr.getAddress().toString().contains("localhost")) { //if live, send to server logger
				DatagramPacket packet = buildLoggerPacket(m);
				Util.sendRequest_ReturnReply(packet);
			}
		}
		
	}
	
	/**
	 * logSchedulerEvent is used to print scheduler events logged 
	 * 
	 * @param schedulerLogString string to be printed from the scheduler (Temp)
	 */
	public void logSchedulerEvent(String schedulerLogString) {
		if(printSchedulerEvents == true) {
			String m = (dateTime.now()+ " " + schedulerLogString);
			System.out.println(m);
			if(!sLoggerAddr.getAddress().toString().contains("localhost")) { //if live, send to server logger
				DatagramPacket packet = buildLoggerPacket(m);
				Util.sendRequest_ReturnReply(packet);
			}
		}
	}
	
	/**
	 * logTimeManagementEvent is used to print the Time Management System events logged
	 * 
	 * @param temp string to be printed from the Time Management System (Temp)
	 */
	public void logTimeManagementSystemEvent(String timeManagementLogString) {
		if(printTimeManagementSystemEvent == true) {
			String m = (dateTime.now() + " " + timeManagementLogString);
			System.out.println(m);
			if(!sLoggerAddr.getAddress().toString().contains("localhost")) { //if live, send to server logger
				DatagramPacket packet = buildLoggerPacket(m);
				Util.sendRequest_ReturnReply(packet);
			}
		}
	}
	
	/**
	 * Builds datagram packet to be sent to server logger
	 * @param m message to be sent to server logger
	 * @return datagram packet of message
	 */
	private DatagramPacket buildLoggerPacket(String m){
		byte[] data = {};
		
		try {
			data = Util.serialize(m);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return new DatagramPacket(
				data,
				data.length,
				sLoggerAddr.getAddress(),
				sLoggerAddr.getPort()
		);
	}
}
