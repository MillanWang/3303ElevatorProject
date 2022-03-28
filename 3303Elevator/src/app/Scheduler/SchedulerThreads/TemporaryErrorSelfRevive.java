package app.Scheduler.SchedulerThreads;

import app.Config.Config;
import app.Scheduler.ElevatorSpecificScheduler;


/**
 * Thread to revive a temporary error ElevatorSpecificScheduler
 * @author Millan Wang
 *
 */
public class TemporaryErrorSelfRevive implements Runnable {
	public static final int TEMPORARY_ERROR_SELF_REVIVE_TIME = 20*1000;
	ElevatorSpecificScheduler elevatorSpecificScheduler;
	
	public TemporaryErrorSelfRevive(ElevatorSpecificScheduler elevatorSpecificScheduler) {
		this.elevatorSpecificScheduler = elevatorSpecificScheduler;
	}
	
	
	@Override
	public void run() {
		try {Thread.sleep(TEMPORARY_ERROR_SELF_REVIVE_TIME);} catch (InterruptedException e) {}
		this.elevatorSpecificScheduler.reviveFromTempError();
		System.out.println("\n\nTEMPORARY ERROR EXPIRED - Elevator: "+this.elevatorSpecificScheduler.getElevatorID()+" is back online\n\n");
	}

}
