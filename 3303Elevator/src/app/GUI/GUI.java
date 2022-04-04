package app.GUI;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.GridLayout;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import app.Config.Config;
import app.ElevatorSubsystem.Elevator.*;
import app.UDP.Util;

/**
 * GUI class to display each elevator status and make new requests
 * @author Abdelrahim Karaja
 *
 */
public class GUI implements Runnable {
	private ArrayList<ElevatorInfo> elevatorInfo;
	private ArrayList<JPanel> panels;
	private ArrayList<Elevator> elevators;
	private JFrame frame;
	private JPanel panel;
	private Config config;
	
	/**
	 * Constructor for GUI class
	 * @param numElevators - number of elevators in system
	 */
	public GUI(Config config) {
		frame = new JFrame();
		frame.setTitle("Elevator Information");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(750,750);
		
		this.config = config;
		int numElevators = config.getInt("elevator.total.number");
		
		panel = new JPanel(new GridLayout((numElevators-numElevators % 4)/4, 4, 10, 10));
		elevatorInfo = new ArrayList<ElevatorInfo>();
		elevators = new ArrayList<Elevator>();
		panels = new ArrayList<JPanel>();
		
		frame.setVisible(true);
		frame.add(new JLabel("JLabel in the frame no problem"));
	}
	
	/**
	 * Function to add elevator to GUI class for continuous updates
	 * @param e - elevator
	 */
	public void addElevator(Elevator e) {
		elevators.add(e);
	}
	
	/**
	 * Update elevator information continuously
	 */
	public void updateElevatorInfo() {
		elevatorInfo.clear();
		for(int i = 0; i < elevators.size(); i++) {
			elevatorInfo.add(elevators.get(i).getInfo());
		}
		updateView(null);
	}
	
	/**
	 * Initializes the panels for each elevator and info at 0s
	 */
	public void addPanel() {
		for(int i = 0; i < elevatorInfo.size(); i++) {
			JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));
			JLabel id = new JLabel("Elevator " + elevatorInfo.get(i).getId());
			JLabel currFloor = new JLabel("Current Floor: " + elevatorInfo.get(i).getFloor());
			JLabel state = new JLabel("State: " + elevatorInfo.get(i).getState());
			
			p.add(id);
			p.add(currFloor);
			p.add(state);
			
			panels.add(p);
			panel.add(p);
		}
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Updates the information displayed when changes occur
	 */
	public void updateView(GUIUpdateInfo guiUpdateInfo) { 
		//TODO: Make sure that this is updated with the new comms object
		System.out.println("Gotta update the view my dude");
	}
	
	/**
	 * Run method for starting the necessary threads associated with the GUI subsystem
	 */
	@Override
	public void run() {
		(new Thread(new GUI_PacketReceiver("GUI_PacketReceiver", this.config.getInt("gui.port"), this), "GUI_PacketReceiver")).start();
	}
	
	/**
	 * Main method for starting the GUI subsystem
	 * @param args
	 */
	public static void main(String[] args) {
//		Config config = new Config("multi.properties");
		Config config = new Config("local.properties");
		(new Thread(new GUI(config), "GUI")).start();
	}
	
}
