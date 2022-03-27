package tests.SchedulerTests;
import org.junit.Assert;
import org.junit.Test;

import app.Scheduler.ElevatorSpecificSchedulerManager;

/**
 * ElevatorSpecificSchedulerManager Testing
 * These tests show how requests get distributed to 
 * the different elevators managed by ElevatorSpecificSchedulerManager
 * 
 * See how each ElevatorSpecificScheduler handles elevator movements 
 * in ElevatorSpecificSchedulerTests
 * 
 * 
 * @author Millan Wang
 *
 */
public class ElevatorSpecificSchedulerManagerTests {
	//TODO : Make test to show the assignment of requests to elevators along algorithm
	//
	public static final boolean SIMPLE_LEAST_LOAD_ALGORITHM = true;
	public static final boolean DIRECTIONAL_PRIORITY_ALGORITHM = false;
	/**
	 */
	@Test
	public void test_SimpleLeastLoadAlgorithm_RegularRequestDistributed(){
		ElevatorSpecificSchedulerManager essm = new ElevatorSpecificSchedulerManager(SIMPLE_LEAST_LOAD_ALGORITHM);
		essm.scheduleFloorRequest(1, 4, 0); //Schedule a regular up request
		essm.scheduleFloorRequest(12, 4, 0); //Schedule a regular down request
		
		Assert.assertTrue(essm.toString().contains("Downwards floors to visit : [12] (currently known)"));
		Assert.assertTrue(essm.toString().contains("Upwards floors to visit : [1] (currently known)"));
	}

	
	@Test
	public void test_SimpleLeastLoadAlgorithm_PermanentErrorRequestDistributed(){
		ElevatorSpecificSchedulerManager essm = new ElevatorSpecificSchedulerManager(SIMPLE_LEAST_LOAD_ALGORITHM);
		essm.scheduleFloorRequest(1, 4, 2); //Schedule a permanent error request

		Assert.assertTrue(essm.toString().contains("State : PERMANENT_OUT_OF_SERVICE"));
		
		essm.scheduleFloorRequest(3, 4, 2); //Schedule another permanent error request
		essm.scheduleFloorRequest(5, 4, 2); //Schedule another permanent error request
		essm.scheduleFloorRequest(7, 4, 2); //Schedule another permanent error request
		
		//Ensure that the "PERMANENT_OUT_OF_SERVICE" string appears 4 times
		String currentString = essm.toString();
		int occurances = (currentString.length() - currentString.replaceAll("PERMANENT_OUT_OF_SERVICE","").length())/"PERMANENT_OUT_OF_SERVICE".length();
		Assert.assertEquals(4, occurances);
		
		essm.scheduleFloorRequest(1, 3,2);//Schedule another permanent error request. This will get discarded
	}
}
