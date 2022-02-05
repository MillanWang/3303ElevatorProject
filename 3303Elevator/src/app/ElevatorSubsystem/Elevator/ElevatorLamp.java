package app.ElevatorSubsystem.Elevator;

public class ElevatorLamp {

	private Lamp status;

	public ElevatorLamp(){
		this.status = Lamp.OFF;
	}

	public Lamp getElevatorLamp(){
		return this.status;
	}

	public void setElevatorLamp(Lamp status){
		this.status = status;
	}
}
