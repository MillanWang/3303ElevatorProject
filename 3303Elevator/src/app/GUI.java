package app;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.GridLayout;
import java.util.ArrayList;
import app.ElevatorSubsystem.Elevator.*;

/**
 * GUI class to display each elevator status and make new requests
 * @author Abdelrahim Karaja
 *
 */
public class GUI{
	private ArrayList<ElevatorInfo> elevatorInfo;
	private ArrayList<JPanel> panels;
	private ArrayList<Elevator> elevators;
	private JFrame frame;
	private JPanel panel;
	
	/**
	 * Constructor for GUI class
	 * @param numElevators - number of elevators in system
	 */
	public GUI(int numElevators) {
		frame = new JFrame();
		frame.setTitle("Elevator Information");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(750,750);
		
		panel = new JPanel(new GridLayout((numElevators-numElevators % 4)/4, 4, 10, 10));
		elevatorInfo = new ArrayList<ElevatorInfo>();
		elevators = new ArrayList<Elevator>();
		panels = new ArrayList<JPanel>();
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
		updateView();
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
	public void updateView() {
	}
	
	
}
