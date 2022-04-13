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
import app.GUI.GUIUpdateInfo;
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
	/**
	 * Feature flag to choose which algorithm to use for distributing floor requests
	 */
	public static final boolean USE_SIMPLE_LEAST_LOAD_ALGORITHM = true;
	
	/**
	 * Feature flag for instantly scheduling all incoming floor requests
	 */
	private boolean skipDelaysOnFloorInputs;
	
	/**
	 * ElevatorSpecificSchedulerManager that manages the individual elevator scheduling
	 */
	private ElevatorSpecificSchedulerManager elevatorSpecificSchedulerManager;
	
	/**
	 * The highest floor number represented by the system
	 */
	private int highestFloorNumber;
	
	/**
	 * The previous nextFloorHashMap used to make sure that repeat hashmaps are not sent
	 */
	private HashMap<Integer,Integer> previousNextFloorHashMap;
	
	/**
	 * UDP Communication connection fields
	 */
	private int elevatorSubsystemReceivePort;
	public int floorSubsystemReceivePort;
	private InetAddress elevatorSubsystemInetAddress;
	private int elevatorSubsystemSendPort;
	private InetAddress guiSubsystemInetAddress;
	private int guiSubsystemSendPort;
	
	/**
	 * Local logger reference to track updates
	 */
	private Logger logger;
	
	/**
	 * Launch context dependent configuration data
	 */
	private Config config;
	
	/**
	 * Fields used to track execution time for addressing all floor requests
	 */
	private long startTime; //long is 0 by default, no initialization needed  
	private long endTime; //long is 0 by default, no initialization needed 
	private long timeElapsed; //long is 0 by default, no initialization needed 
	
	
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
		this.guiSubsystemSendPort = this.config.getInt("gui.port");
		
		try {
			this.elevatorSubsystemInetAddress = InetAddress.getByName(this.config.getString("elevator.address"));
			this.guiSubsystemInetAddress = InetAddress.getByName(this.config.getString("gui.address"));
		} catch (UnknownHostException e) {e.printStackTrace();}
		
		
		this.logger = logger;
		
		this.skipDelaysOnFloorInputs= this.config.getInt("scheduler.skipDelaysOnFloorInputs")==1;
	}
	
	
	/**
	 * Schedules an incoming floorSystemRequest to corresponding directional floor queue
	 * @param floorSystemRequests List of ScheduledElevatorRequest
	 */
	public synchronized void floorSystemScheduleRequest(List<ScheduledElevatorRequest> floorSystemRequests) {
		this.logger.logSchedulerEvent("Scheduler received request(s) from floor system");
		
		//Start time measurement
		if(startTime == 0) {
			startTime = System.currentTimeMillis();
		}
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
				allElevatorInfos.add(new ElevatorInfo(i+1, 1, -1, null, Direction.UP));
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
			this.logger.logSchedulerEvent("Elevator "+requestReceiverElevatorID+" has been scheduled go handle floor request "+startFloor+"->"+destinationFloor+ ". This is a " +(requestType==0?"Normal":((requestType==1?"Temporary Error":"PERMANENT ERROR")))+" request");
		}
		this.logger.logSchedulerEvent(this.elevatorSpecificSchedulerManager.toString());
	}
	
	
	/**
	 * Sends next communication object to the elevator subsystem
	 * @param allElevatorInfos
	 */
	public synchronized void sendNextPacket_elevatorSpecificNextFloor(LinkedList<ElevatorInfo> allElevatorInfos) {
		
		HashMap<Integer,Integer> currentNextFloorsHashMap = getNextFloorsToVisit(allElevatorInfos);
		if (currentNextFloorsHashMap.equals(previousNextFloorHashMap)) {
			if(elevatorSpecificSchedulerManager.getTotalActiveNumberOfStopsCount() == 0 && startTime != 0) {
				endTime = System.currentTimeMillis();
				timeElapsed = endTime - startTime;
				logger.logTimeMeasurements("System took "+ timeElapsed + " milliseconds to handle all requests on the input file");
				startTime = 0;
				endTime = 0;
				timeElapsed = 0;
				
			}
			//Don't send repeats more than once
			return;
		} else {
			this.previousNextFloorHashMap=currentNextFloorsHashMap;
		}
		
		SchedulerInfo schedulerInfo = new SchedulerInfo(currentNextFloorsHashMap, this.elevatorSpecificSchedulerManager.getElevatorErrorMap());
		
		
        //Create byte array to build reply packet contents more easily
        ByteArrayOutputStream packetMessageOutputStream = new ByteArrayOutputStream();
		try {
			packetMessageOutputStream.write(Util.serialize(schedulerInfo));
		} catch (IOException e) {e.printStackTrace();}
        //Create packet to reply with. Then send
        byte[] replyData = packetMessageOutputStream.toByteArray();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, elevatorSubsystemInetAddress, elevatorSubsystemSendPort);
        
        
        this.logger.logSchedulerEvent(this.elevatorSpecificSchedulerManager.toString());
        this.logger.logSchedulerEvent("[Scheduler] : About to send nextFloorToVisitHashMap : " + currentNextFloorsHashMap.toString());
		
        
        Util.sendRequest_ReturnReply(replyPacket);
        sendNextPacket_UpdateGUI();
	}
	
	/**
	 * Sends a packet containing the current scheduler information to the GUI Subsystem to update the view
	 */
	private synchronized void sendNextPacket_UpdateGUI() {
		GUIUpdateInfo guiInfo = this.elevatorSpecificSchedulerManager.createGUIUpdate();
		 //Create byte array to build reply packet contents more easily
        ByteArrayOutputStream packetMessageOutputStream = new ByteArrayOutputStream();
		try {
			packetMessageOutputStream.write(Util.serialize(guiInfo));
		} catch (IOException e) {e.printStackTrace();}
        //Create packet to reply with. Then send
        byte[] replyData = packetMessageOutputStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(replyData, replyData.length,this.guiSubsystemInetAddress, this.guiSubsystemSendPort);
        Util.sendRequest_NoReply(packet);
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
	
	/**
	 * Run method to start the scheduler subsystem as a thread
	 */
	@Override
	public void run() {
		// Create message receiver threads for messages from floor subsystem and elevator subsystem
		Scheduler_FloorSubsystemPacketReceiver fssReceiver = new Scheduler_FloorSubsystemPacketReceiver( this.floorSubsystemReceivePort, this);
		Scheduler_ElevatorSubsystemPacketReceiver essReceiver = new Scheduler_ElevatorSubsystemPacketReceiver(this.elevatorSubsystemReceivePort, this);
		(new Thread(fssReceiver, "Scheduler_FloorSubsystemPacketReceiver")).start();
		(new Thread(essReceiver, "Scheduler_ElevatorSubsystemPacketReceiver")).start();
	}
	
	/**
	 * Main method for starting the scheduler subsystem as a standalone 
	 * @param args
	 */
	public static void main(String[] args) {
		Config config = new Config("multi.properties");
//		Config config = new Config("local.properties");
		Scheduler scheduler = new Scheduler(new Logger(config), config);
		(new Thread(scheduler, "Scheduler")).start();
	}
}
