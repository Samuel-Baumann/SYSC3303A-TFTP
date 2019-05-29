# Summer 2019 SYSC3303A: Trivial File Transfer Protocol Project
  A file transfer system based on the TFTP specification (RFC 1350). The TFTP program will contain three programs: Client, Error Simulation, and Server. The goal of the project is to be able to run all of the programs separately and transfer files from or to Client and Server. 
 
## Members
  Sirak Berhane   - 101030433
  
  Samuel Baumann  - 101033635
  
  Ruchi Bhatia    - 100970682
 
## Iteration 1 - Completed On: Tuesday, May 21, 2019
Note: This iteration also includes Iteration 0.
### Deliverables:
* README (this document)
* UML Class Diagram
* State Machine Diagrams (Client, Host/Err. Simulator, Server)
* Use Case Document
* Code for each component + supplementary classes & constants
### Breakdown:
* Code - Sirak (adapted from Sirak's Assignment 1 and further expanded to include file transfer + helper classes)
* Supporting Documentation, UML and Use Case: Sam
* State Machine diagrams and added verbose/quit user input code to Client: Ruchi
### Diagram:
![alt text](https://github.com/sirakberhane/SYSC3303A-TFTP/blob/master/Diagrams/Class%20Diagram%20-%20Iteration%20%231.png)
### Setup:
To run our first iteration, a user must have an instance of the client and server on the same machine, and must also run the connection handler and error simulator if they wish to run tests. Once all of these consoles are open and the threads are running, follow the steps outlined in the Client's main() function to select your mode, file locations and other customizable options. To quit, again follow the instruction in Client to send a shutdown command to the system.
