/**
 * Elevator project
 * the floor subsystem test class is responsible for testing the requests communications from and to the scheduler
 * 
 * @author Peter Tanyous
 */
package tests.FloorSubsystemTests;

import static org.junit.Assert.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import app.ElevatorSubsystem.Elevator.Movement;
import app.FloorSubsystem.FloorSubsystem;
import app.FloorSubsystem.ScheduledElevatorRequest;
import app.Scheduler.Scheduler;

public class FloorSubsystemTests {
	
	Scheduler scheduler = new Scheduler(5, true); 
	FloorSubsystem floorSubsys = new FloorSubsystem(scheduler);
	ScheduledElevatorRequest testInput; 
	@Test
	/**
	 * tests the InputRequests in floorSubsystem (Checks for proper file reading, and additional getter methods to confirm the addition of the requests
	 */
	public void addInputRequeststest() {
		scheduler.setFloorSubsys(floorSubsys);
		floorSubsys.addInputRequests("src/app/FloorSubsystem/inputfile.txt");
		assertNotEquals(0,floorSubsys.getRequests().size());
		assertNull(floorSubsys.getRequests().get(floorSubsys.getRequests().size() - 1).getTime());
		assertNotNull(floorSubsys.getRequests().get(0).getTime());
	}
	@Test
	/**
	 * tests the schedule requests to be received from the scheduler 
	 */
	public void addScheduleRequeststest() {
		testInput = new ScheduledElevatorRequest(LocalTime.now(), 1 , true, 5); //LocalTime time, int startfloor, boolean Upwards, int destinationfloor
		assertEquals(0, floorSubsys.getSchedulerRequests().size());
		floorSubsys.addScheduleRequests(testInput);
		assertEquals(1, floorSubsys.getSchedulerRequests().size());
	}
	@Test 
	/**
	 * tests the updateElevatorPosition method in floorSubsystem get status update from scheduler
	 */
	public void updateElevatorPositiontest() {
		assertNull(floorSubsys.getElevatorPosition());
		assertNull(floorSubsys.getElevatorStatus()); 
		floorSubsys.updateElevatorPosition(2, Movement.UP);
		assertEquals( 2 , (int)floorSubsys.getElevatorPosition()); 
		assertEquals(Movement.UP, floorSubsys.getElevatorStatus());
	}
}
