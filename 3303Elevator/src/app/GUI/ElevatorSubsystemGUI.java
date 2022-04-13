/**
 * 
 */
package app.GUI;
import app.ElevatorSubsystem.Elevator.ElevatorInfo;
import app.ElevatorSubsystem.StateMachine.ElevatorStateMachine;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * @author Abdelrahim Karaja
 *Elevator Subsystem GUI class that will create and update the table to be added to the main frame to display Elevator subsystem information
 */

public class ElevatorSubsystemGUI extends JPanel{
	private String name;
	private JTable table;
	private int[] floors = new int[] {22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
	private String[] columns = {"Current Floor", "Elevator Buttons"};
	private String[][] data = {{"22", "22"}, {"21", "21"}, {"20", "20"}, {"19", "19"}, {"18", "18"}, {"17", "17"}, {"16", "16"}, {"15", "15"}, {"14", "14"}, {"13", "13"}, {"12", "12"}, {"11", "11"}, {"10", "10"}, {"9", "9"}, {"7", "7"}, {"6", "6"}, {"5", "5"}, {"4", "4"}, {"3", "3"}, {"2", "2"}, {"1", "1"}};
	private ElevatorInfo elevatorInfo;
	private TreeSet<Integer> destinations;
	private boolean isPermanentlyDown;
	
	/**
	 * Constructor to initialize the FSS GUI panel
	 */
	public ElevatorSubsystemGUI(ElevatorInfo EI, TreeSet<Integer> dests) {
		elevatorInfo = EI;
		destinations = dests;
		isPermanentlyDown = false;
		
		//Elevator Table
		table = new JTable(data, columns){
			public boolean isCellEditable(int data, int columns) {
				return false;
			}
			
			public Component prepareRenderer(TableCellRenderer r, int data, int columns) {
				Component c = super.prepareRenderer(r, data, columns);
				
				
				if (columns == 0) {
					//Setting Elevator Location Colours
					if (elevatorInfo.getFloor() == floors[data+1]) { //If elevator is on current floor
						if(elevatorInfo.getError() == -2) { //Temp Error
							c.setBackground(Color.YELLOW);
						}
						else if(elevatorInfo.getError() == -3 || isPermanentlyDown) { //Permanent Error
							c.setBackground(Color.RED);
							isPermanentlyDown = true;
						}
						else if(elevatorInfo.getState() == ElevatorStateMachine.Idle && elevatorInfo.getError() != -3 && elevatorInfo.getError() != -2) { //Idle
							c.setBackground(Color.PINK);
						}
						else if(elevatorInfo.getState() == ElevatorStateMachine.MoveUp || elevatorInfo.getState() == ElevatorStateMachine.MoveDown) { //Moving
							c.setBackground(Color.DARK_GRAY);
						}
						else if(elevatorInfo.getState() == ElevatorStateMachine.DoorOpening) { //Doors Opening
							c.setBackground(Color.CYAN);//new Color(50,205,50));
						}
						else if(elevatorInfo.getState() == ElevatorStateMachine.OpenDoor) { //Doors Open
							c.setBackground(Color.MAGENTA);//new Color(34,139,34));
						}
						else if(elevatorInfo.getState() == ElevatorStateMachine.DoorClosing) { //Doors Closing
							c.setBackground(Color.ORANGE);
						}
					} else {
						c.setBackground(Color.LIGHT_GRAY);
					}
					
				} else if (columns == 1) {
					//Setting Elevator Button Colours
					if (destinations.contains(floors[data+1])) {
						c.setBackground(Color.GREEN);
					}
					else {
						c.setBackground(Color.WHITE);
					}
					
				}
				
//				//Setting Elevator Location Colours
//				if (elevatorInfo.getFloor() == floors[data+1] && columns == 0) { //If elevator is on current floor
//					if(elevatorInfo.getState() == ElevatorStateMachine.Idle) { //Idle
//						c.setBackground(Color.LIGHT_GRAY);
//					}
//					else if(elevatorInfo.getState() == ElevatorStateMachine.MoveUp || elevatorInfo.getState() == ElevatorStateMachine.MoveDown) { //Moving
//						c.setBackground(Color.DARK_GRAY);
//					}
//					else if(elevatorInfo.getState() == ElevatorStateMachine.DoorOpening) { //Doors Opening
//						c.setBackground(new Color(50,205,50));
//					}
//					else if(elevatorInfo.getState() == ElevatorStateMachine.OpenDoor) { //Doors Open
//						c.setBackground(new Color(34,139,34));
//					}
//					else if(elevatorInfo.getState() == ElevatorStateMachine.DoorClosing) { //Doors Closing
//						c.setBackground(Color.ORANGE);
//					}
//					else if(elevatorInfo.getError() == 1) { //Temp Error
//						c.setBackground(Color.YELLOW);
//					}
//					else if(elevatorInfo.getError() == 2) { //Permanent Error
//						c.setBackground(Color.RED);
//					}
//				} else {
//					c.setBackground(Color.WHITE);
//				}
				
//				//Setting Elevator Button Colours
//				if (destinations.contains(floors[data+1]) && columns == 1) {
//					c.setBackground(Color.GREEN);
//				}
//				else {
//					c.setBackground(Color.WHITE);
//				}
				
				return c;
			}
		};
		table.setPreferredScrollableViewportSize(new Dimension(300,340));
		table.setFillsViewportHeight(true);
		
		JScrollPane jps = new JScrollPane(table);
		add(jps);
		//this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Elevator " + elevatorInfo.getId(), TitledBorder.CENTER, TitledBorder.TOP));
	}
	
	/**
	 * Main method for testing this component individually
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("Elevator Subsystem GUI");
		frame.setSize(350,500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
