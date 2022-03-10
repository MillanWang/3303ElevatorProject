/**
 * 
 */
package app.Scheduler;

/**
 * SYSC 3303, Final Project Iteration 1&0
 * TimeManagementSystem.java
 * 
 * Time manager for elevator movement between floors and time spent loading/unloading passengers
 * 
 * @author Abdelrahim Karaja
 * 
 */
import java.util.Random;

import app.Logger;

import java.util.ArrayList;
public class TimeManagementSystem {
	private float timeMultiplier; //Used to scale time for testing purposes
	private Logger logger;
	
	/**
	* Constructor for TimeManagementSystem Class
	* 
	* @param multiplier time multiplier for testing purposes
	* @param logger Logger object used to track class activity
	*/
	public TimeManagementSystem(float multiplier, Logger logger){
		this.timeMultiplier = Math.abs(multiplier);
		this.logger = logger;
	}
	
	/**
	 * Elevator loading time generator
	 * Generates a random float value in milliseconds for time while elevator door is opened.
	 * 
	 * @return single float time value
	 */
	public float getElevatorLoadingTime() {
		Random r = new Random();
		float t = timeMultiplier * ((7.92f + r.nextFloat() * 3.22f) * 1000); //Generating time
		logger.logTimeManagementSystemEvent("Generated an elevator loading time of " + t + " milliseconds."); //Logging time to system
		return t;
	}
	
	/**
	 * Function to generate random array of times within predetermined range, one for each level of movement.
	 * @param currentFloor current user floor
	 * @param destinationFloor final destination floor for user
	 * @return ArrayList of float time values
	 */
	public ArrayList<Float> getElevatorTransitTime(int currentFloor, int destinationFloor) {
		//Create array of times for travel between floors
		Random r = new Random();
		ArrayList<Float> times = new ArrayList<>();
		
		//If only traveling one floor
		if(Math.abs(destinationFloor - currentFloor) == 1) { //If movement is only one floor, return value that accounts for accel and decel in same time
			times.add(timeMultiplier * (((3.8f + r.nextFloat() * 0.4f)/2) * 1000));
		}
		else { //If traveling more than one floor, generate times for take off + movement at max velocity + coming to stop
			times.add(timeMultiplier * ((3.8f + r.nextFloat() * 0.4f) * 1000)); //Taking off from rest
			//Generate array of times from floor to floor at maximum velocity
			for(int i = 1; i < Math.abs(destinationFloor - currentFloor - 1); i++) {
				times.add(timeMultiplier * ((1.9f + r.nextFloat() * 0.2f) * 1000)); //Add each time
			}
			times.add(timeMultiplier * ((3.8f + r.nextFloat() * 0.4f) * 1000)); //Coming to rest from max velocity
		}
		
		logger.logTimeManagementSystemEvent("Generated elevator movement times array of " + times.toString() + " (milliseconds)."); //Logging generated times to system
		return times;
	}
}
