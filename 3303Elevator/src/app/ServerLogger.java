/**
 * 
 */
package app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

import app.Config.Config;
import app.UDP.PacketReceiver;
import app.UDP.Util;

/**
 * @author Abdelrahim
 *
 * ServerLogger class to log all program operations in one location
 */
public class ServerLogger extends PacketReceiver{
	
	public ServerLogger(int port) {
		super("Server Logger", port);
	};
	
	/**
	 * Creates a reply packet given a request packet
	 */
	@Override
	protected DatagramPacket createReplyPacketGivenRequestPacket(DatagramPacket requestPacket) {
        //Deserialize packet contents to become input for scheduler's next floors to visit
        try {
			String message = (String) Util.deserialize(requestPacket.getData());
			System.out.println(message);
		} catch (ClassNotFoundException | IOException e1) {e1.printStackTrace();}
        
        //Create byte array to build reply packet contents more easily
        ByteArrayOutputStream packetMessageOutputStream = new ByteArrayOutputStream();
        
        //Write serialized response object to packet
        try {
			packetMessageOutputStream.write(Util.serialize("200 OK Message Received"));
		} catch (IOException e) {e.printStackTrace();}
        
        //Create packet to reply with. Then send
        byte[] replyData = packetMessageOutputStream.toByteArray();
        DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, requestPacket.getAddress(), requestPacket.getPort());
		return replyPacket;
	}
	
	/**
	 * Main method to start serverlogger thread to send and receive log data
	 * @param args
	 */
	public static void main(String[] args) {
		Config c = new Config("multi.properties");
		// TODO Auto-generated method stub
		ServerLogger sLogger = new ServerLogger(c.getInt("logger.port"));
		Thread sLoggerThread = new Thread(sLogger, "Server Logger Thread");
		sLoggerThread.run();
	}

}
