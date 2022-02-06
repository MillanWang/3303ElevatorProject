# 3303ElevatorProject
Group project for SYSC 3303 : Real-Time Concurrent Systems
Iteration 0&1 Due : February 5th @ 11:59 PM


## Project Planning Word Document
https://cmailcarletonca-my.sharepoint.com/:w:/g/personal/benkittilsen_cmail_carleton_ca/EVCLIIHlio1Pg7XQRifEO-oBd3obcuQRZk8ShG26z0HKVA?e=eySmeT

Group Members:
Ben Kittilsen (101101290), benkittilsen@cmail.carleton.ca 

Peter Tanyous (101127203), petertanyous@cmail.carleton.ca 

Millan Wang (101114457), millanwang@cmail.carleton.ca 

Abdelrahim Karaja (101187105), AbdelrahimKaraja@cmail.carleton.ca

Files:
  I) Elevator Subsystem: Handles interaction between scheduler and elevator
      -Elevator.java -> Contains the state of elevator and its movement
      -ElevatorButton.java -> (Non-Functional Requirement for IT1) Will be used as GUI indicators in future iterations
      -ElevatorDoor.java -> Contains a tms and handles events when elevator is loading/offloading
      -ElevatorLamp.java -> (Non-Functional Requirement for IT1) Will be used as GUI indicators in future iterations
      -Lamp.java -> Enum for lamp status
      -Movement.java -> Enum which handles elevator movement info for output
      
  II) Floor Subsystem:
      -FloorSubsystem.java -> Makes requests to and recieves requests from scheduler as subsytem will act as multiple floors
      -Input.java -> contains the elevator request details to be sent to the scheduler from floor subsystem
      -TextFileReader.java -> Reads input file which contains elevator requests
      
  III) Scheduler:
      -DelayedRequest.java -> Delays the execution of requests to match elevator loc
      -Scheduler.java -> Coordinates requests from floor subsystem to elevator subsystem
      -TimeManagementSystem.java -> Random time generator for elevator movement and elevator door status
  
  IV) Test files:
      -ElevatorSubsystemTests, ElevatorTests, FloorSubsystemTests, SchedulerTests, TimeManagementTests -> Junit test classes for each class respective to the name

Role Division for iteration 1:
    -Ben Kittilsen: Elevator Subsystem + testing + UML
    -Peter Tanyous: Floor Subsystem + testing + UML
    -Millan Wang: Scheduler + User Interface + testing + UML
    -Abdelrahim Karaja: (Statistical Analysis) Time Management System + testing + read.me + UML
