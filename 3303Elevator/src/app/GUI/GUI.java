package app.GUI;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

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
	private JPanel floorPanel, e1Panel, e2Panel, e3Panel, e4Panel;
	private Config config;
	
	private ElevatorInfo elevator1Info;
	private ElevatorInfo elevator2Info;
	private ElevatorInfo elevator3Info;
	private ElevatorInfo elevator4Info;
	private TreeSet<Integer> elevator1Destinations;
	private TreeSet<Integer> elevator2Destinations;
	private TreeSet<Integer> elevator3Destinations;
	private TreeSet<Integer> elevator4Destinations;
	private TreeSet<Integer> allUpwardsFloorButtons;
	private TreeSet<Integer> allDownwardsFloorButtons;
	
	
	
	/**
	 * Constructor for GUI class
	 * @param numElevators - number of elevators in system
	 */
	public GUI(Config config) {
		frame = new JFrame();
		frame.setTitle("Elevator Information");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(750,750);
		frame.setLayout(new FlowLayout());
		
		this.config = config;
		int numElevators = config.getInt("elevator.total.number");
		
//		panel = new JPanel(new GridLayout((numElevators-numElevators % 4)/4, 4, 10, 10));
//		elevatorInfo = new ArrayList<ElevatorInfo>();
//		elevators = new ArrayList<Elevator>();
//		panels = new ArrayList<JPanel>();
		
		frame.setVisible(true);
		
		
		
		
//		TreeSet<Integer> up = new TreeSet<>();
//		up.add(1);
//		up.add(5);
//		up.add(6);
//		
//		TreeSet<Integer> down = new TreeSet<>();
//		down.add(3);
//		down.add(15);
//		frame.add(new FloorSubsystemGUI(up, down));
//		frame.pack();
//		
//		frame.add(new FloorSubsystemGUI(down, up));
//		
//		
//		
//		
//		
//		
//		   frame = new JFrame();
//        frame.setTitle("Elevator Information");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(750,750);
//        frame.setLayout(new FlowLayout());
//
//        this.config = config;
//        int numElevators = config.getInt("elevator.total.number");
//        
//        panel = new JPanel(new GridLayout((numElevators-numElevators % 4)/4, 4, 10, 10));
//        elevatorInfo = new ArrayList<ElevatorInfo>();
//        elevators = new ArrayList<Elevator>();
//        panels = new ArrayList<JPanel>();
//        
//
//        frame.setVisible(true);
//        //frame.add(new JLabel("JLabel in the frame no problem"));
//        //Creating treesets TEST
//        TreeSet<Integer> up = new TreeSet<>();
//        up.add(1);
//        up.add(5);
//        up.add(6);
//
//        TreeSet<Integer> down = new TreeSet<>();
//        down.add(3);
//        down.add(15);
//
//        frame.setVisible(true);
//        FSSPanel = new JPanel();
//        FSSPanel.add(new FloorSubsystemGUI(up, down));
//        frame.add(FSSPanel);
//        FSSPanel.removeAll();
//
//        //FSSPanel2 = new JPanel();
//        FSSPanel.add(new FloorSubsystemGUI(down, up));
//        frame.add(FSSPanel);
//        frame.pack();
		
		
        this.floorPanel= new JPanel();
        this.floorPanel.add(new FloorSubsystemGUI(new TreeSet<>(), new TreeSet<>()));
        
        this.e1Panel= new JPanel();
        this.e1Panel.add(new FloorSubsystemGUI(new TreeSet<>(), new TreeSet<>()));
        
        this.e2Panel= new JPanel();
        this.e2Panel.add(new FloorSubsystemGUI(new TreeSet<>(), new TreeSet<>()));
        
        this.e3Panel= new JPanel();
        this.e3Panel.add(new FloorSubsystemGUI(new TreeSet<>(), new TreeSet<>()));
        
        this.e4Panel= new JPanel();
        this.e4Panel.add(new FloorSubsystemGUI(new TreeSet<>(), new TreeSet<>()));
		
		
        frame.add(this.floorPanel);
        frame.add(this.e1Panel);
        frame.add(this.e2Panel);
        frame.add(this.e3Panel);
        frame.add(this.e4Panel);
		
        frame.pack();
	}
	
