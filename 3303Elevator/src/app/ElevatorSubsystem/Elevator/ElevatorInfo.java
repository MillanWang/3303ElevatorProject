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
	
	private int id, floor, error;
	private ElevatorStateMachine state;
	private Direction mostRecent;
	
	public ElevatorInfo(int id, int floor, int error, ElevatorStateMachine state, Direction mostRecent){
		this.id = id;
		this.floor = floor;
		this.error = error;
		this.state = state;
		this.mostRecent = mostRecent;
	}
	
	public int getId() {
		return this.id;
	}
	
	public int getFloor() {
		return this.floor;
	}
	
	public int getError() {
		return this.error;
	}
	
	public ElevatorStateMachine getState() {
		return this.state;
	}
	
	public Direction getMostRecentDirection() {
		return this.mostRecent;
	}
	
	@Override
	public String toString() {
		String outline = "*********************************************";
		String returnString = outline + "\n";
		returnString+= "[Elevator "+this.id+" - Elevator Info]\n";
		returnString+= "\tState                 : " + this.state + "\n";
		returnString+= "\tError                 : " + this.error + "\n";
		returnString+= "\tMost recent floor     : " + this.floor + "\n";
		returnString+= "\tMost recent direction : " + this.mostRecent + "\n";
		returnString+= outline;
		return returnString;
	}
}
