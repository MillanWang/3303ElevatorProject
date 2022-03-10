package app.ElevatorSubsystem.Elevator;

import app.Scheduler.TimeManagementSystem;
import app.Logger;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.StateMachine.*;
import java.util.ArrayList;
/**
 * SYSC 3303, Final Project
 * Elevator.java
 * Purpose: contains the state of the elevator
 *
 * @author Ben Kittilsen
 * */
public class Elevator{

	private final int id;

	private ElevatorDoor door;
	private int currentFloor, floorsMoved;
	private ElevatorStateMachine state;
	private TimeManagementSystem tms;
	private Logger logger;

	/**
	 * Maximum number of floors
	 */
	private int maxFloorCount;

	public Elevator(int id, int maxFloorCount,Logger logger, TimeManagementSystem tms) {
		this.maxFloorCount = maxFloorCount;
		this.currentFloor = 1;
		this.floorsMoved = 0;
		this.state = ElevatorStateMachine.Idle;
		this.tms = tms;
		this.door = new ElevatorDoor(this.tms);
		this.logger = logger;
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	/**
	 * delay thread while elevator is being loaded
	 * */
	public boolean loadElevator() {
		try {
			this.door.loadElevator();
			return true;
		}catch(InterruptedException e) {
			return false;
		}
	}

	/**
	 * Delay thread while moving up or down
	 * */
	public void waitTransit(int finalDest) {

		int dest;
		if(state.getDirection() == Direction.UP) {
			dest = currentFloor + 1;
		}else if(state.getDirection() == Direction.DOWN) {
			dest = currentFloor + 1; //tms does not do time cal for destination below current
		}else {
			return;
		}

		Float waitTime = tms.getElevatorTransitTime(floorsMoved, dest, finalDest);
		try {
			Thread.sleep(waitTime.intValue());
			floorsMoved += 1;
		}catch(InterruptedException e) {
			System.out.println("[ERROR]#Elevator#waitTransite() issue waiting");
		}
	}

	/**
	 * Get floor
	 * */
	public int getFloor() {
		return this.currentFloor;
	}

	/**
	 * Get state machine for elevator
	 * */
	public ElevatorStateMachine getState() {
		return state;
	}

	/**
	 * Update state to next state
	 * */
	public void nextState() {
		state = state.nextState();

		if(state == ElevatorStateMachine.MoveUp) {
			currentFloor++;
		}else if(state == ElevatorStateMachine.MoveDown) {
			currentFloor--;
		}
	}

	/**
	 * Determines if the elevator is in a state that is moving
	 * @return if the elevator is moving
	 */
	public boolean isMoving() {
		return state == ElevatorStateMachine.MoveUp || state == ElevatorStateMachine.MoveDown;
	}

	/**
	 * Determines if the elevator is idle or waiting for next stopping processing
	 *
	 * @return if the elevator is currently stationary
	 */
	public boolean isStationary() {
		return state == ElevatorStateMachine.Idle || state == ElevatorStateMachine.NextStopProcessing;
	}

	/**
	 * Get the elevator's direction state
	 * @return the elevator's direction state
	 */
	public Direction getDirection() {
		return state.getDirection();
	}

	/***
	 * Set the direction of the state, can not set direction up if on max floor, can not set direction down if on bottom floor.
	 * @param d Direction
	 */
	public void setDirection(Direction d) {

		if(this.currentFloor == 1 && d == Direction.DOWN) {
			d = Direction.AWAITING_NEXT_REQUEST;
		}

		if( this.currentFloor == this.maxFloorCount && d == Direction.UP) {
			d = Direction.AWAITING_NEXT_REQUEST;
		}

		state.setDirection(d);
	}

	/**
	 * Logs message to logger
	 * @param message
	 */
	public void log(String msg){
		logger.logElevatorEvents("[Elevator "+ this.id + "]"+msg);
	}

	/**
	 * Checks the destination floor and updates the elevators state
	 * @param destinationFloor
	 */
	private void checkFloor(int destinationFloor) {
		this.log("at floor " + this.getFloor());
		if(this.getFloor() == destinationFloor){ //or max floor
			this.nextState();// stopping or opening door

			if(this.getState() == ElevatorStateMachine.Stopping) {
				//elevator slowing down
				this.nextState();//open door if stopping before
			}
			this.log("has arrived at desitnation ");

			this.log("doors starting to open");
			this.nextState();//open door
			this.loadElevator();
			this.nextState();//door closing
			this.nextState();
			this.log("doors finished closing");
		}
	}

	public void move() {
		// boolean movingUp = elevator.getState() == ElevatorStateMachine.MoveUp;
		// I think we should pass the elevator state to the scheduler
		// the three states that we need to consider are moving up (MoveUp), moving down (MoveDown),
		// and finally the parked state which is now (nextProcessing, or Idle state) almost identical.

		//log("" + elevator.getState());

		//SortedSet<Integer> floorsToVisit = scheduler.getNextFloorsToVisit(elevator.getFloor(), true);//tmp for now
		//log("" + floorsToVisit);
		ArrayList<Integer> floorsToVisit = new ArrayList<Integer>();

		if(floorsToVisit.size() == 0) {
			if(!this.isStationary()) {
				//error here
			}
			return;
		}


		// checking if the elevator is idle or next processing
		if(this.isStationary()) {
			//int destFloor = floorsToVisit.first();
			int destFloor = floorsToVisit.get(0);
			floorsMoved = 0;

			if(destFloor > this.getFloor()) {
				this.setDirection(Direction.UP);
			}else if(destFloor < this.getFloor()) {
				this.setDirection(Direction.DOWN);
			}else if(destFloor == this.getFloor()) {
				this.setDirection(Direction.AWAITING_NEXT_REQUEST);
			}else {
				//There is an issue
			}

			this.waitTransit(destFloor);
			this.nextState();
			checkFloor(destFloor);
		// check if the elevator is moving up or down
		}else if(this.isMoving()) {
			// used to determine the movement we need to continue
			//int destFloor = this.getState() == ElevatorStateMachine.MoveUp ? floorsToVisit.first() : floorsToVisit.last();
			int destFloor = floorsToVisit.get(0);

			if(destFloor > this.getFloor()) {
				this.setDirection(Direction.UP);
			}else if(destFloor < this.getFloor()) {
				this.setDirection(Direction.DOWN);
			}

			this.waitTransit(destFloor);
			this.nextState();
			checkFloor(destFloor);
		}
		System.out.println("\n\n");
	}

}
