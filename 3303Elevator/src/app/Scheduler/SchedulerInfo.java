package app.Scheduler;

import java.io.Serializable;
import java.util.HashMap;

public class SchedulerInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private HashMap<Integer, Integer> nextFloorsToVisit;
	private HashMap<Integer, Integer> errors;
	
	public SchedulerInfo(HashMap<Integer, Integer> nextFloorsToVisit, HashMap<Integer, Integer> errors) {
		this.nextFloorsToVisit = nextFloorsToVisit;
		this.errors = errors; 
	}
	
	public  HashMap<Integer, Integer> getNextFloorsToVisit() {	
		return this.nextFloorsToVisit;
	}
	
	public HashMap<Integer, Integer> getErrors() {
		return this.errors;
	}
	
}
