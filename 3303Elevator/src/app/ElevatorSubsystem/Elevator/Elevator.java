package app.ElevatorSubsystem.Elevator;

import app.Scheduler.TimeManagementSystem;
import app.UDP.Util;
import app.Logger;
import app.ElevatorSubsystem.ElevatorNextFloorBuffer;
import app.ElevatorSubsystem.ElevatorStatusBuffer;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.StateMachine.*;
import app.GUI.GUIUpdateInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
/**
 * SYSC 3303, Final Project
 * Elevator.java
 * Purpose: contains the state of the elevator
 *
 * @author Ben Kittilsen
 * */
public class Elevator implements Runnable {

	private final int id;

	private ElevatorDoor door;
	private int currentFloor, floorsMoved, reqFloor, error;
	private boolean exit, tempError, guiEnabled;
	private ElevatorState state;
	private TimeManagementSystem tms;
	private Logger logger;
	private ElevatorNextFloorBuffer nextFloorBuf;
	private ElevatorStatusBuffer statusBuf;
	private Direction last;
	private InetSocketAddress guiAddr;

	/**
	 * Maximum number of floors
	 */
	private int maxFloorCount;

	
	/***
	 * Elevator Constructor
	 * 
	 * @param id of the elvator 
	 * @param maxFloorCount max floor the elevator can visit
	 * @param logger used to log messages 
	 * @param guiAddr ip addr and port the gui to send messages to the gui
	 * @param guiEnabled if the gui is active
	 * @param tms the time managment system 
	 * @param nextFloorBuf buffer to access next floors
	 * @param statusBuf buffer to pass status
	 */
	public Elevator(int id, int maxFloorCount,Logger logger, InetSocketAddress guiAddr, boolean guiEnabled, TimeManagementSystem tms, ElevatorNextFloorBuffer nextFloorBuf, ElevatorStatusBuffer statusBuf) {
		this.maxFloorCount = maxFloorCount;
		this.currentFloor = 1;
		this.floorsMoved = 0;
		this.exit = false;
		this.state = new ElevatorState();
		this.tms = tms;
		this.door = new ElevatorDoor(this.tms);
		this.logger = logger;
		this.id = id;
		this.nextFloorBuf = nextFloorBuf;
		this.statusBuf = statusBuf;
		this.last= Direction.UP;
		this.tempError = false;
		this.guiEnabled = guiEnabled;
		this.guiAddr = guiAddr;
	}

	public int getId() {
		return this.id;
	}

	/***
	 * For this current elevator creates instance of the elevator Info
	 *
	 * @return a new elevator info object
	 */
	public ElevatorInfo getInfo() {
		int tempError = this.error;
		if (this.error==-2) {
			if (this.reqFloor!=this.currentFloor) {
				tempError=-1;
			}
		}
		return new ElevatorInfo(this.id, this.currentFloor, tempError, this.state.getState(), this.last);
	}

	/**
	 * delay thread while elevator is being loaded
	 * */
	public boolean loadElevator() {
		try {
			this.door.loadElevator();
			return true;
		}catch(InterruptedException e) {
			return false;
		}
	}

	/**
	 * Delay thread while moving up or down
	 *
	 * @param finalDest current final elevator destination floor
	 * */
	public void waitTransit(int finalDest) {

		int dest;
		if(state.getDirection() == Direction.UP) {
			dest = currentFloor + 1;
		}else if(state.getDirection() == Direction.DOWN) {
			dest = currentFloor + 1; //tms does not do time cal for destination below current
		}else {
			return;
		}

		Float waitTime = tms.getElevatorTransitTime(floorsMoved, dest, finalDest);
		try {
			Thread.sleep(waitTime.intValue());
			floorsMoved += 1;
		}catch(InterruptedException e) {
			System.out.println("[ERROR]#Elevator#waitTransite() issue waiting");
		}
	}

	/**
	 * Get floor
	 * */
	public int getFloor() {
		return this.currentFloor;
	}

	/**
	 * Get state machine for elevator
	 * */
	public ElevatorStateMachine getState() {
		return state.getState();
	}

	/**
	 * Update state to next state
	 * */
	public void nextState() {
		state.nextState();

		if(state.getState() == ElevatorStateMachine.MoveUp) {
			currentFloor++;
			if(this.guiEnabled) {
				this.sendGUIUpdate();
			}
		}else if(state.getState() == ElevatorStateMachine.MoveDown) {
			currentFloor--;
			if(this.guiEnabled) {
				this.sendGUIUpdate();
			}
		}
	}

