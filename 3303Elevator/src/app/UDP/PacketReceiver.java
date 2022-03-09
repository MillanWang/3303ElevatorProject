package app.UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Class for running a thread that receives packets and sends replies
 * @author Millan Wang
 *
 */
public abstract class PacketReceiver implements Runnable {
	/**
	 * Name of the packet receiver
	 */
	protected String name;
	
	/**
	 * The socket to receive packets on 
	 */
	protected DatagramSocket receiveSocket;
	
	/**
	 * Abstract Constructor for PacketReceivers
	 * @param name Name of the packet receiver
	 * @param port port number to be used by the DatagramSocket
	 */
	protected PacketReceiver(String name, int port){
		try {
			this.receiveSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.name=name;
	}
	
	/**
	 * Receives and returns the next incoming packet on the current receiveSocket
	 * @return The incoming receive packet
	 */
	private DatagramPacket receiveNextPacket() {
		//Create a packet to receive next packet
        byte[] data = new byte[512];
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
	 * Sends reply packet on a temporary DatagramSocket
	 * @param replyPacket the packet to reply with
	 */
	private void sendReply(DatagramPacket replyPacket) {
		//Create socket to send the reply packet and then close
		try {
			DatagramSocket responseSocket = new DatagramSocket();
			responseSocket.send(replyPacket);
			responseSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates a reply packet given a request packet
	 */
	abstract protected DatagramPacket createReplyPacketGivenRequestPacket(DatagramPacket requestPacket);
	
	@Override
	public void run() {
		System.out.println("Starting " + name + "...");
		while(true) {
			this.sendReply(
					createReplyPacketGivenRequestPacket(this.receiveNextPacket()));
		}

	}

}
