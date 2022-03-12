package app.ElevatorSubsystem.Elevator;

import java.io.Serializable;

import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;

/***
 * Used to transfer information from elevator to scheduler
 * @author benki
 */
public class ElevatorInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id;
	private int floor;
	private ElevatorStateMachine state;
	private Direction mostRecent;
	
	public ElevatorInfo(int id, int floor, ElevatorStateMachine state, Direction mostRecent){
		this.id = id;
		this.floor = floor;
		this.state = state;
		this.mostRecent = mostRecent;
	}
	
	public int getId() {
		return this.id;
	}
	
	public int getFloor() {
		return this.floor;
	}
	
	public ElevatorStateMachine getState() {
		return this.state;
	}
	
	public Direction getMostRecentDirection() {
		return this.mostRecent;
	}
	
	
	
	
}
