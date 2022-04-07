package tests.SchedulerTests;


import org.junit.Assert;
import org.junit.Test;

import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;
import app.Scheduler.ElevatorSpecificScheduler;
import app.Scheduler.ElevatorSpecificSchedulerState;

/**
 * ElevatorSpecificScheduler testing
 * These tests show the functionality of an individual scheduler
 * to handle new floorRequests and to show how the floors to visit 
 * progress with simulated elevator movement
 * 
 * @author Millan Wang
 *
 */
public class ElevatorSpecificSchedulerTests {
	
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
		eInfo = new ElevatorInfo(ELEVATOR_ID, 2,-1, null, Direction.UP);
		Assert.assertEquals(3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		//Now 4 should have been added to the getUpwardsFloorsToVisit
		Assert.assertEquals(2, esScheduler.getUpwardsFloorsToVisit().size());
		Assert.assertTrue(esScheduler.getUpwardsFloorsToVisit().contains(4));
		
		//Still at floor 2 in a different elevator state. Next floor still 3
		eInfo = new ElevatorInfo(ELEVATOR_ID, 2, -1, ElevatorStateMachine.Stopping, Direction.UP);
		Assert.assertEquals(3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Arrive at floor 3, next floor to visit should be 4
		eInfo = new ElevatorInfo(ELEVATOR_ID, 3,-1, null, Direction.UP);
		Assert.assertEquals(4,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Arrive at floor 4, no next floor to visit meaning -1 next floor
		eInfo = new ElevatorInfo(ELEVATOR_ID, 4,-1, null, Direction.UP);
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
		eInfo = new ElevatorInfo(ELEVATOR_ID, 7,-1, null, Direction.UP);
		Assert.assertEquals(9,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		Assert.assertTrue(esScheduler.getUpwardsFloorsToVisit().contains(9)); //Destination 9 is now known
		
		//Still at floor 7, Go up to 8 without stopping before dropping off at 9
		moveBetweenFloorsWithoutStopping(7,8,ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT);

		//Floor 9 - Drop off. Only active request now is 3->10. Going downwards to lowest up floor=3 which is only known up floor
		eInfo = new ElevatorInfo(ELEVATOR_ID, 9,-1, null, Direction.UP);
		Assert.assertEquals(3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT, esScheduler.getCurrentState());
		Assert.assertTrue(esScheduler.getUpwardsFloorsToVisit().contains(3)); 
		Assert.assertFalse(esScheduler.getUpwardsFloorsToVisit().contains(10)); //Doesn't know destination at this point 
		
		//Go down from floor 8->4 without stopping in the MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT state
		moveBetweenFloorsWithoutStopping(8,4,ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT);
		
		//Pickup at floor 3 - Next floor to visit should be 10 moving upwards
		eInfo = new ElevatorInfo(ELEVATOR_ID, 3,-1, null, Direction.DOWN);
		Assert.assertEquals(10,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Still at floor 9, Move up to 9 without stopping
		moveBetweenFloorsWithoutStopping(3,9,ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT);
		
		//Drop off at floor 10 - No more requests to -1 next floor
		eInfo = new ElevatorInfo(ELEVATOR_ID, 10,-1, null, Direction.UP);
		Assert.assertEquals(-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		
		//If elevator somehow moves without scheduler instruction, still no next floor to visit
		eInfo = new ElevatorInfo(ELEVATOR_ID, 11,-1, null, Direction.UP);
		Assert.assertEquals(-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));	
	}
	
	/**
	 * Simulates the elevator progression with a lot of incoming down requests
	 */
	@Test
	public void test_DownRequestSeries() {
		this.esScheduler = new ElevatorSpecificScheduler(ELEVATOR_ID);
		//Awaiting elevator requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Add some downwards requests
		esScheduler.addRequest(21, 4, 0);
		esScheduler.addRequest(13, 11, 0);
		
		//Still awaiting elevator requests with active floor requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Currently at floor 1, getDownwardsFloorsToVisit doesn't know that 4&11 are destinations 
		Assert.assertEquals(2, esScheduler.getDownwardsFloorsToVisit().size());
		Assert.assertEquals(4,esScheduler.getActiveNumberOfStopsCount());
		
		//Note that elevator is currently at floor 1 and that the next floor is the highest down to visit, 21
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1,-1, null, Direction.DOWN);
		Assert.assertEquals(21,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT, esScheduler.getCurrentState());
		
		//Move up to floor 20 without stopping 
		moveBetweenFloorsWithoutStopping(1,20,ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT);
		
		//Stop at floor 21 to start going down to 13
		eInfo = new ElevatorInfo(ELEVATOR_ID, 21,-1, null, Direction.DOWN);
		Assert.assertEquals(13,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Move down to 14 without stopping
		moveBetweenFloorsWithoutStopping(21,14,ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT);
		
		//Pick up at 13, next floor to visit is 11
		eInfo = new ElevatorInfo(ELEVATOR_ID, 13,-1, null, Direction.DOWN);
		Assert.assertEquals(11,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Go down to 12
		moveBetweenFloorsWithoutStopping(13,12,ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT);
		
		//pickup at 11, next stop is 4
		eInfo = new ElevatorInfo(ELEVATOR_ID, 11,-1, null, Direction.DOWN);
		Assert.assertEquals(4,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Go down to 5
		moveBetweenFloorsWithoutStopping(11,5,ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT);
		
		//Drop off at 4, No more stops and -1 next floor
		eInfo = new ElevatorInfo(ELEVATOR_ID, 4,-1, null, Direction.DOWN);
		Assert.assertEquals(-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
	}
	
	/**
	 * Tests the edge cases of an incoming down request while the elevator is in the moving down to lowest Up floor state
	 */
	@Test
	public void test_IncomingDownRequestsWhenMovingDownToLowestUp() {
		this.esScheduler = new ElevatorSpecificScheduler(ELEVATOR_ID);
		//Awaiting elevator requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Add an up request
		esScheduler.addRequest(1, 22, 0);
		
		//Still awaiting elevator requests with active floor requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Currently at floor 1, getDownwardsFloorsToVisit doesn't know that 4&11 are destinations 
		Assert.assertEquals(1, esScheduler.getUpwardsFloorsToVisit().size());
		Assert.assertEquals(2, esScheduler.getActiveNumberOfStopsCount());
		
		//Note that elevator is currently at floor 1 and that the next floor is the highest down to visit, 21
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1,-1, null, Direction.DOWN);
		Assert.assertEquals(22,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Go down to 21
		moveBetweenFloorsWithoutStopping(1,21,ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT);
		
		//Add an up request starting below current floor
		esScheduler.addRequest(1, 22, 0);
		
		//Drop off at floor 22
		eInfo = new ElevatorInfo(ELEVATOR_ID, 22,-1, null, Direction.DOWN);
		Assert.assertEquals(1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT, esScheduler.getCurrentState());
		
		//Go down to floor 10
		moveBetweenFloorsWithoutStopping(22,10,ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT);
		
		//Schedule a down request starting below 10, and another starting above 10
		esScheduler.addRequest(9, 1, 0);
		esScheduler.addRequest(19, 1, 0);
		
		//Next stop should still be the 9 and we will service downwards to get there
		eInfo = new ElevatorInfo(ELEVATOR_ID, 10,-1, null, Direction.DOWN);
		Assert.assertEquals(9,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Pickup at 9 to get there
		eInfo = new ElevatorInfo(ELEVATOR_ID, 9,-1, null, Direction.DOWN);
		Assert.assertEquals(1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		
		//Go down to floor 2
		moveBetweenFloorsWithoutStopping(9,2,ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT);
		
		//Drop off and pick up at floor 1
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1,-1, null, Direction.DOWN);
		Assert.assertEquals(19,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT, esScheduler.getCurrentState());
		
		//Move up to 18
		moveBetweenFloorsWithoutStopping(1,18,ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT);
		
		//Pick up at 19
		eInfo = new ElevatorInfo(ELEVATOR_ID, 19,-1, null, Direction.DOWN);
		Assert.assertEquals(1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Move down to 2
		moveBetweenFloorsWithoutStopping(19,2,ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT);
		
		//Drop off at 1, no more requests to deal with
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1,-1, null, Direction.DOWN);
		Assert.assertEquals(-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
	}
	
	/**
	 * Tests the edge cases of an incoming up request while the elevator is in the Moving up to highest down floor state
	 */
	@Test
	public void test_IncomingUpRequestsWhenMovingUpToHighestDown() {
		this.esScheduler = new ElevatorSpecificScheduler(ELEVATOR_ID);
		//Awaiting elevator requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Add a downwards request that is above the current floor
		esScheduler.addRequest(21, 4, 0);
		
		//Still awaiting elevator requests with active floor requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Currently at floor 1, getDownwardsFloorsToVisit doesn't know that 4 is a destination
		Assert.assertEquals(1, esScheduler.getDownwardsFloorsToVisit().size());
		Assert.assertEquals(2,esScheduler.getActiveNumberOfStopsCount());
		
		//Note that elevator is currently at floor 1 and that the next floor is the highest down to visit, 21
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1,-1, null, Direction.DOWN);
		Assert.assertEquals(21,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT, esScheduler.getCurrentState());
		
		//Move up to floor 10 without stopping 
		moveBetweenFloorsWithoutStopping(1,10,ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT);
		
		//Add an up request above the current floor. Should start to service upwards floors to visit
		esScheduler.addRequest(11, 12, 0);
		eInfo = new ElevatorInfo(ELEVATOR_ID, 10,-1, null, Direction.DOWN);
		Assert.assertEquals(11,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Pickup and go to next stop is destination
		eInfo = new ElevatorInfo(ELEVATOR_ID, 11,-1, null, Direction.DOWN);
		Assert.assertEquals(12,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Drop off and resume going up to start servicing downs
		eInfo = new ElevatorInfo(ELEVATOR_ID, 12,-1, null, Direction.DOWN);
		Assert.assertEquals(21,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT, esScheduler.getCurrentState());
		
		//Move up to floor 15
		moveBetweenFloorsWithoutStopping(12,15,ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT);
		
		
		//Schedule a new up request starting below current floor
		esScheduler.addRequest(1, 20, 0);
		eInfo = new ElevatorInfo(ELEVATOR_ID, 15,-1, null, Direction.DOWN);
		Assert.assertEquals(21,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT, esScheduler.getCurrentState());
		moveBetweenFloorsWithoutStopping(15,20,ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT);
		
		
		//Pickup at floor 21, then next stop if floor 4 to drop off
		eInfo = new ElevatorInfo(ELEVATOR_ID, 21,-1, null, Direction.DOWN);
		Assert.assertEquals(4,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Move up to floor 5
		moveBetweenFloorsWithoutStopping(21,5,ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT);
		
		//Drop off at floor 4, now move down to floor 1 to pickup to go to 20
		eInfo = new ElevatorInfo(ELEVATOR_ID, 4,-1, null, Direction.UP);
		Assert.assertEquals(1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT, esScheduler.getCurrentState());
	
		//Move down to floor 2
		moveBetweenFloorsWithoutStopping(4,2,ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT);
		
		//Pickup at floor 1. Now we're going to 20
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1,-1, null, Direction.UP);
		Assert.assertEquals(20,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Move up to 19
		moveBetweenFloorsWithoutStopping(1,19,ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT);
		
		//Drop off at 20. No more requests
		eInfo = new ElevatorInfo(ELEVATOR_ID, 20,-1, null, Direction.UP);
		Assert.assertEquals(-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
	}
	
	/**
	 * Test handles all possible floor requests made at the same time. Every possible start floor to every possible destination floor
	 */
	@Test
	public void test_ManyRequestsToStart() {
		this.esScheduler = new ElevatorSpecificScheduler(ELEVATOR_ID);
		//Awaiting elevator requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
		
		//Add every possible request 
		int floorCount= (new Config("local.properties")).getInt("floor.highestFloorNumber"); 
		for (int i = 1 ; i<=floorCount ; i++) {
			for (int j = 1; j<=floorCount; j++) {
				if (i!=j) {
					esScheduler.addRequest(i, j, 0);
				}
			}	
		}
		
	
		//No destination floors are currently known, only start floors
		//There should startFloors-1 floor to visit in each direction. The highest floor is unknown for up, floor 1 is unknown for down cause you can't start those requests on those floors
		Assert.assertEquals(floorCount-1, esScheduler.getUpwardsFloorsToVisit().size());
		Assert.assertEquals(floorCount-1, esScheduler.getDownwardsFloorsToVisit().size());
		//Every floor will be visited in both directions
		Assert.assertEquals(floorCount*2,esScheduler.getActiveNumberOfStopsCount());
		
		//Still awaiting elevator requests with active floor requests
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());

		//Visit every floor on the way to the top floor while going upwards
		for (int i = 1; i<floorCount; i++) {
			eInfo = new ElevatorInfo(ELEVATOR_ID, i,-1, null, Direction.UP);
			Assert.assertEquals(i+1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
			Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		}
		//Reached top floor going up, next stop 2nd highest floor cause we're switching directions
		eInfo = new ElevatorInfo(ELEVATOR_ID, floorCount,-1, null, Direction.UP);
		Assert.assertEquals(floorCount-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		
		//Visit every floor on the way down to the bottom floor
		for (int i = floorCount; i>1; i--) {
			eInfo = new ElevatorInfo(ELEVATOR_ID, i,-1, null, Direction.DOWN);
			Assert.assertEquals(i-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
			Assert.assertEquals(ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT, esScheduler.getCurrentState());
		}
		//Reached bottom floor going down, No more floors to visit so negative 1 next floor
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1,-1, null, Direction.DOWN);
		Assert.assertEquals(-1,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		Assert.assertEquals(ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST, esScheduler.getCurrentState());
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
		eInfo = new ElevatorInfo(ELEVATOR_ID, 1,-1, null, Direction.UP);
		Assert.assertEquals(7,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		
		
		//Now schedule a PERMANENT error request
		esScheduler.addRequest(6, 9, 2);
		Assert.assertEquals(ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE, esScheduler.getCurrentState());
		//Next floor to visit with permanent error is -3
		Assert.assertEquals(-3,esScheduler.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		//The permanent error request does not get scheduled to add new floors to visit cause this elevator is dead
		Assert.assertEquals(3, esScheduler.getActiveNumberOfStopsCount());

		
		//Even if the elevator somehow moves, it will still always have -3 for next floor to visit in the PERMANENT_OUT_OF_SERVICE state
		moveBetweenFloorsWithoutStopping(21,7,ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE);
		eInfo = new ElevatorInfo(ELEVATOR_ID, 5,-1, null, Direction.UP);
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
			eInfo = new ElevatorInfo(ELEVATOR_ID, i,-1, null, direction);
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
