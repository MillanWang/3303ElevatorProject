/**
 * Elevator project
 * the floor subsystem class is responsible for sending and receiving requests from and to the scheduler
 * 
 * @author Peter Tanyous
 */

package app.FloorSubsystem;
import java.util.*;

import app.ElevatorSubsystem.Elevator.Movement;
import app.Scheduler.Scheduler;
public class FloorSubsystem extends Thread{

	
	private Scheduler scheduler; 
	private ArrayList<ScheduledElevatorRequest> requests; 
	private ArrayList<ScheduledElevatorRequest> schedulerRequests; 
	private Integer elevatorPosition; 
	private Movement elevatorStatus; 
	private String inputFileLocation;
	
	/**
	 * Constructor initializes the floor subsystem with the serving scheduler 
	 * @param Scheduler 
	 * @param inputFile: the file path to be accessed 
	 */
	public FloorSubsystem(Scheduler scheduler) {
		this.scheduler = scheduler; 
		this.requests = new ArrayList<ScheduledElevatorRequest>();
		this.schedulerRequests = new ArrayList<ScheduledElevatorRequest>();
		this.inputFileLocation = "src/app/FloorSubsystem/inputfile.txt";
	}
	
	/**
	 * Constructor initializes the floor subsystem with the serving scheduler 
	 * @param Scheduler 
	 * @param inputFile: the file path to be accessed 
	 */
	public FloorSubsystem(Scheduler scheduler, String inputFile) {
		this.scheduler = scheduler; 
		this.requests = new ArrayList<ScheduledElevatorRequest>();
		this.schedulerRequests = new ArrayList<ScheduledElevatorRequest>();
		this.inputFileLocation = inputFile;
	}
	
	/**
	 * add_input_requests method adds all the inputs from the input.txt file
	 * @path; file path to input.txt 
	 */
	public void addInputRequests(String path) {
		this.requests.addAll(TextFileReader.getrequests(path)); 
	}
	
	/**
	 * add_schedule_requests methods receives requests from the scheduler and adds it to the schedulerRequests collection
	 * @param request; Input type parameter that holds the request's details
	 */
	public void addScheduleRequests(ScheduledElevatorRequest request) {
		this.schedulerRequests.add(request);
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
	public void updateElevatorPosition(Integer floorno, Movement elevatorStatus) {
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
	 * @return elevator status (Movement)
	 */
	public Movement getElevatorStatus() {
		return this.elevatorStatus;
	}
	/**
	 * Runs the floorSubsystem thread
	 */
	public synchronized void run() {
		addInputRequests(this.inputFileLocation); 
		for (ScheduledElevatorRequest request: this.requests) {
			this.scheduler.floorSystemScheduleRequest(request);
		}
			
	}
}
