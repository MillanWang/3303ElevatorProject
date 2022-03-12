/**
 * Elevator project 
 * The logger class is the object that tracks the system time and the elevator status throughout the system operations 
 * 
 * @author petertanyous
 * #ID 101127203 
 */
package app;

import java.time.LocalTime;

import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.FloorSubsystem.ScheduledElevatorRequest;

public class Logger {

	private LocalTime dateTime; 
	private boolean printElevatorEvents;
	private boolean printSchedulerEvents; 
	private boolean printFloorEvents; 
	private boolean printTimeManagementSystemEvent; 
	
	/**
	 * Constructor selects the events types to be printed through the boolean parameters  
	 * 
	 * @param printElevatorEvents: true if the logger is to print Elevator events else false
	 * @param printSchedulerEvents: true if the logger is to print Scheduler events else false 
	 * @param printFloorEvents: true if the logger is to print floor events else false
	 * @param printTimeManagementSystemEvent: true if the logger is to print Time management system events else false 
	 * 
	 */
	public Logger(Config c) {
		this.printElevatorEvents = c.getBoolean("elevator.log");
		this.printSchedulerEvents = c.getBoolean("scheduler.log"); 
		this.printFloorEvents = c.getBoolean("floor.log"); 
		this.printTimeManagementSystemEvent = c.getBoolean("time.log"); 
	}
	
	/**
	 * logElevatorEvents is used to print the elevator events logged
	 * 
	 * @param movement: movement enum specifying the movement state of the elevator 
	 * @param floorNumber: floor number to which the elevator is moving to or is parked at 
	 */
	public void logElevatorEvents(String message) {
		if(printElevatorEvents == true) {
			System.out.println("[" + dateTime.now() + "]" + message);
		}
	}
	
	/**
	 * logFloorEvent is used to print the floorsubsystem events logged
	 * 
	 * @param request: the request logged at the floor subsystem 
	 * 
	 */
	public void logFloorEvent(ScheduledElevatorRequest request) {
		if(printFloorEvents == true) {
			System.out.println(dateTime.now()+ " floor number " +  request.getStartFloor() + " logged a floor request at time " +request.getTime() + "  to floor " +request.getDestinationFloor());
		}
		
	}
	
	/**
	 * logSchedulerEvent is used to print scheduler events logged 
	 * 
	 * @param schedulerLogString string to be printed from the scheduler (Temp)
	 */
	public void logSchedulerEvent(String schedulerLogString) {
		if(printSchedulerEvents == true) {
			System.out.println(dateTime.now()+ " " + schedulerLogString);
		}
	}
	
	/**
	 * logTimeManagementEvent is used to print the Time Management System events logged
	 * 
	 * @param temp string to be printed from the Time Management System (Temp)
	 */
	public void logTimeManagementSystemEvent(String timeManagementLogString) {
		if(printTimeManagementSystemEvent == true) {
			System.out.println(dateTime.now() + " " + timeManagementLogString);
		}
	}
	
}
