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
	/**
	 * Maximum number of floors
	 */
	private int maxFloorCount;

	public Elevator( int maxFloorCount) {
		this.maxFloorCount = maxFloorCount;
		this.currentFloor = 1;
		this.state = Movement.UP;
		this.door = new ElevatorDoor();
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
	
	public void moveUp(){
		this.state = Movement.UP;
		if (currentFloor < maxFloorCount) {
			this.currentFloor++;
		} 
		if(currentFloor >= maxFloorCount){
			park();
		}
		
	}
	
	public void moveDown() {
		this.state = Movement.DOWN;
		if (currentFloor > 1) {
			this.currentFloor--;
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
