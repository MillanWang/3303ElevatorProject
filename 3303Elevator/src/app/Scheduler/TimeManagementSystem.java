/**
 * 
 */
package app.Scheduler;

/**
 * @author Abdelrahim Karaja
 * Time manager for elevator movement between floors and time spent loading/unloading passengers
 */
import java.util.Random;
import java.util.ArrayList;
public class TimeManagementSystem {
	public float timeMultiplier; //Used to scale time for testing purposes
	
	TimeManagementSystem(float multiplier){
		this.timeMultiplier = multiplier;
	}
	
	public float getElevatorLoadingTime() {
		//Generates a random value in seconds for time while elevator door is opened.
		Random r = new Random();
		return timeMultiplier * (7.92f + r.nextFloat() * 3.22f);
	}
	
	public ArrayList<Float> getElevatorTransitTime(int currentFloor, int destinationFloor) {
		//Generates array of times for travel between floors
		Random r = new Random();
		ArrayList<Float> times = new ArrayList<>();
		
		//Generate array of times from floor to floor
		for(int i = 0; i < destinationFloor - currentFloor; i++) {
			times.add(timeMultiplier * (2.77f + r.nextFloat() * 1.07f));
		}
		return times;
	}
}
