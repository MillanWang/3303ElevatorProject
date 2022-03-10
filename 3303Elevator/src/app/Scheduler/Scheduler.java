package app.Scheduler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import app.Logger;
import app.MainProgramRunner;
import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
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
	
	private TreeSet<Integer> upwardsToVisitSet;
	private TreeSet<Integer> downwardsToVisitSet;
	private LinkedList<Integer[]> unscheduledRequests;
	
	private LinkedList<TreeSet<Integer>> upwardsDestinations;
	private LinkedList<TreeSet<Integer>> downwardsDestinations;
	
	public static final int ELEVATOR_COUNT = 1;

	private boolean skipDelaysOnFloorInputs;
	private int highestFloorNumber;
	private int elevatorCurrentFloor; //Eventually expand to be an array for all elevators
	private Direction currentElevatorDirection; //Eventually expand to be an array for all elevators
	private FloorSubsystem floorSubsys;
	
	private Logger logger;
	

	/**
	 * Constructor for scheduler lass
	 * 
	 * @param highestFloorNumber highest floor number
	 * @param skipDelaysOnFloorInputs boolean indicating if all incoming timeSpecified requests should be ran without delay
	 * @param floorSubsys Reference to the floor subsystem dependency
	 */
	public Scheduler(Logger logger, int highestFloorNumber, boolean skipDelaysOnFloorInputs) {
		this.logger = logger;
		
		this.highestFloorNumber = highestFloorNumber;
		this.skipDelaysOnFloorInputs= skipDelaysOnFloorInputs; 
		this.elevatorCurrentFloor = 1;
		this.currentElevatorDirection = Direction.AWAITING_NEXT_REQUEST;
		
		//Sorted set of destinations to visit in each direction
		this.upwardsToVisitSet = new TreeSet<Integer>();
		this.downwardsToVisitSet = new TreeSet<Integer>();
		
		//List for requests that cannot be immediately scheduled
		this.unscheduledRequests= new LinkedList<Integer[]>();
		
		//Directional destinations per floor
		this.upwardsDestinations= new LinkedList<TreeSet<Integer>>();
		this.downwardsDestinations = new LinkedList<TreeSet<Integer>>();
		//Populate them with TreeSets
		for (int i = 0; i<highestFloorNumber ; i++) {
			this.upwardsDestinations.add(new TreeSet<Integer>());
			this.downwardsDestinations.add(new TreeSet<Integer>());
		}
	}
	
	/**
	 * Sets the floorSubsys field to the FloorSubsystem
	 * @param floorSubsys
	 */
	public void setFloorSubsys(FloorSubsystem floorSubsys) {
		this.floorSubsys = floorSubsys;
	}
	
	/**
	 * Schedules an incoming floorSystemRequest to corresponding directional floor queue
	 * @param requestEvent
	 */
	public synchronized void floorSystemScheduleRequest(ScheduledElevatorRequest floorSystemRequest) {
		//Assuming sanitized inputs
		Integer startFloor = floorSystemRequest.getStartFloor();
		Integer destinationFloor = floorSystemRequest.getDestinationFloor();
		
		if (startFloor > highestFloorNumber ||destinationFloor > highestFloorNumber || startFloor <= 0 || destinationFloor <= 0) {
			System.err.println("Non existent floor received");
			return;
		}

		if (skipDelaysOnFloorInputs || floorSystemRequest.getMillisecondDelay()==0) {
			//No delay means instantly add request to queue
			this.addElevatorRequest(startFloor, destinationFloor);
		} else {
			//Create a thread with a delay that eventually calls addsElevatorRequest
			(new Thread(new DelayedRequest(this,startFloor, destinationFloor, floorSystemRequest.getMillisecondDelay()), "RequestOccuringAt_"+floorSystemRequest.getTime().toString())).start();
		}
	}
	
	/**
	 * Adds an elevator request to the scheduling system
	 * ALGORITHM USED IN ITERATION 1 
	 * 
	 * @param startFloor Starting floor of the request
	 * @param destinationFloor destination floor of the request
	 */
	public synchronized void addElevatorRequest(Integer startFloor, Integer destinationFloor) {

		boolean didUnparking = false;
		
		//If we are currently parked, add the startFloor to the corresponding directional toVisitSet
		if (this.upwardsToVisitSet.isEmpty() && this.downwardsToVisitSet.isEmpty() ) {
			
			//Need to go down if startFloor is below current
			if (elevatorCurrentFloor > startFloor) {
				this.downwardsToVisitSet.add(startFloor);
				didUnparking = true;
				
			} //Need to go up if startFloor is above current
			else if (elevatorCurrentFloor < startFloor) {	
				this.upwardsToVisitSet.add(startFloor);
				didUnparking = true;
			} 
			//Already at start floor otherwise. No need to move elevator
			//After un-parking and adding startFloor as a destination, try to schedule the request
		} 
		
		
		boolean isUpwards = startFloor < destinationFloor;
		
		//Add elevator request to corresponding directionalToVisitSet if it isn't already queued
		if (isUpwards) { 
			
			//Add to visit set iff the start floor is above or equal to current. Otherwise add to unscheduledRequests
			if (startFloor >= elevatorCurrentFloor || didUnparking) {
				this.upwardsToVisitSet.add(startFloor);
				this.upwardsToVisitSet.add(destinationFloor);
			} else {
				this.unscheduledRequests.add(new Integer[]{startFloor,destinationFloor});
			}
			
			
		} else if (!isUpwards ) {
			//Add to visit set iff the start floor is below or equal to current. Otherwise add to unscheduledRequests
			if (startFloor <= elevatorCurrentFloor || didUnparking) {
				this.downwardsToVisitSet.add(startFloor);
				this.downwardsToVisitSet.add(destinationFloor);
			} else {
				this.unscheduledRequests.add(new Integer[]{startFloor,destinationFloor});
			}
			
		}
		
		notifyAll();
	}
	
	/**
	 * Adds an elevator request to the scheduling system
	 * ATTEMPTING TO CHANGE ALGORITHM FOR ITERATION 2
	 * 
	 * WORK IN PROGRESS - NOT PERFECT YET
	 * 
	 * @param startFloor Starting floor of the request
	 * @param destinationFloor destination floor of the request
	 */
	public synchronized void addElevatorRequest2(Integer startFloor, Integer destinationFloor) {
		boolean isUpwards = startFloor < destinationFloor;
		
		//Add elevator request to corresponding directionalToVisitSet if it isn't already queued
		if (isUpwards) {
			
			//Destination will only be known once we arrive at the start floor
			this.upwardsDestinations.get(startFloor-1).add(destinationFloor);
			
			
		} else if (!isUpwards ) {
			
			//Destination will only be known once we arrive at the start floor
			this.downwardsDestinations.get(startFloor-1).add(destinationFloor);
		
			
		}
		
		notifyAll();
	}
	
	
	/**
	 * Attempts to schedule all of the requests that were previously unscheduled. 
	 * Occurs every time the elevator switches directions
	 */
	private void attemptToScheduleUnscheduledRequests() {
		for (int i = 0; i < this.unscheduledRequests.size(); i++) {
			Integer[] ir = this.unscheduledRequests.pop();
			this.addElevatorRequest(ir[0], ir[1]);
		}
	}
	
	
	/**
	 * Returns a TreeSet of the floors to be visited by the current elevator given it's current direction and location
	 * 
	 * WORK IN PROGRESS - NOT PERFECT YET
	 *
	 * @param currentFloorNumber The elevators current floor number
	 * @param currentElevatorDirectionIsUpwards if the elevator is currently going upwards
	 * @return TreeSet of the remaining floors to visit in this direction
	 */
	public synchronized SortedSet<Integer> getNextFloorsToVisit(Integer currentFloorNumber, boolean currentElevatorDirection) {
		//wait loop until there is a destination to visit - Waiting here means elevator is parked
		while (this.upwardsToVisitSet.isEmpty() && this.downwardsToVisitSet.isEmpty() ) {
			this.floorSubsys.updateElevatorPosition(currentFloorNumber, Direction.AWAITING_NEXT_REQUEST);
			
			logger.logSchedulerEvent("Elevator is waiting for Scheduler's next request");

			try {wait();} catch (InterruptedException e) {}
		}

		this.elevatorCurrentFloor = currentFloorNumber;
		
		
		//just visited currentFloorNumber. pop it out of the corresponding directional to visit set
		//Inconsequential attempting to remove non existing element
		if (currentElevatorDirection) {
			upwardsToVisitSet.remove(currentFloorNumber);
		} else {
			downwardsToVisitSet.remove(currentFloorNumber);
		}

		SortedSet<Integer> floorsToVisit;
		boolean nextDirectionUp = currentElevatorDirection;
		
		if (currentElevatorDirection) {
			floorsToVisit = getStopsGoingUpwards(currentFloorNumber);
			nextDirectionUp = true;
			//Change direction if no more upwards floors to visit. Try scheduling the unscheduled
			
			logger.logSchedulerEvent("Elevator has no more upwards floors to visit. Attempting to change directions");
			
			if (floorsToVisit.isEmpty()) {
				attemptToScheduleUnscheduledRequests();
				floorsToVisit = getStopsGoingUpwards(currentFloorNumber);
				if (floorsToVisit.isEmpty()) {
					floorsToVisit = getRemainingStopsGoingDownward(currentFloorNumber);
					nextDirectionUp = false; 
				}
			} 
		} else {
			floorsToVisit = getRemainingStopsGoingDownward(currentFloorNumber);
			nextDirectionUp = false; 
			//Change direction if no more downwards floors to visit. Try scheduling the unscheduled
			logger.logSchedulerEvent("Elevator has no more downwards floors to visit. Attempting to change directions");
			
			if (floorsToVisit.isEmpty()) {
				attemptToScheduleUnscheduledRequests();
				if (floorsToVisit.isEmpty()) {
					floorsToVisit = getStopsGoingUpwards(currentFloorNumber);
					nextDirectionUp = true; 
				}
			} 
		}
		
		
		
		//When at the bottom floor, cannot have any more downwards floors to visit
		if (currentFloorNumber == 1 ) {
			this.downwardsToVisitSet.clear();
		}

		//When at the top floor, cannot have any more downwards floors to visit
		if (currentFloorNumber == this.highestFloorNumber) {
			this.upwardsToVisitSet.clear();
		}
		

		this.floorSubsys.updateElevatorPosition(currentFloorNumber,nextDirectionUp? Direction.UP : Direction.DOWN);
		return floorsToVisit;
	}
	
	
	/**
	 * Returns a TreeSet of the above floors that need to be visited by the current upwards moving elevator
	 * 
	 * @param currentFloor the current floor that the elevator is at
	 * @return List of floors that the current elevator needs to visit
	 */
	private synchronized SortedSet<Integer> getStopsGoingUpwards(Integer currentFloorNumber) {
		
		this.upwardsToVisitSet =new TreeSet<Integer>( this.upwardsToVisitSet.tailSet(currentFloorNumber, false));
		
		return this.upwardsToVisitSet.tailSet(currentFloorNumber, false);
	}
	
	/**
	 * Returns a TreeSet of the below floors that need to be visited by the current downwards moving elevator
	 * 
	 * @param currentFloor the current floor that the elevator is at
	 * @return List of floors that the current elevator needs to visit
	 */
	private synchronized SortedSet<Integer> getRemainingStopsGoingDownward(Integer currentFloorNumber) {
		
		this.downwardsToVisitSet = new TreeSet<Integer>( this.downwardsToVisitSet.headSet(currentFloorNumber, false));
		
		return this.downwardsToVisitSet.headSet(currentFloorNumber, false);
	}
	


	@Override
	public void run() {
		Config config = new Config("local.properties");
		// Create message receiver threads for messages from floor subsystem and elevator subsystem
		FloorSubsystemPacketReceiver fssReceiver = new FloorSubsystemPacketReceiver( config.getInt("scheduler.floorReceivePort"), this);
		ElevatorSubsystemPacketReceiver essReceiver = new ElevatorSubsystemPacketReceiver( config.getInt("scheduler.elevatorReceivePort"), this);
		(new Thread(fssReceiver, "FloorSubsystemPacketReceiver")).start();
		(new Thread(essReceiver, "ElevatorSubsystemPacketReceiver")).start();
	}
	
	public static void main(String[] args) {

		
		
		
		Scheduler scheduler = new Scheduler(null, MainProgramRunner.FLOOR_COUNT, MainProgramRunner.INSTANTLY_SCHEDULE_REQUESTS);
		(new Thread(scheduler, "Scheduler")).start();
	}
}
