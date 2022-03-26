package tests.SchedulerTests;
import org.junit.Assert;
import org.junit.Test;

import app.Scheduler.ElevatorSpecificSchedulerManager;

public class ElevatorSpecificSchedulerManagerTests {
	//TODO : Make test to show the assignment of requests to elevators along algorithm
	//
	public static final boolean SIMPLE_LEAST_LOAD_ALGORITHM = true;
	public static final boolean DIRECTIONAL_PRIORITY_ALGORITHM = false;
	/**
	 */
	@Test
	public void test_FIRST_BUT_GOTTA_FIX_THIS(){
		ElevatorSpecificSchedulerManager essm = new ElevatorSpecificSchedulerManager(SIMPLE_LEAST_LOAD_ALGORITHM);
		System.out.println(essm.toString());		System.out.println(essm.toString());
		Assert.assertTrue(false);
	}
}
