package app.UDP;

import java.io.*;
import java.net.*;
import java.net.InetAddress;
import java.net.SocketAddress;

public class Util {

	/**
     * Sends the given packet via a dynamically made DatagramSocket and
     * returns the received reply packet
     *
     * @param packet The packet to send
     * @return The the received data packet
     */
    public static DatagramPacket sendRequest(DatagramPacket packet) {
        //Create socket instance to send request
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Create a packet to hold the reply packet
        byte[] data = new byte[512];
        DatagramPacket receievedPacket = new DatagramPacket(data, data.length);

        //Receive reply response on same socket before closing
        try {
            socket.receive(receievedPacket);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return receievedPacket;
    }
}
