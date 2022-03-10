package app.UDP;

import java.io.*;
import java.net.*;
import java.net.InetAddress;
import java.net.SocketAddress;

import app.Config.Config;

public class Util {
	
	private static Config config = new Config("local.properties");

	/**
     * Sends the given packet via a dynamically made DatagramSocket and
     * returns the received reply packet
     *
     * @param packet The packet to send
     * @return The the received data packet
     */
    public static DatagramPacket sendRequest_ReturnReply(DatagramPacket packet) {
        //Create socket instance to send request
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int bufferSize = config.getInt("udp.buffer.size");
        //Create a packet to hold the reply packet
        byte[] data = new byte[bufferSize];
        DatagramPacket receivedPacket = new DatagramPacket(data, data.length);

        //Receive reply response on same socket before closing
        try {
            socket.receive(receivedPacket);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return receivedPacket;
    }
    
    /***
     * Given an object that can be serialized returns the bytes for that object
     * 
     * @param s an object that can be serialized
     * @return an array of bytes
     * @throws IOException 
     */
    public static byte[] serialize(Serializable s) throws IOException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ObjectOutputStream oos = new ObjectOutputStream(baos);
    	oos.writeObject(s);
    	oos.close();
    	return baos.toByteArray();
    }
    
    
    /***
     * Given an array of bytes returns a general object, which will than need to be wrapped
     * 
     * @param b an array of bytes
     * @return a general object that needs to be wrapped
     * @throws IOException 
     * @throws ClassNotFoundException
     */
    public static Object deserialize(byte[] b) throws IOException, ClassNotFoundException{
    	   	ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
    	   	Object obj = ois.readObject();
    	   	ois.close();
    	   	return obj;
    }
    
    
}
