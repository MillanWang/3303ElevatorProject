package app;

import java.util.Scanner;

import app.ElevatorSubsystem.ElevatorSubsystem;
import app.FloorSubsystem.FloorSubsystem;
import app.Scheduler.Scheduler;

public class MainProgramRunner {
	public static final int FLOOR_COUNT = 7;
	public static final boolean INSTANTLY_SCHEDULE_REQUESTS = true;
	
	
	public static void main(String[] args) {
		
		float timeMultiplier = 0; 
		
		Scheduler scheduler = new Scheduler(FLOOR_COUNT, INSTANTLY_SCHEDULE_REQUESTS);
		FloorSubsystem floorSubsys = new FloorSubsystem(scheduler);
		ElevatorSubsystem elevatorSubsys = new ElevatorSubsystem(scheduler, FLOOR_COUNT, timeMultiplier);
		scheduler.setFloorSubsys(floorSubsys);
		
		Thread elevatorThread = new Thread(elevatorSubsys, "ElevatorSubsystemThread");
		Thread floorThread = new Thread(floorSubsys, "FloorSubsystemThread");
		
		elevatorThread.start();
		floorThread.start();
		
		
		//COMMAND LINE QUIT OPTION
	     Scanner sc = new Scanner(System.in);
	     System.out.println("Elevator program running. Press q then enter to exit");
	     while(sc.hasNextLine()) {
	    	 if (sc.next().equals("q")) {
	    		 System.out.println("Successfully terminated elevator program");
	    		 System.exit(0);
	    	 }
	    	 System.out.println("Press q then enter to exit");
	     }
	     
		
	}
}
