# Summer 2019 SYSC3303A: Trivial File Transfer Protocol Project
  A file transfer system based on the TFTP specification (RFC 1350). The TFTP program will contain three programs: Client, Error Simulation, and Server. The goal of the project is to be able to run all of the programs separately and transfer files from or to Client and Server. 
 
## Members
  Sirak Berhane   - 101030433
  
  Samuel Baumann  - 101033635
  
  Ruchi Bhatia    - 100970682
  
  Henri Umba      - 101022562
  
  Gen Li          - 100967203
 
## Iteration 1 - Submitted On: Tuesday, May 21, 2019
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

## Iteration 2 - Submitted On: Tuesday, May 28, 2019
Note: Group 5 has merged its remaining 3 members with Group 9's remaining 2 members
### Deliverables:
* README (this document)
* UML Class Diagram
* State Machine Diagrams (Client, Host/Err. Simulator, Server)
* Use Case Document
* UML Sequence Diagram
* Updated Code for each component + supplementary classes & constants
### Breakdown:
* Code - Sirak, Henri and Ruchi (modified with group 9's iteration 1 + addition of testing in Error simulator)
* Supporting Documentation, UML Class and Use Case: Sam
* UML Sequence and State Machine Diagrams: Gen
### Setup:
Running the second iteration is similar to the first in that a user must have an instance of the client, error simulator and server open on the same machine, and run the connection handler to log testing, a crucial part of iteration 2. In this case however, though the client is still responsible for accepting user input to select mode, file locations and other options, the Error simluator can be customized to intentionall delay, duplicate or lose packets in order to record how the server behaves when presented with irregular messages.
### Known issues:
At present, iteration 2 contains bugs that prevent the project from functioning fully. First, when the user sends a request from the client, the server will acknowledge and generate a response as normal, but the server will resend the request upon receiving the response, looping until manually shut down. In addition, because of this error, the project must be terminated from the console rather than from a shutdown command. These issues have delayed the implementation of error testing and simulation, and are being reveiewed.
