package app;

import java.util.Scanner;

import app.ElevatorSubsystem.ElevatorSubsystem;
import app.FloorSubsystem.FloorSubsystem;
import app.Scheduler.Scheduler;

public class MainProgramRunner {
	public static final int FLOOR_COUNT = 7;
	public static final boolean INSTANTLY_SCHEDULE_REQUESTS = true;
	public static final float TIME_MULTIPLIER = 0;
	public static final String UI_COMMAND_EXPLAIN_STRING = "Elevator Simulation Program : Type a command and press enter to continue\nCommands:  \n\t\"n\" - schedule next request\n\t\"q\" - exit program";
	
	
	public static void main(String[] args) {

		Scheduler scheduler = new Scheduler(FLOOR_COUNT, INSTANTLY_SCHEDULE_REQUESTS);
		FloorSubsystem floorSubsys = new FloorSubsystem(scheduler);
		ElevatorSubsystem elevatorSubsys = new ElevatorSubsystem(scheduler, FLOOR_COUNT, TIME_MULTIPLIER);
		scheduler.setFloorSubsys(floorSubsys);
		
		Thread elevatorThread = new Thread(elevatorSubsys, "ElevatorSubsystemThread");
		Thread floorThread = new Thread(floorSubsys, "FloorSubsystemThread");
		
		elevatorThread.start();
		floorThread.start();
		
		runCommandLineUI(scheduler);
		
		
	}
	
	/**
	 * Command line user interface for adding new requests to the elevator system dynamically
	 * @param scheduler The elevator system scheduler
	 */
	private static void runCommandLineUI(Scheduler scheduler) {
	     Scanner sc = new Scanner(System.in);
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
	    		 System.out.println("Enter your command with the following format <CurrentFloor> <DestinationFloor>\n"+"Example: 2 6 for a request to go from fllor 2 to 6");
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
