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

import app.FloorSubsystem.FloorSubsystemThreads.FloorSubsystem_SchedulerPacketReceiver;
import app.Logger;
import app.Config.Config;
import app.ElevatorSubsystem.ElevatorSubsystem;
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;
import app.Scheduler.Scheduler;
import app.Scheduler.SchedulerThreads.Scheduler_ElevatorSubsystemPacketReceiver;
import app.UDP.Util;
public class FloorSubsystem extends Thread{

	private ArrayList<ScheduledElevatorRequest> requests; 
	private LinkedList<ElevatorInfo> elevatorInfo; 
	private Integer elevatorPosition; 
	private Direction elevatorStatus; 
	private String inputFileLocation;
	private Logger currentLogger; 
	private Scanner sc;
	private Config conf;
	private int floorCount;
	public static final String UI_COMMAND_EXPLAIN_STRING = "Elevator Simulation Program : Type a command and press enter to continue\nCommands:  \n\t\"n\" - schedule next request\n\t\"q\" - exit program";
	public static final String UI_ASK_TO_CHOOSE_FILE_STRING = "Welcome to the Elevator simulation program. \nWould you like to choose an input file or use the default? \n\t\"y\" - choose file\n\t\"n\"  - default file";
	public static final String DEFAULT_INPUT_FILE_ABSOLUTE_PATH = System.getProperty("user.dir")+"\\src\\app\\FloorSubsystem\\inputfile.txt";
	
