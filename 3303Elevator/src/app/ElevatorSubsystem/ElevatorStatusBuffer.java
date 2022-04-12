package app.ElevatorSubsystem;

import java.util.LinkedList;

import app.ElevatorSubsystem.Elevator.ElevatorInfo;

/***
 * This class was designed to passed the status of each elevator
 * to the elevator subsystem.
 * 
 * 
 * @author benki
 *
 */
public class ElevatorStatusBuffer {
	
	private int numOfElevators, dec;
	private LinkedList<ElevatorInfo> eStatus;
	private boolean read;

	public ElevatorStatusBuffer(int numOfElevators) {
		this.numOfElevators = numOfElevators;
		this.eStatus = new LinkedList<>();
		this.read = false;
		this.dec = 0;
	}
	

	/***
	 * Adds status for each elevator to pass to scheduler
	 *
	 * @param req
	 */
	public synchronized void addStatus(ElevatorInfo req) {
		while(this.read) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}
		
		// If the elevator has been permenatly disabled
		if(req.getError() == -3){ //USED TO BE GET FLOOR
			this.dec++;
		}

		this.eStatus.add(req);

		if(this.eStatus.size() == this.numOfElevators) {
			this.read = true;
		}

		notifyAll();
	}

	
	/***
	 * Gets the linked list of elevator status for scheduler
	 *
	 * @return
	 */
	public synchronized LinkedList<ElevatorInfo> getAllStatus() {

		while(!this.read) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}

		@SuppressWarnings("unchecked")
		LinkedList<ElevatorInfo> tmp = (LinkedList<ElevatorInfo>) this.eStatus.clone();
		this.eStatus.clear();
		this.read = false;
		this.numOfElevators -= this.dec;
		this.dec = 0;
		notifyAll();
		return tmp;
	}
}