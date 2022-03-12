/**
 * Elevator project
 * the floor subsystem class is responsible for sending and receiving requests from and to the scheduler
 * 
 * @author Peter Tanyous
 */

package app.FloorSubsystem;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.*;

import javax.swing.JFileChooser;

import FloorSubsystemThreads.SchedulerPacketReceiver;
import app.Logger;
import app.Config.Config;
import app.ElevatorSubsystem.ElevatorSubsystem;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;
import app.Scheduler.Scheduler;
import app.Scheduler.SchedulerThreads.ElevatorSubsystemPacketReceiver;
import app.UDP.Util;
public class FloorSubsystem extends Thread{

	
	//private Scheduler scheduler; 
	private static ArrayList<ScheduledElevatorRequest> requests; 
	private LinkedList<ElevatorInfo> ElevatorInfo; 
	private Integer elevatorPosition; 
	private Direction elevatorStatus; 
	private String inputFileLocation;
	private Logger currentLogger; 
	private Scanner sc;
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
		sc = new Scanner(System.in);
		
		//this.scheduler = scheduler; 
		this.requests = new ArrayList<ScheduledElevatorRequest>();
		this.ElevatorInfo = new LinkedList<ElevatorInfo>();
		this.inputFileLocation = this.askToChooseFileOrUseDefault(sc);//System.getProperty("user.dir")+"\\src\\app\\FloorSubsystem\\inputfile.txt";
		this.currentLogger = log;
		this.floorCount = floorCount;
		
	    
		
		
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
	public void addElevatorInfo(LinkedList<ElevatorInfo> info) {
		this.ElevatorInfo = info;
		//currentLogger.logFloorEvent(request);
		logElevatorInfo();
		//this.scheduler.scheduleRequest(request);
	}
	//This has to be tested out first 
	public void logElevatorInfo() {
		for(int i=0; i < this.ElevatorInfo.size(); i++) {
			if(ElevatorInfo.get(i).getState().equals(ElevatorStateMachine.Stopping)) {
				System.out.println("Elevator " + ElevatorInfo.get(i).getId() + " ");
			}
		}
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
	public LinkedList<ElevatorInfo> getElevatorInfo(){
		return this.ElevatorInfo; 
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
	
	private void sendRequestToScheduler() {
		Config config = new Config("local.properties");
		byte[] data = null;
		try {
			data = Util.serialize(this.requests);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(config.getString("scheduler.address")), config.getInt("scheduler.floorReceivePort"));
			Util.sendRequest_ReturnReply(sendPacket);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.requests = new ArrayList<ScheduledElevatorRequest>();
		
	}
	
	/**
	 * Runs the floorSubsystem thread
	 */
	public synchronized void run() {
		Config config = new Config("local.properties");
		//Don't schedule anything with blank input file
		if (this.inputFileLocation.equals("")) return;
		
		
		addInputRequests(this.inputFileLocation); 
		//this.sendRequestToScheduler();
		//SchedulerPacketReceiver sReceiver = new SchedulerPacketReceiver( this, config.getInt("floor.schedulerReceeivePort"));
		//(new Thread(sReceiver, "SchedulerPacketReceiver")).start();
		runCommandLineUI(sc); //, scheduler from runCommandLineUI	
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
    
    private static void printUIGuidelines() {
    		 System.err.println("Invalid Input, please use the following guidelines to schedule an elevator request ");
	 		 System.err.println("1. Enter <Time in milliseconds> or <Timestamp 'hh:mm:ss' eg:22:51:00.00> followed by");
	 		 System.err.println("2. Enter <CrrentFloor> as a positive non zero number");
	 		 System.err.println("3. Enter <Direction> as 'Up' or 'Down'");
	 		 System.err.println("4. Enter <DestinationFloor> as a positive non zero number ");
	 		 System.err.println("5. Either use 'Up' or 'Down' for the directions");
	 		 System.err.println("6. Remove all spaces and separate the arguments using a comma ','");
	 		 System.err.println("Example: 1000,5,Down,2 for a request to go from floor 5 to 2 after 1000 milliseconds");
	 		 System.err.println("Example: 22:51:00,5,Down,2 for a request to go from floor 5 to 2 at 22:51");
    }
    
    private static boolean newScheduledElevatorRequestCheck(boolean isUp, int start, int destination) {
    	boolean canSchedule = true;
    	if(start == destination) {
    		System.err.println("Start floor and Destination floor cannot be the same");
    		canSchedule = false;
    	}
    	else if(isUp) {
    		//Elevator has to be moving from start < destination if the direction is up 
    		if(start > destination) {
    			 System.err.println("Please use the appropriate direction to go from floor " + start + " to " + destination);
    			canSchedule = false;
    		}
    	}
    	//Elevator has to be moving from start > destination if the direction is down 
    	else if(isUp == false) {
    		if(destination > start) {
    			System.err.println("Please use the appropriate direction to go from floor " + start + " to " + destination);
    			canSchedule = false;
    		}
    	}
    	return canSchedule; 
    	
    }
	
	/**
	 * Command line user interface for adding new requests to the elevator system dynamically
	 * @param scheduler The elevator system scheduler
	 */
	private void runCommandLineUI(Scanner sc) { //, Scheduler scheduler has been removed from
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
	    		 System.out.println("Enter your command with the following format <Time in milliseconds delay or Timestamp 'hh:mm:ss'> <CurrentFloor> <Direction> <DestinationFloor>\n"+"Example: 1000,5,Down,2 for a request to go from floor 5 to 2 after 1000 milliseconds");
	    		 System.out.println("Example: 22:51:00,5,Down,2 for a request to go from floor 5 to 2 at 22:51");
	    		 //System.out.println("or in the following format <Time in LocalTimeFormat> <CurrentFloor> <Direction> <DestinationFloor>\n" + "Example: 22:51:00.00,5,Down,2 for a request to go from floor 5 to 2 at 22:51");
	    		 next = sc.nextLine();
	    		 String commands[] = next.split(",");
	    		 //time =  LocalTime.parse(lineValues[0]);
	   	    	 if (commands.length == 4 & (commands[0].split(":").length == 1)) {
	   	    		 try {   
	   	    			 	 int startFloor = Integer.parseInt(commands[1]);
	    	    		     int endFloor = Integer.parseInt(commands[3]);
	   	    			 	 boolean isUp;
	   	    			 	 if ( (commands[2]).equals("Up")){
	   	    			 		 isUp = true;
	   	    			 		 if(newScheduledElevatorRequestCheck(isUp,startFloor, endFloor)) {
		   	    			 	 try{
		   	    			 		 ScheduledElevatorRequest req = new ScheduledElevatorRequest(new Long(Integer.parseInt(commands[0])), startFloor, isUp , endFloor);
		   	    			 		 System.out.println("New request sent to scheduler");
		   	    			 		 //this.requests = new ArrayList<ScheduledElevatorRequest>();
  	    			 				 //this.requests.add(req);
  	    			 				 //sendRequestToScheduler();
		   	    			 	 } catch(Exception e){
		   	    			 		 printUIGuidelines();
		   	    			 	 }
	   	    			 		 }
	   	    			 	 }
	   	    			 	 else if ( (commands[2]).equals("Down")) {
	   	    			 		 isUp= false;
	   	    			 		 if(newScheduledElevatorRequestCheck(isUp,startFloor, endFloor)) {
	   	    			 			 try{
	   	    			 				 ScheduledElevatorRequest req = new ScheduledElevatorRequest(new Long(Integer.parseInt(commands[0])), Integer.parseInt(commands[1]), isUp , Integer.parseInt(commands[3]));
	   	    			 				 System.out.println("New request sent to scheduler");
	   	    			 				 //this.requests = new ArrayList<ScheduledElevatorRequest>();
	  	    			 				 //this.requests.add(req);
	  	    			 				 //sendRequestToScheduler();
	   	    			 			 } catch(Exception e){ 
	   	    			 				 printUIGuidelines();
	   	    			 			 }
	   	    			 		 }
	   	    			 	 }
		    	    			 
		    	    			
		    	    		 
	   	    		 } catch  (NumberFormatException e) {
	   	    			printUIGuidelines();
	   	    		 }
	   	    		 
	   	    	 }
	   	    	 else if(commands.length == 4 & (commands[0].split(":").length == 3)) {
	   	    		try {   
  	    			 	 int startFloor = Integer.parseInt(commands[1]);
  	    			 	 int endFloor = Integer.parseInt(commands[3]);
  	    			 	 //LocalTime time = LocalTime.of(Integer.parseInt(commands[0].split(":")[0]),Integer.parseInt(commands[0].split(":")[1]),Integer.parseInt(commands[0].split(":")[2])); 
  	    			 	 boolean isUp;
  	    			 	 
  	    			 	 if ( (commands[2]).equals("Up")){
  	    			 		 isUp = true;
  	    			 		 if(newScheduledElevatorRequestCheck(isUp,startFloor, endFloor)) {
	   	    			 	 try{
	   	    			 		 LocalTime time = LocalTime.of(Integer.parseInt(commands[0].split(":")[0]),Integer.parseInt(commands[0].split(":")[1]),Integer.parseInt(commands[0].split(":")[2])); 
	   	    			 		 ScheduledElevatorRequest req = new ScheduledElevatorRequest(time, startFloor, isUp , endFloor);
	   	    			 		 System.out.println("New request sent to scheduler");
	   	    			 		 //this.requests = new ArrayList<ScheduledElevatorRequest>();
	   	    			 		 //this.requests.add(req);
	    			 			 //sendRequestToScheduler();
	   	    			 	 } catch(Exception e){
	   	    			 		 printUIGuidelines();
	   	    			 	 }
  	    			 		 }
  	    			 	 }
  	    			 	 else if ( (commands[2]).equals("Down")) {
  	    			 		 isUp= false;
  	    			 		 if(newScheduledElevatorRequestCheck(isUp,startFloor, endFloor)) {
  	    			 			 try{
  	    			 				 LocalTime time = LocalTime.of(Integer.parseInt(commands[0].split(":")[0]),Integer.parseInt(commands[0].split(":")[1]),Integer.parseInt(commands[0].split(":")[2])); 
  		   	    			 		 ScheduledElevatorRequest req = new ScheduledElevatorRequest(time, startFloor, isUp , endFloor);
  	    			 				 System.out.println("New request sent to scheduler");
  	    			 				 //this.requests = new ArrayList<ScheduledElevatorRequest>();
  	    			 				 //this.requests.add(req);
  	    			 				 //sendRequestToScheduler();
  	    			 				 
  	    			 			 } catch(Exception e){ 
  	    			 				 printUIGuidelines();
  	    			 			 }
  	    			 		 }
  	    			 	 }
	    	    			 
	    	    			
	    	    		 
  	    		 } catch  (NumberFormatException e) {
  	    			printUIGuidelines();
  	    		 }
  	    		 
	   	    	 }
	   	    	 else {
	   	    		printUIGuidelines();
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
