package app.Scheduler;

import java.util.LinkedList;
import java.util.TreeSet;

import app.Config.Config;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;


/**
 * Class to be an elevator specific to visit list to be sent from the scheduler to the elevator subsystem
 * @author Millan Wang
 *
 */
public class ElevatorSpecificScheduler {
	
	/**
	 * The ID of the elevator to receive the floorsToVisit set
	 */
	private int elevatorID;
	
	/**
	 * Most recently known floor 
	 */
	private int mostRecentFloor;
	
	/**
	 * Most recent next floor to visit value
	 */
	private int mostRecentNextFloor;
	
	/**
	 * To track which is the most recent scheduled direction
	 */
	private boolean isUpwards;
	
	/**
	 * Current state of the elevator specific scheduler
	 */
	private ElevatorSpecificSchedulerState currentState;
	
	/**
	 * Directional sets of floor numbers for the current elevator to visit
	 */
	private TreeSet<Integer> upwardsFloorsToVisit;
	private TreeSet<Integer> downwardsFloorsToVisit;
	
	/**
	 * Directional lists of sets of destinations to visit once arriving at the floor
	 */
	private LinkedList<TreeSet<Integer>> upwardsDestinationsPerFloor;
	private LinkedList<TreeSet<Integer>> downwardsDestinationsPerFloor;
	
	private TreeSet<Integer> pressedButtons;

	
	/**
	 * Constructor for the ElevatorSpecificFloorsToVisit class
	 * @param floorsToVisit
	 * @param elevatorID
	 */
	public ElevatorSpecificScheduler(int elevatorID) {
		this.elevatorID = elevatorID;
		this.mostRecentFloor = -1;
		mostRecentNextFloor=-1;
		this.isUpwards=true;
		this.upwardsFloorsToVisit = new TreeSet<Integer>();
		this.downwardsFloorsToVisit = new TreeSet<Integer>();
		this.pressedButtons = new TreeSet<Integer>();
		this.currentState = ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST;
		
		int highestFloorNumber= (new Config("local.properties")).getInt("floor.highestFloorNumber"); 
		//Directional destinations per floor
		this.upwardsDestinationsPerFloor= new LinkedList<TreeSet<Integer>>();
		this.downwardsDestinationsPerFloor = new LinkedList<TreeSet<Integer>>();
		//Populate them with TreeSets
		for (int i = 0; i<highestFloorNumber ; i++) {
			this.upwardsDestinationsPerFloor.add(new TreeSet<Integer>());
			this.downwardsDestinationsPerFloor.add(new TreeSet<Integer>());
		}
	}
	
	/**
	 * Returns the current elevator's ID
	 * @return the elevatorID
	 */
	public int getElevatorID() {
		return elevatorID;
	}

	/**
	 * @return the currentState of the current elevator scheduler
	 */
	public ElevatorSpecificSchedulerState getCurrentState() {
		return currentState;
	}

	/**
	 * @return the mostRecentFloor
	 */
	public int getMostRecentFloor() {
		return mostRecentFloor;
	}

	/**
	 * Returns the number of active number of remaining stops on the current elevator
	 * @return the number of active number of remaining stops on the current elevator
	 */
	public synchronized int getActiveNumberOfStopsCount() {
		TreeSet<Integer> upDestinationsSet = new TreeSet<Integer>();
		TreeSet<Integer> downDestinationsSet = new TreeSet<Integer>();
		for (TreeSet<Integer> dests : this.downwardsDestinationsPerFloor) {
			downDestinationsSet.addAll(dests);
		}
		for (TreeSet<Integer> dests : this.upwardsDestinationsPerFloor) {
			upDestinationsSet.addAll(dests);
		}
		
		upDestinationsSet.addAll(this.upwardsFloorsToVisit);
		downDestinationsSet.addAll(this.downwardsFloorsToVisit);
		
		return upDestinationsSet.size() + downDestinationsSet.size();
	}
	
	/**
	 * Returns the current elevators upwards set of floors to visit
	 * @return the floorsToVisit
	 */
	public TreeSet<Integer> getUpwardsFloorsToVisit() {
		return upwardsFloorsToVisit;
	}
	
