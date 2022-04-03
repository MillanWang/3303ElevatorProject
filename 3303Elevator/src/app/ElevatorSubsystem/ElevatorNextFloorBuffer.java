package app.ElevatorSubsystem;

import java.util.HashMap;

public class ElevatorNextFloorBuffer {
	private HashMap<Integer, Integer> eReq;
	private HashMap<Integer, Integer> errors;
	private boolean readErrors, readFloors;

	public ElevatorNextFloorBuffer() {
		this.readErrors = false;
		this.readFloors = false;
	}
	
	/***
	 * From elevator system sets the hashmap of elevator id and destinations
	 *
	 * @param map
	 */
	public synchronized void addReq(HashMap<Integer,Integer> floorsToVisit, HashMap<Integer, Integer> errors){
		while(this.readErrors && this.readFloors) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}
		this.eReq = floorsToVisit;
		this.errors = errors;
		this.readErrors = true;
		this.readFloors = true;
		notifyAll();
	}

	/***
	 * Given an elevator ID get's a destination
	 *
	 * @param id
	 * @return
	 */
	public synchronized int getNextFloor(int id) {
		while(!this.readFloors) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}

		int nextFloor = 0;

		if(this.eReq.containsKey(id)) {
			nextFloor = this.eReq.get(id);
			this.eReq.remove(id);
		}
		
		if(this.eReq.size() == 0) {
			this.readFloors = false;
		}

		notifyAll();
		return nextFloor;
	}
	
	/***
	 * Given an elevator ID get a error
	 *
	 * @param id
	 * @return
	 */
	public synchronized int getError(int id) {
		while(!this.readErrors) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}

		int error = 0;

		if(this.errors.containsKey(id)) {
			error = this.errors.get(id);
			this.errors.remove(id);
		}

		if(this.errors.size() == 0) {
			this.readErrors = false;
		}
		
		notifyAll();
		return error;
	}
	
	
	
}
