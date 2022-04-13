/**
 * 
 */
package app.GUI;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * @author Abdelrahim Karaja
 *Floor Subsystem GUI class that will create and update the table to be added to the main frame to display floor subsytem information
 */

public class FloorSubsystemGUI extends JPanel{
	private String name;
	private JTable table;
	private int[] floors = new int[] {22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
	private String[] columns = {"Upwards Floor Buttons", "Downwards Floor Buttons"};
	private String[][] data = {{"22", "22"}, {"21", "21"}, {"20", "20"}, {"19", "19"}, {"18", "18"}, {"17", "17"}, {"16", "16"}, {"15", "15"}, {"14", "14"}, {"13", "13"}, {"12", "12"}, {"11", "11"}, {"10", "10"}, {"9", "9"}, {"7", "7"}, {"6", "6"}, {"5", "5"}, {"4", "4"}, {"3", "3"}, {"2", "2"}, {"1", "1"}};
	private TreeSet<Integer> allUpwardsFloorButtons;
	private TreeSet<Integer> allDownwardsFloorButtons;
	
	/**
	 * Constructor to initialize the FSS GUI panel
	 */
	public FloorSubsystemGUI(TreeSet<Integer> upwardsFloorButtons, TreeSet<Integer> downwardsFloorButtons) {
		allUpwardsFloorButtons = upwardsFloorButtons;
		allDownwardsFloorButtons = downwardsFloorButtons;
		
		table = new JTable(data, columns) {
			public boolean isCellEditable(int data, int columns) {
				return false;
			}
			
			public Component prepareRenderer(TableCellRenderer r, int data, int columns) {
				Component c = super.prepareRenderer(r, data, columns);
				if (allUpwardsFloorButtons.contains(floors[data+1]) && columns == 0 || allDownwardsFloorButtons.contains(floors[data+1]) && columns == 1 ) {
					c.setBackground(Color.GREEN);
				}
				else {
					c.setBackground(Color.WHITE);
				}
				
				return c;
			}
		};
		
		table.setPreferredScrollableViewportSize(new Dimension(300,340));
		table.setFillsViewportHeight(true);
		
		JScrollPane jps = new JScrollPane(table);
		add(jps);
	}
	
	/**
	 * Main method for testing
	 * @param args
	 */
	public static void main(String[] args) {
		//Creating treesets TEST
		TreeSet<Integer> up = new TreeSet<>();
		up.add(1);
		up.add(5);
		up.add(6);
		
		TreeSet<Integer> down = new TreeSet<>();
		down.add(3);
		down.add(15);
		
		JFrame frame = new JFrame();
		FloorSubsystemGUI fssGUI = new FloorSubsystemGUI(up, down);
		frame.setTitle("Floor Subsystem GUI");
		frame.setSize(350,500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(fssGUI);
	}
}
