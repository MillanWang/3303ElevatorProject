/**
 * Elevator project
 * the LoggerTest class is responsible for testing the logger events
 * 
 * @author Peter Tanyous
 */

package tests.FloorSubsystemTests;

import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalTime;

import org.junit.Test;

import app.Logger;
import app.ElevatorSubsystem.Direction.Direction;
import app.FloorSubsystem.ScheduledElevatorRequest;

import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertTrue;

public class LoggerTest {

	private Logger log = new Logger(true, true, true, true) ;
	private final PrintStream standardOut = System.out;
	private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
	private LocalTime time; 
	@Before
	public void setUp() {
	    System.setOut(new PrintStream(outputStreamCaptor));
	}
	
	@After
	public void tearDown() {
	    System.setOut(standardOut);
	}
	
	@Test
	/*
	 * tests the elevator log message when elevator is moving up 
	 */
	public void elevatorUpLoggerTest() {
		log.logElevatorEvents(Direction.UP, 4);
		assertTrue(outputStreamCaptor.toString().trim().contains(" Elevator is moving up to floor number 4"));
	}
	
	@Test
	/*
	 * tests the elevator log message when elevator is moving down
	 */
	public void elevatorDownLoggerTest() {
		log.logElevatorEvents(Direction.DOWN, 2);
		assertTrue(outputStreamCaptor.toString().trim().contains(" Elevator is moving down to floor number 2"));
	}
	
	@Test
	/*
	 * tests the elevator log message when elevator is parked
	 */
	public void elevatorParkedLoggerTest() {
		log.logElevatorEvents(Direction.AWAITING_NEXT_REQUEST, 2);
		assertTrue(outputStreamCaptor.toString().trim().contains("Elevator is parked at floor number 2"));
	}
	
	@Test
	/*
	 * tests the elevator log message when elevator is parked
	 */
	public void elevatorStoppedLoggerTest() {
		log.logElevatorEvents(Direction.STOPPED_AT_FLOOR, 2);
		assertTrue(outputStreamCaptor.toString().trim().contains("Elevator is stopped at floor number 2"));
	}
	
	@Test
	/*
	 * tests the floor log message when new request is added
	 */
	public void logFloorEventTest() {
		ScheduledElevatorRequest request = new ScheduledElevatorRequest(10000, 2, true, 6);
		log.logFloorEvent(request);
		assertTrue(outputStreamCaptor.toString().trim().contains("floor number " +  request.getStartFloor() + " logged a floor request at time " +request.getTime() + "  to floor " +request.getDestinationFloor()));
	}
	
	@Test
	/*
	 * tests the Scheduler logger message
	 */
	public void logSchedulerEventTest() {
		log.logSchedulerEvent("A new request is scheduled");
		assertTrue(outputStreamCaptor.toString().trim().contains("A new request is scheduled"));
	}
	
	@Test
	/*
	 * tests the timeManagementEvent logger message
	 */
	public void logTimeManagementSystemEventTest() {
		log.logTimeManagementSystemEvent("New wait time is set");
		assertTrue(outputStreamCaptor.toString().trim().contains("New wait time is set"));
	}
}

