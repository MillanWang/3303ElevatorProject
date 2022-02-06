package app.ElevatorSubsystem.Elevator;

import app.Scheduler.*;

public class ElevatorDoor{
	
	private TimeManagementSystem tms; 
	
	//TODO: The constructor should use time system
	public ElevatorDoor(TimeManagementSystem tms){
		this.tms = tms;
	}

	public void loadElevator() throws InterruptedException {
		//System.out.println("#ElevatorDoor#loadElevator() going to sleep");
		Thread.sleep((int) this.tms.getElevatorLoadingTime());
	}

}
