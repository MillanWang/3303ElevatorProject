/**
 * The inputTest class tests the Input class which is the object that contains details of elevator request to be sent to the scheduler by  the floor subsystem 
 * 
 * @author petertanyous
 * #ID 101127203 
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
import app.FloorSubsystem.Input;
import app.Scheduler.Scheduler;
public class InputTest {
	Input req = new Input(LocalTime.now(), 1 , true, 5);
	Input reqTwo = new Input(LocalTime.now(), 6, false, 2);
	Input reqThree = new Input(null, 2, true, 4); 
	
	@Test
	/*
	 * reqtest tests an upwards request
	 */
	public void reqtest() {
		assertEquals(1, req.getStartFloor());
		assertEquals(5, req.getDestinationFloor());
		assertTrue(req.isUpwards());
		assertNotNull(req.getTime());
		
	}
	@Test
	/*
	 * reqTwotest tests a downwards request
	 */
	public void reqtwotest() {
		assertEquals(6, reqTwo.getStartFloor());
		assertEquals(2, reqTwo.getDestinationFloor());
		assertFalse(reqTwo.isUpwards());
		assertNotNull(reqTwo.getTime());
		
	}
	@Test
	/*
	 * reqThreetest tests a null time request
	 */
	public void reqthreetest() {
		assertEquals(2, reqThree.getStartFloor());
		assertEquals(4, reqThree.getDestinationFloor());
		assertTrue(reqThree.isUpwards());
		assertNull(reqThree.getTime());
		
	}
	
}
	
