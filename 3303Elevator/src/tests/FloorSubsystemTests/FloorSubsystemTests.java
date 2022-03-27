/**
 * Elevator project
 * the floor subsystem test class is responsible for testing the requests communications from and to the scheduler
 * 
 * @author Peter Tanyous
 */
package tests.FloorSubsystemTests;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.Logger;
import app.Config.Config;
import app.ElevatorSubsystem.Direction.Direction;
import app.FloorSubsystem.FloorSubsystem;
import app.FloorSubsystem.ScheduledElevatorRequest;
import app.FloorSubsystem.FloorSubsystemThreads.SchedulerPacketReceiver;
import app.Scheduler.Scheduler;
import app.Scheduler.SchedulerThreads.ElevatorSubsystemPacketReceiver;
import app.Scheduler.SchedulerThreads.FloorSubsystemPacketReceiver;

public class FloorSubsystemTests {
	Config config = new Config("test.properties");
	Logger log = new Logger(config);
	int floorSubsystemReceivePort = config.getInt("scheduler.floorReceivePort");
	Scheduler scheduler = new Scheduler(log, config); 
	FloorSubsystem floorSubsys = new FloorSubsystem(log, config);
	ScheduledElevatorRequest testInput; 
	private final PrintStream standardOut = System.out;
	private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
	
	@Before
	public void setUp() {
	    System.setOut(new PrintStream(outputStreamCaptor));
	}
	
	@After
	public void tearDown() {
	    System.setOut(standardOut);
	}
	@Test
	/**
	 * tests the InputRequests in floorSubsystem (Checks for proper file reading, and additional getter methods to confirm the addition of the requests
	 */
	public void addInputRequeststest() {
		
		floorSubsys.setInputFile(System.getProperty("user.dir")+"\\src\\app\\FloorSubsystem\\inputfile.txt");
		floorSubsys.addInputRequests("src/app/FloorSubsystem/inputfile.txt");
		assertNotEquals(0,floorSubsys.getRequests().size());
		assertNull(floorSubsys.getRequests().get(floorSubsys.getRequests().size() - 1).getTime());
		assertNotNull(floorSubsys.getRequests().get(0).getTime());
	}
	@Test
	/**
	 * tests the schedule requests to be received from the scheduler 
	 */
	public void addScheduleRequeststest() {
		testInput = new ScheduledElevatorRequest(LocalTime.now(), 1, true, 5 , 0);//LocalTime time, int startfloor, boolean Upwards, int destinationfloor
		assertEquals(0, floorSubsys.getRequests().size());
		floorSubsys.getRequests().add(testInput);
		assertEquals(1, floorSubsys.getRequests().size());
	}
	@Test 
	/**
	 * tests the updateElevatorPosition method in floorSubsystem get status update from scheduler
	 */
	public void updateElevatorPositiontest() {
		assertNull(floorSubsys.getElevatorPosition());
		assertNull(floorSubsys.getElevatorStatus()); 
		floorSubsys.updateElevatorPosition(2, Direction.UP);
		assertEquals( 2 , (int)floorSubsys.getElevatorPosition()); 
		assertEquals(Direction.UP, floorSubsys.getElevatorStatus());
	}
//	
	//testing scheduler packets
	@Test
	public void packetstest() {
	FloorSubsystemPacketReceiver fssReceiver = new FloorSubsystemPacketReceiver(floorSubsystemReceivePort, scheduler);
	(new Thread(fssReceiver, "FloorSubsystemPacketReceiver")).start();
	floorSubsys.sendRequestToScheduler();
	assertTrue(outputStreamCaptor.toString().trim().contains("Scheduler received request(s) from floor system")); //assures that scheduler received the requests 
	}
}

