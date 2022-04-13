package tests.ElevatorSubsystemTests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import org.junit.Test;

import app.Config.Config;
import app.ElevatorSubsystem.ElevatorSubsystem;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.Scheduler.SchedulerInfo;
import app.UDP.Util;
/***
 * NOTE: Elevator subsystem testing can be buggy when trying to run all the test
 * 		 this is due to fact it is trying to test the full system and the next test can 
 * 		some times run before the last test finished. Best is to test one case at a time. 
 * 
 * @author benki
 *
 */
public class ElevatorSubsystemTests {
	
	@Test
	public void testSingleElevator() {
		//Testing a single request moving up then down
		Config c = new Config("test.properties");
		ElevatorSubsystem e = new ElevatorSubsystem(c);
		FakeScheduler f = new FakeScheduler(c);
		(new Thread(e)).start();
		
		HashMap<Integer, Integer> req = new HashMap<>();
		HashMap<Integer, Integer> errors = new HashMap<>();
		req.put(1, 4);
		
		SchedulerInfo info = new SchedulerInfo(req, errors);
		
		LinkedList<ElevatorInfo> res = f.fakeNextFloorRequest(info);
		assertTrue(this.checkIfSame(c.getInt("elevator.total.number"), res, req));
		
		req = new HashMap<>();
		errors = new HashMap<>();
		req.put(1, 2);
		info = new SchedulerInfo(req, errors);
		res = f.fakeNextFloorRequest(info);
		assertTrue(this.checkIfSame(c.getInt("elevator.total.number"), res, req));
		this.closeElevatorSubsystemSockets(e, f);
	}

	@Test
	public void testMultiElevator() {
		Config c = new Config("test.properties");
		ElevatorSubsystem e = new ElevatorSubsystem(c);
		FakeScheduler f = new FakeScheduler(c);
		(new Thread(e)).start();
		
		HashMap<Integer, Integer> req = new HashMap<>();
		HashMap<Integer, Integer> errors = new HashMap<>();
		req.put(1, 4);
		req.put(3, 5);
		
		SchedulerInfo info = new SchedulerInfo(req, errors);
		
		LinkedList<ElevatorInfo> res = f.fakeNextFloorRequest(info);
		assertTrue(this.checkIfSame(c.getInt("elevator.total.number"), res, req));
		this.closeElevatorSubsystemSockets(e, f);
	}
	
	@Test
	public void testMultiElevatorMultiTimes() {
		Config c = new Config("test.properties");
		ElevatorSubsystem e = new ElevatorSubsystem(c);
		FakeScheduler f = new FakeScheduler(c);
		(new Thread(e)).start();
		
		HashMap<Integer, Integer> req = new HashMap<>();
		HashMap<Integer, Integer> errors = new HashMap<>();
		req.put(1, 4);
		req.put(3, 5);
		
		SchedulerInfo info = new SchedulerInfo(req, errors);
		LinkedList<ElevatorInfo> res = f.fakeNextFloorRequest(info);
		assertTrue(this.checkIfSame(c.getInt("elevator.total.number"), res, req));
		
		req = new HashMap<>();
		errors = new HashMap<>();
		
		req.put(1, 2);
		req.put(3, 15);
		req.put(4, 20);
		
		info = new SchedulerInfo(req, errors);
		res = f.fakeNextFloorRequest(info);
		assertTrue(this.checkIfSame(c.getInt("elevator.total.number"), res, req));
		this.closeElevatorSubsystemSockets(e, f);
	}
	
	@Test
	public void testOutOfBondsFloor() {
		Config c = new Config("test.properties");
		ElevatorSubsystem e = new ElevatorSubsystem(c);
		FakeScheduler f = new FakeScheduler(c);
		(new Thread(e)).start();
		
		HashMap<Integer, Integer> req = new HashMap<>();
		HashMap<Integer, Integer> errors = new HashMap<>();
		
		req.put(1, -5);
		req.put(3, 1000);
		
		SchedulerInfo info = new SchedulerInfo(req, errors);
		LinkedList<ElevatorInfo> res = f.fakeNextFloorRequest(info);
		
		
		// update the required requests to 
		req.put(3, 1);
		req.put(1, 1);
		assertTrue(this.checkIfSame(c.getInt("elevator.total.number"), res, req));
		this.closeElevatorSubsystemSockets(e, f);
	}
	
