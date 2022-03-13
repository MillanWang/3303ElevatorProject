package app.Scheduler;

import java.util.TreeSet;

import app.ElevatorSubsystem.Direction.Direction;

/**
 * Class to be an elevator specific to visit list to be sent from the scheduler to the elevator subsystem
 * @author Millan Wang
 *
 */
public class ElevatorSpecificFloorsToVisit {
	
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
	 * Constructor for the ElevatorSpecificFloorsToVisit class
	 * @param floorsToVisit
	 * @param elevatorID
	 */
	public ElevatorSpecificFloorsToVisit(int elevatorID) {
		this.elevatorID = elevatorID;
		this.upwardsFloorsToVisit = new TreeSet<Integer>();
		this.downwardsFloorsToVisit = new TreeSet<Integer>();
	}
	
	/**
	 * Returns the current elevator's ID
	 * @return the elevatorID
	 */
	public int getElevatorID() {
		return elevatorID;
	}

	/**
	 * Returns the number of active requests on the current elevator
	 * @return the number of active requests on the current elevator
	 */
	public int getActiveRequestCount() {
		return this.downwardsFloorsToVisit.size() + this.upwardsFloorsToVisit.size();
	}
	
	/**
	 * Returns the current elevators upwards set of floors to visit
	 * @return the floorsToVisit
	 */
	public TreeSet<Integer> getUpwardsFloorsToVisit() {
		return upwardsFloorsToVisit;
	}

	/**
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
	}
	
	/**
	 * Returns the current elevators downwards set of floors to visit
	 * @return the floorsToVisit
	 */
	public TreeSet<Integer> getDownwardsFloorsToVisit() {
		return downwardsFloorsToVisit;
	}
	
	/**
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
	}

	/**
	 * Returns the floor to stop at for the current elevator
	 * @param elevatorCurrentFloor The floor that the elevator is currently at
	 * @param direction The direction that the current elevator is moving in
	 * @return the floor number of the next floor to visit
	 */
	public Integer getNextFloorToVisit(int elevatorCurrentFloor, Direction direction) {
		//Return negative 1 to indicate that there is no next floor to visit
		if (this.upwardsFloorsToVisit.isEmpty() && this.downwardsFloorsToVisit.isEmpty()) return -1;
		
		if(direction==Direction.DOWN) {
			if(this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).isEmpty()) {
				//No more downwards floors to visit. Go to the lowest/first upwards
				//If the lowest up floor is lower than the current, we will go down to it.
					//Elevator will know to switch to upwards when nextFloor>currentFloor
				return this.upwardsFloorsToVisit.first();
			} else {
				//There are more downwards floors to visit below the current. Return closest/highest/last one
				return this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).last();
			}
			
			
		} else if (direction==Direction.UP){
			if (this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).isEmpty()) {
				//No more upwards floors to visit. Go to Highest/last downwards floor
				//If the highest floor is above the current, we will keep going up to it
					//Elevator will know to switch to down when we have nextFloor<currentFloor
				return this.downwardsFloorsToVisit.last();
			} else {
				//There are more upwards floors to visit above the current. Return the closest/lowest/first one
				return this.downwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first();
			}

			
		} else {
			//If the direction is not specified to be up or down, return the closest floor to visit
			if (this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).last() > this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first()) {
				//Closest downwards is closer than closest upwards
				return this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor,false).last();
			} else {
				//Closest upwards is closer than closet downwards
				return this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first();
			}
		}
	}
}
