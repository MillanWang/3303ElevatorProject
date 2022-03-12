package app.ElevatorSubsystem.Elevator;

import java.io.Serializable;

import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;

/***
 * Used to transfer information from elevator to scheduler
 * @author benki
 */
public class ElevatorInfo implements Serializable{

	private int id;
	private int floor;
	private ElevatorStateMachine state;
	
	public ElevatorInfo(int id, int floor, ElevatorStateMachine state){
		this.id = id;
		this.floor = floor;
		this.state = state;
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
	
	
}
