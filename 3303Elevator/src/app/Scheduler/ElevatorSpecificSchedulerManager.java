package app.Scheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;

public class ElevatorSpecificSchedulerManager {
	private HashMap<Integer, ElevatorSpecificScheduler> allElevatorSpecificSchedulers;
	private LinkedList<ElevatorInfo> mostRecentAllElevatorInfo;
	private ElevatorSpecificSchedulerManagerState currentState;
	private boolean useSimpleLeastLoadAlgorithm;
	
	
	
	public ElevatorSpecificSchedulerManager (boolean useSimpleLeastLoadAlgorithm) {
		this.useSimpleLeastLoadAlgorithm=useSimpleLeastLoadAlgorithm;
		this.currentState = ElevatorSpecificSchedulerManagerState.AWAITING_NEXT_ELEVATOR_REQUEST;
		
		//Creating an elevator specific scheduler for each elevator
		this.allElevatorSpecificSchedulers = new HashMap<Integer, ElevatorSpecificScheduler>();
		for (int i = 1; i <= (new Config("local.properties")).getInt("elevator.total.number") ; i++) {
			this.allElevatorSpecificSchedulers.put(i, new ElevatorSpecificScheduler(i));
		}
	}
	
	/**
	 * Getter for most recent list of ElevatorInfo
	 * @return the allElevatorInfo
	 */
	public LinkedList<ElevatorInfo> getMostRecentAllElevatorInfo() {
		return mostRecentAllElevatorInfo;
	}

	/**
	 * Schedules a floor request to an algorithmically determined elevatorID
	 * @param startFloor
	 * @param destinationFloor
	 * @param requestType
	 * @return
	 */
	public int scheduleFloorRequest(int startFloor, int destinationFloor, int requestType) {
		if (currentState==ElevatorSpecificSchedulerManagerState.ALL_ELEVATORS_OUT_OF_SERVICE||checkIfAllElevatorsArePermanentError()) {
			return -1;
		}
		int elevatorID_toSchedule=-1;
		if (this.useSimpleLeastLoadAlgorithm) {
			elevatorID_toSchedule=getBestElevatorId_SimpleLeastLoadAlgorithm();
		} else {
			elevatorID_toSchedule=getBestElevatorId_DirectionalPriorityAlgorithm(startFloor, startFloor<destinationFloor);
		}
		this.allElevatorSpecificSchedulers.get(elevatorID_toSchedule).addRequest(startFloor, destinationFloor, requestType);
		return elevatorID_toSchedule;
	}
	
	/**
	 * Checks if all elevators are currently in an error state
	 * @return if all elevators are in an error state
	 */
	private boolean checkIfAllElevatorsArePermanentError() {
		//Iterate through all ElevatorSpecificScheduler states. Return false if any are not out of service
		for (Integer i : this.allElevatorSpecificSchedulers.keySet()) {
			if (this.allElevatorSpecificSchedulers.get(i).getCurrentState()!=ElevatorSpecificSchedulerState.TEMPORARY_OUT_OF_SERVICE||
				this.allElevatorSpecificSchedulers.get(i).getCurrentState()!=ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE) {
				this.currentState = ElevatorSpecificSchedulerManagerState.AWAITING_NEXT_ELEVATOR_REQUEST;
				return false;
			}
		}
		this.currentState = ElevatorSpecificSchedulerManagerState.ALL_ELEVATORS_OUT_OF_SERVICE;
		return true;
	}
	
	/**
	 * Backup algorithm used to assign requests to the elevators with the least floors to visit, regardless of position. 
	 * @return ID of the elevator with the least floors to visit
	 */
	private synchronized int getBestElevatorId_SimpleLeastLoadAlgorithm() {
		Integer minFloorsToVisitElevatorID = 1;
		for (Integer id : this.allElevatorSpecificSchedulers.keySet()) {
			if (this.allElevatorSpecificSchedulers.get(id).getActiveNumberOfStopsCount() <= this.allElevatorSpecificSchedulers.get(minFloorsToVisitElevatorID).getActiveNumberOfStopsCount()) {
				minFloorsToVisitElevatorID = id;
			}
		}
		return minFloorsToVisitElevatorID;
	}
	
