import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
 * @author Group 5
 * @version 5/11/2018 (Iteration #0)
 * 
 * Client-side Algorithm
 */
public class Client extends Thread{	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;
	private Print printable;
	private static Constants.ModeType mode;
	private static boolean requestedThreadClosing;
	private byte msg[] = new byte[512];
	private static String filename = "";
	private static String typeOfRequest = "";
	private static Scanner in = new Scanner(System.in);
	private static String userInput;
	private static String userPreference;
	private static int userPortPrefence;
	private static String optionSelected = "";

	public Client() {
		this.printable = new Print(mode);

		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e){
			print("[CLIENT] ERROR OCCURED: " + e.getStackTrace().toString());
		}
	}

	public void run() {
		try {
			sendReceivePackets();
		} catch (Exception e) {
			print("[Client] Thread Error Occured: " + e.getMessage());
			System.out.print("[Client] Stack trace information --> ");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void sendReceivePackets() throws Exception {
		String mode = "";
		receivePacket = new DatagramPacket(new byte[512], 512);
		sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), userPortPrefence);

		for(;;) {
			if (requestedThreadClosing == true) {
				msg = new String("CloseServerThreads").getBytes();
				sendPacket.setData(msg);
				printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.HOST, sendPacket.getAddress(),
						sendPacket.getPort(), sendPacket.getLength(), sendPacket.getData());
				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					print("Client: Error occured while sending packet ==> Stack Trace "  + e.getStackTrace().toString());
					System.exit(1);
				}
				Thread.currentThread().interrupt();
				break;
			} else {
				// Read (RRQ) or Write (WRQ) requests write format into packet data
				byte data[] = filename.getBytes();
				for(int i = 0; i < data.length; i++) {
					msg[0] = Constants.PacketByte.ZERO.getPacketByteType();
					if (typeOfRequest.equals("RRQ")) {
						msg[1] = Constants.PacketByte.RRQ.getPacketByteType();
					} else if (typeOfRequest.equals("WRQ")) {
						msg[1] = Constants.PacketByte.WRQ.getPacketByteType();
					} else {
						msg = new String("CloseServerThreads").getBytes();
						sendPacket.setData(msg);
						printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.HOST, sendPacket.getAddress(),
								sendPacket.getPort(), sendPacket.getLength(), sendPacket.getData());
						try {
							sendReceiveSocket.send(sendPacket);
						} catch (IOException e) {
							print("Client: Error occured while sending packet ==> Stack Trace "  + e.getStackTrace().toString());
							System.exit(1);
						}
						Thread.currentThread().interrupt();
						break;
					}

					System.arraycopy(data,0,msg,2,data.length);
					msg[filename.getBytes().length + 2] = Constants.PacketByte.ZERO.getPacketByteType();

					// Convert mode from String to Byte[] and add it to message, then add 0 byte.
					mode = "netascii";
					byte[] modeArray = mode.getBytes();
					System.arraycopy(modeArray, 0, msg, filename.getBytes().length + 3, modeArray.length);
					int sizeOfMsg = filename.getBytes().length + modeArray.length + 4;
					msg[sizeOfMsg - 1] = Constants.PacketByte.ZERO.getPacketByteType();
					sendPacket = new DatagramPacket(msg, sizeOfMsg, InetAddress.getLocalHost(), userPortPrefence);
				}

				// Add actual packet send or build logic
				if (typeOfRequest.equals("RRQ")) {
					print("[Client]: Reading file \n");
				} else {
					print("[Client]: Writing file \n");
				}

				// Send the packet to Host
				printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.HOST, sendPacket.getAddress(),
						sendPacket.getPort(), sendPacket.getLength(), sendPacket.getData());

				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

				print("Client: Packet sent to Host.\n");

				// Receive a packet from Host
				print("Client: Waiting for packets to arrive.\n");

				try {
					sendReceiveSocket.receive(receivePacket);
				} catch(IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				printable.PrintReceivedPackets(Constants.ServerType.CLIENT, Constants.ServerType.HOST, receivePacket.getAddress(),
						receivePacket.getPort(), receivePacket.getLength(), receivePacket.getData());
			}
		}
		print("Client: Closing old sockets ...");
		sendReceiveSocket.close();
		print("[Done] Client: Closed old sockets");
		System.exit(0);
	}

	public static void getRequestTypeFromUser() throws IOException {		
		do {
			System.out.println("Enter (R) for read request, (W) for write request, or (E) for program termination: ");
			optionSelected = in.nextLine().toUpperCase();
			System.out.println("[Client] Requested inputted: " + optionSelected + "\n");
		} while (!(optionSelected.equals("R") || optionSelected.equals("W") || optionSelected.equals("E")));

		if (optionSelected.equals("R")) {
			typeOfRequest = Constants.PacketString.RRQ.getPacketStringType();
		} 

		if (optionSelected.toUpperCase().equals("W")) {
			typeOfRequest = Constants.PacketString.WRQ.getPacketStringType();
		} 

		if (optionSelected.toUpperCase().equals("E")) {
			System.out.println("Client: Shutdown requested to server");
			requestedThreadClosing = true;
		}
	}

	public static void getFileNameFromUser() throws IOException{
		System.out.println("Enter [1] Default File name on Server (i.e. test.txt) [2] Custom File name on Server: \n");
		int fileDefault = in.nextInt();

		if (fileDefault == 2) {
			do {				
				System.out.println("Enter file name: \n");
				filename = in.nextLine();
			} while (filename.length() == 0);
		} else {
			filename = "test.txt";
		}

		if (filename.toUpperCase().equals("E")) {
			System.out.println("Client: Shutdown requested to server");
			requestedThreadClosing = true;
		} else {
			System.out.println("\n [Client] Filename entered: " + filename);
		}
	}
	
	private void print(String printable) {
		if (mode == Constants.ModeType.VERBOSE) {
			System.out.println(printable);
		}
	}

	public static void main(String[] args) throws IOException {		
		do {
			System.out.println("Enter console output mode (VERBOSE or QUIET)");
			userInput = in.nextLine().toUpperCase();
		} while (!(userInput.equals("VERBOSE") || userInput.equals("QUIET")));

		if (userInput.equals("VERBOSE")) {
			mode = Constants.ModeType.VERBOSE;
		} 

		if (userInput.equals("QUIET")) {
			mode = Constants.ModeType.QUIET;
		}

		do {
			System.out.println("Send packets directly to Server or send to Host first ([Y]es {Connect Directly to Server} / [N]o {Host First}) ?");
			userPreference = in.nextLine().toUpperCase();
		} while (!(userPreference.equals("Y") || userPreference.equals("N")));

		if (userPreference.equals("Y")) {
			userPortPrefence = Constants.ClientPacketSendType.CONNECT_DIRECTLY.getPortID();
		} 

		if (userPreference.equals("N")) {
			userPortPrefence = Constants.ClientPacketSendType.HOST_FIRST.getPortID();
		}

		try {
			getRequestTypeFromUser();
		} catch (IOException e) {
			System.out.println("[Client] Request type error occured: " + e.getMessage());
		}

		try {
			getFileNameFromUser();
		} catch (Exception e) {
			System.out.println("[Client] File type error occured: " + e.getMessage());
		}

		in.close();
		try {
			Client client = new Client();
			client.start();
			System.out.println("[Client] ~ Started Client \n");
		} catch (Exception e) {
			System.out.println("[Client] ~ Error occured while starting host: " + e.getMessage());
		}
	}
}
