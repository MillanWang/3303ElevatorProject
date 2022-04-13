package app.ElevatorSubsystem.Elevator;

import app.Scheduler.*;

/***
 * This class is used to simulated the loading 
 * time for each elevator. 
 * 
 * @author benki
 *
 */
public class ElevatorDoor{
	
	private TimeManagementSystem tms; 
	
	/***
	 * Pass an instance of the time management system
	 * that is configured to simulate the desired loading 
	 * time
	 * 
	 * @param tms responsible for the loading time
	 */
	public ElevatorDoor(TimeManagementSystem tms){
		this.tms = tms;
	}

	/***
	 * This method will sleep the thread it is called from
	 * to simulate the loading time
	 * 
	 * @throws  InterruptedException if there is an error when
	 * 			sleeping the thread it will throw an error. 
	 */
	public void loadElevator() throws InterruptedException {
		Thread.sleep((int) this.tms.getElevatorLoadingTime());
	}

}
