package app.ElevatorSubsystem.Elevator;

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

	public Elevator() {
		this.currentFloor = 1;
		this.state = Movement.PARKED;
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
		if(this.state == Movement.UP)
			this.moveUp();
		else if(this.state == Movement.DOWN)
			this.moveDown();
		else if(this.state == Movement.PARKED)
			System.out.println("[ERROR]#Elevator#move() can't handle parked");
	}
	
	private void moveUp(){
		this.state = Movement.UP;
		this.currentFloor++;
	}
	
	private void moveDown() {
		this.state = Movement.DOWN;
		this.currentFloor--;
	}
	
	public Movement getState() {
		return this.state; 
	}
	
	public void park() {
		this.state = Movement.PARKED;
	}
	
}
