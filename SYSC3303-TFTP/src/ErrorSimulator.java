import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

/** 
 * @author Sirak Berhane, Henri Umba
 * 
 * ErrorSimulator.java
 * This class is the beginnings of an error simulator for a simple TFTP server 
 * based on UDP/IP. The simulator receives a read or write packet from a client and
 * passes it on to the server.  Upon receiving a response, it passes it on to the 
 * client.
 * One socket (23) is used to receive from the client, and another to send/receive
 * from the server.  A new socket is used for each communication back to the client.  
 */
public class ErrorSimulator {
	// UDP Data gram packets and sockets used to send / receive
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;
	private int mode;
	private int delay = 5000;
	private int blockNum;
	private static Constants.ModeType printType;
	private Constants.ModeType printMode;
	private Print printable;

	public ErrorSimulator(int mode, int delay, int blockNum) {
		this.mode = mode;
		this.delay = delay;
		this.blockNum = blockNum;
		this.printMode = printType;
		this.printable = new Print(printMode);
		try {
			// Construct a datagram socket and bind it to port 23
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets from clients.
			receiveSocket = new DatagramSocket(23);
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets from the server.
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void passOnTFTP(){
		byte[] data;
		int clientPort, serverThreadPort=-1, len, dup = 1, info;
		boolean send = true;
		int target = blockNum;

		for(;;) { // loop forever
			// Construct a DatagramPacket for receiving packets up
			// to 100 bytes long (the length of the byte array).
			data = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

			System.out.println("Simulator: Waiting for packet.");
			// Block until a datagram packet is received from receiveSocket.
			try {
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			clientPort = receivePacket.getPort();
			len = receivePacket.getLength();
			int currBlockNum = receivePacket.getData()[2]*256 + receivePacket.getData()[3];
			info = receivePacket.getData()[1];
			printable.PrintReceivedPackets(Constants.ServerType.ERROR_SIMULATOR, Constants.ServerType.CLIENT, receivePacket.getAddress(), clientPort, len, currBlockNum, data);

			if(mode != 6) {
				switch(mode) {
				case 0:
					if(info == 3) {
						if(currBlockNum == target) {
							System.out.println("Losing Packet from Client");
							send = false;
							dup = 1;
						}
					}
					break;
				case 1:
					if(info == 3) {
						if(currBlockNum == target) {
							System.out.println("Delaying Packet from Client");
							send = true;
							dup = 1;
							try {Thread.sleep(delay);}catch(InterruptedException ie) {ie.printStackTrace();}
						}
					}
					break;
				case 2:
					if(info == 3) {
						if(currBlockNum == target) {
							System.out.println("Duplicate Packet from Client");
							send = true;
							dup = 2;
						}
					}
					break;
				case 3:
					if(info == 4) {
						if(currBlockNum == target) {
							System.out.println("Losing Packet from Client");
							send = false;
							dup = 1;
						}
					}
					break;
				case 4:
					if(info == 4) {
						if(currBlockNum == target) {
							System.out.println("Delaying Packet from Client");
							send = true;
							dup = 1;
							try {Thread.sleep(delay);}catch(InterruptedException ie) {ie.printStackTrace();}
						}
					}
					break;
				case 5:
					if(info == 4) {
						if(currBlockNum == target) {
							System.out.println("Duplicate Packet from Client");
							send = true;
							dup = 2;
						}
					}
					break;
				case 7:
					if(info == 1) {
						System.out.println("Lose RRQ");
						send = false;
						dup = 1;
					}
					break;
				case 8:
					if(info == 1) {
						System.out.println("Delay RRQ");
						send = true;
						dup = 1;
						try {Thread.sleep(delay);}catch(InterruptedException ie) {ie.printStackTrace();}
					}
					break;
				case 9:
					if(info == 1) {
						System.out.println("Duplicate RRQ");
						send = true;
						dup = 2;
					}
					break;
				case 10:
					if(info == 2) {
						System.out.println("Lose WRQ");
						send = false;
						dup = 1;

					}
					break;
				case 11:
					if(info == 2) {
						System.out.println("Delay WRQ");
						send = true;
						dup = 1;
						try {Thread.sleep(delay);}catch(InterruptedException ie) {ie.printStackTrace();}
					}
					break;
				case 12:
					if(info == 2) {
						System.out.println("Duplicate WRQ");
						send = true;
						dup = 2;
					}
					break;
				}
				
			}

			for(int x = 0; x<dup; x++) {
				if(send) {
					sendPacket = new DatagramPacket(data, len,
							receivePacket.getAddress(), (serverThreadPort==-1)?69:serverThreadPort);
					
					len = sendPacket.getLength();
					printable.PrintSendingPackets(Constants.ServerType.ERROR_SIMULATOR, Constants.ServerType.SERVER, sendPacket.getAddress(), sendPacket.getPort(), len, null, sendPacket.getData());

					// Send the datagram packet to the server via the send/receive socket.
					try {
						sendReceiveSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			}

			send = true;

			// Construct a DatagramPacket for receiving packets up
			// to 100 bytes long (the length of the byte array).
			data = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

			System.out.println("Simulator: Waiting for packet.");
			try {
				// Block until a datagram is received via sendReceiveSocket.
				sendReceiveSocket.receive(receivePacket);
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			len = receivePacket.getLength();
			printable.PrintReceivedPackets(Constants.ServerType.ERROR_SIMULATOR, Constants.ServerType.SERVER, receivePacket.getAddress(), receivePacket.getPort(), len, null, receivePacket.getData());
			serverThreadPort = receivePacket.getPort();

			sendPacket = new DatagramPacket(data, receivePacket.getLength(),
					receivePacket.getAddress(), clientPort);
			
			len = sendPacket.getLength();
			printable.PrintSendingPackets(Constants.ServerType.ERROR_SIMULATOR, Constants.ServerType.SERVER, sendPacket.getAddress(), sendPacket.getPort(), len, null, sendPacket.getData());
			// Send the datagram packet to the client via a new socket.
			try {
				// Construct a new datagram socket and bind it to any port
				// on the local host machine. This socket will be used to
				// send UDP Datagram packets.
				sendSocket = new DatagramSocket();
			} catch (SocketException se) {
				se.printStackTrace();
				System.exit(1);
			}

			try {
				sendSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			// We're finished with this socket, so close it.
			sendSocket.close();
		} // end of loop
	}

	public static void main(String args[]){
		Scanner input = new Scanner(System.in);
<<<<<<< HEAD
<<<<<<< HEAD
		int mode;
		printType = Constants.ModeType.VERBOSE;
		int delay = 5000;	//default delay is 5 seconds
		int blockNum = 8;	//initialized for compilation, always changed later;
=======
		int mode; String inputText;
>>>>>>> parent of de32d0e... Fixed Read and Write minor bug
=======
		int mode; String inputText;
>>>>>>> parent of de32d0e... Fixed Read and Write minor bug
		
		System.out.println("Pick a mode: "
				+ "\n[0]Lost data"
				+ "\n[1]Delay data"
				+ "\n[2]Duplicate data"
				+ "\n[3]Lost ack"
				+ "\n[4]Delay ack"
				+ "\n[5]Duplicate ack"
<<<<<<< HEAD
				+ "\n[6]NORMAL MODE"
				+ "\n[7]Lost RRQ"
				+ "\n[8]Delay RRQ"
				+ "\n[9]Duplicate RRQ"
				+ "\n[10]Lost WRQ"
				+ "\n[11]Delay WRQ"
				+ "\n[12]Duplicate WRQ"
				+ "\n[13]TO DO");
		mode = input.nextInt();
		
		if(mode == 1 || mode == 4 || mode == 8 || mode == 11) {
			System.out.println("Enter desired delay in milliseconds: ");
			delay = input.nextInt();
		}
		
		if(mode < 6) {
			System.out.println("Enter the block number to interfere with");
			blockNum = input.nextInt();
		}
		
		
		
=======
				+ "\n[6]Normal Mode\n");
		mode = input.nextInt();

		System.out.println("Enter v for verbose or q for quiet console output mode: ");
		inputText = input.nextLine();
		if (inputText.equals("v")) {
			printType = Constants.ModeType.VERBOSE;
		} else {
			printType = Constants.ModeType.QUIET;
		}
		
<<<<<<< HEAD
>>>>>>> parent of de32d0e... Fixed Read and Write minor bug
=======
>>>>>>> parent of de32d0e... Fixed Read and Write minor bug
		input.close();
		ErrorSimulator sim = new ErrorSimulator(mode, delay, blockNum);
		sim.passOnTFTP();
	}
}
