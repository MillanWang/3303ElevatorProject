package app.GUI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeSet;

import app.ElevatorSubsystem.Elevator.ElevatorInfo;

public class GUIUpdateInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private HashMap<Integer, ElevatorInfo> allElevatorInfo;
	private HashMap<Integer, TreeSet<Integer>> allElevatorDestinations;
	private TreeSet<Integer> allUpwardsFloorButtons;
	private TreeSet<Integer> allDownwardsFloorButtons;
	
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
	
	public HashMap<Integer, ElevatorInfo> getAllElevatorInfoObject() {
		return this.allElevatorInfo;
	}
	
	public HashMap<Integer, TreeSet<Integer>> getAllElevatorDestinations() {
		return this.allElevatorDestinations;
	}
	
	public TreeSet<Integer> getAllUpwardsFloorButtons() {
		return this.allUpwardsFloorButtons;
	}
	
	public TreeSet<Integer>getAllDownwardsFloorButton() {
		return this.allDownwardsFloorButtons;
	}
	
	
}
