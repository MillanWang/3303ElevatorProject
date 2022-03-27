package app.Scheduler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import app.Logger;
import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.FloorSubsystem.*;
import app.Scheduler.SchedulerThreads.DelayedRequest;
import app.Scheduler.SchedulerThreads.Scheduler_ElevatorSubsystemPacketReceiver;
import app.Scheduler.SchedulerThreads.Scheduler_FloorSubsystemPacketReceiver;
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
	public int floorSubsystemReceivePort;
	private InetAddress floorSubsystemInetAddress;
	private int floorSubsystemSendPort;
	private InetAddress elevatorSubsystemInetAddress;
	private int elevatorSubsystemSendPort;
	
	private Logger logger;
	private Config config;
	

	/**
	 * Constructor for scheduler class
	 * 
	 * @param highestFloorNumber highest floor number
	 * @param skipDelaysOnFloorInputs boolean indicating if all incoming timeSpecified requests should be ran without delay
	 * @param floorSubsys Reference to the floor subsystem dependency
	 */
	public Scheduler(Logger logger, Config config) {
		this.config=config;
		this.highestFloorNumber= this.config.getInt("floor.highestFloorNumber"); 
		this.elevatorSpecificSchedulerManager = new ElevatorSpecificSchedulerManager(USE_SIMPLE_LEAST_LOAD_ALGORITHM); 
		this.elevatorSubsystemReceivePort = this.config.getInt("scheduler.elevatorReceivePort");
		this.floorSubsystemReceivePort = this.config.getInt("scheduler.floorReceivePort");
		this.elevatorSubsystemSendPort = this.config.getInt("elevator.port");
		
		try {
			this.floorSubsystemInetAddress = InetAddress.getByName(this.config.getString("floor.schedulerReceivePort"));
			this.elevatorSubsystemInetAddress = InetAddress.getByName(this.config.getString("elevator.address"));
		} catch (UnknownHostException e) {e.printStackTrace();}
		this.floorSubsystemSendPort = this.config.getInt("scheduler.elevatorReceivePort");
		
		
		this.logger = logger;
		
		this.skipDelaysOnFloorInputs= this.config.getInt("scheduler.skipDelaysOnFloorInputs")==1; 
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
			Integer requestType = ser.getRequestType();
			
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
		LinkedList<ElevatorInfo> allElevatorInfos = this.elevatorSpecificSchedulerManager.getMostRecentAllElevatorInfo();
		//First request, need to initialize elevators
		if (allElevatorInfos==null) {
			int numElevators = this.config.getInt("elevator.total.number");
			allElevatorInfos= new LinkedList<ElevatorInfo>();
			for(int i = 0; i < numElevators; i++) {
				allElevatorInfos.add(new ElevatorInfo(i+1, 1,  null, Direction.UP));
			}
		}
		this.sendNextPacket_elevatorSpecificNextFloor(allElevatorInfos);
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
		DatagramPacket packetToSend = new DatagramPacket(serializedListOfElevatorInfo, serializedListOfElevatorInfo.length, this.floorSubsystemInetAddress, this.floorSubsystemSendPort);
		Util.sendRequest_ReturnReply(packetToSend);
	}

	
	
	/**
	 * Sends 
	 * @param allElevatorInfos
	 */
	public synchronized void sendNextPacket_elevatorSpecificNextFloor(LinkedList<ElevatorInfo> allElevatorInfos) {
        //Create byte array to build reply packet contents more easily
        ByteArrayOutputStream packetMessageOutputStream = new ByteArrayOutputStream();
		try {
			packetMessageOutputStream.write(Util.serialize(getNextFloorsToVisit(allElevatorInfos)));
		} catch (IOException e) {e.printStackTrace();}
        //Create packet to reply with. Then send
        byte[] replyData = packetMessageOutputStream.toByteArray();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, elevatorSubsystemInetAddress, elevatorSubsystemSendPort);
		Util.sendRequest_ReturnReply(replyPacket);
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
		Scheduler_FloorSubsystemPacketReceiver fssReceiver = new Scheduler_FloorSubsystemPacketReceiver( this.floorSubsystemReceivePort, this);
		Scheduler_ElevatorSubsystemPacketReceiver essReceiver = new Scheduler_ElevatorSubsystemPacketReceiver(this.elevatorSubsystemReceivePort, this);
		(new Thread(fssReceiver, "Scheduler_FloorSubsystemPacketReceiver")).start();
		(new Thread(essReceiver, "Scheduler_ElevatorSubsystemPacketReceiver")).start();
	}
	
	public static void main(String[] args) {
		Config config = new Config("multi.properties");
//		Config config = new Config("local.properties");
		Scheduler scheduler = new Scheduler(new Logger(config), config);
		(new Thread(scheduler, "Scheduler")).start();
	}
}
