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
 * Client-side Algorithm
 */
public class Client extends Thread{	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;
	private Print printable;
	private Constants.ModeType mode;
	private boolean shutoff;

	public Client(Constants.ModeType mode, boolean shutoff) {
		this.mode = mode;
		this.setShutoff(shutoff);
		printable = new Print(this.mode);

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
			print("[Client] Thread Error Occured: " + e.getStackTrace().toString());
		}
	}

	public void sendReceivePackets() {
		byte msg[] = new byte[100];
		String filename = "";
		String mode = "";

		// Alternate between Read (even) and Write (odd) requests.
		//		msg[0] = 0;
		//		msg[1] = 1;
		//		
		//		msg[0] = 0;
		//		msg[1] = 2;
		//		
		//		msg[0] = 0;
		//		msg[1] = 5;		// Invalid Request occurs on the 11th request

		// Convert filename from String to Byte[] and add it to message, then add 0 byte.
		filename = "test.txt";
		System.arraycopy(filename.getBytes(), 0, msg, 2, filename.getBytes().length);
		msg[filename.getBytes().length + 2] = "0".getBytes()[0];

		// Convert mode from String to Byte[] and add it to message, then add 0 byte.
		mode = "netascii";
		System.arraycopy(mode.getBytes(), 0, msg, filename.getBytes().length + 3, mode.getBytes().length);
		int sizeOfMsg = filename.getBytes().length + mode.getBytes().length + 4;
		msg[sizeOfMsg - 1] = "0".getBytes()[0];

		// Send the packet to Host
		try {
			sendPacket = new DatagramPacket(msg, sizeOfMsg, InetAddress.getLocalHost(), 23);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		printable.PrintSendingPackets(Constants.ServerType.CLIENT, Constants.ServerType.HOST, sendPacket.getAddress(),
				sendPacket.getPort(), sendPacket.getLength(), sendPacket.getData());
		
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		print("Client: Packet sent to Host.\n");

		// Receive a packet from Host [ACK]
		byte data[] = new byte[4];
		receivePacket = new DatagramPacket(data, data.length);

		print("Client: Waiting for packets to arrive.\n");

		try {
			sendReceiveSocket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		printable.PrintReceivedPackets(Constants.ServerType.CLIENT, Constants.ServerType.HOST, receivePacket.getAddress(),
				receivePacket.getPort(), receivePacket.getLength(), receivePacket.getData());

		// TODO: Process response code [DATA]

		print("Client: Closing old sockets ...");
		sendReceiveSocket.close();
		print("[Done] Client: Closed old sockets");
		System.exit(0);
	}

	private void print(String printable) {
		if (mode == Constants.ModeType.VERBOSE) {
			System.out.println(printable);
		}
	}

	public boolean isShutoff() {
		return shutoff;
	}

	public void setShutoff(boolean shutoff) {
		this.shutoff = shutoff;
	}
}
