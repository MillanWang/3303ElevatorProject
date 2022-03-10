package app.Scheduler;

import java.util.TreeSet;

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
	 * The set of floor numbers for the current elevator to visit
	 */
	private TreeSet<Integer> floorsToVisit;
	
	/**
	 * Constructor for the ElevatorSpecificFloorsToVisit class
	 * @param floorsToVisit
	 * @param elevatorID
	 */
	public ElevatorSpecificFloorsToVisit(TreeSet<Integer> floorsToVisit, int elevatorID) {
		this.elevatorID = elevatorID;
		this.floorsToVisit = floorsToVisit;
	}
	
	/**
	 * Returns the current elevator's ID
	 * @return the elevatorID
	 */
	public int getElevatorID() {
		return elevatorID;
	}

	/**
	 * Returns the current elevators set of floors to visit
	 * @return the floorsToVisit
	 */
	public TreeSet<Integer> getFloorsToVisit() {
		return floorsToVisit;
	}
}
