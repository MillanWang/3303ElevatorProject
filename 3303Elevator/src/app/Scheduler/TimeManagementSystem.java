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
		this.timeMultiplier = multiplier == 0 ? 0 : 1/Math.abs(multiplier);
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
	 * Function to generate random times within predetermined range, one for each level of movement.
	 * @param floorsMoved floors moved without stopping
	 * @param nextFloor next floor that elevator will visit
	 * @param finalDest final destination floor for user
	 * @return ArrayList of float time values
	 */
	public Float getElevatorTransitTime(int floorsMoved, int nextFloor, int finalDest) {
		Random r = new Random();
		Float time;
		if(floorsMoved > 0) { //If moving at max velocity
			if(nextFloor != finalDest) { //Not reaching final destination at next floor
				time = timeMultiplier * ((1.9f + r.nextFloat() * 0.2f) * 1000);
			}
			else { //Reaching final destination at next floor
				time = timeMultiplier * ((3.8f + r.nextFloat() * 0.4f) * 1000);
			}
		}
		else { //Taking off from 0
			if(nextFloor != finalDest) { //Taking off for more than one floor
				time = timeMultiplier * ((3.8f + r.nextFloat() * 0.4f) * 1000);
			}
			else { //Traveling only one floor
				time = timeMultiplier * (((3.8f + r.nextFloat() * 0.4f)/2) * 1000);
			}
		}
		
		logger.logTimeManagementSystemEvent("Generated elevator movement time of " + time.toString() + " (milliseconds)."); //Logging generated times to system
		return time;
	}
}
