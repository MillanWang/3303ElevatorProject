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
	private InetSocketAddress schedulerAddr;
	private Logger logger;
	private TimeManagementSystem tms;
	private ElevatorNextFloorBuffer nextFloorBuf;
	private ElevatorStatusBuffer statusBuf;
	private Config config;
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

	public void createElevators() {
		this.log("creating elevators");
		for(int i = 0; i < this.numElevators; i++) {
			Elevator e = new Elevator(i+1, this.maxFloor, this.logger, this.tms, this.nextFloorBuf, this.statusBuf);
			Thread t = new Thread(e);
			t.start();
		}

	}

	public void updateElevators(HashMap<Integer, Integer> nextFloorRequests){
		int elevatorCountDecrease = 0;
		// Filling in the elevator requests if not present
		for(int i = 0; i < this.numElevators; i++) {
			if(!nextFloorRequests.containsKey(i+1)) {
				nextFloorRequests.put(i+1, -1);
			}else if(nextFloorRequests.get(i+1) == -3){
				// if an elevator is permentially disable used to
				// decrease elevators
				elevatorCountDecrease++;
			}
		}
		this.numElevators -= elevatorCountDecrease;
		this.nextFloorBuf.addReq(nextFloorRequests);

		if(this.numElevators == 0){
			System.exit(0);
		}
	}

	public void sendUpdateToScheduler(){
		this.log("sending all elevator update to scheduler");
		DatagramPacket sendPacket = this.buildSchedulerPacket();
		DatagramPacket receieved = Util.sendRequest_ReturnReply(sendPacket);

		String status = receieved.getData().toString();

		if (status.equals("200 OK")) {
			this.log("scheduler succesfully receieved elevators status");
		}else{
			this.log("scheduler failed to recieve elevators status");
		}
	}

	@Override
	public void run(){
		(new Thread(
			new ElevatorSubsystem_SchedulerPacketReceiver(
				"ElevatorSubsystem_SchedulerPacketReceiver",
				this.config.getInt("elevator.port"),
				this
			),
			"ElevatorSubsystem_SchedulerPacketReceiver")
		).start();
	}

	public static void main(String[] args){
		//Config config = new Config("multi.properties");
		Config config = new Config("local.properties");
		ElevatorSubsystem e = new ElevatorSubsystem(config);
		(new Thread(e)).start();
	}

}
