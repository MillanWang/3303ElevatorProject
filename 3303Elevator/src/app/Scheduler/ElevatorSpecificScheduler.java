package app.Scheduler;

import java.util.LinkedList;
import java.util.TreeSet;

import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;

/**
 * Class to be an elevator specific to visit list to be sent from the scheduler to the elevator subsystem
 * @author Millan Wang
 *
 */
public class ElevatorSpecificScheduler {
	
	/**
	 * The ID of the elevator to receive the floorsToVisit set
	 */
	private int elevatorID;
	
	/**
	 * Most recently known floor 
	 */
	private int mostRecentFloor;
	
	/**
	 * To track which is the most recent scheduled direction
	 */
	private boolean isUpwards;
	
	/**
	 * Current state of the elevator specific scheduler
	 */
	private ElevatorSpecificSchedulerState currentState;
	
	/**
	 * Directional sets of floor numbers for the current elevator to visit
	 */
	private TreeSet<Integer> upwardsFloorsToVisit;
	private TreeSet<Integer> downwardsFloorsToVisit;
	
	/**
	 * Directional lists of sets of destinations to visit once arriving at the floor
	 */
	private LinkedList<TreeSet<Integer>> upwardsDestinationsPerFloor;
	private LinkedList<TreeSet<Integer>> downwardsDestinationsPerFloor;
	

	
	/**
	 * Constructor for the ElevatorSpecificFloorsToVisit class
	 * @param floorsToVisit
	 * @param elevatorID
	 */
	public ElevatorSpecificScheduler(int elevatorID) {
		this.elevatorID = elevatorID;
		this.mostRecentFloor = -1; 
		this.isUpwards=true;
		this.upwardsFloorsToVisit = new TreeSet<Integer>();
		this.downwardsFloorsToVisit = new TreeSet<Integer>();
		this.currentState = ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST;
		
		int highestFloorNumber= (new Config("local.properties")).getInt("floor.highestFloorNumber"); 
		//Directional destinations per floor
		this.upwardsDestinationsPerFloor= new LinkedList<TreeSet<Integer>>();
		this.downwardsDestinationsPerFloor = new LinkedList<TreeSet<Integer>>();
		//Populate them with TreeSets
		for (int i = 0; i<highestFloorNumber ; i++) {
			this.upwardsDestinationsPerFloor.add(new TreeSet<Integer>());
			this.downwardsDestinationsPerFloor.add(new TreeSet<Integer>());
		}
	}
	
	/**
	 * Returns the current elevator's ID
	 * @return the elevatorID
	 */
	public int getElevatorID() {
		return elevatorID;
	}

	/**
	 * @return the currentState of the current elevator scheduler
	 */
	public ElevatorSpecificSchedulerState getCurrentState() {
		return currentState;
	}

	/**
	 * Returns the number of active number of remaining stops on the current elevator
	 * @return the number of active number of remaining stops on the current elevator
	 */
	public int getActiveNumberOfStopsCount() {
		TreeSet<Integer> upDestinationsSet = new TreeSet<Integer>();
		TreeSet<Integer> downDestinationsSet = new TreeSet<Integer>();
		for (TreeSet<Integer> dests : this.downwardsDestinationsPerFloor) {
			downDestinationsSet.addAll(dests);
		}
		for (TreeSet<Integer> dests : this.upwardsDestinationsPerFloor) {
			upDestinationsSet.addAll(dests);
		}
		
		upDestinationsSet.addAll(this.upwardsFloorsToVisit);
		downDestinationsSet.addAll(this.downwardsFloorsToVisit);
		
		return upDestinationsSet.size() + downDestinationsSet.size();
	}
	
	/**
	 * Returns the current elevators upwards set of floors to visit
	 * @return the floorsToVisit
	 */
	public TreeSet<Integer> getUpwardsFloorsToVisit() {
		return upwardsFloorsToVisit;
	}
	
	/**
	 * Returns the current elevators downwards set of floors to visit
	 * @return the floorsToVisit
	 */
	public TreeSet<Integer> getDownwardsFloorsToVisit() {
		return downwardsFloorsToVisit;
	}
	
