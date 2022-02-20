package app.ElevatorSubsystem.Elevator;

import app.ElevatorSubsystem.Direction.Direction;

/**
 * WORK IN PROGRESS - Eventually for GUI operations to indicate the status of the elevator with the lamp
 * @author Millan Wang
 *
 */
public class ElevatorLamp {
	

	private Direction lampDirection;

	public ElevatorLamp(){
		this.lampDirection = Direction.AWAITING_NEXT_REQUEST;
	}

	public Direction getElevatorLampDirection(){
		return this.lampDirection;
	}

	public void setElevatorLampDirection(Direction direction){
		this.lampDirection = direction;
	}
}
