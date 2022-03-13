package app.ElevatorSubsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.SortedSet;

import app.Logger;
import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.Elevator;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;
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
	private ElevatorBuffer buf;
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
		this.buf = new ElevatorBuffer(numElevators);
		this.elevators = new ArrayList<Elevator>();
		this.logger = new Logger(config);
		this.tms =  new TimeManagementSystem(config.getInt("time.multiplier"), this.logger);;
	}
	
	private DatagramPacket buildSchedulerPacket(){
		LinkedList<ElevatorInfo> list = this.buf.getAllStatus();
		
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
			Elevator e = new Elevator(i+1, this.maxFloor, this.logger, this.tms, this.buf);
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
		while(true) {
			DatagramPacket sendPacket = this.buildSchedulerPacket();
			this.log("sending status to scheduler");
			DatagramPacket recievedPacket = Util.sendRequest_ReturnReply(sendPacket);
			this.log("receieved packet from scheduler");
			
			HashMap<Integer, Integer> info = null;
			
			try {
					Object obj = Util.deserialize(recievedPacket.getData());
					info = (HashMap<Integer, Integer>) obj;
			}catch(IOException e) {
				e.printStackTrace();
			}catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			if(info != null) {
				this.buf.addReq(info);
			}
		}
	}

	public static void main(String[] args){
		Config config = new Config("local.properties");
		ElevatorSubsystem e = new ElevatorSubsystem(config);
		(new Thread(e)).start();
	}

}
