package tests.SchedulerTests;


import org.junit.Assert;
import org.junit.Test;

import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;
import app.Scheduler.ElevatorSpecificScheduler;
import app.Scheduler.ElevatorSpecificSchedulerState;

public class ElevatorSpecificFloorsToVisitTest {
	
	private static final int ELEVATOR_ID = 1;
	private ElevatorSpecificScheduler esScheduler;
	private ElevatorInfo eInfo;

	/**
	 * Ensure that the getters return the values defined in the constructor
	 */
	@Test
	public void testElevatorSpecificFloorsToVisit_IdSetProperly(){
		this.esScheduler = new ElevatorSpecificScheduler(ELEVATOR_ID);
		Assert.assertTrue(esScheduler.getElevatorID() == 1);
	}
	
	
	@Test
	public void testElevatorSpecificFloorsToVisit_A_Shaped(){
		this.esScheduler = new ElevatorSpecificScheduler(ELEVATOR_ID);
		//Awaiting elevator requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Add 2 upwards requests
		esScheduler.addRequest(2, 4, 0);
		esScheduler.addRequest(3, 4, 0);
		
		//Still awaiting elevator requests with active floor requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Currently at floor 1, getUpwardsFloorsToVisit doesn't know that 4 is a destination 
		Assert.assertEquals(2, esScheduler.getUpwardsFloorsToVisit().size());
		Assert.assertEquals(3,esScheduler.getActiveNumberOfStopsCount());
		
		//Arrive at floor 2, next floor to visit should be 3
		eInfo = new ElevatorInfo(1, 2, null, Direction.UP);
		Assert.assertEquals(3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		//Now 4 should have been added to the getUpwardsFloorsToVisit
		Assert.assertEquals(2, esScheduler.getUpwardsFloorsToVisit().size());
		Assert.assertTrue(esScheduler.getUpwardsFloorsToVisit().contains(4));
		
		//Still at floor 2 in a different elevator state. Next floor still 3
		eInfo = new ElevatorInfo(1, 2, ElevatorStateMachine.Stopping, Direction.UP);
		Assert.assertEquals(3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Arrive at floor 3, next floor to visit should be 4
		eInfo = new ElevatorInfo(1, 3, null, Direction.UP);
		Assert.assertEquals(4,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Arrive at floor 4, no next floor to visit meaning -1 next floor
		eInfo = new ElevatorInfo(1, 4, null, Direction.UP);
		Assert.assertEquals(-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		

		//Add new upwards request starting above current floor 4
		esScheduler.addRequest(7, 9, 0);
		//Add new upwards request starting below current floor 4
		esScheduler.addRequest(3, 10, 0);
		//Still awaiting elevator requests with active floor requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		
		//Still at floor 4, Go up to 7 first 
		eInfo = new ElevatorInfo(1, 4, null, Direction.UP);
		Assert.assertEquals(7,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		//Floor 5
		eInfo = new ElevatorInfo(1, 5, null, Direction.UP);
		Assert.assertEquals(7,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		//Floor 6
		eInfo = new ElevatorInfo(1, 6, null, Direction.UP);
		Assert.assertEquals(7,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		Assert.assertFalse(esScheduler.getUpwardsFloorsToVisit().contains(10)); //Destination not known yet
		//Floor 7 - Pickup 
		eInfo = new ElevatorInfo(1, 7, null, Direction.UP);
		Assert.assertEquals(9,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		Assert.assertTrue(esScheduler.getUpwardsFloorsToVisit().contains(9)); //Destination now known
		//Floor 8  
		eInfo = new ElevatorInfo(1, 8, null, Direction.UP);
		Assert.assertEquals(9,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		//Floor 9 - Drop off  
		eInfo = new ElevatorInfo(1, 9, null, Direction.UP);
		Assert.assertEquals(9,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
	}
}
