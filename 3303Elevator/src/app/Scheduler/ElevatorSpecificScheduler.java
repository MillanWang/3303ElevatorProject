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
	 * Most recent next floor to visit value
	 */
	private int mostRecentNextFloor;
	
	/**
	 * To track which is the most recent scheduled direction
	 */
	private boolean isUpwards;
	
	/**
	 * Current state of the elevator specific scheduler
	 */
	private ElevatorSpecificSchedulerState currentState;
	private ElevatorSpecificSchedulerState previousStateBeforeTempError;
	
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
		mostRecentNextFloor=-1;
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
			// Temporary error request type. Schedule incoming request to be dealt with when back online
			this.previousStateBeforeTempError = currentState; 
			this.currentState = ElevatorSpecificSchedulerState.TEMPORARY_OUT_OF_SERVICE;
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
		
		if (currentState==ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT) {
			if (!isUpwards && startFloor<=this.mostRecentFloor) {
				currentState = ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
			}
		} else if (currentState==ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT) {
			if (isUpwards && startFloor>=this.mostRecentFloor) {
				currentState = ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
			}
		}
	}
	

	
	/**
	 * Removes the floor to visit from the current elevator upon arrival
	 * @param floor that elevator just arrived to
	 */
	private void upwardsFloorIsVisited(Integer floor) {
		if (currentState==ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT) {
			if (floor>this.mostRecentNextFloor) return;
		}
		this.upwardsFloorsToVisit.remove(floor);
		this.upwardsFloorsToVisit.addAll(this.upwardsDestinationsPerFloor.get(floor-1));
		this.upwardsDestinationsPerFloor.get(floor-1).clear();
	}
	
	/**
	 * Removes the floor to visit from the current elevator upon arrival
	 * @param floor that elevator just arrived to
	 */
	private void downwardsFloorIsVisited(Integer floor) {
		if (currentState==ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT) {
			if (floor<this.mostRecentNextFloor) return;
		}
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
		if (this.currentState == ElevatorSpecificSchedulerState.TEMPORARY_OUT_OF_SERVICE) { 
			this.mostRecentNextFloor=-2;
			return this.mostRecentNextFloor;
		}
		//Permanent out of service
		if (this.currentState == ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE) {
			this.mostRecentNextFloor=-3;
			return this.mostRecentNextFloor;
		}
		
		//Return negative 1 to indicate that there is no next floor to visit
		if (this.upwardsFloorsToVisit.isEmpty() && this.downwardsFloorsToVisit.isEmpty()) {
			this.currentState = ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST;
			this.mostRecentNextFloor=-1; 
			return this.mostRecentNextFloor;
		}
		
		// Most recent direction is down. Try to keep going down
		if(!this.isUpwards){
			// If there are no more downwards floors to visit below the current floor
			if(this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).isEmpty() || currentState==ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT) {

				//If there are downwards floors to visit, Go to the lowest/first upwards
				if (!this.upwardsFloorsToVisit.isEmpty()) {
					//If the lowest up floor is lower than the current, we will go down to it.
					//Elevator will know to switch to upwards when nextFloor>currentFloor
					this.isUpwards=true;
					this.mostRecentNextFloor = this.upwardsFloorsToVisit.first();
					this.currentState = mostRecentNextFloor < elevatorCurrentFloor ? 
													ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT : 
													ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
					return mostRecentNextFloor;
				
					
				// No upwards floors to visit, but there are downwards floors to visit, possibly above the current floor
				} else if (!this.downwardsFloorsToVisit.isEmpty()) {
					//Move upwards to highest/last downwards floor to visit
					this.isUpwards=false;
					this.mostRecentNextFloor = this.downwardsFloorsToVisit.last();
					this.currentState = mostRecentNextFloor > elevatorCurrentFloor ? 
													ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT : 
													ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
					return this.mostRecentNextFloor;

	
				// There are no more floors to visit at all
				} else {
					this.currentState = ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST;
					this.mostRecentNextFloor=-1;
					return this.mostRecentNextFloor;
				}
				
				
			// When there are more downwards floors to visit below the current floor, Return closest/highest/last one
			} else {
				this.isUpwards=false;
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
				this.mostRecentNextFloor= this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).last();
				return this.mostRecentNextFloor;
			}
			
			
		// Most recent direction is up. Try to keep going up
		} else if (this.isUpwards){
			// When there are no more upwards floors to visit above the current.
			if (this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).isEmpty() ||  currentState==ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT) {
				 
				

				// If there are downwards floors to visit, Go to Highest/last downwards floor
				if (!this.downwardsFloorsToVisit.isEmpty()) {
					//If the highest floor is above the current, we will go up to start servicing the downs
					//Elevator will know to switch to down when we have nextFloor<currentFloor
					this.isUpwards=false;
					this.mostRecentNextFloor = this.downwardsFloorsToVisit.last();
					this.currentState = this.mostRecentNextFloor>elevatorCurrentFloor ? 
													ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT : 
													ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
					return this.mostRecentNextFloor;
				
				// No downwards floors to visit, but there are upwards floors to visit, possibly below current floor
				} else if (!this.upwardsFloorsToVisit.isEmpty()) {
					//Move towards the lowest/first upwards floor to visit
					this.isUpwards=true;
					this.mostRecentNextFloor =this.upwardsFloorsToVisit.first();
					this.currentState = mostRecentNextFloor>elevatorCurrentFloor ? 
												ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT : 
												ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT;
					return this.mostRecentNextFloor;
				
					
					
					
					
					

					
					
					
					
					
				// No more floors to visit at all
				} else {
					this.currentState = ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST;
					this.mostRecentNextFloor =-1;
					return this.mostRecentNextFloor;
				}
				
			//When there are more upwards floors to visit above the current. Return the closest/lowest/first one
			} else {
				this.isUpwards=true;
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
				this.mostRecentNextFloor = this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first();
				return this.mostRecentNextFloor;
			}

		//If the direction is not specified to be up or down, return the closest floor to visit
		} else {
			//Should never get here. Only for the compiler
			return -1;
		}
	}
	
	/**
	 * Handles a change in the elevator's information
	 * @param elevatorInfo
	 * @return
	 */
	public int handleElevatorInfoChange_returnNextFloorToVisit(ElevatorInfo elevatorInfo) {
		if (elevatorInfo.getFloor()==-3) return-3;
		if (elevatorInfo.getFloor()==-2) return-2;
		
		//TEMPORARY ERROR REVIVAL - When a valid current floor is returned instead of the out of service negatives
		if (currentState == ElevatorSpecificSchedulerState.TEMPORARY_OUT_OF_SERVICE || elevatorInfo.getFloor()>=-1) {
			this.currentState = this.previousStateBeforeTempError;
			this.previousStateBeforeTempError = null;
		}
		
		if (this.isUpwards) {
			this.upwardsFloorIsVisited(elevatorInfo.getFloor());
		} else if (!this.isUpwards) {
			this.downwardsFloorIsVisited(elevatorInfo.getFloor());
		} else {
			System.err.println("Expecting only up and down for elevator info most recent direction... Need review");
			return -1000;
		}
		this.mostRecentFloor = elevatorInfo.getFloor();
		return this.getNextFloorToVisit(this.mostRecentFloor);//, elevatorInfo.getMostRecentDirection()); //TODO: REVIEW IF NEEDED??
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
