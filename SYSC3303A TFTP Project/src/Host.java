import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 
 * @author Group 5
 * @version 5/11/2018 (Iteration #0)
 * 
 * Intermediate Host-side Algorithm
 *
 */
public class Host extends Thread{
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket, receiveSocket, sendSocket;
	private byte dataRecieved[] = new byte[100];
	private InetAddress clientAddress;
	private int clientPort;
	private int clientLength;


	public Host() {
		try {
			receiveSocket = new DatagramSocket(23);
			sendSocket = new DatagramSocket();
			sendReceiveSocket = new DatagramSocket();
			receiveSocket.setSoTimeout(60000);
			sendSocket.setSoTimeout(60000);
			sendReceiveSocket.setSoTimeout(60000);
		} catch (SocketException e){
			print("HOST ERROR OCCURED: " + e.getStackTrace().toString());
		}
	}

	public void run() {
		try {
			sendReceievePackets();
		}catch (Exception e) {

		}
	}

	public void sendReceievePackets() {
		// Repeat send and receive requests indefinitely 

		// Receive a request from Client Instance
		print("Host: Waiting for packets to arrive.\n");
		receivePacket = new DatagramPacket(dataRecieved, dataRecieved.length);

		try {
			receiveSocket.receive(receivePacket);
		} catch(IOException e) {
			print("Host: Closing all ports.");
			System.exit(1);
		}

		print("Host: Packet received.");
		print("From client: " + receivePacket.getAddress());
		clientAddress = receivePacket.getAddress();
		print("Host port: " + receivePacket.getPort());
		clientPort = receivePacket.getPort();
		print("Length: " + receivePacket.getLength());
		clientLength = receivePacket.getLength();
		print("Containing (String): " + new String(dataRecieved,0,receivePacket.getLength()));
		String data = "|";
		for(int j = 0; j < dataRecieved.length; j++) {
			data += dataRecieved[j] + "|";
		}
		print("Containing (byte): " + data + "\n");

		// Send the received packets to server at port 69
		print("Host: Echo packets to server. \n");

		try {
			sendPacket = new DatagramPacket(dataRecieved, dataRecieved.length, InetAddress.getLocalHost(), 69);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		print("Host: Sending packet.");
		print("To server: " + sendPacket.getAddress());
		print("Destination server port: " + sendPacket.getPort());
		print("Length: " + sendPacket.getLength());
		print("Containing (String): " + new String(dataRecieved,0,receivePacket.getLength()));
		String dataToServer = "|";
		for(int j = 0; j < dataRecieved.length; j++) {
			dataToServer += dataRecieved[j] + "|";
		}
		print("Containing (byte): " + dataToServer + "\n");

		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		print("Host: Packet sent to Server.\n");

		// Receive response from server
		try {
			sendReceiveSocket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		print("Host: Packet received.");
		print("From server: " + receivePacket.getAddress());
		print("Host port: " + receivePacket.getPort());
		print("Length: " + receivePacket.getLength());
		print("Containing (String): " + new String(dataRecieved,0,receivePacket.getLength()));
		String dataServer = "|";
		for(int j = 0; j < dataRecieved.length; j++) {
			dataServer += dataRecieved[j] + "|";
		}
		print("Containing (byte): " + dataServer + "\n");

		sendPacket = new DatagramPacket(dataRecieved, dataRecieved.length, clientAddress, clientPort);

		print("Host: Sending packet.");
		print("To client: " + clientAddress);
		print("Destination client port: " + clientPort);
		print("Length: " + clientLength);
		print("Containing (String): " + new String(dataRecieved,0,receivePacket.getLength()));
		String dataClient = "|";
		for(int j = 0; j < dataRecieved.length; j++) {
			dataClient += dataRecieved[j] + "|";
		}
		print("Containing (byte): " + dataClient + "\n");

		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void print(String printable) {
		System.out.println(printable);
	}
}
