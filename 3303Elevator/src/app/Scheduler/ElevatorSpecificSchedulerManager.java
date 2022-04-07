package app.Scheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.GUI.GUIUpdateInfo;

/**
 * Class for ElevatorSpecificSchedulerManager object which manages multiple 
 * instances of ElevatorSpecificScheduler objects
 * 
 * @author Millan Wang
 *
 */
public class ElevatorSpecificSchedulerManager {
	/**
	 * Collection of ElevatorSpecificScheduler objects with the Key being the ID
	 */
	private HashMap<Integer, ElevatorSpecificScheduler> allElevatorSpecificSchedulers;
	
	/**
	 * Most recent ElevatorInfo objects received from the elevator subsystem
	 */
	private LinkedList<ElevatorInfo> mostRecentAllElevatorInfo;
	
	/**
	 * The current state of the ElevatorSpecificSchedulerManager object
	 */
	private ElevatorSpecificSchedulerManagerState currentState;
	
	/**
	 * Feature flag to determine which algorithm to use for request distribution (Always using simple one due to time budget)
	 */
	private boolean useSimpleLeastLoadAlgorithm;
	
	/**
	 * Mapping of elevaor IDs to error type
	 */
	private HashMap<Integer, Integer> errorMapping;
	
	/**
	 * Sets of directional start floors to indicate the floor buttons on the GUI
	 */
	private TreeSet<Integer> allUpwardsStartFloors;
	private TreeSet<Integer> allDownwardsStartFloors;
	
	
	