	/**
	 * Adds a request to the current elevator
	 * @param startFloor starting floor of the request
	 * @param destinationFloor destination floor of the request
	 */
	public void addRequest(int startFloor, int destinationFloor, int requestType) {
		if (requestType==1) {
			// Temporary error request type. Discard incoming request
			this.currentState = ElevatorSpecificSchedulerState.TEMPORARY_OUT_OF_SERVICE;
			return;
		} else if (requestType==2) {
			//Permanent error request type. Discard incoming request
			this.currentState = ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE;
			return;
		}
		
		
		boolean isUpwards = startFloor < destinationFloor;
		
		//Add elevator request to corresponding directionalToVisitSet if it isn't already queued
		if (isUpwards) {
			this.upwardsFloorsToVisit.add(startFloor);
			//Destination will only be known & added to floorsToVisitList once we arrive at the start floor
			this.upwardsDestinationsPerFloor.get(startFloor-1).add(destinationFloor);
		} else if (!isUpwards ) {
			this.downwardsFloorsToVisit.add(startFloor);
			//Destination will only be known & added to floorsToVisitList once we arrive at the start floor
			this.downwardsDestinationsPerFloor.get(startFloor-1).add(destinationFloor);
		}
	}
	

	
	/**
	 * Removes the floor to visit from the current elevator upon arrival
	 * @param floor that elevator just arrived to
	 */
	private void upwardsFloorIsVisited(Integer floor) {
		this.upwardsFloorsToVisit.remove(floor);
		this.upwardsFloorsToVisit.addAll(this.upwardsDestinationsPerFloor.get(floor-1));
		this.upwardsDestinationsPerFloor.get(floor-1).clear();
	}
	
	/**
	 * Removes the floor to visit from the current elevator upon arrival
	 * @param floor that elevator just arrived to
	 */
	private void downwardsFloorIsVisited(Integer floor) {
		this.downwardsFloorsToVisit.remove(floor);
		this.downwardsFloorsToVisit.addAll(this.downwardsDestinationsPerFloor.get(floor-1));
		this.downwardsDestinationsPerFloor.get(floor-1).clear();
	}