	/**
	 * Returns the current elevators downwards set of floors to visit
	 * @return the floorsToVisit
	 */
	public TreeSet<Integer> getDownwardsFloorsToVisit() {
		return downwardsFloorsToVisit;
	}
	
	public void clearAllFloorsToVisit() {
		this.upwardsFloorsToVisit.clear();
		this.downwardsFloorsToVisit.clear();
		this.upwardsDestinationsPerFloor.clear();
		this.downwardsDestinationsPerFloor.clear();
	}
	
	/**
	 * @return the pressedButtons
	 */
	public TreeSet<Integer> getPressedButtons() {
		return pressedButtons;
	}

	/**
	 * Adds a request to the current elevator
	 * @param startFloor starting floor of the request
	 * @param destinationFloor destination floor of the request
	 */
	public synchronized void addRequest(int startFloor, int destinationFloor, int requestType) {
		if (requestType==1) {
			// Temporary error request type. Schedule incoming request to be dealt with when back online
//			this.previousStateBeforeTempError = currentState; //TODO Conclude if ESSched should know about temp errors
//			this.currentState = ElevatorSpecificSchedulerState.TEMPORARY_OUT_OF_SERVICE;
		} else if (requestType==2) {
			//Permanent error request type. Discard incoming request
			this.currentState = ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE;
			return;
		}
		
		
		boolean isUpwards = startFloor < destinationFloor;
		
		//Add elevator request to corresponding directionalToVisitSet if it isn't already queued
		if (isUpwards) {
			this.upwardsFloorsToVisit.add(startFloor);
			//Destination will only be known & added to floorsToVisitList once we arrive at the start floor
			this.upwardsDestinationsPerFloor.get(startFloor-1).add(destinationFloor);
		} else if (!isUpwards ) {
			this.downwardsFloorsToVisit.add(startFloor);
			//Destination will only be known & added to floorsToVisitList once we arrive at the start floor
			this.downwardsDestinationsPerFloor.get(startFloor-1).add(destinationFloor);
		}
		
		if (currentState==ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT) {
			if (!isUpwards && startFloor<=this.mostRecentFloor) {
				currentState = ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
			}
		} else if (currentState==ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT) {
			if (isUpwards && startFloor>=this.mostRecentFloor) {
				currentState = ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
			}
		}
	}
	

	
	/**
	 * Removes the floor to visit from the current elevator upon arrival
	 * @param floor that elevator just arrived to
	 */
	private void upwardsFloorIsVisited(Integer floor) {
		if (currentState==ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE)return;
		if (currentState==ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT) {
			if (floor>this.mostRecentNextFloor) return;
		}
		this.pressedButtons.remove(floor);
		this.upwardsFloorsToVisit.remove(floor);
		this.upwardsFloorsToVisit.addAll(this.upwardsDestinationsPerFloor.get(floor-1));
		this.pressedButtons.addAll(this.upwardsDestinationsPerFloor.get(floor-1));
		this.upwardsDestinationsPerFloor.get(floor-1).clear();
	}
	
	/**
	 * Removes the floor to visit from the current elevator upon arrival
	 * @param floor that elevator just arrived to
	 */
	private void downwardsFloorIsVisited(Integer floor) {
		if (currentState==ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE)return;
		if (currentState==ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT) {
			if (floor<this.mostRecentNextFloor) return;
		}
		this.pressedButtons.remove(floor);
		this.downwardsFloorsToVisit.remove(floor);
		this.downwardsFloorsToVisit.addAll(this.downwardsDestinationsPerFloor.get(floor-1));
		this.pressedButtons.addAll(this.downwardsDestinationsPerFloor.get(floor-1));
		this.downwardsDestinationsPerFloor.get(floor-1).clear();
	}

