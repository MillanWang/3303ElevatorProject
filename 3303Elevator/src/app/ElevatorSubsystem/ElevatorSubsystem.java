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
import app.GUI.GUI;
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
	private ArrayList<Integer> permErrors;
	private InetSocketAddress schedulerAddr;
	private Logger logger;
	private TimeManagementSystem tms;
	private ElevatorNextFloorBuffer nextFloorBuf;
	private ElevatorStatusBuffer statusBuf;
	private Config config;
	private ElevatorSubsystem_SchedulerPacketReceiver esspr;

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
		this.config = config;
		this.maxFloor = config.getInt("floor.highestFloorNumber");
		this.numElevators = config.getInt("elevator.total.number");
		this.nextFloorBuf = new ElevatorNextFloorBuffer();
		this.statusBuf = new ElevatorStatusBuffer(numElevators);
		this.logger = new Logger(config);
		this.tms =  new TimeManagementSystem(config.getInt("time.multiplier"), this.logger);
		this.permErrors = new ArrayList<>();
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

	public void log(String msg) {
		this.logger.logElevatorEvents("[Elevator Subsystem] "+msg);
	}

	private void createElevators() {
		this.log("creating elevators");
		for(int i = 1; i <= this.numElevators; i++) {
			Elevator e = new Elevator(i, this.maxFloor, this.logger, this.tms, this.nextFloorBuf, this.statusBuf);
			Thread t = new Thread(e, "Elevator " + i);
			t.start();
		}
	}

	public void updateElevators(SchedulerInfo info){
		//gui.updateElevatorInfo();
		HashMap<Integer, Integer> nextFloorRequests = info.getNextFloorsToVisit();
		HashMap<Integer, Integer> errors = info.getErrors();
		
		this.log("adding elevator requests");
		
		// Filling in the elevator requests if not present
		for(int i = 0; i < this.numElevators; i++) {
			int id = i + 1;
			if(!nextFloorRequests.containsKey(id)) {
				if(!this.permErrors.contains(id)) {
					nextFloorRequests.put(id, -1);	
				}
			}
		}
		
		
		for(int i = 0; i < this.numElevators; i++) {
			int id = i + 1;
			if(!errors.containsKey(id)) {
				if(!this.permErrors.contains(id)) {
					errors.put(id, -1);	
				}
			}else if(errors.get(id) == -3){
				if(this.permErrors.contains(id)) {
					errors.remove(id);	
				}else {
					this.permErrors.add(id);
				}
			}else if(this.permErrors.contains(id)) {
				errors.remove(id);
			}
		}
		
		String msg1 = "next floors ";
		String msg2 = "errors ";
		for(int i = 0; i < this.numElevators; i++) {
			if(nextFloorRequests.containsKey(i+1)) {
				msg1 += (i + 1) + ":" + nextFloorRequests.get(i+1) + " ";
				msg2 += (i + 1) + ":" + errors.get(i+1) + " ";
			}
		}
		this.log(msg1);
		this.log(msg2);
		this.nextFloorBuf.addReq(nextFloorRequests, errors);
	}

	public void sendUpdateToScheduler(){
		DatagramPacket sendPacket = this.buildSchedulerPacket();
		this.log("sending all elevator update to scheduler");
		DatagramPacket receieved = Util.sendRequest_ReturnReply(sendPacket);
		this.log("scheduler succesfully receieved elevators status");
	}

	@Override
	public void run(){
		this.esspr = new ElevatorSubsystem_SchedulerPacketReceiver(
				"ElevatorSubsystem_SchedulerPacketReceiver",
				this.config.getInt("elevator.port"),
				this
			);
		
		(new Thread(
			this.esspr,
			"ElevatorSubsystem_SchedulerPacketReceiver")
		).start();
		this.createElevators();
	}

	public void exit() {
		this.esspr.exit();
	}
	
	public static void main(String[] args){
		//Config config = new Config("multi.properties");
		Config config = new Config("local.properties");
		ElevatorSubsystem e = new ElevatorSubsystem(config);
		(new Thread(e)).start();
	}

}
