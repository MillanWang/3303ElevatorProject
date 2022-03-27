package app.ElevatorSubsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import app.Logger;
import app.Config.Config;
import app.ElevatorSubsystem.Elevator.Elevator;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.Scheduler.*;
import app.UDP.Util;

/**
 * SYSC 3303, Final Project
 * ElevatorSubsystem.java
 * Purpose: system interactions between scheduler and elevator
 *
 * @author Ben Kittilsen
 * */
public class ElevatorSubsystem implements Runnable{

	private int maxFloor, numElevators;
	private ArrayList<Elevator> elevators;
	private InetSocketAddress schedulerAddr;
	private Logger logger;
	private TimeManagementSystem tms;
	private ElevatorNextFloorBuffer nextFloorBuf; 
	private ElevatorStatusBuffer statusBuf;

	/**
	 * Constructor used to create elevator subsystem
	 *
	 * @param scheduler 	 the scheduler used to communication
	 * */
	public ElevatorSubsystem(Config config){

		try {
			this.schedulerAddr = new InetSocketAddress(config.getString("scheduler.address"), config.getInt("scheduler.elevatorReceivePort"));
		}catch(Exception e) {
			System.exit(1);
		}

		this.maxFloor = config.getInt("floor.highestFloorNumber");
		this.numElevators = config.getInt("elevator.total.number");
		this.nextFloorBuf = new ElevatorNextFloorBuffer();
		this.statusBuf = new ElevatorStatusBuffer(numElevators);
		this.elevators = new ArrayList<Elevator>();
		this.logger = new Logger(config);
		this.tms =  new TimeManagementSystem(config.getInt("time.multiplier"), this.logger);;
	}

	private DatagramPacket buildSchedulerPacket(){
		LinkedList<ElevatorInfo> list = this.statusBuf.getAllStatus();

		byte[] data = {};

		try {
			data = Util.serialize(list);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return new DatagramPacket(
				data,
				data.length,
				schedulerAddr.getAddress(),
				schedulerAddr.getPort()
		);
	}

	private void log(String msg) {
		this.logger.logElevatorEvents("[Elevator Subsystem] "+msg);
	}

	private void createElevators() {
		this.log("creating elevators");
		for(int i = 0; i < this.numElevators; i++) {
			Elevator e = new Elevator(i+1, this.maxFloor, this.logger, this.tms, this.nextFloorBuf, this.statusBuf);
			this.elevators.add(e);
			Thread t = new Thread(e);
			t.start();
		}

	}


	/**
	 * Continuously retrieves directions from the scheduler to operate the elevators
	 */
	@SuppressWarnings("unchecked")
	public void run(){
		this.createElevators();
		boolean disableLog = false;
		while(true) {
			if(!disableLog) this.log("building packet");
			DatagramPacket sendPacket = this.buildSchedulerPacket();
			if(!disableLog) this.log("sending status to scheduler");
			DatagramPacket recievedPacket = Util.sendRequest_ReturnReply(sendPacket);
			if(!disableLog) this.log("receieved packet from scheduler");

			disableLog = false;
			
			HashMap<Integer, Integer> nextFloorRequests = null;

			try {
				Object obj = Util.deserialize(recievedPacket.getData());
				nextFloorRequests = (HashMap<Integer, Integer>) obj;
			}catch(IOException e) {
				e.printStackTrace();
			}catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			if(nextFloorRequests != null) {
				
				int elevatorCountDecrease = 0;
				for(int i = 0; i < this.numElevators; i++) {
					if(!nextFloorRequests.containsKey(i+1)) {
						nextFloorRequests.put(i+1, -1);
					}else if(nextFloorRequests.get(i+1) == -3){
						elevatorCountDecrease++;
					}
				}
				
				int noNewFloorRequestCount = 0;
				for(int i = 0; i < this.numElevators; i++) {
					if(nextFloorRequests.get(i+1) == -1) {
						noNewFloorRequestCount++;
					}
				}
				
				if(noNewFloorRequestCount == this.numElevators) {
					disableLog = true;
				}
				
				this.numElevators -= elevatorCountDecrease;
				this.nextFloorBuf.addReq(nextFloorRequests);
				
				if(this.numElevators == 0) {
					// Exit if there are no elevators left 
					// Only required for the testing purposes 
					return;
				}
			}
		}
	}

	public static void main(String[] args){
		//Config config = new Config("multi.properties");
		Config config = new Config("local.properties");

		ElevatorSubsystem e = new ElevatorSubsystem(config);
		(new Thread(e)).start();
	}

}
