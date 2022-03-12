package app.ElevatorSubsystem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;

import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.Scheduler.ElevatorSpecificFloorsToVisit;

public class ElevatorBuffer {
	
	private int numOfElevators;
	private ArrayList<ElevatorSpecificFloorsToVisit> eReq;
	private ArrayList<ElevatorInfo> eStatus; 
	private boolean writeReq, writeStatus;
	
	public ElevatorBuffer(int numOfElevators) {
		this.numOfElevators = numOfElevators;
		this.eReq = new ArrayList<>();
		this.eStatus = new ArrayList<>();
	
		this.writeReq = true;
		this.writeStatus = true;
	}
	
	public synchronized void addReq(ElevatorSpecificFloorsToVisit req) {
		
		while(this.writeReq) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.eReq.add(req);
		
		if(this.eReq.size() == this.numOfElevators) {
			this.writeReq = false;
		}
		
		notifyAll();
	}
	
	public synchronized TreeSet<Integer> getReq(int id) {
		
		while(!this.writeReq) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}
		
		ElevatorSpecificFloorsToVisit tmp = null;
		
		for(int i = 0; i < this.eReq.size(); i++) {
			if(this.eReq.get(i).getElevatorID() == id) {
				tmp = this.eReq.remove(i);
			}
		}
		
		if(this.eReq.size() == 0) {
			this.writeReq = true;
		}
		
		notifyAll();
		return tmp.getFloorsToVisit();
	}
	
	public synchronized void addStatus(ElevatorInfo req) {
		while(this.writeStatus) {
			try {
				wait();
			}catch(InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.eStatus.add(req);
		
		if(this.eStatus.size() == this.numOfElevators) {
			this.writeStatus = false;
		}
		
		notifyAll();
	}

	public synchronized LinkedList<ElevatorInfo> getAllStatus() {
		
		while(!this.writeStatus) {
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
			this.writeStatus = true;
		}
		
		notifyAll();
		return tmp;
	}
	
}
