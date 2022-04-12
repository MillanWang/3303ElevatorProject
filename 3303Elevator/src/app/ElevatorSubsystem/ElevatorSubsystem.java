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
//import app.GUI.GUI;
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
	private boolean guiEnabled;
	private ArrayList<Integer> permErrors;
	private InetSocketAddress schedulerAddr;
	private InetSocketAddress guiAddr;
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
			this.guiAddr = new InetSocketAddress(config.getString("gui.address"), config.getInt("gui.port"));
		}catch(Exception e) {
			System.exit(1);
		}
		this.config = config;
		this.maxFloor = config.getInt("floor.highestFloorNumber");
		this.numElevators = config.getInt("elevator.total.number");
		this.guiEnabled = config.getBoolean("elevator.gui");
		this.nextFloorBuf = new ElevatorNextFloorBuffer();
		this.statusBuf = new ElevatorStatusBuffer(numElevators);
		this.logger = new Logger(config);
		this.tms =  new TimeManagementSystem(config.getInt("time.multiplier"), this.logger);
		this.permErrors = new ArrayList<>();
	}

	/***
	 * building packets that are to be sent to the elevator
	 * @return datagram packet to be sent to scheduler
	 */
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

 	/***
 	 * Logs a message using the logger class
 	 * 
 	 * @param msg
 	 */
	public void log(String msg) {
		this.logger.logElevatorEvents("[Elevator Subsystem] "+msg);
	}

	/***
	 * Creates the elevators on there own thread
	 */
	private void createElevators() {
		this.log("creating elevators");
		for(int i = 1; i <= this.numElevators; i++) {
			Elevator e = new Elevator(i, this.maxFloor, this.logger, this.guiAddr, this.guiEnabled, this.tms, this.nextFloorBuf, this.statusBuf);
			Thread t = new Thread(e, "Elevator " + i);
			t.start();
		}
	}

	/***
	 * Given the scheduler info object updates the elevator nextFloor buffer
	 * such that each elevator can determine there next job
	 * @param info
	 */
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

	
	/***
	 * Sends an update to the scheduler
	 * the Data gram Packet is build using the class
	 * above 
	 */
	public void sendUpdateToScheduler(){
		DatagramPacket sendPacket = this.buildSchedulerPacket();
		this.log("sending all elevator update to scheduler");
		DatagramPacket receieved = Util.sendRequest_ReturnReply(sendPacket);
		this.log("scheduler succesfully receieved elevators status");
	}

	/***
	 * The main run method for elevator subsystem, creates the packet receiver
	 * used to updates the elevators as well as creates the elevators
	 */
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

	/***
	 * This method is only in testing it is used to shutdown the 
	 * elevator subsystem, so it can be restarted.
	 */
	public void exit() {
		this.esspr.exit();
	}
	
	/***
	 * Main method to start the elevator subsystem
	 * changing the Config file will change the setup for the elevator system
	 * see documentation for clear instructions
	 * @param args
	 */
	public static void main(String[] args){
		//Config config = new Config("multi.properties");
		Config config = new Config("local.properties");
		ElevatorSubsystem e = new ElevatorSubsystem(config);
		(new Thread(e)).start();
	}

}
