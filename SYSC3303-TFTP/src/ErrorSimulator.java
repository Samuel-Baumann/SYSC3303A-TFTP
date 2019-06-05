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
	private float delay = 5000f;
	private int type;
	private int blockNum;
	private static Constants.ModeType printType;
	private Constants.ModeType printMode;
	private Print printable;

	public ErrorSimulator(int mode, float delay,int type,  int blockNum) {
		this.mode = mode;
		this.delay = delay;
		this.type = type;
		this.blockNum = blockNum;
		this.printMode = printType;
		this.printable = new Print(printMode);

		try {
			receiveSocket = new DatagramSocket(23);
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

		while(true) {
			data = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

			System.out.println("Simulator: Waiting for packet.");
			try {
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			clientPort = receivePacket.getPort();
			len = receivePacket.getLength();
			info = receivePacket.getData()[1];
			int currBlockNum = -1;
			if(info==3 || info==4) currBlockNum = receivePacket.getData()[2]*256 + receivePacket.getData()[3];
			printable.PrintReceivedPackets(Constants.ServerType.ERROR_SIMULATOR, Constants.ServerType.CLIENT, receivePacket.getAddress(), clientPort, len, currBlockNum, data);


			switch(mode) {
			case 0:
				System.out.println("******Losing Mode********* type: "+type+" info: "+info+" currBlockNum: "+currBlockNum+" blockNum: "+blockNum);//Test
				if((info == type) && currBlockNum==blockNum) {// if it's of type packet and it has the same blockNumber than lose it
					System.out.println("Losing Packet from Client");
					send = false;
					dup = 1;
				}
				break;
			case 1:
				if(info == type && currBlockNum==blockNum) {
					System.out.println("Delaying Packet from Client");
					send = true;
					dup = 1;
					try {Thread.sleep((int)delay);}catch(InterruptedException ie) {ie.printStackTrace();}
				}
				break;
			case 2:
				if(info == type && currBlockNum == blockNum) {
					System.out.println("Duplicate Packet from Client");
					send = true;
					dup = 2;
				}
				break;
			case 3:
				if(info == type && currBlockNum == blockNum) {
					System.out.println("Corrupting Packet from Client");
					send = true;
					dup = 2;
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

			info = receivePacket.getData()[1];
			switch(mode) {
			case 0:
				System.out.println("******Losing Mode********* type: "+type+" info: "+info+" currBlockNum: "+currBlockNum+" blockNum: "+blockNum);//Test
				if((info == type) && currBlockNum==blockNum) {// if it's of type packet and it has the same blockNumber than lose it
					System.out.println("Losing Packet from Client");
					send = false;
					dup = 1;
				}
				break;
			case 1:
				if(info == type && currBlockNum==blockNum) {
					System.out.println("Delaying Packet from Client");
					send = true;
					dup = 1;
					try {Thread.sleep((int)delay);}catch(InterruptedException ie) {ie.printStackTrace();}
				}
				break;
			case 2:
				if(info == type && currBlockNum == blockNum) {
					System.out.println("Duplicate Packet from Client");
					send = true;
					dup = 2;
				}
				break;
			case 3:
				if(info == type && currBlockNum == blockNum) {
					System.out.println("Corrupting Packet from Client");
					send = true;
					dup = 2;
				}
			}
			
			len = sendPacket.getLength();
			printable.PrintSendingPackets(Constants.ServerType.ERROR_SIMULATOR, Constants.ServerType.CLIENT, sendPacket.getAddress(), sendPacket.getPort(), len, null, sendPacket.getData());
			
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
		int mode;
		printType = Constants.ModeType.VERBOSE;
		float delay = -0.5f;
		int blockNum = -1;	//initialized for compilation, always changed later;
		int type;

		while(true) {
			System.out.println("What type of Error would you like: "
					+ "\n\t[0] Lose"
					+ "\n\t[1] Delay"
					+ "\n\t[2] Duplicate"
					+ "\n\t[3] Corrupt");
			mode = input.nextInt();
			if(mode>=0 && mode<=2) break;
		}

		// How long should the delay be
		if(mode == 1) {
			while(true) {
				System.out.println("In seconds, how long should the delay be: (0.5 for half a second)");
				try {
					delay = input.nextFloat()*1000;
					System.out.println("you send :"+delay);
				}catch(Exception e) {
					System.out.println("Error: not a float");
				}
				if(delay>0) break;
			}
		}

		// Which packet should error be applied to
		while(true) {
			System.out.println("What is the type of the packet: "
					+ "\n\t[1] Read request"
					+ "\n\t[2] Write request"
					+ "\n\t[3] Data packet"
					+ "\n\t[4] Acknowlegement");
			type = input.nextInt();
			if(type<=4 && type>=1)break;
		}

		// What block number
		if(type==3 || type == 4) {
			while(true) {
				System.out.println("What is the block number of the packet: ");
				blockNum = input.nextInt();
				if(blockNum>(65536*512) || blockNum<0) {
					System.out.println("input is out of range");
				}else {break;}
			}
		}


		input.close();
		ErrorSimulator sim = new ErrorSimulator(mode, delay, type, blockNum);
		sim.passOnTFTP();
	}
}

