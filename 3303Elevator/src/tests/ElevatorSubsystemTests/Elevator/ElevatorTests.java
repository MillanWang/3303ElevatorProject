package tests.ElevatorSubsystemTests.Elevator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.*;
import app.ElevatorSubsystem.StateMachine.*;
import app.Scheduler.TimeManagementSystem;
import app.Logger;

public class ElevatorTests {
	private TimeManagementSystem tms = new TimeManagementSystem(1, new Logger(false, false, false, true));
	
	@Test
	public void testElevator() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms);
		assertSame(ElevatorStateMachine.Idle,elevator.getState());
	}

	
	@Test
	public void testGetFloor() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms);
		assertSame(1,elevator.getFloor());
	}

	@Test
	public void testGetState() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms);
		assertSame(ElevatorStateMachine.Idle,elevator.getState());
	}
	
	@Test
	public void testnextState() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms);
		elevator.nextState();
		assertSame(ElevatorStateMachine.Idle,elevator.getState());
		elevator.setDirection(Direction.UP);
		elevator.nextState();
		assertSame(ElevatorStateMachine.MoveUp,elevator.getState());
		assertSame(2, elevator.getFloor());
		elevator.nextState();//stopping
		elevator.nextState();//doors opening
		elevator.nextState();//open
		elevator.nextState();//doors closing
		elevator.nextState();//next processing
		elevator.nextState();
		assertSame(ElevatorStateMachine.Idle,elevator.getState());
		elevator.setDirection(Direction.DOWN);
		elevator.nextState();
		assertSame(ElevatorStateMachine.MoveDown,elevator.getState());
		assertSame(1, elevator.getFloor());
	}

	@Test
	public void testIsMoving() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms);
		assertSame(false, elevator.isMoving());
		elevator.setDirection(Direction.UP);
		elevator.nextState();
		assertSame(true, elevator.isMoving());
		elevator.nextState();//stopping
		elevator.nextState();//doors opening
		elevator.nextState();//open
		elevator.nextState();//doors closing
		elevator.nextState();//next processing
		elevator.nextState();
		assertSame(ElevatorStateMachine.Idle,elevator.getState());
		elevator.setDirection(Direction.DOWN);
		elevator.nextState();
		assertSame(true,elevator.isMoving());
		
	}
	
	@Test
	public void testIsStationary() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms);
		assertSame(true, elevator.isStationary());
		elevator.setDirection(Direction.UP);
		elevator.nextState();
		assertSame(false, elevator.isStationary());
		elevator.nextState();//stopping
		elevator.nextState();//doors opening
		elevator.nextState();//open
		elevator.nextState();//doors closing
		elevator.nextState();
		assertSame(ElevatorStateMachine.NextStopProcessing,elevator.getState());
		assertSame(true, elevator.isStationary());
	}
	
	@Test
	public void testGetDirection() {
		Logger logger = null;
		Elevator e = new Elevator(0,7,logger, tms);
		assertSame(Direction.AWAITING_NEXT_REQUEST, e.getDirection());
	}
	
	@Test
	public void testSetDirection() {
		Logger logger = null;
		Elevator e = new Elevator(0,7,logger, tms);
		e.setDirection(Direction.DOWN);
		assertSame(Direction.AWAITING_NEXT_REQUEST, e.getDirection());
		e.setDirection(Direction.UP);
		e.nextState();
		e.setDirection(Direction.UP);
		assertSame(2, e.getFloor());
		assertSame(Direction.AWAITING_NEXT_REQUEST, e.getDirection());
	}
	
	
	
}
