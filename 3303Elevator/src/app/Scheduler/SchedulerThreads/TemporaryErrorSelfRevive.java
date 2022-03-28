package app.Scheduler.SchedulerThreads;

import app.Config.Config;
import app.Scheduler.ElevatorSpecificScheduler;


/**
 * Thread to revive a temporary error ElevatorSpecificScheduler
 * @author Millan Wang
 *
 */
public class TemporaryErrorSelfRevive implements Runnable {
	public static final int TEMPORARY_ERROR_SELF_REVIVE_TIME = 10*1000;
	ElevatorSpecificScheduler eScheduler;
	Config config;
	
	public TemporaryErrorSelfRevive(ElevatorSpecificScheduler eScheduler) {
		this.eScheduler = eScheduler;
	}
	
	
	@Override
	public void run() {
		try {Thread.sleep(TEMPORARY_ERROR_SELF_REVIVE_TIME);} catch (InterruptedException e) {}
		this.eScheduler.reviveFromTempError();
		System.out.println("\n\nTEMPORARY ERROR EXPIRED - Elevator: "+this.eScheduler.getElevatorID()+" is back online\n\n");
	}

}
