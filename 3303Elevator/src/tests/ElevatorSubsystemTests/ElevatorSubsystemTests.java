package tests.ElevatorSubsystemTests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

import app.Config.Config;
import app.ElevatorSubsystem.ElevatorSubsystem;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.UDP.Util;

public class ElevatorSubsystemTests {
	
	@Test
	public void testSingleElevator() {
		Config c = new Config("test.properties");
		ElevatorSubsystem e = new ElevatorSubsystem(c);
		FakeScheduler f = new FakeScheduler(c);
		(new Thread(e)).start();
		
		HashMap<Integer, Integer> req = new HashMap<>();
		req.put(1, 4);
		
		LinkedList<ElevatorInfo> res = f.fakeNextFloorRequest(req);
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
		req.put(1, 4);
		req.put(3, 5);
		
		LinkedList<ElevatorInfo> res = f.fakeNextFloorRequest(req);
		assertTrue(this.checkIfSame(c.getInt("elevator.total.number"), res, req));
		this.closeElevatorSubsystemSockets(e, f);
	}
	
	public boolean checkIfSame(int count, LinkedList<ElevatorInfo> res, HashMap<Integer, Integer> req) {
		for(int i = 0; i < count; i++ ) {
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
		req.put(1, -3); 
		req.put(2, -3);
		req.put(3, -3); 
		req.put(4, -3);
		f.fakeNextFloorRequest(req);
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
	
	private DatagramPacket buildNextFloorPacket(HashMap<Integer, Integer> nextFloorReq) {
		byte[] data = {};
		
		try {
			data = Util.serialize(nextFloorReq);
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
	public LinkedList<ElevatorInfo> fakeNextFloorRequest(HashMap<Integer, Integer> nextFloorReq){
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
