import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

/** 
 * @author Sirak Berhane, Ruchi Bhatia, Henri Umba
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
	
	public ErrorSimulator()
	{
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
		int rrq = 0;
		int data = 0;
		int ack = 0;
		
		boolean done = false;
		while(!done) {
			while(true) {
				System.out.println(	"Which Mode to Apply: "
									+ "\n    [0]Normal Mode"
									+ "\n    [1]Lost packet Mode"
									+ "\n    [2]Delayed Packet Mode"
									+ "\n    [3]Duplicated Packet Mode"
									+ "\n    [4]Done");
				int mode = input.nextInt();
				if(!(mode>-1 && mode<4)) {System.out.println("Pick One of the Option! (1-4)");}
				if(mode==4) {done = true;break;}
				
				System.out.println("Which Packet type to apply Mode:"
									+ "\n    [0]Read Request (RRQ) Packets"
									+ "\n    [1]Write Request Packets"
									+ "\n    [2]Data Packets");
			}
		}
		
		ErrorSimulator sim = new ErrorSimulator();
		sim.passOnTFTP();
	}
}