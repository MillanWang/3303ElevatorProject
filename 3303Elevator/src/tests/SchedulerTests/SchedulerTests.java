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
		
	}
	
	//TODO Show that delayed threads take time to schedule
	//TODO Show that requests get discarded when everyone is out of service
	//TODO Show that floor requests lead to elevators having a next floor to visit
	//TODO Show that there will be negative floors to visit with no requests and out of service elevators
}
