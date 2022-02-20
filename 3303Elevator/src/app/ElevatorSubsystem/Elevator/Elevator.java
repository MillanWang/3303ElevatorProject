package app.ElevatorSubsystem.Elevator;

import app.Scheduler.TimeManagementSystem;
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
	public void waitTransit() {
		
		int dest; 
		if(state.getDirection() == Direction.UP) {
			dest = currentFloor + 1;
		}else if(state.getDirection() == Direction.DOWN) {
			dest = currentFloor + 1; //tms does not do time cal for destination below current
		}else {
			return;
		}
		
		ArrayList<Float> waitTime = tms.getElevatorTransitTime(currentFloor, dest);
		try {
			Thread.sleep(waitTime.get(0).intValue());
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
	
}
