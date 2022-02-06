package tests.ElevatorSubsystemTests.Elevator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import app.ElevatorSubsystem.Elevator.*;

public class ElevatorTests {
	
	@Test
	public void testElevator() {
		Elevator elevator = new Elevator(7,1);
		assertSame(Movement.UP,elevator.getState());
		assertSame(1,elevator.getFloor());
	}

	
	@Test
	public void testGetFloor() {
		Elevator elevator = new Elevator(7,1);
		assertSame(1,elevator.getFloor());
	}


	@Test
	public void testMoveUp() {
		Elevator elevator = new Elevator(7,1);
		assertSame(1,elevator.getFloor());
		try {
			elevator.moveUp();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		assertSame(Movement.UP,elevator.getState());
		assertSame(2,elevator.getFloor());
	}

	@Test
	public void testMoveDown() {
		Elevator elevator = new Elevator(7,1);
		assertSame(1,elevator.getFloor());
		//need to move up before moving down assume move up works correct
		try {
			elevator.moveUp();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		//testing move down
		try {
			elevator.moveDown();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertSame(1,elevator.getFloor());
	}

	@Test
	public void testGetState() {
		Elevator elevator = new Elevator(7,1);
		assertSame(Movement.UP,elevator.getState());
	}

	@Test
	public void testPark() {
		Elevator elevator = new Elevator(7,1);
		elevator.park();
		assertSame(Movement.PARKED,elevator.getState());
	}

}
