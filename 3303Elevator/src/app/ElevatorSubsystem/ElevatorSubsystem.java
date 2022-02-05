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

	public void log(String message){
		System.out.println("Elevator [" + this.name + "] " + message);
	}

	public void run(){
		while (true) {
			boolean isUp = elevator.getState() == Movement.UP ? true: false;
			//TODO replace isUp with elevator state. 
			SortedSet<Integer> floorsToVisit = scheduler.getNextFloorsToVisit(elevator.getFloor(), isUp);
			
			if(elevator.getState() != Movement.PARKED) {
				int destinationFloor = elevator.getState() == Movement.UP ? floorsToVisit.first() : floorsToVisit.last();
				//The move method will move the elevator accordingly does not handle parked
				this.log("is moving " + elevator.getState());
				elevator.move();
				this.log(" at floor " + elevator.getFloor());
				
				if(elevator.getFloor() == destinationFloor) {
					this.log("has arrived at desitnation ");
					this.log("doors starting to open");
					if(elevator.loadElevator()) {
						this.log("doors are now closed");
					}else{
						this.log("error occured opening doors");
					}
				}
			}
			
			// Need to add logic if the elevator is parked. 
			
		}
	}

}