//	/**
//	 * Function to add elevator to GUI class for continuous updates
//	 * @param e - elevator
//	 */
//	public void addElevator(Elevator e) {
//		elevators.add(e);
//	}
//	
//	/**
//	 * Update elevator information continuously
//	 */
//	public void updateElevatorInfo() {
//		elevatorInfo.clear();
//		for(int i = 0; i < elevators.size(); i++) {
//			elevatorInfo.add(elevators.get(i).getInfo());
//		}
//		updateView(null);
//	}
	
//	/**
//	 * Initializes the panels for each elevator and info at 0s
//	 */
//	public void addPanel() {
//		for(int i = 0; i < elevatorInfo.size(); i++) {
//			JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));
//			JLabel id = new JLabel("Elevator " + elevatorInfo.get(i).getId());
//			JLabel currFloor = new JLabel("Current Floor: " + elevatorInfo.get(i).getFloor());
//			JLabel state = new JLabel("State: " + elevatorInfo.get(i).getState());
//			
//			p.add(id);
//			p.add(currFloor);
//			p.add(state);
//			
//			panels.add(p);
//			panel.add(p);
//		}
//		frame.add(panel);
//		frame.pack();
//		frame.setVisible(true);
//	}
	
	public void log(String msg) {
		System.out.println("[GUI_PacketReceiver]" + msg);
	}
	
	/**
	 * Updates the information displayed when changes occur
	 */
	public void updateView(GUIUpdateInfo guiUpdateInfo) {
		
		HashMap<Integer, ElevatorInfo> elevatorInfos = guiUpdateInfo.getAllElevatorInfoObject();
		if(elevatorInfos != null) {
			for(Map.Entry<Integer, ElevatorInfo> entry : elevatorInfos.entrySet()) {
				if(entry.getKey() == 1) {
					this.elevator1Info = entry.getValue();
				}else if(entry.getKey() == 2) {
					this.elevator2Info = entry.getValue();
				}else if(entry.getKey() == 3) {
					this.elevator3Info = entry.getValue();
				}else if(entry.getKey() == 4) {
					this.elevator4Info = entry.getValue();
				}
			}
		}
		
		HashMap<Integer, TreeSet<Integer>> elevatorDestinations_pressedButtons = guiUpdateInfo.getAllElevatorDestinations();
		if (elevatorDestinations_pressedButtons!=null) {
			//Safe to override if they are sent. All state tracking done on the scheduler side
			for (Integer id : elevatorDestinations_pressedButtons.keySet()) {
				if (id==1) {
					this.elevator1Destinations = elevatorDestinations_pressedButtons.get(id);
				} else if (id==2) {
					this.elevator2Destinations = elevatorDestinations_pressedButtons.get(id);
				} else if (id==3) {
					this.elevator3Destinations = elevatorDestinations_pressedButtons.get(id);
				} else if (id==4) {
					this.elevator4Destinations = elevatorDestinations_pressedButtons.get(id);
				}
			}
		}
		
		TreeSet<Integer> upwardsFloorButtons = guiUpdateInfo.getAllUpwardsFloorButtons();
		if (upwardsFloorButtons!=null) {
			//Safe to override every time. Should send the whole thing and not just the delta
			this.allUpwardsFloorButtons = upwardsFloorButtons;
		}
		TreeSet<Integer> downwardsFloorButtons = guiUpdateInfo.getAllDownwardsFloorButton();
		if (downwardsFloorButtons!=null) {
			//Safe to override every time. Should send the whole thing and not just the delta
			this.allDownwardsFloorButtons = downwardsFloorButtons;
		}
		
		
		refreshView();
	}
	
	private void refreshView() {
		this.floorPanel.removeAll();
        this.floorPanel.add(new FloorSubsystemGUI(allUpwardsFloorButtons, allDownwardsFloorButtons));
        
        this.e1Panel.removeAll();
        this.e1Panel.add(new FloorSubsystemGUI(new TreeSet<>(), new TreeSet<>()));
        
        this.e2Panel.removeAll();
        this.e2Panel.add(new FloorSubsystemGUI(new TreeSet<>(), new TreeSet<>()));
        
        this.e3Panel.removeAll();
        this.e3Panel.add(new FloorSubsystemGUI(new TreeSet<>(), new TreeSet<>()));
        
        this.e4Panel.removeAll();
        this.e4Panel.add(new FloorSubsystemGUI(new TreeSet<>(), new TreeSet<>()));
        
        this.frame.pack();
		
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
