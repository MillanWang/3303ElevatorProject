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
 * SYSC 3303, Final Project Iteration 2
 * Scheduler.java
 * 
 * Scheduler class coordinating requests from the FloorSubsystem into directions for the ElevatorSubsytem 
 * 
 * @author Millan Wang
 *
 */
public class Scheduler implements Runnable{
	
	private LinkedList<ElevatorSpecificFloorsToVisit> allElevatorsAllFloorsToVisit;
	private LinkedList<TreeSet<Integer>> upwardsDestinationsPerFloor;
	private LinkedList<TreeSet<Integer>> downwardsDestinationsPerFloor;
	private LinkedList<ElevatorInfo> allElevatorInfo;

	private boolean skipDelaysOnFloorInputs;
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
		this.highestFloorNumber= config.getInt("floor.highestFloorNumber"); ; 
		this.elevatorSubsystemReceivePort = config.getInt("scheduler.elevatorReceivePort");
		this.floorSubsystemReceivePort = config.getInt("scheduler.floorReceivePort");
		
		try {
			this.floorInetAddress = InetAddress.getByName(config.getString("floor.schedulerReceivePort"));
		} catch (UnknownHostException e) {e.printStackTrace();}
		this.floorSubsystemSendPort = config.getInt("scheduler.elevatorReceivePort");
		
		
		this.logger = logger;
		
		this.skipDelaysOnFloorInputs= config.getInt("scheduler.skipDelaysOnFloorInputs")==1; 
		
		//Directional destinations per floor
		this.upwardsDestinationsPerFloor= new LinkedList<TreeSet<Integer>>();
		this.downwardsDestinationsPerFloor = new LinkedList<TreeSet<Integer>>();
		//Populate them with TreeSets
		for (int i = 0; i<highestFloorNumber ; i++) {
			this.upwardsDestinationsPerFloor.add(new TreeSet<Integer>());
			this.downwardsDestinationsPerFloor.add(new TreeSet<Integer>());
		}
		
