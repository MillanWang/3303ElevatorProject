package app.ElevatorSubsystem.Direction;

import java.io.Serializable;

/**
 * SYSC 3303, Final Project
 * Direction.java
 * Purpose: Direction of the elevator used to determine next state
 *
 * @author Ben Kittilsen
 * */
public enum Direction implements Serializable{
	UP,
	DOWN,
	STOPPED_AT_FLOOR,
	AWAITING_NEXT_REQUEST
}
