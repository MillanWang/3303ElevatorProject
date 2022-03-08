package app.ElevatorSubsystem;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.SortedSet;

import app.Logger;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.Elevator;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;
import app.Scheduler.*;

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
	private SocketAddress schedulerAddr;
	private int maxFloor;
	private Logger logger;
	private TimeManagementSystem tms;
	
	/**
	 * Constructor used to create elevator subsystem
	 *
	 * @param scheduler 	 the scheduler used to communication
	 * */
	public ElevatorSubsystem(SocketAddress schedulerAddr, int numElevators, int maxFloor, Logger logger, TimeManagementSystem tms){
		this.name = Thread.currentThread().getName();
		this.maxFloor = maxFloor;
		
		elevators = new ArrayList<Elevator>();
		for(int i = 0; i < numElevators; i++) {
			elevators.add(new Elevator(maxFloor,logger, tms)); 
		}
		
		this.tms = tms;
		this.schedulerAddr = schedulerAddr;
		this.logger = logger;
		
	}

	
	/**
	 * Continuously retrieves directions from the scheduler to operate the elevators
	 */
	public void run(){
		/*
		this.log("starting at floor 1");
		System.out.println("\n\n");
		while (true) {
			boolean movingUp = elevator.getState() == ElevatorStateMachine.MoveUp;
			// I think we should pass the elevator state to the scheduler
			// the three states that we need to consider are moving up (MoveUp), moving down (MoveDown),
			// and finally the parked state which is now (nextProcessing, or Idle state) almost identical.
			
			//log("" + elevator.getState());
			
			SortedSet<Integer> floorsToVisit = scheduler.getNextFloorsToVisit(elevator.getFloor(), true);//tmp for now
			//log("" + floorsToVisit);
			if(floorsToVisit.size() == 0) {
				if(!elevator.isStationary()) {
					//error here
				}
				continue;
			}

			
			// checking if the elevator is idle or next processing
			if(elevator.isStationary()) {
				int destFloor = floorsToVisit.first();
				
				if(destFloor > elevator.getFloor()) {
					elevator.setDirection(Direction.UP);
				}else if(destFloor < elevator.getFloor()) {
					elevator.setDirection(Direction.DOWN);
				}else if(destFloor == elevator.getFloor()) {
					elevator.setDirection(Direction.AWAITING_NEXT_REQUEST);
				}else {
					//There is an issue
				}
				
				elevator.waitTransit();
				elevator.nextState();
				checkFloor(destFloor);
			// check if the elevator is moving up or down
			}else if(elevator.isMoving()) {
				// used to determine the movement we need to continue
				int destFloor = elevator.getState() == ElevatorStateMachine.MoveUp ? floorsToVisit.first() : floorsToVisit.last();
				
				if(destFloor > elevator.getFloor()) {
					elevator.setDirection(Direction.UP);
				}else if(destFloor < elevator.getFloor()) {
					elevator.setDirection(Direction.DOWN);
				}
				
				elevator.waitTransit();
				elevator.nextState();
				checkFloor(destFloor);
			}
			System.out.println("\n\n");
		}*/
	}
	
	public static void main(String[] args){
		SocketAddress schedulerAddr = null;
		try {
			schedulerAddr = new InetSocketAddress(InetAddress.getLocalHost(), 3000);
		}catch(Exception e) {
			System.exit(1);
		}
		int numFloors = 7;
		int numElevators = 4;
		Logger logger = new Logger(true, false, false, false);
		TimeManagementSystem tms = new TimeManagementSystem(0, logger);
		
		ElevatorSubsystem e = new ElevatorSubsystem(schedulerAddr,numElevators,numFloors,logger, tms);
		//Thread elevatorSubThread = new Thread(e, "ElevatorSubsystemThread");
		//elevatorSubThread.start();
	}
		
}
