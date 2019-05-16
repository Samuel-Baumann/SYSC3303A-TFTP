import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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
	private Scanner in = new Scanner(System.in);
	
	public Client(Constants.ModeType mode, boolean requestedThreadClosing) {
		this.mode = mode;
		this.setRequestedThreadClosing(requestedThreadClosing);
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
		}
	}

	public void sendReceivePackets() throws UnknownHostException {
		byte msg[] = new byte[18];
		String filename = "";
		String mode = "";
		String typeOfRequest = "";
		String optionSelected;

		sendPacket = new DatagramPacket(new byte[18], 18, InetAddress.getLocalHost(), 23);
		receivePacket = new DatagramPacket(new byte[1], 1);

		for(;;) {
			for(;;) {
				if (requestedThreadClosing == true) {
					break;
				}
				System.out.print("Enter (R) for read request, (W) for write request, or (E) for program termination: ");
				optionSelected = in.nextLine();
				if (optionSelected.toUpperCase().equals("R")) {
					typeOfRequest = Constants.PacketString.RRQ.getPacketStringType();
					print(typeOfRequest);
					break;
				} else if (optionSelected.toUpperCase().equals("W")) {
					typeOfRequest = Constants.PacketString.WRQ.getPacketStringType();
					break;
				} else if (optionSelected.toUpperCase().equals("E")) {
					print("Client: Shutdown requested to server");
					this.setRequestedThreadClosing(true);
					break;
				} else {
					print("Invalid input! Please select again.");
				}
			}

			for(;;) {
				if (requestedThreadClosing == true) {
					break;
				}
				print("Enter file name: ");
				filename = in.nextLine();
				if (filename.toUpperCase().equals("E")) {
					print("Client: Shutdown requested to server");
					this.setRequestedThreadClosing(true);
					break;
				} else if (filename != "") {
					break;
				} else {
					print("Invalid input! Please select again.");
				}
			}

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
				// Alternate between Read (even) and Write (odd) requests.
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
}
