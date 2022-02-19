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
import org.junit.Test;
import app.ElevatorSubsystem.Elevator.Movement;
import app.FloorSubsystem.Logger;
import app.FloorSubsystem.ScheduledElevatorRequest;

import org.junit.After;
import org.junit.Before;

public class LoggerTest {

	private Logger log = new Logger(true, true, true, true) ;
	private final PrintStream standardOut = System.out;
	private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
	
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
		log.logElevatorEvents(Movement.UP, 4);
		assertEquals("Elevator is moving up to floor number 4", outputStreamCaptor.toString().trim() );
	}
	@Test
	/*
	 * tests the elevator log message when elevator is moving down
	 */
	public void elevatorDownLoggerTest() {
		log.logElevatorEvents(Movement.DOWN, 2);
		assertEquals("Elevator is moving down to floor number 2", outputStreamCaptor.toString().trim() );
	}
	
	@Test
	/*
	 * tests the elevator log message when elevator is parked
	 */
	public void elevatorParkedLoggerTest() {
		log.logElevatorEvents(Movement.PARKED, 2);
		assertEquals("Elevator is parked at floor number 2", outputStreamCaptor.toString().trim() );
	}
	@Test
	/*
	 * tests the floor log message when new request is added
	 */
	public void logFloorEventTest() {
		ScheduledElevatorRequest request = new ScheduledElevatorRequest(10000, 2, true, 6);
		log.logFloorEvent(request);
		assertEquals("floor number " +  request.getStartFloor() + " logged a floor request at time " +request.getTime() + "  to floor " +request.getDestinationFloor(), outputStreamCaptor.toString().trim() );
	}
	
	@Test
	/*
	 * tests the Sheduler logger message
	 */
	public void logSchedulerEventTest() {
		log.logSchedulerEvent("A new request is scheduled");
		assertEquals("A new request is scheduled", outputStreamCaptor.toString().trim() );
	}
	
	@Test
	/*
	 * tests the timeManagementEvent logger message
	 */
	public void logTimeManagementSystemEventTest() {
		log.logTimeManagementSystemEvent("New wait time is set");
		assertEquals("New wait time is set", outputStreamCaptor.toString().trim() );
	}
}

