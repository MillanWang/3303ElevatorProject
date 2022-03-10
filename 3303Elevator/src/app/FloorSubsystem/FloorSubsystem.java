/**
 * Elevator project
 * the floor subsystem class is responsible for sending and receiving requests from and to the scheduler
 * 
 * @author Peter Tanyous
 */

package app.FloorSubsystem;
import java.io.File;
import java.util.*;

import javax.swing.JFileChooser;

import app.Logger;
import app.ElevatorSubsystem.ElevatorSubsystem;
import app.ElevatorSubsystem.Direction.Direction;
import app.Scheduler.Scheduler;
public class FloorSubsystem extends Thread{

	
	//private Scheduler scheduler; 
	private ArrayList<ScheduledElevatorRequest> requests; 
	private ArrayList<ScheduledElevatorRequest> schedulerRequests; 
	private Integer elevatorPosition; 
	private Direction elevatorStatus; 
	private String inputFileLocation;
	private Logger currentLogger; 
	
	private static int floorCount;
	public static final String UI_COMMAND_EXPLAIN_STRING = "Elevator Simulation Program : Type a command and press enter to continue\nCommands:  \n\t\"n\" - schedule next request\n\t\"q\" - exit program";
	public static final String UI_ASK_TO_CHOOSE_FILE_STRING = "Welcome to the Elevator simulation program. \nWould you like to choose an input file or use the default? \n\t\"y\" - choose file\n\t\"n\"  - default file";
	public static final String DEFAULT_INPUT_FILE_ABSOLUTE_PATH = System.getProperty("user.dir")+"\\src\\app\\FloorSubsystem\\inputfile.txt";
	
