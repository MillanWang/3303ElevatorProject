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
import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.FloorSubsystem.ScheduledElevatorRequest;

import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertTrue;

public class LoggerTest {

	private Logger log = new Logger(new Config("test.properties")) ;
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
	public void elevatorLoggerTest() {
		log.logElevatorEvents(" test");
		assertTrue(outputStreamCaptor.toString().trim().contains(" test"));
	}
	
	@Test
	/*
	 * tests the floor log message when new request is added
	 */
	public void logFloorEventTest() {
		ScheduledElevatorRequest request = new ScheduledElevatorRequest(10000, 2, true, 6,0);
		log.logFloorEvent(request);
		assertTrue(outputStreamCaptor.toString().trim().contains(" logged a floor request at time " +request.getTime() + "  to floor " +request.getDestinationFloor()));
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
	@Test
	/*
	 * tests the time measurement logger message 
	 */
	public void logTimeMeasurmentTest() {
		log.logTimeMeasurements("System took 97 seconds to handle all requests on the input file");
		assertTrue(outputStreamCaptor.toString().trim().contains("******"+"System took 97 seconds to handle all requests on the input file"+"******"));
	}
}

