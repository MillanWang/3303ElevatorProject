package app.Scheduler;

import java.time.LocalTime;
import java.time.temporal.*;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import app.FloorSubsystem.*;

/**
 * SYSC 3303, Final Project Iteration 1&0
 * Scheduler.java
 * 
 * Scheduler class coordinating requests from the FloorSubsystem into directions for the ElevatorSubsytem 
 * 
 * @author Millan Wang
 *
 */
public class Scheduler {
	
	private TreeSet<Integer> upwardsToVisitSet;
	private TreeSet<Integer> downwardsToVisitSet;
	private LinkedList<Integer[]> unscheduledRequests;
	
	public static final int ELEVATOR_COUNT = 1;

	private boolean skipDelaysOnFloorInputs;
	private int highestFloorNumber;
	private int elevatorCurrentFloor; //Eventually expand to be an array for all elevators
	private FloorSubsystem floorSubsys;
	
	
	
	
	/**
	 * Constructor for scheduler lass
	 * 
	 * @param highestFloorNumber highest floor number
	 * @param skipDelaysOnFloorInputs boolean indicating if all incoming timeSpecified requests should be ran without delay
	 * @param floorSubsys Reference to the floor subsystem dependency
	 */
	public Scheduler(int highestFloorNumber, boolean skipDelaysOnFloorInputs, FloorSubsystem floorSubsys) {
		this.highestFloorNumber = highestFloorNumber;
		this.skipDelaysOnFloorInputs= skipDelaysOnFloorInputs; 
		this.elevatorCurrentFloor = 1;
		this.floorSubsys = floorSubsys; 

		//Sorted set of destinations to visit in each direction
		this.upwardsToVisitSet = new TreeSet<Integer>();
		this.downwardsToVisitSet = new TreeSet<Integer>();
		
		//List for requests that cannot be immediately scheduled
		this.unscheduledRequests= new LinkedList<Integer[]>(); 
	}
	
	
	/**
	 * Schedules an incoming floorSystemRequest to corresponding directional floor queue
	 * @param requestEvent
	 */
	public synchronized void floorSystemScheduleRequest(Input floorSystemRequest) {
		//Assuming sanitized inputs
		Integer startFloor = floorSystemRequest.getStartFloor();
		Integer destinationFloor = floorSystemRequest.getDestinationFloor();
		
		if (startFloor > highestFloorNumber ||destinationFloor > highestFloorNumber) {
			System.out.println("Non existent floor received");
			return;
		}
		
		long milliSecondDelay = getMillisecondDelayUntilRequest(floorSystemRequest.getTime());
		
		if (milliSecondDelay==0) {
			//No delay means instantly add request to queue
			this.addElevatorRequest(startFloor, destinationFloor);
		} else {
			//Create a thread with a delay that eventually calls addsElevatorRequest
			(new Thread(new DelayedRequest(this,startFloor, destinationFloor, milliSecondDelay), "RequestOccuringAt_"+floorSystemRequest.getTime().toString())).start();
		}
	}
	
	/**
	 * Calculates the time in milliseconds to delay between now and the given the LocalTime execution time
	 * @param executionTime LocalTime when the wait should end
	 * @return time in milliseconds to wait before reaching the given LocalTime
	 */
	private long getMillisecondDelayUntilRequest(LocalTime executionTime) {
		if ( executionTime == null || skipDelaysOnFloorInputs ) {
			return 0;
		} else {
			long milliseconds = LocalTime.now().until(executionTime, ChronoUnit.MILLIS );
			
			//if the scheduled time is before now, milliseconds will be negative. Add it to ms in a day to get time until it occurs again tomorrow
			if (milliseconds < 0) {
				milliseconds = 24*60*60*1000 + milliseconds;
			}
			return milliseconds;
		}
		
	}
	
