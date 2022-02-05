/**
 * Elevator project 
 * The input class is the object that contains details of elevator request to be sent to the scheduler by  the floor subsystem 
 * 
 * @author petertanyous
 * #ID 101127203 
 */
package app.FloorSubsystem;
import java.time.LocalTime; 
public class Input {
	
	private LocalTime time; //time of request
	private int startfloor;
	private boolean Upwards;
	private int destinationfloor;  
	
	/**
	 * Constructor parses the input string to assign the fields 
	 * @param input; the input string (from text file)
	 */
	public Input(LocalTime time, int startfloor, boolean Upwards, int destinationfloor) {
		this.time = time;
		this.startfloor = startfloor; 
		this.Upwards = Upwards;
		this.destinationfloor = destinationfloor;
	}
	
	/**
	 * returns the time of request event
	 */
	public LocalTime getTime() {
		return this.time;
	}
	
	/**
	 * returns the floor at which the elevator is requested
	 */
	public int getStartFloor() {
		return this.startfloor;
	}
	
	/**
	 * returns True if the direction is Up and False if the direction is down
	 */
	public boolean isUpwards() {
		return this.Upwards;
	}
	
	/**
	 * returns the destination of the request  
	 */
	public int getDestinationFloor() {
		return this.destinationfloor;
	}

}
