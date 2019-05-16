import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author Group 5
 * @version 5/11/2018 (Iteration #0)
 * 
 * Secondary Server-side Algorithm
 */
public class SecondaryServer extends Thread{
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket receiveSocket, sendSocket;
	private byte dataRecieved[] = new byte[18];
	private Constants.ModeType mode;
	private Print printable;
	private static File file;
	private static boolean errorFound = false;

	/**
	 * 
	 * @param mode
	 * @param receiveSocket
	 */
	public SecondaryServer(Constants.ModeType mode, DatagramSocket receiveSocket) {
		this.mode = mode;
		this.receiveSocket = receiveSocket;
		printable = new Print(this.mode);

		try {		
			sendSocket = new DatagramSocket();
		} catch (SocketException e){
			print("SERVER ERROR OCCURED: " + e.getStackTrace().toString());
		}
	}

	/**
	 * 
	 */
	public void run() {
		try {
			sendReceivePackets();
		} catch (Exception e) {
			print("Secondary Server: Thread Error Occured -> " + e.getStackTrace().toString());
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void sendReceivePackets() throws Exception {
		// Receive and parse the packet that was sent from main server.
		print("Server: Waiting for packets to arrive. \n");
		receivePacket = new DatagramPacket(dataRecieved, dataRecieved.length);
		InetAddress address = InetAddress.getLocalHost();

		try {
			receiveSocket.receive(receivePacket);
		} catch(IOException e) {
			print("Server: Closing all ports.");
			System.exit(1);
		}

		int port = receivePacket.getPort();
		printable.PrintReceivedPackets(Constants.ServerType.SECONDARY_SERVER, Constants.ServerType.MAIN_SERVER, receivePacket.getAddress(),
				receivePacket.getPort(), receivePacket.getLength(), receivePacket.getData());

		// File process here
		// this.file = find(new File("C:\\"), new String (dataRecieved, 2, dataRecieved.length-12));

		if (errorFound == true) {
			// Send Error Packet --> Error Message "File doesnt exist on the server
		}
		
		// Read file into bytes

		// Verify packet format and send block 0 or 1 bytes of data
		byte packetResponse[] = new byte[1];
		if(dataRecieved[1] == Constants.PacketByte.RRQ.getPacketByteType()) {
			// 1) RRQ Request
			// 2) Data Sent Back (n-1)
			// 3) ACK from client (n-1)
			// 4) Last Data and ACK sent (n)
			packetResponse = new byte[]{0x00};
		} else if(dataRecieved[1] == Constants.PacketByte.WRQ.getPacketByteType()) {
			// 1) WRQ Request
			// 2) ACK Sent Back
			// 3) New Data comes in 512 bytes size
			// 4) Send ACK for each data until the last (data < 512 bytes)
			packetResponse = new byte[]{0x01};
		} else {
			throw new Exception("InvalidPacketFormatException");
		}

		// Send echo back to Host
		sendPacket = new DatagramPacket(packetResponse, packetResponse.length, address, 23);
		printable.PrintSendingPackets(Constants.ServerType.SECONDARY_SERVER, Constants.ServerType.HOST, sendPacket.getAddress(),
				sendPacket.getPort(), sendPacket.getLength(), sendPacket.getData());

		try {
			sendSocket.send(sendPacket);
			print("Secondary Server: Closing thread instance @(PORT " + port + ")");
			sendSocket.close();
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			print("Secondary Server: Error occured while sending packet ==> Stack Trace "  + e.getStackTrace().toString());
			System.exit(1);
		}
	}

	/**
	 * Recursive file finder.
	 * 
	 * @param root the default root folder is C://
	 * @param filename filename to be found
	 * @return file return the file's location in the drive
	 */
	public File find(File root, String filename) {
		try {
			// Try to find the file
			for (File temp : root.listFiles()) {
				if (temp.isDirectory()) {
					find(temp, filename);
					// If found assign <file> to path
				} else if (temp.getName().endsWith(filename)) {
					file = temp.getAbsoluteFile();
				}
			}
			// If a null directory was inputed i.e directory 
			// is not found output error message + type of error
		} catch (NullPointerException e) {
			print("\nERROR: " + "[" + e.getMessage() + "]" + " Invalid directory was given or file doesn't exist.");
			errorFound = true;
		}
		//Return full path name
		return file;
	}

	/**
	 * 
	 * @param printable
	 */
	private void print(String printable) {
		if (mode == Constants.ModeType.VERBOSE) {
			System.out.println(printable);
		}
	}
}