	/**
	 * Adds an elevator request to the scheduling system
	 * 
	 * @param startFloor Starting floor of the request
	 * @param destinationFloor destination floor of the request
	 */
	public synchronized void addElevatorRequest(Integer startFloor, Integer destinationFloor) {

		//If we are currently parked, add the startFloor to the corresponding directional toVisitSet
		if (this.upwardsToVisitSet.isEmpty() && this.downwardsToVisitSet.isEmpty() ) {
			
			//Need to go down if startFloor is below current
			if (elevatorCurrentFloor > startFloor) {
				this.downwardsToVisitSet.add(startFloor);
				
			} //Need to go up if startFloor is above current
			else if (elevatorCurrentFloor < startFloor) {	
				this.upwardsToVisitSet.add(startFloor);
			} 
			//Already at start floor otherwise. No need to move elevator
			//After un-parking and adding startFloor as a destination, try to schedule the request
		} 
		
		
		boolean isUpwards = startFloor < destinationFloor;
		
		//Add elevator request to corresponding directionalToVisitSet if it isn't already queued
		if (isUpwards) { 
			
			//Add to visit set iff the start floor is above or equal to current. Otherwise add to unscheduledRequests
			if (startFloor >= elevatorCurrentFloor) {
				this.upwardsToVisitSet.add(startFloor);
				this.upwardsToVisitSet.add(destinationFloor);
			} else {
				this.unscheduledRequests.add(new Integer[]{startFloor,destinationFloor});
			}
			
			
		} else if (!isUpwards ) {
			//Add to visit set iff the start floor is below or equal to current. Otherwise add to unscheduledRequests
			if (startFloor <= elevatorCurrentFloor) {
				this.downwardsToVisitSet.add(startFloor);
				this.downwardsToVisitSet.add(destinationFloor);
			} else {
				this.unscheduledRequests.add(new Integer[]{startFloor,destinationFloor});
			}
			
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
	 * Returns a treeset of the floors to be visited by the current elevator given it's current direction and location
	 *
	 * @param currentFloorNumber The elevators current floor number
	 * @param currentElevatorDirectionIsUpwards if the elevator is currently going upwards
	 * @return TreeSet of the remaining floors to visit in this direction
	 */
	public synchronized SortedSet<Integer> getNextFloorsToVisit(Integer currentFloorNumber, boolean currentElevatorDirection) {
		
		this.elevatorCurrentFloor = currentFloorNumber;
		
		//just visited currentFloorNumber. pop it out of the corresponding directional to visit set
		//Inconsequential attempting to remove non existing element
		if (currentElevatorDirection) {
			upwardsToVisitSet.remove(currentFloorNumber);
		} else {
			downwardsToVisitSet.remove(currentFloorNumber);
		}
		
		
		//wait loop until there is a destination to visit - Waiting here means elevator is parked
		while (this.upwardsToVisitSet.isEmpty() && this.downwardsToVisitSet.isEmpty() ) {
			//this.floorSubsystem.NOTIFY FLOOR SYS THAT THE ELEVATOR IS PARKED HERE MY DUDE
		/**
		 * DONT FORGET DONT FORGET DONT FORGET AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
		 */
			
			try {wait();} catch (InterruptedException e) {}
		}
		
		SortedSet<Integer> floorsToVisit;
		
		if (currentElevatorDirection) {
			floorsToVisit = getStopsGoingUpwards(currentFloorNumber);
			//Change direction if no more upwards floors to visit. Try scheduling the unscheduled
			if (floorsToVisit.isEmpty()) {
				attemptToScheduleUnscheduledRequests();
				floorsToVisit = getRemainingStopsGoingDownward(currentFloorNumber);
			}
		} else {
			floorsToVisit = getRemainingStopsGoingDownward(currentFloorNumber);
			//Change direction if no more downwards floors to visit. Try scheduling the unscheduled
			if (floorsToVisit.isEmpty()) {
				attemptToScheduleUnscheduledRequests();
				floorsToVisit = getStopsGoingUpwards(currentFloorNumber);
			}
		}
		
		
		/**
		 * DONT FORGET DONT FORGET DONT FORGET AHHHH	AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
		 */
		//NEED TO UPDATE FLOOR ABOUT THE ELEVATOR MOVEMENTS
//		this.floorSubsys.update(); Elevator position notification
		return floorsToVisit;
	}
	
	
	/**
	 * Returns a TreeSet of the above floors that need to be visited by the current upwards moving elevator
	 * 
	 * @param currentFloor the current floor that the elevator is at
	 * @return List of floors that the current elevator needs to visit
	 */
	private synchronized SortedSet<Integer> getStopsGoingUpwards(Integer currentFloorNumber) {
		return this.upwardsToVisitSet.tailSet(currentFloorNumber, false);
	}
	
	/**
	 * Returns a TreeSet of the below floors that need to be visited by the current downwards moving elevator
	 * 
	 * @param currentFloor the current floor that the elevator is at
	 * @return List of floors that the current elevator needs to visit
	 */
	private synchronized SortedSet<Integer> getRemainingStopsGoingDownward(Integer currentFloorNumber) {
		return this.downwardsToVisitSet.headSet(currentFloorNumber, false);
	}

}
