# 3303ElevatorProject
Group project for SYSC 3303 : Real-Time Concurrent Systems
Code Complete Final Version : April 12, 2022 - No anticipated changes after submission

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
		II) GUI
		III) Scheduler
		IV) FloorSubsystem
		V) ElevatorSubsystem

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
	-Provides command line UI options to choose an input file and dynamically add requests
     	-ScheduledElevatorRequest.java -> contains the elevator request details to be sent to the scheduler from floor subsystem
      	-TextFileReader.java -> Reads an input file to create elevator requests event objects
		-emptyinputfile.txt -> Empty input file
		-inputfile.txt -> Default input file

	-Floor Subsystem Threads
		- FloorSubsystem_SchedulerPacketReceiver.java -> [WORK IN PROGRESS - Redesign possibly needed] to receive updates about the system. Planned for GUI operations
    
    -GUI
    	-ElevatorSubsystemGUI.java-> Elevator Subsystem GUI class that will create and update the table to be added to the main frame to display Elevator subsystem information 
	-FloorSubsystemGUI.java->Floor Subsystem GUI class that will create and update the table to be added to the main frame to display floor subsytem information
	-GUI_PacketReceiver.java-> Class for receiving one way UDP Communication for the GUI system
	-GUI.java-> GUI class to display each elevator status and make new requests
	-GUIUpdateInfo.java -> Communication object sent by scheduler and elevator subsystems to be handled by the GUI to update the view 
    -Scheduler:
		-ElevatorSpecificScheduler.java -> To track the scheduling of a particular elevator
		-ElevatorSpecificSchedulerState.java -> Enum for the state of a ElevatorSpecificScheduler
		-ElevatorSpecificSchedulerManager.java -> To distribute requests to elevators along an algorithm. 
		-ElevatorSpecificSchedulerManagerState.java -> Enum for the state of an ElevatorSpecificSchedulerManager
      	-Scheduler.java -> Coordinates requests from floor subsystem to movements in the elevator subsystem
      	-TimeManagementSystem.java -> Generator for delay times for elevator movements and elevator loading

	-Scheduler Threads
      	-DelayedRequest.java -> Delays the execution of requests to match request time
		-Scheduler_ElevatorSubsystemPacketReceiver.java -> For receiving packets from the elevator subsystem to update known elevator positions
		-Scheduler_FloorSubsystemPacketReceiver.java -> For receiving packets from the floor subsystem to make new floor requests
		-TemporaryErrorSelfRevive.java -> For getting elevators out of the temporary error state after a specified period of time

	-UDP
		-PacketReceiver.java -> Abstract class that offers base functionality for packet communications. Can be run with an implementation of the packet handling and response generation
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
	-Time Management System
		-Testing
		-Update methods to work with ElevatorSubsystem
	-ServerLogger
		-Create server logger class
	-Logger
		-Update methods to call on server logger
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
		-Testing packetreceiver on floorSubsystem 
		-requestType updates 
		-new requestType handling in cmd UI
	-ScheduledElevatorRequset
		-requestType updates
		-tests updated
	-TextFileReader
		-reads requestType from txt file 
	
	-Server Logger
		-writes logger output to new text file to keep track 
	-UML Class Diagram
		-updated 

-Millan Wang:
	-Scheduler Areas
		-Planning, design, Redesign
		-Addressing edge cases
		-Implementation
		-Temporary error self revive process
		-Testing
		-State machine diagrams
		-Algorithm flow charts
	-MainProgramRunner
		-Testing
		-State machine diagram
	-UDP Comms 
		-Designed and implemented abstract PacketReceiver class
	-UML Class Diagram
	-UML Sequence Diagram
	-Timing diagrams
	-Demo video editing and recording
	-Integration Testing


