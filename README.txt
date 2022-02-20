# 3303ElevatorProject
Group project for SYSC 3303 : Real-Time Concurrent Systems
Iteration 2 Due : February 19th @ 23:59 EST


## Project Planning Word Document
https://cmailcarletonca-my.sharepoint.com/:w:/g/personal/benkittilsen_cmail_carleton_ca/EVCLIIHlio1Pg7XQRifEO-oBd3obcuQRZk8ShG26z0HKVA?e=eySmeT

##Group Members:
Abdelrahim Karaja (101187105), AbdelrahimKaraja@cmail.carleton.ca
Ben Kittilsen (101101290), benkittilsen@cmail.carleton.ca 
Peter Tanyous (101127203), petertanyous@cmail.carleton.ca 
Millan Wang (101114457), millanwang@cmail.carleton.ca 


Source Code File Breakdown:
    -App
	-MainProgramRunner.java -> main(String[] args) to run the program
			-Provides command line UI options to choose an input file and dynamically add requests
	-Logger.java -> Logs the occurance of events in the program in a single object. Eventually will be used for performance tracking 

    -Elevator Subsystem: 
	-ElevatorSubsystem.java -> Handles interaction between scheduler and elevator

      	-Direction.java -> Enum which handles elevator movement info for output

      	-Elevator.java -> Contains the state of elevator and its movement
      	-ElevatorButton.java -> (Non-Functional Requirement for IT2) Will be used as GUI indicators in future iterations
    	-ElevatorDoor.java -> Contains a TimeManagementSystem and handles events/delays when elevator is loading/offloading
	-ElevatorLamp.java -> (Non-Functional Requirement for IT2) Will be used as GUI indicators in future iterations
	
	-ElevatorStateMachine -> State machine enum representing the execution process of an elevator within the subsystem

      
    -Floor Subsystem:
      	-FloorSubsystem.java -> Makes requests to and recieves requests from scheduler as subsytem will act as multiple floors
     	-ScheduledElevatorRequest.java -> contains the elevator request details to be sent to the scheduler from floor subsystem
      	-TextFileReader.java -> Reads an input file to create elevator requests event objects
	-emptyinputfile.txt -> Empty input file
	-inputfile.txt -> Default input file
      
    -Scheduler:
      	-DelayedRequest.java -> Delays the execution of requests to match request time
      	-Scheduler.java -> Coordinates requests from floor subsystem to movements in the elevator subsystem
      	-TimeManagementSystem.java -> Generator for delay times for elevator movements and elevator loading
  
    -Test files:
      	[
	  -ElevatorSubsystemTests.java, 
	  -ElevatorTests.java,
	  -ElevatorStateMachineTests.java,
	  -FloorSubsystemTests.java,
	  -LoggerTest.java,
	  -ScheduledElevatorRequestTest.java
	  -SchedulerTests.java, 
	  -TimeManagementTest.java 
	]-> Junit test classes for each class respective to the name



Roles and Division of Responsibilities for iteration 2:
    -Abdelrahim Karaja: 
	-Time Management System
		-Testing
		-Statistical Analysis
	-UML Class Diagram

    -Ben Kittilsen: 
	-Elevator Subsystem 
		-Testing
		-State machine diagram
	-UML Class Diagram

    -Peter Tanyous: 
	-Floor Subsystem 
		-Testing
	-Logger
		-Testing
	-UML Class Diagram

    -Millan Wang:
	-Scheduler 
		-Testing
		-State machine diagram
	-MainProgramRunner
		-Testing
		-State machine diagram
	-UML Class Diagram
	-UML Sequence Diagram

