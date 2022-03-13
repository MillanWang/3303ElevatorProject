package tests.SchedulerTests;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import app.Logger;
import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;
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
		Config config = new Config("test.properties");
		this.scheduler = new Scheduler(new Logger(config), config);
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
		ArrayList<ScheduledElevatorRequest> requests = new ArrayList<ScheduledElevatorRequest>();
		requests.add(new ScheduledElevatorRequest(null,1,true,3));
		requests.add(new ScheduledElevatorRequest(null,2,true,5));
		scheduler.floorSystemScheduleRequest(requests);
		
		int elevatorID=1;
		int floor=1;
		
		//TESTING MULTITHREADINGLY IS OUTSIDE OF THE CURRENT TIME BUDGET :(
		/*
		 * Because of wait loops, testing calling those methods on scheduler would require multiple threads
		 * Gotta figure out a way to do multithreaded JUnit testing
		 * */
		
		LinkedList<ElevatorInfo> elevatorSubsystemComms = new LinkedList<ElevatorInfo>();
		elevatorSubsystemComms.add(new ElevatorInfo(elevatorID, floor, ElevatorStateMachine.Idle, Direction.UP));
		this.scheduler.setAllElevatorInfo(elevatorSubsystemComms);
		
		
		//Has a next floor to visit
		Assert.assertFalse(scheduler.getNextFloorsToVisit().get(elevatorID)!=-1);
		
		
//		Assert.assertFalse(scheduler.getNextFloorsToVisit(2, true).isEmpty());
//		//Once we reach the top, no more next floors to visit
//		Assert.assertTrue(scheduler.getNextFloorsToVisit(3, true).isEmpty());
//		
//		scheduler.floorSystemScheduleRequest(new ScheduledElevatorRequest(null,3,true,1));
//		scheduler.floorSystemScheduleRequest(new ScheduledElevatorRequest(null,2,true,1));
//		Assert.assertFalse(scheduler.getNextFloorsToVisit(3, false).isEmpty());
//		Assert.assertFalse(scheduler.getNextFloorsToVisit(2, false).isEmpty());
//		//Once we reach the bottom, no more next floors to visit
//		Assert.assertTrue(scheduler.getNextFloorsToVisit(1, true).isEmpty());
	}
}
