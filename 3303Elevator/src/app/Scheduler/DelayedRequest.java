package app.Scheduler;

/**
 * Internal class for delaying the execution of scheduled requests
 * @author Millan Wang
 *
 */
public class DelayedRequest implements Runnable{
	private Scheduler scheduler;
	private Integer startFloor, destinationFloor;
	private long delay;
	
	/**
	 * Constructor for the delayed request
	 * @param scheduler reference to the scheduler
	 * @param startFloor floor that the request starts at
	 * @param destinationFloor floor that the request ends at
	 * @param delay time in milliseconds to wait before sending the request
	 */
	public DelayedRequest(Scheduler scheduler, Integer startFloor, Integer destinationFloor, long delay) {
		this.scheduler = scheduler;
		this.startFloor = startFloor;
		this.destinationFloor = destinationFloor;
		this.delay = delay;
	}

	/**
	 * Delays the sending of the elevator request for the specified amount of time
	 */
	@Override
	public void run() {
		try {Thread.sleep(delay);} catch (InterruptedException e) {}
		this.scheduler.addElevatorRequest(startFloor, destinationFloor);
	}
	
}