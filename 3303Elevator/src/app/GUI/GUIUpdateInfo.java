package app.GUI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeSet;

import app.ElevatorSubsystem.Elevator.ElevatorInfo;

public class GUIUpdateInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * System properties of interest to show on GUI
	 */
	private HashMap<Integer, ElevatorInfo> allElevatorInfo;
	private HashMap<Integer, TreeSet<Integer>> allElevatorDestinations;
	private TreeSet<Integer> allUpwardsFloorButtons;
	private TreeSet<Integer> allDownwardsFloorButtons;
	
	/**
	 * Constructor for GUIUpdateInfo
	 * @param allElevatorInfo mapping of elevatorID : ElevatorInfo
	 * @param allElevatorDestinations Mapping of elevatorID : current destinations
	 * @param allUpwardsFloorButtons Set of all upwards pressed floor buttons
	 * @param allDownwardsFloorButtons set of all downwards pressed floor buttons
	 */
	public GUIUpdateInfo(
				HashMap<Integer, ElevatorInfo> allElevatorInfo,
				HashMap<Integer, TreeSet<Integer>> allElevatorDestinations,
				TreeSet<Integer> allUpwardsFloorButtons,
				TreeSet<Integer> allDownwardsFloorButtons
			) {
		
		this.allElevatorInfo = allElevatorInfo;
		this.allElevatorDestinations = allElevatorDestinations;
		this.allUpwardsFloorButtons = allUpwardsFloorButtons;
		this.allDownwardsFloorButtons = allDownwardsFloorButtons;
	}
	
	/**
	 * Returns map of ElevatorInfo objects
	 * @return map of ElevatorInfo objects
	 */
	public HashMap<Integer, ElevatorInfo> getAllElevatorInfoObject() {
		return this.allElevatorInfo;
	}
	
	/**
	 * Returns map of current elevator destinations
	 * @return map of current elevator destinations
	 */
	public HashMap<Integer, TreeSet<Integer>> getAllElevatorDestinations() {
		return this.allElevatorDestinations;
	}
	
	/**
	 * Returns all upwards pressed floor buttons
	 * @return all upwards pressed floor buttons
	 */
	public TreeSet<Integer> getAllUpwardsFloorButtons() {
		return this.allUpwardsFloorButtons;
	}
	
	/**
	 * Returns all downwards pressed floor buttons
	 * @return all downwards pressed floor buttons
	 */
	public TreeSet<Integer>getAllDownwardsFloorButton() {
		return this.allDownwardsFloorButtons;
	}
}
