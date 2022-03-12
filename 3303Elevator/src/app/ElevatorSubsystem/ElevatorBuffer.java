package app.ElevatorSubsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.Scheduler.ElevatorSpecificFloorsToVisit;

public class ElevatorBuffer {
	
	private int numOfElevators;
	private HashMap<Integer, Integer> eReq;
	private ArrayList<ElevatorInfo> eStatus; 
	private boolean readNextFloor, readStatus;
	
	public ElevatorBuffer(int numOfElevators) {
		this.numOfElevators = numOfElevators;
		this.eStatus = new ArrayList<>();
		this.readNextFloor = false;
		this.readStatus = false;
	}
	
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

	public synchronized LinkedList<ElevatorInfo> getAllStatus() {

		while(!this.readStatus) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}

		LinkedList<ElevatorInfo> tmp = new LinkedList<>();
		
		for(int i = 0; i < this.eStatus.size(); i++) {
			tmp.add(this.eStatus.get(0));
		}
		
		this.eStatus.clear();
		
		if(this.eStatus.size() == 0) {
			this.readStatus = false;
		}
		
		notifyAll();
		return tmp;
	}
	
}
