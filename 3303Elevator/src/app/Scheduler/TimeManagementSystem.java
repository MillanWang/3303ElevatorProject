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
import java.util.ArrayList;
public class TimeManagementSystem {
	public float timeMultiplier; //Used to scale time for testing purposes
		
	/**
	* Constructor for TimeManagementSystem Class
	* 
	* @param multiplier time multiplier for testing purposes
	*/
	public TimeManagementSystem(float multiplier){
		this.timeMultiplier = Math.abs(multiplier);
	}
	
	/**
	 * Elevator loading time generator
	 * Generates a random float value in milliseconds for time while elevator door is opened.
	 * 
	 * @return single float time value
	 */
	public float getElevatorLoadingTime() {
		Random r = new Random();
		return timeMultiplier * ((7.92f + r.nextFloat() * 3.22f) * 1000);
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
		
		//Generate array of times from floor to floor
		for(int i = 0; i < destinationFloor - currentFloor; i++) {
			times.add(timeMultiplier * ((2.77f + r.nextFloat() * 1.07f) * 1000)); //Add each time
		}
		return times;
	}
}
