package tests.SchedulerTests;





import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import app.Logger;
import app.FloorSubsystem.FloorSubsystem;
import app.FloorSubsystem.ScheduledElevatorRequest;
import app.Scheduler.Scheduler;

public class SchedulerTests {
	
	Scheduler scheduler;
	

	/**
	 * Creates new scheduler object for each test case
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.scheduler = new Scheduler(new Logger(true,true,true,true), 3,false);
		this.scheduler.setFloorSubsys(new FloorSubsystem(scheduler, new Logger(true,true,true,true)));
	}

	/**
	 * Testing the scheduler returning a set of destinations that need to be reached given Input 
	 * object parameters
	 * 
	 * Path is an A shape with commands coming incrementally
	 */
	@Test
	public void testFloorSystemScheduleRequestCanGetPickedUp_AShape_SplitRequests() {
		//Schedule trip from 1 to 3 and 
		scheduler.floorSystemScheduleRequest(new ScheduledElevatorRequest(null,1,false,3));
		Assert.assertFalse(scheduler.getNextFloorsToVisit(1, true).isEmpty());
		Assert.assertFalse(scheduler.getNextFloorsToVisit(2, true).isEmpty());
		//Once we reach the top, no more next floors to visit
		Assert.assertTrue(scheduler.getNextFloorsToVisit(3, true).isEmpty());
		
		scheduler.floorSystemScheduleRequest(new ScheduledElevatorRequest(null,3,true,1));
		scheduler.floorSystemScheduleRequest(new ScheduledElevatorRequest(null,2,true,1));
		Assert.assertFalse(scheduler.getNextFloorsToVisit(3, false).isEmpty());
		Assert.assertFalse(scheduler.getNextFloorsToVisit(2, false).isEmpty());
		//Once we reach the bottom, no more next floors to visit
		Assert.assertTrue(scheduler.getNextFloorsToVisit(1, true).isEmpty());
	}
	
	/**
	 * Testing the scheduler returning a set of destinations that need to be reached given Input 
	 * object parameters
	 * 
	 * Path is an A shape with commands all at beginning
	 */
	@Test
	public void testFloorSystemScheduleRequestCanGetPickedUp_AShape_UpFrontRequests() {
		//Schedule trip from 1 to 3 and 
		scheduler.floorSystemScheduleRequest(new ScheduledElevatorRequest(null,1,false,3));
		scheduler.floorSystemScheduleRequest(new ScheduledElevatorRequest(null,3,true,1));
		scheduler.floorSystemScheduleRequest(new ScheduledElevatorRequest(null,2,true,1));
		
		Assert.assertFalse(scheduler.getNextFloorsToVisit(1, true).isEmpty());
		Assert.assertFalse(scheduler.getNextFloorsToVisit(2, true).isEmpty());
		
		//Once we reach the top,  next floors to visit will now be downwards
		Assert.assertFalse(scheduler.getNextFloorsToVisit(3, true).isEmpty());
		
		Assert.assertFalse(scheduler.getNextFloorsToVisit(3, false).isEmpty());
		Assert.assertFalse(scheduler.getNextFloorsToVisit(2, false).isEmpty());
		//Once we reach the bottom, no more next floors to visit
		Assert.assertTrue(scheduler.getNextFloorsToVisit(1, true).isEmpty());
	}
	
	/**
	 * Test scheduler returning a set of destinations to be reached 
	 * given source and destination parameters
	 * 
	 * Path is an A shape with commands coming incrementally
	 */
	@Test
	public void testAddRequestCanGetPickedUp_AShape_SplitRequests() {
		//Schedule trip from 1 to 2
		scheduler.addElevatorRequest(1,2);
		Assert.assertFalse(scheduler.getNextFloorsToVisit(1, true).isEmpty());
		//Once we reach the top, no more next floors to visit
		Assert.assertTrue(scheduler.getNextFloorsToVisit(2, true).isEmpty());
		
		//Schedule other trips 3 to 1 and 2 to 1
		scheduler.addElevatorRequest(3,1);
		scheduler.addElevatorRequest(2,1);
		Assert.assertFalse(scheduler.getNextFloorsToVisit(3, false).isEmpty());
		Assert.assertFalse(scheduler.getNextFloorsToVisit(2, false).isEmpty());
		//Once we reach the bottom, no more next floors to visit
		Assert.assertTrue(scheduler.getNextFloorsToVisit(1, true).isEmpty());
	}
	
	
	/**
	 * Testing the scheduler returning a set of destinations that need to be reached
	 *  given start and destination integer parameters
	 * 
	 * Path is an A shape with commands all at beginning
	 */
	@Test
	public void testAddRequestCanGetPickedUp_AShape_UpFrontRequests() {
		//Schedule trip from 1 to 3 and 
		scheduler.addElevatorRequest(3,1);
		scheduler.addElevatorRequest(2,1);
		scheduler.addElevatorRequest(1,2);
		
		Assert.assertFalse(scheduler.getNextFloorsToVisit(1, true).isEmpty());
		Assert.assertFalse(scheduler.getNextFloorsToVisit(2, true).isEmpty());
		
		//Once we reach the top,  next floors to visit will now be downwards
		Assert.assertFalse(scheduler.getNextFloorsToVisit(3, true).isEmpty());
		
		Assert.assertFalse(scheduler.getNextFloorsToVisit(3, false).isEmpty());
		Assert.assertFalse(scheduler.getNextFloorsToVisit(2, false).isEmpty());
		//Once we reach the bottom, no more next floors to visit
		Assert.assertTrue(scheduler.getNextFloorsToVisit(1, true).isEmpty());
	}
	
}
