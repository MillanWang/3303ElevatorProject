package app.ElevatorSubsystem.Elevator;

/**
 * SYSC 3303, Final Project
 * Movement.java
 * Purpose: Movement of elevator
 * 
 * @author Ben Kittilsen
 * */
public enum Movement {
	UP("up"),
	DOWN("down"),
	PARKED("parked");
	
	/**
	 * Name for enum
	 * */
	private final String name;
	
	/**
	 * Constructor for assigning name to enum
	 * */
	Movement(String name){
		this.name = name;
	}
	
	
	/**
	 * Overrides the default string method to return the string version of the enum
	 * 
	 * @return name		the name of the enum 
	 * */
	@Override
	public String toString() {
		return this.name; 
	}
}
