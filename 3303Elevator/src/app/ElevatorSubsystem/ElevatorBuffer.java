package app.ElevatorSubsystem;

import java.util.HashMap;
import java.util.LinkedList;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;

/***
 * Used to transfer information between elevators and elevators subsystem
 * 
 * @author benki
 */
public class ElevatorBuffer {
	
	private int numOfElevators;
	private HashMap<Integer, Integer> eReq;
	private LinkedList<ElevatorInfo> eStatus; 
	private boolean readNextFloor, readStatus;
	
	public ElevatorBuffer(int numOfElevators) {
		this.numOfElevators = numOfElevators;
		this.eStatus = new LinkedList<>();
		this.readNextFloor = false;
		this.readStatus = false;
	}
	
	/***
	 * From elevator system sets the hashmap of elevator id and destinations
	 * 
	 * @param map
	 */
	public synchronized void addReq(HashMap<Integer,Integer> map){
		
		while(this.readNextFloor) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.eReq = map;
		this.readNextFloor = true;
		notifyAll();
	}
	
	/***
	 * Given an elevator ID get's a destination
	 * 
	 * @param id
	 * @return
	 */
	public synchronized int getNextFloor(int id) {
		
		while(!this.readNextFloor) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}
		
		int nextFloor = -1;
		
		if(this.eReq.containsKey(id)) {
			nextFloor = this.eReq.get(id);
			this.eReq.remove(id);
		}
		
		if(this.eReq.size() == 0) {
			this.readNextFloor = false;
		}
		
		notifyAll();
		return nextFloor;
	}
	
	/***
	 * Adds status for each elevator to pass to scheduler
	 * 
	 * @param req
	 */
	public synchronized void addStatus(ElevatorInfo req) {
		while(this.readStatus) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.eStatus.add(req);

		if(this.eStatus.size() == this.numOfElevators) {
			this.readStatus = true;
		}
		
		notifyAll();
	}

	/***
	 * Gets the linked list of elevator status for scheduler
	 * 
	 * @return
	 */
	public synchronized LinkedList<ElevatorInfo> getAllStatus() {

		while(!this.readStatus) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}

		@SuppressWarnings("unchecked")
		LinkedList<ElevatorInfo> tmp = (LinkedList<ElevatorInfo>) this.eStatus.clone();
		this.eStatus.clear();
		
		if(this.eStatus.size() == 0) {
			this.readStatus = false;
		}
		
		notifyAll();
		return tmp;
	}
	
}
