package tests.SchedulerTests;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import app.Logger;
import app.Config.Config;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.FloorSubsystem.ScheduledElevatorRequest;
import app.Scheduler.Scheduler;
import app.UDP.PacketReceiver;

public class SchedulerTests {
	
	Scheduler scheduler;
	Config config;
	

	/**
	 * Creates new scheduler object for each test case
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.config = new Config("test.properties");
		this.scheduler = new Scheduler(new Logger(config), config);
	}

	/**
	 * Testing that the scheduler will properly return next floors to visit given a collection of floor requests
	 */
	@Test
	public void testGetNextFloorsToVisit() {
		//Add a floor request
		LinkedList<ScheduledElevatorRequest> floorSystemRequests = new LinkedList<ScheduledElevatorRequest>();
		floorSystemRequests.add(new ScheduledElevatorRequest(0, 1, true, 9, 0));
		
		new Thread(new FakePacketReceiver("Name", this.config.getInt("elevator.port"))).start();
		this.scheduler.floorSystemScheduleRequest(floorSystemRequests);
		
		
		LinkedList<ElevatorInfo> allElevatorInfos = new LinkedList<ElevatorInfo>();
		allElevatorInfos.add(new ElevatorInfo(1, 1, 0, null, null));
		allElevatorInfos.add(new ElevatorInfo(2, 1, 0, null, null));
		allElevatorInfos.add(new ElevatorInfo(3, 1, 0, null, null));
		allElevatorInfos.add(new ElevatorInfo(4, 1, 0, null, null));
		
		HashMap<Integer,Integer> nextFloors = this.scheduler.getNextFloorsToVisit(allElevatorInfos);
		
		for (Integer i : nextFloors.keySet()) {
			if (nextFloors.get(i) != -1) {
				//Test successful, a next floor is available
				return;
			}
		}
		//Only getting here if unsuccessful
		Assert.fail();
		
	}
	
	/**
	 * Fake packet receiver that simulates ElevatorSubsystem_SchedulerPacketReceiver but doesn't involve a real ElevatorSubsystem
	 * @author Millan Wang
	 *
	 */
	private class FakePacketReceiver extends PacketReceiver{

		protected FakePacketReceiver(String name, int port) {
			super(name, port);
		}

		@Override
		protected DatagramPacket createReplyPacketGivenRequestPacket(DatagramPacket requestPacket) {
			//Create packet to reply with. Then send
	        byte[] replyData = "200 OK".getBytes();
	        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, requestPacket.getAddress(), requestPacket.getPort());
			return replyPacket;
		}
	
		@Override
		public void run() {
			System.out.println("Starting " + name + "...");
			this.sendReply(createReplyPacketGivenRequestPacket(this.receiveNextPacket()));
			System.out.println("FakePacketReceiver - DONE");

		}
		
	}
}
