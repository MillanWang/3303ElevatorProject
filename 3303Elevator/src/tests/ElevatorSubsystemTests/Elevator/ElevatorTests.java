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

public class ElevatorTests {
	
	@Test
	public void testElevator() {
		Elevator elevator = new Elevator(7,1);
		assertSame(ElevatorStateMachine.Idle,elevator.getState());
	}

	
	@Test
	public void testGetFloor() {
		Elevator elevator = new Elevator(7,1);
		assertSame(1,elevator.getFloor());
	}

	@Test
	public void testGetState() {
		Elevator elevator = new Elevator(7,1);
		assertSame(ElevatorStateMachine.Idle,elevator.getState());
	}
	
	@Test
	public void testnextState() {
		Elevator elevator = new Elevator(7,1);
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
		Elevator elevator = new Elevator(7,1);
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
		Elevator elevator = new Elevator(7,1);
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
		Elevator e = new Elevator(7,1);
		assertSame(Direction.NONE, e.getDirection());
	}
	
	@Test
	public void testSetDirection() {
		Elevator e = new Elevator(2,1);
		e.setDirection(Direction.DOWN);
		assertSame(Direction.NONE, e.getDirection());
		e.setDirection(Direction.UP);
		e.nextState();
		e.setDirection(Direction.UP);
		assertSame(2, e.getFloor());
		assertSame(Direction.NONE, e.getDirection());
	}
	
	
	
}
