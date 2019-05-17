import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Group 5
 * @version 5/21/2018 (Iteration #1)
 * 
 * Main Server-side Algorithm
 */
public class Server extends Thread implements Runnable{
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket receiveSocket, sendSocket;	
	private byte dataRecieved[] = new byte[512];
	private Constants.ModeType mode;
	private Print printable;
	
	/**
	 * 
	 * @param mode
	 * @throws UnknownHostException
	 */
	public Server(Constants.ModeType mode) throws UnknownHostException {
		this.mode = mode;
		printable = new Print(this.mode);
		
		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(69, InetAddress.getLocalHost());
		} catch (SocketException e){
			print("MAIN SERVER ERROR OCCURED: " + e.getMessage());
		}
	}
	
	/**
	 * 
	 */
	public void run() {
		try {
			createSecondaryServerInstance();
		} catch (Exception e) {
			print("Main Server: Thread Error Occured -> " + e.getStackTrace().toString());
		}
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void createSecondaryServerInstance() throws IOException {
		receivePacket = new DatagramPacket(dataRecieved, dataRecieved.length);
		sendPacket = receivePacket;
		
		for(;;) {
			receiveSocket.receive(receivePacket);
			printable.PrintReceivedPackets(Constants.ServerType.MAIN_SERVER, Constants.ServerType.HOST, receivePacket.getAddress(),
					receivePacket.getPort(), receivePacket.getLength(), receivePacket.getData());
			
			String msg = new String(receivePacket.getData());
			if (msg.equals("CloseServerThreads")) {
				print("[Main Server]: Closing all sockets and thread instances ...");
				receiveSocket.close();
				Thread.currentThread().interrupt();
				print("[Main Server]: Server thread instance closed.");
				break;
			}
			
			// Create a new temporary socket for Secondary Server
			DatagramSocket tempSocket = new DatagramSocket();
			sendPacket.setPort(tempSocket.getLocalPort());
			
			// Create a new temporary Secondary Server Thread with the inherited console output mode and start it 
			ServerConnectionHandler serverConnectionHandler = new ServerConnectionHandler(mode, tempSocket);
			print("[Main Server]: Created a new secondary server instance at port --> " + tempSocket.getLocalPort());
			serverConnectionHandler.start();
			
			// Send the new packet to Secondary Server 
			printable.PrintSendingPackets(Constants.ServerType.MAIN_SERVER, Constants.ServerType.SECONDARY_SERVER,
					sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength(), sendPacket.getData());
			sendSocket.send(sendPacket);
		}
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
