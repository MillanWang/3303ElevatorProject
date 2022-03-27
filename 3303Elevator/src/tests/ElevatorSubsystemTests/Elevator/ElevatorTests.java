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
import app.Config.Config;

public class ElevatorTests {
	private TimeManagementSystem tms = new TimeManagementSystem(1, new Logger(new Config("local.properties")));
	
	@Test
	public void testElevator() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms, null, null);
		assertSame(ElevatorStateMachine.Idle,elevator.getState());
	}

	
	@Test
	public void testGetFloor() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms, null, null);
		assertSame(1,elevator.getFloor());
	}

	@Test
	public void testGetState() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms, null, null);
		assertSame(ElevatorStateMachine.Idle,elevator.getState());
	}
	
	@Test
	public void testnextState() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms, null, null);
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
		Elevator elevator = new Elevator(0, 7,logger, tms, null, null);
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
		Elevator elevator = new Elevator(0, 7,logger, tms, null, null);
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
		Elevator elevator = new Elevator(0, 7,logger, tms, null, null);
		assertSame(Direction.AWAITING_NEXT_REQUEST, elevator.getDirection());
	}
	
	@Test
	public void testSetDirection() {
		Logger logger = null;
		Elevator elevator = new Elevator(0, 7,logger, tms, null, null);
		elevator.setDirection(Direction.DOWN);
		assertSame(Direction.AWAITING_NEXT_REQUEST, elevator.getDirection());
		elevator.setDirection(Direction.UP);
		elevator.nextState();
		elevator.setDirection(Direction.UP);
		assertSame(2, elevator.getFloor());
		assertSame(Direction.UP, elevator.getDirection());
	}
	
}
