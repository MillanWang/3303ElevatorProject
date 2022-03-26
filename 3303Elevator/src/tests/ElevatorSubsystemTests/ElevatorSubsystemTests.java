package tests.ElevatorSubsystemTests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import app.Config.Config;
import app.ElevatorSubsystem.ElevatorSubsystem;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.UDP.Util;

public class ElevatorSubsystemTests {
	
	private LinkedList<ElevatorInfo> res1, res2;
	private HashMap<Integer, Integer> testCase;
	
	@Before 
	public void setUp() {
		Config config = new Config("test.properties");
		Thread s, e;
		
		testCase = new HashMap<>();
		testCase.put(1, 5);
		FakeScheduler f = new FakeScheduler(config, testCase);
		ElevatorSubsystem eSub = new ElevatorSubsystem(config);
		
		s = new Thread(f,"Scheduler");
		e = new Thread(eSub);
		s.start();
		
		e.start();
		
		try {
			e.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		res1 = f.getFirst();
		res2 = f.getSecond();
	}
	
	@Test
	public void test() {
		Config config = new Config("test.properties");
		assertTrue(checkIfSame(config.getInt("elevator.total.number"), res2, testCase));
	}
	
	public boolean checkIfSame(int count, LinkedList<ElevatorInfo> res, HashMap<Integer, Integer> req) {
		for(int i = 0; i < count; i++ ) {
			int id = res.get(i).getId();
			if(req.containsKey(id)) {
				if(req.get(id) != res.get(i).getFloor()) {
					return false;
				}
			}
		}
		
		return true;
	}

}

class FakeScheduler implements Runnable {
	
	private DatagramSocket socket;
	private DatagramPacket packet;
	private HashMap<Integer, Integer> request;
	private LinkedList<ElevatorInfo> first, second;
	private int bufferSize;
	private boolean isDone, isWaiting;
	
	public FakeScheduler(Config config, HashMap<Integer, Integer> request) {
		try {
			this.request = request;
			socket = new DatagramSocket(config.getInt("scheduler.elevatorReceivePort"));		
			this.bufferSize = config.getInt("udp.buffer.size");
			isDone = false;
			isWaiting = false;
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public LinkedList<ElevatorInfo> getFirst(){
		return first;
	}

	public LinkedList<ElevatorInfo> getSecond(){
		return second;
	}
	
	public boolean isDone() {
		return isDone; 
	}
	
	public boolean isWaiting() {
		return isWaiting;
	}
	
	
	@SuppressWarnings("unchecked")
	public void run() {
		try {
			// Waiting to send the first packet
			byte[] data = new byte[this.bufferSize];
			packet = new DatagramPacket(data,data.length);
			isWaiting = true;
			socket.receive(packet);
			Object obj = Util.deserialize(packet.getData());
			this.first = (LinkedList<ElevatorInfo>) obj;
			data = Util.serialize(this.request);
			packet = new DatagramPacket(data, data.length,packet.getAddress(), packet.getPort());
			socket.send(packet);
			// Waiting for the second packet
			data = new byte[this.bufferSize];
			packet = new DatagramPacket(data,data.length);
			socket.receive(packet);
			obj = Util.deserialize(packet.getData());
			this.second = (LinkedList<ElevatorInfo>) obj;
			isDone = true;
			
			// Shutting down all elevators
			request = new HashMap<Integer,Integer>();
			
			request.put(1, -3);
			request.put(2, -3);
			request.put(3, -3);
			request.put(4, -3);
			
			data = Util.serialize(request);
			packet = new DatagramPacket(data, data.length,packet.getAddress(), packet.getPort());
			socket.send(packet);
		}catch(IOException e){
			e.printStackTrace();
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
	}	
}
