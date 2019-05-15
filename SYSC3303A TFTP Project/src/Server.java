import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * 
 * @author Group 5
 * @version 5/11/2018 (Iteration #0)
 * 
 * Server-side Algorithm
 *
 */
public class Server extends Thread{
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket receiveSocket, sendSocket;
	private byte dataRecieved[] = new byte[100];
	public boolean verbose = true;

	// Possible Responses Codes
	private static final byte validReadRequest[] = {"0".getBytes()[0], "3".getBytes()[0], "0".getBytes()[0], "1".getBytes()[0]};
	private static final byte validWriteRequest[] = {"0".getBytes()[0], "4".getBytes()[0], "0".getBytes()[0],"0".getBytes()[0]};
	private static final byte invalidRequest[] = {"0".getBytes()[0], "5".getBytes()[0]};

	public Server() {
		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(69);
			sendSocket.setSoTimeout(60000);
			receiveSocket.setSoTimeout(60000);
		} catch (SocketException e){
			print("SERVER ERROR OCCURED: " + e.getStackTrace().toString());
		}
	}

	public void run() {
		try {
			sendReceivePackets();
		}catch (Exception e) {

		}
	}

	public void sendReceivePackets() {
		// Receive and parse the packet that was sent from host.
		print("Server: Waiting for packets to arrive. \n");
		receivePacket = new DatagramPacket(dataRecieved, dataRecieved.length);

		try {
			receiveSocket.receive(receivePacket);
		} catch(IOException e) {
			print("Server: Closing all ports.");
			System.exit(1);
		}

		print("Server: Packet received.");
		print("From host: " + receivePacket.getAddress());
		print("Host port: " + receivePacket.getPort());
		print("Length: " + receivePacket.getLength());
		print("Containing (String): " + new String(dataRecieved,0,receivePacket.getLength()));
		String data = "|";
		for(int j = 0; j < dataRecieved.length; j++) {
			data += dataRecieved[j] + "|";
		}
		print("Containing (byte): " + data + "\n");

		// Verify packet format
		byte packetResponse[] = new byte[4];
		if(dataRecieved[0] == 48 && dataRecieved[1] == 49) {
			packetResponse = validReadRequest;
		}else if(dataRecieved[0] == 48 && dataRecieved[1] == 50) {
			packetResponse = validWriteRequest;
		}else {
			packetResponse = invalidRequest;
		}

		// 5 seconds server delay before sending to host.
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e ) {
			e.printStackTrace();
			System.exit(1);
		}

		// Send echo back to host.
		sendPacket = new DatagramPacket(packetResponse, packetResponse.length,receivePacket.getAddress(), receivePacket.getPort());

		print("Server: Sending packet.");
		print("To host: " + sendPacket.getAddress());
		print("Destination host port: " + sendPacket.getPort());
		print("Length: " + sendPacket.getLength());
		print("Containing (String): " + new String(packetResponse,0,packetResponse.length));
		String dataHost = "|";
		for(int j = 0; j < packetResponse.length; j++) {
			dataHost += packetResponse[j] + "|";
		}
		print("Containing (byte): " + dataHost + "\n");

		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
	
	public void toggleMode(boolean volume){
		verbose = volume;
	}

	private void print(String printable) {
		if(verbose){
			System.out.println(printable)
		}
	}
}