	/**
	 * Returns the ID of the best elevator to handle this request
	 * @param startFloor The starting floor of the request
	 * @param isUpwards if the request is upwards
	 * @return The ID of the most suitable elevator for this request
	 */
	private synchronized int getBestElevatorId_DirectionalPriorityAlgorithm(int startFloor, boolean isUpwards) { //TODO : Feature flag to determine if we shall use easy or hard algorithm
		if (isUpwards) {
			//Upwards. First check if there are any upwards or parked elevators under us
			if (this.findClosestElevatorBelowWithState(startFloor, Direction.UP)!=-1) {
				return this.findClosestElevatorBelowWithState(startFloor, Direction.UP);
			} else if (this.findClosestElevatorBelowWithState(startFloor, Direction.AWAITING_NEXT_REQUEST)!=-1) {
				return this.findClosestElevatorBelowWithState(startFloor, Direction.AWAITING_NEXT_REQUEST);
			} else {
				//No upwards or parked elevators below start floor. Randomly select one
				Collections.shuffle(this.mostRecentAllElevatorInfo);
				return this.mostRecentAllElevatorInfo.get(0).getId();
			}
		} else {
			//Downwards. First check if there are any downwards or packed elevators above us
			if (this.findClosestElevatorAboveWithState(startFloor, Direction.DOWN)!=-1) {
				return this.findClosestElevatorAboveWithState(startFloor, Direction.DOWN);
			} else if (this.findClosestElevatorAboveWithState(startFloor, Direction.AWAITING_NEXT_REQUEST)!=-1) {
				return this.findClosestElevatorAboveWithState(startFloor, Direction.AWAITING_NEXT_REQUEST);
			} else {
				//No downwards or parked elevators above start floor. Randomly select one
				Collections.shuffle(this.mostRecentAllElevatorInfo);
				return this.mostRecentAllElevatorInfo.get(0).getId();
			}
		}
	}
	
	/**
	 * Finds the closest elevator below the start floor with the given state
	 * @param floor The start floor
	 * @param eState the state to look for
	 * @return The ID of the closest elevator below the start floor with the given state. -1 if there is none
	 */
	private synchronized int findClosestElevatorBelowWithState(int floor, Direction direction) {
		if (direction==null) return -1;
		
		LinkedList<Integer[]> belowElevators = new LinkedList<Integer[]>();
		//Identify elevators below
		for (ElevatorInfo eInfo : this.mostRecentAllElevatorInfo) {
			
			if (eInfo.getFloor()<=floor && eInfo.getMostRecentDirection().equals(direction)) {
				belowElevators.add(new Integer[] {eInfo.getId(), eInfo.getFloor()});
			}
		}
		
		//None found
		if (belowElevators.isEmpty()) return -1;
		
		//Identify the closest one. Largest in this case
		int[] currentMax = new int[] {belowElevators.get(0)[0], belowElevators.get(0)[1]};
		for (Integer[] arr : belowElevators) {
			if (arr[1]>currentMax[1]) {
				currentMax[0] = arr[0];
				currentMax[1] = arr[1];
			}
		}
		
		return currentMax[0];
	}
	
	/**
	 * Finds the closest elevator above the start floor with the given state
	 * @param floor The start floor
	 * @param eState the state to look for
	 * @return The ID of the closest elevator above the start floor with the given state. -1 if there is none
	 */
	private synchronized int findClosestElevatorAboveWithState(int floor, Direction direction) {
		if (direction==null) return -1;
		LinkedList<Integer[]> aboveElevators = new LinkedList<Integer[]>();
		//Identify above elevators 
		for (ElevatorInfo eInfo : this.mostRecentAllElevatorInfo) {
			if (eInfo.getFloor()>=floor && eInfo.getMostRecentDirection().equals(direction)) { 
				aboveElevators.add(new Integer[] {eInfo.getId(), eInfo.getFloor()});
			}
		}
		
		//None found
		if (aboveElevators.isEmpty()) return -1;
		
		//Identify the closest one. Smallest in this case
		int[] currentMin = new int[] {aboveElevators.get(0)[0], aboveElevators.get(0)[1]};
		for (Integer[] arr : aboveElevators) {
			if (arr[1]<currentMin[1]) {
				currentMin[0] = arr[0];
				currentMin[1] = arr[1];
			}
		}
		
		return currentMin[0];
	}

	public HashMap<Integer, Integer> getAllElevatorsNextFloorToVisit(LinkedList<ElevatorInfo> allElevatorInfos){
		this.mostRecentAllElevatorInfo= allElevatorInfos;
		HashMap<Integer, Integer> elevatorID_nextFloorMapping = new HashMap<Integer, Integer>();
		for (ElevatorInfo eInfo : allElevatorInfos) {
			elevatorID_nextFloorMapping.put(eInfo.getId(), 
											this.allElevatorSpecificSchedulers.get(eInfo.getId())
												.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
		}
		return elevatorID_nextFloorMapping;
	} 
	
	@Override
	public String toString() {
		String returnString = "";
		returnString+="\n\n********************************\n";
		returnString+="[ElevatorSpecificSchedulerManager]\nState : "+this.currentState+"\n";
		for (Integer i : this.allElevatorSpecificSchedulers.keySet()) {
			returnString+=allElevatorSpecificSchedulers.get(i).toString();
		}
		returnString+="********************************\n\n";
		return returnString;
	}
	
}
