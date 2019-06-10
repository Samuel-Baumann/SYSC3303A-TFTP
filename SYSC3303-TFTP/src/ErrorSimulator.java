import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

/** 
 * @author Sirak Berhane, Henri Umba, Samuel Baumann
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
	private int userCorrupt;
	private byte userByte1, userByte2;
	private String newFileName;
	private boolean octetCorrupt;
	private static Constants.ModeType printType;
	private Constants.ModeType printMode;
	private Print printable;

	public ErrorSimulator(int mode, float delay,int type,  int blockNum, int userCorrupt, byte userByte1, byte userByte2, String newFileName, boolean octetCorrupt) {
		this.mode = mode;
		this.delay = delay;
		this.type = type;
		this.blockNum = blockNum;
		this.userCorrupt = userCorrupt;
		this.userByte1 = userByte1;
		this.userByte2 = userByte2;
		this.newFileName = newFileName;
		this.setOctetCorrupt(octetCorrupt);
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
				if((info == type) && currBlockNum==blockNum) {// if it's of type packet and it has the same blockNumber then lose it
					System.out.println("Losing Packet");
					send = false;
					dup = 1;
				}
				break;
			case 1:
				if(info == type && currBlockNum==blockNum) {
					System.out.println("Delaying Packet");
					send = true;
					dup = 1;
					try {Thread.sleep((int)delay);}catch(InterruptedException ie) {ie.printStackTrace();}
				}
				break;
			case 2:
				if(info == type && currBlockNum == blockNum) {
					System.out.println("Duplicate Packet");
					send = true;
					dup = 2;
				}
				break;
			case 3:
				if(info == type && currBlockNum == blockNum) {
					System.out.println("Corrupting Packet");
					if(userCorrupt == 1) {
						receivePacket.getData()[0] = userByte1;
						receivePacket.getData()[1] = userByte2;
					}else if(userCorrupt == 2){	//must be read or write
						byte[] newFN = newFileName.getBytes();
						int currPosition = 2;
						while(receivePacket.getData()[currPosition] != 0) {	//replace filename
							receivePacket.getData()[currPosition] = newFN[currPosition - 2];
						}
					}else if(userCorrupt == 3) { //must be read or write
						receivePacket.getData()[receivePacket.getLength() - 2] = 3; //just arbitrarily mess up the usual OCTET format
					}
					send = true;
					dup = 1;
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
					System.out.println("Losing Packet");
					send = false;
					dup = 1;
				}
				break;
			case 1:
				if(info == type && currBlockNum==blockNum) {
					System.out.println("Delaying Packet");
					send = true;
					dup = 1;
					try {Thread.sleep((int)delay);}catch(InterruptedException ie) {ie.printStackTrace();}
				}
				break;
			case 2:
				if(info == type && currBlockNum == blockNum) {
					System.out.println("Duplicate Packet");
					send = true;
					dup = 2;
				}
				break;
			case 3:
				if(info == type && currBlockNum == blockNum) {
					System.out.println("Corrupting Packet");
					if(userCorrupt == 1) {
						receivePacket.getData()[0] = userByte1;
						receivePacket.getData()[1] = userByte2;
					}else if(userCorrupt == 2){	//must be read or write
						byte[] newFN = newFileName.getBytes();
						int currPosition = 2;
						while(receivePacket.getData()[currPosition] != 0) {	//replace filename
							receivePacket.getData()[currPosition] = newFN[currPosition - 2];
						}
					}else if(userCorrupt == 3) { //must be read or write
						receivePacket.getData()[receivePacket.getLength() - 2] = 3; //just arbitrarily mess up the usual OCTET format
					}
					send = true;
					dup = 1;
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

	public boolean isOctetCorrupt() {
		return octetCorrupt;
	}

	public void setOctetCorrupt(boolean octetCorrupt) {
		this.octetCorrupt = octetCorrupt;
	}

	public static void main(String args[]){
		Scanner input = new Scanner(System.in);
		int mode;
		printType = Constants.ModeType.VERBOSE;
		float delay = -0.5f;
		int blockNum = -1;	//initialized for compilation, always changed later;
		int type;
		int userCorrupt = -1;
		byte userByte1 = -1, userByte2 = -1;
		String newFileName = "";
		boolean octetCorrupt = false;

		while(true) {
			System.out.println("What type of Error would you like: "
					+ "\n\t[0] Lose"
					+ "\n\t[1] Delay"
					+ "\n\t[2] Duplicate"
					+ "\n\t[3] Corrupt");
			mode = input.nextInt();
			if(mode>=0 && mode<=3) break;
		}

		// How long should the delay be
		if(mode == 1) {
			while(true) {
				System.out.println("In seconds, how long should the delay be: (0.5 for half a second)");
				try {
					delay = input.nextFloat()*1000;
					System.out.println("you send: "+delay);
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
		
		if(mode == 3) {
			while(true) {
				System.out.println("What part would you like to corrupt? (1 - opcode, 2 - Filename, 3 - Mode (2-3 for RRQ WRQ only");
				userCorrupt = input.nextInt();
				if(userCorrupt < 1 || userCorrupt > 3) {
					System.out.println("input is out of range");
				}else if(userCorrupt == 1) {
					System.out.println("Enter the first byte for the new Opcode");
					userByte1 = input.nextByte();
					System.out.println("Enter the second byte for the new Opcode");
					userByte2 = input.nextByte();
					break;
				}else if(userCorrupt == 2) {
					System.out.println("Enter the new filename");
					newFileName = input.nextLine();
					break;
				}else if(userCorrupt == 3) {
					System.out.println("interfering with Octet string mode");
					octetCorrupt = true;
					break;
				}
			}
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
		ErrorSimulator sim = new ErrorSimulator(mode, delay, type, blockNum, userCorrupt, userByte1, userByte2, newFileName, octetCorrupt);
		sim.passOnTFTP();
	}
}
