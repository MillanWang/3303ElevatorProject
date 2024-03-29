package app.GUI;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;

import app.Config.Config;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.UDP.Util;

/**
 * Class for receiving one way UDP Communication for the GUI system
 * @author Millan Wang
 *
 */
public class GUI_PacketReceiver implements Runnable{
	/**
	 * Name of the packet receiver
	 */
	private String name;

	/**
	 * The socket to receive packets on
	 */
	private DatagramSocket receiveSocket;
	
	/**
	 * GUI to control 
	 */
	private GUI gui;
	
	/**
	 * Constructor for the GUI_Packet Receiver class
	 * @param name name of thread
	 * @param port port to receive packets on
	 * @param gui Reference to GUI to control
	 */
	public GUI_PacketReceiver(String name, int port, GUI gui) {
		this.name = name;
		try {
			this.receiveSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.gui = gui;
	}
	
	/**
	 * Receives and returns the next incoming packet on the current receiveSocket
	 * @return The incoming receive packet
	 */
	private DatagramPacket receiveNextPacket() {
		//Create a packet to receive next packet
        byte[] data = new byte[(new Config("multi.properties")).getInt("udp.buffer.size")];
        DatagramPacket receivedPacket = new DatagramPacket(data, data.length);

        //Receive the packet
        try {
        	this.receiveSocket.receive(receivedPacket);
        } catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
        return receivedPacket;
	}
	
	/**
	 * Handles the received packet
	 * @param requestPacket incoming GUI update packet
	 */
	private void handlePacket(DatagramPacket requestPacket) {
        //De-serialize packet contents to become input for scheduler's next floors to visit
		GUIUpdateInfo guiUpdate = null;
        try {
        	guiUpdate = (GUIUpdateInfo) Util.deserialize(requestPacket.getData());
		} catch (ClassNotFoundException | IOException e1) {e1.printStackTrace(); return;} 
        this.gui.updateView(guiUpdate);
	}

	
	/**
	 * Run method to run this packet receiver as a thread
	 */
	@Override
	public void run() {
		System.out.println("Starting " + name + "...");
		while(true) {
			this.handlePacket(this.receiveNextPacket());
		}

	}

}
