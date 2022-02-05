package app.ElevatorSubsystem.Elevator;

import app.Scheduler.*;

public class ElevatorDoor{
	
	private TimeManagementSystem tms; 
	
	//TODO: The constructor should use time system
	public ElevatorDoor(){
		this.tms = new TimeManagementSystem(1);
	}

	public void loadElevator() throws InterruptedException {
		System.out.println("#ElevatorDoor#loadElevator() going to sleep");
		Thread.sleep((int) this.tms.getElevatorLoadingTime());
	}

}
