import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Team 05 (Sirak Berhane, Samuel Baumann, Ruchi Bhatia)
 * @version 5/21/2018 (Iteration #1)
 * 
 * Receives packets from both Error Simulator and Client,
 * creates a new client-server thread connection to handle request.
 * When it is completed the client-server thread connection is 
 * terminated until a new connection is made.  
 */
public class Server extends Thread implements Runnable{
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket receiveSocket, sendSocket;	
	private byte dataRecieved[] = new byte[512];
	private Constants.ModeType mode;
	private Print printable;

	/**
	 * Server constructor
	 * 
	 * @param mode type of console output mode (i.e. Verbose or Quiet)
	 * @throws UnknownHostException to indicate that the IP address of
	 * the server could not be determined.
	 */
	public Server(Constants.ModeType mode) throws UnknownHostException {
		this.mode = mode;
		printable = new Print(this.mode);

		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(69, InetAddress.getLocalHost());
		} catch (SocketException e){
			print("Server: Socket Error Occured --> " + e.getMessage());
		}
	}

	/**
	 * Server thread runnable
	 */
	public void run() {
		try {
			createSecondaryServerInstance();
		} catch (Exception e) {
			print("Server: Thread Error Occured -> " + e.getStackTrace().toString());
		}
	}

	/**
	 * Creates a new client-server connection if no errors occurs
	 * 
	 * @throws IOException if input is invalid format
	 */
	public void createSecondaryServerInstance() throws IOException {
		receivePacket = new DatagramPacket(dataRecieved, dataRecieved.length);
		sendPacket = receivePacket;

		for(;;) {
			receiveSocket.receive(receivePacket);
			printable.PrintReceivedPackets(Constants.ServerType.SERVER, Constants.ServerType.ERROR_SIMULATOR, receivePacket.getAddress(),
					receivePacket.getPort(), receivePacket.getLength(), receivePacket.getData());

			//			String msg = new String(receivePacket.getData());
			//			if (msg.equals("CloseServerThreads")) {
			//				print("[Server]: Closing all sockets and thread instances ...");
			//				receiveSocket.close();
			//				Thread.currentThread().interrupt();
			//				print("[Server]: Server thread instance closed.");
			//				break;
			//			}

			// Create a new temporary socket for Secondary Server
			DatagramSocket tempSocket = new DatagramSocket();
			sendPacket.setPort(tempSocket.getLocalPort());

			// Create a new temporary Secondary Server Thread with the inherited console output mode and start it 
			ServerConnectionHandler serverConnectionHandler = new ServerConnectionHandler(mode, tempSocket);
			print("[Server]: Created a new secondary server instance at port --> " + tempSocket.getLocalPort());
			serverConnectionHandler.start();

			// Send the new packet to Secondary Server 
			printable.PrintSendingPackets(Constants.ServerType.SERVER, Constants.ServerType.SERVER_CONNECTION_HANDLER,
					sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), sendPacket.getData());
			sendSocket.send(sendPacket);
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
			Server server = new Server(Constants.ModeType.VERBOSE);
			server.start();
			System.out.println("[Main Server] ~ Started Main Server");
		} catch (UnknownHostException e) {
			System.out.println("[Main Server] ~ Error occured while starting main server: " + e.getMessage());
		}
	}
}
