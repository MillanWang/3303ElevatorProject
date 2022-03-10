package app.ElevatorSubsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
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

	private String name;
	private ArrayList<Elevator> elevators;
	private InetSocketAddress schedulerAddr;
	private int maxFloor;
	private Logger logger;
	private TimeManagementSystem tms;

	/**
	 * Constructor used to create elevator subsystem
	 *
	 * @param scheduler 	 the scheduler used to communication
	 * */
	public ElevatorSubsystem(InetSocketAddress schedulerAddr, int numElevators, int maxFloor, Logger logger, TimeManagementSystem tms){
		this.name = Thread.currentThread().getName();
		this.maxFloor = maxFloor;

		elevators = new ArrayList<Elevator>();
		for(int i = 0; i < numElevators; i++) {
			elevators.add(new Elevator(i+1,maxFloor,logger, tms));
		}

		this.tms = tms;
		this.schedulerAddr = schedulerAddr;
		this.logger = logger;
	}
	
	public DatagramPacket buildSchedulerPacket(){
		LinkedList<ElevatorInfo> list = new LinkedList<ElevatorInfo>();
		
		for(int i = 0; i < elevators.size(); i++) {
			list.add(elevators.get(i+1).getInfo());
		}
		
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

	/**
	 * Continuously retrieves directions from the scheduler to operate the elevators
	 */
	public void run(){
		while(true) {
			DatagramPacket sendPacket = this.buildSchedulerPacket();
			DatagramPacket recievedPacket = Util.sendRequest_ReturnReply(sendPacket);
			
			
			
		}
	}

	public static void main(String[] args){
		Config config = new Config("local.properties");
		int numFloors = Integer.parseInt(config.get("floor.total.number"));
		int numElevators = Integer.parseInt(config.get("elevator.total.number"));
		int multiplier = Integer.parseInt(config.get("time.multiplier"));


		InetSocketAddress schedulerAddr = null;
		try {
			schedulerAddr = new InetSocketAddress(config.get("scheduler.address"), Integer.parseInt(config.get("scheduler.elevatorReceivePort")));
		}catch(Exception e) {
			System.exit(1);
		}

		Logger logger = new Logger(true, false, false, false);
		TimeManagementSystem tms = new TimeManagementSystem(multiplier, logger);

		ElevatorSubsystem e = new ElevatorSubsystem(schedulerAddr,numElevators,numFloors,logger, tms);
		//Thread elevatorSubThread = new Thread(e, "ElevatorSubsystemThread");
		//elevatorSubThread.start();
	}

}
