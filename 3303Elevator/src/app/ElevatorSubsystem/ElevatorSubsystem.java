package app.ElevatorSubsystem;

import java.util.SortedSet;

import app.Logger;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.Elevator;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;
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
	 * Maximum number of floors
	 */
	private int maxFloorCount;
	
	/**
	 * Logger to track what happens
	 */
	private Logger logger;
	
	private TimeManagementSystem tms;
	/**
	 * Constructor used to create elevator subsystem
	 *
	 * @param scheduler 	 the scheduler used to communication
	 * */
	public ElevatorSubsystem(Scheduler scheduler, int maxFloorCount, Logger logger, TimeManagementSystem tms){
		this.maxFloorCount = maxFloorCount;
		this.name = Thread.currentThread().getName();
		this.elevator = new Elevator(maxFloorCount, tms);
		this.scheduler = scheduler;
		this.logger = logger;
		this.tms = tms;
	}

	/**
	 * Logs message to console 
	 * WIP NEEDS TO INTEGRATE WITH LOGGER
	 * @param message
	 */
	public void log(String message){
		System.out.println("Elevator [" + this.name + "] " + message);
	}

	/**
	 * Checks the destination floor and updates the elevators state
	 * @param destinationFloor
	 */
	public void checkFloor(int destinationFloor) {
		this.log("at floor " + elevator.getFloor());
		if(this.elevator.getFloor() == destinationFloor){ //or max floor
			elevator.nextState();// stopping or opening door
			
			if(elevator.getState() == ElevatorStateMachine.Stopping) {
				//elevator slowing down
				elevator.nextState();//open door if stopping before
			}
			this.log("has arrived at desitnation ");
			
			this.log("doors starting to open");
			elevator.nextState();//open door
			elevator.loadElevator();
			elevator.nextState();//door closing
			elevator.nextState();
			this.log("doors finished closing");
		}
	}
	
	/**
	 * Continuously retrieves directions from the scheduler to operate the elevators
	 */
	public void run(){
		this.log("starting at floor 1");
		System.out.println("\n\n");
		while (true) {
			boolean movingUp = elevator.getState() == ElevatorStateMachine.MoveUp;
			// I think we should pass the elevator state to the scheduler
			// the three states that we need to consider are moving up (MoveUp), moving down (MoveDown),
			// and finally the parked state which is now (nextProcessing, or Idle state) almost identical.
			
			//log("" + elevator.getState());
			
			SortedSet<Integer> floorsToVisit = scheduler.getNextFloorsToVisit(elevator.getFloor(), true);//tmp for now
			//log("" + floorsToVisit);
			if(floorsToVisit.size() == 0) {
				if(!elevator.isStationary()) {
					//error here
				}
				continue;
			}

			
			// checking if the elevator is idle or next processing
			if(elevator.isStationary()) {
				int destFloor = floorsToVisit.first();
				
				if(destFloor > elevator.getFloor()) {
					elevator.setDirection(Direction.UP);
				}else if(destFloor < elevator.getFloor()) {
					elevator.setDirection(Direction.DOWN);
				}else if(destFloor == elevator.getFloor()) {
					elevator.setDirection(Direction.AWAITING_NEXT_REQUEST);
				}else {
					//There is an issue
				}
				
				elevator.waitTransit();
				elevator.nextState();
				checkFloor(destFloor);
			// check if the elevator is moving up or down
			}else if(elevator.isMoving()) {
				// used to determine the movement we need to continue
				int destFloor = elevator.getState() == ElevatorStateMachine.MoveUp ? floorsToVisit.first() : floorsToVisit.last();
				
				if(destFloor > elevator.getFloor()) {
					elevator.setDirection(Direction.UP);
				}else if(destFloor < elevator.getFloor()) {
					elevator.setDirection(Direction.DOWN);
				}
				
				elevator.waitTransit();
				elevator.nextState();
				checkFloor(destFloor);
			}
			System.out.println("\n\n");
		}
	}

}
