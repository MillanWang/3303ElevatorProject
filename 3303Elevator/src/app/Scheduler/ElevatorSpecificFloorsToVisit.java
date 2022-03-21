package app.Scheduler;

import java.util.LinkedList;
import java.util.TreeSet;

import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;

/**
 * Class to be an elevator specific to visit list to be sent from the scheduler to the elevator subsystem
 * @author Millan Wang
 *
 */
public class ElevatorSpecificFloorsToVisit {
	
	public enum ElevatorSpecificSchedulingState{
		AWAITING_NEXT_REQUEST,
		SERVICING_DOWNWARDS_FLOORS_TO_VISIT,
		SERVICING_UPWARDS_FLOORS_TO_VISIT,
		MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT,
		MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT,
		OUT_OF_SERVICE
	}
	
	/**
	 * The ID of the elevator to receive the floorsToVisit set
	 */
	private int elevatorID;
	
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
	
	private ElevatorSpecificSchedulingState currentState;
	
	/**
	 * Constructor for the ElevatorSpecificFloorsToVisit class
	 * @param floorsToVisit
	 * @param elevatorID
	 */
	public ElevatorSpecificFloorsToVisit(int elevatorID) {
		this.elevatorID = elevatorID;
		this.upwardsFloorsToVisit = new TreeSet<Integer>();
		this.downwardsFloorsToVisit = new TreeSet<Integer>();
		this.currentState = ElevatorSpecificSchedulingState.AWAITING_NEXT_REQUEST;
		
		int highestFloorNumber= (new Config("local.properties")).getInt("floor.highestFloorNumber"); ; 
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
	public ElevatorSpecificSchedulingState getElevatorSpecificCurrentState() {
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
	 * Adds a request to the current elevator
	 * @param startFloor starting floor of the request
	 * @param destinationFloor destination floor of the request
	 */
	public void addRequest(int startFloor, int destinationFloor) {
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
	 * Returns the current elevators upwards set of floors to visit
	 * @return the floorsToVisit
	 */
	public TreeSet<Integer> getUpwardsFloorsToVisit() {
		return upwardsFloorsToVisit;
	}

	/**
	 * 
	 * DEPRECATED???
	 * 
	 * Adds a floor to the upwards floors to visit 
	 * @param floor to visit
	 */
	public void addUpwardsFloorToVisit(Integer floor) {
		this.upwardsFloorsToVisit.add(floor);
	}
		/**
	 * Removes the floor to visit from the current elevator upon arrival
	 * @param floor that elevator just arrived to
	 */
	public void upwardsFloorIsVisited(Integer floor) {
		this.upwardsFloorsToVisit.remove(floor);
		this.upwardsFloorsToVisit.addAll(this.upwardsDestinationsPerFloor.get(floor-1));
		this.upwardsDestinationsPerFloor.get(floor-1).clear();
	}
	
	/**
	 * Returns the current elevators downwards set of floors to visit
	 * @return the floorsToVisit
	 */
	public TreeSet<Integer> getDownwardsFloorsToVisit() {
		return downwardsFloorsToVisit;
	}
	
	/**
	 * DEPRECATED???
	 * 
	 * 
	 * Adds a floor to the downwards floors to visit 
	 * @param floor to visit
	 */
	public void addDownwardsFloorToVisit(Integer floor) {
		this.downwardsFloorsToVisit.add(floor);
	}
	
	/**
	 * Removes the floor to visit from the current elevator upon arrival
	 * @param floor that elevator just arrived to
	 */
	public void downwardsFloorIsVisited(Integer floor) {
		this.downwardsFloorsToVisit.remove(floor);
		this.downwardsFloorsToVisit.addAll(this.downwardsDestinationsPerFloor.get(floor-1));
		this.downwardsDestinationsPerFloor.get(floor-1).clear();
	}

	/**
	 * Returns the floor to stop at for the current elevator
	 * @param elevatorCurrentFloor The floor that the elevator is currently at
	 * @param direction The direction that the current elevator is moving in
	 * @return the floor number of the next floor to visit
	 */
	public Integer getNextFloorToVisit(int elevatorCurrentFloor, Direction direction) {
		//Return negative 1 to indicate that there is no next floor to visit
		if (this.upwardsFloorsToVisit.isEmpty() && this.downwardsFloorsToVisit.isEmpty()) {
			this.currentState = ElevatorSpecificSchedulingState.AWAITING_NEXT_REQUEST;
			return -1;
		}
		
		if(direction==Direction.DOWN) {
			if(this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).isEmpty()) {
				//No more downwards floors to visit. Go to the lowest/first upwards
				//If the lowest up floor is lower than the current, we will go down to it.
					//Elevator will know to switch to upwards when nextFloor>currentFloor
				Integer nextFloor = this.upwardsFloorsToVisit.first();
				this.currentState = nextFloor<elevatorCurrentFloor ? 
												ElevatorSpecificSchedulingState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT : 
												ElevatorSpecificSchedulingState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
				return nextFloor;
			} else {
				//There are more downwards floors to visit below the current. Return closest/highest/last one
				this.currentState = ElevatorSpecificSchedulingState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
				return this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).last();
			}
			
			
		} else if (direction==Direction.UP){
			if (this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).isEmpty()) {
				//No more upwards floors to visit. Go to Highest/last downwards floor
				//If the highest floor is above the current, we will keep going up to it
					//Elevator will know to switch to down when we have nextFloor<currentFloor
				Integer nextFloor = this.downwardsFloorsToVisit.last();
				this.currentState = nextFloor>elevatorCurrentFloor ? 
												ElevatorSpecificSchedulingState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT : 
												ElevatorSpecificSchedulingState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
				return nextFloor;
			} else {
				//There are more upwards floors to visit above the current. Return the closest/lowest/first one
				this.currentState = ElevatorSpecificSchedulingState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
				return this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first();
			}

			
		} else {
			//If the direction is not specified to be up or down, return the closest floor to visit
			if (this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).last() > this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first()) { //TODO : Review how this checks for closest
				//Closest downwards is closer than closest upwards
				Integer nextFloor = this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).last();
				this.currentState = nextFloor<elevatorCurrentFloor ? 
						ElevatorSpecificSchedulingState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT : 
						ElevatorSpecificSchedulingState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
				return nextFloor;
			} else {
				//Closest upwards is closer than closet downwards
				Integer nextFloor =  this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first();
				this.currentState = nextFloor>elevatorCurrentFloor ? 
						ElevatorSpecificSchedulingState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT : 
						ElevatorSpecificSchedulingState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
				return nextFloor;
			}
		}
	}
}
