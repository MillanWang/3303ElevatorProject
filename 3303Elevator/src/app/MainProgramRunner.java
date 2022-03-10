package app;

import java.io.File;
import java.util.Scanner;

import javax.swing.JFileChooser;

import app.ElevatorSubsystem.ElevatorSubsystem;
import app.FloorSubsystem.FloorSubsystem;
import app.Scheduler.Scheduler;
import app.Scheduler.TimeManagementSystem;

public class MainProgramRunner {
	public static final int FLOOR_COUNT = 7;
	public static final boolean INSTANTLY_SCHEDULE_REQUESTS = true;
	public static final float TIME_MULTIPLIER = 0;
	public static final String UI_COMMAND_EXPLAIN_STRING = "Elevator Simulation Program : Type a command and press enter to continue\nCommands:  \n\t\"n\" - schedule next request\n\t\"q\" - exit program";
	public static final String UI_ASK_TO_CHOOSE_FILE_STRING = "Welcome to the Elevator simulation program. \nWould you like to choose an input file or use the default? \n\t\"y\" - choose file\n\t\"n\" - no input file";
	
	//LOGGER INITIALIZATION PARAMS
	public static final boolean ELEVATOR_LOGGING = true;
	public static final boolean SCHEDULER_LOGGING = true;
	public static final boolean FLOORSUBSYSTEM_LOGGING = true;
	public static final boolean TIMEMANAGEMENT_LOGGING = true;


	public static final String DEFAULT_INPUT_FILE_ABSOLUTE_PATH = System.getProperty("user.dir")+"\\src\\app\\FloorSubsystem\\inputfile.txt";
	
	
	public static void main(String[] args) {

		Logger logger = new Logger(ELEVATOR_LOGGING,SCHEDULER_LOGGING ,FLOORSUBSYSTEM_LOGGING,TIMEMANAGEMENT_LOGGING); 

		Scheduler scheduler = new Scheduler(logger, FLOOR_COUNT, INSTANTLY_SCHEDULE_REQUESTS);

		ElevatorSubsystem elevatorSubsys = new ElevatorSubsystem(scheduler, FLOOR_COUNT, TIME_MULTIPLIER, logger);
		//Asks user via cmd line if they want to specify an input file or go with default
		FloorSubsystem floorSubsys = new FloorSubsystem(scheduler,askToChooseFileOrUseDefault(sc), logger);
		scheduler.setFloorSubsys(floorSubsys);
		
		
		TimeManagementSystem tms = new TimeManagementSystem(TIME_MULTIPLIER, logger); //Time management system to be used by all elevators
		ElevatorSubsystem elevatorSubsys = new ElevatorSubsystem(scheduler, FLOOR_COUNT, logger, tms);
		
		Thread elevatorThread = new Thread(elevatorSubsys, "ElevatorSubsystemThread");
		Thread floorThread = new Thread(floorSubsys, "FloorSubsystemThread");
		
		elevatorThread.start();
		floorThread.start();
		
		//runCommandLineUI(sc, scheduler);
	}
	
}
