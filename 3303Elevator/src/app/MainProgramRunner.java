package app;

import java.io.File;
import java.util.Scanner;

import javax.swing.JFileChooser;

import app.ElevatorSubsystem.ElevatorSubsystem;
import app.FloorSubsystem.FloorSubsystem;
import app.FloorSubsystem.Logger;
import app.Scheduler.Scheduler;

public class MainProgramRunner {
	public static final int FLOOR_COUNT = 7;
	public static final boolean INSTANTLY_SCHEDULE_REQUESTS = true;
	public static final float TIME_MULTIPLIER = 0;
	public static final String UI_COMMAND_EXPLAIN_STRING = "Elevator Simulation Program : Type a command and press enter to continue\nCommands:  \n\t\"n\" - schedule next request\n\t\"q\" - exit program";
	public static final String UI_ASK_TO_CHOOSE_FILE_STRING = "Welcome to the Elevator simulation program. \nWould you like to choose an input file or use the default? \n\t\"y\" - choose file\n\t\"n\"  - default file";
	public static final boolean ELEVATOR_LOGGING = true;
	public static final boolean SCHEDULER_LOGGING = true;
	public static final boolean FLOORSUBSYSTEM_LOGGING = true;
	public static final boolean TIMEMANAGEMENT_LOGGING = true;
	public static final String DEFAULT_INPUT_FILE_ABSOLUTE_PATH = System.getProperty("user.dir")+"\\src\\app\\FloorSubsystem\\inputfile.txt";
	
	
	public static void main(String[] args) {
		Logger log = new Logger(ELEVATOR_LOGGING,SCHEDULER_LOGGING ,FLOORSUBSYSTEM_LOGGING,TIMEMANAGEMENT_LOGGING); 
		Scanner sc = new Scanner(System.in);

		Scheduler scheduler = new Scheduler(FLOOR_COUNT, INSTANTLY_SCHEDULE_REQUESTS);

    //Asks user via cmd line if they want to specify an input file or go with default
		FloorSubsystem floorSubsys = new FloorSubsystem(scheduler,askToChooseFileOrUseDefault(sc), log);

		ElevatorSubsystem elevatorSubsys = new ElevatorSubsystem(scheduler, FLOOR_COUNT, TIME_MULTIPLIER);
		scheduler.setFloorSubsys(floorSubsys);
		
		Thread elevatorThread = new Thread(elevatorSubsys, "ElevatorSubsystemThread");
		Thread floorThread = new Thread(floorSubsys, "FloorSubsystemThread");
		
		elevatorThread.start();
		floorThread.start();
		
		runCommandLineUI(sc, scheduler);
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
	    		 return chooseFile();
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
    private static String chooseFile(){
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
	private static void runCommandLineUI(Scanner sc, Scheduler scheduler) {
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
		    	    		 if (startFloor<1 || endFloor<1 || startFloor>FLOOR_COUNT || endFloor>FLOOR_COUNT ) {
		    	    			 System.err.println("Invalid floors received");
		    	    		 } else if (startFloor == endFloor) {
		    	    			 System.err.println("Current floor and destination floors cannot be the same");
		    	    		 }else {
		    	    			 System.out.println("New request sent to scheduler");
		    	    			 scheduler.addElevatorRequest(startFloor, endFloor);
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
}
