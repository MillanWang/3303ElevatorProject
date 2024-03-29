package app;

import app.Config.Config;
import app.ElevatorSubsystem.ElevatorSubsystem;
import app.FloorSubsystem.FloorSubsystem;
import app.GUI.GUI;
import app.Scheduler.Scheduler;
import app.Scheduler.TimeManagementSystem;

public class MainProgramRunner {
	
	
	public static final int FLOOR_COUNT = 7;
	public static final int ELEVATOR_COUNT = 4;
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
	
	/**
	 * Main method for running all subsystems
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		Config config = new Config("local.properties");
		Logger logger = new Logger(config); 
		Scheduler scheduler = new Scheduler(logger, config);
		FloorSubsystem floorSubsys = new FloorSubsystem(logger, config);
		ElevatorSubsystem elevatorSubsys = new ElevatorSubsystem(config);
		GUI gui = new GUI(config);
		
		Thread schedulerThread = new Thread(scheduler, "SchedulerThread");
		Thread floorThread = new Thread(floorSubsys, "FloorSubsystemThread");
		Thread elevatorThread = new Thread(elevatorSubsys, "ElevatorSubsystemThread");
		Thread guiThread = new Thread(gui, "StartGUI_Thread");

		guiThread.start();
		schedulerThread.start();
		floorThread.start();
		elevatorThread.start();

	}
	
}
