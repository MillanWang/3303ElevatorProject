package app.ElevatorSubsystem.StateMachine;

import app.ElevatorSubsystem.Direction.Direction;

public class ElevatorState {
	
	private ElevatorStateMachine state;
	private Direction direction; 
	
	public ElevatorState() {
		this.state = ElevatorStateMachine.Idle;
		this.direction = Direction.AWAITING_NEXT_REQUEST;
	}
	
	public ElevatorStateMachine getState() {
		return this.state;
	}
	
	public void nextState() {
		this.state = this.state.nextState(this.direction);
		this.direction = Direction.AWAITING_NEXT_REQUEST;
	}
	
	public Direction getDirection() {
		return this.direction;
	}
	
	public void setDirection(Direction d) {
		this.direction = d;
	}
	
}
