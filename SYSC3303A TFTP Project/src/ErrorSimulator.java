import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Team 05 - (Sirak Berhane, Samuel Baumann, Ruchi Bhatia)
 * @version 5/21/2018 (Iteration #1)
 * 
 * Error simulation thread, establishes a connection between client 
 * and server connection for simulating packet error or loss.
 */
public class ErrorSimulator extends Thread implements Runnable{
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket, receiveSocket, sendSocket;
	private byte dataRecieved[] = new byte[512];
	private Constants.ModeType mode;
	private Print printable;

	/**
	 * Error simulator constructor
	 * 
	 * @param mode type of console output mode (i.e. Verbose or Quiet)
	 * @throws UnknownHostException to indicate that the IP address of
	 * the error simulator could not be determined.
	 */
	public ErrorSimulator(Constants.ModeType mode) throws UnknownHostException {
		this.mode = mode;
		printable = new Print(this.mode);

		try {
			receiveSocket = new DatagramSocket(23);
			sendSocket = new DatagramSocket();
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e){
			print("ErrorSimulator: Socket Error Occured --> " + e.getMessage());
		}
	}

	/**
	 * Error simulator thread runnable
	 */
	public void run() {
		try {
			sendReceievePackets();
		} catch (Exception e) {
			print("ErrorSimulator: Thread Error Occured");
		}
	}

	/**
	 * Send and receive loop between Cleint/Server.
	 * 
	 * @throws UnknownHostException to indicate that the IP address of
	 * the error simulator could not be determined.
	 */
	public void sendReceievePackets() throws UnknownHostException {
		// Receive a request from Client Instance
		InetAddress address = Client.getServerAddress();
		InetAddress clientAddress;
		int port;

		for(;;) {
			print("ErrorSimulator: Waiting for packets to arrive.\n");
			receivePacket = new DatagramPacket(dataRecieved, dataRecieved.length);

			try {
				receiveSocket.receive(receivePacket);
			} catch(IOException e) {
				print("ErrorSimulator: Error occured while receiving packet ==> Stack Trace "  + e.getStackTrace().toString());
				System.exit(1);
			}

			port = receivePacket.getPort();
			clientAddress = receivePacket.getAddress();
			printable.PrintReceivedPackets(Constants.ServerType.ERROR_SIMULATOR, Constants.ServerType.CLIENT, receivePacket.getAddress(),
					receivePacket.getPort(), receivePacket.getLength(), dataRecieved);

			// Send the received packets to server at port 69
			print("ErrorSimulator: Echo packets to main server. \n");
			try {
				sendPacket = new DatagramPacket(dataRecieved, dataRecieved.length, InetAddress.getLocalHost(), 69);
			} catch (Exception e) {
				print("ErrorSimulator: Error occured while creating packet ==> Stack Trace "  + e.getStackTrace().toString());
				System.exit(1);
			}

			printable.PrintSendingPackets(Constants.ServerType.ERROR_SIMULATOR, Constants.ServerType.SERVER, sendPacket.getAddress(),
					sendPacket.getPort(), sendPacket.getLength(), dataRecieved);

			try {
				sendReceiveSocket.send(sendPacket);
			} catch (IOException e) {
				print("ErrorSimulator: Error occured while sending packet ==> Stack Trace "  + e.getStackTrace().toString());
				System.exit(1);
			}

			print("ErrorSimulator: Packet sent to Main Server.\n");

			// Receive response from Secondary Server
			receivePacket = new DatagramPacket(new byte[512], 512);
			try {
				receiveSocket.receive(receivePacket);
			} catch(IOException e) {
				print("ErrorSimulator: Error occured while receiving packet ==> Stack Trace "  + e.getStackTrace().toString());
				System.exit(1);
			}

			// Print the response packet from Secondary Server
			printable.PrintReceivedPackets(Constants.ServerType.ERROR_SIMULATOR, Constants.ServerType.SERVER_CONNECTION_HANDLER, receivePacket.getAddress(),
					receivePacket.getPort(), receivePacket.getLength(), dataRecieved);

			// Create a packet response for client and print the contents of the packet before sending
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), clientAddress, port);
			printable.PrintSendingPackets(Constants.ServerType.ERROR_SIMULATOR, Constants.ServerType.CLIENT, sendPacket.getAddress(),
					sendPacket.getPort(), sendPacket.getLength(), sendPacket.getData());

			try {
				sendSocket.send(sendPacket);
			} catch (IOException e) {
				print("ErrorSimulator: Error occured while sending packet ==> Stack Trace "  + e.getStackTrace().toString());
				System.exit(1);
			}
		}
	}

	/**
	 * General verbose print funtion for non-static methods.
	 * 
	 * @param printable string output value
	 */
	private void print(String printable) {
		if (mode == Constants.ModeType.VERBOSE) {
			System.out.println(printable);
		}
	}

	public static void main(String[] args) {
		try {
			ErrorSimulator errorSimulator = new ErrorSimulator(Constants.ModeType.VERBOSE);
			errorSimulator.start();
			System.out.println("[ErrorSimulator] ~ Started ErrorSimulator \n");
		} catch (UnknownHostException e) {
			System.out.println("[host] ~ Error occured while starting host: " + e.getMessage());
		}	
	}
}
