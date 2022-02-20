package tests.ElevatorSubsystemTests.StateMachine;

import static org.junit.Assert.*;
import org.junit.Test;

import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.StateMachine.*;

public class ElevatorStateMachineTest {

	@Test
	public void testNextState() {
		ElevatorStateMachine state = ElevatorStateMachine.Idle;
		assertSame(Direction.AWAITING_NEXT_REQUEST, state.getDirection());
		assertSame(ElevatorStateMachine.Idle, state);
		assertSame("idle",state.toString());
		state = state.nextState();
		assertSame(ElevatorStateMachine.Idle, state);
		state.setDirection(Direction.UP);
		state = state.nextState();
		assertSame(ElevatorStateMachine.MoveUp, state);
		state.setDirection(Direction.UP);
		state = state.nextState();
		assertSame(ElevatorStateMachine.MoveUp, state);
		assertSame("moving up",state.toString());
		state = state.nextState();
		assertSame(ElevatorStateMachine.Stopping, state);
		assertSame("stopping",state.toString());
		state = state.nextState();
		assertSame(ElevatorStateMachine.DoorOpening, state);
		assertSame("door opening",state.toString());
		state = state.nextState();
		assertSame(ElevatorStateMachine.OpenDoor, state);
		assertSame("door open",state.toString());
		state = state.nextState();
		assertSame(ElevatorStateMachine.DoorClosing, state);
		assertSame("door closing",state.toString());
		state.setDirection(Direction.STOPPED_AT_FLOOR);
		state = state.nextState();
		assertSame(ElevatorStateMachine.DoorOpening, state);
		state = state.nextState();
		state = state.nextState();
		state = state.nextState();
		assertSame(ElevatorStateMachine.NextStopProcessing, state);
		assertSame("next stop processing", state.toString());
		// NSP going up
		state.setDirection(Direction.UP);
		state = state.nextState();
		assertSame(ElevatorStateMachine.MoveUp, state);
		// NSP going down
		state = ElevatorStateMachine.NextStopProcessing;
		state.setDirection(Direction.DOWN);
		state = state.nextState();
		assertSame(ElevatorStateMachine.MoveDown, state);
		state = ElevatorStateMachine.NextStopProcessing; 
		// NSP idle
		state = ElevatorStateMachine.NextStopProcessing;
		state = state.nextState();
		assertSame(ElevatorStateMachine.Idle, state);
		
		// idle going down
		state.setDirection(Direction.DOWN);
		state = state.nextState();
		assertSame(ElevatorStateMachine.MoveDown, state);
		
		// moving down
		state.setDirection(Direction.DOWN);
		state = state.nextState();
		assertSame(ElevatorStateMachine.MoveDown, state);
		assertSame("moving down", state.toString());
		
		//moving down to stopping
		state = state.nextState();
		assertSame(ElevatorStateMachine.Stopping, state);
		
		//idle current
		state = ElevatorStateMachine.Idle;
		state.setDirection(Direction.STOPPED_AT_FLOOR);
		state = state.nextState();
		assertSame(ElevatorStateMachine.DoorOpening, state); 
	}

}