	/**
	 * Constructor initializes the floor subsystem with the serving scheduler 
	 * @param Scheduler 
	 * @param inputFile: the file path to be accessed 
	 */
	public FloorSubsystem( Logger log, int floorCount) { //Scheduler scheduler,  removed from constructor
		Scanner sc = new Scanner(System.in);
		
		//this.scheduler = scheduler; 
		this.requests = new ArrayList<ScheduledElevatorRequest>();
		this.schedulerRequests = new ArrayList<ScheduledElevatorRequest>();
		this.inputFileLocation = this.askToChooseFileOrUseDefault(sc);//System.getProperty("user.dir")+"\\src\\app\\FloorSubsystem\\inputfile.txt";
		this.currentLogger = log;
		this.floorCount = floorCount;
		
	    runCommandLineUI(sc); //, scheduler from runCommandLineUI
		
		
	}
	
	
	/**
	 * Constructor initializes the floor subsystem with the serving scheduler 
	 * @param Scheduler 
	 * @param inputFile: the file path to be accessed 
	 */
	//public FloorSubsystem(Scheduler scheduler, String inputFile, Logger log) {
		//this.scheduler = scheduler; 
		//this.requests = new ArrayList<ScheduledElevatorRequest>();
		//this.schedulerRequests = new ArrayList<ScheduledElevatorRequest>();
		//this.inputFileLocation = inputFile;
		//this.currentLogger = log;
	//}
	
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
		addInputRequests(this.inputFileLocation); 
		for (ScheduledElevatorRequest request: this.requests) {
			//this.scheduler.floorSystemScheduleRequest(request);
			System.out.println("Should be doing this.scheduler.floorSystemScheduleRequest(request");
		}
			
	}
	
	/**
	 * Asks the user if they would like to specify an input file.
	 * Opens file explorer if yes
	 * returns default file path if no
	 * @return The absolute file path to use for the input file
	 */
	private static String askToChooseFileOrUseDefault(Scanner sc) {
	    System.out.println(UI_ASK_TO_CHOOSE_FILE_STRING);
	    while(sc.hasNextLine()) {
	    	 //Read the current command line
	    	 String next = sc.nextLine();
	    	 
	    	 //CHOOSE FILE OPTION
	    	 if (next.equals("y") || next.equals("Y")) {
	    		 return chooseFileWithExplorer();

	   	    // ANY OTHER OPTION
	    	 } else {
	    		 return DEFAULT_INPUT_FILE_ABSOLUTE_PATH;
	    	}
	     }
	    return null;//Should never get here
	}

	/**
     * Return the chosen file name path or the default if none selected
     * @return fileName Absolute path of selected file if not default file
     */
    private static String chooseFileWithExplorer(){
        String fileName="";
        //so we let the fileChooser open in the current directory of the user
        String userDirLocation = System.getProperty("user.dir");
        File userDir = new File(userDirLocation);
        // default to user directory
        JFileChooser fileChooser = new JFileChooser(userDir);
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
            //get the file
            fileName = fileChooser.getSelectedFile().getPath();
            
            File f = new File(fileName);
            if (!f.exists() || f.isDirectory()) {
            	//If file is invalid, use default
            	fileName = DEFAULT_INPUT_FILE_ABSOLUTE_PATH;
            }
            System.out.println(fileName);
        } else {
        	return DEFAULT_INPUT_FILE_ABSOLUTE_PATH;
        }
        return fileName;
    }
    

	
	/**
	 * Command line user interface for adding new requests to the elevator system dynamically
	 * @param scheduler The elevator system scheduler
	 */
	private static void runCommandLineUI(Scanner sc) { //, Scheduler scheduler has been removed from
	     System.out.println(UI_COMMAND_EXPLAIN_STRING);
	     while(sc.hasNextLine()) {
	    	 //Read the current command line
	    	 String next = sc.nextLine();
	    	 
	    	 //QUIT OPTION
	    	 if (next.equals("q")) {
	    		 System.out.println("Successfully terminated elevator program");
	    		 sc.close();
	    		 System.exit(0);
	    		 
	    	//ADD REQUEST OPTION
	    		 
	    		 //TODO : THIS GOTTA BE TAILORED TO FLOOR SUBSYSTEM MAKING INPUT REQUESTS
	    	 } else if (next.equals("n")) {
	    		 System.out.println("Enter your command with the following format <CurrentFloor> <DestinationFloor>\n"+"Example: 2 6 for a request to go from floor 2 to 6");
	    		 next = sc.nextLine();
	    		 String commands[] = next.split(" ");
   	    	 
	   	    	 if (commands.length == 2) {
	   	    		 try {
		    	    		 int startFloor = Integer.parseInt(commands[0]);
		    	    		 int endFloor = Integer.parseInt(commands[1]);
		    	    		 if (startFloor<1 || endFloor<1 || startFloor>floorCount || endFloor>floorCount ) {
		    	    			 System.err.println("Invalid floors received");
		    	    		 } else if (startFloor == endFloor) {
		    	    			 System.err.println("Current floor and destination floors cannot be the same");
		    	    		 }else {
		    	    			 System.out.println("New request sent to scheduler");
		    	    			 //scheduler.addElevatorRequest(startFloor, endFloor);
		    	    			 
		    	    		 }
	   	    		 } catch  (NumberFormatException e) {
	   	    			 System.err.println("Cannot have non numerical arguments when creating new requests");
	   	    		 }
	   	    	 } else {
	   	    		 System.err.println("Must have exactly 2 arguments to create a request");
	   	    	 }
	    		 
	   	    //UNKNOWN OPTION
	    	 } else {
	    		 System.err.println("Unknown command\n");
	    	 }
	    	 System.out.println(UI_COMMAND_EXPLAIN_STRING);
	     }
	}
	
	public static void main(String[] args) {
		Logger logger = new Logger(true,true ,true,true); 
		FloorSubsystem floorSubsys = new FloorSubsystem(logger, 7 ); //FLOOR_COUNT = 7
		
		
		Thread floorThread = new Thread(floorSubsys, "FloorSubsystemThread");
		
		floorThread.start();
		
		//runCommandLineUI(sc, scheduler);
	}
	
}
