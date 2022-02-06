package app.ElevatorSubsystem;

import java.util.SortedSet;

import app.ElevatorSubsystem.Elevator.Elevator;
import app.ElevatorSubsystem.Elevator.Movement;
import app.Scheduler.*;

/**
 * SYSC 3303, Final Project
 * ElevatorSubsystem.java
 * Purpose: system interactions between scheduler and elevator
 *
 * @author Ben Kittilsen
 * */
public class ElevatorSubsystem implements Runnable{

	/**
	 * Name for the elevator
	 * */
	private String name;

	/**
	 * Elevator the subsystem controls
	 * */
	private Elevator elevator;

	/**
	 * The scheduler the elevator needs to communicate with
	 * */
	private Scheduler scheduler;

	/**
	 * Constructor used to create elevator subsystem
	 *
	 * @param scheduler 	 the scheduler used to communication
	 * */
	public ElevatorSubsystem(Scheduler scheduler){
		this.name = Thread.currentThread().getName();
		this.elevator = new Elevator();
		this.scheduler = scheduler;
	}

	/**
	 * Logs message to console
	 * @param message
	 */
	public void log(String message){
		System.out.println("Elevator [" + this.name + "] " + message);
	}

	public void checkFloor(int destinationFloor) {
		this.log("at floor " + elevator.getFloor());
		if(this.elevator.getFloor() == destinationFloor) {
			this.log("has arrived at desitnation ");
			this.log("doors starting to open");
			if(this.elevator.loadElevator()) {
				this.log("doors are now closed");
			}else{
				this.log("error occured opening doors");
			}
		}
	}
	
	/**
	 * Continuously retrieves directions from the scheduler to operate the elevators
	 */
	public void run(){
		while (true) {
			boolean isUp = elevator.getState() == Movement.UP ? true: false;
			//TODO replace isUp with elevator state.
			SortedSet<Integer> floorsToVisit = scheduler.getNextFloorsToVisit(elevator.getFloor(), isUp);

			if(floorsToVisit.size() == 0) {
				this.elevator.park();
				continue;
			}

			if(elevator.getState() != Movement.PARKED) {
				int destinationFloor = elevator.getState() == Movement.UP ? floorsToVisit.first() : floorsToVisit.last();
				//The move method will move the elevator accordingly does not handle parked
				this.log("is moving " + this.elevator.getState());
				elevator.move();
				this.checkFloor(destinationFloor);
			}

			if(elevator.getState() == Movement.PARKED) {
				this.log("is moving from " + this.elevator.getState());
				int destinationFloor = floorsToVisit.first();
				
				if(destinationFloor > this.elevator.getFloor()) {
					this.log("is moving up");
					this.elevator.moveUp();
				}else if(destinationFloor < this.elevator.getFloor()) {
					this.log("is moving down");
					this.elevator.moveDown();	
				}
				
				this.checkFloor(destinationFloor);
			}

		}
	}

}
