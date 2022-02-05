package app.Scheduler;

import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * @author Millan
 *
 */
public class Scheduler {
	
	private TreeSet<Integer> upwardsToVisitSet;
	private TreeSet<Integer> downwardsToVisitSet;
	

	public static final int ELEVATOR_COUNT = 1;
	private boolean[] isElevatorParked;
	
	private int highestFloorNumber;

	
	
	
	public Scheduler(int highestFloorNumber) {
		
		this.highestFloorNumber = highestFloorNumber;
		this.isElevatorParked = new boolean[ELEVATOR_COUNT]; //ITERATION 1 HARDCODE ELEVATOR COUNT

		
		//Sorted set of destinations to visit in each direction
		this.upwardsToVisitSet = new TreeSet<Integer>();
		this.downwardsToVisitSet = new TreeSet<Integer>();
	}
	
	
	/**
	 * Schedules an incoming floorSystemRequest to corresponding directional floor queue
	 * @param requestEvent
	 */
	public synchronized void scheduleRequest(Object floorSystemRequest) {
		//Assuming sanitized inputs
		

		/*REFERENCE FLOOR NUMBER FROM FLOOR REQUEST EVENT PLEASE MY DUDE DONT FORGET THIS */
		Object elevatorRequest = translateFloorRequestEventToElevatorRequest(floorSystemRequest);
		Integer requestStartFloor = 1234;
		Integer requestDestinationFloor = 1234;
		
		if (requestStartFloor > highestFloorNumber ||requestDestinationFloor > highestFloorNumber) {
			System.out.println("Non existent floor received");
			return;
		}
		
		boolean isUpwards = requestStartFloor < requestDestinationFloor;
		
		//NEED TO DEAL WITH DELAYS
		
		//Elevator should track upwards destinations left and downwards destinations left
		//Every incoming RQ should be translated into a directionalToVisitList addition
		
		
		
		
		//Add elevator request to corresponding directionalToVisitSet if it isn't already queued
		if (isUpwards) { 
			this.upwardsToVisitSet.add(requestStartFloor);
			this.upwardsToVisitSet.add(requestDestinationFloor);
			
		} else if (!isUpwards ) { 
			this.downwardsToVisitSet.add(requestStartFloor);
			this.downwardsToVisitSet.add(requestDestinationFloor);
		}
	}
	
	/**
	 * Translates the floor request event into an elevator request
	 * @param floorRequestEvent to be translated
	 * @return elevator request
	 */
	private Object translateFloorRequestEventToElevatorRequest(Object floorRequestEvent) {
		//TODO: Figure out how to translate RQ into destinations
		
		// If a destination limiter is needed, here is the place. to put it. 
		// Can't have someone Christmas tree the elevator buttons at an office and waste salaried time
		
		
		
		
		return floorRequestEvent;
	}
	
	
	
	
	
	

	/*
	 * Every time an elevator changes floors, it should check with the scheduler
	 * 
	 * 
	 * If we get an up RQ, keep moving up continuously servicing up RQs until...
	 * 			No more upwards requests. Must happen at or before reaching the top floor
	 * There might be down RQs queued. Check for the for downward requests. PARK STATE IF NOT
	 * 			Then service down requests until no more down requests. 
	 * 			
	 * Elevator sys shall be waitlooping the arrival of a request from either the up or down queue. Depends on mode
	 * 
	 * Need method to return next queued request depending on the previous direction of request. 
	 * 					previous direction matching should stop once that direction's queue is clear
	 * 
	 * 
	 * */
	
	
	
	
	/**
	 * Returns a treeset of the floors to be visited by the current elevator given it's current direction and location
	 *
	 * @param currentFloorNumber The elevators current floor number
	 * @param currentElevatorDirectionIsUpwards if the elevator is currently going upwards
	 * @return TreeSet of the remaining floors to visit in this direction
	 */
	public synchronized SortedSet<Integer> getNextFloorsToVisit(Integer currentFloorNumber, boolean currentElevatorDirectionIsUpwards) {
		//wait loop until there is a destination to visit - Waiting here means elevator is parked
		while (this.upwardsToVisitSet.isEmpty() && this.downwardsToVisitSet.isEmpty() ) {
			try {wait();} catch (InterruptedException e) {}
		}
		
		SortedSet<Integer> floorsToVisit;
		
		if (currentElevatorDirectionIsUpwards) {
			floorsToVisit = getStopsGoingUpwards(currentFloorNumber);
			//Change direction if no more upwards floors to visit
			if (floorsToVisit.isEmpty()) {
				floorsToVisit = getRemainingStopsGoingDownward(currentFloorNumber);
			}
		} else {
			floorsToVisit = getRemainingStopsGoingDownward(currentFloorNumber);
			//Change direction if no more downwards floors to visit
			if (floorsToVisit.isEmpty()) {
				floorsToVisit = getStopsGoingUpwards(currentFloorNumber);
			}
		}
		
		//Should a negative number flag be inserted into the returned set to indicate a direction change?????????
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
