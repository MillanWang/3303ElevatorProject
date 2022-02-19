package app.ElevatorSubsystem.Elevator;

import app.Scheduler.TimeManagementSystem;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.StateMachine.*;
/**
 * SYSC 3303, Final Project
 * Elevator.java
 * Purpose: contains the state of the elevator
 * 
 * @author Ben Kittilsen
 * */
public class Elevator{

	private ElevatorDoor door;
	private int currentFloor;
	private ElevatorStateMachine state;
	private TimeManagementSystem tms;
	
	/**
	 * Maximum number of floors
	 */
	private int maxFloorCount;

	public Elevator(int maxFloorCount, float timeMultiplier) {
		this.maxFloorCount = maxFloorCount;
		this.currentFloor = 1;
		this.state = ElevatorStateMachine.Idle;
		this.tms = new TimeManagementSystem(timeMultiplier); 
		this.door = new ElevatorDoor(this.tms);
	}

	public boolean loadElevator() {
		try {
			this.door.loadElevator();
			return true; 
		}catch(InterruptedException e) {
			return false;
		}
	}
	
	public int getFloor() {
		return this.currentFloor;
	}
	
	public ElevatorStateMachine getState() {
		return state;
	}
	
	public void nextState() {
		state = state.nextState();
		
		if(state == ElevatorStateMachine.MoveUp) {
			currentFloor++;
		}else if(state == ElevatorStateMachine.MoveDown) {
			currentFloor--;
		}
		
	}
	
	public boolean isMoving() {
		return state == ElevatorStateMachine.MoveUp || state == ElevatorStateMachine.MoveDown;
	}

	public boolean isStationary() {
		return state == ElevatorStateMachine.Idle || state == ElevatorStateMachine.NextStopProcessing;
	}
	
	public Direction getDirection() {
		return state.getDirection();
	}
	
	public void setDirection(Direction d) {
		
		if(this.currentFloor == 1 && d == Direction.DOWN) {
			d = Direction.NONE;
		}
		
		if( this.currentFloor == this.maxFloorCount && d == Direction.UP) {
			d = Direction.NONE;
		}
		
		state.setDirection(d);
	}
	
}
