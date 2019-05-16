import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Group 5
 * @version 5/11/2018 (Iteration #0)
 * 
 * Intermediate Host-side Algorithm
 */
public class Host extends Thread {
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket, receiveSocket, sendSocket;
	private byte dataRecieved[] = new byte[18];
	private Constants.ModeType mode;
	private Print printable;

	public Host(Constants.ModeType mode) throws UnknownHostException {
		this.mode = mode;
		printable = new Print(this.mode);

		try {
			receiveSocket = new DatagramSocket(23, InetAddress.getLocalHost());
			sendSocket = new DatagramSocket();
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e){
			print("HOST ERROR OCCURED: " + e.getStackTrace().toString());
		}
	}

	public void run() {
		try {
			sendReceievePackets();
		} catch (Exception e) {
			print("Host: Thread Error Occured");
		}
	}

	public void sendReceievePackets() throws UnknownHostException {
		// Receive a request from Client Instance
		InetAddress address = InetAddress.getLocalHost();
		int port;
		
		for(;;) {
			print("Host: Waiting for packets to arrive.\n");
			receivePacket = new DatagramPacket(dataRecieved, dataRecieved.length);
	
			try {
				receiveSocket.receive(receivePacket);
			} catch(IOException e) {
				print("Host: Error occured while receiving packet ==> Stack Trace "  + e.getStackTrace().toString());
				System.exit(1);
			}
	
			port = receivePacket.getPort();
			printable.PrintReceivedPackets(Constants.ServerType.HOST, Constants.ServerType.CLIENT, receivePacket.getAddress(),
					receivePacket.getPort(), receivePacket.getLength(), dataRecieved);
			
			String msg = new String(receivePacket.getData());
			if (msg.equals("CloseServerThreads")) {
				print("[Host]: Closing all sockets and thread instances ...");
				receiveSocket.close();
				Thread.currentThread().interrupt();
				print("[Host]: Server thread instance closed.");
				break;
			}
	
			// Send the received packets to server at port 69
			print("Host: Echo packets to main server. \n");
	
			try {
				sendPacket = new DatagramPacket(dataRecieved, dataRecieved.length, address, 69);
			} catch (Exception e) {
				print("Host: Error occured while creating packet ==> Stack Trace "  + e.getStackTrace().toString());
				System.exit(1);
			}
		
			printable.PrintSendingPackets(Constants.ServerType.HOST, Constants.ServerType.MAIN_SERVER, sendPacket.getAddress(),
					sendPacket.getPort(), sendPacket.getLength(), dataRecieved);
	
			try {
				sendReceiveSocket.send(sendPacket);
			} catch (IOException e) {
				print("Host: Error occured while sending packet ==> Stack Trace "  + e.getStackTrace().toString());
				System.exit(1);
			}
	
			print("Host: Packet sent to Main Server.\n");
	
			receivePacket = new DatagramPacket(new byte[1],1);
			// Receive response from Secondary Server
			try {
				sendReceiveSocket.receive(receivePacket);
			} catch(IOException e) {
				print("Host: Error occured while receiving packet ==> Stack Trace "  + e.getStackTrace().toString());
				System.exit(1);
			}
	
			// Print the response packet from Secondary Server
			printable.PrintReceivedPackets(Constants.ServerType.HOST, Constants.ServerType.SECONDARY_SERVER, receivePacket.getAddress(),
					receivePacket.getPort(), receivePacket.getLength(), dataRecieved);
	
			// Create a packet response for client and print the contents of the packet before sending
			sendPacket = new DatagramPacket(new byte[1], 1, address, port);
			printable.PrintSendingPackets(Constants.ServerType.HOST, Constants.ServerType.CLIENT, sendPacket.getAddress(),
					sendPacket.getPort(), sendPacket.getLength(), sendPacket.getData());
	
			try {
				sendSocket.send(sendPacket);
			} catch (IOException e) {
				print("Host: Error occured while sending packet ==> Stack Trace "  + e.getStackTrace().toString());
				System.exit(1);
			}
		}
	}

	private void print(String printable) {
		if (mode == Constants.ModeType.VERBOSE) {
			System.out.println(printable);
		}
	}
}
