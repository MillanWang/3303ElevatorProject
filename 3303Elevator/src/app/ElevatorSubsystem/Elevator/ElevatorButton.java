package app.ElevatorSubsystem.Elevator;

public class ElevatorButton {

	private boolean status;

	public ElevatorButton(){
		this.status = false;
	}

	public boolean getStatus(){
		return this.status;
	}

	public void press(){
		this.status = true;
	}
	
	public void turnOff() {
		this.status = false;
	}
}