		this.allElevatorsAllFloorsToVisit = new LinkedList<ElevatorSpecificFloorsToVisit>();
	}
	
	
	/**
	 * Schedules an incoming floorSystemRequest to corresponding directional floor queue
	 * @param floorSystemRequests List of ScheduledElevatorRequest
	 */
	public synchronized void floorSystemScheduleRequest(List<ScheduledElevatorRequest> floorSystemRequests) {
		this.logger.logSchedulerEvent("Scheduler received request(s) from floor system");
		//Wait loop until elevator info is known
		while (this.allElevatorInfo==null) {
			try {wait();} catch (InterruptedException e) {}
		}
		
		for (ScheduledElevatorRequest ser : floorSystemRequests) {
			
			//Assuming sanitized inputs
			Integer startFloor = ser.getStartFloor();
			Integer destinationFloor = ser.getDestinationFloor();
			
			if (startFloor > highestFloorNumber ||destinationFloor > highestFloorNumber || startFloor <= 0 || destinationFloor <= 0) {
				System.err.println("Non existent floor received");
				return;
			}

			if (skipDelaysOnFloorInputs || ser.getMillisecondDelay()==0) {
				//No delay means instantly add request to queue
				this.addElevatorRequest(startFloor, destinationFloor);
			} else {
				//Create a thread with a delay that eventually calls addsElevatorRequest
				(new Thread(new DelayedRequest(this,startFloor, destinationFloor, ser.getMillisecondDelay()), "RequestOccuringAt_"+ser.getTime().toString())).start();
			}
			
		}
		notifyAll();
	}
	

	
	/**
	 * Adds an elevator request to the scheduling system so that an elevator have instructions on where to go
	 * 
	 * @param startFloor Starting floor of the request
	 * @param destinationFloor destination floor of the request
	 */
	public synchronized void addElevatorRequest(Integer startFloor, Integer destinationFloor) {

		boolean isUpwards = startFloor < destinationFloor;
		
		//Add elevator request to corresponding directionalToVisitSet if it isn't already queued
		if (isUpwards) {
			//Destination will only be known once we arrive at the start floor
			this.upwardsDestinationsPerFloor.get(startFloor-1).add(destinationFloor);
			//Find the best elevator to handle this request, then put the start floor on it's up list
			int bestElevatorID = getBestElevatorId(startFloor,isUpwards);
			this.logger.logSchedulerEvent("Elevator "+bestElevatorID+" has been scheduled go up to floor "+destinationFloor);
			this.getElevatorSpecificFloorsToVisit(bestElevatorID).addUpwardsFloorToVisit(startFloor);
			
			
			
		} else if (!isUpwards ) {
			//Destination will only be known once we arrive at the start floor
			this.downwardsDestinationsPerFloor.get(startFloor-1).add(destinationFloor);
			//Find the best elevator to handle this request, then put the start floor on it's down list
			int bestElevatorID = getBestElevatorId(startFloor,isUpwards);
			this.logger.logSchedulerEvent("Elevator "+bestElevatorID+" has been scheduled go down to floor "+destinationFloor);
			this.getElevatorSpecificFloorsToVisit(bestElevatorID).addDownwardsFloorToVisit(startFloor);
		}
		notifyAll();
	}
	
	/**
	 * Returns the ID of the best elevator to handle this request
	 * @param startFloor The starting floor of the request
	 * @param isUpwards if the request is upwards
	 * @return The ID of the most suitable elevator for this request
	 */
	private synchronized int getBestElevatorId(int startFloor, boolean isUpwards) {
		if (isUpwards) {
			//Upwards. First check if there are any upwards or parked elevators under us
			if (this.findClosestElevatorBelowWithState(startFloor, Direction.UP)!=-1) {
				return this.findClosestElevatorBelowWithState(startFloor, Direction.UP);
			} else if (this.findClosestElevatorBelowWithState(startFloor, Direction.AWAITING_NEXT_REQUEST)!=-1) {
				return this.findClosestElevatorBelowWithState(startFloor, Direction.AWAITING_NEXT_REQUEST);
			} else {
				//No upwards or parked elevators below start floor. Randomly select one
				Collections.shuffle(this.allElevatorInfo);
				return this.allElevatorInfo.get(0).getId();
			}
		} else {
			//Downwards. First check if there are any downwards or packed elevators above us
			if (this.findClosestElevatorAboveWithState(startFloor, Direction.DOWN)!=-1) {
				return this.findClosestElevatorAboveWithState(startFloor, Direction.DOWN);
			} else if (this.findClosestElevatorAboveWithState(startFloor, Direction.AWAITING_NEXT_REQUEST)!=-1) {
				return this.findClosestElevatorAboveWithState(startFloor, Direction.AWAITING_NEXT_REQUEST);
			} else {
				//No downwards or parked elevators above start floor. Randomly select one
				Collections.shuffle(this.allElevatorInfo);
				return this.allElevatorInfo.get(0).getId();
			}
		}
	}
	
	/**
	 * Finds the closest elevator below the start floor with the given state
	 * @param floor The start floor
	 * @param eState the state to look for
	 * @return The ID of the closest elevator below the start floor with the given state. -1 if there is none
	 */
	private synchronized int findClosestElevatorBelowWithState(int floor, Direction direction) {
		if (direction==null) return -1;
		
		LinkedList<Integer[]> belowElevators = new LinkedList<Integer[]>();
		//Identify elevators below
		for (ElevatorInfo eInfo : this.allElevatorInfo) {
			
			if (eInfo.getFloor()<=floor && eInfo.getMostRecentDirection().equals(direction)) {
				belowElevators.add(new Integer[] {eInfo.getId(), eInfo.getFloor()});
			}
		}
		
		//None found
		if (belowElevators.isEmpty()) return -1;
		
		//Identify the closest one. Largest in this case
		int[] currentMax = new int[] {belowElevators.get(0)[0], belowElevators.get(0)[1]};
		for (Integer[] arr : belowElevators) {
			if (arr[1]>currentMax[1]) {
				currentMax[0] = arr[0];
				currentMax[1] = arr[1];
			}
		}
		
		return currentMax[0];
	}
	
	/**
	 * Finds the closest elevator above the start floor with the given state
	 * @param floor The start floor
	 * @param eState the state to look for
	 * @return The ID of the closest elevator above the start floor with the given state. -1 if there is none
	 */
	private synchronized int findClosestElevatorAboveWithState(int floor, Direction direction) {
		if (direction==null) return -1;
		LinkedList<Integer[]> aboveElevators = new LinkedList<Integer[]>();
		//Identify above elevators 
		for (ElevatorInfo eInfo : this.allElevatorInfo) {
			if (eInfo.getFloor()>=floor && eInfo.getMostRecentDirection().equals(direction)) { 
				aboveElevators.add(new Integer[] {eInfo.getId(), eInfo.getFloor()});
			}
		}
		
		//None found
		if (aboveElevators.isEmpty()) return -1;
		
		//Identify the closest one. Smallest in this case
		int[] currentMin = new int[] {aboveElevators.get(0)[0], aboveElevators.get(0)[1]};
		for (Integer[] arr : aboveElevators) {
			if (arr[1]<currentMin[1]) {
				currentMin[0] = arr[0];
				currentMin[1] = arr[1];
			}
		}
		
		return currentMin[0];
	}
	
	/**
	 * Sets the allElevatorInfo 
	 * @param allElevatorInfo the new allElevatorInfo list
	 */
	public synchronized void setAllElevatorInfo(LinkedList<ElevatorInfo> allElevatorInfo) {
		//On first request, populate the list of ElevatorSpecific floors to visit
		if (this.allElevatorsAllFloorsToVisit.isEmpty()) {
			for (ElevatorInfo e : allElevatorInfo) {
				this.allElevatorsAllFloorsToVisit.add(new ElevatorSpecificFloorsToVisit(e.getId()));
			}
		}
		
		this.allElevatorInfo = allElevatorInfo;
		//TODO Figure out how to deal with thsiSENDS BEFORE THE FLOOR IS ONLINE
		//this.sendUpdateToFloorSubsystem();
		notifyAll();
	}
	
	/**
	 * Returns a TreeSet of the floors to be visited by the current elevator given it's current direction and location
	 * 
	 * @param currentFloorNumber The elevators current floor number
	 * @param currentElevatorDirectionIsUpwards if the elevator is currently going upwards
	 * @return TreeSet of the remaining floors to visit in this direction
	 */
	public synchronized HashMap<Integer,Integer> getNextFloorsToVisit() {
		
		//wait loop until there is a destination to visit - Waiting here means all elevators are parked
		while (!areThereUnvisitedFloors()) {
			logger.logSchedulerEvent("Elevator is waiting for Scheduler's next request");
			try {wait();} catch (InterruptedException e) {}
		}
		
		//Given the updated elevator infos, update all the ElevatorSpecificFloorsToVisit objects
		for (ElevatorInfo eInfo : this.allElevatorInfo) {
			
			//Check if elevator is in an arrived at floor state
			if (eInfo.getState().equals(ElevatorStateMachine.Stopping)||
				eInfo.getState().equals(ElevatorStateMachine.DoorClosing)||
				eInfo.getState().equals(ElevatorStateMachine.DoorOpening)||
				eInfo.getState().equals(ElevatorStateMachine.OpenDoor)) {

				if (eInfo.getMostRecentDirection()==Direction.UP) {
					//Previous elevator direction upwards
					this.getElevatorSpecificFloorsToVisit(eInfo.getId()).upwardsFloorIsVisited(eInfo.getFloor());
					//Add upwards destinations of this floor to the current elevator
					for (Integer i : this.upwardsDestinationsPerFloor.get(eInfo.getFloor()-1)) {
						this.getElevatorSpecificFloorsToVisit(eInfo.getId()).addUpwardsFloorToVisit(i);	
					}
					this.upwardsDestinationsPerFloor.get(eInfo.getFloor()-1).clear();

					
				} else {
					//Previous elevator direction downwards
					this.getElevatorSpecificFloorsToVisit(eInfo.getId()).downwardsFloorIsVisited(eInfo.getFloor());
					//Add the downwards destinations of this floor to the current elevator
					for (Integer i : this.downwardsDestinationsPerFloor.get(eInfo.getFloor()-1)) {
						this.getElevatorSpecificFloorsToVisit(eInfo.getId()).addDownwardsFloorToVisit(i);	
					}
					this.downwardsDestinationsPerFloor.get(eInfo.getFloor()-1).clear();
				}
			}
		}// end of for (ElevatorInfo eInfo : this.allElevatorInfo)

		//Create elevatorID:NextFloorToVisit hashMap
		HashMap<Integer,Integer> elevatorID_NextFloorToVisit = new HashMap<Integer,Integer>();
		for (ElevatorInfo eInfo : this.allElevatorInfo) {
			elevatorID_NextFloorToVisit.put(eInfo.getId(), this.getElevatorSpecificFloorsToVisit(eInfo.getId()).getNextFloorToVisit(eInfo.getFloor(), eInfo.getMostRecentDirection()));
		}


		return elevatorID_NextFloorToVisit;
	}
	
	/**
	 * Checks if there are active requests running right now
	 * @return
	 */
	private synchronized boolean areThereUnvisitedFloors() {
		for (ElevatorSpecificFloorsToVisit esftv : this.allElevatorsAllFloorsToVisit) {
			if (esftv.getActiveRequestCount()>0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the ElevatorSpecificFloorsToVisit given the elevatorID
	 * @param elevatorID the ID of the elevator
	 * @return the ElevatorSpecificFloorsToVisit object corresponding to that ID
	 */
	private synchronized ElevatorSpecificFloorsToVisit getElevatorSpecificFloorsToVisit(int elevatorID) {
		for (ElevatorSpecificFloorsToVisit esftv : this.allElevatorsAllFloorsToVisit) {
			if (esftv.getElevatorID()==elevatorID) {
				return esftv;
			}
		}
		//Should never get here. Elevator ID should always be valid
		return null;
	}
	

	
	/**
	 * Sends all elevator's info to the floor subsystem via UDP packet
	 * 
	 * @param listOfElevatorInfo
	 */
	private synchronized void sendUpdateToFloorSubsystem() {
		byte[] serializedListOfElevatorInfo = null;
		try {
			serializedListOfElevatorInfo = Util.serialize(this.allElevatorInfo);
		} catch (IOException e) {}
		DatagramPacket packetToSend = new DatagramPacket(serializedListOfElevatorInfo, serializedListOfElevatorInfo.length, this.floorInetAddress, this.floorSubsystemSendPort);
		Util.sendRequest_ReturnReply(packetToSend);
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
