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
 
Iteration 2 - Completed On: Tuesday, May 28, 2019

Deliverables:
* README (this document)
* UML Class Diagram
* Sequence Diagrams (Client, Host/Err. Simulator, Server)
* State Machine Diagrams (Client, Host/Err. Simulator, Server)
* Use Case Document
* Code for each component + supplementary classes & constants

Breakdown:
* Code - Sirak and Henri: 
	* Fixed prior Read and Write errors that existed in Iteration #1
	* Added support for error simulation for both server and client side
* Supporting Documentation, UML, Sequence and Use Case: Sam
* State Machine diagrams: Gen

Setup:
To run, a user must have an instance of the client and server on 
the same machine, and must also run the error simulator if they wish to run tests. 

Make sure that you decide which mode you want the error simulator to be in 
before you transfer between the server and client.

Once all of these consoles are open and the threads are running, follow the steps outlined in the Client's 
main() function to select your mode, file locations and other customizable options. To quit,
again follow the instruction in Client to send a shutdown command to the system.
