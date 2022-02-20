package app.ElevatorSubsystem.Elevator;


/**
 * WORK IN PROGRESS - For eventual use in the GUI to indicate future floors that
 * an elevator will visit. Also to log info for when destinations get set
 * 
 * @author Millan Wang
 *
 */
public class ElevatorButton {

	private boolean isPressed;

	public ElevatorButton(){
		this.isPressed = false;
	}

	public boolean getIsPressed(){
		return this.isPressed;
	}

	public void setIsPressed(boolean isPressed){
		this.isPressed = isPressed;
	}
}
