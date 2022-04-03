package app.Scheduler;

/**
 * Enum to track the state of the elevator specific scheduler
 * 
 * @author Millan Wang
 *
 */
public enum ElevatorSpecificSchedulerState {
	AWAITING_NEXT_ELEVATOR_REQUEST,
	SERVICING_DOWNWARDS_FLOORS_TO_VISIT,
	SERVICING_UPWARDS_FLOORS_TO_VISIT,
	MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT,
	MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT,
	PERMANENT_OUT_OF_SERVICE	
}
