package app.ElevatorSubsystem;

import java.util.HashMap;

public class ElevatorNextFloorBuffer {
	private HashMap<Integer, Integer> eReq;
	private boolean read;

	public ElevatorNextFloorBuffer() {
		this.read = false;
	}
	
	/***
	 * From elevator system sets the hashmap of elevator id and destinations
	 *
	 * @param map
	 */
	public synchronized void addReq(HashMap<Integer,Integer> map){
		while(this.read) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}
		this.eReq = map;
		this.read = true;
		notifyAll();
	}

	/***
	 * Given an elevator ID get's a destination
	 *
	 * @param id
	 * @return
	 */
	public synchronized int getNextFloor(int id) {
		while(!this.read) {
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
			this.read = false;
		}

		notifyAll();
		return nextFloor;
	}
	
}
