import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;

/**
 * @author Group 5
 * @version 5/21/2018 (Iteration #1)
 * 
 * Client to Server connection handler, any file transfer to and from 
 * clients is handled by this thread until file transfer is complete 
 * then it will terminate. 
 */
public class ServerConnectionHandler extends Thread implements Runnable{
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket receiveSocket, sendSocket;
	private byte dataRecieved[] = new byte[512];
	private Constants.ModeType mode;
	private Print printable;
	private static File file;
	private static boolean errorFound = false;
	private static byte blockNum = 0x00;

	/**
	 * 
	 * @param mode
	 * @param receiveSocket
	 */
	public ServerConnectionHandler(Constants.ModeType mode, DatagramSocket receiveSocket) {
		this.mode = mode;
		this.receiveSocket = receiveSocket;
		printable = new Print(this.mode);

		try {		
			sendSocket = new DatagramSocket();
		} catch (SocketException e){
			System.out.print("[Client] Stack trace information --> ");
			e.printStackTrace();
			System.exit(1);
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
		print("[Secondary Server]: Waiting for packets to arrive. \n");
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

		// File process here --> This only currently works for test.txt file
		file = find(new File("Server"), new String ("test.txt"));

		if (errorFound == true) {
			throw new Exception("FileNotFoundException");
		}

		byte[] packetResponse = new byte[Files.readAllBytes(file.toPath()).length + 4];
		if(dataRecieved[1] == Constants.PacketByte.RRQ.getPacketByteType()) {
			packetResponse[0] = 0;
			packetResponse[1] = 3;
			packetResponse[2] = blockNum;
			packetResponse[3] = blockNum;
			try {
				System.arraycopy(Files.readAllBytes(file.toPath()), 0, packetResponse, 4, Files.readAllBytes(file.toPath()).length);
			} catch (Exception e) {
				e.printStackTrace();
			}
			blockNum++;
		} else if(dataRecieved[1] == Constants.PacketByte.WRQ.getPacketByteType()) {
			packetResponse[0] = 0;
			packetResponse[1] = 4;
			packetResponse[2] = blockNum;
			packetResponse[3] = blockNum;
			blockNum++;
		} else {
			throw new Exception("InvalidPacketFormatException");
		}

		// Send to client 
		sendPacket = new DatagramPacket(packetResponse, packetResponse.length, address, 42);
		printable.PrintSendingPackets(Constants.ServerType.SECONDARY_SERVER, Constants.ServerType.CLIENT, sendPacket.getAddress(),
				sendPacket.getPort(), sendPacket.getLength(), sendPacket.getData());

		try {
			sendSocket.send(sendPacket);
			print("Secondary Server: Closing thread instance @(PORT " + port + ")");
			sendSocket.close();
			ServerConnectionHandler.currentThread().interrupt();
		} catch (Exception e) {
			print("Secondary Server: Error occured while sending packet ==> Stack Trace "  + e.getStackTrace());
			System.exit(1);
		}
	}

	/**
	 * Recursive file finder.
	 * 
	 * @param root the default root folder that the file is located in
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
			print("\nServer File ERROR: " + "[" + e.getMessage() + "]" + " Invalid directory was given or file doesn't exist.");
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
