package app.Scheduler;

import java.util.Collections;
import java.util.LinkedList;

import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;

public class AllElevatorsSchedulingManager {
	private LinkedList<ElevatorSpecificFloorsToVisit> allElevatorsAllFloorsToVisit;
	private LinkedList<ElevatorInfo> allElevatorInfo;
	
	
	
	public AllElevatorsSchedulingManager () {
		this.allElevatorsAllFloorsToVisit = new LinkedList<ElevatorSpecificFloorsToVisit>();
	}

	
	
	/**
	 * Sets the allElevatorInfo 
	 * @param allElevatorInfo the new allElevatorInfo list
	 */
	public synchronized void setAllElevatorInfo(LinkedList<ElevatorInfo> allElevatorInfo) {
		//On first request, populate the list of ElevatorSpecific floors to visit
		if (this.allElevatorsAllFloorsToVisit.isEmpty()) {
			for (ElevatorInfo e : allElevatorInfo) {
				this.allElevatorsAllFloorsToVisit.add(new ElevatorSpecificFloorsToVisit(e.getId()));
			}
		}
		
		this.allElevatorInfo = allElevatorInfo;
//		notifyAll(); Is this even needed?????
	}
	
	/**
	 * Returns the ID of the best elevator to handle this request
	 * @param startFloor The starting floor of the request
	 * @param isUpwards if the request is upwards
	 * @return The ID of the most suitable elevator for this request
	 */
	private synchronized int getBestElevatorId(int startFloor, boolean isUpwards) {
		if (isUpwards) {
			//Upwards. First check if there are any upwards or parked elevators under us
			if (this.findClosestElevatorBelowWithState(startFloor, Direction.UP)!=-1) {
				return this.findClosestElevatorBelowWithState(startFloor, Direction.UP);
			} else if (this.findClosestElevatorBelowWithState(startFloor, Direction.AWAITING_NEXT_REQUEST)!=-1) {
				return this.findClosestElevatorBelowWithState(startFloor, Direction.AWAITING_NEXT_REQUEST);
			} else {
				//No upwards or parked elevators below start floor. Randomly select one
				Collections.shuffle(this.allElevatorInfo);
				return this.allElevatorInfo.get(0).getId();
			}
		} else {
			//Downwards. First check if there are any downwards or packed elevators above us
			if (this.findClosestElevatorAboveWithState(startFloor, Direction.DOWN)!=-1) {
				return this.findClosestElevatorAboveWithState(startFloor, Direction.DOWN);
			} else if (this.findClosestElevatorAboveWithState(startFloor, Direction.AWAITING_NEXT_REQUEST)!=-1) {
				return this.findClosestElevatorAboveWithState(startFloor, Direction.AWAITING_NEXT_REQUEST);
			} else {
				//No downwards or parked elevators above start floor. Randomly select one
				Collections.shuffle(this.allElevatorInfo);
				return this.allElevatorInfo.get(0).getId();
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
		for (ElevatorInfo eInfo : this.allElevatorInfo) {
			
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
		for (ElevatorInfo eInfo : this.allElevatorInfo) {
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
	 * Returns the ElevatorSpecificFloorsToVisit given the elevatorID
	 * @param elevatorID the ID of the elevator
	 * @return the ElevatorSpecificFloorsToVisit object corresponding to that ID
	 */
	private synchronized ElevatorSpecificFloorsToVisit getElevatorSpecificFloorsToVisit(int elevatorID) {
		for (ElevatorSpecificFloorsToVisit esftv : this.allElevatorsAllFloorsToVisit) {
			if (esftv.getElevatorID()==elevatorID) {
				return esftv;
			}
		}
		//Should never get here. Elevator ID should always be valid
		return null;
	}
}
