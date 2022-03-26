package tests.ElevatorSubsystemTests.StateMachine;

import static org.junit.Assert.*;
import org.junit.Test;

import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.StateMachine.*;

public class ElevatorStateMachineTest {

	@Test
	public void testNextState() {
		ElevatorStateMachine state = ElevatorStateMachine.Idle;
		assertSame(ElevatorStateMachine.Idle, state);
		assertSame("idle",state.toString());
		state = state.nextState(Direction.AWAITING_NEXT_REQUEST);
		assertSame(ElevatorStateMachine.Idle, state);
		state = state.nextState(Direction.UP);
		assertSame(ElevatorStateMachine.MoveUp, state);
		state = state.nextState(Direction.UP);
		assertSame(ElevatorStateMachine.MoveUp, state);
		assertSame("moving up",state.toString());
		state = state.nextState(Direction.STOPPED_AT_FLOOR);
		assertSame(ElevatorStateMachine.Stopping, state);
		assertSame("stopping",state.toString());
		state = state.nextState(Direction.AWAITING_NEXT_REQUEST);
		assertSame(ElevatorStateMachine.DoorOpening, state);
		assertSame("door opening",state.toString());
		state = state.nextState(Direction.AWAITING_NEXT_REQUEST);
		assertSame(ElevatorStateMachine.OpenDoor, state);
		assertSame("door open",state.toString());
		state = state.nextState(Direction.AWAITING_NEXT_REQUEST);
		assertSame(ElevatorStateMachine.DoorClosing, state);
		assertSame("door closing",state.toString());
		state = state.nextState(Direction.STOPPED_AT_FLOOR);
		assertSame(ElevatorStateMachine.DoorOpening, state);
		state = state.nextState(Direction.AWAITING_NEXT_REQUEST);
		state = state.nextState(Direction.AWAITING_NEXT_REQUEST);
		state = state.nextState(Direction.AWAITING_NEXT_REQUEST);
		assertSame(ElevatorStateMachine.NextStopProcessing, state);
		assertSame("next stop processing", state.toString());
		// NSP going up
		state = state.nextState(Direction.UP);
		assertSame(ElevatorStateMachine.MoveUp, state);
		// NSP going down
		state = ElevatorStateMachine.NextStopProcessing;
		state = state.nextState(Direction.DOWN);
		assertSame(ElevatorStateMachine.MoveDown, state);
		state = ElevatorStateMachine.NextStopProcessing; 
		// NSP idle
		state = ElevatorStateMachine.NextStopProcessing;
		state = state.nextState(Direction.AWAITING_NEXT_REQUEST);
		assertSame(ElevatorStateMachine.Idle, state);
		
		// idle going down
		state = state.nextState(Direction.DOWN);
		assertSame(ElevatorStateMachine.MoveDown, state);
		
		// moving down
		state = state.nextState(Direction.DOWN);
		assertSame(ElevatorStateMachine.MoveDown, state);
		assertSame("moving down", state.toString());
		
		//moving down to stopping
		state = state.nextState(Direction.AWAITING_NEXT_REQUEST);
		assertSame(ElevatorStateMachine.Stopping, state);
		
		//idle current
		state = ElevatorStateMachine.Idle;
		state = state.nextState(Direction.STOPPED_AT_FLOOR);
		assertSame(ElevatorStateMachine.DoorOpening, state); 
	}

}
