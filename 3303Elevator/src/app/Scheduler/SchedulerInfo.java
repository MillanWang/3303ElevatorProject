package app.Scheduler;

import java.io.Serializable;
import java.util.HashMap;

public class SchedulerInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * Mapping of elevatorID : next floor to visit
	 */
	private HashMap<Integer, Integer> nextFloorsToVisit;
	
	/**
	 * Mapping of elevatorID : error type
	 */
	private HashMap<Integer, Integer> errors;
	
	/**
	 * Constructor for SchedulerInfo object
	 * @param nextFloorsToVisit Map of Next floors to visit
	 * @param errors map of errors
	 */
	public SchedulerInfo(HashMap<Integer, Integer> nextFloorsToVisit, HashMap<Integer, Integer> errors) {
		this.nextFloorsToVisit = nextFloorsToVisit;
		this.errors = errors; 
	}
	
	/**
	 * Returns the next floors to visit per elevator
	 * @return mapping of elevatorID to next floor to visit
	 */
	public  HashMap<Integer, Integer> getNextFloorsToVisit() {	
		return this.nextFloorsToVisit;
	}
	
	/**
	 * Returns the error per elevator
	 * @return Mapping of elevatorID to error type
	 */
	public HashMap<Integer, Integer> getErrors() {
		return this.errors;
	}
	
}
