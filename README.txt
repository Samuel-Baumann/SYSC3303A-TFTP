Summer 2019 SYSC3303A: Trivial File Transfer Protocol Project
 
  A file transfer system based on the TFTP specification (RFC 1350). 
The TFTP program will contain three programs: Client, Error Simulation, 
and Server. The goal of the project is to be able to run all of the 
programs separately and transfer files from or to Client and Server. 

Members
  Sirak Berhane - 101030433

  Samuel Baumann - 101033635

  Ruchi Bhatia - 100970682

  Henri Umba - 101022562

  Gen Li - 100967203
 
Iteration 3 - Completed On: Tuesday, June 4, 2019

Deliverables:
* README (this document)
* UML Class Diagram
* Sequence Diagrams (Client, Host/Err. Simulator, Server)
* State Machine Diagrams (Client, Host/Err. Simulator, Server)
* Use Case Document
* Code for each component + supplementary classes & constants

Breakdown:
* Code - Sirak, Henri and Sam (Client/Server by Sirak & Henri, Error Simulator Updates by all 3)
* Supporting Documentation, Use Cases and State Machine Updates - Sam
* Updated UML Class and Squence Diagrams - Gen

Setup:
To run, a user must have an instance of the client and server on 
the same machine, and must also run the error simulator if they wish to run tests. 

Make sure that you decide which mode you want the error simulator to be in 
before you transfer between the server and client.

Once all of these consoles are open and the threads are running, follow the steps outlined in the Client's 
main() function to select your mode, file locations and other customizable options. To quit,
again follow the instruction in Client to send a shutdown command to the system.

Known issue:
* Client doesn't timeout during error simulator interactions.
* Corrupting packets doesn't work properly.


