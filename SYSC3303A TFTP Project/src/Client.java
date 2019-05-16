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
	private Constants.ModeType mode;
	private boolean requestedThreadClosing;
	private byte msg[] = new byte[18];
	private static String filename = "";
	private static String typeOfRequest = "";
	private static String optionSelected = "";
	private static Scanner in = new Scanner(System.in);

	public Client(Constants.ModeType mode, boolean requestedThreadClosing) {
		this.mode = mode;
		this.requestedThreadClosing = requestedThreadClosing;
		this.printable = new Print(this.mode);
		
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
			System.exit(1);
		}
	}

	public void sendReceivePackets() throws Exception {
		String mode = "";
		sendPacket = new DatagramPacket(new byte[18], 18, InetAddress.getLocalHost(), 23);
		receivePacket = new DatagramPacket(new byte[1], 1);

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

					msg[i+2] = data[i];
					msg[filename.getBytes().length + 2] = Constants.PacketByte.ZERO.getPacketByteType();

					// Convert mode from String to Byte[] and add it to message, then add 0 byte.
					mode = "netascii";
					System.arraycopy(mode.getBytes(), 0, msg, filename.getBytes().length + 3, mode.getBytes().length);
					int sizeOfMsg = filename.getBytes().length + mode.getBytes().length + 4;
					msg[sizeOfMsg - 1] = Constants.PacketByte.ZERO.getPacketByteType();;
					sendPacket.setData(msg);
				}

				// Add actual packet send or build logic
				if (typeOfRequest.equals("RRQ")) {
					print("[Client]: Reading file");
				} else {
					print("[Client]: Writing file");
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
		in.close();
		print("[Done] Client: Closed old sockets");
		System.exit(0);
	}

	public void getRequestTypeFromUser() throws IOException {		
		do {
			System.out.println("Enter (R) for read request, (W) for write request, or (E) for program termination: ");
			optionSelected = in.nextLine().toUpperCase();
			System.out.println("[Client] Requested inputted: " + optionSelected);
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

	public void getFileNameFromUser() throws IOException{
		System.out.println("Enter [1] Default File Location on Server [2] Custom File Location on Server: \n");
		int fileDefault = in.nextInt();

		if (fileDefault == 1) {
			do {				
				System.out.println("Enter file name: ");
				filename = in.nextLine();
			} while (filename.length() == 0);
		} else {
			filename = "test.txt";
		}

		if (filename.toUpperCase().equals("E")) {
			System.out.println("Client: Shutdown requested to server");
			requestedThreadClosing = true;
		} else {
			System.out.println("[Client] Filename entered: " + filename);
		}
	}

	private void print(String printable) {
		if (mode == Constants.ModeType.VERBOSE) {
			System.out.println(printable);
		}
	}

	public boolean isrequestedThreadClosing() {
		return requestedThreadClosing;
	}

	public void setRequestedThreadClosing(boolean requestedThreadClosing) {
		this.requestedThreadClosing = requestedThreadClosing;
	}
	
	public void startClient(Constants.ModeType mode, boolean requestedThreadClosing) {
		try {
			getRequestTypeFromUser();
		} catch (IOException e) {
			print("[Client] Request type error occured: " + e.getMessage());
		}
		
		try {
			getFileNameFromUser();
		} catch (Exception e) {
			print("[Client] File type error occured: " + e.getMessage());
		}
		
		Thread.currentThread().start();
	}
}
