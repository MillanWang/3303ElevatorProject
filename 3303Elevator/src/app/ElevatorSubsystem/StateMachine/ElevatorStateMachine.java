package app.ElevatorSubsystem.StateMachine;

import static org.junit.Assert.assertSame;

import app.ElevatorSubsystem.Direction.Direction;

/**
 * SYSC 3303, Final Project
 * ElevatorStateMachine.java
 * Purpose: StateMachine for the elevator
 * 
 * @author Ben Kittilsen
 * */
public enum ElevatorStateMachine {
	/**
	 * idle waits to move up, down, or open doors. if no direction 
	 * stays idle
	 * */
	Idle {
		@Override
		public ElevatorStateMachine nextState() {
			ElevatorStateMachine state; 

			if(this.getDirection() == Direction.UP) {
				state = MoveUp;
			}else if(this.getDirection() == Direction.DOWN) {
				state = MoveDown;
			}else if(this.getDirection() == Direction.CURRENT){
				state = DoorOpening;
			}else {
				state = Idle;
			}
			
			this.setDirection(Direction.NONE);
			return state;
		}
		
		@Override
		public String toString() {
			return "idle";
		}
	},
	/**
	 * Moves up, if direction set to up moves up again
	 * */
	MoveUp{
		@Override
		public ElevatorStateMachine nextState() {
			ElevatorStateMachine state = Stopping;
			
			if(this.getDirection() == Direction.UP) {
				state = MoveUp;
			}
			
			this.setDirection(Direction.NONE);
			return state;
		}
		
		@Override
		public String toString() {
			return "moving up";
		}
	},
	/**
	 * Moves down, if direction is down moves down again
	 * */
	MoveDown{
		@Override
		public ElevatorStateMachine nextState() {
			ElevatorStateMachine state = Stopping;
			
			if(this.getDirection() == Direction.DOWN) {
				state = MoveDown;
			}
			
			this.setDirection(Direction.NONE);
			return state;
		}
		
		@Override
		public String toString() {
			return "moving down";
		}
	},
	/**
	 * from moving needs time to stop, before doors open
	 * */
	Stopping{
		@Override
		public ElevatorStateMachine nextState() {
			return DoorOpening;
		}
		
		@Override
		public String toString() {
			return "stopping";
		}
	},
	/**
	 * starting time for the doors to open
	 * */
	DoorOpening{
		@Override
		public ElevatorStateMachine nextState() {
			return OpenDoor;
		}
		
		@Override
		public String toString() {
			return "door opening";
		}
	},
	/**
	 * doors are open for some period
	 * */
	OpenDoor{
		@Override
		public ElevatorStateMachine nextState() {
			return DoorClosing;
		}
		
		@Override
		public String toString() {
			return "door open";
		}
	},
	/**
	 * doors closed end of time
	 * */
	DoorClosing{
		@Override
		public ElevatorStateMachine nextState() {
			ElevatorStateMachine state; 
			if(this.getDirection() == Direction.CURRENT) {
				state = DoorOpening; 
			}else {
				state = NextStopProcessing;
			}
			
			this.setDirection(Direction.NONE);
			return state;
		}
		
		@Override
		public String toString() {
			return "door closing";
		}
	},
	/**
	 * waiting for next processing request
	 * */
	NextStopProcessing{
		@Override
		public ElevatorStateMachine nextState() {
			ElevatorStateMachine state;
			
			if(this.getDirection()== Direction.UP) {
				state = MoveUp;
			}else if(this.getDirection() == Direction.DOWN) {
				state = MoveDown;
			}else {
				state = Idle;
			}
			
			this.setDirection(Direction.NONE);
			return state;
		}
		
		@Override
		public String toString() {
			return "next stop processing";
		}
	};

	public abstract ElevatorStateMachine nextState();
	public abstract String toString();
	
	private Direction direction = Direction.NONE;
	public Direction getDirection() {
		return direction;
	}
	
	public void setDirection(Direction d) {
		direction = d;
	}
}