	/**
	 * Returns the floor to stop at for the current elevator
	 * @param elevatorCurrentFloor The floor that the elevator is currently at
	 * @return the floor number of the next floor to visit
	 */
	private int getNextFloorToVisit(int elevatorCurrentFloor) {

		//Permanent out of service
		if (this.currentState == ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE) {
			this.mostRecentNextFloor=-3;
			return this.mostRecentNextFloor;
		}
		
		//In service, temporary errors are dealt with on the elevator side
		int lowestUpStopAboveCurrent = this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).isEmpty() ? -1 : this.upwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).first();
		int lowestUpStopBelowCurrent =  this.upwardsFloorsToVisit.headSet(elevatorCurrentFloor, false).isEmpty() ? -1 : this.upwardsFloorsToVisit.headSet(elevatorCurrentFloor, false).first();
		int highestDownStopAboveCurrent = this.downwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).isEmpty() ? -1 : this.downwardsFloorsToVisit.tailSet(elevatorCurrentFloor, false).last();
		int highestDownStopBelowCurrent = this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor, false).isEmpty() ? -1 : this.downwardsFloorsToVisit.headSet(elevatorCurrentFloor, false).last();

			
		if (this.currentState == ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT) {
			//Check if there are downwards stops below the current floor
			if (highestDownStopBelowCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
				this.isUpwards = false;
				this.mostRecentNextFloor = highestDownStopBelowCurrent;
				return highestDownStopBelowCurrent;
			}
			//Check if there are upwards stops below the current floor
			if (lowestUpStopBelowCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT;
				this.isUpwards = true;
				this.mostRecentNextFloor = lowestUpStopBelowCurrent;
				return lowestUpStopBelowCurrent;
			}
			//Check if there are upwards stops above the current floor
			if (lowestUpStopAboveCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
				this.isUpwards = true;
				this.mostRecentNextFloor = lowestUpStopAboveCurrent;
				return lowestUpStopAboveCurrent;
			}
			
			//Check if there are downwards stops above the current floor
			if (highestDownStopAboveCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT;
				this.isUpwards = false;
				this.mostRecentNextFloor = highestDownStopAboveCurrent;
				return highestDownStopAboveCurrent;
			}
			//At this point, there are no more stops at all

			
			
		} else if (this.currentState == ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT) {
			//Check if there are upwards stops above the current floor
			if (lowestUpStopAboveCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
				this.isUpwards = true;
				this.mostRecentNextFloor = lowestUpStopAboveCurrent;
				return lowestUpStopAboveCurrent;
			}
			//Check if there are downwards stops above the current floor
			if (highestDownStopAboveCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT;
				this.isUpwards = false;
				this.mostRecentNextFloor = highestDownStopAboveCurrent;
				return highestDownStopAboveCurrent;
			}
			//Check if there are downwards stops below the current floor
			if (highestDownStopBelowCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
				this.isUpwards = false;
				this.mostRecentNextFloor = highestDownStopBelowCurrent;
				return highestDownStopBelowCurrent;
			}
			//Check if there are upwards stops below the current floor
			if (lowestUpStopBelowCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT;
				this.isUpwards = true;
				this.mostRecentNextFloor = lowestUpStopBelowCurrent;
				return lowestUpStopBelowCurrent;
			}
			//At this point, there are no more stops at all. Carry on to outside general case

		
		
		} else if (this.currentState == ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT) {
			//Check if there are downwards stops below the current floor
			if (highestDownStopBelowCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
				this.isUpwards = false;
				this.mostRecentNextFloor = highestDownStopBelowCurrent;
				return highestDownStopBelowCurrent;
			}
			//Check if there are upwards stops below the current floor
			if (lowestUpStopBelowCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT;
				this.isUpwards = true;
				this.mostRecentNextFloor = lowestUpStopBelowCurrent;
				return lowestUpStopBelowCurrent;
			}
			//Check if there are upwards stops above the current floor
			if (lowestUpStopAboveCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
				this.isUpwards = true;
				this.mostRecentNextFloor = lowestUpStopAboveCurrent;
				return lowestUpStopAboveCurrent;
			}
			//Check if there are downwards stops above the current floor
			if (highestDownStopAboveCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT;
				this.isUpwards = false;
				this.mostRecentNextFloor = highestDownStopAboveCurrent;
				return highestDownStopAboveCurrent;
			}
			//At this point, there are no more stops at all. Carry on to outside general case
	
			
			
		} else if (this.currentState == ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT) {
			//Check if there are upwards stops above the current floor
			if (lowestUpStopAboveCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
				this.isUpwards = true;
				this.mostRecentNextFloor = lowestUpStopAboveCurrent;
				return lowestUpStopAboveCurrent;
			}
			//Check if there are downwards stops above the current floor
			if (highestDownStopAboveCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT;
				this.isUpwards = false;
				this.mostRecentNextFloor = highestDownStopAboveCurrent;
				return highestDownStopAboveCurrent;
			}
			//Check if there are downwards stops below the current floor
			if (highestDownStopBelowCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
				this.isUpwards = false;
				this.mostRecentNextFloor = highestDownStopBelowCurrent;
				return highestDownStopBelowCurrent;
			}
			//Check if there are upwards stops below the current floor
			if (lowestUpStopBelowCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT;
				this.isUpwards = true;
				this.mostRecentNextFloor = lowestUpStopBelowCurrent;
				return lowestUpStopBelowCurrent;
			}
			//At this point, there are no more stops at all. Carry on to outside general case

			
			
		} else if (this.currentState == ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST) {
			//Check if there are down stops below the current floor
			if (highestDownStopBelowCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT;
				this.isUpwards = false;
				this.mostRecentNextFloor = highestDownStopBelowCurrent;
				return highestDownStopBelowCurrent;
			}
			//Check if there are up stops above the current floor
			if (lowestUpStopAboveCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT;
				this.isUpwards = true;
				this.mostRecentNextFloor = lowestUpStopAboveCurrent;
				return lowestUpStopAboveCurrent;
			}
			//Check if there are down stops above the current floor
			if (highestDownStopAboveCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT;
				this.isUpwards = false;
				this.mostRecentNextFloor = highestDownStopAboveCurrent;
				return highestDownStopAboveCurrent;
			}
			
			//Check if there are up stops below the current floor
			if (lowestUpStopBelowCurrent != -1) {
				this.currentState = ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT;
				this.isUpwards = true;
				this.mostRecentNextFloor = lowestUpStopBelowCurrent;
				return lowestUpStopBelowCurrent;
			}
			//At this point, there are no more stops at all. Carry on to outside general case
		}
		

		//No more floors to visit at all
		this.currentState = ElevatorSpecificSchedulerState.AWAITING_NEXT_ELEVATOR_REQUEST;
		return -1;
	}
	
	/**
	 * Handles a change in the elevator's information
	 * @param elevatorInfo
	 * @return
	 */
	public synchronized int handleElevatorInfoChange_returnNextFloorToVisit(ElevatorInfo elevatorInfo) {
		if (elevatorInfo.getFloor()<=0) return elevatorInfo.getFloor();
		
		if (this.isUpwards) {
			this.upwardsFloorIsVisited(elevatorInfo.getFloor());
		} else if (!this.isUpwards) {
			this.downwardsFloorIsVisited(elevatorInfo.getFloor());
		} else {
			System.err.println("Expecting only up and down for elevator info most recent direction... Need review");
			return -1000;
		}
		this.mostRecentFloor = elevatorInfo.getFloor();
		return this.getNextFloorToVisit(this.mostRecentFloor);
	}
	
	
	/**
	 * Returns a formatted string describing all current details about the current object
	 */
	@Override
    public String toString() {
		String returnString = "[Elevator "+this.elevatorID+" - ElevatorSpecificScheduler]\n";
		returnString+= "\tState : " + currentState + "\n";
		returnString+= "\tMost recent floor : " + (this.mostRecentFloor<=0?"(none)":this.mostRecentFloor) + "\n";
		returnString+= "\tUpwards floors to visit : " + this.upwardsFloorsToVisit.toString() + " (currently known)\n";
		returnString+= "\tDownwards floors to visit : " + this.downwardsFloorsToVisit.toString() + " (currently known)\n";
		returnString+= "\tTotal floors to visit : " + this.getActiveNumberOfStopsCount() + " (including currently unknown destinations)\n";
		return returnString;
	}
	
}
