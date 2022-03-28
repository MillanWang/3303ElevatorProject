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

	-Config
		-Config.java -> Handles reading properties file config varaibles
		-local.properties -> properties for running locally
		-multi.properties -> properties for running on multiple computers (Set up to us a vpn connection that only Members from this group have permision to use)
		-test.properties -> properties for runnning the test cases

    -Elevator Subsystem:
		-ElevatorNextFloorBuffer.java -> Used to send next floor requests to elevators from ElevatorSubsystem
		-ElevatorStatusBuffer.java -> Used to send elevator status from elevators to ElevatorSubsystem
		-ElevatorSubsystem_SchedulerPacketReceiver.java -> Used to receive request from schedule and send elevators status after elevators move
		-ElevatorSubsystem.java -> Handles interaction between scheduler and elevator

	-Direction
      	-Direction.java -> Enum which handles elevator movement info for output

	-Elevator
      	-Elevator.java -> Contains the state of elevator and its movement
      	-ElevatorButton.java -> (Non-Functional Requirement for IT2) Will be used as GUI indicators in future iterations
    	-ElevatorDoor.java -> Contains a TimeManagementSystem and handles events/delays when elevator is loading/offloading
		-ElevatorInfo.java -> used to store an elevators state at a point in time before sending to schedule
		-ElevatorLamp.java -> (Non-Functional Requirement for IT2) Will be used as GUI indicators in future iterations

	-Elevator State machine
		-ElevatorState -> holds the elevators state machine enum controlling the changes
		-ElevatorStateMachine -> State machine enum representing the execution process of an elevator within the subsystem

    -Floor Subsystem:
      	-FloorSubsystem.java -> Makes requests to and recieves requests from scheduler as subsytem will act as multiple floors and operates command line UI to send new requests
	 to scheduler
     	-ScheduledElevatorRequest.java -> contains the elevator request details to be sent to the scheduler from floor subsystem
      	-TextFileReader.java -> Reads an input file to create elevator requests event objects
		-emptyinputfile.txt -> Empty input file
		-inputfile.txt -> Default input file

	-Floor Subsystem Threads
		- FloorSubsystem_SchedulerPacketREceiver.java -> Millan

    -Scheduler:
		-ElevatorSpecificScheduler.java -> Millan
		-ElevatorSpecificSchedulerManager.java -> Millan
		-ElevatorSpecificSchedulerManagerState.java -> Millan
      	-Scheduler.java -> Coordinates requests from floor subsystem to movements in the elevator subsystem
      	-TimeManagementSystem.java -> Generator for delay times for elevator movements and elevator loading

	-Scheduler Threads
      	-DelayedRequest.java -> Delays the execution of requests to match request time
		-Scheduler_ElevatorSubsystemPacketReceiver.java -> Millan
		-Scheduler_FloorSubsystemPacketReceiver.java -> Millan

	-UDP
		-PacketReceiver.java -> Millan
		-Util.java -> Common udp util functionality that was made static to the project

    -Test files:
      	[
	  -ConfigTests.java
	  -ElevatorSubsystemTests.java,
	  -ElevatorTests.java,
	  -ElevatorStateMachineTests.java,
	  -FloorSubsystemTests.java,
	  -LoggerTest.java,
	  -ScheduledElevatorRequestTest.java
	  -ElevatorSpecificSchedulerManagerTests.java
	  -ElevatorSpecificSchedulerTests.java
	  -SchedulerTests.java,
	  -TimeManagementTest.java
	]-> Junit test classes for each class respective to the name



Roles and Division of Responsibilities for iteration 4:
*Refer to git history for a more detailed breakdown of code contributions*

    -Abdelrahim Karaja:
	-GUI
		-Added blueprint for GUI class to display elevator information - WIP**
	-UML Class Diagram
	-Debugging/Testing of code

    -Ben Kittilsen:
	-Elevator Subsystem
		-Receiving next floor packets from scheduler
		-Sending elevator status packets
		-Thread coms with elevators
		-Testing
		-State machine diagram
	-Elevator
		-Thread coms with elevator subsytem
		-Error Handling
	-Config
	-Integration Testing
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

