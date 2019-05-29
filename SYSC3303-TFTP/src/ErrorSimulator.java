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

	public ErrorSimulator(int mode) {
		this.mode = mode;
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
		int clientPort, serverThreadPort=-1, len;

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

			// Process the received datagram.
			System.out.println("Simulator: Packet received:");
			System.out.println("From CLIENT: " + receivePacket.getAddress());
			clientPort = receivePacket.getPort();
			System.out.println("Client port: " + clientPort);
			len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: " +new String(receivePacket.getData(),2,len-2));

			// TODO: Server to Client port
			sendPacket = new DatagramPacket(data, len,
					receivePacket.getAddress(), (serverThreadPort==-1)?69:serverThreadPort);

			System.out.println("Simulator: sending packet.");
			System.out.println("To SERVER: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			System.out.println("Containing: " +new String("0"+(int)sendPacket.getData()[1]+sendPacket.getData()[2]*256+sendPacket.getData()[2]%256));

			// Send the datagram packet to the server via the send/receive socket.

			try {
				sendReceiveSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

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
			System.out.println("Simulator: Packet received:");
			System.out.println("From SERVER: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			System.out.println("Containing: " +
					new String("0"+(int)receivePacket.getData()[1]+receivePacket.getData()[2]*256+receivePacket.getData()[2]%256));

			serverThreadPort = receivePacket.getPort();

			sendPacket = new DatagramPacket(data, receivePacket.getLength(),
					receivePacket.getAddress(), clientPort);

			System.out.println( "Simulator: Sending packet:");
			System.out.println("To ClIENT: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			System.out.println("Containing: " +new String("0"+(int)sendPacket.getData()[1]+sendPacket.getData()[2]*256+sendPacket.getData()[2]%256));

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
		int mode;

		System.out.println("Pick a mode: "
				+ "\n[0]Lost Request"
				+ "\n[1]Delay Request"
				+ "\n[2]Duplicate Request"
				+ "\n[3]Lost data Request"
				+ "\n[4]Delay data Request"
				+ "\n[5]Duplicate data Request"
				+ "\n[6]Lost ack Request"
				+ "\n[7]Delay ack Request"
				+ "\n[8]Duplicate ack Request"
				+ "\n[9]Normal Mode\n");
		
		mode = input.nextInt();
		
		
		input.close();

		ErrorSimulator sim = new ErrorSimulator(mode);
		sim.passOnTFTP();
	}
}