	@Test
	public void testPermErrorThreads() {
		Config c = new Config("test.properties");
		ElevatorSubsystem e = new ElevatorSubsystem(c);
		FakeScheduler f = new FakeScheduler(c);
		(new Thread(e)).start();
		
		HashMap<Integer, Integer> req = new HashMap<>();
		HashMap<Integer, Integer> errors = new HashMap<>();
		errors.put(1, -3);
		
		SchedulerInfo info = new SchedulerInfo(req, errors); 
		LinkedList<ElevatorInfo> res = f.fakeNextFloorRequest(info);
		
		req = new HashMap<>();
		errors = new HashMap<>();
		errors.put(2, -3);
		
		info = new SchedulerInfo(req, errors); 
		res = f.fakeNextFloorRequest(info);
		
		req = new HashMap<>();
		errors = new HashMap<>();
		errors.put(3, -3);
		
		info = new SchedulerInfo(req, errors); 
		res = f.fakeNextFloorRequest(info);
		
		assertTrue(this.checkIfSame(c.getInt("elevator.total.number"), res, req));
		this.closeElevatorSubsystemSockets(e, f);
	}
	
	public boolean checkIfSame(int count, LinkedList<ElevatorInfo> res, HashMap<Integer, Integer> req) {
		for(int i = 0; i < count; i++ ) {
			if(!res.contains(i)) {
				continue;
			}
			
			int id = res.get(i).getId();
			if(req.containsKey(id)) {
				if(req.get(id) != res.get(i).getFloor()) {
					System.out.println(id + " " + req.get(id) + " " + res.get(id).getFloor());
					return false;
				}
			}
		}
		
		return true;
	}
	
	private void closeElevatorSubsystemSockets(ElevatorSubsystem e, FakeScheduler f) {
		e.exit();
		HashMap<Integer, Integer>req = new HashMap<>();
		HashMap<Integer, Integer>errors = new HashMap<>();
		errors.put(1, -3); 
		errors.put(2, -3);
		errors.put(3, -3); 
		errors.put(4, -3);
		
		SchedulerInfo info = new SchedulerInfo(req, errors);
		
		f.fakeNextFloorRequest(info);
		f.closeSocket();
	}

}

class FakeScheduler {
	
	private DatagramSocket rSocket;
	private InetSocketAddress elevatorAddr;
	private int bufferSize;

	public FakeScheduler(Config config) {
		try {
			rSocket = new DatagramSocket(config.getInt("scheduler.elevatorReceivePort"));		
			this.bufferSize = config.getInt("udp.buffer.size");
			this.elevatorAddr = new InetSocketAddress(config.getString("elevator.address"), config.getInt("elevator.port"));
		}catch(IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}catch(Exception e2) {
			e2.printStackTrace();
			System.exit(1);
		}
	}
	
	private DatagramPacket buildNextFloorPacket(SchedulerInfo info) {
		byte[] data = {};
		
		try {
			data = Util.serialize(info);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return new DatagramPacket(
				data,
				data.length,
				elevatorAddr.getAddress(),
				elevatorAddr.getPort()
		);
	}
	
	protected DatagramPacket receiveNextPacket() {
		//Create a packet to receive next packet
        byte[] data = new byte[bufferSize];
        DatagramPacket receivedPacket = new DatagramPacket(data, data.length);

        //Receive the packet
        try {
        	this.rSocket.receive(receivedPacket);
        } catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
        
        data = "200 OK".getBytes();
        this.sendReply(new DatagramPacket(data, data.length, receivedPacket.getAddress(), receivedPacket.getPort()));
        return receivedPacket;
	}
	
	protected void sendReply(DatagramPacket packet) {
		//Create socket to send the reply packet and then close
		try {
			DatagramSocket responseSocket = new DatagramSocket();
			responseSocket.send(packet);
			responseSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@SuppressWarnings("unchecked")
	public LinkedList<ElevatorInfo> fakeNextFloorRequest(SchedulerInfo nextFloorReq){
		LinkedList<ElevatorInfo> elevatorInfoRes = null;
		DatagramPacket sendPacket = this.buildNextFloorPacket(nextFloorReq);
		DatagramPacket res = Util.sendRequest_ReturnReply(sendPacket);
		
		res = this.receiveNextPacket();
		
		try {
			Object obj = Util.deserialize(res.getData());
			elevatorInfoRes = (LinkedList<ElevatorInfo>) obj;
		}catch(IOException e1) {
			e1.printStackTrace();
			return null;
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
			return null;
		}
		
		return elevatorInfoRes;
	}
	
	public void closeSocket() {
		this.rSocket.close();
	}
	
}
