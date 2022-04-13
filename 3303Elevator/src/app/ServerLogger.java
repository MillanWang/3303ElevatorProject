/**
 * 
 */
package app;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import app.Config.Config;
import app.UDP.PacketReceiver;
import app.UDP.Util;

/**
 * @author Abdelrahim
 *
 * ServerLogger class to log all program operations in one location
 */
public class ServerLogger extends PacketReceiver{
	
	private BufferedWriter writer; //writes to the logger file 
	
	public ServerLogger(int port , String filename) {
		super("Server Logger", port);
		try {
			
			writer = new BufferedWriter(new FileWriter(filename, false));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
			writer.newLine();
			writer.write(message); //writes the logger message to the logger file 
			writer.flush();
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
		//creating logger filename using current date and time 
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime now = LocalDateTime.now(); 
		String time = now.format(formatter);
		time = time +"-logger.txt";
		ServerLogger sLogger = new ServerLogger(c.getInt("logger.port"), time ); 
		Thread sLoggerThread = new Thread(sLogger, "Server Logger Thread");
		sLoggerThread.run();
	}

}
