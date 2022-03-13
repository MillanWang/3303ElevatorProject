/**
 * Elevator project 
 * The ScheduledElevatorRequest class is the object that contains details of elevator request to be sent to the scheduler by  the floor subsystem 
 * 
 * @author petertanyous
 * #ID 101127203 
 */
package app.FloorSubsystem;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit; 

public class ScheduledElevatorRequest implements Serializable{

	
	private LocalTime time; //time of request
	private long millisecondDelay; 
	private int startfloor;
	private boolean isUpwards;
	private int destinationfloor;  
	
	/**
	 * Constructor parses the input string to assign the fields 
	 * @param input; the input string (from text file)
	 */
	public ScheduledElevatorRequest(LocalTime time, int startfloor, boolean Upwards, int destinationfloor) {
		this.time = time;
		this.startfloor = startfloor; 
		this.isUpwards = Upwards;
		this.destinationfloor = destinationfloor;
		this.millisecondDelay = this.calculateMilliSecondDelay(time);
	}
	public ScheduledElevatorRequest(long millisecondDelay, int startfloor, boolean Upwards, int destinationfloor) {
		this.time = this.getLocalTimeDelay(millisecondDelay);
		this.startfloor = startfloor; 
		this.isUpwards = Upwards;
		this.destinationfloor = destinationfloor;
		this.millisecondDelay = millisecondDelay;
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
	 * @return the millisecondDelay
	 */
	public long getMillisecondDelay() {
		return millisecondDelay;
	}
	/**
	 * returns True if the direction is Up and False if the direction is down
	 */
	public boolean isUpwards() {
		return this.isUpwards;
	}
	
	/**
	 * returns the destination of the request  
	 */
	public int getDestinationFloor() {
		return this.destinationfloor;
	}
	
	/**
	 * Calculates the time in milliseconds to delay between now and the given the LocalTime execution time
	 * @param time
	 * @return milliseconds
	 */
	private long calculateMilliSecondDelay(LocalTime time) {
		
		if (time==null) {
			return 0;
		}
		
		long milliseconds = LocalTime.now().until(time, ChronoUnit.MILLIS );
		
		//if the scheduled time is before now, milliseconds will be negative. Add it to ms in a day to get time until it occurs again tomorrow
		if (milliseconds < 0) {
			milliseconds = 24*60*60*1000 + milliseconds;
		}
		return milliseconds;
	}
	/**
	 * Calculates the time in LocalTime stamp to delay between now and the given milliseconds delay
	 * 
	 * @param MilliSecond
	 * @return LocalTime timeDelay
	 */
	private LocalTime getLocalTimeDelay(long milliSeconds) {
		if(milliSeconds > 0) {
			LocalTime timeDelay = LocalTime.now().plus(milliSeconds, ChronoUnit.MILLIS);
			return timeDelay;
		} else {
			return LocalTime.now();
		}
	}
	
	
	
}
