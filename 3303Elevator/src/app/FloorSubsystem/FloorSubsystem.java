/**
 * Elevator project
 * the floor subsystem class is responsible for sending and receiving requests from and to the scheduler
 * 
 * @author Peter Tanyous
 */

package app.FloorSubsystem;
import java.util.*;

import app.Logger;
import app.ElevatorSubsystem.Direction.Direction;
import app.Scheduler.Scheduler;
public class FloorSubsystem extends Thread{

	
	private Scheduler scheduler; 
	private ArrayList<ScheduledElevatorRequest> requests; 
	private ArrayList<ScheduledElevatorRequest> schedulerRequests; 
	private Integer elevatorPosition; 
	private Direction elevatorStatus; 
	private String inputFileLocation;
	private Logger currentLogger; 
	
	/**
	 * Constructor initializes the floor subsystem with the serving scheduler 
	 * @param Scheduler 
	 * @param inputFile: the file path to be accessed 
	 */
	public FloorSubsystem(Scheduler scheduler, Logger log) {
		this.scheduler = scheduler; 
		this.requests = new ArrayList<ScheduledElevatorRequest>();
		this.schedulerRequests = new ArrayList<ScheduledElevatorRequest>();
		this.inputFileLocation = System.getProperty("user.dir")+"\\src\\app\\FloorSubsystem\\inputfile.txt";
		this.currentLogger = log;
	}
	
	/**
	 * Constructor initializes the floor subsystem with the serving scheduler 
	 * @param Scheduler 
	 * @param inputFile: the file path to be accessed 
	 */
	public FloorSubsystem(Scheduler scheduler, String inputFile, Logger log) {
		this.scheduler = scheduler; 
		this.requests = new ArrayList<ScheduledElevatorRequest>();
		this.schedulerRequests = new ArrayList<ScheduledElevatorRequest>();
		this.inputFileLocation = inputFile;
		this.currentLogger = log;
	}
	
	/**
	 * add_input_requests method adds all the inputs from the input.txt file
	 * @path; file path to input.txt 
	 */
	public void addInputRequests(String path) {
		this.requests.addAll(TextFileReader.getRequests(path)); 
		for(int i = 0; i < requests.size(); i++) {
			currentLogger.logFloorEvent(requests.get(i));
		}
	}
	
	/**
	 * add_schedule_requests methods receives requests from the scheduler and adds it to the schedulerRequests collection
	 * @param request; Input type parameter that holds the request's details
	 */
	public void addScheduleRequests(ScheduledElevatorRequest request) {
		this.schedulerRequests.add(request);
		currentLogger.logFloorEvent(request);
		//this.scheduler.scheduleRequest(request);
	}
	
	/**
	 * @return the requests added from the input.txt
	 */
	public ArrayList<ScheduledElevatorRequest> getRequests(){
		return this.requests;
	}
	
	/**
	 * @return the requests received from the scheduler
	 */
	public ArrayList<ScheduledElevatorRequest> getSchedulerRequests(){
		return this.schedulerRequests; 
	}
	
	/**
	 * Updates elevator position and status
	 * @param floorno
	 * @param isUpwards
	 */
	public void updateElevatorPosition(Integer floorno, Direction elevatorStatus) {
		this.elevatorPosition = floorno;
		this.elevatorStatus = elevatorStatus; 
	}
	/**
	 * gets the elevator position
	 * @return elevator position (floor no)
	 */
	public Integer getElevatorPosition() {
		return this.elevatorPosition;
	}
	/**
	 * gets the elevator position
	 * @return elevator status (Direction)
	 */
	public Direction getElevatorStatus() {
		return this.elevatorStatus;
	}
	/**
	 * Runs the floorSubsystem thread
	 */
	public synchronized void run() {
		//Don't schedule anything with blank input file
		if (this.inputFileLocation.equals("")) return;
		
		
		addInputRequests(this.inputFileLocation); 
		for (ScheduledElevatorRequest request: this.requests) {
			this.scheduler.floorSystemScheduleRequest(request);
		}
			
	}
}
