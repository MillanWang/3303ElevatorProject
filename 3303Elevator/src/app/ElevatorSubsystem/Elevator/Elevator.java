package app.ElevatorSubsystem.Elevator;

import app.Scheduler.TimeManagementSystem;
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
	private Movement state;
	private TimeManagementSystem tms;
	
	/**
	 * Maximum number of floors
	 */
	private int maxFloorCount;

	public Elevator(int maxFloorCount, float timeMultiplier) {
		this.maxFloorCount = maxFloorCount;
		this.currentFloor = 1;
		this.state = Movement.UP;
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

	public void move() {
		try {
			if(this.state == Movement.UP)
				this.moveUp();
			else if(this.state == Movement.DOWN)
				this.moveDown();
			else if(this.state == Movement.PARKED)
				System.out.println("[ERROR]#Elevator#move() can't handle parked");
		} catch(InterruptedException e) {
			System.out.println("[ERROR]#Elevator#move() sleep error");
		}
	}
	
	public void moveUp() throws InterruptedException {
		this.state = Movement.UP;
		if (currentFloor < maxFloorCount) {
			this.currentFloor++;
			float f = tms.getElevatorTransitTime(currentFloor, currentFloor+1).get(0) ;
			int time = (int) f;
			Thread.sleep(time);
		} 
		if(currentFloor >= maxFloorCount){
			park();
		}
		
	}
	
	public void moveDown() throws InterruptedException {
		this.state = Movement.DOWN;
		if (currentFloor > 1) {
			this.currentFloor--;
			float f = tms.getElevatorTransitTime(currentFloor, currentFloor+1).get(0) ;
			int time = (int) f;
			Thread.sleep(time);
		} 
		if (currentFloor <= 1){
			park();
		}
	}
	
	public Movement getState() {
		return this.state; 
	}
	
	public void park() {
		this.state = Movement.PARKED;
	}
	
}
