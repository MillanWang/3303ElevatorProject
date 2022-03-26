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
	public void test_IdSetProperly(){
		this.esScheduler = new ElevatorSpecificScheduler(ELEVATOR_ID);
		Assert.assertTrue(esScheduler.getElevatorID() == 1);
	}
	
	
	/**
	 * PATH OF TRAVEL
	 * Make 2 up requests 2->4 and 3->4
	 * travel 1,2,3,4 - No more floors to visit now
	 * Add 2 up requests 7->9, 3->10
	 * travel 4,5,6,7pickup,8,9 in servicing up list state
	 * travel 9,8,7,6,5,4,3pickup in moving down to lowest up request state
	 * travel 3,4,5,6,7,8,9,10
	 * No more active requests
	 */
	@Test
	public void test_UpRequestSeries(){
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
		
		//Pickup at floor 2, next floor to visit should be 3
		eInfo = new ElevatorInfo(ELEVATOR_ID, 2, null, Direction.UP);
		Assert.assertEquals(3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		//Now 4 should have been added to the getUpwardsFloorsToVisit
		Assert.assertEquals(2, esScheduler.getUpwardsFloorsToVisit().size());
		Assert.assertTrue(esScheduler.getUpwardsFloorsToVisit().contains(4));
		
		//Still at floor 2 in a different elevator state. Next floor still 3
		eInfo = new ElevatorInfo(ELEVATOR_ID, 2, ElevatorStateMachine.Stopping, Direction.UP);
		Assert.assertEquals(3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Arrive at floor 3, next floor to visit should be 4
		eInfo = new ElevatorInfo(ELEVATOR_ID, 3, null, Direction.UP);
		Assert.assertEquals(4,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Arrive at floor 4, no next floor to visit meaning -1 next floor
		eInfo = new ElevatorInfo(ELEVATOR_ID, 4, null, Direction.UP);
		Assert.assertEquals(-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		

		//Add new upwards request starting above current floor 4
		esScheduler.addRequest(7, 9, 0);
		//Add new upwards request starting below current floor 4
		esScheduler.addRequest(3, 10, 0);
		//Still awaiting elevator requests with active floor requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Still at floor 4, Go up to 6 without stopping before picking up 7 
		moveBetweenFloorsWithoutStopping(4,6,ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT);

		//Floor 7 - Pickup 
		eInfo = new ElevatorInfo(ELEVATOR_ID, 7, null, Direction.UP);
		Assert.assertEquals(9,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		Assert.assertTrue(esScheduler.getUpwardsFloorsToVisit().contains(9)); //Destination 9 is now known
		
		//Still at floor 7, Go up to 8 without stopping before dropping off at 9
		moveBetweenFloorsWithoutStopping(7,8,ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT);

		//Floor 9 - Drop off. Only active request now is 3->10. Going downwards to lowest up floor=3 which is only known up floor
		eInfo = new ElevatorInfo(ELEVATOR_ID, 9, null, Direction.UP);
		Assert.assertEquals(3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT, esScheduler.getCurrentState());
		Assert.assertTrue(esScheduler.getUpwardsFloorsToVisit().contains(3)); 
		Assert.assertFalse(esScheduler.getUpwardsFloorsToVisit().contains(10)); //Doesn't know destination at this point 
		
		//Go down from floor 8->4 without stopping in the MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT state
		moveBetweenFloorsWithoutStopping(8,4,ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT);
		
		//Pickup at floor 3 - Next floor to visit should be 10 moving upwards
		eInfo = new ElevatorInfo(ELEVATOR_ID, 3, null, Direction.DOWN);
		Assert.assertEquals(10,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Still at floor 9, Move up to 9 without stopping
		moveBetweenFloorsWithoutStopping(3,9,ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT);
		
		//Drop off at floor 10 - No more requests to -1 next floor
		eInfo = new ElevatorInfo(ELEVATOR_ID, 10, null, Direction.UP);
		Assert.assertEquals(-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		
		//If elevator somehow moves without scheduler instruction, still no next floor to visit
		eInfo = new ElevatorInfo(ELEVATOR_ID, 11, null, Direction.UP);
		Assert.assertEquals(-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));	
	}
	
	@Test
	public void test_DownRequestSeries() {
		//TODO : Make it similar to the above up request series test but with only downs
	}
	
	@Test
	public void test_IncomingDownRequestsWhenMovingDownToLowestUp() {
		//TODO : while in MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT, prove that we can switch to servicing down if appropriate
	}
	
	@Test
	public void test_IncomingUpRequestsWhenMovingUpToHighestDown() {
		//TODO : directionally mirrored version of above test
	}
	
	@Test
	public void test_ManyRequestsToStart() {
		//TODO : Start with a bunch of requests. Service all of them in one go
	}
	
	@Test
	public void test_TemporaryOutOfService() {
		this.esScheduler = new ElevatorSpecificScheduler(ELEVATOR_ID);
		//Awaiting elevator requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Add a normal request in each direction
		esScheduler.addRequest(5, 1, 0);
		esScheduler.addRequest(1, 7, 0);
		
		//Still awaiting elevator requests with active floor requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		//Have 4 overall floors to visit 
		Assert.assertEquals(4, esScheduler.getActiveNumberOfStopsCount());
		
		//Get next floor to visit. Should be 7 as default is to start at 1 going upwards
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1, null, Direction.UP);
		Assert.assertEquals(7,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		
		
		//Now schedule a temporary error request
		esScheduler.addRequest(6, 9, 1);
		Assert.assertEquals(ElevatorSpecificSchedulerState.TEMPORARY_OUT_OF_SERVICE, esScheduler.getCurrentState());
		//Next floor to visit with temporary error is -2
		Assert.assertEquals(-2,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		//Add 2 floors to visit which will be dealt with once back online
		Assert.assertEquals(5, esScheduler.getActiveNumberOfStopsCount());
		
		
		//TODO : Find out how to indicate a revive elevator message with elevator info
		// Getting next floor to visit after elevator revival with ElevatorInfo.
		// Resume scheduled plan with next floor=7
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1, null, Direction.UP);
//		Assert.assertEquals(7,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));TODO CHECK THAT NEXT FLOOR IS 7 WHEN TEMP ERROR DONE
//		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState()); TODO Uncomment out when elevator is back online
	}
	
	/**
	 * Test the functionality of permanently out of service elevators
	 */
	@Test
	public void test_PermanentOutOfService() {
		this.esScheduler = new ElevatorSpecificScheduler(ELEVATOR_ID);
		//Awaiting elevator requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Add a normal request in each direction
		esScheduler.addRequest(5, 1, 0);
		esScheduler.addRequest(1, 7, 0);
		
		//Still awaiting elevator requests with active floor requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		//Have 4 overall floors to visit 
		Assert.assertEquals(4, esScheduler.getActiveNumberOfStopsCount());
		
		//Get next floor to visit. Should be 7 as default is to start at 1 going upwards
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1, null, Direction.UP);
		Assert.assertEquals(7,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		
		
		//Now schedule a PERMANENT error request
		esScheduler.addRequest(6, 9, 2);
		Assert.assertEquals(ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE, esScheduler.getCurrentState());
		//Next floor to visit with permanent error is -3
		Assert.assertEquals(-3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		//Added 2 floors to visit which will not be visited cause this elevator is dead
		Assert.assertEquals(5, esScheduler.getActiveNumberOfStopsCount());
		
		//TODO Show that the process for reviving from temp errors does not work for permanent errors

		
		//Even if the elevator somehow moves, it will still always have -3 for next floor to visit in the PERMANENT_OUT_OF_SERVICE state
		moveBetweenFloorsWithoutStopping(21,7,ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE);
		eInfo = new ElevatorInfo(ELEVATOR_ID, 5, null, Direction.UP);
		Assert.assertEquals(-3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
	}
	
	
	/**
	 * Moves between the start and end floor without stopping and asserts that there
	 * is no change to the next floor to visit or ElevatorSpecificSchedulerState for the ride
	 * 
	 * @param startFloor the starting floor of the trip
	 * @param endFloor the ending floor of the trip. One away from a stop
	 * @param tripState The state in that the scheduler will be in for the whole trip
	 */
	private void moveBetweenFloorsWithoutStopping(int startFloor, int endFloor, ElevatorSpecificSchedulerState tripState) {
		if (startFloor==endFloor || startFloor<=0 || endFloor<=0) {
			Assert.fail("Illegal values for moving between floors");
		}
		Direction direction = startFloor < endFloor ? Direction.UP : Direction.DOWN;
		for (int i = startFloor;;) {
			eInfo = new ElevatorInfo(ELEVATOR_ID, i, null, direction);
			if (direction == Direction.UP) {
				//Upwards, next floor to visit must be above/greaterThan endFloor
				Assert.assertTrue(endFloor < esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
				Assert.assertEquals(tripState, esScheduler.getCurrentState());
				//Break once we reach top endFloor
				i++;
				if (i>=endFloor) break;
			} else {
				//Downwards, next floor to visit must be below/lessThan endFloor
				Assert.assertTrue(endFloor > esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
				Assert.assertEquals(tripState, esScheduler.getCurrentState());
				//Break once we reach the bottom endFloor
				i--;
				if (i<=endFloor) break;
			}
		}
	}
}
