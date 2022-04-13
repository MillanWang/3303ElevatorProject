package app.GUI;

import app.GUI.FloorSubsystemGUI;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

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
import app.ElevatorSubsystem.Direction.Direction;
import app.ElevatorSubsystem.Elevator.*;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;
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
	private JPanel topPanel, bottomPanel, floorPanel, e1Panel, e2Panel, e3Panel, e4Panel, colourKeyPanel;
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
	
	private JTextArea elevator1Text,elevator2Text,elevator3Text,elevator4Text;
	
	
	/**
	 * Constructor for GUI class
	 * @param numElevators - number of elevators in system
	 */
	public GUI(Config config) {
		frame = new JFrame();
		frame.setTitle("Elevator Information");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(750,750);
		frame.setLayout(new GridLayout(2, 1));
		
		this.config = config;
		int numElevators = config.getInt("elevator.total.number");

		
		this.elevator1Info = new ElevatorInfo(1, 1, -1, ElevatorStateMachine.Idle, Direction.UP);
		this.elevator2Info= new ElevatorInfo(1, 1, -1, ElevatorStateMachine.Idle, Direction.UP) ;
		this.elevator3Info= new ElevatorInfo(1, 1, -1, ElevatorStateMachine.Idle, Direction.UP) ;
		this.elevator4Info= new ElevatorInfo(1, 1, -1, ElevatorStateMachine.Idle, Direction.UP) ;

		
		frame.setVisible(true);

		
        this.floorPanel= new JPanel();
        this.floorPanel.add(new FloorSubsystemGUI(new TreeSet<>(), new TreeSet<>()));
        
        this.e1Panel= new JPanel();
        this.e1Panel.add(new ElevatorSubsystemGUI(elevator1Info, new TreeSet<>()));
        this.e1Panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Elevator 1", TitledBorder.CENTER, TitledBorder.TOP));
        
        this.e2Panel= new JPanel();
        this.e2Panel.add(new ElevatorSubsystemGUI(elevator2Info, new TreeSet<>()));
        this.e2Panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Elevator 2", TitledBorder.CENTER, TitledBorder.TOP));

        
        this.e3Panel= new JPanel();
        this.e3Panel.add(new ElevatorSubsystemGUI(elevator3Info, new TreeSet<>()));
        this.e3Panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Elevator 3", TitledBorder.CENTER, TitledBorder.TOP));

        
        this.e4Panel= new JPanel();
        this.e4Panel.add(new ElevatorSubsystemGUI(elevator4Info, new TreeSet<>()));
        this.e4Panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Elevator 4", TitledBorder.CENTER, TitledBorder.TOP));

		
		
        this.topPanel = new JPanel();
        topPanel.add(this.floorPanel);
        topPanel.add(this.e1Panel);
        topPanel.add(this.e2Panel);
        topPanel.add(this.e3Panel);
        topPanel.add(this.e4Panel);
        frame.add(topPanel);
		
        
        elevator1Text = new JTextArea();
        elevator2Text = new JTextArea();
        elevator3Text = new JTextArea();
        elevator4Text = new JTextArea();
        
        this.bottomPanel = new JPanel();
        bottomPanel.add(this.elevator1Text);
        bottomPanel.add(this.elevator2Text);
        bottomPanel.add(this.elevator3Text);
        bottomPanel.add(this.elevator4Text);
        frame.add(bottomPanel);
        
        frame.pack();

	}
	
	
	public void log(String msg) {
		System.out.println("[GUI_PacketReceiver]" + msg);
	}
	
	/**
	 * Updates the information displayed when changes occur
	 */
	public void updateView(GUIUpdateInfo guiUpdateInfo) {
		
		HashMap<Integer, ElevatorInfo> elevatorInfos = guiUpdateInfo.getAllElevatorInfoObject();
		if(elevatorInfos != null) {
			for(Integer id : elevatorInfos.keySet()) {
				if(id == 1) {
					this.elevator1Info = elevatorInfos.get(id);
				}else if(id == 2) {
					this.elevator2Info = elevatorInfos.get(id);
				}else if(id == 3) {
					this.elevator3Info = elevatorInfos.get(id);
				}else if(id == 4) {
					this.elevator4Info = elevatorInfos.get(id);
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
        this.e1Panel.add(new ElevatorSubsystemGUI(this.elevator1Info, elevator1Destinations));
        
        this.e2Panel.removeAll();
        this.e2Panel.add(new ElevatorSubsystemGUI(this.elevator2Info, elevator2Destinations));
        
        this.e3Panel.removeAll();
        this.e3Panel.add(new ElevatorSubsystemGUI(this.elevator3Info, elevator3Destinations));
        
        this.e4Panel.removeAll();
        this.e4Panel.add(new ElevatorSubsystemGUI(this.elevator4Info, elevator4Destinations));
        
        
        elevator1Text.setText(elevator1Info.toString() + "\nPressed Elevator Buttons : " + elevator1Destinations);
        elevator2Text.setText(elevator2Info.toString() + "\nPressed Elevator Buttons : " + elevator2Destinations);
        elevator3Text.setText(elevator3Info.toString() + "\nPressed Elevator Buttons : " + elevator3Destinations);
        elevator4Text.setText(elevator4Info.toString() + "\nPressed Elevator Buttons : " + elevator4Destinations);
        
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
