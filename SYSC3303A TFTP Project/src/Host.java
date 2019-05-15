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
	private byte dataRecieved[] = new byte[100];
	private InetAddress clientAddress;
	private Constants.ModeType mode;
	private Print printable;
	private int clientPort;
	private int clientLength;

	public Host(Constants.ModeType mode) {
		this.mode = mode;
		printable = new Print(this.mode);

		try {
			receiveSocket = new DatagramSocket(23);
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

	public void sendReceievePackets() {
		// Receive a request from Client Instance
		print("Host: Waiting for packets to arrive.\n");
		receivePacket = new DatagramPacket(dataRecieved, dataRecieved.length);

		try {
			receiveSocket.receive(receivePacket);
		} catch(IOException e) {
			print("Host: Closing all ports.");
			System.exit(1);
		}

		clientAddress = receivePacket.getAddress();
		clientPort = receivePacket.getPort();
		clientLength = receivePacket.getLength();

		printable.PrintReceivedPackets(Constants.ServerType.HOST, Constants.ServerType.CLIENT, receivePacket.getAddress(),
				receivePacket.getPort(), receivePacket.getLength(), dataRecieved);

		// Send the received packets to server at port 69
		print("Host: Echo packets to main server. \n");

		try {
			sendPacket = new DatagramPacket(dataRecieved, dataRecieved.length, InetAddress.getLocalHost(), 69);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
	
		printable.PrintSendingPackets(Constants.ServerType.HOST, Constants.ServerType.MAIN_SERVER, sendPacket.getAddress(),
				sendPacket.getPort(), sendPacket.getLength(), dataRecieved);

		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		print("Host: Packet sent to Main Server.\n");

		// Receive response from Main Server
		try {
			sendReceiveSocket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Print the response packet from Main Server
		printable.PrintReceivedPackets(Constants.ServerType.HOST, Constants.ServerType.MAIN_SERVER, receivePacket.getAddress(),
				receivePacket.getPort(), receivePacket.getLength(), dataRecieved);

		// Create a packet response for client and print the contents of the packet before sending
		sendPacket = new DatagramPacket(dataRecieved, dataRecieved.length, clientAddress, clientPort);
		printable.PrintSendingPackets(Constants.ServerType.HOST, Constants.ServerType.CLIENT, clientAddress, clientPort, clientLength, dataRecieved);

		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void print(String printable) {
		if (mode == Constants.ModeType.VERBOSE) {
			System.out.println(printable);
		}
	}
}
