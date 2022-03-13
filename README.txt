# 3303ElevatorProject
Group project for SYSC 3303 : Real-Time Concurrent Systems
Iteration 3 Due : March 12th @ 23:59 EST

##SETUP INSTRUCTIONS

-Running program in Eclipse
	TO RUN LOCALLY:
	1.Load the project in eclipse, run the main method in MainProgramRunner.java
	2.You will be prompted if you want to choose an input file.
		-NOTE : When running the program in eclipse, the program will seem stuck but the
			file explorer is actually appearing behind the eclipse window. Alt+Tab to find it
		-Choose yes if you have a properly formatted input file to use
		-Choose no and you will use a default input file
		-Non existant files go to the default input file
		-Files with formatting issues will throw an exception, but will still run with no pre-scheduled requests
	3.You will then encounter the command line UI where you will be able to use the n option to schedule new requests or quit

	TO RUN WITH UDP COMMUNICATION:
	-Load project and start in following order:
		I) ServerLogger
		II) Scheduler
		III) FloorSubsystem
		IV) ElevatorSubsystem

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
	-Logger.java -> Logs the occurance of events in the program in a single object locally. Eventually will be used for performance tracking
	-ServerLogger.java -> Logs events accross all systems into one location through communication with Logger

    -Elevator Subsystem:
	-ElevatorSubsystem.java -> Handles interaction between scheduler and elevator

      	-Direction.java -> Enum which handles elevator movement info for output

      	-Elevator.java -> Contains the state of elevator and its movement
      	-ElevatorButton.java -> (Non-Functional Requirement for IT2) Will be used as GUI indicators in future iterations
    	-ElevatorDoor.java -> Contains a TimeManagementSystem and handles events/delays when elevator is loading/offloading
	-ElevatorLamp.java -> (Non-Functional Requirement for IT2) Will be used as GUI indicators in future iterations

	-ElevatorStateMachine -> State machine enum representing the execution process of an elevator within the subsystem


    -Floor Subsystem:
      	-FloorSubsystem.java -> Makes requests to and recieves requests from scheduler as subsytem will act as multiple floors and operates command line UI to send new requests 
	 to scheduler
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



Roles and Division of Responsibilities for iteration 3:
*Refer to git history for a more detailed breakdown of code contributions*

    -Abdelrahim Karaja:
	-Time Management System
		-Testing
		-Update methods to work with ElevatorSubsystem
	-ServerLogger
		-Create server logger class
	-Logger
		-Update methods to call on server logger
	-UML Class Diagram

    -Ben Kittilsen:
	-Elevator Subsystem
		-Testing
		-State machine diagram
	-Config
	-VPN Configuration
	-UML Class Diagram

    -Peter Tanyous:
	-Floor Subsystem
		-Testing
		-commandline UI new handler
		-floorSubsystem sequence diagram
	-SchedulerPacketReceiver
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

