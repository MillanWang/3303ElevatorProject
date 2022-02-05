/*
 * Elevator project
 * the floor subsystem class is responsible for sending and receiving requests from and to the scheduler
 * 
 * @author Peter Tanyous
 */

package app.FloorSubsystem;

import app.Scheduler.*;
import java.util.*;

public class FloorSubsystem extends Threads{
	
	private Scheduler scheduler; 
	private ArrayList<Input> requests; 
	private ArrayList<Input> schedulerRequests; 
	
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
	public void add_input_requests(String path) {
		this.requests.addAll(TextFileReader.getrequests(path)); //"C:/Users/peter/Desktop/Winter_2022/SYSC_3303_Assignments/3303ElevatorProject/3303ElevatorProject/3303Elevator/src/app/FloorSubsystem/inputfile.txt"
	}
	/*
	 * add_schedule_requests methods receives requests from the scheduler and adds it to the schedulerRequests collection
	 * @param request; Input type parameter that holds the request's details
	 */
	public void add_schedule_requests(Input request) {
		this.schedulerRequests.add(request);
	}
	/*
	 * @return the requests added from the input.txt
	 */
	public ArrayList<Input> get_requests(){
		return this.requests;
	}
	/*
	 * @return the requests received from the scheduler
	 */
	public ArrayList<Input> get_scheduler_requests(){
		return this.schedulerRequests; 
	}
	
	public synchronized void run() {
		add_input_requests("C:/Users/peter/Desktop/Winter_2022/SYSC_3303_Assignments/3303ElevatorProject/3303ElevatorProject/3303Elevator/src/app/FloorSubsystem/inputfile.txt");
		
	}

}
