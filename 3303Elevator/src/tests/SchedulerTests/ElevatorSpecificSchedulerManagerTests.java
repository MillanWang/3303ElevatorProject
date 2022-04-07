package tests.SchedulerTests;
import org.junit.Assert;
import org.junit.Test;

import app.Scheduler.ElevatorSpecificSchedulerManager;
import app.Scheduler.ElevatorSpecificSchedulerManagerState;

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
	public static final boolean SIMPLE_LEAST_LOAD_ALGORITHM = true;
	public static final boolean DIRECTIONAL_PRIORITY_ALGORITHM = false;
	
	/**
	 * Tests the simple least load random distribution algorithm to ensure distribution to one 
	 * of the elevatos that has the lowest number of active requests.
	 */
	@Test
	public void test_SimpleLeastLoadAlgorithm_RegularRequestDistributed(){
		ElevatorSpecificSchedulerManager essm = new ElevatorSpecificSchedulerManager(SIMPLE_LEAST_LOAD_ALGORITHM);
		essm.scheduleFloorRequest(1, 4, 0); //Schedule a regular up request
		essm.scheduleFloorRequest(12, 4, 0); //Schedule a regular down request
		
		Assert.assertTrue(essm.toString().contains("Downwards floors to visit : [12] (currently known)"));
		Assert.assertTrue(essm.toString().contains("Upwards floors to visit : [1] (currently known)"));
	}

	/**
	 * Tests the distribution of requests with permanent errors
	 */
	@Test
	public void test_SimpleLeastLoadAlgorithm_PermanentErrorRequestDistributed(){
		ElevatorSpecificSchedulerManager essm = new ElevatorSpecificSchedulerManager(SIMPLE_LEAST_LOAD_ALGORITHM);
		essm.scheduleFloorRequest(1, 4, 2); //Schedule a permanent error request

		Assert.assertTrue(essm.toString().contains("State : PERMANENT_OUT_OF_SERVICE"));
		Assert.assertEquals( ElevatorSpecificSchedulerManagerState.AWAITING_NEXT_ELEVATOR_REQUEST, essm.getCurrentState());
		
		essm.scheduleFloorRequest(3, 4, 2); //Schedule another permanent error request
		essm.scheduleFloorRequest(5, 4, 2); //Schedule another permanent error request
		essm.scheduleFloorRequest(7, 4, 2); //Schedule another permanent error request
		
		//Ensure that the "PERMANENT_OUT_OF_SERVICE" string appears 4 times
		String currentString = essm.toString();
		int occurances = (currentString.length() - currentString.replaceAll("PERMANENT_OUT_OF_SERVICE","").length())/"PERMANENT_OUT_OF_SERVICE".length();
		Assert.assertEquals(4, occurances);
		
		essm.scheduleFloorRequest(1, 3,2);//Schedule another permanent error request. This will get discarded
		Assert.assertEquals( ElevatorSpecificSchedulerManagerState.ALL_ELEVATORS_OUT_OF_SERVICE, essm.getCurrentState());
	}
	
	/**
	 * DEV NOTES
	 * 
	 * Given that the algorithm for finding the best elevator to receive a request did not have strict requirements,
	 * it was determined that thoroughly testing the more advanced & more efficient algorithm would not justify the 
	 * time investment needed to ensure that it works flawlessly. Given that we cannot be 100% certain that it is 
	 * implemented flawlessly, we decided to exclusively use the much simpler request distribution algorithm, given
	 * that algorithm efficiency does not appear to be a significant contributor to marks in this project
	 * 
	 */
}