	/**
	 * Constructor for the ElevatorSpecificSchedulerManager class
	 * @param useSimpleLeastLoadAlgorithm Boolean to determine which request distribution algorithm to use
	 */
	public ElevatorSpecificSchedulerManager (boolean useSimpleLeastLoadAlgorithm) {
		this.useSimpleLeastLoadAlgorithm=useSimpleLeastLoadAlgorithm;
		this.currentState = ElevatorSpecificSchedulerManagerState.AWAITING_NEXT_ELEVATOR_REQUEST;
		this.errorMapping = new HashMap<Integer, Integer>();
		
		this.allUpwardsStartFloors = new TreeSet<Integer>();
		this.allDownwardsStartFloors = new TreeSet<Integer>();
		
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
	 * Returns the current state of the ElevatorSpecificSchedulerManager
	 * @return the currentState
	 */
	public ElevatorSpecificSchedulerManagerState getCurrentState() {
		return currentState;
	}

	/**
	 * Schedules a floor request to an algorithmically determined elevatorID
	 * @param startFloor
	 * @param destinationFloor
	 * @param requestType
	 * @return
	 */
	public int scheduleFloorRequest(int startFloor, int destinationFloor, int requestType) {
		if (checkIfAllElevatorsArePermanentError()) {
			return -1;
		}
		
		int elevatorID_toSchedule=-1;
		
		//Choose which algorithm to use to distribute the floor request
		if (this.useSimpleLeastLoadAlgorithm) {
			elevatorID_toSchedule=getBestElevatorId_SimpleLeastLoadAlgorithm();
		} else {
			elevatorID_toSchedule=getBestElevatorId_DirectionalPriorityAlgorithm(startFloor, startFloor<destinationFloor);
		}
		
		//If the request is an error, then add it to the errorMapping
		if (requestType!=0) {
			this.errorMapping.put(elevatorID_toSchedule, requestType==1 ? -2 : -3); //-2:Temp error , -3:PermanentError
		}
		
		//If the request is normal or temporary error, track the start floor
		if (requestType!=2) {
			if (startFloor<destinationFloor) {
				this.allUpwardsStartFloors.add(startFloor);
			} else { 
				this.allDownwardsStartFloors.add(startFloor);
			}
		}
		
		//If perma-error, remove all the floors associated with it
		if (requestType==3) {
			TreeSet<Integer> downs = this.allElevatorSpecificSchedulers.get(elevatorID_toSchedule).getDownwardsFloorsToVisit();
			TreeSet<Integer> ups = this.allElevatorSpecificSchedulers.get(elevatorID_toSchedule).getUpwardsFloorsToVisit();
			this.allUpwardsStartFloors.removeAll(downs);
			this.allUpwardsStartFloors.removeAll(ups);
			
			this.allElevatorSpecificSchedulers.get(elevatorID_toSchedule).clearAllFloorsToVisit();
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
			if (this.allElevatorSpecificSchedulers.get(i).getCurrentState()!=ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE) {
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

		int minStops = 999999999;
		for (Integer id : this.allElevatorSpecificSchedulers.keySet()) {
			if (this.allElevatorSpecificSchedulers.get(id).getCurrentState()!= ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE && this.allElevatorSpecificSchedulers.get(id).getActiveNumberOfStopsCount() <= minStops) {
				minStops = this.allElevatorSpecificSchedulers.get(id).getActiveNumberOfStopsCount();
			}
		}
		LinkedList<Integer> minFloorElevatorIDs = new LinkedList<Integer>();
		for (Integer id : this.allElevatorSpecificSchedulers.keySet()) {
			if (this.allElevatorSpecificSchedulers.get(id).getCurrentState()!= ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE && this.allElevatorSpecificSchedulers.get(id).getActiveNumberOfStopsCount() == minStops) {
				minFloorElevatorIDs.add(id);
			}
		}
		if (minFloorElevatorIDs.isEmpty()) return -1;
		Collections.shuffle(minFloorElevatorIDs);
		return minFloorElevatorIDs.pop();
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

	/**
	 * Gets the next floor to visit from all ElevatorSpecificSchedulers given a collection of ElevatorInfoObjects
	 * @param allElevatorInfos Collection of ElevatorInfo objects representing the elevators in the system
	 * @return Hashmap of elevatorID:NextFloorToVisit
	 */
	public HashMap<Integer, Integer> getAllElevatorsNextFloorToVisit(LinkedList<ElevatorInfo> allElevatorInfos){
		this.mostRecentAllElevatorInfo= allElevatorInfos;
		HashMap<Integer, Integer> elevatorID_nextFloorMapping = new HashMap<Integer, Integer>();
		for (ElevatorInfo eInfo : allElevatorInfos) {
			elevatorID_nextFloorMapping.put(eInfo.getId(), 
											this.allElevatorSpecificSchedulers.get(eInfo.getId())
												.handleElevatorInfoChange_returnNextFloorToVisit(eInfo));
			if (this.allElevatorSpecificSchedulers.get(eInfo.getId()).getCurrentState()==ElevatorSpecificSchedulerState.SERVICING_UPWARDS_FLOORS_TO_VISIT || this.allElevatorSpecificSchedulers.get(eInfo.getId()).getCurrentState()==ElevatorSpecificSchedulerState.MOVING_DOWN_TO_LOWEST_UPWARDS_FLOOR_TO_VISIT) {
				this.allUpwardsStartFloors.remove(eInfo.getFloor());
			} else if (this.allElevatorSpecificSchedulers.get(eInfo.getId()).getCurrentState()==ElevatorSpecificSchedulerState.SERVICING_DOWNWARDS_FLOORS_TO_VISIT || this.allElevatorSpecificSchedulers.get(eInfo.getId()).getCurrentState()==ElevatorSpecificSchedulerState.MOVING_UP_TO_HIGHEST_DOWNWARDS_FLOOR_TO_VISIT  ) {
				this.allDownwardsStartFloors.remove(eInfo.getFloor());
			}
		}
		return elevatorID_nextFloorMapping;
	}
	
	/**
	 * Returns the total number of active stops to visit across all elevators, excluding permanently down ones
	 * 
	 * @return Number of active stops across all elevators.
	 */
	public int getTotalActiveNumberOfStopsCount() {
		//Iterate through all ElevatorSpecificScheduler states. Return false if any are not out of service
		int totalStopCount = 0;
		for (Integer i : this.allElevatorSpecificSchedulers.keySet()) {
			if (this.allElevatorSpecificSchedulers.get(i).getCurrentState()!=ElevatorSpecificSchedulerState.PERMANENT_OUT_OF_SERVICE) {
				totalStopCount += this.allElevatorSpecificSchedulers.get(i).getActiveNumberOfStopsCount();
			}
		}
		return totalStopCount;
		
	}
	
	/**
	 * Returns the elevatorErrorMap field
	 * @return the current elevatorErrorMap
	 */
	public HashMap<Integer, Integer> getElevatorErrorMap(){
		HashMap<Integer, Integer> hm = (HashMap<Integer, Integer>) this.errorMapping.clone();
		this.errorMapping.clear();
		return hm;
	}
	
	/**
	 * Creates a GUIUpdateInfo object to be sent to the GUI subsystem
	 * @return
	 */
	public GUIUpdateInfo createGUIUpdate() {
		HashMap<Integer, TreeSet<Integer>> allElevatorDestinations = new HashMap<Integer, TreeSet<Integer>>();
		for (Integer i : this.allElevatorSpecificSchedulers.keySet()) {
			allElevatorDestinations.put(i, this.allElevatorSpecificSchedulers.get(i).getPressedButtons());
		}
		
		if (this.getTotalActiveNumberOfStopsCount()==0) {
			this.allUpwardsStartFloors.clear();
			this.allDownwardsStartFloors.clear();
		}
		
		GUIUpdateInfo guiInfo = new GUIUpdateInfo(null/*ElevatorInfo is only for elevator to send*/, 
												  allElevatorDestinations,
												  this.allUpwardsStartFloors,
												  this.allDownwardsStartFloors);
		return guiInfo;
	}
	
	/**
	 * Gets a string detailing the current state of the ElevatorSpecificSchedulerManager and all of the contained ElevatorSpecificScheduler
	 */
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