	/**
	 * Returns the floor to stop at for the current elevator
	 * @param elevatorCurrentFloor The floor that the elevator is currently at
	 * @param direction The direction that the current elevator is moving in//TODO : REVIEW IF DIRECTION NEEDED
	 * @return the floor number of the next floor to visit
	 */
	private int getNextFloorToVisit(int elevatorCurrentFloor) {//, Direction direction) { //TODO : REVIEW IF DIRECTION NEEDED
		
		//Temporary out of service
		if (this.currentState == ElevatorSpecificSchedulerState.TEMPORARY_OUT_OF_SERVICE) return -2;
		//Permanent out of service
		if (this.currentState == ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE) return -3;
		
		
		//Return negative 1 to indicate that there is no next floor to visit
		if (this.upwardsFloorsToVisit.isEmpty() && this.downwardsFloorsToVisit.isEmpty()) {
			this.currentState = ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST;
			return -1;
		}
		// Most recent direction is down. Try to keep going down
		if(!this.isUpwards){//direction==Direction.DOWN) {//TODO: Review the need to have the direction enum
			// If there are no more downwards floors to visit below the current floor
			if(this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).isEmpty()) {
				//If there are downwards floors to visit, Go to the lowest/first upwards
				if (!this.upwardsFloorsToVisit.isEmpty()) {
					//If the lowest up floor is lower than the current, we will go down to it.
					//Elevator will know to switch to upwards when nextFloor>currentFloor
					this.isUpwards=true;
					Integer nextFloor = this.upwardsFloorsToVisit.first();
					this.currentState = nextFloor<elevatorCurrentFloor ? 
													ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT : 
													ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
					return nextFloor;
				
					
				// No upwards floors to visit, but there are downwards floors to visit above the current floor
				} else if (!this.downwardsFloorsToVisit.isEmpty()) {
					//Move upwards to highest/last downwards floor to visit
					this.isUpwards=false;
					this.currentState = ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT;
					return this.downwardsFloorsToVisit.last();
				

				// There are no more floors to visit at all
				} else {
					this.currentState = ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST;
					return -1;
				}
				
				
			// When there are more downwards floors to visit below the current floor, Return closest/highest/last one
			} else {
				this.isUpwards=false;
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
				return this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).last();
			}
			
			
		// Most recent direction is up. Try to keep going up
		} else if (this.isUpwards){//direction==Direction.UP){ //TODO: Review the need to have the direction enum
			// When there are no more upwards floors to visit above the current.
			if (this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).isEmpty()) {
				 
				// If there are downwards floors to visit, Go to Highest/last downwards floor
				if (!this.downwardsFloorsToVisit.isEmpty()) {
					//If the highest floor is above the current, we will go up to start servicing the downs
					//Elevator will know to switch to down when we have nextFloor<currentFloor
					this.isUpwards=false;
					Integer nextFloor = this.downwardsFloorsToVisit.last();
					this.currentState = nextFloor>elevatorCurrentFloor ? 
													ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT : 
													ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
					return nextFloor;
				
				// No downwards floors to visit, but there are upwards floors to visit below current floor
				} else if (!this.upwardsFloorsToVisit.isEmpty()) {
					
					//Move downwards towards the lowest/first upwards floor to visit
					this.isUpwards=true;
					this.currentState = ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT;
					return this.upwardsFloorsToVisit.first();
				
				// No more floors to visit at all
				} else {
					this.currentState = ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST;
					return -1;
				}
				
			//When there are more upwards floors to visit above the current. Return the closest/lowest/first one
			} else {
				this.isUpwards=true;
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
				return this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first();
			}

		//If the direction is not specified to be up or down, return the closest floor to visit
		} else {
			//TODO : Figure out if this should even be possible?????? Maybe default to a prev direction???? CONTACT BEN
			return -1;

//			if (this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).last() > this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first()) { //TODO : Review how this checks for closest
//				//Closest downwards is closer than closest upwards
//				Integer nextFloor = this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).last();
//				this.currentState = nextFloor<elevatorCurrentFloor ? 
//						ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT : 
//						ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
//				return nextFloor;
//			} else {
//				//Closest upwards is closer than closet downwards
//				Integer nextFloor =  this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first();
//				this.currentState = nextFloor>elevatorCurrentFloor ? 
//						ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT : 
//						ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
//				return nextFloor;
//			}
		}
	}
	
	public int handleElevatorInfoChange_returnNextFloorToVisit(ElevatorInfo elevatorInfo) {
		if (this.isUpwards) {//elevatorInfo.getMostRecentDirection() == Direction.UP) {
			this.upwardsFloorIsVisited(elevatorInfo.getFloor());
		} else if (!this.isUpwards) {//elevatorInfo.getMostRecentDirection() == Direction.DOWN) {
			this.downwardsFloorIsVisited(elevatorInfo.getFloor());
		} else {
			System.err.println("Expecting only up and down for elevator info most recent direction... Need review");
//			return -1000;
		}
		this.mostRecentFloor = elevatorInfo.getFloor();
		return this.getNextFloorToVisit(elevatorInfo.getFloor());//, elevatorInfo.getMostRecentDirection()); //TODO: REVIEW IF NEEDED??
	}
	
	/**
	 * Returns a formatted string describing all current details about the current object
	 */
	@Override
    public String toString() {
		String returnString = "[Elevator "+this.elevatorID+" - ElevatorSpecificScheduler]\n";
		returnString+= "\tState : " + currentState + "\n";
		returnString+= "\tMost recent floor : " + (this.mostRecentFloor<=0?"(none)":this.mostRecentFloor) + "\n";
		returnString+= "\tUpwards floors to visit : " + this.upwardsFloorsToVisit.toString() + " (currently known)\n";
		returnString+= "\tDownwards floors to visit : " + this.downwardsFloorsToVisit.toString() + " (currently known)\n";
		returnString+= "\tTotal floors to visit : " + this.getActiveNumberOfStopsCount() + " (including currently unknown destinations)\n";
		return returnString;
	}
	
}