	private void sendGUIUpdate() {
		HashMap<Integer, ElevatorInfo> info = new HashMap<>();
		info.put(this.id, this.getInfo());
		GUIUpdateInfo req = new GUIUpdateInfo(info, null, null, null);
		try {
			byte[] data = Util.serialize(req);
			DatagramPacket packet = new DatagramPacket(data,data.length, this.guiAddr.getAddress(), this.guiAddr.getPort());
			Util.sendRequest_NoReply(packet);
		}catch(IOException e) {
			this.log("failed to send gui update");
		}
	}
	
	/**
	 * Determines if the elevator is in a state that is moving
	 * @return if the elevator is moving
	 */
	public boolean isMoving() {
		return state.getState() == ElevatorStateMachine.MoveUp || state.getState() == ElevatorStateMachine.MoveDown;
	}

	/**
	 * Determines if the elevator is idle or waiting for next stopping processing
	 *
	 * @return if the elevator is currently stationary
	 */
	public boolean isStationary() {
		return state.getState() == ElevatorStateMachine.Idle || state.getState() == ElevatorStateMachine.NextStopProcessing;
	}

	/**
	 * Get the elevator's direction state
	 * @return the elevator's direction state
	 */
	public Direction getDirection() {
		return state.getDirection();
	}

	/***
	 * Set the direction of the state, can not set direction up if on max floor, can not set direction down if on bottom floor.
	 * @param d Direction
	 */
	public void setDirection(Direction d) {

		if(this.currentFloor == 1 && d == Direction.DOWN) {
			d = Direction.AWAITING_NEXT_REQUEST;
		}

		if( this.currentFloor == this.maxFloorCount && d == Direction.UP) {
			d = Direction.AWAITING_NEXT_REQUEST;
		}

		state.setDirection(d);
	}

	/**
	 * Logs message to logger
	 * @param message
	 */
	public void log(String msg){
		logger.logElevatorEvents("[Elevator "+ this.id + "] "+msg);
	}

	/**
	 * Checks the destination floor and updates the elevators state
	 * @param destinationFloor
	 */
	private boolean checkFloor(int destinationFloor) {
		this.log("at floor " + this.currentFloor);
		if(this.getFloor() == destinationFloor){ //or max floor

			if(this.state.getState() == ElevatorStateMachine.Idle) {
				this.setDirection(Direction.STOPPED_AT_FLOOR);
			}


			this.nextState();// stopping or opening door

			if(this.getState() == ElevatorStateMachine.Stopping) {
				//elevator slowing down
				this.nextState();//open door if stopping before
			}
			this.log("has arrived at desitnation ");

			this.log("doors starting to open");
			this.nextState();//open door
			this.loadElevator();
			
			if(this.tempError) {
				this.log("\n*******************************************************************\nTEMPORARY ERROR : Elevator "+this.id+" Doors stuck open\n*******************************************************************\n\n");
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				this.log("doors are unstuck");
			}
			
			this.nextState();//door closing
			this.nextState();
			this.log("doors finished closing");
			return true;
		}
		return false;
	}
	
	public void stop() {
		this.exit = true;
	}

	public void run() {
		this.log("is online");

		while(!exit) {

			this.reqFloor = 0;
			
			int nextFloor = nextFloorBuf.getNextFloor(this.id);
			int error = nextFloorBuf.getError(this.id);
			//this.log("" + nextFloor);
			//this.log("" + error);
			
			while(nextFloor == 0) {
				nextFloor = nextFloorBuf.getNextFloor(this.id);
			}
	
			while(error == 0) {
				error = nextFloorBuf.getError(this.id);
			}
			
			this.reqFloor = nextFloor; 
			this.error = error;
			//  Next Floor speciel cases
			// -1: No next stop
			// -2: temporary out of service
			// -3: Permanents

			if(error == -2){
				this.tempError = true;
			}

			if(error == -3){
				this.log("has permanently been stopped");
//				this.currentFloor = error;
				this.statusBuf.addStatus(this.getInfo());
				this.sendGUIUpdate();
				return;
			}


			while((nextFloor != -1) && !this.checkFloor(nextFloor) && !(nextFloor < 1 || nextFloor > this.maxFloorCount)) {
				//this.log(" :" + this.currentFloor + " :" + nextFloor);
				if(nextFloor > this.currentFloor) {
					this.setDirection(Direction.UP);
					this.last = Direction.UP;
				}else if(nextFloor < this.currentFloor){
					this.setDirection(Direction.DOWN);
					this.last = Direction.DOWN;
				}

				this.waitTransit(nextFloor);
				this.nextState();
			}
			
			this.tempError = false;
			ElevatorInfo ei = this.getInfo();
			
			if(nextFloor > 0) {
				System.out.println(ei);
			}
			
			this.statusBuf.addStatus(ei);
			this.last = Direction.AWAITING_NEXT_REQUEST;
		}
		this.log("is offline");
	}
}