	/**
	 * Constructor initializes the floor subsystem with the serving scheduler 
	 * @param Scheduler 
	 * @param inputFile: the file path to be accessed 
	 */
	public FloorSubsystem( Logger log, Config conf) { //Scheduler scheduler,  removed from constructor
		sc = new Scanner(System.in);
		
		//this.scheduler = scheduler; 
		this.requests = new ArrayList<ScheduledElevatorRequest>();
		this.elevatorInfo = new LinkedList<ElevatorInfo>();
		
		this.currentLogger = log;
		this.floorCount = conf.getInt("floor.highestFloorNumber");
		this.conf = conf;
	    
		
		
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
	public void addElevatorInfo(LinkedList<ElevatorInfo> info) {
		this.elevatorInfo = info;
		logElevatorInfo();
	}
	/**
	 * prints out the status of all elevators 
	 */
	public void logElevatorInfo() {
		String statement = "";
		for(int i=0; i < this.elevatorInfo.size(); i++) {
				statement = statement + "Elevator " + elevatorInfo.get(i).getId() + " at floor " + elevatorInfo.get(i).getFloor() + " Status: " + elevatorInfo.get(i).getState().toString()+ "\n";
		}
		System.out.println(statement);
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
		return this.elevatorInfo; 
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
	 * sets the inputFileLocation to be used for testing 
	 * 
	 */
	public void setInputFile(String filePath) {
		this.inputFileLocation = filePath;
	}
	 /**
	  * sends to scheduler an arraylist of scheduledElevatorRequest using datagram packet after serialization 
	  */
	public void sendRequestToScheduler() {
		
		byte[] data = null;
		try {
			data = Util.serialize(this.requests);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(conf.getString("scheduler.address")), conf.getInt("scheduler.floorReceivePort"));
			Util.sendRequest_ReturnReply(sendPacket);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.requests = new ArrayList<ScheduledElevatorRequest>();
		
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
     *prints guidlines to use the UI 
     */
    private static void printUIGuidelines() {
    		 System.err.println("Invalid Input, please use the following guidelines to schedule an elevator request\n" +
	 		  "1. Enter <Time in milliseconds> or <Timestamp 'hh:mm:ss' eg:22:51:00.00> followed by\n"+
	 		  "2. Enter <CrrentFloor> as a positive non zero number\n"+
	 		  "3. Enter <Direction> as 'Up' or 'Down'\n" + 
	 		  "4. Enter <DestinationFloor> as a positive non zero number\n"+
	 		  "5. Enter <requestType>  0 if no requestType, 1 Transient requestType, 2 permanent requestType\n"+
	 		  "6. Either use 'Up' or 'Down' for the directions\n"+
	 		  "7. Remove all spaces and separate the arguments using a comma ','\n"+
	 		  "Example: 1000,5,Down,2,0 for a request to go from floor 5 to 2 after 1000 milliseconds with no requestType\n"+
	 		 "Example: 22:51:00,5,Down,2,1 for a request to go from floor 5 to 2 at 22:51 with a transient requestType");
    }
    /**
     * checks if the UI input is valid to initialize a ScheduledElevatorRequest
     * @param isUp
     * @param start
     * @param destination
     * @return
     */
    private static boolean newScheduledElevatorRequestCheck(boolean isUp, int start, int destination, int requestType) {
    	boolean canSchedule = true;
    	if(requestType > 2 || requestType <0 ) {
    		System.err.print("requestType type can only be 0 for no error or 1 for a transient error or 2 for a permanent error");
    		canSchedule = false;
    	}
    	else if(start == destination) {
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
	    		 
	    	 } else if (next.equals("n")) {
	    		 System.out.println("Enter your command with the following format <Time in milliseconds delay or Timestamp 'hh:mm:ss'> <CurrentFloor> <Direction> <DestinationFloor> <requestTypeType> \n"
	    				 +"Example: 1000,5,Down,2,0 for a request to go from floor 5 to 2 after 1000 milliseconds with no requestTypes \n" + 
	    				 "Example: 22:51:00,5,Down,2,1 for a request to go from floor 5 to 2 at 22:51 with a transient requestType \n" + 
	    				 "Example: 23:45:00,5,Down,2,2 for a request to go from floor 5 to 2 at 23:45 with a permanent requestType \n");
	    		 //System.out.println("or in the following format <Time in LocalTimeFormat> <CurrentFloor> <Direction> <DestinationFloor>\n" + "Example: 22:51:00.00,5,Down,2 for a request to go from floor 5 to 2 at 22:51");
	    		 next = sc.nextLine();
	    		 String commands[] = next.split(",");
	    		 //time =  LocalTime.parse(lineValues[0]);
	   	    	 if (commands.length == 5 & (commands[0].split(":").length == 1)) {
	   	    		 try {   
	   	    			 	 int startFloor = Integer.parseInt(commands[1]);
	    	    		     int endFloor = Integer.parseInt(commands[3]);
	   	    			 	 boolean isUp;
	   	    			 	 int requestType = Integer.parseInt(commands[4]);
	   	    			 	 if ( (commands[2]).equals("Up")){
	   	    			 		 isUp = true;
	   	    			 		 if(newScheduledElevatorRequestCheck(isUp,startFloor, endFloor, requestType)) {
		   	    			 	 try{
		   	    			 		 ScheduledElevatorRequest req = new ScheduledElevatorRequest(new Long(Integer.parseInt(commands[0])), startFloor, isUp , endFloor, requestType);
		   	    			 		 this.requests = new ArrayList<ScheduledElevatorRequest>();
  	    			 				 this.requests.add(req);
  	    			 				 sendRequestToScheduler();
		   	    			 	 } catch(Exception e){ 
		   	    			 		 printUIGuidelines();
		   	    			 	 }
	   	    			 		 }
	   	    			 	 }
	   	    			 	 else if ( (commands[2]).equals("Down")) {
	   	    			 		 isUp= false;
	   	    			 		 if(newScheduledElevatorRequestCheck(isUp,startFloor, endFloor ,requestType)) {
	   	    			 			 try{
	   	    			 				 ScheduledElevatorRequest req = new ScheduledElevatorRequest(new Long(Integer.parseInt(commands[0])), startFloor, isUp , endFloor, requestType );
	   	    			 				 System.out.println("New request sent to scheduler");
	   	    			 				 this.requests = new ArrayList<ScheduledElevatorRequest>();
	  	    			 				 this.requests.add(req);
	  	    			 				 sendRequestToScheduler();
	   	    			 			 } catch(Exception e){ 
	   	    			 				 printUIGuidelines();
	   	    			 			 }
	   	    			 		 }
	   	    			 	 }
		    	    			 
		    	    			
		    	    		 
	   	    		 } catch  (NumberFormatException e) {
	   	    			printUIGuidelines();
	   	    		 }
	   	    		 
	   	    	 }
	   	    	 else if(commands.length == 5 & (commands[0].split(":").length == 3)) {
	   	    		try {   
  	    			 	 int startFloor = Integer.parseInt(commands[1]);
  	    			 	 int endFloor = Integer.parseInt(commands[3]);
  	    			 	 //LocalTime time = LocalTime.of(Integer.parseInt(commands[0].split(":")[0]),Integer.parseInt(commands[0].split(":")[1]),Integer.parseInt(commands[0].split(":")[2])); 
  	    			 	 boolean isUp;
  	    			 	 int requestType = Integer.parseInt(commands[4]);
  	    			 	 
  	    			 	 if ( (commands[2]).equals("Up")){
  	    			 		 isUp = true;
  	    			 		 if(newScheduledElevatorRequestCheck(isUp,startFloor, endFloor, requestType)) {
	   	    			 	 try{
	   	    			 		 LocalTime time = LocalTime.of(Integer.parseInt(commands[0].split(":")[0]),Integer.parseInt(commands[0].split(":")[1]),Integer.parseInt(commands[0].split(":")[2])); 
	   	    			 		 ScheduledElevatorRequest req = new ScheduledElevatorRequest(time, startFloor, isUp , endFloor, requestType);
	   	    			 		 System.out.println("New request sent to scheduler");
	   	    			 		 this.requests = new ArrayList<ScheduledElevatorRequest>();
	    			 			 this.requests.add(req);
	    			 			 sendRequestToScheduler();
	   	    			 	 } catch(Exception e){
	   	    			 		 printUIGuidelines();
	   	    			 	 }
  	    			 		 }
  	    			 	 }
  	    			 	 else if ( (commands[2]).equals("Down")) {
  	    			 		 isUp= false;
  	    			 		 if(newScheduledElevatorRequestCheck(isUp,startFloor, endFloor, requestType)) {
  	    			 			 try{
  	    			 				 LocalTime time = LocalTime.of(Integer.parseInt(commands[0].split(":")[0]),Integer.parseInt(commands[0].split(":")[1]),Integer.parseInt(commands[0].split(":")[2])); 
  		   	    			 		 ScheduledElevatorRequest req = new ScheduledElevatorRequest(time, startFloor, isUp , endFloor, requestType);
  	    			 				 System.out.println("New request sent to scheduler");
  	    			 				 this.requests = new ArrayList<ScheduledElevatorRequest>();
  	    			 				 this.requests.add(req);
  	    			 				 sendRequestToScheduler();
  	    			 				 
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

	/**
	 * Runs the floorSubsystem thread
	 */
	public synchronized void run() {
		Config config = new Config("local.properties");
		//Don't schedule anything with blank input file
		this.inputFileLocation = this.askToChooseFileOrUseDefault(sc);//System.getProperty("user.dir")+"\\src\\app\\FloorSubsystem\\inputfile.txt";
		if (this.inputFileLocation.equals("")) return;
		
		
		addInputRequests(this.inputFileLocation); 
		this.sendRequestToScheduler();
		FloorSubsystem_SchedulerPacketReceiver sReceiver = new FloorSubsystem_SchedulerPacketReceiver( this, config.getInt("floor.schedulerReceivePort"));
		(new Thread(sReceiver, "FloorSubsystem_SchedulerPacketReceiver")).start();
		runCommandLineUI(sc); //, scheduler from runCommandLineUI	
	}
	
	public static void main(String[] args) {
//		Config config = new Config("multi.properties");
		Config config = new Config("local.properties");
		
		Logger logger = new Logger(config); 
		FloorSubsystem floorSubsys = new FloorSubsystem(logger, config ); //FLOOR_COUNT = 7
		
		
		Thread floorThread = new Thread(floorSubsys, "FloorSubsystemThread");
		
		floorThread.start();
		
		
		//runCommandLineUI(sc, scheduler);
	}
	
}
