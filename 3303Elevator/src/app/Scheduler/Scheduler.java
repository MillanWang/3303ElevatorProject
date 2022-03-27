package app.Scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import app.Logger;
import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;
import app.FloorSubsystem.*;
import app.Scheduler.SchedulerThreads.DelayedRequest;
import app.Scheduler.SchedulerThreads.ElevatorSubsystemPacketReceiver;
import app.Scheduler.SchedulerThreads.FloorSubsystemPacketReceiver;
import app.UDP.Util;

/**
 * Scheduler class coordinating requests from the FloorSubsystem into directions for the ElevatorSubsytem 
 * 
 * @author Millan Wang
 *
 */
public class Scheduler implements Runnable{
	
	public static final boolean USE_SIMPLE_LEAST_LOAD_ALGORITHM = true;
	
	private boolean skipDelaysOnFloorInputs;
	private ElevatorSpecificSchedulerManager elevatorSpecificSchedulerManager;
	private int highestFloorNumber;
	
	private int elevatorSubsystemReceivePort;
	private int floorSubsystemReceivePort;
	private InetAddress floorInetAddress;
	private int floorSubsystemSendPort;

	
	private Logger logger;
	

	/**
	 * Constructor for scheduler lass
	 * 
	 * @param highestFloorNumber highest floor number
	 * @param skipDelaysOnFloorInputs boolean indicating if all incoming timeSpecified requests should be ran without delay
	 * @param floorSubsys Reference to the floor subsystem dependency
	 */
	public Scheduler(Logger logger, Config config) {
		this.highestFloorNumber= config.getInt("floor.highestFloorNumber"); 
		this.elevatorSpecificSchedulerManager = new ElevatorSpecificSchedulerManager(USE_SIMPLE_LEAST_LOAD_ALGORITHM); 
		this.elevatorSubsystemReceivePort = config.getInt("scheduler.elevatorReceivePort");
		this.floorSubsystemReceivePort = config.getInt("scheduler.floorReceivePort");
		
		try {
			this.floorInetAddress = InetAddress.getByName(config.getString("floor.schedulerReceivePort"));
		} catch (UnknownHostException e) {e.printStackTrace();}
		this.floorSubsystemSendPort = config.getInt("scheduler.elevatorReceivePort");
		
		
		this.logger = logger;
		
		this.skipDelaysOnFloorInputs= config.getInt("scheduler.skipDelaysOnFloorInputs")==1; 
	}
	
	
	/**
	 * Schedules an incoming floorSystemRequest to corresponding directional floor queue
	 * @param floorSystemRequests List of ScheduledElevatorRequest
	 */
	public synchronized void floorSystemScheduleRequest(List<ScheduledElevatorRequest> floorSystemRequests) {
		this.logger.logSchedulerEvent("Scheduler received request(s) from floor system");
		
		for (ScheduledElevatorRequest ser : floorSystemRequests) {
			
			//Assuming sanitized inputs
			Integer startFloor = ser.getStartFloor();
			Integer destinationFloor = ser.getDestinationFloor();
			Integer requestType = 0; // ser.getRequestType(); TODO : Make this retrieve the request type from the obj
			
			if (startFloor > highestFloorNumber ||destinationFloor > highestFloorNumber || startFloor <= 0 || destinationFloor <= 0) {
				System.err.println("Non existent floor received");
				return;
			}

			if (skipDelaysOnFloorInputs || ser.getMillisecondDelay()==0) {
				//No delay means instantly add request to queue
				this.addElevatorRequest(startFloor, destinationFloor, requestType );
			} else {
				//Create a thread with a delay that eventually calls addsElevatorRequest
				(new Thread(new DelayedRequest(this,startFloor, destinationFloor,requestType, ser.getMillisecondDelay()), "RequestOccuringAt_"+ser.getTime().toString())).start();
			}
			
		}
	}
	

	
	/**
	 * Adds an elevator request to the scheduling system so that an elevator have instructions on where to go
	 * 
	 * @param startFloor Starting floor of the request
	 * @param destinationFloor destination floor of the request
	 */
	public synchronized void addElevatorRequest(Integer startFloor, Integer destinationFloor, Integer requestType) {
		int requestReceiverElevatorID = this.elevatorSpecificSchedulerManager.scheduleFloorRequest(startFloor, destinationFloor, requestType);
		if (requestReceiverElevatorID<=0) {
			this.logger.logSchedulerEvent("Unable to schedule floor request "+startFloor+"->"+destinationFloor);
		}else {
			this.logger.logSchedulerEvent("Elevator "+requestReceiverElevatorID+" has been scheduled go handle floor request "+startFloor+"->"+destinationFloor);
		}
		this.logger.logSchedulerEvent(this.elevatorSpecificSchedulerManager.toString());
	}

	/**
	 * TODO : Assess if this is needed. With the future GUI interfacing, it'll make more sense to send comms there instead of to floor
	 * Sends all elevator's info to the floor subsystem via UDP packet
	 * 
	 * @param listOfElevatorInfo
	 */
	private synchronized void sendUpdateToFloorSubsystem() {
		byte[] serializedListOfElevatorInfo = null;
		try {
			serializedListOfElevatorInfo = Util.serialize(this.elevatorSpecificSchedulerManager.getMostRecentAllElevatorInfo());
		} catch (IOException e) {}
		DatagramPacket packetToSend = new DatagramPacket(serializedListOfElevatorInfo, serializedListOfElevatorInfo.length, this.floorInetAddress, this.floorSubsystemSendPort);
		Util.sendRequest_ReturnReply(packetToSend);
	}

	/**
	 * Returns a HashMap of ElevatorID:nextFloorToVisit for each elevator
	 * 
	 * @param allElevatorInfo Linked list of all ElevatorInfo objects
	 * @return TreeSet of the remaining floors to visit in this direction
	 */
	public synchronized HashMap<Integer,Integer> getNextFloorsToVisit(LinkedList<ElevatorInfo> allElevatorInfos) {
		return this.elevatorSpecificSchedulerManager.getAllElevatorsNextFloorToVisit(allElevatorInfos);
	}
	

	@Override
	public void run() {
		// Create message receiver threads for messages from floor subsystem and elevator subsystem
		FloorSubsystemPacketReceiver fssReceiver = new FloorSubsystemPacketReceiver( this.floorSubsystemReceivePort, this);
		ElevatorSubsystemPacketReceiver essReceiver = new ElevatorSubsystemPacketReceiver(this.elevatorSubsystemReceivePort, this);
		(new Thread(fssReceiver, "FloorSubsystemPacketReceiver")).start();
		(new Thread(essReceiver, "ElevatorSubsystemPacketReceiver")).start();
	}
	
	public static void main(String[] args) {
		Config config = new Config("multi.properties");
//		Config config = new Config("local.properties");
		Scheduler scheduler = new Scheduler(new Logger(config), config);
		(new Thread(scheduler, "Scheduler")).start();
	}
}
