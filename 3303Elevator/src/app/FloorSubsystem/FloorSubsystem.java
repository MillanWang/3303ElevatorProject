/*
 * Elevator project
 * the floor subsystem class is responsible for sending and receiving requests from and to the scheduler
 * 
 * @author Peter Tanyous
 */

package app.FloorSubsystem;
import java.util.*;


import app.Scheduler.Scheduler;
public class FloorSubsystem extends Thread{

	
	private Scheduler scheduler; 
	private ArrayList<Input> requests; 
	private ArrayList<Input> schedulerRequests; 
	private Integer elevatorPosition; 
	private boolean upwards; 
	
	/*
	 * Constructor initializes the floor subsystem with the serving scheduler 
	 * @param Scheduler 
	 */
	public FloorSubsystem(Scheduler scheduler) {
		this.scheduler = scheduler; 
		this.requests = new ArrayList<Input>();
		this.schedulerRequests = new ArrayList<Input>();
		
	}
	/*
	 * add_input_requests method adds all the inputs from the input.txt file
	 * @path; file path to input.txt 
	 */
	public void addInputRequests(String path) {
		this.requests.addAll(TextFileReader.getrequests(path)); //"C:/Users/peter/Desktop/Winter_2022/SYSC_3303_Assignments/3303ElevatorProject/3303ElevatorProject/3303Elevator/src/app/FloorSubsystem/inputfile.txt"
	}
	/*
	 * add_schedule_requests methods receives requests from the scheduler and adds it to the schedulerRequests collection
	 * @param request; Input type parameter that holds the request's details
	 */
	public void addScheduleRequests(Input request) {
		this.schedulerRequests.add(request);
		//this.scheduler.scheduleRequest(request);
	}
	/*
	 * @return the requests added from the input.txt
	 */
	public ArrayList<Input> getRequests(){
		return this.requests;
	}
	/*
	 * @return the requests received from the scheduler
	 */
	public ArrayList<Input> getSchedulerRequests(){
		return this.schedulerRequests; 
	}
	
	public void updateElevatorPosition(Integer floorno, boolean isUpwards) {
		
		this.elevatorPosition = floorno;
		this.upwards = isUpwards; 
	}
	
	public synchronized void run() {
		addInputRequests("C:/Users/peter/Desktop/Winter_2022/SYSC_3303_Assignments/3303ElevatorProject/3303ElevatorProject/3303Elevator/src/app/FloorSubsystem/inputfile.txt");
		for (Input request: this.requests) {
			this.scheduler.floorSystemScheduleRequest(request);
		}
			
	}

}
